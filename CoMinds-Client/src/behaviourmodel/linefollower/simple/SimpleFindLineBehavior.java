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
package behaviourmodel.linefollower.simple;

import behaviourmodel.StoppableBehavior;
import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.robotics.navigation.DifferentialPilot;


/**
 * This behavior comes to action if the floor underneath the ColorLightSensor is
 * not black. It will then search for a black field on the floor (presuming this
 * to be the line). It starts searching by rotating 10 degrees to the right and
 * multiplies the degrees with -2 if no line was found. This behavior can be
 * stopped by calling stop().
 * 
 * @author Annabelle Klarl
 */
public class SimpleFindLineBehavior extends StoppableBehavior {

	private final DifferentialPilot pilot;
	private final ColorSensor light;

	private int sweep = 10;

	/**
	 * Constructor
	 * 
	 * @param pilot
	 *            the pilot that drives the robot
	 * @param light
	 *            the LightSensor
	 */
	public SimpleFindLineBehavior(DifferentialPilot pilot, ColorSensor light) {
		super("find line");
		this.pilot = pilot;
		this.light = light;
	}

	/**
	 * returns whether the color of the floor underneath the robot is black
	 * 
	 * @return whether the color underneath is black
	 */
	private boolean lineFound() {
		return this.light.getColorID() == Color.BLACK;
	}

	@Override
	public boolean specialTakeControl() {
		return !this.lineFound();
	}

	@Override
	public boolean actionStopCondition() {
		return this.lineFound();
	}

	@Override
	public void doAction() {
		this.pilot.rotate(this.sweep, false);
		this.sweep *= -2;
	}

	@Override
	public void stopAction() {
		this.pilot.stop();
		this.sweep = 10;
	}

}
