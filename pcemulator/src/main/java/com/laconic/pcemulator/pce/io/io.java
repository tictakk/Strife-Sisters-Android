package com.laconic.pcemulator.pce.io;

public class io {
    //this will have all of the static-ish data from the gamepad which we will query
    /*
        D7 : CD-ROM base unit sense bit (1= Not attached, 0= attached)
        D6 : Country detection (1= PC-Engine, 0= TurboGrafx-16)
        D5 : Always returns '1'
        D4 : Always returns '1'
        D3 : Joypad port pin 5 (read)
        D2 : Joypad port pin 4 (read) 
        D1 : Joypad port pin 3 (read) / pin 7 (write) 
        D0 : Joypad port pin 2 (read) / pin 6 (write)
    */
}