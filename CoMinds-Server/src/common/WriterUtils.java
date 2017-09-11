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

/**
 * This class holds some util methods. They are not subclassed, but mixed
 * together.
 * 
 * @author Annabelle Klarl
 */
public class WriterUtils {

	/**
	 * This method concats a byte and several byte-arrays to one byte array
	 * 
	 * @param first
	 *            a byte
	 * @param others
	 *            several arrays to concat together
	 * @return the concatenated array
	 */
	public static byte[] concatArrays(byte first, byte[]... others) {

		// determine new array length
		int length = 1;
		for (byte[] array : others) {
			length += array.length;
		}

		return concatArraysIntoArrayWithLength(length, first, others);

	}

	/**
	 * This method concats several byte-arrays to one byte array
	 * 
	 * @param first
	 *            the first array to concat
	 * @param others
	 *            several arrays to concat together
	 * @return the concatenated array
	 */
	public static byte[] concatArrays(byte[] first, byte[]... others) {

		// determine new array length
		int length = first.length;
		for (byte[] array : others) {
			length += array.length;
		}

		return concatArraysIntoArrayWithLength(length, first, others);

	}

	/**
	 * concats a byte and several byte-arrays to one byte array and stores it in
	 * an array with the given length
	 * 
	 * @param length
	 *            the length of the array to store the concatenation
	 * @param first
	 *            the first byte
	 * @param others
	 *            several array to concat together
	 * @return the concatenated array with length intoArray
	 */
	public static byte[] concatArraysIntoArrayWithLength(int length,
			byte first, byte[]... others) {

		byte[] intoArray = new byte[length];

		try {
			// fill array
			int index = 0;
			intoArray[index] = first;
			index++;
			for (byte[] array : others) {
				for (int i = 0; i < array.length; i++, index++) {
					intoArray[index] = array[i];
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			// stop inserting bytes
		}

		return intoArray;
	}

	/**
	 * concats several byte-arrays to one byte array and stores it in an array
	 * with the given length
	 * 
	 * @param length
	 *            the length of the array to store the concatenation
	 * @param first
	 *            the first byte
	 * @param others
	 *            several array to concat together
	 * @return the concatenated array with length intoArray
	 */
	public static byte[] concatArraysIntoArrayWithLength(int length,
			byte[] first, byte[]... others) {

		byte[] intoArray = new byte[length];

		// fill array
		try {
			int index = 0;
			for (int i = 0; i < first.length; i++, index++) {
				intoArray[index] = first[i];
			}
			for (byte[] array : others) {
				for (int i = 0; i < array.length; i++, index++) {
					intoArray[index] = array[i];
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			// stop inserting bytes
		}

		return intoArray;
	}
}
