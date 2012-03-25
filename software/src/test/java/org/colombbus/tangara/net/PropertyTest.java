package org.colombbus.tangara.net;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PropertyTest {

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
	public void multiLoad() {
		Properties prop = new Properties();
		FileInputStream in1 = null;
		FileInputStream in2 = null;
		try {
			in1 = new FileInputStream("src/test/data/PropertyTest1.properties");
			prop.load(in1);
			assertEquals("1", prop.get("key1"));
			assertEquals("11", prop.get("commonkey"));
			in2 = new FileInputStream("src/test/data/PropertyTest2.properties");
			prop.load(in2);
			assertEquals("22", prop.get("commonkey"));
			assertEquals("1", prop.get("key1"));
			assertEquals("2", prop.get("key2"));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			IOUtils.closeQuietly(in1);
			IOUtils.closeQuietly(in2);
		}

	}

}
