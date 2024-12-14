package com.laconic.pcemulator.emulator;

import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.Deflater;

public class VirtualMachine extends Thread {

    private Emulator emulator = null;
    private Socket socket = null;
    protected BufferedOutputStream output = null;
    private Deflater compressor = new Deflater();
    // ByteBuffer buffer = ByteBuffer.allocate(23040);
    // private double id = 0.0;

    ByteBuffer buffer = ByteBuffer.allocate(23040);
    long startTime = 0;
    long endTime = 0;
    long cycle = 0;
    long cycleTime = 1_000_000_000; // billion
    int hz = 60;

    long doneTimestamp = 0;
    private long fps_counter = 0;
    // int ran = (new Random()).nextInt(100);

    @Override
    public void run() {
        if (socket != null) {
//      fps_counter = System.nanoTime();
            try {
                this.output = new BufferedOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        doneTimestamp = System.nanoTime();
        step();
    }

    public VirtualMachine(Emulator emulator, Socket socket) {
        super("VirtualMachine");
        try {
            this.emulator = emulator;
            this.socket = socket;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VirtualMachine(Emulator emulator) {
        this.emulator = emulator;
        System.out.println("initialized");
    }

    public void loop() { }

    public void step() {

        int MAX_CYCLES = 69905;
        long cycles = 0;
//        int counter = 0;

        // do a cycle every 1/60th of a second
        while (true) {

            startTime = System.nanoTime();
            while(cycles < MAX_CYCLES){
                emulator.decode();
                cycles += emulator.getCurrentCycles();
            }
            cycles = 0;
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

                this.output.write(len);
                for(int i=0; i<length; i++){
                    this.output.write(compressedData[i]);
                }
                compressor.reset();
                this.output.flush();

            } catch (Exception e) {
                System.out.println("input shut: " + this.socket.isInputShutdown());
                System.out.println("output shut: " + this.socket.isOutputShutdown());
                disconnect();
                System.out.println(e.getMessage());
                break;
            }
            pause(startTime);
            }
    }

    private void disconnect() {
        try {
            this.output.close();
            this.socket.close();
            this.output = null;
            this.socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadROM(String romName){ }

    public String saveState(){
        return emulator.saveState("");
    }

    public void pause(long start){
        long waitTime = System.nanoTime() - start;
        while(waitTime < (1_000_000_000 / 60)){
            try{
                Thread.sleep(1);
                waitTime = System.nanoTime() - start;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void sendCompressedStream(byte[] data){

    }

    private void sendStream(byte[] data){

    }


}
