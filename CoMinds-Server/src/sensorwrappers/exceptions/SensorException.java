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
package sensorwrappers.exceptions;

/**
 * This exception is thrown if there is no data available at the sensor to be
 * read. That might be if e.g. a compass sensor returns -1. Or if a remote
 * sensor did not answer. Or if a ColorLightSensor returns light values smaller
 * than 0.0 or bigger than 100.0
 * 
 * @author Annabelle Klarl
 */
@SuppressWarnings("serial")
public class SensorException extends Exception {

	/**
	 * Constructor
	 * 
	 * @param message
	 *            a String what was wrong with the sensor
	 */
	public SensorException(String message) {
		super(message);
	}
}
