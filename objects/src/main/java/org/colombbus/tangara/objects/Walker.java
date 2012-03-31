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
import java.util.ArrayList;
import java.util.HashMap;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;

@SuppressWarnings("serial")
@Localize(value="Walker",localizeParent=true)
public abstract class Walker extends Sprite
{
	private static final int BOTTOM=10000;
	private static final int JUMP_STEPS = 5;
	private static final int JUMP_MAX_AMPLITUDE=46;
	
	private boolean jump = false;
	private int jumpStep;
	private int jumpAmplitude;
	private java.util.List<Block> blockList = new ArrayList<Block>();
	
	public int gravitySpeed;
	public double gravitySpeedStep;
	public double currentGravitySpeedStep;
	private int gravityPixelStep;
	
	protected boolean gravity = false;
	protected boolean searchingGround = false;
	protected boolean groundFound = false;
    protected boolean stopMoving = false;
	protected int groundY = 0;
	
	 
	@Localize(value="Walker")
	public Walker()
	{
	    super();
		setGravitySpeed(50);
	}
	 
	@Localize(value="Walker")
	public Walker(String fileName)
	{
		this();
		addImage(fileName,fileName); //$NON-NLS-1$
		displayImage(fileName); //$NON-NLS-1$
	}

    /**
     * Sets that the object is falling down the screen.
     * @param value
     */
    @Localize(value="Walker.mayFall")
    public void mayFall(boolean value)
    {
    	synchronized (destination)
    	{
    		gravity = value;		
    		if (value)
    			destination.y=BOTTOM;									
    		else				
    			destination.y=getObjectY();				
    	}
    }		
	
	/**
	 * Makes the object jump.
	 */
	@Localize(value="Walker.jump")
	public void jump()
	{
		if (gravity && !jump)
		{	
		    if (testJump())
		        actualJump();
		}
	}
	
	/**
	 * Tests if there is a block under the Walker
	 */
	private boolean testJump()
	{
	    Point current = getObjectLocation();
	    Point newP = testCollisionWithBlocks(current, new Point(current.x, current.y+gravityPixelStep), Block.DIRECTION_DOWN);
		return ((newP.y-current.y)<gravityPixelStep);
	}
	
	
	/**
	 * Adds a block to the list of objects that can block the Walker
	 */
	@Localize(value="Walker.addBlock")
	public void addBlock(Block aBlock)
	{
		if (!blockList.contains(aBlock))
			blockList.add(aBlock);
	}
	
	/**
	 * Clears the list of blocks
	 */
	@Localize(value="Walker.removeBlocks")
	public void removeBlocks()
	{
		blockList.clear();
	}
	
	/**
	 * This method enables to define the speed of the gravity
	 */
	@Localize(value="Walker.setGravitySpeed")
	public void setGravitySpeed(int value)
	{
		if (value<=0)
			gravity = false;
		else
		{
			if (value<=100)			
				gravitySpeed = value;
			else if (value>100)
			{
				gravitySpeed = 100;
				Program.instance().writeMessage(getMessage("Error.maxSpeed")); //$NON-NLS-1$
			}
			gravitySpeedStep = 0.05 + (gravitySpeed-1)*(MAX_PIXEL_STEP-0.05)/99;
			currentGravitySpeedStep = gravitySpeedStep;
		}
	    gravityPixelStep = Math.max(1, (int)gravitySpeedStep);
	}
	
	/**
	 * Override
	 */
	@Override
	public void travel()
	{
		Point current = getObjectLocation();
		int newX, newY;
		newX = current.x;
		newY = current.y;
        byte directionByte = 0;
		if (!pause)
		{
			if (goingOn != STOP )
			{
				synchronized(destination)
				{
					switch (goingOn)
					{
						case RIGHT:
							newX +=pixelStep;
							directionByte = Block.DIRECTION_RIGHT;
							break;
						case LEFT:
							newX-=pixelStep;
                            directionByte = Block.DIRECTION_LEFT;
							break;
						case UP:
							newY-=pixelStep;
                            directionByte = Block.DIRECTION_UP;
							break;
						case DOWN:
							newY+=pixelStep;
                            directionByte = Block.DIRECTION_DOWN;
							break;
					}
					if (jump)
					{
					    jumpStep--;
                        if (jumpStep > 0)
                        {
                            newY-=jumpAmplitude;
                            jumpAmplitude=jumpAmplitude/2;
                            directionByte = (byte)(directionByte|Block.DIRECTION_UP);
                        }
                        else
                        {
                            jump = false;                       
                        }

					}
				}
				// now that we have our potential destination, check for collisions
				stopOccured = false;
				Point newP = testCollisionWithBlocks(current, new Point(newX, newY), directionByte);
				if (newP.equals(current)) {
                    // We cannot move: we stop, but don't initialize the collision manager
				    this.stop(false);
				}
				newX = newP.x;
				newY = newP.y;
                if (jump&&(newY == current.y))
                    // Walker was jumping, but hurt the ceiling: we stop jumping
                    jump = false;
                testCollision(newX,newY);
				if (!stopOccured)
				{
					synchronized (destination)
					{
						setLocationPrivate(newX,newY);
						destination.x = getObjectLocation().x;
						destination.y = getObjectLocation().y;
					}
					if (testMove)
					{
						HashMap<String,Object> info = new HashMap<String,Object>();
						info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
						info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
						processEvent("move", info); //$NON-NLS-1$
					}
				}
			}		
			else if (jump|!current.equals(destination))
			{
				synchronized(destination)
				{
					if (current.x<destination.x)
					{
						newX = Math.min(newX+pixelStep, destination.x);
						directionByte = (byte)(directionByte|Block.DIRECTION_RIGHT);
					}
					else if (current.x>destination.x)
					{
						newX = Math.max(newX-pixelStep, destination.x);
                        directionByte = (byte)(directionByte|Block.DIRECTION_LEFT);
					}
                    if (jump)
                    {                   
                        jumpStep--;
                        if (jumpStep > 0)
                        {
                            newY = current.y-jumpAmplitude;
                            destination.y = newY;
                            jumpAmplitude=jumpAmplitude/2;
                            directionByte = (byte)(directionByte|Block.DIRECTION_UP);
                        }
                        else
                        {
                            jump = false;                       
                        }
                    }
                    else
                    {
                        if (current.y<destination.y)
                        {
                            newY = Math.min(newY+pixelStep, destination.y);
                            directionByte = (byte)(directionByte|Block.DIRECTION_DOWN);
                        }
                        else if (current.y>destination.y)
                        {
                            newY = Math.max(newY-pixelStep, destination.y);
                            directionByte = (byte)(directionByte|Block.DIRECTION_UP);
                        }
                    }
				}
				// Now that we have a potential new location, check for collisions
                stopOccured = false;
            	Point newP = testCollisionWithBlocks(current, new Point(newX, newY), directionByte);
		        newX = newP.x;
                newY = newP.y;
                if (jump&&(newY == current.y))
                    // Walker was jumping, but hurt the ceiling: we stop jumping
                    jump = false;
                if (newP.equals(current))
                {
                    // We cannot move: we stop, but don't initialize the collision manager
                    this.stop(false);
                }
				testCollision(newX,newY);
				if (!stopOccured)
				{
					synchronized(destination)
					{
						setLocationPrivate(newX, newY);
					}
					if (testMove)
					{
						HashMap<String,Object> info = new HashMap<String,Object>();
						info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
						info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
						processEvent("move", info); //$NON-NLS-1$
					}
					Point newPosition = new Point(newX, newY);
					if (newPosition.equals(destination)) 
					{
						stopOccured = true;
					    if (!gravity)
					    {
						    if (testStop)
							{
								HashMap<String,Object> info = new HashMap<String,Object>();
								info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
								info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
								processEvent("stop", info); //$NON-NLS-1$
							}
					    }
					    else
					        stopMoving = true;
					}
				}
			}
		}
	}	 
	
	private void actualJump()
	{
        jump = true;
        jumpStep = JUMP_STEPS;
        jumpAmplitude = JUMP_MAX_AMPLITUDE;
	}
	
	public void fall()
	{
	    if (gravity && !jump)
		{
		    Point current;
            synchronized(destination)
            {
                current = getObjectLocation();
            }
            Point newP = testCollisionWithBlocks(current, new Point(current.x, current.y+gravityPixelStep), Block.DIRECTION_DOWN, false);
            int newX = newP.x;
            int newY = newP.y;
            if (!newP.equals(current))
            {
                testCollision(newX,newY);
                synchronized(destination)
                {
                    setLocationPrivate(newX, newY);
                    if (destination.x == current.x)
                        destination.x = newX;
                    destination.y = newY;
                }
                if (testMove)
                {
                    HashMap<String,Object> info = new HashMap<String,Object>();
                    info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
                    info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
                    processEvent("move", info); //$NON-NLS-1$
                }
            }
            else if (stopMoving&&testStop)
            {
                HashMap<String,Object> info = new HashMap<String,Object>();
                info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
                info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
                processEvent("stop", info); //$NON-NLS-1$
                stopMoving = false;
            }
		}
	}
	
	/**
	 * Sets the object location.
	 */
	@Override
	@Localize(value="common.setObjectLocation1")
	public void setObjectLocation(int x, int y)
	{
		super.setObjectLocation(x, y);
		synchronized(destination)
		{
			destination.x = x;
			if (!gravity)
				destination.y=y;
		}
		// Test if the sprite is out of the screen
		if (testOutOfScreen)
		{
			if ((x+getObjectWidth()<0)||(x>this.getGraphicsPane().getWidth())||(y>this.getGraphicsPane().getHeight())||(y+getObjectHeight()<0))
			{
				if (wasVisible)
				{
					wasVisible = false;
					processEvent("outOfScreen"); //$NON-NLS-1$
				}
			}
			else
				wasVisible = true;
		}
		collisionManager.updateLocation(x, y);		
	}
	
	@Override
	@Localize(value="common.setObjectLocation2")
	public void setObjectLocation(double x, double y)
	{
			setObjectLocation((int) x, (int) y);
	}
	
	@Override
	@Localize(value="common.setObjectLocation1")
    public void setObjectLocation(Point p)
    {
    	setObjectLocation(p.x, p.y);
    }
	
	
	public void testCollision(int x, int y, Sprite s)
	{
        if (searchingGround)
        {
            java.awt.Rectangle r = collisionManager.getRelativeBounds();
            int shift = r.height+r.y-1;
            if (s.getCollisionAreaTop()>=getObjectY()+shift)
            {
                groundY = Math.min(groundY, s.getCollisionAreaTop()-shift);
                groundFound = true;
            }
        }
	}
	
	
	@Override
	public void step()
	{
	    super.step();
        if (gravity)
        {
		    if (currentGravitySpeedStep>=1.0)
            {
                fall();
                currentGravitySpeedStep = gravitySpeedStep;
            }                                   
            else                    
                currentGravitySpeedStep += gravitySpeedStep;
        }
	}
	
    @Override
	@Localize(value="Sprite.stop")
    public void stop()
    {
    	stop(true);
    }
    
    protected void stop(boolean initManager) {
        synchronized(destination)
        {
            goingOn = STOP;
            destination.x = getObjectLocation().x;
            destination.y = getObjectLocation().y;
            if (initManager)
            	collisionManager.init();
        }
		stopOccured = true;
        if (testStop)
        {
            if (!gravity)
            {
                HashMap<String,Object> info = new HashMap<String,Object>();
                info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
                info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
                processEvent("stop", info); //$NON-NLS-1$
            }
            else {
                stopMoving = true;
            }
        }
    }
	
	protected Point testCollisionWithBlocks(Point oldP, Point newP, byte directionByte)
	{
		return testCollisionWithBlocks(oldP, newP, directionByte, true);
	}
	
	protected Point testCollisionWithBlocks(Point oldP, Point newP, byte directionByte, boolean tolerance)
	{
	    java.awt.Rectangle previousLocation = collisionManager.getAbsoluteBounds();
	    java.awt.Rectangle newLocation = new java.awt.Rectangle(previousLocation);
	    newLocation.translate(newP.x-oldP.x, newP.y-oldP.y);
	    for (Block b:blockList)
	    {
	        newLocation = b.computeMove(previousLocation, newLocation, directionByte, tolerance);
	    }
	    
	    java.awt.Rectangle collisionArea = collisionManager.getRelativeBounds();
	    Point location = newLocation.getLocation();
	    return new Point(location.x-collisionArea.x, location.y-collisionArea.y);
		
	}

	

}
