package com.aibuilt.retrythepreviousbuildunchanged.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aibuilt.retrythepreviousbuildunchanged.data.LoadState
import com.aibuilt.retrythepreviousbuildunchanged.model.RetryStatus
import com.aibuilt.retrythepreviousbuildunchanged.ui.RetryUiState
import com.aibuilt.retrythepreviousbuildunchanged.ui.components.EmptyState
import com.aibuilt.retrythepreviousbuildunchanged.ui.components.ErrorState
import com.aibuilt.retrythepreviousbuildunchanged.ui.components.LoadingState
import com.aibuilt.retrythepreviousbuildunchanged.ui.components.RecordCard

private val statusFilters: List<Pair<String, RetryStatus?>> = listOf(
    "All" to null,
    "Waiting" to RetryStatus.PENDING,
    "Retrying" to RetryStatus.RUNNING,
    "Failed" to RetryStatus.FAILED,
    "Worked" to RetryStatus.SUCCEEDED,
    "Given up" to RetryStatus.ABANDONED
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: RetryUiState,
    onRecordClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (RetryStatus?) -> Unit,
    onRetryLoad: () -> Unit,
    onErrorShown: () -> Unit,
    onLogFailed: (String) -> Unit,
    onLogSuccess: (String) -> Unit,
    onShowResolved: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val message = state.error
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Retry Log", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = if (state.activeCount == 0) {
                                "Nothing waiting for a retry"
                            } else {
                                "${state.activeCount} still needing a retry"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New record") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search name, project or note") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusFilters.forEach { (label, status) ->
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { onFilterChange(status) },
                        label = { Text(label) }
                    )
                }
            }

            if (!state.settings.showResolved && state.hiddenResolvedCount > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.hiddenResolvedCount} settled record(s) hidden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onShowResolved) { Text("Show") }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            when (val loadState = state.loadState) {
                LoadState.Loading -> LoadingState()

                is LoadState.Error -> ErrorState(message = loadState.message, onRetry = onRetryLoad)

                LoadState.Ready -> {
                    when {
                        state.hasNoRecords -> EmptyState(
                            icon = Icons.Filled.Inbox,
                            title = "No retries logged yet",
                            message = "Add the build that keeps failing and track every attempt until it works.",
                            actionLabel = "Add your first record",
                            onAction = onAddClick
                        )

                        state.hasNoMatches -> EmptyState(
                            icon = Icons.Filled.SearchOff,
                            title = "Nothing matches",
                            message = "Try a different search word or choose another status chip.",
                            actionLabel = "Clear filters",
                            onAction = {
                                onQueryChange("")
                                onFilterChange(null)
                            }
                        )

                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.records, key = { it.id }) { record ->
                                RecordCard(
                                    record = record,
                                    onClick = { onRecordClick(record.id) },
                                    onLogFailed = { onLogFailed(record.id) },
                                    onLogSuccess = { onLogSuccess(record.id) }
                                )
                            }
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${state.totalCount} record(s) saved on this device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
