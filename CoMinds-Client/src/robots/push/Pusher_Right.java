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
package robots.push;

import logging.Logger;
import logging.Logger.LogLevel;
import sensorwrappers.compass.PollingRemoteCompassSensorWrapper;

import communication.exceptions.ManagerException;

public class Pusher_Right extends Pusher {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("log_right.txt",
			LogLevel.DEBUG, 500, true);

	public Pusher_Right(PollingRemoteCompassSensorWrapper remoteCompassController,
			boolean leftSide) {
		super(remoteCompassController, leftSide);
	}

	public static void main(String[] args) {
		try {
			main(new Pusher_Right(new PollingRemoteCompassSensorWrapper(0), false));
		}
		catch (ManagerException e) {
			// should not happen here because only the
			// PollingRemoteCompassWrapper inits a NXTConnectionManager
		}
	}

}
