package ru.kartashovaa.idledisabler.util


/**
 * Created by Kartashov A.A. on 2/16/18.
 * Wrapping log. While debugging, allow to easily trace all logs to file
 */
object Log {
    private const val TAG = "APP_LOG"

    fun e(any: Any, e: Throwable, tag: String = TAG) {
        android.util.Log.e(tag, any.toString(), e)
    }

    fun d(any: Any, tag: String = TAG) {
        android.util.Log.d(tag, any.toString())
    }

    fun i(any: Any, tag: String = TAG) {
        android.util.Log.i(tag, any.toString())
    }
}