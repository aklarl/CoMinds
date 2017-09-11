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

import behaviourmodel.BehaviorUtils;
import common.Writer;
import common.exceptions.QueueBlockedException;

/**
 * This class extends the normal message writer to a bluetooth writer. It will
 * add a second header to the message send via bluetooth that contains the name
 * of the nxt from which the message originates and the name of the nxt to which
 * the message is directed. The messages will then be send to a writer that
 * writes the message to an output stream. This writer can be shared between
 * several logical connections.
 * 
 * @author Annabelle Klarl
 */
class RoutingWriter extends MessageWriter {

	private byte[] remoteName;
	private byte[] myName;
	private boolean blocked;

	/**
	 * Constructor
	 * 
	 * @param myName
	 *            the nxt from which all the messages are
	 * @param remoteName
	 *            the nxt to which to send the messages
	 * @param writer
	 *            the writer which writes the messages to the output
	 */
	public RoutingWriter(String myName, String remoteName, Writer writer) {
		super(writer);
		this.remoteName = communication.CommunicationUtils
				.convertStringToByteArray(remoteName);
		this.myName = communication.CommunicationUtils
				.convertStringToByteArray(myName);
	}

	/**
	 * writes a message to the message buffer/queue. This method asks whether to
	 * block the message buffer afterwards.
	 * 
	 * @param message
	 *            the message itself as a byte array
	 * @param blocked
	 *            whether to block the message queue after writing this message
	 *            or not
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	@Override
	public boolean write(byte[] message, boolean blocked)
			throws QueueBlockedException {
		if (this.blocked) {
			throw new QueueBlockedException(
					"Cannot push into queue because queue is blocked");
		}

		this.blocked = blocked;

		message = common.WriterUtils.concatArrays(this.myName, this.remoteName,
				new byte[] { NXTConnectionManager.DATA },
				communication.CommunicationUtils
						.convertIntToByteArray(message.length), message);

		return this.writer.write(message, false);
	}

	/**
	 * deblocks the writing
	 */
	@Override
	public void deblockQueue() {
		this.blocked = false;
		// caution: if you don't override write(byte[],boolean), then here
		// this.writer.deblockQueue() must be called!
	}
}
