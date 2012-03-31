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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TWitness;

/**
 * This object can follow the object path
 * @author ESIEE
 *
 */
@SuppressWarnings("serial")
@Localize(value="Rider",localizeParent=true)
public abstract class Rider extends Sprite {
	
	private static final Logger LOG = Logger.getLogger(Rider.class);
	
	
	private Path path;	 	

	private static final int MIN_STEP = 5;
	private static final double TWO_PI = 2* Math.PI;
	private static final int VECTOR_SIZE = 10;
	private int previousDirection[] = {-1, -1 };
	private final Point vector = new Point();
	private int SQUARESTEP;
	private int moves;
	private boolean testCrossing = false;
	private static final double PEAK_TOLERANCE = Math.PI/4;
	private Point previousDir = new Point();
	private boolean initialMove = false;
	private Point[] previousLocations = new Point[VECTOR_SIZE];
	private int previousLocationIndex = 0;
	
	/**
	 * Creates a new Rider object
	 */
	@Localize(value="Rider")
	public Rider()
	{
		registerEvent("crossing"); //$NON-NLS-1$
		translatedEvents.put(getMessage("crossing"),"crossing"); //$NON-NLS-1$ //$NON-NLS-2$
	 }
	 
	/**
	 * Creates a new Rider object
	 */
	@Localize(value="Rider")
	public Rider(String fileName)
	{
		this();
		addImage(fileName,fileName); //$NON-NLS-1$
		displayImage(fileName); //$NON-NLS-1$
	}
	 
	/**
	 * Displays the specified image
	 * @param the image Name
	 */ 
	@Override
	@Localize(value="Rider.displayImage")
	public void displayImage(String imageName)
	{		 
		if (imageName!=currentImageName)
	 	{
	 		Point center = getCenterLocation();
	 		super.displayImage(imageName);
		 	setObjectWidth(currentImage.getWidth());
		 	setObjectHeight(currentImage.getHeight());
		 	setLocationPrivate(center.x-getObjectWidth()/2 , center.y-getObjectHeight()/2);			 	
	 	}		
	 	else
		{
			display();
		} 		 	
	}

	/**
	 * Defines the path of the rider
	 * @param p
	 * 	a path
	 */
	@Localize(value="Rider.setPath")
	public void setPath(Path p)
	{
		path = p;
	}

	/**
	 * Gets the current path
	 * @return A Path
	 */
	@Localize (value="Rider.getPath")
	public Path getPath()
	{
		return path;
	}
	
	@Localize(value="Rider.turnAround")
	public void turnAround() {
		vector.setLocation(-vector.x, -vector.y);
	}
	 
	@Override
	@Localize(value="Rider.alwaysMoveForward")
	public void alwaysMoveForward()
	{
		if (check())
		{
			synchronized(destination)
			{
				goingOn = RIGHT;
				vector.setLocation(1,0);
				pause = false;
				setDirection(RIGHT);
				initialMove = true;
			}
		}
	}
	
	@Override
	@Localize(value="common.moveForward")
	public void moveForward(int value)
	{
		if (check())
		{
			synchronized(destination)
			{
				goingOn = STOP;
				moves+=value;
				vector.setLocation(1,0);
				pause = false;
				setDirection(RIGHT);
				initialMove = true;
			}
		}
	}
	 
	@Override
	@Localize(value="Rider.alwaysMoveBackward")
	public void alwaysMoveBackward()
	{
		if (check())
		{
			synchronized(destination)
			{
				goingOn = LEFT;
				vector.setLocation(-1,0);
				pause = false;
				setDirection(LEFT);
				initialMove = true;
			}
		}
	}
	 	 
	@Override
	@Localize(value="common.moveBackward")
	public void moveBackward(int value)
	{
		if (check())
		{
			synchronized(destination)
			{
				goingOn = STOP;
				moves+=value;
				vector.setLocation(-1,0);
				pause = false;
				setDirection(LEFT);
				initialMove = true;
			}
		}
	}
	 
	@Override
	@Localize(value="Rider.alwaysMoveUp")
	public void alwaysMoveUp()
	{
		if (check())
		{
			synchronized(destination)
			{
				goingOn = UP;
				vector.setLocation(0,-1);
				pause = false;
				setDirection(UP);
				initialMove = true;
			}
		}
	}
	 
	@Override
	@Localize(value="common.moveUp")
	public void moveUp(int value)
	{
		if (check())
		{
			synchronized(destination)
			{
				goingOn = STOP;
				moves+=value;
				vector.setLocation(0,-1);
				pause = false;
				setDirection(UP);
				initialMove = true;
			}
		}
	}
	 
	@Override
	@Localize(value="Rider.alwaysMoveDown")
	public void alwaysMoveDown()
	{
		if (check())
		{
			synchronized(destination)
			{
				goingOn = DOWN;
				vector.setLocation(0,1);
				pause = false;
				setDirection(DOWN);
				initialMove = true;
			}
		}
	}
	 	 
	@Override
	@Localize(value="common.moveDown")
	public void moveDown(int value)
	{
		if (check())
		{
			synchronized(destination)
			{
				goingOn = STOP;
				moves+=value;
				vector.setLocation(0,1);
				pause = false;
				setDirection(DOWN);
				initialMove = true;
			}
		}
	 }
	 
	/**
	 * Sets the rider's position and tests if there is a collision
	 */
	@Override
	@Localize(value="common.setObjectLocation1")
	public void setObjectLocation(int x, int y)
	{
		super.setObjectLocation(x, y);
		moves = 0;
		goingOn = STOP;
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
		moves = 0;
		goingOn = STOP;
	}

	@Localize(value="Rider.setCenterLocation")
	public void setCenterLocation(int x, int y)
	{
		int cornerLeftX = x-getObjectWidth()/2;
		int cornerLeftY = y-getObjectHeight()/2;
		setObjectLocation(cornerLeftX, cornerLeftY);
	}
	
	protected void setCenterLocationPrivate(int x, int y)
	{
		int cornerLeftX = x-getObjectWidth()/2;
		int cornerLeftY = y-getObjectHeight()/2;
		setLocationPrivate(cornerLeftX, cornerLeftY);
	}

	/**
	 * Stops the rider
	 */
	@Override
	@Localize(value="Rider.stop")
	public void stop()
	{
		synchronized(destination)
		{
			goingOn = STOP;
			moves = 0;
			stopOccured = true;
		}
		if (testStop)
		{
			HashMap<String,Object> info = new HashMap<String,Object>();
			info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
			info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
			processEvent("stop", info); //$NON-NLS-1$
		}
	}
	
	@Localize(value="Rider.isStoped")
	public boolean isStoped() {
		return goingOn == STOP;
	}
	
	private Vector<Point> lookForPath(Point location, Path aPath)
	{
		Vector<Point> result = new Vector<Point>();
		for (int x = location.x-SQUARESTEP;x <= location.x+SQUARESTEP;x++)
		{
			if (isOnPath(x,location.y-SQUARESTEP,aPath))
			{
				result.add(new Point(x,location.y-SQUARESTEP));
			}
		}
		for (int y = location.y-SQUARESTEP;y <= location.y+SQUARESTEP;y++)
		{
			if (isOnPath(location.x+SQUARESTEP,y,aPath))
			{
				result.add(new Point(location.x+SQUARESTEP,y));
			}
		}
		for (int x = location.x+SQUARESTEP-1;x >= location.x-SQUARESTEP;x--)
		{
			if (isOnPath(x,location.y+SQUARESTEP,aPath))
			{
				result.add(new Point(x,location.y+SQUARESTEP));
			}
		}
		for (int y = location.y+SQUARESTEP-1;y >=location.y-SQUARESTEP ;y--)
		{
			if (isOnPath(location.x-SQUARESTEP,y,aPath))
			{
				result.add(new Point(location.x-SQUARESTEP,y));
			}
		}
		if (result.size()==0)
			return null;
		return result;
	}

	/**
	* Returns the closest position on the path from the center of the rider
	* @return
	*/
	private Point getClosestLocation()
	{
		
		// center coordinates
		Point p = getCenterLocation();
		int centerX = (int)p.getX();
		int centerY = (int)p.getY();
		
		// max values
		int maxHeight = this.getGraphicsPane().getHeight();
		int maxWidth = this.getGraphicsPane().getWidth();
		int maxStepHorizontal = Math.max(centerX, maxWidth-centerX);
		int maxStepVertical = Math.max(centerY, maxHeight-centerY);
		int maxStep = Math.max(maxStepHorizontal, maxStepVertical);
		
		// we start from the center and go horizontally, vertically, and in diagonals until we meet the path
		for (int step=0;step<maxStep;step++)
		{
			if (isOnPath(centerX-step,centerY))
				return new Point(centerX-step,centerY);
			if (isOnPath(centerX+step,centerY))
				return new Point(centerX+step,centerY);
			if (isOnPath(centerX,centerY-step))
				return new Point(centerX,centerY-step);
			if (isOnPath(centerX,centerY+step))
				return new Point(centerX,centerY+step);
			if (isOnPath(centerX-step,centerY-step))
				return new Point(centerX-step,centerY-step);
			if (isOnPath(centerX-step,centerY+step))
				return new Point(centerX-step,centerY+step);
			if (isOnPath(centerX+step,centerY+step))
				return new Point(centerX+step,centerY+step);
			if (isOnPath(centerX+step,centerY-step))
				return new Point(centerX+step,centerY-step);
		}
		return null;
	 }
	 
	// Tests whether the point (x, y) is on the path
	private boolean isOnPath(int x, int y)
	{
		return isOnPath(x,y,path);
	}

	// Tests whether the point (x, y) is on the path given in parameter
	private boolean isOnPath(int x, int y, Path aPath)
	{
		int relX = x - aPath.getObjectX();
		int relY = y - aPath.getObjectY();
		try
		{
			if ((relX<0)||(relX>aPath.getObjectWidth())||(relY<0)||(relY>aPath.getObjectHeight()))
				return false;
			else {
				int pixel = aPath.picture.getRGB(relX, relY);
				return ( (pixel!=-1) && ( (pixel & 0x00ffffff) != pixel));
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return false;
		}
	}
	
	private void setDirection(int dir)
	{
		previousDirection[1] = previousDirection[0];
		previousDirection[0] = direction;
		direction = dir;
		if (testDirection&&(previousDirection[1]!=previousDirection[0]) && (previousDirection[0]==direction))
		{
			HashMap<String,Object> info = new HashMap<String,Object>();
			info.put("oldDirection", translateDirection(previousDirection[1])); //$NON-NLS-1$
			info.put("newDirection", getDirection()); //$NON-NLS-1$
			info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
			info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
			processEvent("directionChange",info); //$NON-NLS-1$
		}
	}
	
	private double diffAngle(double angle1, double angle2) {
		/*double angle = Math.abs(angle1 - angle2);
		return Math.min(angle, TWO_PI - angle);*/
		return (TWO_PI + angle1-angle2)%TWO_PI;
	}
	
	private Point findLocationAccordingToVector(Point location, Point direction,Point previousDirection, Vector<Point> possibleLocations) {
		double angleVector = getAngle(direction);
		double angleMin = Math.PI/2;
		Point newLocation = null;
		double angleWithPreviousDir = 2*Math.PI;
		for (Point p : possibleLocations) {
			Point newVector = new Point(p.x-location.x,p.y-location.y);
			double angleDif = diffAngle(angleVector,getAngle(newVector));
			angleDif = Math.min(angleDif, TWO_PI-angleDif);
			if (angleDif < angleMin) {
				angleMin = angleDif;
				newLocation = p;
				angleWithPreviousDir = diffAngle(getAngle(previousDirection),getAngle(newVector));
			} else if(angleDif == angleMin) {
				//in this case, there is two possibilities, for example right and left,
				//we have to choose the good side according to the previous direction.
				if(diffAngle(getAngle(previousDirection),getAngle(newVector)) < angleWithPreviousDir) {
					angleMin = angleDif;
					newLocation = p;
					angleWithPreviousDir = diffAngle(getAngle(previousDirection),getAngle(newVector));
				}
			} 
		}
		return newLocation;
	}
	
	private Point findNextLocation(Point center, Path aPath, Vector<Point> possibleLocations)
	{
		// 1st we try to find a point in the "same direction", ie the angle is close to the current vector (+/- pi/2)
		Point bestPoint = findLocationAccordingToVector(center, vector, previousDir, possibleLocations);
		
		if (initialMove) {
			initialMove = false;
			return bestPoint;
		}
		
		if (bestPoint!= null) 
			return bestPoint;
				
		// We haven't found a point in the "same direction" : we try to find a point that is not in the exact reverse direction than previous one
		// (in order not to make a half turn)
		double angleMin = Math.PI-PEAK_TOLERANCE;
		// 1st number of positions required to compute vector
		int numberOfPositions = (VECTOR_SIZE/SQUARESTEP);
		for (Point p : possibleLocations) {
			Point currentLocation = p;
			Point newVector = new Point(p.x-center.x,p.y-center.y);
			boolean iterationOver = true;
			// iterate to get the new position required to compute the newVector
			for (int i = 0;i<numberOfPositions;i++){
				iterationOver = false;
				Vector<Point> newLocations = lookForPath(currentLocation, aPath);
				if (newLocations == null)
					break;
				currentLocation = findLocationAccordingToVector(currentLocation, newVector, newVector, newLocations);
				if (currentLocation == null)
					break;
				newVector = new Point(currentLocation.x-center.x,currentLocation.y-center.y);
				iterationOver = true;
			}
			if (!iterationOver)
				break;
			double angleDif = diffAngle(getAngle(vector),getAngle(newVector));
			angleDif = Math.min(angleDif, TWO_PI-angleDif);
			if (angleDif < angleMin) {
				angleMin = angleDif;
				bestPoint = p;
			}
		}
		
		return bestPoint;
	}
	
	@Override
	public void travel()
	{
		if ((!pause)&&((goingOn != STOP)||(moves>0)))
		{
			int newX=0;
			int newY=0;
			Point rightLocation = null;
			Point center = getCenterLocation();
			synchronized(destination) {
				SQUARESTEP = Math.max(MIN_STEP, pixelStep);
				
				// 1st in case of initialMove, we locate the object on the path
				if (initialMove) {
					// Initial move: we ensure that the object is on the path
					Point newLocation = getClosestLocation();
					if (newLocation != null) {
						setCenterLocationPrivate(newLocation.x,newLocation.y);
						center = new Point(newLocation);
					} else {
						// We did not find any point on the path: we look for other paths
						boolean pathFound = false;
						Path previousPath = getPath();
						for (Path p:path.getPathList()) {
							setPath(p);
							newLocation = getClosestLocation();
							if (newLocation != null) {
								setCenterLocationPrivate(newLocation.x,newLocation.y);
								center = new Point(newLocation);
								pathFound = true;
							}
						}
						if (!pathFound)
							setPath(previousPath);
					}
				}
				
				// 2nd look for new locations on the path
				Vector<Point> newLocations = lookForPath(center, path);
				
				if (newLocations == null) {
					// We did not find any point on the path: we look for other paths
					for (Path p:path.getPathList()) {
						newLocations = lookForPath(center, p);
						if (newLocations != null) {
							setPath(p);
							break;
						}
					}
					if (newLocations == null) {
						// We could not find any path: we look for the closest position on the screen
						Point newLocation = getClosestLocation();
						if (newLocation == null) {
							// still no path : we alert and stop
							Program.instance().writeMessage(getMessage("error.pathNotFound")); //$NON-NLS-1$
							LOG.error("path not found"); //$NON-NLS-1$
							stop();
						} else {
							setCenterLocationPrivate(newLocation.x,newLocation.y);
							center = new Point(newLocation);
						}
						// We stop here
						return;
					}
				}

				rightLocation = findNextLocation(center,path,newLocations);

				if (rightLocation == null) {
					// no right location from the possible locations found: we look for other paths
					for (Path p:path.getPathList()) {
						newLocations = lookForPath(center, p);
						if (newLocations != null) {
							rightLocation = findNextLocation(center,p,newLocations);
							if (rightLocation != null) {
								// We found a good location
								setPath(p);
								break;
							}
						}
					}
				}

				if (rightLocation == null) {
					// no other location than the one we come from: we stop
					stop();
					return;
				}
								
				// Now we have our rightLocation
				// We move form 'STEP' pixels in this direction
				if (rightLocation.x>center.x) {
					newX = Math.min(center.x+pixelStep, rightLocation.x);
				} else {
					newX = Math.max(center.x-pixelStep, rightLocation.x);
				}
				if (rightLocation.y>center.y) {
					newY = Math.min(center.y+pixelStep, rightLocation.y);
				} else {
					newY = Math.max(center.y-pixelStep, rightLocation.y);
				}
			}
			
			// now that we have our new theoretical location, we test collision
			stopOccured = false;
			testCollision(newX - getObjectWidth()/2,newY - getObjectHeight()/2);
			if (!stopOccured&&!initialMove) {
				// no stop or change of direction during potential collision: we may proceed
				synchronized(destination) {
					// Compute new vector
					// 1st save previous direction
					previousDir.x = vector.x;
					previousDir.y = vector.y;
					// 2nd save current location
					previousLocations[previousLocationIndex] = new Point(center);
					// 3rd number of positions required to compute vector
					int numberOfPositions = (VECTOR_SIZE/SQUARESTEP);
					// 4th retrieve corresponding location
					Point oldPosition = previousLocations[(VECTOR_SIZE+previousLocationIndex-numberOfPositions)%VECTOR_SIZE];
					if (oldPosition == null) {
						// old position not initialized yet: approximate vector from previous position
						vector.x = newX - center.x;
						vector.y = newY - center.y;
					} else {
						// old position found: compute vector
						vector.x = newX - oldPosition.x;
						vector.y = newY - oldPosition.y;
					}

					previousLocationIndex = (previousLocationIndex+1)%VECTOR_SIZE;
					
					// set new location
					setCenterLocationPrivate(newX,newY);
					
					// Deal with moves
					if ((goingOn==STOP)&&(moves > 0)) {
						// little approximation here...
						int diff = Math.max(Math.abs(newX-center.x), Math.abs(newY-center.y));
						moves = Math.max(0, moves-diff);
					}
					// Deal with direction
					double newAngle = getAngle(vector);
					double piDivBy8 = Math.PI/8; 
					if (newAngle<piDivBy8)
						setDirection(RIGHT);
					else if (newAngle < 3*piDivBy8)
						setDirection(UPRIGHT);
					else if (newAngle < 5*piDivBy8)
						setDirection(UP);
					else if (newAngle < 7*piDivBy8)
						setDirection(UPLEFT);
					else if (newAngle < 9*piDivBy8)
						setDirection(LEFT);
					else if (newAngle < 11*piDivBy8)
						setDirection(DOWNLEFT);
					else if (newAngle < 13*piDivBy8)
						setDirection(DOWN);
					else if (newAngle < 15*piDivBy8)
						setDirection(DOWNRIGHT);
					else
						setDirection(RIGHT);
				}
				HashMap<String,Object> info = new HashMap<String,Object>();
				if (testMove) {
					info.put("x", Integer.valueOf(newX)); //$NON-NLS-1$
					info.put("y", Integer.valueOf(newY)); //$NON-NLS-1$
					processEvent("move", info); //$NON-NLS-1$
				}
			}
			if ((goingOn == STOP)&&(moves == 0)) {
				stopOccured = true;
				if (testStop) {
					HashMap<String,Object> info = new HashMap<String,Object>();
					info.put("x", Integer.valueOf(newX)); //$NON-NLS-1$
					info.put("y", Integer.valueOf(newY)); //$NON-NLS-1$
					processEvent("stop", info); //$NON-NLS-1$
				}
			}
			// we test crossing
			testCrossing(center, new Point(newX, newY));
		}
	}

	
	private boolean check()
	{
		if (currentImage==null)
		{
			Program.instance().writeMessage(getMessage("error.noPicture")); //$NON-NLS-1$
			LOG.error("No picture defined"); //$NON-NLS-1$
			return false;
		}
		else if (path==null)
		{
			Program.instance().writeMessage(getMessage("error.noPath")); //$NON-NLS-1$
			LOG.error("No path defined"); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	 
	/**
	 * Tests whether or not there is another path and runs the actions of the specified handler
	 */
	private void testCrossing(Point previous, Point next)
	{	
		if (testCrossing)
		{
			int x1 = previous.x;
			int y1 = previous.y;
			int x2 = next.x;
			int y2 = next.y;
			
			int stepX=0;
			int stepY=0;
			
			if (x1<x2)
				stepX=1;
			else if (x1>x2)
				stepX=-1;

			if (y1<y2)
				stepY=1;
			else if (y1>y2)
				stepY=-1;

			int x=x1;
			int y=y1;
			
			while ((x!=x2)||(y!=y2))
			{
				Component [] components = this.getGraphicsPane().getComponentsAt(x, y);
				for (Component c:components)
				{
					if (c instanceof Path && c != path)
					{
						if (isOnPath(x,y,(Path)c))
						{
							HashMap<String, Object> info = new HashMap<String, Object>();
							info.put("pathCrossed", c); //$NON-NLS-1$
							info.put("pathCrossedName", Program.instance().getObjectName(c)); //$NON-NLS-1$
							info.put("x", Integer.valueOf(x)); //$NON-NLS-1$
							info.put("y", Integer.valueOf(y)); //$NON-NLS-1$
							processEvent("crossing", info); //$NON-NLS-1$
							return;
						}
					}
				}
				if (x!=x2)
					x+=stepX;
				if (y!=y2)
					y+=stepY;
			}
		}
	}
	 
	/**
	 * Returns the position of the center of the rider
	 * @return
	 * 	the center point
	 */
	private Point getCenterLocation()
	{
		Point p = new Point(getObjectX()+ getObjectWidth()/2, getObjectY() + getObjectHeight()/2);
		return p;
	}

	private double getAngle(Point p)
	{
		// -y because when we go up, y decreases.
		return (TWO_PI+Math.atan2(-p.y, p.x))%TWO_PI;
	}
	 
	@Localize(value="Rider.crossing")
    public void ifCrossing(String command)
    {
    	addHandler("crossing",command); //$NON-NLS-1$
    	testCrossing = true;
    }

	@Localize(value="Rider.crossing")
    public void ifCrossing(String command, TWitness witness)
    {
    	addHandler("crossing", command); //$NON-NLS-1$
    	addWitness("crossing", witness); //$NON-NLS-1$
    	testCrossing = true;
    }
	
    /**
     * Enables dragging.
     */
	@Override
	protected void enableDragging()
    {
        if (draggingMouseListener==null)
        {
            draggingMouseListener = new PictureDraggingMouse();
            this.addMouseListener(draggingMouseListener);
        }
        if (draggingMouseMotionListener==null)
        {
            draggingMouseMotionListener = new PictureDraggingMouseMotion();
            this.addMouseMotionListener(draggingMouseMotionListener);
        }
    }

    class PictureDraggingMouseMotion  extends Sprite.PictureDraggingMouseMotion
    {
        @Override
		public void mouseDragged(MouseEvent e)
        {
            int dragNewX = e.getX()+getObjectX();
            int dragNewY = e.getY()+getObjectY();
            int dragShiftX = dragNewX-dragPreviousX;
            int dragShiftY = dragNewY-dragPreviousY;
            if (Math.abs(dragShiftY)>Math.abs(dragShiftX)) 
            {
                if (dragShiftY>0)
                    moveDown(dragShiftY);
                else
                    moveUp(-dragShiftY);
            } 
            else
            {
                if (dragShiftX>0)
                    moveForward(dragShiftX);
                else
                    moveBackward(-dragShiftX);
            }
            /*xCoordinate += dragShiftX;
            yCoordinate += dragShiftY;*/
            dragPreviousX = dragNewX;
            dragPreviousY = dragNewY;
        }
    }

		 
}		 