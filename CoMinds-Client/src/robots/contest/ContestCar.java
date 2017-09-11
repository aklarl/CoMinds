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
package robots.contest;

import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.ColorSensor.Color;
import logging.Logger;
import logging.Logger.LogLevel;
import robots.abstractRobots.properties.BotProperties;
import robots.abstractRobots.properties.TrackyProperties;
import robots.abstractRobots.properties.TrikeProperties;
import robots.heading.AbstractCompassCar;
import sensorwrappers.color.OwnColorSensorWrapper;
import sensorwrappers.compass.RemoteCompassSensorWrapper;
import behaviourmodel.StoppableBehavior;
import behaviourmodel.contest.SteerCompassBehavior;
import behaviourmodel.contest.WaitIfBlackBehavior;

import communication.NXTConnectionManager;
import communication.exceptions.ManagerException;

/**
 * This models a car that connects to a remote device, waits for compass data
 * and arcs to get the same compass data itself.
 * 
 * @author Annabelle Klarl
 */
public class ContestCar extends AbstractCompassCar<RemoteCompassSensorWrapper> {

	// it is needed for correct log-file creation
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("contest_car.txt",
			LogLevel.INFO, 500, true);

	protected static final SensorPort COLOR_LIGHT_SENSOR = SensorPort.S2;
	private final ColorSensor light;

	public static final float BLACK = 10f;
	public static final long WAIT_INTERVAL = 1000;
	public static final long AFTER_WAIT_INTERVAL = 1000;

	/**
	 * Constructor that only initializes the two compass sensors and the pilot
	 * (it will use the shooter bot properties), also the light sensor will be
	 * initialized
	 * 
	 * @param remoteCompassController
	 *            the remote compass controller to use
	 */
	public ContestCar(RemoteCompassSensorWrapper remoteCompassController) {
		this(remoteCompassController, new TrackyProperties());
	}

	/**
	 * Constructor that only initializes the two compass sensors and the pilot
	 * according to the BotProperties, also the light sensor will be initialized
	 * 
	 * @param remoteCompassController
	 *            the remote compass controller to use
	 * @param properties
	 *            the properties of this car
	 */
	public ContestCar(RemoteCompassSensorWrapper remoteCompassController,
			BotProperties properties) {
		super(remoteCompassController, false, properties);
		this.light = new ColorSensor(COLOR_LIGHT_SENSOR, Color.BLUE);
	}

	/**
	 * initializes the behavior that the car arcs forward to get the same
	 * compass data from the own and the remote compass controller. It will stop
	 * for {@link #WAIT_INTERVAL} milli seconds if the floor underneath the
	 * color sensor is black.
	 * 
	 * @return the behaviors for this car
	 */
	@Override
	protected StoppableBehavior[] initializeBehaviors() {
		OwnColorSensorWrapper lightWrapper = OwnColorSensorWrapper
				.initLightWrapper(this.light, this.monitor);

		StoppableBehavior[] behaviors = {
				new SteerCompassBehavior(this.pilot, this.ownCompassController,
						this.remoteCompassController),
				new WaitIfBlackBehavior(lightWrapper, BLACK, WAIT_INTERVAL,
						AFTER_WAIT_INTERVAL) };

		return behaviors;
	}

	/**
	 * stops the car. It therefore stops the connection to the remote device and
	 * the logging
	 */
	@Override
	public void stop() {
		try {
			NXTConnectionManager.closeManager();
		}
		catch (Throwable e) {
			System.out.println("ContestCar-Exc " + e.getClass());
		}

		super.stop();
	}

	public static void main(String[] args) {
		try {
			main(new ContestCar(new RemoteCompassSensorWrapper(0),
					new TrikeProperties()));
		}
		catch (ManagerException e) {
			// should not happen here because only the
			// RemoteCompassWrapper inits a NXTConnectionManager
		}
	}
}
