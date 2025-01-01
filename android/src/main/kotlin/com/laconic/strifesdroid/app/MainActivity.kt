package com.laconic.strifesdroid.app

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

//import com.laconic.strifesdroid.NesGLSurfaceView
//import com.laconic.strifesdroid.GamepadOverlay
//import com.laconic.strifesdroid.Emulator
//import com.laconic.strifesdroid.GLSprite

import com.laconic.hyperxengine.NesGLSurfaceView
import com.laconic.hyperxengine.GamepadOverlay
import com.laconic.hyperxengine.Emulator
import com.laconic.hyperxengine.GLSprite

import com.laconic.hyperxengine.R
import java.io.File

import com.laconic.hyperxengine.EmulatorActivity
import com.laconic.hyperxengine.UIState

class MainActivity : AppCompatActivity(), EmulatorActivity {
  private val nesGlSurfaceView: NesGLSurfaceView by lazy { findViewById(R.id.nesGLSurfaceView) }
  private val gamepadOverlay: GamepadOverlay by lazy { findViewById(R.id.gamepadOverlay) }
  private lateinit var emulator: Emulator

  private val handlerThread = HandlerThread("Console Thread")
  private lateinit var handler: Handler
  private lateinit var glSprite: GLSprite

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN)
    setContentView(com.laconic.hyperxengine.R.layout.full_screen_main)//R.layout.full_screen_main)

    glSprite = GLSprite()
    nesGlSurfaceView.setSprite(glSprite)
    emulator = Emulator.createInstance(this, com.laconic.strifesdroid.R.raw.strifesisters)
    glSprite.toggleRunState()
    gamepadOverlay.attachActivity(this)
    gamepadOverlay.setOnClickListener{
      if(emulator.isPaused()){
        toggleConsoleState()
        gamepadOverlay.hideDropdown()
      }
    }
  }

  override fun toggleConsoleState(){
//    if(emulator.emulatorState == EmulatorState.RUNNING){
    if(!emulator.isPaused()){
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

  override fun maybeSaveState() {
    save()
    toggleConsoleState()
  }

  private fun save(){
    emulator.saveState(File(filesDir.absolutePath+"/saveState.mss").absolutePath)
    Toast.makeText(this.applicationContext,"Game state saved",Toast.LENGTH_LONG).show()
  }

  override fun maybeRestoreState() {
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

  override fun reset(){
    emulator.reset()
    toggleConsoleState()
  }
}
