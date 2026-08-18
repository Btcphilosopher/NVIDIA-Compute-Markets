package com.example.models

enum class GpuChip(
    val code: String,
    val series: String,
    val architecture: String,
    val vramGb: Int,
    val fp8Tflops: Int,
    val baseTdpWatts: Int,
    val releaseYear: Int,
    val memoryType: String = when {
        code.startsWith("GB") -> "HBM3e / Unified"
        code.startsWith("B") -> "HBM3e 8-Hi/12-Hi"
        code == "H200" -> "HBM3e 141GB"
        else -> "HBM3 80GB"
    }
) {
    H100("H100", "Hopper", "Hopper GH100", 80, 1979, 700, 2022),
    H200("H200", "Hopper", "Hopper GH200 NVL", 141, 1979, 700, 2023),
    B200("B200", "Blackwell", "Blackwell GB200/B200", 192, 4500, 1000, 2024),
    B300("B300", "Blackwell Ultra", "Blackwell Ultra B300", 288, 5200, 1200, 2025),
    GB200("GB200", "Grace Blackwell", "Grace CPU + 2x B200", 384, 9000, 2700, 2024),
    GB300("GB300", "Grace Blackwell Ultra", "Grace CPU + 2x B300", 576, 10400, 3200, 2025);

    val tdpWatts: Int get() = baseTdpWatts
}

enum class MarketRegion(val code: String, val regionName: String) {
    GLOBAL("GLOBAL", "Global Aggregate"),
    NORTH_AMERICA("NA", "North America (US-East/West/Central)"),
    EUROPE("EU", "Europe (Frankfurt/Dublin/Nordics)"),
    ASIA_PACIFIC("APAC", "Asia-Pacific (Tokyo/Singapore)"),
    TAIWAN("TW", "Taiwan (Hsinchu/Taipei/Tainan)"),
    CHINA("CN", "China (Regional domestic)");

    val displayName: String get() = regionName
}
