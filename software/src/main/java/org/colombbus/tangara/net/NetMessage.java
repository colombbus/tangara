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
 * @author gwen
 */
public class NetMessage {

	private String messageID;

	private String sourceUser;

	private String targetUser;

	private String targetObjectName;

	private String type;

	private String content;

	/**
	 * @param messageID
	 * @param sourceUser
	 * @param targetUser
	 * @param targetObjectName
	 * @param type
	 * @param content
	 */
	public NetMessage(String messageID, String sourceUser,
			String targetUser, String targetObjectName, String type,
			String content) {
		super();
		assert sourceUser != null : "sourceConnectID parameter shall not be null";
		assert targetUser != null : "targetConnectID parameter shall not be null";
		assert targetObjectName != null : "targetObjectName parameter shall not be null";
		assert type != null : "type parameter shall not be null";
		assert content != null : "content parameter shall not be null";

		this.messageID = messageID;
		this.sourceUser = sourceUser;
		this.targetUser = targetUser;
		this.targetObjectName = targetObjectName;
		this.type = type;
		this.content = content;
	}

	public String getMessageID() {
		return messageID;
	}

	public String getSourceUser() {
		return sourceUser;
	}

	public String getTargetUser() {
		return targetUser;
	}

	public String getTargetObjectName() {
		return targetObjectName;
	}

	public String getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof NetMessage) {
			NetMessage msg = (NetMessage) obj;
			if (msg.messageID == null || messageID == null) {
				return msg.messageID == messageID;
			}
			// TODO enhance, compare each parameter
			return msg.messageID.equals(messageID);
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Message[id=").append(
				messageID).append("][source=").append(sourceUser).append(
				"][target=").append(targetUser).append("][object=")
				.append(targetObjectName).append("][type=").append(type)
				.append("][content=").append(content).append("]");
		return builder.toString();
	}

}
