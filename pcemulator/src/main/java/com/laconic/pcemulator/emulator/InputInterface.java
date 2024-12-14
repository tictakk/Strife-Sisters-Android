package com.laconic.pcemulator.emulator;

public interface InputInterface {

    byte getButtons();
    byte getDirection();
    void setDirection(byte d);
    void setButton(byte b);
    void resetDirection(byte d);
    void resetButton(byte b);

}
