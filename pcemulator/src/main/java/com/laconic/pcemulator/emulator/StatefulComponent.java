package com.strifesdroid.pcemulator.emulator;

import java.nio.ByteBuffer;

public interface StatefulComponent {
    abstract byte[] save();
    abstract void load(byte[] data);

    /*
      SAVE
    */
    default void appendBuffer(String header, byte[] data, ByteBuffer buffer){
        // System.out.println("buffer put "+header+": "+header.getBytes().length);
        buffer.put(header.getBytes());
        // System.out.println("data length: "+data.length);
        buffer.putInt(data.length);
        // System.out.println("data: "+data[0]+" "+ data[1] +" "+ data[2] +" "+data[3]);
        buffer.put(data);
      }

      default void appendByte(String header, byte symbol, ByteBuffer buffer){
        buffer.put(header.getBytes());
        buffer.put(symbol);
      }

      default void appendInt(String header, int symbol, ByteBuffer buffer){
        buffer.put(header.getBytes());
        buffer.putInt(symbol);
      }

      /*
       LOAD
      */

      default void loadData(String header, ByteBuffer buffer, byte[] component){
        int length;
        byte[] actualHeader = new byte[3];
        buffer.get(actualHeader);
        // System.out.println((new String(actualHeader)));
        // System.out.println(header.equals(new String(actualHeader)));
        length = buffer.getInt();
        buffer.get(component);
      }

      default int loadInt(String header, ByteBuffer buffer){
        byte[] actualHeader = new byte[3];
        buffer.get(actualHeader);
        // System.out.println((new String(actualHeader)));
        // System.out.println(header.equals(new String(actualHeader)));
        return buffer.getInt();
      }

      default byte loadByte(String header, ByteBuffer buffer){
        byte[] actualHeader = new byte[3];
        buffer.get(actualHeader);
        // System.out.println((new String(actualHeader)));
        // System.out.println(header.equals(new String(actualHeader)));
        return buffer.get();
      }
}   