/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008 Colombbus (http://www.colombbus.org)
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

package org.colombbus.tangara;

/**
 * A set of functions to check values and, in case of failure, print an error
 * message on the screen.
 * 
 * @author gwen
 * 
 */
public final class ValueCheck {

	/**
	 * No instance !
	 */
	private ValueCheck() {
		super();
	}

	/**
	 * Check if an integer value belongs to a space.
	 * 
	 * @param value
	 *            the value to check
	 * @param min
	 *            space minimum value
	 * @param max
	 *            space maximum value
	 * @param errorMsg
	 *            the error message to print if the value does not belongs to
	 *            the space
	 * @return <code>true</code> if the value belongs to the space.
	 */
	public static boolean isInside(int value, int min, int max, String errorMsg) {
		boolean inside = min <= value && value <= max;
		if (inside == false) {
			Program.instance().printError(errorMsg);
		}

		return inside;
	}

}
