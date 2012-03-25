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

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Send a command to the server to set and to get the avatar of a user.
 * <p>
 * An avatar is just a byte array interpreted by the service caller.
 * </p>
 * @author gwen
 */
public class AvatarCommand extends Command {

	/** avatar XML element */
	private static final String AVATAR_E = "avatar";//$NON-NLS-1$

	/** avatar/image XML attribute */
	private static final String IMAGE_A = "image";//$NON-NLS-1$

	/** avatar/type XML attribute */
	private static final String TYPE_A = "type";//$NON-NLS-1$

	/** The PHP command relative to the server */
	private static final String PHP_COMMAND ="connection.php";//$NON-NLS-1$

	/**
	 * Default constructor 
	 */
	public AvatarCommand() {
		super(PHP_COMMAND);//$NON-NLS-1$
	}


	/**
	 * set the image associated to a user
	 * 
	 * @param conn
	 * @param connectID
	 *            the identifier of the user represented by the image
	 * @param image
	 *            the image
	 * @param type
	 */
	
	public void setImage(final TConnection conn, final String connectID, final byte[] image, final String type)
			throws CommandException {
		// clear previous parameters
		clearURLArgs();
		clearBodyArgs();

		// set new parameters
		addURLArg("action", "registerAvatar"); //$NON-NLS-1$ //$NON-NLS-2$
		addURLArg("connectID", connectID); //$NON-NLS-1$

		String base64Image = new String(Base64.encodeBase64(image));
		addBodyArg(IMAGE_A, base64Image);
		addBodyArg(TYPE_A, type);
		conn.sendCommand(this);
		LOG.info("Avatar of " + connectID + " is sent");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Get the avatar of a user
	 * 
	 * @param conn
	 *            the connection to the server
	 * @param username
	 *            the name of the user
	 * @return the avatar of the user <code>username</code>
	 * @throws CommandException
	 *             if an error occurs during the operation
	 */
	public Avatar getAvatar(final TConnection conn, final String username )throws CommandException {
		// clear previous parameters
		clearURLArgs();
		clearBodyArgs();

		// set new parameters
		addURLArg("action", "getAvatar"); //$NON-NLS-1$ //$NON-NLS-2$
		addURLArg("username", username); //$NON-NLS-1$
		Document doc = conn.sendCommand(this);

		Element avatarE = doc.getRootElement().getChild(AVATAR_E);
		String type = avatarE.getAttributeValue(TYPE_A);
		String image = avatarE.getAttributeValue(IMAGE_A);
		byte[] fileContentBase64 = image.getBytes();
		byte[] fileContentBinary = Base64.decodeBase64(fileContentBase64);
		
		return new Avatar(username, fileContentBinary, type);
	}

	/** Class logger */
	private static Logger LOG = Logger.getLogger(AvatarCommand.class);
}
