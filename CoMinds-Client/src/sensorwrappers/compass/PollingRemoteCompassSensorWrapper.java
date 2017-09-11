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
package sensorwrappers.compass;

import sensorwrappers.exceptions.RemoteSensorException;
import sensorwrappers.exceptions.SensorException;

import common.exceptions.QueueBlockedException;
import communication.exceptions.ConnectionClosedException;
import communication.exceptions.ManagerException;

/**
 * This class extends a remote compass controller by explicitly requesting the
 * compass data from the remote device. It will request new data only every
 * 500ms
 * 
 * @author Annabelle Klarl
 */
public class PollingRemoteCompassSensorWrapper extends
		RemoteCompassSensorWrapper {

	private long lastRequest = 0;
	private static final int REQUEST_INTERVAL = 500;
	private static final int WAIT_INTERVAL = 2000;

	/**
	 * Constructor
	 * 
	 * @param timeout
	 *            the time to wait for a connection
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections
	 */
	public PollingRemoteCompassSensorWrapper(int timeout)
			throws ManagerException {
		super(timeout);
	}

	/**
	 * Constructor
	 * 
	 * @param directConnection
	 *            whether to connect directly or via a managed connection
	 * @param remoteName
	 *            the name of the remote device
	 * @param timeout
	 *            the time to wait for a connection
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public PollingRemoteCompassSensorWrapper(boolean directConnection,
			String remoteName, int timeout) throws ManagerException {
		super(directConnection, remoteName, timeout);
	}

	/**
	 * Constructor
	 * 
	 * @param directConnection
	 *            whether to connect directly or via a managed connection
	 * @param nxtName
	 *            the name of the nxt where to get the compass data from
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public PollingRemoteCompassSensorWrapper(boolean directConnection,
			String nxtName) throws ManagerException {
		super(directConnection, nxtName);
	}

	@Override
	public synchronized float getDegree() throws SensorException,
			RemoteSensorException {
		try {
			long now = System.currentTimeMillis();
			if (!this.btcomm.isClosed()
					&& now - this.lastRequest > REQUEST_INTERVAL) {
				this.lastRequest = now;
				this.btcomm.requestDegree();
				synchronized (this.btcomm) {
					while (!this.btcomm.isClosed()
							&& !this.isDegreeNew
							&& !(System.currentTimeMillis() - this.lastRequest >= WAIT_INTERVAL)) {
						try {
							this.btcomm.wait(WAIT_INTERVAL);
						}
						catch (InterruptedException e) {
						}
					}
				}

				this.lastRequest = System.currentTimeMillis();
			}
		}
		catch (ConnectionClosedException e1) {
			// no data do get
			logger.error("cannot req deg because conn closed");
		}
		catch (QueueBlockedException e1) {
			// no request possible
			logger.error("cannot req deg because queue blocked");
		}

		if (this.isDegreeNew) {
			return super.getDegree();
		}
		else {
			throw new RemoteSensorException(
					"No data could be retrieved from the remote device");
		}
	}
}
