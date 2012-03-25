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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * The thread in charge of the processing of the messages exchanged with the
 * server.
 *
 * @author gwen
 */
public class TProcessMsgThread extends Thread {

	/**
	 *
	 *
	 * @param toProcessMsgList the list of messages to process (i.e. received from server)
	 * @param toSendMsgList the list of messages to send (i.e. to put to the server)
	 * @param msgProcessor the processor of messages
	 * @param connectID the connection identifier
	 * @param timeout the connection timeout in milliseconds
	 */
	public TProcessMsgThread(BlockingQueue<NetMessage> toProcessMsgList,
			Queue<NetMessage> toSendMsgList,
			NetMsgProcessor msgProcessor, String connectID,
			long timeout) {
		super("TangaraProcessMsg");
		this.toProcessMsgList = toProcessMsgList;
		this.toSendMsgList = toSendMsgList;
		this.msgProcessor = msgProcessor;
		this.pollTimeout = timeout;
		this.connectID = connectID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		LOG.info("Start of the thread of " + connectID);
		final List<NetMessage> outNetMsgList = new ArrayList<NetMessage>();
		try {
			while (shallShutdown == false) {
				final NetMessage msg = toProcessMsgList.poll(pollTimeout,
						TimeUnit.MILLISECONDS);

				if (msg != null) {
					LOG.info("start processing "+msg.getMessageID());
					try {
						msgProcessor.processMessage(connectID, msg, outNetMsgList);
					} catch( Throwable th ) {
						LOG.error("Unexpected exception during the processing of "+msg.getMessageID(), th);
					}
					if (outNetMsgList.isEmpty() == false) {
						toSendMsgList.addAll(outNetMsgList);
						outNetMsgList.clear();
					}
					LOG.info("end processing "+msg.getMessageID());
				}
			}
		} catch (InterruptedException e) {
			LOG.warn("Process message poller interrupted", e);
			shutdown();
		} catch( Throwable th) {
			LOG.error("Unexpected exception in the main processing loop", th);
		}
		LOG.info("End of the thread of " + connectID);
	}

	/**
	 * Stop message processing and end the thread execution.
	 * <p>
	 * After this method call, the instance of {@link TProcessMsgThread} is no
	 * more usable
	 * </p>
	 */
	public void shutdown() {
		shallShutdown = true;
	}

	/**
	 * the activation status of the main loop in {@link #run()} method. A value
	 * of <code>true</code> stops the main loop.
	 */
	private boolean shallShutdown = false;

	/** The list of the messages to process */
	private BlockingQueue<NetMessage> toProcessMsgList;

	/** The list of the message that shall be sent to the server */
	private Queue<NetMessage> toSendMsgList;

	/** Message processor */
	private NetMsgProcessor msgProcessor;

	/** Polling timeout */
	private long pollTimeout;

	/** The connection identifier used to exchange messages */
	private String connectID;

	/** Class logger */
	private static Logger LOG = Logger.getLogger(TProcessMsgThread.class);
}
