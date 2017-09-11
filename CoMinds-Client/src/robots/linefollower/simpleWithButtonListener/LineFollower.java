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
package robots.linefollower.simpleWithButtonListener;

import behaviourmodel.linefollower.simple.SimpleDriveForwardBehavior;
import behaviourmodel.linefollower.simple.SimpleFindLineBehavior;
import actuatorwrappers.ExtendedDifferentialPilot;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import logging.Logger;
import logging.Logger.LogLevel;


/**
 * A robot with this program will follow a black line on the floor and can be
 * stopped by pressing ESCAPE. It uses therefore Behavior and Pilot classes. It
 * requires tracked vehicle with two independently controlled tracks with motors
 * connected to motor ports A and C and a color light sensor mounted forwards
 * and pointing down connected to sensor port 1. Press ENTER to start the robot.
 * 
 * @author Annabelle Klarl
 */
public class LineFollower {

	private static final Logger logger = Logger.getLogger("log_follower.txt",
			LogLevel.DEBUG, 500, true);

	public static void main(String[] aArg) throws Exception {

		// shooter bot
		// final DifferentialPilot pilot = new ExtendedDifferentialPilot(3.6f,
		// 13.1f, Motor.C, Motor.A, false, 1.01f);

		// easy bot
		final DifferentialPilot pilot = new ExtendedDifferentialPilot(4.32f,
				8.85f, Motor.A, Motor.C, false, 1.59f);
		pilot.setRotateSpeed(180);

		SensorPort.S2.setType(ColorSensor.TYPE_COLORFULL);
		final ColorSensor light = new ColorSensor(SensorPort.S2,
				ColorSensor.WHITE);

		final SimpleDriveForwardBehavior forward = new SimpleDriveForwardBehavior(
				pilot);
		final SimpleFindLineBehavior finder = new SimpleFindLineBehavior(pilot,
				light);

		LCD.drawString("LineFollower ", 0, 1);

		Button.ESCAPE.addButtonListener(new ButtonListener() {

			@Override
			public void buttonPressed(Button b) {
				forward.stop();
				finder.stop();
			}

			@Override
			public void buttonReleased(Button b) {
			}

		});

		Button.waitForPress();

		Behavior[] behaviors = { forward, finder };
		(new Arbitrator(behaviors, true)).start();

		logger.stopLogging();
	}
}
