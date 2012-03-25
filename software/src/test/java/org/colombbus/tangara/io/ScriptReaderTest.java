/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2009 Colombbus (http://www.colombbus.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara.io;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;

import org.colombbus.tangara.core.Version;
import org.junit.*;

@SuppressWarnings("nls")
public class ScriptReaderTest {

	private static final Charset UTF_8 = Charset.forName("UTF-8");
	private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

	private static final ScriptHeader HEADER_UTF8 = new ScriptHeaderImpl(UTF_8, new Version("1"));
	private static final ScriptHeader HEADER_ISO_8859_1 = new ScriptHeaderImpl(ISO_8859_1, new Version("1"));
	private static final String SCRIPT_WITH_ACCENT = "a = new Personnage();\na.éternuer();";
	private static final String SCRIPT_NO_ACCENT = "a = new Personnage();";

	private ScriptReader reader;

	@Before
	public void setUp() throws Exception {
		reader = new ScriptReader();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadScriptUTF8() throws IOException {
		InputStream in = createEncodedInputStreamWithHeader(HEADER_UTF8, SCRIPT_WITH_ACCENT, UTF_8);
		String script = reader.readScript(in);
		assertEquals(SCRIPT_WITH_ACCENT, script);
	}

	@Test
	public void testReadScriptISO_8859_1() throws IOException {
		InputStream in = createEncodedInputStreamWithHeader(HEADER_ISO_8859_1, SCRIPT_WITH_ACCENT, ISO_8859_1);
		String script = reader.readScript(in);
		assertEquals(SCRIPT_WITH_ACCENT, script);
	}

	private static InputStream createEncodedInputStreamWithHeader(ScriptHeader header, String content, Charset contentCharset)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] headerBuffer = ScriptHeaderHelper.toByteArray(header);
		out.write(headerBuffer);

		byte[] codeBuffer = contentCharset.encode(content).array();
		out.write(codeBuffer);

		out.flush();

		return new ByteArrayInputStream(out.toByteArray());
	}

	@Test
	public void testReadScriptNoHeaderNoAccentUTF_8() throws IOException {
		InputStream in = createEncodedInputStream(SCRIPT_NO_ACCENT, UTF_8);
		String script = reader.readScript(in);
		assertEquals(SCRIPT_NO_ACCENT, script);
	}

	private static InputStream createEncodedInputStream(String content, Charset charset) {
		ByteBuffer bbuf = charset.encode(content);
		byte[] buffer = new byte[bbuf.limit()];
		bbuf.get(buffer);
		return new ByteArrayInputStream(buffer);
	}

	@Test
	public void testReadScriptNoHeaderNoAccentISO_8859_1() throws IOException {
		InputStream in = createEncodedInputStream(SCRIPT_NO_ACCENT, ISO_8859_1);
		String script = reader.readScript(in);
		assertEquals(SCRIPT_NO_ACCENT, script);
	}

	@Test
	public void testReadScriptNoHeaderWithAccentUTF_8() throws IOException {
		InputStream in = createEncodedInputStream(SCRIPT_WITH_ACCENT, UTF_8);
		String script = reader.readScript(in);
		assertEquals(SCRIPT_WITH_ACCENT, script);
	}

	@Test
	public void testReadScriptNoHeaderWithAccentISO_8859_1() throws IOException {
		InputStream in = createEncodedInputStream(SCRIPT_WITH_ACCENT, ISO_8859_1);
		String script = reader.readScript(in);
		assertEquals(SCRIPT_WITH_ACCENT, script);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadScriptNullStream() throws IOException {
		InputStream in = null;
		reader.readScript(in);
	}

	@Test
	public void testLastScriptCharsetInit() {
		assertEquals(UTF_8, reader.lastScriptCharset());
	}

	@Test
	public void testLastScriptCharsetWithHeaderUTF_8() throws IOException {

		InputStream in = createEncodedInputStreamWithHeader(HEADER_UTF8, SCRIPT_WITH_ACCENT, UTF_8);
		reader.readScript(in);
		assertEquals(UTF_8, reader.lastScriptCharset());
	}

	@Test
	public void testLastScriptCharsetWithHeaderISO_8859_1() throws IOException {

		InputStream in = createEncodedInputStreamWithHeader(HEADER_ISO_8859_1, SCRIPT_WITH_ACCENT, ISO_8859_1);
		reader.readScript(in);
		assertEquals(ISO_8859_1, reader.lastScriptCharset());
	}

	@Test
	public void testLastScriptCharsetNoHeaderUTF_8() throws IOException {
		InputStream in = createEncodedInputStream(SCRIPT_WITH_ACCENT, UTF_8);
		reader.readScript(in);
		assertEquals(UTF_8, reader.lastScriptCharset());
	}

	@Test
	public void testLastScriptCharsetNoHeaderISO_8859_1() throws IOException {
		InputStream in = createEncodedInputStream(SCRIPT_WITH_ACCENT, ISO_8859_1);
		reader.readScript(in);
		assertEquals(ISO_8859_1, reader.lastScriptCharset());
	}

	@Test
	public void testConversion() {
		String s = "c'est un bon exemple éèàùç";
		ByteBuffer sbuf = UTF_8.encode(s);
		ByteBuffer bbuf = ByteBuffer.allocate(s.length() * 2);
		bbuf.put(sbuf);

		bbuf.rewind();
		CharBuffer cbuf = UTF_8.decode(bbuf);
		cbuf.rewind();
		String res = cbuf.toString();
		assertFalse(s.length() == res.length());
		assertEquals(s, res.trim());
		assertTrue(s.length() == res.trim().length());
	}

	@Test
	public void testConversionUTFtoISO() throws IOException {
		ByteBuffer in = UTF_8.encode(SCRIPT_WITH_ACCENT);

		CharsetDecoder decoderUTF8 = createDecoder(UTF_8);
		in.rewind();
		CharBuffer out = CharBuffer.allocate(500);
		CoderResult codeRes = decoderUTF8.decode(in, out, true);
		assertFalse(codeRes.isError());
		out.rewind();
		assertEquals(SCRIPT_WITH_ACCENT, out.toString().trim());
	}

	private static CharsetDecoder createDecoder(Charset cs) {
		CharsetDecoder decoder = cs.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		return decoder;
	}

	@Test
	public void testConversionISOtoUTF() throws IOException {
		ByteBuffer in = ISO_8859_1.encode(SCRIPT_WITH_ACCENT);

		CharsetDecoder decoderISO_8859_1 = createDecoder(ISO_8859_1);
		in.rewind();
		CharBuffer out = CharBuffer.allocate(500);
		CoderResult codeRes = decoderISO_8859_1.decode(in, out, true);
		assertFalse(codeRes.isError());
		out.rewind();
		assertEquals(SCRIPT_WITH_ACCENT, out.toString().trim());

		CharsetDecoder decoderUTF8 = createDecoder(UTF_8);
		in.rewind();
		out = CharBuffer.allocate(500);
		codeRes = decoderUTF8.decode(in, out, true);
		assertTrue(codeRes.isError());
		assertTrue(codeRes.isMalformed());
	}
}