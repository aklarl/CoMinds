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

import logging.Logger;

import common.Map;
import common.exceptions.QueueBlockedException;
import communication.exceptions.ConnectionClosedException;

/**
 * This class manages all connection of a nxt. It can manage direct connection
 * to a specific nxt, direct connection that are inbound or managed connection
 * that are inbound or outbound. It manages that there is only one connection to
 * each other device (reuse of existing connections) and that all connections
 * are closed if the connection manager is stopped.
 * 
 * @author Annabelle Klarl
 */
public class PCConnectionManager extends BTCommObserver {

	private static final Logger logger = Logger.getLogger();

	// Commands in the upper four bits
	// sending data to the nxt
	protected static final byte DATA = 0x00; // 0000 0000
	// administration of the virtual connection
	protected static final byte VIRTUAL_ADMIN = 0x10; // 0001 0000
	// negotiation of close of physical connection
	protected static final byte COMMAND = 0x20; // 0010 0000

	// Commands in the lower four bits
	// adminstration of the virtual connection
	protected static final byte CONNECT_REQUEST = 0x00; // 0000 0000
	protected static final byte CONNECT_ACK = 0x01; // 0000 0001
	protected static final byte CONNECT_DECLINE = 0x02; // 0000 0010
	protected static final byte RECEIVER_NOT_KNOWN = 0x03; // 0000 0011

	// negotiation of physical close
	protected static final byte CLOSE_PHYSICAL = 0x05; // 0000 0101
	protected static final byte CLOSE_PHYSICAL_ACK = 0x06; // 0000 0110
	protected static final byte CLOSE_PHYSICAL_DECLINE = 0x07; // 0000 0111

	protected static final byte UPPER_BITS = -0x10;
	protected static final byte LOWER_BITS = 0x0F;

	private static PCConnectionManager manager;

	// for virtual connections (forwarding only)
	private Map<String, BTComm> nxtConnections = new Map<String, BTComm>();

	// for direct connections (for communication between pc and nxt)
	private Map<String, BTComm> directConnections = new Map<String, BTComm>();
	private Map<String, Byte> waitingConnections = new Map<String, Byte>();

	private boolean initFinished = false;
	private String myName = "PC";

	private boolean closeRequested = false;

	/**
	 * Constructor for a connection manager that manages direct and managed
	 * connections. This constructor will establish a connection to the pc
	 * server
	 * 
	 * @param queueSize
	 *            the queuesize of the writer thread for the managed connections
	 */
	private PCConnectionManager(int queueSize, boolean logging,
			String... nxtNames) {
		for (String nxtName : nxtNames) {
			BTComm btcomm = new BTComm(this.myName, nxtName, queueSize,
					logging, this);
			this.nxtConnections.put(nxtName, btcomm);
		}
		synchronized (this) {
			this.initFinished = true;
			this.notify();
		}
	}

	/**
	 * gets a connection manager that manages all connections
	 * 
	 * @param nxtNames
	 *            to which nxts to connect
	 * @return a connection manager
	 */
	public static PCConnectionManager getManager(String... nxtNames) {
		return getManager(500, true, nxtNames);
	}

	/**
	 * gets a connection manager that manages all connections
	 * 
	 * @param queueSize
	 *            the queuesize of the writer thread for each managed
	 *            connections
	 * @param logging
	 *            whether the communications shall be logged
	 * @param nxtNames
	 *            to which nxts to connect
	 * @return a connection manager
	 */
	public static PCConnectionManager getManager(int queueSize,
			boolean logging, String... nxtNames) {
		if (manager == null) {
			manager = new PCConnectionManager(queueSize, logging, nxtNames);
		}
		return manager;
	}

	/**
	 * gets a connection to the given nxt. The return value may be null if no
	 * connection to this nxt exists
	 * 
	 * @param nxtName
	 *            the nxt to connect to
	 * @return a bt comm connection to the nxt or null if no exists
	 */
	public BTComm getConnection(String nxtName) {
		try {
			this.connect(nxtName);
		}
		catch (QueueBlockedException e) {
			logger.error(e);
		}

		logger.debug("get connection to " + nxtName);
		return this.nxtConnections.get(nxtName);
	}

	/**
	 * connects to a remote device by using the managed connection to the pc.
	 * This method will return true if a managed connection could be established
	 * to the remote device and false otherwse
	 * 
	 * @param remoteName
	 *            the device to connect to
	 * @return whether a connection could be established
	 */
	private boolean connect(String remoteName) throws QueueBlockedException {
		if (this.hasConnectionTo(remoteName)
				&& !this.nxtConnections.get(remoteName).isClosed()) {
			this.writeManagerCommand(this.myName, remoteName,
					getVirtualAdminCommand(CONNECT_REQUEST));

			byte response = this.waitForResponse(0, remoteName);
			if (response == getVirtualAdminCommand(CONNECT_ACK)) {
				BTComm btcomm = this.nxtConnections.get(remoteName);
				if (btcomm.isDirectlyClosed()) {
					btcomm.setDirectlyClosed(false);
				}
				try {
					btcomm.register(this, BTEvent.CLOSE);
				}
				catch (ConnectionClosedException e) {
					// should not happen
					e.printStackTrace();
				}

				this.directConnections.put(remoteName, btcomm);
				return true;
			}
			else {
				// no connection could be established
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * waits that the managed bt reader gets a response for the connection to
	 * the device with name remoteName. It waits for a response until the
	 * timeout is reached.
	 * 
	 * @param timeout
	 *            how long to wait for a response
	 * @param remoteName
	 *            the name of the device from which a response is expected
	 * @return the response or null if no response is given until the timeout
	 */
	private byte waitForResponse(int timeout, String remoteName) {

		this.waitingConnections.put(remoteName, null);

		if (timeout == 0) {
			timeout = Integer.MAX_VALUE;
		}

		long now = System.currentTimeMillis();
		while (this.waitingConnections.get(remoteName) == null
				&& (System.currentTimeMillis() - now) < timeout) {
			synchronized (this) {
				try {
					this.wait(timeout);
				}
				catch (InterruptedException e) {
				}
			}
		}

		return this.waitingConnections.remove(remoteName);

	}

	/**
	 * returns whether there is a connection to the device with the name
	 * remoteName
	 * 
	 * @param remoteName
	 *            the device to connect to
	 * @return whether there is a connection to the device with the name
	 *         remoteName
	 */
	private boolean hasConnectionTo(String remoteName) {
		while (!this.initFinished) {
			synchronized (this) {
				try {
					this.wait();
				}
				catch (InterruptedException e) {
				}
			}
		}
		return this.nxtConnections.get(remoteName) != null;
	}

	/**
	 * reads any given connect command, notifies the waiting connection for the
	 * command and answer the command if necessary
	 * 
	 * @param fromName
	 *            from which the command originates from
	 * @param command
	 *            the command itself
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked
	 */
	protected void readConnectCommand(String fromName, byte command)
			throws QueueBlockedException {
		command = (byte) (command & PCConnectionManager.LOWER_BITS);

		switch (command) {
		case CONNECT_REQUEST:
			this.writeManagerCommand(this.myName, fromName,
					getVirtualAdminCommand(CONNECT_ACK));
			break;
		}

		this.notifyWaitingConnectionForResponse(fromName, command);

	}

	/**
	 * notifies a any connection that is waiting for a connection from the
	 * remote device with the name remoteName that this device send the given
	 * command
	 * 
	 * @param remoteName
	 *            the remote device that send the command
	 * @param response
	 *            the received command
	 */
	private void notifyWaitingConnectionForResponse(String remoteName,
			byte response) {
		logger.debug("via manager: received command " + response + " from "
				+ remoteName);
		this.waitingConnections.put(remoteName,
				getVirtualAdminCommand(response));

		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * forwards a connect command to a remote device if there is a remote device
	 * (otherwise it will send receiver not known to the originator device).
	 * While close is requested, no messages will be send.
	 * 
	 * @param fromName
	 *            from which device the command originates from
	 * @param toName
	 *            to which device to send
	 * @param command
	 *            the command to be send
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked
	 */
	protected void forwardConnectCommand(String fromName, String toName,
			byte command) throws QueueBlockedException {
		if (this.closeRequested) {
			logger
					.info("forwarding of connect command while close is requested");
			return;
		}

		this.writeManagerCommand(fromName, toName,
				getVirtualAdminCommand(command));
	}

	/**
	 * forwards data from one nxt to another. While close is requested no
	 * messages are send.
	 * 
	 * @param fromName
	 *            the nxt the data originates from
	 * @param toName
	 *            the nxt the data is directed to
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked
	 */
	protected void forwardData(String fromName, String toName, byte[] data)
			throws QueueBlockedException {
		if (this.closeRequested) {
			logger
					.info("forwarding of connect command while close is requested");
			return;
		}

		BTComm btcomm = this.nxtConnections.get(toName);
		if (btcomm != null) {
			btcomm.forwardData(fromName, data);
			logger.debug("data from " + fromName + " forwarded to " + toName);
		}
		else {
			btcomm = this.nxtConnections.get(fromName);
			if (btcomm != null) {
				btcomm.forwardCommand(toName,
						getVirtualAdminCommand(RECEIVER_NOT_KNOWN));
			}
		}
	}

	/**
	 * writes an ack to a connection request that !concerns the PC! to the
	 * remote device
	 * 
	 * @param remoteName
	 *            the name of the remote nxt from which the connect request
	 *            originates from
	 * @throws QueueBlockedException
	 *             if the writer thread is blocked
	 */
	protected void acknowledgePCConnectionRequest(String remoteName)
			throws QueueBlockedException {
		this.writeManagerCommand(this.myName, remoteName,
				getVirtualAdminCommand(CONNECT_ACK));
	}

	/**
	 * acknowledges a close request
	 * 
	 * @param remoteName
	 *            the nxt that send the close request
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked
	 */
	protected void acknowledgeCloseToNXT(String remoteName)
			throws QueueBlockedException {
		BTComm btcomm = this.nxtConnections.get(remoteName);
		if (btcomm != null) {
			btcomm.closeNotSuccessful();
			this.writeManagerCommand(this.myName, remoteName,
					getCloseCommand(CLOSE_PHYSICAL_ACK));
		}
	}

	/**
	 * writes a command of the manager to the connection to the device with the
	 * name remoteName and blocks the writer queue afterwards
	 * 
	 * @param fromName
	 *            the device from which the command originates from
	 * @param toName
	 *            the remote device to which to write
	 * @param command
	 *            the command to write (shall only be command of the manager)
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked
	 */
	private void writeManagerCommand(String fromName, String toName,
			byte command) throws QueueBlockedException {

		// waiting for the manager to finish the init process
		this.hasConnectionTo(toName);

		BTComm btcomm = this.nxtConnections.get(toName);
		if (btcomm != null) {
			btcomm.forwardCommand(fromName, command);
			logger.debug("command " + command + " from " + fromName
					+ " send to " + toName);
		}
		else {
			btcomm = this.nxtConnections.get(fromName);
			if (btcomm != null) {
				btcomm.forwardCommand(toName,
						getVirtualAdminCommand(RECEIVER_NOT_KNOWN));
			}
		}
	}

	/**
	 * notifies the connection manager that there is no connection to the device
	 * with the name remoteName on the pc server. So close this logical BTComm
	 * on the nxt (this should not happen for a PC)
	 * 
	 * @param remoteName
	 *            the name of the remote device
	 */
	public void notifyManagerForUnknownReceiver(String remoteName) {
		BTComm btcomm = this.nxtConnections.remove(remoteName);
		if (btcomm != null) {
			logger.warn("via manager: " + remoteName + " not known");
			btcomm.close();
			synchronized (btcomm) {
				btcomm.notify();
			}
		}
	}

	/**
	 * notifies the observer that the bt connection will be closed.
	 * 
	 * @param btcomm
	 *            the bluetooth connection that is closed
	 */
	@Override
	protected void notifyForClose(BTComm btcomm) {
		String name = btcomm.getRemoteName();
		BTComm btc = this.directConnections.get(name);
		if (btc != null && btc.isClosed() && btcomm.equals(btc)) {
			this.directConnections.remove(name);
		}
	}

	/**
	 * notifies the manager that a close request to the device from was not
	 * successfull (it was declined).
	 * 
	 * @param from
	 *            the device from which the decline originated
	 */
	public void closeNotSuccessful(String from) {
		// nothing to do at the moment
	}

	/**
	 * notifies the ConnectionManager that the bt connection to the nxt with the
	 * name fromName is closed
	 * 
	 * @param fromName
	 *            the name of the nxt of the connection that was closed
	 * @param message
	 *            why the connection has been closed
	 */
	protected void notifyManagerForNXTConnectionClosed(String fromName,
			String message) {
		BTComm btcomm = this.nxtConnections.remove(fromName);
		if (btcomm != null) {
			btcomm.close();
			this.notifyForClose(btcomm);
		}

		logger.info("connection to " + fromName + ": " + message);

	}

	/**
	 * closes the connection manager which will close all open connections and
	 * the connection to the pc if there is any
	 */
	private void close() {
		this.closeRequested = true;

		for (String nxtName : this.directConnections.keys()) {
			BTComm btcomm = this.nxtConnections.get(nxtName);
			this.directClose(btcomm);
		}

		logger.info("direct connections closed");

		for (String nxtName : this.nxtConnections.keys()) {
			BTComm btcomm = this.nxtConnections.get(nxtName);
			this.managedClose(btcomm);
		}

		logger.info("nxt connections closed");

		logger.info("manager closed");
	}

	/**
	 * closes the nxt connection manager if a connection manager has been
	 * established
	 */
	public static void closeManager() {
		if (manager != null) {
			manager.close();
		}
	}

	/**
	 * combines the given command with the command for virtual administration
	 * 
	 * @param command
	 *            the command to combine with
	 * @return the combined command
	 */
	private static byte getVirtualAdminCommand(byte command) {
		return (byte) (PCConnectionManager.VIRTUAL_ADMIN | command);
	}

	/**
	 * combines the given command with the command for close negotiation
	 * 
	 * @param command
	 *            the command to combine with
	 * @return the combined command
	 */
	protected static byte getCloseCommand(byte command) {
		return (byte) (PCConnectionManager.COMMAND | command);
	}
}
