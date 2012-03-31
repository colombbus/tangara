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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.text.MessageFormat;
import java.util.Vector;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.StringUtils;

/**
 * This class enables to define special objects. Indeed, this objects are portable by one or several characters.
 * So its carrier moves, the object will move too.
 * @author benoit
 *
 */
@SuppressWarnings("serial")
@Localize(value="ToCarry",localizeParent=true)
public abstract class ToCarry extends Picture
{
	private java.util.List<MayTakeMe> whoTakesMe = new Vector<MayTakeMe>();
	private boolean isTaken = false;
	private Character owner;
	private String ownersHand;
	private Polygon me;
	private int shiftX;
	private int shiftY;
	private String messageWhenTaken;

	/**
	 * Creates a default ToCarry object.
	 *
	 */
	@Localize(value="ToCarry")
	public ToCarry()
	{
		computePolygon();
	}

	/**
	 * Creates a ToCarry object and sets its image according to the string passed as parameters.
	 * @param imageName
	 * 		the url of the object's image
	 */
	@Localize(value="ToCarry")
	public ToCarry(String imageName)
	{
		this();
		loadPicture(imageName);
	}

	/**
	 * Adds a new carrier of this object.
	 * @param p
	 * 		the character who carries the object.
	 * @param handName
	 * 		the hand who carries the object.
	 */
	@Localize(value="ToCarry.watch")
	public void watch(Character p, String handName)
	{
		whoTakesMe.add(new MayTakeMe(p,handName));
	}

	/**
	 * Sets the text that will display when a character catches the object.
	 * @param text
	 * 		the new message.
	 */
	@Localize(value="ToCarry.setMessage")
	public void setMessage(String text)
	{
		messageWhenTaken = text;
	}

	/**
	 * Sets the image
	 * @param imageName
	 * 		the url of the object's name
	 */
	@Override
	@Localize(value="common.loadPicture")
	public void loadPicture(String imageName)
	{
		super.loadPicture(imageName);
		computePolygon();
	}

	/**
     * Moves forward this object of <code>value</code>.
     * @param value
     * 		Represents the value of the forward step according to the x-axis.
     */
	@Override
	@Localize(value="common.moveForward")
	public void moveForward(int value)
	{
		super.moveForward(value);
		computePolygon();
	}

	 /**
     * Moves backward this object of <code>value</code>.
     * @param value
     * 		Represents the value of the backward step according to the x-axis.
     */
	@Override
	@Localize(value="common.moveBackward")
	public void moveBackward(int value)
	{
		super.moveBackward(value);
		computePolygon();
	}

	/**
     * Moves up this object of <code>value</code>.
     * @param value
     * 		Represents the value of the forward step according to the y-axis.
     */
	@Override
	@Localize(value="common.moveUp")
	public void moveUp(int value)
	{
		super.moveUp(value);
		computePolygon();
	}

	/**
     * Moves back this object of <code>value</code>.
     * @param value
     * 		Represents the value of the backward step according to the y-axis.
     */
	@Override
	@Localize(value="common.moveDown")
	public void moveDown(int value)
	{
		super.moveDown(value);
		computePolygon();
	}


	/**
	 * Sets the location of this object
	 * @param x
	 * 		the new x-coordinate
	 * @param y
	 * 		the new y-coordinate
	 */
    @Override
	@Localize(value="common.setObjectLocation1")
	public void setObjectLocation(int x, int y)
	{
		super.setObjectLocation(x, y);
		computePolygon();
	}
    
    @Override
	@Localize(value="common.setObjectLocation1")
    public void setObjectLocation(Point p)
    {
    	setObjectLocation(p.x, p.y);
    }
    
    @Override
	@Localize(value="common.setObjectLocation2")
	public void setObjectLocation(double x, double y)
	{
		super.setObjectLocation(x, y);
		computePolygon();
	}

    /**
     * Computes the size of the object according to its image.
     *
     */
	private void computePolygon()
	{
		int[] xCoordinates = {getObjectX(),getObjectX()+getObjectWidth(),getObjectX()+getObjectWidth(),getObjectX()};
		int[] yCoordinates = {getObjectY(),getObjectY(),getObjectY()+getObjectHeight(),getObjectY()+getObjectHeight()};
		me = new Polygon(xCoordinates,yCoordinates,4);
		shiftX = (getObjectWidth()/2);
		shiftY = (getObjectHeight()/2);
	}

	/**
	 * This method will display the object's message for each character who carries it.
	 *
	 */
	private void test()
	{
		if (me==null)
		{
			computePolygon();
		}
		if (whoTakesMe!=null)
		{
			for (MayTakeMe m:whoTakesMe)
			{
				Point handCoordinates = m.getPersonnage().getCoordinates(m.getHandName());
				if (me.contains(handCoordinates)) // if the object is in the right place
				{
					isTaken = true;
					owner = m.getPersonnage();
					owner.gotMe(this);
					ownersHand = m.getHandName();
					if (messageWhenTaken!=null)
					{
						Program.instance().writeMessage(messageWhenTaken);
					}
					break;
				}
			}
		}
	}

	/**
     * Draws the object.
     * @param g
     * 		the Graphics context in which to paint.
     */
	@Override
	public void paintComponent(Graphics g)
	{
		if (!isTaken)
		{
			test();
		}
		super.paintComponent(g);
	}

	/**
	 * Enables the portable object to follow its owner
	 *
	 */
	public void follow()
	{
		Point handCoordinates = owner.getCoordinates(ownersHand);
		setObjectLocation(handCoordinates.x-shiftX, handCoordinates.y-shiftY);
	}

	/**
	 * Removes the character as a carrier of this object and deletes this object.
	 */
	@Override
	public void deleteObject()
	{
		if (isTaken)
		{
			owner.releaseMe(this);
		}
		super.deleteObject();
	}

	/**
	 * This class enables to have a field which associates a character with a hand name.
	 * The hand name represents the hand that carries the portable object.
	 * @author benoit
	 *
	 */
	class MayTakeMe
	{
		private Character who;
		private String handName;

		/**
		 * Creates a MayTakeMe object.
		 * @param who
		 * 		the carrier.
		 * @param handName
		 * 		the hand which carries the object.
		 */
		public MayTakeMe(Character who,String handName)
		{
			this.who = who;
			String handName2 = StringUtils.removeAccents(handName);
			if (!containsMessage(handName2)) {
	            String message = MessageFormat.format(getMessage("hand.name_error"), handName);
	            Program.instance().writeMessage(message);
				LOG.error("Could not find hand from name '"+handName+"'");
			} else {
				this.handName = getMessage(handName2);
			}
		}

		/**
		 * Gets the carrier
		 * @return
		 * 		the carrier
		 */
		public Character getPersonnage()
		{
			return who;
		}

		/**
		 * Gets the hand who carries.
		 * @return
		 * 		the name of the hand which carries.
		 */
		public String getHandName()
		{
			return handName;
		}

	}
}
