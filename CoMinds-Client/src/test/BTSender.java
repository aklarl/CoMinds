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
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.Button;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

/**
 * This is a test class for two bt connection in sequence. It sends two integers
 * to the first connection and one to the second connection. You can test
 * whether all integers are received in the right connection and nothing is lost
 * or received in the wrong connection.
 * 
 * @author Annabelle Klarl
 */
public class BTSender {

	public static void main(String[] args) {
		System.out.println("sender started");

		BTConnection btc = Bluetooth.waitForConnection(0, NXTConnection.PACKET);
		System.out.println("connected");

		DataOutputStream dos = btc.openDataOutputStream();

		try {
			dos.writeInt(10);
			dos.writeInt(15);
			dos.close();
			btc.close();
		}
		catch (IOException e) {
			System.out.println("IOException");
		}

		System.out.println("first connection closed");

		BTConnection btc2 = Bluetooth
				.waitForConnection(0, NXTConnection.PACKET);
		System.out.println("connected");

		DataOutputStream dos2 = btc2.openDataOutputStream();

		try {
			dos2.writeInt(20);
			dos2.close();
			btc2.close();
		}
		catch (IOException e) {
			System.out.println("IOException");
		}

		System.out.println("second connection closed");
		Button.waitForPress();

	}

}
