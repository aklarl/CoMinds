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
 * These are the main properties for any moving robot with wheels.
 * 
 * @author Annabelle Klarl
 */
public abstract class BotProperties {

	// standard speed parameters
	private static final float TRAVEL_SPEED = 20f;
	private static final float ROTATE_SPEED = 10f;
	private static final int ACCELERATION = 3000;

	/**
	 * The diameter of one wheel
	 * 
	 * @return the diameter
	 */
	public abstract float getWheelDiameter();

	/**
	 * the distance between the middle of the left wheel and the right wheel
	 * 
	 * @return
	 */
	public abstract float getTrackWidth();

	/**
	 * the port where the left motor is plugged in
	 * 
	 * @return the port for the left motor
	 */
	public abstract Motor getLeftMotor();

	/**
	 * the port where the right motor is plugged in
	 * 
	 * @return the port for the right motor
	 */
	public abstract Motor getRightMotor();

	/**
	 * the speed for traveling, forward, backward and steer
	 * 
	 * @return the speed
	 */
	public float getTravelSpeed() {
		return TRAVEL_SPEED;
	}

	/**
	 * the speed for rotating
	 * 
	 * @return the speed
	 */
	public float getRotateSpeed() {
		return ROTATE_SPEED;
	}

	/**
	 * how smooth the motors will start or end the motion
	 * 
	 * @return the acceleration of the motors
	 */
	public int getAcceleration() {
		return ACCELERATION;
	}

	/**
	 * the factor with which to multiply the degrees to rotate so that the robot
	 * really rotates the given degrees.
	 * 
	 * @return the scaling factor
	 */
	public abstract float getScaleFactor();

}
