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
package sensorwrappers.compass;

public class CompassSensorUtils {

	public static float getSmallestDiff(float reference, float currentDegree) {
		float currentError = currentDegree - reference;

		// currentError might be more than 180 degrees or less than -180
		// degrees: normalize it to a scale from -180 to 180
		if (currentError > 0 && currentError >= 180) {
			currentError = currentError - 359;
		}
		else if (currentError < 0 && currentError <= -180) {
			currentError = currentError + 359;
		}

		return currentError;
	}

	/**
	 * for any degree it will compute the difference to the null point (at
	 * maximum 180 or -180 degree)
	 * 
	 * @param degree
	 * @return
	 */
	public static float absTo180(float degree) {
		degree = degree % 359;
		if (degree >= 180) {
			return degree - 359;
		}
		else if (degree <= -180) {
			return degree + 359;
		}
		else {
			return degree % 359;
		}
	}

}
