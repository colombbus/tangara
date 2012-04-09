package org.colombbus.helpengine;

import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;

import org.junit.*;

public class HelpEngineImplValidTest {
	HelpEngine engine;
	Semaphore semaphore ;


	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStandalone() {
		semaphore = new Semaphore(0);
		startHelpServer();

		Runnable startup = new Runnable() {
			@SuppressWarnings("unused")
			@Override
			public void run() {
				new StandaloneFrame(engine,semaphore);
			}
		};

		SwingUtilities.invokeLater(startup);
		acquire();
	}

	private void startHelpServer() {
		engine = new DefaultHelpEngine();
		engine.setPort(7777);
		engine.startup();
	}

	private void acquire() {
		try {
			semaphore.acquire();
		} catch(InterruptedException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

}
