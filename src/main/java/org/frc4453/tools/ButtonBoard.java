package org.frc4453.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import com.pi4j.platform.PlatformAlreadyAssignedException;


public class ButtonBoard {

	static char[] msgReport = new char[] { '\0', '\0'};
	final static char[] endReport = new char[] { '\0', '\0'};

	final static String devHID = "/dev/hidg0";

	final static int NUM_BUTTONS = 20;
	final static int MS_DEBOUNCE = 1000;

	static FileWriter out = null;

	final static Map<Pin, Integer> msg = new HashMap<Pin, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put(RaspiPin.GPIO_01, 1);
			put(RaspiPin.GPIO_02, 1<<1);
			put(RaspiPin.GPIO_03, 1<<2);
			put(RaspiPin.GPIO_04, 1<<3);
			put(RaspiPin.GPIO_05, 1<<4);
			put(RaspiPin.GPIO_06, 1<<5);
			put(RaspiPin.GPIO_07, 1<<6);
			put(RaspiPin.GPIO_10, 1<<7);

			put(RaspiPin.GPIO_11, 1<<8);
			put(RaspiPin.GPIO_12, 1<<9);
			put(RaspiPin.GPIO_13, 1<<10);
			put(RaspiPin.GPIO_14, 1<<11);
			put(RaspiPin.GPIO_15, 1<<12);
			put(RaspiPin.GPIO_16, 1<<13);
			put(RaspiPin.GPIO_21, 1<<14);
			put(RaspiPin.GPIO_22, 1<<15);
		}
	};
	
	/*
	 * #%L **********************************************************************
	 * ORGANIZATION : Pi4J PROJECT : Pi4J :: Java Examples FILENAME :
	 * GpioListenAllExample.java
	 *
	 * This file is part of the Pi4J project. More information about this project
	 * can be found here: http://www.pi4j.com/
	 * ********************************************************************** %%
	 * Copyright (C) 2012 - 2016 Pi4J %% This program is free software: you can
	 * redistribute it and/or modify it under the terms of the GNU Lesser General
	 * Public License as published by the Free Software Foundation, either version 3
	 * of the License, or (at your option) any later version.
	 *
	 * This program is distributed in the hope that it will be useful, but WITHOUT
	 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
	 * FOR A PARTICULAR PURPOSE. See the GNU General Lesser Public License for more
	 * details.
	 *
	 * You should have received a copy of the GNU General Lesser Public License
	 * along with this program. If not, see
	 * <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
	 */

	/**
	 * This example code demonstrates how to setup a listener for all available GPIO
	 * pins on the RaspberryPi (by specific model).
	 *
	 * @author Robert Savage
	 */

	/**
	 * [ARGUMENT/OPTION "--pull (up|down|off)" | "-l (up|down|off)" | "--up" |
	 * "--down" ] This example program accepts an optional argument for specifying
	 * pin pull resistance. Supported values: "up|down" (or simply "1|0"). If no
	 * value is specified in the command argument, then the pin pull resistance will
	 * be set to PULL_UP by default. -- EXAMPLES: "--pull up", "-pull down", "--pull
	 * off", "--up", "--down", "-pull 0", "--pull 1", "-l up", "-l down".
	 *
	 * @param args
	 * @throws InterruptedException
	 * @throws PlatformAlreadyAssignedException
	 * @throws IOException
	 */
	public static void main(String args[]) throws InterruptedException, PlatformAlreadyAssignedException, IOException {
		System.out.println("<-- The Pi4J Project -->\nGPIO Listen (All Pins) Example");

		System.out.println("Opening file: "+devHID);
		out = new FileWriter(devHID);		

		// create GPIO controller
		final GpioController gpio = GpioFactory.getInstance();

		// create pins collection to store provisioned pin instances
		List<GpioPinDigitalInput> provisionedPins = new ArrayList<>();
		Pin[] pins;

		// get a collection of raw pins based
		pins = RaspiPin.allPins();
		pins = ArrayUtils.removeElement(pins, RaspiPin.GPIO_00); // Can not address/send button 0 via USB
		pins = ArrayUtils.removeElement(pins, RaspiPin.GPIO_08); // Can pull down
		pins = ArrayUtils.removeElement(pins, RaspiPin.GPIO_09); // Can pull down
		pins = ArrayUtils.removeElement(pins, RaspiPin.GPIO_30); // Can pull down
		pins = ArrayUtils.removeElement(pins, RaspiPin.GPIO_31); // Can pull down

		// provision GPIO input pins with its internal pull down resistor set and
		// debounce 1000ms
		for (Pin pin : pins) {
			try {
				GpioPinDigitalInput provisionedPin = gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);
				provisionedPins.add(provisionedPin);

				provisionedPin.setDebounce(MS_DEBOUNCE);

				// unexport the provisioned GPIO pins when program exits
				provisionedPin.setShutdownOptions(true);
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		}

		System.out.println(" ... GPIO pins provisioned and ready for use.");

		// --------------------------------
		// EVENT-BASED GPIO PIN MONITORING
		// --------------------------------

		// create and register gpio pin listeners for event pins
		gpio.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {

				// Get button address and collapse removing unused buttons
				Pin pin = event.getPin().getPin();
								
				if (event.getState().isHigh()) {
					System.out.println("Setting button on USB");

					// shift button into message
						msgReport[1] |= (char) ((msg.get(pin) >>> 8) & 0x00ff);
						msgReport[0] |= (char) (msg.get(pin) & 0x00ff);						
									
					// write message to host computer
					try {
						writeReport(msgReport);
					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					System.out.println("Resetting button on USB");	

					// shift button into message
					msgReport[1] &= (char) ~((msg.get(pin) >>> 8) & 0x00ff);
					msgReport[0] &= (char) ~(msg.get(pin) & 0x00ff);						

					// write message to host computer
					try {
						writeReport(msgReport);
					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

				}

				// display pin state on console
				System.out.println(" --> GPIO PIN STATE CHANGE (EVENT): " + event.getPin() + " = " + event.getState());

			}
		}, provisionedPins.toArray(new GpioPinDigitalInput[0]));

		// keep program running until user aborts (CTRL-C)
		while (true) {
			Thread.sleep(500);
		}
	}

	static public void writeReport(char[] report) throws IOException, InterruptedException {

		System.out.println("Sending bytes:");
		System.out.println(String.format("0: 0x%08X", (int)report[0]));
		System.out.println(String.format("1: 0x%08X", (int)report[1]));

		try {
			out.write(report);
			System.out.println(report);
			out.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
