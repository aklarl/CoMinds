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
package robots.heading.pidController;

import robots.abstractRobots.properties.BotProperties;
import robots.heading.simpleController.CompassControl;
import robots.linefollower.pid.PIDLineFollower;
import robots.linefollower.pid.PIDProperties;
import robots.linefollower.pid.TrackyPIDProperties;

import communication.exceptions.ManagerException;

/**
 * This class is written for a controller. It only needs the brick and a compass
 * sensor. This programm will connect to one inbound connection and will wait
 * for any request from the remote device. It will also connect to the nxt
 * Crownie and will send its compass data to it until the connection is closed
 * or ESCAPE is pressed. The whole controller will stop after pressing ESCAPE.
 * 
 * @author Annabelle Klarl
 */
public class PIDCompass extends CompassControl {

	public static final int SEND_INTERVAL = 500;

	private final PIDLineFollower pid;

	/**
	 * Constructor which initializes the compass sensor (only direct
	 * connections)
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
	public PIDCompass(int timeout, String... nxtNames) throws ManagerException {
		this(new TrackyPIDProperties(), new TrackyPIDProperties(), timeout,
				nxtNames);
	}

	/**
	 * Constructor which initializes the compass sensor (only direct
	 * connections)
	 * 
	 * @param properties
	 *            the properties for the Car
	 * @param pidProperties
	 *            the properties for the PID-Controller
	 * @param timeout
	 *            the timeout for an inbound connection (if -1 no inbound
	 *            connection will be initialized)
	 * @param nxtNames
	 *            the names of the nxts to connect to
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections
	 */
	public PIDCompass(BotProperties properties, PIDProperties pidProperties,
			int timeout, String... nxtNames) throws ManagerException {
		super(timeout, nxtNames);
		this.pid = new PIDLineFollower(properties, pidProperties);
	}

	/**
	 * Constructor which initializes the compass sensor
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
	public PIDCompass(boolean directConnection, String... nxtNames)
			throws ManagerException {
		this(new TrackyPIDProperties(), new TrackyPIDProperties(),
				directConnection, nxtNames);
	}

	/**
	 * Constructor which initializes the compass sensor
	 * 
	 * @param properties
	 *            the properties for the Car
	 * @param pidProperties
	 *            the properties for the PID-Controller
	 * @param directConnection
	 *            whether to connect directly or by managed connections
	 * @param nxtNames
	 *            the names of the nxts to connect to
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public PIDCompass(BotProperties properties, PIDProperties pidProperties,
			boolean directConnection, String... nxtNames)
			throws ManagerException {
		super(directConnection, nxtNames);
		this.pid = new PIDLineFollower(properties, pidProperties);
	}

	/**
	 * starts the line following behavior
	 */
	@Override
	protected void startBehavior() {
		this.pid.startBehavior();
	}

	/**
	 * stops any bluetooth communication and the logging
	 */
	@Override
	protected void stop() {
		this.pid.setFloodlight(false);
		super.stop();
	}

	public static void main(String[] args) {
		try {
			// TrackyPIDProperties props = new TrackyPIDProperties();
			HeadingTrikePIDProperties props = new HeadingTrikePIDProperties();
			main(new PIDCompass(props, props, false, "Crownie", "Josy"),
					SEND_INTERVAL, "Josy", "Crownie");
			// main(
			// new PIDCompass(props, props, false, "Jenny", "Crownie",
			// "Josy"), SEND_INTERVAL, "Josy", "Crownie");
		}
		catch (ManagerException e) {
			// should not happen here because only the AbstractController inits
			// a NXTConnectionManager
		}
	}
}
