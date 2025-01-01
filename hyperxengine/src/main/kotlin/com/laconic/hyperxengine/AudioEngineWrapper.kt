package com.laconic.hyperxengine

import android.os.Build
import android.util.Log

class AudioEngineWrapper {
  fun start() {
//    println("starting audio engine?")
    startAudioEngine(getExclusiveCores())
  }

  fun stop() {
    stopAudioEngine()
  }

  fun resume() {
    resumeAudioEngine()
  }

  fun pause() {
    pauseAudioEngine()
  }

  // Obtain CPU cores which are reserved for the foreground app. The audio thread can be
  // bound to these cores to avoids the risk of it being migrated to slower or more contended
  // core(s).
  private fun getExclusiveCores(): IntArray {
    try {
      return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        Log.w(
          TAG,
          "getExclusiveCores() not supported. Only available on API ${Build.VERSION_CODES.N}+"
        )
        intArrayOf()
      } else {
        android.os.Process.getExclusiveCores()
      }
    }catch(e: Exception) {
      Log.w(
        TAG,
        "getExclusiveCores() not supported. Only available on this device"
      )
      return intArrayOf()
    }
  }

  companion object {
    private const val TAG = "AudioEngineWrapper"

    init {
      System.loadLibrary("hyperxengine-audio")
    }
  }
}