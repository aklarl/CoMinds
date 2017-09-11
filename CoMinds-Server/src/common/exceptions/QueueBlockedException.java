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
package common.exceptions;

/**
 * This exception is thrown if a queue is blocked so that no more elements can
 * be retrieved from the queue. After calling deblock it may be possible to get
 * elements again.
 * 
 * @author Annabelle Klarl
 */
@SuppressWarnings("serial")
public class QueueBlockedException extends Exception {

	/**
	 * Constructor
	 * 
	 * @param message
	 *            a String which method was called while queue is blocked
	 */
	public QueueBlockedException(String message) {
		super(message);
	}

}
