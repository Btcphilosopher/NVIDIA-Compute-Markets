package com.example.presentation

import com.example.models.*

enum class TerminalScreen(val route: String) {
    OVERVIEW("overview"),
    GPU_MARKET("gpu_market"),
    SPOT_PRICES("spot_prices"),
    FORWARD_CURVE("forward_curve"),
    STRIKE_PRICE("strike_price"),
    VOL_SURFACE("vol_surface"),
    CAPACITY("capacity"),
    DATACENTERS("datacenters"),
    POWER_COST("power_cost"),
    SUPPLY_DEMAND("supply_demand"),
    RISK("risk"),
    SIMULATION("simulation"),
    GPU_DETAIL("gpu_detail"),
    API_HEALTH("api_health"),
    SETTINGS("settings")
}

enum class StrikeChartMode {
    PRICE_PAYOFF,
    IMPLIED_VOL,
    PROBABILITY,
    GREEKS
}

enum class VolSurfaceViewMode {
    SURFACE_3D,
    HEATMAP_2D,
    STRIKE_SMILE_SLICE
}

enum class DisplayCurrency(val code: String, val symbol: String, val fxRateToUsd: Double) {
    USD("USD", "$", 1.0),
    TWD("TWD", "NT$", 32.5),
    EUR("EUR", "€", 0.92),
    CNY("CNY", "¥", 7.24)
}

data class AiMessage(
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean = false
)

data class CommandPaletteItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val targetScreen: TerminalScreen,
    val targetChip: GpuChip? = null,
    val targetDatacenterId: String? = null
)
