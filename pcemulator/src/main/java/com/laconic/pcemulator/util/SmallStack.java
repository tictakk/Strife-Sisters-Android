package com.laconic.pcemulator.util;

public class SmallStack{

  private short[] stack;
  private int stackSize;
  private short pointer=-1;

  public SmallStack(int stackSize){
    this.stack = new short[stackSize];
    this.stackSize = stackSize;
  }

  public void push(short op){
    if(this.pointer>=stackSize-1){
      //do nothing
    }else{
      this.stack[++this.pointer] = op;
    }
  }

  public short pop(){
    if(this.pointer<0){
      return -1;
    }
    return this.stack[this.pointer--];
  }

  public short getPointerLocation(){ return this.pointer; }

  public short[] getStack(){ return this.stack; }

  public int getStackSize(){ return this.stackSize; }
}
