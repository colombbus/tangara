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
 * This class allows you to translate the java environement to the spoken language. 
 * Indeed, it has a TypedResourceBundle attribute to find a string matching the name 
 * of a method or a color or a style of writing.
 */
import java.awt.Color;
import java.awt.Font;
import java.text.MessageFormat;

public class Messages {
	private static final String BUNDLE_NAME = "org.colombbus.tangara.messages"; 

	private static TypedResourceBundle RESOURCE_BUNDLE = new TypedResourceBundle(BUNDLE_NAME, true);

	/**
	 * Creates a new instance of Messages 
	 * (no init)
	 */
	private Messages() {
	}

	/**
	 * Initializes the TypedResourceBundle attribute according to the spoken language
	 * @param language
	 * 		the spoken language used by the kids
	 */
	public static void loadLocalizedResource(String language)
	{
		RESOURCE_BUNDLE = new TypedResourceBundle(BUNDLE_NAME,language,true);
	}
	
	/**
	 * Returns a string that corresponds to the key
	 * @param key
	 * 		a string in messages_lg.properties
	 * @return
	 * 		the string in the spoken language
	 */
	public static String getString(String key) {
		return RESOURCE_BUNDLE.getString(key);
	}
	
	/**
	 * See {@link String#format(String, Object...)}
	 * @param key
	 * @param args
	 * @return
	 */
	public static String formatString( String key, Object... args) {
		String format = getString( key );
		return String.format(format, args);
	}

	/**
	 * See {@link MessageFormat#format(String, Object...)}
	 * @param key
	 * @param args
	 * @return
	 */
	public static String formatMessage(String key, Object... args) {
		String pattern = getString(key);
		String message = MessageFormat.format(pattern, args);
		return message;
	}
	
	/**
	 * Returns a java.awt.Font that corresponds to the key
	 * @param key
	 * 		a string in messages_lg.properties
	 * @return
	 * 		the font in the spoken language
	 */
	public static Font getFont( String key) {
		return RESOURCE_BUNDLE.getFont(key);
	}
	
	/**
	 * Returns a java.awt.Color that corresponds to the key
	 * @param key
	 * 		a string in messages_lg.properties
	 * @return
	 * 		the color in the spoken language
	 */
	public static Color getColor(String key) {
		return RESOURCE_BUNDLE.getColor(key);
	}
	
	/**
	 * Returns if the .properties contains the key
	 * @param key
	 * 		the string to check
	 * @return
	 * 		a boolean
	 */
	public static boolean containsMessage(String key)
	{
		return RESOURCE_BUNDLE.containsMessage(key);
	}
	
}
