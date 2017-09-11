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
package robots.linefollower.distributed;

import robots.linefollower.pid.TrikePIDProperties;

public class RemoteTrikePIDProperties extends TrikePIDProperties {

	public static final float DEFAULT_NORMALIZED_LINE_LIGHT_VALUE = 30f;
	private static final float TRAVEL_SPEED = 2f;

	protected static final float KP = 1.7f;
	protected static final float KI = 0.1f;
	protected static final float KD = 2.5f;

	@Override
	public float getKD() {
		return KD;
	}

	@Override
	public float getKI() {
		return KI;
	}

	@Override
	public float getKP() {
		return KP;
	}

	@Override
	public float getTravelSpeed() {
		return TRAVEL_SPEED;
	}
}
