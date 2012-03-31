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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;

import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;
import org.colombbus.tangara.TWitness;

@SuppressWarnings("serial")
@Localize(value="Block",localizeParent=true)
public class Block extends TGraphicalObject{

	private static final String[] DEFAULT_EXTENSIONS = {"png","bmp","gif","jpg","jpeg"}; //$NON-NLS-1$
	protected static final int TOLERANCE = 3;
	
	BufferedImage picture;	
	private BufferedImage picturebis;
	private boolean hide = false;
	private boolean[][] mask;
	private boolean displayMask = false;
	
	protected final Map<String, String> translatedEvents = new HashMap<String,String>();

    public static final byte DIRECTION_RIGHT = 1;
    public static final byte DIRECTION_LEFT = 2;
    public static final byte DIRECTION_UP = 4;
    public static final byte DIRECTION_DOWN = 8;
    
    protected boolean testClick = false;
    protected boolean testStopClick = false;
    
    protected int xCoordinate,yCoordinate,dragPreviousX,dragPreviousY;
    protected PictureDraggingMouse draggingMouseListener;
	protected PictureDraggingMouseMotion draggingMouseMotionListener;
    
	@Localize(value="Block")
	public Block()
	{
		super();
        setSize(50,50);
		setBorder(BorderFactory.createEmptyBorder());
		setOpaque(false); // in order to handle transparent images.
		
		registerEvent("click"); //$NON-NLS-1$
		registerEvent("stopClick"); //$NON-NLS-1$
		
		translatedEvents.put(getMessage("click"),"click"); //$NON-NLS-1$ //$NON-NLS-2$
        translatedEvents.put(getMessage("stopClick"),"stopClick"); //$NON-NLS-1$ //$NON-NLS-2$
        
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
		
		displayObject();
	}
	
	@Localize(value="Block")
	public Block(String fileName)
	{
		this();
    	loadFile(fileName);	
	}	
	
	/**
	 * Draws the image.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		if (!hide)
		{
			super.paintComponent(g);
			if (picturebis!=null)
			{
			    if (displayMask)
			    {
			        int width = getWidth();
			        int height = getHeight();
			        for (int x=0;x<width;x++)
			        {
			            for (int y=0;y<height;y++)
			            {
			                if (mask[x][y])
			                {
			                    g.setColor(Color.red);
			                    g.drawLine(x, y, x, y);
			                }
			            }
			        }
			    }
			    else
			    {
			        g.drawImage(picturebis, 0, 0, null);
			    }
			}
		}
	}
	
	/**
	 * Loads an image from a file.
	 * @param fileName
	 */
	@Localize(value="Block.loadFile")
    public void loadFile(String fileName)
    {
    	try
    	{
    		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
    		if (file == null)
        		throw new Exception("file not found"); //$NON-NLS-1$
    		BufferedImage newImage = ImageIO.read(file);
    		// Make use of Toolkit rather than ImageIO, to manage transparent images
    		setPicture(newImage);
    	}
    	catch (Exception e)
    	{
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", fileName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Program.instance().writeMessage(message);
    	}
    }
	
	/**
	 * Sets the image to draw.
	 * @param img
	 */
	public void setPicture(BufferedImage img)
	{
		if (img !=null)
		{
    		int width = img.getWidth();
    		int height = img.getHeight();
    		picturebis = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
    		picture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    		Graphics g = picturebis.getGraphics();
    		g.drawImage(img,0,0,null);
    		picture.getGraphics().drawImage(img,0,0,null);
    		this.setSize(width, height);
    		this.repaint();
		}
	}
	
    /**
     * Sets a transparent color.
     * @param colorName
     */
    @Localize(value="Block.transparentColor")
    public void transparentColor(String colorName)
    {
    	Color c = TColor.translateColor(colorName, Color.black);
    	TransparentFilter filter = new TransparentFilter(c);    
    	Image newPicture =Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(picturebis.getSource(),filter));
    	picturebis = new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_ARGB);
		picturebis.getGraphics().drawImage(newPicture,0,0,null);
    	computeMask();
		repaint();
    }
    
    /**
     * Hides the component     
     */
    @Override
	@Localize(value="Block.hide")
    public void hide()
    {
    	hide = true;
    	repaint();    	
    }
    
    /**
     * Shows the component
     */
    @Localize(value="Block.display")
    public void display()
    {
    	hide = false;
    	repaint();
    }
	
	@Override
	@Localize(value="common.setObjectWidth")
	public void setObjectWidth(int value)
	{
		super.setObjectWidth(value);
		computeMask();
	}
	
	@Override
	@Localize(value="common.setObjectHeight")
	public void setObjectHeight(int value)
	{
        super.setObjectHeight(value);
        computeMask();
	}

	private void computeMask()
	{
	    int width = getObjectWidth();
	    int height = getObjectHeight();
	    mask = new boolean[width][height];
	    if (picturebis != null)
	    {
    	    for (int x = 0; x<width;x++)
    	    {
    	        for (int y=0;y<height;y++)
    	        {
    	            if ((picturebis.getRGB(x, y)& 0xff000000)==0)
    	                mask[x][y] = false;
    	            else
                        mask[x][y] = true;
    	        }
    	    }
	    }
	}
	
	@Override
	public void setSize(int x, int y)
	{
	    super.setSize(x, y);
	    computeMask();
	}
	
    @Localize(value="Block.displayMask")
    public void displayMask(boolean value)
	{
	    displayMask = value;
	    repaint();
	}
	
    class TransparentFilter extends RGBImageFilter
    {
        private Color transparent;
        
        public TransparentFilter(Color target)
        {
            transparent = new Color(target.getRGB());
        }

        @Override
		public int filterRGB(int x, int y, int rgb)
        {
            Color c = new Color(rgb);
            if (TColor.testCloseColor(transparent, c))
            {
                return (rgb & 0x00ffffff);
            }
            else
            {
                return rgb;
            }
        }
    }
    
    public Rectangle computeMove(java.awt.Rectangle previousLocation, java.awt.Rectangle newLocation, byte direction, boolean withTolerance)
    {
        if (!hide)
        {
        	int tolerance = 0;
        	if (withTolerance)
        		tolerance = TOLERANCE;
            Rectangle2D union  = previousLocation.createUnion(newLocation);
            Rectangle computedLocation = new Rectangle(previousLocation);
            Rectangle maskBounds = this.getObjectBounds();
            boolean maskEncountered = false;
            if (this.getObjectBounds().intersects(union))
            {
                // this block is overlapped : we have to make some tests
                
                int oldX = (int)previousLocation.getX()-maskBounds.x;
                int oldY = (int)previousLocation.getY()-maskBounds.y;
                int newX = (int)newLocation.getX()-maskBounds.x;
                int newY = (int)newLocation.getY()-maskBounds.y;
                int width =  previousLocation.width;
                int height =  previousLocation.height;
                int x,y,x0,x1,y0,y1;
                int shiftX,shiftY;
                int contiguousSpaces,contiguousStart,contiguousEnd;
                
                
                if ((direction&DIRECTION_RIGHT) != 0)
                {
                    x0 = Math.max(oldX+width,0);
                    x1 = Math.min(newX+width,maskBounds.width);
                    y0 = Math.max(oldY-tolerance, 0);
                    y1 = Math.min(oldY+height+tolerance, maskBounds.height);
                    contiguousStart = Math.max(0, -(oldY-tolerance));
                    contiguousEnd = Math.max(0, oldY+height+tolerance-maskBounds.height);
                    shiftY = 0;
                    for (x = x0 ; x<x1; x++)
                    {
                        contiguousSpaces = contiguousStart;
                        for (y = y0 ; y<y1 ; y++)
                        {
                            if (mask[x][y])
                            {
                                if (contiguousSpaces>=height)
                                    break;
                                else
                                    contiguousSpaces=0;
                            }
                            else
                                contiguousSpaces++;
                        }
                        int foundHeight = contiguousSpaces+contiguousEnd;
                        if (foundHeight>=height)
                        {
                            int foundY = y-contiguousSpaces;
                            if (!((foundY<=oldY)&&(foundY+foundHeight>=oldY+height)))
                            {
                                int newShiftY = foundY-oldY;
                                if (Math.abs(newShiftY)>Math.abs(shiftY))
                                    shiftY = newShiftY;
                            }
                        }
                        else
                        {
                            maskEncountered = true;
                            break;
                        }
                    }
                    if (maskEncountered)
                        computedLocation.translate(x-width+maskBounds.x-computedLocation.x,shiftY);
                    else
                        computedLocation.translate(newLocation.x-computedLocation.x,shiftY);
     
                }
                if ((direction&DIRECTION_LEFT) != 0)
                {
                    x0 = Math.max(Math.min(oldX-1,maskBounds.width-1),0);
                    x1 = Math.max(newX,0);
                    y0 = Math.max(oldY-tolerance, 0);
                    y1 = Math.min(oldY+height+tolerance, maskBounds.height);
                    contiguousStart = Math.max(0, -(oldY-tolerance));
                    contiguousEnd = Math.max(0, oldY+height+tolerance-maskBounds.height);
                    shiftY = 0;
                    for (x = x0 ; x>=x1; x--)
                    {
                        contiguousSpaces = contiguousStart;
                        for (y = y0 ; y<y1 ; y++)
                        {
                            if (mask[x][y])
                            {
                                if (contiguousSpaces>=height)
                                    break;
                                else
                                    contiguousSpaces = 0;
                            }
                            else
                                contiguousSpaces++;
                        }
                        int foundHeight = contiguousSpaces+contiguousEnd;
                        if (foundHeight>=height)
                        {
                            int foundY = y-contiguousSpaces;
                            if (!((foundY<=oldY)&&(foundY+foundHeight>=oldY+height)))
                            {
                                int newShiftY = foundY-oldY;
                                if (Math.abs(newShiftY)>Math.abs(shiftY))
                                    shiftY = newShiftY;
                            }
                        }
                        else
                        {
                            maskEncountered = true;
                            break;
                        }
                    }
                    if (maskEncountered)
                    {
                        computedLocation.translate(x+1+maskBounds.x-computedLocation.x, shiftY);
                    }
                    else
                        computedLocation.translate(newLocation.x-computedLocation.x,shiftY);
                }
                if ((direction&DIRECTION_UP) != 0)
                {
                    y0 = Math.max(Math.min(oldY-1, maskBounds.height-1),0);
                    y1 = Math.max(newY,0);
                    x0 = Math.max(oldX-tolerance,0);
                    x1 = Math.min(oldX+width+tolerance,maskBounds.width);
                    contiguousStart = Math.max(0, -(oldX-tolerance));
                    contiguousEnd = Math.max(0, oldX+width+tolerance-maskBounds.width);
                    shiftX = 0;
                    
                    for (y = y0 ; y>=y1; y--)
                    {
                        contiguousSpaces = contiguousStart;
                        for (x = x0 ; x<x1 ; x++)
                        {
                            if (mask[x][y])
                            {
                                if (contiguousSpaces>=width)
                                    break;
                                else
                                    contiguousSpaces = 0;
                            }
                            else
                            {
                                contiguousSpaces++;
                            }
                        }
                        int foundWidth = contiguousSpaces+contiguousEnd;
                        if (foundWidth>=width)
                        {
                            int foundX = x-contiguousSpaces;
                            if (!((foundX<=oldX)&&(foundX+foundWidth>=oldX+width)))
                            {
                                int newShiftX = foundX-oldX;
                                if (Math.abs(newShiftX)>Math.abs(shiftX))
                                    shiftX = newShiftX;
                            }
                        }
                        else
                        {
                            maskEncountered = true;
                            break;
                        }
                    }
                    if (maskEncountered)
                        computedLocation.translate(shiftX,y+1+maskBounds.y-computedLocation.y);
                    else
                        computedLocation.translate(shiftX,newLocation.y-computedLocation.y);
                }
                if ((direction&DIRECTION_DOWN) != 0)
                {
                    y0 = Math.max(oldY+height,0);
                    y1 = Math.min(newY+height,maskBounds.height);
                    x0 = Math.max(oldX-tolerance,0);
                    x1 = Math.min(oldX+width+tolerance,maskBounds.width);
                    contiguousStart = Math.max(0, -(oldX-tolerance));
                    contiguousEnd = Math.max(0, oldX+width+tolerance-maskBounds.width);
                    shiftX = 0;
                    for (y = y0 ; y<y1; y++)
                    {
                        contiguousSpaces = contiguousStart;
                        for (x = x0 ; x<x1 ; x++)
                        {
                            if (mask[x][y])
                            {
                                if (contiguousSpaces>=width)
                                    break;
                                else
                                    contiguousSpaces = 0;
                            }
                            else
                            {
                                contiguousSpaces++;
                            }
                        }
                        int foundWidth = contiguousSpaces+contiguousEnd;
                        if (foundWidth>=width)
                        {
                            int foundX = x-contiguousSpaces;
                            if (!((foundX<=oldX)&&(foundX+foundWidth>=oldX+width)))
                            {
                                int newShiftX = foundX-oldX;
                                if (Math.abs(newShiftX)>Math.abs(shiftX))
                                    shiftX = newShiftX;
                            }
                        }
                        else
                        {
                            maskEncountered = true;
                            break;
                        }
                    }
                    if (maskEncountered)
                        computedLocation.translate(shiftX,y-height+maskBounds.y-computedLocation.y);
                    else
                        computedLocation.translate(shiftX,newLocation.y-computedLocation.y);
                }
                return computedLocation;
            }
            else
            {
                // this block is not overlapped
                return newLocation;
            }
        }
        else
            // this block is hidden
            return newLocation;
    }
    
    @Localize(value="Block.ifClick")
    public void ifClick(String command)
    {
    	addHandler("click",command); //$NON-NLS-1$
    	testClick = true;
    }

	@Localize(value="Block.ifClick2")
    public void ifClick(String command, TWitness witness)
    {
    	addHandler("click", command); //$NON-NLS-1$
    	addWitness("click", witness); //$NON-NLS-1$
    	testClick = true;
    }  

	@Localize(value="Block.ifStopClick")
    public void ifStopClick(String command)
    {
        addHandler("stopClick",command); //$NON-NLS-1$
        testStopClick = true;
    }
    
    @Localize(value="Block.ifStopClick2")
    public void ifStopClick(String command, TWitness witness)
    {
        addHandler("stopClick", command); //$NON-NLS-1$
        addWitness("stopClick", witness); //$NON-NLS-1$
        testStopClick = true;
    }
    
    /**
     * Sets the dragging status.
     * @param value
     */
    @Localize(value="Block.followMouse")
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
			if (eventName.equals("click")) //$NON-NLS-1$
				testClick = false;
		}
	}
}
