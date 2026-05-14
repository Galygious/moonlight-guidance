package org.arcanaforge.app

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MoonlightGuidanceApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        applicationScope.launch {
            runCatching {
                container.databaseSeeder.seedIfNeeded()
            }.onFailure { throwable ->
                Log.e(LOG_TAG, "Initial database seed failed.", throwable)
            }
        }
    }

    private companion object {
        const val LOG_TAG = "MoonlightGuidance"
    }
}
