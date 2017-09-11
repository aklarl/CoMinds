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

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import logging.Logger;
import logging.Logger.LogLevel;


/**
 * This is a test class for calibrating a vehicle. It will turn the robot in
 * place. When you push the ESCAPE Button it will return how many degrees it
 * thinks it has rotated. With that you can define a SCALING Factor if the
 * vehicle does not exactly turn 360 degrees if asked for.
 * 
 * @author Annabelle Klarl
 */
public class Find360Degree {

	private static final Logger logger = Logger.getLogger("logFile.txt",
			LogLevel.DEBUG, 500, true);

	public static final float MOVE_SPEED = 10f;
	public static final float WHEEL_DIAMETER = 3.6f;
	public static final float TRACK_WIDTH = 13.1f;
	public static final Motor LEFT_MOTOR = Motor.C;
	public static final Motor RIGHT_MOTOR = Motor.A;

	public static void main(String[] aArg) throws Exception {
		System.out.println("find 360 started");
		logger.debug("find 360 started");

		final DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAMETER,
				TRACK_WIDTH, LEFT_MOTOR, RIGHT_MOTOR);
		pilot.setTravelSpeed(MOVE_SPEED);
		final Behavior turn = new Behavior() {

			@Override
			public void action() {
				pilot.rotate(1080, false);
			}

			@Override
			public void suppress() {
				pilot.stop();
			}

			@Override
			public boolean takeControl() {
				return true;
			}
		};

		Behavior[] bArray = { turn };
		Button.waitForPress();

		(new Arbitrator(bArray, true)).start();
		logger.debug("find 360 stopped");
		logger.stopLogging();
	}
}
