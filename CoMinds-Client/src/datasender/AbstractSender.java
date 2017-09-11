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
package datasender;

import logging.Logger;
import communication.BTComm;

/**
 * This thread sends continously any data via the given BTComm until it is
 * stopped.
 * 
 * @author Annabelle Klarl
 */
public abstract class AbstractSender extends Thread {

	protected static final Logger logger = Logger.getLogger();

	protected BTComm btcomm;
	private final int sendInterval;
	private boolean stopped = false;

	private long lastSend = 0;

	/**
	 * Constructor
	 * 
	 * @param btcomm
	 *            bluetooth connection via which to send the data
	 * @param sendInterval
	 *            how fast the data will be send
	 */
	public AbstractSender(BTComm btcomm, int sendInterval) {
		this.btcomm = btcomm;
		this.sendInterval = sendInterval;
	}

	/**
	 * sends any data via bluetooth until it is stopped (it also controls that
	 * the data is not send faster than the sendInterval)
	 */
	@Override
	public void run() {
		try {
			long now = System.currentTimeMillis();
			while (!this.stopped && !this.btcomm.isClosed()) {
				// not sending at highspeed!
				now = System.currentTimeMillis();
				if (now - this.lastSend < this.sendInterval) {
					synchronized (this) {
						try {
							this
									.wait(this.sendInterval
											- (now - this.lastSend));
						}
						catch (InterruptedException e) {
						}
					}
				}
				this.lastSend = System.currentTimeMillis();
				this.doAction();
			}
		}
		catch (Throwable e) {
			System.out.println("AbstractSender-Exc:" + e.getClass());
			logger.error("closing abstract sender after Throwable "
					+ e.getClass());
		}
	}

	/**
	 * this method manages what it actually send in each step until the bt
	 * connection is closed or the sending is stopped
	 */
	protected abstract void doAction();

	/**
	 * stops this thread
	 */
	public void stop() {
		this.stopped = true;
	}

}
