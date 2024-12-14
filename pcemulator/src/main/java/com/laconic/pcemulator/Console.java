package com.laconic.pcemulator;

import com.laconic.pcemulator.pce.PCEngine;
import com.laconic.pcemulator.pce.io.Gamepad;

public class Console {
    private final long CONSOLE_FREQ = 1_789_772;
    private final int FPS = 60;
    public final double SECS_PER_FRAME = 1.0/FPS;
    public final long MS_PER_FRAME = (long)(SECS_PER_FRAME * 1_000);
    public int fps = 0;
    private double totalFrames = 0.0;
    private double totalMs = 0.0;

    private PCEngine pce;
    private boolean isRunning = false;

    public Console(byte romData[]){
        this.pce = new PCEngine(romData);
    }

    public long stepSeconds(double seconds){
//        val startTime =
//        double cyclesToRun = seconds * (double)CONSOLE_FREQ;
//        double totalCycles = 0.0;
//        long startTime = System.currentTimeMillis();
//        isRunning = true;
//        while(isRunning == true && totalCycles < cyclesToRun){
//            totalCycles += this.pce.stepWithCycles();
//        }
//        return (System.currentTimeMillis() - startTime);
        return 0;
    }

    public long stepToFrameReady(){
        return this.pce.stepToFrameReady();
    }

    public void updateFPS(double ms){
        totalMs += ms;
        totalFrames++;
        if(totalMs == 0.0) {
            System.out.println("fps: "+ms);
        }else{
            double fps = totalFrames / (totalMs/1_000.0);
            System.out.println("fps: "+fps);
        }
    }

    public int[] getVideoBuffer(){ return this.pce.getVideoBuffer(); }


    public void startConsole(){
        if(!this.isRunning){
            this.isRunning = true;
        }
    }

    public void stopConsole(){
        if(isRunning){
            this.isRunning = false;
        }
    }

    public void resetConsole(){
        if(this.isRunning)
        {
            this.pce.reset();
        }
    }

    public void buttonDown(Gamepad.button data){
        System.out.println(data);
        this.pce.controllerOne.buttonDown(data);
    }

    public void buttonUp(Gamepad.button data){
        System.out.println(data);
        this.pce.controllerOne.buttonRelease(data);
    }
}
