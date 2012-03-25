package org.colombbus.tangara.net;

import java.util.List;

import org.apache.log4j.Logger;

public class SimuAdmin extends Thread {

	public SimuAdmin(int nbUsers, TConnection conn) {
		super("SimuAdmin");
		this.nbUsers = nbUsers;
		this.conn = conn;
	}

	private TConnection conn;
	
	
	@Override
	public void run() {
		UserRegistrationCommand cmd = new UserRegistrationCommand();

		try {
			while (continueAdmin) {
				try {
					List<UserInfo> users = cmd.getRegisteredUsers(conn);
					if (users.size() < nbUsers) {
						LOG.fatal("Missing users "+users.size()+"/"+nbUsers);
					}
				} catch (CommandException e) {
					LOG.error("fail to get registered users",e);
				}
				sleep(1000 * 30);
			}
		} catch (InterruptedException ex) {
			LOG.fatal("Interrupted", ex);
			System.exit(-1);
		}

	}

	public void stopAdmin() {
		continueAdmin = false;
	}

	private boolean continueAdmin = true;

	private int nbUsers;

	private static final Logger LOG = Logger.getLogger(SimuAdmin.class);
}
