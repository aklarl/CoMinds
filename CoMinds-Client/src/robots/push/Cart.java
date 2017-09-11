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
package robots.push;

import robots.heading.simpleController.CompassControl;

import communication.exceptions.ManagerException;

/**
 * This class is an object that will be pushed in the direction it is heading in
 * the beginning. There it sends its compass data to the cars pushing it.
 * 
 * @author Annabelle Klarl
 */
public class Cart extends CompassControl {

	public static final int SEND_INTERVAL = 300;

	/**
	 * Constructor
	 * 
	 * @param timeout
	 *            the timeout for an inbound connection (if -1 no inbound
	 *            connection will be initialized)
	 * @param nxtNames
	 *            the names of the nxts to connect to
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections
	 */
	public Cart(int timeout, String... nxtNames) throws ManagerException {
		super(timeout, nxtNames);
	}

	/**
	 * Constructor
	 * 
	 * @param nxtNames
	 *            the names of the nxts to connect to
	 * @param directConnection
	 *            whether to connect directly or by managed connections
	 * @throws ManagerException
	 *             if the singleton manager has been initialized beforehands for
	 *             managed connections and the parameter directConnection is set
	 *             to true (and the other way round)
	 */
	public Cart(boolean directConnection, String... nxtNames)
			throws ManagerException {
		super(directConnection, nxtNames);
	}

	public static void main(String[] args) {
		try {
			main(new Cart(true, "Josy", "Jenny"));
		}
		catch (Throwable e) {
			System.out.println("Cart-Exc " + e.getClass());
		}
	}
}
