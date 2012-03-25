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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Reader of scripts
 */
public class ScriptReader {

	/** List of supported charset list */
	private static final List<Charset> COMMON_CHARSETS = new ArrayList<Charset>();

	private static final void addCommonCharsetIfSupported(String charsetName) {
		if (Charset.isSupported(charsetName)) {
			Charset cs = Charset.forName(charsetName);
			if (COMMON_CHARSETS.contains(cs) == false) {
				COMMON_CHARSETS.add(cs);
			}
		}
	}

	static {
		addCommonCharsetIfSupported("UTF-8"); //$NON-NLS-1$
		addCommonCharsetIfSupported("ISO-8859-1"); //$NON-NLS-1$
		addCommonCharsetIfSupported("x-MacRoman"); //$NON-NLS-1$
	}

	private static final char[] WITNESS_CHARS = { 'é', 'è', 'ë', 'ç', 'à', 'ù', 'ü', 'É', 'È', 'Ë', 'Ç', 'À', 'Ù', 'Ü' };

	private Charset lastScriptCharset = Charset.forName("UTF-8"); //$NON-NLS-1$

	/**
	 * Create a script reader
	 *
	 */
	public ScriptReader() {
	}

	public String readScript(InputStream in) throws IOException, MalformedScriptException {
		Validate.notNull(in, "in argument is null"); //$NON-NLS-1$

		ByteBuffer content = toByteBuffer(in);

		if (ScriptHeaderHelper.containsHeader(content)) {
			return extractScriptWithHeader(content);
		} else {
			return extractScriptWithoutHeader(content);
		}
	}

	public String readScript(File scriptFile) throws IOException, MalformedScriptException {
		InputStream in = null;
		try {
			in = new FileInputStream(scriptFile);
			String script = readScript(in);
			return script;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private static ByteBuffer toByteBuffer(InputStream in) throws IOException {
		byte[] contentByteArray = IOUtils.toByteArray(in);
		ByteBuffer content = ByteBuffer.wrap(contentByteArray);
		return content;
	}

	private String extractScriptWithoutHeader(ByteBuffer content) throws UnsupportedEncodingException {
		for (Charset cs : COMMON_CHARSETS) {
			String decodedScript = tryDecodeBufferWithCharset(content, cs);
			if (containsWitnessChar(decodedScript)) {
				lastScriptCharset = cs;
				return decodedScript;
			}
		}

		for (Charset cs : Charset.availableCharsets().values()) {
			String decodedScript = tryDecodeBufferWithCharset(content, cs);
			if (decodedScript != null) {
				lastScriptCharset = cs;
				return decodedScript;
			}
		}

		throw new UnsupportedEncodingException();
	}

	private static boolean containsWitnessChar(String script) {
		if (script != null)
			return StringUtils.containsAny(script, WITNESS_CHARS);
		return false;
	}

	/**
	 * Try to decode a byte buffer with a charset
	 *
	 * @param content
	 *            the bute buffer
	 * @param cs
	 *            the charset
	 * @return <code>null</code> if the charset is not supported, or the decoded
	 *         string
	 */
	private static String tryDecodeBufferWithCharset(ByteBuffer content, Charset cs) {
		CharBuffer buffer = CharBuffer.allocate(content.capacity() * 2);
		CharsetDecoder decoder = createDecoder(cs);
		content.rewind();
		CoderResult coderRes = decoder.decode(content, buffer, true);
		if (coderRes.isError() == false) {
			buffer.rewind();
			return buffer.toString().trim();
		}
		return null;
	}

	private static CharsetDecoder createDecoder(Charset cs) {
		CharsetDecoder decoder = cs.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		return decoder;
	}

	private String extractScriptWithHeader(ByteBuffer content) throws UnsupportedEncodingException {
		content.rewind();

		ScriptHeader header = ScriptHeaderHelper.extractHeader(content);
		lastScriptCharset = header.getCharset();

		String script = extractScriptWithCharset(content, header.getCharset());
		return script;
	}

	private static String extractScriptWithCharset(ByteBuffer content, Charset charset) {
		CharBuffer contentBuffer = charset.decode(content);
		return contentBuffer.toString().trim();
	}

	/**
	 * Get the charset of the last decoded script. If no script has been read,
	 * it returns UTF-8 charset
	 *
	 * @return a charset, never <code>null</code>
	 */
	public Charset lastScriptCharset() {
		return lastScriptCharset;
	}

}
