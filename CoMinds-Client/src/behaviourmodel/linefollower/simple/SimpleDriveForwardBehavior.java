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
import lejos.robotics.navigation.DifferentialPilot;


/**
 * This behavior keeps the robot driving forward. The behavior can be stopped by
 * calling stop().
 * 
 * @author Annabelle Klarl
 */
public class SimpleDriveForwardBehavior extends StoppableBehavior {
	private final DifferentialPilot pilot;

	public SimpleDriveForwardBehavior(DifferentialPilot pilot) {
		super("drive");
		this.pilot = pilot;
	}

	@Override
	public boolean specialTakeControl() {
		return true;
	}

	@Override
	public boolean actionStopCondition() {
		return false;
	}

	@Override
	public void doAction() {
		this.pilot.forward();
	}

	@Override
	public void stopAction() {
		this.pilot.stop();
	}
}
