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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.StringUtils;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class provides a box in which one can draw.
 * @author Benoit
 */
@SuppressWarnings("serial")
@Localize(value="Drawing",localizeParent=true)
public abstract class Drawing extends TGraphicalObject {

    public final String DEFAULT_EXTENSION = getMessage("defaultExtension");
	private static final String[] DEFAULT_EXTENSIONS = {"png","bmp","gif","jpg","jpeg"}; //$NON-NLS-1$

    public final String MODE_DRAW = getMessage("mode.draw");
    public final String MODE_LINE = getMessage("mode.line");
    public final String MODE_RECTANGLE = getMessage("mode.rectangle");
    public final String MODE_ERASE = getMessage("mode.erase");
    public final String MODE_ELLIPSE = getMessage("mode.ellipse");
    public final String MODE_FILL = getMessage("mode.fill");

    private static final int INT_MODE_DRAW = 0;
    private static final int INT_MODE_LINE = 1;
    private static final int INT_MODE_RECTANGLE = 2;
    private static final int INT_MODE_ELLIPSE = 3;
    private static final int INT_MODE_ERASE = 4;
    private static final int INT_MODE_FILL = 5;

    private static final int ERASER_SIZE = 10;
    private static final int FILL_TOLERANCE= 20;

    private static final int UNDO_MAX_LEVEL = 5;
    
    private BufferedImage[] buffers = null;
    private BufferedImage tempBuffer=null;
    private Color drawingColor = Color.black;
    private final Color BACKGROUND_COLOR = Color.white;
    private Point origin = null;
    private int currentMode = INT_MODE_DRAW;
    private boolean tempMode = false;
    private Cursor eraserCursor = null;
    private Cursor fillCursor = null;
    private int fillReferenceColor = 0;
    private int bufferIndex = 0;
    private int undoLevel = 0;
    
    private static Logger LOG = Logger.getLogger(Drawing.class);

    /**
     * Creates a new instance of drawing
     */
    @Localize(value="Drawing")
    public Drawing()
    {
        super();
        setSize(300,100);
        defineBuffer();
        setBorder(LineBorder.createBlackLineBorder());
        initCursors();

        this.addMouseListener(new MouseAdapter(){
        	@Override
			public void mousePressed(MouseEvent e)
        	{
        		origin = new Point(e.getX(),e.getY());
        		switch (currentMode)
        		{
        			case INT_MODE_LINE:
        			case INT_MODE_RECTANGLE:
        			case INT_MODE_ELLIPSE:
        				copyTempBuffer();
        				tempMode = true;
        				break;
        			case INT_MODE_ERASE:
        				shiftBuffers();
        				erase(origin);
        				break;
        			case INT_MODE_FILL:
        				shiftBuffers();
        				fillShape(origin);
        				break;
        			case INT_MODE_DRAW:
        				shiftBuffers();
        				break;
        		}
        	}

        	@Override
			public void mouseReleased(MouseEvent e)
        	{
				Point destination = new Point(e.getX(),e.getY());
        		switch (currentMode)
        		{
        			case INT_MODE_RECTANGLE:
        				tempMode=false;
        				shiftBuffers();
        				drawRectangle(buffers[bufferIndex],origin,destination);
        				break;
        			case INT_MODE_ELLIPSE:
        				tempMode=false;
        				shiftBuffers();
        				drawEllipse(buffers[bufferIndex],origin,destination);
        				break;
        			case INT_MODE_LINE:
        				tempMode = false;
        				shiftBuffers();
        			case INT_MODE_DRAW:
        				drawLine(buffers[bufferIndex],origin,destination);
        				break;
        		}
        	}
        });
        this.addMouseMotionListener(new MouseMotionAdapter(){
        	@Override
			public void mouseDragged(MouseEvent e)
        	{
        		Point destination = new Point(e.getX(),e.getY());
        		switch (currentMode)
        		{
        			case INT_MODE_RECTANGLE:
        				copyTempBuffer();
        				drawRectangle(tempBuffer,origin,destination);
	            		break;
	            	case INT_MODE_ELLIPSE:
        				copyTempBuffer();
        				drawEllipse(tempBuffer,origin,destination);
	            		break;
        			case INT_MODE_LINE:
        				copyTempBuffer();
        				drawLine(tempBuffer,origin,destination);
	            		break;
        			case INT_MODE_DRAW:
        				drawLine(buffers[bufferIndex],origin,destination);
	            		origin = destination;
	            		break;
        			case INT_MODE_ERASE:
        				erase(destination);
        				break;
        		}
        	}
        });
        displayObject();
    }


    /**
     * Initializes the cursors.
     */
    private void initCursors()
    {
    	Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension cursorSize = tk.getBestCursorSize(ERASER_SIZE, ERASER_SIZE);
        if (cursorSize.width>0)
        {
            BufferedImage eraserImage = new BufferedImage(cursorSize.width,cursorSize.height,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2D = eraserImage.createGraphics();
            Color transparent = new Color(0, 0, 0, 0);
            g2D.setColor(transparent);
            g2D.setComposite(AlphaComposite.Src);
            g2D.fillRect(0, 0, cursorSize.width, cursorSize.height);
            g2D.setColor(Color.white);
            g2D.fillRect(0, 0, ERASER_SIZE-1, ERASER_SIZE-1);
            g2D.setColor(Color.black);
            g2D.drawRect(0, 0, ERASER_SIZE-1, ERASER_SIZE-1);
            eraserCursor = tk.createCustomCursor(eraserImage, new Point(0,0), "eraser");
        }    	
		try {
			URL fillURL = getResource("logoFill.png").toURL();
			if (fillURL!=null)
	    	{
	    		try
	    		{
	        		BufferedImage fillImage = ImageIO.read(fillURL);
	    			cursorSize = tk.getBestCursorSize(fillImage.getWidth(), fillImage.getHeight());
	    			if (cursorSize.width>0)
	    			{
	        			BufferedImage fillCursorImage = new BufferedImage(cursorSize.width,cursorSize.height,BufferedImage.TYPE_INT_ARGB);
	    	            Graphics2D g2D = fillCursorImage.createGraphics();
	    	            Color transparent = new Color(0, 0, 0, 0);
	    	            g2D.setColor(transparent);
	    	            g2D.setComposite(AlphaComposite.Src);
	    	            g2D.fillRect(0, 0, cursorSize.width, cursorSize.height);
	    	            g2D.drawImage(fillImage, 0, 0, null);
		                fillCursor = tk.createCustomCursor(fillCursorImage, new Point(15,17), "fill");
	    			}
	    		}
	    		catch (IOException e)
	    		{}
	    	}
		} catch (MalformedURLException e1) {
			LOG.debug("Error " + e1);
		}    	
    }

    /**
     * Sets the pen color.
     * @param colorName
     */
    @Localize(value="common.setColor")
    public void setColor(String colorName)
    {
		Color c = TColor.translateColor(colorName, Color.black);
    	drawingColor = c;
    }

    /**
     * Sets the drawing mode.
     * @param mode
     */
    @Localize(value="Drawing.setMode")
    public void setMode(String mode)
    {
    	mode = StringUtils.removeAccents(mode);
    	if (mode.compareTo(MODE_DRAW)==0)
    	{
    		currentMode = INT_MODE_DRAW;
    	}
    	else if (mode.compareTo(MODE_LINE)==0)
    	{
    		currentMode = INT_MODE_LINE;
    	}
    	else if (mode.compareTo(MODE_RECTANGLE)==0)
    	{
    		currentMode = INT_MODE_RECTANGLE;
    	}
    	else if (mode.compareTo(MODE_ERASE)==0)
    	{
    		currentMode  =INT_MODE_ERASE;
    	}
    	else if (mode.compareTo(MODE_ELLIPSE)==0)
    	{
    		currentMode = INT_MODE_ELLIPSE;
    	}
    	else if (mode.compareTo(MODE_FILL)==0)
    	{
    		currentMode = INT_MODE_FILL;
    	}
    	else // by default : MODE_DRAW
    	{
			Program.instance().printError(MessageFormat.format(getMessage("mode.unknown"), mode)); //$NON-NLS-1$ 
    		currentMode = INT_MODE_DRAW;
    	}

    	if ((currentMode==INT_MODE_ERASE)&&(eraserCursor!=null))
    	{
    		setCursor(eraserCursor);
    	}
    	else if ((currentMode==INT_MODE_FILL)&&(fillCursor!=null))
    	{
    		setCursor(fillCursor);
    	}
    	else
    	{
    		setCursor(Cursor.getDefaultCursor());
    	}
    }

    /**
     * Clears the drawing area.
     */
    @Localize(value="Drawing.erase")
    public void erase()
    {
    	shiftBuffers();
    	Graphics g = buffers[bufferIndex].getGraphics();
    	g.setColor(BACKGROUND_COLOR);
    	g.fillRect(0, 0, getObjectWidth(), getObjectHeight());
    	repaint();
    }

    /**
     * Draws a line.
     * @param buf
     * @param a
     * @param b
     */
    private void drawLine(BufferedImage buf, Point a,Point b)
    {
    	Graphics2D g = (Graphics2D)buf.getGraphics();
    	g.setColor(drawingColor);
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    	g.drawLine(a.x, a.y, b.x, b.y);
    	repaint();
    }

    /**
     * Draws a rectangle.
     * @param buf
     * @param a
     * @param b
     */
    private void drawRectangle(BufferedImage buf, Point a,Point b)
    {
    	Graphics2D g = (Graphics2D)buf.getGraphics();
    	g.setColor(drawingColor);
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    	int[] xCoordinates = new int[4];
    	xCoordinates[0] = a.x;
    	xCoordinates[1] = a.x;
    	xCoordinates[2] = b.x;
    	xCoordinates[3] = b.x;
    	int[] yCoordinates = new int[4];
    	yCoordinates[0] = a.y;
    	yCoordinates[1] = b.y;
    	yCoordinates[2] = b.y;
    	yCoordinates[3] = a.y;
    	g.drawPolygon(xCoordinates, yCoordinates, 4);
    	repaint();
    }

    /**
     * Draws an ellipse.
     * @param buf
     * @param a
     * @param b
     */
    private void drawEllipse(BufferedImage buf, Point a,Point b)
    {
    	Graphics2D g = (Graphics2D)buf.getGraphics();
    	g.setColor(drawingColor);
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    	int ellipseX = Math.min(a.x, b.x);
    	int ellipseY = Math.min(a.y, b.y);
    	int ellipseWidth = Math.max(a.x, b.x)-ellipseX;
    	int ellipseHeight = Math.max(a.y, b.y)-ellipseY;
    	g.drawOval(ellipseX,ellipseY, ellipseWidth,ellipseHeight);
    	repaint();
    }

    /**
     * Erases a given point.
     * @param a
     */
    private void erase(Point a)
    {
    	Graphics g = buffers[bufferIndex].getGraphics();
    	g.setColor(BACKGROUND_COLOR);
    	g.fillRect(a.x, a.y, ERASER_SIZE, ERASER_SIZE);
    	repaint();
    }

    /**
     * Defines the buffer.
     */
    private void defineBuffer()
    {
    	if (buffers == null)
    	{
    		buffers = new BufferedImage[UNDO_MAX_LEVEL];
    	}
    	
    	for (int i =0;i<UNDO_MAX_LEVEL;i++)
    	{
    		BufferedImage newBuffer = new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_RGB);
    		Graphics g = newBuffer.getGraphics();
    		g.setColor(BACKGROUND_COLOR);
    		g.fillRect(0, 0, getObjectWidth(), getObjectHeight());
    		if (buffers[i] == null)
    		{
    			buffers[i] = newBuffer;
    		}
    		else
    		{
    			newBuffer.getGraphics().drawImage(buffers[i], 0, 0, null);
    			buffers[i] = newBuffer;
    			repaint();
    		}
    	}
    	tempBuffer=new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Copies TempBuffer.
     */
    private void copyTempBuffer()
    {
    	if (tempBuffer ==null)
    	{
    		return;
    	}
    	Graphics g = tempBuffer.getGraphics();
    	g.setColor(BACKGROUND_COLOR);
    	g.fillRect(0, 0, getObjectWidth(), getObjectHeight());
    	g.drawImage(buffers[bufferIndex], 0, 0, null);
    }

    /**
     * Draws the appropriate buffer.
     */
    @Override
	public void paintComponent(Graphics g)
    {
    	if (tempMode)
        	g.drawImage(tempBuffer, 0, 0, null);
    	else
    		g.drawImage(buffers[bufferIndex], 0, 0, null);
    }

    /**
     * Returns the buffer.
     * @return
     */
    public BufferedImage getImage()
    {
    	return buffers[bufferIndex];
    }

    /**
     * Draws a given image.
     * @param img
     */
    public void paintImage(BufferedImage img)
    {
    	shiftBuffers();
    	Graphics g = buffers[bufferIndex].getGraphics();
    	g.setColor(BACKGROUND_COLOR);
    	g.fillRect(0, 0, getObjectWidth(), getObjectHeight());
    	g.drawImage(img, 0, 0, null);
    	repaint();
    }

    /**
     * Returns if the given pixel is in a shape to fill.
     * @param x
     * @param y
     * @return
     */
    private synchronized boolean inFillingShape(int x, int y)
    {
    	int color =buffers[bufferIndex].getRGB(x, y);
    	if (color==drawingColor.getRGB())
    	{
    		return false;
    	}
    	int dr = Math.abs(((color&0xFF0000)>>16)-((fillReferenceColor&0xFF0000)>>16));
    	int dg = Math.abs(((color&0xFF00)>>8)-((fillReferenceColor&0xFF00)>>8));
    	int db = Math.abs((color&0xFF)-(fillReferenceColor&0xFF));
    	return ((dr<FILL_TOLERANCE)&&(dg<FILL_TOLERANCE)&&(db<FILL_TOLERANCE));
    }

    /**
     * Fills with color the area around a given point.
     * @param where
     */
    private void fillShape(Point where)
    {
    	Cursor currentCursor = this.getCursor();
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	fillReferenceColor = buffers[bufferIndex].getRGB(where.x, where.y);
    	computeFillingShape(where.x,where.y);
    	repaint();
    	setCursor(currentCursor);
    }

    /**
     * Fills a part of the screen.
     */
    private synchronized void computeFillingShape(int fillX, int fillY)
    {
    	int x,y,xi,xf ;
    	Vector<Point> points = new Vector<Point>();
    	points.add(new Point(fillX,fillY));
    	Graphics g = buffers[bufferIndex].getGraphics();
    	g.setColor(drawingColor);

    	// Main loop : draw all the segments
    	while (points.size() > 0)
    	{
    		Point point = points.get(points.size()-1);
    		xi = xf = x = point.x;
    		y = point.y;
		   //	Look for the end of the segment
		   x++ ;
		   while ((x<getObjectWidth())&&inFillingShape(x,y))
		   {
			   xf = x ;
			   x++ ;
		   }

		   // Look for the start of the segment
		   x = point.x-1 ;
		   while ((x>=0)&&inFillingShape(x,y))
		   {
			   xi = x ;
			   x-- ;
			}
		   g.drawLine(xi, y, xf, y);
		   // remove this point since the corresponding segment has been drawn
		   points.remove(points.size()-1);


		   // examine the next line
		   if (y+1<getObjectHeight())
		   {
			   x = xf ;
			   while (x>=xi)
			   {
				   while ((x>=xi)&& !inFillingShape(x,y+1))
				   {
					   x-- ;
				   }
				   if ((x>=xi) && inFillingShape(x,y+1))
				   {
					   points.add(new Point(x,y+1));
				   }
				   while ((x>=xi)&&inFillingShape(x,y+1))
				   {
					   x-- ;
				   }
			   }
		   }

		   // examine the previous line
		   x = xf ;
		   if (y-1>=0)
		   {
			   while (x>=xi )
			   {
				   while ((x>=xi)&&!inFillingShape(x,y-1))
				   {
					   x-- ;
				   }
				   if ( (x>=xi) && inFillingShape(x,y-1))
				   {
					   points.add(new Point(x,y-1));
				   }
				   while ((x>=xi)&&inFillingShape(x,y-1))
				   {
					   x-- ;
				   }
			   }
		   }
    	}
	}

    /**
     * Saves a file.
     * @param fileName
     * @return
     */
    @Localize(value="Drawing.saveFile")
    public boolean saveFile(String fileName)
    {
    	String extension = "";
    	try
    	{
            String actualFileName = new File(fileName).getName();
	    	int pointPosition = actualFileName.lastIndexOf('.');
	    	if (pointPosition>-1)
	    	{
	    		extension = actualFileName.substring(pointPosition+1);
	    		if (extension.length()==0)
	    		{
	    			extension = DEFAULT_EXTENSION;
	    			fileName += DEFAULT_EXTENSION;
	    		}
	    	}
	    	else
	    	{
				extension = DEFAULT_EXTENSION;
				fileName += "." + DEFAULT_EXTENSION;
	    	}
            File file = new File(fileName);
	    	if (!file.isAbsolute())
	    	{
	    		// the name does not contain any directory reference : add the current directory
	    		file = new File(Program.instance().getCurrentDirectory(),fileName);
	    		fileName = file.getAbsolutePath();
	    	}
	    	if (!file.getParentFile().exists())
	    	{
	    		throw new Exception("cannot find directory "+file.getParent());
	    	}
	    	Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(extension);
	    	if ((writers==null)||(!(writers.hasNext())))
	    	{
	    		throw new Exception("format unknown");
	    	}
   			if (file.exists())
   			{
   				String title = getMessage("save.override.title");
   				String message = MessageFormat.format(getMessage("save.override.message"), fileName);
				int reponse = JOptionPane.showConfirmDialog(getGraphicsPane(), message,title, JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
				if (reponse != JOptionPane.OK_OPTION)
					return false;
   			}
			ImageIO.write(getImage(), extension, file);
    	}
    	catch (Exception e)
    	{
            String message = MessageFormat.format(getMessage("save.error")+" ("+e.getMessage()+")", fileName,extension);
            Program.instance().writeMessage(message);
            return false;
    	}
    	return true;
    }

    /**
     * Loads a file.
     * @param fileName
     */
    @Localize(value="Drawing.loadFile")
    public void loadFile(String fileName)
    {
    	try
    	{
    		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
    		if (file == null)
    			throw new Exception("file not found");
    		BufferedImage newImage = ImageIO.read(file);
    		paintImage(newImage);
    	}
    	catch (Exception e)
    	{
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", fileName);
            Program.instance().writeMessage(message);
    	}
    }

    /**
     * Shifts all buffers and free a new one to make edits.
     */
    private void shiftBuffers()
    {
    	if (bufferIndex > 0)
    	{
    		// redo actions have been done : we now erase the following redo actions if any
        	for (int i=0;i<UNDO_MAX_LEVEL-bufferIndex;i++)
        	{
        		buffers[i] = buffers[i+bufferIndex];
        	}
        	undoLevel -= bufferIndex; 
        	bufferIndex = 0;
    	}
    	// We shift the buffers by 1
    	for (int i=UNDO_MAX_LEVEL-1;i>0;i--)
    	{
    		buffers[i] = buffers[i-1];
    	}

    	// We make a copy of the last current buffer in the first one
		BufferedImage newBuffer = new BufferedImage(buffers[1].getWidth(),buffers[1].getHeight(),BufferedImage.TYPE_INT_RGB);
		newBuffer.getGraphics().drawImage(buffers[1], 0, 0, null);
    	buffers[0] = newBuffer;
    	
    	undoLevel=Math.min(UNDO_MAX_LEVEL, undoLevel+1);
    }

    
    
    /**
     * Undo the last action
     */
    @Localize(value="Drawing.undo")
    public void undo()
    {
    	if (bufferIndex+1>undoLevel)
    	{
            Program.instance().writeMessage(getMessage("undo.error"));
    	}
    	else
    	{
    		bufferIndex++;
    		repaint();
    	}
    }
    
    /**
     * Redo the last undone action
     */
    @Localize(value="Drawing.redo")
    public void redo()
    {
    	if (bufferIndex==0)
    	{
            Program.instance().writeMessage(getMessage("redo.error"));
    	}
    	else
    	{
    		bufferIndex--;
    		repaint();
    	}
    }
    
    @Override
	@Localize(value="common.setObjectWidth")
    public void setObjectWidth(int width)
    {
    	super.setObjectWidth(width);
    	defineBuffer();
    }

    @Override
	@Localize(value="common.setObjectHeight")
    public void setObjectHeight(int height)
    {
    	super.setObjectHeight(height);
    	defineBuffer();
    }
    
}
