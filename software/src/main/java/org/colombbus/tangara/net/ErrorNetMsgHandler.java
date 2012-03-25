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
import java.util.StringTokenizer;

import org.colombbus.tangara.Program;

/**
 * Handler of <code>error</code> messages 
 * @author gwen
 */
public class ErrorNetMsgHandler extends AbstractNetMessageHandler {

	/** The type of error message */
	public static final String TYPE = "error";

	/**
	 * Create a new handler of error messages
	 */
	public ErrorNetMsgHandler() {
		super(TYPE);
	}

	/* (non-Javadoc)
	 * @see org.colombbus.tangara.net.NetMessageHandler#processMessage(org.colombbus.tangara.net.NetMessage, java.lang.String, java.util.List)
	 */
	@Override
	public void processMessage(NetMessage netMsg, String connectID,
			List<NetMessage> outNetMsg) {
		StringTokenizer tokenizer = new StringTokenizer(netMsg.getContent(),
				":");
		String code = tokenizer.nextToken();
		if ("027".equals(code)) {
			String username = tokenizer.nextToken();
			String outMsg = MessageFormat.format(Messages
					.getString("ErrorNetMsgHandler.notConnected"), username); //$NON-NLS-1$
			Program.instance().printError(outMsg);
		}
	}

}
