package com.laconic.pcemulator.emulator;

import java.util.ArrayList;

import com.laconic.pcemulator.util.Endpoint;// gameboy.Endpoint;
//import com.laconic.gameboy.GBInterruptManager;
//import com.laconic.gameboy.GBSerial;

public class LinkCable implements Endpoint{

    Endpoint slave;
    // ArrayList<Endpoint> endpoints = new ArrayList<Endpoint>();

    public void linkEndpoint(Endpoint e){
        this.slave = e;
        // endpoints.add(e);
    }

    public void connectSlave(Endpoint slave){
        this.slave = slave;
    }

    @Override
    public byte transfer(byte sb) {
        return slave.transfer(sb);
    }

}