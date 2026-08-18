package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AppLanguage
import com.example.presentation.TerminalScreen
import com.example.ui.theme.*
import com.example.utils.Localization

data class NavItemSpec(
    val screen: TerminalScreen,
    val locKey: String,
    val icon: ImageVector
)

val PrimaryNavItems = listOf(
    NavItemSpec(TerminalScreen.OVERVIEW, "nav_overview", Icons.Default.Dashboard),
    NavItemSpec(TerminalScreen.GPU_MARKET, "nav_gpu_market", Icons.Default.Memory),
    NavItemSpec(TerminalScreen.SPOT_PRICES, "nav_spot_prices", Icons.Default.ShowChart),
    NavItemSpec(TerminalScreen.FORWARD_CURVE, "nav_forward_curve", Icons.AutoMirrored.Filled.TrendingUp),
    NavItemSpec(TerminalScreen.STRIKE_PRICE, "nav_strike_price", Icons.Default.Calculate),
    NavItemSpec(TerminalScreen.VOL_SURFACE, "nav_vol_surface", Icons.Default.Grain),
    NavItemSpec(TerminalScreen.CAPACITY, "nav_capacity", Icons.Default.Storage),
    NavItemSpec(TerminalScreen.DATACENTERS, "nav_datacenters", Icons.Default.Public),
    NavItemSpec(TerminalScreen.POWER_COST, "nav_power_cost", Icons.Default.Bolt),
    NavItemSpec(TerminalScreen.SUPPLY_DEMAND, "nav_supply_demand", Icons.Default.Balance),
    NavItemSpec(TerminalScreen.RISK, "nav_risk", Icons.Default.Warning),
    NavItemSpec(TerminalScreen.SIMULATION, "nav_simulation", Icons.Default.Science),
    NavItemSpec(TerminalScreen.API_HEALTH, "nav_api_health", Icons.Default.Speed),
    NavItemSpec(TerminalScreen.SETTINGS, "nav_settings", Icons.Default.Settings)
)

@Composable
fun TerminalBottomNavBar(
    activeScreen: TerminalScreen,
    onNavigate: (TerminalScreen) -> Unit,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = TerminalBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PrimaryNavItems.forEach { item ->
                val isSelected = activeScreen == item.screen
                val title = Localization.t(item.locKey, currentLanguage)

                Surface(
                    onClick = { onNavigate(item.screen) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) NvidiaGreenSurface else TerminalSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NvidiaGreenDim else TerminalBorder
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = title,
                            tint = if (isSelected) NvidiaGreenGlow else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) NvidiaGreenGlow else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
