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
package robots.calibrate;

import behaviourmodel.turn.TurnBehavior;
import actuatorwrappers.ExtendedDifferentialPilot;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import logging.Logger;
import logging.Logger.LogLevel;


/**
 * This class is for testing if the parameters for the pilot are set so that the
 * robot will turn exactly 360 degree. The robot will turn exactly 360 degree.
 * Just change the parameters for the track width and the diameter according to
 * your vehicle.
 * 
 * @author Annabelle Klarl
 */
public class Turn360Degrees {

	private static final Logger logger = Logger.getLogger("logFile.txt",
			LogLevel.DEBUG, 500, true);

	public static final float TRAVEL_SPEED = 15f;
	public static final float ROTATE_SPEED = 100f;
	public static final int ACCELERATION = 3000;
	// shooter bot
	public static final float WHEEL_DIAMETER = 3.6f;
	public static final float TRACK_WIDTH = 13.1f;
	public static final Motor LEFT_MOTOR = Motor.C;
	public static final Motor RIGHT_MOTOR = Motor.A;

	// easy bot
	// public static final float WHEEL_DIAMETER = 4.32f;
	// public static final float TRACK_WIDTH = 8.85f;
	// public static final Motor LEFT_MOTOR = Motor.A;
	// public static final Motor RIGHT_MOTOR = Motor.C;

	public static void main(String[] aArg) throws Exception {
		System.out.println("turn 360 started");
		logger.debug("turn 360 started");

		final DifferentialPilot pilot = new ExtendedDifferentialPilot(
				WHEEL_DIAMETER, TRACK_WIDTH, LEFT_MOTOR, RIGHT_MOTOR, 1.01f);
		// for pilot.travel()
		// -> motorspeed = Math.round(0.5f * travelSpeed *
		// (_leftDegPerDistance + _rightDegPerDistance))
		// motorspeed for pilot.forward(), backward(), steer(), arc()
		pilot.setTravelSpeed(TRAVEL_SPEED);
		// for pilot.rotate()
		pilot.setRotateSpeed(ROTATE_SPEED);
		pilot.setAcceleration(ACCELERATION);

		final TurnBehavior turn = new TurnBehavior(pilot, 5, 7, true);

		Button.ESCAPE.addButtonListener(new ButtonListener() {

			@Override
			public void buttonPressed(Button b) {
				turn.stop();
			}

			@Override
			public void buttonReleased(Button b) {
			}

		});

		TurnBehavior[] bArray = { turn };
		Button.waitForPress();

		(new Arbitrator(bArray, true)).start();
		logger.debug("turn 360 stopped");
		logger.stopLogging();
	}
}
