package org.colombbus.tangara.net;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserInfoTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

	@Test
	public void testEqualsObject() {
		UserInfo user1 = new UserInfo("User1", "123.123.456.456");
		UserInfo user2 = new UserInfo("User2", "123.123.456.456");
		UserInfo user2b = new UserInfo("User2", "123.123.456.456");
		assertEquals(user2, user2b);
		assertFalse(user1.equals(user2));
		assertFalse(user1.equals(user2b));

		List<UserInfo> users = new ArrayList<UserInfo>();
		users.add(user1);
		users.add(user2);
		assertTrue(users.contains(user2));
	}

	@Test
	public void testFilExt() {
		assertTrue(FilenameUtils.isExtension("toto.tgr", "tgr"));
		assertFalse(FilenameUtils.isExtension("toto.tgr", ".tgr"));
	}
}
