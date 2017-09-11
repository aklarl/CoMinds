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
package test;
import java.io.DataInputStream;
import java.io.IOException;

import lejos.nxt.Button;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

/**
 * This is a test class for receiving something via bluetooth. It connects to a
 * known device and will read one integer from the connection.
 * 
 * @author Annabelle Klarl
 */
public class BTReceiver {

	public static void main(String[] args) {
		System.out.println("receiver started");

		BTConnection btc = Bluetooth.connect("Johnny", NXTConnection.PACKET);
		System.out.println("connected");

		DataInputStream dis = btc.openDataInputStream();

		int read = 0;
		try {
			read = dis.readInt();
			dis.close();
			btc.close();
		}
		catch (IOException e) {
			System.out.println("IOException");
		}
		System.out.println("read: " + read);

		Button.waitForPress();
	}

}
