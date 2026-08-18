package com.example.utils

import com.example.models.AppLanguage

object Localization {
    fun t(key: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> enMap[key] ?: zhMap[key] ?: key
            AppLanguage.TAIWANESE_HOKKIEN -> hokkienMap[key] ?: zhMap[key] ?: key
            AppLanguage.CHINESE_MANDARIN -> zhMap[key] ?: enMap[key] ?: key
        }
    }

    private val enMap = mapOf(
        // Navigation & Screens
        "app_title" to "NVIDIA COMPUTE MARKETS",
        "terminal_subtitle" to "Institutional Compute Pricing & Derivatives Terminal",
        "nav_overview" to "Market Overview",
        "nav_gpu_market" to "GPU Market",
        "nav_spot_prices" to "Spot Prices",
        "nav_forward_curve" to "Forward Curve",
        "nav_strike_price" to "Strike Price",
        "nav_vol_surface" to "Vol Surface",
        "nav_capacity" to "Compute Capacity",
        "nav_datacenters" to "Datacenter Map",
        "nav_power_cost" to "Power & Cost",
        "nav_supply_demand" to "Supply & Demand",
        "nav_risk" to "Risk Center",
        "nav_simulation" to "Scenario Sim",
        "nav_api_health" to "API Health",
        "nav_settings" to "Settings",

        // Ticker & Metrics
        "spot" to "Spot",
        "forward_1m" to "1M Fwd",
        "forward_3m" to "3M Fwd",
        "forward_6m" to "6M Fwd",
        "forward_12m" to "12M Fwd",
        "volatility" to "Volatility",
        "utilization" to "Utilization",
        "available_capacity" to "Available Capacity",
        "demand" to "Demand",
        "supply" to "Supply",
        "price_hour" to "/ GPU-hr",
        "live_feed" to "LIVE FEED",
        "contango" to "CONTANGO (Upward Forward Curve)",
        "backwardation" to "BACKWARDATION (Spot Premium)",

        // Strike & Options
        "strike_title" to "Strike Price & Compute Derivatives",
        "select_gpu" to "GPU Instrument",
        "select_region" to "Compute Region",
        "select_maturity" to "Expiry / Tenor",
        "contract_size" to "Contract Size (GPU-Hours)",
        "strike_price" to "Strike Price",
        "current_spot" to "Current Spot Price",
        "implied_vol" to "Implied Vol (IV)",
        "expected_price" to "Expected Delivery Price",
        "total_premium" to "Option Premium",
        "probability_profit" to "Prob. of Profit",
        "breakeven" to "Breakeven Price",
        "delta" to "Delta (Δ)",
        "gamma" to "Gamma (Γ)",
        "vega" to "Vega (ν)",
        "theta" to "Theta (Θ)",
        "rho" to "Rho (ρ)",

        // Chart Toggles
        "chart_price" to "Price & Payoff",
        "chart_iv" to "Implied Vol Skew",
        "chart_prob" to "Probability Density",
        "chart_greeks" to "Greeks Profile",

        // Capacity & Datacenters
        "global_compute_power" to "Global AI Compute Infrastructure",
        "total_capacity" to "Total Capacity",
        "running_active" to "Active Running",
        "available_idle" to "Available Idle",
        "queue_wait" to "Queue Wait Time",
        "power_draw" to "Power Draw (MW)",
        "pue_ratio" to "PUE Ratio",
        "inspect_node" to "Inspect Node",

        // Cost Breakdown
        "cost_analysis_title" to "Compute Levelized Cost (LCOC)",
        "real_economic_cost" to "Real Economic Cost per GPU-Hour",
        "spot_margin" to "Spot Market Margin",
        "hardware_capex" to "GPU Server Capex Amortization",
        "electricity_power" to "Electricity & Power Delivery",
        "liquid_cooling" to "Direct Liquid Cooling & Chilling",
        "datacenter_colo" to "Facility Space & Physical Colo",
        "network_fabric" to "InfiniBand / NVLink Network Fabric",
        "maintenance_sla" to "Maintenance, RMA & SLA Reserve",
        "ops_overhead" to "Operations & Engineering Overhead",

        // Simulation
        "simulation_title" to "Macro Compute Scenario Simulation",
        "run_simulation" to "RUN MONTE CARLO SIMULATION",
        "gpu_supply_shock" to "GPU Supply Shift",
        "ai_demand_growth" to "AI Token Demand Growth",
        "power_price_delta" to "Power Price Variance",
        "dc_capacity_growth" to "Datacenter Buildout Expansion",
        "utilization_swing" to "Workload Utilization Shift",
        "perf_boost" to "Next-Gen Performance Boost",
        "sim_results" to "Simulated Impact vs Baseline",
        "shortage_risk" to "Shortage Risk Index",

        // Command Palette & AI
        "command_search_placeholder" to "Type command, GPU, region, or screen (Ctrl+K)...",
        "ai_assistant_title" to "NVIDIA Compute Market Quant AI",
        "ask_ai" to "Ask Quant Assistant...",
        "ai_chip_h100_spike" to "Why is H100 spot price rising today?",
        "ai_chip_b200_fwd" to "Show B200 1-year forward curve analysis",
        "ai_chip_strike_calc" to "Calculate H100 3-month ATM strike price",
        "ai_chip_cheapest_dc" to "Which datacenter region has the cheapest compute?",
        "ai_chip_h100_b200_cost" to "Compare H100 and B200 real economic costs",

        // Status & Settings
        "api_status" to "API Status & Connectivity",
        "rest_api" to "REST Pricing API",
        "websocket_stream" to "WebSocket Stream",
        "market_feed" to "Market Data Feed",
        "capacity_feed" to "Capacity Feed",
        "forecast_feed" to "Forecast Service",
        "normal" to "Normal (Real-time)",
        "connecting" to "Connecting...",
        "reconnecting" to "Reconnecting WebSocket stream...",
        "disconnected" to "Disconnected",
        "language_select" to "Terminal Language",
        "currency_select" to "Display Currency",
        "refresh_interval" to "Market Tick Interval",
        "theme_customization" to "Terminal Display Theme"
    )

    private val hokkienMap = mapOf(
        // Navigation & Screens
        "app_title" to "輝達算力交易市場",
        "terminal_subtitle" to "法人級算力定價與衍生性商品終端機",
        "nav_overview" to "算力總覽",
        "nav_gpu_market" to "GPU 市場",
        "nav_spot_prices" to "現貨價格",
        "nav_forward_curve" to "遠期曲線",
        "nav_strike_price" to "履約價格",
        "nav_vol_surface" to "波動度曲面",
        "nav_capacity" to "GPU 容量",
        "nav_datacenters" to "機房地圖",
        "nav_power_cost" to "電費與成本",
        "nav_supply_demand" to "供需分析",
        "nav_risk" to "風險控管",
        "nav_simulation" to "情境試算",
        "nav_api_health" to "連線狀態",
        "nav_settings" to "系統設定",

        // Ticker & Metrics
        "spot" to "現貨價",
        "forward_1m" to "1個月遠期",
        "forward_3m" to "3個月遠期",
        "forward_6m" to "6個月遠期",
        "forward_12m" to "1年遠期",
        "volatility" to "波動度",
        "utilization" to "使用率",
        "available_capacity" to "會使開的容量",
        "demand" to "算力需求",
        "supply" to "算力供給",
        "price_hour" to "/ 每小時",
        "live_feed" to "即時連線",
        "contango" to "正價差 (遠期比現貨貴)",
        "backwardation" to "逆價差 (現貨急需升水)",

        // Strike & Options
        "strike_title" to "履約價格與算力期權合約",
        "select_gpu" to "選擇 GPU 晶片",
        "select_region" to "算力地區",
        "select_maturity" to "到期日 / 天數",
        "contract_size" to "合約大小 (GPU小時數)",
        "strike_price" to "履約價格",
        "current_spot" to "這馬現貨價",
        "implied_vol" to "隱含波動度 (IV)",
        "expected_price" to "交割預計價",
        "total_premium" to "合約權利金",
        "probability_profit" to "賺錢機率",
        "breakeven" to "損益平衡價",
        "delta" to "Delta (Δ 價格敏感度)",
        "gamma" to "Gamma (Γ 加速度)",
        "vega" to "Vega (ν 波動度敏感度)",
        "theta" to "Theta (Θ 時間消逝)",
        "rho" to "Rho (ρ 利率敏感度)",

        // Chart Toggles
        "chart_price" to "價格與回報",
        "chart_iv" to "波動度偏斜",
        "chart_prob" to "機率分佈",
        "chart_greeks" to "Greeks 敏感度",

        // Capacity & Datacenters
        "global_compute_power" to "全球算力機房與能源配置",
        "total_capacity" to "全部容量",
        "running_active" to "衝刺運作中",
        "available_idle" to "現成有閒",
        "queue_wait" to "排隊等候時間",
        "power_draw" to "用電量 (MW)",
        "pue_ratio" to "PUE 能源效率",
        "inspect_node" to "查看機房詳細資料",

        // Cost Breakdown
        "cost_analysis_title" to "算力平準化真實成本分析 (LCOC)",
        "real_economic_cost" to "每小時真實經濟成本",
        "spot_margin" to "現貨市場利潤率",
        "hardware_capex" to "GPU 硬體設備折舊攤提",
        "electricity_power" to "發電與電力傳輸花費",
        "liquid_cooling" to "水冷散熱與空調系統",
        "datacenter_colo" to "機房租賃與土建維護",
        "network_fabric" to "高速網卡與光纖交換網路",
        "maintenance_sla" to "維修更換與保證運作合約",
        "ops_overhead" to "機房工程師與維運人事費用",

        // Simulation
        "simulation_title" to "總體市場震撼情境模擬",
        "run_simulation" to "開始模擬計算 (蒙地卡羅)",
        "gpu_supply_shock" to "GPU 出貨供給變動",
        "ai_demand_growth" to "AI 運算需求增長",
        "power_price_delta" to "電價波動幅度",
        "dc_capacity_growth" to "新機房擴充速度",
        "utilization_swing" to "算力使用率變動",
        "perf_boost" to "下一代晶片效能提升",
        "sim_results" to "模擬影響與基準對比",
        "shortage_risk" to "算力缺貨風險指數",

        // Command Palette & AI
        "command_search_placeholder" to "輸入指令、GPU、地區或畫面 (Ctrl+K)...",
        "ai_assistant_title" to "輝達算力市場 AI 量化助理",
        "ask_ai" to "請教量化助理...",
        "ai_chip_h100_spike" to "為啥物 H100 今仔日會起價？",
        "ai_chip_b200_fwd" to "請分析 B200 1年遠期曲線走勢",
        "ai_chip_strike_calc" to "試算 H100 3個月履約期權價格",
        "ai_chip_cheapest_dc" to "佗一個所在機房的算力上平準上平？",
        "ai_chip_h100_b200_cost" to "比較 H100 佮 B200 的真實經濟成本",

        // Status & Settings
        "api_status" to "API 連線與系統狀態",
        "rest_api" to "REST 報價 API",
        "websocket_stream" to "WebSocket 即時串流",
        "market_feed" to "市場數據饋線",
        "capacity_feed" to "容量監控饋線",
        "forecast_feed" to "預測服務",
        "normal" to "正常 (即時)",
        "connecting" to "連線中……",
        "reconnecting" to "即時連線中斷，正咧重新連線……",
        "disconnected" to "連線中斷",
        "language_select" to "終端介面語言",
        "currency_select" to "計價貨幣",
        "refresh_interval" to "行情更新頻率",
        "theme_customization" to "終端面板樣式"
    )

    private val zhMap = mapOf(
        // Navigation & Screens
        "app_title" to "NVIDIA 算力交易市场",
        "terminal_subtitle" to "机构级 AI 算力定价与衍生品终端",
        "nav_overview" to "市场总览",
        "nav_gpu_market" to "GPU 市场",
        "nav_spot_prices" to "即期价格",
        "nav_forward_curve" to "远期曲线",
        "nav_strike_price" to "执行价格",
        "nav_vol_surface" to "波动率",
        "nav_capacity" to "GPU 容量",
        "nav_datacenters" to "数据中心",
        "nav_power_cost" to "电力成本",
        "nav_supply_demand" to "供需",
        "nav_risk" to "风险",
        "nav_simulation" to "情景模拟",
        "nav_api_health" to "API 状态",
        "nav_settings" to "设置",

        // Ticker & Metrics
        "spot" to "即期价格",
        "forward_1m" to "1个月",
        "forward_3m" to "3个月",
        "forward_6m" to "6个月",
        "forward_12m" to "12个月",
        "volatility" to "波动率",
        "utilization" to "利用率",
        "available_capacity" to "可用容量",
        "demand" to "需求",
        "supply" to "供应",
        "price_hour" to "/ GPU小时",
        "live_feed" to "实时数据",
        "contango" to "正价差 (远期升水)",
        "backwardation" to "逆价差 (现货升水)",

        // Strike & Options
        "strike_title" to "执行价格与算力期权",
        "select_gpu" to "选择 GPU",
        "select_region" to "选择地区",
        "select_maturity" to "到期时间",
        "contract_size" to "合同规模 (GPU小时)",
        "strike_price" to "执行价格",
        "current_spot" to "当前价格",
        "implied_vol" to "隐含波动率",
        "expected_price" to "预计价格",
        "total_premium" to "期权价值",
        "probability_profit" to "盈利概率",
        "breakeven" to "损益平衡点",
        "delta" to "Delta",
        "gamma" to "Gamma",
        "vega" to "Vega",
        "theta" to "Theta",
        "rho" to "Rho",

        // Chart Toggles
        "chart_price" to "价格与回报",
        "chart_iv" to "隐含波动率",
        "chart_prob" to "概率分布",
        "chart_greeks" to "Greeks 希腊值",

        // Capacity & Datacenters
        "global_compute_power" to "全球计算力与基础设施",
        "total_capacity" to "总容量",
        "running_active" to "运行中",
        "available_idle" to "可用",
        "queue_wait" to "排队时间",
        "power_draw" to "功率 (MW)",
        "pue_ratio" to "PUE 能效比",
        "inspect_node" to "查看节点详情",

        // Cost Breakdown
        "cost_analysis_title" to "GPU 每小时真实经济成本分析",
        "real_economic_cost" to "真实经济成本",
        "spot_margin" to "即期市场毛利",
        "hardware_capex" to "GPU 硬件成本折旧",
        "electricity_power" to "电力",
        "liquid_cooling" to "冷却",
        "datacenter_colo" to "数据中心",
        "network_fabric" to "网络",
        "maintenance_sla" to "维护与 SLA",
        "ops_overhead" to "运营",

        // Simulation
        "simulation_title" to "宏观算力情景模拟",
        "run_simulation" to "运行模拟",
        "gpu_supply_shock" to "GPU 供应变化",
        "ai_demand_growth" to "AI 需求变化",
        "power_price_delta" to "电力价格变化",
        "dc_capacity_growth" to "数据中心增长",
        "utilization_swing" to "利用率变化",
        "perf_boost" to "GPU 性能提升",
        "sim_results" to "即期与远期价格变化",
        "shortage_risk" to "短缺风险指数",

        // Command Palette & AI
        "command_search_placeholder" to "输入命令、GPU、数据中心或界面 (Ctrl+K)...",
        "ai_assistant_title" to "NVIDIA 算力市场 AI 量化终端",
        "ask_ai" to "向量化助理提问...",
        "ai_chip_h100_spike" to "为什么 H100 今天上涨？",
        "ai_chip_b200_fwd" to "显示 B200 一年远期曲线。",
        "ai_chip_strike_calc" to "计算 H100 的执行价格。",
        "ai_chip_cheapest_dc" to "哪些地区计算力最便宜？",
        "ai_chip_h100_b200_cost" to "比较 H100 和 B200 的经济成本。",

        // Status & Settings
        "api_status" to "API 状态",
        "rest_api" to "REST API",
        "websocket_stream" to "WebSocket",
        "market_feed" to "市场数据",
        "capacity_feed" to "容量数据",
        "forecast_feed" to "预测服务",
        "normal" to "正常",
        "connecting" to "连接中……",
        "reconnecting" to "实时数据连接已断开，正在重新连接……",
        "disconnected" to "已断开",
        "language_select" to "界面语言",
        "currency_select" to "计价货币",
        "refresh_interval" to "刷新频率",
        "theme_customization" to "终端主题"
    )
}
