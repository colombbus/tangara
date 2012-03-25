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

import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

/**
 * Manager of version build with four fields (integer, integer, integer, text)
 * <ul>
 * <li>major version</li>
 * <li>minor version</li>
 * <li>fix version</li>
 * <li>qualifier</li>
 * </ul>
 *
 */
public final class Version implements Cloneable, Comparable<Version> {

	public static final String FIELD_SEPARATOR = "."; //$NON-NLS-1$
	private static final String QUALIFIER_REGEX = "([a-zA-Z0-9_\\-]+)?"; //$NON-NLS-1$
	public static final String VERSION_REGEX = "[0-9]+(\\.[0-9]+(\\.[0-9]+(\\." + QUALIFIER_REGEX + ")?)?)?"; //$NON-NLS-1$ //$NON-NLS-2$

	private int major = 0;
	private int minor = 0;
	private int fix = 0;
	private String qualifier = ""; //$NON-NLS-1$

	/**
	 * Create a default version 0.0.0
	 */
	public Version() {
	}

	public Version(Version version) {
		Validate.notNull(version, "version argument is null"); //$NON-NLS-1$

		this.major = version.major;
		this.minor = version.minor;
		this.fix = version.fix;
		this.qualifier = version.qualifier;
	}

	/**
	 * Create a version from a textual representation
	 *
	 * @param textVersion
	 *            a textual representation of the version
	 */
	public Version(String textVersion) {
		checkTextFormat(textVersion);
		extractFieldsFromText(textVersion);
	}

	private static void checkTextFormat(String textVersion) {
		Validate.notNull(textVersion, "textVersion argument is null"); //$NON-NLS-1$

		boolean matchTextPattern = Pattern.matches(VERSION_REGEX, textVersion);
		if (matchTextPattern == false) {
			throw new IllegalArgumentException("textVersion argument is invalid. It shall match " + VERSION_REGEX); //$NON-NLS-1$
		}
	}

	private void extractFieldsFromText(String textVersion) {
		try {
			doExtractFieldsFromText(textVersion);
		} catch (Exception ex) {
			throw new IllegalArgumentException("textVersion argument is invalid. It shall match " + VERSION_REGEX); //$NON-NLS-1$
		}
	}

	private void doExtractFieldsFromText(String textVersion) throws Exception {
		Scanner scanner = new Scanner(textVersion);
		scanner.useDelimiter("\\."); //$NON-NLS-1$
		major = scanner.nextInt();
		if (scanner.hasNext())
			minor = scanner.nextInt();
		if (scanner.hasNext())
			fix = scanner.nextInt();
		if (scanner.hasNext())
			qualifier = scanner.next();
		if (scanner.hasNext()) {
			throw new Exception("Too many fields"); //$NON-NLS-1$
		}
		scanner.close();
	}

	public Version(int major, int minor, int fix, String qualifier) {
		setMajor(major);
		setMinor(minor);
		setFix(fix);
		setQualifier(qualifier);
	}

	public int getMajor() {
		return major;
	}

	private void setMajor(int major) {
		Validate.isTrue(major >= 0, "major number shall be equal or greater than 0"); //$NON-NLS-1$
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	private void setMinor(int minor) {
		Validate.isTrue(minor >= 0, "minor number shall be equal or greater than 0"); //$NON-NLS-1$
		this.minor = minor;
	}

	public int getFix() {
		return fix;
	}

	private void setFix(int fix) {
		Validate.isTrue(fix >= 0, "fix number shall be equal or greater than 0"); //$NON-NLS-1$
		this.fix = fix;
	}

	public String getQualifier() {
		return qualifier;
	}

	private void setQualifier(String qualifier) {
		Validate.notNull(qualifier, "qualifier is null"); //$NON-NLS-1$
		Validate.isTrue(Pattern.matches(QUALIFIER_REGEX, qualifier), "qualifier argument is invalid"); //$NON-NLS-1$

		this.qualifier = qualifier;
	}

	@Override
	public String toString() {
		StringBuilder textVersion = new StringBuilder();
		textVersion.append(major);
		if (minor != 0 || fix != 0 || qualifier.length() > 0) {
			textVersion.append(FIELD_SEPARATOR).append(minor);
			if (fix != 0 || qualifier.length() > 0) {
				textVersion.append(FIELD_SEPARATOR).append(fix);
				if (qualifier.length() > 0) {
					textVersion.append(FIELD_SEPARATOR).append(qualifier);
				}
			}
		}

		return textVersion.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fix;
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + ((qualifier == null) ? 0 : qualifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (fix != other.fix)
			return false;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		if (qualifier == null) {
			if (other.qualifier != null)
				return false;
		} else if (!qualifier.equals(other.qualifier))
			return false;
		return true;
	}

	@Override
	public int compareTo(Version version) {
		if (version == null)
			throw new IllegalArgumentException("version argument is null"); //$NON-NLS-1$

		if (major < version.major)
			return -1;
		else if (major > version.major)
			return +1;

		if (minor < version.minor)
			return -1;
		else if (minor > version.minor)
			return +1;

		if (fix < version.fix)
			return -1;
		else if (fix > version.fix)
			return +1;

		return qualifier.compareTo(version.qualifier);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Version copy = new Version(this);
		return copy;
	}

}
