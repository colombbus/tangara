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
 * This class provides a component helping to set the style of a string.
 * It serves to save a certain number of parameters: boolean open, int aLevel, int aPosition)
 * @author Lionel
 */
public class Link
	{
		private boolean mayOpen = false;
		private int level = -1;
		private int position = -1;
		
		public Link()
		{
		}
		
		public Link(boolean open, int aLevel, int aPosition)
		{
			this.mayOpen = open;
			this.level = aLevel;
			this.position = aPosition;
		}
		
		public boolean getMayOpen()
		{
			return mayOpen;
		}
		
		public int getLevel()
		{
			return level;		
		}
		
		public int getPosition()
		{
			return position;		
		}
		
		public void setMayOpen(boolean open)
		{
			mayOpen = open;
		}
		
		public void setLevel(int aLevel)
		{
			this.level = aLevel;			
		}
		
		public void setPosition(int aPosition)
		{
			this.position = aPosition;
		}
	}
