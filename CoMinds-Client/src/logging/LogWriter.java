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
package logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import behaviourmodel.BehaviorUtils;

import logging.Logger.LogLevel;

import common.Writer;
import common.exceptions.QueueBlockedException;
import communication.NXTConnectionManager;

/**
 * private class that holds a message queue with all log message to be written
 * to file and which writes the log messages to file in a new thread
 * 
 * @author Annabelle Klarl
 */
class LogWriter extends Writer {

	private boolean transfer;
	private String fileName;

	/**
	 * Constructor
	 * 
	 * @param fileName
	 *            the name of the logfile
	 * @param queueSize
	 *            the size of the buffer for log messages
	 * @param transfer
	 *            whether the logFile should be transfered to the PC
	 */
	public LogWriter(String fileName, int queueSize, boolean transfer) {
		super(initFileOutput(fileName), queueSize);
		this.fileName = fileName;
		this.transfer = transfer;
	}

	/**
	 * initializes the file handler
	 * 
	 * @param fileName
	 *            the name of the file to write
	 * @return a handler for the file
	 */
	private static FileOutputStream initFileOutput(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		}
		catch (IOException e) {
			System.out.println("No logger!");
		}

		// for freeing the resources if a big file was deleted
		File.freeMemory();

		return new FileOutputStream(file);
	}

	/**
	 * see documentation
	 * {@link LogWriter#write(LogLevel, Long, String, boolean)}. This method
	 * will not block the writing queue afterwards.
	 * 
	 * @param level
	 *            the log-level of this message
	 * @param time
	 *            the timestamp of this message
	 * @param message
	 *            the message itself
	 */
	public void write(LogLevel level, Long time, String message) {
		this.write(level, time, message, false);
	}

	/**
	 * writes a log message to the log message buffer/queue
	 * 
	 * @param level
	 *            the log-level of this message
	 * @param time
	 *            the timestamp of this message
	 * @param message
	 *            the message itself
	 * @param blocked
	 *            whether to block the message queue after logging this message
	 */
	public void write(LogLevel level, Long time, String message, boolean blocked) {
		byte[] levelBytes = (level.toString() + " ").getBytes(null);
		byte[] timeBytes = (time.toString() + " ").getBytes(null);
		byte[] messageBytes = (message + "\n").getBytes(null);

		byte[] byteMessage = common.WriterUtils.concatArrays(levelBytes, timeBytes,
				messageBytes);

		try {
			this.write(byteMessage, blocked);
		}
		catch (QueueBlockedException e) {
			System.out
					.println("Log after logging stopped! Message: " + message);
		}
	}

	@Override
	public void run() {
		super.run();

		// wait for stop command
		synchronized (this) {
			while (!this.stopped) {
				try {
					this.wait();
				}
				catch (InterruptedException e) {
				}
			}
		}

		if (this.transfer) {
			NXTConnectionManager.transferFileToPC(this.fileName);
		}
	}

	@Override
	public boolean tooFewSpaceInOutput(byte[] message) {
		return message.length > File.freeMemory();
	}
}
