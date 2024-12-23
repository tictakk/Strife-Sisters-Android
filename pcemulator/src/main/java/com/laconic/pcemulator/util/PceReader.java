package com.strifesdroid.pcemulator.util;

import com.strifesdroid.pcemulator.pce.hucard.Hucard;

public class PceReader extends RomReader{
    
    public static Hucard loadCart(String filename){
        return new Hucard(readROM(filename));
    }
}