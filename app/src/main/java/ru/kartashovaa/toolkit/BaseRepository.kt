package ru.kartashovaa.toolkit

import android.app.Application

/**
 * Created by Kartashov A.A. on 22/12/2017.
 * The basic repository class. In common situation,
 * the repository mean to use app context (preferences, databases, e.t.c.)
 * init(application) is called for Repository automatically by Component
 */

abstract class BaseRepository : Repository {
    private var applicationBackend: Application? = null
    val application: Application
        get() {
            if (applicationBackend == null) {
                throw RuntimeException("You must call init(application) before using ${javaClass.name} repository!")
            } else {
                return applicationBackend!!
            }
        }

    override fun init(application: Application) {
        applicationBackend = application
    }
}