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
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.text.MessageFormat;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;

import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

@SuppressWarnings("serial")
@Localize(value="Path",localizeParent=true)
public class Path extends TGraphicalObject{

	private static final String[] DEFAULT_EXTENSIONS = {"png","bmp","gif","jpg","jpeg"}; //$NON-NLS-1$

	BufferedImage picture;	
	private BufferedImage picturebis;
	private java.util.List<Path> pathList = new Vector<Path>(); 
	private boolean hide = false;
	
	
	@Localize(value="Path")
	public Path()
	{
		super();
        setSize(50,50);
		setBorder(BorderFactory.createEmptyBorder());
		setOpaque(false); // in order to handle transparent images.	
		displayObject();
	}
	
	@Localize(value="Path")
	public Path(String fileName)
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
				g.drawImage(picturebis, 0, 0, null);
			}
		}
	}
	
	/**
	 * Loads an image from a file.
	 * @param fileName
	 */
	@Localize(value="Path.loadFile")
    public void loadFile(String fileName)
    {
    	try {
    		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
    		if (file == null)
    			throw new Exception("file not found");
    		BufferedImage newImage = ImageIO.read(file);
    		// Make use of Toolkit rather than ImageIO, to manage transparent images
    		setPicture(newImage);
    	} catch (Exception e) {
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", fileName);
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
    @Localize(value="Path.transparentColor")
    public void transparentColor(String colorName)
    {
    	Color c = TColor.translateColor(colorName, Color.black);
    	TransparentFilter filter = new TransparentFilter(c);    
    	Image newPicture =Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(picturebis.getSource(),filter));
    	picturebis = new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_ARGB);
		picturebis.getGraphics().drawImage(newPicture,0,0,null);
    	repaint();
    }
    
    /**
     * Hides the component     
     */
    @Override
	@Localize(value="Path.hide")
    public void hide()
    {
    	hide = true;
    	repaint();    	
    }
    
    /**
     * Shows the component
     */
    @Localize(value="Path.display")
    public void display()
    {
    	hide = false;
    	repaint();
    }
	
	@Override
	@Localize(value="common.setObjectWidth")
	public void setObjectWidth(int value)
	{
		
	}
	
	@Override
	@Localize(value="common.setObjectHeight")
	public void setObjectHeight(int value)
	{
		
	}

	@Localize(value="Path.associatePath")
	public void associatePath(Path p)
	{
		pathList.add(p);
		p.getPathList().add(this);
	}
	
	public java.util.List<Path> getPathList()
	{
		return pathList;
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
}
