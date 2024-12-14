package com.laconic.pcemulator.pce.hucard;

public class Hucard {
    public byte[] data;

    public Hucard(byte[] data){
        this.data = data;
    }

    public byte read(int address){
        return data[address];
    }
}