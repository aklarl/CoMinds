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
package robots.linefollower.pid;

/**
 * These are any properties needed especially for line following purposes with a
 * PID Controller.
 * 
 * @author Annabelle Klarl
 */
public interface PIDProperties {

	// parameter for following the line
	public static final float DEFAULT_NORMALIZED_LINE_LIGHT_VALUE = 40f;
	public static final float DEFAULT_NORMALIZED_OFFLINE_LIGHT_VALUE = 98f;

	// parameters for finding the line
	public static final float TRAVEL_DISTANCE = 7;
	public static final float SWEEP = -20;

	/**
	 * the light value that describes the line to follow
	 * 
	 * @return the light value for the line
	 */
	public float getNormalizedLineLightValue();

	/**
	 * the light value that means that the robot has lost the line
	 * 
	 * @return the light value for offline
	 */
	public float getNormalizedOfflineLightValue();

	/**
	 * gets the distance to travel while searching the line
	 * 
	 * @return the distance to travel
	 */
	public float getTravelDistance();

	/**
	 * gets the degrees how far to rotate to search the line
	 * 
	 * @return the degrees to rotate to search the line
	 */
	public float getSweep();

	/**
	 * the kp factor of the PID Controller
	 * 
	 * @return the kp factor
	 */
	public float getKP();

	/**
	 * the kd factor of the PID Controller
	 * 
	 * @return the kd factor
	 */
	public float getKD();

	/**
	 * the ki factor of the PID Controller
	 * 
	 * @return the ki factor
	 */
	public float getKI();

	/**
	 * the travel speed (for steering) of this robot (might be different if line
	 * following is wanted)
	 * 
	 * @return the travel speed
	 */
	public float getTravelSpeed();
}
