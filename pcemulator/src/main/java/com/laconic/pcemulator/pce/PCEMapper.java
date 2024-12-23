package com.strifesdroid.pcemulator.pce;

public class PCEMapper {
    
    byte[][] MAP = new byte[0x8][0x2000];

    public void mapTo(int key, byte[] data){
        System.arraycopy(data, 0, MAP, 0, 0x2000);
    }

    public byte[] get(int key){
        return MAP[key];
    }
}