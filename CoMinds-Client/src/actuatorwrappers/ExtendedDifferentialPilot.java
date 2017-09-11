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
package actuatorwrappers;

import lejos.robotics.TachoMotor;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * This is an extended version of the TachoPilot. For the rotating method it
 * will multiply the angle to rotate with the given scale factor.
 * 
 * @author Annabelle Klarl
 */
public class ExtendedDifferentialPilot extends DifferentialPilot {

	private float scaleFactor;
	private boolean reverse;

	/**
	 * Constructor: see documentation
	 * {@link DifferentialPilot#DifferentialPilot(float, float, TachoMotor, TachoMotor)}
	 */
	public ExtendedDifferentialPilot(final float wheelDiameter,
			final float trackWidth, final TachoMotor leftMotor,
			final TachoMotor rightMotor, final float scaleFactor) {
		this(wheelDiameter, trackWidth, leftMotor, rightMotor, false,
				scaleFactor);
	}

	/**
	 * Constructor: see documentation
	 * {@link DifferentialPilot#DifferentialPilot(float, float, TachoMotor, TachoMotor)}
	 */
	public ExtendedDifferentialPilot(final float wheelDiameter,
			final float trackWidth, final TachoMotor leftMotor,
			final TachoMotor rightMotor, final boolean reverse,
			final float scaleFactor) {
		this(wheelDiameter, wheelDiameter, trackWidth, leftMotor, rightMotor,
				reverse, scaleFactor);
	}

	/**
	 * Constructor: see documentation
	 * {@link DifferentialPilot#DifferentialPilot(float, float, float, TachoMotor, TachoMotor, boolean)}
	 */
	public ExtendedDifferentialPilot(final float leftWheelDiameter,
			final float rightWheelDiameter, final float trackWidth,
			final TachoMotor leftMotor, final TachoMotor rightMotor,
			final boolean reverse, final float scaleFactor) {
		super(leftWheelDiameter, rightWheelDiameter, trackWidth, leftMotor,
				rightMotor, reverse);
		this.scaleFactor = scaleFactor;
		this.reverse = reverse;
	}

	@Override
	public void steer(float turnRate) {
		super.steer((this.reverse ? -1 : 1) * turnRate * this.scaleFactor);
	}

	@Override
	public void arcForward(float radius) {
		super.arcForward((this.reverse ? -1 : 1) * radius * this.scaleFactor);
	}

	@Override
	public void arcBackward(float radius) {
		super.arcBackward((this.reverse ? -1 : 1) * radius * this.scaleFactor);
	}

	@Override
	public void rotate(float angle) {
		super.rotate((this.reverse ? -1 : 1) * angle * this.scaleFactor);
	}

	@Override
	public void rotate(float angle, boolean immediateReturn) {
		super.rotate((this.reverse ? -1 : 1) * angle * this.scaleFactor,
				immediateReturn);
	}

	@Override
	public float getAngleIncrement() {
		return (this.reverse ? -1 : 1) * super.getAngleIncrement()
				/ this.scaleFactor;
	}

}
