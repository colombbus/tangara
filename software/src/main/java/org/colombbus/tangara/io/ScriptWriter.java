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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.lang.Validate;


/**
 * @author gleroux
 *
 */
public class ScriptWriter {

	private ScriptHeader header;

	/**
	 *
	 */
	public ScriptWriter(ScriptHeader header) {
	    Validate.notNull(header, "header argument is null"); //$NON-NLS-1$

		this.header = header;
	}

	public void writeScript( String script, OutputStream out ) throws IOException {
	    Validate.notNull(script, "script argument is null"); //$NON-NLS-1$
        Validate.notNull(out, "out argument is null"); //$NON-NLS-1$

        out.write(ScriptHeaderHelper.toByteArray(header));
        out.flush();

		Writer writer = new OutputStreamWriter(out,header.getCharset());
		writer.write(script);
		writer.flush();
	}
}
