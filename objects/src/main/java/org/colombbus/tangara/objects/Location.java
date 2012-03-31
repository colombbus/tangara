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

import java.awt.Point;
import org.colombbus.build.Localize;
import org.colombbus.tangara.TObject;

@Localize(value="Location",localizeParent=true)
public class Location extends TObject {

	protected Point position;	
	
	@Localize(value="Location")
	public Location()
	{
		position = new Point();
	}
	
	@Localize(value="Location")
	public Location(int x, int y)
	{
		position.setLocation(x,y);
	}

	@Localize(value="Location.getXCoordinate")
	public int getXCoordinate()
	{
		return position.x;
	}
	
	@Localize(value="Location.getYCoordinate")
	public int getYCoordinate()
	{
		return position.y;
	}

	@Localize(value="Location.setXCoordinate")
	public void setXCoordinate(int x)
	{
		position.x = x;
	}
	
	@Localize(value="Location.setYCoordinate")
	public void setYCoordinate(int y)
	{
		position.y = y;
	}
	
	@Localize(value="Location.setPosition")
	public void setPosition(int i, int j)
	{
		position.setLocation(i,j);
	}

}
