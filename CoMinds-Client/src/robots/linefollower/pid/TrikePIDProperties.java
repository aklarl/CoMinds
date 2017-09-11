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

import robots.abstractRobots.properties.TrikeProperties;

/**
 * The properties for a trike that should follow a line
 * 
 * @author Annabelle Klarl
 */
public class TrikePIDProperties extends TrikeProperties implements
		PIDProperties {

	protected static final float KP = 1.5f;
	protected static final float KI = 0.1f;
	protected static final float KD = 1.5f;

	@Override
	public float getNormalizedLineLightValue() {
		return DEFAULT_NORMALIZED_LINE_LIGHT_VALUE;
	}

	@Override
	public float getNormalizedOfflineLightValue() {
		return DEFAULT_NORMALIZED_OFFLINE_LIGHT_VALUE;
	}

	@Override
	public float getSweep() {
		return SWEEP;
	}

	@Override
	public float getTravelDistance() {
		return TRAVEL_DISTANCE;
	}

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
}
