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

import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * This class uses ResourceBundle. Thus, we can have a program independent from
 * the spoken language
 */
public class TypedResourceBundle {

	private static Logger LOG = Logger.getLogger(TypedResourceBundle.class);
	private static final Color DEFAULT_COLOR = Color.BLACK;

	private final Map<String, Color> colorTranslator = new Hashtable<String, Color>();
	private ResourceBundle bundle;

	/**
	 * Initializes the resource bundle without initializing the language and the
	 * color translator
	 *
	 * @param resourceName
	 *            the base name of the resource bundle
	 */
	public TypedResourceBundle(String resourceName) {
		this(ResourceBundle.getBundle(resourceName), false);
	}

	/**
	 * Initializes the resource bundle
	 *
	 * @param resourceName
	 *            the base name of the resource bundle
	 * @param initColor
	 *            To choose if you want to initialize the color translator
	 */
	public TypedResourceBundle(String resourceName, boolean initColor) {
		this(ResourceBundle.getBundle(resourceName), initColor);
	}

	/**
	 * Initializes the resource bundle with the spoken language (most often
	 * used).
	 *
	 * @param resourceName
	 *            the base name of the resource bundle
	 * @param language
	 *            the spoken language
	 * @param initColor
	 *            To choose if you want to initialize the color translator
	 */
	public TypedResourceBundle(String resourceName, String language, boolean initColor) {
		Locale defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
		this.bundle = ResourceBundle.getBundle(resourceName, new Locale(language));
		Locale.setDefault(defaultLocale);
		if (initColor) {
			initColor();
		}
	}

	/**
	 * Creates a TypedResourceBundle instance from a resource bundle.
	 *
	 * @param bundle
	 *            the resource bundle
	 */
	public TypedResourceBundle(ResourceBundle bundle) {
		this(bundle, false);
	}

	/**
	 * Creates a TypedResourceBundle instance from a resource bundle and
	 * initializes or not the color translator
	 *
	 * @param bundle
	 *            the resource bundle
	 * @param initColor
	 *            To choose if you want to initialize the color translator.
	 */
	public TypedResourceBundle(ResourceBundle bundle, boolean initColor) {
		super();
		assert bundle != null;
		this.bundle = bundle;
		if (initColor) {
			initColor();
		}
	}

	/**
	 * Initializes the color translator
	 *
	 */
	private void initColor() {
		colorTranslator.put(getString("color.black"), Color.BLACK); //$NON-NLS-1$
		colorTranslator.put("black", Color.BLACK); //$NON-NLS-1$
		colorTranslator.put(getString("color.blue"), Color.BLUE); //$NON-NLS-1$
		colorTranslator.put("blue", Color.BLUE); //$NON-NLS-1$
		colorTranslator.put(getString("color.cyan"), Color.CYAN); //$NON-NLS-1$
		colorTranslator.put("cyan", Color.CYAN); //$NON-NLS-1$
		colorTranslator.put(getString("color.yellow"), Color.YELLOW); //$NON-NLS-1$
		colorTranslator.put("yellow", Color.YELLOW); //$NON-NLS-1$
		colorTranslator.put(getString("color.white"), Color.WHITE); //$NON-NLS-1$
		colorTranslator.put("white", Color.WHITE); //$NON-NLS-1$
		colorTranslator.put(getString("color.red"), Color.RED); //$NON-NLS-1$
		colorTranslator.put("red", Color.RED); //$NON-NLS-1$
		colorTranslator.put(getString("color.pink"), Color.PINK); //$NON-NLS-1$
		colorTranslator.put("pink", Color.PINK); //$NON-NLS-1$
		colorTranslator.put(getString("color.orange"), Color.ORANGE); //$NON-NLS-1$
		colorTranslator.put("orange", Color.ORANGE); //$NON-NLS-1$
		colorTranslator.put(getString("color.magenta"), Color.MAGENTA); //$NON-NLS-1$
		colorTranslator.put("magenta", Color.MAGENTA); //$NON-NLS-1$
		colorTranslator.put(getString("color.lightGray"), Color.LIGHT_GRAY); //$NON-NLS-1$
		colorTranslator.put("light gray", Color.LIGHT_GRAY); //$NON-NLS-1$
		colorTranslator.put(getString("color.green"), Color.GREEN); //$NON-NLS-1$
		colorTranslator.put("green", Color.GREEN); //$NON-NLS-1$
		colorTranslator.put(getString("color.gray"), Color.GRAY); //$NON-NLS-1$
		colorTranslator.put("gray", Color.GRAY); //$NON-NLS-1$
		colorTranslator.put(getString("color.darkGrey"), Color.DARK_GRAY); //$NON-NLS-1$
		colorTranslator.put("dark grey", Color.DARK_GRAY); //$NON-NLS-1$
	}

	/**
	 * Gets a string for the given key from this resource bundle.
	 *
	 * @param key
	 *            the key for the desired string
	 * @return the string for the given key
	 */
	public String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			LOG.warn("Could not find string resource " + key);//$NON-NLS-1$
			return '!' + key + '!';
		}
	}

	/**
	 * Gets a java.awt.Font for the given key from this resource bundle
	 *
	 * @param key
	 *            the key for the desired font
	 * @return the java.awt.Font for the given key
	 */
	public Font getFont(String key) {
		String fontDesc = null;
		try {
			fontDesc = bundle.getString(key);
		} catch (MissingResourceException e) {
			LOG.warn("Could not find font resource " + key);//$NON-NLS-1$
		}
		if (fontDesc != null) {
			fontDesc = fontDesc.trim();
		}
		return Font.decode(fontDesc);
	}

	/**
	 * Gets a java.awt.Color for the given key from the color translator
	 *
	 * @param key
	 *            the key for the desired color
	 * @return the java.awt.Color for the given key
	 */
	public Color getColor(String key) {
		Color color = null;
		try {
			String strValue = bundle.getString(key);
			color = colorTranslator.get(strValue);
			if (color == null) {
				try {
					StringTokenizer tokenizer = new StringTokenizer(strValue);
					int red = Integer.parseInt(tokenizer.nextToken());
					int green = Integer.parseInt(tokenizer.nextToken());
					int blue = Integer.parseInt(tokenizer.nextToken());
					if (tokenizer.hasMoreTokens()) {
						int alpha = Integer.parseInt(tokenizer.nextToken());
						color = new Color(red, green, blue, alpha);
					} else {
						color = new Color(red, green, blue);
					}
				} catch (Exception ex) {
					LOG.error("Failed loading color " + key, ex); //$NON-NLS-1$
				}
			}
		} catch (MissingResourceException e) {
			LOG.warn("Could not find color resource " + key);//$NON-NLS-1$
		}

		if (color == null) {
			LOG.warn("Could not load color resource " + key);//$NON-NLS-1$
			color = DEFAULT_COLOR;
		}

		return color;
	}

	/**
	 * Returns if the .properties contains the key
	 *
	 * @param key
	 *            the string to check
	 * @return a boolean
	 */
	public boolean containsMessage(String key) {
		if (bundle != null) {
			if (System.getProperty("java.version").startsWith("1.6"))
				return bundle.containsKey(key);
			else {
				Enumeration<String> enume = bundle.getKeys();
				String s;
				while (enume.hasMoreElements()) {
					s = enume.nextElement();
					if (s.equals(key))
						return true;
				}
				return false;
			}
		} else
			return false;
	}

}
