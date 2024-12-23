package com.strifesdroid.pcemulator.pce.cpu;

public class REGISTERS{
    
    /*
    * STATUS REGISTER
    * 0 - Carry - C
    * 1 - Zero - Z
    * 2 - Interrupt - I
    * 3 - Decmial - D  
    * 4 - Break - B
    * 5 - Control - T
    * 6 - Overflow - V
    * 7 - Negative - N
    */ 

    public short PC = 0;
    public byte SP = (byte)0xFF;
    public byte Y;
    public byte X;
    public byte A;
    public byte status;

    public byte getStatus(){
        return this.status;
    }

    public void setCarry(){
        this.status = (byte) (this.status |  0b00000001);
    }

    public void resetCarry(){
        this.status = (byte)(this.status & 0b11111110);
    }

    public void setZero(){
        this.status = (byte) (this.status |  0b00000010);
    }

    public void resetZero(){
        this.status = (byte)(this.status & 0b11111101);
    }

    public void setInterrupt(){
        this.status = (byte) (this.status |  0b00000100);
    }

    public void resetInterrupt(){
        this.status = (byte)(this.status & 0b11111011);
    }

    public void setBCD(){
        this.status = (byte) (this.status |  0b00001000);
    }

    public void resetBCD(){
        this.status = (byte)(this.status & 0b11110111);
    }

    public void setBreak(){
        this.status = (byte) (this.status | 0b00010000);
    }

    public void resetBreak(){
        this.status = (byte)(this.status & 0b11101111);
    }

    public void setControl(){
        this.status = (byte)(this.status | 0b00100000);
    }

    public void resetControl(){
        this.status = (byte)(this.status & 0b11011111);
    }

    public void setOverflow(){
        this.status = (byte)(this.status |  0b01000000);
    }

    public void resetOverflow(){
        this.status = (byte)(this.status & 0b10111111);
    }

    public void setNegative(){
        this.status = (byte) (this.status |  0b10000000);
    }

    public void resetNegative(){
        this.status = (byte)(this.status & 0b01111111);
    }

    public void zeroCheck(byte result){
        if(result == 0){
            setZero();
        }else{
            resetZero();
        }
    }

    public void negativeCheck(byte result){
        if((result & 0b10000000) != 0){
            setNegative();
        }else{
            resetNegative();
        }
    }

    public boolean carrySet(){ return ((this.status & 0b00000001) != 0); }

    public boolean zeroSet(){ return ((this.status & 0b00000010) != 0); }

    public boolean interruptSet(){ 
        return ((this.status & 0x4) == 0); 
    }

    public boolean BCDSet(){ return ((this.status & 0b00001000) != 0); }

    public boolean breakSet() { return ((this.status & 0b00010000) != 0); }

    public boolean constrolSet() { return ((this.status & 0b00100000) != 0); }

    public boolean overflowSet(){ return ((this.status & 0b01000000) != 0); }

    public boolean negativeSet(){ return ((this.status & 0b10000000) != 0); }

    @Override
    public String toString(){
        return 
        "A: " + String.format("0x%02X",this.A)+"\n" +
        "X: " + String.format("0x%02X",this.X)+"\n" +
        "Y: " + String.format("0x%02X",this.Y)+"\n" +
        "SP: " + String.format("0x%04X",this.SP)+"\n" +
        "PC: " + String.format("0x%04X",this.PC)+"\n" +
        "P: " + String.format("0x%02X",this.status)+"\n";
    }


    // sourceArea.setText("CPU registers\n");
    // sourceArea.append("A: "+String.format("0x%02X",(((GBProcessor)cpu).registers.A & 0xFF))+"\n");
    // sourceArea.append("F: "+String.format("0x%02X",(((GBProcessor)cpu).registers.F & 0xFF))+"\n");
    // sourceArea.append("B: "+String.format("0x%02X",(((GBProcessor)cpu).registers.B & 0xFF))+"\n");
    // sourceArea.append("C: "+String.format("0x%02X",(((GBProcessor)cpu).registers.C & 0xFF))+"\n");
    // sourceArea.append("D: "+String.format("0x%02X",(((GBProcessor)cpu).registers.D & 0xFF))+"\n");
    // sourceArea.append("E: "+String.format("0x%02X",(((GBProcessor)cpu).registers.E & 0xFF))+"\n");
    // sourceArea.append("H: "+String.format("0x%02X",(((GBProcessor)cpu).registers.H & 0xFF))+"\n");
    // sourceArea.append("L: "+String.format("0x%02X",(((GBProcessor)cpu).registers.L & 0xFF))+"\n");
    // sourceArea.append("PC: "+String.format("0x%02X",(((GBProcessor)cpu).registers.PC & 0xFFFF))+"\n");
    // sourceArea.append("Serial transfer byte: "+String.format("0x%02X",(mmu.read(0xFF01)))+"\n");
    // sourceArea.append("Serial control: "+String.format("0x%02X",(mmu.read(0xFF02)))+"\n");
    // sourceArea.append("BGP: "+String.format("0x%02X",(mmu.read(0xFF47) & 0xFF))+"\n");
    // sourceArea.append("OBP0: "+String.format("0x%02X",(mmu.read(0xFF48) & 0xFF))+"\n");
    // sourceArea.append("OBP1: "+String.format("0x%02X",(mmu.read(0xFF49) & 0xFF))+"\n");
    // sourceArea.append("STAT: "+String.format("0x%02X",(mmu.read(0xFF41) & 0xFF))+"\n");
}