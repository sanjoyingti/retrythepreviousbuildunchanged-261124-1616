package com.aibuilt.retrythepreviousbuildunchanged

import android.app.Application
import android.content.Context
import com.aibuilt.retrythepreviousbuildunchanged.data.RetryRepository
import com.aibuilt.retrythepreviousbuildunchanged.data.SettingsRepository
import java.io.File

class AppContainer(context: Context) {
    private val appContext: Context = context.applicationContext

    val retryRepository: RetryRepository = RetryRepository(File(appContext.filesDir, "retry_records.json"))

    val settingsRepository: SettingsRepository = SettingsRepository(appContext)
}

class RetryApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
