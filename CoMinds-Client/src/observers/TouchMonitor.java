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
package observers;

import java.util.ArrayList;

import lejos.nxt.TouchSensor;

/**
 * This class monitors a given touch sensor. It will notify any observer if the
 * state of the touch monitor changes.
 * 
 * @author Annabelle Klarl
 */
public class TouchMonitor extends Thread {

	private ArrayList<TouchObserver> observers;
	private TouchSensor touch;
	private boolean pressed;

	/**
	 * Constructor
	 * 
	 * @param touch
	 *            the touch sensor to monitor
	 */
	public TouchMonitor(TouchSensor touch) {
		this.touch = touch;
		this.observers = new ArrayList<TouchObserver>();
		this.setDaemon(true);
	}

	/**
	 * registers an observer for the touch sensor
	 * 
	 * @param observer
	 *            the ButtonObserver to register
	 */
	public void register(TouchObserver observer) {
		if (this.observers.indexOf(observer) == -1) {
			this.observers.add(observer);
		}

	}

	/**
	 * unregisters a observer
	 * 
	 * @param observer
	 *            the ButtonObserver to unregister
	 */
	public void unregister(TouchObserver observer) {
		this.observers.remove(observer);
	}

	/**
	 * notifies all observers if the state of the touch sensor changes
	 */
	private void notifyObservers() {
		for (TouchObserver observer : this.observers) {
			observer.stateChanged(this.pressed);
		}
	}

	/**
	 * monitors the touch sensor and notifies all observers if the state of the
	 * touch sensor changes
	 */
	@Override
	public void run() {
		while (true) {
			if (this.pressed && !this.touch.isPressed()) {
				this.pressed = false;
				this.notifyObservers();
			}
			if (!this.pressed && this.touch.isPressed()) {
				this.pressed = true;
				this.notifyObservers();
			}
			Thread.yield();
		}
	}

}
