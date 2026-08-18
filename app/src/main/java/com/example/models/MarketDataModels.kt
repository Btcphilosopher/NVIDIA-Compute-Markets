package com.example.models

data class GpuMarketInstrument(
    val chip: GpuChip,
    val spotPrice: Double,
    val forward1M: Double,
    val forward3M: Double,
    val forward6M: Double,
    val forward12M: Double,
    val forward24M: Double,
    val forward36M: Double,
    val priceChange24h: Double,
    val priceChangePercent24h: Double,
    val impliedVol: Double, // % e.g. 38.4%
    val historicalVol: Double,
    val utilizationRate: Double, // % e.g. 91.4%
    val totalCapacityGpus: Int,
    val availableCapacityGpus: Int,
    val activeRunningGpus: Int,
    val queueWaitTimeHours: Double,
    val demandIndex: Double, // 100 base
    val supplyIndex: Double,
    val averagePowerCostKwh: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val marketStatus: MarketStatus = MarketStatus.ACTIVE
)

enum class MarketStatus {
    ACTIVE,
    HIGH_VOLATILITY,
    CAPACITY_CONSTRAINED,
    POWER_CURTAILED
}

data class PriceTick(
    val timestamp: Long,
    val price: Double,
    val volumeGpuHours: Long,
    val open: Double = price,
    val high: Double = price,
    val low: Double = price,
    val close: Double = price
)

data class ForwardCurvePoint(
    val maturityCode: String,
    val maturityLabel: String,
    val monthsAhead: Double,
    val price: Double,
    val spotDelta: Double,
    val basisSpreadAnnualized: Double,
    val impliedRepoRate: Double
)

enum class CurveStructure {
    CONTANGO,
    BACKWARDATION,
    FLAT
}

data class ForwardCurveData(
    val chip: GpuChip,
    val spotPrice: Double,
    val points: List<ForwardCurvePoint>,
    val structure: CurveStructure,
    val curveSlope: Double, // annualized delta
    val timestamp: Long
)
