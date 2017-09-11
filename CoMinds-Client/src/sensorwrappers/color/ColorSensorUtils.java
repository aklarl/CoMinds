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

import sensorwrappers.exceptions.SensorException;
import lejos.nxt.ColorSensor;


/**
 * This class implements some missing features of the ColorSensor as the
 * getLightValue with an Exception if the light value is not between 0.0 and
 * 100.0.
 * 
 * @author Annabelle Klarl
 */
class ColorSensorUtils {

	/**
	 * returns the percentage of lightness underneath the ColorLightSensor (will
	 * not change whether the lamp is switched on or off)
	 * 
	 * @param light
	 *            the given ColorLightSensor
	 * @return the percentage of lightness
	 * @throws SensorNoiseException
	 *             if grey value is lower than 0.0 or higher than 100.0
	 */
	public static float getLightValue(ColorSensor light) throws SensorException {
		int rawColor = light.getRawLightValue();

		if (rawColor < 0) {
			throw new SensorException("Grey value too low!");
		}
		else if (rawColor > 1023) {
			throw new SensorException("Grey value too high!");
		}

		return rawColor / 10.0f;
	}
}
