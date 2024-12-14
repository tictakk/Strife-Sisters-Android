package com.laconic.pcemulator.pce.gpu;

public class InterruptControl {

    private final static int RESET_ADDRESS = 0x1FFE;
    private final static int NMI_ADDRESS = 0x1FFC;
    private final static int TIQ_ADDRESS = 0xFFFA; //timer
    private final static int IRQ1_ADDRESS = 0xFFF8; //VDC
    private final static int IRQ2_ADDRESS = 0xFFF6;
    private final static int BRK_ADDRESS  = 0x1FF6;

    // RESET > NMI > TIQ > IRQ1 > IRQ2 > BRK

    private byte InterruptMask = 0;//(byte)0b11111111;//enable register -- 0x1402 --controlled by the programmer
    private byte IDR = 0;//disable register -- 0x1403 --controlled by the hardware

    public enum InterruptType{
        // NMI, IRQ1, IRQ2, BRK, RESET, TIQ;
        IRQ2(1){
            @Override
            public boolean requested(byte req){
                return mask==((~req) & 0b001);
            }
        },
        IRQ1(2){
            @Override
            public boolean requested(byte req){
                return mask==((~req) & 0b010);
            }
        },
        TIQ(4){
            @Override
            public boolean requested(byte req){
                return mask==((~req) & 0b100);
            }
        };

        byte mask;

        InterruptType(int data){
            mask = (byte)data;
        }

        public boolean requested(byte req){
            return false;
        }
    }

    public byte read(int address){
        switch(address){

            case 0x1402:
            return this.InterruptMask;

            case 0x1403:
            return this.IDR;

            default:
            System.out.println("bad Interrupt controller read address: "+address);
            return -1;
        }
    }

    public void write(int address, byte data){
        switch(address){
            case 0x1402:
            this.InterruptMask = (byte)(data & 0x7);
            break;

            case 0x1403:
            this.IDR &= 0b11; //clearing timer
            break;
        }
    }

    public void acknowledgeInterrupt(InterruptType interrupt){
        switch(interrupt){

            case IRQ2:
            this.IDR |= 0b110;
            break;

            case IRQ1:
            this.IDR &= 0b101;
            break;

            case TIQ:
            this.IDR &= 0b11;//don't think this can ever be called
            break;

            default:
            break;

        }
    }

    public short getInterruptVector(){

        if( (((~this.InterruptMask) & this.IDR) & 1) != 0 ){
            return (short)IRQ2_ADDRESS;
        }
        if( (((~this.InterruptMask) & this.IDR) & 2) != 0 ){
            // this.IDR = 
            return (short)IRQ1_ADDRESS;
        }
        if( (((~this.InterruptMask) & this.IDR) & 4) != 0 ){
            return (short)TIQ_ADDRESS;
        }

        System.out.println("error returning interrupt vector");
        return -1;
    }

    public boolean availableInterrupt(){
        return ((~this.InterruptMask) & this.IDR) != 0;
    }

    //only the hardward can call this
    public void requestInterrupt(InterruptType interrupt){
        switch(interrupt){
            
            case IRQ2:
            this.IDR |= 0b001;
            break;

            case IRQ1:
            this.IDR |= 0b010;
            break;

            case TIQ:
            this.IDR |= 0b100;
            break;

            default:
            break;
        }
    }
}