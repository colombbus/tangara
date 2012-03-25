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

/**
 * Exception throwned when a problem occurs during exchanges between the tangara
 * client and the tangara php server
 * 
 * @author gwen
 */
@SuppressWarnings("serial")
public class CommandException extends Exception {

	/** Error occurs during XML parsing: the content of the response is not XML */
	public static final int NOT_XML_RESPONSE_ERR = -1;

	/**
	 * Error occurs during XML parsing: the content of the response is not a
	 * tangara XML document (but a XML document)
	 */
	public static final int BAD_XML_RESPONSE_ERR = -2;

	/** Error occurs during the parsing of the error */
	public static final int RESPONSE_PARSING_ERR = -3;

	/** An HTTP error occurs */
	public static final int HTTP_ERR = -4;

	/** Cannot determine local host address */
	public static final int NO_LOCAL_HOST_ERR = -5;

	/** Cannot create the command to send to the server */
	public static final int COMMAND_CREATION_ERR = -6;
	
	/** An unexpected and unknown error has been sent */
	public static final int UNKNOWN_ERR = -7;

	/**
	 * Create a command exception
	 * 
	 * @param url
	 *            the server address
	 * @param code
	 *            the error code
	 * @param message
	 *            a message describing the error
	 * @param cause
	 *            the original cause of the error
	 */
	public CommandException(String url, int code, String message, Throwable cause) {
		super(message, cause);
		errorCode = code;
		this.serverURL = url;
	}

	/**
	 * Create a command exception
	 * 
	 * @param url
	 *            the server address
	 * @param code
	 *            the error code
	 * @param message
	 *            a message describing the error
	 */
	public CommandException(String url, int code, String message) {
		super(message);
		errorCode = code;
		this.serverURL = url;
	}

	/**
	 * Get the code of the error
	 * <p>
	 * <code>0</code> is the default value. If the code is greater than 0, the
	 * code identifies an error on the server. If the code is lesser that 0, the
	 * code identifies an error on the client.
	 * </p>
	 * 
	 * @return errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Get the address of the Tangara server
	 * 
	 * @return the URL of the server in a {@link String} format
	 */
	public String getServerURL() {
		return serverURL;
	}

	/**
	 * The code of the error
	 */
	private int errorCode = 0;

	/**
	 * The address of the tangara server
	 */
	private String serverURL;
}
