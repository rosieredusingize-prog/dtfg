package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assistant
import com.example.data.model.AssistantId
import com.example.data.model.ChatMessage
import com.example.data.model.DTFGAssistants
import com.example.data.model.MessageSender
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DTFGBorder
import com.example.ui.theme.DTFGCyan
import com.example.ui.theme.DTFGDarkBg
import com.example.ui.theme.DTFGSurface
import com.example.ui.theme.DTFGSurfaceVariant
import com.example.ui.theme.DTFGTextMuted
import com.example.ui.theme.DTFGTextPrimary
import com.example.ui.theme.DTFGTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryModal(
    historyMessages: List<ChatMessage>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectAssistant: (AssistantId) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedFilterId by remember { mutableStateOf<AssistantId?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredMessages = remember(historyMessages, selectedFilterId) {
        if (selectedFilterId == null) historyMessages
        else historyMessages.filter { it.assistantId == selectedFilterId }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DTFGSurface,
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DTFG Chat History",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DTFGTextPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                if (historyMessages.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("clear_all_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Clear all history",
                            tint = Color(0xFFFF5252)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_history_modal")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DTFGTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text("Search past questions and insights...", color = DTFGTextMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = DTFGCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = DTFGTextMuted
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DTFGCyan,
                    unfocusedBorderColor = DTFGBorder,
                    focusedTextColor = DTFGTextPrimary,
                    unfocusedTextColor = DTFGTextPrimary,
                    focusedContainerColor = DTFGDarkBg,
                    unfocusedContainerColor = DTFGDarkBg
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Assistant filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilterId == null,
                        onClick = { selectedFilterId = null },
                        label = { Text("All (${historyMessages.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DTFGCyan,
                            selectedLabelColor = Color(0xFF041021),
                            containerColor = DTFGSurfaceVariant,
                            labelColor = DTFGTextSecondary
                        )
                    )
                }

                items(DTFGAssistants.all) { assistant ->
                    val count = historyMessages.count { it.assistantId == assistant.id }
                    FilterChip(
                        selected = selectedFilterId == assistant.id,
                        onClick = {
                            selectedFilterId = if (selectedFilterId == assistant.id) null else assistant.id
                        },
                        label = { Text("${assistant.shortName} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = assistant.primaryColor,
                            selectedLabelColor = Color(0xFF041021),
                            containerColor = DTFGSurfaceVariant,
                            labelColor = DTFGTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // History Message List
            if (filteredMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No messages matching \"$searchQuery\""
                            else "No chat history yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = DTFGTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start a conversation with any DTFG Assistant to see records here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DTFGTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        val assistant = DTFGAssistants.get(msg.assistantId)
                        HistoryMessageCard(
                            message = msg,
                            assistant = assistant,
                            onClick = {
                                onSelectAssistant(msg.assistantId)
                                onDismiss()
                            },
                            onDelete = { onDeleteMessage(msg.id) }
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Clear All Chat History?",
                    color = DTFGTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will erase all recorded conversations across all 7 DTFG assistants from this device. This action cannot be undone.",
                    color = DTFGTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = DTFGTextMuted)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun HistoryMessageCard(
    message: ChatMessage,
    assistant: Assistant,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DTFGDarkBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DTFGBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(assistant.primaryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAssistantIcon(assistant.id),
                        contentDescription = assistant.name,
                        tint = assistant.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = assistant.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = assistant.primaryColor
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isUser) DTFGCyan.copy(alpha = 0.15f) else DTFGSurfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (isUser) "PROMPT" else "RESPONSE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (isUser) DTFGCyan else DTFGTextSecondary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = formatHistoryDate(message.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = DTFGTextMuted
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = DTFGTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                color = DTFGTextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatHistoryDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
