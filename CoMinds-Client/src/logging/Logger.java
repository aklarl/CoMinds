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

/**
 * With this singleton logging is possible. There are four log-levels (DEBUG,
 * WARNING, INFO and ERROR). The messages will be written to a Buffer where a
 * new Thread will fetch the messages and write them to a logfile.
 * 
 * @author Annabelle Klarl
 */
public class Logger {

	/**
	 * The different log-levels (DEBUG, WARNIN and ERROR) and their String
	 * representations
	 * 
	 * @author Annabelle Klarl
	 */
	public enum LogLevel {
		DEBUG("D"), WARNING("W"), INFO("I"), ERROR("E");

		private String stringRepresentation;

		private LogLevel(String stringRepresentation) {
			this.stringRepresentation = stringRepresentation;
		}

		@Override
		public String toString() {
			return this.stringRepresentation;
		}
	}

	private LogLevel level;
	private final LogWriter worker;
	private static Logger singleton;
	private Object sync = new Object();

	/**
	 * Constructor
	 * 
	 * @param fileName
	 *            the name or location of the logfile
	 * @param level
	 *            the log level
	 * @param queueSize
	 *            the size of the Buffer for the log messages
	 * @param transfer
	 *            whether the logFile should be transfered to the PC
	 */
	private Logger(String fileName, LogLevel level, int queueSize,
			boolean transfer) {
		this.level = level;
		this.worker = new LogWriter(fileName, queueSize, transfer);
		new Thread(this.worker, "LoggerWriterThread").start();
	}

	/**
	 * returns the single logger if the logger was already instantiated or
	 * returns a new logger instance at the specified location fileName for the
	 * specified LogLevel. The queueSize is the length of the queue for buffered
	 * messages not yet written to file. The boolean flag transfer means whether
	 * the log-file should be transfered to the PC afterwards or not.
	 * 
	 * @param fileName
	 *            the name of the log file to write
	 * @param level
	 *            the level of the logger
	 * @param queueSize
	 *            the size of the message buffer for writing to the log file
	 * @param transfer
	 *            whether to transfer the file to the PC
	 * @return the logger
	 */
	public static Logger getLogger(String fileName, LogLevel level,
			int queueSize, boolean transfer) {
		if (singleton == null) {
			singleton = new Logger(fileName, level, queueSize, transfer);
		}
		return singleton;
	}

	/**
	 * returns the single logger if the logger was already instantiated or
	 * creates a new logger instance with fileName log.txt, LogLevel ERROR and
	 * queueSize 100. The log-file will automatically transfered to the PC.
	 * 
	 * @return a logger
	 */
	public static Logger getLogger() {
		if (singleton == null) {
			singleton = new Logger("log.txt", LogLevel.ERROR, 100, true);
		}
		return singleton;
	}

	/**
	 * writes the given message together with the system time to the message
	 * queue
	 * 
	 * @param level
	 *            the log level of this message
	 * @param message
	 *            the message itself
	 */
	private void writeMessage(LogLevel level, String message) {
		synchronized (this.sync) {
			this.worker.write(level, System.currentTimeMillis(), message);
		}
	}

	/**
	 * writes debug message if the log level of the logger is smaller or equal
	 * than DEBUG log level
	 * 
	 * @param message
	 *            the message to write
	 */
	public void debug(String message) {
		if (LogLevel.DEBUG.compareTo(this.level) >= 0) {
			this.writeMessage(LogLevel.DEBUG, message);
		}
	}

	/**
	 * writes warn message if the log level of the logger is smaller or equal
	 * than WARNING log level
	 * 
	 * @param message
	 *            the message to write
	 */
	public void warn(String message) {
		if (LogLevel.WARNING.compareTo(this.level) >= 0) {

			this.writeMessage(LogLevel.WARNING, message);
		}
	}

	/**
	 * writes info message if the log level of the logger is smaller or equal
	 * than INFO log level
	 * 
	 * @param message
	 *            the message to write
	 */
	public void info(String message) {
		if (LogLevel.INFO.compareTo(this.level) >= 0) {
			this.writeMessage(LogLevel.INFO, message);
		}
	}

	/**
	 * writes error message if the log level of the logger is smaller or equal
	 * than ERROR log level
	 * 
	 * @param message
	 *            the message to write
	 */
	public void error(String message) {
		if (LogLevel.ERROR.compareTo(this.level) >= 0) {
			this.writeMessage(LogLevel.ERROR, message);
		}
	}

	/**
	 * stops the logging (or writing to file) thread
	 */
	public void stopLogging() {
		synchronized (this.sync) {
			this.worker.write(LogLevel.INFO, System.currentTimeMillis(),
					"logging stopped", true);
			this.worker.stop();
		}
		synchronized (this.worker) {
			if (!this.worker.isFinished()) {
				try {
					this.worker.wait();
				}
				catch (InterruptedException e) {
				}
			}
		}
	}
}
