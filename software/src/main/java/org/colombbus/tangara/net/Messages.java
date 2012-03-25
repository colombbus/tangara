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

package org.colombbus.tangara.net;

import java.awt.Color;
import java.awt.Font;

import org.colombbus.tangara.TypedResourceBundle;

public class Messages {
	
	private static final String BUNDLE_NAME = "org.colombbus.tangara.net.messages"; //$NON-NLS-1$

	private static TypedResourceBundle RESOURCE_BUNDLE = new TypedResourceBundle(BUNDLE_NAME, false);

	private Messages() {
	}

	public static void loadLocalizedResource(String language)
	{		
		RESOURCE_BUNDLE = new TypedResourceBundle(BUNDLE_NAME,language,false);		
	}

	public static String getString(final String key) {
		return RESOURCE_BUNDLE.getString(key);
	}

	public static Font getFont(final String key) {
		return RESOURCE_BUNDLE.getFont(key);
	}

	public static Color getColor(final String key) {
		return RESOURCE_BUNDLE.getColor(key);
	}
	
	public static boolean containsMessage(String key)
	{
		return RESOURCE_BUNDLE.containsMessage(key);
	}
}
