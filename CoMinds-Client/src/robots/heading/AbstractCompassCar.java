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

import lejos.nxt.SensorPort;
import lejos.nxt.addon.CompassSensor;
import robots.abstractRobots.AbstractCar;
import robots.abstractRobots.properties.BotProperties;
import robots.abstractRobots.properties.TrackyProperties;
import sensorwrappers.compass.AbstractCompassSensorWrapper;
import sensorwrappers.compass.OwnCompassSensorWrapper;
import sensorwrappers.compass.RemoteCompassSensorWrapper;
import behaviourmodel.StoppableBehavior;
import behaviourmodel.headingfollower.FollowCompassBehavior;

import communication.NXTConnectionManager;

/**
 * This is an abstract implementation of a car that has its own compass sensor
 * and connects to a remote nxt that also has a compass sensor. It will then
 * receive the compass data from the remote device via Bluetooth. It will try to
 * rotate itself so far that its own compass sensor and the remote compass
 * sensor will read the same compass data. All subclasses shall use the method
 * {@link AbstractCar#main(AbstractCar)} to instantiate a specific car with a
 * specific remote compass controller and to start the behavior of a car.
 * 
 * @author Annabelle Klarl
 */
public abstract class AbstractCompassCar<E extends RemoteCompassSensorWrapper>
		extends AbstractCar {

	public static final SensorPort COMPASS_SENSOR = SensorPort.S3;

	protected final AbstractCompassSensorWrapper ownCompassController;
	protected final E remoteCompassController;
	protected final boolean rotate;

	/**
	 * Constructor that only initializes the two compass sensors and the pilot
	 * (it will use the shooter bot properties)
	 * 
	 * @param remoteCompassController
	 *            the remote compass controller to use
	 * @param rotate
	 *            whether this robot shall rotate (otherwise it will arcForward)
	 */
	public AbstractCompassCar(E remoteCompassController, boolean rotate) {
		this(remoteCompassController, rotate, new TrackyProperties());
	}

	/**
	 * Constructor that only initializes the two compass sensors and the pilot
	 * according to the BotProperties
	 * 
	 * @param remoteCompassController
	 *            the remote compass controller to use
	 * @param rotate
	 *            whether this robot shall rotate (otherwise it will arcForward)
	 * @param properties
	 *            the properties of this car
	 */
	public AbstractCompassCar(E remoteCompassController, boolean rotate,
			BotProperties properties) {
		super(properties);

		// initialize own compass sensor
		this.ownCompassController = new OwnCompassSensorWrapper(
				new CompassSensor(COMPASS_SENSOR));

		// initialize remote compass sensor
		this.remoteCompassController = remoteCompassController;

		this.rotate = rotate;
	}

	/**
	 * initializes the behavior that the car rotates to get the same compass
	 * data from the own and the remote compass controller
	 * 
	 * @return the behaviors for this car
	 */
	@Override
	protected StoppableBehavior[] initializeBehaviors() {
		StoppableBehavior[] behaviors = {
				// pseudo behavior so that the arbitrator may not terminate
				// without pressing stop
				new StoppableBehavior("yield") {
					@Override
					public boolean actionStopCondition() {
						return false;
					}

					@Override
					public void doAction() {
						Thread.yield();
					}

					@Override
					public boolean specialTakeControl() {
						return true;
					}

					@Override
					public void stopAction() {
					}
				},
				new FollowCompassBehavior(this.pilot,
						this.ownCompassController,
						this.remoteCompassController, this.rotate) };
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
			System.out.println("AbstractCompasscar-Exc " + e.getClass());
		}

		super.stop();
	}

}
