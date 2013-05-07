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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.text.MessageFormat;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;

import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;


/**
 * This class provides an object that can display a picture.
 * @author Benoit
 *
 */
@SuppressWarnings("serial")
@Localize(value="Picture",localizeParent=true)
public abstract class Picture extends TGraphicalObject
{

	private static final String[] DEFAULT_EXTENSIONS = {"png","bmp","gif","jpg","jpeg"}; //$NON-NLS-1$
	private BufferedImage originalPicture;	
	private BufferedImage picture;	
    private int xCoordinate,yCoordinate,dragPreviousX,dragPreviousY;

	private TransparentFilter imageFilter= new TransparentFilter();
	
	/**
	 * Creates a new instance of Picture
	 */
	@Localize(value="Picture")
    public Picture() {
    	super();
        setSize(50,50);
		setBorder(BorderFactory.createEmptyBorder());
		setOpaque(false); // in order to handle transparent images.		

		displayObject();
    }

	/**
	 * Creates a new Instance of Picture and initializes it.
	 * @param fileName
	 */
	@Localize(value="Picture")
    public Picture(String fileName)
    {
    	this();
    	loadPicture(fileName);
    }

	/**
	 * Draws the image.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (picture!=null)
		{
			g.drawImage(picture, 0, 0, null);
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
    		originalPicture = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
    		Graphics g = originalPicture.getGraphics();
    		g.drawImage(img,0,0,null);
    		this.setSize(width, height);
    		generateImage();
		}
	}
	
	/**
	 * Loads an image from a file.
	 * @param fileName
	 
	@Localize(value="Picture.loadPicture")
    public void loadPicture(String fileName)
    {
		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
		try {
			if (file == null) {
				throw new Exception("file not found");
			} else {
	    		BufferedImage newImage = ImageIO.read(file);
	    		// Make use of Toolkit rather than ImageIO, to manage transparent images
	    		setPicture(newImage);
	    	} 
		} catch (Exception e) {
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", fileName);
            Program.instance().writeMessage(message);
		}
    }/*


	/**
	 * Sets the border.
	 * @param value
	 */
    @Localize(value="Picture.frame")
    public void frame(boolean value)
    {
    	if (value)
    	{
    		setBorder(BorderFactory.createLineBorder(Color.black,1));
    	}
    	else
    	{
    		setBorder(BorderFactory.createEmptyBorder());
    	}
    }

    /**
     * Fills with a color.
     * @param colorName
     */
    @Localize(value="Picture.fillColor")
    public void fillColor(String colorName)
    {
		Color c = TColor.translateColor(colorName, Color.black);
		picture = new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics g = picture.getGraphics();
		g.setColor(c);
		g.fillRect(0, 0, getObjectWidth(), getObjectHeight());
		this.repaint();
    }

    /**
     * Loads a picture.
     * @param source
     */
    @Localize(value="Picture.loadPicture2")
    public void loadPicture(BufferedImage source)
    {
    	setPicture(source);
    }

    /**
     * Clears a picture.
     */
    @Localize(value="Picture.clear")
    public void clear()
    {
		picture = new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics g = picture.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, getObjectWidth(), getObjectHeight());
		repaint();
    }

    /**
     * Sets a transparent color.
     * @param colorName
     */
    @Localize(value="Picture.transparentColor")
    public void transparentColor(String colorName)
    {
    	Color c = TColor.translateColor(colorName, Color.black);
    	imageFilter.setTransparentColor(c);
    	generateImage();
    }

	protected void generateImage()
	{
    	Image newPicture = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(originalPicture.getSource(),imageFilter));
    	picture = new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_ARGB);
		picture.getGraphics().drawImage(newPicture,0,0,null);
    	repaint();
	}
    
	@Localize(value="Picture.setTransparency")
	public void setTransparency(int coef)
	{
		if ((coef<0)||(coef>100))
		{
            Program.instance().writeMessage(getMessage("transparency.error"));
			return;
		}
		imageFilter.setTransparency((255-(coef*255/100)));
		generateImage();
	}
	
    class TransparentFilter extends RGBImageFilter
    {
    	private long filter = 0xffffffff;
    	private boolean transparentColor = false;
    	private Color transparent;

        public TransparentFilter()
        {
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
}
