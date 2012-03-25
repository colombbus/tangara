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
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * This class enables to create an object that makes a correspondence between the spoken language
 * and the java environnement as regards the color
 * @author gwen
 *
 */
public class TColor {

	private static final int TOLERANCE = 10;

	/**
	 * Creates a new intance of TColor with a black color by default
	 *
	 */
	public TColor() {
		color = Color.BLACK;
	}

	/**
	 * Creates a new instance of TColor with the color name passed as parameters
	 * @param colorName
	 * 			the color to choose
	 */
	public TColor(String colorName) {
		super();
		color = translateColor(colorName);
	}

	/**
	 * Creates a new instance of TColor with the color parameters (red, green and blue) given as arguments
	 * @param red
	 * 		represents the intensity of red
	 * @param green
	 * 		represents the intensity of green
	 * @param blue
	 * 		represents the intensity of blue
	 */
	public TColor(int red, int green, int blue) {
		boolean redOk = ValueCheck.isInside(red, MIN_COLOR, MAX_COLOR,
				Messages.getString("Color.badRedColor")); //$NON-NLS-1$
		boolean greenOk = ValueCheck.isInside(green, MIN_COLOR, MAX_COLOR,
				Messages.getString("Color.badGreenColor")); //$NON-NLS-1$
		boolean blueOk = ValueCheck.isInside(blue, MIN_COLOR, MAX_COLOR,
				Messages.getString("Color.badBlueColor")); //$NON-NLS-1$

		if (redOk && greenOk && blueOk) {
			color = new Color(red, green, blue);
		}

	}

	public static boolean testCloseColor(Color c1, Color c2)
	{
		if ((Math.abs(c1.getRed()-c2.getRed())<TOLERANCE)&&(Math.abs(c1.getGreen()-c2.getGreen())<TOLERANCE)&&(Math.abs(c1.getBlue()-c2.getBlue())<TOLERANCE))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Translates the color name in  the spoken language into a java color object. If the color is not found,
	 * the color black is returned by default
	 * @param colorName
	 * 		the color name in the spoken language
	 * @return
	 * 		java.awt.Color
	 */
	public static final Color translateColor(String colorName) {
		return translateColor(colorName, Color.BLACK);
	}

	/**
	 * Translate the color name in the spoken language to a java color object. If the color is not found,
	 * the color passed as parameters is returned
	 * @param colorName
	 * 		the color name in the spoken language
	 * @param defaultColor
	 * 		the default color returned if the color is not found
	 * @return
	 * 		java.awt.Color
	 */
	public static final Color translateColor(String colorName, Color defaultColor) {
		String normalizedName = StringUtils.removeAccents(colorName.trim().toLowerCase());
		Color color = TRANSLATOR.get(normalizedName);
		if (color == null) {
			Program.instance().printError(MessageFormat.format(Messages.getString("Color.unknownColor"), colorName)); //$NON-NLS-1$
			color = defaultColor;
		}
		return color;
	}

	/**
	 * Gets the java.awt.Color
	 * @return
	 * 		java.awt.Color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Returns all the colors you can choose
	 * @return
	 * 		a vector of strings containing all the colors you can choose
	 */
	public static Vector<String> getColors()
	{
		Vector<String> result = new Vector<String>();
		result.addAll(TRANSLATOR.keySet());
		return result;
	}

	private Color color;

	private static final int MIN_COLOR = 0;

	private static final int MAX_COLOR = 255;

	private static final Map<String, Color> TRANSLATOR = new Hashtable<String, Color>();

	static {
		TRANSLATOR.put(Messages.getString("color.black"), Color.BLACK); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.blue"), Color.BLUE); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.cyan"), Color.CYAN); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.yellow"), Color.YELLOW); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.white"), Color.WHITE); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.red"), Color.RED); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.pink"), Color.PINK); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.orange"), Color.ORANGE); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.magenta"), Color.MAGENTA); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.lightGray"), Color.LIGHT_GRAY); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.green"), Color.GREEN); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.gray"), Color.GRAY); //$NON-NLS-1$
		TRANSLATOR.put(Messages.getString("color.darkGrey"), Color.DARK_GRAY); //$NON-NLS-1$
	}
}
