/**
 * 
 */
package org.colombbus.tangara.net;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author gwen
 * 
 */
public class DefaultNetMessageHandler implements NetMessageHandler {

	public static final String TEST_MSG_TYPE = "test-msg";

	public static final String ALIVE_MSG = "alive?";

	/**
	 * 
	 */
	public DefaultNetMessageHandler(String username) {
		this.username = username;
	}

	// public void processUnsupportedType(NetMessage netMsg,
	// String connectID, List<NetMessage> outMsgList) {
	// LOG.error(username + " processUnsupportedType: " + netMsg.getType()
	// + " from " + netMsg.getSourceUser() + " with ID="
	// + netMsg.getMessageID() + " to " + connectID);
	// unprocessedMsgCount++;
	// }

	@Override
	public String getHandledType() {
		return TEST_MSG_TYPE;
	}

	@Override
	public void processMessage(NetMessage netMsg, String connectID,
			List<NetMessage> outNetMsg) {
		if (netMsg.getType().equalsIgnoreCase(TEST_MSG_TYPE)) {
			if (netMsg.getContent().equals(ALIVE_MSG)) {
				aliveMsgCount++;
				LOG.info(username + " process alive message " + aliveMsgCount);
			} else {
				processedMsgCount++;
				LOG.info(username + " process message " + processedMsgCount);
			}
		} else {
			LOG.error(username + " processUnsupportedType: " + netMsg.getType()
					+ " from " + netMsg.getSourceUser() + " with ID="
					+ netMsg.getMessageID() + " to " + connectID);
			unprocessedMsgCount++;
		}
	}

	/**
	 * @return the processedMsgCount
	 */
	public int getProcessedMsgCount() {
		return processedMsgCount;
	}

	/**
	 * @return the unprocessedMsgCount
	 */
	public int getUnprocessedMsgCount() {
		return unprocessedMsgCount;
	}

	public int getTotalMsgCount() {
		return aliveMsgCount + processedMsgCount + unprocessedMsgCount;
	}

	/**
	 * @return the aliveMsgCount
	 */
	public int getAliveMsgCount() {
		return aliveMsgCount;
	}

	private int aliveMsgCount = 0;

	private int processedMsgCount = 0;

	private int unprocessedMsgCount = 0;

	private String username;

	private static final Logger LOG = Logger
			.getLogger(DefaultNetMessageHandler.class);
}
