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

package org.colombbus.tangara.objects;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.Configuration;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;
import org.colombbus.tangara.net.Avatar;
import org.colombbus.tangara.net.AvatarCommand;
import org.colombbus.tangara.net.CheckedException;
import org.colombbus.tangara.net.CommandException;
import org.colombbus.tangara.net.CommandNetMsgHandler;
import org.colombbus.tangara.net.ConsoleMsgExchErrHandler;
import org.colombbus.tangara.net.DefaultNetMsgProcessor;
import org.colombbus.tangara.net.FileNetMessageHandler;
import org.colombbus.tangara.net.MessageNetMsgHandler;
import org.colombbus.tangara.net.NetMessage;
import org.colombbus.tangara.net.TConnection;
import org.colombbus.tangara.net.TConnectionFactory;
import org.colombbus.tangara.net.TMessageExchangeManager;
import org.colombbus.tangara.net.UserInfo;
import org.colombbus.tangara.net.UserRegistrationCommand;
import org.colombbus.tangara.net.VersionCommand;

/**
 * This class provides a tool to use Internet services.
 */
@Localize(value="Internet",localizeParent=true)
public abstract class Internet extends TObject{


	// TODO explicit this error codes: just for server usage or also for client usage
	public static final int ERROR_USER_ALREADY_CONNECTED = 25;

	public static final int ERROR_USER_ALREADY_EXISTS = 23;

	public static final int ERROR_CONNECTION_FAILURE = 30;

	public static final int ERROR_UNKNOWN = 40;

	public static final int MALFORMED_ADDRESS= 50;

	private static final TConnectionFactory CONN_FACTORY = new TConnectionFactory();

	private static final DefaultNetMsgProcessor MSG_PROCESSOR = new DefaultNetMsgProcessor();

	/**
	 * Creates an instance of this class.
	 */
	@Localize(value="Internet")
	public Internet() {
		super();
	}

	/**
	 * Connects the user on the server.
     * @param server
     * @param user
     * @return 0 if no errors occurred
	 */
	@Localize(value="Internet.connectServer")
	public int connectServer(String server, String user) {
		if (user == null || user.length() == 0) {
			String message = getMessage("badUsername"); //$NON-NLS-1$
			Program.instance().printOutputMessage(message);
			return ERROR_UNKNOWN;
		}

		try {
			conn = CONN_FACTORY.getConnection(server);
		} catch (MalformedURLException e) {
			LOG.error("malformed server address",e);
			return MALFORMED_ADDRESS;
		}

		VersionCommand cmd = new VersionCommand();
		try {
			cmd.getVersion(conn);
		} catch (CommandException e) {
			LOG
					.debug("Connection failure to " + server + ": "
							+ e.getMessage());
			String message = MessageFormat.format(getMessage("badServerName"), server); //$NON-NLS-1$
			Program.instance().printError(message);
			return ERROR_CONNECTION_FAILURE;
		}

		if (connected)
		{
			disconnectServer();
		}
		
		username = user;
		UserRegistrationCommand connectCmd = new UserRegistrationCommand();

		try {
			connectID = connectCmd.register(conn, username);
		} catch (CommandException e) {
			String message = null;
			switch (e.getErrorCode()) {
			case ERROR_USER_ALREADY_EXISTS:
				message = MessageFormat.format(getMessage("userAlreadyExist"), username); //$NON-NLS-1$
				Program.instance().printError(message);
				return ERROR_USER_ALREADY_EXISTS;
			case ERROR_USER_ALREADY_CONNECTED:
				message = MessageFormat.format(getMessage("userAlreadyConnected"), username); //$NON-NLS-1$
				Program.instance().printOutputMessage(message);
				initializeConnection();
				connected = true;
				return ERROR_USER_ALREADY_CONNECTED;
			default:
				message = getMessage("error"); //$NON-NLS-1$
				Program.instance().printError(message);
				break;
			}
			return ERROR_UNKNOWN;
		}

		// everything is fine
		String message = MessageFormat.format(getMessage("connected"), username, conn.getServerURL());
		Program.instance().printOutputMessage(message);
		connected = initializeConnection();
		return 0;
	}

	/**
	 * initializes the message pump
	 *
	 * @return <code>true</code> if the connection is initialized,
	 *         <code>false</code> otherwise
	 */
	// modif BP: removed until the need for it to be published is understood
	//@Localize(value="Internet.initConnection")
	public boolean initializeConnection() {
		try {
			String checkFrequencyStr = Configuration.instance().getProperty("network.check-frequence");
			long checkFrequency = Long.parseLong(checkFrequencyStr);

			String serverVersion = Configuration.instance().getProperty("tangara.version");//$NON-NLS-1$

			String nbMaxMsgStr = Configuration.instance().getProperty("network.max-msg-per-send");
			int nbMaxMsgPerSend = Integer.parseInt(nbMaxMsgStr);

			synchronized (MSG_PROCESSOR) {
				//TODO may be put this is configuration class ?
				if( MSG_PROCESSOR.hasHandlers()==false) {
					MSG_PROCESSOR.loadConfiguration();
				}
			}
			if (exchangeMgr !=null )
			{
				exchangeMgr.shutdown();
			}
			ConsoleMsgExchErrHandler consoleErrHlder = new ConsoleMsgExchErrHandler();
			exchangeMgr = new TMessageExchangeManager(conn,connectID,checkFrequency,nbMaxMsgPerSend, serverVersion, MSG_PROCESSOR,consoleErrHlder);
		} catch (Exception e) {
			LOG.error("Fail to init connection", e);
			String msg = getMessage("initConnectionFailed");
			Program.instance().printError(msg);
			return false;
		}
		return true;
	}

	/**
	 * Enables to disconnect from the server
	 */
	@Localize(value="Internet.disconnectServer")
	public void disconnectServer() {
		if (connected == false) {
			String message = MessageFormat.format(getMessage("userAlreadyDisconnected"), username); //$NON-NLS-1$
			Program.instance().printError(message);
			return;
		}
		exchangeMgr.shutdown();
		connected = false;
		UserRegistrationCommand connectCmd = new UserRegistrationCommand();

		try {
			connectCmd.unregister(conn, connectID);
		} catch (CommandException e) {
			String message = MessageFormat.format(getMessage("error"), username); //$NON-NLS-1$
			Program.instance().printError(message);
			return;
		}

		String message = MessageFormat
				.format(
						getMessage("disconnected"), username, conn.getServerURL()); //$NON-NLS-1$
		Program.instance().printOutputMessage(message);
	}

	/**
	 * Prints all the clients in the console.
	 */
	@Localize(value="Internet.printClients")
	public void printClients() {
		try {
			checkConnected();
			UserRegistrationCommand connectCmd = new UserRegistrationCommand();
			List<UserInfo> users = null;
			try {
				users = connectCmd.getRegisteredUsers(conn);
			} catch (CommandException e) {
				String message = MessageFormat.format(getMessage("error"), username); //$NON-NLS-1$
				Program.instance().printError(message);
				return;
			}

			StringBuilder message = new StringBuilder(getMessage("userList"));
			for (UserInfo user : users) {
				message.append("\n\t").append(user.getUsername());
			}

			Program.instance().printOutputMessage(message.toString());
		} catch (CheckedException chEx) {
			// ignore
		} catch (Throwable thEx) {
			LOG.warn("Uncaught exception ", thEx);
		}
	}

	/**
	 * Returns the list of registered users.
	 * @return List<String>
	 */
	// modif BP: removed until the need for it to be published is understood
	//@Localize(value="Internet.getClients")
	public List<String> getClients() {
		try {
			checkConnected();
			UserRegistrationCommand connectCmd = new UserRegistrationCommand();
			List<UserInfo> users = null;
			try {
				users = connectCmd.getRegisteredUsers(conn);
			} catch (CommandException e) {
				return null;
			}
			List<String> result = new ArrayList<String>();
			for (UserInfo user : users) {
				result.add(user.getUsername());
			}
			return result;
		} catch (CheckedException chEx) {
			// ignore
		} catch (Throwable thEx) {
			LOG.warn("Uncaught exception ", thEx);
		}
		// by default, return an empty list
		return Collections.<String>emptyList();
	}

/**
 * Returns username if it exists.
 * @return username
 */
	@Localize(value="Internet.getMyName")
	public String getMyName()
	{
		if (username != null)
			return username;
		else
			return "";
	}

	/**
	 * sends a message to a user
	 * @param user
	 * @param message
	 */
	@Localize(value="Internet.sendMessage")
	public void sendMessage(String user, String message)
	{
		try {
			checkConnected();
			checkTargetUser(user);

			NetMessage msg = new NetMessage("", Internet.username,
					user, "", MessageNetMsgHandler.TYPE, message);
			exchangeMgr.sendMessage(msg);
		} catch (CheckedException ignore) {
		} catch (Throwable thEx) {
			LOG.warn("uncaught exception ", thEx);
		}
	}

	/**
	 * sends a file to a user
	 * @param user
	 * @param fileName
	 */
	@Localize(value = "Internet.sendFile")
	public void sendFile(String user, String fileName)
	{
		try {
			checkConnected();
			checkTargetUser(user);
			File path = findReadableFile(fileName);
			byte[] binaryContent = toByteArray(path, fileName);
			byte[] base64content = Base64.encodeBase64(binaryContent);
			String fileContent = new String(base64content);

			StringBuilder xmlContent = new StringBuilder();
			xmlContent
					.append("<file name=\"").append(path.getName()).append("\">")//$NON-NLS-1$ //$NON-NLS-2$
					.append(fileContent).append("</file>");//$NON-NLS-1$
			NetMessage msg = new NetMessage("", Internet.username,
					user, "", FileNetMessageHandler.TYPE, xmlContent
							.toString());
			exchangeMgr.sendMessage(msg);
		} catch (CheckedException chEx) {
			// ignore
		} catch (Throwable thEx) {
			LOG.warn("Uncaught exception ", thEx);
		}
	}

	/**
	 * Convert the content of a file to a byte array
	 *
	 * @param path
	 *            the full path to the file
	 * @param fileName
	 *            the name used to identify the file in the user messages
	 * @return binaryContent
	 *           the file content in byte array format
	 * @throws CheckedException
	 *             if an error occurs
	 */
	// modif BP : removed until the need for it to be published is understood
	//@Localize(value="Internet.toByteArray")
	public byte[] toByteArray(File path, String fileName)
			throws CheckedException {
		String maxSizeStr = Configuration.instance().getProperty("internet.max-exchange-size");
		long maxFileSize = Long.parseLong(maxSizeStr);
		if (path.length() > maxFileSize) {
			String msg = path.getAbsolutePath()
					+ " is too big. Maximum authorized size is " + maxSizeStr
					+ " bytes";
			String errMsg = MessageFormat.format(getMessage("dataTooBig"), fileName, maxSizeStr); //$NON-NLS-1$
			Program.instance().printError(errMsg);
			throw new CheckedException(msg);
		}

		byte[] binaryContent = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(path);
			binaryContent = IOUtils.toByteArray(in);
		} catch (IOException ioEx) {
			String errMsg = MessageFormat.format(getMessage("dataTooBig"), fileName, maxSizeStr); //$NON-NLS-1$
			Program.instance().printError(errMsg);
		} finally {
			IOUtils.closeQuietly(in);
		}

		return binaryContent;
	}

	/**
	 * Sends a command to a user
	 * @param user
	 * @param command
	 */
	@Localize(value = "Internet.sendCommand")
	public void sendCommand(String user, String command)
	{
		try {
			checkConnected();
			checkTargetUser(user);

			NetMessage msg = new NetMessage("", Internet.username,
					user, "", CommandNetMsgHandler.TYPE, command);
			exchangeMgr.sendMessage(msg);
		} catch (CheckedException chEx) {
			// ignore
		} catch (Throwable thEx) {
			LOG.warn("Uncaught exception ", thEx);
		}
	}

	/**
	 * defines the user's avatar
	 * @param fileName
	 */
	@Localize(value = "Internet.defineAvatar")
	public void defineAvatar(String fileName)
	{
		try {
			checkConnected();
			File avatarPath = findReadableFile(fileName);
			byte[] fileContent = toByteArray(avatarPath, fileName);

			AvatarCommand avatarCmd = new AvatarCommand();
			String fileExt = FilenameUtils.getExtension(avatarPath.getName());
			avatarCmd.setImage(conn, connectID, fileContent, fileExt);
		} catch (CheckedException chEx) {
			// ignore
		} catch (CommandException cmdEx) {
			String msg = getMessage("failToRegisterAvatar");
			Program.instance().printError(msg);
		} catch (Throwable thEx) {
			LOG.warn("Uncaught exception ", thEx);
		}
	}


	/**
	 * returns the user's avatar
	 * @param user
	 * @return BufferedImage
	 */
	@Localize(value="Internet.getAvatar")
	public BufferedImage getAvatar(String user)
	{
		try {
			checkConnected();
			checkTargetUser(user);

			AvatarCommand avatarCmd = new AvatarCommand();
			Avatar avatar = avatarCmd.getAvatar(conn, user);

			if (avatar.getImage().length == 0) {
				LOG.info("No avatar associated to user " + user);
				String pattern = getMessage("noAvatar");
				String msg = MessageFormat.format(pattern, avatar.getName());
				Program.instance().printError(msg);
				return null;
			}

			ByteArrayInputStream byteStream = new ByteArrayInputStream(avatar
					.getImage());
			BufferedImage image = ImageIO.read(byteStream);
			IOUtils.closeQuietly(byteStream);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Image loaded. height:" + image.getHeight()
						+ ", width:" + image.getWidth());
			}

			return image;
		} catch (CommandException cmdEx) {
			switch (cmdEx.getErrorCode()) {
			case 27:
				LOG.warn("Unknown user " + user + ". Cannot get its avatar");
				String pattern = getMessage("notConnected");
				String msg = MessageFormat.format(pattern, user);
				Program.instance().printError(msg);
				break;
			default:
				LOG.warn("Unhandle exception ", cmdEx);
				// TODO
			}
		} catch (CheckedException ignore) {
		} catch (Throwable thEx) {
			LOG.warn("Uncaught exception ", thEx);
		}
		return null;
	}

	/**
	 * checks that the connection is fine (that connected = true)
	 * @throws CheckedException
	 */
	// modif BP : removed until the need for it to be published is understood
	//@Localize(value="Internet.checkConnected")
	public void checkConnected() throws CheckedException {
		if (connected == false) {
			String msg = "User " + username + " not connected";
			String userMsg = null;
			if (username != null) {
				String pattern = getMessage("notConnected");
				userMsg = MessageFormat.format(pattern, username); //$NON-NLS-1$
			} else
				userMsg = getMessage("notConnectedAndNullClient");
			Program.instance().printError(userMsg);
			throw new CheckedException(msg);
		}
	}

	/**
	 * checks that a user exists
	 * @param targetUser
	 * @throws CheckedException
	 */
	// modif BP : removed until the need for it to be published is understood 
	//@Localize(value="Internet.checkTargetUser")
	public void checkTargetUser(String targetUser) throws CheckedException {
		if (targetUser == null || targetUser.length() == 0) {
			String msg = "Invalid target user. It cannot be empty";
			String errMsg = MessageFormat.format(getMessage("emptyTargetUser"), targetUser); //$NON-NLS-1$
			Program.instance().printError(errMsg);
			throw new CheckedException(msg);
		}
	}

	/**
	 * returns the file of a given file name if it can be read
	 * @param fileName
	 * @return File
	 * @throws CheckedException
	 */
	// modif BP : removed until the need for it to be published is understood 
	//@Localize(value="Internet.findReadableFile")
	public File findReadableFile(String fileName) throws CheckedException {
		File path = FileUtils.findPath(fileName);
		if (path.isFile() == false) {
			String msg = path.getAbsolutePath() + " is not a file";
			String errMsg = MessageFormat.format(getMessage("notFile"), fileName); //$NON-NLS-1$
			Program.instance().printError(errMsg);
			throw new CheckedException(msg);
		} else if (path.canRead() == false) {
			String msg = fileName + " cannot be read";
			String errMsg = MessageFormat.format(getMessage("notReadableFile"), fileName); //$NON-NLS-1$
			Program.instance().printError(errMsg);
			throw new CheckedException(msg);
		}
		return path;
	}

	@Override
	public void deleteObject()
	{
		if (connected)
		{
			disconnectServer();
		}
		super.deleteObject();
	}
	
	private static String username;

	private static boolean connected = false;

	private static String connectID;

	private static TConnection conn;

	private static TMessageExchangeManager exchangeMgr = null;

	/** Class logger */
	private final Logger LOG = Logger.getLogger(Internet.class);
}
