package com.strifesdroid.pcemulator.emulator;

//import com.laconic.gameboy.GBProcessor;

public class LinkedVM extends VM {

    // private Emulator slave;
    private VM m;
    private VM s;

    public LinkedVM(VM master, VM slave) {
        // super();
        this.m = master;
        this.s = slave;
//        ((GBProcessor)this.m.emulator).serial.endpoint = ((GBProcessor)this.s.emulator).serial;
        this.isAlive = true;
    }

    /*
     * master and slave need to run in tandem, which is one this one has to implement
     * stepToScreen differently 
     */
     
    @Override
    public void stepToScreenUpdate(){
        int cycles = 0;
        while(cycles < MAX_CYCLES){
            this.m.emulator.decode();
            this.s.emulator.decode();
            cycles += this.m.emulator.getCurrentCycles();
        }
    }

    /*
     * Just need to run video update on both of these
     */
    @Override
    public void updateVideoDisplay(){
        m.updateVideoDisplay();
        s.updateVideoDisplay();
    }

}