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

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;

public class BTReceiverPC {
	public static void main(String[] args) {
		NXTConnector conn = new NXTConnector();

		boolean connected = conn.connectTo("Johnny", null,
				NXTCommFactory.BLUETOOTH, NXTComm.PACKET);

		if (!connected) {
			System.err.println("Failed to connect to any NXT");
			System.exit(1);
		}
		else {
			System.out.println("Connected to " + "Crownie");
		}

		DataInputStream dis = conn.getDataIn();

		int read = 0;
		try {
			read = dis.readInt();
			dis.close();
			conn.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(read);
	}
}
