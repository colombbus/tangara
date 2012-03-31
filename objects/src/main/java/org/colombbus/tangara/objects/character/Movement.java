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

package org.colombbus.tangara.objects.character;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.colombbus.tangara.objects.Character;
import org.colombbus.tangara.objects.ToCarry;


/**
 * This class enables character to move. Indeed, it simulates the breathing movement, the arms movements or rotation etc....
 * Nevertheless, according to the character's type, you can do some movements or not.
 * @author benoit
 */
public class Movement implements ActionListener{

    private Character character;
    private Skeleton skeleton;
    private int chestShift=0;
    private boolean chestUp=true;
    private boolean leftArmRotation=false;
    private boolean rightArmRotation=false;
    private boolean move;
    private double leftArmAngle=0;
    private double rightArmAngle=0;
    private double tailAngle=0;
    private Point destination=new Point(0,0);
    private double leftArmAimedAngle=0;
    private double rightArmAimedAngle=0;
    private int breathCounter=BREATH_STEP;
    private boolean leftArmMovement;
    private boolean rightArmMovement;
    private boolean chestMovement;
    private boolean tailMovement;

    private static final int BREATHING_AMPLITUDE = 3;
    private static final double BREATH_ANGLE_STEP = 1;
    private static final double MOVE_ANGLE_STEP = 10;
    private static final int MOVE_STEP = 25;
    private static final int BREATH_STEP = 3;

    private final List<ToCarry> followers = new Vector<ToCarry>();

    /**
     * Creates a new Movement instance
     *
     */
    public Movement()
    {
    }

    /**
     * Sets the skeleton used by the character.
     * @param skeleton
     * 		the skeleton to use.
     */
    public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;

		// Detection of movable elements
		tailMovement = skeleton.hasTail();

		// It is an animal: chest and arms do not move
		if (tailMovement) {
			rightArmMovement = false;
			leftArmMovement = false;
			chestMovement = false;
		} else {
			leftArmMovement = skeleton.hasLeftArm();
			rightArmMovement = skeleton.hasRightArm();
			chestMovement = skeleton.hasChest();
		}
	}

    /**
     * Sets the character who uses this movement.
     * @param aCharacter
     * 		the character who uses this movement.
     */
    public void setCharacter(Character aCharacter)
    {
        character = aCharacter;
    }

   /**
     * Changes the aimed angle of left arm.
     * @param position
     * 		the aimed angle.
     */
    public void rotateLeftArm(double position)
    {
    	if (skeleton.hasLeftArm())
    	{
	        leftArmAimedAngle+= Math.round(position);
	        leftArmRotation = true;
    	}
    }

    /**
     * Changes the aimed angle of the right arm.
     * @param position
     * 		the aimed angle.
     */
    public void rotateRightArm(double position)
    {
    	if (skeleton.hasRightArm())
    	{
	        rightArmAimedAngle+= Math.round(position);
	        rightArmRotation = true;
    	}
    }

    /**
     * Notifies that we want to change the location of our character
     * @param where
     * 		the new location
     */
    public void move(Dimension where)
    {
        move = true;
        destination = new Point(destination.x+where.width,destination.y+where.height);
    }

    /**
     * Sets the character's location.
     * @param x
     * 		the x-coordinate.
     * @param y
     * 		the y-coordinate.
     */
    synchronized public void setLocation(int x, int y)
    {
    	move = false;
    	destination = new Point(x, y);
    }

    /**
     * Turns the left arm (and the left hand) until it reaches the angle passed as parameters.
     * @param angle
     * 		the new angle.
     * @throws Exception
     */
    private void turnLeftArm(double angle) throws Exception
    {
        skeleton.rotateElement(Skeleton.LEFT_SHOULDER,Skeleton.LEFT_ARM,angle);
        skeleton.rotatePoint(Skeleton.LEFT_SHOULDER, Skeleton.LEFT_HAND, angle);
    }

    /**
     * Turns the right arm (and the right hand) until it reaches the angle passed as parameters.
     * @param angle
     * 		the new angle.
     * @throws Exception
     */
    private void turnRightArm(double angle) throws Exception
    {
    	 skeleton.rotateElement(Skeleton.RIGHT_SHOULDER,Skeleton.RIGHT_ARM,angle);
         skeleton.rotatePoint(Skeleton.RIGHT_SHOULDER, Skeleton.RIGHT_HAND, angle);
    }


    /**
     * This run method enables to create arms and breathing movements for characters.
     * In addition, its permits the fact that the carried objects follow the character.
     */
    @Override
	public void actionPerformed(ActionEvent e1)
    {
       	handleLeftArmMovement();
        handleRightArmMovement();
        handleCharacterMovement();
        handleBreathing();
        for (ToCarry object:followers)
          	object.follow();
    }

    /**
     * Simulates the breathing movement
     *
     */
	private void handleBreathing() {
		// Manage Breathing of Character
		breathCounter--;
		if (breathCounter<=0)
		{
		    breathCounter = BREATH_STEP;
		    try
		    {
		        if (chestUp)
		        {
		            chestShift--;
		            if (rightArmMovement&&(!rightArmRotation))
		            {
		                rightArmAngle+=BREATH_ANGLE_STEP;
		                turnRightArm(rightArmAngle);
		            }
		            if (leftArmMovement&&(!leftArmRotation))
		            {
		                leftArmAngle-=BREATH_ANGLE_STEP;
		                turnLeftArm(leftArmAngle);
		            }
		            if (chestShift<-BREATHING_AMPLITUDE)
		            {
		                chestUp = false;
		            }
		            if (chestMovement)
		            {
		            	skeleton.shilftElement(Skeleton.CHEST,new Dimension(0,chestShift));
		            }
		            if (tailMovement)
		            {
		                tailAngle-=BREATH_ANGLE_STEP;
		                skeleton.rotateElement(Skeleton.TAIL_BASE,Skeleton.TAIL,tailAngle);
		            }
		        }
		        else
		        {
		            chestShift++;
		            if (rightArmMovement&&(!rightArmRotation))
		            {
		                rightArmAngle-=BREATH_ANGLE_STEP;
		                turnRightArm(rightArmAngle);
		            }
		            if (leftArmMovement&&(!leftArmRotation))
		            {
		                leftArmAngle+=BREATH_ANGLE_STEP;
		                turnLeftArm(leftArmAngle);
		            }
		            if (chestShift>0)
		            {
		                chestUp = true;
		            }
		            if (chestMovement)
		            {
		            	skeleton.shilftElement(Skeleton.CHEST,new Dimension(0,chestShift));
		            }
		            if (tailMovement)
		            {
		                tailAngle+=BREATH_ANGLE_STEP;
		                skeleton.rotateElement(Skeleton.TAIL_BASE,Skeleton.TAIL,tailAngle);
		            }
		        }
		        repaint();
		    }
		    catch (Exception e)
		    {
		        LOG.warn("Breathing failed",e);
		    }
		}
		else
		{
		    if (leftArmRotation||rightArmRotation||move)
		    {
		        repaint();
		    }
		}
	}

	/**
	 * Simulates the moving of character
	 *
	 */
	private void handleCharacterMovement() {
		// Manage Moving of Character
		if (move)
		{
			Point position = character.getObjectLocation();
			int xCoordinate = position.x;
			int yCoordinate = position.y;
		    if (xCoordinate==destination.x && yCoordinate==destination.y )
		    {
		        move = false;
		    }
		    else
		    {
		        if (xCoordinate<destination.x)
		        {
		            xCoordinate=Math.min(xCoordinate+MOVE_STEP,destination.x);
		        }
		        else if (xCoordinate>destination.x)
		        {
		            xCoordinate=Math.max(xCoordinate-MOVE_STEP,destination.x);
		        }

		        if (yCoordinate<destination.y)
		        {
		            yCoordinate=Math.min(yCoordinate+MOVE_STEP,destination.y);
		        }
		        else if (yCoordinate>destination.y)
		        {
		            yCoordinate=Math.max(yCoordinate-MOVE_STEP,destination.y);
		        }
		        character.moveTo(xCoordinate, yCoordinate);
		    }
		}
	}

	/**
	 * Simulates the rotation of right arm
	 *
	 */
	private void handleRightArmMovement() {
		// Manage rotation of Right Arm
		if (rightArmRotation)
		{
		    if (rightArmAngle==rightArmAimedAngle)
		    {
		        rightArmRotation= false;
		    }
		    else
		    {
		        if (rightArmAngle>rightArmAimedAngle)
		        {
		            rightArmAngle=Math.max(rightArmAngle-MOVE_ANGLE_STEP,rightArmAimedAngle);
		        }
		        else
		        {
		            rightArmAngle=Math.min(rightArmAngle+MOVE_ANGLE_STEP,rightArmAimedAngle);
		        }

		        try
		        {
		        	turnRightArm(rightArmAngle);
		        }
		        catch (Exception e)
		        {
		            LOG.warn("Fail to rotate right arm",e);
		        }
		    }
		}
	}

	/**
	 * Simulates rotation of left arm
	 *
	 */
	private void handleLeftArmMovement() {
		// Manage rotation of Left Arm
		if (leftArmRotation)
		{
		    if (leftArmAngle==leftArmAimedAngle)
		    {
		        leftArmRotation= false;
		    }
		    else
		    {
		        if (leftArmAngle>leftArmAimedAngle)
		        {
		            leftArmAngle=Math.max(leftArmAngle-MOVE_ANGLE_STEP,leftArmAimedAngle);
		        }
		        else
		        {
		            leftArmAngle=Math.min(leftArmAngle+MOVE_ANGLE_STEP,leftArmAimedAngle);
		        }

		        try
		        {
		        	turnLeftArm(leftArmAngle);
		        }
		        catch (Exception e)
		        {
		            LOG.warn("Fail to rotate left arm",e);
		        }
		    }
		}
	}

	/**
	 * Repaints the movement
	 *
	 */
	private void repaint()
	{
		character.repaint();
	}

	/**
	 * Adds the object, passed as parameters, as a object that is carried by the character
	 * @param object
	 * 		the object to carry
	 */
	public void willFollow(ToCarry object)
	{
		followers.add(object);
	}

	/**
	 * Removes the object, passed as paramaters, as a carried object
	 * @param object
	 * 		the object to remove
	 */
	public void wontFollow(ToCarry object)
	{
		followers.remove(object);
	}


	/** Class logger */
    private static Logger LOG = Logger.getLogger(Movement.class);

}
