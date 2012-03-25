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

public class UserInfo {

	public UserInfo(String username, String ipAddress) {
		super();
		assert username != null : "username parameter shall not be null";
		assert ipAddress != null : "ipAddress parameter shall not be null";
		this.username = username;
		this.ipAddress = ipAddress;
	}

	public String getUsername() {
		return username;
	}


	public String getIPAddress() {
		return ipAddress;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		if (obj instanceof UserInfo) {
			UserInfo user = (UserInfo) obj;
			equals =  username.equals(user.username);
		}
		return equals;
	}

	@Override
	public String toString() {
		return new StringBuilder("User[").append(username).append(", ").
				append(ipAddress).append("]")
				.toString();
	}

	private String username;

	private String ipAddress;
}
