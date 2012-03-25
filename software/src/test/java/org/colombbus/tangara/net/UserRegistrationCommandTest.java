package org.colombbus.tangara.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserRegistrationCommandTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private static final TConnectionFactory CONN_FACTORY = new TConnectionFactory();



	@Before
	public void setUp() throws Exception {
		conn = CONN_FACTORY.getConnection(TestInit.BASE_URL);
		cmd = new UserRegistrationCommand();
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testRegister() {
		String ipAddress = getIPAddress();

		try {
			cmd.clearUserList(conn);
			cmd.register(conn, "ça");
			List<UserInfo> users = cmd.getRegisteredUsers(conn);
			assertEquals(1, users.size());
			assertTrue(users.contains(new UserInfo("ça", ipAddress)));
			cmd.register(conn, "tata");
			users = cmd.getRegisteredUsers(conn);
			assertEquals(2, users.size());
			assertTrue(users.contains(new UserInfo("ça", ipAddress)));
			assertTrue(users.contains(new UserInfo("tata", ipAddress)));
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
	}

	private String getIPAddress() {
		String ipAddress = null;
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail();
		}
		System.out.println("IP address: " + ipAddress);
		return ipAddress;
	}

	@Test
	public void testRegisterTwice() {
		try {
			cmd.clearUserList(conn);
			cmd.register(conn, "toto");
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}

		try {
			cmd.register(conn, "toto");
			fail();
		} catch (CommandException e) {
			// nothing to do
		}
	}

	@Test
	public void testUnregister() {
		String ipAddress = getIPAddress();
		try {
			cmd.clearUserList(conn);
			String connectID = cmd.register(conn, "toto");
			List<UserInfo> users = cmd.getRegisteredUsers(conn);
			assertEquals(1, users.size());
			assertTrue(users.contains(new UserInfo("toto", ipAddress)));
			cmd.unregister(conn, connectID);
			users = cmd.getRegisteredUsers(conn);
			assertEquals(0, users.size());
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testClear() {
		List<UserInfo> userList = null;
		try {
			cmd.clearUserList(conn);
			cmd.register(conn, "titi");
			cmd.register(conn, "toto");
			cmd.register(conn, "tata");

			userList = cmd.getRegisteredUsers(conn);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(userList);
		assertEquals(3, userList.size());
	}

	@Test
	public void testEmptyListClearing() {
		List<UserInfo> userList = null;
		try {
			cmd.clearUserList(conn);
			userList = cmd.getRegisteredUsers(conn);
		} catch (CommandException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(userList);
		assertEquals(0, userList.size());
	}

	@Test
	public void testUID() {
		UID uid = new UID();
		String str = null;
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream outStream = new DataOutputStream(byteStream);
			uid.write(outStream);
			outStream.close();
			byteStream.close();
			byte[] bytes = byteStream.toByteArray();
			str = new String(Hex.encodeHex(bytes));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		System.out.println("-----\n" + str + "\n-----");
		UID uid2 = null;
		try {
			byte[] bytes = Hex.decodeHex(str.toCharArray());
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			DataInputStream inStream = new DataInputStream(byteStream);
			uid2 = UID.read(inStream);
			inStream.close();
			byteStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals(uid, uid2);
	}

	@Test
	public void testSerializable() {
		MyClass i1 = new MyClass(3, "Hello world");
		String str = null;
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			// DataOutputStream outStream = new DataOutputStream(byteStream);
			ObjectOutputStream outStream = new ObjectOutputStream(byteStream);
			outStream.writeObject(i1);
			// uid.write(outStream);
			outStream.close();
			byteStream.close();
			byte[] bytes = byteStream.toByteArray();
			str = new String(Hex.encodeHex(bytes));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(str.indexOf(' ') < 0);

		System.out.println("[[[" + str + "]]]\n");
		MyClass i2 = null;
		try {
			byte[] bytes = Hex.decodeHex(str.toCharArray());
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			// DataInputStream inStream = new DataInputStream(byteStream);
			ObjectInputStream inStream = new ObjectInputStream(byteStream);
			// uid2 = UID.read(inStream);
			i2 = (MyClass) inStream.readObject();
			inStream.close();
			byteStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals(i1, i2);
	}

	private UserRegistrationCommand cmd;

	private TConnection conn;
}
