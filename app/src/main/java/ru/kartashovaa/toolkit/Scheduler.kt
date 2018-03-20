package ru.kartashovaa.toolkit

import android.os.Looper
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Kartashov A.A. on 2/19/18.
 * Android UI Scheduler
 */

object Scheduler {
    val ui: Scheduler = AndroidSchedulers.from(Looper.getMainLooper())
}