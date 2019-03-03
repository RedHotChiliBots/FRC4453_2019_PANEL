package org.frc4453.tools;

import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;

public class LedStrips {
	public static void main(String args[]) {
		Ws281xLedStrip leds = new Ws281xLedStrip(150, 12, 800000, 10, 255, 0, false, LedStripType.WS2811_STRIP_GRB, false);
		float o = 0;
		while(true) {
			
			for(int i = 0; i < 150; i++) {
				java.awt.Color c = java.awt.Color.getHSBColor(i*.2f + o, 1.0f, 1.0f);
				leds.setPixel(i, c.getRed(), c.getGreen(), c.getBlue());
			}

			leds.render();

			o+= 0.01;

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}