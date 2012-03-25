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

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TMessageExchangeManager {

	/**
	 * @param conn
	 * @param connectID
	 * @param exchangeFreqMs
	 *            The exchange frequency (in milliseconds) with the server
	 * @param maxMsgPerSend
	 * @param serverVersion
	 *            the version of the Tangara server
	 * @param errHandler
	 */
	public TMessageExchangeManager(TConnection conn, String connectID,
			long exchangeFreqMs, int maxMsgPerSend, String serverVersion,
			NetMsgProcessor msgProcessor, MessageExchangeErrHandler errHandler) {
		super();
		this.errHandler = errHandler;
		exchangeMsgTh = new TExchangeMsgThread(toSendMsgList, toProcessMsgList,
				exchangeFreqMs, conn, new MessageCommand(serverVersion),
				connectID, maxMsgPerSend, this.errHandler);
		processMsgTh = new TProcessMsgThread(toProcessMsgList, toSendMsgList,
				msgProcessor, connectID, exchangeFreqMs);

		exchangeMsgTh.start();
		processMsgTh.start();
	}

	public void sendMessage( NetMessage msg) {
		toSendMsgList.add(msg);
	}

	public void shutdown() {
		exchangeMsgTh.shutdown();
		processMsgTh.shutdown();
	}


	/** The hander of errors */
	private MessageExchangeErrHandler errHandler;

	/** The messages to send to the server */
	private Queue<NetMessage> toSendMsgList = new ConcurrentLinkedQueue<NetMessage>();

	/** The messages to process */
	private BlockingQueue<NetMessage> toProcessMsgList = new LinkedBlockingQueue<NetMessage>();

	/** The thread that exchanges messages with the tangara server */
	private TExchangeMsgThread exchangeMsgTh;

	/** The thread that processes the messages received from the server */
	private TProcessMsgThread processMsgTh;

}
