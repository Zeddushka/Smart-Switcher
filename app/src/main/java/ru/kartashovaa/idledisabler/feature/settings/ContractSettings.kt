package ru.kartashovaa.idledisabler.feature.settings

import io.reactivex.Single
import ru.kartashovaa.idledisabler.R
import ru.kartashovaa.idledisabler.model.Settings


/**
 * Created by Kartashov A.A. on 2/15/18.
 * Settings contract definition
 * Settings feature holds user-driven application configuration
 */
interface ContractSettings {

    enum class PeriodPickerShown(val resource: Int) {
        NONE(0),
        WIFI_ENABLE(R.string.period_picker_name_wifi_enable),
        WIFI_DISABLE(R.string.period_picker_name_wifi_disable),
        BT_ENABLE(R.string.period_picker_name_bt_enable),
        BT_DISABLE(R.string.period_picker_name_bt_disable)
    }

    interface View: ru.kartashovaa.toolkit.View {
        var wifiDisableWhenIdle: Boolean
        var wifiDisablePeriod: Long
        var wifiEnableWhenSee: Boolean
        var wifiEnablePeriod: Long
        var bluetoothDisableWhenIdle: Boolean
        var bluetoothDisablePeriod: Long
        var bluetoothEnableWhenSee: Boolean
        var bluetoothEnablePeriod: Long

        fun showPeriodSelector(type: PeriodPickerShown, time: Long)
        fun showError(messageResource: Int)
    }

    interface Presenter: ru.kartashovaa.toolkit.Presenter<View> {
        fun onWifiDisableWhenIdleChange(checked: Boolean)
        fun onWifiEnableWhenSeeChange(checked: Boolean)
        fun onBluetoothDisableWhenIdleChange(checked: Boolean)
        fun onBluetoothEnableWhenSeeChange(checked: Boolean)
        fun onWifiDisableTimeClick()
        fun onWifiEnableTimeClick()
        fun onBluetoothDisableTimeClick()
        fun onBluetoothEnableTimeClick()

        fun timePeriodSelected(time: Long)
        fun timePickerCanceled()
    }

    interface Interactor: ru.kartashovaa.toolkit.Interactor<Presenter> {
        fun fetchSettings(): Single<Settings>
        fun saveSettings(settings: Settings)
    }
}