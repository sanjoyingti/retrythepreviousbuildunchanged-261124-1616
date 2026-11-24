package com.aibuilt.retrythepreviousbuildunchanged.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class RetryStatus {
    PENDING,
    RUNNING,
    FAILED,
    SUCCEEDED,
    ABANDONED;

    val label: String
        get() = when (this) {
            PENDING -> "Waiting"
            RUNNING -> "Retrying"
            FAILED -> "Failed"
            SUCCEEDED -> "Worked"
            ABANDONED -> "Given up"
        }

    val isResolved: Boolean
        get() = this == SUCCEEDED || this == ABANDONED
}

@Serializable
data class AttemptEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val status: RetryStatus = RetryStatus.RUNNING,
    val note: String = ""
)

@Serializable
data class BuildRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val project: String = "",
    val notes: String = "",
    val status: RetryStatus = RetryStatus.PENDING,
    val attempts: Int = 0,
    val maxAttempts: Int = 3,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val history: List<AttemptEntry> = emptyList()
) {
    val progress: Float
        get() = if (maxAttempts <= 0) 0f else (attempts.toFloat() / maxAttempts.toFloat()).coerceIn(0f, 1f)

    val isResolved: Boolean
        get() = status.isResolved

    fun withAttempt(status: RetryStatus, note: String = ""): BuildRecord {
        val now = System.currentTimeMillis()
        return copy(
            status = status,
            attempts = attempts + 1,
            updatedAt = now,
            history = history + AttemptEntry(timestamp = now, status = status, note = note)
        )
    }

    fun withStatus(status: RetryStatus): BuildRecord =
        copy(status = status, updatedAt = System.currentTimeMillis())
}
