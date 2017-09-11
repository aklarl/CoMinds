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

import sensorwrappers.exceptions.SensorException;
import communication.BTComm;
import communication.BTCommObserver;
import communication.BTEvent;
import communication.NXTConnectionManager;
import communication.exceptions.ConnectionClosedException;
import communication.exceptions.ConnectionException;
import communication.exceptions.ManagerException;

/**
 * This class wraps a remote compass sensor. Either it connects to a remote
 * device via name or waits for any inbound connection. It registers itself as
 * an observer for the bluetooth connection to the remote device. Whenever the
 * method getDegree is called, it will have look whether there is new compass
 * data available. If it is, it will fetch the new data and return the new data.
 * If it is not, it will simply return the old compass data. This class does NOT
 * ask for new compass data from the remote device, it simply waits until new
 * data is send.
 * 
 * @author Annabelle Klarl
 */
public class RemoteCompassSensorWrapper extends BTCommObserver implements
		AbstractCompassSensorWrapper {

	protected BTComm btcomm;
	protected float degree = -1;
	protected boolean isDegreeNew = false;

	private NXTConnectionManager manager;
	private static final int QUEUESIZE = 500;

	/**
	 * Constructor (this will connect directly to the remote device)
	 * 
	 * @param timeout
	 *            the time to wait for a connection
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public RemoteCompassSensorWrapper(int timeout) throws ManagerException {
		this(true, "", timeout);
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
	public RemoteCompassSensorWrapper(boolean directConnection, String remoteName,
			int timeout) throws ManagerException {
		try {
			if (directConnection) {
				this.manager = NXTConnectionManager.getManager();
				this.btcomm = this.manager.getDirectConnection(timeout, 500,
						true);
			}
			else {
				this.manager = NXTConnectionManager.getManager(QUEUESIZE);
				this.btcomm = this.manager.getManagedConnection(timeout,
						remoteName, true);

			}
			try {
				this.btcomm.register(this, BTEvent.DEGREE);
			}
			catch (ConnectionClosedException e) {
				System.out.println("RemoteCompass:remote close");
				logger.info("register: conn closed from remote");
			}
		}
		catch (ConnectionException e) {
			// should not happen
			logger.error("RemoteCompassController throws ConnectionException");
			System.out.println("RemoteCompass:no conn");
		}
		catch (ConnectionClosedException e) {
			// should not happen
			logger
					.error("RemoteCompassController throws ConnectionClosedException");
			System.out.println("RemoteCompass:manager closed");
		}
	}

	/**
	 * Constructor
	 * 
	 * @param directConnection
	 *            whether to connect directly or via a managed connection
	 * @param remoteName
	 *            the name of the nxt where to get the compass data from
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public RemoteCompassSensorWrapper(boolean directConnection, String remoteName)
			throws ManagerException {
		try {
			if (directConnection) {
				this.manager = NXTConnectionManager.getManager();
			}
			else {
				this.manager = NXTConnectionManager.getManager(QUEUESIZE);

			}

			this.btcomm = this.manager.getConnection(remoteName,
					directConnection, true);

			try {
				this.btcomm.register(this, BTEvent.DEGREE);
			}
			catch (ConnectionClosedException e) {
				System.out.println("RemoteCompass:remote close");
				logger.info("register: conn closed from remote");
			}
		}
		catch (ConnectionException e) {
			// should not happen
			logger.error("RemoteCompassController throws ConnectionException");
			System.out.println("RemoteCompass:no conn");
		}
		catch (ConnectionClosedException e) {
			// should not happen
			logger
					.error("RemoteCompassController throws ConnectionClosedException");
			System.out.println("RemoteCompass:manager closed");
		}
	}

	@Override
	public synchronized float getDegree() throws SensorException {
		if (this.isDegreeNew) {
			this.degree = this.btcomm.getRemoteDegree();
			this.isDegreeNew = false;
		}

		if (this.degree < 0) {
			throw new SensorException("remote CompassSensor not yet available");
		}
		if (this.degree > 360) {
			throw new SensorException("Degree from remote bigger than 360");
		}

		return this.degree;
	}

	@Override
	public void notify(BTComm btcomm, BTEvent event) {
		super.notify(btcomm, event);
		switch (event) {
		case DEGREE:
			synchronized (btcomm) {
				this.isDegreeNew = true;
				btcomm.notify();
			}
		}

	}

	/**
	 * requests to close the bt connection to the remote compass sensor
	 */
	public void stop() {
		this.close(this.btcomm);
	}

}
