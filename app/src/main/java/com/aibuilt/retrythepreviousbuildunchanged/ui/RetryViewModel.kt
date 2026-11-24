package com.aibuilt.retrythepreviousbuildunchanged.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aibuilt.retrythepreviousbuildunchanged.data.LoadState
import com.aibuilt.retrythepreviousbuildunchanged.data.RetryRepository
import com.aibuilt.retrythepreviousbuildunchanged.data.SettingsRepository
import com.aibuilt.retrythepreviousbuildunchanged.model.AppSettings
import com.aibuilt.retrythepreviousbuildunchanged.model.BuildRecord
import com.aibuilt.retrythepreviousbuildunchanged.model.RetryStatus
import com.aibuilt.retrythepreviousbuildunchanged.model.SortOrder
import com.aibuilt.retrythepreviousbuildunchanged.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RetryUiState(
    val records: List<BuildRecord> = emptyList(),
    val allRecords: List<BuildRecord> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val loadState: LoadState = LoadState.Loading,
    val query: String = "",
    val statusFilter: RetryStatus? = null,
    val error: String? = null,
    val hiddenResolvedCount: Int = 0
) {
    val activeCount: Int get() = allRecords.count { !it.isResolved }
    val totalCount: Int get() = allRecords.size
    val hasNoRecords: Boolean get() = loadState == LoadState.Ready && allRecords.isEmpty()
    val hasNoMatches: Boolean
        get() = loadState == LoadState.Ready && allRecords.isNotEmpty() && records.isEmpty()
}

class RetryViewModel(
    private val repository: RetryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<RetryStatus?>(null)

    private val dataState = combine(
        repository.records,
        repository.state,
        settingsRepository.settings,
        query,
        statusFilter
    ) { records, loadState, settings, searchText, filter ->
        val matching = records.filter { record ->
            val matchesQuery = searchText.isBlank() ||
                record.name.contains(searchText, ignoreCase = true) ||
                record.project.contains(searchText, ignoreCase = true) ||
                record.notes.contains(searchText, ignoreCase = true)
            val matchesFilter = filter == null || record.status == filter
            val matchesVisibility = settings.showResolved || !record.isResolved
            matchesQuery && matchesFilter && matchesVisibility
        }
        val sorted = when (settings.sortOrder) {
            SortOrder.RECENT -> matching.sortedByDescending { it.updatedAt }
            SortOrder.NAME -> matching.sortedWith(
                compareBy({ it.name.lowercase() }, { it.project.lowercase() })
            )
            SortOrder.STATUS -> matching.sortedWith(
                compareBy<BuildRecord> { it.status.ordinal }.thenByDescending { it.updatedAt }
            )
        }
        RetryUiState(
            records = sorted,
            allRecords = records,
            settings = settings,
            loadState = loadState,
            query = searchText,
            statusFilter = filter,
            hiddenResolvedCount = if (settings.showResolved) 0 else records.count { it.isResolved }
        )
    }

    val uiState: StateFlow<RetryUiState> = combine(dataState, repository.errors) { state, error ->
        state.copy(error = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RetryUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { repository.load() }
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setStatusFilter(value: RetryStatus?) {
        statusFilter.value = value
    }

    fun saveRecord(
        existing: BuildRecord?,
        name: String,
        project: String,
        notes: String,
        maxAttempts: Int
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val record = if (existing == null) {
                BuildRecord(
                    name = cleanName,
                    project = project.trim(),
                    notes = notes.trim(),
                    status = RetryStatus.PENDING,
                    attempts = 0,
                    maxAttempts = maxAttempts.coerceIn(1, 25),
                    createdAt = now,
                    updatedAt = now
                )
            } else {
                existing.copy(
                    name = cleanName,
                    project = project.trim(),
                    notes = notes.trim(),
                    maxAttempts = maxAttempts.coerceIn(1, 25),
                    updatedAt = now
                )
            }
            repository.upsert(record)
        }
    }

    fun logAttempt(id: String, status: RetryStatus, note: String = "") {
        viewModelScope.launch {
            val record = repository.records.value.firstOrNull { it.id == id } ?: return@launch
            repository.upsert(record.withAttempt(status, note.trim()))
        }
    }

    fun changeStatus(id: String, status: RetryStatus) {
        viewModelScope.launch {
            val record = repository.records.value.firstOrNull { it.id == id } ?: return@launch
            val updated = if (status == RetryStatus.PENDING) {
                record.copy(
                    status = RetryStatus.PENDING,
                    maxAttempts = maxOf(record.maxAttempts, record.attempts + 1),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                record.withStatus(status)
            }
            repository.upsert(updated)
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun deleteEverything() {
        viewModelScope.launch { repository.deleteAll() }
    }

    fun clearError() {
        repository.clearError()
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch { settingsRepository.setSortOrder(order) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDefaultMaxAttempts(value: Int) {
        viewModelScope.launch { settingsRepository.setDefaultMaxAttempts(value) }
    }

    fun setShowResolved(value: Boolean) {
        viewModelScope.launch { settingsRepository.setShowResolved(value) }
    }

    companion object {
        fun factory(
            retryRepository: RetryRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { RetryViewModel(retryRepository, settingsRepository) }
        }
    }
}
