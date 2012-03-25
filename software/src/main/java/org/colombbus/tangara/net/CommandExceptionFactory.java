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

import java.lang.reflect.Constructor;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * @author gwen
 * 
 */
public class CommandExceptionFactory {

	/**
	 * static class
	 */
	private CommandExceptionFactory() {
		super();
	}

	public static BadServerCmdException createBadServerException(
			HttpMethod method, int code, String msg) {
		return new BadServerCmdException(method.getPath(), code, msg);
	}

	public static BadServerCmdException throwBadServerException(
			HttpMethod method, int code, String msg)
			throws BadServerCmdException {
		throw createBadServerException(method, code, msg);
	}

	public static BadServerCmdException createBadServerException(
			HttpMethod method, int code, String msg, Throwable th) {
		return new BadServerCmdException(method.getPath(), code, msg, th);
	}

	public static BadServerCmdException throwBadServerException(
			HttpMethod method, int code, String msg, Throwable th)
			throws BadServerCmdException {
		throw createBadServerException(method, code, msg, th);
	}

	public static CommandException createException(HttpMethod method, int code,
			String msg, Throwable th) {
		CommandException cmdEx = null;

		// MalformedCommandException
		// BadServerCmdException
		Class<?> exClass = null;
		switch (code) {
		case 1: // Could not connect to database
		case 2: // Could not select database
		case 3: // Connection count query failed
		case 4: // Old connection removing query failed
		case 7: // List user query failed
		case 10: // Insert registration query failed
			// FIXME, the remove registration has the same error code
		case 11: // Delete all objects query failed
		case 14: // List objects query failed
		case 15: // Insert object registration query failed. <sql error code>
		case 16: // Unregister object failed. <sql error code>
		case 17: // Fail to start transaction
		case 18: // Fail to commit transaction
		case 19: // Fail to analyse message
		case 20: // Fail to insert message
		case 21: // Fail to list messages
		case 22: // Fail to find a connection
		case 26: // Fail to update last connection time
		case 28: // Old connection message removing query failed
		case 29: // Fail to find previous unset messages
		case 30: // Fail to delete previous unset messages
		case 36: // QUERY_FAILURE The query execution failed [$query]
			exClass = InternalSeverCmdException.class;
			break;
		case 5: // No action defined
		case 6: // Unsupported action +$action
		case 8: // username parameter not defined
		case 9: // connectID parameter not defined
		case 12: // objectname parameter not defined
		case 13: // objectclass parameter not defined
		case 24: // ipAddress parameter not defined
			exClass = MalformedCommandException.class;
			break;
		case 23: // User $username already exists with address $ipAddress //
			// already connected
		case 25: // User already connected // already exists
		case 27: // Unknown user
		case 31: // Fail to get username from connectID $connectID
		case 32: // Fail to register avatar image of user $connectID
		case 34: // Fail to get avatar image of user $username
			exClass = BadParamCmdException.class;
			break;
		case 35: // NOT_CONNECTED The connectID $connectID does not exist
			exClass = UnknownUserCmdException.class;
			break;
		default:
			LOG.warn("unhandled error code " + code);
			exClass = CommandException.class;
		}

		try {
			Constructor<?> construct = null;
			Object[] args = null;
			if (th == null) {
				construct = exClass.getConstructor(String.class, Integer.TYPE,
						String.class);
				args = new Object[3];
				args[0] = method.getPath();
				args[1] = code;
				args[2] = msg;
			} else {
				construct = exClass.getConstructor(String.class, Integer.TYPE,
						String.class, Throwable.class);
				args = new Object[4];
				args[0] = method.getPath();
				args[1] = code;
				args[2] = msg;
				args[3] = th;
			}
			cmdEx = (CommandException) construct.newInstance(args);
		} catch (Throwable thEx) {
			LOG.error("Cannot instanciate the dedicated command exception",
					thEx);
			if (th == null) {
				cmdEx = new CommandException(method.getPath(), 0, msg);
			} else {
				cmdEx = new CommandException(method.getPath(), 0, msg, th);
			}
		}

		return cmdEx;
	}

	public static void throwException(HttpMethod method, int code, String msg,
			Throwable th) throws CommandException {
		CommandException cmdEx = createException(method, code, msg, th);
		throw cmdEx;
	}

	public static void throwException(HttpMethod method, int code, String msg)
			throws CommandException {
		CommandException cmdEx = createException(method, code, msg, null);
		throw cmdEx;
	}

	public static void throwException(HttpMethod method, Element errorE)
			throws CommandException {
		// ENHANCE check the parsed value
		int code = Integer.parseInt(errorE.getAttributeValue("code")); //$NON-NLS-1$
		String msg = errorE.getText();
		throwException(method, code, msg);
	}
	
	public static CommunicationCmdException createCommunicationException(HttpMethod method, int code, String msg, Throwable th) {
		return new CommunicationCmdException(method.getPath(), code, msg, th);
	}
//	public static void throwCommunicationException(HttpMethod method, int code, String msg, Throwable th) throws CommunicationCmdException{
//		throw new CommunicationCmdException(method.getPath(), code, msg, th);
//	}

	/** Class logger */
	private static Logger LOG = Logger.getLogger(CommandException.class);
}
