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
import sensorwrappers.compass.PollingRemoteCompassSensorWrapper;

import communication.exceptions.ManagerException;

/**
 * This is a compass car that connects to a specific nxt by name to get the
 * degree from the remote device. It DOES request to get the compass data!
 * 
 * @author Annabelle Klarl
 */
public class Car_Jenny extends
		AbstractCompassCar<PollingRemoteCompassSensorWrapper> {

	// it is needed for correct log-file creation
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("log_jenny.txt",
			LogLevel.DEBUG, 500, true);

	public Car_Jenny(PollingRemoteCompassSensorWrapper compass,
			BotProperties properties) {
		super(compass, true, properties);
	}

	public static void main(String[] args) {
		try {
			// main(new Car_Jenny(new PollingRemoteCompassWrapper(true,
			// "Johnny"),
			// new TrackyProperties()));
			main(new Car_Jenny(new PollingRemoteCompassSensorWrapper(false,
					"Johnny", 0), new HeadingTrackyProperties()));
		}
		catch (ManagerException e) {
			// should not happen here because only the RemoteCompassWrapper
			// inits a NXTConnectionManager
		}
	}

}