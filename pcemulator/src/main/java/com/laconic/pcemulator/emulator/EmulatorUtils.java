package com.laconic.pcemulator.emulator;

public class EmulatorUtils{

    public static int joinBytes(byte byteOne, byte byteTwo){
        return (((byteOne & 0xFF) << 8) + (byteTwo & 0xFF));
    }

    public static int joinLittleEndian(byte byteOne, byte byteTwo){
        return (((byteTwo & 0xFF) << 8) + (byteOne & 0xFF));
    }

    public static byte[] splitBytes(int data){
        byte[] result = new byte[2];//little endian
        result[0] = (byte) (data >> 8);
        result[1] = (byte) data;
        return result;
    }

}
