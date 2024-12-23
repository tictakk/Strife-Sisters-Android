package com.strifesdroid.pcemulator.emulator;

public interface Emulator {
  public abstract byte[] getMemory();
  abstract byte[][] getDisplay();
  abstract void CPUReset();
  abstract void clearDisplay();
  abstract void releaseKey(int key);
  abstract void setKey(int key);
  abstract void decode();
  int getCurrentCycles();
  // abstract void loadToMemory(File file);
  abstract void loadToMemory(byte[] data, int romLength);
  abstract int getRegister(int regNumber); //this will be an int for the moment
  abstract EmulatorType getEmulatorType();
  abstract int getMaxWidth();
  abstract int getMaxHeight();
  abstract String saveState(String username);
  abstract void loadState(String filename);
  abstract EmulatorType getEmuType();
  abstract byte[] getAudioBuffer();
  // abstract byte[][] loadDisplay();
}
