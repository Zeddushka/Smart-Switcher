package ru.kartashovaa.idledisabler.feature.settings.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import ru.kartashovaa.idledisabler.R
import ru.kartashovaa.idledisabler.util.Log
import ru.kartashovaa.idledisabler.util.Text

/**
 * Created by Kartashov A.A. on 3/1/18.
 * Custom dialogue used to pick intervals
 */
class MinutePickerDialog : AlertDialog {

    private val timePickedCallback: (Int) -> Unit
    private val cancelCallback: () -> Unit
    private val view: View

    constructor(context: Context, title: Int, currentMinutes: Int, timePickedCallback: (Int) -> Unit, cancelCallback: () -> Unit) : super(context) {
        this.timePickedCallback = timePickedCallback
        this.cancelCallback = cancelCallback

        view = LayoutInflater.from(context).inflate(R.layout.dialog_minute_picker, null, false)
        setView(view)
        view.findViewById<TextView>(R.id.textPickerHeader).text = context.getText(title)
        setSelectedMinutesText(currentMinutes)
        view.findViewById<SeekBar>(R.id.seekPickerMinute).progress = currentMinutes

        setupButtons()
        setupListeners()
    }

    private fun setSelectedMinutesText(minutes: Int) {
        view.findViewById<TextView>(R.id.textPickerCurrentSelected).text = "$minutes ${context.getString(Text.getMinuteEndingRes(minutes))}"
    }

    private fun setupButtons() {
        setButton(android.app.AlertDialog.BUTTON_POSITIVE, context.getText(R.string.dialog_picker_positive)) { dialog, _ ->
            val progress = view.findViewById<SeekBar>(R.id.seekPickerMinute).progress
            timePickedCallback(if (progress < 1) 1 else progress)
            dialog.dismiss()
        }
        setButton(android.app.AlertDialog.BUTTON_NEUTRAL, context.getText(R.string.dialog_picker_neutral)) { dialog, _ ->
            cancelCallback()
            dialog.dismiss()
        }
    }

    private fun setupListeners() {
        setOnCancelListener { cancelCallback() }
        view.findViewById<SeekBar>(R.id.seekPickerMinute).setOnSeekBarChangeListener(
                object: SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        setSelectedMinutesText(if (progress < 1) 1 else progress )
                        Log.d("onProgressChanged ${seekBar?.progress}", TAG)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        Log.d("onStartTrackingTouch ${seekBar?.progress}", TAG)
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        seekBar?.run {
                            if (progress < 0) {
                                progress = 1
                            }
                        }
                        Log.d("onStopTrackingTouch ${seekBar?.progress}", TAG)
                    }
                }
        )
    }

    companion object {
        private const val TAG = "MinutePickerDialog"
    }
}