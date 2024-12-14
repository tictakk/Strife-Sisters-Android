package com.laconic.pcemulator.pce.io;

public class Gamepad {

    /*
        D7 : CD-ROM base unit sense bit (1= Not attached, 0= attached)
        D6 : Country detection (1= PC-Engine, 0= TurboGrafx-16)
        D5 : Always returns '1'
        D4 : Always returns '1'
        D3 : Joypad port pin 5 (read)
        D2 : Joypad port pin 4 (read) 
        D1 : Joypad port pin 3 (read) / pin 7 (write) 
        D0 : Joypad port pin 2 (read) / pin 6 (write)
    */

    public byte state = (byte)0b10111111;
    public byte directionState = 0b1111; //10001
    public byte buttonState = 0b1111;
    public boolean SEL = true;
    public boolean CLR = true;

    public enum button{
        UP, DOWN, LEFT, RIGHT, SELECT, RUN, I, II;
    }

    public void write(int address, byte data){
        // System.out.println("address: "+address+" gamepad data:"+data);
        switch(data){
            case 0:
            SEL = false;
            CLR = false;
            break;

            case 1:
            SEL = true;
            CLR = false;
            break;

            case 2:
            SEL = false;
            CLR = true;
            break;

            case 3:
            SEL = true;
            CLR = true;
        }
    }

    public byte read(int address){
        if(CLR){
            return 0;
        }else{
            return (byte)(state & ((SEL)?directionState : buttonState));
        }
    }

    public void buttonDown(button b){
        switch(b){
            case UP: directionState &= 0b1110;
            break;

            case DOWN: directionState &= 0b1011;
            break;

            case LEFT: directionState &= 0b0111;
            break;

            case RIGHT: directionState &= 0b1101;
            break;

            case RUN: buttonState &= 0b0111;
            break;

            case SELECT: buttonState &= 0b1011;
            break;

            case I: buttonState &= 0b1110;
            break;

            case II: buttonState &= 0b1101;
            break;
        }
    }

    public void buttonRelease(button b){
        switch(b){
            case UP: directionState |= ~0b1110;
            break;

            case DOWN: directionState |= ~0b1011;
            break;

            case LEFT: directionState |= ~0b0111;
            break;

            case RIGHT: directionState |= ~0b1101;
            break;

            case RUN: buttonState |= ~0b0111;
            break;

            case SELECT: buttonState |= ~0b1011;
            break;

            case I: buttonState |= ~0b1110;
            break;

            case II: buttonState |= ~0b1101;
            break;
        }
    }
}