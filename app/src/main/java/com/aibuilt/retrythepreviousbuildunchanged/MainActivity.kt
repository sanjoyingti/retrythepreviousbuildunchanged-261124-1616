package com.aibuilt.retrythepreviousbuildunchanged

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.retrythepreviousbuildunchanged.ui.RetryApp
import com.aibuilt.retrythepreviousbuildunchanged.ui.RetryViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as RetryApplication).container

        setContent {
            val factory = remember(container) {
                RetryViewModel.factory(
                    retryRepository = container.retryRepository,
                    settingsRepository = container.settingsRepository
                )
            }
            val retryViewModel: RetryViewModel = viewModel(factory = factory)
            RetryApp(viewModel = retryViewModel)
        }
    }
}
