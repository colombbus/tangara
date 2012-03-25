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

import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class defines a tangara key (from keyboard) cf keymanager
 * @author gwen
 *
 */
public class TKey
{
	/**
	 * Creates an empty TKey object
	 *
	 */
	public TKey() 
	{
	}

	/**
	 * Returns the ASCII code for this char
	 * @param c
	 * 		the char to translate
	 * @return
	 * 		the ASCII code for the char
	 */
	public static int getKeyCode(char c)
	{
		// For standard letters VK code corresponds to the ASCII code of the upper case letter
		return Character.toUpperCase(c);
	}

	/**
	 * Returns an number associated to each touch from the keyboard (up, down, right, left)
	 * @param s
	 * 		the string to translate
	 * @return
	 * 		the integer associated
	 * @throws Exception
	 */
	public static int getKeyCode(String s) throws Exception
	{
		if (TRANSLATOR.containsKey(s))
		{
			return TRANSLATOR.get(s);
		}
		else
			throw new Exception(Messages.getString("key.error.codeNotFound"));
	}
	
	private static final Map<String, Integer> TRANSLATOR = new Hashtable<String, Integer>();

	static {
		TRANSLATOR.put(Messages.getString("key.up"), KeyEvent.VK_UP);
		TRANSLATOR.put(Messages.getString("key.down"), KeyEvent.VK_DOWN);
		TRANSLATOR.put(Messages.getString("key.left"), KeyEvent.VK_LEFT);
		TRANSLATOR.put(Messages.getString("key.right"), KeyEvent.VK_RIGHT);
		TRANSLATOR.put(Messages.getString("key.space"), KeyEvent.VK_SPACE);
	}

	
}
