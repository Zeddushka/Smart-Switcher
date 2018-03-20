package ru.kartashovaa.idledisabler.feature.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.kartashovaa.idledisabler.R
import ru.kartashovaa.idledisabler.model.Settings
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.toolkit.Repositories
import ru.kartashovaa.toolkit.Scheduler
import ru.kartashovaa.toolkit.BasePresenter

/**
 * Created by Kartashov A.A. on 2/15/18.
 */
class PresenterSettings : BasePresenter<ContractSettings.View, ContractSettings.Interactor>(),
        ContractSettings.Presenter {

    override val interactorClass = InteractorSettings::class.java

    companion object {
        private var shownPeriod = ContractSettings.PeriodPickerShown.NONE
        private var shownPickerTime = 0L
        private const val TAG = "PresenterSettings"
    }

    private var broadcastReceiver: EventBroadcastReceiver
    private var settings = Settings()

    init {
        broadcastReceiver = EventBroadcastReceiver()
        Repositories.device.startEnabledServices(TAG)
    }

    override fun onViewAttached() {
        updateView()
    }

    private fun updateView() {
        interactor.fetchSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(Scheduler.ui)
                .subscribeBy (
                        onSuccess = {
                            settings = it
                            fillViewWithSettings()
                        },
                        onError = this::showError
                )
    }

    private fun fillViewWithSettings() {
        view?.apply {
            if (shownPeriod != ContractSettings.PeriodPickerShown.NONE) showPeriodSelector(shownPeriod, shownPickerTime)
            wifiDisableWhenIdle = settings.wifiDisableWhenIdle
            wifiDisablePeriod = settings.wifiDisablePeriod
            wifiEnableWhenSee = settings.wifiEnableWhenSee
            wifiEnablePeriod = settings.wifiEnablePeriod
            bluetoothDisableWhenIdle = settings.bluetoothDisableWhenIdle
            bluetoothDisablePeriod = settings.bluetoothDisablePeriod
            bluetoothEnableWhenSee = settings.bluetoothEnableWhenSee
            bluetoothEnablePeriod = settings.bluetoothEnablePeriod
        }
    }

    private fun showError(throwable: Throwable) {
        Log.e("Error fetching settings", throwable)
        view?.showError(R.string.error_fetch_settings)
        fillViewWithSettings()
    }

    private fun updateSettings() {
        interactor.saveSettings(settings)
    }

    override fun timePeriodSelected(time: Long) {
        Log.d("Period $shownPeriod selected to $time", TAG)
        when (shownPeriod) {
            ContractSettings.PeriodPickerShown.NONE -> {}
            ContractSettings.PeriodPickerShown.WIFI_ENABLE -> {
                settings.wifiEnablePeriod = time
                view?.wifiEnablePeriod = time
            }
            ContractSettings.PeriodPickerShown.WIFI_DISABLE -> {
                settings.wifiDisablePeriod = time
                view?.wifiDisablePeriod = time
            }
            ContractSettings.PeriodPickerShown.BT_ENABLE -> {
                settings.bluetoothEnablePeriod = time
                view?.bluetoothEnablePeriod = time
            }
            ContractSettings.PeriodPickerShown.BT_DISABLE -> {
                settings.bluetoothDisablePeriod = time
                view?.bluetoothDisablePeriod = time
            }
        }
        shownPeriod = ContractSettings.PeriodPickerShown.NONE
        updateSettings()
    }

    override fun onWifiDisableWhenIdleChange(checked: Boolean) {
        settings.wifiDisableWhenIdle = checked
        updateSettings()
    }

    override fun onWifiEnableWhenSeeChange(checked: Boolean) {
        settings.wifiEnableWhenSee = checked
        updateSettings()
    }

    override fun onBluetoothDisableWhenIdleChange(checked: Boolean) {
        settings.bluetoothDisableWhenIdle = checked
        updateSettings()
    }

    override fun onBluetoothEnableWhenSeeChange(checked: Boolean) {
        settings.bluetoothEnableWhenSee = checked
        updateSettings()
    }

    override fun onWifiDisableTimeClick() {
        showPicker(ContractSettings.PeriodPickerShown.WIFI_DISABLE, settings.wifiDisablePeriod)
    }

    override fun onWifiEnableTimeClick() {
        showPicker(ContractSettings.PeriodPickerShown.WIFI_ENABLE, settings.wifiEnablePeriod)
    }

    override fun onBluetoothDisableTimeClick() {
        showPicker(ContractSettings.PeriodPickerShown.BT_DISABLE, settings.bluetoothDisablePeriod)
    }

    override fun onBluetoothEnableTimeClick() {
        showPicker(ContractSettings.PeriodPickerShown.BT_ENABLE, settings.bluetoothEnablePeriod)
    }

    private fun showPicker(pickerType: ContractSettings.PeriodPickerShown, pickerTime: Long) {
        shownPeriod = pickerType
        shownPickerTime = pickerTime
        view?.showPeriodSelector(pickerType, shownPickerTime)
    }

    override fun timePickerCanceled() {
        Log.d("Time picker for $shownPeriod cancelled")
        shownPeriod = ContractSettings.PeriodPickerShown.NONE
    }

    class EventBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }
}