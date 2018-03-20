package ru.kartashovaa.toolkit

/**
 * Created by Kartashov A.A. on 27/10/17.
 * Basic realisation of interactor, paired to presenter
 * Abstract field must be overloaded with proper presenter class
 */

abstract class BaseInteractor<out P : Presenter<*>> {

    abstract val presenterClass: Class<*>
    val presenter: P
        get() = Component.getComponent(presenterClass) as P

}