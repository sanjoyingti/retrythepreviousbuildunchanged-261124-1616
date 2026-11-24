package com.aibuilt.retrythepreviousbuildunchanged.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aibuilt.retrythepreviousbuildunchanged.model.AttemptEntry
import com.aibuilt.retrythepreviousbuildunchanged.model.BuildRecord
import com.aibuilt.retrythepreviousbuildunchanged.ui.components.StatusBadge
import com.aibuilt.retrythepreviousbuildunchanged.ui.components.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    record: BuildRecord,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onLogFailed: () -> Unit,
    onLogSuccess: () -> Unit,
    onReset: () -> Unit,
    onAbandon: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by rememberSaveable(record.id) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = record.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit record")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(record.status)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Attempt ${record.attempts} of ${record.maxAttempts}",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        LinearProgressIndicator(
                            progress = { record.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        if (record.project.isNotBlank()) {
                            Text(
                                text = record.project,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (record.notes.isNotBlank()) {
                            Text(
                                text = record.notes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text(
                            text = "Started ${formatTimestamp(record.createdAt)} · last change ${formatTimestamp(record.updatedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onLogFailed,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Still failing")
                        }
                        Button(
                            onClick = onLogSuccess,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("It worked")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onReset,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back to waiting")
                        }
                        OutlinedButton(
                            onClick = onAbandon,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Give up")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Attempt history",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (record.history.isEmpty()) {
                item {
                    Text(
                        text = "No tries logged yet. Use the buttons above after your next build.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(record.history.sortedByDescending { it.timestamp }) { entry ->
                    HistoryRow(entry = entry)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete this record")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this record?") },
            text = { Text("The record and its whole attempt history will be removed from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HistoryRow(entry: AttemptEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        StatusBadge(entry.status)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatTimestamp(entry.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (entry.note.isNotBlank()) {
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
