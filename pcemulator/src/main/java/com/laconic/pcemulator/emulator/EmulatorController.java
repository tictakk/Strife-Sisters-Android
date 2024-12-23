package com.strifesdroid.pcemulator.emulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class EmulatorController extends Thread {

//    private List<Thread> emulators;
    private List<Thread> emulators;
//    private ThreadPoolExecutor executor;

    public EmulatorController(){
        emulators = new ArrayList<Thread>();
    }



}
