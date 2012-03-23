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

import javax.lang.model.element.TypeElement;

public class TypeElementHelper {

	public static String extractPackageName(final TypeElement classType) {
		String qualifiedClassName = classType.getQualifiedName().toString();
		int lastDotPosition = qualifiedClassName.lastIndexOf("."); //$NON-NLS-1$
		return qualifiedClassName.substring(0, lastDotPosition);
	}

	public static String extractClassName(final TypeElement aClass) {
		String qualifiedClassName = aClass.getQualifiedName().toString();
		int lastDotPosition = qualifiedClassName.lastIndexOf("."); //$NON-NLS-1$
		return qualifiedClassName.substring(lastDotPosition + 1);
	}

}
