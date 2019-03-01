package org.frc4453.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.platform.PlatformAlreadyAssignedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ButtonBoard {

	final static Logger logger = LogManager.getLogger(ButtonBoard.class);

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
	
	
	public static void main(String args[]) throws InterruptedException, PlatformAlreadyAssignedException, IOException {
		logger.info("Starting ButtonBoard...");

		logger.debug("Opening HID device: " + devHID);
		try{
			out = new FileWriter(devHID);		
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return;
		}

		logger.debug("Provisioning GPIO pins...");
		// create GPIO controller
		final GpioController gpio = GpioFactory.getInstance();

		// provision GPIO input pins with its internal pull down resistor set and
		// debounce 1000ms
		for (Pin pin : msg.keySet()) {
			try {
				if(pin.getSupportedPinPullResistance().contains(PinPullResistance.PULL_DOWN)) {
					logger.error("Pin " + pin + " does not support pulldown, please use another.");
					return;
				}
				
				GpioPinDigitalInput provisionedPin = gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);

				provisionedPin.setDebounce(MS_DEBOUNCE);

				// unexport the provisioned GPIO pins when program exits
				provisionedPin.setShutdownOptions(true);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}
		}

		logger.debug("Registering listener...");

		gpio.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {

				// Get button address and collapse removing unused buttons
				Pin pin = event.getPin().getPin();
								
				if (event.getState().isHigh()) {
					logger.debug("Setting button on USB");

					// shift button into message
						msgReport[1] |= (char) ((msg.get(pin) >>> 8) & 0x00ff);
						msgReport[0] |= (char) (msg.get(pin) & 0x00ff);						
									
					// write message to host computer
					try {
						writeReport(msgReport);
					} catch (IOException | InterruptedException e) {
						logger.error(e.getMessage());
					}

				} else {
					logger.debug("Resetting button on USB");	

					// shift button into message
					msgReport[1] &= (char) ~((msg.get(pin) >>> 8) & 0x00ff);
					msgReport[0] &= (char) ~(msg.get(pin) & 0x00ff);						

					// write message to host computer
					try {
						writeReport(msgReport);
					} catch (IOException | InterruptedException e) {
						logger.error(e.getMessage());
					}
				}

				// display pin state on console
				logger.debug("Button event: " + event.getPin() + " = " + event.getState());
			}
		}, msg.keySet().toArray(new GpioPinDigitalInput[0]));

		logger.info("ButtonBoard ready!");

		// keep program running until user aborts (CTRL-C)
		while (true) {
			Thread.sleep(500);
		}
	}

	static public void writeReport(char[] report) throws IOException, InterruptedException {

		logger.trace("Sending bytes:");
		logger.trace(String.format("0: 0x%08X", (int)report[0]));
		logger.trace(String.format("1: 0x%08X", (int)report[1]));

		try {
			out.write(report);
			out.flush();

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}
}
