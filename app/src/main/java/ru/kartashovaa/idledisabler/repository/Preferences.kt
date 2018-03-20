package ru.kartashovaa.idledisabler.repository

import android.app.Application
import android.net.wifi.SupplicantState
import com.google.gson.Gson
import org.jetbrains.anko.defaultSharedPreferences
import ru.kartashovaa.idledisabler.model.Settings
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.toolkit.BaseRepository

/**
 * Created by Kartashov A.A. on 2/16/18.
 * Repository that holding SharedPreferences usage
 */
class Preferences : BaseRepository() {
    companion object {
        //SharedPreferences keys
        const val KEY_SETTINGS = "KEY_SETTINGS"
        const val KEY_LAST_BLUETOOTH_SCAN = "KEY_LAST_BLUETOOTH_SCAN_AT"
        const val KEY_LAST_WIFI_SCAN = "KEY_LAST_WIFI_SCAN_AT"
        const val KEY_WIFI_ENABLED = "KEY_WIFI_ENABLED_AT"
        const val KEY_WIFI_DISABLED = "KEY_WIFI_DISABLED_AT"
        const val KEY_BT_ENABLED = "KEY_BT_ENABLED_AT"
        const val KEY_BT_DISABLED = "KEY_BT_DISABLED_AT"
        const val KEY_BT_FOUND_DEVICES = "KEY_BT_FOUND_DEVICES"
        const val KEY_BT_CONNECTED_DEVICES = "KEY_BT_CONNECTED_DEVICES"
        const val KEY_BT_STARTED_FOR_KNOWN_SCAN = "KEY_BT_STARTED_FOR_KNOWN_SCAN"
        const val KEY_BT_STARTED_FOR_IDLE_SCAN = "KEY_BT_STARTED_FOR_IDLE_SCAN"
        const val KEY_WIFI_PREVIOUS_STATE = "KEY_WIFI_PREVIOUS_STATE"
    }

    override fun init(application: Application) {
        super.init(application)
        initDefaultTimings()
        initDefaultSettings()
        initDefaultValues()
    }

    private fun initDefaultTimings() {
        val keys = mutableSetOf<String>()
        val prefs = preferences
        if (!prefs.contains(KEY_WIFI_ENABLED)) keys.add(KEY_WIFI_ENABLED)
        if (!prefs.contains(KEY_WIFI_DISABLED)) keys.add(KEY_WIFI_DISABLED)
        if (!prefs.contains(KEY_BT_ENABLED)) keys.add(KEY_BT_ENABLED)
        if (!prefs.contains(KEY_BT_DISABLED)) keys.add(KEY_BT_DISABLED)
        if (keys.size > 0) {
            val time = System.currentTimeMillis()
            prefs.edit().apply { keys.map { it -> putLong(it, time) } }.apply()
        }
    }

    private fun initDefaultSettings() {
        if (!preferences.contains(KEY_SETTINGS)) settings = Settings()
    }

    private fun initDefaultValues() {
        if (!preferences.contains(KEY_BT_STARTED_FOR_KNOWN_SCAN)) isBluetoothKnownScan = false
        if (!preferences.contains(KEY_BT_STARTED_FOR_IDLE_SCAN)) isBluetoothIdleScan = false
        if (!preferences.contains(KEY_WIFI_PREVIOUS_STATE)) wifiPreviousStateName = SupplicantState.DISCONNECTED.name
    }

    private val preferences
        get() = application.defaultSharedPreferences

    var settings: Settings
        get() {
            return if (preferences.contains(KEY_SETTINGS)) {
                try {
                    Gson().fromJson(preferences.getString(KEY_SETTINGS, ""), Settings::class.java)
                } catch (e: Exception) {
                    Log.e("Error getting Settings from SharedPreferences", e)
                    preferences.edit().remove(KEY_SETTINGS)
                    Settings()
                }
            } else {
                Settings()
            }
        }
        set(value) = preferences.edit().putString(KEY_SETTINGS, Gson().toJson(value)).apply()

    var lastBluetoothScan: Long
        get() = preferences.getLong(KEY_LAST_BLUETOOTH_SCAN, 0)
        set(value) = preferences.edit().putLong(KEY_LAST_BLUETOOTH_SCAN, value).apply()

    var lastWifiScan: Long
        get() = preferences.getLong(KEY_LAST_WIFI_SCAN, 0)
        set(value) = preferences.edit().putLong(KEY_LAST_WIFI_SCAN, value).apply()

    var wifiEnabled: Long
        get() = preferences.getLong(KEY_WIFI_ENABLED, 0)
        set(value) = preferences.edit().putLong(KEY_WIFI_ENABLED, value).apply()

    var wifiDisabled: Long
        get() = preferences.getLong(KEY_WIFI_DISABLED, 0)
        set(value) = preferences.edit().putLong(KEY_WIFI_DISABLED, value).apply()

    var bluetoothEnabled: Long
        get() = preferences.getLong(KEY_BT_ENABLED, 0)
        set(value) = preferences.edit().putLong(KEY_BT_ENABLED, value).apply()

    var bluetoothDisabled: Long
        get() = preferences.getLong(KEY_BT_DISABLED, 0)
        set(value) = preferences.edit().putLong(KEY_BT_DISABLED, value).apply()

    var bluetoothFoundAddresses: Set<String> = mutableSetOf()
        get() {
            if (field.isEmpty())
                field = preferences.getString(KEY_BT_FOUND_DEVICES, "")
                        .split(",").toSet()
            return field
        }
        set(value) {
            field = value
            preferences.edit().putString(KEY_BT_FOUND_DEVICES, value.joinToString(",")).apply()
        }

    var bluetoothConnectedAddresses: Set<String> = mutableSetOf()
        get() {
            if (field.isEmpty())
                field = preferences.getString(KEY_BT_CONNECTED_DEVICES, "")
                        .split(",").filter { it -> it.isNotBlank() }.toSet()
            return field
        }
        set(value) {
            field = value.filter { it -> it.isNotBlank() }.toSet()
            preferences.edit().putString(KEY_BT_CONNECTED_DEVICES, field.joinToString(",")).apply()
        }

    var isBluetoothKnownScan: Boolean
        get() = preferences.getBoolean(KEY_BT_STARTED_FOR_KNOWN_SCAN, false)
        set(value) = preferences.edit().putBoolean(KEY_BT_STARTED_FOR_KNOWN_SCAN, value).apply()

    var isBluetoothIdleScan: Boolean
        get() = preferences.getBoolean(KEY_BT_STARTED_FOR_IDLE_SCAN, false)
        set(value) = preferences.edit().putBoolean(KEY_BT_STARTED_FOR_IDLE_SCAN, value).apply()

    var wifiPreviousStateName: String
        get() = preferences.getString(KEY_WIFI_PREVIOUS_STATE, SupplicantState.DISCONNECTED.name)
        set(value) = preferences.edit().putString(KEY_WIFI_PREVIOUS_STATE, value).apply()
}