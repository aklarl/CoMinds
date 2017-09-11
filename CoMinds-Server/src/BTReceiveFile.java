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
import logging.Logger;
import logging.Logger.LogLevel;

import common.FileLocalizer;
import communication.BTComm;
import communication.BTCommObserver;
import communication.BTEvent;
import communication.PCConnectionManager;
import communication.exceptions.ConnectionClosedException;

/**
 * This class gets any file that is send from a nxt. You can specify the nxt to
 * connect to by the command line parameter -name.
 */
public class BTReceiveFile extends BTCommObserver {

	private static final Logger logger = Logger.getLogger(LogLevel.DEBUG, 500);

	private boolean fileReceived = false;

	@Override
	public void notify(BTComm btcomm, BTEvent event) {
		super.notify(btcomm, event);
		switch (event) {
		case FILE:
			this.fileReceived = true;
			synchronized (this) {
				this.notify();
			}
		}
	}

	public static void main(String[] args) {

		PCConnectionManager manager = null;
		BTReceiveFile receiver = new BTReceiveFile();

		try {
			String name = "Crownie";
			if (args[0].equals("-name")) {
				name = args[1];
			}
			manager = PCConnectionManager.getManager(name);
			BTComm btcomm = manager.getConnection(name);

			try {
				if (!btcomm.isClosed()) {
					btcomm.register(receiver, BTEvent.FILE);
					btcomm.setOwnFileLocalizer(new FileLocalizer() {

						@Override
						public String getPathToFile(String requestedFileName) {
							if (requestedFileName != null) {
								return "C:\\Users\\Annabelle\\Desktop\\"
										+ requestedFileName;
							}
							else {
								return null;
							}
						}
					});
				}
			}
			catch (ConnectionClosedException e) {
				logger.error(e);
			}

			// wait for file
			if (!btcomm.isClosed() && !receiver.fileReceived) {
				synchronized (receiver) {
					try {
						receiver.wait();
					}
					catch (InterruptedException e) {
						logger.error(e);
					}
				}
			}

		}
		finally {
			// close bluetooth connection
			PCConnectionManager.closeManager();

			// stop logging
			logger.stopLogging();
		}
	}
}
