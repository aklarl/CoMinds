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
package behaviourmodel.contest;

import sensorwrappers.color.OwnColorSensorWrapper;
import sensorwrappers.exceptions.SensorException;
import behaviourmodel.StoppableBehavior;

//TODO-COMMENT
public class WaitIfBlackBehavior extends StoppableBehavior {

	private final OwnColorSensorWrapper light;
	private final float black;
	private final long waitInterval;
	private final long afterWaitInterval;

	private long lastCall = System.currentTimeMillis();

	public WaitIfBlackBehavior(OwnColorSensorWrapper light, float black,
			long waitInterval, long afterWaitInterval) {
		super("wait");
		this.light = light;
		this.black = black;
		this.waitInterval = waitInterval;
		this.afterWaitInterval = afterWaitInterval;
	}

	@Override
	public void action() {
		logger.info(this.name + " started");

		synchronized (this) {
			try {
				this.wait(this.waitInterval);
			}
			catch (InterruptedException e) {
			}
		}
		this.lastCall = System.currentTimeMillis();
		this.suppressed = false;
	}

	@Override
	public boolean specialTakeControl() {
		try {
			return (System.currentTimeMillis() - this.lastCall) >= this.afterWaitInterval
					+ this.waitInterval
					&& this.light.getLightValue() < this.black;
		}
		catch (SensorException e) {
			return false;
		}
	}

	@Override
	public boolean actionStopCondition() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doAction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopAction() {
		// TODO Auto-generated method stub

	}
}
