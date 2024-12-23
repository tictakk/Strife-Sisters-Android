package com.strifesdroid.pcemulator.pce.cpu;

import com.strifesdroid.pcemulator.emulator.EmulatorUtils;
import com.strifesdroid.pcemulator.pce.PCEMMU;
import com.strifesdroid.pcemulator.pce.gpu.InterruptControl;

import java.util.ArrayList;

import com.strifesdroid.pcemulator.emulator.Emulator;
import com.strifesdroid.pcemulator.emulator.EmulatorState;
import com.strifesdroid.pcemulator.emulator.EmulatorType;
import com.strifesdroid.pcemulator.emulator.StatefulComponent;

public class HuC6280 extends EmulatorState implements Emulator, StatefulComponent {

    private static int LO_SPEED = 12;//1_789_772; //21.477 / 12
    private static int HI_SPEED = 3;//7_519_090; //21.477 / 3

    private ArrayList<String> routines = new ArrayList<String>();
    private ArrayList<String> routineReturn = new ArrayList<String>();
    private ArrayList<String> intReturn = new ArrayList<String>();

    public int speed = LO_SPEED;
    public long timestamp = 0;

    public REGISTERS registers = new REGISTERS();
    public PCEMMU mmu; //= new PCEMMU();
    // public byte interruptRequestMask = 0;
    public byte currentInstruction = 0;
    private byte lastTAM = 0;
    private boolean processingInterrupt = false;

    private InterruptControl ic;
    public int clock = 0;

    private int transferSrc = 0;
    private int transferDst = 0;
    private int transferLen = 0;     
    private int alt = 0;
    private boolean tranferInProgress = false;
    // private Timer timer = new Timer(ic);

    public HuC6280(PCEMMU mmu, InterruptControl ic){
        this.mmu = mmu;
        this.ic = ic;
        this.registers.PC = (short)((this.mmu.read(0xFFFE) & 0xFF) | ((this.mmu.read(0xFFFF) & 0xFF) << 8));
    }

    
    public HuC6280(PCEMMU mmu, InterruptControl ic, short pc){ //ONLY FOR TESTING
        this.mmu = mmu;
        this.ic = ic;
        this.registers.PC = pc;
    }

    void init(){
        // this.registers.set
    }

    public byte fetch(){
        byte fetched = this.mmu.read((this.registers.PC));
        // this.currentInstruction = (byte)(fetched & 0xFF);
        this.registers.PC += 1;

        return fetched;
    }

    private short processInterrupt(short interruptVector){
        // this.registers.PC--;
        PUSH_PC();
        // this.registers.setInterrupt();
        PUSH_STATUS();
        this.registers.setInterrupt();  
        return interruptVector;
        // return (short) GBUtils.joinLittleEndian(fetch(), fetch());
    }

    @Override
    public void decode(){

        if(!tranferInProgress){
            if(this.registers.interruptSet()){
                if(this.registers.interruptSet() && ic.availableInterrupt()){
                    this.registers.PC = processInterrupt(this.ic.getInterruptVector()); 
                    this.registers.PC = absolute_address(0);
                    processingInterrupt = true;
                }
            }
        }

        byte opcode = fetch();
        this.currentInstruction = (byte)(opcode & 0xFF);
        short address;
        byte zeroAddress;
        byte result;
        clock = ops.opcodes[(opcode & 0xFF)];
        this.timestamp += clock;

        switch(opcode & 0xFF){

            case 0x69:
                ADC(fetch()); //immdeiate
                break;

            case 0x65:
                ADC(zeroPageFetch(0)); //zero page
                break;

            case 0x75:
                ADC(zeroPageFetch(this.registers.X)); //zero page + X
                break;

            case 0x6D:
                ADC(absolute_read(0)); //absolute_read
                break;

            case 0x7D:
                ADC(absolute_read(this.registers.X)); //absolute_read X
                break;

            case 0x79:
                ADC(absolute_read(this.registers.Y)); //absolute_read Y
                break;

            case 0x72:
                ADC(indrect_read(fetch())); //indrect
                break;

            case 0x61:
                ADC(indexed_indrect_read(fetch())); //indexed_indrect_read
                break;

            case 0x71:
                ADC(indrect_indexed_read(fetch())); //indrect_indexed_read
                break;

            case 0x29:
                AND(fetch());
                break;

            case 0x25:
                AND(zeroPageFetch(0));
                break;

            case 0x35:
                AND(zeroPageFetch(this.registers.X));
                break;

            case 0x2D:
                AND(absolute_read(0));
                break;

            case 0x3D:
                AND(absolute_read(this.registers.X));
                break;

            case 0x39:
                AND(absolute_read(this.registers.Y));
                break;

            case 0x32:
                AND(indrect_read(fetch()));
                break;

            case 0x21:
                AND(indexed_indrect_read(fetch()));
                break;

            case 0x31:
                AND(indrect_indexed_read(fetch()));
                break;

            case 0x0A:
                ASL_A();
                break;
        
            case 0x06://breakpoint pls
                zeroAddress = fetch();
                result = shift_left(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                // ASL_M(zeroPageFetch(0));
                break;
            
            case 0x16:
                // zeroAddress = (byte)(fetch()+this.registers.X);
                zeroAddress = (byte)(fetch()+(this.registers.X & 0xFF));
                result = shift_left(this.mmu.zeroPageRead((zeroAddress)));
                this.mmu.zeroPageWrite(zeroAddress, result);
                // ASL_M(zeroPageFetch(this.registers.X));
                break;
                
            case 0x0E:
                address = (short)EmulatorUtils.joinLittleEndian(fetch(), fetch());
                result = shift_left(this.mmu.read(address));
                this.mmu.write(address,result);
                // ASL_M(absolute_read(0));
                break;

            case 0x1E:
                // address = (short)(GBUtils.joinLittleEndian(fetch(), fetch()) + this.registers.X);
                address = (short)(EmulatorUtils.joinLittleEndian(fetch(), fetch()) + (this.registers.X & 0xFF));
                result = shift_left(this.mmu.read(address));
                this.mmu.write(address,result);
                // ASL_M(absolute_read(this.registers.X));
                break;

            case 0x0F:
                BBR((byte)0,zeroPageFetch(0),fetch());
                break;

            case 0x1F:
                BBR((byte)1,zeroPageFetch(0),fetch());
                break;

            case 0x2F:
                BBR((byte)2,zeroPageFetch(0),fetch());
                break;

            case 0x3F:
                BBR((byte)3,zeroPageFetch(0),fetch());
                break;

            case 0x4F:
                BBR((byte)4,zeroPageFetch(0),fetch());
                break;

            case 0x5F:
                BBR((byte)5,zeroPageFetch(0),fetch());
                break;

            case 0x6F:
                BBR((byte)6,zeroPageFetch(0),fetch());
                break;

            case 0x7F:
                BBR((byte)7,zeroPageFetch(0),fetch());
                break;

            case 0x8F:
                BBS((byte)0,zeroPageFetch(0),fetch());
                break;

            case 0x9F:
                BBS((byte)1,zeroPageFetch(0),fetch());
                break;

            case 0xAF:
                BBS((byte)2,zeroPageFetch(0),fetch());
                break;

            case 0xBF:
                BBS((byte)3,zeroPageFetch(0),fetch());
                break;

            case 0xCF:
                BBS((byte)4,zeroPageFetch(0),fetch());
                break;

            case 0xDF:
                BBS((byte)5,zeroPageFetch(0),fetch());
                break;

            case 0xEF:
                BBS((byte)6,zeroPageFetch(0),fetch());
                break;

            case 0xFF:
                BBS((byte)7,zeroPageFetch(0),fetch());
                break;

            case 0x90:
                BCC(fetch());
                break;

            case 0xB0:
                BCS(fetch());
                break;

            case 0xF0:
                BEQ(fetch());
                break;

            case 0x89:
                BIT(fetch());
                break;

            case 0x24:
                BIT(zeroPageFetch(0));
                break;

            case 0x34:
                BIT(zeroPageFetch(this.registers.X));
                break;    

            case 0x2C:
                BIT(absolute_read(0));
                break;

            case 0x3C:
                BIT(absolute_read(this.registers.X));
                break;

            case 0x30:
                BMI(fetch());
                break;

            case 0xD0:
                BNE(fetch());
                break;

            case 0x10:
                BPL(fetch());
                break;

            case 0x80:
                BRA(fetch());
                break;

            case 0x00:
                BRK();
                break;

            case 0x44:
                BSR(fetch());
                break;

            case 0x50:
                BVC(fetch());
                break;

            case 0x70:
                BVS(fetch());
                break;

            case 0x62:
                CLA();
                break;

            case 0x18:
                CLC();
                break;

            case 0xD8:
                CLD();
                break;

            case 0x58:
                CLI();
                break;

            case 0xB8:
                CLV();
                break;

            case 0x82:
                CLX();
                break;

            case 0xC2:
                CLY();
                break;

            case 0xC9:
                COMP(this.registers.A, fetch());
                break;

            case 0xC5:
                COMP(this.registers.A,zeroPageFetch(0));
                break;

            case 0xD5:
                COMP(this.registers.A, zeroPageFetch(this.registers.X));
                break;

            case 0xCD:
                COMP(this.registers.A, absolute_read(0));
                break;

            case 0xDD:
                COMP(this.registers.A, absolute_read(this.registers.X));
                break;

            case 0xD9:
                COMP(this.registers.A, absolute_read(this.registers.Y));
                break;

            case 0xD2:
                COMP(this.registers.A, indrect_read(fetch()));
                break;

            case 0xC1:
                COMP(this.registers.A, indexed_indrect_read(fetch()));
                break;

            case 0xD1:
                COMP(this.registers.A, indrect_indexed_read(fetch()));
                break;

            case 0xE0:
                COMP(this.registers.X, fetch());
                break;

            case 0xE4:
                COMP(this.registers.X, zeroPageFetch(0));
                break;

            case 0xEC:
                COMP(this.registers.X, absolute_read(0));
                break;

            case 0xC0:
                COMP(this.registers.Y, fetch());
                break;

            case 0xC4:
                COMP(this.registers.Y, zeroPageFetch(0));
                break;

            case 0xCC:
                COMP(this.registers.Y, absolute_read(0));
                break;

            case 0xD4:
                CSH();
                break;
            
            case 0x54:
                CSL();
                break;

            case 0x3A:
                this.registers.A = DEC(this.registers.A);
                break;

            case 0xC6:
                zeroAddress = fetch();
                result = DEC(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0xD6:
                // zeroAddress = (byte)(fetch()  + this.registers.X);
                zeroAddress = (byte)(fetch()  + (this.registers.X & 0xFF));
                result = DEC(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0xCE:
                address = absolute_address(0);
                result = DEC(this.mmu.read(address));
                this.mmu.write(address, result);
                break;

            case 0xDE:
                address = absolute_address(this.registers.X);
                result = DEC(this.mmu.read(address));
                this.mmu.write(address, result);
                break;

            case 0xCA:
                DEC_X();
                break;

            case 0x88:
                DEC_Y();
                break;

            case 0x49:
                EOR(fetch());
                break;

            case 0x45:
                EOR(zeroPageFetch(0));
                break;

            case 0x55:
                EOR(zeroPageFetch(this.registers.X));
                break;

            case 0x4D://breakpoint pls
                EOR(absolute_read(0));
                break;

            case 0x5D:
                EOR(absolute_read(this.registers.X));
                break;

            case 0x59://breakpoint pls 2
                EOR(absolute_read(this.registers.Y));
                break;

            case 0x52:
                EOR(indrect_read(fetch()));
                break;

            case 0x41:
                EOR(indexed_indrect_read(fetch()));
                break;

            case 0x51:
                EOR(indrect_indexed_read(fetch()));
                break;

            case 0x1A:
                this.registers.A = INC(this.registers.A);
                break;

            case 0xE6:
                zeroAddress = fetch();
                result = INC(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0xF6:
                zeroAddress = (byte)(fetch() + (this.registers.X & 0xFF));
                // zeroAddress = (byte)(fetch() + this.registers.X);
                result = INC(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0xEE:
                address = (short)EmulatorUtils.joinLittleEndian(fetch(), fetch());
                result = INC(this.mmu.read(address));
                this.mmu.write(address, result);
                break;

            case 0xFE:
                address = (short)(EmulatorUtils.joinLittleEndian(fetch(), fetch()) + (this.registers.X & 0xFF));
                // result = INC(this.mmu.read((address + this.registers.X)));
                result = INC(this.mmu.read(address));
                this.mmu.write(address, result);
                break;

            case 0xE8:
                INC_X();
                break;

            case 0xC8:
                INC_Y();
                break;

            case 0x4C:
                JUMP((short)EmulatorUtils.joinLittleEndian(fetch(), fetch()));
                break;

            case 0x6C:
                address = (short)EmulatorUtils.joinLittleEndian(fetch(), fetch());
                JUMP((short)EmulatorUtils.joinLittleEndian(this.mmu.read(address), this.mmu.read(address+1)));
                break;

            case 0x7C:
                //the pcengine doc says it's indexed indrect but the 65C02 doc says
                //this instruction is absolute_read + X. I'm following the latter because
                //the pc engine doc has a type that says it's zzzz,X which would be abs + X
                // address = (short)(GBUtils.joinLittleEndian(fetch(), fetch()) + this.registers.X);
                // JUMP(address);
                int tmpAddress = absolute_address(this.registers.X);
                byte bOne = this.mmu.read(tmpAddress);
                byte bTwo = this.mmu.read(tmpAddress+1);
                address = (short)EmulatorUtils.joinBytes(bTwo, bOne);
                this.registers.PC = address;
                break;

            case 0x20:
                JSR(fetch(),fetch());
                break;

            case 0xA9:
                LD_A(fetch());
                break;

            case 0xA5:
                LD_A(zeroPageFetch(0));
                break;

            case 0xB5:
                LD_A(zeroPageFetch(this.registers.X));
                break;

            case 0xAD:
                LD_A(absolute_read(0));
                break;

            case 0xBD:
                LD_A(absolute_read(this.registers.X));
                break;

            case 0xB9:
                LD_A(absolute_read(this.registers.Y));
                break;
            
            case 0xB2:
                LD_A(indrect_read(fetch()));
                break;

            case 0xA1:
                LD_A(indexed_indrect_read(fetch()));
                break;

            case 0xB1:
                LD_A(indrect_indexed_read(fetch()));
                break;

            case 0xA2:
                LD_X(fetch());
                break;

            case 0xA6:
                LD_X(zeroPageFetch(0));
                break;

            case 0xB6:
                LD_X(zeroPageFetch(this.registers.Y));
                break;

            case 0xAE:
                LD_X(absolute_read(0));
                break;

            case 0xBE:
                LD_X(absolute_read(this.registers.Y));
                break;

            case 0xA0:
                LD_Y(fetch());
                break;

            case 0xA4:
                LD_Y(zeroPageFetch(0));
                break;

            case 0xB4:
                LD_Y(zeroPageFetch(this.registers.X));
                break;

            case 0xAC:
                LD_Y(absolute_read(0));
                break;

            case 0xBC:
                LD_Y(absolute_read(this.registers.X));
                break;

            case 0x4A:
                this.registers.A = LSR(this.registers.A);
                break;

            case 0x46://breakpoint pls
                zeroAddress = fetch();
                result = LSR(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0x56:
                // zeroAddress = (byte)(fetch() + this.registers.X);
                zeroAddress = (byte)(fetch() + (this.registers.X & 0xFF));
                result = LSR(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0x4E:
                address = (short)EmulatorUtils.joinLittleEndian(fetch(), fetch());
                result = LSR(this.mmu.read(address));
                this.mmu.write(address, result);
                break;

            case 0x5E:
                // address = (short)(GBUtils.joinLittleEndian(fetch(), fetch())  + this.registers.X);
                address = (short)(EmulatorUtils.joinLittleEndian(fetch(), fetch())  + (this.registers.X & 0xFF));
                result = LSR(this.mmu.read(address));
                this.mmu.write(address, result);
                break;

            case 0xEA: //2 cycles
                NOP();
                break;

            case 0x09:
                ORA(fetch());
                break;

            case 0x05:
                ORA(zeroPageFetch(0));
                break;

            case 0x15:
                ORA(zeroPageFetch(this.registers.X));
                break;

            case 0x0D:
                ORA(absolute_read(0));
                break;

            case 0x1D:
                ORA(absolute_read(this.registers.X));
                break;

            case 0x19:
                ORA(absolute_read(this.registers.Y));
                break;

            case 0x12:
                ORA(indrect_read(fetch()));
                break;

            case 0x01:
                ORA(indexed_indrect_read(fetch()));
                break;

            case 0x11:
                ORA(indrect_indexed_read(fetch()));
                break;

            case 0x48:
                PUSH_A();
                break;

            case 0x08:
                PUSH_STATUS();
                break;
            
            case 0xDA:
                PUSH_X();
                break;

            case 0x5A:
                PUSH_Y();
                break;

            case 0x68:
                PLA();
                break;

            case 0x28:
                PLP();
                break;

            case 0xFA:
                PLX();
                break;

            case 0x7A:
                PLY();
                break;

            case 0x07:
                RMB((byte)0);
                break;

            case 0x17:
                RMB((byte)1);
                break;

            case 0x27:
                RMB((byte)2);
                break;

            case 0x37:
                RMB((byte)3);
                break;

            case 0x47:
                RMB((byte)4);
                break;

            case 0x57:
                RMB((byte)5);
                break;

            case 0x67:
                RMB((byte)6);
                break;
            
            case 0x77:
                RMB((byte)7);
                break;
                
            case 0x2A:
                this.registers.A = ROL(this.registers.A);
                break;

            case 0x26://breakplint pls
                zeroAddress = fetch();
                result = ROL(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0x36:
                // zeroAddress = (byte)(fetch() + this.registers.X);
                zeroAddress = (byte)(fetch() + (this.registers.X & 0xFF));
                result = ROL(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.write(zeroAddress,result);
                break;

            case 0x2E://breakpoint pls
                address = (short) EmulatorUtils.joinLittleEndian(fetch(), fetch());//absolute_address(0);
                result = ROL(this.mmu.read(address));
                this.mmu.write(address,result);
                // ROL(absolute_read(0));
                break;

            case 0x3E:
                address = (short) (EmulatorUtils.joinLittleEndian(fetch(),fetch()) + (this.registers.X & 0xFF));
                // address = (short) (GBUtils.joinLittleEndian(fetch(),fetch()) + (this.registers.X));
                result = ROL(this.mmu.read(address));
                this.mmu.write(address,result);
                // ROL(absolute_read(this.registers.X));
                break;

            case 0x6A:
                this.registers.A = ROR(this.registers.A);
                break;

            case 0x66:
                zeroAddress = fetch();
                result = ROR(this.mmu.zeroPageRead(zeroAddress));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0x76:
                // zeroAddress = (byte)(fetch() + this.registers.X);
                zeroAddress = (byte)(fetch() + (this.registers.X & 0xFF));
                result = ROR((this.mmu.zeroPageRead(zeroAddress)));
                this.mmu.zeroPageWrite(zeroAddress, result);
                break;

            case 0x6E:
                address = (short) EmulatorUtils.joinLittleEndian(fetch(), fetch());//absolute_address(0);
                result = ROR(this.mmu.read(address));
                this.mmu.write(address,result);
                // ROR(absolute_read(0));
                break;

            case 0x7E:
                // address = (short) (GBUtils.joinLittleEndian(fetch(), fetch()) + this.registers.X);
                address = (short) (EmulatorUtils.joinLittleEndian(fetch(), fetch()) + (this.registers.X & 0xFF));
                result = ROR(this.mmu.read(address));
                this.mmu.write(address,result);
                // ROR(absolute_read(this.registers.X));
                break;

            case 0x40:
                RTI();
                break;

            case 0x60:
                RTS();
                break;

            case 0x22:
                SAX();
                break;

            case 0x42:
                SAY();
                break;

            case 0xE9:
                SBC(fetch());
                break;

            case 0xE5:
                SBC(zeroPageFetch(0));
                break;

            case 0xF5:
                SBC(zeroPageFetch(this.registers.X));
                break;

            case 0xED:
                SBC(absolute_read(0));
                break;

            case 0xFD:
                SBC(absolute_read(this.registers.X));
                break;

            case 0xF9:
                SBC(absolute_read(this.registers.Y));
                break;

            case 0xF2:
                SBC(indrect_read(fetch()));
                break;

            case 0xE1:
                SBC(indexed_indrect_read(fetch()));
                break;

            case 0xF1:
                SBC(indrect_indexed_read(fetch()));
                break;
               
            case 0x38:
                SEC();
                break;

            case 0xF8:
                SED();
                break;            
                
            case 0x78:
                SEI();
                break;

            case 0xF4:
                SET();
                break;

            case 0x87:
                SMB((byte)0);
                break;

            case 0x97:
                SMB((byte)1);
                break;

            case 0xA7:
                SMB((byte)2);
                break;

            case 0xB7:
                SMB((byte)3);
                break;

            case 0xC7:
                SMB((byte)4);
                break;

            case 0xD7:
                SMB((byte)5);
                break;

            case 0xE7:
                SMB((byte)6);
                break;

            case 0xF7:
                SMB((byte)7);
                break;

            case 0x85: //these are the STA instructions
                this.mmu.zeroPageWrite(fetch(), this.registers.A);
                break;

            case 0x95:
                this.mmu.zeroPageWrite((byte)(fetch() + (this.registers.X & 0xFF)), this.registers.A);
                break;

            case 0x8D:
                // short tpc = this.registers.PC;
                // byte tb1 = fetch();
                // byte tb2 = fetch();
                // int testAddr = GBUtils.joinLittleEndian(tb1,tb2);
                this.mmu.write(EmulatorUtils.joinLittleEndian(fetch(),fetch()), this.registers.A);
                break;

            case 0x9D://breakpoint pls
                this.mmu.write((EmulatorUtils.joinLittleEndian(fetch(), fetch()) + (this.registers.X & 0xFF)), this.registers.A);
                break;

            case 0x99://breakpoint pls
                short tpc = this.registers.PC;
                byte tb1 = fetch();
                byte tb2 = fetch();
                int testAddr = EmulatorUtils.joinLittleEndian(tb1,tb2);

                this.mmu.write(testAddr + (this.registers.Y & 0xFF), this.registers.A);
                break;

            case 0x92://breakpoint pls
                this.mmu.write(indirect_address(fetch()), this.registers.A);
                break;

            case 0x81:
                this.mmu.write(indexed_indirect_address(fetch()),this.registers.A);
                break;

            case 0x91:// breakpoint pls 2
                zeroAddress = fetch();
                this.mmu.write(indirect_indexed_address(zeroAddress),this.registers.A);
                // this.mmu.write(indirect_indexed_address(fetch()),this.registers.A);
                break;

            //STX    
            case 0x86:
                this.mmu.zeroPageWrite(fetch(), this.registers.X);
                break;

            case 0x96:
                // this.mmu.zeroPageWrite((byte)(fetch() + this.registers.Y),this.registers.X);
                this.mmu.zeroPageWrite((byte)(fetch() + (this.registers.Y & 0xFF)),this.registers.X);
                break;

            case 0x8E:
                this.mmu.write(EmulatorUtils.joinLittleEndian(fetch(), fetch()), this.registers.X);
                break;

             //STY   
            case 0x84:
                this.mmu.zeroPageWrite(fetch(), this.registers.Y);
                break;

            case 0x94:
                // this.mmu.zeroPageWrite((byte)(fetch() + this.registers.X), this.registers.Y);
                this.mmu.zeroPageWrite((byte)(fetch() + (this.registers.X & 0xFF)), this.registers.Y);
                break;

            case 0x8C:
                this.mmu.write(EmulatorUtils.joinLittleEndian(fetch(), fetch()),this.registers.Y);
                break;

            //STZ    
            case 0x64:
                this.mmu.zeroPageWrite(fetch(), (byte)0);
                break;

            case 0x74:
                // this.mmu.write()
                // this.mmu.zeroPageWrite((byte)(fetch() + this.registers.X), (byte)0);
                this.mmu.zeroPageWrite((byte)(fetch() + (this.registers.X & 0xFF)), (byte)0);
                break;

            case 0x9C:
                this.mmu.write(EmulatorUtils.joinLittleEndian(fetch(), fetch()),(byte)0);
                break;

            case 0x9E:
                // this.mmu.write((GBUtils.joinLittleEndian(fetch(), fetch()) + this.registers.X),(byte)0);
                this.mmu.write((EmulatorUtils.joinLittleEndian(fetch(), fetch()) + (this.registers.X & 0xFF)),(byte)0);
                break;

            case 0x02:
                SXY();
                break;
            
            case 0xF3:
                TAI();
                break;

            case 0x53:
                byte testy = fetch();
                TAM(testy);
                break;

            case 0xAA:
                TAX();
                break;

            case 0xA8:
                TAY();
                break;

            case 0xC3:
                TDD();
                break;

            case 0xE3:  
                TIA();
                break;

            case 0x73:
                TII();
                break; 

            case 0xD3:
                TIN();
                break;

            case 0x43:
                TMA(fetch());
                break;
            
            case 0x14:
                TRB_Z(fetch());
                break;

            case 0x1C:
                TRB((short)EmulatorUtils.joinLittleEndian(fetch(), fetch()));
                break;
            
            case 0x04:
                TSB_Z(fetch());
                break;

            case 0x0C:
                TSB((short)EmulatorUtils.joinLittleEndian(fetch(), fetch()));
                break;

            case 0x83:
                result = fetch();
                zeroAddress = fetch();
                TST(result,this.mmu.zeroPageRead(zeroAddress));
                // this.mmu.zeroPageWrite(zeroAddress, TST(result,this.mmu.zeroPageRead(zeroAddress)));
                break;

            case 0xA3:
                result = fetch();
                zeroAddress = (byte)(fetch() + this.registers.X);
                TST(result,this.mmu.zeroPageRead(zeroAddress));
                // this.mmu.zeroPageWrite(zeroAddress, TST(result,this.mmu.zeroPageRead(zeroAddress)));
                break;

            case 0x93:
                result = fetch();
                address = absolute_address(0);
                TST(result,this.mmu.read(address));
                // this.mmu.write(address,TST(result,this.mmu.read(address)));
                break;

            case 0xB3:
                result = fetch();
                address = absolute_address(this.registers.X);
                TST(result,this.mmu.read(address));
                // this.mmu.write(address,TST(result,this.mmu.read(address)));
                break;

            case 0xBA:
                TSX();
                break;

            case 0x8A:
                TXA();
                break;

            case 0x9A:
                TXS();
                break;

            case 0x98:
                TYA();
                break;

            case 0x03:
                ST0(fetch());
                break;

            case 0x13:
                ST1(fetch());
                break;
                
            case 0x23:
                ST2(fetch());
                break;

            default:
                System.out.println("OPCODE not found: "+String.format("0x%02X",opcode));
                break;
        }
    }

    /*
    *
    * INSTRUCTIONS: "complete"
    * F47B
    */

    public byte BSR(byte branchTo){
        PUSH_PC();
        this.registers.PC += branchTo+1;
        this.registers.resetControl();
        return 8;
    }

    public byte TST(byte operand, byte addressop){
        int result = (operand & 0xFF) & (addressop & 0xFF);
        this.registers.negativeCheck((byte)result);
        if((result &0b1000000) != 0){
            this.registers.setOverflow();
        }else{
            this.registers.resetOverflow();
        }

        this.registers.zeroCheck((byte)result);
        return (byte)result;
        // return 7;
    }

    public byte TMA(byte operand){
        if(lastTAM == 0){
            return 4;
        }else{
            for(byte b=0; b<8; b++){
                if(((1 << b) & operand) != 0){
                    this.registers.A = this.mmu.MRP[b];
                    return 4;
                }
            }
        }
        // this.registers.A = this.mmu.MRP[operand];
        return 4;//can't reach
    }

    public byte TIN(){
        if(tranferInProgress){
            byte data = this.mmu.read(transferSrc);
            this.mmu.write(transferDst,data);
            transferSrc++;
            transferLen--;
            this.clock = 6;
            this.registers.PC--;
            if(transferLen==0){
                tranferInProgress = !tranferInProgress;
                this.registers.PC+=7;
            }
        }else{           
            transferSrc = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferDst = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferLen = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            tranferInProgress = true;
            this.clock = 17;
            this.registers.PC -= 7;
        }
        return 0;
    }

    public byte TII(){
        if(tranferInProgress){
            byte data = this.mmu.read(transferSrc);
            this.mmu.write(transferDst,data);
            transferSrc++;
            transferDst++;
            transferLen--;
            this.clock = 6;
            this.registers.PC--;
            if(transferLen==0){
                tranferInProgress = !tranferInProgress;
                this.registers.PC+=7;
            }
        }else{           
            transferSrc = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferDst = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferLen = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            tranferInProgress = true;
            this.clock = 17;
            this.registers.PC -= 7;
        }
        return 0;
    }

    public byte TIA(){
        if(tranferInProgress){
            byte data = this.mmu.read(transferSrc);
            this.mmu.write(transferDst+alt,data);
            transferSrc++;
            transferLen--;
            alt ^= 1;
            this.clock = 6;
            this.registers.PC--;
            if(transferLen==0){
                tranferInProgress = !tranferInProgress;
                this.registers.PC+=7;
            }
        }else{           
            alt = 0;
            transferSrc = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferDst = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferLen = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            tranferInProgress = true;
            this.clock = 17;
            this.registers.PC -= 7;
        }
        return 0;
    }

    public byte TDD(){
        if(tranferInProgress){
            byte data = this.mmu.read(transferSrc);
            this.mmu.write(transferDst,data);
            transferSrc--;
            transferLen--;
            transferDst--;
            this.clock = 6;
            this.registers.PC--;
            if(transferLen==0){
                tranferInProgress = !tranferInProgress;
                this.registers.PC+=7;
            }
        }else{           
            transferSrc = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferDst = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferLen = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            tranferInProgress = true;
            this.clock = 17;
            this.registers.PC -= 7;
        }
        return 0;
    }

    public byte TAI(){
        if(tranferInProgress){
            byte data = this.mmu.read(transferSrc+alt);
            this.mmu.write(transferDst,data);
            transferDst++;
            transferLen--;
            alt ^= 1;
            this.clock = 6;
            this.registers.PC--;
            if(transferLen==0){
                tranferInProgress = !tranferInProgress;
                this.registers.PC+=7;
            }
        }else{           
            alt = 0;
            transferSrc = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferDst = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            transferLen = EmulatorUtils.joinLittleEndian(fetch(), fetch());
            tranferInProgress = true;
            this.clock = 17;
            this.registers.PC -= 7;
        }
        return 0;
    }

    public byte SXY(){//the sexy function
        byte temp = this.registers.X;
        this.registers.X = this.registers.Y;
        this.registers.Y = temp;
        return 3;
    }

    //THIS WORKS
    public byte ST0(byte data){
        this.mmu.vdc.write(0, data);
        // this.mmu.write(0x0, data);
        return 5;
    }

    public byte ST1(byte data){
        this.mmu.vdc.write(0x2, data);
        // this.mmu.write(0x2, data);
        return 5;
    }

    public byte ST2(byte data){
        this.mmu.vdc.write(0x3, data);
        // this.mmu.write(0x3, data);
        return 5;
    }

    public byte SET(){
        this.registers.setBreak();
        return 2;
    }

    public byte SAX(){
        byte temp = this.registers.X;
        this.registers.X = this.registers.A;
        this.registers.A = temp;
        this.registers.resetBreak(); //THIS MAY NOT BE RESET BREAK BUT SOMETHING ELSE
        return 3;
    }

    public byte SAY(){
        byte temp = this.registers.Y;
        this.registers.Y = this.registers.A;
        this.registers.A = temp;
        this.registers.resetBreak();
        return 3;
    }

    public byte CLA(){
        this.registers.A = 0;
        return 1;
    }

    public byte CLX(){
        this.registers.X = 0;
        return 2;
    }

    public byte CLY(){
        this.registers.Y = 0;
        return 2;
    }

    // private byte ADC(byte n){
    //     byte bit = (byte)((this.registers.carrySet())? 1 : 0);
    //     byte result = (byte)(this.registers.A + n + bit);

    //     if(result == 0){
    //       this.registers.setZero();
    //     }else{
    //       this.registers.resetZero();
    //     }
        
    //     if(((n & 0xFF) + (bit & 0xFF) + (this.registers.A & 0xFF)) > 0xFF){
    //       this.registers.setCarry();
    //     }else{
    //       this.registers.resetCarry();
    //     }

    //     if((((~this.registers.A & ~n & result) | (this.registers.A & n & ~result)) & 0x80) == 0x80)
	// 		this.registers.setOverflow();
	// 	else
	// 		this.registers.resetOverflow();
        
    //     this.registers.negativeCheck(result);
    //     this.registers.A = (byte) result;
    //     return 4;
    //   }

    public byte ADC(byte operand){
        byte data;
        if(this.registers.breakSet()){
            data = this.mmu.zeroPageRead(this.registers.X);
        }else{
            data = this.registers.A;
        }

        int tmp = (data & 0xFF) + (operand & 0xFF) + (this.registers.carrySet()? 1 : 0);

        boolean overflow = (~(data ^ operand) & (data ^ tmp) & 0x80) == 0x80;

        if(tmp > 0xFF){
            this.registers.setCarry();
        }else{
            this.registers.resetCarry();
        }

        if((tmp & 0xFF) == 0){
            this.registers.setZero();
        }else{
            this.registers.resetZero();
        }

        if((tmp & 0x80) == 0x80){
            this.registers.setNegative();
        }else{
            this.registers.resetNegative();
        }
        
        if(overflow){
            this.registers.setOverflow();
        }else{
            this.registers.resetOverflow();
        }
        if(this.registers.breakSet()){
            this.mmu.zeroPageWrite(this.registers.X, (byte)tmp);
        }else{
            this.registers.A = (byte) tmp;
        }
        this.registers.resetBreak();
        return 2;
    }

    public byte AND(byte operand){
        byte data;
        if(this.registers.breakSet()){
            data = this.mmu.zeroPageRead(this.registers.X);
        }else{
            data = this.registers.A;
        }
        byte result = (byte) (data & operand);

        if(getBit(7,result) != 0){
            this.registers.setNegative();
        }else{
            this.registers.resetNegative();
        }

        if(result == 0){
            this.registers.setZero();
        }else{
            this.registers.resetZero();
        }

        if(this.registers.breakSet()){
            this.mmu.zeroPageWrite(this.registers.X, result);
        }else{
            this.registers.A = result;
        }

        this.registers.resetControl();
        this.registers.resetBreak();
        
        return 2;
    }

    public byte shift_left(byte operand){
        int result = (operand & 0xFF) << 1;
        if(result > 0xFF){
            this.registers.setCarry();
        }else{
            this.registers.resetCarry();
        }

        this.registers.zeroCheck((byte)(result & 0xFF));
        this.registers.negativeCheck((byte)(result & 0xFF));
        return (byte)(result);
    }

    public byte ASL_A(){
        this.registers.A = shift_left(this.registers.A);
        return 0;
    }

    public byte ASL_M(short address){
        this.mmu.write(address,shift_left(this.mmu.read(address)));
        return 0;
    }

    public byte BBR(byte bitToTest, byte zeroAddress, byte branchTo){
        if (((1 << bitToTest) & (zeroAddress & 0xFF)) == 0){
            this.registers.PC += branchTo;
            return 0;
        }else{
            return 0;
        }
    }

    public byte BBS(byte bitToTest, byte byteToTest, byte branchTo){
        // if (((1 << bitToTest) & this.mmu.read(zeroAddress & 0xFF)) != 0){
        //     this.registers.PC += branchTo;
        // }
        if (((1 << bitToTest) & byteToTest) != 0){
            this.registers.PC += branchTo;
        }
        this.registers.resetControl();
        return 0;
    }

    public byte BCC(byte branchTo){
        if(!this.registers.carrySet()){
            this.registers.PC += branchTo;
        }
        this.registers.resetControl();
        return 0;
    }

    public byte BCS(byte branchTo){
        if(this.registers.carrySet()){
            this.registers.PC += branchTo;
        }
        this.registers.resetControl();
        return 0;
    }
    
    public byte BEQ(byte branchTo){
        if(this.registers.zeroSet()){
            this.registers.PC += branchTo;
        }
        this.registers.resetControl();
        return 0;
    }

    public byte BIT(byte data){
        byte result = (byte) (data & this.registers.A);

        if(result == 0){
            this.registers.setZero();
        }else{
            this.registers.resetZero();
        }

        if((0b10000000 & data) != 0){
            this.registers.setNegative();
        }else{
            this.registers.resetNegative();
        }

        if((0b1000000 & data) != 0){
            this.registers.setOverflow();
        }else{
            this.registers.resetOverflow();
        }
        return 2;
    }

    public byte BMI(byte branchTo){
        this.registers.resetControl();
        if(this.registers.negativeSet()){
            this.registers.PC += branchTo;
            return 4;
        }else{
            return 2;
        }
    }

    public byte BNE(byte branchTo){
        this.registers.resetControl();
        if(!this.registers.zeroSet()){
            this.registers.PC += branchTo;
            return 4;
        }else{
            return 2;
        }
    }

    public byte BPL(byte branchTo){
        this.registers.resetControl();
        if(!this.registers.negativeSet()){
            this.registers.PC += branchTo;
            return 4;
        }else{
            return 2;
        }
    }

    public byte BRA(byte branchTo){
        this.registers.PC += branchTo;
        this.registers.resetControl();
        return 2;
    }

    //TODO: Force interrupt generation
    //The BRK instruction forces the generation of an interrupt request. The program counter and processor status are pushed on the stack then the IRQ interrupt vector at $FFFE/F is loaded into the PC and the break flag in the status set to one.
    public byte BRK(){
        this.registers.breakSet();
        // this.registers.interruptSet();
        PUSH_PC();
        PUSH_STATUS();
        this.registers.PC = (short)0xFFF6;
        this.registers.PC = (short)EmulatorUtils.joinLittleEndian(fetch(), fetch());
        return 8;
    }

    public byte BVC(byte branchTo){
        if(!this.registers.overflowSet()){
            this.registers.PC += branchTo;
        }
        return 2;
    }

    public byte BVS(byte branchTo){
        if(this.registers.overflowSet()){
            this.registers.PC += branchTo;
        }
        return 2;
    }

    public byte CLC(){
        this.registers.resetCarry();
        this.registers.resetControl();
        return 2;
    }

    public byte CLD(){
        this.registers.resetBCD();
        this.registers.resetControl();
        return 2;
    }

    public byte CLI(){
        this.registers.resetInterrupt();
        this.registers.resetControl();
        return 2;
    }

    public byte CLV(){
        this.registers.resetOverflow();
        this.registers.resetControl();
        return 2;
    }

    //this can do carry A, X, Y
    public byte COMP(byte value, byte operand){
        byte result = (byte) (value - operand);
        if((value & 0xFF) >= (operand & 0xFF)){
            this.registers.setCarry();
        }else{
            this.registers.resetCarry();
        }
        if(value == operand){
            this.registers.setZero();
        }else{
            this.registers.resetZero();
        }

        if((result & 0b10000000) != 0){
            this.registers.setNegative();
        }else{
            this.registers.resetNegative();
        }

        return 0;
    }

    public byte DEC_M(short address){
        this.mmu.write(address,DEC((this.mmu.read(address))));
        return 2;
    }

    public byte DEC_X(){
        this.registers.X = DEC(this.registers.X);
        return 2;
    }

    public byte DEC_Y(){
        this.registers.Y = DEC(this.registers.Y);
        return 2;
    }

    public byte EOR(byte operand){
        byte data;
        if(this.registers.breakSet()){
            data = this.mmu.zeroPageRead(this.registers.X);
        }else{
            data = this.registers.A;
        }

        byte result = (byte)(data ^ operand);
        this.registers.zeroCheck(result);
        this.registers.negativeCheck(result);
        this.registers.resetControl();

        if(this.registers.breakSet()){
            this.mmu.zeroPageWrite(this.registers.X, result);
        }else{
            this.registers.A = result;
        }
        this.registers.resetBreak();
        return 2;
    }

    public byte INC_X(){
        this.registers.X = INC(this.registers.X);
        return 0;
    }

    public byte INC_Y(){
        this.registers.Y = INC(this.registers.Y);
        return 0;
    }

    public byte JUMP(short address){
        // PUSH_PC();
        this.registers.PC = address;//(short)GBUtils.joinBytes(jp1,jp2);
        // System.out.println("jump to: "+String.format("0x%04X",(address & 0xFFFF)));
        return 0;
    }

    public byte JSR(byte jp1, byte jp2){
        // --this.registers.SP;
        PUSH_PC();
        if((EmulatorUtils.joinBytes(jp2,jp1) & 0xFFFF) < 10){
            System.out.println("derp");
        }
        this.registers.PC = (short)EmulatorUtils.joinBytes(jp2,jp1);
        this.routines.add(String.format("0x%04X",(this.registers.PC & 0xFFFF)));
        // System.out.println("subroutine at: "+String.format("0x%04X",(this.registers.PC & 0xFFFF)));
        return 7;
    }

    public byte LD_A(byte data){
        this.registers.A = data;
        this.registers.zeroCheck(this.registers.A);
        this.registers.negativeCheck(this.registers.A);
        this.registers.resetControl();

        return 2;
    }

    public byte LD_I_A(byte value){
        this.registers.A = value;
        this.registers.zeroCheck(this.registers.A);
        this.registers.negativeCheck(this.registers.A);
        return 2;
    }

    public byte LD_X(byte data){
        this.registers.X = data;

        this.registers.zeroCheck(this.registers.X);
        this.registers.negativeCheck(this.registers.X);
        return 0;
    }

    public byte LD_Y(byte data){
        this.registers.Y = data;

        this.registers.zeroCheck(this.registers.Y);
        this.registers.negativeCheck(this.registers.Y);
        return 0;
    }

    public byte LSR_A(){
        this.registers.A = shift_right(this.registers.A);
        return 2;
    }

    public byte LSR_N(short address){
        this.mmu.write(address, shift_right(this.mmu.read(address)));
        return 0;
    }

    public byte LSR(byte data){
        return shift_right(data);
    }

    public byte NOP(){
        return 2;
    }

    public byte ORA(byte operand){
        byte data;
        if(this.registers.breakSet()){
            data = this.mmu.zeroPageRead(this.registers.X);
        }else{
            data = this.registers.A;
        }

        byte result = (byte)(data | operand);
        this.registers.zeroCheck(result);
        this.registers.negativeCheck(result);

        if(this.registers.breakSet()){
            this.mmu.zeroPageWrite(this.registers.X,result);
        }else{
            this.registers.A = result;
        }
        this.registers.resetBreak();
        return 2;
    }

    public byte PUSH_A(){
        PUSH_BYTE(this.registers.A);
        return 3;
    }

    public byte PUSH_STATUS(){
        PUSH_BYTE(this.registers.status);
        return 3;
    }

    public byte PUSH_X(){
        PUSH_BYTE(this.registers.X);
        return 3;
    }

    public byte PUSH_Y(){
        PUSH_BYTE(this.registers.Y);
        return 3;
    }

    //this is pop
    public byte PLA(){
        this.registers.A = this.mmu.stackRead(++this.registers.SP);    
        this.registers.negativeCheck(this.registers.A);
        this.registers.zeroCheck(this.registers.A);
        return 4;
    }

    public byte PLP(){
        this.registers.status = this.mmu.stackRead(++this.registers.SP);
        return 4;
    }

    public byte PLX(){
        // this.registers.X = this.mmu.read(this.registers.SP++);
        this.registers.X = this.mmu.stackRead(++this.registers.SP);
        this.registers.negativeCheck(this.registers.X);
        this.registers.zeroCheck(this.registers.X);
        return 4;
    }

    public byte PLY(){
        this.registers.Y = this.mmu.stackRead(++this.registers.SP);
        this.registers.negativeCheck(this.registers.Y);
        this.registers.zeroCheck(this.registers.Y);
        return 4;
    }

    public byte RMB(byte bit){
        //zero page address
        byte address = fetch();
        byte resetResult = (byte)(this.mmu.zeroPageRead(address) & ~(1 << bit));
        this.mmu.zeroPageWrite(address, resetResult);
        // this.mmu.write(address,resetResult);
        return 7;
    }

    public byte ROL(byte operand){
        return rotate_left(operand);
    }

    public byte ROR(byte operand){
        return rotate_right(operand);
    }

    public byte ROR_A(){
        this.registers.A = rotate_right(this.registers.A);
        return 2;
    }

    public byte ROR_M(short address){
        this.mmu.write(address,this.mmu.read(address));
        return 0;
    }

    public byte RTI(){
        this.registers.status = this.mmu.stackRead(++this.registers.SP);
        this.registers.status &= 0b11101111;
        byte P = this.mmu.stackRead(++this.registers.SP);
        byte C = this.mmu.stackRead(++this.registers.SP);
        this.registers.PC = (short)(EmulatorUtils.joinBytes(C,P) + 1);
        intReturn.add(String.format("0x%04X",(this.registers.PC & 0xFFFF)));
        processingInterrupt = false;
        return 6;
    }

    public byte RTS(){
        byte P = this.mmu.stackRead(++this.registers.SP);
        byte C = this.mmu.stackRead(++this.registers.SP);
        this.registers.PC = (short)(EmulatorUtils.joinBytes(C,P) + 1);
        routineReturn.add(String.format("0x%04X",(this.registers.PC & 0xFFFF)));
        // System.out.println("return to: "+String.format("0x%04X",(this.registers.PC & 0xFFFF)));
        return 6;
    }

    public byte SBC(byte op){
        int operand = (op & 0xFF) ^ 0x00FF;
        // byte operand = (byte)~op;
        int tmp = (this.registers.A & 0xFF) + (operand) + (this.registers.carrySet()? 1 : 0);
        boolean overflow = (((tmp ^ this.registers.A) & (tmp ^ operand)) & 0x80) == 0x80;
        // boolean overflow = ((this.registers.A ^ tmp) & (operand ^ tmp) & 0x80) == 0x80;

        if(tmp > 0xFF){
            this.registers.setCarry();
        }else{
            this.registers.resetCarry();
        }

        if((tmp & 0xFF) == 0){
            this.registers.setZero();
        }else{
            this.registers.resetZero();
        }

        if((tmp & 0x80) == 0x80){
            this.registers.setNegative();
        }else{
            this.registers.resetNegative();
        }
        
        if(overflow){
            this.registers.setOverflow();
        }else{
            this.registers.resetOverflow();
        }
        this.registers.A = (byte) tmp;
        return 2;
    }

    public byte SEC(){
        this.registers.setCarry();
        return 2;
    }

    public byte SED(){
        this.registers.setBCD();
        return 2;
    }

    public byte SEI(){
        this.registers.setInterrupt();  
        return 2;
    }

    //this is always at zero page
    public byte SMB(byte bitSet){
        byte address = fetch();
        byte setResult = (byte)(this.mmu.zeroPageRead(address) | (1 << bitSet));
        this.mmu.zeroPageWrite(address, setResult);
        return 7;
    }

    public byte STA(short address){
        this.mmu.write(address,this.registers.A);
        return 2;
    }

    public byte STOP(){
        //STOP
        return 3;
    }

    public byte STX(short address){
        this.mmu.write(address,this.registers.X);
        return 0;
    }

    public byte STY(short address){
        this.mmu.write(address,this.registers.Y);
        return 0;
    }

    public byte STZ(short address){
        this.mmu.write(address,(byte)0);
        return 0;
    }

    public byte TAX(){
        this.registers.X = this.registers.A;
        this.registers.zeroCheck(this.registers.X);
        this.registers.negativeCheck(this.registers.X);
        return 2;
    }

    public byte TAY(){
        this.registers.Y = this.registers.A;
        this.registers.zeroCheck(this.registers.Y);
        this.registers.negativeCheck(this.registers.Y);
        return 2;
    }

    public byte TRB(short address){
        byte testData = this.mmu.read(address);
        byte mask = (byte)((testData & 0xFF) & (~(this.registers.A & 0xFF)));
        byte result = (byte)((testData & 0xFF) & (this.registers.A & 0xFF));
        this.mmu.write(address, mask);

        if(result != 0){
            this.registers.resetZero();
        }else{
            this.registers.setZero();
        }

        this.registers.negativeCheck(testData);
        if((testData & 0b1000000) == 0b1000000){
            this.registers.setOverflow();
        }else{
            this.registers.resetOverflow();
        }
        return 7;
    }

    public byte TRB_Z(byte address){
        // byte testData = this.mmu.read(address);
        byte testData = this.mmu.zeroPageRead(address);
        byte mask = (byte)((testData & 0xFF) & (~(this.registers.A & 0xFF)));
        byte result = (byte)((testData & 0xFF) & ((this.registers.A & 0xFF)));

        this.mmu.zeroPageWrite(address, mask);

        if(result != 0){
            this.registers.resetZero();
        }else{
            this.registers.setZero();
        }
        this.registers.negativeCheck(testData);
        if((testData & 0b1000000) == 0b1000000){
            this.registers.setOverflow();
        }else{
            this.registers.resetOverflow();
        }
        return 6;
    }

    public byte TSB(short address){
        byte result = (byte)((this.mmu.read(address) & 0xFF) | (this.registers.A & 0xFF));
        byte flagResault = (byte)(this.mmu.read(address) & 0xFF & (this.registers.A & 0xFF));
        this.mmu.write(address,result);

        this.registers.negativeCheck(flagResault);
        this.registers.zeroCheck(flagResault);
        if((flagResault & 0b1000000) == 0b1000000){
            this.registers.setOverflow();
        }else{
            this.registers.resetOverflow();
        }

        return 7;
    }

    public byte TSB_Z(byte address){
        byte zeroAddress = this.mmu.zeroPageRead(address);
        byte result = (byte)((zeroAddress & 0xFF) | (this.registers.A & 0xFF));
        this.mmu.zeroPageWrite(address,result);

        byte mask = (byte)((zeroAddress & 0xFF) & (this.registers.A & 0xFF));
        this.registers.negativeCheck(mask);
        this.registers.zeroCheck(mask);

        if((mask & 0b1000000) == 0b1000000){
            this.registers.setOverflow();
        }else{
            this.registers.resetOverflow();
        }      
        return 6;
    }

    public byte TSX(){
        this.registers.X = this.registers.SP;
        this.registers.zeroCheck(this.registers.X);
        this.registers.negativeCheck(this.registers.X);
        return 0;
    }

    public byte TXA(){
        this.registers.A = this.registers.X;
        this.registers.zeroCheck(this.registers.A);
        this.registers.negativeCheck(this.registers.A);
        return 2;
    }

    public byte TXS(){
        this.registers.SP = this.registers.X;
        this.registers.resetControl();
        return 2;
    }

    public byte TYA(){
        this.registers.A = this.registers.Y;
        this.registers.zeroCheck(this.registers.A);
        this.registers.negativeCheck(this.registers.A);
        return 2;
    }
                                                                       
    public byte WAI(){
        //wait until interrupt occurs
        this.registers.PC--;
        this.registers.setInterrupt();
        return 3;
    }

    public byte TAM(byte operand){
        lastTAM = operand;
        for(byte b=0; b<8; b++){
            if(((1 << b) & operand) != 0){
                this.mmu.MRP[b] = this.registers.A;
            }
        }
        return 5;
    }

    public byte CSL(){
        this.speed = LO_SPEED;
        this.registers.resetControl();

        return 3;
    }

    public byte CSH(){
        this.speed = HI_SPEED;
        this.registers.resetControl();
        return 3;
    }


    /*
    * OPCODES END
    */

    private byte rotate_right(byte operand){
        byte tmp = (byte) ((this.registers.carrySet())? 1 : 0);
        if((operand & 0x01) == 0x01){
            this.registers.setCarry();
        }else{
            this.registers.resetCarry();
        }
        byte result = (byte)((operand & 0xFF) >> 1 | (tmp << 7));
        this.registers.zeroCheck(result);
        this.registers.negativeCheck(result);
        return result;
    }
    
    private byte rotate_left(byte value){
        int result = ((value & 0xFF) << 1) | ((this.registers.carrySet())? 1 : 0);

        if(result > 0xFF){
            this.registers.setCarry();
        }else{
            this.registers.resetCarry();
        }
        this.registers.zeroCheck((byte)(result & 0xFF));
        this.registers.negativeCheck((byte)(result & 0xFF));
        return (byte)(result & 0xFF);
    }

    private byte shift_right(byte operand){
        if((operand & 0x01) == 0x01){
            this.registers.setCarry();
        }else{
            this.registers.resetCarry();
        }

        byte tmp = (byte) ((operand & 0xFF) >> 1);
        this.registers.negativeCheck(tmp);
        this.registers.zeroCheck(tmp);
        return tmp;
    }

    private byte INC(byte value){
        byte result = (byte)(value + 1);

        this.registers.zeroCheck(result);
        this.registers.negativeCheck(result);
        this.registers.resetControl();

        return result;
    }

    //this doesn't return the cycle but the actual value
    private byte DEC(byte value){
        byte result =(byte)(value - 1);

        if(result == 0){
            this.registers.setZero();
        }else{
            this.registers.resetZero();
        }

        if((result & 0b10000000) != 0){
            this.registers.setNegative();
        }else{
            this.registers.resetNegative();
        }
        this.registers.resetControl();

        return result;
    }

    public byte getBit(int shift,int byteValue){
        return (byte)(byteValue & (1 << shift));
    }

    private void PUSH_PC(){
        byte[] bytes = EmulatorUtils.splitBytes(--this.registers.PC & 0xFFFF);
        mmu.stackWrite(this.registers.SP--, bytes[0]);
        mmu.stackWrite(this.registers.SP--, bytes[1]);
    }

    private void PUSH_BYTE(byte value){
        // mmu.write(--this.registers.SP,value);
        mmu.stackWrite(this.registers.SP--, value);
    }

    /*
    *
    *Addressing functions
    *
    */

    public byte zeroPageFetch(int xOrY){
        byte fetched = this.mmu.zeroPageRead((byte)((fetch() & 0xFF) + (byte)(xOrY & 0xFF)));
        // this.registers.PC += 1;
        return fetched;
    }

    public byte direct(){
        return fetch();
    }

    //get address
    public short absolute_address(int xOrY){
        return (short)(EmulatorUtils.joinLittleEndian(fetch(),fetch()) + (xOrY & 0xFF));
    }

    public short indirect_address(byte zAddress){
        byte bOne = this.mmu.zeroPageRead(zAddress);
        byte bTwo = this.mmu.zeroPageRead((byte)(zAddress+1));
        return (short)(EmulatorUtils.joinBytes(bTwo, bOne));
    }

    public short indirect_indexed_address(byte zAddress){
        short address = (short)(indirect_address(zAddress) + (this.registers.Y & 0xFF));
        return address;
    }

    public short indexed_indirect_address(byte address){
        byte bOne = this.mmu.zeroPageRead(address);
        byte bTwo = this.mmu.zeroPageRead((byte)(address+1));
        return (short)(EmulatorUtils.joinBytes(bTwo, bOne) + (this.registers.Y & 0xFF));
    }

    //get data
    public byte absolute_read(int xOrY){
        return this.mmu.read(EmulatorUtils.joinLittleEndian(fetch(),fetch()) + (xOrY & 0xFF));
    }

    public byte indrect_read(byte zAddress){
        byte bOne = this.mmu.zeroPageRead(zAddress);
        byte bTwo = this.mmu.zeroPageRead((byte)(zAddress+1));

        return this.mmu.read(EmulatorUtils.joinBytes(bTwo, bOne));
    }

    public byte indexed_indrect_read(byte address){
        // byte zAddress = (byte)(this.mmu.zeroPageRead(address) + this.registers.X);
        return indrect_read((byte)(address + this.registers.X));
        // return indrect((address & 0xFF) + (this.registers.X & 0xFF));
    }

    public byte indrect_indexed_read(byte address){
        byte bOne = this.mmu.zeroPageRead(address);
        byte bTwo = this.mmu.zeroPageRead((byte)(address+1));
        short test = (short) (EmulatorUtils.joinBytes(bTwo, bOne) + (this.registers.Y & 0xFF));
        return this.mmu.read(test);
        // return this.mmu.read(GBUtils.joinBytes(bOne, this.registers.Y));
    }

	@Override
	public byte[] save() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void load(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getMemory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[][] getDisplay() {
		// TODO Auto-generated method stub
		return new byte[256][512];
	}

	@Override
	public void CPUReset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearDisplay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void releaseKey(int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setKey(int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCurrentCycles() {
		return 4;
	}

	@Override
	public void loadToMemory(byte[] data, int romLength) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getRegister(int regNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EmulatorType getEmulatorType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String saveState(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadState(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EmulatorType getEmuType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getAudioBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
}
