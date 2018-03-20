package ru.kartashovaa.idledisabler

import android.app.Application
import ru.kartashovaa.idledisabler.repository.Device
import ru.kartashovaa.toolkit.Component

/**
 * Created by Kartashov A.A. on 2/16/18.
 * Application that initialize Component on startup
 */
class SmartSwitcherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Component.applicationContext = this
        Component.persistComponents(Device::class.java)
    }
}