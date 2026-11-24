package com.aibuilt.retrythepreviousbuildunchanged.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.aibuilt.retrythepreviousbuildunchanged.model.AppSettings
import com.aibuilt.retrythepreviousbuildunchanged.model.BuildRecord
import com.aibuilt.retrythepreviousbuildunchanged.ui.components.NumberStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEditScreen(
    recordId: String?,
    settings: AppSettings,
    existing: BuildRecord?,
    onSave: (name: String, project: String, notes: String, maxAttempts: Int) -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit
) {
    if (recordId != null && existing == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Record missing") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "This record is gone",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "It was deleted, so there is nothing left to edit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Go back") }
            }
        }
        return
    }

    var name by rememberSaveable(recordId) { mutableStateOf(existing?.name ?: "") }
    var project by rememberSaveable(recordId) { mutableStateOf(existing?.project ?: "") }
    var notes by rememberSaveable(recordId) { mutableStateOf(existing?.notes ?: "") }
    var maxAttempts by rememberSaveable(recordId) {
        mutableStateOf(existing?.maxAttempts ?: settings.defaultMaxAttempts)
    }
    var nameTouched by rememberSaveable(recordId) { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable(recordId) { mutableStateOf(false) }

    val nameIsEmpty = name.isBlank()
    val showNameError = nameTouched && nameIsEmpty

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "New retry record" else "Edit record") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameTouched = true
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("What keeps failing?") },
                placeholder = { Text("For example: Login screen crashes") },
                singleLine = true,
                isError = showNameError,
                supportingText = {
                    Text(
                        if (showNameError) {
                            "Please type a short name"
                        } else {
                            "Required"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = project,
                onValueChange = { project = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Project or app (optional)") },
                placeholder = { Text("For example: Shopping app") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") },
                placeholder = { Text("Error message, what you already tried…") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            Column {
                Text(
                    text = "How many tries before giving up?",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Calls the limit shown on the progress bar. You can change it any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                NumberStepper(
                    value = maxAttempts,
                    onValueChange = { maxAttempts = it }
                )
            }

            Button(
                onClick = { onSave(name, project, notes, maxAttempts) },
                enabled = !nameIsEmpty,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing == null) "Save record" else "Save changes")
            }

            if (onDelete != null) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete this record")
                }
            }
        }
    }

    val deleteAction = onDelete
    if (showDeleteDialog && deleteAction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this record?") },
            text = { Text("The record and its whole attempt history will be removed from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteAction()
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
