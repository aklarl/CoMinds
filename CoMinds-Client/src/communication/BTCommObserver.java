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
package communication;

import common.exceptions.QueueBlockedException;
import communication.exceptions.ConnectionClosedException;

/**
 * This is an observer of a BT connection. The observer will be notified if an
 * event for which it is registerd occurs and if the connection will be closed.
 * 
 * @author Annabelle Klarl
 */
public class BTCommObserver {

	private static final int WAIT_INTERVAL = 10000;
	private boolean closeRequested = false;

	/**
	 * notifies the observer that an event occurred for which he registered
	 * 
	 * @param btcomm
	 *            the bluetooth connection that sends the event
	 * @param event
	 *            the BTEvent that occurred
	 */
	protected void notify(BTComm btcomm, BTEvent event) {
		switch (event) {
		case CLOSE_REQUEST_DECLINED:
			synchronized (btcomm) {
				btcomm.notify();
			}
		}

	}

	/**
	 * notifies the observer that an event occurred for which he registered and
	 * sends the content of this event
	 * 
	 * @param btcomm
	 *            the bluetooth connection that sends the event
	 * @param event
	 *            the BTEvent that occurred
	 * @param content
	 *            the Object with the content of this event
	 */
	protected void notify(BTComm btcomm, BTEvent event, Object content) {
		this.notify(btcomm, event);
	}

	/**
	 * notifies the observer that the bt connection will be closed.
	 * 
	 * @param btcomm
	 *            the bluetooth connection that is closed
	 */
	protected void notifyForClose(BTComm btcomm) {
		synchronized (btcomm) {
			btcomm.notify();
		}
	}

	/**
	 * whether the a request for close was send
	 * 
	 * @return whether a request was send
	 */
	protected boolean isCloseRequested() {
		return this.closeRequested;
	}

	/**
	 * closes the connection to the given bluetooth connection
	 * 
	 * @param btcomm
	 *            the bluetooth connection to close
	 */
	protected void close(BTComm btcomm) {
		// try to close the bt communication if it is not already closed
		try {
			long now = System.currentTimeMillis();
			int usersBefore = btcomm.getUsers();
			while (!btcomm.isClosed() && usersBefore >= btcomm.getUsers()
					&& System.currentTimeMillis() - now < WAIT_INTERVAL) {
				try {
					this.closeRequested = true;
					btcomm.requestClose(this);
				}
				catch (QueueBlockedException e) {
					// observers has been notified for an event that wasn't
					// close ACK and didn't deblock the bt queue, so just
					// simply wait until next event
				}
				synchronized (btcomm) {
					try {
						// wait until observer is notified (then test in
						// while-loop if the event was a close event)
						if (!btcomm.isClosed()
								&& usersBefore >= btcomm.getUsers()) {
							btcomm.wait(WAIT_INTERVAL);
						}
					}
					catch (InterruptedException e) {
					}
				}
			}
		}
		catch (ConnectionClosedException e) {
			// ok
		}
	}

}
