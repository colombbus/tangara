package org.colombbus.tangara.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.colombbus.tangara.core.Version;
import org.colombbus.tangara.io.MalformedScriptException;
import org.colombbus.tangara.io.ScriptHeader;
import org.colombbus.tangara.io.ScriptHeaderHelper;
import org.colombbus.tangara.io.ScriptHeaderImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScriptHeaderHelperTest {

	private static final Charset HEADER_CHARSET = Charset.forName("US-ASCII");

	private static final String HEADER_REGEX = "//<tangara version=\""
			+ Version.VERSION_REGEX + "\" encoding=\"([a-zA-Z0-9\\- _]+)\">//";
	private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final Version VERSION = new Version("1.2.3.beta");
	private static final String STANDARD_TEXT_HEADER = "//<tangara version=\"1.2.3.beta\" encoding=\"ISO-8859-1\">//";
	private static final String UNSUPPORTED_VERSION_TEXT_HEADER = "//<tangara version=\"1.2.3.beta\" encoding=\"ISO-8859-1\">//";
	private static final String SUPPORTED_VERSION_TEXT_HEADER = "//<tangara version=\"1.0\" encoding=\"ISO-8859-1\">//";
	private static final String BAD_ENCODING_TEXT_HEADER = "//<tangara version=\"1.0\" encoding=\"ISO-885-1\">//";
	private static final String MALFORMED_TEXT_HEADER = "//<tangara version encoding=\"ISO-8859-1\">//";

	private static final String SCRIPT_CONTENT = "a = new Personnage();\nb = new Voiture();\n\n";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	private static byte[] createHeader(String header) {
		ByteBuffer bbuf = HEADER_CHARSET.encode(header);
		bbuf.rewind();
		byte[] byteHeader = new byte[bbuf.limit()];
		bbuf.get(byteHeader);
		return byteHeader;
	}

	private static ByteBuffer createScriptBuffer(byte[] header) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			if (header != null)
				out.write(header);

			Writer writer = new OutputStreamWriter(out, ISO_8859_1);
			writer.write(SCRIPT_CONTENT);
			writer.flush();

			return ByteBuffer.wrap(out.toByteArray());
		} catch (IOException ioEx) {
			fail();
			throw new RuntimeException(ioEx);
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = IllegalArgumentException.class)
	public void testContainsHeaderNUllArg() {
		ScriptHeaderHelper.containsHeader(null);
	}

	@Test
	public void testContainsHeaderNoHeader() throws IOException {
		ByteBuffer scriptContent = createScriptBuffer(null);
		assertFalse(ScriptHeaderHelper.containsHeader(scriptContent));
	}

	@Test
	public void testContainsHeaderISOHeader() throws IOException {
		byte[] header = createHeader(STANDARD_TEXT_HEADER);
		ByteBuffer scriptContent = createScriptBuffer(header);
		assertTrue(ScriptHeaderHelper.containsHeader(scriptContent));
	}

	@Test
	public void testContainsHeaderMalformedHeader() throws IOException {
		byte[] header = createHeader(MALFORMED_TEXT_HEADER);
		ByteBuffer scriptContent = createScriptBuffer(header);
		assertTrue(ScriptHeaderHelper.containsHeader(scriptContent));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExtractHeaderNullArg() {
		ScriptHeaderHelper.extractHeader(null);
	}
	
	@Test(expected = MalformedScriptException.class)
	public void testExtractHeaderEmptyStream() {
		ByteBuffer content= ByteBuffer.allocate(0);
		ScriptHeaderHelper.extractHeader(content);
	}
	
	@Test
	public void testExtractHeaderEmptyContent() {
		byte[] header = createHeader(SUPPORTED_VERSION_TEXT_HEADER);
		ByteBuffer content = createScriptBuffer(header);
		ScriptHeaderHelper.extractHeader(content);
	}



	@Test(expected = MalformedScriptException.class)
	public void testExtractHeaderNoHeader() {
		ByteBuffer content = createScriptBuffer(null);
		ScriptHeaderHelper.extractHeader(content);
	}

	@Test(expected=MalformedScriptException.class)
	public void testExtractHeaderMalformedHeader() {
		byte[] malformedHeader = createHeader(MALFORMED_TEXT_HEADER);
		ByteBuffer content = createScriptBuffer(malformedHeader);
		ScriptHeaderHelper.extractHeader(content);
	}

	@Test(expected=MalformedScriptException.class)
	public void testExtractHeaderUnsupportedVersion() {
		byte[] header = createHeader(UNSUPPORTED_VERSION_TEXT_HEADER);
		ByteBuffer content = createScriptBuffer(header);
		ScriptHeaderHelper.extractHeader(content);
	}

	@Test(expected=MalformedScriptException.class)
	public void testExtractHeaderBadEncoding() {
		byte[] header = createHeader(BAD_ENCODING_TEXT_HEADER);
		ByteBuffer content = createScriptBuffer(header);
		ScriptHeaderHelper.extractHeader(content);
	}

	@Test
	public void testExtractHeader() {
		byte[] header = createHeader(SUPPORTED_VERSION_TEXT_HEADER);
		ByteBuffer content = createScriptBuffer(header);
		ScriptHeaderHelper.extractHeader(content);
	}

	@Test
	public void testToByteArray() {
		byte[] byteHeader = createHeader(STANDARD_TEXT_HEADER);

		ScriptHeader header = new ScriptHeaderImpl(ISO_8859_1, VERSION);
		String s1 = new String(byteHeader);
		String s2 = new String(ScriptHeaderHelper.toByteArray(header));
		assertEquals(s1, s2);
		assertArrayEquals(byteHeader, ScriptHeaderHelper
				.toByteArray(header));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToByteArrayNullArg() {
		ScriptHeaderHelper.toByteArray(null);
	}

	@Test
	public void testHeaderMapping() {
		assertTrue(Pattern.matches(HEADER_REGEX,
				"//<tangara version=\"2.4\" encoding=\"" + UTF8.name()
						+ "\">//"));
		assertTrue(Pattern.matches(HEADER_REGEX,
				"//<tangara version=\"2.4\" encoding=\"" + ISO_8859_1.name()
						+ "\">//"));
		assertTrue(Pattern.matches(HEADER_REGEX, STANDARD_TEXT_HEADER));
		assertFalse(Pattern.matches(HEADER_REGEX, MALFORMED_TEXT_HEADER));
	}

}
