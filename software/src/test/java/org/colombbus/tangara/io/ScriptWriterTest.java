package org.colombbus.tangara.io;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.colombbus.tangara.core.Version;
import org.colombbus.tangara.io.ScriptHeader;
import org.colombbus.tangara.io.ScriptHeaderImpl;
import org.colombbus.tangara.io.ScriptWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ScriptWriterTest {

	private static final String SCRIPT = "a = new Personnage();\na.écouterLeVent()";
	private static final String OUT_SCRIPT = "//<tangara version=\"1\" encoding=\"UTF-8\">//"+SCRIPT;
	private ScriptHeader FILE_HEADER = new ScriptHeaderImpl(Charset
			.forName("UTF-8"), new Version("1"));

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

	@SuppressWarnings("unused")
	@Test
	public void testScriptWriter() {
		new ScriptWriter(FILE_HEADER);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testScriptWriterNullScriptFileHeader() {
		new ScriptWriter(null);
	}

	@Test
	public void testWriteScript() throws IOException {
		ScriptWriter writer = new ScriptWriter(FILE_HEADER);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeScript(SCRIPT, out);

		String content = decodeByteArray(out.toByteArray(), FILE_HEADER.getCharset());
		assertEquals( OUT_SCRIPT, content);
	}

	private String decodeByteArray(byte[] byteBuffer, Charset cs) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(byteBuffer);
			Reader reader = new InputStreamReader(in, cs);
			char[] cbuf = new char[5000];
			reader.read(cbuf);
			reader.close();
			return new String( cbuf).trim();
		} catch (Throwable th) {
			th.printStackTrace();
			return null;
		}
	}



	@Test(expected = IllegalArgumentException.class)
	public void testWriteScriptNullScript() throws IOException {
		ScriptWriter writer = new ScriptWriter(FILE_HEADER);
		writer.writeScript(null, new ByteArrayOutputStream());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWriteScriptNullStream() throws IOException {
		ScriptWriter writer = new ScriptWriter(FILE_HEADER);
		writer.writeScript(SCRIPT, null);
	}
}
