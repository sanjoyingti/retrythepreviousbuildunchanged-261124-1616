package com.aibuilt.retrythepreviousbuildunchanged.model

enum class SortOrder {
    RECENT,
    NAME,
    STATUS;

    val label: String
        get() = when (this) {
            RECENT -> "Recently updated"
            NAME -> "Name"
            STATUS -> "Status"
        }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    val label: String
        get() = when (this) {
            SYSTEM -> "Follow system"
            LIGHT -> "Light"
            DARK -> "Dark"
        }
}

data class AppSettings(
    val sortOrder: SortOrder = SortOrder.RECENT,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultMaxAttempts: Int = 3,
    val showResolved: Boolean = true
)
