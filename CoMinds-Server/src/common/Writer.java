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
package common;

import java.io.IOException;
import java.io.OutputStream;

import common.exceptions.EmptyQueueException;
import common.exceptions.QueueBlockedException;
import common.exceptions.TooFewSpaceException;

/**
 * This class holds a message queue with messages to be written to an output
 * stream and writes the messages to the output stream in a new thread.
 * 
 * @author Annabelle Klarl
 */
public class Writer implements Runnable {

	private OutputStream output;
	protected GenericBoundedQueue<byte[]> messageQueue;

	private final boolean flushAfterWrite;
	protected boolean stopped;
	private boolean finishedWriting;

	/**
	 * Constructor (output will NOT be flushed after each write)
	 * 
	 * @param output
	 *            the stream to write to
	 * @param queueSize
	 *            the size of the buffer for log messages
	 */
	public Writer(OutputStream output, int queueSize) {
		this.flushAfterWrite = false;
		this.stopped = false;
		this.finishedWriting = false;

		this.output = output;
		this.messageQueue = new GenericBoundedQueue<byte[]>(queueSize);
	}

	/**
	 * Constructor (output will be flushed after each write according to the
	 * parameter flush)
	 * 
	 * @param output
	 *            the stream to write to
	 * @param queueSize
	 *            the size of the buffer for log messages
	 * @param flushAfterWrite
	 *            whether the output should be flushed after each write
	 */
	public Writer(OutputStream output, int queueSize, boolean flushAfterWrite) {
		this.flushAfterWrite = flushAfterWrite;
		this.stopped = false;
		this.finishedWriting = false;

		this.output = output;
		this.messageQueue = new GenericBoundedQueue<byte[]>(queueSize);
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
	public boolean write(byte[] message, boolean blocked)
			throws QueueBlockedException {
		if (this.finishedWriting) {
			return false;
		}
		synchronized (this) {
			this.messageQueue.push(message, blocked);

			// notify anybody that there are new elements in the queue
			this.notify();
			return true;
		}
	}

	/**
	 * see documentation {@link Writer#write(byte[], boolean)}
	 * 
	 * @param message
	 *            the message itself as a byte array
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	public boolean write(byte[] message) throws QueueBlockedException {
		return this.write(message, false);
	}

	/**
	 * writes a single byte message to the message buffer/queue. This method
	 * asks whether to block the message buffer afterwards.
	 * 
	 * @param message
	 *            the message itself as a byte
	 * @param blocked
	 *            whether to block the message queue after writing this message
	 *            or not
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	public boolean write(byte message, boolean blocked)
			throws QueueBlockedException {
		synchronized (this) {
			if (this.finishedWriting) {
				return false;
			}
			else {
				this.messageQueue.push(new byte[] { message }, blocked);

				// notify anybody that there are new elements in the queue
				this.notify();
				return true;
			}
		}
	}

	/**
	 * see documentation {@link Writer#write(byte, boolean)}
	 * 
	 * @param message
	 *            the message itself as a byte
	 * @return returns whether the message was put into the queue or not (if not
	 *         than the output is full and nothing can be written to it any
	 *         more)
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	public boolean write(byte message) throws QueueBlockedException {
		return this.write(message, false);
	}

	/**
	 * deblocks a former blocked message queue
	 */
	public void deblockQueue() {
		synchronized (this) {
			this.messageQueue.deblock();

			// notify anybody that queue is not blocked anymore -> elements can
			// be written or read again
			this.notify();
		}
	}

	/**
	 * stops the writing thread (but the thread will first empty the message
	 * buffer before stopping)
	 */
	public void stop() {
		synchronized (this) {
			this.stopped = true;

			// notify anybody that writing should be stopped
			this.notify();
		}
	}

	/**
	 * gets whether this thread is finished with writing or is still writing
	 * something
	 * 
	 * @return whether this thread is finished with writing
	 */
	public boolean isFinished() {
		return this.finishedWriting;
	}

	/**
	 * returns whether the writer has no more messages to write to the output
	 * and is therefore idle
	 * 
	 * @return whether the writer waits for more messages
	 */
	public boolean isWaitingForMessages() {
		return this.messageQueue.isEmpty();
	}

	/**
	 * writes the content of the message queue to the output stream. If the
	 * queue is blocked nothing will be written to the stream until it is
	 * deblock. If the writer thread is stopped the content will be written to
	 * the output and then the thread will be closed.
	 */
	@Override
	public void run() {
		try {
			byte[] currentMessage;
			while (!this.stopped) {
				try {
					currentMessage = this.messageQueue.pop();
					if (!this.tooFewSpaceInOutput(currentMessage)) {
						this.output.write(currentMessage);
						if (this.flushAfterWrite) {
							this.output.flush();
						}
					}
					else {
						throw new TooFewSpaceException();
					}
				}
				catch (QueueBlockedException e) {
					try {
						synchronized (this) {
							this.wait();
						}
					}
					catch (InterruptedException e1) {
					}
				}
				catch (EmptyQueueException e) {
					try {
						synchronized (this) {
							this.wait();
						}
					}
					catch (InterruptedException e1) {
					}
				}
				catch (IOException e) {
				}

			}

			while (!this.messageQueue.isEmpty()) {
				try {
					currentMessage = this.messageQueue.pop();
					if (!this.tooFewSpaceInOutput(currentMessage)) {
						this.output.write(currentMessage);
						if (this.flushAfterWrite) {
							this.output.flush();
						}
					}
					else {
						throw new TooFewSpaceException();
					}
				}
				catch (IOException e) {
				}
				catch (EmptyQueueException e) {
					// should not happen
					break;
				}
				catch (QueueBlockedException e) {
					// queue is blocked so the output should not be written any
					// more
					break;
				}
			}
		}

		catch (TooFewSpaceException e) {
			System.out.println(e.getMessage());
		}

		// to avoid the weird beep
		catch (Throwable e) {
			e.printStackTrace();
		}

		finally {
			try {
				this.output.flush();
				this.output.close();
			}
			// thrown if output could not be closed
			catch (IOException e) {
			}
			// to avoid the weird beep
			catch (Throwable e) {
				e.printStackTrace();
			}
			finally {

				// notify anybody who is waiting for the writer to stop working
				synchronized (this) {
					this.finishedWriting = true;
					this.notify();
				}
			}
		}
	}

	/**
	 * returns whether the queue can be written to the output -> there is enough
	 * space at the output
	 * 
	 * @return whether there is enough space
	 */
	protected boolean tooFewSpaceInOutput(byte[] message) {
		return false;
	}
}
