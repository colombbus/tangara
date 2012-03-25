/**
 *
 */
package org.colombbus.tangara.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Dear reader, this test class is a mess and has not been maintained.
 *
 * <pre>
 * So you have two choices:
 * 	- leave this file code (without any consequences)
 * 	- read and fix this file code (there are consequencies)
 * </pre>
 */
@Ignore
public class MessagePumpTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		WildcardFileFilter filter = new WildcardFileFilter("*.log*");
		File logDir = new File("test/logs/");
		File[] files = logDir.listFiles((FilenameFilter)filter);
		for( File f : files ) {
			System.out.println("delete file "+f.getName());
			assertTrue(f.delete());
		}

		File cfgFile = new File("src/test/data/MessagePumpTest-log4j.properties");
		assertTrue(cfgFile.exists());
		PropertyConfigurator.configure(cfgFile.getAbsolutePath());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	private static final int NB_SIMULATORS = 15;

	private static final int MSG_SIZE = 1024*100; //100Ko per message

	private static final int POST_FREQ = 3000; // every 3 secondes

	private static final int SIMU_DURATION = 1000*60*15; // 15 minutes

	private static final int SERVER_EXCHANGE_FREQ = 2000; // every 2 seconds

	private static final TConnectionFactory CONN_FACTORY = new TConnectionFactory();


	/**
	 * Test method for  org.colombbus.tangara.net.MessagePump#run()
	 */
	@Test
	public void testRunAll() {
		// clear all previously users
		TConnection conn=null;
		try {
			conn = CONN_FACTORY.getConnection(TestInit.BASE_URL);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			fail();
		}
		UserRegistrationCommand cmd = new UserRegistrationCommand();
		try {
			cmd.clearUserList(conn);
		} catch (CommandException cmdEx) {
			cmdEx.printStackTrace();
			fail();
		}


		// make a message with the good size
		char[] buffer = new char[MSG_SIZE];
		Arrays.fill(buffer, 'o');
		String message = new String(buffer);

		List<UserActivitySimulator> simulators = new ArrayList<UserActivitySimulator>();
		// initialize the simulators
		try {
			for (int i = 0; i < NB_SIMULATORS; i++) {
				UserActivitySimulator sim = new UserActivitySimulator("Simu"
						+ i, TestInit.BASE_URL, SERVER_EXCHANGE_FREQ, POST_FREQ, SIMU_DURATION, message);
				simulators.add(sim);
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			fail();
		}

		SimuAdmin admin = new SimuAdmin(NB_SIMULATORS,conn);

		// launch the simulators
		for (UserActivitySimulator simul : simulators) {
			simul.start();
		}
		admin.start();

		// wait the end of the simulators execution
		for (UserActivitySimulator simul : simulators) {
			try {
				simul.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			}
		}

		admin.stopAdmin();

		// process the simulators results
		int nbMsgSentTotal = 0;
		int nbMsgReceivedTotal = 0;
		for (UserActivitySimulator simul : simulators) {
			nbMsgSentTotal += simul.getNbSentMsg();
			nbMsgReceivedTotal += simul.getReceivedMsg();
			if( simul.isConnectedAtTheEnd()==false) {
				LOG.fatal(simul.getName() +" died before the end");
			}
			if( simul.getUnprocessedMsg() != 0 ){
				LOG.warn(simul.getUnprocessedMsg() +" unprocessed messages from "+simul.getName());
			}
		}

		LOG.info("Nb of message sent :"+nbMsgSentTotal);
		LOG.info("Nb of message received :"+nbMsgReceivedTotal);
		assertEquals(nbMsgSentTotal, nbMsgReceivedTotal);
	}

	private static final Logger LOG = Logger.getLogger(MessagePumpTest.class);
}
