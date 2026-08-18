package com.example.network

import com.example.models.*
import kotlinx.coroutines.flow.Flow

interface ComputeApi {
    val marketDataApi: MarketDataApi
    val forwardCurveApi: ForwardCurveApi
    val optionsApi: OptionsApi
    val strikePriceApi: StrikePriceApi
    val capacityApi: CapacityApi
    val datacenterApi: DatacenterApi
    val forecastApi: ForecastApi
    val simulationApi: SimulationApi
}

interface MarketDataApi {
    suspend fun getMarketOverview(): List<GpuMarketInstrument>
    suspend fun getGpuDetail(chip: GpuChip): GpuMarketInstrument
    suspend fun getPriceHistory(chip: GpuChip, timeframeHours: Int): List<PriceTick>
    fun subscribeMarketTicks(): Flow<Map<GpuChip, Double>>
}

interface ForwardCurveApi {
    suspend fun getForwardCurve(chip: GpuChip): ForwardCurveData
    suspend fun getAllForwardCurves(): List<ForwardCurveData>
    fun subscribeForwardCurveUpdates(): Flow<ForwardCurveData>
}

interface OptionsApi {
    suspend fun getOptionChain(chip: GpuChip, region: MarketRegion): List<StrikeOptionContract>
    suspend fun getVolatilitySurface(chip: GpuChip): VolatilitySurfaceData
}

interface StrikePriceApi {
    suspend fun calculateStrikeContract(
        chip: GpuChip,
        region: MarketRegion,
        optionType: OptionType,
        strikePrice: Double,
        maturityDays: Int,
        contractSizeGpuHours: Int
    ): StrikeOptionContract
}

interface CapacityApi {
    suspend fun getGlobalCapacity(): GlobalCapacityOverview
    suspend fun getRegionalCapacity(region: MarketRegion): RegionalCapacity
    fun subscribeCapacityLiveUpdates(): Flow<GlobalCapacityOverview>
}

interface DatacenterApi {
    suspend fun getDatacenters(): List<DatacenterNode>
    suspend fun getDatacenterDetails(id: String): DatacenterNode?
}

interface ForecastApi {
    suspend fun getCostBreakdown(chip: GpuChip): LevelizedCostOfCompute
    suspend fun getSupplyDemandOverview(region: MarketRegion): SupplyDemandOverview
    suspend fun getRiskDashboard(): ComputeRiskDashboard
}

interface SimulationApi {
    suspend fun runScenarioSimulation(input: SimulationInput): SimulationResult
}
