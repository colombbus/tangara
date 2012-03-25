package org.colombbus.tangara.net;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.*;

import org.apache.log4j.BasicConfigurator;
import org.colombbus.tangara.Configuration;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.junit.*;

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
@SuppressWarnings("nls")
public class MessageCommandTest {

	private static final String DEFAULT_TYPE = "text";

	private static final String DEFAULT_MSG = "je mets plein d'accents : çé partî. Et j'ajoute des \"\"\" partout !!!";

	private static final String USER_1 = "sampleà";

	private static final String USER_2 = "sample2é";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		conn = factory.getConnection(TestInit.BASE_URL);
		connectCmd = new UserRegistrationCommand();
		connectCmd.clearUserList(conn);
		connectID1 = connectCmd.register(conn, USER_1);
		connectID2 = connectCmd.register(conn, USER_2);
		cmd = new MessageCommand("0.9");
		Configuration.instance().load();
	}

	private TConnectionFactory factory = new TConnectionFactory();

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEmptyExchange() {
		try {
			cmd.deleteAll(conn);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}

		List<NetMessage> msgList = new ArrayList<NetMessage>();
		try {
			cmd.exchangeMessage(conn, connectID1, msgList);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void testMessageCommand() {
		String str = "<?xml version=\"1.0\"?> <tangara version=\"0.4\"><error code=\"019\">Fail to analyse sent messages</error></tangara>";
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			StringReader reader = new StringReader(str);
			doc = builder.build(reader);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(doc);
	}

	@Test
	public void testExchangeMessage() {
		try {
			cmd.deleteAll(conn);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}

		NetMessage msg = new NetMessage("001", USER_1, USER_2, "object1", DEFAULT_TYPE, DEFAULT_MSG);
		List<NetMessage> msgList = new ArrayList<NetMessage>();
		msgList.add(msg);
		List<NetMessage> outList = null;

		try {
			outList = cmd.exchangeMessage(conn, connectID1, msgList);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
		msgList.clear();

		assertNotNull(outList);
		assertEquals(0, outList.size());

		try {
			outList = cmd.exchangeMessage(conn, connectID2, msgList);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(outList);

		System.out.println("---->>>>");
		for (NetMessage nm : outList) {
			System.out.println("MSG: " + nm.toString());
		}
		assertEquals(1, outList.size());
		msg = outList.get(0);
		// assertEquals("001", msg.getMessageID());
		assertEquals(USER_1, msg.getSourceUser());
		assertEquals(USER_2, msg.getTargetUser());
		assertEquals("object1", msg.getTargetObjectName());
		assertEquals(DEFAULT_TYPE, msg.getType());
		assertEquals(DEFAULT_MSG, msg.getContent());
	}

	@Test
	public void testMassiveExchangeMessage() {
		try {
			cmd.deleteAll(conn);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
		int nbSend = 1000;
		int nbMsgPerSend = 50;
		List<NetMessage> user2AllMsgList = new ArrayList<NetMessage>();
		List<NetMessage> emptyMsgList = Collections.<NetMessage> emptyList();

		List<NetMessage> msgList = new ArrayList<NetMessage>();
		for (int i = 0; i < nbMsgPerSend; i++) {
			NetMessage msg = new NetMessage("001", USER_1, USER_2, "object", DEFAULT_TYPE, "This is a text");
			msgList.add(msg);
		}

		for (int send = 0; send < nbSend; send++) {
			List<NetMessage> outList = null;
			try {
				outList = cmd.exchangeMessage(conn, connectID1, msgList);
			} catch (CommandException e) {
				e.printStackTrace();
				fail();
			}
			assertNotNull(outList);
			if (outList.size() > 0) {
				for (NetMessage msg : outList) {
					msg.toString();
				}
				fail("connectID1 cannot get a message !");
			}

			try {
				outList = cmd.exchangeMessage(conn, connectID2, emptyMsgList);
				user2AllMsgList.addAll(outList);
			} catch (CommandException e) {
				e.printStackTrace();
				fail();
			}

		}

		// msgList.clear();
		// List<NetMessage> outList = null;
		// try {
		// outList = cmd.exchangeMessage(connectID2, msgList);
		// } catch (CommandException e) {
		// e.printStackTrace();
		// fail();
		// }
		assertNotNull(user2AllMsgList);
		assertEquals(nbMsgPerSend * nbSend, user2AllMsgList.size());
		for (NetMessage msg : user2AllMsgList) {
			assertEquals(USER_1, msg.getSourceUser());
			assertEquals(USER_2, msg.getTargetUser());
			assertEquals("object", msg.getTargetObjectName());
			assertEquals(DEFAULT_TYPE, msg.getType());
			assertEquals("This is a text", msg.getContent());
		}
	}

	// @Test
	public void testDeleteAll() {
		try {
			cmd.deleteAll(conn);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testConnectionUpdate() {

		List<NetMessage> msgList = new ArrayList<NetMessage>();
		List<NetMessage> outList = null;
		try {
			outList = cmd.exchangeMessage(conn, connectID1, msgList);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(outList);

		for (int i = 0; i < 8; i++) {
			try {
				Thread.sleep(10 * 1000);
				outList = cmd.exchangeMessage(conn, connectID1, msgList);
			} catch (CommandException e) {
				e.printStackTrace();
				fail();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			}
			assertNotNull(outList);
		}

	}

	private MessageCommand cmd;

	private String connectID1;

	private String connectID2;

	private UserRegistrationCommand connectCmd;

	private TConnection conn;
}
