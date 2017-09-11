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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import logging.Logger;
import sensorwrappers.color.AbstractColorSensorWrapper;
import sensorwrappers.compass.AbstractCompassSensorWrapper;

import common.FileLocalizer;
import common.exceptions.QueueBlockedException;
import communication.exceptions.UnexpectedResponseException;
import communication.extendedClasses.ExtendedDataInputStream;

/**
 * This is a reader for a bluetooth connection that is managed by a pc. It will
 * read the wrapper header around the original message with a second header that
 * contains the name of the nxt the message originates from, the name of the nxt
 * the message shall be send to, a code which message is send (see
 * {@link PCConnectionManager}) and optional some content or data.
 * 
 * @author Annabelle Klarl
 */
class RoutingReader implements Runnable {

	private static final Logger logger = Logger.getLogger();

	// bt connection
	private String myName;
	private String remoteName;
	private ExtendedDataInputStream dis;
	private PCConnectionManager owner;

	private MessageReader messageReader;

	private boolean closeRequested = false;
	private boolean closed = false;

	/**
	 * Constructor
	 * 
	 * @param myName
	 *            the name of this pc (normally "PC")
	 * @param remoteName
	 *            the name of the nxt this reader reads from
	 * @param dis
	 *            the input stream where to read the messages from
	 * @param owner
	 *            the owning connection manager of this reader
	 * @param messageReader
	 *            the reader which can read the messages from a remote device
	 */
	public RoutingReader(String myName, String remoteName,
			ExtendedDataInputStream dis, PCConnectionManager owner,
			MessageReader messageReader) {
		this.myName = myName;
		this.remoteName = remoteName;
		this.dis = dis;
		this.owner = owner;
		this.messageReader = messageReader;
	}

	/**
	 * sets the getter which gets the path for a given file name for this bt
	 * connection
	 * 
	 * @param ownFileLocalizer
	 *            a FileLocalizer that converts the filename to a path
	 */
	public void setOwnFileLocalizer(FileLocalizer ownFileLocalizer) {
		this.messageReader.setOwnFileLocalizer(ownFileLocalizer);
	}

	/**
	 * sets the getter which gets the degree to send for this bt connection
	 * 
	 * @param ownCompassController
	 *            a CompassController that gets the degree of this device
	 */
	public void setOwnCompassController(
			AbstractCompassSensorWrapper ownCompassController) {
		this.messageReader.setOwnCompassController(ownCompassController);
	}

	/**
	 * sets the getter which gets the light value to send for this bt connection
	 * 
	 * @param ownlightValueGetter
	 *            a AbstractLightValueGetter that gets the light value of this
	 *            device
	 */
	public void setOwnLightValueGetter(
			AbstractColorSensorWrapper ownlightValueGetter) {
		this.messageReader.setOwnLightValueGetter(ownlightValueGetter);
	}

	/**
	 * sets the parameter that a managed close request was send to the remote
	 * device
	 * 
	 * @param closeRequested
	 *            whether a managed close is requested
	 */
	public void setManagedCloseRequested(boolean closeRequested) {
		this.closeRequested = closeRequested;
	}

	/**
	 * sets the parameter that a direct close request was send to the remote
	 * device
	 * 
	 * @param closeRequested
	 *            whether a direct close is requested
	 */
	public void setDirectCloseRequested(boolean closeRequested) {
		this.messageReader.setCloseRequested(closeRequested);
	}

	/**
	 * gets the degree that was fetched from a remote device (should only be
	 * called after a BTCommObserver was notified of the corresponding BTEvent)
	 * 
	 * @return the degree from the remote device
	 */
	public float getRemoteDegree() {
		return this.messageReader.getRemoteDegree();
	}

	/**
	 * gets the light value that was fetched from a remote device (should only
	 * be called after a BTCommObserver was notified of the corresponding
	 * BTEvent)
	 * 
	 * @return the light value from the remote device
	 */
	public float getRemoteLightValue() {
		return this.messageReader.getRemoteLightValue();
	}

	/**
	 * gets the name of the file that was fetched from a remote device (should
	 * only be called after a BTCommObserver was notified of the corresponding
	 * BTEvent)
	 * 
	 * @return the name of the file that was fetched from the remote device
	 */
	public String getRemoteFileName() {
		return this.messageReader.getRemoteFileName();
	}

	/**
	 * returns whether this reader is closed
	 * 
	 * @return whether this reader is closed
	 */
	public boolean isClosed() {
		return this.closed;
	}

	/**
	 * closes the connection
	 * 
	 * @param remoteName
	 *            the name of the remote device to which the connection is
	 *            closed
	 * @param message
	 *            the message why the connection is closed
	 */
	private void closeConnection(String remoteName, String message) {
		this.closed = true;
		this.owner.notifyManagerForNXTConnectionClosed(remoteName, message);
	}

	@Override
	public void run() {
		try {
			String from = null;
			String to = null;
			byte code = -1;

			while (!this.closed) {
				try {
					from = this.dis.readString();
					to = this.dis.readString();
					code = (byte) this.dis.read();

					if (code == -1) {
						this
								.closeConnection(this.remoteName,
										"end of stream while reading. close connection...");
					}
					else {
						byte upperCode = (byte) (code & PCConnectionManager.UPPER_BITS);
						byte lowerCode = (byte) (code & PCConnectionManager.LOWER_BITS);

						if (PCConnectionManager.DATA == upperCode) {
							this.readData(from, to);
						}
						else if (PCConnectionManager.VIRTUAL_ADMIN == upperCode) {
							this.readVirtualAdmin(lowerCode, from, to);
						}
						else if (PCConnectionManager.COMMAND == upperCode) {
							this.readCommand(lowerCode, from, to);
						}
						else {
							throw new UnexpectedResponseException(
									"UnexpectedResponse: managed connection received command "
											+ code);
						}
					}
				}
				catch (IOException e) {
					if (!this.closed) {
						logger.error(e);
						this.closeConnection(this.remoteName,
								"IOException, will close connection.");
					}
				}
				catch (QueueBlockedException e) {
					logger.error("Writing from " + from + " to " + to
							+ " while manager closes all connections");
				}
			}
		}
		// to avoid the weird beep
		catch (Throwable e) {
			logger.error(e);
		}
		finally {

			try {
				// concatenate any parts of files that are not concatenated
				// already
				this.messageReader.concatenateLeftFiles();

				if (!this.closed) {
					this.closeConnection(this.remoteName,
							"closing conn after Throwable");
				}
			}
			// to avoid the weird beep
			catch (Throwable e) {
				logger.error(e);
			}
		}
	}

	/**
	 * forwards the sent data to the specified device
	 * 
	 * @param from
	 *            from which device the data was sent
	 * @param to
	 *            to which device the data is sent
	 * @throws IOException
	 *             thrown if nothing can be read from the input stream
	 * @throws QueueBlockedException
	 *             thrown if nothing can be written to the output stream
	 */
	private void readData(String from, String to) throws IOException,
			QueueBlockedException {
		int len = this.dis.readInt();
		byte[] data = new byte[len];
		this.dis.read(data);
		if (this.myName.equals(to)) {
			this.messageReader.read(new ExtendedDataInputStream(
					new ByteArrayInputStream(data)));
		}
		else {
			this.owner.forwardData(from, to, data);
		}

	}

	/**
	 * reads a command concerning the administration of the virtual connection
	 * (may be CONNECT_REQUEST, CONNECT_ACK, CONNECT_DECLINE, CLOSE_VIRTUAL or
	 * RECEIVER_NOT_KNOWN) and either respondes to the connection init or closes
	 * the connection
	 * 
	 * @param code
	 *            the command
	 * @param from
	 *            from which device the command originates
	 * @param to
	 *            to which device the command is sent
	 * @throws QueueBlockedException
	 *             thrown if nothing can be written to the remote device
	 * @throws UnexpectedResponseException
	 *             thrown if the sent command was not a command for virtual
	 *             connection administration
	 */
	private void readVirtualAdmin(byte code, String from, String to)
			throws QueueBlockedException, UnexpectedResponseException {
		code = (byte) (code & PCConnectionManager.LOWER_BITS);

		if (code == PCConnectionManager.CONNECT_REQUEST
				|| code == PCConnectionManager.CONNECT_ACK
				|| code == PCConnectionManager.CONNECT_DECLINE) {
			if (to.equals(this.myName)) {
				this.owner.readConnectCommand(from, code);
			}
			else {
				this.owner.forwardConnectCommand(from, to, code);
			}
		}
		else if (code == PCConnectionManager.RECEIVER_NOT_KNOWN
				&& to.equals(this.myName)) {
			this.owner.notifyManagerForUnknownReceiver(from);
		}
		else {
			throw new UnexpectedResponseException(
					"UnexpectedResponse in virtual admin: received " + code);
		}

	}

	/**
	 * reads a command concerning the negotiation of close of the underlying
	 * physical connection
	 * 
	 * @param code
	 *            the command
	 * @param from
	 *            from which device the command originates
	 * @param to
	 *            to which device the command is sent
	 * @throws QueueBlockedException
	 *             thrown if nothing can be written to the remote device
	 * @throws UnexpectedResponseException
	 *             thrown if the sent command was not a command for close
	 *             negotiation
	 */
	private void readCommand(byte code, String from, String to)
			throws QueueBlockedException, UnexpectedResponseException {
		code = (byte) (code & PCConnectionManager.LOWER_BITS);

		if (code == PCConnectionManager.CLOSE_PHYSICAL
				&& to.equals(this.myName)) {
			// close this connection between pc and nxt
			this.owner.acknowledgeCloseToNXT(from);
			this.closeConnection(from,
					"close command received. close connection...");
		}
		else if (code == PCConnectionManager.CLOSE_PHYSICAL_ACK
				&& to.equals(this.myName)) {
			if (this.closeRequested) {
				this.closeConnection(from,
						"close ACK received. close connection...");
			}
			else {
				this
						.closeConnection(from,
								"close ACK received without close request. close connection anyway...");
			}
		}
		else if (code == PCConnectionManager.CLOSE_PHYSICAL_DECLINE) {
			// notify owner that close request was not successful
			this.setManagedCloseRequested(false);
			this.owner.closeNotSuccessful(from);
		}
		else {
			throw new UnexpectedResponseException(
					"UnexpectedResponse in close negotiation: received " + code);
		}
	}
}
