package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.models.AppLanguage
import com.example.presentation.AiMessage
import com.example.ui.theme.*
import com.example.utils.Localization
import kotlinx.coroutines.launch

@Composable
fun AiTerminalDialog(
    messages: List<AiMessage>,
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestedPrompts = remember(currentLanguage) {
        when (currentLanguage) {
            AppLanguage.ENGLISH -> listOf(
                "Why did H100 spot price spike?",
                "Analyze Blackwell B200 forward curve",
                "Where is the cheapest datacenter for training?",
                "Compare LCOC between H100 and B200"
            )
            AppLanguage.TAIWANESE_HOKKIEN -> listOf(
                "按怎 H100 現貨價會大起價？",
                "分析 Blackwell B200 遠期曲線逆價差",
                "全世界上便宜的訓練機房佇佗位？",
                "比較 H100 佮 B200 算力平準化真實成本"
            )
            AppLanguage.CHINESE_MANDARIN -> listOf(
                "为什么 H100 即期价格出现异动？",
                "分析 Blackwell B200 远期曲线与基差",
                "全球最具成本效益的数据中心节点在哪里？",
                "对比 H100 与 B200 的真实平准化算力成本 (LCOC)"
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(12.dp)),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NvidiaGreenDim)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NvidiaGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "NVIDIA QUANT AI COPILOT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Black-76 Greeks & Datacenter Telemetry Integration",
                                style = MaterialTheme.typography.labelSmall,
                                color = NvidiaGreenGlow
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                HorizontalDivider(color = TerminalBorder)

                // Message Thread
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                if (!msg.isUser) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NvidiaGreenGlow)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = msg.sender,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (msg.isUser) FinancialCyan else NvidiaGreenGlow,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (msg.isUser) TerminalSurfaceCard else TerminalSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (msg.isUser) FinancialCyan.copy(alpha = 0.5f) else TerminalBorder
                                ),
                                modifier = Modifier.widthIn(max = 340.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = NvidiaGreenGlow,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Analyzing quant options surfaces & compute logs...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Suggested Prompt Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalBackground)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestedPrompts.forEach { prompt ->
                        Surface(
                            onClick = { onSendMessage(prompt) },
                            shape = RoundedCornerShape(12.dp),
                            color = TerminalSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NvidiaGreenDim)
                        ) {
                            Text(
                                text = prompt,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(color = TerminalBorder)

                // Input Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = {
                            Text(
                                "Ask NVIDIA Quant Copilot...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = TerminalSurface,
                            unfocusedContainerColor = TerminalSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NvidiaGreenGlow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (promptInput.isNotBlank()) {
                                onSendMessage(promptInput)
                                promptInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NvidiaGreen)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}
