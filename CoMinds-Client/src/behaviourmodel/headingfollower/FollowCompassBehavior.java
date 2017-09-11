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
package behaviourmodel.headingfollower;

import behaviourmodel.StoppableBehavior;
import lejos.robotics.navigation.DifferentialPilot;
import sensorwrappers.compass.AbstractCompassSensorWrapper;
import sensorwrappers.compass.CompassSensorUtils;
import sensorwrappers.exceptions.RemoteSensorException;
import sensorwrappers.exceptions.SensorException;


/**
 * This behavior needs two CompassController from which it can read compass
 * data. It will try to rotate so far that the data read from the ownCompass and
 * the data read from the remoteCompass will be the same besides an epsilon of
 * {@link FollowCompassBehavior#ALLOWED_DIFF_BETWEEN_DEGREES}. No PID-Controller
 * is used because a simple rotation about the difference between the two read
 * compass data is sufficient.
 * 
 * @author Annabelle Klarl
 */
public class FollowCompassBehavior extends StoppableBehavior {

	public static final float ALLOWED_DIFF_BETWEEN_DEGREES = 1.0f;

	private final DifferentialPilot pilot;
	private final AbstractCompassSensorWrapper ownCompass;
	private final AbstractCompassSensorWrapper remoteCompass;

	private final boolean rotate;

	// private variables so that isInDirection and other calls for degrees will
	// get the same values
	private float ownDegrees = -1;
	private float remoteDegrees = -1;

	/**
	 * Constructor
	 * 
	 * @param pilot
	 *            the pilot which is used to rotate the robot
	 * @param ownCompass
	 *            the controller for the own compass
	 * @param remoteCompass
	 *            the controller for the remote compass
	 * @param rotate
	 *            whether this robot shall rotate (otherwise it will arcForward)
	 */
	public FollowCompassBehavior(DifferentialPilot pilot,
			AbstractCompassSensorWrapper ownCompass,
			AbstractCompassSensorWrapper remoteCompass, boolean rotate) {
		super("follow compass");
		this.ownCompass = ownCompass;
		this.pilot = pilot;
		this.remoteCompass = remoteCompass;
		this.rotate = rotate;
	}

	/**
	 * returns whether the data read from the own compass and the remote compass
	 * only differ {@link FollowCompassBehavior#ALLOWED_DIFF_BETWEEN_DEGREES}
	 * degree.
	 * 
	 * @return whether the difference is insignificant
	 */
	private boolean isInDirection() {
		try {
			this.ownDegrees = this.ownCompass.getDegree();
		}
		catch (RemoteSensorException e) {
			return true;
		}
		catch (SensorException e) {
			logger.debug(this.name + e.getMessage());
			return true;
		}

		try {
			this.remoteDegrees = this.remoteCompass.getDegree();
		}
		catch (SensorException e) {
			if (this.remoteDegrees == -1) {
				return true;
			}
		}
		return Math.abs(this.ownDegrees - this.remoteDegrees) <= ALLOWED_DIFF_BETWEEN_DEGREES;
	}

	@Override
	public boolean specialTakeControl() {
		return !this.isInDirection();
	}

	@Override
	public boolean actionStopCondition() {
		return this.isInDirection();
	}

	@Override
	public void doAction() {
		logger.info("own deg: " + this.ownDegrees);
		logger.info("remote deg: " + this.remoteDegrees);

		// error update
		float currentError = CompassSensorUtils.getSmallestDiff(this.remoteDegrees,
				this.ownDegrees);

		logger.info("turn deg: " + currentError);
		if (this.rotate) {
			this.pilot.rotate(currentError, false);
		}
		else {
			this.pilot.arcForward(currentError);
		}
	}

	@Override
	public void stopAction() {
		this.pilot.stop();
	}

}
