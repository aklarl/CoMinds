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
package robots.linefollower.simple;

import actuatorwrappers.ExtendedDifferentialPilot;
import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.ColorSensor.Color;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;


/**
 * This class is a demonstration of the use of the Behavior and Pilot classes to
 * implement a simple line following robot. It requires a wheeled vehicle with
 * two independently controlled wheels with motors connected to motor ports A
 * and C, and a light sensor mounted forwards and pointing down, connected to
 * sensor port 1. Press ENTER to start the robot.
 * 
 * @author Lawrie Griffiths
 */
public class SimpleFollower {

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
		/**
		 * this behavior wants to take contrtol when the light sensor sees the
		 * line
		 */
		Behavior DriveForward = new Behavior() {
			public boolean takeControl() {
				return light.getColorID() == Color.BLACK;
			}

			public void suppress() {
				pilot.stop();
			}

			public void action() {
				pilot.forward();
				while (light.getColorID() == Color.BLACK) {
					Thread.yield(); // action complete when not on line
				}
			}
		};

		Behavior OffLine = new Behavior() {
			private boolean suppress = false;

			public boolean takeControl() {
				return light.getColorID() != Color.BLACK;
			}

			public void suppress() {
				this.suppress = true;
			}

			public void action() {
				int sweep = 10;
				while (!this.suppress) {
					pilot.rotate(sweep, true);
					while (!this.suppress && pilot.isMoving()) {
						Thread.yield();
					}
					sweep *= -2;
				}
				pilot.stop();
				this.suppress = false;
			}
		};

		Behavior[] bArray = { OffLine, DriveForward };
		LCD.drawString("SimpleLineFollower ", 0, 1);
		Button.waitForPress();
		(new Arbitrator(bArray)).start();
	}
}
