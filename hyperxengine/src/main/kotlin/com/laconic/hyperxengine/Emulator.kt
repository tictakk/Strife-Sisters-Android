package com.laconic.hyperxengine

import android.content.Context

const val FPS = 60
const val SECS_PER_FRAME = 1.0 / FPS
const val MS_PER_FRAME = (SECS_PER_FRAME * 1000).toLong()

public enum class EmulatorState{
    RUNNING, PAUSED, SAVING, LOADING, EXITED
}

class Emulator(private val romData: ByteArray) {
    private val audioEngine = AudioEngineWrapper()
    private var _emulatorState: EmulatorState = EmulatorState.EXITED
    val emulatorState get() = _emulatorState

    var isRunning = false
    val _isRunning get() = isRunning
    private lateinit var thread: Thread

    init {
        System.loadLibrary("hyperxengine-audio")
        loadROM(romData)
        startEmulator()
    }

    external fun loadROM(romData: ByteArray)
    external fun runFrame(): Unit
    external fun saveState(file: String)
    external fun loadState(file: String)

    fun reset(){
        loadROM(romData)
    }

    fun startEmulator(){
        audioEngine.start()
        isRunning = true
        _emulatorState = EmulatorState.RUNNING
        thread = Thread{
            while(true){
//                if(isRunning) {
                if(_emulatorState == EmulatorState.RUNNING){
                    val startTime = System.currentTimeMillis().toDouble()

                    runFrame()

                    val endTime = (System.currentTimeMillis().toDouble() - startTime).toLong()
                    val msLeft = MS_PER_FRAME - endTime;

                    if (msLeft > 0) {
                        Thread.sleep(msLeft)
                    }
                }
            }
        }
        thread.start()
        audioEngine.start()
    }

    fun resumeEmulator(){
        _emulatorState = EmulatorState.RUNNING
        audioEngine.start()
    }

    fun pauseEmulator(){
        _emulatorState = EmulatorState.PAUSED
        audioEngine.stop()
    }

    fun saveEmulatorState(){
    }

    fun loadEmulatorState(){}

    fun isPaused(): Boolean{
        return emulator.emulatorState == EmulatorState.RUNNING
    }

    companion object{
        private lateinit var emulator: Emulator
        val ROM = R.raw.rom

        fun createInstance(context: Context, id: Int): Emulator {
            emulator = Emulator(context.resources.openRawResource(id).readBytes())
            return emulator
        }
    }

}