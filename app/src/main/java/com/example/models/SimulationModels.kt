package com.example.models

data class SimulationInput(
    val gpuSupplyDeltaPercent: Float = 0f, // e.g. -20% to +50%
    val aiDemandGrowthDeltaPercent: Float = 0f, // e.g. -30% to +80%
    val electricityPriceDeltaPercent: Float = 0f, // e.g. -50% to +100%
    val datacenterCapacityExpansionPercent: Float = 0f,
    val utilizationShiftPercent: Float = 0f,
    val nextGenPerformanceBoostPercent: Float = 0f
)

data class SimulationResult(
    val chip: GpuChip,
    val baselineSpotPrice: Double,
    val simulatedSpotPrice: Double,
    val spotPriceChangePercent: Double,
    val baseline1YForward: Double,
    val simulated1YForward: Double,
    val forward1YChangePercent: Double,
    val baselineUtilization: Double,
    val simulatedUtilization: Double,
    val baselineVol: Double,
    val simulatedVol: Double,
    val baselineCapacityAvailable: Int,
    val simulatedCapacityAvailable: Int,
    val shortageRiskLevel: String,
    val forwardCurveComparison: List<SimulatedCurvePoint>,
    val timestamp: Long = System.currentTimeMillis()
)

data class SimulatedCurvePoint(
    val maturityLabel: String,
    val baselinePrice: Double,
    val simulatedPrice: Double
)

data class ApiHealthState(
    val restApiLatencyMs: Long,
    val restApiStatusOk: Boolean,
    val webSocketLatencyMs: Long,
    val webSocketConnected: Boolean,
    val lastMarketDataSync: Long,
    val marketDataFeedLive: Boolean,
    val capacityFeedLive: Boolean,
    val forecastServiceLive: Boolean,
    val simulationServiceLive: Boolean,
    val requestErrorCount: Int,
    val activeEndpointUrl: String
)
