package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assistant
import com.example.data.model.AssistantId
import com.example.ui.theme.DTFGBorder
import com.example.ui.theme.DTFGDarkBg
import com.example.ui.theme.DTFGSurfaceVariant
import com.example.ui.theme.DTFGTextMuted
import com.example.ui.theme.DTFGTextPrimary

@Composable
fun AssistantSelectorBar(
    assistants: List<Assistant>,
    selectedAssistant: Assistant,
    onSelectAssistant: (AssistantId) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DTFGDarkBg)
            .padding(vertical = 8.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(assistants, key = { it.id.key }) { assistant ->
                val isSelected = assistant.id == selectedAssistant.id
                AssistantChip(
                    assistant = assistant,
                    isSelected = isSelected,
                    onClick = { onSelectAssistant(assistant.id) }
                )
            }
        }
    }
}

@Composable
private fun AssistantChip(
    assistant: Assistant,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) assistant.primaryColor else DTFGBorder,
        label = "chipBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) DTFGSurfaceVariant else DTFGSurfaceVariant.copy(alpha = 0.4f),
        label = "chipBg"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .testTag("assistant_chip_${assistant.id.key}")
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) assistant.primaryColor.copy(alpha = 0.2f)
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getAssistantIcon(assistant.id),
                contentDescription = assistant.name,
                tint = if (isSelected) assistant.primaryColor else DTFGTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = assistant.shortName,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) DTFGTextPrimary else DTFGTextMuted
        )

        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(assistant.primaryColor)
            )
        }
    }
}

fun getAssistantIcon(id: AssistantId): ImageVector {
    return when (id) {
        AssistantId.GENERAL -> Icons.Default.AutoAwesome
        AssistantId.TECH -> Icons.Default.Code
        AssistantId.BUSINESS -> Icons.AutoMirrored.Filled.TrendingUp
        AssistantId.PROJECT -> Icons.Default.AccountTree
        AssistantId.RESEARCH -> Icons.Default.Biotech
        AssistantId.DATA -> Icons.Default.QueryStats
        AssistantId.LEARNING -> Icons.Default.School
    }
}
