package ru.kartashovaa.idledisabler.feature.settings

import io.reactivex.Single
import ru.kartashovaa.idledisabler.model.Settings
import ru.kartashovaa.idledisabler.services.BluetoothDisableService
import ru.kartashovaa.idledisabler.services.BluetoothEnableService
import ru.kartashovaa.idledisabler.services.WifiDisableService
import ru.kartashovaa.idledisabler.services.WifiEnableService
import ru.kartashovaa.toolkit.Repositories
import ru.kartashovaa.toolkit.BaseInteractor

/**
 * Created by Kartashov A.A. on 2/15/18.
 */
class InteractorSettings : BaseInteractor<ContractSettings.Presenter>(),
        ContractSettings.Interactor {

    override val presenterClass = PresenterSettings::class.java

    override fun fetchSettings(): Single<Settings> {
        return Single.just(Repositories.preference.settings)
    }

    //TODO: Если клацать как упоротый на чекбоксе, наблюдаются фризы, хореографер негодует!
    override fun saveSettings(settings: Settings) {
        val oldSettings = Repositories.preference.settings
        if (oldSettings.wifiEnableWhenSee != settings.wifiEnableWhenSee)
            wifiEnablePolicyChanged(settings.wifiEnableWhenSee)
        if (oldSettings.wifiDisableWhenIdle != settings.wifiDisableWhenIdle)
            wifiDisablePolicyChanged(settings.wifiDisableWhenIdle)
        if (oldSettings.bluetoothEnableWhenSee != settings.bluetoothEnableWhenSee)
            bluetoothEnablePolicyChanged(settings.bluetoothEnableWhenSee)
        if (oldSettings.bluetoothDisableWhenIdle != settings.bluetoothDisableWhenIdle)
            bluetoothDisablePolicyChanged(settings.bluetoothDisableWhenIdle)
        Repositories.preference.settings = settings
    }

    private fun wifiEnablePolicyChanged(enabled: Boolean) {
        if (enabled) {
            if (!Repositories.device.isWifiEnabled) WifiEnableService.startCommand()
        } else {
            WifiEnableService.cancelAllPendingIntents()
        }
    }

    private fun wifiDisablePolicyChanged(enabled: Boolean) {
        if (enabled) {
            if (!Repositories.device.isWifiConnected) WifiDisableService.startCommand()
        } else {
            WifiDisableService.cancelPendingIntents()
        }
    }

    private fun bluetoothEnablePolicyChanged(enabled: Boolean) {
        if (enabled) {
            BluetoothEnableService.startCommand()
        } else {
            BluetoothEnableService.cancelPendingIntents()
        }
    }

    private fun bluetoothDisablePolicyChanged(enabled: Boolean) {
        if (enabled) {
            if (Repositories.device.isBluetoothEnabled) BluetoothDisableService.startCommand()
        } else {
            BluetoothDisableService.cancelPendingIntents()
        }
    }
}