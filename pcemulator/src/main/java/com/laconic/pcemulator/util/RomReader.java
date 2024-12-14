package com.laconic.pcemulator.util;

import java.io.File;
import java.io.FileInputStream;

public abstract class RomReader{

    public static byte[] readROM(String filename){
        byte[] rom;
        try{
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            long len = fis.getChannel().size();
            rom = new byte[(int)len];

            fis.read(rom);
            fis.close();
            return rom;
        }catch(Exception e){
            e.printStackTrace();
            return new byte[1];
        }
    }
}