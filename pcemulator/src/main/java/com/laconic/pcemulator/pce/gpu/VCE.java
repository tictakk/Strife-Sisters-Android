package com.strifesdroid.pcemulator.pce.gpu;

public class VCE {

    private static long VCE_CLOCK = 21_477_270;
    private int divider = 4;

    private int control;
    private int addressSelect = 0;
    short[] palettes = new short[512];

    public byte read(int address){
        switch(address - 0x400){
            case 0x4: 
            return (byte)(this.palettes[addressSelect] & 0xFF);

            case 0x5:
            return (byte)(this.palettes[addressSelect] >> 8 );

            default:
            System.out.println("bad vce read location: "+(address - 0x400));
            return -1;
        }
        // return 1;
    }

    public void write(int address, byte data){
        switch(address - 1024){
            case 0x0: 
                this.control = (data&0xFF);//leave this just for temp;

                if((data & 0x1) == 0){
                    this.divider = 4;
                }else{
                    this.divider = 3;
                }
                break;

            case 0x1:
                if((data & 0x2) == 0x2){
                    this.divider = 2;
                    System.out.println("VCE clock set to 10MHz!!!");
                }
                break;

            case 0x2: //low
            this.addressSelect &= 0xFF00;
            this.addressSelect |= data;
            break;

            case 0x3: 
            this.addressSelect &= 0xFF;
            this.addressSelect |= data << 8;
            break;

            case 0x4: 
            try{
                this.palettes[addressSelect] = (short)((this.palettes[addressSelect] & 0xFF00) | data);
            }catch(Exception e){
                e.printStackTrace();
            }
            break;

            case 0x5: 
            this.palettes[addressSelect] = (short)((this.palettes[addressSelect] & 0x00FF) | (data << 8));
            this.addressSelect++;
            break;

            default: 
            System.out.println("error writing to VCE, address: "+(address - 1024)+", value: "+data);
            break;
        }
    }

    public int getClockSpeed(){
        return divider;
    }
    
    public int getHsync(){
        return (int)(((VCE_CLOCK / divider)/262)/60);
    }

    public int getVsync(){
        return (int)((1365 * 262)/divider);
    }

    public short[] getPalette(int palNumber){
        short[] tmp = new short[16];
        for(int i=0; i<16; i++){
            tmp[i] = palettes[palNumber*16+i];
        }
        return tmp;
    }

    public int getControl(){
        return this.control;
    }
}