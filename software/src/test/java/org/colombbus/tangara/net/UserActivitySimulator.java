package org.colombbus.tangara.net;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class UserActivitySimulator extends Thread {

	private static final TConnectionFactory CONN_FACTORY = new TConnectionFactory();

	public UserActivitySimulator(String username, String serverURL,
			long exchangeFreq, long sendFreq, int duration, String message) throws MalformedURLException {
		this.setName(username);
		this.username = username;
		this.exchangeFreq = exchangeFreq;
		this.sendFreq = sendFreq;
		this.conn = CONN_FACTORY.getConnection(serverURL);
		this.duration = duration;
		this.message = message;
		this.msgHandler = new DefaultNetMessageHandler(username);
	}

	@Override
	public void run() {
		try {
			LOG.info(username + " initialisation");
			connectID = userMgtCmd.register(conn, username);
			LOG.info(username + " user registered");

			DefaultNetMessageHandler handler = new DefaultNetMessageHandler(username);
			DefaultNetMsgProcessor processor = new DefaultNetMsgProcessor();
			processor.addMessageHandler(handler);
			processor.setUnsupportedMessageHandler(handler);
			
			LOG.info(username + " pump created");
			TMessageExchangeManager mgr = new TMessageExchangeManager(conn,connectID,sendFreq,10, "0.10",processor, new ConsoleMsgExchErrHandler());
			//MessagePump pump = new MessagePump(conn, exchangeFreq, connectID, msgHandler, "0.9");
			//pump.start();
			LOG.info(username + " pump activated");

			nbSentMsg = 0;
			long maxTime = System.currentTimeMillis()+duration;
			LOG.info(username+" shall end at "+DATE_FORMATTER.format(new Date(maxTime)));
			try {
				while (System.currentTimeMillis() < maxTime ) {
					List<UserInfo> users = userMgtCmd.getRegisteredUsers(conn);
					if (users.size() > 1) {
						// remove myself
						UserInfo selUser = null;
						for (UserInfo user : users) {
							if (user.getUsername().equals("Simu0")) {
								selUser=user;
								break;
							}
						}
						// find a user
//						int userId = rand.nextInt(users.size());
//						System.out.println(userId+" / "+users.size());
//						if (userId < 0 || userId >= users.size()) {
//							System.err.println("bad userId value (userId="
//									+ userId + ", max=" + users.size() + ")");
//							System.exit(-1);
//						}
//						UserInfo user = users.get(userId);
						if( selUser != null ) {
							sendMessage(mgr, selUser);
						}
					} else {
						LOG
								.debug(username
										+ " Not enough users connected. Wait a little bit");
					}

					Thread.sleep(sendFreq);
				}

				Thread.sleep(1000*60*2 );// sleep during long time and check if we are still connected at the end 
				
				sendAliveMessage(mgr);

				Thread.sleep(exchangeFreq * 3);// let time to take all remaining messages
			} catch (InterruptedException interruptedEx) {
				LOG.info(username + " sleep interrupted");
				return;
			}

			mgr.shutdown();
			LOG.info(username + " Pump shutdown");
			userMgtCmd.unregister(conn, connectID);
			LOG.info(username + " user unregistered");
		} catch (Throwable th) {
			LOG.info(username + " error detected " + th.getMessage());
			lastError = th;
		}
	}

	private void sendMessage(TMessageExchangeManager mgr, UserInfo user) {
		// send him a message
		NetMessage msg = new NetMessage("", username, user
				.getUsername(), "", DefaultNetMessageHandler.TEST_MSG_TYPE,
				message);
		nbSentMsg++;
		LOG.info(username + " send message " + nbSentMsg + " to "
				+ user.getUsername());
		mgr.sendMessage(msg);
	}

	private void sendAliveMessage(TMessageExchangeManager mgr) {
		// send a still alive message at the end
		NetMessage msg = new NetMessage("", username, username, "",
				DefaultNetMessageHandler.TEST_MSG_TYPE,
				DefaultNetMessageHandler.ALIVE_MSG);
		mgr.sendMessage(msg);
		nbSentMsg++;
	}

	/**
	 * @return the lastError
	 */
	public Throwable getLastError() {
		return lastError;
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @return the nbSentMsg
	 */
	public int getNbSentMsg() {
		return nbSentMsg;
	}

	public int getReceivedMsg() {
		return msgHandler.getTotalMsgCount();
	}

	public int getProcessedMsg() {
		return msgHandler.getProcessedMsgCount();
	}

	public int getUnprocessedMsg() {
		return msgHandler.getUnprocessedMsgCount();
	}

	public boolean isConnectedAtTheEnd() {
		return msgHandler.getAliveMsgCount() > 0;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	private DefaultNetMessageHandler msgHandler;

	private UserRegistrationCommand userMgtCmd = new UserRegistrationCommand();

	private String username;

	private TConnection conn;

	private long exchangeFreq;

	private long sendFreq;

	private String connectID;

	private Throwable lastError;

	private int nbSentMsg;

	private int duration;

	private String message;
	
	//private static final Random rand = new Random(System.currentTimeMillis());

	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");
	
	private static final Logger LOG = Logger
			.getLogger(UserActivitySimulator.class);
}
