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

import sensorwrappers.color.AbstractColorSensorWrapper;
import sensorwrappers.exceptions.SensorException;

import common.exceptions.QueueBlockedException;
import communication.BTComm;
import communication.exceptions.ConnectionClosedException;

/**
 * This threads sends continously the light value of the private
 * LightValueGetter via the given BTComm until it is stopped.
 * 
 * @author Annabelle Klarl
 */
public class LightValueSender extends AbstractSender {

	private AbstractColorSensorWrapper ownLightValueGetter;

	/**
	 * Constructor
	 * 
	 * @param btcomm
	 *            bluetooth connection via which to send the light value
	 * @param ownLightValueGetter
	 *            the light value getter where to get the light value from
	 * @param sendInterval
	 *            how fast the data will be send
	 */
	public LightValueSender(BTComm btcomm,
			AbstractColorSensorWrapper ownLightValueGetter, int sendInterval) {
		super(btcomm, sendInterval);
		this.ownLightValueGetter = ownLightValueGetter;
	}

	@Override
	protected void doAction() {
		try {
			float lightValue = this.ownLightValueGetter.getLightValue();
			this.btcomm.writeLightValue(lightValue);
		}
		catch (SensorException e) {
			logger.error("SensorException: " + e.getMessage());
			Thread.yield();
		}
		catch (QueueBlockedException e) {
			System.out.println("LightSender:queue locked");
			logger.info("LightSender: queue blocked");
			Thread.yield();
		}
		catch (ConnectionClosedException e) {
			System.out.println("LightSender:remote close");
			logger.info("LightSender: conn closed from remote");
		}

	}

}
