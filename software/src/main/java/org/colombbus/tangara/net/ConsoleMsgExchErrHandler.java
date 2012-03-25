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
 * 
 * 
 * @author gwen
 */
public class ConsoleMsgExchErrHandler implements MessageExchangeErrHandler {

	/**
	 * Default constructor
	 */
	public ConsoleMsgExchErrHandler() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.colombbus.tangara.net.MessageExchangeErrHandler#handleExchangeError(org.colombbus.tangara.net.CommandException)
	 */
	@Override
	public boolean handleExchangeError(CommandException cmdEx) {
		//Programme.instance().printlnError(msg);
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.colombbus.tangara.net.MessageExchangeErrHandler#handleExchangeError(java.lang.Throwable)
	 */
	@Override
	public boolean handleExchangeError(Throwable th) {
		// TODO Auto-generated method stub
		return false;
	}

}
