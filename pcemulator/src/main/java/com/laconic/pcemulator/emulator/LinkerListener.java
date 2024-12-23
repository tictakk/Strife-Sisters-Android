package com.strifesdroid.pcemulator.emulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

//import com.laconic.chipeight.VMScheduler;

public class LinkerListener {

    public Vector<Integer> availableHosts = new Vector<Integer>();
    public Map<Integer,Integer> linked = new HashMap<Integer,Integer>();

    public LinkerListener(){} 

    public void makeMaster(Integer id){
        availableHosts.add(id);
    }

    public void removeHost(Integer id){
        availableHosts.remove(id);
    }

    public void link(Integer slave, Integer host){
        if(availableHosts.size() < 1){
            System.out.println("no gameboys available to link, master id: "+host);
            return;
        }

        if(availableHosts.remove(host)){
            this.linked.put(host, slave);
        }
    }

    public boolean hasLinked(){
        return this.linked.size() > 0;
    }

    public Vector getHosts(){
        return this.availableHosts;
    }

}