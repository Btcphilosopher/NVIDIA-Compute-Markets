package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.components.*
import com.example.presentation.*
import com.example.screens.*
import com.example.ui.theme.ComputeMarketsTheme
import com.example.ui.theme.TerminalBackground

class MainActivity : ComponentActivity() {

    private val viewModel: ComputeMarketsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComputeMarketsTheme {
                TerminalMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TerminalMainScreen(
    viewModel: ComputeMarketsViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val displayCurrency by viewModel.displayCurrency.collectAsState()
    val activeScreen by viewModel.activeScreen.collectAsState()
    val instruments by viewModel.marketInstruments.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()
    val apiHealth by viewModel.apiHealthState.collectAsState()

    val isCommandPaletteOpen by viewModel.isCommandPaletteOpen.collectAsState()
    val commandQuery by viewModel.commandQuery.collectAsState()

    val isAiTerminalOpen by viewModel.isAiTerminalOpen.collectAsState()
    val aiMessages by viewModel.aiMessages.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground),
        containerColor = TerminalBackground,
        topBar = {
            MarketTickerTopBar(
                instruments = instruments,
                selectedChip = selectedChip,
                onSelectChip = { viewModel.selectChip(it) },
                currentLanguage = currentLanguage,
                onSetLanguage = { viewModel.setLanguage(it) },
                isWebSocketLive = apiHealth.webSocketConnected,
                onOpenCommandPalette = { viewModel.openCommandPalette() },
                onOpenAiTerminal = { viewModel.openAiTerminal() },
                onNavigateTo = { viewModel.navigateTo(it) },
                currencySymbol = displayCurrency.symbol
            )
        },
        bottomBar = {
            TerminalBottomNavBar(
                activeScreen = activeScreen,
                onNavigate = { viewModel.navigateTo(it) },
                currentLanguage = currentLanguage
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TerminalBackground)
        ) {
            AnimatedContent(
                targetState = activeScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    TerminalScreen.OVERVIEW -> MarketOverviewScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    TerminalScreen.GPU_MARKET -> GpuMarketScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    TerminalScreen.SPOT_PRICES -> SpotPricesScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.FORWARD_CURVE -> ForwardCurveScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.STRIKE_PRICE -> StrikePriceScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.VOL_SURFACE -> VolSurfaceScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.CAPACITY -> CapacityScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.DATACENTERS -> DatacentersScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.POWER_COST -> PowerCostScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.SUPPLY_DEMAND -> SupplyDemandScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.RISK -> RiskDashboardScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.SIMULATION -> SimulationScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.GPU_DETAIL -> GpuDetailScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    TerminalScreen.API_HEALTH -> ApiHealthScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                    TerminalScreen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        currentLanguage = currentLanguage,
                        displayCurrency = displayCurrency
                    )
                }
            }

            // Command Palette Modal Dialog
            if (isCommandPaletteOpen) {
                CommandPaletteDialog(
                    query = commandQuery,
                    onQueryChange = { viewModel.setCommandQuery(it) },
                    items = viewModel.getFilteredCommandItems(),
                    onSelectItem = { viewModel.executeCommand(it) },
                    onDismiss = { viewModel.closeCommandPalette() }
                )
            }

            // AI Copilot Dialog
            if (isAiTerminalOpen) {
                AiTerminalDialog(
                    messages = aiMessages,
                    isGenerating = isAiGenerating,
                    onSendMessage = { viewModel.sendAiPrompt(it) },
                    currentLanguage = currentLanguage,
                    onDismiss = { viewModel.closeAiTerminal() }
                )
            }
        }
    }
}
