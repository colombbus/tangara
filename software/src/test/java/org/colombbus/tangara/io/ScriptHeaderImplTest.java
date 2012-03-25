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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.nio.charset.Charset;

import org.colombbus.tangara.core.Version;
import org.colombbus.tangara.io.ScriptHeader;
import org.colombbus.tangara.io.ScriptHeaderImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author gwen
 *
 */
public class ScriptHeaderImplTest {

	private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	private static final Version VERSION = new Version("1.2.3.beta");

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unused")
	@Test
	public void testFileHeaderImplCharsetVersion() {
		new ScriptHeaderImpl(ISO_8859_1, VERSION);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testFileHeaderImplCharsetVersionNullCharset() {
		new ScriptHeaderImpl(null, VERSION);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testFileHeaderImplCharsetVersionNullVersion() {
		new ScriptHeaderImpl(ISO_8859_1, (Version) null);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testFileHeaderImplStringStringNullEncoding() {
		new ScriptHeaderImpl((Charset) null, VERSION);
	}

	/**
	 * Test method for
	 * {@link org.colombbus.tangara.io.ScriptHeaderImpl#getEncoding()}.
	 */
	@Test
	public void testGetCharset() {
		ScriptHeader header = new ScriptHeaderImpl(ISO_8859_1, VERSION);
		assertEquals(ISO_8859_1, header.getCharset());
	}

	/**
	 * Test method for
	 * {@link org.colombbus.tangara.io.ScriptHeaderImpl#getVersion()} .
	 */
	@Test
	public void testGetVersion() {
		ScriptHeader header = new ScriptHeaderImpl(ISO_8859_1, VERSION);
		assertNotSame(VERSION, header.getVersion());
		assertEquals(VERSION, header.getVersion());
	}


}
