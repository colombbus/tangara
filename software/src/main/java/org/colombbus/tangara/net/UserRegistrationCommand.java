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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

public class UserRegistrationCommand extends Command {

	public UserRegistrationCommand() {
		super("connection.php");
	}


	public String register(TConnection conn, String username) throws CommandException {
		LOG.info("Registering user " + username); //$NON-NLS-1$ 
		clearAllArgs();
		addURLArg("username", username); //$NON-NLS-1$
		addURLArg("action", "register"); //$NON-NLS-1$ //$NON-NLS-2$
		String ipAddress = null;
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			String msg = "Cannot found localhost"; //$NON-NLS-1$
			LOG.error(msg, e);
			throw new CommandException(ipAddress,CommandException.NO_LOCAL_HOST_ERR, msg, e);
		}
		addURLArg("ipAddress", ipAddress); //$NON-NLS-1$
		Document doc = conn.sendCommand(this);
		String connectID = doc.getRootElement().getChild("user")
				.getAttributeValue("connectID");

		if (LOG.isInfoEnabled()) {
			String msg = String
					.format(
							"User %s registered with connection ID %s", username, connectID);//$NON-NLS-1$ 
			LOG.info(msg);
		}
		
		LOG.info("User " + username + " registered "); //$NON-NLS-1$  //$NON-NLS-2$
		return connectID;
	}

	public void unregister(TConnection conn,String connectID) throws CommandException {
		LOG.info("Unregistering user " + connectID); //$NON-NLS-1$
		clearAllArgs();
		addURLArg("connectID", connectID); //$NON-NLS-1$
		addURLArg("action", "unregister"); //$NON-NLS-1$ //$NON-NLS-2$
		conn.sendCommand(this);
		LOG.info("User " + connectID + " unregistered"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@SuppressWarnings("unchecked")
	public List<UserInfo> getRegisteredUsers(final TConnection conn) throws CommandException {
		LOG.info("Listing registered users"); //$NON-NLS-1$
		final List<UserInfo> userList = new ArrayList<UserInfo>();
		clearAllArgs();
		addURLArg("action", "list"); //$NON-NLS-1$ //$NON-NLS-2$
		Document doc = conn.sendCommand(this);
		final List<Element> userElemList = doc.getRootElement().getChildren("user"); //$NON-NLS-1$
		for (Element userElem : userElemList) {
			String username = userElem.getAttributeValue("name"); //$NON-NLS-1$
			// //$NON-NLS-1$
			String ipAddress = userElem.getAttributeValue("ipAddress"); //$NON-NLS-1$
			UserInfo user = new UserInfo(username, ipAddress);
			userList.add(user);
		}

		LOG.info("Registered users listed"); //$NON-NLS-1$
		return userList;
	}

	public void clearUserList(TConnection conn) throws CommandException {
		LOG.info("Clearing all users"); //$NON-NLS-1$
		clearAllArgs();
		addURLArg("action", "clear"); //$NON-NLS-1$ //$NON-NLS-2$
		conn.sendCommand(this);
		LOG.info("All users cleared"); //$NON-NLS-1$
	}

	/** Class logger */
	private static Logger LOG = Logger.getLogger(UserRegistrationCommand.class);
}
