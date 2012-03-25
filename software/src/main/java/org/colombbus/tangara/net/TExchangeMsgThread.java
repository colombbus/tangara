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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * A thread that exchange (i.e. send and receive) messages with the server.
 * <p>
 * 
 * </p>
 * @author gwen
 */
public class TExchangeMsgThread extends Thread {

	/**
	 * 
	 * @param toSendMsgList
	 *            the messages to send to the server
	 * @param toProcessMsgList
	 *            the messages received from the server and to process
	 * @param exchangeFreqMs
	 *            The exchange frequency (in milliseconds) with the server
	 * @param conn
	 *            the connection to use
	 * @param exchangeMsgCmd
	 *            the command for exchanging messages with the server
	 * @param connectID
	 *            the identifier of the current connection
	 * @param maxMsgPerSend
	 *            maximum number of messages send at the same time
	 * @param errHandler
	 *            the handler of errors
	 */
	public TExchangeMsgThread(Queue<NetMessage> toSendMsgList,
			Queue<NetMessage> toProcessMsgList, long exchangeFreqMs,
			TConnection conn, MessageCommand exchangeMsgCmd, String connectID,
			int maxMsgPerSend, MessageExchangeErrHandler errHandler) {
		super("TangaraExchangeMsg");
		this.conn = conn;
		this.connectID = connectID;
		this.toSendMsgList = toSendMsgList;
		this.toProcessMsgList = toProcessMsgList;
		this.exchangeFreqMs = exchangeFreqMs;
		this.exchangeMsgCmd = exchangeMsgCmd;
		this.maxMsgPerSend = maxMsgPerSend;
		this.errHandler = errHandler;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		List<NetMessage> toSendNowMsgList = new ArrayList<NetMessage>(maxMsgPerSend);
		
		while (shutdown == false) {
			// fill the list of the messages to send for the next exchange
			fillMsgToSendNow(toSendNowMsgList);
			
			try {
				// send the messages to the server and get the response
				List<NetMessage> receivedMsgList = exchangeMsgCmd.exchangeMessage(conn, connectID, toSendNowMsgList);
				// at this step, the messages are stored by the server. 
				// They shall be deleted from the client to avoid to send them twice.
				toSendNowMsgList.clear();
				
				// if some messages have been received, handle them
				if( receivedMsgList.size()>0) {
					toProcessMsgList.addAll(receivedMsgList);
				}
			} catch (CommandException cmdEx) {
				String msg = String.format( 
						"An error occurs during a message exchange with the server (connectID=%s)",//$NON-NLS-1$ 
						connectID);
				LOG.error(msg, cmdEx);
				shutdown = errHandler.handleExchangeError(cmdEx);
			} catch(Throwable th ) {
				String msg = String.format( 
						"An unknown error occurs during a message exchange with the server (connectID=%s)",//$NON-NLS-1$ 
						connectID);
				LOG.error(msg, th); 
				shutdown = errHandler.handleExchangeError(th);
			}

			// sleep a little bit
			if( shutdown == false) {
				try {
					Thread.sleep(exchangeFreqMs);
				} catch (InterruptedException e) {
					String msg = String.format( 
							"Message exchange with the server interrupted (connectID=%s)",//$NON-NLS-1$ 
							connectID);
					LOG.warn(msg, e);
					shutdown=true;
					return;
				}
			}
		}
	}

	/**
	 * Fill the list of the messages to send for the next exchange
	 * 
	 * @param toSendNowMsgList
	 *            the lise of the messages that will be sent
	 */
	private void fillMsgToSendNow(List<NetMessage> toSendNowMsgList) {
		while (toSendMsgList.isEmpty() == false 
				&& toSendNowMsgList.size() < maxMsgPerSend) {
			NetMessage msg = toSendMsgList.poll();
			if( msg != null ) {
				toSendNowMsgList.add(msg);
			}
		}
	}

	/**
	 * Stop all exchanges with the server and end the thread execution.
	 * <p>
	 * After this method call, the instance of {@link TExchangeMsgThread} is no
	 * more usable
	 * </p>
	 */
	public void shutdown() {
		shutdown = true;
	}

	/** The handler of errors */
	private MessageExchangeErrHandler errHandler;
	
	/** The connection identifier for exchanging messages with the server */
	private String connectID ;

	/**
	 * the activation status of the main loop in {@link #run()} method. A value
	 * of <code>true</code> stops the main loop.
	 */
	private boolean shutdown = false;
	
	/** The maximum number of messages to send per exchange with the server */
	private int maxMsgPerSend;
	
	/** The connection with the Tangara server */
	private TConnection conn;

	/** The command to exchange messages with the server */
	private MessageCommand exchangeMsgCmd;

	/** The list of the messages to send to the server */
	private Queue<NetMessage> toSendMsgList;

	/** The list that contains the messages received from the server */
	private Queue<NetMessage> toProcessMsgList;

	/** The exchange frequency (in milliseconds) with the server */
	private long exchangeFreqMs;
	
	/** Class logger */
	private static Logger LOG = Logger.getLogger(TExchangeMsgThread.class);
}
