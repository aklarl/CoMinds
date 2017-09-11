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

import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.ColorSensor.Color;
import robots.abstractRobots.AbstractController;
import sensorwrappers.color.OwnColorSensorWrapper;

import communication.BTComm;
import communication.exceptions.ManagerException;

import datasender.AbstractSender;
import datasender.LightValueSender;

/**
 * This class models an object without a motor that should be pushed along a
 * line. It connects to different remote devices. It then reads the light value
 * underneath its color sensor and converts it with a light value scaler.
 * Afterwards it either sends the data to the connected devices or waits until a
 * device polls for the data.
 * 
 * @author Annabelle Klarl
 */
public class LineReader extends AbstractController {

	public static final int SEND_INTERVAL = 90;
	public static final SensorPort COLOR_LIGHT_SENSOR = SensorPort.S2;
	private final ColorSensor light;
	private OwnColorSensorWrapper lightWrapper;

	/**
	 * Constructor which initializes the color light sensor (it connects
	 * directly)
	 * 
	 * @param timeout
	 *            the timeout for an inbound connection (if -1 no inbound
	 *            connection will be initialized)
	 * @param nxtNames
	 *            the names of the nxts to connect to
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections
	 */
	public LineReader(int timeout, String... nxtNames) throws ManagerException {
		super(timeout, nxtNames);

		// initialize own compass sensor
		this.light = new ColorSensor(COLOR_LIGHT_SENSOR, Color.BLUE);
	}

	/**
	 * Constructor which initializes the color light sensor
	 * 
	 * @param directConnection
	 *            whether to connect directly or by managed connections
	 * @param nxtNames
	 *            the names of the nxts to connect to
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public LineReader(boolean directConnection, String... nxtNames)
			throws ManagerException {
		super(directConnection, nxtNames);

		// initialize own compass sensor
		this.light = new ColorSensor(COLOR_LIGHT_SENSOR, Color.BLUE);
	}

	/**
	 * sends the light value if the light value was requested
	 */
	@Override
	protected void initResponse() {
		this.lightWrapper = OwnColorSensorWrapper.initLightWrapper(this.light,
				this.monitor);

		if (this.inboundComm != null) {
			this.inboundComm.setOwnLightValueGetter(this.lightWrapper);
		}

		for (BTComm outbound : this.outboundComm) {
			if (outbound != null) {
				outbound.setOwnLightValueGetter(this.lightWrapper);
			}
		}
	}

	/**
	 * nothing is send via bt connection by the controller itself
	 */
	@Override
	protected AbstractSender[] initSending(int sendInterval, String... btcomms) {
		final LightValueSender[] senders = new LightValueSender[btcomms.length];

		for (int i = 0; i < btcomms.length; i++) {
			for (BTComm outboundComm : this.outboundComm) {
				if (outboundComm != null
						&& outboundComm.getRemoteName().equals(btcomms[i])) {
					LightValueSender sender = new LightValueSender(
							outboundComm, this.lightWrapper, sendInterval);
					senders[i] = sender;
					sender.start();
				}
			}
		}
		return senders;
	}

	@Override
	protected void stop() {
		this.light.setFloodlight(false);
		super.stop();
	}

	public static void main(String[] args) {
		try {
			// main(new LineReader(true, "Josy", "Johnny"), SEND_INTERVAL,
			// "Josy", "Johnny");
			main(new LineReader(false, "Josy", "Johnny"), SEND_INTERVAL,
					"Josy", "Johnny");
		}
		catch (ManagerException e) {
			// should not happen here because only the AbstractController inits
			// a NXTConnectionManager
		}
	}

}
