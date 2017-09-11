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

import java.io.IOException;

import logging.Logger;

import common.exceptions.QueueBlockedException;
import communication.exceptions.UnexpectedResponseException;
import communication.extendedClasses.ExtendedDataInputStream;

/**
 * This is a reader for a bluetooth connection that is managed by a pc. It will
 * read the wrapper header around the original message with a second header that
 * contains the name of the nxt the message originates from, the name of the nxt
 * the message shall be send to, a code which message is send (see
 * {@link NXTConnectionManager}) and optional some content or data.
 * 
 * @author Annabelle Klarl
 */
class RoutingReader implements Runnable {

	private static final Logger logger = Logger.getLogger();

	private String myName;
	private boolean closed = false;

	// where to read from (shall be the input stream from the pc)
	private ExtendedDataInputStream dis;

	// where to write to or where to forward the data
	private NXTConnectionManager owner;

	/**
	 * Constructor
	 * 
	 * @param myName
	 *            the name of this nxt
	 * @param dis
	 *            the input stream where to read the messages from
	 * @param owner
	 *            the owning connection manager of this reader
	 */
	public RoutingReader(String myName, ExtendedDataInputStream dis,
			NXTConnectionManager owner) {
		this.myName = myName;
		this.dis = dis;
		this.owner = owner;
	}

	/**
	 * closes the btreader and notifies the owner that the reader is closed, so
	 * also the connection is closed (all the virtual connection must be closed
	 * afterwards!)
	 * 
	 * @param message
	 *            why the connection was closed
	 */
	private void closeConnection(String message) {
		this.closed = true;
		this.owner.notifyManagerForPCConnectionClosed(message);
	}

	/**
	 * returns whether this reader is closed
	 * 
	 * @return whether this reader is closed
	 */
	public boolean isClosed() {
		return this.closed;
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
								.closeConnection("end of stream while reading. close connection...");
					}
					else {
						byte upperCode = (byte) (code & NXTConnectionManager.UPPER_BITS);
						byte lowerCode = (byte) (code & NXTConnectionManager.LOWER_BITS);

						if (NXTConnectionManager.DATA == upperCode) {
							this.readData(from, to);
						}
						else if (NXTConnectionManager.VIRTUAL_ADMIN == upperCode) {
							this.readVirtualAdmin(lowerCode, from, to);
						}
						else if (NXTConnectionManager.COMMAND == upperCode) {
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
						System.out.println("IOException");
						this
								.closeConnection("IOException while reading. close connection...");
					}
				}
			}
		}
		// to avoid the weird beep
		catch (Throwable e) {
			System.out.println("BTReader-Exc " + e.getClass());
			logger.error("BTReader-Throwable " + e.getClass());
		}
		finally {
			try {
				if (!this.closed) {
					this.closeConnection("closing conn after Throwable");
				}
			}
			// to avoid the weird beep
			catch (Throwable e) {
				System.out.println("BTReader-Exc " + e.getClass());
				logger.error("BTReader-Throwable " + e.getClass());
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
	 */
	private void readData(String from, String to) throws IOException {
		int len = this.dis.readInt();
		byte[] data = new byte[len];
		this.dis.read(data);
		if (this.myName.equals(to)) {
			this.owner.notifyManagedConnectionForData(from, data);
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
		code = (byte) (code & NXTConnectionManager.LOWER_BITS);

		if (code == NXTConnectionManager.CONNECT_REQUEST
				&& to.equals(this.myName)) {
			if (this.owner.isAnyConnWaiting(from)) {
				this.owner.acknowledgeConnectionRequest(from);
			}
			else {
				this.owner.declineConnectionRequest(from);
			}
		}
		else if ((code == NXTConnectionManager.CONNECT_ACK || code == NXTConnectionManager.CONNECT_DECLINE)
				&& to.equals(this.myName)) {
			this.owner.notifyWaitingConnectionForResponse(from, code);
		}
		else if (code == NXTConnectionManager.RECEIVER_NOT_KNOWN
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
		code = (byte) (code & NXTConnectionManager.LOWER_BITS);

		if (code == NXTConnectionManager.CLOSE_PHYSICAL
				&& to.equals(this.myName)) {
			// close this connection between pc and nxt
			this.owner.acknowledgeCloseToPC(from);
			this.closeConnection("close command received. close connection...");
		}
		else if (code == NXTConnectionManager.CLOSE_PHYSICAL_ACK
				&& to.equals(this.myName)) {
			// close this connection between pc and nxt
			this.closeConnection("close ACK received. close connection...");
		}
		else if (code == NXTConnectionManager.CLOSE_PHYSICAL_DECLINE) {
			// notify owner that close request was not successful
			this.owner.closeNotSuccessful(from);
		}
		else {
			throw new UnexpectedResponseException(
					"UnexpectedResponse in close negotiation: received " + code);
		}
	}
}
