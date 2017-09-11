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
package communication.extendedClasses;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class extends the DataInputStream by a method for reading a String from
 * the stream
 * 
 * @author Annabelle Klarl
 */
public class ExtendedDataInputStream extends DataInputStream {

	/**
	 * Constructor
	 * 
	 * @param in
	 *            the input stream to extend
	 */
	public ExtendedDataInputStream(InputStream in) {
		super(in);
	}

	/**
	 * reads a String from the input stream. It will read first the length of
	 * the string and then the String itself
	 * 
	 * @return the String that was read
	 * @throws IOException
	 *             thrown if it couldn't be read from the input stream
	 */
	public String readString() throws IOException {
		int length = this.readInt();
		if (length < 0) {
			throw new IOException(
					"Negative length of String. Might be end of stream...");
		}

		byte[] messageBytes = new byte[length];
		this.read(messageBytes);
		return new String(messageBytes);
	}

}
