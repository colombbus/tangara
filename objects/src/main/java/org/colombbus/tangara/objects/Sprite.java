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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Timer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;
import org.colombbus.tangara.TWitness;
import org.colombbus.tangara.objects.sprite.CollisionManager;
import org.colombbus.tangara.objects.sprite.DefaultCollisionManager;
import org.colombbus.tangara.objects.sprite.EllipticalCollisionManager;
import org.colombbus.tangara.objects.sprite.RectangularCollisionManager;
import org.colombbus.tangara.objects.sprite.SpriteMovement;

/**
 * This class permits to create a box with an image in it.
 * @author Benoit
 *
 */
@SuppressWarnings("serial")
@Localize(value="Sprite",localizeParent=true)
public abstract class Sprite extends TGraphicalObject
{
	private static final String[] DEFAULT_EXTENSIONS = {"png","bmp","gif","jpg","jpeg"}; //$NON-NLS-1$
	private static final int SLEEP_TIME = 50;	
	
	private static final java.util.List<Sprite> currentSprites = new ArrayList<Sprite>();

	protected static final int STOP = 0;
	protected static final int UP = 1;
	protected static final int DOWN = 2;
	protected static final int LEFT = 3;
	protected static final int RIGHT = 4;
	protected static final int UPLEFT = 5;
	protected static final int UPRIGHT = 6;
	protected static final int DOWNRIGHT = 7;
	protected static final int DOWNLEFT = 8;

	
	protected static final int MAX_PIXEL_STEP = 30;

	protected int direction = -1;
	protected int goingOn = STOP;

	private final Map<String,String> originalImages = new Hashtable<String,String>();
	private final Map<String,BufferedImage> images = new Hashtable<String,BufferedImage>();
	
	
	protected BufferedImage currentImage;
	private java.util.List<String> imageNames = new ArrayList<String>();
	protected String currentImageName;
	private boolean displayCollisionArea = false;
	private static SpriteMovement movement = new SpriteMovement();
	public Point destination = new Point();
	protected boolean testOutOfScreen = false;
	protected boolean testDirection = false;
	protected boolean testCollision = false;
    protected boolean testCollisionWith = false;
	protected boolean testClick = false;
    protected boolean testStopClick = false;
	protected boolean testImage = false;
	protected boolean testStop = false;
	protected boolean testMove = false;
	protected boolean loopImages = true;
	protected boolean stopOccured = false;
	
	protected CollisionManager collisionManager;
	
	protected boolean wasVisible = true;
	protected boolean pause = false;
	
	protected int pixelStep;
	
	
	protected boolean hide = false;
	
	private int speed;

	public double speedStep;
	public double currentSpeedStep;
	protected final Map<String, String> translatedEvents = new HashMap<String,String>();
	
	private TransparentFilter imageFilter = new TransparentFilter();
	
	protected int xCoordinate,yCoordinate,dragPreviousX,dragPreviousY;
	protected PictureDraggingMouse draggingMouseListener;
	protected PictureDraggingMouseMotion draggingMouseMotionListener;

	private boolean overlapProcessing = false;
	private java.util.List<Sprite> overlappedObjects = new ArrayList<Sprite>();
	
	private final Map<Sprite,String> specificCollisionEvents = new HashMap<Sprite,String>();

	protected WitnessCollision collisionWitness;
	protected WitnessDirectionChange directionWitness;
	
	static {
		 Timer t = new Timer(SLEEP_TIME, movement);
		 t.start();
	}
	
	/**
	 * Instanciates an instance of this class.
	 */
	@Localize(value="Sprite")
	public Sprite()
	{
    	super();
    	setSpeed(50);
        setSize(50,50);
		setBorder(BorderFactory.createEmptyBorder());
		setOpaque(false); // in order to handle transparent images.

		// by default
		direction = UP;
		
		collisionManager = new DefaultCollisionManager(this);

		registerEvent("collision"); //$NON-NLS-1$
		registerEvent("imageChange"); //$NON-NLS-1$
		registerEvent("stop"); //$NON-NLS-1$
		registerEvent("move"); //$NON-NLS-1$
		registerEvent("click"); //$NON-NLS-1$
		registerEvent("stopClick"); //$NON-NLS-1$
        registerEvent("outOfScreen"); //$NON-NLS-1$
		registerEvent("directionChange"); //$NON-NLS-1$
		
		translatedEvents.put(getMessage("collision"),"collision"); //$NON-NLS-1$ //$NON-NLS-2$
		translatedEvents.put(getMessage("directionChange"),"directionChange"); //$NON-NLS-1$ //$NON-NLS-2$
		translatedEvents.put(getMessage("imageChange"),"imageChange"); //$NON-NLS-1$ //$NON-NLS-2$
		translatedEvents.put(getMessage("move"),"move"); //$NON-NLS-1$ //$NON-NLS-2$
		translatedEvents.put(getMessage("click"),"click"); //$NON-NLS-1$ //$NON-NLS-2$
        translatedEvents.put(getMessage("stopClick"),"stopClick"); //$NON-NLS-1$ //$NON-NLS-2$
		translatedEvents.put(getMessage("outOfScreen"),"outOfScreen"); //$NON-NLS-1$ //$NON-NLS-2$
		translatedEvents.put(getMessage("stop"),"stop"); //$NON-NLS-1$ //$NON-NLS-2$
		
		addMouseListener(new MouseAdapter()
    	{
    		@Override
			public void mousePressed(MouseEvent e)
    		{
    			if (testClick)
    			{
	    			HashMap<String,Object> info = new HashMap<String,Object>();
	    			info.put("x", Integer.valueOf(e.getX()+shiftX)); //$NON-NLS-1$
	    			info.put("y", Integer.valueOf(e.getY()+shiftY)); //$NON-NLS-1$
	    			processEvent("click", info); //$NON-NLS-1$
    			}
    		}
    		
    		@Override
			public void mouseReleased(MouseEvent e)
    		{
                if (testStopClick)
                {
                    HashMap<String,Object> info = new HashMap<String,Object>();
                    info.put("x", Integer.valueOf(e.getX()+shiftX)); //$NON-NLS-1$
                    info.put("y", Integer.valueOf(e.getY()+shiftY)); //$NON-NLS-1$
                    processEvent("stopClick", info); //$NON-NLS-1$
                }
    		}
   		});
		synchronized (currentSprites)
		{
			currentSprites.add(this);
		}
		collisionWitness = new WitnessCollision();
		directionWitness = new WitnessDirectionChange();
    	addWitness("collision", collisionWitness); //$NON-NLS-1$
    	addWitness("directionChange", directionWitness); //$NON-NLS-1$
    	displayObject();
	}	
	
	
	/**
	 * Instanciates a new instance of this class
	 */
	@Localize(value="Sprite")
	public Sprite(String fileName)
	{
		this();
		addImage(fileName); //$NON-NLS-1$
		displayImage(fileName); //$NON-NLS-1$
	}
	
	/**
	 * Adds an image to the image list.
	 * @param imageName
	 * @param path
	 */
    @Localize(value="Sprite.addImage")
	public void addImage(String path, String imageName)
	{
		try
		{
			BufferedImage newImage = loadFile(path);
			originalImages.put(imageName, path);
			filterAndAddImage(imageName, newImage);
			if (!imageNames.contains(imageName))
			    imageNames.add(imageName);
		}
		catch (Exception e)
		{
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", path); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Program.instance().writeMessage(message);
		}
	}


	/**
	 * Adds an image to the image list.
	 * @param imageName
	 * @param path
	 */
    @Localize(value="Sprite.addImage3")
	public void addImage(String path)
	{
    	// User path as image name
    	addImage(path, path);
	}

    
    /**
	 * Sets the current image.
	 * @param path
	 */
	@Localize(value="Sprite.addImage2")
	public void addImage(BufferedImage anImage, String imageName)
	{
		BufferedImage copy = new BufferedImage(anImage.getWidth(),anImage.getHeight(),BufferedImage.TYPE_INT_ARGB);
		copy.getGraphics().drawImage(anImage, 0, 0, null);
		filterAndAddImage(imageName, copy);
        if (!imageNames.contains(imageName))
            imageNames.add(imageName);
	}
		
	
	/**
     * Hides the component     
     */
    @Override
	@Localize(value="Sprite.hide")
    public void hide()
    {
    	hide = true;
    	repaint();
    }
    
    /**
     * Shows the component
     */
    @Localize(value="Sprite.display")
    public void display()
    {	    		    
    	hide = false;
    	repaint();
    }
    	    
	 		
	
	/**
	 * Enables to move the object to getX()+diffx and getY()+diffy 
	 * @param diffx
	 * @param diffy
	 */
	@Localize(value="Sprite.moveTo")
	public void moveTo(int x, int y)
	{
		int newDirection;
		synchronized(destination)
		{
			goingOn = STOP;
			destination = new Point(x, y);
			pause = false;
			Point current  = getObjectLocation();
			if (destination.x>current.x)
			{
				if (destination.y>current.y)
					newDirection = DOWNRIGHT;
				else if (destination.y<current.y)
					newDirection = UPRIGHT;
				else
					newDirection = RIGHT;
			}
			else
			{
				if (destination.y>current.y)
					newDirection = DOWNLEFT;
				else if (destination.y<current.y)
					newDirection = UPLEFT;
				else
					newDirection = LEFT;
			}
		}
		int oldDirection = direction;
		direction = newDirection;
		eventDirection(oldDirection, newDirection);
	}

	/**
	 * Displays an image if it exists in the image list.
	 * @param imageName
	 */
	@Localize(value="Sprite.displayImage")
	public void displayImage(String imageName)
	{
		synchronized(images)
		{
			if (images.containsKey(imageName))
			{			
				currentImage = images.get(imageName);
				boolean test  = imageName.equals(currentImageName);
				String oldImageName ="";  //$NON-NLS-1$
				if (currentImageName != null)
					oldImageName = currentImageName;
				currentImageName = imageName;
				hide = false;
				repaint();
				if (!test)
				{
					setObjectWidth(currentImage.getWidth());
				 	setObjectHeight(currentImage.getHeight());
					if (testImage)
					{
					 	HashMap<String,Object> info = new HashMap<String,Object>();
						info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
						info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
						info.put("oldImageName", oldImageName); //$NON-NLS-1$
						info.put("newImageName", currentImageName); //$NON-NLS-1$
						processEvent("imageChange", info); //$NON-NLS-1$
					}
				}
			}
			else
			{
	            String message = MessageFormat.format(getMessage("display.error"), imageName); //$NON-NLS-1$
	            Program.instance().writeMessage(message);
			}
		}
	}	
	
	/**
	 * Displays the next image in the image list. If there is no more image, jump to the first image in the list
	 */
	@Localize(value="Sprite.displayNextImage")
	public void displayNextImage()
	{
		synchronized(images)
		{
			if (images.size()>0)
			{
				if (currentImage!=null)
				{
					int newIndex = imageNames.lastIndexOf(currentImageName)+1;
					if (newIndex<images.size())
						displayImage(imageNames.get(newIndex));
					else if (loopImages)
						// we are at the last image : jump to the first one
						displayImage(imageNames.get(0));
				}
				else
				{
					// no image is currently displayed
					displayImage(imageNames.get(0));
				}
			}
			else
			{
				// no image to display
		        Program.instance().writeMessage(getMessage("displayNext.error")); //$NON-NLS-1$
			}
		}
	}
	
	   /**
     * Displays the previous image in the image list. If the current image is the first one, jump to the last image in the list
     */
    @Localize(value="Sprite.displayPreviousImage")
    public void displayPreviousImage()
    {
        synchronized(images)
        {
            if (images.size()>0)
            {
                if (currentImage!=null)
                {
                    int newIndex = imageNames.lastIndexOf(currentImageName)-1;
                    if (newIndex>=0)
                        displayImage(imageNames.get(newIndex));
                    else if (loopImages)
                        // we are at the first image : jump to the last one
                        displayImage(imageNames.get(imageNames.size()-1));
                }
                else
                {
                    // no image is currently displayed
                    displayImage(imageNames.get(0));
                }
            }
            else
            {
                // no image to display
                Program.instance().writeMessage(getMessage("displayPrevious.error")); //$NON-NLS-1$
            }
        }
    }
	
	/**
	 * Removes an image from the image list.
	 * @param imageName
	 */
	@Localize(value="Sprite.removeImage")
	public void removeImage(String imageName)
	{
		synchronized(images)
		{
			if (images.containsKey(imageName))
			{
				if (currentImage == images.get(imageName))
				{
					images.remove(imageName);
					imageNames.remove(imageName);
					currentImage = null;
					currentImageName = null;		
					repaint();				
				}
				else
				{
					images.remove(imageName);
					imageNames.remove(imageName);
				}
			}
			else
			{
	            String message = MessageFormat.format(getMessage("remove.error"), imageName); //$NON-NLS-1$
	            Program.instance().writeMessage(message);
			}
		}
	}

	/**
	 * Returns the current image name
	 * @return
	 * 		a String that corresponds to the current image name
	 */	
	@Localize(value="Sprite.getCurrentImageName")
	public String getCurrentImageName()
	{
		return currentImageName;
	}

	/**
	 * Draws the current image if there is one.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		if (!hide)
		{
			super.paintComponent(g);
			if (currentImage!=null)
			{
				g.drawImage(currentImage, 0, 0, null);
			}
			if (displayCollisionArea)
			{
				collisionManager.drawCollisionArea(g);
			}
		}
	}
	
	public void paintComponent(Graphics g, int x, int y)
	{
		if (!hide)
		{
			super.paintComponent(g);
			if (currentImage!=null)
			{
				g.drawImage(currentImage, x, y, null);
			}
			if (displayCollisionArea)
			{
				collisionManager.drawCollisionArea(g);
			}
		}
	}
	
	/**
	 * Sets the current speed
	 * @param value
	 * 		the new speed
	 */
	@Localize(value="Sprite.setSpeed")
	public void setSpeed(int value)
	{		
		if (value<=0)
			stop();
		else
		{
			if (value<=100)			
				speed = value;
			else if (value>100)
			{
				speed = 100;
				Program.instance().writeMessage(getMessage("Error.maxSpeed")); //$NON-NLS-1$
			}								
			speedStep = 0.05 + (speed-1)*(MAX_PIXEL_STEP-0.05)/99;	
			currentSpeedStep = speedStep;
		}
		pixelStep = Math.max(1, (int)speedStep);
	}
	
	
	/**
	 * Returns the current speed
	 * @return
	 * 		the current speed
	 */
	public int getSpeed()
	{
		return speed;
	}
	
	/**
	 * Enables to display the margins
	 * @param value
	 * 		whether or not display the margins
	 */
	// Just for backward compatibility
	@Localize(value="Sprite.displayMargins")
	public void displayMargins(boolean value)
	{
		displayCollisionArea = value;
		repaint();
	}

	/**
	 * Enables to display the margins
	 * @param value
	 * 		whether or not display the margins
	 */
	@Localize(value="Sprite.displayCollisionArea")
	public void displayCollisionArea(boolean value)
	{
		displayCollisionArea = value;
		repaint();
	}

	/**
	 * Makes a color transparent.
	 * @param colorName
	 * @throws Exception 
	 */
    @Localize(value="Sprite.transparentColor")
    public void transparentColor(String colorName) throws Exception
    {
    	Color c = TColor.translateColor(colorName, Color.black);
    	imageFilter.setTransparentColor(c);
    	updateImages();
    }

    /**
     * Creates a BufferedImage from a file.
     * @param fileName
     * @return
     * @throws Exception
     */
	protected BufferedImage loadFile(String fileName) throws Exception
    {
		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
    	if (file == null)
    		throw new Exception("file not found"); //$NON-NLS-1$
		BufferedImage newImage = ImageIO.read(file);
		return newImage;
    }
	
	/**
     * Sets the dragging status.
     * @param value
     */
    @Localize(value="Sprite.followMouse")
    public void followMouse(boolean value)
    {
		if (value)
			enableDragging();
		else
    		disableDragging();
    }
	
    /**
     * Enables dragging.
     */
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

    /**
     * Disables dragging.
     */
    protected void disableDragging()
    {
    	if (draggingMouseListener !=null)
    	{
    		this.removeMouseListener(draggingMouseListener);
    		draggingMouseListener = null;
    	}
    	if (draggingMouseMotionListener !=null)
    	{
    		this.removeMouseMotionListener(draggingMouseMotionListener);
    		draggingMouseMotionListener = null;
    	}
    }
    
    class PictureDraggingMouse extends MouseAdapter
    {
		@Override
		public void mousePressed(MouseEvent e)
		{
			xCoordinate = getX();
			yCoordinate = getY();
			dragPreviousX = e.getX()+xCoordinate;
			dragPreviousY = e.getY()+yCoordinate;
		}
    }

    class PictureDraggingMouseMotion  extends MouseMotionAdapter
    {
		@Override
		public void mouseDragged(MouseEvent e)
		{
			int dragNewX = e.getX()+xCoordinate;
			int dragNewY = e.getY()+yCoordinate;
			xCoordinate += dragNewX-dragPreviousX;
			yCoordinate += dragNewY-dragPreviousY;
           	setObjectLocation(xCoordinate+shiftX,yCoordinate+shiftY);
            dragPreviousX = dragNewX;
            dragPreviousY = dragNewY;
		}
    }
	
	/**
	 * Moves the object to the given point.
	 * @param value
	 */
	public void moveSprite(Dimension value)
	{
		moveTo(destination.x+value.width,destination.y+value.height);
	}
	

	public void travel()
	{
		if (!pause)
		{
			Point current = getObjectLocation();
			int newX, newY;
			if (goingOn!=STOP)
			{			
				synchronized(destination)
				{
					newX = current.x;
					newY = current.y;
					switch (goingOn)
					{
						case RIGHT:
							newX+=pixelStep;
							break;
						case LEFT:
							newX-=pixelStep;
							break;
						case UP:
							newY-=pixelStep;
							break;
						case DOWN:
							newY+=pixelStep;
							break;
					}
				}
				// now that we have a potential new position, we check for collisions
				stopOccured = false;
				testCollision(newX,newY);
				if (!stopOccured)
				{
					synchronized(destination)
					{
						setLocationPrivate(newX, newY);
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
			else if (!current.equals(destination))
			{						
				Point newPosition;
				synchronized(destination)
				{
					newPosition = new Point(current);
					if (current.x<destination.x)
					{
						int min = Math.min(newPosition.x+pixelStep, destination.x);
						newPosition.x = min;
					}
					else if (current.x>destination.x)
					{
						int max = Math.max(newPosition.x-pixelStep, destination.x);
						newPosition.x = max;
					}				
					if (current.y<destination.y)
					{
						int min = Math.min(newPosition.y+pixelStep, destination.y);
						newPosition.y = min;
					}				
					else if (current.y>destination.y)
					{
						int max = Math.max(newPosition.y-pixelStep, destination.y);
						newPosition.y = max;
					}
				}
				stopOccured = false;
				testCollision(newPosition.x,newPosition.y);
				if (!stopOccured)
				{
					synchronized(destination)
					{
						setLocationPrivate(newPosition.x, newPosition.y);
					}
					HashMap<String,Object> info = new HashMap<String,Object>();
					if (testMove)
					{
						info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
						info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
						processEvent("move", info); //$NON-NLS-1$
					}
					if (newPosition.equals(destination))
					{
						stopOccured = true;
						if (testStop)
							processEvent("stop", info);  //$NON-NLS-1$
					}
				}
			}
		}
	}  
	
	@Override
	@Localize(value="common.moveForward")
	public void moveForward(int value)
	{
		moveSprite(new Dimension(value, 0));
		if (direction!=RIGHT)
		{
			int oldDirection = direction;
			direction = RIGHT;
			eventDirection(oldDirection, RIGHT);
		}
		else
			direction = RIGHT;
	}
	
	@Localize(value="Sprite.alwaysMoveForward")
	public void alwaysMoveForward()
	{
		synchronized(destination) 
		{
			goingOn = RIGHT;
		}
		pause = false;
		if (direction!=RIGHT)
		{
			int oldDirection = direction;
			direction = RIGHT;
			eventDirection(oldDirection, RIGHT);
		}
		else
			direction = RIGHT;

	}

	@Override
	@Localize(value="common.moveBackward")
	public void moveBackward(int value)
	{
		moveSprite(new Dimension(-value,0));
		if (direction!=LEFT)
		{
			int oldDirection = direction;
			direction = LEFT;
			eventDirection(oldDirection, LEFT);
		}
		else
			direction = LEFT;
	}
	
	@Localize(value="Sprite.alwaysMoveBackward")
	public void alwaysMoveBackward()
	{
		synchronized(destination) 
		{
			goingOn = LEFT;
		}
		pause = false;
		if (direction!=LEFT)
		{
			int oldDirection = direction;
			direction = LEFT;
			eventDirection(oldDirection, LEFT);
		}
		else
			direction = LEFT;
	}

	@Override
	@Localize(value="common.moveUp")
	public void moveUp(int value)
	{
		moveSprite(new Dimension(0,-value));
		if (direction!=UP)
		{
			int oldDirection = direction;
			direction = UP;
			eventDirection(oldDirection, UP);
		}
		else
			direction = UP;
	}
	
	@Localize(value="Sprite.alwaysMoveUp")
	public void alwaysMoveUp()
	{
		synchronized(destination) 
		{
			goingOn = UP;
		}
		pause = false;
		if (direction!=UP)
		{
			int oldDirection = direction;
			direction = UP;
			eventDirection(oldDirection, UP);
		}
		else
			direction = UP;
	}

	@Override
	@Localize(value="common.moveDown")
	public void moveDown(int value)
	{
		moveSprite(new Dimension(0, value));
		if (direction!=DOWN)
		{
			int oldDirection = direction;
			direction = DOWN;
			eventDirection(oldDirection, DOWN);
		}
		else
			direction = DOWN;
	}

	@Localize(value="Sprite.alwaysMoveDown")
	public void alwaysMoveDown()
	{
		synchronized(destination) 
		{
			goingOn = DOWN;
		}
		pause = false;
		if (direction!=DOWN)
		{
			int oldDirection = direction;
			direction = DOWN;
			eventDirection(oldDirection, DOWN);
		}
		else
			direction = DOWN;
	}
	
	@Localize(value="Sprite.stop")
	public void stop()
	{
		synchronized(destination)
		{
			goingOn = STOP;
			destination.x = getObjectLocation().x;
			destination.y = getObjectLocation().y;
			collisionManager.init();
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
	
	@Override
	@Localize(value="common.setObjectWidth")
	public void setObjectWidth(int value)
	{
		super.setObjectWidth(value);
	}

	@Override
	@Localize(value="common.setObjectHeight")
	public void setObjectHeight(int value)
	{
		super.setObjectHeight(value);
	}

	/**
	 * Returns the current direction in the spoken language
	 * @return
	 * 	a string
	 */
	@Localize(value="Rider.getDirection")
	public String getDirection()
	{
		return translateDirection(direction);
	}

	protected String translateDirection(int value)
	{
		switch (value)
		{
			case LEFT:
				return getMessage("left"); //$NON-NLS-1$
			case RIGHT:
				return getMessage("right"); //$NON-NLS-1$
			case UP:
				return getMessage("up"); //$NON-NLS-1$
			case DOWN:
				return getMessage("down"); //$NON-NLS-1$
			case UPLEFT:
				return getMessage("upleft"); //$NON-NLS-1$
			case UPRIGHT:
				return getMessage("upright"); //$NON-NLS-1$
			case DOWNLEFT:
				return getMessage("downleft"); //$NON-NLS-1$
			case DOWNRIGHT:
				return getMessage("downright"); //$NON-NLS-1$
		}
		return ""; // should never happen! //$NON-NLS-1$
	}
	
	private void eventDirection(int oldDirection, int newDirection)
	{
		if (testDirection)
		{
			HashMap<String,Object> info = new HashMap<String,Object>();
			info.put("oldDirection", translateDirection(oldDirection)); //$NON-NLS-1$
			info.put("newDirection", translateDirection(newDirection)); //$NON-NLS-1$
			info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
			info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
			processEvent("directionChange",info); //$NON-NLS-1$
		}
	}
	
	/**
	 * Sets the object location.
	 */
	@Override
	@Localize(value="common.setObjectLocation1")
	public void setObjectLocation(int x, int y)
	{	
		boolean wasMoving = isMoving();
		synchronized(destination)
		{
			destination.x = x;
			destination.y = y;
			
		}
		setLocationPrivate(x,y);
		if (wasMoving)
			stop();
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
		boolean wasMoving = isMoving();
		synchronized(destination)
		{
			destination.x = (int) x;
			destination.y = (int) y;
		}
		setLocationPrivate((int) x, (int) y);
		if (wasMoving)
			stop();
	}
	
	@Override
	@Localize(value="common.setObjectXCoordinate")
	public void setObjectXCoordinate(int x)
	{
		setObjectLocation(x, getLocation().y);
	}
	
	@Override
	@Localize(value="common.setObjectXCoordinate")
	public void setObjectXCoordinate(double x)
	{
		setObjectLocation((int)x, getLocation().y);
	}
	
	@Override
	@Localize(value="common.setObjectYCoordinate")
	public void setObjectYCoordinate(int y)
	{
		setObjectLocation(getLocation().x, y);
	}
	
	@Override
	@Localize(value="common.setObjectYCoordinate")
	public void setObjectYCoordinate(double y)
	{
		setObjectLocation(getLocation().x, y);
	}
	
	protected void setLocationPrivate(int x, int y)
	{
		super.setObjectLocation(x, y);
		// Test if the sprite is out of the screen
		if (testOutOfScreen)
		{
			if ((x+getObjectWidth()<0)||(x>this.getGraphicsPane().getWidth())||(y>this.getGraphicsPane().getHeight())||(y+getObjectHeight()<0))
			{
				if (wasVisible)
				{
					wasVisible = false;
					if (testOutOfScreen)
					{
						HashMap<String,Object> info = new HashMap<String,Object>();
						info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
						info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
						processEvent("outOfScreen", info); //$NON-NLS-1$
					}
				}
			}
			else
				wasVisible = true;
		}
		collisionManager.updateLocation(x, y);
	}
	
	@Localize(value="Sprite.getXCoordinate")
	public int getXCoordinate()
	{
		return getObjectX();
	}
	
	@Localize(value="Sprite.getYCoordinate")
    public int getYCoordinate()
    {
    	return getObjectY();
    }		
	
	/*@Localize(value="Sprite.getCoordinates")
	public Point getCoordinates()
	{
		return new Point(getObjectX(), getObjectY());
	}*/
	
	/**
	 * This method enables to test if the margins enter in collision with another Sprite object
	 * @param xdiff
	 * 		the horizontal movement
	 * @param ydiff
	 * 		the vertical movement
	 */
	protected void testCollision(int x, int y)
	{
		collisionManager.testCollision(x, y, currentSprites, false);
	}

	/**
	 * Enables to freeze the object
	 */
	@Override
	public void freeze(boolean value)
	{	
		if (value)
		{						
			movement.removeSprite(this);			
		}
		else
		{			
			movement.addSprite(this);
		}
	}
	
	/**
	 * Deletes the object.
	 */
	@Override
	public void deleteObject()
	{
		pause = true;
		stopOccured = true;
		movement.removeSprite(this);
		synchronized(currentSprites)
		{
			currentSprites.remove(this);
		}
		super.deleteObject();
	}
    
	
	@Localize(value="Sprite.loopImages")
	public void loopImages(boolean value) {
		loopImages = value;
	}
	
    class TransparentFilter extends RGBImageFilter
    {
    	private long filter;
    	private boolean transparentColor;
    	private Color transparent;

        public TransparentFilter()
        {
        	filter = 0xffffffff;
        	transparentColor = false;
        }

        public void setTransparency(int value)
        {
        	filter = (value<<24 | 0xffffff);
        }
        
        public void setTransparentColor(Color target)
        {
        	transparentColor = true;
        	transparent = new Color(target.getRGB());
        }

    	@Override
		public int filterRGB(int x, int y, int rgb)
        {
    		if (transparentColor)
    		{
    			Color c = new Color(rgb);
				if (TColor.testCloseColor(transparent, c))
				{
					return (rgb & 0x00ffffff);
				}
				else
				{
					return (int)(rgb & filter);
				}
    		}
    		else
			{
				return (int)(rgb & filter);
			}
        }
    	
    }
    
	@Localize(value="Sprite.pause")
    public void pause()
    {
    	pause = true;
        collisionManager.init();
        stopOccured = true;
		if (testStop)
		{
	    	HashMap<String,Object> info = new HashMap<String,Object>();
			info.put("x", Integer.valueOf(getObjectX())); //$NON-NLS-1$
			info.put("y", Integer.valueOf(getObjectY())); //$NON-NLS-1$
			processEvent("stop", info); //$NON-NLS-1$
		}
    }

	@Localize(value="Sprite.resume")
    public void resume()
    {
    	pause = false;
    }
    
	@Localize(value="Sprite.ifCollision")
    public void ifCollision(String command)
    {
    	addHandler("collision",command); //$NON-NLS-1$
    	testCollision = true;
    }
    
	@Localize(value="Sprite.ifCollision2")
    public void ifCollision(String command, TWitness witness)
    {
    	addHandler("collision", command); //$NON-NLS-1$
    	addWitness("collision", witness); //$NON-NLS-1$
    	testCollision = true;
    }
    
	
    @Localize(value="Sprite.ifCollisionWith")
	public void ifCollisionWith(Sprite obj, String command)
	{
	    String eventCode;
	    if (specificCollisionEvents.containsKey(obj))
	    {
	        eventCode = specificCollisionEvents.get(obj);
	        addHandler(eventCode, command);
	    }
	    else
	    {
	        eventCode = "collision_"+UUID.randomUUID(); //$NON-NLS-1$
	        specificCollisionEvents.put(obj,eventCode);
	        registerEvent(eventCode);
            addHandler(eventCode, command);
            // add my own witness to the event
            addWitness(eventCode, collisionWitness);
	    }
	    testCollisionWith = true;
	}
	
    @Localize(value="Sprite.ifCollisionWith2")
	public void ifCollisionWith(Sprite obj, String command, TWitness witness)
	{
        String eventCode;
        if (specificCollisionEvents.containsKey(obj))
        {
            eventCode = specificCollisionEvents.get(obj);
            addHandler(eventCode, command);
            addWitness(eventCode, witness);
        }
        else
        {
            eventCode = "collision_"+UUID.randomUUID(); //$NON-NLS-1$
            specificCollisionEvents.put(obj,eventCode);
            registerEvent(eventCode);
            addHandler(eventCode, command);
            addWitness(eventCode, witness);
            // add my own witness to the event
            addWitness(eventCode, collisionWitness);
        }
        testCollisionWith = true;
    }
    
    @Localize(value="Sprite.ifCollisionWith3")
    public void ifCollisionWith(List<Sprite> list, String command) {
    	for (Sprite s:list) {
    		ifCollisionWith(s, command);
    	}
    }

    @Localize(value="Sprite.ifCollisionWith4")
    public void ifCollisionWith(List<Sprite> list, String command, TWitness witness) {
    	for (Sprite s:list) {
    		ifCollisionWith(s, command, witness);
    	}
    }
    
	@Localize(value="Sprite.ifImageChange")
	public void ifImageChange(String command)
    {
    	addHandler("imageChange",command); //$NON-NLS-1$
    	testImage = true;
    }

	@Localize(value="Sprite.ifImageChange2")
    public void ifImageChange(String command, TWitness witness)
    {
    	addHandler("imageChange", command); //$NON-NLS-1$
    	addWitness("imageChange", witness); //$NON-NLS-1$
    	testImage = true;
    }

	@Localize(value="Sprite.ifStop")
    public void ifStop(String command)
    {
    	addHandler("stop",command); //$NON-NLS-1$
    	testStop = true;
    }

	@Localize(value="Sprite.ifStop2")
    public void ifStop(String command, TWitness witness)
    {
    	addHandler("stop", command); //$NON-NLS-1$
    	addWitness("stop", witness); //$NON-NLS-1$
    	testStop = true;
    }  

	@Localize(value="Sprite.ifClick")
    public void ifClick(String command)
    {
    	addHandler("click",command); //$NON-NLS-1$
    	testClick = true;
    }

	@Localize(value="Sprite.ifClick2")
    public void ifClick(String command, TWitness witness)
    {
    	addHandler("click", command); //$NON-NLS-1$
    	addWitness("click", witness); //$NON-NLS-1$
    	testClick = true;
    }  

	@Localize(value="Sprite.ifStopClick")
    public void ifStopClick(String command)
    {
        addHandler("stopClick",command); //$NON-NLS-1$
        testStopClick = true;
    }
    
    @Localize(value="Sprite.ifStopClick2")
    public void ifStopClick(String command, TWitness witness)
    {
        addHandler("stopClick", command); //$NON-NLS-1$
        addWitness("stopClick", witness); //$NON-NLS-1$
        testStopClick = true;
    }  

	
	@Localize(value="Sprite.ifOutOfScreen")
    public void ifOutOfScreen(String command)
    {
    	addHandler("outOfScreen",command); //$NON-NLS-1$
    	testOutOfScreen = true;
    }

	@Localize(value="Sprite.ifOutOfScreen2")
    public void ifOutOfScreen(String command, TWitness witness)
    {
    	addHandler("outOfScreen", command); //$NON-NLS-1$
    	addWitness("outOfScreen", witness); //$NON-NLS-1$
    	testOutOfScreen = true;
    }
	
	@Localize(value="Sprite.ifMove")
    public void ifMove(String command)
    {
    	addHandler("move",command); //$NON-NLS-1$
    	testMove = true;
    }

	@Localize(value="Sprite.ifMove2")
    public void ifMove(String command, TWitness witness)
    {
    	addHandler("move", command); //$NON-NLS-1$
    	addWitness("move", witness); //$NON-NLS-1$
    	testMove = true;
    }
	
	@Localize(value="Sprite.ifDirectionChange")
    public void ifDirectionChange(String command)
    {
    	addHandler("directionChange",command); //$NON-NLS-1$
    	testDirection = true;
    }

	@Localize(value="Sprite.ifDirectionChange2")
    public void ifDirectionChange(String command, TWitness witness)
    {
    	addHandler("directionChange", command); //$NON-NLS-1$
    	addWitness("directionChange", witness); //$NON-NLS-1$
    	testDirection = true;
    }  

	@Localize(value="Sprite.displayCommands")
    public void displayCommands(boolean value)
    {
		displayEvents = value;
    }  
	
	// TODO: find a better way
	@Localize(value="Sprite.resetEvent")
	public void resetEvent(String name)
	{
		if (!translatedEvents.containsKey(name))
		{
			Program.instance().writeMessage(MessageFormat.format(getMessage("resetEvent.error"), name)); //$NON-NLS-1$
		}
		else
		{
			String eventName = translatedEvents.get(name);
		    clearEvent(eventName);
			if (eventName.equals("directionChange")){
				testDirection = false;
		    	addWitness("directionChange", directionWitness); //$NON-NLS-1$
			}
			else if (eventName.equals("outOfScreen")) //$NON-NLS-1$
				testOutOfScreen = false;
			else if (eventName.equals("collision")) //$NON-NLS-1$
			{
				testCollision = false;
				testCollisionWith = false;
				Iterator<String> values = specificCollisionEvents.values().iterator();
				while (values.hasNext())
				{
				    String eventCode = values.next();
				    clearEvent(eventCode);
				}
				specificCollisionEvents.clear();
				// and finally add my own witness to collision events
	            addWitness("collision", collisionWitness);
			}
			else if (eventName.equals("move")) //$NON-NLS-1$
				testMove = false;
			else if (eventName.equals("click")) //$NON-NLS-1$
				testClick = false;
			else if (eventName.equals("stop")) //$NON-NLS-1$
				testStop = false;
			else if (eventName.equals("imageChange")) //$NON-NLS-1$
				testImage = false;
		}
	}
	
	@Localize(value="Sprite.setCollisionRectangle")
    public void setCollisionRectangle(int x, int y, int width, int height)
	{
		collisionManager = new RectangularCollisionManager(x,y,width,height,this);
		collisionManager.updateLocation(getObjectX(), getObjectY());
		repaint();
	}

	@Localize(value="Sprite.setCollisionEllipse")
    public void setCollisionEllipse(int x, int y, int width, int height)
	{
		collisionManager = new EllipticalCollisionManager(x,y,width,height,this);
		collisionManager.updateLocation(getObjectX(), getObjectY());
		repaint();
	}
	
	public boolean intersects(Rectangle r)
	{
		return collisionManager.testIntersection(r);
	}

	public boolean intersects(Area a)
	{
		return collisionManager.testIntersection(a);
	}

	// For backward compatibility only
	@Localize(value="Sprite.setMargins")
	public void setMargins(int valueUp, int valueDown, int valueLeft, int valueRight)
	{
		setCollisionRectangle(valueLeft,valueUp,getObjectWidth()-valueLeft-valueRight,getObjectHeight()-valueUp-valueDown);
	}
	
	public void processCollision(int x, int y, Sprite s)
	{
		if (overlapProcessing)
		{
			overlappedObjects.add(s);
		}
		else
		{
		    if (testCollisionWith)
		    {
		        if (specificCollisionEvents.containsKey(s))
		        {
	                HashMap <String,Object> info = new HashMap<String,Object>();                            
                    info.put("x", Integer.valueOf(x)); //$NON-NLS-1$
                    info.put("y", Integer.valueOf(y)); //$NON-NLS-1$
                    info.put("lastCollision", s); //$NON-NLS-1$
                    info.put("lastCollisionName", Program.instance().getObjectName(s)); //$NON-NLS-1$
                    processEvent(specificCollisionEvents.get(s),info);
		        }
		    }
			if (testCollision)
			{
				HashMap <String,Object> info = new HashMap<String,Object>();							
				info.put("x", Integer.valueOf(x)); //$NON-NLS-1$
				info.put("y", Integer.valueOf(y)); //$NON-NLS-1$
				info.put("lastCollision", s); //$NON-NLS-1$
                info.put("lastCollisionName", Program.instance().getObjectName(s)); //$NON-NLS-1$
				processEvent("collision",info); //$NON-NLS-1$
			}
		}
	}
	
	public int getCollisionAreaTop()
	{
		return collisionManager.getAreaTop();
	}

	@Localize(value="Sprite.setTransparency")
	public void setTransparency(int coef) throws Exception
	{
		if ((coef<0)||(coef>100))
		{
            Program.instance().writeMessage(getMessage("transparency.error")); //$NON-NLS-1$
			return;
		}
		imageFilter.setTransparency((255-(coef*255/100)));
		updateImages();
	}
	
	protected void filterAndAddImage(String key, BufferedImage bufferedImage)
	{
    	int width;
    	int height;
		Image img2 =Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(bufferedImage.getSource(),imageFilter));
		width = bufferedImage.getWidth();
		height = bufferedImage.getHeight();
	    BufferedImage img3 = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
		img3.getGraphics().drawImage(img2,0,0,null);
		images.put(key, img3);		
    	if ((currentImageName!=null)&&(images.containsKey(currentImageName)))
    	{
    		currentImage = images.get(currentImageName);
    	}
		repaint();
	}
	
	protected void updateImages() throws Exception {
		Iterator<Entry<String, String>> set = originalImages.entrySet().iterator();
		images.clear();
		while (set.hasNext())
		{
			Entry<String, String> entry = set.next();
			BufferedImage newImage = loadFile(entry.getValue());
			filterAndAddImage(entry.getKey(), newImage);
		}
	}
	
	@Localize(value="Sprite.getOverlappedObjects")
	public void getOverlappedObjects(org.colombbus.tangara.objects.List list)
	{
		overlapProcessing = true;
		overlappedObjects = new ArrayList<Sprite>();
		collisionManager.testCollision(getObjectX(), getObjectY(), currentSprites, true);
		overlapProcessing = false;
		list.clear();
		Iterator<Sprite> it = overlappedObjects.iterator();
		while (it.hasNext())
		{
			list.add(it.next());
		}
	}
	
	public void step()
	{
        if (currentSpeedStep>=1.0)
        {
            travel();
            currentSpeedStep = speedStep;
        }                                   
        else
            currentSpeedStep += speedStep;
	    
	}
	
	public boolean isMoving()
	{
		Point current = getObjectLocation();
		if ((goingOn != STOP) || !destination.equals(current))
			return true;
		return false;
	}

	@Localize(value="Sprite.getEncounteredObject")
	public TGraphicalObject getEncounteredObject()
	{
		return collisionWitness.getEncounteredObject();
	}
	
	@Localize(value="Sprite.getCollisionXCoordinate")
	public int getCollisionXCoordinate()
	{
		return collisionWitness.getXCoordinate();
	}

	@Localize(value="Sprite.getCollisionYCoordinate")
	public int getCollisionYCoordinate()
	{
		return collisionWitness.getYCoordinate();
	}
	
	@Localize(value="Sprite.getPreviousDirection")
	public String getPreviousDirection() 
	{
		return directionWitness.getPreviousDirection();
	}
	
	public boolean isHidden() {
		return hide;
	}

}
