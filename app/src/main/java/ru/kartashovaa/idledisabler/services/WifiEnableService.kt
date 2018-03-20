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


class WifiEnableService : IntentService("WifiEnableService") {

    override fun onHandleIntent(intent: Intent?) {
        if (Repositories.preference.settings.wifiEnableWhenSee && Repositories.device.hasLocationPermission) {
            when (intent?.action) {
                ACTION_CHECK_WIFI_KNOWN -> checkWifiSeen()
                ACTION_CHECK_WIFI_KNOWN_SCAN_FINISH -> checkScannedStatus()
                else -> Log.e("Unknown action in WiFi disable service", Throwable(intent?.action), TAG)
            }
        } else {
            Log.i("WiFi turn on function disabled or permission revoked", TAG)
            cancelAllPendingIntents()
        }
    }

    private fun checkWifiSeen() {
        Log.d("Check WiFi seen", TAG)
        cancelPendingStartIntents()
        val pref = Repositories.preference
        val device = Repositories.device
        if (!device.isWifiConnected) {
            val timeDiff = System.currentTimeMillis() - pref.lastWifiScan
            if (timeDiff < pref.settings.wifiEnablePeriod * 1000) {
                Log.i("WiFi scan was ${timeDiff / 1000} seconds ago, will not start scan", TAG)
                scheduleStartIntent()
            } else {
                Log.i("Enabling WiFi for scanning known networks", TAG)
                Repositories.preference.lastWifiScan = System.currentTimeMillis()
                device.isWifiEnabled = true
                scheduleScanDoneIntent()
            }
        } else {
            Log.i("WiFi already connected", TAG)
        }
    }

    private fun checkScannedStatus() {
        Log.d("Check WiFi scan result", TAG)
        val device = Repositories.device
        if (!device.isWifiConnected) {
            Log.i("WiFi is not connected in $WIFI_SEARCH_TIME seconds, turning off", TAG)
            device.isWifiEnabled = false
        } else {
            Log.i("WiFi is connected", TAG)
        }
    }

    companion object {
        private const val TAG = "WifiEnableService"
        private const val TAG_LOOKUP = "lookup"
        private const val TAG_LOOKUP_END = "lookup end"
        private const val ACTION_CHECK_WIFI_KNOWN = "ru.kartashovaa.idledisabler.action.CHECK_WIFI_KNOWN"
        private const val ACTION_CHECK_WIFI_KNOWN_STOP = "ru.kartashovaa.idledisabler.action.CHECK_WIFI_KNOWN_STOP"
        private const val ACTION_CHECK_WIFI_KNOWN_SCAN_FINISH = "ru.kartashovaa.idledisabler.action.CHECK_WIFI_KNOWN_SCAN_FINISH"
        private const val REQUEST_CODE = 1
        private const val WIFI_SEARCH_TIME = 15L
        private val startIntent: Intent
        private val scanIntent: Intent
        private val stopIntent: Intent
        private val pendingIntent: PendingIntent
        private val pendingScanIntent: PendingIntent

        init {
            val app = Component.applicationContext
            startIntent = Intent(app, WifiEnableService::class.java).apply { action = ACTION_CHECK_WIFI_KNOWN }
            scanIntent = Intent(app, WifiEnableService::class.java).apply { action = ACTION_CHECK_WIFI_KNOWN_SCAN_FINISH }
            stopIntent = Intent(app, WifiEnableService::class.java).apply { action = ACTION_CHECK_WIFI_KNOWN_STOP }
            pendingIntent = PendingIntent.getService(app, REQUEST_CODE, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            pendingScanIntent = PendingIntent.getService(app, REQUEST_CODE, scanIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun startCommand() {
            val app = Component.applicationContext
            app.startService(startIntent)
        }

        fun scheduleStartIntent() {
            cancelPendingStartIntents()
            val timeDiff = System.currentTimeMillis() - Repositories.preference.lastWifiScan
            val wifiPeriod = Repositories.preference.settings.wifiEnablePeriod * 1000
            var triggerAfterMills = wifiPeriod
            if (timeDiff in 0..wifiPeriod) triggerAfterMills -= timeDiff
            schedulePendingIntent(triggerAfterMills, pendingIntent, TAG_LOOKUP)
        }

        fun scheduleScanDoneIntent() {
            cancelPendingScanIntents()
            schedulePendingIntent(WIFI_SEARCH_TIME * 1000, pendingScanIntent, TAG_LOOKUP_END)
        }

        fun cancelAllPendingIntents() {
            cancelPendingScanIntents()
            cancelPendingStartIntents()
        }

        fun cancelPendingStartIntents() {
            cancelPendingIntent(pendingIntent, TAG_LOOKUP)
        }

        fun cancelPendingScanIntents() {
            cancelPendingIntent(pendingScanIntent, TAG_LOOKUP_END)
        }

        private fun cancelPendingIntent(pendingIntent: PendingIntent, tag: String) {
            Log.d("Cancel $tag triggers", TAG)
            Component.applicationContext.alarmManager.cancel(pendingIntent)
        }

        private fun schedulePendingIntent(triggerAfterMills: Long, pendingIntent: PendingIntent, tag: String) {
            val triggerAtMills = System.currentTimeMillis() + triggerAfterMills
            val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMills }
            Log.i("WiFi $tag will trigger at ${Text.TIME_FORMAT.format(calendar.time)}", TAG)
            Component.applicationContext.alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMills, pendingIntent)
        }
    }
}
