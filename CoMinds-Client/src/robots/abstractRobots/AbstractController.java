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
package robots.abstractRobots;

import observers.ButtonMonitor;
import observers.ButtonObserver;
import lejos.nxt.Button;
import logging.Logger;
import logging.Logger.LogLevel;

import communication.BTComm;
import communication.BTCommObserver;
import communication.BTCommUnmanaged;
import communication.BTEvent;
import communication.NXTConnectionManager;
import communication.exceptions.ConnectionClosedException;
import communication.exceptions.ConnectionException;
import communication.exceptions.ManagerException;
import communication.exceptions.NoDirectConnectionsException;
import datasender.AbstractSender;

/**
 * This class is written for an abstract controller. This programm will connect
 * to one inbound connection and will wait for any request from the remote
 * device. It will also connect to the different nxts given by name. After that
 * one can initialize the sending of some data to the connected devices via the
 * method {@link #initSending(int, String...)}. It will send the data to the
 * remote devices until ESCAPE is pressed or the connection is closed. The whole
 * controller will stop after pressing ESCAPE.
 * 
 * @author Annabelle Klarl
 */
public abstract class AbstractController extends BTCommObserver {

	protected static final Logger logger = Logger.getLogger(
			"log_controller.txt", LogLevel.DEBUG, 500, true);

	protected final NXTConnectionManager manager;
	private static final int QUEUESIZE = 500;

	protected final ButtonMonitor monitor = new ButtonMonitor();
	protected BTCommUnmanaged inboundComm;
	protected BTComm[] outboundComm = new BTCommUnmanaged[3];

	/**
	 * Constructor which initializes the compass sensor (this will only get
	 * connections that are direct)
	 * 
	 * @param timeout
	 *            the timeout for an inbound connection (if -1 no inbound
	 *            connection will be initialized)
	 * @param nxtNames
	 *            the names of the nxts to connect to
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections
	 */
	public AbstractController(int timeout, String... nxtNames)
			throws ManagerException {
		logger.info("controller start");
		System.out.println("control start");

		this.manager = NXTConnectionManager.getManager();
		this.monitor.start();

		if (timeout == -1) {
			this.initBTComms(true, nxtNames);
		}
		else {
			this.initBTComms(timeout, nxtNames);
		}
	}

	/**
	 * Constructor which initializes the compass sensor (can connect directly or
	 * indirectly to the nxts)
	 * 
	 * @param directConnection
	 *            whether to connect directly or by managed connections
	 * @param nxtNames
	 *            the names of the nxts to connect to
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public AbstractController(boolean directConnection, String... nxtNames)
			throws ManagerException {
		logger.info("controller start");
		System.out.println("control start");

		this.monitor.start();

		if (directConnection) {
			this.manager = NXTConnectionManager.getManager();
		}
		else {
			this.manager = NXTConnectionManager.getManager(QUEUESIZE);
		}
		this.initBTComms(directConnection, nxtNames);
	}

	/**
	 * initializes one inbound communication with timeout and up to three
	 * outbound connections to the given nxtNames
	 * 
	 * @param timeout
	 *            timeout for the inbound communication
	 * @param nxtNames
	 *            to which nxts to connect
	 */
	private void initBTComms(int timeout, String... nxtNames) {
		// initialize inbound communication
		try {
			this.inboundComm = this.manager.getDirectConnection(timeout, 1,
					true);
			// initialize observing of the bt communication
			try {
				this.inboundComm.register(this, BTEvent.CLOSE);
			}
			catch (ConnectionClosedException e) {
				System.out.println("AbstractController:remote close");
				logger.info("register: conn closed from remote");
			}
		}
		catch (NoDirectConnectionsException e) {
			// should not happen
			logger
					.error("AbstractController throws NoDirectConnectionsException");
			System.out.println("AbstractController:no direct conn");
		}
		catch (ConnectionClosedException e) {
			// should not happen
			logger.error("AbstractController throws ConnectionClosedException");
			System.out.println("AbstractController:manager closed");
		}

		// initialize outbound communications
		for (int i = 0; i < nxtNames.length && i < 3; i++) {
			try {
				this.outboundComm[i] = this.manager.getDirectConnection(
						nxtNames[i], 1, true);

				// initialize observing of the bt communication
				try {
					this.outboundComm[i].register(this, BTEvent.CLOSE);
				}
				catch (ConnectionClosedException e2) {
					System.out.println("AbstractController:remote close");
					logger.info("register: conn closed from remote");
				}
			}
			catch (NoDirectConnectionsException e) {
				// should not happen
				logger
						.error("AbstractController throws NoDirectConnectionsException");
				System.out.println("AbstractController:no direct conn");
			}
			catch (ConnectionClosedException e) {
				// should not happen
				logger
						.error("AbstractController throws ConnectionClosedException");
				System.out.println("AbstractController:manager closed");
			}
		}
	}

	/**
	 * see documenation {@link #initBTComms(int, String...)}. This will not init
	 * an inbound connection. Depending on the parameter directConnection this
	 * will create direct or managed connections.
	 * 
	 * @param directConnection
	 *            whether to create direct or managed connections
	 */
	private void initBTComms(boolean directConnection, String... nxtNames) {
		// initialize outbound communications
		try {
			for (int i = 0; i < nxtNames.length; i++) {
				if (directConnection) {
					this.outboundComm[i] = this.manager.getDirectConnection(
							nxtNames[i], 1, true);
				}
				else {
					this.outboundComm[i] = this.manager.getManagedConnection(
							nxtNames[i], true);
				}

				// initialize observing of the bt communication
				try {
					this.outboundComm[i].register(this, BTEvent.CLOSE);
				}
				catch (ConnectionClosedException e) {
					System.out.println("AbstractController:remote close");
					logger.info("register: conn closed from remote");
				}
			}
		}
		catch (ConnectionException e) {
			// should not happen
			logger.error("AbstractController throws ConnectionException");
			System.out.println("AbstractController:no conn");
		}
		catch (ConnectionClosedException e) {
			// should not happen
			logger.error("AbstractController throws ConnectionClosedException");
			System.out.println("AbstractController:manager closed");
		}
	}

	/**
	 * this method should initialize that the controller can answer any request
	 * for data via bt. Therefore, you must set what to send via the bt
	 * connection (also if the controller does not send something by himself,
	 * but after request)
	 */
	protected abstract void initResponse();

	/**
	 * this method should initialize the sending of this controller. Therefore,
	 * you must init the sending of the data the controller should send it by
	 * itself
	 * 
	 * @param sendInterval
	 *            how fast the data will be send
	 * @param btcomms
	 *            the bt connection where to send the degrees (by name)
	 * @return returns the initiatialized senders or null if no senders where
	 *         initialized
	 */
	protected abstract AbstractSender[] initSending(int sendInterval,
			String... btcomms);

	/**
	 * adds for each sender a stop listener so that the sending is stopped if
	 * the ESCAPE button is pressed
	 * 
	 * @param senders
	 *            the senders to stop with ESCAPE
	 */
	private void addStopListener(final AbstractSender[] senders) {
		if (senders != null) {
			this.monitor.register(new ButtonObserver() {
				@Override
				public void notifyForEvent(Button button) {
					if (button.equals(Button.ESCAPE)) {
						for (AbstractSender degreeSender : senders) {
							if (degreeSender != null) {
								degreeSender.stop();
							}
						}
					}
				}
			}, Button.ESCAPE);
		}
	}

	/**
	 * stops any bluetooth communication and the logging
	 */
	protected void stop() {
		try {
			// close all connections
			NXTConnectionManager.closeManager();

			// stop logging
			logger.stopLogging();

		}
		catch (Throwable e) {
			System.out.println("AbstractController-Exc " + e.getClass());
		}

		Button.waitForPress();
	}

	/**
	 * starts the behavior of this controller (here: just waiting)
	 */
	protected void startBehavior() {
		this.monitor.register(new ButtonObserver() {
			@Override
			public void notifyForEvent(Button button) {
				if (button.equals(Button.ESCAPE)) {
					synchronized (AbstractController.this) {
						AbstractController.this.notify();
					}
				}
			}
		}, Button.ESCAPE);

		synchronized (this) {
			try {
				this.wait();
			}
			catch (InterruptedException e) {
			}
		}
	}

	/**
	 * initializes all bt connections and the degree sending for the specified
	 * connections
	 * 
	 * @param controller
	 * @param sendInterval
	 *            how fast the data will be send
	 * @param nxtToSendTo
	 *            to which nxts to send the compass data (pushing the data) or
	 *            null if it should not be send to any nxt
	 */
	public static void main(AbstractController controller, int sendInterval,
			String... nxtToSendTo) {
		try {
			controller.initResponse();
			if (nxtToSendTo != null) {
				AbstractSender[] senders = controller.initSending(sendInterval,
						nxtToSendTo);
				controller.addStopListener(senders);
			}
			controller.startBehavior();
		}
		catch (Throwable e) {
			System.out.println("AbstractController-Exc " + e.getClass());
		}
		finally {
			// stopping connection and logging
			System.out.println("stopping...");
			controller.stop();
		}
	}

	/**
	 * initializes all bt connections
	 * 
	 * @param controller
	 */
	public static void main(AbstractController controller) {
		main(controller, 0, (String) null);
	}
}
