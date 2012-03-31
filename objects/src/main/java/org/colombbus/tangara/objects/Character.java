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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.Configuration;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.StringUtils;
import org.colombbus.tangara.TGraphicalObject;
import org.colombbus.tangara.objects.character.Movement;
import org.colombbus.tangara.objects.character.Skeleton;

/**
 * This class creates a Tangara character.
 * It could be a boy, a girl, a cat, a dog, a robot ....
 * The character can move and carry special objects (cf ToCarry objects).
 * This class uses the package org.colombbus.tangara.objects.character to create movements.
 * @author benoit
 *
 */
@SuppressWarnings("serial")
@Localize(value="Character",localizeParent=true)
public abstract class Character extends TGraphicalObject
{

	private static String DEFAULT_CHARACTER = null;
	private static final String JAR_EXT = ".jar";
	private int marginTop;
	private int marginLeft;
	private int marginRight;
	private int marginBottom;
	private Timer timer;
	public Skeleton skeleton;
    private Movement movement;

    /** Class logger */
    private static Logger LOG = Logger.getLogger(Character.class);

    /**
     * Creates a new character. By default it will be a boy.
     *
     */
    @Localize(value="Character")
    public Character()
    {
    	super();
    	if (DEFAULT_CHARACTER == null)
    		DEFAULT_CHARACTER = getMessage("defaultCharacter");
        initialize(DEFAULT_CHARACTER);
    }

    /**
     * Creates a new Character and sets its type according to the string passed as parameters.
     * @param characterName
     * 		the character's type in the current language
     */
    @Localize(value="Character")
    public Character(String characterName)
    {
    	super();
    	if (DEFAULT_CHARACTER == null)
    		DEFAULT_CHARACTER = getMessage("defaultCharacter");
        initialize(characterName);
    }

    public void initialize(String characterName)
    {
        skeleton = new Skeleton();
        movement = new Movement();
        setOpaque(false);

        String skeletonName = searchSkeletonType(characterName);
        try
        {
        	String skeletonFile = StringUtils.removeAccents(skeletonName) + JAR_EXT;
        	URL skeletonJar = getResource(skeletonFile).toURL();
            skeleton.readSkeleton(skeletonFile, skeletonJar);
            movement.setSkeleton(skeleton);
            movement.setCharacter(this);
            setDimensions();
		    timer = new Timer(100, movement);
            timer.start();
        }
        catch (Exception e)
        {
            LOG.error("Character creation error", e);
            String message = MessageFormat.format(getMessage("error"), e.getMessage());
            Program.instance().writeMessage(message);
        }
        displayObject();
    }

    public String searchSkeletonType(String characterName)
    {
        String language = Configuration.instance().getLanguage();
        String keyWord = "characterType." + language + "." + characterName;
        String skeletonName;
        if (containsMessage(keyWord))
        {
        	skeletonName = getMessage(keyWord);
        } else
        {
        	Program.instance().writeMessage(getMessage("CharacterTypeUnknown"));
        	skeletonName =  getMessage("characterType." + language + "." + DEFAULT_CHARACTER);
        }
        return skeletonName;
    }

    /**
     * Sets the dimensions of the character according to its type.
     *
     */
    private void setDimensions()
    {
    	try
    	{
		    int[] margins = skeleton.computeMargins();
		    marginTop = margins[0];
		    marginLeft = margins[1];
		    marginRight = margins[2];
		    marginBottom = margins[3];
		    setObjectWidth(skeleton.getWidth()+marginLeft+marginRight);
		    setObjectHeight(skeleton.getHeight()+marginTop+marginBottom);
   	}
    	catch (Exception e)
    	{
    		LOG.error("Error during computation of character dimensions : "+e.getMessage());
    	}
    }

    /**
     * Draws the character.
     * @param g
     * 		the Graphics context in which to paint.
     */
    @Override
	public void paintComponent(Graphics g)
    {
    	super.paintComponent(g);
    	if( skeleton != null)
    		skeleton.paintAt(g, new Point(marginLeft,marginTop));
    }

    /**
     * Moves forward this character of <code>value</code>.
     * @param value
     * 		Represents the value of the forward step according to the x-axis.
     */
	@Override
	@Localize(value="common.moveForward")
    public void moveForward(int value)
    {
        movement.move(new Dimension(value,0));
    }

	 /**
     * Moves backward this character of <code>value</code>.
     * @param value
     * 		Represents the value of the backward step according to the x-axis.
     */
	@Override
	@Localize(value="common.moveBackward")
    public void moveBackward(int value)
    {
        movement.move(new Dimension(-value,0));
    }

	/**
     * Moves up this character of <code>value</code>.
     * @param value
     * 		Represents the value of the forward step according to the y-axis.
     */
	@Override
	@Localize(value="common.moveUp")
    public void moveUp(int value)
    {
        movement.move(new Dimension(0,-value));
    }

	 /**
     * Moves back this character of <code>value</code>.
     * @param value
     * 		Represents the value of the backward step according to the y-axis.
     */
	@Override
	@Localize(value="common.moveDown")
    public void moveDown(int value)
    {
        movement.move(new Dimension(0,value));
    }

    /**
     * Changes the character's type
     * @param typeCharacter
     * 		the new character's type
     */
	@Localize(value="Character.change")
    public void change(String characterType)
    {
        try
        {
        	timer.stop();
        	Point prevCoordinates = getObjectLocation();
            int prevHeight = skeleton.getHeight();
            int prevWidth = skeleton.getWidth();
            int prevMarginLeft = marginLeft;
            int prevMarginTop = marginTop;
            String skeletonName = searchSkeletonType(characterType);
        	String skeletonFile = StringUtils.removeAccents(skeletonName) + JAR_EXT;
        	URL skeletonJar = getResource(skeletonFile).toURL();
            skeleton.readSkeleton(skeletonFile, skeletonJar);
            movement.setSkeleton(skeleton);
            int newHeight = skeleton.getHeight();
            int newWidth = skeleton.getWidth();

            // locates the new Character at the same baseline as the previous one
            setDimensions();
            setObjectLocation((prevCoordinates.x+prevMarginLeft-marginLeft+(prevWidth-newWidth)/2),prevCoordinates.y+prevMarginTop-marginTop+prevHeight-newHeight);
            repaint();
        }
        catch (Exception e)
        {
            String message = MessageFormat.format(getMessage("error"), e.getMessage());//$NON-NLS-1$
            Program.instance().writeMessage(message);
        }
        finally
        {
        	timer.restart();
        }
    }

	/**
	 * Raise the left arm.
	 * @param angle
	 * 		the value of the rotation.
	 */
	@Localize(value="Character.raiseLeftArm")
    public void raiseLeftArm(int angle)
    {
        movement.rotateLeftArm(-angle);
    }

	/**
	 * Loweer the left arm.
	 * @param angle
	 * 		the value of the rotation.
	 */
	@Localize(value="Character.lowerLeftArm")
    public void lowerLeftArm(int angle)
    {
        movement.rotateLeftArm(angle);
    }

	
	/**
	 * Raise the right arm.
	 * @param angle
	 * 		the value of the rotation.
	 */
	@Localize(value="Character.raiseRightArm")
    public void raiseRightArm(int angle)
    {
        movement.rotateRightArm(angle);
    }

	/**
	 * Lower the right arm.
	 * @param angle
	 * 		the value of the rotation.
	 */
	@Localize(value="Character.lowerRightArm")
    public void lowerRightArm(int angle)
    {
        movement.rotateRightArm(-angle);
    }

	
	/**
	 * Sets the location of this character
	 * @param x
	 * 		the new x-coordinate
	 * @param y
	 * 		the new y-coordinate
	 */
    @Override
	@Localize(value="common.setObjectLocation1")
    public void setObjectLocation(int x, int y)
    {
    	movement.setLocation(x, y);
    	super.setObjectLocation(x, y);
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
    	movement.setLocation((int)x, (int)y);
    	super.setObjectLocation(x, y);
    }

    /**
     * Moves the character to the location passed as parameters.
     * @param x
     * 		the new x-coordinate.
     * @param y
     * 		the new y-coordinate.
     */
    public void moveTo(int x, int y)
    {
    	super.setObjectLocation(x, y);
    }

    /**
     * Deletes the Character object.
     */
    @Override
	public void deleteObject()
    {
    	timer.stop();
        movement = null;
        super.deleteObject();
    }

    /**
     * Gets the coordinates of this character
     * @param pointName
     */
    public Point getCoordinates(String pointName)
    {
    	try
    	{
    		Point coordonneesRelatives = skeleton.getPointCoordinates(pointName);
    		Point coordonneesOrigine = getObjectLocation();
    		int x = coordonneesOrigine.x+coordonneesRelatives.x+marginLeft;
    		int y = coordonneesOrigine.y+coordonneesRelatives.y+marginTop;
    		return new Point(x,y);
    	}
    	catch (Exception e)
    	{
    		LOG.warn("Failure to recuperate the coordinates of a point", e);
    		return new Point(0,0);
    	}
    }


    /**
     * Freezes the character's movements or not
     * @param shallFreeze
     * 		true = freeze the movement
     */
    @Override
	public void freeze(boolean shallFreeze)
    {
    	super.freeze(shallFreeze);
    	if (shallFreeze)
    		timer.stop();
    	else
    		timer.restart();
    }

    /**
     * Adds this character as a carrier of the object passed as parameters.
     * So if we move this object (by another character for instance) our character will follow this object.
     * @param object
     * 		the object to carry.
     */
    public void gotMe(ToCarry object)
    {
    	movement.willFollow(object);
    }

    /**
     * Removes this character as a carrier of the object passed as parameters.
     * @param object
     * 		the object to release.
     */
    public void releaseMe(ToCarry object)
    {
    	movement.wontFollow(object);
    }

}
