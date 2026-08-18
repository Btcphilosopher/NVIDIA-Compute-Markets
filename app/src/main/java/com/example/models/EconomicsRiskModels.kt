package com.example.models

data class ComputeCostComponent(
    val id: String,
    val nameEn: String,
    val nameHokkien: String,
    val nameZh: String,
    val costPerHour: Double,
    val percentageOfTotal: Double,
    val colorHex: Long
)

data class LevelizedCostOfCompute(
    val chip: GpuChip,
    val totalRealHourlyCost: Double,
    val marketSpotPrice: Double,
    val grossMarginPercentage: Double,
    val electricityKwhPrice: Double,
    val components: List<ComputeCostComponent>,
    val serverAcquisitionCostUsd: Double,
    val usefulLifeYears: Double,
    val residualSalvageValueUsd: Double,
    val rackPowerDensityKw: Double
)

data class SupplyDemandTimeSeries(
    val timestamp: Long,
    val timeLabel: String,
    val demandUnitsGpu: Double,
    val supplyCapacityUnitsGpu: Double,
    val utilizationPercent: Double,
    val projectedDeficitGpu: Double
)

data class SupplyDemandOverview(
    val region: MarketRegion,
    val currentDemandTflopsNorm: Double,
    val currentSupplyTflopsNorm: Double,
    val utilizationPercent: Double,
    val forecastQuarterlyCapacityGrowthPercent: Double,
    val projectedShortageNext3MonthsGpus: Int,
    val historyAndForecast: List<SupplyDemandTimeSeries>
)

data class RiskFactor(
    val category: RiskCategory,
    val title: String,
    val scoreOutOf100: Int,
    val severity: RiskSeverity,
    val description: String,
    val metricValue: String,
    val mitigationStrategy: String
)

enum class RiskCategory {
    PRICE_VOLATILITY,
    CAPACITY_EXHAUSTION,
    POWER_GRID_RELIABILITY,
    SUPPLY_CHAIN_PACKAGING,
    TECH_OBSOLESCENCE,
    UTILIZATION_DECAY
}

enum class RiskSeverity {
    LOW,
    MODERATE,
    ELEVATED,
    CRITICAL
}

data class ComputeRiskDashboard(
    val overallRiskScore: Int, // 0 - 100
    val valueAtRisk1Day95: Double, // % potential spot drawdown
    val capacityExhaustionProb: Double, // %
    val gridCurtailmentRiskProb: Double, // %
    val factors: List<RiskFactor>
)
