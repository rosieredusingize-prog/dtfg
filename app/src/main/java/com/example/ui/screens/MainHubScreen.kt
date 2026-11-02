package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AssistantSelectorBar
import com.example.ui.components.AssistantWelcomeView
import com.example.ui.components.ChatHistoryModal
import com.example.ui.components.ChatInputField
import com.example.ui.components.ChatMessageList
import com.example.ui.components.DTFGTopBar
import com.example.ui.components.HubInfoDialog
import com.example.ui.theme.DTFGDarkBg
import com.example.ui.theme.DTFGSurfaceVariant
import com.example.ui.viewmodel.DTFGViewModel

@Composable
fun MainHubScreen(
    viewModel: DTFGViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val selectedAssistant by viewModel.selectedAssistant.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val allHistory by viewModel.allHistory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showHistoryModal by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DTFGDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DTFGTopBar(
                currentAssistant = selectedAssistant,
                historyCount = allHistory.size,
                onOpenHistory = { showHistoryModal = true },
                onOpenInfo = { showInfoDialog = true },
                onClearChat = { viewModel.clearCurrentAssistantHistory() }
            )
        },
        bottomBar = {
            ChatInputField(
                assistant = selectedAssistant,
                text = inputText,
                onTextChange = viewModel::onInputChange,
                onSend = { viewModel.sendMessage() },
                isGenerating = isGenerating
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Assistant Selector Tabs
            AssistantSelectorBar(
                assistants = viewModel.assistants,
                selectedAssistant = selectedAssistant,
                onSelectAssistant = viewModel::selectAssistant
            )

            // Error banner if any error occurred
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B151E))
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFFFFCDD2),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = viewModel::dismissError,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = Color(0xFFFFCDD2),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (messages.isEmpty() && !isGenerating) {
                    AssistantWelcomeView(
                        assistant = selectedAssistant,
                        onSelectPrompt = { prompt ->
                            viewModel.sendMessage(promptOverride = prompt)
                        }
                    )
                } else {
                    ChatMessageList(
                        messages = messages,
                        assistant = selectedAssistant,
                        isGenerating = isGenerating
                    )
                }
            }
        }
    }

    // Chat History Modal
    if (showHistoryModal) {
        ChatHistoryModal(
            historyMessages = allHistory,
            searchQuery = searchQuery,
            onSearchChange = viewModel::onSearchQueryChange,
            onSelectAssistant = { assistantId ->
                viewModel.selectAssistant(assistantId)
                showHistoryModal = false
            },
            onDeleteMessage = viewModel::deleteMessage,
            onClearAllHistory = viewModel::clearAllHistory,
            onDismiss = {
                showHistoryModal = false
                viewModel.onSearchQueryChange("")
            }
        )
    }

    // Hub Info Dialog
    if (showInfoDialog) {
        HubInfoDialog(
            onDismiss = { showInfoDialog = false }
        )
    }
}
