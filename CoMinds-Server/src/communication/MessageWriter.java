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
import java.io.IOException;
import java.io.OutputStream;

import common.Writer;
import common.exceptions.QueueBlockedException;

/**
 * This class writes anything that was given to the bluetooth output stream.
 * Don't use any private methods! The public methods will send the message
 * header first and then the message.
 * 
 * @author Annabelle Klarl
 */
class MessageWriter {

	protected Writer writer;

	/**
	 * Constructor
	 * 
	 * @param output
	 *            the OutputStream to which to write
	 * @param queueSize
	 *            an int for the size of the message buffer
	 */
	public MessageWriter(OutputStream output, int queueSize) {
		this.writer = new Writer(output, queueSize, true);
	}

	/**
	 * Constructor
	 * 
	 * @param writer
	 *            the writer which writes any message to the output (specified
	 *            in the writer)
	 */
	public MessageWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * starts the writer thread behind this class that actually writes the
	 * message to the output
	 */
	public void start() {
		Thread writerThread = new Thread(this.writer, "BTWriterThread");
		writerThread.start();
	}

	/**
	 * writes a file with the given fileName to the message queue (with message
	 * header, file name and file length). This method will split the file in
	 * parts with 4095 bytes each. If the fileName is null nothing except the
	 * file name as an empty String (and the message header) will be written to
	 * the output.
	 * 
	 * @param fileName
	 *            a String for the name of the file to be send
	 * @throws FileNotFoundException
	 *             if the file with this name was not found on the hard disc
	 * @throws IOException
	 *             if the file could not be read from hard disc
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void writeFile(String fileName) throws FileNotFoundException,
			IOException, QueueBlockedException {

		// message header
		byte sendCommandByte;

		// content (filename, filelength and file)
		byte[] messageBytes;

		if (fileName == null) {
			// write send command
			sendCommandByte = this.getSendCommand(BTComm.EMPTY_FILE);

			// concat messages
			messageBytes = common.WriterUtils.concatArrays(sendCommandByte);
		}
		else {
			// read file
			FileInputStream input = new FileInputStream(new File(fileName));
			int availableBytes = input.available();

			// send file in one part
			int partSize = 4095;
			if (availableBytes <= partSize) {

				// wait until there is enough space to store the byte array of
				// this file
				this.waitForFreeHeap(availableBytes);

				// write send command: whole
				sendCommandByte = this.getSendCommand(BTComm.FILE_WHOLE);

				// get file
				messageBytes = this.getFilePart(sendCommandByte, fileName,
						availableBytes, input);
			}
			// send the file in parts
			else {
				// write send command: parts
				sendCommandByte = this.getSendCommand(BTComm.FILE_PART);
				int numberOfParts = availableBytes / partSize + 1;

				// read and write: 4095 bytes each
				int i = partSize;
				int j = 0;
				for (; i < availableBytes; i = i + partSize, j++) {
					// wait until there is enough space to store the byte array
					// of this file
					this.waitForFreeHeap(partSize);

					messageBytes = this.getFilePart(sendCommandByte,
							numberOfParts, fileName + j, partSize, input);
					this.write(messageBytes);
				}

				int lengthLastPart = availableBytes - i + partSize;

				// wait until there is enough space to store the byte array of
				// this file
				this.waitForFreeHeap(lengthLastPart);

				messageBytes = this.getFilePart(sendCommandByte, numberOfParts,
						fileName + j, lengthLastPart, input);
			}
		}

		// write message to bt queue
		this.write(messageBytes);

	}

	/**
	 * this methods only returns if there are at least size+50 bytes free space
	 * of memory on the heap. It will also return if the messageQueue is empty.
	 * Then there is not enough space on the heap at all!
	 * 
	 * @param size
	 *            how many bytes are needed (will be increased by 50)
	 */
	private void waitForFreeHeap(int size) {

		long now = System.currentTimeMillis();
		while ((size + 50 > Runtime.getRuntime().freeMemory() || !this.writer
				.isWaitingForMessages())
				// if there is not enough space left (or the garbage collector
				// does not work
				&& (System.currentTimeMillis() - now < 3000)) {
			Thread.yield();
		}

	}

	/**
	 * see documenation
	 * {@link #getFilePart(byte, int, String, int, FileInputStream)}. Here just
	 * one part of file is send. That means the file is send as a whole.
	 */
	private byte[] getFilePart(byte command, String fileName, int partSize,
			FileInputStream input) throws IOException {
		return this.getFilePart(command, 0, fileName, partSize, input);
	}

	/**
	 * gets the bytes for a part of a file given by filename. This method return
	 * the command concantenated with the number of file parts to send, the name
	 * for this part of file (that is send), the size of this part and the part
	 * of the file itself.
	 * 
	 * @param command
	 *            the send command
	 * @param numberOfParts
	 *            how many parts of the file are send
	 * @param fileName
	 *            the name of the whole file
	 * @param partSize
	 *            the size of this part of file
	 * @param input
	 *            the input stream where to read the file from
	 * @return all the information about this part of file as a byte array
	 * @throws IOException
	 *             if something could not be read from this file
	 */
	private byte[] getFilePart(byte command, int numberOfParts,
			String fileName, int partSize, FileInputStream input)
			throws IOException {

		// number of parts to array
		byte[] numberOfPartsBytes;
		if (numberOfParts == 0) {
			numberOfPartsBytes = new byte[0];
		}
		else {
			numberOfPartsBytes = CommunicationUtils.convertIntToByteArray(numberOfParts);
		}

		// file name to array
		byte[] fileNameBytes = CommunicationUtils.convertStringToByteArray(fileName);

		// file length as array
		byte[] fileLengthBytes = CommunicationUtils.convertIntToByteArray(partSize);

		int length = 1 + numberOfPartsBytes.length + fileNameBytes.length
				+ fileLengthBytes.length + partSize;
		byte[] messageBytes = common.WriterUtils.concatArraysIntoArrayWithLength(
				length, command, numberOfPartsBytes, fileNameBytes,
				fileLengthBytes);

		input.read(messageBytes, length - partSize, partSize);
		return messageBytes;
	}

	/**
	 * writes the given degree to the message queue (with message header)
	 * 
	 * @param degrees
	 *            the degree to be send
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void writeDegree(float degrees) throws QueueBlockedException {
		// message header
		byte sendCommandByte = this.getSendCommand(BTComm.DEGREE);

		// degree as byte array
		byte[] degreeBytes = CommunicationUtils.convertFloatToByteArray(degrees);

		// write message to bt queue
		this.write(common.WriterUtils.concatArrays(sendCommandByte, degreeBytes));
	}

	/**
	 * writes the given light value to the message queue (with message header)
	 * 
	 * @param lightValue
	 *            the light value to be send
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void writeLightValue(float lightValue) throws QueueBlockedException {
		// message header
		byte sendCommandByte = this.getSendCommand(BTComm.LIGHT);

		// degree as byte array
		byte[] lightBytes = CommunicationUtils.convertFloatToByteArray(lightValue);

		// write message to bt queue
		this.write(common.WriterUtils.concatArrays(sendCommandByte, lightBytes));
	}

	/**
	 * writes a SEND-command to the output. This method will take a command and
	 * will return it as a SEND command.
	 * 
	 * @param command
	 *            a byte for the command to send
	 * @return the given command with the header for send as a byte
	 */
	private byte getSendCommand(byte command) {
		return (byte) (BTComm.SEND | command);
	}

	/**
	 * writes REQUEST-command to the ouput. This method will take a command and
	 * will return it as a REQUEST command.
	 * 
	 * @param command
	 *            a byte for the command to send
	 * @return the command with the header for request as a byte
	 */
	private byte getRequestCommand(byte command) {
		return (byte) (BTComm.REQUEST | command);
	}

	/**
	 * see documentation {@link BTWriter#writeCommand(byte, boolean)}. The
	 * message queue will not be blocked afterwards.
	 * 
	 * @param command
	 *            a byte for the command to write
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void writeCommand(byte command) throws QueueBlockedException {
		this.writeCommand(command, false);
	}

	/**
	 * writes a command to the output (the bits for COMMAND will be added to the
	 * parameter command). This method asks whether to block the writer thread
	 * afterwards.
	 * 
	 * @param command
	 *            a byte for the command to write
	 * @param blocked
	 *            whether to block the message queue afterwards
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void writeCommand(byte command, boolean blocked)
			throws QueueBlockedException {
		this.write((byte) (BTComm.COMMAND | command), blocked);
	}

	/**
	 * writes a message to the writer. This method asks whether to block the
	 * message buffer afterwards.
	 * 
	 * @param message
	 *            the message itself as a byte array
	 * @param blocked
	 *            whether to block the writer after writing this message or not
	 * @return returns whether the message was put into the writing queue or not
	 *         (if not than the output is full and nothing can be written to it
	 *         any more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	protected boolean write(byte[] message, boolean blocked)
			throws QueueBlockedException {
		return this.writer.write(message, blocked);
	}

	/**
	 * see documentation {@link MessageWriter#write(byte[], boolean)}
	 * 
	 * @param message
	 *            the message itself as a byte array
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	protected boolean write(byte[] message) throws QueueBlockedException {
		return this.write(message, false);
	}

	/**
	 * writes a single byte message to the writer. This method asks whether to
	 * block the message buffer afterwards.
	 * 
	 * @param message
	 *            the message itself as a byte
	 * @param blocked
	 *            whether to block the writer after writing this message or not
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	protected boolean write(byte message, boolean blocked)
			throws QueueBlockedException {
		return this.write(new byte[] { message }, blocked);
	}

	/**
	 * see documentation {@link MessageWriter#write(byte, boolean)}
	 * 
	 * @param message
	 *            the message itself as a byte
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	protected boolean write(byte message) throws QueueBlockedException {
		return this.write(message, false);
	}

	/**
	 * requests to get a file with the given fileName from the remote device.
	 * 
	 * @param fileName
	 *            the name of file to get from the remote device and also the
	 *            location where to store the received file
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void requestFile(String fileName) throws QueueBlockedException {
		// message header (request for file)
		byte requestCommandByte = this.getRequestCommand(BTComm.FILE);

		// file name as array
		byte[] fileNameBytes = CommunicationUtils.convertStringToByteArray(fileName);

		// write message to bt queue
		this
				.write(common.WriterUtils.concatArrays(requestCommandByte,
						fileNameBytes));
	}

	/**
	 * requests to get the degree from the remote device.
	 * 
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void requestDegree() throws QueueBlockedException {
		// send message header (request for degree)
		byte requestCommandByte = this.getRequestCommand(BTComm.DEGREE);

		// write message to bt queue
		this.write(requestCommandByte);
	}

	/**
	 * requests to get the light value from the remote device.
	 * 
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void requestLightValue() throws QueueBlockedException {
		// send message header (request for degree)
		byte requestCommandByte = this.getRequestCommand(BTComm.LIGHT);

		// write message to bt queue
		this.write(requestCommandByte);
	}

	/**
	 * requests to close the BT connection and blocks the writer thread after
	 * writing the close message
	 * 
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void requestDirectClose() throws QueueBlockedException {
		// write close request;
		this.writeCommand(BTComm.CLOSE_VIRTUAL, true);
	}

	/**
	 * stops the writer thread
	 */
	public void stop() {
		this.writer.stop();
	}

	/**
	 * returns whether the writer thread has written all data to the output and
	 * is closed now
	 * 
	 * @return whether the writer thread is finished
	 */
	public boolean isFinished() {
		return this.writer.isFinished();
	}

	/**
	 * deblocks the queue of the writer thread
	 */
	public void deblockQueue() {
		this.writer.deblockQueue();
	}
}
