package com.example.models

enum class OptionType {
    CALL_OPTION,
    PUT_OPTION
}

typealias OptionGreeks = Greeks

data class Greeks(
    val delta: Double,
    val gamma: Double,
    val vega: Double,
    val theta: Double,
    val rho: Double
)

data class StrikeOptionContract(
    val chip: GpuChip,
    val region: MarketRegion,
    val optionType: OptionType,
    val strikePrice: Double,
    val currentSpotPrice: Double,
    val maturityDays: Int,
    val maturityLabel: String,
    val contractSizeGpuHours: Int, // e.g. 10,000 GPU-hours
    val impliedVolatility: Double, // % e.g. 42.5%
    val estimatedPricePerGpuHour: Double,
    val totalPremium: Double,
    val probabilityOfProfit: Double,
    val breakevenPrice: Double,
    val greeks: Greeks
)

data class VolSurfacePoint(
    val strikePrice: Double,
    val maturityMonths: Double,
    val impliedVol: Double,
    val moneyness: Double // Strike / Spot
)

data class VolatilitySurfaceData(
    val chip: GpuChip,
    val spotPrice: Double,
    val strikes: List<Double>,
    val maturitiesMonths: List<Double>,
    val grid: List<List<Double>>, // [maturityIdx][strikeIdx] -> IV
    val points: List<VolSurfacePoint>,
    val atmVol: Double,
    val skewSlope: Double
)
