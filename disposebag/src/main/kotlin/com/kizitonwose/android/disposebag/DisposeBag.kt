package com.kizitonwose.android.disposebag

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer

/**
 * Created by Kizito Nwose
 */

fun Disposable.disposedBy(bag: DisposeBag) = bag.add(this)

object DisposeBagPlugins {
    @JvmStatic var defaultLifecycleDisposeEvent = Lifecycle.Event.ON_DESTROY
}

class DisposeBag @JvmOverloads constructor(owner: LifecycleOwner,
                                           private val event: Lifecycle.Event = DisposeBagPlugins.defaultLifecycleDisposeEvent)
    : Disposable, DisposableContainer, DefaultLifecycleObserver {

    @JvmOverloads constructor(resources: Iterable<Disposable>,
                              owner: LifecycleOwner,
                              event: Lifecycle.Event = DisposeBagPlugins.defaultLifecycleDisposeEvent)
            : this(owner, event) {

        resources.forEach { composite.add(it) }
    }

    private val TAG = this::class.java.simpleName

    private val lifecycle = owner.lifecycle

    private val composite by lazy { CompositeDisposable() }

    init {
        lifecycle.addObserver(this)
    }

    fun size() = composite.size()

    fun clear() = composite.clear()

    override fun isDisposed() = composite.isDisposed

    override fun dispose() {
        lifecycle.removeObserver(this)
        composite.dispose()
    }

    override fun add(d: Disposable) = composite.add(d)

    override fun remove(d: Disposable) = composite.remove(d)

    override fun delete(d: Disposable) = composite.delete(d)

    override fun onPause(owner: LifecycleOwner) {
        Log.d(TAG, "onPause, composite={isDisposed: " + composite.isDisposed + ", size: " + composite.size() + "}")

        if (event == Lifecycle.Event.ON_PAUSE) dispose()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "onStop, composite={isDisposed: " + composite.isDisposed + ", size: " + composite.size() + "}")
        composite.clear()

        if (event == Lifecycle.Event.ON_STOP) dispose()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "onDestroy, composite={isDisposed: " + composite.isDisposed + ", size: " + composite.size() + "}")

        if (event == Lifecycle.Event.ON_DESTROY) dispose()
    }
}

