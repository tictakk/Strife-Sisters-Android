package com.laconic.pcemulator.util;

import com.laconic.pcemulator.pce.hucard.Hucard;

public class PceReader extends RomReader{
    
    public static Hucard loadCart(String filename){
        return new Hucard(readROM(filename));
    }
}