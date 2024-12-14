package com.laconic.pcemulator.pce.gpu;

public class Timer {

    private final static int CLOCK_CYCLES = 1_023;
    private InterruptControl ic;

    private byte control = 0;
    private byte counter = 0;
    private boolean enabled = false;

    private int cyclesCounter = CLOCK_CYCLES;

    public Timer(InterruptControl ic){
        this.ic=ic; 
    }

    public void cycle(int cycles){
        cyclesCounter -= cycles;
        if(enabled){
            if(cyclesCounter < 0){
                this.cyclesCounter = CLOCK_CYCLES;
                counter--;
            }
            if(counter <= 0){
                this.ic.requestInterrupt(InterruptControl.InterruptType.TIQ);
                this.counter = (byte)(control+1);
            }
        }
    }

    public byte read(int address){
        return 1;
    }

    public void write(int address, byte data){
        switch(address){
            case 0xC00:
            this.control = (byte)(data & 0x7F);
            break;

            case 0xC01:
            this.enabled = (data == 0)? false : true;
            break;
            
            default:
            // System.out.println("error writing to timer address: "+address);
            break;

        }
    } 
}