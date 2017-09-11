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
package datasender;

import sensorwrappers.compass.AbstractCompassSensorWrapper;
import sensorwrappers.exceptions.SensorException;

import common.exceptions.QueueBlockedException;
import communication.BTComm;
import communication.exceptions.ConnectionClosedException;

/**
 * This threads send continously the compass data of the private
 * CompassController via the given BTComm until it is stopped.
 * 
 * @author Annabelle Klarl
 */
public class DegreeSender extends AbstractSender {

	private AbstractCompassSensorWrapper ownCompassController;

	/**
	 * Constructor
	 * 
	 * @param btcomm
	 *            bluetooth connection via which to send the compass data
	 * @param ownCompassController
	 *            the compass controller where to get the compass data from
	 * @param sendInterval
	 *            how fast the data will be send
	 */
	public DegreeSender(BTComm btcomm,
			AbstractCompassSensorWrapper ownCompassController, int sendInterval) {
		super(btcomm, sendInterval);
		this.ownCompassController = ownCompassController;
	}

	@Override
	protected void doAction() {
		try {
			float degree = this.ownCompassController.getDegree();
			this.btcomm.writeDegree(degree);
		}
		catch (SensorException e) {
			logger.error("SensorException: " + e.getMessage());
			Thread.yield();
		}
		catch (QueueBlockedException e) {
			System.out.println("DegreeSender:queue locked");
			logger.info("DegreeSender: queue blocked");
			Thread.yield();
		}
		catch (ConnectionClosedException e) {
			System.out.println("DegreeSender:remote close");
			logger.info("DegreeSender: conn closed from remote");
		}

	}
}
