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

import behaviourmodel.StoppableBehavior;
import robots.abstractRobots.properties.TrackyProperties;
import robots.heading.AbstractCompassCar;
import sensorwrappers.compass.PollingRemoteCompassSensorWrapper;
import sensorwrappers.exceptions.SensorException;


/**
 * This is a super class for a car that pushes an object according to a specific
 * heading.
 * 
 * @author Annabelle Klarl
 */
public class Pusher extends AbstractCompassCar<PollingRemoteCompassSensorWrapper> {

	public static final float KP = 0.1f;
	public static final float KI = 0.005f;
	public static final float KD = 0.2f;

	private final boolean leftSide;

	/**
	 * initializes a car and a remote compass controller (this will take the
	 * TrackyProperties, but will set travel speed to 10 and acceleration to
	 * 6000)
	 * 
	 * @param remoteCompassController
	 *            a compass controller that polls for compass data
	 * @param leftSide
	 *            whether this car pushes at the left side
	 */
	public Pusher(PollingRemoteCompassSensorWrapper remoteCompassController,
			boolean leftSide) {
		super(remoteCompassController, true, new TrackyProperties() {
			@Override
			public float getTravelSpeed() {
				return 10.0f;
			}

			@Override
			public int getAcceleration() {
				return 6000;
			}
		});

		this.leftSide = leftSide;
	}

	/**
	 * inits the push behavior
	 */
	@Override
	protected StoppableBehavior[] initializeBehaviors() {

		float referenceDegree = -1;
		logger.debug("get degree from remote");
		System.out.println("get degree");
		while (true) {
			try {
				referenceDegree = this.remoteCompassController.getDegree();
			}
			catch (SensorException e) {
				continue;
			}
			break;
		}

		StoppableBehavior[] behaviors = { new PushBehavior(this.pilot,
				this.ownCompassController, this.remoteCompassController,
				referenceDegree, this.leftSide, KP, KI, KD) };
		return behaviors;
	}
}
