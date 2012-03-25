package org.colombbus.tangara.net;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConversionTest {

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
	public void test64Conversion() {
		File path = new File("src/main/resources/org/colombbus/tangara/splash.png");
		assertTrue(path.exists());
		assertTrue(path.isFile());
		assertTrue(path.canRead());
		byte[] binaryContent = new byte[(int) path.length()];
		FileInputStream in = null;
		try {
			in = new FileInputStream(path);
			in.read(binaryContent);
			in.close();
		} catch( Exception ex) {
			fail();
		}
		byte[] base64content = Base64.encodeBase64(binaryContent);

		String fileContent = new String( base64content);

		assertEquals(fileContent.length(), base64content.length);

		// at this stage, the Base64 conversion is done and the format in String

		byte[] outBase64Content = fileContent.getBytes();
		assertEquals(outBase64Content.length, fileContent.length());
		byte[] outputResult = Base64.decodeBase64(outBase64Content);
		assertEquals( outputResult.length, binaryContent.length);

		assertTrue(Arrays.equals(binaryContent, outputResult));
	}

}
