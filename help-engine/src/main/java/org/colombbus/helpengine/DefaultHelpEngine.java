package org.colombbus.helpengine;

import org.slf4j.*;

/**
 * Default implementation of an {@link HelpEngine help engine}.
 *
 * <pre>
 * This implementation use command line browser launcher.
 * </pre>
 */
public class DefaultHelpEngine implements HelpEngine {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultHelpEngine.class);

	private ServerRunnable serverRun = new ServerRunnable();
	private Thread thread;
	private int port = 8080;

	@Override
	public void setPort(int port) {
		verifyNotRunning();
		this.port = port;
		serverRun.setPort(port);
	}

	private void verifyNotRunning() {
		if (thread != null)
			throw new IllegalStateException("Server already running");//$NON-NLS-1$
	}

	@Override
	public void startup() {
		verifyNotRunning();
		thread = new Thread(serverRun);
		thread.start();
	}

	@Override
	public void shutdown() {
		if (thread == null)
			return;

		serverRun.shutown();
		thread = null;
	}

	@Override
	public void openHelp() {
		if (thread == null)
			throw new IllegalStateException("HelpEngine not startup");//$NON-NLS-1$

		String defaultAddress = String.format("http://localhost:%d/logiciel_commandes.html", port); //$NON-NLS-1$
		LOG.trace("Open help url {}", defaultAddress); //$NON-NLS-1$
		try {
			BrowserLauncher.browse(defaultAddress);
		} catch (Exception ex) {
			LOG.warn("Fail to open browser at page " + defaultAddress, ex); //$NON-NLS-1$
		}
	}
}