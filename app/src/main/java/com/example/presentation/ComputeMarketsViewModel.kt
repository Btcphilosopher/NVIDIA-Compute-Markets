package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ComputeMarketRepository
import com.example.models.*
import com.example.utils.Localization
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ComputeMarketsViewModel(
    val repository: ComputeMarketRepository = ComputeMarketRepository()
) : ViewModel() {

    // Language & Preferences
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _displayCurrency = MutableStateFlow(DisplayCurrency.USD)
    val displayCurrency: StateFlow<DisplayCurrency> = _displayCurrency.asStateFlow()

    // Active Screen & Navigation
    private val _activeScreen = MutableStateFlow(TerminalScreen.OVERVIEW)
    val activeScreen: StateFlow<TerminalScreen> = _activeScreen.asStateFlow()

    // Selected Instruments
    private val _selectedChip = MutableStateFlow(GpuChip.H100)
    val selectedChip: StateFlow<GpuChip> = _selectedChip.asStateFlow()

    private val _selectedRegion = MutableStateFlow(MarketRegion.GLOBAL)
    val selectedRegion: StateFlow<MarketRegion> = _selectedRegion.asStateFlow()

    private val _selectedTimeframeHours = MutableStateFlow(24)
    val selectedTimeframeHours: StateFlow<Int> = _selectedTimeframeHours.asStateFlow()

    // Real-time market instruments
    private val _marketInstruments = MutableStateFlow<List<GpuMarketInstrument>>(emptyList())
    val marketInstruments: StateFlow<List<GpuMarketInstrument>> = _marketInstruments.asStateFlow()

    // Price History
    private val _priceHistory = MutableStateFlow<List<PriceTick>>(emptyList())
    val priceHistory: StateFlow<List<PriceTick>> = _priceHistory.asStateFlow()

    // Forward Curve Data
    private val _forwardCurve = MutableStateFlow<ForwardCurveData?>(null)
    val forwardCurve: StateFlow<ForwardCurveData?> = _forwardCurve.asStateFlow()

    private val _allForwardCurves = MutableStateFlow<List<ForwardCurveData>>(emptyList())
    val allForwardCurves: StateFlow<List<ForwardCurveData>> = _allForwardCurves.asStateFlow()

    // Strike & Options State
    private val _strikeOptionType = MutableStateFlow(OptionType.CALL_OPTION)
    val strikeOptionType: StateFlow<OptionType> = _strikeOptionType.asStateFlow()

    private val _strikePriceValue = MutableStateFlow(2.80)
    val strikePriceValue: StateFlow<Double> = _strikePriceValue.asStateFlow()

    private val _strikeMaturityDays = MutableStateFlow(90)
    val strikeMaturityDays: StateFlow<Int> = _strikeMaturityDays.asStateFlow()

    private val _strikeContractSize = MutableStateFlow(10000)
    val strikeContractSize: StateFlow<Int> = _strikeContractSize.asStateFlow()

    private val _currentStrikeContract = MutableStateFlow<StrikeOptionContract?>(null)
    val currentStrikeContract: StateFlow<StrikeOptionContract?> = _currentStrikeContract.asStateFlow()

    private val _strikeChartMode = MutableStateFlow(StrikeChartMode.PRICE_PAYOFF)
    val strikeChartMode: StateFlow<StrikeChartMode> = _strikeChartMode.asStateFlow()

    // Volatility Surface State
    private val _volSurfaceData = MutableStateFlow<VolatilitySurfaceData?>(null)
    val volSurfaceData: StateFlow<VolatilitySurfaceData?> = _volSurfaceData.asStateFlow()

    private val _volSurfaceViewMode = MutableStateFlow(VolSurfaceViewMode.SURFACE_3D)
    val volSurfaceViewMode: StateFlow<VolSurfaceViewMode> = _volSurfaceViewMode.asStateFlow()

    // Capacity & Datacenters
    private val _globalCapacity = MutableStateFlow<GlobalCapacityOverview?>(null)
    val globalCapacity: StateFlow<GlobalCapacityOverview?> = _globalCapacity.asStateFlow()

    private val _datacenters = MutableStateFlow<List<DatacenterNode>>(emptyList())
    val datacenters: StateFlow<List<DatacenterNode>> = _datacenters.asStateFlow()

    private val _selectedDatacenter = MutableStateFlow<DatacenterNode?>(null)
    val selectedDatacenter: StateFlow<DatacenterNode?> = _selectedDatacenter.asStateFlow()

    // Economics, Supply/Demand & Risk
    private val _costBreakdown = MutableStateFlow<LevelizedCostOfCompute?>(null)
    val costBreakdown: StateFlow<LevelizedCostOfCompute?> = _costBreakdown.asStateFlow()

    private val _supplyDemand = MutableStateFlow<SupplyDemandOverview?>(null)
    val supplyDemand: StateFlow<SupplyDemandOverview?> = _supplyDemand.asStateFlow()

    private val _riskDashboard = MutableStateFlow<ComputeRiskDashboard?>(null)
    val riskDashboard: StateFlow<ComputeRiskDashboard?> = _riskDashboard.asStateFlow()

    // Scenario Simulation
    private val _simulationInput = MutableStateFlow(SimulationInput())
    val simulationInput: StateFlow<SimulationInput> = _simulationInput.asStateFlow()

    private val _simulationResult = MutableStateFlow<SimulationResult?>(null)
    val simulationResult: StateFlow<SimulationResult?> = _simulationResult.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    // Command Palette State
    private val _isCommandPaletteOpen = MutableStateFlow(false)
    val isCommandPaletteOpen: StateFlow<Boolean> = _isCommandPaletteOpen.asStateFlow()

    private val _commandQuery = MutableStateFlow("")
    val commandQuery: StateFlow<String> = _commandQuery.asStateFlow()

    // AI Terminal Assistant State
    private val _isAiTerminalOpen = MutableStateFlow(false)
    val isAiTerminalOpen: StateFlow<Boolean> = _isAiTerminalOpen.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiMessage>>(emptyList())
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    // API Health & Status
    private val _apiHealthState = MutableStateFlow(repository.getApiHealthState())
    val apiHealthState: StateFlow<ApiHealthState> = _apiHealthState.asStateFlow()

    private var tickJob: Job? = null

    init {
        loadInitialData()
        startLiveTickListener()
        initializeAiAssistantWelcome()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val overview = repository.getMarketOverview()
                _marketInstruments.value = overview
                val initialChip = _selectedChip.value
                val chipSpot = overview.firstOrNull { it.chip == initialChip }?.spotPrice ?: 2.84
                _strikePriceValue.value = chipSpot

                loadChipSpecificData(initialChip)
                _allForwardCurves.value = repository.getAllForwardCurves()
                _globalCapacity.value = repository.getGlobalCapacity()
                _datacenters.value = repository.getDatacenters()
                _costBreakdown.value = repository.getCostBreakdown(initialChip)
                _supplyDemand.value = repository.getSupplyDemandOverview(_selectedRegion.value)
                _riskDashboard.value = repository.getRiskDashboard()
                runSimulation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startLiveTickListener() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            repository.subscribeMarketTicks().collect { priceMap ->
                val current = _marketInstruments.value
                if (current.isNotEmpty()) {
                    _marketInstruments.value = current.map { item ->
                        val newSpot = priceMap[item.chip] ?: item.spotPrice
                        val change = newSpot - (item.spotPrice - item.priceChange24h)
                        val changePct = (change / (newSpot - change)) * 100.0
                        item.copy(
                            spotPrice = newSpot,
                            priceChange24h = (change * 100.0).roundToInt() / 100.0,
                            priceChangePercent24h = (changePct * 10.0).roundToInt() / 10.0,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                }
                _apiHealthState.value = repository.getApiHealthState()
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        initializeAiAssistantWelcome()
    }

    fun setCurrency(currency: DisplayCurrency) {
        _displayCurrency.value = currency
    }

    fun navigateTo(screen: TerminalScreen) {
        _activeScreen.value = screen
    }

    fun selectChip(chip: GpuChip) {
        _selectedChip.value = chip
        val spot = _marketInstruments.value.firstOrNull { it.chip == chip }?.spotPrice ?: 3.0
        _strikePriceValue.value = spot
        loadChipSpecificData(chip)
    }

    private fun loadChipSpecificData(chip: GpuChip) {
        viewModelScope.launch {
            _priceHistory.value = repository.getPriceHistory(chip, _selectedTimeframeHours.value)
            _forwardCurve.value = repository.getForwardCurve(chip)
            _volSurfaceData.value = repository.getVolatilitySurface(chip)
            _costBreakdown.value = repository.getCostBreakdown(chip)
            recalculateStrikeContract()
        }
    }

    fun setTimeframe(hours: Int) {
        _selectedTimeframeHours.value = hours
        viewModelScope.launch {
            _priceHistory.value = repository.getPriceHistory(_selectedChip.value, hours)
        }
    }

    fun setRegion(region: MarketRegion) {
        _selectedRegion.value = region
        viewModelScope.launch {
            _supplyDemand.value = repository.getSupplyDemandOverview(region)
            recalculateStrikeContract()
        }
    }

    // Strike & Options Controls
    fun setStrikeOptionType(type: OptionType) {
        _strikeOptionType.value = type
        recalculateStrikeContract()
    }

    fun setStrikePrice(price: Double) {
        _strikePriceValue.value = price
        recalculateStrikeContract()
    }

    fun setStrikeMaturity(days: Int) {
        _strikeMaturityDays.value = days
        recalculateStrikeContract()
    }

    fun setStrikeContractSize(size: Int) {
        _strikeContractSize.value = size
        recalculateStrikeContract()
    }

    fun setStrikeChartMode(mode: StrikeChartMode) {
        _strikeChartMode.value = mode
    }

    private fun recalculateStrikeContract() {
        viewModelScope.launch {
            _currentStrikeContract.value = repository.calculateStrikeContract(
                chip = _selectedChip.value,
                region = _selectedRegion.value,
                optionType = _strikeOptionType.value,
                strikePrice = _strikePriceValue.value,
                maturityDays = _strikeMaturityDays.value,
                contractSizeGpuHours = _strikeContractSize.value
            )
        }
    }

    // Vol Surface Controls
    fun setVolSurfaceViewMode(mode: VolSurfaceViewMode) {
        _volSurfaceViewMode.value = mode
    }

    // Datacenter selection
    fun selectDatacenter(node: DatacenterNode?) {
        _selectedDatacenter.value = node
    }

    // Simulation Controls
    fun updateSimulationInput(transform: (SimulationInput) -> SimulationInput) {
        _simulationInput.value = transform(_simulationInput.value)
    }

    fun resetSimulationInput() {
        _simulationInput.value = SimulationInput()
        runSimulation()
    }

    fun runSimulation() {
        viewModelScope.launch {
            _isSimulating.value = true
            delay(250) // High-precision feel
            _simulationResult.value = repository.runScenarioSimulation(_simulationInput.value)
            _isSimulating.value = false
        }
    }

    // Command Palette
    fun openCommandPalette() {
        _isCommandPaletteOpen.value = true
        _commandQuery.value = ""
    }

    fun closeCommandPalette() {
        _isCommandPaletteOpen.value = false
    }

    fun setCommandQuery(query: String) {
        _commandQuery.value = query
    }

    fun getFilteredCommandItems(): List<CommandPaletteItem> {
        val q = _commandQuery.value.trim().lowercase()
        val lang = _currentLanguage.value

        val allItems = mutableListOf<CommandPaletteItem>()

        // Screens
        TerminalScreen.entries.forEach { screen ->
            val key = when(screen) {
                TerminalScreen.OVERVIEW -> "nav_overview"
                TerminalScreen.GPU_MARKET -> "nav_gpu_market"
                TerminalScreen.SPOT_PRICES -> "nav_spot_prices"
                TerminalScreen.FORWARD_CURVE -> "nav_forward_curve"
                TerminalScreen.STRIKE_PRICE -> "nav_strike_price"
                TerminalScreen.VOL_SURFACE -> "nav_vol_surface"
                TerminalScreen.CAPACITY -> "nav_capacity"
                TerminalScreen.DATACENTERS -> "nav_datacenters"
                TerminalScreen.POWER_COST -> "nav_power_cost"
                TerminalScreen.SUPPLY_DEMAND -> "nav_supply_demand"
                TerminalScreen.RISK -> "nav_risk"
                TerminalScreen.SIMULATION -> "nav_simulation"
                TerminalScreen.GPU_DETAIL -> "nav_gpu_market"
                TerminalScreen.API_HEALTH -> "nav_api_health"
                TerminalScreen.SETTINGS -> "nav_settings"
            }
            val title = Localization.t(key, lang)
            allItems.add(
                CommandPaletteItem(
                    id = "screen_${screen.name}",
                    title = title,
                    subtitle = "Navigate to ${screen.name.lowercase().replace('_', ' ')}",
                    category = "Navigation / 畫面導覽",
                    targetScreen = screen
                )
            )
        }

        // GPU Chips
        GpuChip.entries.forEach { chip ->
            allItems.add(
                CommandPaletteItem(
                    id = "gpu_${chip.name}",
                    title = "NVIDIA ${chip.name} (${chip.vramGb}GB VRAM)",
                    subtitle = "Open ${chip.name} Deep Instrument & Forward Curve",
                    category = "Instruments / 算力標的",
                    targetScreen = TerminalScreen.GPU_DETAIL,
                    targetChip = chip
                )
            )
            allItems.add(
                CommandPaletteItem(
                    id = "strike_${chip.name}",
                    title = "${chip.name} Strike Price & Options",
                    subtitle = "Open Black-76 compute options pricer for ${chip.name}",
                    category = "Derivatives / 衍生品",
                    targetScreen = TerminalScreen.STRIKE_PRICE,
                    targetChip = chip
                )
            )
        }

        // Datacenters
        _datacenters.value.forEach { dc ->
            allItems.add(
                CommandPaletteItem(
                    id = "dc_${dc.id}",
                    title = dc.name,
                    subtitle = "${dc.location} • ${dc.dominantGpu} • ${dc.totalPowerMw}MW",
                    category = "Datacenters / 機房節點",
                    targetScreen = TerminalScreen.DATACENTERS,
                    targetDatacenterId = dc.id
                )
            )
        }

        if (q.isEmpty()) return allItems.take(12)
        return allItems.filter {
            it.title.lowercase().contains(q) ||
            it.subtitle.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
    }

    fun executeCommand(item: CommandPaletteItem) {
        closeCommandPalette()
        item.targetChip?.let { selectChip(it) }
        item.targetDatacenterId?.let { dcId ->
            val node = _datacenters.value.firstOrNull { it.id == dcId }
            selectDatacenter(node)
        }
        navigateTo(item.targetScreen)
    }

    // AI Terminal Assistant
    fun openAiTerminal() {
        _isAiTerminalOpen.value = true
    }

    fun closeAiTerminal() {
        _isAiTerminalOpen.value = false
    }

    private fun initializeAiAssistantWelcome() {
        val lang = _currentLanguage.value
        val welcomeText = when (lang) {
            AppLanguage.ENGLISH -> "NVIDIA Compute Market Quant AI online. Connected to live pricing feeds, options pricing engines, datacenter telemetry, and forward curve models. Ask any compute market query or tap suggested prompts below."
            AppLanguage.TAIWANESE_HOKKIEN -> "輝達算力市場量化 AI 助理已就位。這馬連線到即時報價饋線、期權履約計算器、全球機房監控佮遠期曲線模型。有啥物算力市場的問題隨時提出來！"
            AppLanguage.CHINESE_MANDARIN -> "NVIDIA 算力市场量化 AI 助手在线。已实时连接即期报价值、期权定价引擎、数据中心遥测及远期曲线模型。请在下方输入问题或点击预设提示词。"
        }
        _aiMessages.value = listOf(
            AiMessage(
                id = "welcome",
                sender = "NVIDIA Quant AI",
                text = welcomeText,
                isUser = false
            )
        )
    }

    fun sendAiPrompt(promptText: String) {
        if (promptText.isBlank()) return

        val userMsg = AiMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = "Institutional Trader",
            text = promptText,
            isUser = true
        )

        _aiMessages.value = _aiMessages.value + userMsg
        _isAiGenerating.value = true

        viewModelScope.launch {
            delay(650) // realistic quant response latency
            val lang = _currentLanguage.value
            val responseText = generateInstitutionalAiAnswer(promptText, lang)

            val aiMsg = AiMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = "NVIDIA Quant AI",
                text = responseText,
                isUser = false
            )
            _aiMessages.value = _aiMessages.value + aiMsg
            _isAiGenerating.value = false
        }
    }

    private fun generateInstitutionalAiAnswer(prompt: String, lang: AppLanguage): String {
        val p = prompt.lowercase()
        val h100 = _marketInstruments.value.firstOrNull { it.chip == GpuChip.H100 }
        val b200 = _marketInstruments.value.firstOrNull { it.chip == GpuChip.B200 }
        val h100Spot = h100?.spotPrice ?: 2.84
        val b200Spot = b200?.spotPrice ?: 3.84

        return when {
            p.contains("h100") && (p.contains("spike") || p.contains("上涨") || p.contains("起價") || p.contains("why")) -> {
                when (lang) {
                    AppLanguage.ENGLISH -> "H100 spot compute is trading at $${h100Spot}/GPU-hr (+${h100?.priceChangePercent24h ?: 3.2}%). Primary drivers: (1) Tightening cluster availability in US-East/Virginia (91.4% utilization); (2) Frontier reasoning model RL distillation demand spikes; (3) Grid power curtailment in Tier-1 hyperscale interconnects. Recommendation: Lock in 3M forward contracts at $${h100?.forward3M ?: 2.63} to hedge basis risk."
                    AppLanguage.TAIWANESE_HOKKIEN -> "H100 現貨價這馬報價每小時 $${h100Spot} (+${h100?.priceChangePercent24h ?: 3.2}%)。主要原因：(1) 美東維吉尼亞機房使用率衝到 91.4%，現成有閒的算力真食緊；(2) 推理模型強化學習訓練需求大爆發；(3) 特高壓變電所並網排隊。建議：利用 3個月遠期合約 ($${h100?.forward3M ?: 2.63}) 鎖定成本。"
                    AppLanguage.CHINESE_MANDARIN -> "H100 即期价格目前报 $${h100Spot}/GPU小时（24小时涨幅 +${h100?.priceChangePercent24h ?: 3.2}%）。核心驱动因素：(1) 美东与亚太核心集群利用率达 91.4%，可用容量紧俏；(2) 深度推理模型后训练算力需求剧增；(3) 数据中心特高压电力接网约束。建议通过 3个月远期（$${h100?.forward3M ?: 2.63}）进行基差套期保值。"
                }
            }
            p.contains("b200") || p.contains("forward") || p.contains("远期") || p.contains("遠期") -> {
                when (lang) {
                    AppLanguage.ENGLISH -> "Blackwell B200 Forward Term Structure analysis: Spot is $${b200Spot}/hr, 1M forward at $${b200?.forward1M ?: 3.71}, 12M forward at $${b200?.forward12M ?: 3.12}. The curve displays pronounced backwardation (-18.7% annualized slope) reflecting scheduled TSMC CoWoS packaging capacity expansion and subsequent delivery ramp."
                    AppLanguage.TAIWANESE_HOKKIEN -> "Blackwell B200 遠期曲線分析：這馬現貨價 $${b200Spot}/小時，1個月遠期 $${b200?.forward1M ?: 3.71}，1年遠期 $${b200?.forward12M ?: 3.12}。曲線展現強烈逆價差（年化斜率 -18.7%），主要反映台積電 CoWoS 產能大量開出後的供給預期。"
                    AppLanguage.CHINESE_MANDARIN -> "Blackwell B200 远期期限结构分析：即期价格 $${b200Spot}，1个月远期 $${b200?.forward1M ?: 3.71}，12个月远期 $${b200?.forward12M ?: 3.12}。曲线呈现明显的逆价差（年化斜率 -18.7%），体现出台积电 CoWoS 先进封装产能持续释放后的算力供给增长预期。"
                }
            }
            p.contains("cheapest") || p.contains("便宜") || p.contains("平準") || p.contains("機房") || p.contains("datacenter") -> {
                when (lang) {
                    AppLanguage.ENGLISH -> "Lowest Cost Compute Nodes: (1) Columbia Hydro Campus (Oregon, USA) at $2.68/hr with 100% clean hydroelectric power ($0.048/kWh); (2) TSMC/NVD Hsinchu Science Park (Taiwan) at $2.74/hr for GB200 clusters with ultra-low 1.8ms local interconnect latency."
                    AppLanguage.TAIWANESE_HOKKIEN -> "全世界上平價算力節點：(1) 美國奧勒岡水力發電園區每小時 $2.68（水力發電一度電只要 $0.048 美金）；(2) 臺灣新竹科學園區 GB200 機房每小時 $2.74，本地網路延遲只有 1.8 毫秒。"
                    AppLanguage.CHINESE_MANDARIN -> "全球最具成本效益的数据中心节点：(1) 美国俄勒冈哥伦比亚水电园区（$2.68/小时），得益于 100% 水电清洁能源（$0.048/kWh）；(2) 中国台湾新竹科学园区 GB200 算力枢纽（$2.74/小时），具备 1.8ms 极低本地时延。"
                }
            }
            p.contains("cost") || p.contains("成本") || p.contains("比较") || p.contains("比較") -> {
                when (lang) {
                    AppLanguage.ENGLISH -> "Levelized Cost of Compute (LCOC) comparison: H100 real hourly cost is $1.92/hr (hardware capex $1.18, power $0.28, cooling $0.08, colo $0.12, network $0.10, ops $0.16). B200 real hourly cost is $2.44/hr, but delivers 2.28x higher FP8 TFLOPS per dollar, yielding a 38% net economic cost efficiency improvement per token generated."
                    AppLanguage.TAIWANESE_HOKKIEN -> "算力平準化真實成本 (LCOC) 評估：H100 每小時真實成本是 $1.92（硬體折舊 $1.18、電費 $0.28、水冷 $0.08、機房 $0.12、網卡 $0.10、運維 $0.16）。B200 每小時成本雖然是 $2.44，毋過每塊錢產出的 FP8 算力多出 2.28倍，換算每一個 Token 的真實成本省下 38%。"
                    AppLanguage.CHINESE_MANDARIN -> "平准化算力成本 (LCOC) 对比分析：H100 每小时真实经济成本为 $1.92（硬件折旧 $1.18、电力 $0.28、冷却 $0.08、机架 $0.12、网络 $0.10、运维 $0.16）。B200 每小时成本为 $2.44，但单位美元提供的 FP8 算力提升 2.28倍，使单 Token 实际生成成本降低 38%。"
                }
            }
            else -> {
                when (lang) {
                    AppLanguage.ENGLISH -> "Institutional Market Analysis: Global compute index stands at 4.82M GPUs with 87.3% utilization. Implied volatility for Hopper series remains steady at 34.8% while Blackwell Ultra contracts trade at 48.0% IV. Quantitative market pricing models indicate strong term premium support across 6M-12M delivery horizons."
                    AppLanguage.TAIWANESE_HOKKIEN -> "法人量化市場快報：全球算力總池達到 482萬粒 GPU，整體使用率 87.3%。Hopper 系列隱含波動度維持在 34.8%，Blackwell Ultra 期權隱含波動度在 48.0%。量化定價模型顯示 6個月到 12個月遠期交割具有堅實的基差支撐。"
                    AppLanguage.CHINESE_MANDARIN -> "机构量化市场速报：全球算力总规模 482万张 GPU，综合利用率 87.3%。Hopper 系列隐含波动率维持在 34.8%，Blackwell Ultra 算力期权报 48.0% IV。量化定价模型显示 6个月至 12个月远期交割具有强劲的期限升贴水支撑。"
                }
            }
        }
    }

    // Toggle WebSocket stream for API health testing
    fun toggleWebSocketStream() {
        val newState = !repository.isWebSocketConnected
        repository.toggleWebSocketConnection(newState)
        _apiHealthState.value = repository.getApiHealthState()
    }

    fun triggerReconnect() {
        viewModelScope.launch {
            repository.toggleWebSocketConnection(false)
            _apiHealthState.value = repository.getApiHealthState()
            delay(800)
            repository.toggleWebSocketConnection(true)
            _apiHealthState.value = repository.getApiHealthState()
        }
    }
}
