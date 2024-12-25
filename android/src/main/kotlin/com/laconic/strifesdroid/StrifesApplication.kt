package com.laconic.strifesdroid

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class StrifesApplication : Application() {

    class SimpleLifecycleListener : DefaultLifecycleObserver{
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            println("lifecycle created")
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            println("lifecycle stopped")
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            println("lifecycle paused")
        }
    }

    private val lifecycleListener: SimpleLifecycleListener = SimpleLifecycleListener()
}

