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

import robots.abstractRobots.AbstractCar;
import robots.abstractRobots.properties.BotProperties;
import robots.linefollower.pid.PIDProperties;
import robots.linefollower.pid.TrackyPIDProperties;
import sensorwrappers.color.RemoteColorSensorWrapper;
import behaviourmodel.StoppableBehavior;
import behaviourmodel.linefollower.FindLineBehavior;
import behaviourmodel.linefollower.PIDBehavior;

import communication.NXTConnectionManager;

/**
 * This is a super class for a robot that is connected to an object on the side.
 * It therefore can move this object by simple driving. The robot shall move the
 * object along a line. Therefore the robot polls for the light values from the
 * object and drives according to this light value. For the robot you must set
 * on which side of the object the robot is and how big the distance between the
 * middle of the robot and the middle of the object is.
 * 
 * @author Annabelle Klarl
 */
public class Pusher extends AbstractCar {

	private final RemoteColorSensorWrapper remoteLight;
	private final float distance;
	private final BotProperties properties;
	private final PIDProperties pidProperties;

	/**
	 * Constructor that initializes a remote light value getter and a car with
	 * the parameters of a shooter bot
	 * 
	 * @param light
	 *            the remote light value getter with which to get the light
	 *            value from the remote device
	 * @param distance
	 *            the distance between the middle of the robot and the middle of
	 *            the object
	 * @param reverse
	 *            whether the driving direction should be reversed
	 */
	public Pusher(RemoteColorSensorWrapper light, float distance,
			boolean reverse) {
		this(new TrackyPIDProperties(), new TrackyPIDProperties(), light,
				distance, reverse);
	}

	/**
	 * Constructor that initializes a remote light value getter and a car with
	 * the travelspeed, rotatespeed and acceleration
	 * 
	 * @param properties
	 *            the properties of this car
	 * @param pidProperties
	 *            the properties for the pid controller of this car (the speed
	 *            will be overwritten by this one)
	 * @param light
	 *            the remote light value getter with which to get the light
	 *            value from the remote device
	 * @param distance
	 *            the distance between the middle of the robot and the middle of
	 *            the object
	 * @param reverse
	 *            whether the driving direction should be reversed
	 */
	public Pusher(BotProperties properties, PIDProperties pidProperties,
			RemoteColorSensorWrapper light, float distance, boolean reverse) {
		super(properties, reverse);
		this.remoteLight = light;
		this.distance = distance;
		this.properties = properties;
		this.pidProperties = pidProperties;
		this.pilot.setTravelSpeed(this.pidProperties.getTravelSpeed());
	}

	/**
	 * just initializes a PID-behavior where the light value is get from a
	 * remote device
	 */
	@Override
	protected StoppableBehavior[] initializeBehaviors() {

		final PIDBehavior pid = new PIDBehavior(this.pilot, this.properties
				.getTrackWidth(), this.remoteLight, this.pidProperties
				.getNormalizedLineLightValue(), this.pidProperties.getKP(),
				this.pidProperties.getKI(), this.pidProperties.getKD(),
				this.distance);
		final FindLineBehavior findLine = new FindLineBehavior(this.pilot,
				this.remoteLight, this.pidProperties
						.getNormalizedLineLightValue(), this.pidProperties
						.getNormalizedOfflineLightValue(), this.pidProperties
						.getSweep(), this.pidProperties.getTravelDistance(),
				this.distance);

		StoppableBehavior[] behaviors = { pid, findLine };
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
			System.out.println("Pusher-Exc " + e.getClass());
		}

		super.stop();
	}

}
