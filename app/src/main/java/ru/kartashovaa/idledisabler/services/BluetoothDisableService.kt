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


class BluetoothDisableService : IntentService("BluetoothDisableService") {

    override fun onHandleIntent(intent: Intent?) {
        if (Repositories.preference.settings.bluetoothDisableWhenIdle && Repositories.device.hasLocationPermission) {
            when (intent?.action) {
                ACTION_ENABLE_BT_FOR_IDLE -> {
                    checkBluetoothIdle()
                }
                ACTION_CHECK_BT_IDLE -> {
                    checkBluetoothScanDone()
                }
                else -> Log.e("Unknown action in BT disable service", Throwable(intent?.action), TAG)
            }
        } else {
            Log.i("Bluetooth turn off function is disabled or permission revoked", TAG)
            cancelPendingIntents()
        }
    }

    private fun checkBluetoothIdle() {
        Log.d("checkBluetoothIdle", TAG)
        cancelPendingIntents()
        val preference = Repositories.preference
        val device = Repositories.device
        if (device.isBluetoothEnabled) {
            if (!device.isBluetoothDiscovering) {
                val timeDiff = System.currentTimeMillis() - preference.bluetoothEnabled
                val scanTimeDiff = System.currentTimeMillis() - preference.lastBluetoothScan

                if (scanTimeDiff > SCAN_INTERVAL_OLD) {
                    Log.i("Bluetooth discovery data is obsolete (${scanTimeDiff / 1000} seconds ago), started for scanning known device", TAG)
                    preference.isBluetoothIdleScan = true
                    device.isBluetoothDiscovering = true
                } else {
                    if (device.isBondedDevicesFound) {
                        Log.d("Bluetooth is not idling", TAG)
                        preference.bluetoothEnabled = System.currentTimeMillis()
                        BluetoothDisableService.scheduleStartIntent()
                    } else {
                        Log.d("Bluetooth is idling for ${timeDiff / 1000} seconds", TAG)
                        if (timeDiff > preference.settings.bluetoothDisablePeriod * 1000) {
                            preference.lastBluetoothScan = System.currentTimeMillis()
                            Log.i("Bluetooth disabled in case of idle and known devices is not seen", TAG)
                            device.isBluetoothEnabled = false
                        } else {
                            BluetoothDisableService.scheduleStartIntent()
                        }
                    }
                }
            } else {
                Log.d("Bluetooth discovering in progress", TAG)
            }
        } else {
            Log.i("Bluetooth already disabled", TAG)
        }
    }

    private fun checkBluetoothScanDone() {
        Log.d("checkBluetoothScanDone", TAG)
        cancelPendingIntents()
        Log.i("Bluetooth discovery done, checking for known devices", TAG)
        val device = Repositories.device
        val preference = Repositories.preference
        if (preference.isBluetoothIdleScan) {
            preference.isBluetoothIdleScan = false
            if (!device.isBondedDevicesFound && preference.bluetoothConnectedAddresses.isEmpty()) {
                Log.i("Bluetooth does not see any known device/does not connected, perform idle check", TAG)
                checkBluetoothIdle()
            } else {
                Log.i("Bluetooth have found bonded devices or has connected addresses", TAG)
                BluetoothDisableService.scheduleStartIntent()
            }
        } else {
            Log.i("Bluetooth scan was not idle scan", TAG)
            BluetoothDisableService.scheduleStartIntent()
        }
    }

    companion object {
        private const val TAG = "BluetoothDisableService"
        private const val ACTION_ENABLE_BT_FOR_IDLE = "ru.kartashovaa.idledisabler.action.ENABLE_BT_FOR_IDLE"
        private const val ACTION_CHECK_BT_IDLE = "ru.kartashovaa.idledisabler.action.CHECK_BT_IDLE"
        private const val REQUEST_CODE = 3
        private const val SCAN_INTERVAL_OLD = 60_000
        private val startIntent: Intent
        private val checkIntent: Intent
        private val pendingIntent: PendingIntent

        init {
            val app = Component.applicationContext
            startIntent = Intent(app, BluetoothDisableService::class.java).apply { action = ACTION_ENABLE_BT_FOR_IDLE }
            checkIntent = Intent(app, BluetoothDisableService::class.java).apply { action = ACTION_CHECK_BT_IDLE }
            pendingIntent = PendingIntent.getService(app, REQUEST_CODE, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun startCommand() {
            Component.applicationContext.startService(startIntent)
        }

        fun startCheckCommand() {
            Component.applicationContext.startService(checkIntent)
        }

        fun scheduleStartIntent() {
            cancelPendingIntents()
            val timeDiff = System.currentTimeMillis() - Repositories.preference.bluetoothEnabled
            val bluetoothPeriod = Repositories.preference.settings.bluetoothDisablePeriod * 1000
            var triggerAtMills = System.currentTimeMillis() + bluetoothPeriod
            if (timeDiff in 0..bluetoothPeriod) triggerAtMills -= timeDiff
            val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMills }
            Log.i("Bluetooth idle check will trigger at ${Text.TIME_FORMAT.format(calendar.time)}", TAG)
            Component.applicationContext.alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMills, pendingIntent)
        }

        fun cancelPendingIntents() {
            Log.d("cancelPendingIntents", TAG)
            Component.applicationContext.alarmManager.cancel(pendingIntent)
        }
    }
}
