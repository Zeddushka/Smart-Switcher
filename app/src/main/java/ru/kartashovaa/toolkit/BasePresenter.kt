package ru.kartashovaa.toolkit

/**
 * Created by Kartashov A.A. on 27/10/17.
 * Basic realisation of presenter, paired to view+interactor
 * Abstract field must be overloaded with proper interactor class
 */

abstract class BasePresenter<V : View, out I : Interactor<*>> : Presenter<V> {
    var view: V? = null

    abstract val interactorClass: Class<*>
    val interactor: I
        get() = Component.getComponent(interactorClass) as I

    override fun attachView(view: V) {
        this.view = view
        onViewAttached()
    }

    override fun detachView() {
        view = null
    }

    /**
     * Called at the end of method attachView
     * In this method checking state logic must be written
     */
    abstract fun onViewAttached()
}