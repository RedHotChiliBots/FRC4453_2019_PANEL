package org.frc4453.tools;

import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;

public class LedStrips {
	public static void main(String args[]) {
		Ws281xLedStrip leds = new Ws281xLedStrip(150, 12, 800000, 10, 255, 0, false, LedStripType.WS2811_STRIP_GRB, false);
		double o = 0;
		while(true) {
			
			for(double i = 0; i < 150; i++) {
				double u = (Math.sin(i*.2 + o + (3*Math.PI/2)) + 1.0) * 127.0;
				double v = (Math.sin(i*.2 + o) + 1.0) * 127.0;
				leds.setPixel((int)i, (int)(255.0 - v), (int)v, (int)u);
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