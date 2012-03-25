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

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.colombbus.tangara.Program;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author gwen
 * 
 */
public class FileNetMessageHandler extends AbstractNetMessageHandler {

	/** Type of message */
	public static final String TYPE = "file";//$NON-NLS-1$

	/**
	 * Create a new message handler for file
	 */
	public FileNetMessageHandler() {
		super(TYPE);
	}

	/* (non-Javadoc)
	 * @see org.colombbus.tangara.net.NetMessageHandler#processMessage(org.colombbus.tangara.net.NetMessage, java.lang.String, java.util.List)
	 */
	@Override
	public void processMessage(NetMessage netMsg, String connectID,
			List<NetMessage> outNetMsg) {
		String content = netMsg.getContent();
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(new StringReader(content));
		} catch (JDOMException e) {
			LOG.error("Cannot parse XML content message:\n" + content, e);
			String pattern = Messages
					.getString("FileNetMessageHandler.badXMLFormat");
			String errMsg = MessageFormat.format(pattern, netMsg
					.getSourceUser()); //$NON-NLS-1$
			Program.instance().printError(errMsg);
		} catch (IOException e) {
			// never happen because we use memory reader
			LOG.fatal("Cannot parse XML content message", e);
			String msgName = Messages
					.getString("FileNetMessageHandler.readError");//$NON-NLS-1$
			String errMsg = MessageFormat.format(msgName, netMsg
					.getSourceUser());
			Program.instance().printError(errMsg);
			return;
		}
		String filename = doc.getRootElement().getAttributeValue("name");
		String fileContentStr = doc.getRootElement().getValue();
		byte[] fileContentBase64 = fileContentStr.getBytes();
		byte[] fileContentBinary = Base64.decodeBase64(fileContentBase64);
		
		LOG.info("Reception of file " + filename + " sent by " + netMsg.getSourceUser());

		String pattern = Messages.getString("FileNetMessageHandler.saveAsContext");
		String contextMsg = MessageFormat.format(pattern, netMsg.getSourceUser(), filename);		
		FileReceptionDialog saveAsDlg = new FileReceptionDialog(Program.instance().getFrame(),contextMsg,filename, fileContentBinary);
		saveAsDlg.setVisible();
	}

	/** Class logger */
	private static Logger LOG = Logger.getLogger(FileNetMessageHandler.class);
}
