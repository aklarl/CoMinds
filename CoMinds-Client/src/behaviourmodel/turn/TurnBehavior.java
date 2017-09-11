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
package behaviourmodel.turn;

import behaviourmodel.StoppableBehavior;
import lejos.robotics.navigation.DifferentialPilot;
import logging.Logger;


/**
 * This behavior either turns 360 degree. Or this behavior starts with rotating
 * sweep degrees to the right. Then it will muliply the degrees with -2 and
 * start rotating again until 360 degrees are reached. Then it will travel
 * travelDistance forward and start the behavior again. This behavior can be
 * stopped by calling stop(). It is for testing whether your robot rotates
 * exactly 360 degree.
 * 
 * @author Annabelle Klarl
 */
public class TurnBehavior extends StoppableBehavior {

	private static final Logger logger = Logger.getLogger();

	// the scaling factor for rotating exactly 360 degree
	public static final float SCALE_FACTOR_ROTATING = 1.6f;

	// flag for suppressing
	private boolean suppress = false;
	private boolean turnOnce = false;

	private final DifferentialPilot pilot;
	private final int sweep;
	private final float travelDistance;

	/**
	 * Constructor
	 * 
	 * @param pilot
	 *            the pilot for controlling the motors
	 * @param sweep
	 *            the number of degrees the robot turns first for searching the
	 *            line
	 * @param travelDistance
	 *            the distance the robot drives when having searched 360 degrees
	 * @param turnOnce
	 *            whether the robot should only turn 360 degrees or keeps
	 *            turning with increasing angle
	 */
	public TurnBehavior(DifferentialPilot pilot, int sweep,
			float travelDistance, boolean turnOnce) {
		super("turn");
		this.turnOnce = turnOnce;
		this.pilot = pilot;
		this.sweep = sweep;
		this.travelDistance = travelDistance;
	}

	@Override
	public void action() {
		if (this.turnOnce) {
			this.actionOnce();
		}
		else {
			this.actionMultiple();
		}
	}

	/**
	 * starts with rotating sweep degrees to the right. Then it will muliply the
	 * degrees with -2 and start rotating again until 360 degrees are reached.
	 * Then it will travel travelDistance forward and start the behavior again.
	 */
	public void actionMultiple() {
		logger.debug("turn started");

		// while not stopped by calling method stop
		// and not suppressed by another behavior,
		// turn
		while (!this.stopped && !this.suppress) {

			// turn a whole circle
			int currentSweep = this.sweep;
			int angleToTheMiddle = 0;
			while (!this.stopped && !this.suppress
					&& Math.abs(angleToTheMiddle) < 180) {

				logger.debug("turn angle " + currentSweep);
				this.pilot.rotate(SCALE_FACTOR_ROTATING * currentSweep, false);

				angleToTheMiddle = currentSweep + angleToTheMiddle;
				currentSweep *= -2;
			}

			if (!this.stopped && !this.suppress) {
				// rotate to the front
				if (angleToTheMiddle < 0) {
					currentSweep = -360 - angleToTheMiddle;
				}
				else if (angleToTheMiddle > 0) {
					currentSweep = 360 - angleToTheMiddle;
				}
				logger.debug("turn angle " + currentSweep);
				this.pilot.rotate(SCALE_FACTOR_ROTATING * currentSweep, false);

				// travel some distance and turn again
				logger.debug("travel");
				this.pilot.travel(this.travelDistance, false);

			}

		}
		this.pilot.stop();
		this.suppress = false;
	}

	/**
	 * turns 360 degree (one time) and will stop afterwards
	 */
	public void actionOnce() {
		this.pilot.rotate(SCALE_FACTOR_ROTATING * 360, true);
		while (!this.suppress && this.pilot.isMoving()) {
			Thread.yield();
		}
		this.stop();
	}

	@Override
	public boolean actionStopCondition() {
		return false;
	}

	@Override
	public void doAction() {
	}

	@Override
	public boolean specialTakeControl() {
		return true;
	}

	@Override
	public void stopAction() {
	}
}
