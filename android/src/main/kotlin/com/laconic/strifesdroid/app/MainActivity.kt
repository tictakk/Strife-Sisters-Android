package com.laconic.strifesdroid.app

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.felipecsl.knes.MS_PER_FRAME
import com.felipecsl.knes.currentTimeMs
import com.laconic.strifesdroid.R
import com.laconic.android.NesGLSurfaceView
import com.laconic.android.GamepadOverlay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.laconic.android.Emulator
import com.laconic.strifesdroid.AudioEngineWrapper
import com.laconic.strifesdroid.GLSprite
import com.laconic.util.RecyclerAdapter
import io.github.controlwear.virtual.joystick.android.JoystickView

class MainActivity : AppCompatActivity(), Runnable {
  private val nesGlSurfaceView by lazy { findViewById<NesGLSurfaceView>(R.id.nesGLSurfaceView) }
  private val gamepadOverlay by lazy { findViewById<GamepadOverlay>(R.id.gamepadOverlay) }
  private lateinit var emulator: Emulator

  private val handlerThread = HandlerThread("Console Thread")
  private lateinit var handler: Handler
  private lateinit var glSprite: GLSprite

  @RequiresApi(Build.VERSION_CODES.M)
  @SuppressLint("ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.full_screen_main)

    glSprite = GLSprite()
    nesGlSurfaceView.setSprite(glSprite)
    emulator = Emulator.createInstance(this)
    glSprite.toggleRunState()
    gamepadOverlay.attachActivity(this)
  }

  fun toggleConsoleState(){
    if(emulator._isRunning){
      pauseConsole()
    }else{
      resumeConsole()
    }
  }

  fun pauseConsole() {
    emulator.pauseEmulator()
  }

  private fun resetConsole() {
  }

  private fun resumeConsole() {
    emulator.resumeEmulator()
  }

  private fun updatePlayPauseIcon() {
  }

//  private fun updateFPS(ms: Long){
//    totalMs += ms
//    totalFrames++
//    if(totalMs == 0.0) {
//      fpsNumber.setText("fps: ${ms}")
//    }else{
//      val fps = totalFrames / (totalMs/1_000.0)
//      fpsNumber.setText("fps: ${fps}")
//    }
//  }

  override fun run() {
//    while(true) {
//      if (isRunning) {
//        val startTime = currentTimeMs()
//        runFrame();
//
//        val endTime = (currentTimeMs() - startTime).toLong()
//        val msLeft = MS_PER_FRAME - endTime;
//
//        if(msLeft > 0){
//          Thread.sleep(msLeft)
//        }
//      }
//    }
  }

  private fun maybeSaveState() {
  }

  private fun maybeRestoreState() {
  }

//  companion object {
//    const val ROM = R.raw.strifesisters
////    private const val STATE_PREFS_KEY = "KTNES_STATE"
////    internal var staticDirector: Director? = null
//
//    external fun getAudioBuffer(): IntArray?//FloatArray
//
//    init {
//      System.loadLibrary("ktnes-audio")
//    }
////     Called from JNI AudioEngine
//    @Suppress("unused")
//    @JvmStatic fun audioBuffer(): IntArray? {
//      return getAudioBuffer() ?: IntArray(0)//FloatArray(0)
//    }
//  }
}
