package ru.kartashovaa.idledisabler.util

import ru.kartashovaa.idledisabler.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Kartashov A.A. on 3/1/18.
 * Text utils (some pain) to hold ru+en word endings
 */
object Text {

    val TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    /**
     * Return string resource corresponding quantity of minutes
     */
    fun getMinuteEndingRes(minutes: Int, given: Boolean = false): Int {
        return when (minutes) {
            1 -> if (!given) R.string.text_ending_minute_1 else R.string.text_ending_minute_1given
            in 10..19 -> //ru exceptions
                R.string.text_ending_minute_other
            else -> //common rules
                when (minutes % 10) {
                    1 -> if (!given) R.string.text_ending_minute_1x else R.string.text_ending_minute_1xgiven
                    2, 3, 4 -> R.string.text_ending_minute_234
                    else -> R.string.text_ending_minute_other
                }
        }
    }
}