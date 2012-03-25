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

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.colombbus.build.Localize;

@SuppressWarnings("serial")
@Localize(value="",localizeParent=true,localizeThis=false)
public abstract class TInternalFrame extends TGraphicalObject{

	
	 /**
     * Creates the object.
     */
	public TInternalFrame()
    {
    	super();
        setSize(300,100);
        Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        Border compound = BorderFactory.createCompoundBorder(
    			  raisedbevel, loweredbevel);
        setBorder(compound);
        setBackground(DEFAULT_BACKGROUND);
        displayObject();
    }

	/**
	 * Sets the background color of the Frame.
	 * @param colorName
	 */	
    public void setColor(String colorName)
    {
		Color c = TColor.translateColor(colorName, Color.black);
        setBackground(c);
        getGraphicsPane().revalidate();
        getGraphicsPane().repaint();
    }
}
