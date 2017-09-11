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

import common.MapOfLists;

import lejos.nxt.Button;

/**
 * This class replaces a button listener. It listens for all button events and
 * notifies the observer. An Observer can register for any Button he likes and
 * will be notified if the button was pressed.
 * 
 * @author Annabelle Klarl
 */
public class ButtonMonitor extends Thread {

	private final boolean runForever;
	private MapOfLists<Button, ButtonObserver> observers;

	/**
	 * Constructor
	 * 
	 * @param runForever
	 *            whether the run method shall multiple times or not
	 */
	public ButtonMonitor(boolean runForever) {
		this.runForever = runForever;
		this.observers = new MapOfLists<Button, ButtonObserver>();
		this.setDaemon(true);
	}

	/**
	 * Constructor were the button listener runs forever (or until the parent
	 * thread is terminated)
	 */
	public ButtonMonitor() {
		this(true);
	}

	/**
	 * registers an observer for aa button
	 * 
	 * @param observer
	 *            the ButtonObserver to register
	 * @param event
	 *            the BTEvent to register for
	 */
	public void register(ButtonObserver observer, Button button) {
		this.observers.putElement(button, observer);

	}

	/**
	 * unregisters the observer for all buttons he is registered for
	 * 
	 * @param observer
	 *            the ButtonObserver to unregister
	 */
	public void unregister(ButtonObserver observer) {
		this.observers.removeElement(observer);
	}

	/**
	 * unregisters the observer for a specific button
	 * 
	 * @param observer
	 *            the ButtonObserver to unregister
	 * @param button
	 *            the button to unregister for
	 */
	public void unregister(ButtonObserver observer, Button button) {
		this.observers.removeElement(button, observer);
	}

	/**
	 * waits until any button is pressed and notifies the registered observer
	 * for this event
	 */
	@Override
	public void run() {
		do {
			int button = Button.waitForPress();
			switch (button) {
			case Button.ID_ENTER:
				ArrayList<ButtonObserver> enterObservers = this.observers
						.get(Button.ENTER);
				if (enterObservers != null) {
					for (ButtonObserver buttonO : enterObservers) {
						buttonO.notifyForEvent(Button.ENTER);
					}
				}
				break;
			case Button.ID_ESCAPE:
				ArrayList<ButtonObserver> escapeObservers = this.observers
						.get(Button.ESCAPE);
				if (escapeObservers != null) {
					for (ButtonObserver buttonO : escapeObservers) {
						buttonO.notifyForEvent(Button.ESCAPE);
					}
				}
				break;
			case Button.ID_LEFT:
				ArrayList<ButtonObserver> leftObservers = this.observers
						.get(Button.LEFT);
				if (leftObservers != null) {
					for (ButtonObserver buttonO : leftObservers) {
						buttonO.notifyForEvent(Button.LEFT);
					}
				}
				break;
			case Button.ID_RIGHT:
				ArrayList<ButtonObserver> rightObservers = this.observers
						.get(Button.RIGHT);
				if (rightObservers != null) {
					for (ButtonObserver buttonO : rightObservers) {
						buttonO.notifyForEvent(Button.RIGHT);
					}
				}
				break;
			}
		}
		while (this.runForever);
	}

}
