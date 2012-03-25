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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JFrame;

/**
 * This abstract class defines the structure of the main frame of Tangara
 */
@SuppressWarnings("serial")
public abstract class TFrame extends JFrame
{

	/**
     * Get panel containing the game panel
     *
     * @return
     * 		the panel containing the game area
     */
	public abstract GraphicsPane getGraphicsPane();

	/**
     * Initializes the banner and the line mode before displaying the welcome message
     */
	public abstract void afterInit();

	/**
	 * Computes the size of the main frame
	 */
	protected boolean computeSize() {
		boolean frameFound = false;
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;

		Component[] components = getGraphicsPane().getComponents();
		for (Component component:components)
		{
			if (component.getClass().getSuperclass().getSuperclass().getName().equals("org.colombbus.tangara.TInternalFrame"))
			{
				org.colombbus.tangara.TGraphicalObject frame = (org.colombbus.tangara.TGraphicalObject)component;
				Point framePosition = frame.getLocation();
				int frameX = framePosition.x;
				int frameY = framePosition.y;
				int frameWidth = frame.getWidth();
				int frameHeight = frame.getHeight();
				if (!frameFound)
				{
					// first frame encountered
					frameFound = true;
					x = frameX;
					y = frameY;
					width = frameWidth;
					height = frameHeight;
				}
				else
				{
					// not the first frame encountered
					if (frameX<x)
					{
						width+=(x-frameX);
						x = frameX;
					}
					else
					{
						frameWidth+=(frameX-x);
					}
					if (frameWidth>width)
						width = frameWidth;
					if (frameY<y)
					{
						height+=(y-frameY);
						y = frameY;
					}
					else
					{
						frameHeight+=(frameY-y);
					}
					if (frameHeight>height)
						height = frameHeight;
				}
			}
		}
		if (frameFound) {
			getGraphicsPane().setPreferredSize(new Dimension(width,height));
			for (Component component:components)
			{
				if (component instanceof TGraphicalObject)
				{
					((TGraphicalObject)component).shiftLocation(x, y);
				}
			}
		}
		else {
			GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Rectangle bounds = graphicsEnvironment.getMaximumWindowBounds();
			setPreferredSize(bounds.getSize());
		}
		return frameFound;
	}

	public void addLogMsg(String message, int style, int lineNumber) {

	}


	public void setErrorLines(int index, int number, int errorLine) {

	}

	public int getCurrentLogIndex() {
		return 0;
	}

}
