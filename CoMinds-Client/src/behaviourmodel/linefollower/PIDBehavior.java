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
 * This behavior makes the LineFollower follow a line left hand. It is
 * implemented as a PID-Controller with proportional, integral and derivative
 * term (very simple! see <a href="URL#http://www.inpharmix.com/jps/PID_Controller_For_Lego_Mindstorms_Robots.html"
 * >PID controlled LineFollower</a>). This behavior can be used for a
 * linefollower that turns itself or a robot that turns another object
 * (therefore you need the parameters whether the robot is on the left side of
 * the object and how big is the distance between the middle of the robot and
 * the middle of the object)
 * 
 * @author Annabelle Klarl
 */
public class PIDBehavior extends StoppableBehavior {

	private static final float MAX_SPEED = 20.0f;

	private final DifferentialPilot pilot;
	private final float trackWidth;
	private final float normalSpeed;

	private final AbstractColorSensorWrapper light;
	private float lineLightValue;

	private final float kp;
	private final float ki;
	private final float kd;

	private final float distance;

	private float currentError = 0.0f;
	private float integral = 0.0f;
	private float derivative = 0.0f;
	private float lastError = this.currentError;
	private long lastCall = -1;

	/**
	 * Constructor
	 * 
	 * @param pilot
	 *            the pilot to control the motors
	 * @param trackWidth
	 *            the distance between the two wheels/tracks of this robot
	 * @param light
	 *            a light value getter that gets the light value underneath an
	 *            own or remote color sensor
	 * @param lineLightValue
	 *            the light value if the robot is on the line (which should be
	 *            followed)
	 * @param kp
	 *            the factor for multiplying the proportional
	 * @param ki
	 *            the factor for multiplying the integrative
	 * @param kd
	 *            the factor for multiplying the derivative
	 * @param distance
	 *            how big is the distance between the middle of the robot and
	 *            the middle of the object
	 */
	public PIDBehavior(DifferentialPilot pilot, float trackWidth,
			AbstractColorSensorWrapper light, float lineLightValue, float kp,
			float ki, float kd, float distance) {
		super("pid");
		this.pilot = pilot;
		this.trackWidth = trackWidth;
		this.normalSpeed = this.pilot.getTravelSpeed();

		this.light = light;
		this.lineLightValue = lineLightValue;

		this.kp = kp;
		this.ki = ki;
		this.kd = kd;

		this.distance = distance;
	}

	/**
	 * Constructor
	 * 
	 * @param pilot
	 *            the pilot to control the motors
	 * @param trackWidth
	 *            the distance between the two wheels/tracks of this robot
	 * @param light
	 *            a light value getter that gets the light value underneath an
	 *            own or remote color sensor
	 * @param lineLightValue
	 *            the light value if the robot is on the line (which should be
	 *            followed)
	 * @param kp
	 *            the factor for multiplying the proportional
	 * @param ki
	 *            the factor for multiplying the integrative
	 * @param kd
	 *            the factor for multiplying the derivative
	 */
	public PIDBehavior(DifferentialPilot pilot, float trackWidth,
			AbstractColorSensorWrapper light, float lineLightValue, float kp,
			float ki, float kd) {
		this(pilot, trackWidth, light, lineLightValue, kp, ki, kd, 0.0f);
	}

	@Override
	public boolean specialTakeControl() {
		return true;
	}

	@Override
	public boolean actionStopCondition() {
		return false;
	}

	/**
	 * following a line with a pid controller
	 */
	@Override
	public void doAction() {
		float lightValue = 0.0f;
		try {
			lightValue = this.light.getLightValue();
			logger.debug(this.name + " lightvalue " + lightValue);
		}
		catch (SensorException e) {
			// ignore noise reads
			return;
		}

		// updates
		this.currentError = lightValue - this.lineLightValue;

		this.integral = this.integral + this.currentError;
		this.derivative = this.currentError - this.lastError;
		this.lastError = this.currentError;

		float turnRate = -(this.kp * this.currentError + this.ki
				* this.integral + this.kd * this.derivative);

		if (this.lastCall != -1) {
			long now = System.currentTimeMillis();
			float duration = now - this.lastCall;
			this.lastCall = now;
			turnRate = turnRate * 20 / duration;

		}

		if (this.distance != 0.0f) {

			// converted as converted in arcForward to steer in the
			// DifferentialPilot
			float radius_middle = this.turnRate2radius(turnRate);

			// converts the radius to a radius that a robot must follow if it is
			// distance away from the center of the turn
			float radius_side = radius_middle + this.distance;
			float ratioSide = Math.abs(radius_side / radius_middle);
			float radius_other_side = radius_middle - this.distance;
			float ratioOtherSide = Math.abs(radius_other_side / radius_middle);

			// adapt speed
			float speed_side = ratioSide * this.normalSpeed;
			float speed_other_side = ratioOtherSide * this.normalSpeed;
			if (speed_side > MAX_SPEED) {
				speed_side = MAX_SPEED;
			}
			else if (speed_other_side > MAX_SPEED) {
				float newNormalSpeed = MAX_SPEED / ratioOtherSide;
				speed_side = ratioSide * newNormalSpeed;
			}
			this.pilot.setTravelSpeed(speed_side);

			// if the robot must arc forward or backwards depends on the radius
			// of the object. If the radius is smaller than the distance, the
			// inner robot must arc backward
			if (Math.signum(radius_middle) != Math.signum(radius_side)) {
				logger.debug(this.name + " radius bwd: " + radius_side
						+ " (speed " + speed_side + ")");
				this.pilot.arcBackward(radius_side);
			}
			else {
				logger.debug(this.name + " radius fwd: " + radius_side
						+ " (speed " + speed_side + ")");
				this.pilot.arcForward(radius_side);
			}
		}
		else {
			if (turnRate > 200) {
				logger.error(this.name + " turnrate > 200: " + turnRate);
				turnRate = 200f;
			}
			else if (turnRate < -200) {
				logger.error(this.name + " turnrate < -200: " + turnRate);
				turnRate = -200f;
			}
			else {
				logger.debug(this.name + " turnrate: " + turnRate);
			}

			// faster variant
			this.pilot.steer(turnRate);
		}
	}

	private float turnRate2radius(float turnRate) {
		int direction;
		float turnRateToUse;
		if (turnRate < 0) {
			direction = -1;
			turnRateToUse = -turnRate;
		}
		else {
			direction = 1;
			turnRateToUse = turnRate;
		}
		return direction * this.trackWidth * (100f / turnRateToUse - 0.5f);
	}

	@Override
	public void stopAction() {
		this.currentError = 0.0f;
		this.integral = 0.0f;
		this.derivative = 0.0f;
		this.lastError = this.currentError;

		this.pilot.stop();
	}
}
