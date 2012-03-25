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
import org.colombbus.tangara.ScriptExecResult;

/**
 * 
 * @author gwen
 */
public class CommandNetMsgHandler extends AbstractNetMessageHandler {

	/**
	 * Tangara programme object name
	 */
	public static final String PROG_OBJ_NAME = "programme";//$NON-NLS-1$

	/**
	 * The message type
	 */
	public static final String TYPE = "command";//$NON-NLS-1$

	/**
	 * Default constructor
	 *
	 */
	public CommandNetMsgHandler() {
		super(TYPE);
	}

	/* (non-Javadoc)
	 * @see org.colombbus.tangara.net.NetMessageHandler#processMessage(org.colombbus.tangara.net.NetMessage, java.lang.String, java.util.List)
	 */
	@Override
	public void processMessage(final NetMessage netMsg, final String connectID,
			final List<NetMessage> outNetMsg) {
		String script = netMsg.getContent();
		String scriptMsg = MessageFormat.format(Messages
				.getString("CommandNetMsgHandler.runScript"), netMsg.getSourceUser()); //$NON-NLS-1$
		Program.instance().printOutputMessage(scriptMsg);
		ScriptExecResult scriptRes = Program.instance().executeScriptGetResult(script);		
		if ((scriptRes!=null)&&(outNetMsg!=null))
		{
			if (scriptRes.getOuput() != null && scriptRes.getOuput().length() > 0) {
				NetMessage outMsg = new NetMessage("", connectID, //$NON-NLS-1$
						netMsg.getSourceUser(), PROG_OBJ_NAME, 
						"command-output", scriptRes.getOuput()); //$NON-NLS-1$
				outNetMsg.add(outMsg);
			}
			if (scriptRes.getError() != null && scriptRes.getError().length() > 0) {
				NetMessage errMsg = new NetMessage("", connectID, //$NON-NLS-1$
						netMsg.getSourceUser(), PROG_OBJ_NAME, 
						"command-error", scriptRes.getError()); //$NON-NLS-1$
				outNetMsg.add(errMsg);
			}
		}
	}

}
