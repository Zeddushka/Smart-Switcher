package ru.kartashovaa.toolkit

import ru.kartashovaa.idledisabler.repository.Device
import ru.kartashovaa.idledisabler.repository.Preferences

/**
 * Created by Kartashov A.A. on 2/16/18.
 * Some syntax sugar for repository usage
 */
object Repositories {
    val device
        get() = Component.getComponent(Device::class.java)

    val preference
        get() = Component.getComponent(Preferences::class.java)
}