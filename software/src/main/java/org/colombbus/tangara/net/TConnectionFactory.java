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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A factory of connection.
 * <p>
 * The usage of this class allows to share a single connection between different
 * objects and thread.
 * </p>
 * 
 * @author gwen
 */
public class TConnectionFactory {

	/**
	 * Create a new factory with default parameters
	 */
	public TConnectionFactory() {
		super();
	}

	/**
	 * Create a new factory with set parameters
	 * 
	 * @param userAgent
	 *            the user-agent parameter in HTTP header
	 * @param charset
	 *            the character encoding set used in HTTP header
	 * @param retryCount
	 *            the number of retries to attempt when a communication failure
	 *            occurs.
	 * @param timeout
	 *            the socket timeout (in millisecond) of the connections. 
	 */
	public TConnectionFactory(String userAgent, String charset, int retryCount,
			int timeout) {
		super();
		if( retryCount < 0) {
			String msg="retryCount parameter shall be greater or equal to 0";
			LOG.error(msg);
			throw new IllegalArgumentException(msg);
		}
		if( timeout<0) {
			String msg="timeout parameter shall be greater or equal to 0";
			LOG.error(msg);
			throw new IllegalArgumentException(msg);
		}
		this.userAgent = userAgent;
		this.charset = charset;
		this.retryCount = retryCount;
		this.timeout = timeout;
	}

	/**
	 * Set the user-agent used in the HTTP header
	 * 
	 * @param userAgent
	 *            the user-agent parameter in HTTP header
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * Set the character encoding set used in HTTP header
	 * 
	 * @param charset
	 *            the character encoding set used in HTTP header
	 */
	public void setCharSet(String charset) {
		this.charset = charset;
	}

	/**
	 * Set the number of connection retries
	 * 
	 * @param retryCount
	 *            the number of retries to attempt when a connection failure
	 *            occurs.
	 */
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	/**
	 * Set the socket timeout connection
	 * 
	 * @param timeout
	 *            the timeout in milliseconds. It shall be greater than 0
	 */
	public void setTimeout(int timeout) {
		if( timeout<1) {
			throw new IllegalArgumentException("timeout parameter shall be greater than 0");
		}
		this.timeout = timeout;
	}


	/**
	 * Get the connection associated to a specific server address.
	 * <p>
	 * The connection is instanciate the first time this method is called with
	 * this parameter. The next times, the same connection instance is returned.
	 * </p>
	 * 
	 * @param serverURL
	 *            server address
	 * @return the connection associated to the server
	 * @throws MalformedURLException
	 *             if the <code>serverURL</code> is not an URL.
	 */
	public synchronized TConnection getConnection(String serverURL)
			throws MalformedURLException {
		URL url = new URL(serverURL);
		if (connections.containsKey(url) == false) {
			LOG.info("Creation of a new connection for "+serverURL);
			TConnection conn = new TConnection(userAgent,charset, serverURL,retryCount, timeout);
			connections.put(url, conn);
		}
		return connections.get(url);
	}
	

	/** User agent used in HTTP header */
	private String userAgent = "TangaraClient";

	/** Character encoding used in HTTP header */
	private String charset = "UTF-8";

	/** The number of connection retry count */
	private int retryCount = 3;

	/** The socket timeout */
	private int timeout = 3000;

	/**
	 * The set of existing connections
	 * <ul>
	 * <li>key: the URL of the tangara server</li>
	 * <li>value: the associated connection</li>
	 * </ul>
	 */	
	private Map<URL, TConnection> connections = new Hashtable<URL, TConnection>();

	/** Class logger */
	private static Logger LOG = Logger.getLogger(TConnectionFactory.class);
}
