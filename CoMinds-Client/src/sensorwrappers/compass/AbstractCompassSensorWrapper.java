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
import logging.Logger;

/**
 * This is a wrapper class for all classes that can get data from a compass
 * sensor. It can be used to wrap a remote device or an own compass sensor.
 * 
 * @author Annabelle Klarl
 */
public interface AbstractCompassSensorWrapper {

	public static final Logger logger = Logger.getLogger();

	/**
	 * gets the degree from a compass sensor (either remote or home sensor)
	 * 
	 * @return the degree
	 * @throws SensorNoiseException
	 *             thrown if no data can be fetched from the sensor
	 */
	public abstract float getDegree() throws SensorException;

}
