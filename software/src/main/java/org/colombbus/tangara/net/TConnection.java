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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * A connection to a Tangara server
 *
 * @author gwen
 *
 */
public class TConnection {

	/** Separator of URL */
	private static final String URL_SEPARATOR = "/";

	/**
	 * Create a new connection
	 *
	 * @param userAgent
	 *            the user-agent of the HTTP header
	 * @param charset
	 *            the character set encoding of the HTTP communication
	 * @param serverURL
	 *            the address of the server as an URL
	 * @param retryCount
	 * @param timeout
	 */
	public TConnection(String userAgent, String charset, String serverURL, int retryCount, int timeout) {
		super();
		this.serverURL = serverURL;
		this.encodingCharset = charset;
		this.retryHandler = new HttpConnRetryHandler(retryCount);
		params.setParameter(HttpMethodParams.USER_AGENT, userAgent);
		params.setContentCharset(charset);
		params.setConnectionManagerTimeout(timeout);
		params.setSoTimeout(timeout);
		params.setParameter(HttpMethodParams.RETRY_HANDLER, this.retryHandler);
	}


	public String getServerURL() {
		return serverURL;
	}

	private synchronized HttpClient getClient() {
		if( client == null ) {
			client = new HttpClient(params);
		}
		return client;
	}

	/**
	 * Create the HTTP method from the command
	 *
	 * @param cmd
	 * @return HttpMethod
	 */
	private HttpMethod createMethod(Command cmd) {
		// url args
		String url = buildURL(cmd);

		// method choice
		HttpMethod method = null;
		if (cmd.getBodyArgs().isEmpty()) {
			method = new GetMethod(url);
		} else {
			PostMethod postMethod = new PostMethod(url);
			// body args
			for (String argName : cmd.getBodyArgs().keySet()) {
				String value = cmd.getBodyArgs().get(argName);
				postMethod.addParameter(argName, value);
			}
			method = postMethod;
		}

		return method;
	}

	private String buildURL(Command cmd) {
		StringBuilder urlBuilder = new StringBuilder(serverURL);
		String relURL = cmd.getRelativeURL();
		if( (serverURL.endsWith(URL_SEPARATOR) && relURL.startsWith(URL_SEPARATOR)) == false ) {
			urlBuilder.append(URL_SEPARATOR); //$NON-NLS-1$
		}
		urlBuilder.append(cmd.getRelativeURL());
		if (cmd.getURLArgs().isEmpty() == false ) {
			urlBuilder.append('?');//$NON-NLS-1$
			boolean firstArg = true;
			for (Entry<String, String> entry : cmd.getURLArgs().entrySet()) {
				String argName = entry.getKey();
				String argValue = entry.getValue();
				if (firstArg) {
					firstArg = false;
				} else {
					urlBuilder.append('&');//$NON-NLS-1$
				}

				try {
					String urlArgName = URLEncoder.encode(argName, encodingCharset);
					String urlArgValue = URLEncoder.encode(argValue, encodingCharset);
					urlBuilder.append(urlArgName).append('=') //$NON-NLS-1$
							.append(urlArgValue);
				} catch (UnsupportedEncodingException e) {
					LOG.error(encodingCharset + " not supported for URLs", e); //$NON-NLS-1$
					urlBuilder.append(argName).append('=').append(argValue); //$NON-NLS-1$
				}
			}
		}
		String url = urlBuilder.toString();
		return url;
	}

	private String executeMethod(HttpMethod method) throws CommandException {
		int statusCode = 0;
		try {
			statusCode = getClient().executeMethod(method);
			if (statusCode == HttpStatus.SC_REQUEST_TIMEOUT) {
				LOG.warn("Request timeout");
			}
			if (statusCode != HttpStatus.SC_OK) {
				String msg = "Method failed: " + method.getStatusLine(); //$NON-NLS-1$
				LOG.error(msg);
				throw new IOException(msg);
			}

			InputStream in = method.getResponseBodyAsStream();
			byte[] buffer = IOUtils.toByteArray(in);

			String response = new String(buffer, encodingCharset);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Response content:\n" + response);
			}
			return response;
		} catch (HttpException httpEx) {
			String msg = String.format("An HTTP error occurs during the " +//$NON-NLS-1$
									   "execution of the method %s. The returned HTTP code is %d",//$NON-NLS-1$
									   method.getPath(), statusCode);
			LOG.error(msg, httpEx);
			throw CommandExceptionFactory.createCommunicationException(method,
					CommandException.HTTP_ERR, msg, httpEx);
		} catch (IOException ioEx) {
			String msg = String.format( "An IO communication error occurs during the execution of the method %s.",
										method.getPath());
			LOG.error(msg, ioEx);
			throw CommandExceptionFactory.createCommunicationException(method,
					CommandException.HTTP_ERR, msg, ioEx);
		} catch (Throwable th) {
			String msg = String
					.format( "An unknown error occurs during the execution of the method %s",
							 method.getPath());
			LOG.error(msg, th);
			throw CommandExceptionFactory.createCommunicationException(method,
					CommandException.HTTP_ERR, msg, th);

		}
	}

	private Document buildXmlResponse(HttpMethod method, String httpResponse)
			throws CommandException {
		StringReader reader = new StringReader(httpResponse);
		Document doc = null;
		try {
			doc = saxBuilder.build(reader);
			if ("tangara".equals(doc.getRootElement().getName()) == false) { //$NON-NLS-1$
				String msg = String.format(
						"Response from %s is not a tangara XML document",//$NON-NLS-1$
						serverURL);
				LOG.error(msg);
				CommandExceptionFactory.throwBadServerException(method,
						CommandException.BAD_XML_RESPONSE_ERR,
						"Response is not a tangara XML document");//$NON-NLS-1$
			}

			// detect error responses
			Element errorE = doc.getRootElement().getChild("error"); //$NON-NLS-1$
			if (errorE != null) {
				CommandExceptionFactory.throwException(method, errorE);
			}

		} catch (JDOMException domEx) {
			String msg = String.format(
					"Bad response content. %s is not a Tangara server",//$NON-NLS-1$
					serverURL);
			LOG.error(msg, domEx);
			CommandExceptionFactory.throwBadServerException(method,
					CommandException.NOT_XML_RESPONSE_ERR,
					"Response is not a tangara XML document", domEx);//$NON-NLS-1$
		} catch (IOException ioEx) { // never happend
			String msg = String.format("Fail to parse the response content.");//$NON-NLS-1$
			LOG.error(msg, ioEx);
			LOG.error("SHOULD NOT HAPPEND. CHECK THE CODE", ioEx); //$NON-NLS-1$
			CommandExceptionFactory.throwBadServerException(method,
					CommandException.RESPONSE_PARSING_ERR,
					"Response parsing failed", ioEx);//$NON-NLS-1$
		}
		return doc;
	}

	public synchronized Document sendCommand(Command cmd)
			throws CommandException {
		HttpMethod method = createMethod(cmd);
		Document doc = null;

		try {
			String logMsg = String.format("Prepare executing method %s", //$NON-NLS-1$
					method.getPath());
			LOG.info(logMsg);

			String httpResponse = executeMethod(method);
			doc = buildXmlResponse(method, httpResponse);

			logMsg = String.format("Method succeed %s %s", //$NON-NLS-1$
					method.getPath(), method.getStatusLine().toString());
			LOG.info(logMsg);
		} catch (CommandException cmdEx) {
			LOG.error("Command excetion catched in the command", cmdEx);//$NON-NLS-1$
			throw cmdEx;
		} catch (Throwable th) {
			LOG.error("Unknown error catched in the command", th);//$NON-NLS-1$
			throw new CommandException(method.getPath(), CommandException.UNKNOWN_ERR,
					"Unknown error catched in the command", th);//$NON-NLS-1$
		} finally {
			method.releaseConnection();
		}

		return doc;
	}




	/** Connection client */
	private HttpClient client;

	/** Connection parameters */
	private HttpClientParams params= new HttpClientParams();

	/** Base URL method */
	private String serverURL;

	/** Retry handler */
	private HttpMethodRetryHandler retryHandler;

	/** Encoding character set of the exchanges (HTTP and XML content) */
	private String encodingCharset;

	/** Builder for parsing responses */
	private SAXBuilder saxBuilder = new SAXBuilder();

	/** Class logger */
	private static Logger LOG = Logger.getLogger(TConnection.class);
}
