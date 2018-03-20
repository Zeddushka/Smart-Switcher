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


class BluetoothEnableService : IntentService("BluetoothEnableService") {

    override fun onHandleIntent(intent: Intent?) {
        if (Repositories.preference.settings.bluetoothEnableWhenSee && Repositories.device.hasLocationPermission) {
            when (intent?.action) {
                ACTION_CHECK_BT_KNOWN -> {
                    enableBluetoothKnownScan()
                }
                ACTION_CHECK_BT_KNOWN_SCAN_DONE -> {
                    checkKnownDevices()
                }
                else -> Log.e("Unknown action in BT enable service", Throwable(intent?.action), TAG)
            }
        } else {
            Log.i("Bluetooth turn on feature disabled or permission revoked", TAG)
            cancelPendingIntents()
        }
    }

    private fun enableBluetoothKnownScan() {
        Log.d("enableBluetoothKnownScan", TAG)
        cancelPendingIntents()

        val preferences = Repositories.preference
        val device = Repositories.device
        if (device.isBluetoothEnabled) {
            Log.i("Bluetooth already enabled", TAG)
        } else {
            val timeDiff = System.currentTimeMillis() - preferences.lastBluetoothScan
            if (timeDiff > preferences.settings.bluetoothEnablePeriod * 1000) {
                Log.i("Bluetooth enabled to scan known devices", TAG)
                preferences.isBluetoothKnownScan = true
                device.isBluetoothEnabled = true
//                device.isBluetoothDiscovering = true
            } else {
                Log.i("Bluetooth known check was ${timeDiff / 1000} seconds ago", TAG)
                scheduleIntent()
            }
        }
    }

    private fun checkKnownDevices() {
        Log.d("checkKnownDevices", TAG)
        val preferences = Repositories.preference
        val device = Repositories.device
        if (preferences.isBluetoothKnownScan) {
            if (!device.isBondedDevicesFound && preferences.bluetoothConnectedAddresses.isEmpty()) {
                Log.i("Bluetooth does not found any known device (and no any connected), disabling it", TAG)
                device.isBluetoothEnabled = false
            } else {
                Log.i("Bluetooth have found bonded devices", TAG)
            }
            preferences.isBluetoothKnownScan = false
        } else {
            Log.i("Bluetooth enabled is not for known scan (manual/idle check/etc)", TAG)
        }
    }

    companion object {
        private const val TAG = "BluetoothEnableService"
        private const val ACTION_CHECK_BT_KNOWN = "ru.kartashovaa.idledisabler.action.CHECK_BT_KNOWN"
        private const val ACTION_CHECK_BT_KNOWN_SCAN_DONE = "ru.kartashovaa.idledisabler.action.CHECK_BT_KNOWN_SCAN_DONE"
        private const val REQUEST_CODE = 2
        private val startIntent: Intent
        private val scanIntent: Intent
        private val pendingIntent: PendingIntent

        init {
            val app = Component.applicationContext
            startIntent = Intent(app, BluetoothEnableService::class.java).apply { action = ACTION_CHECK_BT_KNOWN }
            scanIntent = Intent(app, BluetoothEnableService::class.java).apply { action = ACTION_CHECK_BT_KNOWN_SCAN_DONE }
            pendingIntent = PendingIntent.getService(app, REQUEST_CODE, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun startCommand() {
            Component.applicationContext.startService(startIntent)
        }

        fun scanDoneCommand() {
            Component.applicationContext.startService(scanIntent)
        }

        fun scheduleIntent() {
            cancelPendingIntents()
            val timeDiff = System.currentTimeMillis() - Repositories.preference.lastBluetoothScan
            val bluetoothPeriod = Repositories.preference.settings.bluetoothEnablePeriod * 1000
            var triggerAtMills = System.currentTimeMillis() + bluetoothPeriod
            if (timeDiff in 0..bluetoothPeriod) triggerAtMills -= timeDiff
            val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMills }
            Log.i("Bluetooth known check will trigger at ${Text.TIME_FORMAT.format(calendar.time)}", TAG)
            Component.applicationContext.alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMills, pendingIntent)
        }

        fun cancelPendingIntents() {
            Log.d("cancelPendingIntents", TAG)
            Component.applicationContext.alarmManager.cancel(pendingIntent)
        }
    }
}
