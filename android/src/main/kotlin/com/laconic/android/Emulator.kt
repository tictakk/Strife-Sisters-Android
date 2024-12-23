package com.laconic.android

import android.content.Context
import com.felipecsl.knes.MS_PER_FRAME
import com.felipecsl.knes.currentTimeMs
import com.laconic.strifesdroid.AudioEngineWrapper
import com.laconic.strifesdroid.R

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
    external fun saveState()
    external fun loadState()

    fun startEmulator(){
        audioEngine.start()
        isRunning = true
        thread = Thread{
            while(true){
                if(isRunning) {
                    val startTime = currentTimeMs()

                    runFrame()

                    val endTime = (currentTimeMs() - startTime).toLong()
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
    fun saveEmulatorState(){}
    fun loadEmulatorState(){}

    companion object{
        private lateinit var emulator: Emulator
        const val ROM = R.raw.strifesisters

        fun createInstance(context: Context): Emulator{
            emulator = Emulator(context.resources.openRawResource(ROM).readBytes())
            return emulator
        }
    }

}