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

import java.awt.Color;
import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TObject;

@Localize(value="Pixel",localizeParent=true)
public class Pixel extends TObject {

	private final Point position = new Point();	
	private Color color= Color.BLACK;
	
	@Localize(value="Pixel")
	public Pixel()
	{
	}
	
	@Localize(value="Pixel")
	public Pixel(Photo photo, int x, int y)
	{
		position.setLocation(x,y);
		color = extractPixelColor(photo, x, y);
	}

	private Color extractPixelColor(Photo photo, int x, int y) {
		return new Color(photo.getPicture().getRGB(x,y));
	}
		
	
	@Localize(value="Pixel.compareColor")
	public boolean compareColor(String colorName)
	{
		Color c2 = TColor.translateColor(colorName, Color.BLACK);		
		return TColor.testCloseColor(color, c2);
	}
	
	@Localize(value="Pixel.getColor")
	public String getColor()
	{
		Collection<String> listColors = TColor.getColors();
		Iterator<String> it = listColors.iterator();
		while (it.hasNext() )
		{
			String s = it.next();
			if (TColor.testCloseColor(TColor.translateColor(s), color))
				return s;
			s = it.next();
		}		
		return "";		
	}
	
	@Localize(value="Pixel.getXCoordinate")
	public int getXCoordinate()
	{
		return position.x;
	}
	
	@Localize(value="Pixel.getYCoordinate")
	public int getYCoordinate()
	{
		return position.y;
	}
	
	@Localize(value="Pixel.setColor")
	public void setColor(String colorName)
	{
		color = TColor.translateColor(colorName, Color.BLACK);		
	}
	
	public void setColor(Color aColor)
	{
		color = aColor;
	}
	
	@Localize(value="Pixel.setPosition")
	public void setPosition(int i, int j)
	{
		position.setLocation(i,j);
	}

	@Localize(value="Pixel.get")
	public void get(Photo photo, int x, int y)
	{
		position.setLocation(x,y);
		color = extractPixelColor(photo, x, y);
	}

}
