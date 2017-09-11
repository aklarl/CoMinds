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
package test;
import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CompassSensor;
import logging.Logger;
import logging.Logger.LogLevel;
import sensorwrappers.compass.OwnCompassSensorWrapper;

import communication.BTComm;
import communication.BTCommObserver;
import communication.NXTConnectionManager;
import communication.exceptions.NoManagedConnectionsException;
import datasender.DegreeSender;

public class NXT3 extends BTCommObserver {

	public static final Logger logger = Logger.getLogger("Johnny.txt",
			LogLevel.DEBUG, 500, true);

	public static void main(String[] args) {
		try {

			OwnCompassSensorWrapper ownCompass = new OwnCompassSensorWrapper(
					new CompassSensor(SensorPort.S3));

			DegreeSender sender = null;
			DegreeSender sender2 = null;

			// NXTConnectionManager manager = NXTConnectionManager.getManager();
			// try {
			// BTComm btcomm = manager.getDirectConnection(0, 1, true);
			// sender = new DegreeSender(btcomm, ownCompass, 50);
			// sender.start();
			// }
			// catch (Throwable e) {
			// System.out.println("Exc" + e.getClass());
			// }
			// try {
			// BTComm btcomm2 = manager.getDirectConnection("Jamy", 1, true);
			// sender2 = new DegreeSender(btcomm2, ownCompass, 50);
			// sender2.start();
			// }
			// catch (Throwable e) {
			// System.out.println("Exc" + e.getClass());
			// }

			// ///////////////////////////////

			NXTConnectionManager manager = NXTConnectionManager.getManager(500);

			try {
				// BTComm btcomm = manager.getDirectConnection(0);
				BTComm btcomm = manager.getManagedConnection(0, "Josy", true);
				sender = new DegreeSender(btcomm, ownCompass, 50);
				sender.start();
			}
			catch (Throwable e) {
				System.out.println("Exc" + e.getClass());
			}
			try {
				BTComm btcomm2 = manager.getManagedConnection("Jamy", true);
				sender2 = new DegreeSender(btcomm2, ownCompass, 50);
				sender2.start();
			}
			catch (NoManagedConnectionsException e) {
			}
			catch (Throwable e) {
				System.out.println("MainExc" + e.getClass());
			}

			Button.waitForPress();
			logger.debug("ESCAPE");
			System.out.println("ESCAPE");

			sender.stop();
			sender2.stop();

			System.out.println("senderstop");
			NXTConnectionManager.closeManager();

			System.out.println("stopping");
			logger.stopLogging();
		}
		catch (Throwable e) {
			System.out.println("Main" + e.getClass());
		}
	}

}
