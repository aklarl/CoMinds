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

import java.io.DataOutputStream;

import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

import communication.extendedClasses.ExtendedDataInputStream;

/**
 * This is a class for an unmanaged bt connection. The connectoin will be
 * direct.
 * 
 * @author Annabelle Klarl
 */
public class BTCommUnmanaged extends BTComm {

	// for inbound connections
	private int timeout = -1;

	// bt connection
	private BTConnection btc;
	private ExtendedDataInputStream dis;
	private DataOutputStream dos;

	// size of the message queue to write to bt
	private int queueSize;

	/**
	 * Constructor which connects to the nxt given by nxtName (all communication
	 * will be logged). The queue size will be 1.
	 * 
	 * @param remoteName
	 *            a String to which nxt to connect
	 */
	protected BTCommUnmanaged(String myName, String remoteName) {
		this(myName, remoteName, 1, true);
	}

	/**
	 * Constructor which connects to the nxt given by nxtName
	 * 
	 * @param remoteName
	 *            a String to which nxt to connect
	 * @param queueSize
	 *            an int for the queue size for the writer thread
	 * @param logging
	 *            whether all communication shall be logged to a log file
	 */
	protected BTCommUnmanaged(String myName, String remoteName, int queueSize,
			boolean logging) {
		super(myName, remoteName, logging);

		this.queueSize = queueSize;

		if (this.connect()) {
			this.initReader(logging);
			this.initWriter(this.queueSize);
		}
		else {
			this.closed = true;
		}
	}

	/**
	 * Constructor which simply waits for a connection (all communication will
	 * be logged). The queue size will be 1.
	 * 
	 * @param timeOut
	 *            an int how long to wait
	 */
	protected BTCommUnmanaged(String myName, int timeOut) {
		this(myName, timeOut, 1, true);
	}

	/**
	 * Constructor which simply waits for an connection
	 * 
	 * @param timeOut
	 *            an int how long to wait
	 * @param queueSize
	 *            an int for the queue size for the writer thread
	 * @param logging
	 *            whether all communication shall be logged
	 */
	protected BTCommUnmanaged(String myName, int timeOut, int queueSize,
			boolean logging) {
		super(myName, "unknown", logging);
		this.timeout = timeOut;
		this.queueSize = queueSize;

		if (this.waitForConnection()) {
			this.initReader(logging);
			this.initWriter(this.queueSize);
		}
		else {
			this.closed = true;
		}
	}

	/**
	 * connects to the a given nxt (via the private variable nxtName) and opens
	 * input and output streams for the connection
	 * 
	 * @return returns whether a connection could be found or not
	 */
	private boolean connect() {
		System.out.println("conn to " + this.remoteName);
		if (this.logging) {
			logger.debug("connecting to " + this.remoteName);
		}

		this.btc = Bluetooth.connect(this.remoteName, NXTConnection.PACKET);

		if (this.btc != null) {

			this.dis = new ExtendedDataInputStream(this.btc
					.openDataInputStream());
			this.dos = this.btc.openDataOutputStream();

			if (this.logging) {
				logger.info("conn to " + this.remoteName);
			}
			System.out.println("conn to " + this.remoteName);

			return true;
		}
		else {
			if (this.logging) {
				logger.info("not conn to " + this.remoteName);
			}
			System.out.println("not conn");
			return false;
		}
	}

	/**
	 * waits for any connection until a timeout is reached (via the private
	 * variable timeout)
	 * 
	 * @return returns whether a connection could be found or not
	 */
	private boolean waitForConnection() {
		System.out.println("waiting for conn");
		if (this.logging) {
			logger.debug("waiting for conn");
		}

		this.btc = Bluetooth.waitForConnection(this.timeout,
				NXTConnection.PACKET);

		if (this.btc != null) {
			this.dis = new ExtendedDataInputStream(this.btc
					.openDataInputStream());
			this.dos = this.btc.openDataOutputStream();

			if (this.logging) {
				logger.info("conn");
			}
			System.out.println("conn");

			return true;
		}
		else {
			if (this.logging) {
				logger.info("no conn or too many inbound conns");
			}
			System.out.println("no conn");
			return false;
		}
	}

	/**
	 * initializes the Reader Thread
	 * 
	 * @param logging
	 *            whether the communication shall be logged to a log file
	 */
	private void initReader(boolean logging) {
		this.reader = new MessageReader(this.connName, this.dis, this, false,
				logging);
		new Thread(this.reader, "BTReaderThread").start();
	}

	/**
	 * initializes the Writer Thread
	 * 
	 * @param queueSize
	 *            an in for the size for the message buffer of the writer thread
	 */
	private void initWriter(int queueSize) {
		this.writer = new MessageWriter(this.dos, queueSize);
		this.writer.start();
	}

	/**
	 * sets the connection to closed
	 */
	@Override
	protected void close() {
		this.writer.stop();
		synchronized (this.writer.writer) {
			while (!this.writer.isFinished()) {
				try {
					this.writer.writer.wait();
				}
				catch (InterruptedException e) {
				}
			}
		}
		this.btc.close();
		super.close();
	}

}
