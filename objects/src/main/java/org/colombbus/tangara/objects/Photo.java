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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TObject;

@Localize(value="Photo",localizeParent=true)
public class Photo extends TObject{

	private BufferedImage picture;
	private static Logger LOG = Logger.getLogger(Photo.class);
	private Point position;
	private Object synchronization;
	
	@Localize(value="Photo")
	public Photo()
	{
		super();
		picture = null;
		synchronization = new Object();
	}
	
	@Localize(value="Photo")
	public Photo(int x, int y, int width, int height)
	{
		super();
		capture(x,y,width,height);
	}
	
	@Localize(value="Photo.getPicture")
	public BufferedImage getPicture()
	{
		synchronized(synchronization)
		{
			return picture;
		}
	}
	
	@Localize(value="Photo.capture")
	public void capture(int x, int y, int width, int height)
	{
		synchronized(synchronization)
		{
			try {
				Robot r = new Robot();
				picture =  r.createScreenCapture(new Rectangle(x+Program.instance().getCurrentGraphicsPane().getLocationOnScreen().x, y+Program.instance().getCurrentGraphicsPane().getLocationOnScreen().y, width, height));
				position = new Point(x,y);
			} catch (AWTException e) {
				LOG.error("Error while trying to catchPicture " + e);
				picture = null;
			}
		}
	}
	
	
	@Localize(value="Photo.containsColor")
	public boolean containsColor(String colorName)
	{
		if (picture == null)
		{
			Program.instance().writeMessage(getMessage("error.noPicture"));
			return false;
		}
    	Color c = TColor.translateColor(colorName, Color.black);
    	int colorValue = c.getRGB();
    	for (int i=0;i<picture.getWidth();i++)
    	{
    		for (int j=0;j<picture.getHeight();j++)
    		{
    			if (picture.getRGB(i,j)==colorValue)
    				return true;
    		}
    	}
    	return false;
	}
	
	
	@Localize(value="Photo.getWidth")
	public int getWidth()
	{
		if (picture == null)
		{
			Program.instance().writeMessage(getMessage("error.noPicture"));
			return 0;
		}
		return picture.getWidth();
	}
	
	@Localize(value="Photo.getHeight")
	public int getHeight()
	{
		if (picture == null)
		{
			Program.instance().writeMessage(getMessage("error.noPicture"));
			return 0;
		}
		return picture.getHeight();
	}
	
	@Localize(value="Photo.getXLocation")
	public int getXLocation()
	{
		if (picture == null)
		{
			Program.instance().writeMessage(getMessage("error.noPicture"));
			return 0;
		}
		return position.x;
	}
	
	@Localize(value="Photo.getYLocation")
	public int getYLocation()
	{
		if (picture == null)
		{
			Program.instance().writeMessage(getMessage("error.noPicture"));
			return 0;
		}
		return position.y;
	}
}
