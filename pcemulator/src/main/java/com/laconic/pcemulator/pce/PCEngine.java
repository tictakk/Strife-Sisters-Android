package com.strifesdroid.pcemulator.pce;

import com.strifesdroid.pcemulator.pce.cpu.HuC6280;
import com.strifesdroid.pcemulator.pce.gpu.VCE;
import com.strifesdroid.pcemulator.pce.gpu.VDC;
import com.strifesdroid.pcemulator.pce.hucard.Hucard;
import com.strifesdroid.pcemulator.pce.io.Gamepad;
import com.strifesdroid.pcemulator.util.PceReader;
import com.strifesdroid.pcemulator.pce.gpu.InterruptControl;
import com.strifesdroid.pcemulator.pce.gpu.Timer;

public class PCEngine {
    
    public HuC6280 cpu;
    PCEMMU mmu;
    Hucard hucard;
    InterruptControl ic;
    public Timer timer;

    VDC vdc;
    public Gamepad controllerOne;

    private long startTime = 0;

    public PCEngine(){
         this.hucard = new Hucard(PceReader.readROM("C:\\Users\\Matth\\AndroidStudioProjects\\StrifeSistersDX\\pcemulator\\src\\main\\assets\\strifesisters.pce"));
        this.ic = new InterruptControl();
        // this.hucard = new Hucard(PceReader.readROM("/home/matthew/Downloads/pce_test.pce"));
        this.timer = new Timer(ic);
//        this.mmu = new PCEMMU(this.hucard,timer,ic,new VDC(ic));
        this.cpu = new HuC6280(this.mmu,ic);
    }

    public PCEngine(byte[] rom, VDC vdc){
//        this.hucard = new Hucard(PceReader.readROM("C:\\Users\\Matth\\AndroidStudioProjects\\StrifeSistersDX\\pcemulator\\src\\main\\assets\\strifesisters.pce"));
        this.hucard = new Hucard(rom);
        this.ic = new InterruptControl();
        // this.hucard = new Hucard(PceReader.readROM("/home/matthew/Downloads/pce_test.pce"));
        this.timer = new Timer(ic);
        this.mmu = new PCEMMU(this.hucard,timer,ic,vdc,null);
        this.cpu = new HuC6280(this.mmu,ic);
    }

    public PCEngine(byte[] romData){
        controllerOne = new Gamepad();
        this.hucard = new Hucard(romData);
        this.ic = new InterruptControl();
        this.timer = new Timer(ic);
        this.vdc = new VDC(this.ic,new VCE());
        this.mmu = new PCEMMU(this.hucard,this.timer,this.ic,this.vdc,controllerOne);
        this.cpu = new HuC6280(this.mmu,this.ic);
    }

    public void step(){
        this.cpu.decode();
        this.vdc.cycles(this.cpu.clock);
        timer.cycle(this.cpu.clock);
    }

    public int stepWithCycles(){
        step();
        return this.cpu.clock;
    }

    public int[] getVideoBuffer() {
//        System.out.println("getting video buffer");
        return this.vdc.getIntArrayDisplay();
    }

    public int[] getAudioBuffer(){
        return new int[1];
    }

    public void reset() {

    }

    public long stepToFrameReady(){
        this.startTime = System.currentTimeMillis();

        while(!this.vdc.frameReady()){
            this.cpu.decode();
            this.vdc.cycles(this.cpu.clock);
            this.timer.cycle(this.cpu.clock);
        }
        return this.startTime - System.currentTimeMillis();
    }

    public static String getCum(){
        return "cum";
    }
}