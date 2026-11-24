package com.aibuilt.retrythepreviousbuildunchanged.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aibuilt.retrythepreviousbuildunchanged.data.LoadState
import com.aibuilt.retrythepreviousbuildunchanged.model.RetryStatus
import com.aibuilt.retrythepreviousbuildunchanged.ui.screens.HomeScreen
import com.aibuilt.retrythepreviousbuildunchanged.ui.screens.RecordDetailScreen
import com.aibuilt.retrythepreviousbuildunchanged.ui.screens.RecordEditScreen
import com.aibuilt.retrythepreviousbuildunchanged.ui.screens.SettingsScreen
import com.aibuilt.retrythepreviousbuildunchanged.ui.theme.RetryLogTheme

private object Routes {
    const val HOME = "home"
    const val EDIT_NEW = "edit"
    const val EDIT_EXISTING = "edit/{recordId}"
    const val DETAIL = "detail/{recordId}"
    const val SETTINGS = "settings"

    fun editExisting(id: String): String = "edit/$id"
    fun detail(id: String): String = "detail/$id"
}

@Composable
fun RetryApp(viewModel: RetryViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RetryLogTheme(themeMode = state.settings.themeMode) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = Routes.HOME) {

            composable(Routes.HOME) {
                HomeScreen(
                    state = state,
                    onRecordClick = { id -> navController.navigate(Routes.detail(id)) },
                    onAddClick = { navController.navigate(Routes.EDIT_NEW) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onQueryChange = viewModel::setQuery,
                    onFilterChange = viewModel::setStatusFilter,
                    onRetryLoad = viewModel::refresh,
                    onErrorShown = viewModel::clearError,
                    onLogFailed = { id -> viewModel.logAttempt(id, RetryStatus.FAILED) },
                    onLogSuccess = { id -> viewModel.logAttempt(id, RetryStatus.SUCCEEDED) },
                    onShowResolved = { viewModel.setShowResolved(true) }
                )
            }

            composable(Routes.EDIT_NEW) {
                RecordEditScreen(
                    recordId = null,
                    settings = state.settings,
                    existing = null,
                    onSave = { name, project, notes, maxAttempts ->
                        viewModel.saveRecord(
                            existing = null,
                            name = name,
                            project = project,
                            notes = notes,
                            maxAttempts = maxAttempts
                        )
                        navController.popBackStack()
                    },
                    onDelete = null,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EDIT_EXISTING,
                arguments = listOf(navArgument("recordId") { type = NavType.StringType })
            ) { entry ->
                val recordId = entry.arguments?.getString("recordId").orEmpty()
                val existing = state.allRecords.firstOrNull { it.id == recordId }

                RecordEditScreen(
                    recordId = recordId,
                    settings = state.settings,
                    existing = existing,
                    onSave = { name, project, notes, maxAttempts ->
                        viewModel.saveRecord(
                            existing = existing,
                            name = name,
                            project = project,
                            notes = notes,
                            maxAttempts = maxAttempts
                        )
                        navController.popBackStack()
                    },
                    onDelete = existing?.let {
                        {
                            viewModel.deleteRecord(it.id)
                            navController.popBackStack(Routes.HOME, inclusive = false)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("recordId") { type = NavType.StringType })
            ) { entry ->
                val recordId = entry.arguments?.getString("recordId").orEmpty()
                val record = state.allRecords.firstOrNull { it.id == recordId }

                if (record == null) {
                    LaunchedEffect(recordId, state.loadState) {
                        if (state.loadState == LoadState.Ready) {
                            navController.popBackStack(Routes.HOME, inclusive = false)
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    RecordDetailScreen(
                        record = record,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(Routes.editExisting(record.id)) },
                        onLogFailed = { viewModel.logAttempt(record.id, RetryStatus.FAILED) },
                        onLogSuccess = { viewModel.logAttempt(record.id, RetryStatus.SUCCEEDED) },
                        onReset = { viewModel.changeStatus(record.id, RetryStatus.PENDING) },
                        onAbandon = { viewModel.changeStatus(record.id, RetryStatus.ABANDONED) },
                        onDelete = { viewModel.deleteRecord(record.id) }
                    )
                }
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    settings = state.settings,
                    totalRecords = state.totalCount,
                    activeRecords = state.activeCount,
                    onBack = { navController.popBackStack() },
                    onThemeChange = viewModel::setThemeMode,
                    onSortChange = viewModel::setSortOrder,
                    onDefaultAttemptsChange = viewModel::setDefaultMaxAttempts,
                    onShowResolvedChange = viewModel::setShowResolved,
                    onDeleteAll = { viewModel.deleteEverything() }
                )
            }
        }
    }
}
