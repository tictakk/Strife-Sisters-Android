package com.laconic.pcemulator.pce;

import com.laconic.pcemulator.pce.gpu.InterruptControl;
import com.laconic.pcemulator.pce.gpu.PSG;
import com.laconic.pcemulator.pce.gpu.Timer;
import com.laconic.pcemulator.pce.gpu.VCE;
import com.laconic.pcemulator.pce.gpu.VDC;
import com.laconic.pcemulator.pce.hucard.Hucard;
import com.laconic.pcemulator.pce.io.Gamepad;

public class PCEMMU {

    private int PHYSICAL_MEMORY = 0x200000;
    private static int VDC_ADDRESS = 0x1FE000;
    private static int VCE_ADDRESS = 0x1FE400;
    private static int PSG_PORTS = 0x1FE800;
    private static int TIMER_PORTS = 0x1FEC00;
    private static int IO_PORTS = 0x1FF000;
    private static int IRQ_REGS = 0x1FF000;

    private static int ROM_MAX_BANK = 0x7F;//127
    private static int BATTERY_RAM_BANK = 0xF7;
    private static int RAM_BANK = 0xF8;
    private static int HARDWARE_BANK = 0xFF;

    private static int ZERO_PAGE_MAX = 0x1F00FF;
    private static int ZERO_PAGE_MIN = 0x1F0000;
    private static int STACK_MIN = 0x1F0100;
    private static int STACK_MAX = 0x1F01FF;
    private static int RAM_MIN = 0x1F0200;
    private static int RAM_MAX = 0x1F1FFF;

    public byte[] MRP = new byte[8];
    public byte[] banks = new byte[0x100];
    byte[] stack = new byte[0x100];
    byte[] zerpPage = new byte[0x100];
    byte[] RAM = new byte[0x2000];

    /*
        Thus, the first block of memory, $0000 - $1FFF, is mapped using MPR0. 
        The next segment, $2000 - $3FFF, is mapped using MPR1, and so on. 
        These registers are set using the special TAM and TMA opcodes, described in the opcode section. 

        Memory Segment          MPR #          TAM / TMA argument 
        $E000 - $FFFF             7              10000000 
        $C000 - $DFFF             6              01000000 
        $A000 - $BFFF             5              00100000 
        $8000 - $9FFF             4              00010000  
        $6000 - $7FFF             3              00001000 
        $4000 - $5FFF             2              00000100 
        $2000 - $3FFF             1              00000010 
        $0000 - $1FFF             0              00000001 
    */
    /*
        Physical Addresses          Segment #          Description          Chip Enable Signal 
        1FFC00 - 1FFFFF            FF           Reserved for Expansion 
        1FF800 - 1FFBFF            "             Reserved for Expansion 
        1FF400 - 1FF7FF            "         Interrupt Req./Disable Registers      (/CECG) 
        1FF000 - 1FF3FF            "                    I/O Ports                  (/CEIO) 
        1FEC00 - 1FEFFF            "                   TIMER Ports                 (/CET) 
        1FE800 - 1FEBFF            "                     PSG Ports                 (/CEP) 
        1FE400 - 1FE7FF            "                  HuC6260 Ports                 /CEK 
        1FE000 - 1FE3FF            "                  HuC6270 Ports                 /CE7  
        1FC000 - 1FDFFF            FE 
        1FA000 - 1FBFFF            FD 
        1F8000 - 1F9FFF            FC 
        1F2000 - 1F7FFF          F9 - FB                                            /CER 
        1F0200 - 1F1FFF            F8              Base "scratchpad" RAM              " 
        1F0100 - 1F01FF             "                  Stack Page                     " 
        1F0000 - 1F00FF             "                  Zero Page                      "
        1EE000 - 1EFFFF            F7           Last page of HuCard memory 
        004000 - 1EDFFF         02 - F6             HuCard storage 
        002000 - 003FFF            01 
        001FFE - 001FFF            00                Reset Vector 
        001FFC - 001FFD             "                 NMI Vector 
        001FFA - 001FFB             "                 TIMER Vector 
        001FF8 - 001FF9             "                 IRQ1 Vector 
        001FF6 - 001FF7             "                 IRQ2 Vector (for BRK) 
        000000 - 001FF5             "           First page of HuCard memory 
    */

    private byte[] memory = new byte[0x200000]; //2MB of physical memory
    private Hucard card;
    public VDC vdc; //new VDC();
    private VCE vce;
    private InterruptControl ic = new InterruptControl();
    private PSG psg = new PSG();
    public Timer timer;
    private Gamepad gamepad = new Gamepad();

    public PCEMMU(Hucard card, Timer timer, InterruptControl ic, Gamepad gamepad, VCE vce){
        this.ic = ic;
        this.gamepad = gamepad;
        this.card = card;
        this.timer = timer;
        this.vce = vce;
        this.vdc = new VDC(ic,vce);
        resetMPR();
    }

    public PCEMMU(Hucard card, Timer timer, InterruptControl ic, Gamepad gamepad){
        this.ic = ic;
        this.gamepad = gamepad;
        this.card = card;
        this.timer = timer;
        resetMPR();
        // this.vdc = new VDC(ic);
    }

    public PCEMMU(Hucard card, Timer timer, InterruptControl ic,VDC vdc, Gamepad gamepad){
        this.ic = ic;
        this.card = card;
        this.timer = timer;
        resetMPR();
        this.vdc = vdc;
        this.vce = vdc.getVCE();
        this.gamepad = gamepad;
        // this.vdc = new VDC(ic); 
    }

    private void resetMPR(){
        for(int i=0; i<8; i++){
            // MRP[i] = (byte)0xFF;
            MRP[i] = 0;
        }
        MRP[7] = 0;
    }

    public void write(int logicalAddress, byte value){
        int physicalAddress = convertToPhysicalAddress(logicalAddress & 0xFFFF);
        if(logicalAddress == 8759){
            System.out.print("");
        }
        // System.out.println("writing "+String.format("0x%02X", (value & 0xFF))+" to "+String.format("0x%04X",physicalAddress));

        if(physicalAddress < 0x1EDFFF){

            System.out.println("should never write to ROM "+String.format("0x%04X",physicalAddress));

        }else if(physicalAddress >= ZERO_PAGE_MIN && physicalAddress < STACK_MIN){

            this.RAM[physicalAddress - 0x1F0000] = value;

        }else if(physicalAddress >= STACK_MIN && physicalAddress < RAM_MIN){

            this.RAM[physicalAddress - 0x1F0000] = value;

        }else if(physicalAddress >= RAM_MIN && physicalAddress <= RAM_MAX){

            // System.out.println("writing to RAM");
            this.RAM[physicalAddress - 0x1F0000] = value; //this may not work, we don't know what the logical address is going to be

        }else if(physicalAddress < PHYSICAL_MEMORY){ //Hardware is hit through here
            int hardwareAddress = physicalAddress - 0x1FE000;
            
            if(hardwareAddress < 0x3FF){

                this.vdc.write(hardwareAddress, (byte)value); //0 - VDC

            }else if(hardwareAddress < 0x7FF){

                // System.out.println("write to VCE");
                this.vce.write(hardwareAddress, (byte)value); //0x400 - VCE

            }else if(hardwareAddress < 0xBFF){ 

                // System.out.println("write to PSG");
                this.psg.write(hardwareAddress, (byte)value); //0x800 - PSG

            }else if(hardwareAddress < 0xFFF){

                // System.out.println("write to Timer");
                this.timer.write(hardwareAddress, (byte)value); //0xC00 - timer

            }else if(hardwareAddress < 0x13FF){

                // System.out.println("write to IO ports (I think)");
                this.gamepad.write(hardwareAddress, (byte)value); //0x1000 - gamepad

            }else if(hardwareAddress < 0x17FF){

                // System.out.println("write to Interrupt control");
                this.ic.write(hardwareAddress, (byte)value); //1402 - INT

            }else{
                // System.out.println("why are you writing at hardware " +String.format("0x%02X",hardwareAddress)+" ???");
            }

        }else{

            // System.out.println("UNKOWN ADDRESS WRITE: "+String.format("0x%02X",physicalAddress));
        }
    }

    public byte read(int logicalAddress){
        int lAddress = logicalAddress & 0xFFFF; //for debug purposes
        int physicalAddress = convertToPhysicalAddress(lAddress);

        if(logicalAddress == 8759){
            System.out.print("");
        }

        if(physicalAddress < 0x1EDFFF){

            if(physicalAddress < 0x100000){
                return this.card.read(physicalAddress);
            }else{
                return (byte) 0xFF; //not really sure how to handle this right now
            }

        }else if(physicalAddress >= ZERO_PAGE_MIN && physicalAddress < STACK_MIN){

            // System.out.println("read from ZP");
            return this.RAM[physicalAddress - 0x1F0000];

        }else if(physicalAddress >= STACK_MIN && physicalAddress < RAM_MIN){

            // System.out.println("read from stack");
            return this.RAM[physicalAddress - 0x1F0000];
            // return -1;

        }else if(physicalAddress >= RAM_MIN && physicalAddress <= RAM_MAX){

            // System.out.println("read from RAM");
            // return this.RAM[lAddress % 0x2000]; //this may not work, we don't know what the logical address is going to be
            return this.RAM[physicalAddress - 0x1F0000]; //this may not work, we don't know what the logical address is going to be

        }else if(physicalAddress < PHYSICAL_MEMORY){ //Hardware is hit through here

            int hardwareAddress = physicalAddress - 0x1FE000;
 
            if(hardwareAddress < 0x3FF){

                // System.out.println("read from VDC");
                byte value = this.vdc.read(hardwareAddress);
                return value;

            }else if(hardwareAddress < 0x7FF){ //STA 400

                // System.out.println("read from VCE");
                byte result = this.vce.read(hardwareAddress);
                return result;

            }else if(hardwareAddress < 0xBFF){

                // System.out.println("read from PSG");
                return this.psg.read(hardwareAddress);

            }else if(hardwareAddress < 0xFFF){

                // System.out.println("read from Timer");
                return this.timer.read(hardwareAddress);

            }else if(hardwareAddress < 0x13FF){

                // System.out.println("read from IO ports (I think)");
                return this.gamepad.read(hardwareAddress);

            }else if(hardwareAddress < 0x17FF){

                // System.out.println("read from Interrupt control");
                return this.ic.read(hardwareAddress);

            }else{

                // System.out.println("why are you reading at hardware " +String.format("0x%02X",hardwareAddress)+" ???");
                return -1;
            }

        }else{

            // System.out.println("UNKOWN ADDRESS READ: "+String.format("0x%02X",physicalAddress));
            return -1;
            // return this.card.read(address);
        }
    }

    public int convertToPhysicalAddress(int logical){
        // int mappedAddress = get(MRP[logical >> 13] & 0xFF);
        return ((MRP[logical >> 13] & 0xFF) << 13) | (logical & 0x1FFF); //clean this up
    }

    public byte zeroPageRead(byte address){
        return this.RAM[(address & 0xFF)];
        // return read(0x2000 + (address & 0xFF));
        // return this.memory[convertToPhysicalAddress((short)(0x2000 + (address & 0xFF)))];
    }

    public void zeroPageWrite(byte address, byte data){
        this.RAM[(address & 0xFF)] = data;
        // write(0x2000 + (address & 0xFF),data);
        // this.memory[convertToPhysicalAddress((short)(0x2000 + (address & 0xFF)))] = data;
    }

    public byte stackRead(byte address){
        int stackAddress = 0x100 + (address & 0xFF);
        return this.RAM[stackAddress];
        // return read(0x2100 + (address & 0xFF));
    }

    public void stackWrite(byte address, byte data){
        int stackAddress = 0x100 + (address & 0xFF);
        this.RAM[stackAddress] = data;
        // System.out.println("");
        //write(0x2100 + (address & 0xFF),data);

    }

    public void physicalWrite(int address, byte data){
        this.memory[address & 0x1FFFFF] = data;
    }
}