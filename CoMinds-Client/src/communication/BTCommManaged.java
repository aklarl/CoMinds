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

import common.Writer;
import communication.extendedClasses.ExtendedDataInputStream;

/**
 * This is a class for a managed connection. It will be managed by a pc server
 * and all content is send via the pc.
 * 
 * @author Annabelle Klarl
 */
public class BTCommManaged extends BTComm {

	/**
	 * Constructor which gets a logical connection to the nxt named toNxtName.
	 * This connection will be managed by the ConnectionManager
	 * 
	 * @param myName
	 *            a String with the name of this nxt
	 * @param remoteName
	 *            a String with the name to which nxt to connect
	 * @param the
	 *            writer which writes all message to the output
	 * @param logging
	 *            whether all communication shall be logged to a log file
	 */
	protected BTCommManaged(String myName, String remoteName, Writer writer,
			boolean logging) {
		super(myName, remoteName, logging);

		this.reader = new MessageReader(this.connName,
				new ExtendedDataInputStream(new ByteArrayInputStream(
						new byte[0])), this, true, logging);
		this.writer = new RoutingWriter(myName, remoteName, writer);
	}

	/**
	 * sets a new stream where to get the remote data
	 * 
	 * @param dis
	 *            the new input stream where to get the remote data
	 */
	protected void setDataInput(ExtendedDataInputStream dis) {
		this.reader.setExtendedDataInputStream(dis);
	}

	/**
	 * starts the reading of the input stream (shall be used especially for a
	 * ByteArrayInputStream -> otherwise the MessageReader is a thread and will
	 * read continuously by itself)
	 */
	protected void read() {
		this.reader.read();
	}

}
