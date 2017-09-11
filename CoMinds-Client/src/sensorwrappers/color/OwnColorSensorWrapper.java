/*
 *  CoMinds is a Java framework for collaborative robotic scenarios with
 *  Lego Mindstorms based leJOS. A complete documentation can be found
 *  in docs/thesis.pdf.
 *
 *  Copyright (C) 2010  Annabelle Klarl
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */
package sensorwrappers.color;

import observers.ButtonMonitor;
import observers.ButtonObserver;
import sensorwrappers.exceptions.SensorException;
import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import logging.Logger;


/**
 * This class takes two values that are the calibrated light values for black
 * and white. It therefore normalizes all light values so that a light value as
 * low as the calibrated black is normalized to 0.0 and a light value as high as
 * the calibrated white value is normalized to 100.0. All light values that are
 * smaller or bigger than black and white are normalized to 0.0 or 100.0. With
 * the static method {@link #initScaler(ColorSensor, ButtonMonitor)} you can
 * init a scaler with calibrating black and white.
 * 
 * @author Annabelle Klarl
 */
public class OwnColorSensorWrapper implements AbstractColorSensorWrapper {

	private static final Logger logger = Logger.getLogger();

	// default values for white and black
	public static final float DEFAULT_WHITE = 50f;
	public static final float DEFAULT_BLACK = 20f;
	private static float calibratedColor = -1;

	private ColorSensor light;
	private final float black;
	private final float white;
	private final float scaleFactor;

	/**
	 * Constructor
	 * 
	 * @param light
	 *            the color sensor with which to read the light values
	 * @param black
	 *            the light value that is calibrated for black
	 * @param white
	 *            the light value that is calibrated for white
	 */
	private OwnColorSensorWrapper(ColorSensor light, float black, float white) {
		this.light = light;
		this.black = black;
		this.white = white;

		this.scaleFactor = 100f / (white - black);
	}

	/**
	 * gets the current light value underneath the color sensor and normalizes
	 * it according to the calibrated black and white light values
	 * 
	 * @return a normalized light value between 0.0 and 100.0
	 * @throws SensorException
	 *             if the sensor reads a light value smaller than 0.0 or bigger
	 *             than 100.0
	 */
	public float getLightValue() throws SensorException {
		float lightValue = ColorSensorUtils.getLightValue(this.light);

		if (lightValue <= this.black) {
			return 0.0f;
		}
		else if (lightValue > this.black && lightValue < this.white) {
			return (lightValue - this.black) * this.scaleFactor;
		}
		else {
			return 100.0f;
		}

	}

	/**
	 * reads the light value currently underneath the ColorLightSensor
	 * 
	 * @param light
	 *            the color sensor with which to read the light values
	 * @param color
	 *            the color to be calibrated
	 * @param defaultValue
	 *            the default value for the color
	 * @return the calibrated light value for this color
	 */
	private static float calibrate(ColorSensor light, String color,
			float defaultValue) {
		calibratedColor = -1;

		System.out.println("calibrate " + color);

		synchronized (light) {
			try {
				light.wait();
			}
			catch (InterruptedException e) {
			}
		}

		calibratedColor = (calibratedColor == -1 ? defaultValue
				: calibratedColor);

		System.out.println(color + ": " + calibratedColor);
		logger.debug(color + ": " + calibratedColor);
		return calibratedColor;
	}

	/**
	 * initializes a light value scaler. It therefore calibrates the black and
	 * white light values and initializes the scaling according to the
	 * calibration data.
	 * 
	 * @param light
	 *            the color sensor with which to read the light values
	 * @param monitor
	 *            a ButtonMonitor to monitor the buttons during the calibration
	 * @return the light value scaler that scales according to the calibration
	 *         data
	 */
	public static OwnColorSensorWrapper initLightWrapper(final ColorSensor light,
			ButtonMonitor monitor) {
		ButtonObserver observer = new ButtonObserver() {
			@Override
			public void notifyForEvent(Button button) {
				if (button.equals(Button.ENTER)) {
					synchronized (light) {
						while (true) {
							try {
								calibratedColor = ColorSensorUtils
										.getLightValue(light);
								break;
							}
							catch (SensorException e) {
								// ignore noise and continue
								logger.warn("ColorSensor noise");
							}
						}
						light.notify();
					}
				}
				else if (button.equals(Button.ESCAPE)) {
					synchronized (light) {
						System.out.println("use standard");
						light.notify();
					}
				}
			}
		};
		if (!monitor.isAlive()) {
			monitor.start();
		}
		monitor.register(observer, Button.ESCAPE);
		monitor.register(observer, Button.ENTER);

		float white = calibrate(light, "white", DEFAULT_WHITE);
		float black = calibrate(light, "black", DEFAULT_BLACK);

		monitor.unregister(observer);

		// wait 'til user starts the behavior
		System.out.println("press enter");
		ButtonObserver startObserver = new ButtonObserver() {
			@Override
			public void notifyForEvent(Button button) {
				if (button.equals(Button.ENTER)) {
					synchronized (light) {
						light.notify();
					}
				}

			}
		};
		monitor.register(observer, Button.ENTER);

		synchronized (light) {
			try {
				light.wait();
			}
			catch (InterruptedException e) {
			}
		}

		monitor.unregister(startObserver);

		return new OwnColorSensorWrapper(light, black, white);
	}
}
