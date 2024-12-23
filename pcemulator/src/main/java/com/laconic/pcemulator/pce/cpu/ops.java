package com.strifesdroid.pcemulator.pce.cpu;

public class ops {

    public enum AddressingModes{
        IMMEDIATE, ZERO_PAGE, ZERO_PAGE_X, ABSOLUTE,
        ABSOLUTE_X, ABSOLUTE_Y, INDRECT, INDEXED_INDRECT,
        INDRECT_INDEX;
    }
    
    public static String[][] opstrings = 
    {
        {"ADC","IMMEDIATE"}, {"ADC","ZERO_PAGE"}, 
        {"ADC","ZERO_PAGE_X"}, {"ADC","ABSOLUTE"}, {"ADC","ABSOLUTE_X"},
        {"ADC","ABSOLUTE_Y"}, {"ADC","INDRECT"}, {"ADC","INDEXED_INDRECT"}, 
        {"ADC","INDRECT_INDEXED"},{"AND","IMMEDIATE"},{"AND","ZERO_PAGE"},
        {"AND","ZERO_PAGE_X"}, {"AND", "ABSOLUTE"}, {"AND", "ABSOLUTE_x"},
        {"AND","ABSOLUTE_Y"},{"AND","INDRECT"},{"AND","INDEXED_INDRECT"},
        {"AND","INDRECT_INDEX"}
    };

    public static int[] opcodes = {
                                    8, 7, 3, 4, 6, 4, 6, 7, 3, 2, 2, 2, 7, 5, 7, 6,
                                    2, 7, 7, 4, 6, 4, 6, 7, 2, 5, 2, 2, 7, 5, 7, 6,
                                    7, 7, 3, 4, 4, 4, 6, 7, 3, 2, 2, 2, 5, 5, 7, 6,
                                    2, 7, 7, 2, 4, 4, 6, 7, 2, 5, 2, 2, 5, 5, 7, 6,
                                    7, 7, 3, 4, 8, 4, 6, 7, 3, 2, 2, 2, 4, 5, 7, 6,
                                    2, 7, 7, 5, 2, 4, 6, 7, 2, 5, 3, 2, 2, 5, 7, 6,
                                    7, 7, 2, 2, 4, 4, 6, 7, 3, 2, 2, 2, 7, 5, 7, 6,
                                    2, 7, 7, 0, 4, 4, 6, 7, 2, 5, 3, 2, 7, 5, 7, 6,
                                    4, 7, 2, 7, 4, 4, 4, 7, 2, 2, 2, 2, 5, 5, 5, 6,
                                    2, 7, 7, 8, 4, 4, 4, 7, 2, 5, 2, 2, 5, 5, 5, 6,
                                    2, 7, 2, 7, 4, 4, 4, 7, 2, 2, 2, 2, 5, 5, 5, 6,
                                    2, 7, 7, 8, 4, 4, 4, 7, 2, 5, 2, 2, 5, 5, 5, 6,
                                    2, 7, 2, 0, 4, 4, 6, 7, 2, 2, 2, 2, 5, 5, 7, 6,
                                    2, 7, 7, 0, 2, 4, 6, 7, 2, 5, 3, 2, 2, 5, 7, 6,
                                    2, 7, 2, 0, 4, 4, 6, 7, 2, 2, 2, 2, 5, 5, 7, 6,
                                    2, 7, 7, 0, 2, 4, 6, 7, 2, 5, 3, 2, 2, 5, 7, 6
                                   };
    // {
    //     {0x00,8}, {0x01,4}, {0x75,4}, {0x6D,5}, {0x7D, 5},
    //     {0x79,5}, {0x72,7}, {0x61,7}, {0x71,7},{0x29,2},{0x25,4},
    //     {0x35,4}, {0x2D, 5}, {0x3D, 5},{0x39,5},{0x32,7},{0x21,7},
    //     {0x31,7}
    // };
}