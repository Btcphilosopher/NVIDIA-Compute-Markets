package com.example.models

data class DatacenterNode(
    val id: String,
    val name: String,
    val location: String,
    val region: MarketRegion,
    val latitude: Double,
    val longitude: Double,
    val totalGpus: Int,
    val activeGpus: Int,
    val availableGpus: Int,
    val utilizationRate: Double,
    val totalPowerMw: Double,
    val currentPowerDrawMw: Double,
    val dominantGpu: GpuChip,
    val spotPricePerHour: Double,
    val pue: Double, // Power Usage Effectiveness e.g. 1.15
    val networkLatencyMs: Double,
    val coolingTech: String, // Direct Liquid Cooling (DLC), Immersion, Air
    val powerSource: String, // Hydro, Nuclear, Solar/Battery, Grid
    val queueWaitHours: Double
) {
    val utilizationPercent: Double get() = utilizationRate
}

data class GlobalCapacityOverview(
    val totalGpus: Int,
    val activeRunningGpus: Int,
    val availableGpus: Int,
    val globalUtilization: Double,
    val averageQueueTimeHours: Double,
    val totalPowerConsumptionMw: Double,
    val regions: List<RegionalCapacity>
) {
    val utilizationPercent: Double get() = globalUtilization
}

data class RegionalCapacity(
    val region: MarketRegion,
    val totalGpus: Int,
    val activeGpus: Int,
    val availableGpus: Int,
    val utilizationRate: Double,
    val avgSpotPrice: Double,
    val powerCapacityMw: Double,
    val dominantChip: GpuChip
) {
    val utilizationPercent: Double get() = utilizationRate
    val dominantGpu: GpuChip get() = dominantChip
}
