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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.colombbus.tangara.core.Version;

public class ScriptHeaderHelper {

	private static final String VERSION_FIELD = "version"; //$NON-NLS-1$
	private static final String ENCODING_FIELD = "encoding"; //$NON-NLS-1$
	private static final Charset HEADER_CHARSET = Charset.forName("US-ASCII"); //$NON-NLS-1$
	private static final String HEADER_REGEX = "//<tangara version=\"" + Version.VERSION_REGEX + "\" encoding=\"([a-zA-Z0-9\\- _]+)\">//"; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String BYTE_HEADER_FORMAT = "//<tangara version=\"%s\" encoding=\"%s\">//"; //$NON-NLS-1$

	// TODO is it obsolete ?
	private static final byte[] HEADER_PREFIX = HEADER_CHARSET.encode("//<tangara ").array(); //$NON-NLS-1$

	private static final Version[] SUPPORTED_VERSIONS = { new Version("1.0") }; //$NON-NLS-1$

	// singleton
	private ScriptHeaderHelper() {
	}

	public static boolean containsHeader(ByteBuffer content) {
		Validate.notNull(content, "content argument is null"); //$NON-NLS-1$
		if (content.capacity() < HEADER_PREFIX.length)
			return false;
		for (int i = 0; i < HEADER_PREFIX.length; i++) {
			if (HEADER_PREFIX[i] != content.get(i))
				return false;
		}
		return true;
	}

	public static ScriptHeader extractHeader(ByteBuffer content) {
		Validate.notNull(content, "content argument is null"); //$NON-NLS-1$

		ByteBuffer byteHeader = extractHeaderAsByteBuffer(content);

		String header = HEADER_CHARSET.decode(byteHeader).toString();
		checkHeaderWellformed(header);

		String versionValue = extractFieldValue(VERSION_FIELD, header);
		Version version = new Version(versionValue);
		checkSupportedVersion(version);

		String encoding = extractFieldValue(ENCODING_FIELD, header);
		Charset charset = decodeEncodingCharset(encoding);

		ScriptHeader scriptFileHeader = new ScriptHeaderImpl(charset, version);

		return scriptFileHeader;
	}

	private static Charset decodeEncodingCharset(String encoding) {
		try {
			return Charset.forName(encoding);
		} catch (IllegalCharsetNameException nameEx) {
			String msg = String.format("Illegal charset name '%s'" , encoding ); //$NON-NLS-1$
			throw new MalformedScriptException(msg, nameEx);
		} catch (UnsupportedCharsetException supportEx) {
			String msg = String.format("Unsupported charset name '%s'" , encoding ); //$NON-NLS-1$
			throw new MalformedScriptException(msg, supportEx);
		}
	}

	private static void checkSupportedVersion(Version version) {
		for (Version supportedVersion : SUPPORTED_VERSIONS) {
			if (supportedVersion.equals(version)) {
				return;
			}
		}

		String msg = String.format("Version %s is not supported",version); //$NON-NLS-1$
		throw new MalformedScriptException(msg);
	}

	private static String extractFieldValue(String fieldName, String header) {
		int fieldStart = header.indexOf(fieldName);
		int valueBegin = fieldStart + fieldName.length() + 2;// +2 => '="'
		int valueEnd = header.indexOf('"', valueBegin);
		String value = header.substring(valueBegin, valueEnd);
		return value;
	}

	private static ByteBuffer extractHeaderAsByteBuffer(ByteBuffer content) {
		moveToHeaderEnd(content);
		int headerLength = content.position();
		content.rewind();
		ByteBuffer byteHeader = ByteBuffer.allocate(headerLength);
		content.get(byteHeader.array());
		return byteHeader;
	}

	private static void checkHeaderWellformed(String header) {
		if (Pattern.matches(HEADER_REGEX, header) == false) {
			throw new MalformedScriptException("The header is invalid"); //$NON-NLS-1$
		}
	}

	private static void moveToHeaderEnd(ByteBuffer content) throws MalformedScriptException {
		try {
			while (content.get() != '>')
				;
			if (content.get() != '/' || content.get() != '/') {
				throw new MalformedScriptException();
			}
		} catch (BufferUnderflowException bufEx) {
			throw new MalformedScriptException(bufEx);
		}
	}

	public static byte[] toByteArray(ScriptHeader header) {
		Validate.notNull(header, "header argument is null"); //$NON-NLS-1$

		String textHeader = String.format(BYTE_HEADER_FORMAT, header.getVersion().toString(), header.getCharset().name());
		ByteBuffer byteHeader = HEADER_CHARSET.encode(textHeader);
		return byteHeader.array();
	}

}
