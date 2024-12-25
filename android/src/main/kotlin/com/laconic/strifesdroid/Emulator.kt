package com.laconic.strifesdroid

import android.content.Context

const val FPS = 60
const val SECS_PER_FRAME = 1.0 / FPS
const val MS_PER_FRAME = (SECS_PER_FRAME * 1000).toLong()

class Emulator(private val romData: ByteArray) {
    private val audioEngine = AudioEngineWrapper()
    var isRunning = false
    val _isRunning get() = isRunning
    private lateinit var thread: Thread

    init {
        System.loadLibrary("ktnes-audio")
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
//    external fun reset(romData: ByteArray)

    fun startEmulator(){
        audioEngine.start()
        isRunning = true
        thread = Thread{
            while(true){
                if(isRunning) {
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
        isRunning = true
        audioEngine.start()
    }

    fun pauseEmulator(){
        isRunning = false
        audioEngine.stop()
    }

    fun saveEmulatorState(){
    }

    fun loadEmulatorState(){}

    companion object{
        private lateinit var emulator: Emulator
        val ROM = R.raw.strifesisters

        fun createInstance(context: Context): Emulator {
            emulator = Emulator(context.resources.openRawResource(ROM).readBytes())
            return emulator
        }
    }

}