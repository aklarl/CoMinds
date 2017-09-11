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

import java.util.ArrayList;

/**
 * This implements a map that can store several values for a key.
 * 
 * @author Annabelle Klarl
 * @param <E>
 *            type of the keys
 * @param <T>
 *            type of the values
 */
public class MapOfLists<E, T> extends Map<E, ArrayList<T>> {

	/**
	 * {@link Map#Map()}
	 */
	public MapOfLists() {
		super();
	}

	/**
	 * {@link Map#Map(int)}
	 */
	public MapOfLists(int size) {
		super(size);
	}

	/**
	 * puts a key and a given value into this map. If the key already exists,
	 * the value will be added to the list of values for this key. If the key
	 * does not already exist, the key will be added to the map with a list for
	 * values that only contains the given value.
	 * 
	 * @param key
	 *            the key to add
	 * @param value
	 *            the value to be added for this key
	 */
	public synchronized void putElement(E key, T value) {

		ArrayList<T> values = this.get(key);

		if (values == null) {
			values = new ArrayList<T>();
			values.add(value);
			this.put(key, values);
		}
		else {
			values.add(value);
		}

	}

	/**
	 * removes a value from the map. This will remove the value from all lists,
	 * independent for which key it is stored.
	 * 
	 * @param value
	 *            the value to remove
	 * @return the removed value or null if the value didn't exist in this map
	 */
	public synchronized T removeElement(T value) {

		ArrayList<E> keys = this.keys();

		boolean removed = false;

		for (int i = 0; i < keys.size(); i++) {
			ArrayList<T> values = this.get(keys.get(i));
			removed = removed || values.remove(value);
		}

		if (removed) {
			return value;
		}
		else {
			return null;
		}
	}

	/**
	 * removes a value for a given key from this map. If the value is stored for
	 * any other key, this occurrence will not be removed.
	 * 
	 * @param key
	 *            the key to for which to remove an element
	 * @param value
	 *            the value to remove
	 * @return the value that was removed or null if the wasn't this value for
	 *         the given
	 */
	public T removeElement(E key, T value) {

		ArrayList<T> values = this.get(key);
		boolean removed = values.remove(value);

		if (removed) {
			return value;
		}
		else {
			return null;
		}

	}
}
