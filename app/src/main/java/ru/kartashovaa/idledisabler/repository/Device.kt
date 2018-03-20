package ru.kartashovaa.idledisabler.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import ru.kartashovaa.idledisabler.services.BluetoothDisableService
import ru.kartashovaa.idledisabler.services.BluetoothEnableService
import ru.kartashovaa.idledisabler.services.WifiDisableService
import ru.kartashovaa.idledisabler.services.WifiEnableService
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.toolkit.Repositories
import ru.kartashovaa.toolkit.BaseRepository

/**
 * Created by Kartashov A.A. on 2/16/18.
 * Repository that provides device-specific data, performing device hardware work
 */
class Device : BaseRepository() {

    private val bluetoothAdapter: BluetoothAdapter
        get() = BluetoothAdapter.getDefaultAdapter()

    val bluetoothBondDevices: Set<String>
        get() = bluetoothAdapter.bondedDevices.map { it -> it.address }.toSet()

    var isBluetoothEnabled: Boolean
        get() = bluetoothAdapter.isEnabled
        set(value) = bluetoothAdapter.run {
            if (value) enable()
            else disable()
        }

    var isBluetoothDiscovering: Boolean
        get() = bluetoothAdapter.isDiscovering
        set(value) {
            bluetoothAdapter.run {
                if (value) {
                    setScanMode()
                    startDiscovery()
                }
                else cancelDiscovery()
            }
        }

    private fun setScanMode() {
        try {
            val method = bluetoothAdapter::class.java.getMethod("setScanMode", Int::class.java)
            val result = method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) as Boolean
            Log.d("Scan mode setup ${if (result) "success" else "failed"}", "DeviceRepository")
        } catch (e: Exception) {
            Log.e("Failed to set scan mode", e, "DeviceRepository")
        }
    }

    private val wifiManager: WifiManager
        get() = application.getSystemService(Context.WIFI_SERVICE) as WifiManager

    var isWifiEnabled: Boolean
        get() = wifiManager.isWifiEnabled
        set(value) {
            wifiManager.isWifiEnabled = value
        }

    val isWifiConnected: Boolean
        get() {
            val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected
        }

    val isBondedDevicesFound: Boolean
        get() = bluetoothBondDevices.intersect(Repositories.preference.bluetoothFoundAddresses).isNotEmpty()

    val hasLocationPermission: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                application.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun startEnabledServices(tag: String) {
        Log.i("Starting services", tag)
        val settings = Repositories.preference.settings
        val device = Repositories.device
        if (device.isWifiEnabled) {
            if (settings.wifiDisableWhenIdle && !device.isWifiConnected) WifiDisableService.startCommand()
        } else {
            if (settings.wifiEnableWhenSee) WifiEnableService.startCommand()
        }
        if (device.isBluetoothEnabled) {
            if (settings.bluetoothDisableWhenIdle
                    && Repositories.preference.bluetoothConnectedAddresses.isEmpty()) BluetoothDisableService.startCommand()
        } else {
            if (settings.bluetoothEnableWhenSee) BluetoothEnableService.startCommand()
        }
    }
}