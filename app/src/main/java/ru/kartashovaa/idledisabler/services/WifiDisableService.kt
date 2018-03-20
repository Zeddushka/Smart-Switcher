package ru.kartashovaa.idledisabler.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import org.jetbrains.anko.alarmManager
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.idledisabler.util.Text
import ru.kartashovaa.toolkit.Repositories
import ru.kartashovaa.toolkit.Component
import java.util.*


class WifiDisableService : IntentService("WifiDisableService") {

    override fun onHandleIntent(intent: Intent?) {
        if (Repositories.preference.settings.wifiDisableWhenIdle && Repositories.device.hasLocationPermission) {
            if (intent?.action == ACTION_CHECK_WIFI_IDLE) checkWifiIdle()
            else Log.e("Unknown action in WiFi disable service", Throwable(intent?.action), TAG)
        } else {
            Log.i("WiFi turn off function is disabled or permission revoked", TAG)
            cancelPendingIntents()
        }
    }

    private fun checkWifiIdle() {
        Log.d("checkWifiIdle", TAG)
        cancelPendingIntents()
        val device = Repositories.device
        val preference = Repositories.preference
        if (Repositories.device.isWifiEnabled) {
            if (device.isWifiConnected) {
                Log.i("WiFi is not idling", TAG)
                preference.wifiEnabled = System.currentTimeMillis()
            } else {
                val timeDiff = System.currentTimeMillis() - preference.wifiEnabled
                Log.i("WiFi is idling for ${timeDiff / 1000} seconds", TAG)
                if (timeDiff > preference.settings.wifiDisablePeriod * 1000) {
                    Log.i("Disable WIFI in case of idle", TAG)
                    preference.lastWifiScan = System.currentTimeMillis()
                    device.isWifiEnabled = false
                } else {
                    scheduleIntent()
                }
            }
        } else {
            Log.i("WiFi is already turned off", TAG)
        }
    }

    companion object {
        private const val TAG = "WifiDisableService"
        private const val ACTION_CHECK_WIFI_IDLE = "ru.kartashovaa.idledisabler.action.CHECK_WIFI_IDLE"
        private const val REQUEST_CODE = 0
        private val startIntent: Intent
        private val pendingIntent: PendingIntent

        init {
            val app = Component.applicationContext
            startIntent = Intent(app, WifiDisableService::class.java).apply { action = ACTION_CHECK_WIFI_IDLE }
            pendingIntent = PendingIntent.getService(app, REQUEST_CODE, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun startCommand() {
            val app = Component.applicationContext
            app.startService(startIntent)
        }

        fun cancelPendingIntents() {
            Log.d("Cancel idle check triggers", TAG)
            Component.applicationContext.alarmManager.cancel(pendingIntent)
        }

        fun scheduleIntent() {
            cancelPendingIntents()
            val timeDiff = System.currentTimeMillis() - Repositories.preference.wifiEnabled
            val wifiPeriod = Repositories.preference.settings.wifiDisablePeriod * 1000
            var triggerAtMills = System.currentTimeMillis() + wifiPeriod
            if (timeDiff in 0..wifiPeriod) triggerAtMills -= timeDiff
            val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMills }
            Log.i("WiFi idle check will trigger at ${Text.TIME_FORMAT.format(calendar.time)}", TAG)
            Component.applicationContext.alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMills, pendingIntent)
        }
    }
}
