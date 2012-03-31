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

package org.colombbus.tangara.objects.sprite;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.colombbus.tangara.objects.Sprite;

/**
 * This class permits to manage the movement of the Sprit object.
 * @author Benoit
 *
 */
public class SpriteMovement implements ActionListener
{
	private List<Sprite> sprites= new ArrayList<Sprite>();

	
	/**
	 * Creates a new instance of spriteMovement
	 */
	public SpriteMovement()
	{		 	
	}	

	
	/**
	 * Enables to add a Sprite to the list of sprite to move
	 */
	public void addSprite(Sprite aSprite)
	{
		synchronized(sprites)
		{
			if (aSprite != null)
			{
				if (!sprites.contains(aSprite))
					sprites.add(aSprite);
			}
		}
	}
	
	/**
	 * Enables to remove a Sprite from the list of sprites to move
	 */
	public void removeSprite(Sprite aSprite)
	{
		synchronized(sprites)
		{
			if (sprites.contains(aSprite))
				sprites.remove(aSprite);
		}
	}

	/**
	 * The execution of the Thread.
	 */
	@Override
	public void actionPerformed(ActionEvent event)
	{		
		ArrayList<Sprite> currentList;
		synchronized(sprites)
		{			
			currentList = new ArrayList<Sprite>(sprites);
		}
		for (Sprite s: currentList)		
		{
			if (s!=null)
			{
			    s.step();
			}
		}		
	}
}
