package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Assistant
import com.example.ui.theme.DTFGBorder
import com.example.ui.theme.DTFGCyan
import com.example.ui.theme.DTFGEmerald
import com.example.ui.theme.DTFGSurface
import com.example.ui.theme.DTFGTextMuted
import com.example.ui.theme.DTFGTextPrimary

@Composable
fun DTFGTopBar(
    currentAssistant: Assistant,
    historyCount: Int,
    onOpenHistory: () -> Unit,
    onOpenInfo: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DTFGSurface,
                        DTFGSurface.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = DTFGBorder,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App / Hub Logo
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0B132B))
                    .border(1.5.dp, DTFGCyan.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dtfg_hub_logo),
                    contentDescription = "DTFG World Hub Logo",
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Status
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DTFG WORLD HUB",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = DTFGTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DTFGCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "AI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = DTFGCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(DTFGEmerald)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "7 Specialized Assistants • Public Access",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = DTFGTextMuted
                    )
                }
            }

            // Action: Clear Chat for current assistant
            IconButton(
                onClick = onClearChat,
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Current Chat",
                    tint = DTFGTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Action: History
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("history_button")
            ) {
                if (historyCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = DTFGCyan,
                                contentColor = Color(0xFF041021)
                            ) {
                                Text(
                                    text = if (historyCount > 99) "99+" else historyCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Chat History",
                            tint = DTFGTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Chat History",
                        tint = DTFGTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Action: Info / About DTFG
            IconButton(
                onClick = onOpenInfo,
                modifier = Modifier.testTag("info_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About DTFG World Hub",
                    tint = DTFGCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
