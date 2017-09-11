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
package robots.contest;

import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CompassSensor;
import logging.Logger;
import logging.Logger.LogLevel;
import observers.ButtonObserver;
import robots.abstractRobots.AbstractController;
import sensorwrappers.compass.AbstractCompassSensorWrapper;
import sensorwrappers.compass.OwnCompassSensorWrapper;

import communication.BTComm;
import communication.exceptions.ManagerException;

import datasender.AbstractSender;
import datasender.DegreeSender;

/**
 * This class is written for a compass controller. It only needs the brick and a
 * compass sensor. This programm will connect to one inbound connection and will
 * wait for any request from the remote device. It will also connect to the some
 * nxts given by name and will send its compass data to it until the connection
 * is closed or ESCAPE is pressed. The whole controller will stop after pressing
 * ESCAPE.
 * 
 * @author Annabelle Klarl
 */
public class Controller extends AbstractController {

	protected static final Logger logger = Logger.getLogger(
			"log_controller.txt", LogLevel.DEBUG, 500, true);

	public static final int SEND_INTERVAL = 50;
	public static final SensorPort COMPASS_SENSOR = SensorPort.S3;
	private DegreeSender[] senders;
	private final AbstractCompassSensorWrapper ownCompassController;

	/**
	 * Constructor which initializes the compass sensor (only direct connections
	 * to the nxts are possible)
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
	public Controller(int timeout, String... nxtNames) throws ManagerException {
		super(timeout, nxtNames);

		// initialize own compass sensor
		this.ownCompassController = new OwnCompassSensorWrapper(
				new CompassSensor(COMPASS_SENSOR));
	}

	/**
	 * Constructor which initializes the compass sensor
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
	public Controller(boolean directConnection, String... nxtNames)
			throws ManagerException {
		super(directConnection, nxtNames);

		// initialize own compass sensor
		this.ownCompassController = new OwnCompassSensorWrapper(
				new CompassSensor(COMPASS_SENSOR));
	}

	/**
	 * initializes that each bt connection can send the compass data
	 */
	@Override
	protected void initResponse() {
		if (this.inboundComm != null) {
			this.inboundComm.setOwnCompassController(this.ownCompassController);
		}
		for (BTComm outbound : this.outboundComm) {
			if (outbound != null) {
				outbound.setOwnCompassController(this.ownCompassController);
			}
		}
	}

	/**
	 * initializes the sending of the own degree to the given bt connections
	 * 
	 * @param sendInterval
	 *            how fast the data will be send
	 * @param btcomms
	 *            the bt connection where to send the degrees (by name)
	 * @return returns the initiatialized senders
	 */
	@Override
	protected AbstractSender[] initSending(int sendInterval, String... btcomms) {
		// final DegreeSender[] senders = new DegreeSender[btcomms.length];
		this.senders = new DegreeSender[btcomms.length];

		for (int i = 0; i < btcomms.length; i++) {
			for (BTComm outboundComm : this.outboundComm) {
				if (outboundComm != null
						&& outboundComm.getRemoteName().equals(btcomms[i])) {
					DegreeSender sender = new DegreeSender(outboundComm,
							this.ownCompassController, sendInterval);
					this.senders[i] = sender;
					// sender.start();
				}
			}
		}
		return this.senders;
	}

	@Override
	protected void startBehavior() {
		ButtonObserver observer = new ButtonObserver() {
			@Override
			public void notifyForEvent(Button button) {
				if (button.equals(Button.ENTER)) {
					synchronized (Controller.this) {
						Controller.this.notify();
					}
				}
			}
		};
		this.monitor.register(observer, Button.ENTER);

		synchronized (this) {
			try {
				this.wait();
			}
			catch (InterruptedException e) {
			}
		}

		this.monitor.unregister(observer, Button.ENTER);

		// start sending
		for (DegreeSender sender : this.senders) {
			sender.start();
		}

		super.startBehavior();
	}

	public static void main(String[] args) {
		try {
			main(new Controller(true, "Johnny"), SEND_INTERVAL, "Johnny");
		}
		catch (ManagerException e) {
			// should not happen here because only the AbstractController inits
			// a NXTConnectionManager
		}
	}

}
