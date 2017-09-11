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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import logging.Logger;

import sensorwrappers.color.AbstractColorSensorWrapper;
import sensorwrappers.compass.AbstractCompassSensorWrapper;


import common.FileLocalizer;
import common.MapOfLists;
import common.exceptions.QueueBlockedException;
import communication.exceptions.ConnectionClosedException;

/**
 * This class should be used to handle all bluetooth communications. It should
 * be used to connect to another device (NXT or PC) and to send content to the
 * remote device or receive content from it. There are two subclasses for either
 * a direct connection or a connection that is managed by a pc server. These two
 * can obly be instantiated by using a connection manager.
 * 
 * @author Annabelle Klarl
 */
public abstract class BTComm {

	protected static final Logger logger = Logger.getLogger();

	/*
	 * 0x0 = 0000
	 * 0x1 = 0001
	 * 0x2 = 0010
	 * 0x3 = 0011
	 * 0x4 = 0100
	 * 0x5 = 0101
	 * 0x6 = 0110
	 * 0x7 = 0111
	 * 0x8 = 1000
	 * 0x9 = 1001
	 * 0xA = 1010
	 * 0xB = 1011
	 * 0xC = 1100
	 * 0xD = 1101
	 * 0xE = 1110
	 * 0xF = 1111
	 */

	// Commands in the upper four bits
	protected static final byte REQUEST = 0x00; // 0000 0000
	protected static final byte SEND = 0x10; // 0001 0000
	protected static final byte COMMAND = 0x20; // 0010 0000

	// Commands in the lower four bits
	protected static final byte CLOSE_VIRTUAL = 0x00; // 0000 0000
	protected static final byte CLOSE_VIRTUAL_ACK = 0x01; // 0000 0001
	protected static final byte CLOSE_VIRTUAL_DECLINE = 0x02; // 0000 0010
	protected static final byte DEGREE = 0x00; // 0000 0000
	protected static final byte FILE_EMPTY = 0x01; // 0000 0001
	protected static final byte FILE_WHOLE = 0x02; // 0000 0010
	protected static final byte FILE_PART = 0x03; // 0000 0011
	protected static final byte FILE = 0x04; // 0000 0100
	protected static final byte LIGHT = 0x05; // 0000 0101

	protected static final byte UPPER_BITS = -0x10;
	protected static final byte LOWER_BITS = 0x0F;

	// number of users which use this bt connection
	private int users = 0;

	// from which and to which nxt the connection is established
	private String myName;
	protected String remoteName;
	protected String connName;

	// list for the observers of the bt connection
	private MapOfLists<BTEvent, BTCommObserver> observers = new MapOfLists<BTEvent, BTCommObserver>();

	// whether the bt connection is closed
	protected boolean closed = false;

	// the reader and writer for this connection
	protected MessageReader reader;
	protected MessageWriter writer;

	// whether to log all communication
	protected boolean logging = true;

	// sync object for the communication
	private Object sync = new Object();

	/**
	 * Constructor
	 * 
	 * @param myName
	 *            the name of this nxt
	 * @param remoteName
	 *            the name to which to send the data
	 * @param logging
	 *            whether the communication shall be logged
	 */
	protected BTComm(String myName, String remoteName, boolean logging) {
		this.myName = myName;
		this.remoteName = remoteName;
		this.connName = "to " + remoteName + ": ";

		this.logging = logging;

		this.users++;
	}

	/**
	 * adds a new user to this bt comm. That means the number of users will be
	 * increased
	 */
	public void addUser() {
		this.users++;
	}

	/**
	 * gets the number of users of this bt comm
	 * 
	 * @return the number of users of this bt comm
	 */
	public int getUsers() {
		return this.users;
	}

	/**
	 * sets the getter which gets the path for a given file name for this bt
	 * connection
	 * 
	 * @param ownFileLocalizer
	 *            a FileLocalizer that converts the filename to a path
	 */
	public void setOwnFileLocalizer(FileLocalizer ownFileLocalizer) {
		this.reader.setOwnFileLocalizer(ownFileLocalizer);
	}

	/**
	 * sets the getter which gets the degree to send for this bt connection
	 * 
	 * @param ownCompassController
	 *            a CompassController that gets the degree of this device
	 */
	public void setOwnCompassController(
			AbstractCompassSensorWrapper ownCompassController) {
		this.reader.setOwnCompassController(ownCompassController);
	}

	/**
	 * sets the getter which gets the light value to send for this bt connection
	 * 
	 * @param ownlightValueGetter
	 *            a AbstractLightValueGetter that gets the light value of this
	 *            device
	 */
	public void setOwnLightValueGetter(AbstractColorSensorWrapper ownlightValueGetter) {
		this.reader.setOwnLightValueGetter(ownlightValueGetter);
	}

	/**
	 * returns whether this connection is closed
	 * 
	 * @return whether this connection is closed
	 */
	public boolean isClosed() {
		return this.closed;
	}

	/**
	 * gets the name of the device to which this bt comm is connected
	 * 
	 * @return the name of the connected device
	 */
	public String getRemoteName() {
		return this.remoteName;
	}

	/**
	 * gets the degree that was fetched from a remote device (should only be
	 * called after a BTCommObserver was notified of the corresponding BTEvent)
	 * 
	 * @return the degree from the remote device
	 */
	public float getRemoteDegree() {
		return this.reader.getRemoteDegree();
	}

	/**
	 * gets the light value that was fetched from a remote device (should only
	 * be called after a BTCommObserver was notified of the corresponding
	 * BTEvent)
	 * 
	 * @return the light value from the remote device
	 */
	public float getRemoteLightValue() {
		return this.reader.getRemoteLightValue();
	}

	/**
	 * gets the name of the file that was fetched from a remote device (should
	 * only be called after a BTCommObserver was notified of the corresponding
	 * BTEvent)
	 * 
	 * @return the name of the file that was fetched from the remote device
	 */
	public String getRemoteFileName() {
		return this.reader.getRemoteFileName();
	}

	/**
	 * registers an observer for an event that is send via bluetooth
	 * 
	 * @param observer
	 *            the BTCommObserver to register
	 * @param event
	 *            the BTEvent to register for
	 * @throws ConnectionClosedException
	 *             thrown if the connection was already closed
	 */
	public void register(BTCommObserver observer, BTEvent event)
			throws ConnectionClosedException {
		if (this.closed) {
			throw new ConnectionClosedException(
					"Call of register after closing the connection.");
		}
		else {
			this.observers.putElement(event, observer);
		}

	}

	/**
	 * unregisters the observer for all events he is registered for
	 * 
	 * @param observer
	 *            the BTCommObserver to unregister
	 */
	public void unregister(BTCommObserver observer) {
		this.observers.removeElement(observer);
	}

	/**
	 * unregisters the observer for an event
	 * 
	 * @param observer
	 *            the BTCommObserver to unregister
	 * @param event
	 *            the BTEvent to unregister for
	 */
	public void unregister(BTCommObserver observer, BTEvent event) {
		this.observers.removeElement(event, observer);
	}

	/**
	 * notifies all BTCommObservers that are registered for this event that this
	 * event happened
	 * 
	 * @param event
	 *            the BTEvent for which to notify
	 */
	protected void notifyAllObserversForEvent(BTEvent event) {
		ArrayList<BTCommObserver> currentObservers = this.observers.get(event);
		if (currentObservers != null) {
			for (BTCommObserver commObserver : currentObservers) {
				commObserver.notify(this, event);
			}
		}
	}

	/**
	 * notifies all BTCommObservers that are registered for this event that this
	 * event happened and sends the content that was created by this event
	 * 
	 * @param event
	 *            the BTEvent for what to notify
	 * @param content
	 *            an Object with the content that was created by this event
	 */
	protected void notifyAllObserversForEvent(BTEvent event, Object content) {
		ArrayList<BTCommObserver> currentObservers = this.observers.get(event);
		if (currentObservers != null) {
			for (BTCommObserver commObserver : currentObservers) {
				commObserver.notify(this, event, content);
			}
		}
	}

	/**
	 * notifies all BTCommObservers that this connection was closed
	 */
	protected void notifyAllObserversForClose() {
		ArrayList<BTEvent> keys = this.observers.keys();
		for (BTEvent event : keys) {
			ArrayList<BTCommObserver> currentObservers = this.observers
					.get(event);
			if (currentObservers != null) {
				for (BTCommObserver commObserver : currentObservers) {
					commObserver.notifyForClose(this);
				}
			}
		}
	}

	/**
	 * see documentation {@link BTWriter#writeFile(String)}
	 * 
	 * @param fileName
	 *            a String with the name of the file to send
	 * @throws FileNotFoundException
	 *             thrown if the file could not be found
	 * @throws IOException
	 *             thrown if something couldn't be written to output
	 * @throws ConnectionClosedException
	 *             thrown if this method was called when connection has already
	 *             been closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void writeFile(String fileName) throws FileNotFoundException,
			IOException, ConnectionClosedException, QueueBlockedException {
		synchronized (this.sync) {
			if (this.closed) {
				throw new ConnectionClosedException(
						"Call of write file after closing the connection.");
			}
			else {
				this.writer.writeFile(fileName);
				if (this.logging) {
					logger.debug(this.connName + "file " + fileName + " sent");
				}
			}
		}
	}

	/**
	 * see documentation {@link BTWriter#writeDegree(float)}
	 * 
	 * @param degree
	 *            a float for the degree to write to the remote device
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void writeDegree(float degree) throws ConnectionClosedException,
			QueueBlockedException {
		synchronized (this.sync) {
			if (this.closed) {
				throw new ConnectionClosedException(
						"Call of write degree after closing the connection.");
			}
			else {
				this.writer.writeDegree(degree);
				if (this.logging) {
					logger.debug(this.connName + "degree " + degree + " sent");
				}
			}
		}
	}

	/**
	 * see documentation {@link BTWriter#writeLightValue(float)}
	 * 
	 * @param lightValue
	 *            a float for the light value to write to the remote device
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void writeLightValue(float lightValue)
			throws ConnectionClosedException, QueueBlockedException {
		synchronized (this.sync) {
			if (this.closed) {
				throw new ConnectionClosedException(
						"Call of write light value after closing the connection.");
			}
			else {
				this.writer.writeLightValue(lightValue);
				if (this.logging) {
					logger.debug(this.connName + "lightvalue " + lightValue
							+ " sent");
				}
			}
		}
	}

	/**
	 * see documentation {@link BTComm#writeCommand(byte, boolean)}. Here the
	 * message queue will not be blocked afterwards.
	 * 
	 * @param command
	 *            a byte with the command to write to the remote device
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void writeCommand(byte command) throws ConnectionClosedException,
			QueueBlockedException {
		this.writeCommand(command, false);
	}

	/**
	 * see documentation {@link BTWriter#writeCommand(byte)}
	 * 
	 * @param command
	 *            a byte with the command to write to the remote device
	 * @param blocked
	 *            wether the writer thread shall be blocked after writing this
	 *            command
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void writeCommand(byte command, boolean blocked)
			throws ConnectionClosedException, QueueBlockedException {
		synchronized (this.sync) {
			if (this.closed) {
				throw new ConnectionClosedException(
						"Call of write command after closing the connection.");
			}
			else {
				this.writer.writeCommand(command, blocked);
				if (this.logging) {
					logger.debug(this.connName + "command "
							+ Byte.toString(command) + " sent");
				}
			}
		}
	}

	/**
	 * requests to get a file with the given fileName from the remote device.
	 * This method will also register the BTCommObserver for the receiving of
	 * this file.
	 * 
	 * @param fileName
	 *            a String with the name of file to get from the remote device
	 *            and also the location where to store the received file
	 * @param observer
	 *            the BTCommObserver to register for the receiving of this file
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void requestFile(String fileName, BTCommObserver observer)
			throws ConnectionClosedException, QueueBlockedException {
		if (this.closed) {
			throw new ConnectionClosedException(
					"Call of request file after closing the connection.");
		}
		else {
			// registers the given observer for the receiving of this file
			this.register(observer, BTEvent.FILE);

			this.requestFile(fileName);
		}
	}

	/**
	 * see documentation {@link BTWriter#requestFile(String)}
	 * 
	 * @param fileName
	 *            a String with the name of file to get from the remote device
	 *            and also the location where to store the received file
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void requestFile(String fileName) throws ConnectionClosedException,
			QueueBlockedException {
		synchronized (this.sync) {
			if (this.closed) {
				throw new ConnectionClosedException(
						"Call of request file after closing the connection.");
			}
			else {
				this.writer.requestFile(fileName);
				if (this.logging) {
					logger.debug(this.connName + "file " + fileName + " req");
				}
			}
		}
	}

	/**
	 * requests to get the degree of the compass sensor from the remote device.
	 * This method will also register the BTCommObserver for the receiving of
	 * the degree.
	 * 
	 * @param observer
	 *            the BTCommObserver to register for the receiving of the degree
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void requestDegree(BTCommObserver observer)
			throws ConnectionClosedException, QueueBlockedException {
		if (this.closed) {
			throw new ConnectionClosedException(
					"Call of request degree after closing the connection.");
		}
		else {
			// registers the given observer for the receiving of this file
			this.register(observer, BTEvent.DEGREE);

			this.requestDegree();
		}
	}

	/**
	 * see documentation {@link BTWriter#requestDegree()}
	 * 
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void requestDegree() throws ConnectionClosedException,
			QueueBlockedException {
		synchronized (this.sync) {
			if (this.closed) {
				throw new ConnectionClosedException(
						"Call of request degree after closing the connection.");
			}
			else {
				this.writer.requestDegree();
				if (this.logging) {
					logger.debug(this.connName + "degree req");
				}
			}
		}
	}

	/**
	 * requests to get the light value of the light sensor from the remote
	 * device. This method will also register the BTCommObserver for the
	 * receiving of the light value.
	 * 
	 * @param observer
	 *            the BTCommObserver to register for the receiving of the light
	 *            value
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void requestLightValue(BTCommObserver observer)
			throws ConnectionClosedException, QueueBlockedException {
		if (this.closed) {
			throw new ConnectionClosedException(
					"Call of request light value after closing the connection.");
		}
		else {
			// registers the given observer for the receiving of this file
			this.register(observer, BTEvent.LIGHT);

			this.requestLightValue();
		}
	}

	/**
	 * see documentation {@link BTWriter#requestLightValue()}
	 * 
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void requestLightValue() throws ConnectionClosedException,
			QueueBlockedException {
		synchronized (this.sync) {
			if (this.closed) {
				throw new ConnectionClosedException(
						"Call of request light value after closing the connection.");
			}
			else {
				this.writer.requestLightValue();
				if (this.logging) {
					logger.debug(this.connName + "lightvalue req");
				}
			}
		}
	}

	/**
	 * requests to close the BT connection and registers the BTCommObserver for
	 * the event if the request was declined. This method will block the writer
	 * thread for any other writings.
	 * 
	 * @param observer
	 *            the BTCommObserver to register for the receiving of the
	 *            declination of the request for close
	 * @throws ConnectionClosedException
	 *             thrown if this method was called if connection is closed
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked and nothing can be
	 *             written to it at the moment
	 */
	public void requestClose(BTCommObserver observer)
			throws ConnectionClosedException, QueueBlockedException {
		synchronized (this.sync) {
			if (this.closed) {
				throw new ConnectionClosedException(
						"Call of request close after closing the connection.");
			}
			else if (this.users > 1) {
				this.users--;
				observer.notifyForClose(this);
			}
			else {
				this.register(observer, BTEvent.CLOSE_REQUEST_DECLINED);
				this.reader.setCloseRequested(true);
				this.writer.requestClose();
				if (this.logging) {
					logger.info(this.connName + "close req");
				}
			}
		}
	}

	/**
	 * sets the connection to closed
	 */
	protected void close() {
		this.closed = true;
	}

	/**
	 * resets all that was made upon ACK to a close command
	 */
	protected void closeNotSuccessful() {
		this.writer.deblockQueue();
	}

}
