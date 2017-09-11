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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import logging.Logger;
import sensorwrappers.color.AbstractColorSensorWrapper;
import sensorwrappers.compass.AbstractCompassSensorWrapper;
import sensorwrappers.exceptions.SensorException;

import common.FileLocalizer;
import common.exceptions.QueueBlockedException;
import communication.exceptions.ConnectionClosedException;
import communication.exceptions.UnexpectedResponseException;
import communication.extendedClasses.ExtendedDataInputStream;

/**
 * This class will read anything that is received at the input stream of a bt
 * connection. Don't use any private methods for reading. All the reading is
 * done in the {@link #read()}-method of this thread. The BTCommObserver is
 * notified if something interesting is read from the input stream. Then call
 * the getters to get the information got from the remote device.
 * 
 * @author Annabelle Klarl
 */
class MessageReader implements Runnable {

	private static final Logger logger = Logger.getLogger();

	// bt connection
	private ExtendedDataInputStream dis;
	private BTComm owner;
	private boolean logging;
	private String btcommName;

	// gets a file from the own device
	private FileLocalizer ownFileLocalizer = null;
	// gets the degree from the own device
	private AbstractCompassSensorWrapper ownCompassController = null;
	// gets the normalized light value from the own device
	private AbstractColorSensorWrapper ownlightValueGetter = null;

	// the degree from the remote device
	private float remoteDegree = -1;
	// the light value from the remote device
	private float lightValue = -1;
	// the file that was read from the remote device
	private String remoteFileName = null;

	// number of parts of a file
	private int numberOfParts = 0;
	// counter for counting how many parts of a file have to be read further on
	private int partOfFileLeft = 0;

	private boolean onlyOneTime = false;
	private boolean closeAcknowledged = false;
	private boolean closeRequested = false;
	private boolean closed = false;

	/**
	 * Constructor
	 * 
	 * @param btcommName
	 *            the name of the owning bt connection
	 * @param dis
	 *            the InputStream to read from
	 * @param owner
	 *            the BTComm where to give something that shall be written to
	 *            the output
	 * @param onlyOneTime
	 *            whether the read method shall be executed only one time per
	 *            call or several times (set to false)
	 * @param logging
	 *            whether anything shall be logged into the log file
	 */
	public MessageReader(String btcommName, ExtendedDataInputStream dis,
			BTComm owner, boolean onlyOneTime, boolean logging) {
		this.btcommName = btcommName;
		this.dis = dis;
		this.owner = owner;
		this.onlyOneTime = onlyOneTime;
		this.logging = logging;
	}

	/**
	 * gets the degree send from the remote device
	 * 
	 * @return the degree from the remote device
	 */
	public float getRemoteDegree() {
		return this.remoteDegree;
	}

	/**
	 * gets the light value send from the remote device
	 * 
	 * @return the light value from the remote device
	 */
	public float getRemoteLightValue() {
		return this.lightValue;
	}

	/**
	 * gets the file name of the file from the remote device
	 * 
	 * @return the file name
	 */
	public String getRemoteFileName() {
		return this.remoteFileName;
	}

	/**
	 * sets the file localizer that converts a filename to a path
	 * 
	 * @param ownFileLocalizer
	 *            the FileLocalizer
	 */
	public void setOwnFileLocalizer(FileLocalizer ownFileLocalizer) {
		this.ownFileLocalizer = ownFileLocalizer;
	}

	/**
	 * sets the compass controller where to get the degree
	 * 
	 * @param ownCompassController
	 *            the compass controller
	 */
	public void setOwnCompassController(
			AbstractCompassSensorWrapper ownCompassController) {
		this.ownCompassController = ownCompassController;
	}

	/**
	 * sets the light value getter where to get light value
	 * 
	 * @param ownlightValueGetter
	 *            the light value getter
	 */
	public void setOwnLightValueGetter(
			AbstractColorSensorWrapper ownlightValueGetter) {
		this.ownlightValueGetter = ownlightValueGetter;
	}

	/**
	 * sets a new stream where to get the remote data
	 * 
	 * @param dis
	 *            the new input stream where to get the remote data
	 */
	protected void setExtendedDataInputStream(ExtendedDataInputStream dis) {
		this.dis = dis;
	}

	@Override
	public void run() {
		this.read();
	}

	/**
	 * reads any code that was send and answers the code. All codes can be
	 * parsed (COMMAND, SEND, REQUEST and all their subcommands) and answered.
	 * Any known BTCommObserver will be notified for the corresponding event.
	 */
	protected void read() {

		try {
			byte code = 0;
			boolean previousCloseACK = false;

			do {
				try {
					code = (byte) this.dis.read();
					previousCloseACK = this.closeAcknowledged;

					if (code == -1) {
						String message = null;
						if (this.closeRequested && this.logging) {
							message = "stream closed after close was requested (without close ACK). close connection";
						}
						else if (this.closeAcknowledged && this.logging) {
							message = "stream closed after close ACK. close connection";
						}
						else {
							message = "stream closed without any close command or request. close connection";
						}

						this.closeConnection(message);
					}
					else {
						// if (this.closeRequested) { continue and wait for
						// CLOSE_ACK }
						// if (this.closeAcknowledged) { continue and look
						// whether CLOSE or CLOSE_ACK was send }
						byte upperCode = (byte) (code & BTComm.UPPER_BITS);
						byte lowerCode = (byte) (code & BTComm.LOWER_BITS);

						// switch code
						if (BTComm.COMMAND == upperCode) {
							// reads a command
							this.readCommand(lowerCode);
						}
						else if (BTComm.REQUEST == upperCode) {
							// reads a request and answers the request
							this.readRequest(lowerCode);
						}
						else if (BTComm.SEND == upperCode) {
							// reads anything that was send
							this.readSend(lowerCode);
						}
						else {
							throw new UnexpectedResponseException(
									"Code was not COMMAND or SEND or REQUEST, but "
											+ code);
						}
					}
				}
				catch (IOException e) {
					System.out.println("IOException");
					this.closeConnection("IOException, will close connection.");
				}
				catch (UnexpectedResponseException e) {
					if (this.logging) {
						logger.error(this.btcommName + "UnexpectedResponse: "
								+ e.getMessage());
					}
					System.out.println("UnexpectedResponse");

					// close connection if close was requested or ACK was send,
					// but other side gave an unexpected response
					if (previousCloseACK || this.closeRequested) {
						this
								.closeConnection("UnexpectedResponse after close was requested or acknowledged, will close connection");
					}
				}
			}
			while (!this.closed && !this.onlyOneTime);
		}
		// to avoid the weird beep
		catch (Throwable e) {
			System.out.println("MessageReader-Exc " + e.getClass());
			logger.error("MessageReader-Throwable " + e.getClass());

			this.closeConnection("closing conn after Throwable");
		}
		finally {

			try {
				// concatenate any parts of files that are not concatenated
				// already
				if (this.closed && this.partOfFileLeft != 0) {
					this.numberOfParts -= this.partOfFileLeft;
					this.concatenateFiles();
					this.owner.notifyAllObserversForEvent(BTEvent.FILE,
							this.remoteFileName);
				}
			}
			// to avoid the weird beep
			catch (Throwable e) {
				System.out.println("MessageReader-Exc " + e.getClass());
				logger.error("MessageReader-Throwable " + e.getClass());
			}
		}
	}

	/**
	 * reads a code that starts with the SEND command and gets whatever will be
	 * send afterwards
	 * 
	 * @param code
	 *            the byte code that was send
	 * @throws UnexpectedResponseException
	 *             if code was not known
	 * @throws IOException
	 *             if anything could not be catched from input stream
	 */
	private void readSend(byte code) throws UnexpectedResponseException,
			IOException {
		code = (byte) (code & BTComm.LOWER_BITS);

		if (code == BTComm.LIGHT) {
			this.lightValue = this.readLightValue();
			this.owner.notifyAllObserversForEvent(BTEvent.LIGHT,
					this.lightValue);
		}
		else if (code == BTComm.DEGREE) {
			this.remoteDegree = this.readDegree();
			this.owner.notifyAllObserversForEvent(BTEvent.DEGREE,
					this.remoteDegree);
		}
		else if (code == BTComm.FILE_EMPTY || code == BTComm.FILE_PART
				|| code == BTComm.FILE_WHOLE) {
			this.readFile(code);
		}
		else {
			throw new UnexpectedResponseException(
					"Code was not FILE or DEGREE or LIGHT VALUE, but "
							+ Byte.toString(code));
		}
	}

	/**
	 * reads a file from the input stream (reads file name first, then length
	 * and the file 128 bytes per read) and stores it at the location of the
	 * send file name. It also stores the name of the file in the attribute
	 * {@link #remoteFileName}. If the name of the remote file is the empty
	 * String, then nothing will be read from the input stream.
	 * 
	 * @param code
	 *            the code that was send (it says whether no file is send, parts
	 *            are send or the file as a whole is send)
	 * @throws IOException
	 *             if anything could not be read from inputstream
	 * @throws UnexpectedResponseException
	 *             if file size was negative
	 */
	private void readFile(byte code) throws IOException,
			UnexpectedResponseException {

		switch (code) {
		case BTComm.FILE_EMPTY:
			this.remoteFileName = "";
			this.owner.notifyAllObserversForEvent(BTEvent.FILE,
					this.remoteFileName);
			break;

		case BTComm.FILE_WHOLE:
			this.readFileWithoutHeader();
			this.owner.notifyAllObserversForEvent(BTEvent.FILE,
					this.remoteFileName);
			break;
		case BTComm.FILE_PART:
			// read number of parts
			int currentNumberOfParts = this.dis.readInt();

			// a new file is send
			if (this.partOfFileLeft == 0) {
				this.numberOfParts = currentNumberOfParts;
				this.partOfFileLeft = currentNumberOfParts;
			}
			// a part of a file is send
			else {
				// the number of parts changed during sending the parts
				if (this.numberOfParts != currentNumberOfParts) {
					int diff = this.numberOfParts - currentNumberOfParts;
					this.numberOfParts = currentNumberOfParts;
					this.partOfFileLeft -= diff;
				}
				// else: everything is ok, read next part of file
			}

			// read file
			this.readFileWithoutHeader();

			this.partOfFileLeft--;

			// if no more files are being send, concatenate parts
			if (this.partOfFileLeft == 0) {
				this.concatenateFiles();

				this.partOfFileLeft = 0;
				this.numberOfParts = 0;
				this.owner.notifyAllObserversForEvent(BTEvent.FILE,
						this.remoteFileName);
			}
			break;
		default:
			throw new UnexpectedResponseException(
					"Code was neither EMPTY_FILE nor FILE_WHOLE nor FILE_PART, but "
							+ Byte.toString(code));
		}

		if (this.logging) {
			logger.info(this.btcommName + "file " + this.remoteFileName
					+ " rcv");
		}

	}

	/**
	 * reads a file from the bluetooth input stream without reading the file
	 * header (SEND-FILE). It starts with reading the file name, file size and
	 * then reading the file 128 bytes per step. It also writes it to a file
	 * with the read filename.
	 * 
	 * @throws IOException
	 *             if something could not be read from input stream
	 * @throws UnexpectedResponseException
	 *             if the file size is negative
	 */
	private void readFileWithoutHeader() throws IOException,
			UnexpectedResponseException {
		// read file name
		this.remoteFileName = this.dis.readString();

		if (this.remoteFileName != null && this.remoteFileName.length() != 0) {

			// localizes file on hard disc
			this.remoteFileName = this.ownFileLocalizer
					.getPathToFile(this.remoteFileName);

			FileOutputStream fileOutput = this
					.openFileOutputStream(this.remoteFileName);

			// read file size
			int fileSize = this.dis.readInt();
			if (fileSize < 0) {
				throw new UnexpectedResponseException(
						"Send fileSize was negative");
			}
			this.getFileFromInputToOutput(fileSize, this.dis, fileOutput);

			this.closeFileOutputStream(this.remoteFileName, fileOutput);
		}
	}

	/**
	 * concatenates several files that are named like the private variable
	 * remoteFileName with a trailing number. It concatenates until
	 * numberOfParts files.
	 */
	private void concatenateFiles() {

		this.remoteFileName = this.remoteFileName.substring(0,
				this.remoteFileName.lastIndexOf(Integer
						.toString(this.numberOfParts - 1)));

		FileOutputStream fileOutput = this
				.openFileOutputStream(this.remoteFileName);

		// read each file part
		for (int j = 0; j < this.numberOfParts; j++) {
			try {
				// read file
				File file = new File(this.remoteFileName + j);

				FileInputStream input = new FileInputStream(file);
				int fileSize = input.available();

				this.getFileFromInputToOutput(fileSize, input, fileOutput);
				input.close();

				file.delete();
			}
			catch (IOException e) {
				if (this.logging) {
					logger.error(this.btcommName
							+ "IOException while filepart "
							+ this.remoteFileName + j);
				}
			}
		}
		this.closeFileOutputStream(this.remoteFileName, fileOutput);
	}

	/**
	 * opens a file output stream for the given file name
	 * 
	 * @param fileName
	 *            the file name for which to open an output stream
	 * @return the file output stream to the given file
	 */
	private FileOutputStream openFileOutputStream(String fileName) {
		// open/create new file
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		}
		catch (IOException e) {
			System.out.println("Could not create file " + this.remoteFileName);
			if (this.logging) {
				logger.error(this.btcommName + "could not create file "
						+ this.remoteFileName);
			}
		}
		return new FileOutputStream(file);
	}

	/**
	 * writes file to the fileoutput stream that is send via the given input
	 * stream and has the size fileSize
	 * 
	 * @param fileSize
	 *            the size of the send file
	 * @param input
	 *            the input stream from which to read the file
	 * @param output
	 *            the output stream where to write the file
	 * @throws IOException
	 *             if something went wrong with the input stream
	 */
	private void getFileFromInputToOutput(int fileSize, InputStream input,
			FileOutputStream output) throws IOException {

		// read and write: 128 bytes each
		byte[] filePart;
		int i = 128;
		for (; i < fileSize; i = i + 128) {
			filePart = new byte[128];
			this.readFilePart(input, output, filePart);
		}

		// read and write last few bytes
		filePart = new byte[fileSize - i + 128];
		this.readFilePart(input, output, filePart);
	}

	/**
	 * reads some bytes into the array filePart and writes it to the fileOutput
	 * 
	 * @param input
	 *            the input stream to read from
	 * @param fileOutput
	 *            the file output stream to write to
	 * @param filePart
	 *            the array where to store the read bytes
	 * @throws IOException
	 *             if an exception occurred while reading bytes
	 */
	private void readFilePart(InputStream input, FileOutputStream fileOutput,
			byte[] filePart) throws IOException {
		input.read(filePart);
		try {
			fileOutput.write(filePart);
			fileOutput.flush();
		}
		catch (IOException e) {
			System.out.println("IOException while writing file");
			if (this.logging) {
				logger
						.error(this.btcommName
								+ "IOException while writing file");
			}
		}
	}

	/**
	 * closes the file output stream for the given file name
	 * 
	 * @param fileName
	 *            the file name for which this is an output stream
	 * @param fileOutput
	 *            the output stream to close
	 */
	private void closeFileOutputStream(String fileName,
			FileOutputStream fileOutput) {
		// close file handle
		try {
			fileOutput.close();
		}
		catch (IOException e) {
			System.out.println("Could not close file handle " + fileName);
			if (this.logging) {
				logger.error(this.btcommName + "could not close filehandle "
						+ fileName);
			}
		}
	}

	/**
	 * reads the degree from the input stream
	 * 
	 * @return the read degree
	 * @throws IOException
	 *             if the degree couldn't be read from the input stream
	 * @throws UnexpectedResponseException
	 *             if the received degree was negative or bigger than 360
	 */
	private float readDegree() throws IOException, UnexpectedResponseException {
		float degree = this.dis.readFloat();

		if (degree < 0) {
			this.owner.notifyAllObserversForEvent(BTEvent.DEGREE);
			throw new UnexpectedResponseException("degree was negative");
		}
		else if (degree > 360) {
			this.owner.notifyAllObserversForEvent(BTEvent.DEGREE);
			throw new UnexpectedResponseException("degree was bigger than 360");
		}
		else {
			if (this.logging) {
				logger.info(this.btcommName + "degree " + degree + " rcv");
			}
			return degree;
		}
	}

	/**
	 * reads the light value from the input stream
	 * 
	 * @return the read light value
	 * @throws IOException
	 *             if the light value couldn't be read from the input stream
	 * @throws UnexpectedResponseException
	 *             if the received light value was negative or bigger than 100
	 */
	private float readLightValue() throws IOException,
			UnexpectedResponseException {
		float lightValue = this.dis.readFloat();

		if (lightValue < 0) {
			this.owner.notifyAllObserversForEvent(BTEvent.LIGHT);
			throw new UnexpectedResponseException("light value was negative");
		}
		else if (lightValue > 100) {
			this.owner.notifyAllObserversForEvent(BTEvent.LIGHT);
			throw new UnexpectedResponseException(
					"light value was bigger than 100");
		}
		else {
			if (this.logging) {
				logger.info(this.btcommName + "lightvalue " + lightValue
						+ " rcv");
			}
			return lightValue;
		}
	}

	/**
	 * reads any COMMAND (starts with COMMAND sequence) that was send and
	 * answers the command (executes the command or sends something upon the
	 * request of something or reads something from the input stream upon the
	 * send of something)
	 * 
	 * @param code
	 *            the byte code containing the command (not shortened yet)
	 * @throws UnexpectedResponseException
	 *             if the byte code is unknown
	 * @throws IOException
	 *             if nothing could be read from the input stream
	 */
	private void readCommand(byte code) throws UnexpectedResponseException,
			IOException {
		code = (byte) (code & BTComm.LOWER_BITS);

		if (code == BTComm.CLOSE_VIRTUAL && !this.closed) {
			if (this.partOfFileLeft != 0) {
				try {
					this.owner.writeCommand(BTComm.CLOSE_VIRTUAL_DECLINE);
					if (this.logging) {
						logger
								.warn(this.btcommName
										+ "close command rcv while waiting for files. send close decline");
					}
				}
				catch (ConnectionClosedException e) {
					// should not happen as reader controls closing
					if (this.logging) {
						logger.error(this.btcommName
								+ "in decline close while waiting for files: "
								+ e.getMessage());
					}
				}
				catch (QueueBlockedException e) {
					// should not happen
					if (this.logging) {
						logger.error(this.btcommName
								+ "in decline close while waiting for files: "
								+ e.getMessage());
					}
				}
			}
			else if (this.closeAcknowledged) {
				this
						.closeConnection("close command received after close ACK. close connection");
			}
			else {
				if (this.closeRequested) {
					this.owner.closeNotSuccessful();
				}
				try {
					this.owner.writeCommand(BTComm.CLOSE_VIRTUAL_ACK, true);
					this.closeAcknowledged = true;
					this
							.closeConnection("close command received. send close ACK and close");
				}
				catch (ConnectionClosedException e) {
					// should not happen as reader controls closing
					if (this.logging) {
						logger.error(this.btcommName
								+ "in close ACK after close was requested: "
								+ e.getMessage());
					}
				}
				catch (QueueBlockedException e) {
					// should not happen
					if (this.logging) {
						logger.error(this.btcommName
								+ "in close ACK after close was requested: "
								+ e.getMessage());
					}
				}
			}
		}
		else if (code == BTComm.CLOSE_VIRTUAL_ACK) {
			if (this.partOfFileLeft != 0) {
				this
						.closeConnection("close ACK received while waiting for files. close however...");
			}
			else if (this.closeRequested || this.closeAcknowledged) {
				this.closeConnection("close ACK received. close connection");
			}
			else {
				this
						.closeConnection("close ACK received without requesting close. close connection");
			}
		}
		else if (code == BTComm.CLOSE_VIRTUAL_DECLINE && this.closeRequested) {
			this.owner.closeNotSuccessful();
			this.closeRequested = false;
			this.owner
					.notifyAllObserversForEvent(BTEvent.CLOSE_REQUEST_DECLINED);
			throw new UnexpectedResponseException(
					"Close request was not acknowledged");
		}
		else {
			throw new UnexpectedResponseException("Command was not expected: "
					+ Byte.toString(code));
		}
	}

	/**
	 * closes this connection completely (input stream and connection itself)
	 * and notifies all observers. The given message will be logged.
	 * 
	 * @param message
	 *            the message to log
	 */
	private void closeConnection(String message) {
		try {
			this.dis.close();
			this.owner.close();
			this.closed = true;

			if (this.logging && message != null) {
				logger.info(this.btcommName + message);
			}
			System.out.println("conn closed");

			this.owner.notifyAllObserversForClose();

		}
		catch (IOException e2) {
			System.out.println("conn not closed");
			if (this.logging) {
				logger.error(this.btcommName + "conn not closed");
			}
		}
	}

	/**
	 * reads a code that starts with the REQUEST command. It then reads from the
	 * code what was requested and sends the requested data. If the requested
	 * data cannot be got, for a file only the empty String as a file name will
	 * be send and -1 for the degree.
	 * 
	 * @param code
	 *            the code containing the request
	 * @throws IOException
	 *             if the the requested file could not be read from the hard
	 *             disc or the name of the file could not be read from the input
	 *             stream
	 * @throws UnexpectedResponseException
	 *             if the given code was not FILE or DEGREE
	 */
	private void readRequest(byte code) throws UnexpectedResponseException,
			IOException {
		code = (byte) (code & BTComm.LOWER_BITS);

		if (code == BTComm.LIGHT) {
			this.writeLightValue();
		}
		else if (code == BTComm.DEGREE) {
			this.writeDegree();
		}
		else if (code == BTComm.FILE) {
			String requestedFileName = this.dis.readString();
			this.writeFile(requestedFileName);
		}
		else {
			throw new UnexpectedResponseException(
					"Code was not FILE or DEGREE or LIGHT_VALUE, but "
							+ Byte.toString(code));
		}
	}

	/**
	 * writes the requested file to the owner
	 * 
	 * @param requestedFileName
	 *            the file to write
	 * @throws FileNotFoundException
	 *             thrown if the file cannot be found
	 * @throws IOException
	 *             thrown if the file could not be read
	 */
	private void writeFile(String requestedFileName)
			throws FileNotFoundException, IOException {
		String fileName;

		if (this.ownFileLocalizer == null) {
			fileName = null;
		}
		else {
			fileName = this.ownFileLocalizer.getPathToFile(requestedFileName);
			if (fileName == null) {
				throw new FileNotFoundException("File was not found: "
						+ requestedFileName);
			}
		}

		if (this.logging) {
			logger.debug(this.btcommName + "file " + fileName + " req rcv");
		}

		try {
			this.owner.writeFile(fileName);
		}
		catch (ConnectionClosedException e) {
			// should not happen as reader controls closing
			if (this.logging) {
				logger.error(this.btcommName + "in writeFile after close req: "
						+ e.getMessage());
			}
		}
		catch (QueueBlockedException e) {
			// should not happen
			if (this.logging) {
				logger.error(this.btcommName + "in writeFile after close req: "
						+ e.getMessage());
			}
		}
	}

	/**
	 * writes the current compass data to the owner
	 */
	private void writeDegree() {
		float degree;

		if (this.ownCompassController == null) {
			degree = -1;
		}
		else {
			try {
				degree = this.ownCompassController.getDegree();
			}
			catch (SensorException e) {
				degree = -1;
			}
		}

		if (this.logging) {
			logger.debug(this.btcommName + "degree req rcv");
		}

		try {
			this.owner.writeDegree(degree);
		}
		catch (ConnectionClosedException e) {
			// should not happen as reader controls closing
			if (this.logging) {
				logger.error(this.btcommName
						+ "in writeDegree after close req: " + e.getMessage());
			}
		}
		catch (QueueBlockedException e) {
			// should not happen
			if (this.logging) {
				logger.error(this.btcommName
						+ "in writeDegree after close req: " + e.getMessage());
			}
		}
	}

	/**
	 * writes the current light value to the owner
	 */
	private void writeLightValue() {
		float lightValue;

		if (this.ownlightValueGetter == null) {
			lightValue = -1;
		}
		else {
			try {
				lightValue = this.ownlightValueGetter.getLightValue();
			}
			catch (SensorException e) {
				lightValue = -1;
			}
		}

		if (this.logging) {
			logger.debug(this.btcommName + "lightvalue req rcv");
		}

		try {
			this.owner.writeLightValue(lightValue);
		}
		catch (ConnectionClosedException e) {
			// should not happen as reader controls closing
			if (this.logging) {
				logger.error(this.btcommName
						+ "in writeLightValue after close req: "
						+ e.getMessage());
			}
		}
		catch (QueueBlockedException e) {
			// should not happen
			if (this.logging) {
				logger.error(this.btcommName
						+ "in writeLightValue after close req: "
						+ e.getMessage());
			}
		}
	}

	/**
	 * sets the parameter that a close request was send to the remote device
	 * 
	 * @param closeRequested
	 *            whether a close is requested
	 */
	protected void setCloseRequested(boolean closeRequested) {
		this.closeRequested = closeRequested;
	}

}
