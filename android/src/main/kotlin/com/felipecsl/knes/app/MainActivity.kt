package com.felipecsl.knes.app

import android.annotation.SuppressLint
import android.content.ClipData
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.Layout
import android.util.Range
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.View.VISIBLE
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.felipecsl.android.NesGLSurfaceView
import com.felipecsl.knes.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.controlwear.virtual.joystick.android.JoystickView
import io.github.controlwear.virtual.joystick.android.JoystickView.OnMoveListener

class MainActivity : AppCompatActivity(), Runnable {
  private val nesGlSurfaceView by lazy { findViewById<NesGLSurfaceView>(R.id.nesGLSurfaceView) }
//  private val fabRun by lazy { findViewById<FloatingActionButton>(R.id.fabRun) }
//  private val btnReset by lazy { findViewById<AppCompatButton>(R.id.btnReset) }
//  private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
//  private val btnStart by lazy { findViewById<AppCompatButton>(R.id.btnStart) }
//  private val btnSelect by lazy { findViewById<AppCompatButton>(R.id.btnSelect) }
//  private val btnA by lazy { findViewById<AppCompatButton>(R.id.btnA) }
//  private val arrowUp by lazy { findViewById<AppCompatButton>(R.id.arrowUp) }
//  private val arrowDown by lazy { findViewById<AppCompatButton>(R.id.arrowDown) }
//  private val arrowLeft by lazy { findViewById<AppCompatButton>(R.id.arrowLeft) }
//  private val arrowRight by lazy { findViewById<AppCompatButton>(R.id.arrowRight) }
//  private val fpsNumber by lazy { findViewById<AppCompatTextView>(R.id.fpsNumber) }
  private val runBtn by lazy { findViewById<AppCompatButton>(R.id.runBtn) }
  private val selBtn by lazy { findViewById<AppCompatButton>(R.id.selBtn) }
  private val iiBtn by lazy { findViewById<FloatingActionButton>(R.id.iiBtn) }
  private val runButton by lazy { findViewById<FloatingActionButton>(R.id.iBtn) }
  private val joystick by lazy { findViewById<JoystickView>(R.id.dpadBtn) }
//  private val dpad by lazy { findViewById<FloatingActionButton>(R.id.dpadBtn) }
//  private val dpadLayout by lazy { findViewById<FrameLayout>(R.id.dpad_frame) }

  private val handlerThread = HandlerThread("Console Thread")
  private lateinit var handler: Handler
  private var isRunning = false
  private var isPaused = false
  @Volatile private var shouldSaveState = false
  @Volatile private var shouldRestoreState = false
  private val audioEngine = AudioEngineWrapper()
  private lateinit var director: Director
  @RequiresApi(Build.VERSION_CODES.M)
  private lateinit var glSprite: GLSprite
//  enum Buttons { Up = 0, Down, Left, Right, Select, Run, I, II };

  enum class Pad(i: Int){
    BUTTON_UP(0x1), BUTTON_DOWN(0x2), BUTTON_LEFT(0x3), BUTTON_RIGHT(0x4),
    BUTTON_SELECT(0x4), BUTTON_RUN(0x5), BUTTON_I(0x6), BUTTON_II(0x7)
  }
  @SuppressLint("ClickableViewAccessibility")
  private val onButtonTouched = { b: Pad ->
    View.OnTouchListener { _, e ->
      if (!isRunning) {
        false
      } else {
        when (e.action) {
          MotionEvent.ACTION_DOWN -> pressButton(b.ordinal)//director.controller1.onButtonDown(b)
          MotionEvent.ACTION_UP -> releaseButton(b.ordinal)//director.controller1.onButtonUp(b)
        }
        true
      }
    }
  }

//  @SuppressLint("ClickableViewAccessibility")
//  @RequiresApi(Build.VERSION_CODES.N)
//  private val onLayoutTouched = {
//    View.OnTouchListener{ v, e ->
//      if(e.action == MotionEvent.ACTION_DOWN)
//      {
//        val clip = ClipData.newPlainText("","")
//        val shadowBuilder = DragShadowBuilder(v)
//        v.startDragAndDrop(clip,shadowBuilder,v,0)
//        v.visibility = VISIBLE
//      }
//      true
//    }
//  }
//
//  private val onDrag = {
//    View.OnDragListener { v, e ->
////      println(e.action)
//      if(e.action == MotionEvent.ACTION_OUTSIDE){
//        releaseButton(Pad.BUTTON_DOWN.ordinal)
//        releaseButton(Pad.BUTTON_UP.ordinal)
//        releaseButton(Pad.BUTTON_LEFT.ordinal)
//        releaseButton(Pad.BUTTON_RIGHT.ordinal)
//      }else {
//        if (e.x > 0) {
//          val xVariance: Float = 70f / e.x
//          if (xVariance > 1.25f) {
//            pressButton(Pad.BUTTON_LEFT.ordinal)
//          } else {
//            releaseButton(Pad.BUTTON_LEFT.ordinal)
//          }
//
//          if(xVariance < 0.75f){
//            pressButton(Pad.BUTTON_RIGHT.ordinal)
//          }else{
//            releaseButton(Pad.BUTTON_RIGHT.ordinal)
//          }
//        }
//
//        if (e.y > 0) {
//          val yVariance: Float = 100f / e.y
//          if (yVariance < .75f) {
//            pressButton(Pad.BUTTON_DOWN.ordinal)
//          } else if (yVariance > 1.25f) {
//            pressButton(Pad.BUTTON_UP.ordinal)
//          }
//        }
//      }
//      true
//    }
//  }

  init {
    System.loadLibrary("ktnes-audio")
//    println("starting handler")
//    handlerThread.start()
//    handler = Handler(handlerThread.looper)
  }

  external fun loadROM(byteArray: ByteArray): Unit
  external fun runFrame(): Unit
  external fun pressButton(button: Int)
  external fun releaseButton(button: Int)

  @RequiresApi(Build.VERSION_CODES.M)
  @SuppressLint("ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
//    setContentView(R.layout.activity_main)
    setContentView(R.layout.full_screen_main)
//    setSupportActionBar(toolbar)
//    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//    val glSprite = GLSprite()
    glSprite = GLSprite()
    nesGlSurfaceView.setSprite(glSprite)
//    fabRun.setOnClickListener {
//      onClickPlayPause(glSprite)
//    }
//    runButton.setOnClickListener {
//      onClickPlayPause(glSprite)
//    }
//    btnReset.setOnClickListener {
//
//    }

    joystick.setOnMoveListener(JoystickView.OnMoveListener{ a, s ->
      releaseButton(Pad.BUTTON_LEFT.ordinal);
      releaseButton(Pad.BUTTON_RIGHT.ordinal);
      releaseButton(Pad.BUTTON_UP.ordinal);
      releaseButton(Pad.BUTTON_DOWN.ordinal);
      if(s > 30){//need to find the correct way to set the keystate instead of doing it by every bit every loop
        when(a){
          in 66..100 -> pressButton(Pad.BUTTON_UP.ordinal)
          in 25..65 -> {
            pressButton(Pad.BUTTON_UP.ordinal)
            pressButton(Pad.BUTTON_RIGHT.ordinal)
          }
          in 0..24 -> pressButton(Pad.BUTTON_RIGHT.ordinal)
          in 345..360 -> pressButton(Pad.BUTTON_RIGHT.ordinal)
          in 301..345 -> {
            pressButton(Pad.BUTTON_RIGHT.ordinal)
            pressButton(Pad.BUTTON_DOWN.ordinal)
          }
          in 245..300 -> pressButton(Pad.BUTTON_DOWN.ordinal)
          in 205..246 -> {
            pressButton(Pad.BUTTON_DOWN.ordinal)
            pressButton(Pad.BUTTON_LEFT.ordinal)
          }
          in 165..204 -> pressButton(Pad.BUTTON_LEFT.ordinal)
          in 120..164 -> {
            pressButton(Pad.BUTTON_LEFT.ordinal)
            pressButton(Pad.BUTTON_UP.ordinal)
          }
          in 99..119 -> pressButton(Pad.BUTTON_UP.ordinal)
        }
      }
//      println("angle: "+a)
//      println("str: "+s)
    })
    runButton.setOnTouchListener(onButtonTouched(Pad.BUTTON_I))
    iiBtn.setOnTouchListener(onButtonTouched(Pad.BUTTON_II))
    selBtn.setOnTouchListener(onButtonTouched(Pad.BUTTON_SELECT))
    runBtn.setOnTouchListener(onButtonTouched(Pad.BUTTON_RUN))
    onClickPlayPause(glSprite)
  }

  private fun onClickPlayPause(glSprite: GLSprite) {
//    val cartridgeData = resources.openRawResource(ROM).readBytes()
    val cartridgeData = resources.openRawResource(ROM).readBytes()
    loadROM(cartridgeData)

//    updatePlayPauseIcon()

    if (!isRunning) {
      if (!isPaused) {
        startConsole(cartridgeData, glSprite)
      } else {
        resumeConsole()
      }
    } else {
      pauseConsole()
    }
    isRunning = !isRunning
    isPaused = !isRunning
  }

  private fun pauseConsole() {
//    director.pause()
//    audioEngine.pause()
  }

  private fun resetConsole() {
//    director.reset()
//    audioEngine.stop()
  }

  private fun resumeConsole() {
    // Delay resuming the console so we have time to update the isRunning flag first
    isRunning = true
    handler.postDelayed(this, 100)
//    audioEngine.resume()
  }

  private fun updatePlayPauseIcon() {
//    val icon = if (!isRunning) R.drawable.ic_stat_name else R.drawable.ic_play_arrow_white_48dp
//    runButton.setImageDrawable(ContextCompat.getDrawable(this,icon))
//    fabRun.setImageDrawable(ContextCompat.getDrawable(this, icon))
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

  private fun startConsole(cartridgeData: ByteArray, glSprite: GLSprite) {
    runButton.setAlpha(.65f)
    iiBtn.setAlpha(.65f)
    joystick.setAlpha(.65f)
    runBtn.setAlpha(.65f)
    selBtn.setAlpha(.65f)

    audioEngine.start()
    glSprite.toggleRunState()
    handlerThread.start()
    handler = Handler(handlerThread.looper)
    handler.post(this)
  }

  override fun run() {
    while(true) {
      if (isRunning) {
        val startTime = currentTimeMs()
        runFrame();
//        Thread.sleep(8)
        val endTime = (currentTimeMs() - startTime).toLong()
        val msLeft = MS_PER_FRAME - endTime;
//        println(msLeft)
        if(msLeft > 0){
          Thread.sleep(msLeft)
        }
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return when (item.itemId) {
      R.id.action_save_state -> {
        director.pause()
        shouldSaveState = true
        handler.post(this)
        return true
      }
      R.id.action_restore_state -> {
        director.pause()
        shouldRestoreState = true
        handler.post(this)
        return true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun maybeSaveState() {
//    if (shouldSaveState) {
//      val stateMap = director.dumpState()
//      val sharedPrefs = getSharedPreferences(STATE_PREFS_KEY, Context.MODE_PRIVATE)
//      sharedPrefs.edit().also { p ->
//        stateMap.map { (k, v) ->
//          p.putString(k, v)
//        }
//      }.apply()
//      Snackbar.make(toolbar, "Game state saved", Snackbar.LENGTH_SHORT).show()
//      shouldSaveState = false
//    }
  }

  private fun maybeRestoreState() {
//    if (shouldRestoreState) {
//      val sharedPrefs = getSharedPreferences(STATE_PREFS_KEY, Context.MODE_PRIVATE)
//      val state = sharedPrefs.all
//      if (state.isNotEmpty()) {
//        director.restoreState(state)
//        Snackbar.make(toolbar, "Game state restored", Snackbar.LENGTH_SHORT).show()
//      }
//      shouldRestoreState = false
//    }
  }

  companion object {
    const val ROM = R.raw.strifesisters
//    const val ROM = R.raw.blazinglazers
//    const val ROM = R.raw.devilscrush
//    private const val STATE_PREFS_KEY = "KTNES_STATE"
    internal var staticDirector: Director? = null
    external fun getAudioBuffer(): IntArray?//FloatArray

    init {
      System.loadLibrary("ktnes-audio")
    }
//     Called from JNI AudioEngine
    @Suppress("unused")
    @JvmStatic fun audioBuffer(): IntArray? {
//      return staticDirector?.audioBuffer() ?: FloatArray(0)
      return getAudioBuffer() ?: IntArray(0)//FloatArray(0)
    }
  }
}
