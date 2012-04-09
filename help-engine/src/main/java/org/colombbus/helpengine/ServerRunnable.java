package org.colombbus.helpengine;

import org.eclipse.jetty.server.Server;
import org.slf4j.*;

class ServerRunnable implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ServerRunnable.class);
	private Server server;
	private int port = 8345;

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			launchServer();
		} catch (Exception ex) {

		} finally {
		}
	}

	private void launchServer() throws Exception {
		server = new Server(port);
		server.setHandler(new ResourceFileHandler());
		server.setStopAtShutdown(true);
		server.start();
		server.join();
	}

	public void shutown() {
		try {
			if (server != null)
				doShutdown();
		} catch (Exception ex) {
			LOG.warn("Fail to shutdown",ex);//$NON-NLS-1$
		}
	}

	private void doShutdown() throws Exception {
		if( server.getStopAtShutdown())

			server.stop();
			server = null;
	}

}
