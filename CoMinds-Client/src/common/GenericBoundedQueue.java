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

import java.util.EmptyQueueException;

import common.exceptions.QueueBlockedException;

/**
 * A thread safe generic queue that can only hold a fixed number of elements.
 * 
 * @author Annabelle Klarl
 * @param <T>
 *            the elements the queue holds
 */
public class GenericBoundedQueue<T> {

	private final Object sync;

	private Object[] queue;
	private int size;
	private int pointerPush;
	private int pointerPop;
	private int diff;
	private int pointerBlocked;

	private boolean blockingMessageAlreadyRead = false;

	/**
	 * Constructor
	 * 
	 * @param size
	 *            the number of elements the queue can hold
	 */
	public GenericBoundedQueue(int size) {
		this.sync = new Object();
		this.size = size;
		this.queue = new Object[this.size];
		this.pointerPush = 0;
		this.pointerPop = 0;
		this.diff = 0;
		this.pointerBlocked = -1;
	}

	/**
	 * pushes an object onto the queue (at the last position) => FIFO principal
	 * holds). If the queue size is reached the oldest object will be
	 * overwritten.
	 * 
	 * @param object
	 *            the object to add
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	public void push(T object) throws QueueBlockedException {
		this.push(object, false);
	}

	/**
	 * see documentation {@link GenericBoundedQueue#push(Object)}. The parameter
	 * blocked is for blocking the queue. If it is set to true, elements can
	 * only be got from the queue until the element is reached that was now
	 * pushed to the queue. That means the queue can only give away all elements
	 * that have been in the queue before this element was written. All elements
	 * after the current message cannot be retrieved until the method
	 * {@link GenericBoundedQueue#deblock()} is called.
	 * 
	 * @param object
	 *            the object to add
	 * @param blocked
	 *            whether the queue shall be blocked or not
	 * @throws QueueBlockedException
	 *             thrown if something shall be pushed but the queue is blocked
	 */
	public void push(T object, boolean blocked) throws QueueBlockedException {
		synchronized (this.sync) {
			if (this.pointerBlocked != -1) {
				throw new QueueBlockedException(
						"Cannot push into queue because queue is blocked");
			}

			this.queue[this.pointerPush] = object;

			this.pointerPush = (this.pointerPush + 1) % this.size;

			// push has overridden an element of the last element of the queue
			if (this.diff >= this.size) {
				this.pointerPop = this.pointerPush;
			}
			else {
				this.diff++;
			}

			if (blocked && this.pointerBlocked == -1) {
				this.pointerBlocked = this.pointerPush;
				this.blockingMessageAlreadyRead = false;
			}
		}
	}

	/**
	 * pops an object from the queue (at the first position) => FIFO principal
	 * holds). If the queue is blocked, no element can be retrieved from the
	 * queue and an exception is thrown.
	 * 
	 * @return the oldest object in the queue
	 * @throws EmptyQueueException
	 *             if there is no element to get from the queue
	 * @throws QueueBlockedException
	 *             thrown if the queue is blocked at the current point for
	 *             getting an element so that no more elements can be got from
	 *             the queue until the queue is deblocked
	 */
	@SuppressWarnings("unchecked")
	public T pop() throws EmptyQueueException, QueueBlockedException {
		synchronized (this.sync) {
			if (this.diff <= 0) {
				throw new EmptyQueueException();
			}
			else if (this.pointerBlocked == this.pointerPop
					&& (this.size != 1 || (this.size == 1 && this.blockingMessageAlreadyRead))) {
				throw new QueueBlockedException(
						"Cannot pop from queue because queue is blocked");
			}
			else {
				T result = (T) this.queue[this.pointerPop];
				this.queue[this.pointerPop] = null;

				this.pointerPop = (this.pointerPop + 1) % this.size;
				this.diff--;
				this.blockingMessageAlreadyRead = true;

				return result;
			}
		}
	}

	/**
	 * returns wether the queue is empty
	 * 
	 * @return true if the queue is emtpy, false otherwise
	 */
	public boolean isEmpty() {
		return this.diff == 0;
	}

	/**
	 * returns whether this queue is currently blocked
	 * 
	 * @return whether the queue is blocked
	 */
	public boolean isBlocked() {
		return this.pointerBlocked != -1;
	}

	/**
	 * deblocks the queue. That means if the queue was blocked at a specific
	 * element this method will cancel the blocking.
	 */
	public void deblock() {
		this.pointerBlocked = -1;
	}

	/**
	 * clears this queue
	 */
	public void clearQueue() {
		synchronized (this.sync) {
			this.pointerPush = 0;
			this.pointerPop = 0;
			this.diff = 0;
			this.pointerBlocked = -1;
		}
	}
}
