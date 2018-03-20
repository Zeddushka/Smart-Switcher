package ru.kartashovaa.idledisabler.model

/**
 * Created by Kartashov A.A. on 2/16/18.
 * Data class representing all Settings
 * Time default in seconds
 */
data class Settings(var wifiDisableWhenIdle: Boolean = false,
                    var wifiDisablePeriod: Long = 60,
                    var wifiEnableWhenSee: Boolean = false,
                    var wifiEnablePeriod: Long = 60,
                    var bluetoothDisableWhenIdle: Boolean = false,
                    var bluetoothDisablePeriod: Long = 60,
                    var bluetoothEnableWhenSee: Boolean = false,
                    var bluetoothEnablePeriod: Long = 60)