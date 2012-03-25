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

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.log4j.Logger;

/**
 * A simple handler of HTTP reconnection.
 * <p>
 * It is based on a simple retry count. If the number of retries is greater than
 * a specified value, the handler gives-up.
 * </p>
 * 
 * @author gwen
 */
public class HttpConnRetryHandler implements HttpMethodRetryHandler {

	/**
	 * Create a new handler
	 * 
	 * @param maxRetries
	 *            maximum number of tries. It shall be greater than 0
	 */
	public HttpConnRetryHandler(int maxRetries) {
		super();
		if (maxRetries < 1) {
			LOG.error("Fail to create handler. The maxRetries parameter shall be greater than 0");
			throw new IllegalArgumentException("maxRetries parameter shall be greater than 0"); 
		}
		this.maxRetries = maxRetries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.httpclient.HttpMethodRetryHandler#retryMethod(org.apache.commons.httpclient.HttpMethod,
	 *      java.io.IOException, int)
	 */
	@Override
	public boolean retryMethod(final HttpMethod method,
			final IOException exception, int executionCount) {
		LOG.info("Try number " + executionCount);
		if (executionCount >= maxRetries) {
			LOG.warn("Too many tries (" + executionCount + ")", exception);
			// Do not retry if over max retry count
			return false;
		}
		if (exception instanceof NoHttpResponseException) {
			LOG.error("No response from the server. No more tries", exception);
			// Retry if the server dropped connection on us
			return true;
		}
		if (!method.isRequestSent()) {
			LOG.error("Request not sent. No more tries", exception);
			// Retry if the request has not been sent fully or
			// if it's OK to retry methods that have been sent
			return true;
		}

		// otherwise do not retry
		LOG.error("Source of the HTTP failure not identified. No more tries",
				exception);
		return false;
	}

	/** Number of retries */
	private int maxRetries;

	/** Class logger */
	private static Logger LOG = Logger.getLogger(HttpConnRetryHandler.class);
}
