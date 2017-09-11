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

import logging.Logger;

/**
 * This is an interface that gets the path to a given file name.
 * 
 * @author Annabelle Klarl
 */
public interface FileLocalizer {

	public static final Logger logger = Logger.getLogger();

	/**
	 * gets the path for a given file name
	 * 
	 * @param requestedFileName
	 *            the file to get
	 * @return the path to this file or null if it does not exist
	 */
	public String getPathToFile(String requestedFileName);

}
