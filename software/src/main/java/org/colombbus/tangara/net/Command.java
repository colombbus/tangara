/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008 Colombbus (http://www.colombbus.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara.net;

import java.util.HashMap;
import java.util.Map;

/**
 * A command is sent by a client to a server.
 * 
 * 
 * @author gwen
 */
public class Command {

	/**
	 * Create a new command
	 * 
	 * @param relativeURL
	 *            the path of the command relatives to the server address
	 */
	public Command(String relativeURL) {
		super();
		this.relativeURL = relativeURL;
	}

	/**
	 * Get the path of the command relatives to the server address
	 * @return a partial URL
	 */
	public String getRelativeURL() {
		return relativeURL;
	}

	/**
	 * Remove all arguments
	 */
	public void clearAllArgs() {
		clearBodyArgs();
		clearURLArgs();
	}

	/**
	 * Add a new argument in the URL
	 * 
	 * @param name
	 *            name of the argument
	 * @param value
	 *            the value of the argument
	 */
	public void addURLArg(String name, String value) {
		urlArgs.put(name, value);
	}

	/**
	 * Remove all arguments in the URL
	 */
	public void clearURLArgs() {
		urlArgs.clear();
	}

	/**
	 * Get the arguments in the URL
	 * 
	 * @return a map with argument name as map key and argument value as map
	 *         value
	 */
	public Map<String, String> getURLArgs() {
		return urlArgs;
	}

	/**
	 * Add a new argument in the body part
	 * 
	 * @param name
	 *            name of the argument
	 * @param value
	 *            the value of the argument
	 */
	public void addBodyArg(String name, String value) {
		bodyArgs.put(name, value);
	}

	/**
	 * Remove all arguments in the body part
	 */
	public void clearBodyArgs() {
		bodyArgs.clear();
	}

	/**
	 * Get the arguments in the body part
	 * 
	 * @return a map with argument name as map key and argument value as map
	 *         value
	 */
	public Map<String, String> getBodyArgs() {
		return bodyArgs;
	}

	/**
	 * the arguments in the URL
	 * <ul>
	 * <li>key: argument name</li>
	 * <li>value: argument value</li>
	 * </ul>
	 */
	private Map<String, String> urlArgs = new HashMap<String, String>();

	/**
	 * the arguments in the body part
	 * <ul>
	 * <li>key: argument name</li>
	 * <li>value: argument value</li>
	 * </ul>
	 */
	private Map<String, String> bodyArgs = new HashMap<String, String>();

	/** the path of the command relatives to the server address */
	private String relativeURL;
}
