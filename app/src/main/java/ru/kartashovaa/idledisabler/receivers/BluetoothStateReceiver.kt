package ru.kartashovaa.idledisabler.receivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.kartashovaa.idledisabler.services.BluetoothDisableService
import ru.kartashovaa.idledisabler.services.BluetoothEnableService
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.toolkit.Repositories

class BluetoothStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val bluetoothDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                bluetoothDeviceFound(bluetoothDevice)
            }
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                val connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)
                val previousConnectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1)
                val bluetoothDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                bluetoothDeviceConnectionChanged(previousConnectionState, connectionState, bluetoothDevice)

            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                bluetoothScanStart()
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                bluetoothScanDone()
            }
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                bluetoothStateChanged(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR))
            }
        }
    }

    private fun bluetoothStateChanged(state: Int) {
        when (state) {
            BluetoothAdapter.STATE_OFF -> {
                Log.i("Bluetooth turned off", TAG)
                bluetoothEnabled(false)
            }
            BluetoothAdapter.STATE_ON -> {
                Log.i("Bluetooth turned on", TAG)
                bluetoothEnabled(true)
                Repositories.device.isBluetoothDiscovering = true
            }
            BluetoothAdapter.STATE_TURNING_OFF -> Log.d("Bluetooth turning off", TAG)
            BluetoothAdapter.STATE_TURNING_ON -> Log.d("Bluetooth turning on", TAG)
        }
    }

    private fun bluetoothDeviceFound(bluetoothDevice: BluetoothDevice) {
        val knownDevice = bluetoothDevice.address in Repositories.device.bluetoothBondDevices
        Log.d("Bluetooth device found ${bluetoothDevice.name} (${bluetoothDevice.address}) ${if (knownDevice) "is known device" else ""}", TAG)
        val preference = Repositories.preference
        preference.bluetoothFoundAddresses += bluetoothDevice.address
        if (knownDevice) {
            if (preference.isBluetoothKnownScan || preference.isBluetoothIdleScan) {
                Log.i("Bluetooth device see ${bluetoothDevice.name}, it is known device, we can stop known scan", TAG)
                Repositories.device.isBluetoothDiscovering = false
            }
        }
    }

    private fun bluetoothScanStart() {
        Log.d("Bluetooth discovery started", TAG)
        Repositories.preference.run {
            lastBluetoothScan = System.currentTimeMillis()
            bluetoothFoundAddresses = emptySet()
        }
    }

    private fun bluetoothScanDone() {
        Log.d("Bluetooth discovery finished", TAG)
        val preference = Repositories.preference
        preference.lastBluetoothScan = System.currentTimeMillis()
        if (preference.settings.bluetoothDisableWhenIdle) BluetoothDisableService.startCheckCommand()
        if (preference.settings.bluetoothEnableWhenSee) BluetoothEnableService.scanDoneCommand()
    }

    private fun bluetoothDeviceConnectionChanged(oldState: Int, newState: Int, bluetoothDevice: BluetoothDevice) {
        Log.i("Bluetooth device ${bluetoothDevice.name} connection state changed from " +
                "${stateToStr[oldState]} to ${stateToStr[newState]}", TAG)
        val preferences = Repositories.preference

        when (newState) {
            BluetoothAdapter.STATE_CONNECTED -> {
                preferences.bluetoothEnabled = System.currentTimeMillis()
                preferences.lastBluetoothScan = System.currentTimeMillis()
                preferences.bluetoothConnectedAddresses += bluetoothDevice.address
                if (preferences.settings.bluetoothDisableWhenIdle) {
                    BluetoothDisableService.cancelPendingIntents()
                }
                if (preferences.settings.bluetoothEnableWhenSee) {
                    BluetoothEnableService.cancelPendingIntents()
                }
            }
            BluetoothAdapter.STATE_DISCONNECTED -> {
                val prevConnectedAddresses = preferences.bluetoothConnectedAddresses
                val totalConnectedAddresses = prevConnectedAddresses - bluetoothDevice.address
                preferences.bluetoothConnectedAddresses = totalConnectedAddresses
                if (prevConnectedAddresses.isNotEmpty() //Was launched after one or more devices was connected
                        && totalConnectedAddresses.isEmpty()) {
                    Log.i("Bluetooth devices are all disconnected", TAG)
                    if (preferences.settings.bluetoothDisableWhenIdle) {
                        BluetoothDisableService.startCommand()
                    }
                } else {
                    Log.i("Bluetooth connected devices is not become empty from not empty", TAG)
                }
            }
        }
    }

    private fun bluetoothEnabled(enabled: Boolean) {
        Repositories.preference.run {
            bluetoothConnectedAddresses = emptySet()
            bluetoothFoundAddresses = emptySet()
            if (enabled) {
                bluetoothEnabled = System.currentTimeMillis()
                if (settings.bluetoothEnableWhenSee) BluetoothEnableService.cancelPendingIntents()
                if (settings.bluetoothDisableWhenIdle) BluetoothDisableService.startCommand()
            } else {
                isBluetoothIdleScan = false
                isBluetoothKnownScan = false
                bluetoothDisabled = System.currentTimeMillis()
                if (settings.bluetoothEnableWhenSee) BluetoothEnableService.scheduleIntent()
                if (settings.bluetoothDisableWhenIdle) BluetoothDisableService.cancelPendingIntents()
            }
        }
    }

    companion object {
        private const val TAG = "BluetoothStateReceiver"
        private val stateToStr = mapOf(
                BluetoothAdapter.STATE_CONNECTING to "connecting",
                BluetoothAdapter.STATE_CONNECTED to "connected",
                BluetoothAdapter.STATE_DISCONNECTING to "disconnecting",
                BluetoothAdapter.STATE_DISCONNECTED to "disconnected",
                -1 to "unknown")
    }
}
