package ru.kartashovaa.toolkit

import android.app.Application

/**
 * Created by Kartashov A.A. on 22/12/2017.
 * Method init must be called before use the repository (after object creation)
 */

interface Repository {
    fun init(application: Application)
}