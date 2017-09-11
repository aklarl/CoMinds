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
package test;
import lejos.nxt.Button;
import logging.Logger;
import logging.Logger.LogLevel;

import communication.BTComm;
import communication.BTCommObserver;
import communication.BTEvent;
import communication.NXTConnectionManager;

public class NXT1 extends BTCommObserver {

	public static final Logger logger = Logger.getLogger("Josy.txt",
			LogLevel.DEBUG, 500, true);

	@Override
	public void notify(BTComm btcomm, BTEvent event) {
		super.notify(btcomm, event);
		switch (event) {
		case DEGREE:
			synchronized (btcomm) {
				btcomm.notify();
			}
		}
	}

	public static void main(String[] args) {
		try {
			NXT1 nxt = new NXT1();

			// NXTConnectionManager manager = NXTConnectionManager.getManager();
			// try {
			// BTComm btcomm = manager.getDirectConnection("Johnny", 1, true);
			// btcomm.register(nxt, BTEvent.DEGREE);
			//
			// while (!Button.ESCAPE.isPressed()) {
			// System.out.println(btcomm.getRemoteDegree());
			// }
			// }
			// catch (Throwable e) {
			// System.out.println("Exc" + e.getClass());
			// }

			// ////////////////////////////////////////

			NXTConnectionManager manager = NXTConnectionManager.getManager(500);
			try {
				// BTComm btcomm = manager.getDirectConnection("Johnny");
				BTComm btcomm = manager.getManagedConnection("Johnny", true);
				btcomm.register(nxt, BTEvent.DEGREE);

				while (!Button.ESCAPE.isPressed()) {
					// System.out.println(btcomm.getRemoteDegree());
				}
			}
			catch (Throwable e) {
				System.out.println("Exc" + e.getClass());
			}

			logger.debug("ESCAPE");
			System.out.println("ESCAPE");
			NXTConnectionManager.closeManager();

			System.out.println("stopping");
			logger.stopLogging();
		}
		catch (Throwable e) {
			System.out.println(e.getClass());
		}

		Button.waitForPress();
	}
}
