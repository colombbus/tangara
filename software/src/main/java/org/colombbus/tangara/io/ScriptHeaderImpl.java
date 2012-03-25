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

import java.nio.charset.Charset;

import org.apache.commons.lang.Validate;
import org.colombbus.tangara.core.Version;

public class ScriptHeaderImpl implements ScriptHeader {
	private Charset charset;
	private Version version;

	public ScriptHeaderImpl(Charset charset, Version version) {
		Validate.notNull(charset, "charset argument is null"); //$NON-NLS-1$
		Validate.notNull(version, "version argument is null"); //$NON-NLS-1$

		this.charset = charset;
		this.version = new Version(version);
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public Version getVersion() {
		return version;
	}
}