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
package behaviourmodel.linefollower;

import lejos.robotics.navigation.DifferentialPilot;
import sensorwrappers.color.AbstractColorSensorWrapper;
import sensorwrappers.exceptions.SensorException;
import behaviourmodel.StoppableBehavior;

/**
 * This behavior comes to action if the floor underneath the ColorLightSensor is
 * not black (offlineLightValue must be calibrated beforehand). It will then
 * search for a black (=inlineLightValue) field on the floor (presuming this to
 * be the line). It starts searching by rotating sweep degrees to the right and
 * multiplies the degrees with -2 if no line was found. When the robot has
 * search 360 degrees it will travel travelDistance forward. This behavior can
 * be stopped by calling stop().
 * 
 * @author Annabelle Klarl
 */
public class FindLineBehavior extends StoppableBehavior {

	private final DifferentialPilot pilot;
	private final AbstractColorSensorWrapper light;
	private float inlineLightValue;
	private float offlineLightValue;
	private int numberOfLostsInSequence = 0;
	private final float sweep;
	private final float travelDistance;
	private final float distance;

	/**
	 * Constructor
	 * 
	 * @param pilot
	 *            the pilot for controlling the motors
	 * @param light
	 *            a light value scaler that scales the light values to a scale
	 *            from 0 to 100.0
	 * @param inlineLightValue
	 *            the light value if the robot is on the line
	 * @param offlineLightValue
	 *            the light value if the robot is off the line
	 * @param sweep
	 *            the number of degrees the robot turns first for searching the
	 *            line
	 * @param travelDistance
	 *            the distance the robot drives when having searched 360 degrees
	 * @param distance
	 *            how big is the distance between the middle of the robot and
	 *            the middle of the object
	 */
	public FindLineBehavior(DifferentialPilot pilot,
			AbstractColorSensorWrapper light, float inlineLightValue,
			float offlineLightValue, float sweep, float travelDistance,
			float distance) {
		super("find line");
		this.pilot = pilot;
		this.light = light;
		this.inlineLightValue = inlineLightValue;
		this.offlineLightValue = offlineLightValue;
		this.sweep = sweep;
		this.travelDistance = travelDistance;
		this.distance = distance;
	}

	/**
	 * Constructor
	 * 
	 * @param pilot
	 *            the pilot for controlling the motors
	 * @param light
	 *            a light value scaler that scales the light values to a scale
	 *            from 0 to 100.0
	 * @param inlineLightValue
	 *            the light value if the robot is on the line
	 * @param offlineLightValue
	 *            the light value if the robot is off the line
	 * @param sweep
	 *            the number of degrees the robot turns first for searching the
	 *            line
	 * @param travelDistance
	 *            the distance the robot drives when having searched 360 degrees
	 */
	public FindLineBehavior(DifferentialPilot pilot,
			AbstractColorSensorWrapper light, float inlineLightValue,
			float offlineLightValue, float sweep, float travelDistance) {
		this(pilot, light, inlineLightValue, offlineLightValue, sweep,
				travelDistance, 0.0f);
	}

	/**
	 * sets the inline light value
	 * 
	 * @param inlineLightValue
	 *            the color of the floor when driving on the line
	 * @throws SensorNoiseException
	 *             if inlineLightValue is not between 0 and 100
	 */
	public void setInlineLightValue(float inlineLightValue)
			throws SensorException {
		if (inlineLightValue < 0.0) {
			throw new SensorException("Grey value too low!");
		}
		if (inlineLightValue > 100.0) {
			throw new SensorException("Grey value too high!");
		}
		this.inlineLightValue = inlineLightValue;
	}

	/**
	 * sets the offline light value
	 * 
	 * @param offlineLightValue
	 *            the color of the floor when driving off the line
	 * @throws SensorNoiseException
	 *             if offlineLightValue is not between 0 and 100
	 */
	public void setOfflineLightValue(float offlineLightValue)
			throws SensorException {
		if (offlineLightValue < 0.0) {
			throw new SensorException("Grey value too low!");
		}
		if (offlineLightValue > 100.0) {
			throw new SensorException("Grey value too high!");
		}
		this.offlineLightValue = offlineLightValue;
	}

	@Override
	public boolean specialTakeControl() {
		if (this.lineLost()) {
			if (this.numberOfLostsInSequence <= 2) {
				this.numberOfLostsInSequence++;
			}
			return this.numberOfLostsInSequence > 2;
		}
		else {
			this.numberOfLostsInSequence = 0;
			return false;
		}
	}

	/**
	 * checks whether the color of the floor is brighter than the offline light
	 * value
	 * 
	 * @return true if off the line
	 */
	private boolean lineLost() {
		while (true) {
			try {
				return this.light.getLightValue() > this.offlineLightValue;
			}
			catch (SensorException e) {
				// ignore noise reads and read once more
				continue;
			}
		}
	}

	/**
	 * checks whether the color of the floor is darker than the inline light
	 * value
	 * 
	 * @return true if on the line
	 */
	private boolean lineFound() {
		while (true) {
			try {
				return this.light.getLightValue() < this.inlineLightValue;
			}
			catch (SensorException e) {
				// ignore noise reads and read once more
				continue;
			}
		}
	}

	@Override
	public boolean actionStopCondition() {
		return this.lineFound();
	}

	@Override
	public void doAction() {
		// search a whole circle for the line
		float currentSweep = this.sweep;
		float radius = this.distance;
		float angleToTheMiddle = 0;

		float travelSpeed = this.pilot.getTravelSpeed();
		this.pilot.setTravelSpeed(this.pilot.getRotateSpeed());

		while (!this.stopped && !this.suppressed && !this.lineFound()
				&& Math.abs(angleToTheMiddle) < 180) {

			logger.debug("turn angle " + currentSweep);
			this.pilot.arc(radius, currentSweep, true);

			// look for the line while rotating
			this.lookForLineWhileMoving();

			angleToTheMiddle = currentSweep + angleToTheMiddle;
			currentSweep *= -2;
		}

		// travel some distance and search again
		if (!this.stopped && !this.suppressed && !this.lineFound()) {

			// rotate to the front
			if (angleToTheMiddle < 0) {
				currentSweep = -360 - angleToTheMiddle;
			}
			else if (angleToTheMiddle > 0) {
				currentSweep = 360 - angleToTheMiddle;
			}
			logger.debug("turn angle " + currentSweep);
			this.pilot.arc(radius, currentSweep, true);

			// look for the line while rotating
			this.lookForLineWhileMoving();

			logger.debug("travel");
			this.pilot.setTravelSpeed(travelSpeed);
			this.pilot.travel(this.travelDistance, true);

			// look for the line while traveling
			this.lookForLineWhileMoving();
		}

		this.pilot.setTravelSpeed(travelSpeed);

	}

	/**
	 * checks whether the line was found while the robot is moving. This method
	 * will stop the pilot if the line was found.
	 */
	private void lookForLineWhileMoving() {
		while (!this.stopped && !this.suppressed && this.pilot.isMoving()) {
			// stop rotating immediately if line was found
			if (this.lineFound()) {
				this.suppress();
				break;
			}
			// continue with rotating
			else {
				Thread.yield();
			}
		}
	}

	@Override
	public void stopAction() {
		this.numberOfLostsInSequence = 0;
		this.pilot.stop();
	}
}
