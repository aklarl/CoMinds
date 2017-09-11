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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import logging.Logger;
import logging.Logger.LogLevel;

import communication.PCConnectionManager;

public class Router {

	private static final Logger logger = Logger.getLogger(LogLevel.DEBUG, 500);

	public static void main(String[] args) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			System.out
					.println("for standard connection (Johnny, Josy, Jamy) press ENTER");
			System.out
					.println("for other connections: type name per line, end input with ENTER");

			ArrayList<String> names = new ArrayList<String>();
			String line = null;
			while (!(line = in.readLine()).equals("")) {
				names.add(line);
			}

			if (names.isEmpty()) {
				names.add("Johnny");
				names.add("Josy");
				names.add("Jamy");
			}

			PCConnectionManager.getManager(500, true, names
					.toArray(new String[names.size()]));

			in.read();
		}
		catch (IOException e) {
			logger.error(e);
		}

		logger.info("STOPPING...");
		PCConnectionManager.closeManager();
		logger.stopLogging();
	}
}
