package com.laconic.strifesdroid.app

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
//import com.laconic.R
//import com.laconic.strifesdroid.R
import com.laconic.strifesdroid.NesGLSurfaceView
import com.laconic.strifesdroid.GamepadOverlay
import com.laconic.strifesdroid.Emulator
import com.laconic.strifesdroid.GLSprite
import com.laconic.strifesdroid.R
import java.io.File

//create emulator state here

public enum class EmulatorState{
  RUNNING, PAUSED, SAVING, LOADING, EXITED
}

enum class UIState{
  ACTIVE, BACKGROUND, MENU
}

class MainActivity : AppCompatActivity() {
  private val nesGlSurfaceView by lazy { findViewById<NesGLSurfaceView>(R.id.nesGLSurfaceView) }
  private val gamepadOverlay by lazy { findViewById<GamepadOverlay>(R.id.gamepadOverlay) }
  private lateinit var emulator: Emulator
//  private var _uiState: UIState = UIState.ACTIVE
//  val uiState get() = _uiState

  private val handlerThread = HandlerThread("Console Thread")
  private lateinit var handler: Handler
  private lateinit var glSprite: GLSprite

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN)
    setContentView(R.layout.full_screen_main)

    glSprite = GLSprite()
    nesGlSurfaceView.setSprite(glSprite)
    emulator = Emulator.createInstance(this)
    glSprite.toggleRunState()
    gamepadOverlay.attachActivity(this)
    gamepadOverlay.setOnClickListener{
      if(emulator.emulatorState == EmulatorState.PAUSED){
        toggleConsoleState()
        gamepadOverlay.hideDropdown()
      }
    }
  }

  fun toggleConsoleState(){
    if(emulator.emulatorState == EmulatorState.RUNNING){
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

  override fun onResume() {
    super.onResume()
    if(gamepadOverlay.uiState == UIState.ACTIVE){
      emulator.resumeEmulator()
    }
  }

  override fun onStop() {
    super.onStop()
    emulator.pauseEmulator()
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

//  override fun run() {
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
//  }

  fun maybeSaveState() {
    save()
    toggleConsoleState()
  }

  private fun save(){
    emulator.saveState(File(filesDir.absolutePath+"/saveState.mss").absolutePath)
    Toast.makeText(this.applicationContext,"Game state saved",Toast.LENGTH_LONG).show()
  }

  fun maybeRestoreState() {
    load()
    toggleConsoleState()
  }

  private fun load(){
    val file = File(filesDir.absolutePath+"/saveState.mss")
    if(file.exists()){
      emulator.loadState(file.absolutePath)
      Toast.makeText(this.applicationContext,"Game state loaded",Toast.LENGTH_LONG).show()
    }else{
      Toast.makeText(this.applicationContext,"Save state doesn't exist",Toast.LENGTH_LONG).show()
    }
  }

  fun reset(){
    emulator.reset()
    toggleConsoleState()
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
