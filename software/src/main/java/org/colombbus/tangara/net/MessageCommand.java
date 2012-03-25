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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * Echanging messages with other tangara cliens through the tangara
 * 
 * @author gwen
 */
public class MessageCommand extends Command {

	/**
	 * Create a new message command
	 * @param version
	 */
	public MessageCommand(String version) {
		super("message.php");
		this.version=version;
	}


	public List<NetMessage> exchangeMessage(TConnection conn, String connectID,
			List<NetMessage> inMsgList) throws CommandException {
		LOG.info("Exchanging messages start"); //$NON-NLS-1$
		clearAllArgs();
		addURLArg("action", "exchange"); //$NON-NLS-1$ //$NON-NLS-2$
		addURLArg("connectID", connectID); //$NON-NLS-1$

		// build the input message
		String input = buildInput(conn, inMsgList);
		if( LOG.isDebugEnabled()) {
			LOG.debug("content:\n"+input);
		}
		addBodyArg("message", input); //$NON-NLS-1$

		Document doc = conn.sendCommand(this);

		// parse output message
		List<NetMessage> returnedMessages = parseOutput(doc);

		LOG.info("Exchanging messages done"); //$NON-NLS-1$
		return returnedMessages;
	}

	public void deleteAll(TConnection conn) throws CommandException {
		LOG.info("Delete all messages start"); //$NON-NLS-1$
		clearAllArgs();
		addURLArg("action", "deleteAll"); //$NON-NLS-1$ //$NON-NLS-2$

		conn.sendCommand(this);

		LOG.info("Delete all messages done");//$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	private List<NetMessage> parseOutput(Document doc) {
		List<NetMessage> receivedMsgList = new ArrayList<NetMessage>();
		Element tangaraE = doc.getRootElement();
		List<Element> messageElemList = tangaraE.getChildren("message");//$NON-NLS-1$
		for (Element msgE : messageElemList) {
			String msgId = msgE.getAttributeValue("messageID");//$NON-NLS-1$
			String source = msgE.getAttributeValue("source");//$NON-NLS-1$
			String target = msgE.getAttributeValue("target");//$NON-NLS-1$
			String object = msgE.getAttributeValue("object");//$NON-NLS-1$
			String type = msgE.getAttributeValue("type");//$NON-NLS-1$
			String content = msgE.getAttributeValue("content");

			NetMessage msg = new NetMessage(msgId, source, target,
					object, type, content);
			receivedMsgList.add(msg);
		}

		if (LOG.isDebugEnabled()) {
			int idMsg = 0;
			for (NetMessage msg : receivedMsgList) {
				LOG.debug("Message " + idMsg + ": " + msg.toString());//$NON-NLS-1$ //$NON-NLS-2$ 
			}
		}

		return receivedMsgList;
	}

	private String buildInput(TConnection conn,List<NetMessage> inMsgList)
			throws CommandException {
		Document doc = new Document();
		Element tangaraE = new Element("tangara"); //$NON-NLS-1$
		tangaraE.setAttribute("version", version);//$NON-NLS-1$
		doc.setRootElement(tangaraE);
		

		for (NetMessage msg : inMsgList) {
			Element msgE = new Element("message");//$NON-NLS-1$
			msgE.setAttribute("messageID", msg.getMessageID());//$NON-NLS-1$
			msgE.setAttribute("source", msg.getSourceUser());//$NON-NLS-1$
			msgE.setAttribute("target", msg.getTargetUser());//$NON-NLS-1$
			msgE.setAttribute("object", msg.getTargetObjectName());//$NON-NLS-1$
			msgE.setAttribute("type", msg.getType());//$NON-NLS-1$
			
			msgE.setText(msg.getContent());
			tangaraE.addContent(msgE);
		}

		StringWriter writer = new StringWriter();
		
		try {
			outputter.output(doc, writer);
		} catch (IOException e) {
			String msg = "Fail to convert message to XML";//$NON-NLS-1$
			LOG.error(msg, e);
			CommandException ex = new CommandException(conn.getServerURL(),
					CommandException.COMMAND_CREATION_ERR, msg, e);
			throw ex;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("message to send: " + writer.toString());//$NON-NLS-1$
		}

		return writer.toString();
	}

	private String version; // version of tangara 
	
	private XMLOutputter outputter = new XMLOutputter();

	/** Class logger */
	private static Logger LOG = Logger.getLogger(MessageCommand.class);
}
