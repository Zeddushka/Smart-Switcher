package ru.kartashovaa.toolkit

import android.app.Application
import java.lang.ref.WeakReference

/**
 * Created by Kartashov A.A. on 7/12/17.
 * This class used for providing class instances
 * Some kind of DI for classes without constructor params
 * Holding classes in a weak reference to avoid leaks and optimal memory usage
 */

object Component {
    private val instances = HashMap<String, WeakReference<Any>>()
    private val locks = HashMap<String, Any>()
    private val persistentComponents = HashSet<String>()
    private val persistentInstancesList = mutableListOf<Any>()
    private var applicationWeakRef: WeakReference<Application>? = null
    var applicationContext: Application
        get() {
            val app = applicationWeakRef?.get()
            if (app == null) {
                throw RuntimeException("Component application context is null. You must init component before using getComponent for repositories")
            } else {
                return app
            }
        }
        set(value) {
            if (applicationWeakRef != null) {
                throw RuntimeException("Component application context is not null. You must not call initializer for Component twice")
            } else {
                applicationWeakRef = WeakReference(value)
            }
        }


    /**
     * Return class instance if it already exists, crete new one otherwise
     * @param componentClass class of the requested object
     * @return componentClass instance
     */
    fun <T> getComponent(componentClass: Class<T>): T {
        val className = componentClass.name
        synchronized(getLockForClassName(className)) {
            var component: T? = instances[className]?.get() as T?
            if (component == null) {
                component = componentClass.newInstance()
                if (component is Repository) {
                    component.init(applicationContext)
                }
                if (className in persistentComponents) persistentInstancesList.add(component!!)
            }
            instances[componentClass.name] = WeakReference(component as Any)
            return component
        }
    }

    @Synchronized
    private fun getLockForClassName(className: String): Any {
        val lock = locks[className]
        return if (lock == null) {
            val newLock = Any()
            locks[className] = newLock
            newLock
        } else {
            lock
        }
    }

    fun persistComponents(vararg classes: Class<*>) {
        persistentComponents.addAll(classes.map { it -> it.name })
    }
}