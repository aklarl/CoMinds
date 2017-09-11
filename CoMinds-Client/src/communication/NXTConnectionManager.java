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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import lejos.nxt.Button;
import lejos.nxt.Settings;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import logging.Logger;

import common.Map;
import common.Writer;
import common.exceptions.QueueBlockedException;
import communication.exceptions.ConnectionClosedException;
import communication.exceptions.ConnectionException;
import communication.exceptions.ManagerException;
import communication.exceptions.NoDirectConnectionsException;
import communication.exceptions.NoManagedConnectionsException;
import communication.extendedClasses.ExtendedDataInputStream;

/**
 * This class manages all connection of a nxt. It can manage direct connections
 * to a specific nxt, direct connections that are inbound or managed connections
 * that are inbound or outbound. Managed connections use a pc as a server for
 * their communication and all communication is send to the pc which forwards
 * the data to the receiver. The data will not be send directly to the receiver.
 * The manager takes care that there is only one connection to each other device
 * (reuse of existing connections) and that all connections are closed if the
 * connection manager is stopped. Be careful: The manager cannot manage direct
 * and managed connection simultaneously!
 * 
 * @author Annabelle Klarl
 */
public class NXTConnectionManager extends BTCommObserver {

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

	private static NXTConnectionManager manager;

	private ArrayList<BTCommUnmanaged> inboundConnection = new ArrayList<BTCommUnmanaged>();
	private Map<String, BTCommUnmanaged> directConnections = new Map<String, BTCommUnmanaged>();
	private Map<String, BTCommManaged> managedConnections = new Map<String, BTCommManaged>();
	private Map<String, Byte> waitingManagedConnections = new Map<String, Byte>();

	// the name of the nxt
	private final String myName;
	private final byte[] myNameArray;

	// whether the manager can only manage direct connection (otherwise only
	// managed connection are possible)
	private final boolean directManager;

	// connection to the pc server
	private final BTConnection btc;
	private final RoutingReader managedReader;
	private final Writer managedWriter;

	// whether the connection manager is closed
	private boolean closed = false;

	/**
	 * Constructor for a connection manager that manages only managed
	 * connections. This constructor will establish a connection to the pc
	 * server
	 * 
	 * @param queueSize
	 *            the queuesize of the writer thread for the managed connections
	 */
	private NXTConnectionManager(int queueSize, boolean logging) {
		this.myName = Settings.getProperty("lejos.usb_name", "");
		this.myNameArray = communication.CommunicationUtils
				.convertStringToByteArray(this.myName);
		this.directManager = false;

		System.out.println("wait for manager");
		logger.info("wait for manager");

		this.btc = Bluetooth.waitForConnection(0, NXTConnection.PACKET);
		if (this.btc != null) {
			this.managedReader = new RoutingReader(
					this.myName,
					new ExtendedDataInputStream(this.btc.openDataInputStream()),
					this);
			new Thread(this.managedReader, "ServerReaderThread").start();

			this.managedWriter = new Writer(this.btc.openDataOutputStream(),
					queueSize, true);
			new Thread(this.managedWriter, "ServerWriterThread").start();

			logger.info("manager ready");
			System.out.println("manager ready");
		}
		else {
			this.managedReader = null;
			this.managedWriter = null;

			logger.error("no conn to server");
			System.out.println("no conn manager");
		}
	}

	/**
	 * Constructor for a connection manager that only manages direct connections
	 */
	private NXTConnectionManager() {
		this.myName = Settings.getProperty("lejos.usb_name", "");
		this.myNameArray = communication.CommunicationUtils
				.convertStringToByteArray(this.myName);
		this.directManager = true;

		this.btc = null;
		this.managedReader = null;
		this.managedWriter = null;

		logger.info("manager ready");
		System.out.println("manager ready");
	}

	/**
	 * gets a connection manager that manages only managed connections
	 * 
	 * @param queueSize
	 *            the queuesize of the writer thread for the managed connections
	 * @return a connection manager (maybe null if no connection to the pc
	 *         server could be established)
	 * @throws ManagerException
	 *             thrown if the singleton has already been initialized only for
	 *             direct connections
	 */
	public static NXTConnectionManager getManager(int queueSize)
			throws ManagerException {
		if (manager == null) {
			manager = new NXTConnectionManager(queueSize, true);
			if (manager.btc == null) {
				return null;
			}
		}
		else if (manager.directManager) {
			throw new ManagerException(
					"singleton-manager is a direct manager, but an indirect manager shall be created");
		}
		return manager;
	}

	/**
	 * gets a connection manager that manages all direct and will not create any
	 * managed connections
	 * 
	 * @return a connection manager
	 * @throws ManagerException
	 *             thrown if the singleton has already been initialized only for
	 *             managed connections
	 */
	public static NXTConnectionManager getManager() throws ManagerException {
		if (manager == null) {
			manager = new NXTConnectionManager();
		}
		else if (!manager.directManager) {
			throw new ManagerException(
					"singleton-manager is a indirect manager, but a direct manager shall be created");
		}
		return manager;
	}

	/**
	 * transfers a file to the pc (connection will not be logged)
	 * 
	 * @param fileName
	 *            the file to transfer
	 */
	public static void transferFileToPC(String fileName) {

		BTCommObserver observer = new BTCommObserver();
		NXTConnectionManager manager = null;

		try {
			System.out.println("log to PC");
			System.out.println("press enter");
			int button = Button.waitForPress();

			if (Button.ID_ENTER != button) {
				return;
			}

			manager = new NXTConnectionManager(500, false);
			BTCommManaged btcomm = manager.getManagedConnection(0, "PC", false);

			try {
				btcomm.register(observer, BTEvent.CLOSE);
			}
			catch (ConnectionClosedException e) {
				// should not happen
				System.out.println("Manager: conn already closed");
			}

			try {
				btcomm.writeFile(fileName);
				System.out.println("file written");
			}
			catch (FileNotFoundException e) {
				System.out.println("Manager: file not found");
			}
			catch (IOException e) {
				System.out.println("Manager: IOException while transfer");
			}
			catch (ConnectionClosedException e) {
				System.out.println("Manager: conn already closed");
			}
			catch (QueueBlockedException e) {
				// should not happen
				System.out.println("Manager: queue blocked");
			}
		}
		// to avoid the weird beep
		catch (Throwable e) {
			System.out.println("Manager-Exc " + e.getClass());
		}
		finally {
			try {
				if (manager != null) {
					manager.close();
				}
			}
			// to avoid the weird beep
			catch (Throwable e) {
				System.out.println("Manager-Exc" + e.getClass());
			}
		}
	}

	/**
	 * gets a logical bt connection to the nxt with name remoteName. According
	 * to the parameter directConnection, the connection will be MANAGED by the
	 * pc (parameter is false) or will be DIRECT, the queusize for the writer
	 * thread will be 1 if it is a direct connection and the communication will
	 * be logged according to the parameter logging.
	 * 
	 * @param remoteName
	 *            the nxt to connect to
	 * @param directConnection
	 *            whether the connection shall be direct or managed by a pc
	 * @param logging
	 *            whether the communication shall be logged
	 * @return the logical bt connection
	 * @throws ConnectionException
	 *             thrown if one tries to establish a direct connection, but the
	 *             manager can only manage managed connection and the other way
	 *             round
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTComm getConnection(String remoteName, boolean directConnection,
			boolean logging) throws ConnectionException,
			ConnectionClosedException {
		if (directConnection) {
			return this.getDirectConnection(remoteName, 1, logging);
		}
		else {
			return this.getManagedConnection(remoteName, logging);
		}
	}

	/**
	 * waits for an inbound connection. This connection will be DIRECT, the
	 * queuesize of the writer thread will be 1 and the communication will be
	 * logged. If the connection manager on the pc server could not finish its
	 * init process, null will be returned.
	 * 
	 * @param timeOut
	 *            how long to wait for an inbound bt connection
	 * @return the logical bt connection or null if the connection manager on
	 *         the pc server could not finish its init process
	 * @throws NoDirectConnections
	 *             thrown if one tries to establish a direct connection, but the
	 *             manager can only manage managed connection
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTCommUnmanaged getDirectConnection(int timeOut)
			throws NoDirectConnectionsException, ConnectionClosedException {
		return this.getDirectConnection(0, 1, true);
	}

	/**
	 * waits for an inbound connection. This connection will be DIRECT, the
	 * queuesize of the writer thread will be according to the parameter
	 * queuesize and the communication will be logged according to the parameter
	 * logging. If the connection manager on the pc server could not finish its
	 * init process, null will be returned.
	 * 
	 * @param timeOut
	 *            how long to wait for an inbound bt connection
	 * @param queueSize
	 *            the queuesize of the writer thread for this communication
	 * @param logging
	 *            whether the communication shall be logged
	 * @return the logical bt connection or null if the connection manager on
	 *         the pc server could not finish its init process
	 * @throws NoDirectConnections
	 *             thrown if one tries to establish a direct connection, but the
	 *             manager can only manage managed connection
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTCommUnmanaged getDirectConnection(int timeOut, int queueSize,
			boolean logging) throws NoDirectConnectionsException,
			ConnectionClosedException {
		if (this.closed) {
			throw new ConnectionClosedException(
					"the connection manager is already closed");
		}

		if (!this.directManager) {
			throw new NoDirectConnectionsException();
		}

		BTCommUnmanaged btcomm = new BTCommUnmanaged(this.myName, timeOut,
				queueSize, logging);
		btcomm.register(this, BTEvent.CLOSE);
		this.inboundConnection.add(btcomm);
		return btcomm;
	}

	/**
	 * gets a logical bt connection to the nxt with the name remoteName. This
	 * connection will be DIRECT. If there is already a direct connection to the
	 * remote device, this one will be used. If not, a new one will be
	 * established with the queuesize 1 for the writer thread and logging. If
	 * the connection manager on the pc server could not finish its init
	 * process, null will be returned.
	 * 
	 * @param remoteName
	 *            the nxt to connect to
	 * @return the logical direct connection or null if the connection manager
	 *         on the pc server could not finish its init process
	 * @throws NoDirectConnections
	 *             thrown if one tries to establish a direct connection, but the
	 *             manager can only manage managed connection
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTCommUnmanaged getDirectConnection(String remoteName)
			throws NoDirectConnectionsException, ConnectionClosedException {
		return this.getDirectConnection(remoteName, 1, true);
	}

	/**
	 * gets a logical bt connection to the nxt with the name remoteName. This
	 * connection will be DIRECT. If there is already a direct connection to the
	 * remote device, this one will be used. If not, a new one will be
	 * established with the given queuesize for the writer thread and logging
	 * according to the parameter logging. If the connection manager on the pc
	 * server could not finish its init process, null will be returned.
	 * 
	 * @param remoteName
	 *            the nxt to connect to
	 * @param queueSize
	 *            the queuesize of the writer thread
	 * @param logging
	 *            whether the communication shall be logged
	 * @return the logical direct connection or null if the connection manager
	 *         on the pc server could not finish its init process
	 * @throws NoDirectConnections
	 *             thrown if one tries to establish a direct connection, but the
	 *             manager can only manage managed connection
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTCommUnmanaged getDirectConnection(String remoteName,
			int queueSize, boolean logging)
			throws NoDirectConnectionsException, ConnectionClosedException {
		if (this.closed) {
			throw new ConnectionClosedException(
					"the connection manager is already closed");
		}

		if (!this.directManager) {
			throw new NoDirectConnectionsException();
		}

		if (this.directConnections.contains(remoteName)) {
			BTCommUnmanaged btcomm = this.directConnections.get(remoteName);
			if (btcomm.isClosed()) {
				this.directConnections.remove(remoteName);
			}
			else {
				btcomm.addUser();
				logger.info("direct conn to " + remoteName + " exists");
				return btcomm;
			}
		}

		BTCommUnmanaged btc = new BTCommUnmanaged(this.myName, remoteName,
				queueSize, logging);
		btc.register(this, BTEvent.CLOSE);
		this.directConnections.put(remoteName, btc);

		return btc;
	}

	/**
	 * waits for an inbound connection. This connection will be MANAGED by the
	 * pc and the communication will be logged by default.
	 * 
	 * @param timeOut
	 *            how long to wait for an inbound bt connection
	 * @param remoteName
	 *            the name of the nxt to connect to
	 * @return the logical bt connection
	 * @throws NoManagedConnections
	 *             thrown if the connection manager only manages direct
	 *             connections
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTCommManaged getManagedConnection(int timeOut, String remoteName)
			throws NoManagedConnectionsException, ConnectionClosedException {
		return this.getManagedConnection(timeOut, remoteName, true);
	}

	/**
	 * waits for an inbound connection. This connection will be MANAGED by the
	 * pc and the communication will be logged according to the parameter
	 * logging.
	 * 
	 * @param timeOut
	 *            how long to wait for an inbound bt connection
	 * @param remoteName
	 *            the name of the nxt to connect to
	 * @param logging
	 *            whether the communication shall be logged
	 * @return the logical bt connection or null if no connection could be
	 *         established
	 * @throws NoManagedConnections
	 *             thrown if the connection manager only manages direct
	 *             connections
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTCommManaged getManagedConnection(int timeOut, String remoteName,
			boolean logging) throws NoManagedConnectionsException,
			ConnectionClosedException {
		if (this.closed) {
			throw new ConnectionClosedException(
					"the connection manager is already closed");
		}

		if (this.directManager) {
			throw new NoManagedConnectionsException();
		}

		System.out.println("conn to " + remoteName);
		logger.debug("connecting to " + remoteName);

		if (this.managedConnections.contains(remoteName)) {
			BTCommManaged btcomm = this.managedConnections.get(remoteName);
			if (btcomm.isClosed()) {
				this.managedConnections.remove(remoteName);
			}
			else {
				btcomm.addUser();
				System.out.println("existing conn");
				logger.info("managed conn to " + remoteName + " exists");
				return btcomm;
			}
		}

		if (this.waitForConnection(timeOut, remoteName)) {
			BTCommManaged btc = new BTCommManaged(this.myName, remoteName,
					this.managedWriter, logging);
			btc.register(this, BTEvent.CLOSE);
			this.managedConnections.put(remoteName, btc);
			System.out.println("new managed conn");
			logger.info("managed conn to " + remoteName);

			return btc;
		}
		else {
			System.out.println("no conn");
			logger.error("no conn to " + remoteName);
			return null;
		}
	}

	/**
	 * gets a logical bt connection to the nxt with name remoteName. The
	 * connection will be MANAGED by the pc and the communication will be logged
	 * by default.
	 * 
	 * @param remoteName
	 *            the nxt to connect to
	 * @return the logical bt connection
	 * @throws NoManagedConnections
	 *             thrown if the connection manager only manages direct
	 *             connections
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTCommManaged getManagedConnection(String remoteName)
			throws NoManagedConnectionsException, ConnectionClosedException {
		return this.getManagedConnection(remoteName, true);
	}

	/**
	 * gets a logical bt connection to the nxt with name remoteName (factory
	 * method). The connection will be MANAGED by the pc and the communication
	 * will be logged according to the corresponding parameter.
	 * 
	 * @param remoteName
	 *            the nxt to connect to
	 * @param logging
	 *            whether the communication shall be logged or not
	 * @return the logical managed bt connection or null if no connection could
	 *         be established
	 * @throws NoManagedConnections
	 *             thrown if the connection manager only manages direct
	 *             connections
	 * @throws ConnectionClosedException
	 *             thrown if the connection manager is already closed
	 */
	public BTCommManaged getManagedConnection(String remoteName, boolean logging)
			throws NoManagedConnectionsException, ConnectionClosedException {
		if (this.closed) {
			throw new ConnectionClosedException(
					"the connection manager is already closed");
		}

		if (this.directManager) {
			throw new NoManagedConnectionsException();
		}

		System.out.println("conn to " + remoteName);
		logger.debug("connecting to " + remoteName);

		if (this.managedConnections.contains(remoteName)) {
			BTCommManaged btcomm = this.managedConnections.get(remoteName);
			if (btcomm.isClosed()) {
				this.managedConnections.remove(remoteName);
			}
			else {
				btcomm.addUser();
				System.out.println("existing conn");
				logger.info("managed conn to " + remoteName + " exists");
				return btcomm;
			}
		}

		if (this.connect(remoteName)) {
			BTCommManaged btc = new BTCommManaged(this.myName, remoteName,
					this.managedWriter, logging);
			btc.register(this, BTEvent.CLOSE);
			this.managedConnections.put(remoteName, btc);
			System.out.println("new managed conn");
			logger.info("managed conn to " + remoteName);
			return btc;
		}
		else {
			System.out.println("no conn");
			logger.error("no conn to " + remoteName);
			return null;
		}
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
	private boolean connect(String remoteName) {
		try {
			this
					.writeManagerCommand(
							remoteName,
							this
									.getVirtualAdminCommand(NXTConnectionManager.CONNECT_REQUEST));
		}
		catch (QueueBlockedException e) {
			// should not happen
			logger.error("Manager: " + e.getMessage());
			System.out.println("Manager: QueueBlockedException!");
		}

		byte response = this.waitForResponse(0, remoteName);
		if (response == this.getVirtualAdminCommand(CONNECT_ACK)) {
			return true;
		}
		else if (response == this.getVirtualAdminCommand(CONNECT_DECLINE)) {
			return false;
		}
		else {
			// should not happen
			return false;
		}
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
	private boolean waitForConnection(int timeout, String remoteName) {

		byte response = this.waitForResponse(timeout, remoteName);

		if (response == this.getVirtualAdminCommand(CONNECT_REQUEST)) {
			return true;
		}
		else {
			// no one wanted to connect
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

		this.waitingManagedConnections.put(remoteName, null);

		if (timeout == 0) {
			timeout = Integer.MAX_VALUE;
		}

		long now = System.currentTimeMillis();
		while (this.waitingManagedConnections.get(remoteName) == null
				&& (System.currentTimeMillis() - now) < timeout) {
			synchronized (this) {
				try {
					this.wait(timeout);
				}
				catch (InterruptedException e) {
				}
			}
		}

		return this.waitingManagedConnections.remove(remoteName);

	}

	/**
	 * returns whether there is a request for a connection to the device with
	 * the name remoteName waiting for a response
	 * 
	 * @param remoteName
	 *            the device to connect to
	 * @return whether a connection to this remote device is waiting for a
	 *         response
	 */
	protected boolean isAnyConnWaiting(String remoteName) {
		return this.waitingManagedConnections.contains(remoteName);
	}

	/**
	 * writes an ack to a connection request to the remote device and notifies
	 * any waiting connection for the connection request.
	 * 
	 * @param remoteName
	 *            the name of the remote nxt from which the connect request
	 *            originates from
	 * @throws QueueBlockedException
	 *             if the writer thread is blocked
	 */
	protected void acknowledgeConnectionRequest(String remoteName)
			throws QueueBlockedException {
		this.notifyWaitingConnectionForResponse(remoteName, this
				.getVirtualAdminCommand(NXTConnectionManager.CONNECT_REQUEST));
		this.writeManagerCommand(remoteName, this
				.getVirtualAdminCommand(NXTConnectionManager.CONNECT_ACK));
	}

	/**
	 * writes a decline to a connection request to the remote device and
	 * notifies any waiting connection for the connection decline.
	 * 
	 * @param remoteName
	 *            the name of the remote nxt from which the connect request
	 *            originiates from
	 * @throws QueueBlockedException
	 *             if the writer thread is blocked
	 */
	protected void declineConnectionRequest(String remoteName)
			throws QueueBlockedException {
		this.notifyWaitingConnectionForResponse(remoteName, this
				.getVirtualAdminCommand(NXTConnectionManager.CONNECT_DECLINE));
		this.writeManagerCommand(remoteName, this
				.getVirtualAdminCommand(NXTConnectionManager.CONNECT_DECLINE));
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
	protected void notifyWaitingConnectionForResponse(String remoteName,
			byte response) {
		logger.info("via manager: rcv command " + response + " from "
				+ remoteName);
		this.waitingManagedConnections.put(remoteName, this
				.getVirtualAdminCommand(response));

		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * writes a close ack to a close request (it therefore may deblock the
	 * queue)
	 * 
	 * @param remoteName
	 *            the remote device to which to respond
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked
	 */
	protected void acknowledgeCloseToPC(String remoteName)
			throws QueueBlockedException {
		logger.info("via manager: closeack to " + remoteName);

		this.managedWriter.deblockQueue();
		this.writeManagerCommand(remoteName, this
				.getCloseCommand(CLOSE_PHYSICAL_ACK));
	}

	/**
	 * writes a command of the manager to the connection to the device with the
	 * name remoteName
	 * 
	 * @param remoteName
	 *            the remote device to which to write
	 * @param command
	 *            the command to write (shall only be a command of the manager)
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked
	 */
	private void writeManagerCommand(String remoteName, byte command)
			throws QueueBlockedException {
		this.writeManagerCommand(remoteName, command, false);
	}

	/**
	 * writes a command of the manager to the connection to the device with the
	 * name remoteName and blocks the writer queue afterwards
	 * 
	 * @param remoteName
	 *            the remote device to which to write
	 * @param command
	 *            the command to write (shall only be a command of the manager)
	 * @param blocked
	 *            whether the queue shall be blocked afterwards
	 * @throws QueueBlockedException
	 *             thrown if the writer thread is blocked
	 */
	private void writeManagerCommand(String remoteName, byte command,
			boolean blocked) throws QueueBlockedException {
		logger.info("via manager: command " + command + " to " + remoteName
				+ " sent");

		this.managedWriter.write(common.WriterUtils.concatArrays(
				this.myNameArray, communication.CommunicationUtils
						.convertStringToByteArray(remoteName),
				new byte[] { command }), blocked);
	}

	/**
	 * notifies the bluetooth connection that is waiting for data from the
	 * remote device that new data is available and starts the reading of the
	 * data for this bt comm
	 * 
	 * @param remoteName
	 *            the name of the device from which the data comes
	 * @param data
	 *            the data that was send
	 */
	protected void notifyManagedConnectionForData(String remoteName, byte[] data) {
		BTCommManaged btcomm = this.managedConnections.get(remoteName);
		if (btcomm != null && !btcomm.isClosed()) {
			logger.debug("via manager: data from " + remoteName);
			btcomm.setDataInput(new ExtendedDataInputStream(
					new ByteArrayInputStream(data)));
			btcomm.read();
		}
	}

	/**
	 * notifies the connection manager that there is no connection to the device
	 * with the name remoteName on the pc server. So close this logical BTComm
	 * on the nxt
	 * 
	 * @param remoteName
	 *            the name of the remote device
	 */
	protected void notifyManagerForUnknownReceiver(String remoteName) {
		BTCommManaged btcomm = this.managedConnections.remove(remoteName);
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
		if (this.directManager) {
			Iterator<BTCommUnmanaged> it = this.inboundConnection.iterator();
			while (it.hasNext()) {
				BTCommUnmanaged btc = it.next();
				if (btc.isClosed() && btcomm.equals(btc)) {
					it.remove();
					break;
				}
			}

			BTCommUnmanaged btc = this.directConnections.get(name);
			if (btc != null && btc.isClosed() && btcomm.equals(btc)) {
				this.directConnections.remove(name);
			}
		}

		else {
			BTCommManaged btc = this.managedConnections.get(name);
			if (btc != null && btc.isClosed() && btcomm.equals(btc)) {
				this.managedConnections.remove(name);
			}
		}

		synchronized (btcomm) {
			btcomm.notify();
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
	 * notifies the manager that the connection to the pc was closed
	 */
	protected void notifyManagerForPCConnectionClosed(String message) {
		logger.info(message);

		// close managed connections without sending any close commands
		for (String name : this.managedConnections.keys()) {
			BTCommManaged btcomm = this.managedConnections.get(name);
			btcomm.close();
		}

		this.managedWriter.stop();
		synchronized (this.managedWriter) {
			while (!this.managedWriter.isFinished()) {
				try {
					this.managedWriter.wait();
				}
				catch (InterruptedException e) {
				}
			}
		}
		this.btc.close();
		this.closed = true;

		System.out.println("manager closed");
		logger.info("manager closed");

		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * closes the connection manager which will close all open connections and
	 * the connection to the pc if there is any
	 */
	private void close() {
		for (BTCommUnmanaged btcomm : this.inboundConnection) {
			this.close(btcomm);
		}

		System.out.println("inbound closed");
		logger.info("inbound closed");

		for (String name : this.directConnections.keys()) {
			BTCommUnmanaged btcomm = this.directConnections.get(name);
			this.close(btcomm);
		}

		System.out.println("direct closed");
		logger.info("direct closed");

		for (String name : this.managedConnections.keys()) {
			BTCommManaged btcomm = this.managedConnections.get(name);
			this.close(btcomm);
		}

		System.out.println("managed closed");
		logger.info("managed closed");

		if (!this.directManager && !this.closed) {
			try {
				this.writeManagerCommand("PC", this
						.getCloseCommand(NXTConnectionManager.CLOSE_PHYSICAL),
						true);
			}
			catch (QueueBlockedException e) {
				// should not happen
				logger.error("Manager: " + e.getMessage());
				System.out.println("Manager: QueueBlockedException!");
			}

			// waiting for a notification that the pc has closed the connection
			// (see method notifyManagerForPCConnectionClosed)
			synchronized (this) {
				while (!this.closed) {
					try {
						this.wait();
					}
					catch (InterruptedException e) {
					}
				}
			}
		}
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
	private byte getVirtualAdminCommand(byte command) {
		return (byte) (NXTConnectionManager.VIRTUAL_ADMIN | command);
	}

	/**
	 * combines the given command with the command for close negotiation
	 * 
	 * @param command
	 *            the command to combine with
	 * @return the combined command
	 */
	private byte getCloseCommand(byte command) {
		return (byte) (NXTConnectionManager.COMMAND | command);
	}
}
