package com.strifesdroid.pcemulator.util;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
//import java.awt.Color;

public class Palette{

  Map<Byte, Color> palette;
  Map<Byte,Byte> numMap;

  public Palette(byte b){
    numMap = new HashMap<Byte,Byte>();
    numMap.clear();
    configurePalette(b);
  }

  private void configurePalette(byte b){
    numMap.put((byte)3,(byte)((0b11000000 & b) >> 6));
    numMap.put((byte)2,(byte)((0b00110000 & b) >> 4));
    numMap.put((byte)1,(byte)((0b00001100 & b) >> 2));
    numMap.put((byte)0,(byte)((0b11 & b)));
  }

  public Map<Byte,Byte> getNumMap(){
    return this.numMap;
  }

//  public Palette(){
//    numMap = new HashMap<Byte,Byte>();
//    numMap.clear();
//    configurePalette((byte)0xE4);
//  }

//  public void decodePalette(byte b, boolean isSprite){
//    palette.clear();
//
//    palette.put((byte)3,getColorFromCode((b & 0xC0) >> 6));
//    palette.put((byte)2,getColorFromCode((b & 0x30) >> 4));
//    palette.put((byte)1,getColorFromCode((b & 0x0C) >> 2));
//    if(isSprite){
//    }else{
//      palette.put((byte)0,getColorFromCode(b & 0x03));
//    }
//  }
//
//  public void createNewPalette(java.awt.Color c1, java.awt.Color c2, java.awt.Color c3, java.awt.Color c4){
//    palette.clear();
//    palette.put((byte)0,c1);
//    palette.put((byte)1,c2);
//    palette.put((byte)2,c3);
//    palette.put((byte)3,c4);
//  }

//  public java.awt.Color getColor(int num){
//    return this.palette.get((byte) num);
//  }
//
//  public Color getColorFromCode(int data){
//    if(numMap.get((byte)(data)) == 0){
//      return Color.white;
//    }else if(numMap.get((byte)(data)) == 0b01){
//      return Color.gray;
//    }else if(numMap.get((byte)(data)) == 0b10){
//      return Color.DARK_GRAY;
//    }else if(numMap.get((byte)(data)) == 0b11){
//      return Color.BLACK;
//    }else{
//      System.out.println(data);
//      return Color.red;
//    }
//  }
}
