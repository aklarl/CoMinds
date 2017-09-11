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
package sensorwrappers.color;

import sensorwrappers.exceptions.RemoteSensorException;
import sensorwrappers.exceptions.SensorException;

import common.exceptions.QueueBlockedException;
import communication.exceptions.ConnectionClosedException;
import communication.exceptions.ManagerException;

/**
 * This class extends a remote light value controller by explicitly requesting
 * the light value from the remote device. It will request new data only every
 * {@link #REQUEST_INTERVAL} ms.
 * 
 * @author Annabelle Klarl
 */
public class PollingRemoteColorSensorWrapper extends RemoteColorSensorWrapper {

	private long lastRequest = 0;
	private static final int REQUEST_INTERVAL = 50;
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
	public PollingRemoteColorSensorWrapper(int timeout) throws ManagerException {
		super(timeout);
	}

	/**
	 * Constructor
	 * 
	 * @param directConnection
	 *            whether to connect directly or via a managed connection
	 * @param remoteName
	 *            the name of the nxt where to get the light value from
	 * @param timeout
	 *            the time to wait for a connection
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public PollingRemoteColorSensorWrapper(boolean directConnection,
			String remoteName, int timeout) throws ManagerException {
		super(directConnection, remoteName, timeout);
	}

	/**
	 * Constructor
	 * 
	 * @param directConnection
	 *            whether to connect directly or via a managed connection
	 * @param remoteName
	 *            the name of the nxt where to get the light value from
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public PollingRemoteColorSensorWrapper(boolean directConnection, String remoteName)
			throws ManagerException {
		super(directConnection, remoteName);
	}

	@Override
	public synchronized float getLightValue() throws SensorException,
			RemoteSensorException {
		try {
			long now = System.currentTimeMillis();
			if (!this.btcomm.isClosed()
					&& now - this.lastRequest > REQUEST_INTERVAL) {
				this.lastRequest = now;
				this.btcomm.requestLightValue();

				synchronized (this.btcomm) {
					while (!this.btcomm.isClosed()
							&& !this.isLightValueNew
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
			logger.error("cannot req lightvalue because conn closed");
		}
		catch (QueueBlockedException e1) {
			// no request possible
			logger.error("cannot req lightvalue because queue blocked");
		}

		if (this.isLightValueNew) {
			return super.getLightValue();
		}
		else {
			throw new RemoteSensorException(
					"no data could be retrieved from the remote device");
		}
	}
}
