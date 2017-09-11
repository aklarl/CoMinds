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
import lejos.robotics.navigation.DifferentialPilot;
import sensorwrappers.compass.AbstractCompassSensorWrapper;
import sensorwrappers.compass.CompassSensorUtils;
import sensorwrappers.compass.RemoteCompassSensorWrapper;
import sensorwrappers.exceptions.RemoteSensorException;
import sensorwrappers.exceptions.SensorException;


/**
 * This is a behavior where a car pushes an object according to a specific
 * heading. The heading is given as {@link #referenceDegree}. The car will push
 * more or less according to the current compass data of the object. The plus in
 * the speed will be computed with a PID algorithm.
 * 
 * @author Annabelle Klarl
 */
public class PushBehavior extends StoppableBehavior {

	public static final float ALLOWED_DIFF_BETWEEN_DEGREES = 1.0f;

	private final DifferentialPilot pilot;
	private float normalTravelSpeed;
	private final AbstractCompassSensorWrapper ownCompassController;
	private final RemoteCompassSensorWrapper remoteCompassController;
	private float referenceDegree;
	private boolean leftSide;

	private final float kp;
	private final float ki;
	private final float kd;

	// private variables for holding the remote degrees if there could not be
	// read anything from the remotes
	private float remoteDegrees = -1;

	private float currentOwnError = 0.0f;
	private float currentObjectError = 0.0f;
	private float integral = 0.0f;
	private float derivative = 0.0f;
	private float lastError = this.currentObjectError;
	private float lastTravelSpeed = 0.0f;

	/**
	 * @param pilot
	 * @param ownCompassController
	 * @param remoteCompassController
	 * @param referenceDegree
	 * @param leftSide
	 * @param kp
	 *            the factor for multiplying the proportional
	 * @param ki
	 *            the factor for multiplying the integrative
	 * @param kd
	 *            the factor for multiplying the derivative
	 */
	public PushBehavior(DifferentialPilot pilot,
			AbstractCompassSensorWrapper ownCompassController,
			RemoteCompassSensorWrapper remoteCompassController,
			float referenceDegree, boolean leftSide, float kp, float ki,
			float kd) {
		super("push");
		this.pilot = pilot;
		this.normalTravelSpeed = this.pilot.getTravelSpeed();

		this.ownCompassController = ownCompassController;
		this.remoteCompassController = remoteCompassController;
		this.referenceDegree = referenceDegree;
		this.leftSide = leftSide;

		this.kp = kp;
		this.ki = ki;
		this.kd = kd;

		logger.debug("push ref degree: " + this.referenceDegree);
		logger.debug("push speed: " + this.pilot.getTravelSpeed());
	}

	/**
	 * always wants to take control
	 */
	@Override
	public boolean specialTakeControl() {
		return true;
	}

	/**
	 * never wants to stop
	 */
	@Override
	public boolean actionStopCondition() {
		return false;
	}

	/**
	 * does the specified behavior (pushes an object where the speed is computed
	 * with a PID algorithm)
	 */
	@Override
	public void doAction() {
		// //////////////////////////////////
		// turn yourself ////////////////////
		// //////////////////////////////////
		try {
			float ownDegree = this.ownCompassController.getDegree();
			this.currentOwnError = CompassSensorUtils.getSmallestDiff(
					this.referenceDegree, ownDegree);
		}
		catch (SensorException e) {
			logger.debug("push: No data from own compass");
		}
		logger.debug("push: steer angle " + this.currentOwnError);
		this.pilot.steer(this.currentOwnError);

		try {
			this.remoteDegrees = this.remoteCompassController.getDegree();
		}
		catch (RemoteSensorException e) {
			// if there couldn't be retrieved any data from the remote device
			logger.debug("push: " + e.getMessage());
			Thread.yield();
			return;
		}
		catch (SensorException e) {
			// if sensor noise is happening before any degrees were read
			if (this.remoteDegrees < 0) {
				logger.debug("push: noise");
				Thread.yield();
				return;
			}
		}

		// //////////////////////////////////
		// turn object //////////////////////
		// //////////////////////////////////
		this.currentObjectError = CompassSensorUtils.absTo180(CompassSensorUtils
				.absTo180(this.referenceDegree)
				- CompassSensorUtils.absTo180(this.remoteDegrees));

		// if the object is on course
		if (Math.abs(this.currentObjectError) <= ALLOWED_DIFF_BETWEEN_DEGREES) {
			this.currentOwnError = 0.0f;
			this.currentObjectError = 0.0f;
			this.integral = 0.0f;
			this.derivative = 0.0f;
			this.lastError = this.currentObjectError;

			logger.debug("push: normal speed");
			this.pilot.setTravelSpeed(this.normalTravelSpeed);

			return;
		}
		// PID behavior for pushing the object (drive slower or faster)
		else {

			this.integral = this.integral + this.currentObjectError;
			this.derivative = this.currentObjectError - this.lastError;
			this.lastError = this.currentObjectError;

			float travelSpeedPlus = this.kp * this.currentObjectError + this.ki
					* this.integral + this.kd * this.derivative;

			float travelSpeed = 0.0f;
			this.lastTravelSpeed = this.pilot.getTravelSpeed();

			if (this.leftSide) {
				travelSpeed = this.normalTravelSpeed + travelSpeedPlus;
			}
			else {
				travelSpeed = this.normalTravelSpeed - travelSpeedPlus;
			}

			if (travelSpeed < 0) {
				logger.debug("speed < 0: " + travelSpeed);
				travelSpeed = this.lastTravelSpeed;
			}
			else if (travelSpeed > 200) {
				logger.debug("speed > 200: " + travelSpeed);
				travelSpeed = this.lastTravelSpeed;
			}
			else {
				logger.debug("speed: " + travelSpeed);
			}

			this.pilot.setTravelSpeed(travelSpeed);
		}
	}

	@Override
	public void stopAction() {
		this.currentOwnError = 0.0f;
		this.currentObjectError = 0.0f;
		this.integral = 0.0f;
		this.derivative = 0.0f;
		this.lastError = this.currentObjectError;

		this.pilot.stop();
	}

}
