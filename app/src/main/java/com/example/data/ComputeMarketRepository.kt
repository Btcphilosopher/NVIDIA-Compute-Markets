package com.example.data

import com.example.models.*
import com.example.network.*
import com.example.utils.FinancialMath
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*
import kotlin.random.Random
import java.util.Random as JavaRandom

class ComputeMarketRepository(
    private var apiBaseUrl: String = "https://api.nvidia-compute-markets.org/v1",
    private var authToken: String = "Bearer nv_quant_inst_live_88a91c",
    private var wsEndpointUrl: String = "wss://stream.nvidia-compute-markets.org/feed"
) : ComputeApi, MarketDataApi, ForwardCurveApi, OptionsApi, StrikePriceApi, CapacityApi, DatacenterApi, ForecastApi, SimulationApi {

    override val marketDataApi: MarketDataApi get() = this
    override val forwardCurveApi: ForwardCurveApi get() = this
    override val optionsApi: OptionsApi get() = this
    override val strikePriceApi: StrikePriceApi get() = this
    override val capacityApi: CapacityApi get() = this
    override val datacenterApi: DatacenterApi get() = this
    override val forecastApi: ForecastApi get() = this
    override val simulationApi: SimulationApi get() = this

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val javaRandomInstance = JavaRandom()

    // Internal state cache for realistic market simulation
    private val currentPrices = mutableMapOf(
        GpuChip.H100 to 2.84,
        GpuChip.H200 to 3.12,
        GpuChip.B200 to 3.84,
        GpuChip.B300 to 4.12,
        GpuChip.GB200 to 7.45,
        GpuChip.GB300 to 8.90
    )

    private val baselinePrices = mapOf(
        GpuChip.H100 to 2.75,
        GpuChip.H200 to 3.06,
        GpuChip.B200 to 3.87,
        GpuChip.B300 to 4.02,
        GpuChip.GB200 to 7.20,
        GpuChip.GB300 to 8.65
    )

    private val _tickFlow = MutableSharedFlow<Map<GpuChip, Double>>(replay = 1)
    private val _capacityFlow = MutableSharedFlow<GlobalCapacityOverview>(replay = 1)
    private val _forwardCurveFlow = MutableSharedFlow<ForwardCurveData>(replay = 1)

    var isWebSocketConnected: Boolean = true
        private set

    var webSocketLatencyMs: Long = 18L
        private set

    var restLatencyMs: Long = 42L
        private set

    init {
        // High frequency stochastic live tick generator
        scope.launch {
            while (isActive) {
                delay(1200)
                if (isWebSocketConnected) {
                    // Update prices with geometric Brownian motion
                    GpuChip.entries.forEach { chip ->
                        val current = currentPrices[chip] ?: 3.0
                        val drift = 0.0001
                        val vol = when (chip) {
                            GpuChip.B200, GpuChip.B300 -> 0.003
                            GpuChip.GB200, GpuChip.GB300 -> 0.004
                            else -> 0.002
                        }
                        val shock = (javaRandomInstance.nextGaussian()) * vol + drift
                        val newPrice = max(0.5, (current * (1.0 + shock) * 100.0).roundToInt() / 100.0)
                        currentPrices[chip] = newPrice
                    }
                    _tickFlow.emit(currentPrices.toMap())
                    webSocketLatencyMs = (14 + Random.nextInt(12)).toLong()
                }
            }
        }
    }

    fun setConnectionParams(baseUrl: String, token: String, wsUrl: String) {
        this.apiBaseUrl = baseUrl
        this.authToken = token
        this.wsEndpointUrl = wsUrl
    }

    fun toggleWebSocketConnection(connected: Boolean) {
        this.isWebSocketConnected = connected
    }

    // -------------------------------------------------------------
    // MARKET DATA API IMPLEMENTATION
    // -------------------------------------------------------------
    override suspend fun getMarketOverview(): List<GpuMarketInstrument> = withContext(Dispatchers.Default) {
        GpuChip.entries.map { chip ->
            val spot = currentPrices[chip] ?: 3.00
            val base = baselinePrices[chip] ?: (spot * 0.98)
            val change = spot - base
            val changePct = (change / base) * 100.0

            val fwd1M = spot * when(chip) {
                GpuChip.H100 -> 0.954
                GpuChip.H200 -> 0.965
                GpuChip.B200 -> 0.966
                GpuChip.B300 -> 0.970
                GpuChip.GB200 -> 0.975
                GpuChip.GB300 -> 0.980
            }
            val fwd3M = spot * when(chip) {
                GpuChip.H100 -> 0.926
                GpuChip.H200 -> 0.936
                GpuChip.B200 -> 0.906
                GpuChip.B300 -> 0.920
                GpuChip.GB200 -> 0.940
                GpuChip.GB300 -> 0.950
            }
            val fwd6M = fwd3M * 0.94
            val fwd12M = spot * when(chip) {
                GpuChip.H100 -> 0.813
                GpuChip.H200 -> 0.846
                GpuChip.B200 -> 0.812
                GpuChip.B300 -> 0.830
                GpuChip.GB200 -> 0.860
                GpuChip.GB300 -> 0.875
            }
            val fwd24M = fwd12M * 0.85
            val fwd36M = fwd24M * 0.88

            val (totCap, avail, util) = when(chip) {
                GpuChip.H100 -> Triple(1850000, 159000, 91.4)
                GpuChip.H200 -> Triple(920000, 71760, 92.2)
                GpuChip.B200 -> Triple(1240000, 163680, 86.8)
                GpuChip.B300 -> Triple(410000, 43050, 89.5)
                GpuChip.GB200 -> Triple(280000, 19600, 93.0)
                GpuChip.GB300 -> Triple(120000, 6000, 95.0)
            }

            GpuMarketInstrument(
                chip = chip,
                spotPrice = spot,
                forward1M = (fwd1M * 100.0).roundToInt() / 100.0,
                forward3M = (fwd3M * 100.0).roundToInt() / 100.0,
                forward6M = (fwd6M * 100.0).roundToInt() / 100.0,
                forward12M = (fwd12M * 100.0).roundToInt() / 100.0,
                forward24M = (fwd24M * 100.0).roundToInt() / 100.0,
                forward36M = (fwd36M * 100.0).roundToInt() / 100.0,
                priceChange24h = (change * 100.0).roundToInt() / 100.0,
                priceChangePercent24h = (changePct * 10.0).roundToInt() / 10.0,
                impliedVol = when(chip) {
                    GpuChip.H100 -> 34.8
                    GpuChip.H200 -> 38.2
                    GpuChip.B200 -> 44.5
                    GpuChip.B300 -> 48.0
                    GpuChip.GB200 -> 52.3
                    GpuChip.GB300 -> 56.1
                },
                historicalVol = 29.4,
                utilizationRate = util,
                totalCapacityGpus = totCap,
                availableCapacityGpus = avail,
                activeRunningGpus = totCap - avail,
                queueWaitTimeHours = when(chip) {
                    GpuChip.H100 -> 1.4
                    GpuChip.H200 -> 2.1
                    GpuChip.B200 -> 3.8
                    GpuChip.B300 -> 6.2
                    GpuChip.GB200 -> 12.5
                    GpuChip.GB300 -> 24.0
                },
                demandIndex = 142.5,
                supplyIndex = 118.0,
                averagePowerCostKwh = 0.072,
                timestamp = System.currentTimeMillis(),
                marketStatus = if (util > 93.0) MarketStatus.CAPACITY_CONSTRAINED else MarketStatus.ACTIVE
            )
        }
    }

    override suspend fun getGpuDetail(chip: GpuChip): GpuMarketInstrument = withContext(Dispatchers.Default) {
        getMarketOverview().first { it.chip == chip }
    }

    override suspend fun getPriceHistory(chip: GpuChip, timeframeHours: Int): List<PriceTick> = withContext(Dispatchers.Default) {
        val current = currentPrices[chip] ?: 3.0
        val points = min(60, max(24, timeframeHours))
        val list = mutableListOf<PriceTick>()
        var p = current * (1.0 - (Random.nextDouble() * 0.08 - 0.04))
        val now = System.currentTimeMillis()
        val stepMs = (timeframeHours * 3600L * 1000L) / points

        for (i in points downTo 0) {
            val t = now - (i * stepMs)
            val delta = (Random.nextDouble() - 0.48) * (p * 0.015)
            p = max(0.5, p + delta)
            val high = p + Random.nextDouble() * (p * 0.008)
            val low = p - Random.nextDouble() * (p * 0.008)
            val vol = (50000 + Random.nextInt(150000)).toLong()
            list.add(
                PriceTick(
                    timestamp = t,
                    price = (p * 100.0).roundToInt() / 100.0,
                    volumeGpuHours = vol,
                    open = p,
                    high = high,
                    low = low,
                    close = p
                )
            )
        }
        list
    }

    override fun subscribeMarketTicks(): Flow<Map<GpuChip, Double>> = _tickFlow.asSharedFlow()

    // -------------------------------------------------------------
    // FORWARD CURVE API IMPLEMENTATION
    // -------------------------------------------------------------
    override suspend fun getForwardCurve(chip: GpuChip): ForwardCurveData = withContext(Dispatchers.Default) {
        val spot = currentPrices[chip] ?: 3.0
        val maturities = listOf(
            Triple("SPOT", "即期 (Spot)", 0.0),
            Triple("1M", "1個月 (1M)", 1.0),
            Triple("3M", "3個月 (3M)", 3.0),
            Triple("6M", "6個月 (6M)", 6.0),
            Triple("9M", "9個月 (9M)", 9.0),
            Triple("12M", "12個月 (1Y)", 12.0),
            Triple("24M", "2年 (2Y)", 24.0),
            Triple("36M", "3年 (3Y)", 36.0)
        )

        val decayRates = when (chip) {
            GpuChip.H100 -> listOf(1.0, 0.954, 0.926, 0.880, 0.840, 0.813, 0.690, 0.605)
            GpuChip.H200 -> listOf(1.0, 0.965, 0.936, 0.895, 0.865, 0.846, 0.720, 0.635)
            GpuChip.B200 -> listOf(1.0, 0.966, 0.906, 0.862, 0.835, 0.812, 0.710, 0.620)
            GpuChip.B300 -> listOf(1.0, 0.970, 0.920, 0.880, 0.850, 0.830, 0.730, 0.640)
            GpuChip.GB200 -> listOf(1.0, 0.975, 0.940, 0.905, 0.880, 0.860, 0.760, 0.670)
            GpuChip.GB300 -> listOf(1.0, 0.980, 0.950, 0.920, 0.895, 0.875, 0.780, 0.690)
        }

        val points = maturities.mapIndexed { idx, item ->
            val fwdPrice = (spot * decayRates[idx] * 100.0).roundToInt() / 100.0
            val delta = fwdPrice - spot
            val annualizedBasis = if (item.third > 0) (delta / spot) * (12.0 / item.third) * 100.0 else 0.0
            ForwardCurvePoint(
                maturityCode = item.first,
                maturityLabel = item.second,
                monthsAhead = item.third,
                price = fwdPrice,
                spotDelta = (delta * 100.0).roundToInt() / 100.0,
                basisSpreadAnnualized = (annualizedBasis * 10.0).roundToInt() / 10.0,
                impliedRepoRate = 4.8
            )
        }

        ForwardCurveData(
            chip = chip,
            spotPrice = spot,
            points = points,
            structure = CurveStructure.BACKWARDATION, // Compute forward curves exhibit backwardation due to hardware depreciation and future chip releases
            curveSlope = -18.7, // %/year
            timestamp = System.currentTimeMillis()
        )
    }

    override suspend fun getAllForwardCurves(): List<ForwardCurveData> = withContext(Dispatchers.Default) {
        GpuChip.entries.map { getForwardCurve(it) }
    }

    override fun subscribeForwardCurveUpdates(): Flow<ForwardCurveData> = _forwardCurveFlow.asSharedFlow()

    // -------------------------------------------------------------
    // OPTIONS & STRIKE PRICE API IMPLEMENTATION
    // -------------------------------------------------------------
    override suspend fun getOptionChain(chip: GpuChip, region: MarketRegion): List<StrikeOptionContract> = withContext(Dispatchers.Default) {
        val spot = currentPrices[chip] ?: 3.0
        val strikes = listOf(spot * 0.8, spot * 0.9, spot * 0.95, spot, spot * 1.05, spot * 1.1, spot * 1.2)
        val maturities = listOf(30, 90, 180, 365)

        strikes.flatMap { k ->
            maturities.map { days ->
                calculateStrikeContract(
                    chip = chip,
                    region = region,
                    optionType = OptionType.CALL_OPTION,
                    strikePrice = (k * 100.0).roundToInt() / 100.0,
                    maturityDays = days,
                    contractSizeGpuHours = 10000
                )
            }
        }
    }

    override suspend fun getVolatilitySurface(chip: GpuChip): VolatilitySurfaceData = withContext(Dispatchers.Default) {
        val spot = currentPrices[chip] ?: 3.0
        val strikeSteps = listOf(0.7, 0.8, 0.9, 0.95, 1.0, 1.05, 1.1, 1.2, 1.3)
        val strikes = strikeSteps.map { (spot * it * 100.0).roundToInt() / 100.0 }
        val maturitiesMonths = listOf(1.0, 2.0, 3.0, 6.0, 9.0, 12.0, 24.0)

        val atmVolBase = when (chip) {
            GpuChip.H100 -> 34.8
            GpuChip.H200 -> 38.2
            GpuChip.B200 -> 44.5
            GpuChip.B300 -> 48.0
            GpuChip.GB200 -> 52.3
            GpuChip.GB300 -> 56.1
        }

        val points = mutableListOf<VolSurfacePoint>()
        val grid = maturitiesMonths.map { m ->
            strikes.map { k ->
                val moneyness = k / spot
                // Volatility smile & term structure
                val smile = 4.2 * (moneyness - 1.0).pow(2) - 1.8 * (moneyness - 1.0)
                val termDecay = -0.5 * ln(m + 1.0)
                val iv = max(15.0, (atmVolBase + smile + termDecay * 2.0))
                val roundedIv = (iv * 10.0).roundToInt() / 10.0

                points.add(
                    VolSurfacePoint(
                        strikePrice = k,
                        maturityMonths = m,
                        impliedVol = roundedIv,
                        moneyness = (moneyness * 100.0).roundToInt() / 100.0
                    )
                )
                roundedIv
            }
        }

        VolatilitySurfaceData(
            chip = chip,
            spotPrice = spot,
            strikes = strikes,
            maturitiesMonths = maturitiesMonths,
            grid = grid,
            points = points,
            atmVol = atmVolBase,
            skewSlope = -0.32
        )
    }

    override suspend fun calculateStrikeContract(
        chip: GpuChip,
        region: MarketRegion,
        optionType: OptionType,
        strikePrice: Double,
        maturityDays: Int,
        contractSizeGpuHours: Int
    ): StrikeOptionContract = withContext(Dispatchers.Default) {
        val spot = currentPrices[chip] ?: 3.0
        val tYears = maturityDays / 365.0
        val baseIv = when (chip) {
            GpuChip.H100 -> 0.348
            GpuChip.H200 -> 0.382
            GpuChip.B200 -> 0.445
            GpuChip.B300 -> 0.480
            GpuChip.GB200 -> 0.523
            GpuChip.GB300 -> 0.561
        }

        // Apply skew
        val moneyness = strikePrice / spot
        val iv = max(0.15, baseIv + 0.12 * (moneyness - 1.0).pow(2) - 0.05 * (moneyness - 1.0))
        val r = 0.045 // 4.5% risk free rate

        val (estPricePerHr, greeks) = FinancialMath.calculateBlackOption(
            type = optionType,
            F = spot,
            K = strikePrice,
            T = tYears,
            r = r,
            sigma = iv
        )

        val totalPrem = estPricePerHr * contractSizeGpuHours
        val breakeven = if (optionType == OptionType.CALL_OPTION) strikePrice + estPricePerHr else strikePrice - estPricePerHr
        val prob = FinancialMath.calculateProbabilityOfProfit(optionType, spot, breakeven, tYears, iv)

        val maturityLabel = when {
            maturityDays <= 30 -> "1個月 (30D)"
            maturityDays <= 90 -> "3個月 (90D)"
            maturityDays <= 180 -> "6個月 (180D)"
            maturityDays <= 365 -> "1年 (365D)"
            else -> "${maturityDays}天"
        }

        StrikeOptionContract(
            chip = chip,
            region = region,
            optionType = optionType,
            strikePrice = strikePrice,
            currentSpotPrice = spot,
            maturityDays = maturityDays,
            maturityLabel = maturityLabel,
            contractSizeGpuHours = contractSizeGpuHours,
            impliedVolatility = (iv * 1000.0).roundToInt() / 10.0,
            estimatedPricePerGpuHour = (estPricePerHr * 1000.0).roundToInt() / 1000.0,
            totalPremium = (totalPrem * 10.0).roundToInt() / 10.0,
            probabilityOfProfit = (prob * 10.0).roundToInt() / 10.0,
            breakevenPrice = (breakeven * 100.0).roundToInt() / 100.0,
            greeks = greeks
        )
    }

    // -------------------------------------------------------------
    // CAPACITY & DATACENTER API IMPLEMENTATION
    // -------------------------------------------------------------
    override suspend fun getGlobalCapacity(): GlobalCapacityOverview = withContext(Dispatchers.Default) {
        val total = 4820000
        val active = 4210000
        val avail = total - active
        val util = (active.toDouble() / total.toDouble()) * 100.0

        val regions = listOf(
            RegionalCapacity(MarketRegion.NORTH_AMERICA, 2400000, 2112000, 288000, 88.0, 2.92, 1800.0, GpuChip.B200),
            RegionalCapacity(MarketRegion.EUROPE, 950000, 817000, 133000, 86.0, 3.10, 720.0, GpuChip.H100),
            RegionalCapacity(MarketRegion.ASIA_PACIFIC, 780000, 694200, 85800, 89.0, 2.85, 600.0, GpuChip.H200),
            RegionalCapacity(MarketRegion.TAIWAN, 340000, 312800, 27200, 92.0, 2.74, 280.0, GpuChip.GB200),
            RegionalCapacity(MarketRegion.CHINA, 350000, 273000, 77000, 78.0, 2.55, 260.0, GpuChip.H100)
        )

        GlobalCapacityOverview(
            totalGpus = total,
            activeRunningGpus = active,
            availableGpus = avail,
            globalUtilization = (util * 10.0).roundToInt() / 10.0,
            averageQueueTimeHours = 3.2,
            totalPowerConsumptionMw = 3660.0,
            regions = regions
        )
    }

    override suspend fun getRegionalCapacity(region: MarketRegion): RegionalCapacity = withContext(Dispatchers.Default) {
        getGlobalCapacity().regions.firstOrNull { it.region == region } ?: getGlobalCapacity().regions.first()
    }

    override fun subscribeCapacityLiveUpdates(): Flow<GlobalCapacityOverview> = _capacityFlow.asSharedFlow()

    override suspend fun getDatacenters(): List<DatacenterNode> = withContext(Dispatchers.Default) {
        listOf(
            DatacenterNode(
                id = "DC-TW-HSINCHU-01",
                name = "TSMC / NVD Hsinchu Science Park Compute Vault",
                location = "Hsinchu, Taiwan (新竹科學園區)",
                region = MarketRegion.TAIWAN,
                latitude = 24.78,
                longitude = 120.99,
                totalGpus = 160000,
                activeGpus = 151200,
                availableGpus = 8800,
                utilizationRate = 94.5,
                totalPowerMw = 140.0,
                currentPowerDrawMw = 132.3,
                dominantGpu = GpuChip.GB200,
                spotPricePerHour = 2.74,
                pue = 1.12,
                networkLatencyMs = 1.8,
                coolingTech = "Direct Liquid Cooling (DLC)",
                powerSource = "High-Reliability Nuclear/Substation",
                queueWaitHours = 1.2
            ),
            DatacenterNode(
                id = "DC-TW-TAIPEI-02",
                name = "Taipei Neihu HyperScale Center",
                location = "Taipei, Taiwan (臺北內湖雲端中心)",
                region = MarketRegion.TAIWAN,
                latitude = 25.08,
                longitude = 121.57,
                totalGpus = 180000,
                activeGpus = 161600,
                availableGpus = 18400,
                utilizationRate = 89.8,
                totalPowerMw = 140.0,
                currentPowerDrawMw = 125.7,
                dominantGpu = GpuChip.B200,
                spotPricePerHour = 2.80,
                pue = 1.15,
                networkLatencyMs = 2.1,
                coolingTech = "Closed-Loop Liquid to Air",
                powerSource = "Taipower 161kV Dedicated Feed",
                queueWaitHours = 2.0
            ),
            DatacenterNode(
                id = "DC-US-EAST-VA",
                name = "Ashburn Data Center Alley AI Hub",
                location = "Ashburn, Virginia, USA",
                region = MarketRegion.NORTH_AMERICA,
                latitude = 39.04,
                longitude = -77.48,
                totalGpus = 1100000,
                activeGpus = 980000,
                availableGpus = 120000,
                utilizationRate = 89.1,
                totalPowerMw = 850.0,
                currentPowerDrawMw = 757.0,
                dominantGpu = GpuChip.B200,
                spotPricePerHour = 2.88,
                pue = 1.18,
                networkLatencyMs = 4.2,
                coolingTech = "Direct-to-Chip Liquid Cooling",
                powerSource = "Dominion Energy 500kV",
                queueWaitHours = 3.5
            ),
            DatacenterNode(
                id = "DC-US-WEST-OR",
                name = "Columbia Hydro Compute Campus",
                location = "The Dalles, Oregon, USA",
                region = MarketRegion.NORTH_AMERICA,
                latitude = 45.59,
                longitude = -121.17,
                totalGpus = 800000,
                activeGpus = 712000,
                availableGpus = 88000,
                utilizationRate = 89.0,
                totalPowerMw = 600.0,
                currentPowerDrawMw = 534.0,
                dominantGpu = GpuChip.H100,
                spotPricePerHour = 2.68,
                pue = 1.11,
                networkLatencyMs = 6.8,
                coolingTech = "Hydro-River Evaporative + DLC",
                powerSource = "100% Hydroelectric Clean",
                queueWaitHours = 1.8
            ),
            DatacenterNode(
                id = "DC-EU-FRANKFURT",
                name = "Frankfurt Main Exchange Compute Gateway",
                location = "Frankfurt am Main, Germany",
                region = MarketRegion.EUROPE,
                latitude = 50.11,
                longitude = 8.68,
                totalGpus = 520000,
                activeGpus = 452400,
                availableGpus = 67600,
                utilizationRate = 87.0,
                totalPowerMw = 410.0,
                currentPowerDrawMw = 356.7,
                dominantGpu = GpuChip.H200,
                spotPricePerHour = 3.15,
                pue = 1.19,
                networkLatencyMs = 3.1,
                coolingTech = "Dry Coolers + Heat Export",
                powerSource = "German Grid + Wind PPA",
                queueWaitHours = 4.2
            ),
            DatacenterNode(
                id = "DC-APAC-TOKYO",
                name = "Tokyo Otemachi Financial AI Fabric",
                location = "Tokyo, Japan",
                region = MarketRegion.ASIA_PACIFIC,
                latitude = 35.68,
                longitude = 139.76,
                totalGpus = 460000,
                activeGpus = 418600,
                availableGpus = 41400,
                utilizationRate = 91.0,
                totalPowerMw = 350.0,
                currentPowerDrawMw = 318.5,
                dominantGpu = GpuChip.B300,
                spotPricePerHour = 3.02,
                pue = 1.16,
                networkLatencyMs = 1.9,
                coolingTech = "Immersion Liquid Cool",
                powerSource = "TEPCO High Voltage Dual Feed",
                queueWaitHours = 2.8
            ),
            DatacenterNode(
                id = "DC-APAC-SINGAPORE",
                name = "Singapore Jurong Tropical Superhub",
                location = "Jurong, Singapore",
                region = MarketRegion.ASIA_PACIFIC,
                latitude = 1.33,
                longitude = 103.74,
                totalGpus = 320000,
                activeGpus = 275600,
                availableGpus = 44400,
                utilizationRate = 86.1,
                totalPowerMw = 250.0,
                currentPowerDrawMw = 215.2,
                dominantGpu = GpuChip.H100,
                spotPricePerHour = 2.95,
                pue = 1.25,
                networkLatencyMs = 2.4,
                coolingTech = "Seawater Heat Exchange + DLC",
                powerSource = "LNG Cogeneration",
                queueWaitHours = 3.0
            )
        )
    }

    override suspend fun getDatacenterDetails(id: String): DatacenterNode? = withContext(Dispatchers.Default) {
        getDatacenters().firstOrNull { it.id == id }
    }

    // -------------------------------------------------------------
    // FORECAST, ECONOMICS & RISK API IMPLEMENTATION
    // -------------------------------------------------------------
    override suspend fun getCostBreakdown(chip: GpuChip): LevelizedCostOfCompute = withContext(Dispatchers.Default) {
        val spot = currentPrices[chip] ?: 3.0
        val costVals = when(chip) {
            GpuChip.H100 -> doubleArrayOf(1.18, 0.28, 0.08, 0.12, 0.10, 0.07, 0.09) // total 1.92
            GpuChip.H200 -> doubleArrayOf(1.30, 0.29, 0.09, 0.13, 0.11, 0.08, 0.10) // total 2.10
            GpuChip.B200 -> doubleArrayOf(1.48, 0.38, 0.11, 0.14, 0.13, 0.09, 0.11) // total 2.44
            GpuChip.B300 -> doubleArrayOf(1.62, 0.42, 0.12, 0.15, 0.14, 0.10, 0.12) // total 2.67
            GpuChip.GB200 -> doubleArrayOf(2.75, 0.82, 0.24, 0.28, 0.32, 0.18, 0.21) // total 4.80
            GpuChip.GB300 -> doubleArrayOf(3.20, 0.95, 0.28, 0.32, 0.38, 0.22, 0.25) // total 5.60
        }
        val capex = costVals[0]
        val power = costVals[1]
        val cooling = costVals[2]
        val colo = costVals[3]
        val network = costVals[4]
        val maint = costVals[5]
        val ops = costVals[6]

        val totalCost = capex + power + cooling + colo + network + maint + ops
        val margin = ((spot - totalCost) / spot) * 100.0

        val components = listOf(
            ComputeCostComponent("capex", "Hardware Capex Amortization", "硬體設備折舊攤提", "GPU 硬件成本折旧", capex, (capex/totalCost)*100.0, 0xFF76B900),
            ComputeCostComponent("power", "Electricity & Power Draw", "電力與發電成本", "电力与能耗", power, (power/totalCost)*100.0, 0xFF00D2FF),
            ComputeCostComponent("cooling", "Liquid & Facility Cooling", "水冷散熱與空調", "冷却与热管理", cooling, (cooling/totalCost)*100.0, 0xFF3B82F6),
            ComputeCostComponent("colo", "Datacenter Shell & Colocation", "機房場地租賃與土建", "数据中心与机架", colo, (colo/totalCost)*100.0, 0xFFF59E0B),
            ComputeCostComponent("network", "InfiniBand / NVLink Fabric", "高速交換網路與光纖", "网络与光模块", network, (network/totalCost)*100.0, 0xFF8B5CF6),
            ComputeCostComponent("maint", "Maintenance, RMA & Spares", "備品維護與 SLA 儲備", "维护与硬件备件", maint, (maint/totalCost)*100.0, 0xFFEC4899),
            ComputeCostComponent("ops", "Engineering & Operations", "運維工程師人事", "运营与管理", ops, (ops/totalCost)*100.0, 0xFF64748B)
        )

        LevelizedCostOfCompute(
            chip = chip,
            totalRealHourlyCost = (totalCost * 100.0).roundToInt() / 100.0,
            marketSpotPrice = spot,
            grossMarginPercentage = (margin * 10.0).roundToInt() / 10.0,
            electricityKwhPrice = 0.072,
            components = components,
            serverAcquisitionCostUsd = when(chip) {
                GpuChip.H100 -> 300000.0
                GpuChip.H200 -> 340000.0
                GpuChip.B200 -> 420000.0
                GpuChip.B300 -> 480000.0
                GpuChip.GB200 -> 1800000.0
                GpuChip.GB300 -> 2400000.0
            },
            usefulLifeYears = 3.0,
            residualSalvageValueUsd = 45000.0,
            rackPowerDensityKw = when(chip) {
                GpuChip.GB200, GpuChip.GB300 -> 120.0
                else -> 40.0
            }
        )
    }

    override suspend fun getSupplyDemandOverview(region: MarketRegion): SupplyDemandOverview = withContext(Dispatchers.Default) {
        val now = System.currentTimeMillis()
        val quarters = listOf("Q1-25", "Q2-25", "Q3-25", "Q4-25", "Q1-26", "Q2-26", "Q3-26", "Q4-26")
        val demandBase = 3200000.0
        val supplyBase = 2800000.0

        val timeSeries = quarters.mapIndexed { idx, q ->
            val d = demandBase * (1.0 + idx * 0.14)
            val s = supplyBase * (1.0 + idx * 0.12)
            val util = min(98.0, (d / s) * 100.0)
            val deficit = max(0.0, d - s)
            SupplyDemandTimeSeries(
                timestamp = now + (idx * 90L * 86400L * 1000L),
                timeLabel = q,
                demandUnitsGpu = (d / 1000.0).roundToInt().toDouble(),
                supplyCapacityUnitsGpu = (s / 1000.0).roundToInt().toDouble(),
                utilizationPercent = (util * 10.0).roundToInt() / 10.0,
                projectedDeficitGpu = (deficit / 1000.0).roundToInt().toDouble()
            )
        }

        SupplyDemandOverview(
            region = region,
            currentDemandTflopsNorm = 142000000.0,
            currentSupplyTflopsNorm = 126000000.0,
            utilizationPercent = 88.7,
            forecastQuarterlyCapacityGrowthPercent = 12.4,
            projectedShortageNext3MonthsGpus = 210000,
            historyAndForecast = timeSeries
        )
    }

    override suspend fun getRiskDashboard(): ComputeRiskDashboard = withContext(Dispatchers.Default) {
        val factors = listOf(
            RiskFactor(
                category = RiskCategory.PRICE_VOLATILITY,
                title = "Spot Price VaR & Volatility Spike",
                scoreOutOf100 = 62,
                severity = RiskSeverity.MODERATE,
                description = "Rapid frontier model releases and training cluster reallocations drive sharp spot intraday variance.",
                metricValue = "1-Day 95% VaR: -6.4%",
                mitigationStrategy = "Enter 3M forward collar derivative hedge to lock floor compute revenue."
            ),
            RiskFactor(
                category = RiskCategory.CAPACITY_EXHAUSTION,
                title = "Tier-1 Datacenter Capacity Exhaustion",
                scoreOutOf100 = 78,
                severity = RiskSeverity.ELEVATED,
                description = "Hyperscale cluster utilization in Virginia and Hsinchu exceeds 92%, causing queue wait spikes.",
                metricValue = "Utilization: 91.4% (Critical >95%)",
                mitigationStrategy = "Route non-latency sensitive batch fine-tuning workloads to Oregon / Singapore nodes."
            ),
            RiskFactor(
                category = RiskCategory.POWER_GRID_RELIABILITY,
                title = "Power Interconnect & Grid Curtailment",
                scoreOutOf100 = 71,
                severity = RiskSeverity.ELEVATED,
                description = "Regional utility transformers facing interconnection backlogs for 100MW+ high density liquid racks.",
                metricValue = "Curtailment Prob: 14.2%",
                mitigationStrategy = "Deploy on-site BESS (Battery Energy Storage) and dual-substation feeds."
            ),
            RiskFactor(
                category = RiskCategory.SUPPLY_CHAIN_PACKAGING,
                title = "CoWoS Advanced Packaging Supply Lead Time",
                scoreOutOf100 = 55,
                severity = RiskSeverity.MODERATE,
                description = "TSMC CoWoS-L and HBM3e yield curves improving; Blackwell delivery cycle currently at 8-12 weeks.",
                metricValue = "Lead Time: 10.5 Weeks",
                mitigationStrategy = "Maintain 6-month buffer capacity forward reservations."
            ),
            RiskFactor(
                category = RiskCategory.TECH_OBSOLESCENCE,
                title = "Architecture Deprecation & Residual Value",
                scoreOutOf100 = 48,
                severity = RiskSeverity.LOW,
                description = "Hopper H100 retains strong inference value even as Blackwell ramp accelerates.",
                metricValue = "Annual Depreciation: 22.4%",
                mitigationStrategy = "Repurpose older clusters towards quant inference and agentic pipelines."
            ),
            RiskFactor(
                category = RiskCategory.UTILIZATION_DECAY,
                title = "Off-Peak Night Workload Utilization Drop",
                scoreOutOf100 = 36,
                severity = RiskSeverity.LOW,
                description = "Diurnal inference variance smoothed out by global cross-time-zone batch dispatching.",
                metricValue = "Min Diurnal Load: 84.1%",
                mitigationStrategy = "Offer dynamic off-peak spot discounts for spot preemptible jobs."
            )
        )

        ComputeRiskDashboard(
            overallRiskScore = 64,
            valueAtRisk1Day95 = 6.4,
            capacityExhaustionProb = 18.5,
            gridCurtailmentRiskProb = 14.2,
            factors = factors
        )
    }

    // -------------------------------------------------------------
    // SIMULATION API IMPLEMENTATION
    // -------------------------------------------------------------
    override suspend fun runScenarioSimulation(input: SimulationInput): SimulationResult = withContext(Dispatchers.Default) {
        val baseSpot = currentPrices[GpuChip.H100] ?: 2.84
        val base1YFwd = baseSpot * 0.813
        val baseUtil = 91.4
        val baseVol = 34.8
        val baseAvail = 159000

        // Macro economic impact formulas:
        // Price impact = + (AI Demand * 0.85) - (Supply Delta * 0.70) + (Power Delta * 0.25) - (DC Expansion * 0.40) - (Perf boost * 0.35)
        val priceMultiplier = 1.0 + (
            (input.aiDemandGrowthDeltaPercent * 0.0085) -
            (input.gpuSupplyDeltaPercent * 0.0070) +
            (input.electricityPriceDeltaPercent * 0.0025) -
            (input.datacenterCapacityExpansionPercent * 0.0040) -
            (input.nextGenPerformanceBoostPercent * 0.0035)
        )

        val simSpot = max(0.5, (baseSpot * priceMultiplier * 100.0).roundToInt() / 100.0)
        val spotDeltaPct = ((simSpot - baseSpot) / baseSpot) * 100.0

        val fwdMultiplier = 1.0 + (
            (input.aiDemandGrowthDeltaPercent * 0.0065) -
            (input.gpuSupplyDeltaPercent * 0.0085) -
            (input.datacenterCapacityExpansionPercent * 0.0060)
        )
        val sim1YFwd = max(0.4, (base1YFwd * fwdMultiplier * 100.0).roundToInt() / 100.0)
        val fwdDeltaPct = ((sim1YFwd - base1YFwd) / base1YFwd) * 100.0

        val utilShift = (input.aiDemandGrowthDeltaPercent * 0.15) - (input.gpuSupplyDeltaPercent * 0.12) + (input.utilizationShiftPercent * 0.3)
        val simUtil = max(40.0, min(99.9, baseUtil + utilShift))

        val volShift = abs(spotDeltaPct) * 0.45 + (if (simUtil > 95.0) 12.0 else 0.0)
        val simVol = max(15.0, baseVol + volShift)

        val capacityMultiplier = 1.0 + (input.gpuSupplyDeltaPercent * 0.01) - (input.aiDemandGrowthDeltaPercent * 0.008)
        val simAvail = max(5000, (baseAvail * capacityMultiplier).roundToInt())

        val shortageLevel = when {
            simUtil > 96.0 || spotDeltaPct > 25.0 -> "CRITICAL SHORTAGE"
            simUtil > 92.0 || spotDeltaPct > 10.0 -> "ELEVATED CONSTRAINTS"
            simUtil < 75.0 -> "CAPACITY SURPLUS"
            else -> "BALANCED MARKET"
        }

        val maturities = listOf("Spot", "1M", "3M", "6M", "1Y", "2Y", "3Y")
        val baseDecays = listOf(1.0, 0.954, 0.926, 0.880, 0.813, 0.690, 0.605)
        val simDecays = baseDecays.mapIndexed { idx, d ->
            val shock = 1.0 + (spotDeltaPct * 0.01 * (1.0 - idx * 0.12))
            d * shock
        }

        val curvePoints = maturities.mapIndexed { idx, m ->
            SimulatedCurvePoint(
                maturityLabel = m,
                baselinePrice = (baseSpot * baseDecays[idx] * 100.0).roundToInt() / 100.0,
                simulatedPrice = (baseSpot * simDecays[idx] * 100.0).roundToInt() / 100.0
            )
        }

        SimulationResult(
            chip = GpuChip.H100,
            baselineSpotPrice = baseSpot,
            simulatedSpotPrice = simSpot,
            spotPriceChangePercent = (spotDeltaPct * 10.0).roundToInt() / 10.0,
            baseline1YForward = base1YFwd,
            simulated1YForward = sim1YFwd,
            forward1YChangePercent = (fwdDeltaPct * 10.0).roundToInt() / 10.0,
            baselineUtilization = baseUtil,
            simulatedUtilization = (simUtil * 10.0).roundToInt() / 10.0,
            baselineVol = baseVol,
            simulatedVol = (simVol * 10.0).roundToInt() / 10.0,
            baselineCapacityAvailable = baseAvail,
            simulatedCapacityAvailable = simAvail,
            shortageRiskLevel = shortageLevel,
            forwardCurveComparison = curvePoints
        )
    }

    fun getApiHealthState(): ApiHealthState {
        return ApiHealthState(
            restApiLatencyMs = restLatencyMs,
            restApiStatusOk = true,
            webSocketLatencyMs = webSocketLatencyMs,
            webSocketConnected = isWebSocketConnected,
            lastMarketDataSync = System.currentTimeMillis(),
            marketDataFeedLive = isWebSocketConnected,
            capacityFeedLive = isWebSocketConnected,
            forecastServiceLive = true,
            simulationServiceLive = true,
            requestErrorCount = if (isWebSocketConnected) 0 else 3,
            activeEndpointUrl = apiBaseUrl
        )
    }
}
