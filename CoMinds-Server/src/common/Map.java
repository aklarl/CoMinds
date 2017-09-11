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
 * This is an implementation of a thread safe map.
 * 
 * @author Annabelle Klarl
 * @param <E>
 *            generic type of keys
 * @param <T>
 *            generic type of values
 */
public class Map<E, T> {

	private ArrayList<E> keys;
	private ArrayList<T> values;

	/**
	 * Constructor
	 */
	public Map() {
		this.keys = new ArrayList<E>();
		this.values = new ArrayList<T>();
	}

	/**
	 * Constructor which initializes the map with the given size
	 * 
	 * @param size
	 *            the size of the map (the number of keys)
	 */
	public Map(int size) {
		this.keys = new ArrayList<E>(size);
		this.values = new ArrayList<T>(size);
	}

	/**
	 * puts the value with the specified key into the map (will replace any
	 * previous value for this key)
	 * 
	 * @param key
	 *            the key to add
	 * @param value
	 *            the value to be added for the key
	 */
	public synchronized void put(E key, T value) {
		int index = this.keys.indexOf(key);

		if (index == -1) {
			this.keys.add(key);
			index = this.keys.indexOf(key);
		}

		this.values.add(index, value);
	}

	/**
	 * removes the key and the value for it from the map. It will return the
	 * removed value or null if there was no entry for the key.
	 * 
	 * @param key
	 *            the key to remove
	 * @return which value was removed from the map or null if there was no
	 *         entry for the key
	 */
	public synchronized T remove(E key) {
		int index = this.keys.indexOf(key);

		if (index == -1) {
			return null;
		}
		else {
			this.keys.remove(index);
			return this.values.remove(index);
		}
	}

	/**
	 * gets the value for the given key from the map or null if there was no
	 * entry for the key.
	 * 
	 * @param key
	 *            the key for which to get the value
	 * @return the value for this key or null if there was no entry for the key
	 */
	public T get(E key) {
		int index = this.keys.indexOf(key);

		if (index == -1) {
			return null;
		}
		else {
			return this.values.get(index);
		}
	}

	/**
	 * returns whether this map contains the specified key
	 * 
	 * @param key
	 *            the key to look for
	 * @return whether the key is contained or not
	 */
	public boolean contains(E key) {
		return this.keys.contains(key);
	}

	/**
	 * gets a list of all stored keys
	 * 
	 * @return all keys in this map
	 */
	public ArrayList<E> keys() {
		return new ArrayList<E>(this.keys);
	}

}
