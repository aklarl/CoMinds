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
package sensorwrappers.compass;

import sensorwrappers.exceptions.SensorException;
import lejos.nxt.addon.CompassSensor;


/**
 * This class wraps a compass sensor so that when the compass sensor cannot read
 * correctly the NoDataAvailable-Exception will be thrown.
 * 
 * @author Annabelle Klarl
 */
public class OwnCompassSensorWrapper implements AbstractCompassSensorWrapper {

	private CompassSensor sensor;
	private float degree = -1;
	private long lastCall = -1;
	private static final long CALL_INTERVAL = 500;

	/**
	 * Constructor
	 * 
	 * @param sensor
	 *            the sensor to be read
	 */
	public OwnCompassSensorWrapper(CompassSensor sensor) {
		this.sensor = sensor;
	}

	@Override
	public float getDegree() throws SensorException {
		long now = System.currentTimeMillis();

		if (now - this.lastCall > CALL_INTERVAL) {
			this.degree = this.sensor.getDegrees();
			this.lastCall = now;
		}

		if (this.degree < 0) {
			throw new SensorException("The own sensor returned -1.");
		}
		else {
			return this.degree;
		}
	}

}
