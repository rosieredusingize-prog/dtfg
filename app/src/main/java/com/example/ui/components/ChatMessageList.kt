package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assistant
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.theme.BubbleAssistant
import com.example.ui.theme.BubbleUser
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.DTFGBorder
import com.example.ui.theme.DTFGCyan
import com.example.ui.theme.DTFGDarkBg
import com.example.ui.theme.DTFGTextMuted
import com.example.ui.theme.DTFGTextPrimary
import com.example.ui.theme.DTFGTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    assistant: Assistant,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom whenever new message arrives or generation state toggles
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(messages, key = { it.id.toString() + "_" + it.timestamp }) { message ->
            MessageRow(
                message = message,
                assistant = assistant
            )
        }

        if (isGenerating) {
            item {
                GeneratingIndicator(assistant = assistant)
            }
        }
    }
}

@Composable
private fun MessageRow(
    message: ChatMessage,
    assistant: Assistant
) {
    val isUser = message.sender == MessageSender.USER
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "user_message_item" else "assistant_message_item"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Assistant Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DTFGDarkBg)
                    .border(1.dp, assistant.primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAssistantIcon(assistant.id),
                    contentDescription = assistant.name,
                    tint = assistant.primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message Content Bubble
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) BubbleUser else BubbleAssistant)
                    .border(
                        width = 1.dp,
                        color = if (isUser) DTFGCyan.copy(alpha = 0.5f) else DTFGBorder,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (!isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = assistant.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = assistant.primaryColor
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("DTFG AI Message", message.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = DTFGTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Render formatted text (supports code blocks and paragraphs)
                    FormattedMessageContent(text = message.content)

                    if (!isUser && message.engineNote != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚡ ${message.engineNote}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DTFGTextMuted
                        )
                    }
                }
            }

            // Timestamp
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = DTFGTextMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DTFGCyan.copy(alpha = 0.2f))
                    .border(1.dp, DTFGCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Visitor",
                    tint = DTFGCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FormattedMessageContent(text: String) {
    // Basic parser for code fences ```code```
    val parts = text.split("```")
    for (i in parts.indices) {
        val segment = parts[i]
        if (i % 2 == 1) {
            // Code block
            val lines = segment.trim().lines()
            val language = if (lines.isNotEmpty() && lines[0].matches(Regex("^[a-zA-Z0-9#+]+$"))) lines[0] else ""
            val codeBody = if (language.isNotEmpty()) lines.drop(1).joinToString("\n") else segment.trim()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CodeBlockBackground)
                    .border(1.dp, DTFGBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    if (language.isNotEmpty()) {
                        Text(
                            text = language.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = DTFGCyan,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        Text(
                            text = codeBody,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = DTFGTextPrimary
                        )
                    }
                }
            }
        } else {
            // Standard narrative text
            if (segment.isNotBlank()) {
                Text(
                    text = segment.trim(),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = DTFGTextPrimary
                )
            }
        }
    }
}

@Composable
private fun GeneratingIndicator(assistant: Assistant) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(DTFGDarkBg)
                .border(1.dp, assistant.primaryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = assistant.primaryColor,
                strokeWidth = 2.dp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BubbleAssistant)
                .border(1.dp, assistant.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${assistant.name} is thinking...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DTFGTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
