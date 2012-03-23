/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008-2012 Colombbus (http://www.colombbus.org)
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
package org.colombbus.build;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

/**
 * Logger of the processor, using the messager mechanism.
 */
public class ProcessingLogger {

	private Messager messager;

	public void setMessager(Messager messager) {
		this.messager = messager;
	}

	public void info(String message, Object... args) {
		log(Kind.NOTE, message, args);
	}

	private void log(Kind kind, String message, Object... args) {
		if (messager == null)
			return;

		String formattedMessage = String.format(message, args);
		messager.printMessage(kind, formattedMessage);
	}

	public void warning(String message, Object... args) {
		log(Kind.WARNING, message, args);
	}

	public void error(String message, Object... args) {
		log(Kind.ERROR, message, args);
	}
}
