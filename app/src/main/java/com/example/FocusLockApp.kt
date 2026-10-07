package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.FocusLockDatabase
import com.example.data.repository.FocusLockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusLockApp : Application() {

    lateinit var database: FocusLockDatabase
        private set
    lateinit var repository: FocusLockRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = FocusLockDatabase.getDatabase(this)
        repository = FocusLockRepository(database)

        createNotificationChannels()

        // Seed initial data asynchronously if empty
        CoroutineScope(Dispatchers.IO).launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusChannel = NotificationChannel(
                CHANNEL_ACTIVE_SESSION,
                "Active Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live remaining time during active student study sessions"
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "FocusLock Alerts & Approvals",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for early end requests and session completions"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(focusChannel)
            manager?.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_ACTIVE_SESSION = "focuslock_active_session_channel"
        const val CHANNEL_ALERTS = "focuslock_alerts_channel"
    }
}
