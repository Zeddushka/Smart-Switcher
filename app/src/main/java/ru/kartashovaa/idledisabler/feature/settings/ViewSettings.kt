package ru.kartashovaa.idledisabler.feature.settings

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import ru.kartashovaa.idledisabler.R
import ru.kartashovaa.idledisabler.feature.settings.dialogs.MinutePickerDialog
import ru.kartashovaa.idledisabler.util.Text
import ru.kartashovaa.toolkit.BaseActivity

class ViewSettings : BaseActivity<ContractSettings.View, ContractSettings.Presenter>(), ContractSettings.View {

    override val presenterClass = PresenterSettings::class.java
    private var pickerDialog: MinutePickerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        textWifiDisable.onClick { presenter.onWifiDisableTimeClick() }
        textWifiEnable.onClick { presenter.onWifiEnableTimeClick() }
        textBluetoothEnable.onClick { presenter.onBluetoothEnableTimeClick() }
        textBluetoothDisable.onClick { presenter.onBluetoothDisableTimeClick() }
    }

    override fun showError(messageResource: Int) {
        toast(messageResource)
    }

    override fun showPeriodSelector(type: ContractSettings.PeriodPickerShown, time: Long) {
        val totalMinutes = (time / 60).toInt()
        pickerDialog = MinutePickerDialog(this,
                type.resource,
                totalMinutes,
                { minutes -> presenter.timePeriodSelected(minutes*60L) },
                { presenter.timePickerCanceled() })
                .apply { show() }
    }

    override fun onStop() {
        pickerDialog?.dismiss()
        pickerDialog = null
        super.onStop()
    }

    override var wifiDisableWhenIdle: Boolean
        get() = checkWifiDisable.isChecked
        set(value) {
            val view = checkWifiDisable
            view.isChecked = value
            textWifiDisable.visibility = if (value) View.VISIBLE else View.GONE

            if (!view.isEnabled) {
                view.isEnabled = true
                view.onCheckedChange { _, isChecked ->
                    presenter.onWifiDisableWhenIdleChange(isChecked)
                    textWifiDisable.visibility = if (isChecked) View.VISIBLE else View.GONE
                }
            }
        }

    override var wifiDisablePeriod: Long
        get() = textWifiDisable.text.toString().toLong()
        set(value) {
            val minutes = secondsToMin(value)
            textWifiDisable.text = "${getString(R.string.idle_for)} $minutes ${getString(Text.getMinuteEndingRes(minutes, true))}"
        }

    override var wifiEnableWhenSee: Boolean
        get() = checkWifiEnable.isChecked
        set(value) {
            val view = checkWifiEnable
            view.isChecked = value
            textWifiEnable.visibility = if (value) View.VISIBLE else View.GONE

            if (!view.isEnabled) {
                view.isEnabled = true
                view.onCheckedChange { _, isChecked ->
                    presenter.onWifiEnableWhenSeeChange(isChecked)
                    textWifiEnable.visibility = if (isChecked) View.VISIBLE else View.GONE
                }
            }
        }

    override var wifiEnablePeriod: Long
        get() = textWifiEnable.text.toString().toLong()
        set(value) {
            val minutes = secondsToMin(value)
            textWifiEnable.text = "${getString(R.string.check_every)} $minutes ${getString(Text.getMinuteEndingRes(minutes, true))}"
        }

    override var bluetoothDisableWhenIdle: Boolean
        get() = checkBluetoothDisable.isChecked
        set(value) {
            val view = checkBluetoothDisable
            view.isChecked = value
            textBluetoothDisable.visibility = if (value) View.VISIBLE else View.GONE

            if (!view.isEnabled) {
                view.isEnabled = true
                view.onCheckedChange { _, isChecked ->
                    presenter.onBluetoothDisableWhenIdleChange(isChecked)
                    textBluetoothDisable.visibility = if (isChecked) View.VISIBLE else View.GONE
                }
            }
        }

    override var bluetoothDisablePeriod: Long
        get() = textBluetoothDisable.text.toString().toLong()
        set(value) {
            val minutes = secondsToMin(value)
            textBluetoothDisable.text = "${getString(R.string.idle_for)} $minutes ${getString(Text.getMinuteEndingRes(minutes, true))}"
        }

    override var bluetoothEnableWhenSee: Boolean
        get() = checkBluetoothEnable.isChecked
        set(value) {
            val view = checkBluetoothEnable
            view.isChecked = value
            textBluetoothEnable.visibility = if (value) View.VISIBLE else View.GONE

            if (!view.isEnabled) {
                view.isEnabled = true
                view.onCheckedChange { _, isChecked ->
                    presenter.onBluetoothEnableWhenSeeChange(isChecked)
                    textBluetoothEnable.visibility = if (isChecked) View.VISIBLE else View.GONE
                }
            }
        }

    override var bluetoothEnablePeriod: Long
        get() = textBluetoothEnable.text.toString().toLong()
        set(value) {
            val minutes = secondsToMin(value)
            textBluetoothEnable.text = "${getString(R.string.check_every)} $minutes ${getString(Text.getMinuteEndingRes(minutes, true))}"
        }

    private fun secondsToMin(timeMills: Long): Int {
        return (timeMills / 60).toInt()
    }
}
