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
 * A partial implemention of {@link NetMessageHandler}.
 * <p>
 * It implements the message type part.
 * </p>
 * 
 * @author gwen
 * 
 */
public abstract class AbstractNetMessageHandler implements NetMessageHandler {

	/**
	 * Create a new {@link NetMessageHandler} from a message type
	 * 
	 * @param type
	 *            the handled message type
	 */
	public AbstractNetMessageHandler(String type) {
		super();
		handledType = type;
	}

	/* (non-Javadoc)
	 * @see org.colombbus.tangara.net.NetMessageHandler#getHandledType()
	 */
	@Override
	public String getHandledType() {
		return handledType;
	}

	/** type of message handled */
	private final String handledType;
}
