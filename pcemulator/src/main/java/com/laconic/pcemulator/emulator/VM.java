package com.laconic.pcemulator.emulator;

import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Vector;
import java.util.zip.Deflater;

//import com.laconic.gameboy.GBJoypad;

public class VM{

    public Emulator emulator = null;
    protected Socket socket = null;
    protected BufferedOutputStream output = null;
    protected Deflater compressor = new Deflater();

    ByteBuffer buffer = ByteBuffer.allocate(23040);
    long startTime = 0;
    long endTime = 0;
    long cycle = 0;
    private static long SECOND = 1_000_000_000; // billion
    int hz = 60;
    int MAX_CYCLES = 69905;

    public boolean isAlive;
    public int id = -1;

    public int refreshes = 0;
    public long refreshAcc = 0;
    public long lastRefresh = 0;
    
    public boolean displaySent = true;

    public VM(Emulator emulator, Socket socket){
        this.emulator = emulator;
        this.socket = socket;
        isAlive = true;
        lastRefresh = System.nanoTime();
        try {
            this.output = new BufferedOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VM(){}

    public void stepToScreenUpdate(){
        if(!displaySent){
            return;
        }
        int cycles = 0;
        while(cycles < MAX_CYCLES){
            emulator.decode();
            cycles += emulator.getCurrentCycles();
        }
    }

    public void updateVideoDisplay(){
        if(!displayReady()){
            return;
        }
        try {
            buffer = ByteBuffer.allocate(23040);
            byte[] compressedData = new byte[5_000];
            byte[][] display = emulator.getDisplay();
            for (byte[] b : display) { // we should always flush the display after the appropriate cycle
                buffer.put(b);
            }
            compressor.setInput(buffer.array());
            compressor.finish();
            short length = (short)compressor.deflate(compressedData);

            byte[] len = new byte[2];
            len[0] = (byte)(length >> 8); //higher 8 bits
            len[1] = (byte) length;

            this.output.write("d".getBytes());//d = display flag
            this.output.write(len);//write the length of the compressed display
            for(int i=0; i<length; i++){
                this.output.write(compressedData[i]);
            }

            this.output.write(this.emulator.getAudioBuffer());
            compressor.reset();
            this.output.flush();

            displaySent = true;
            long newRefresh = System.nanoTime();
            refreshAcc += newRefresh - lastRefresh;
            lastRefresh = newRefresh;
            refreshes++;

            if(refreshAcc >= SECOND){
                System.out.println("FPS: "+refreshes);
                refreshes = 0;
                refreshAcc = 0;
            }

        } catch (Exception e) {
            isAlive = false;
            System.out.println("input shut: " + this.socket.isInputShutdown());
            System.out.println("output shut: " + this.socket.isOutputShutdown());
            disconnect();
            System.out.println(e.getMessage());
        }
    }

    public void sendHostList(Vector<Integer> hostList){
        int size = hostList.size();
        if(size < 1){
            return;
        }
        // System.out.println("sending host list: "+size);

        try{
            byte[] sizes = new byte[2];
            sizes[0] = (byte)(size >> 8);
            sizes[1] = (byte)size;

            this.output.write("l".getBytes()); //header that says we're going to send a host list
            this.output.write(sizes); //the number of host list we're sending
            for(int i : hostList){
                byte[] integ = new byte[4]; //please fucking rename this
                integ[0] = (byte)(i >> 24);
                integ[1] = (byte)(i >> 16);
                integ[2] = (byte)(i >> 8);
                integ[3] = (byte)i;
                this.output.write(integ);
            }
            this.output.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean displayReady(){
        return (System.nanoTime() - lastRefresh > (SECOND / 60));
    }

    protected void disconnect() {
        try {
            this.output.close();
            this.socket.close();
            this.output = null;
            this.socket = null;
            this.isAlive = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDisplay(String header){

    }
}
