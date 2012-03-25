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

package org.colombbus.tangara;

import java.security.PrivilegedActionException;

/**
 * An exception thrown during configuration processus
 * 
 * @author gwen
 */
@SuppressWarnings("serial")
public class ConfigurationException extends Exception {

	/**
	 * Constructs a new exception with <code>null</code> as its detail
	 * message. The cause is not initialized,
	 */
	public ConfigurationException() {
	}

	/**
	 * Constructs a new exception with the specified detail message. The cause
	 * is not initialized.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the {@link Throwable#getMessage()} method.
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message
	 * of (<code>cause==null ? null : cause.toString()</code>) (which
	 * typically contains the class and detail message of <code>cause</code>).
	 * This constructor is useful for exceptions that are little more than
	 * wrappers for other throwables (for example,
	 * {@link PrivilegedActionException}).
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link Throwable#getCause()} method). (A <code>null</code>
	 *            value is permitted, and indicates that the <code>cause</code>
	 *            is nonexistent or unknown.)
	 */
	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail <code>message</code>
	 * and <code>cause</code>. Note that the detail message associated with
	 * cause is not automatically incorporated in this exception's detail
	 * message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link Throwable#getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link Throwable#getCause()} method). (A <code>null</code>
	 *            value is permitted, and indicates that the cause is
	 *            nonexistent or unknown.)
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
