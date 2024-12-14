package com.laconic.pcemulator.emulator;

import com.laconic.pcemulator.util.Palette;

//public class Display extends JPanel{
public class Display{
public void refreshDisplay(byte[][] data){
//    this.display=data;
//    isSprite = false;
//    repaint();
  }

  public void refreshDisplay(short[][] data){
//    this.colorDisplay = data;
//    repaint();
  }

  public void refreshDisplay(byte[][] sprite, Palette p){
//    this.display = sprite;
//    isSprite = true;
//    this.spritePalette = p;
//    repaint();
  }

  public void paintComponent(){//Graphics g){
//    super.paintComponent(g);
//    this.g = g;
//
//    for(int i=0; i<this.colorDisplay[0].length; i++){ //w=160
//      for(int j=0; j<this.colorDisplay.length; j++){ //h=144
//        try{
//          paintDisplay(colorDisplay[j][i],i,j);
//        }catch(Exception e){
//          e.printStackTrace();
//        }
//      }
//    }
  }

  private void paintDisplay(int color, int x,int y){
    // if(color==0){
    //   g.setColor(new Color(232,255,232));
    //   g.fillRect(x*multiplier,y*multiplier,1*multiplier,1*multiplier);
    // }else if(color==1){
    //   g.setColor(new Color(149,191,118));
    //   g.fillRect(x*multiplier,y*multiplier,1*multiplier,1*multiplier);
    // }else if(color==2){
      int r = (color & 0b111) << 5;
      int gr = (color & 0b00111000) << 2;
      int b = (color & 0b111000000) >> 1;
      try{
        // g.setColor(new Color(r,gr,b));

        // g.setColor(new Color(b,r,gr));
        // g.setColor(new Color(b,gr,r));

        // g.setColor(new Color(r,gr,b));
        // g.setColor(new Color(r,b,gr));

//        g.setColor(new Color(gr,b,r));
        // g.setColor(new Color(gr,r,b));

//        g.fillRect(x*multiplier, y*multiplier, 1*multiplier, 1*multiplier);
      }catch(Exception e){
        e.printStackTrace();
        System.out.println("r: "+r+" g: "+gr+" b: "+b+" color: "+color);
        System.exit(-1);
      }
    //   g.fillRect(x*multiplier,y*multiplier,1*multiplier,1*multiplier);
    // }else if(color==3){
    //   g.setColor(Color.black);
    //   g.fillRect(x*multiplier,y*multiplier,1*multiplier,1*multiplier);
    // }else{
    //   System.out.println("dunno this color: "+color);
    // }

      // g.setColor(new Color(r,g,b));

  }   

}
