package com.laconic.pcemulator.pce.gpu;

//import com.laconic.gameboy.GBProcessor;
//import com.laconic.gameboy.GBUtils;
import com.laconic.pcemulator.pce.gpu.InterruptControl.InterruptType;


public class VDC {

    /*
        64 simultaneous sprites
        64KB of VRAM
        a background and foreground layer
    */
    private byte vdcSelect = 0;
    private byte MSB = 0;
    private byte LSB = 0;
    private int currentcycles = 0;

    private short readBuffer = (short)0xFFFF;

    private int hStart = 0;
    private int hEnd = 0;
    private int vStart;
    private int vEnd;
    private int vWidth;
    private int tileWidth;
    private int tileHeight;
    private boolean bg = false;

    private boolean frameReady = false;

    //VDC registers
    short STAT;
    byte AR;
    byte DATA;

    //VRAM registers
    public byte MAWR = 0x0; //where writing in VRAM begins
    public byte MARR = 0x1; //reading VRAM starts, auto-increments
    public byte VRR = 0x2; //VRAM read register
    public byte VWR; //VRAM data write register
    public byte CR = 0x5; //control register
    public byte RCR = 0x6; //Raster counter register
    public byte BXR = 0x7; //background x scroll register
    public final byte BYR = 0x8; //background y scroll register
    public byte MWR = 0x9; //Memory width register

    //display registers
    byte HPR; //horizontal synchronous register
    byte HDR; //horizontal display register
    byte VSR; //vertical synchronous register
    byte VDR; //vertical display register
    byte VCR; //vertical display ending position register

    //DMA registers
    byte DCR; //DMA control register
    byte SOUR; //source address register
    byte DESR; //DMA destination address register
    byte LENR; //DMA block length register
    byte SAT = 0x13; //sprite attribute table address register

    int y = 0;

    boolean vramDmaReq = false;
    boolean satbDmaReq = false;

    public short[] VRAM = new short[0x8000];
    short[][] virtualBackground = new short[512][1024];
    public short[] registers = new short[0x14];

    private InterruptControl ime;
    private VCE vce;

    private int scanline = 0;
    MODE mode;

    public int VCEClock = 4;
    private int[] screenSize = {342, 456, 684};
    private int virtualScreenHeight = 0;
    private int virtualScreenWidth = 0;

    private final int SCREEN_HEIGHT_MAX = 262;
    private final int SCREEN_WIDTH_MAX = 684;
//    private final int SCREEN_HEIGHT_MAX = 240;
//    private final int SCREEN_WIDTH_MAX = 256;
    private final int VDC_LINE_CLOCK = 1368;

    short[][] frameBuffer = new short[SCREEN_HEIGHT_MAX][SCREEN_WIDTH_MAX];
    int[] intFrameBuffer = new int[SCREEN_HEIGHT_MAX*SCREEN_WIDTH_MAX];
    short[] SATB = new short[256];
    short[] visibleSprites = new short[256];

    int satbdmaCounter = 0;
    int vramdmaCounter = 0;

    int bgY = 0;
    int bgDrawLine = 0;
    int putLineProgressClock = 0;
    int vdcProgressClock = 0;
    int imageIndex = 0;
    int drawBGIndex = 0;
    int drawBGYLine = 0;
    int drawBGLine = 0;
    int rasterCount = 64;
    int lineWidth = SCREEN_WIDTH_MAX;

    int width = 0;
    int currentOffset = 0;
    int previousOffset = 0;
    boolean drawing = false;

    int displayCounter = 0;
    int displayStart = 0;
    int displayEnd = 0;
    int displayReset = 0;
    int displayWidth = 0;

    int progressClock = 0;
    int verticalProgress = 0;

    int horizontalProgressClock = 0;
    int VDCPutLineProgressClock = 0;
    int putLine = 0;
    int verticalProgressClock = 0;

    int hsync = 0;
    int vsync = 0;

    int drawY = 0;
    int viewX = 0;
    int viewY = 0;

    int spriteCounter = 0;

    final int FRAME_WIDTH = 256;
    // int displayReset = (getVDS() + getVDW() + getVSW() + getVCR() + 3);

    public VDC(InterruptControl ime, VCE vce){
        this.ime = ime;
        this.vce = vce;
        this.hsync = vce.getHsync();
        this.vsync = vce.getVsync();
        // loadZeldaEnemy();
    }

    public void getVCEClock(){
        this.VCEClock = this.vce.getControl();
    }

    private void VDCProcess() {
		this.progressClock -= this.hsync;

        drawBGYLine++;

		if(drawBGYLine == SCREEN_HEIGHT_MAX)
            drawBGYLine = 0;

        if(drawBGYLine < displayStart) {//OVER SCAN
            drawOverscan();
        } else if(drawBGYLine < displayEnd) {//ACTIVE DISPLAY
            spriteSweep();
            drawBGLine = (drawBGYLine == (getVDS() + getVSW()) ? this.registers[0x08] : (drawBGLine + 1)) & (getVScreenHeight() * 8 - 1);
			drawBGLine();
            drawSpriteLine();
        }else {//OVER SCAN
            drawOverscan();
		}

		int vline = getVDS() + getVSW() + getVDW() + 1;
		if(vline > 261) 
			vline -= 261;
		if(drawBGYLine == vline) {
            // this.STAT |= (this.registers[0x05] & 0x0008) << 2;
            if(vBlankOn()){
                 requestVlank();
            }
            if(satbDmaReq) {//VRAMtoSATB
                SATB_DMA();
			}
		}

		rasterCount++;
		if(drawBGYLine == (getVDS() + getVSW() - 1))
			rasterCount = 64;

        if(rasterCount == this.registers[0x06] && (this.STAT & 0x20) == 0x00)
            if(rcrOn()){
                requestRCR();
            }
	}

	public void cycles(int numberOfCycles) {
		this.DMA(numberOfCycles);
		this.progressClock += numberOfCycles;

		while(progressClock >= hsync) {
            VDCProcess();
            this.hsync = vce.getHsync();
		}

		this.VDCPutLineProgressClock += numberOfCycles;
		if(this.VDCPutLineProgressClock >= this.hsync) {
			this.VDCPutLineProgressClock -= this.hsync;
            this.putLine++;
            this.displayStart = (getVDS() + getVSW());
            this.displayEnd = (getVDS() + getVSW() + getVDW());
			if(this.putLine == this.SCREEN_HEIGHT_MAX) {
				this.putLine = 0;
                // this.GetScreenSize();
//                System.out.println("ready");
                frameReady = true;
                this.drawBGYLine = 0;
                if(vramDmaReq){
                    VRAM_DMA();
                }
			}
		}
    }
    
    private void spriteSweep(){
        int spriteZeroY = SATB[0];
        spriteCounter = 0;
        visibleSprites = new short[256];

        for(int i=252; i>0; i-=4){
            int y = (this.SATB[i] & 0x3FF) - 64 + viewY;
            int attr = (this.SATB[i+3] & 0xFFFF);
            int spriteHeight = (((attr & 0x3000)>>8) + 16);
            // int spriteWidth = (((attr & 0x100) == 0)? 16 : 32);

            if(y <= drawBGYLine && y+spriteHeight >= drawBGYLine){
                visibleSprites[(spriteCounter*4)] = SATB[i];
                visibleSprites[(spriteCounter*4)+1] = SATB[i+1];
                visibleSprites[(spriteCounter*4)+2] = SATB[i+2];
                visibleSprites[(spriteCounter*4)+3] = SATB[i+3];
                spriteCounter++;
            }
        }
        if(spriteCounter > 16){
            System.out.print("");
            //trigger sprite overflow
        }
    }
    
    public boolean frameReady(){
        return frameReady;
    }

    private void requestVlank(){
        this.STAT |= 0x20;
        this.ime.requestInterrupt(InterruptControl.InterruptType.IRQ1);
    }

    private void requestRCR(){
        //THIS IS WHERE WE CAN INSPECT THE BUG IN OUR EMULATOR WHERE YOKAI SCROLLS TO END OF MAP
        this.STAT |= 0x4; 
        this.ime.requestInterrupt(InterruptControl.InterruptType.IRQ1);
    }

    private void drawSpriteLine(){
        //TODO: PRETTY CONFIDENT THAT THE SPRITE IS SHOWING ON THE CORRECT COORDS, BUT THE BACKGROUND IS ACTUALLY OFF
        //      BY A FEW PIXELS
        if(!spritesOn()){
            return;
        }
        viewX = (getHSW() - 1)*8 + (getHDS() - 1) * 8; //characters
        int viewMaxX = viewX + (getHDW() - 1)*8;
        viewY = getVDS()+ 2; //scanlines
        // for(int i=252; i>0; i-=4){    
        for(int i=0, s=0; s<spriteCounter; i+=4, s++){    
            int y = (this.visibleSprites[i] & 0x3FF) - 64 + viewY;
            int attr = (this.visibleSprites[i+3] & 0xFFFF);
            int spriteWidth = (((attr & 0x100) == 0)? 16 : 32);
            int spriteHeight = (((attr & 0x3000)>>8) + 16);
            int mask;
            if(spriteHeight == 64){
                mask = 6;
            }else if(spriteHeight == 32){
                mask = 2;
            }else{
                mask = 0;
            }
            mask |= (spriteWidth >> 5);
            mask <<= 1;
            int line = drawBGYLine;
            boolean xInvert = ((attr & 0x800) == 0x800);
            boolean yInvert = ((attr & 0x8000) == 0x8000);
            // int line = (this.scanline) - (getVDS() + getVSW()) + 64;

            // if(line <= (spriteHeight+y-1) && line >= y){   
                int xStart = (this.visibleSprites[i+1] & 0x3FF); 

                if((xStart + spriteWidth) > (viewX*8) || (xStart) < (viewMaxX*8)){
                    int linesIntoSprite = spriteHeight - ((spriteHeight + y) - line);
                    if(yInvert){
                        linesIntoSprite = (spriteHeight-1) - linesIntoSprite;
                    }

                    int patternAddress = (((this.visibleSprites[i+2] & 0x7FE) & ~mask));
                    int address = ((patternAddress) << 5); //Keep in mind, bit 0 is important when the MWR sets CG modes
                    int paletteNumber = (attr & 0xF);
                    boolean priority = ((attr & 0x80) == 0x80);
                    int paletteZero = this.vce.getPalette(0)[0] & 0xFFFF;    
                    int addressOffset = ((linesIntoSprite / 16)*128);
                    // short[][] tile = new short[1][1];
                    short[] tileLine = new short[1];

                    try{
                        tileLine = getTileBySprite((address+addressOffset), paletteNumber,spriteWidth,linesIntoSprite%16);
                        tileLine = (xInvert) ? reverseSpriteLine(tileLine) : tileLine;

                        // tile = getTileBySprite((address+addressOffset), paletteNumber,spriteWidth);
                        // if(xInvert)
                        //     tileLine = reverseSpriteLine(tile[linesIntoSprite%16]);
                        // else
                        //     tileLine = tile[linesIntoSprite%16];

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    int end = (getHSW() + getHDE() + getHDW())*8;
                    for(int z=0; z<spriteWidth; z++){
                        try{
                            if((xStart+z) < end){
                                if(tileLine[z] != 0x200){
                                    if(frameBuffer[drawBGYLine][(xStart+z)] == paletteZero || priority){ //transparent bg
                                        if(yInvert){
                                            frameBuffer[drawBGYLine][(xStart+z)] = tileLine[z];
                                            intFrameBuffer[(drawBGYLine*FRAME_WIDTH)+(xStart+z)] = tileLine[z];
                                        }else{
                                            frameBuffer[drawBGYLine][(xStart+z)] = tileLine[z];
                                            intFrameBuffer[(drawBGYLine*FRAME_WIDTH)+(xStart+z)] = tileLine[z];
                                        }
                                    }
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                // }
            }
        }
    }

    private void drawBGLine(){
        if(!bgOn()){
            return;
        }
        int width = ((getHSW() + getHDE() + getHDW() + getHDS() + 1) << 3);
        lineWidth = getLineWidth(width);

        int line = drawBGLine;

        int tileY = ( ((line / 8) & (getVScreenHeight()-1)) * (getVScreenWidth()));
        int tileX = (this.registers[BXR] >> 3) & (getVScreenWidth() - 1);
        int offset = ((getHDS() + getHSW()) << 3);
        short pixel = 0;

        for(int i=0; i<((getHDW()+1)*8); i+=8){
            try{
                // short[][] tile = getTile(tileY+(tileX + (i/8)));
                short[] tile = getTile(tileY+(tileX+(i/8)), line%8);
                for(int j=0; j<8; j++){
                    // pixel = tile[line%8][j];
                    pixel = tile[j];
                    frameBuffer[drawBGYLine][(i+j)] = pixel;
//                    intFrameBuffer[(drawBGYLine*FRAME_WIDTH)+(offset+i+j)] = pixel;
                    intFrameBuffer[(drawBGYLine*FRAME_WIDTH)+(i+j)] = pixel;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void drawOverscan(){
        int lineWidth = ((getHSW() + getHDE() + getHDW() + getHDS() + 1) << 3);
        lineWidth = getLineWidth(lineWidth);
        int offset = ((getHDS() + getHSW()) << 3);
        short paletteZero = this.vce.getPalette(16)[0];

        // for(int i=0; i<((getHDW()+1)*8); i+=8){
        for(int i =0; i<lineWidth-offset*8; i+=8){
//        for(int i=offset; i<lineWidth; i+=8){
            try{
                // short[][] tile = getTile(tileY+(tileX + (i/8)));
                for(int j=0; j<8; j++){
                    // bgLine[i+j] = tile[scanline % 8][j];
                     frameBuffer[drawY][offset+(i+j)] = paletteZero;//tile[scanline % 8][j];
//                    frameBuffer[drawBGYLine][offset+(i+j)] = paletteZero;//tile[scanline % 8][j];
                    intFrameBuffer[(drawBGYLine*FRAME_WIDTH)+(offset+i+j)] = paletteZero;
                }
            }catch(Exception e){
                e.printStackTrace();
                // System.out.println("line width: "+lineWidth);
            }
        }
    }

    private int getLineWidth(int width){
        if(width <= screenSize[0]){
            return screenSize[0];
        }else if(width <= screenSize[1]){
            return screenSize[1];
        }else{
            return screenSize[2];
        }
    }

    public int[] getIntArrayDisplay(){
        // if(bgOn()){
        // return frameBuffer;
        frameReady = false;
        int vtotal = displayEnd - displayStart;
        int hStart = (getHDS()-1)*8;
        int hWidth = (getHDW()-1)*8;
        int hEnd = (getHDE()-1)*8;
        int hsnc = ((getHSW())*8);
        int line = hWidth + hEnd + hStart;
//            return frameBuffer;
//        if(true){
        if(line < 0){
            return intFrameBuffer;
        }

//        int[] newBuffer = new int[vtotal*(hWidth+hStart+hEnd)];
        int[] newBuffer = new int[256*240];
        for(int i=hStart; i<256; i++){//horizontal
            for(int j=0; j<240; j++){//vert
//        for(int i=hStart; i<hWidth+hStart+hEnd; i++){//horizontal
//            for(int j=displayStart; j<displayEnd; j++){//vert
                try{
                    newBuffer[(j*vtotal)+i] = intFrameBuffer[(j*vtotal)+(i)];
//                    newBuffer[((j-displayStart)*vtotal)+i] = intFrameBuffer[(j*vtotal)+(i)];
//                    newBuffer[j-displayStart][i-hStart] = frameBuffer[j][i+hEnd-hStart];
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return newBuffer;
    }

    public short[][] getDisplay(){
        // if(bgOn()){
            // return frameBuffer;
            frameReady = false;
            int vtotal = displayEnd - displayStart;
            int hStart = (getHDS()-1)*8;
            int hWidth = (getHDW()-1)*8;
            int hEnd = (getHDE()-1)*8;
            int hsnc = ((getHSW())*8);
            int line = hWidth + hEnd + hStart;
//            return frameBuffer;
            if(line < 0){
                return frameBuffer;
            }

            short[][] newBuffer = new short[vtotal][hWidth+hStart+hEnd];
            for(int i=hStart; i<hWidth+hStart+hEnd; i++){//horizontal
                for(int j=displayStart; j<displayEnd; j++){//vert
                    try{
                        newBuffer[j-displayStart][i-hStart] = frameBuffer[j][i+hEnd-hStart];
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            return newBuffer;
    }

    private enum MODE{
        BACKGROUND_PROCESSING, SPRITE_PROCESSING;
    }

    public byte read(int address){
        switch(address){
            case 0:
            byte ret = (byte)(this.STAT & 0xFF);
            this.ime.acknowledgeInterrupt(InterruptControl.InterruptType.IRQ1);
            // return (byte)(this.STAT & 0xFF);
            // this.STAT &= ~0x3F;
            this.STAT &= 0x40;
            return ret;

            case 1:
            return (byte)0xFF;

            case 2:
            byte res = (byte)this.VRAM[this.registers[MARR] & 0xFFFF];
            return res;

            case 3:
            byte result = (byte)(this.VRAM[this.registers[MARR] & 0xFFFF] >> 8);
            this.registers[MARR] += getIW();
            return result;

            default:
        }
        return 1;
    }

    public VCE getVCE(){
        return this.vce;
    }

    public void write(int address, byte data){
         switch(address){

            case 0:
            vdcSelect = data;
            break;

            case 2:
            LSB = data;
            registers[vdcSelect] &= 0xFF00;
            registers[vdcSelect] |= (LSB & 0xFF);

            if(vdcSelect == BYR){
                this.drawBGLine = this.registers[BYR];
            }
            break;

            case 3:
            MSB = data;
            registers[vdcSelect] &= 0xFF;
            registers[vdcSelect] |= (short)((MSB & 0xFF) << 8);
            checkMSBtrigger();
            break;

            default:
            // System.out.println("error writing to VDC address: "+address+", data: "+data);
        }
    }
    
    private short getIW(){
        switch(((this.registers[CR] & 0xFFFF) >> 11) & 0b11){
            case 0:
            return 1;

            case 1:
            return 32;

            case 2:
            return 64;

            case 3:
            return 128;

            default:
            System.out.println("error auto incrementing VRAM write");
            return -1;
        }
    }

    public int getVDS(){ //vertical display start
        return (((this.registers[0xC] & 0b1111111100000000) >> 8) & 0xFFFF);
    }

    public int getVSW(){ //vertical sync pulse width
        return ((this.registers[0xC] & 0b1111) & 0xFFFF);
    }

    public int getVCR(){ //vertical control - end
        return ((this.registers[0xE] & 0x7F)) & 0xFFFF;
    }

    public int getVDW(){ //vertical display width
        return (((this.registers[0xD] & 0xFF)) & 0xFFFF);
    }

    public int getHDS(){ //horizontal display start
        // return (((this.registers[0xA] & 0b111111100000000) >> 8) & 0xFFFF) - 1;
        return (((this.registers[0xA] & 0b111111100000000) >> 8) & 0xFFFF);
    }

    public int getHSW(){ //horizontal sync width
        return ((this.registers[0xA] & 0b1111) & 0xFFFF);
    }

    public int getHDE(){ //horizontal display end
        return (((this.registers[0xB] & 0b111111100000000) >> 8) & 0xFFFF);
    }

    public int getHDW(){ //horizontal display width (in tiles)
        return ((this.registers[0xB] & 0b1111111) & 0xFFFF);
    }

    private int getVScreenHeight(){
        int height = (((this.registers[MWR] & 0x40) >> 6) == 0)? 32 : 64;
        return height;
    }

    private int getVScreenWidth(){
        switch((this.registers[MWR] & 0x30) >> 4){
            case 0b00: return 32;
            case 0b01: return 64;
            default: return 128;
        }
    }

    public short[][] getTileBySprite(int address, int paletteNumber,int width){
        short[][] tile = new short[16][width];
        short[] row = new short[width/4];
        // int rows = 16;
        int planes = width/4;

        for(int i=0; i<16; i++){
            for(int j=0; j<planes; j++){
                row[j] = VRAM[(address)+(j*16)+i];
            } 
            tile[i] = decodeSpritePalette(row, (16+paletteNumber),width);
        }

        return tile;
    }

    public short[] getTileBySprite(int address, int paletteNumber,int width, int lineOffset){
        short[] tile = new short[width];
        short[] row = new short[width/4];
        // int rows = 16;
        int planes = width/4;

        // for(int i=0; i<16; i++){
            for(int j=0; j<planes; j++){
                row[j] = VRAM[(address)+(j*16)+lineOffset];
            } 
            tile = decodeSpritePalette(row, (16+paletteNumber),width);
        // }

        return tile;
    }

    private short[] reverseSpriteLine(short[] spriteLine){
        int length = spriteLine.length;
        short[] reversed = new short[length];

        for(int i=0; i<length; i++){
            reversed[i] = spriteLine[(length-i-1)];
        }
        return reversed;
    }

    private short[] decodeSpritePalette(short[] bitplaneRow, int paletteNumber, int spriteWidth){
        // short[] row = new short[16];
        short[] paletteIndices = new short[spriteWidth];
        short[] palette = this.vce.getPalette(paletteNumber);
        byte index;
        for(int s=0; s<spriteWidth/16; s++){
            for(int i=0; i<16; i++){
                int testBit = (1 << 15-i);
    
                //It's profoundly stupid why I have to & rowData[n] with 0xFF
                //but Java thinks fucking -2 >> 7 == -1. fuck off 2's compliment
                index = (byte)( 
                              ((( (bitplaneRow[0+(s*4)] & 0xFFFF) & (testBit)) >> 15-i) << 0) | 
                              ((( (bitplaneRow[1+(s*4)] & 0xFFFF) & (testBit)) >> 15-i) << 1) |
                              ((( (bitplaneRow[2+(s*4)] & 0xFFFF) & (testBit)) >> 15-i) << 2) |
                              ((( (bitplaneRow[3+(s*4)] & 0xFFFF) & (testBit)) >> 15-i) << 3)
                              );          
    
                try{
                    if(index == 0){
                        paletteIndices[i+(s*16)] = 0x200;
                    }else{
                        paletteIndices[i+(s*16)] = palette[index];
                    }
                }catch(Exception e){
                    System.out.println("index: "+index);
                    e.printStackTrace();
                }  
            }
        }
        return paletteIndices;
        // return row;
    }

    
    public short[] decodeToPalette(byte[] rowData, int paletteNumber){
        short[] paletteIndices = new short[8];
        short[] palette = this.vce.getPalette(paletteNumber);
        short backdrop = this.vce.getPalette(0)[0];
        byte index;
        for(int i=0; i<8; i++){
            int testBit = (byte)(1 << 7-i);

            //It's profoundly stupid why I have to & rowData[n] with 0xFF
            //but Java thinks fucking -2 >> 7 == -1. fuck off 2's compliment

            index = (byte)( 
                          ((( (rowData[0] & 0xFF) & testBit) >> 7-i) << 0) |
                          ((( (rowData[1] & 0xFF) & testBit) >> 7-i) << 1) |
                          ((( (rowData[2] & 0xFF) & testBit) >> 7-i) << 2) |
                          ((( (rowData[3] & 0xFF) & testBit) >> 7-i) << 3)
                          );

            try{
                if(index == 0){
                    paletteIndices[i] = backdrop;
                }else{
                    paletteIndices[i] = palette[index];
                }
            }catch(Exception e){
                e.printStackTrace();
            }                
        }
        return paletteIndices;
    }

    // public short[][] getTile(int pointerIndex){
    //     int pointer = VRAM[pointerIndex] & 0xFFFF;//(((VRAM[pointerIndex] & 0xFFF)) << 4);
    //     int pointerAddress = ((pointer & 0xFFF) << 4);
    //     byte paletteNumber = (byte)((pointer & 0xF000) >> 12); //26464
    //     short[][] tile = new short[8][8];
    //     byte[] row = new byte[4];
    //     int rows = 8;
    //     // int trans = translationStartPosition(pointerIndex);
        
    //     for(int i=0; i<rows; i++){
    //         short secondBytes = VRAM[pointerAddress+8+i];
    //         short firstBytes = VRAM[pointerAddress+i];
    //         row[1] = (byte)((firstBytes & 0xFF00)>>8);
    //         row[0] = (byte)(firstBytes & 0xFF);
            
    //         row[3] = (byte)((secondBytes & 0xFF00)>>8);
    //         row[2] = (byte)((secondBytes & 0xFF));

    //         tile[i] = decodeToPalette(row, paletteNumber);
    //     } 

    //     return tile;
    // }

    public short[] getTile(int pointerIndex, int lineOffset){
        int pointer = VRAM[pointerIndex] & 0xFFFF;//(((VRAM[pointerIndex] & 0xFFF)) << 4);
        int pointerAddress = ((pointer & 0xFFF) << 4);
        byte paletteNumber = (byte)((pointer & 0xF000) >> 12); //26464
        short[] tile = new short[8];
        byte[] row = new byte[4];
        
        short secondBytes = VRAM[pointerAddress+8+lineOffset];
        short firstBytes = VRAM[pointerAddress+lineOffset];
        row[1] = (byte)((firstBytes & 0xFF00)>>8);
        row[0] = (byte)(firstBytes & 0xFF);
            
        row[3] = (byte)((secondBytes & 0xFF00)>>8);
        row[2] = (byte)((secondBytes & 0xFF));

        tile = decodeToPalette(row, paletteNumber);

        return tile;
    }

    private boolean vBlankOn(){
        return ((this.registers[0x5] & 0b1000) == 0b1000);
    }

    private boolean rcrOn(){
        return ((this.registers[0x5] & 0b100) == 0b100);
    }

    private boolean bgOn(){
        return ((this.registers[0x5] & 0b10000000) != 0);
    }

    private boolean spritesOn(){
        return ((this.registers[0x5] & 0b1000000) != 0);
    }

    private void checkMSBtrigger(){
        switch(vdcSelect){
            case 0x2:
                try{
                    this.VRAM[(registers[MAWR] & 0xFFFF)] = this.registers[0x2];
                }catch(Exception e){
                    e.printStackTrace();
                }
                this.registers[MAWR] += getIW();
            break;

            case 0xF:
            break;

            case BYR:
                this.drawBGLine = this.registers[BYR];
            break;

            case 0x12:
                vramDmaReq = true;
            break;

            case 0x13:
                satbDmaReq = true;
            break;
        }
    }

    /*
    *---------------*
    * DMA FUNCTIONS |
    *---------------*
    */ 
    private void SATB_DMA(){
        int address = (this.registers[0x13] & 0xFFFF);
        for(int i=0; i<256; i++){
            this.SATB[i] = this.VRAM[address + i];
        }
        if(((this.registers[0xF] & 0xFFFF) & 0x1) == 0x1){
            // this.STAT |= 0x40;
        }
        satbdmaCounter = 256 * this.vce.getClockSpeed();
    }

    private void VRAM_DMA(){
        //TODO: This does not account for the fact that dma will be halted if it's running
        //while active display begins. It will need to continue the dma after the new active
        //display ends.
        short dmaControl = this.registers[0xF];
        boolean vramIrqEnable = ((dmaControl & 0xFFFF) & 0x2) == 0x2;
        int incSrc = (((dmaControl & 0xFFFF) & 0x4) == 0x4)? 1 : -1;
        int incDst = (((dmaControl & 0xFFFF) & 0x8) == 0x8)? 1 : -1;
        int src = (this.registers[0x10] & 0xFFFF);
        int dst = (this.registers[0x11] & 0xFFFF);
        int len = (this.registers[0x12] & 0xFFFF);

        if(((this.registers[0xF] & 0xFFFF) & 0x1) == 0x1){
            // this.STAT |= 0x40; 
        }

        for(; len>0; len--){
            this.VRAM[dst] = VRAM[src];
            dst += incDst;
            src += incSrc;
        }

        this.registers[0x10] = (short)src;
        this.registers[0x11] = (short)dst;
        this.registers[0x12] = (short)0xFFFF;
        this.vramDmaReq = false;    

        vramdmaCounter = this.vce.getClockSpeed();
    }

    private void DMA(int cycles){
        //TODO: SAT_DMA irq occurs 4 lines AFTER the dma is finished, this will need to be accounted for
        if(satbdmaCounter > 0){
            satbdmaCounter -= cycles;
            if(satbdmaCounter <= 0){
                if(((this.registers[0xF] & 0xFFFF) & 0x1) == 0x1){
                    this.STAT |= 0x8;
                    this.ime.requestInterrupt(InterruptType.IRQ1);
                    this.satbDmaReq = ((this.registers[0xF] & 0xFFFF) & 0x10) == 0x10;
                }
            }
        }

        if(vramdmaCounter > 0){
            vramdmaCounter -= cycles;
            if(vramdmaCounter <= 0){
                if(((this.registers[0xF] & 0xFFFF) & 0x2) == 0x2){
                    this.STAT |= 0x10;
                    this.ime.requestInterrupt(InterruptType.IRQ1);
                }
            }
        }
    }

    public short getStatus(){ return this.STAT; }

    public byte getSelect(){ return vdcSelect; }

    public short getMWR(){ return this.registers[0]; }

    public short getMRR(){ return this.registers[1]; }
}