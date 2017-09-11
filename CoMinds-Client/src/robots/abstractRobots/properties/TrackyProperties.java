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
package robots.abstractRobots.properties;

import lejos.nxt.Motor;

/**
 * The properties of a tracky (tracked vehicle)
 * 
 * @author Annabelle Klarl
 */
public class TrackyProperties extends BotProperties {

	private static final float WHEEL_DIAMETER = 3.6f;
	private static final float TRACK_WIDTH = 13.1f;
	private static final Motor LEFT_MOTOR = Motor.C;
	private static final Motor RIGHT_MOTOR = Motor.A;

	private static final float SCALE_FACTOR = 1.01f;

	@Override
	public float getTrackWidth() {
		return TRACK_WIDTH;
	}

	@Override
	public float getWheelDiameter() {
		return WHEEL_DIAMETER;
	}

	@Override
	public Motor getLeftMotor() {
		return LEFT_MOTOR;
	}

	@Override
	public Motor getRightMotor() {
		return RIGHT_MOTOR;
	}

	@Override
	public float getScaleFactor() {
		return SCALE_FACTOR;
	}

}
