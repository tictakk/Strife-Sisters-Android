package com.laconic.pcemulator.emulator;

//import com.laconic.gameboy.GBRegisters;
import com.laconic.pcemulator.emulator.EmulatorUtils;
import com.laconic.pcemulator.util.SmallStack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public abstract class EmulatorState{

  protected String path = "/home/root/projects/chip8_webservice/states/";
  // protected String path = "/home/matthew/projects/emulation_server/states/";
  protected File file = null;
  protected FileOutputStream fis = null;
  protected FileInputStream inputstream = null;
  protected String filename = null;
  protected int memorySize = 0;
  protected int numberOfRegisters = 0; //might not need this

  public EmulatorType type;

  // public EmulatorState(String user){}

  protected void writeDisplayToFile(byte[][] display){
    String header = "display\n";
    try{
      this.fis.write(header.getBytes());
      this.fis.write(display.length); //for gb
      for(byte[] b : display){
        this.fis.write(b);
      }
      this.fis.write("\n".getBytes());
    }catch(Exception e){
      System.out.println("error writing to file");
      e.printStackTrace();
    }
  }

  protected void writeCounterToFile(short counter){ //don't need this in GB
    String header = "counter\n";
    System.out.print(header);
    System.out.println(counter);
    try{
      this.fis.write(header.getBytes());
      this.fis.write((byte)(counter >> 8));
      this.fis.write((byte)(counter));
      this.fis.write("\n".getBytes());
    }catch(Exception e){
      System.out.println("error writing counter");
      e.printStackTrace();
    }
  }

  protected void writeReigstersToFile(byte[] registers){
    String header = "register\n";
    System.out.println("registers");
    try{
      for(byte b : registers){
        System.out.println(b);
        this.fis.write(header.getBytes());
        this.fis.write(b);
        this.fis.write("\n".getBytes());
      }
    }catch(Exception e){
      System.out.println("error writing registers");
      e.printStackTrace();
    }
  }

//  protected void writeReigstersToFile(GBRegisters registers){
//    String header = "register\n";
//    System.out.println("registers");
//    try{
//      this.fis.write(header.getBytes());
//      this.fis.write(registers.A);//A
//      this.fis.write(registers.B);//A
//      this.fis.write(registers.D);//A
//      this.fis.write(registers.H);//A
//      this.fis.write(registers.C);//A
//      this.fis.write(registers.F);//A
//      this.fis.write(registers.E);//A
//      this.fis.write(registers.L);//A
//      this.fis.write(GBUtils.splitBytes(registers.SP));//A
//      this.fis.write(GBUtils.splitBytes(registers.PC));//A
//      this.fis.write("\n".getBytes());
//    }catch(Exception e){
//      System.out.println("error writing registers");
//      e.printStackTrace();
//    }
//  }

  protected void writeAddrRegToFile(short addressReg){ // no need for GB
    String header = "addressreg\n";
    System.out.println("addressreg");
    System.out.println(addressReg);
    try{
      this.fis.write(header.getBytes());
      this.fis.write((byte)(addressReg >> 8));
      this.fis.write((byte)addressReg);
      this.fis.write("\n".getBytes());
    }catch(Exception e){
      System.out.println("error writing address reg");
      e.printStackTrace();
    }
  }

  protected void writeInstrToFile(short instr){ //don't really use this
    String header = "instruction\n";
    System.out.print(header);
    System.out.println(instr);
    try{
      this.fis.write(header.getBytes());
      this.fis.write((byte)(instr >> 8));
      this.fis.write((byte)instr);
      this.fis.write("\n".getBytes());
    }catch(Exception e){
      System.out.println("error writing current instruction");
      e.printStackTrace();
    }
  }

  protected void mapMemoryToFile(byte[] memory){
    String header = "memory\n";
    try{
      this.fis.write(header.getBytes());
      this.fis.write(memory);
      this.fis.write("\n".getBytes());
    }catch(Exception e){
      System.out.println("error writing registers");
      e.printStackTrace();
    }
  }

  protected SmallStack mapStackToFile(SmallStack stack){ //we need to return this because we empty the stack out when writing the data, need to write it back
    SmallStack stackState = new SmallStack(16);
    SmallStack resStack = new SmallStack(16);
    System.out.println("stack");
    System.out.println("~~~~pointer location~~~~~ "+stack.getPointerLocation());
    System.out.println("~~~~pointer as byte~~~~~ "+(byte)stack.getPointerLocation());
    try{
      String header = "stack\n";
      this.fis.write(header.getBytes()); //write header
      String location = "pointer\n";
      this.fis.write((byte) stack.getPointerLocation()); //write pointer
      this.fis.write("\n".getBytes()); //write newline
      String size = "size\n";
      this.fis.write(size.getBytes()); //write size header
      this.fis.write((byte) stack.getStackSize()); //write stack size
      this.fis.write("\n".getBytes()); //write newline
      while(stack.getPointerLocation() > -1){
          short s = stack.pop();
          System.out.println(s);
          stackState.push(s);
          this.fis.write((byte) (s >> 8));
          this.fis.write((byte) s);
          this.fis.write("\n".getBytes());
      }

      while(stackState.getPointerLocation() > -1){ //please change this, this is so bad
        resStack.push(stackState.pop()); //just reversing the temp stack
      }
    }catch(Exception e){
      System.out.println("error writing stack");
      e.printStackTrace();
    }
    return resStack;
  }

  protected void writeDelayTime(byte delayTimer){
    System.out.println("delayTimer: "+delayTimer);
    try{
      String header = "delay\n";
      this.fis.write(header.getBytes());
      this.fis.write(delayTimer);
      this.fis.write("\n".getBytes());
    }catch(Exception e){
      System.out.println("error writing delay timer");
      e.printStackTrace();
    }
  }

  protected void writeSoundTimer(byte soundTimer){
    System.out.println("soundTimer: "+soundTimer);
    try{
      String header = "sound\n";
      this.fis.write(header.getBytes());
      this.fis.write(soundTimer);
      this.fis.write("\n".getBytes());
    }catch(Exception e){
      System.out.println("error writing sound timer");
      e.printStackTrace();
    }
  }

  protected String generateFileExtension(){
    switch(this.type){

      case CHIP8: return ".ch8";

      case GB: return ".gb";

      case NONE: return "";

      default: 
        System.out.println("Error attempting to generate file extension");
        return null;
    }
  }

  protected void resetFileState(String filename){
    close();
    try{
      this.file = new File(path+filename+generateFileExtension());
      this.fis = new FileOutputStream(this.file);
      this.inputstream = new FileInputStream(this.file);
    }catch(Exception e){
      System.out.println("failed resetting connection");
      e.printStackTrace();
    }
  }

  protected boolean close(){
    try{
      this.file = null;
      this.fis.close();
      return true;
    }catch(Exception e){
      System.out.println("error attempting to close output stream");
      e.printStackTrace();
      return false;
    }
  }

//  protected abstract boolean save();
  // abstract void loadState();


}
