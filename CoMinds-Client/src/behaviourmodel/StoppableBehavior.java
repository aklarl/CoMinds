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
package behaviourmodel;

import lejos.robotics.subsumption.Behavior;
import logging.Logger;

/**
 * This class is an abstract class for a behavior that can be stopped by calling
 * the method stop. When subclassing this behavior, please call takeControl and
 * stop in your new implemented takeControl and stop methods to ensure that the
 * newly implemented behavior can also be stopped.
 * 
 * @author Annabelle Klarl
 */
public abstract class StoppableBehavior implements Behavior {
	protected static final Logger logger = Logger.getLogger();

	protected boolean stopped = false;
	protected boolean suppressed = false;
	protected String name;

	public StoppableBehavior(String name) {
		this.name = name;
	}

	/**
	 * takes control if the action is not stopped
	 * 
	 * @return whether to take control
	 */
	@Override
	public boolean takeControl() {
		return !this.stopped && this.specialTakeControl();
	}

	/**
	 * returns whether a special condition is fulfilled that this action shall
	 * be executed (additional to not stopped)
	 * 
	 * @return whether a special condition is fulfilled
	 */
	public abstract boolean specialTakeControl();

	/**
	 * runs the action of this behavior until the behavior is stopped,
	 * suppressed or a special condition is reached that stops the action
	 */
	@Override
	public void action() {
		logger.info(this.name + " started");

		while (!this.stopped && !this.suppressed && !this.actionStopCondition()) {
			this.doAction();
		}

		this.stopAction();
		this.suppressed = false;
	}

	/**
	 * returns whether a special condition is reached that should stop the
	 * action of this behavior for now (action can be executed later on)
	 * 
	 * @return whether the special condition is reached
	 */
	public abstract boolean actionStopCondition();

	/**
	 * the actual action to do per round
	 */
	public abstract void doAction();

	/**
	 * what to do if the action is finished (some cleaning...)
	 */
	public abstract void stopAction();

	/**
	 * suppresses the action by setting the private field suppressed to true
	 * (use this in while-loop in the action method)
	 */
	@Override
	public void suppress() {
		this.suppressed = true;
		logger.info(this.name + " suppress");
	}

	/**
	 * stops a behavior just by setting the boolean variable stopped to true
	 */
	public void stop() {
		this.stopped = true;
		logger.info(this.name + " stopped");
	}

}
