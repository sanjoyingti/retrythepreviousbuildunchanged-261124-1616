package com.aibuilt.retrythepreviousbuildunchanged.data

import com.aibuilt.retrythepreviousbuildunchanged.model.AppJson
import com.aibuilt.retrythepreviousbuildunchanged.model.BuildRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import java.io.File

sealed interface LoadState {
    data object Loading : LoadState
    data object Ready : LoadState
    data class Error(val message: String) : LoadState
}

class RetryRepository(private val file: File) {

    private val mutex = Mutex()
    private val recordsSerializer = ListSerializer(BuildRecord.serializer())

    private val _records = MutableStateFlow<List<BuildRecord>>(emptyList())
    val records: StateFlow<List<BuildRecord>> = _records.asStateFlow()

    private val _state = MutableStateFlow<LoadState>(LoadState.Loading)
    val state: StateFlow<LoadState> = _state.asStateFlow()

    private val _errors = MutableStateFlow<String?>(null)
    val errors: StateFlow<String?> = _errors.asStateFlow()

    suspend fun load() {
        _state.value = LoadState.Loading
        val result = withContext(Dispatchers.IO) {
            runCatching {
                if (!file.exists()) {
                    emptyList()
                } else {
                    AppJson.decodeFromString(recordsSerializer, file.readText())
                }
            }
        }
        result
            .onSuccess { loaded ->
                _records.value = loaded.sortedByDescending { it.updatedAt }
                _state.value = LoadState.Ready
            }
            .onFailure { error ->
                _records.value = emptyList()
                _state.value = LoadState.Error(error.message ?: "Could not read your saved records")
            }
    }

    suspend fun upsert(record: BuildRecord) = mutate { current ->
        val index = current.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            current.toMutableList().also { it[index] = record }
        } else {
            current + record
        }
    }

    suspend fun delete(id: String) = mutate { current -> current.filterNot { it.id == id } }

    suspend fun deleteAll() = mutate { emptyList() }

    fun clearError() {
        _errors.value = null
    }

    private suspend fun mutate(transform: (List<BuildRecord>) -> List<BuildRecord>) {
        mutex.withLock {
            val updated = transform(_records.value)
            _records.value = updated
            val failure = withContext(Dispatchers.IO) {
                runCatching {
                    file.parentFile?.mkdirs()
                    val temp = File(file.parentFile, file.name + ".tmp")
                    temp.writeText(AppJson.encodeToString(recordsSerializer, updated))
                    if (file.exists() && !file.delete()) {
                        error("Could not replace the records file")
                    }
                    if (!temp.renameTo(file)) {
                        file.writeText(temp.readText())
                        temp.delete()
                    }
                }.exceptionOrNull()
            }
            _errors.value = failure?.let { "Could not save your changes: ${it.message ?: "unknown error"}" }
        }
    }
}
