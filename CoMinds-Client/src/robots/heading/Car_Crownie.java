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
package robots.heading;

import logging.Logger;
import logging.Logger.LogLevel;
import robots.abstractRobots.properties.BotProperties;
import sensorwrappers.compass.RemoteCompassSensorWrapper;

import communication.exceptions.ManagerException;

/**
 * This is a compass car that waits for any inbound connection to get the degree
 * from the remote device. It does NOT poll for the compass data, but simply
 * waits until the remote device sends some data.
 * 
 * @author Annabelle Klarl
 */
public class Car_Crownie extends AbstractCompassCar<RemoteCompassSensorWrapper> {

	// it is needed for correct log-file creation
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("log_crownie.txt",
			LogLevel.DEBUG, 500, true);

	public Car_Crownie(RemoteCompassSensorWrapper compass,
			BotProperties properties) {
		super(compass, true, properties);
	}

	public static void main(String[] args) {
		try {
			// main(new Car_Crownie(new RemoteCompassWrapper(0),
			// new TrackyProperties()));
			main(new Car_Crownie(new RemoteCompassSensorWrapper(false,
					"Johnny", 0), new HeadingTrackyProperties()));
		}
		catch (ManagerException e) {
			// should not happen here because only the RemoteCompassWrapper
			// inits a NXTConnectionManager
		}
	}

}
