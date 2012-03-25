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

import java.text.MessageFormat;
import java.util.List;

import org.colombbus.tangara.Program;

/**
 * An handler of <code>command-error</code> messages
 * <p>
 * A <code>command-error</code> is an error returned by a remote command
 * execution.
 * </p>
 * 
 * @author gwen
 */
public class ErrorCmdNetMsgHandler extends AbstractNetMessageHandler {

	/** The type of the handled messages */
	public static final String TYPE = "command-error";

	/**
	 * Create a new handler
	 */
	public ErrorCmdNetMsgHandler() {
		super(TYPE);
	}

	/* (non-Javadoc)
	 * @see org.colombbus.tangara.net.NetMessageHandler#processMessage(org.colombbus.tangara.net.NetMessage, java.lang.String, java.util.List)
	 */
	@Override
	public void processMessage(NetMessage netMsg, String connectID,
			List<NetMessage> outNetMsg) {
		String errMsg = MessageFormat.format(
							Messages.getString("ErrorCmdNetMsgHandler.runScriptError"),//$NON-NLS-1$ 
							netMsg.getSourceUser(), 
							netMsg.getContent()); 
		Program.instance().printError(errMsg);
	}

}
