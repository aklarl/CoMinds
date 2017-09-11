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
package robots.linefollower.distributed;

import logging.Logger;
import logging.Logger.LogLevel;
import robots.abstractRobots.properties.BotProperties;
import robots.linefollower.pid.PIDProperties;
import robots.linefollower.pid.TrackyPIDProperties;
import sensorwrappers.color.RemoteColorSensorWrapper;

import communication.exceptions.ManagerException;

/**
 * This robot is connected to an object on the RIGHT side and will move this
 * object along a line. -> distance must be negative
 * 
 * @author Annabelle Klarl
 */
public class Pusher_John extends Pusher {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("log_right.txt",
			LogLevel.DEBUG, 500, true);

	private static final float distance = 22.3f;

	/**
	 * Constructor that initializes a remote light value getter and a car with
	 * the parameters of a shooter bot
	 * 
	 * @param light
	 *            the remote light value getter with which to get the light
	 *            value from the remote device
	 * @param distance
	 *            the distance between the middle of the robot and the middle of
	 *            the object
	 * @param reverse
	 *            whether the driving direction should be reversed
	 */
	public Pusher_John(RemoteColorSensorWrapper light, float distance,
			boolean reverse) {
		this(new TrackyPIDProperties(), new TrackyPIDProperties(), light,
				distance, reverse);
	}

	/**
	 * Constructor that initializes a remote light value getter and a car with
	 * the given bot properties
	 * 
	 * @param properties
	 *            the properties of this car
	 * @param pidProperties
	 *            the properties for the pid controller of this car
	 * @param light
	 *            the remote light value getter with which to get the light
	 *            value from the remote device
	 * @param distance
	 *            the distance between the middle of the robot and the middle of
	 *            the object
	 * @param reverse
	 *            whether the driving direction should be reversed
	 */
	public Pusher_John(BotProperties properties, PIDProperties pidProperties,
			RemoteColorSensorWrapper light, float distance, boolean reverse) {
		super(properties, pidProperties, light, distance, reverse);
	}

	public static void main(String[] args) {
		try {
			RemoteTrikePIDProperties props = new RemoteTrikePIDProperties();
			// props for direct connection not good
			// main(new Pusher_John(props, props, new RemoteLightWrapper(true,
			// "Jamy", 0), distance, true));
			main(new Pusher_John(props, props, new RemoteColorSensorWrapper(
					false, "Jamy", 0), distance, true));
		}
		catch (ManagerException e) {
			// should not happen here because only the RemoteLightWrapper inits
			// a NXTConnectionManager
		}
	}

}
