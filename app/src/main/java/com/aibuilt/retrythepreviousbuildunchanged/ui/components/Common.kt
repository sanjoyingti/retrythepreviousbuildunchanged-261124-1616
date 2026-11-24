package com.aibuilt.retrythepreviousbuildunchanged.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aibuilt.retrythepreviousbuildunchanged.model.RetryStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class StatusPalette(val container: Color, val content: Color)

@Composable
fun statusPalette(status: RetryStatus): StatusPalette {
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        RetryStatus.PENDING -> StatusPalette(scheme.surfaceVariant, scheme.onSurfaceVariant)
        RetryStatus.RUNNING -> StatusPalette(scheme.tertiaryContainer, scheme.onTertiaryContainer)
        RetryStatus.FAILED -> StatusPalette(scheme.errorContainer, scheme.onErrorContainer)
        RetryStatus.SUCCEEDED -> StatusPalette(scheme.secondaryContainer, scheme.onSecondaryContainer)
        RetryStatus.ABANDONED -> StatusPalette(scheme.outlineVariant, scheme.onSurfaceVariant)
    }
}

@Composable
fun StatusBadge(status: RetryStatus, modifier: Modifier = Modifier) {
    val palette = statusPalette(status)
    Box(
        modifier = modifier
            .background(palette.container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.content
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun LoadingState(message: String = "Loading your records…", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 1..25
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            enabled = value > range.first
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Fewer attempts")
        }
        Box(
            modifier = Modifier.widthIn(min = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.titleMedium)
        }
        FilledTonalIconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            enabled = value < range.last
        ) {
            Icon(Icons.Filled.Add, contentDescription = "More attempts")
        }
    }
}

private val timestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

fun formatTimestamp(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(millis).atZone(zone).format(timestampFormatter)

fun formatRelative(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - millis
    if (diff < 60_000L) return "just now"
    val minutes = diff / 60_000L
    if (minutes < 60L) return "${minutes}m ago"
    val hours = minutes / 60L
    if (hours < 24L) return "${hours}h ago"
    val days = hours / 24L
    if (days < 7L) return "${days}d ago"
    return formatTimestamp(millis)
}
