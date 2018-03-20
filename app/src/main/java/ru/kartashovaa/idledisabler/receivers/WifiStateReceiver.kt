package ru.kartashovaa.idledisabler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import ru.kartashovaa.idledisabler.services.WifiDisableService
import ru.kartashovaa.idledisabler.services.WifiEnableService
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.toolkit.Repositories

/**
 * Created by Kartashov A.A. on 2/19/18.
 * Hold's WiFi events
 */
class WifiStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiManager.WIFI_STATE_CHANGED_ACTION -> wifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN))
            WifiManager.SUPPLICANT_STATE_CHANGED_ACTION -> supplicantStateChange(intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE))
        }
    }

    private fun wifiStateChanged(state: Int) {

        when (state) {
            WifiManager.WIFI_STATE_ENABLED -> {
                Log.d("WiFi turned on", TAG)
                wifiEnabled(true)
            }
            WifiManager.WIFI_STATE_ENABLING -> Log.d("WIFI turning on", TAG)
            WifiManager.WIFI_STATE_DISABLING -> Log.d("WiFi turning off", TAG)
            WifiManager.WIFI_STATE_DISABLED -> {
                Log.d("WiFi turned off", TAG)
                wifiEnabled(false)
            }
        }
    }

    private fun wifiEnabled(enabled: Boolean) {
        val preference = Repositories.preference
        val currentTime = System.currentTimeMillis()
        if (enabled) {
            preference.wifiEnabled = currentTime
            if (preference.settings.wifiEnableWhenSee) WifiEnableService.cancelPendingStartIntents()
        } else {
            preference.wifiDisabled = currentTime
            if (preference.settings.wifiEnableWhenSee) WifiEnableService.scheduleStartIntent()
            if (preference.settings.wifiDisableWhenIdle) WifiDisableService.cancelPendingIntents()
        }
    }

    private fun supplicantStateChange(state: SupplicantState) {
        Log.d("WiFi Supplicant state = $state", TAG)
        val preference = Repositories.preference
        val currentTime = System.currentTimeMillis()
        when (state) {
            SupplicantState.COMPLETED -> {
                Log.i("WiFi is connected", TAG)
                preference.wifiEnabled = currentTime
                if (preference.settings.wifiEnableWhenSee) WifiEnableService.cancelPendingScanIntents()
                if (preference.settings.wifiDisableWhenIdle) WifiDisableService.cancelPendingIntents()
                preference.wifiPreviousStateName = SupplicantState.COMPLETED.name
            }
            SupplicantState.DISCONNECTED -> {
                Log.i("WiFi is disconnected", TAG)
                if (preference.wifiPreviousStateName == SupplicantState.COMPLETED.name) {
                    preference.lastWifiScan = currentTime
                    preference.wifiEnabled = currentTime
                }
                preference.wifiPreviousStateName = SupplicantState.DISCONNECTED.name
            }
            SupplicantState.SCANNING -> {
                Log.i("WiFi is scanning", TAG)
                preference.lastWifiScan = currentTime
                if (preference.settings.wifiDisableWhenIdle) WifiDisableService.scheduleIntent()
            }
        }
    }

    companion object {
        private const val TAG = "WifiStateReceiver"
    }
}