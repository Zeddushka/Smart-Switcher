package ru.kartashovaa.toolkit

import android.app.Activity


/**
 * Created by Kartashov A.A. on 27/10/17.
 * Basic realisation of activity, paired to presenter
 * Abstract field must be overloaded with proper presenter class
 * Presenter is hold in the hard link until the view detached
 * Attaching and detaching view performed in onStart and onStop methods, so
 * When you override this methods, you MUST call super method
 */

abstract class BaseActivity<in V : View, out P : Presenter<V>> : Activity(), View {

    abstract val presenterClass: Class<*>
    private var presenterHardLink: P? = null

    val presenter: P
        get() {
            if (presenterHardLink == null) {
                presenterHardLink = Component.getComponent(presenterClass) as P
            }
            return presenterHardLink!!
        }

    override fun onStart() {
        super.onStart()
        attachToPresenter()
    }

    override fun onStop() {
        detachFromPresenter()
        super.onStop()
    }

    private fun attachToPresenter() {
        presenter.attachView(this as V)
    }

    private fun detachFromPresenter() {
        presenter.detachView()
        presenterHardLink = null
    }
}