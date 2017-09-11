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

import common.Writer;
import common.WriterUtils;
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

		message = WriterUtils.concatArrays(this.myName, this.remoteName,
				new byte[] { PCConnectionManager.DATA },
				communication.CommunicationUtils
						.convertIntToByteArray(message.length), message);

		return this.writer.write(message, false);
	}

	/**
	 * writes a message to the message buffer/queue. It will add from which nxt
	 * the message is derived from.
	 * 
	 * @param fromName
	 *            from which nxt the message is derived from
	 * @param message
	 *            the message itself as a byte array
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	public boolean write(String fromName, byte[] message)
			throws QueueBlockedException {
		message = WriterUtils.concatArrays(communication.CommunicationUtils
				.convertStringToByteArray(fromName), this.remoteName,
				new byte[] { PCConnectionManager.DATA },
				communication.CommunicationUtils
						.convertIntToByteArray(message.length), message);

		return this.writer.write(message);
	}

	/**
	 * writes a command to the message buffer/queue. It will add from which nxt
	 * the command is derived from.
	 * 
	 * @param fromName
	 *            from which nxt the message is derived from
	 * @param command
	 *            the command itself as a byte
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	public boolean write(String fromName, byte command)
			throws QueueBlockedException {
		return this.write(fromName, command, false);
	}

	/**
	 * writes a command to the message buffer/queue. It will add from which nxt
	 * the command is derived from.
	 * 
	 * @param fromName
	 *            from which nxt the message is derived from
	 * @param command
	 *            the command itself as a byte
	 * @param blocked
	 *            whether to block the writer thread afterwards
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	public boolean write(String fromName, byte command, boolean blocked)
			throws QueueBlockedException {
		byte[] message = WriterUtils.concatArrays(communication.CommunicationUtils
				.convertStringToByteArray(fromName), this.remoteName,
				new byte[] { command });

		return this.writer.write(message, blocked);
	}

	/**
	 * requests to close the BT connection and blocks the writer thread after
	 * writing the close message
	 * 
	 * @throws QueueBlockedException
	 *             thrown if something shall be written to the message queue but
	 *             the queue is blocked
	 */
	public void requestManagedClose() throws QueueBlockedException {
		this.write("PC", PCConnectionManager
				.getCloseCommand(PCConnectionManager.COMMAND), true);
	}

	/**
	 * deblocks the writing
	 */
	@Override
	public void deblockQueue() {
		this.blocked = false;
		this.writer.deblockQueue();
	}
}
