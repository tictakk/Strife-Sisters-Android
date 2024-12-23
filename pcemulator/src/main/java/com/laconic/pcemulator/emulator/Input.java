package com.strifesdroid.pcemulator.emulator;

//import com.laconic.gameboy.GBJoypad;
//import com.laconic.pcemulator.emulator gameboy.GBProcessor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class Input extends Thread {

    private BufferedInputStream input;
//    private GBJoypad joypad;
//    private GBProcessor cpu;
    private VM vm;
    private LinkerListener linker;

    public Input(Socket socket, VM vm, LinkerListener linker) {
        try {
            this.input = new BufferedInputStream(socket.getInputStream());
            this.vm = vm;
            this.linker = linker;
//            this.cpu = (GBProcessor) vm.emulator;
//            this.joypad = this.cpu.joypad;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                while (this.input.available() > 0) {
                    // System.out.println("received data");
                    byte[] key = new byte[2];
                    this.input.read(key, 0, 2);
                    if (key[0] == 49) {

                        mapButtonInput(key[1], true);

                    } else if (key[0] == 48) {

                        mapButtonInput(key[1], false);

                    } else if(key[0] == 108){ //loadrom

                        byte[] l = {key[1]};
                        int length = Integer.valueOf(new String(l));
                        byte[] fileName = new byte[length];
                        this.input.read(fileName,0,fileName.length);
                        String s = new String(fileName);
//                        this.cpu.load(s.trim());
//                        this.joypad = this.cpu.joypad;

                    } else if(key[0] == 109){  //save

                        byte[] l = {key[1]};
                        int length = Integer.valueOf(new String(l));
                        byte[] uname = new byte[length];
                        this.input.read(uname,0,uname.length);
                        String username = new String(uname);

//                        this.cpu.saveState(username);

                    } else if(key[0] == 111){ //load

                        byte[] l = {key[1]};
                        int length = Integer.valueOf(new String(l));
                        byte[] uname = new byte[length];
                        this.input.read(uname,0,uname.length);
                        String username = new String(uname);

//                        this.cpu.loadState(username);

                    } else if(key[0] == 50){
                        
                        // System.out.println("making host");
                        makeHost();

                    } else if(key[0] == 51){ 

                        byte[] id = new byte[this.input.available()];
                        this.input.read(id);

                        // System.out.println("before array copy");
                        byte[] realid = new byte[id.length+1];
                        System.arraycopy(id, 0, realid, 1, id.length);
                        // System.out.println("after array copy");

                        realid[0] = key[1];
                        String hid = new String(realid);
                        // System.out.println("from socket: "+hid);
                        connectToHost(Integer.parseInt(hid));

                    }else {
                        System.out.println("unrecognized input: " + key[0]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                this.input.close();
                this.input = null;
                // this.destroy();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void makeHost(){
        this.linker.makeMaster(this.vm.id);
    }

    private void connectToHost(int hostid){
        // System.out.println("connecting to host: "+hostid);
        this.linker.link(this.vm.id, hostid);
    }

    public void mapButtonInput(byte key, boolean direction){
        // byte[] k = {key};
        // System.out.println("getting input? "+new String(k));
        if(!direction){
            switch(key){
                case 'l': //left
//                    joypad.setDirection((byte)0b1101);
                    break;

                case 't': //right
//                    joypad.setDirection((byte)0b1110);
                    break;

                case 'u': //up
//                    joypad.setDirection((byte)0b1011);
                    break;

                case 'n': //down
//                    joypad.setDirection((byte)0b0111);
                    break;

                case 'a': //A
//                    joypad.setButton((byte)0b1110);
                    break;

                case 's': //B
//                    joypad.setButton((byte)0b1101);
                    break;

                case 'E': //start
//                    joypad.setButton((byte)0b0111);
                    break;

                case 'S': //select
//                    joypad.setButton((byte)0b1011);
                    break;
            }
        }else{
            switch(key){
                case 'l': //left
//                    joypad.resetDirection((byte)0b1101);
                    break;

                case 't': //right
//                    joypad.resetDirection((byte)0b1110);
                    break;

                case 'u': //up
//                    joypad.resetDirection((byte)0b1011);
                    break;

                case 'n': //down
//                    joypad.resetDirection((byte)0b0111);
                    break;

                case 'a': //A
//                    joypad.resetButton((byte)0b1110);
                    break;

                case 's': //B
//                    joypad.resetButton((byte)0b1101);
                    break;

                case 'E': //start
//                    joypad.resetButton((byte)0b0111);
                    break;

                case 'S': //select
//                    joypad.resetButton((byte)0b1011);
                    break;
            }
        }
    }
}
