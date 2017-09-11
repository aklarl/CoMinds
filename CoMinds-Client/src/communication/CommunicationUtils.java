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

public class CommunicationUtils {

	/**
	 * gets a single int as a byte array
	 * 
	 * @param message
	 *            single int
	 * @return the int as a byte array
	 */
	public static byte[] convertIntToByteArray(int message) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (message >>> 24);
		bytes[1] = (byte) (message >>> 16);
		bytes[2] = (byte) (message >>> 8);
		bytes[3] = (byte) message;
		return bytes;
	}

	/**
	 * gets a single float as a byte array for writing it to the bt
	 * 
	 * @param message
	 *            single float
	 */
	public static byte[] convertFloatToByteArray(float message) {
		return convertIntToByteArray(Float.floatToIntBits(message));
	}

	/**
	 * gets a String as a byte array for writing it to the bt (including string
	 * length)
	 * 
	 * @param message
	 *            a String
	 * @return the byte array with the string length and the message itself
	 */
	public static byte[] convertStringToByteArray(String message) {
		byte[] messageBytes = message.getBytes();
		byte[] lengthBytes = convertIntToByteArray(messageBytes.length);

		return common.WriterUtils.concatArrays(lengthBytes, messageBytes);
	}

}
