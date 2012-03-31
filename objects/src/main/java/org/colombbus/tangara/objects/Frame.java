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

package org.colombbus.tangara.objects;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TInternalFrame;

/**
 * This class provides a very simple object showing the borders of a Frame.
 * A command can change the backgroubd color of the Frame.
 * @author benoit
 */
@SuppressWarnings("serial")
@Localize(value="Frame",localizeParent=true)
public abstract class Frame extends TInternalFrame
{

    /**
     * Creates the object.
     */
	@Localize(value="Frame")
	public Frame()
    {
    	super();        
    }

	/**
	 * Sets the background color of the Frame.
	 * @param colorName
	 */
	@Override
	@Localize(value="common.setColor")
    public void setColor(String colorName)
    {
		super.setColor(colorName);
    }

}
