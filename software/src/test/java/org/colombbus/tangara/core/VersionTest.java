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
package org.colombbus.tangara.core;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.colombbus.tangara.core.Version;
import org.junit.*;

@SuppressWarnings("nls")
public class VersionTest {

	@Test
	public void testHashCode() {
		Version v1 = new Version("1.0.0.alpha");
		Version v11 = new Version("1.0.0.alpha");
		Version v2 = new Version("1.0.0.beta");

		assertEquals(v1.hashCode(), v11.hashCode());
		assertFalse(v1.hashCode() == v2.hashCode());
	}

	@Test
	public void Version() {
		Version v = new Version();
		assertEquals(new Version(0, 0, 0, ""), v);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionStringIllegalMajor() {
		new Version("x.14.5.beta");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionStringIllegalMinor() {
		new Version("3.-4.5.beta");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionStringIllegalFix() {
		new Version("3.4.5c.beta");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionStringEmptyQualification() {
		new Version("3.4.5c.");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionStringIllegalQualificationEOL() {
		new Version("3.4.5c.al\npha");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionStringIllegalQualificationDot() {
		new Version("3.4.5c.al\npha.");
	}

	@Test
	public void testVersionStringMajor() {
		Version v = new Version("1");
		assertEquals(1, v.getMajor());
		assertEquals(0, v.getMinor());
		assertEquals(0, v.getFix());
		assertEquals("", v.getQualifier());
	}

	@Test
	public void testVersionStringMajorMinor() {
		Version v = new Version("3.04");
		assertEquals(3, v.getMajor());
		assertEquals(4, v.getMinor());
		assertEquals(0, v.getFix());
		assertEquals("", v.getQualifier());
	}

	@Test
	public void testVersionStringMajorMinorFix() {
		Version v = new Version("3.04.78");
		assertEquals(3, v.getMajor());
		assertEquals(4, v.getMinor());
		assertEquals(78, v.getFix());
		assertEquals("", v.getQualifier());
	}

	@Test
	public void testVersionStringMajorMinorFixQualification() {
		Version v = new Version("3.04.78.alpha36");
		assertEquals(3, v.getMajor());
		assertEquals(4, v.getMinor());
		assertEquals(78, v.getFix());
		assertEquals("alpha36", v.getQualifier());
	}

	@Test
	public void testVersionIntIntIntString() {
		Version v = new Version(1, 2, 3, "alpha");
		assertEquals(1, v.getMajor());
		assertEquals(2, v.getMinor());
		assertEquals(3, v.getFix());
		assertEquals("alpha", v.getQualifier());
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionIntIntIntStringBadMajor() {
		new Version(-1, 2, 3, "alpha");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionIntIntIntStringBadMinor() {
		new Version(1, -2, 3, "alpha");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionIntIntIntStringBadFix() {
		new Version(1, 2, -3, "alpha");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionIntIntIntStringBadQualifier() {
		new Version(1, -2, 3, "alph\na");
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testVersionIntIntIntStringNullQualifier() {
		new Version(1, -2, 3, null);
	}

	@Test
	public void testGetMajor() {
		Version v = new Version("3.04.78.alpha36");
		assertEquals(3, v.getMajor());
	}

	@Test
	public void testGetMinor() {
		Version v = new Version("3.04.78.alpha36");
		assertEquals(4, v.getMinor());
	}

	@Test
	public void testGetFix() {
		Version v = new Version("3.04.78.alpha36");
		assertEquals(78, v.getFix());
	}

	@Test
	public void testGetQualifier() {
		Version v = new Version("3.04.78.alpha36");
		assertEquals("alpha36", v.getQualifier());
	}

	@Test
	public void testToString() {
		assertEquals("3.4.78.alpha36", new Version("3.04.78.alpha36")
				.toString());
		assertEquals("3", new Version("3").toString());
		assertEquals("3.0.1", new Version("3.0.1").toString());
		assertEquals("3.5", new Version("3.5.0").toString());
		assertEquals("3.5.7", new Version("3.5.7").toString());
	}

	@Test
	public void testEqualsObject() {
		Version v1 = new Version("3.4.5");
		Version v2 = new Version("3.4.5");
		assertTrue(v1.equals(v2));
	}

	@Test
	public void testEqualsObjectFixDifferent() {
		Version v1 = new Version("3.4.5");
		Version v2 = new Version("3.4.6");
		assertFalse(v1.equals(v2));
	}

	@Test
	public void testEqualsObjectMinorDifferent() {
		Version v1 = new Version("3.4.5");
		Version v2 = new Version("3.5.5");
		assertFalse(v1.equals(v2));
	}

	@Test
	public void testEqualsObjectMajorDifferent() {
		Version v1 = new Version("3.4.5");
		Version v2 = new Version("7.4.5");
		assertFalse(v1.equals(v2));
	}

	@Test
	public void testEqualsObjectNull() {
		Version v1 = new Version("3.4.5");
		assertFalse(v1.equals(null));
	}

	@Test
	public void testCompareToEquals() {
		Version v1 = new Version("3.4.5");
		assertEquals(0, v1.compareTo(v1));

		Version v2 = new Version("3.4.5");
		assertEquals(0, v1.compareTo(v2));
		assertEquals(0, v2.compareTo(v1));
	}

	@Test
	public void testCompareToMajorDiff() {
		Version v1 = new Version("3.4.5");
		Version v2 = new Version("9.4.5");
		assertTrue(v1.compareTo(v2) < 0);
		assertTrue(v2.compareTo(v1) > 0);
	}

	@Test
	public void testCompareToMinorDiff() {
		Version v1 = new Version("3.4.5");
		Version v2 = new Version("3.8.5");
		assertTrue(v1.compareTo(v2) < 0);
		assertTrue(v2.compareTo(v1) > 0);
	}

	@Test
	public void testCompareToFixDiff() {
		Version v1 = new Version("3.4.5");
		Version v2 = new Version("3.4.24");
		assertTrue(v1.compareTo(v2) < 0);
		assertTrue(v2.compareTo(v1) > 0);
	}

	@Test
	public void testCompareToQualifierDiff() {
		Version v1 = new Version("3.4.5.alpha");
		Version v2 = new Version("3.4.24.beta");
		assertTrue(v1.compareTo(v2) < 0);
		assertTrue(v2.compareTo(v1) > 0);
	}

	@Test
	public void testCompareToNoQualifierDiff() {
		Version v1 = new Version("3.4.5");
		Version v2 = new Version("3.4.24.beta");
		assertTrue(v1.compareTo(v2) < 0);
		assertTrue(v2.compareTo(v1) > 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCompareToNull() {
		Version v1 = new Version("3.4.5");
		v1.compareTo(null);
	}

	@Test
	public void testPattern() {
		String regex = "[0-9]+(\\.[0-9]+(\\.[0-9]+(\\.[a-zA-Z0-9_\\-]+)?)?)?";
		assertTrue(Pattern.matches(regex, "1"));
		assertTrue(Pattern.matches(regex, "1.4"));
		assertTrue(Pattern.matches(regex, "1.4.7"));
		assertTrue(Pattern.matches(regex, "1.4.7.alpha"));
		assertTrue(Pattern.matches(regex, "1.4.7.alpha_"));
		assertTrue(Pattern.matches(regex, "1.4.7.alpha3"));
		assertTrue(Pattern.matches(regex, "1.4.7.alpha-"));
		assertFalse(Pattern.matches(regex, "1.-4.7"));

		Pattern pattern = Pattern.compile(regex);
		assertTrue(pattern.matcher("1").find());
		assertTrue(pattern.matcher("1.4").find());
		assertTrue(pattern.matcher("1.4.7").find());
		assertTrue(pattern.matcher("1.4.7.alpha").find());
		assertTrue(pattern.matcher("1.4.7.alpha_").find());
		assertTrue(pattern.matcher("1.4.7.alpha3").find());
		assertTrue(pattern.matcher("1.4.7.alpha-").find());
	}

	@Test
	public void testClone() throws CloneNotSupportedException {
		Version v = new Version("1.3.5.beta");
		Version clonedVersion =(Version) v.clone();
		assertEquals( v, clonedVersion);
	}

	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void testVersionVersionNullArg() {
		new Version( (Version)null );
	}

	@Test
	public void testVersionVersion() {
		Version v = new Version("1.3.5.beta");
		Version copyVersion = new Version( v );
		assertEquals( v, copyVersion);
	}
}
