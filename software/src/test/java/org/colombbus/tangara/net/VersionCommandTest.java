package org.colombbus.tangara.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class VersionCommandTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private static final TConnectionFactory CONN_FACTORY = new TConnectionFactory();

	@Test
	public void testVersionCommand() {
		TConnection conn=null;
		try {
			conn = CONN_FACTORY.getConnection(TestInit.BASE_URL);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			fail();
		}
		VersionCommand cmd = new VersionCommand();
		try {
			String version = cmd.getVersion(conn);
			assertNotNull(version);
			assertEquals("0.11", version);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}

	}

}
