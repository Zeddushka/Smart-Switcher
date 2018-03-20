package ru.kartashovaa.toolkit

/**
 * Created by Kartashov A.A. on 26/10/17.
 * This interface describe the common part of any presenter
 */

interface Presenter<in T : View> {
    /**
     * Attach view to the presenter
     * @param view the view must be attached
     */
    fun attachView(view: T)

    /**
     * Detach view from presenter
     */
    fun detachView()
}