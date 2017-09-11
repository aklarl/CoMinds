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

import logging.Logger.LogLevel;

/**
 * This is a container for a log message. It contains the message itself, the
 * log-level of the message and the timestamp when the log message was created.
 * 
 * @author Annabelle Klarl
 */
public class LogMessage {

	private LogLevel level;
	private Long time;
	private String message;

	/**
	 * Constructor
	 * 
	 * @param level
	 *            the log-level of the log message
	 * @param time
	 *            the timestamp when the log message was created
	 * @param message
	 *            the message itself
	 */
	public LogMessage(LogLevel level, Long time, String message) {
		this.level = level;
		this.message = message;
		this.time = time;
	}

	/**
	 * gets the log level of the log message
	 * 
	 * @return the log level of the log message
	 */
	public LogLevel getLevel() {
		return this.level;
	}

	/**
	 * gets the log level as a byte array so it can be written easily to an
	 * output stream
	 * 
	 * @return log level as byte array
	 */
	public byte[] getLevelInBytes() {
		return (this.level.toString() + " ").getBytes(null);
	}

	/**
	 * gets the timestamp of the log message
	 * 
	 * @return the timestamp
	 */
	public Long getTime() {
		return this.time;
	}

	/**
	 * gets the timestamp as a byte array so it can be written easily to an
	 * output stream
	 * 
	 * @return timestamp as byte array
	 */
	public byte[] getTimeInBytes() {
		return (this.time.toString() + " ").getBytes(null);
	}

	/**
	 * gets the message of this log message
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * gets the message as a byte array so it can be written easily to an output
	 * stream
	 * 
	 * @return message as byte array
	 */
	public byte[] getMessageInBytes() {
		return (this.message + " ").getBytes(null);
	}

	@Override
	public String toString() {
		return this.level.toString() + " " + this.time.toString() + " "
				+ this.message;
	}

}
