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

/*
 * ElementSquelette.java
 *
 * Created on 25 novembre 2006, 23:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.colombbus.tangara.objects.character;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * This class is used to create objects of a skeleton (execpt points).
 * So each part of the skeleton has its own picture and move (arm, hand, chest...)
 * @author benoit
 */
@SuppressWarnings("serial")
class SkeletonElement extends Component
{
    /** Class logger */
	private static Logger LOG = Logger.getLogger(SkeletonElement.class);
	
	private BufferedImage image;
	private String name;
	private URL imagePath;
	private String imageName;
	private int xCoordinate;
	private int yCoordinate;
	private final Point rotationCenter = new Point();
	private double rotationAngle;

	private final Dimension shift = new Dimension();
    
	/**
	 * Creates an empty new skeleton element
	 *
	 */
    public SkeletonElement() {
    }
        

    /**
     * Creates a new SkeletonElement instance and loads its image
     * @param name
     * 		the name of this element
     * @param resourcePath
     * 		the path of element's image 
     * @param imageName
     * 		the image name
     * @throws Exception
     */
    public SkeletonElement(String name, URL resourcePath, String imageName) throws Exception
    {
        this.name = name;
		loadImage(resourcePath, imageName);
    }
    
    /**
     * Creates a new SkeletonElement instance, loads its image and sets its coordinates.
     * @param elementName
     * 		the element's name
     * @param resourcePath
     * 		the path of element's image
     * @param imageName
     * 		the image name
     * @param x
     * 		the x-coordinate
     * @param y
     * 		the y-coordinate.
     * @throws Exception
     */
    public SkeletonElement(String elementName, URL resourcePath, String imageName, int x, int y) throws Exception
    {
        this(elementName,resourcePath,imageName);
        xCoordinate=x;
        yCoordinate=y;
    }
    
    /**
     * Loads the image passed as parameters to this skeleton element
     * @param resourcePath
     * 		the path of image's name
     * @param imageName
     * 		the image name
     * @throws Exception
     */
    public void loadImage(URL resourcePath, String imageName) throws Exception
    {
        this.imagePath = resourcePath;
        this.imageName = imageName;
        File tmpImageFile = JARUtils.extractFileFromJar(resourcePath,imageName);
        try
        {
            Image imageRaw = loadImageFile(tmpImageFile);            
            this.image = cloneImage(imageRaw);            
        }
        catch(Exception e)
        {
        	String msg = String.format("Cannot access image file %s", tmpImageFile.getAbsolutePath());
        	LOG.warn(msg);
        	throw new Exception(msg);
        } 
        finally 
        {
            tmpImageFile.delete();
        }
    }


	private BufferedImage cloneImage(Image imageRaw) {
		int width = imageRaw.getWidth(this);
		int height = imageRaw.getHeight(this);
		BufferedImage imageCopy = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics g = imageCopy.createGraphics();
		g.drawImage(imageRaw,0,0,this);
		g.dispose();
		return imageCopy;
	}


	private Image loadImageFile(File imageFile) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage(imageFile.getAbsolutePath());
		MediaTracker waitForLoading = new MediaTracker(this);
		waitForLoading.addImage(image,0);
		try
		{
		    waitForLoading.waitForID(0);
		}
		catch (InterruptedException e)
		{
		}
		return image;
	}
    
    /**
     * Sets the coordinates
     * @param x
     * 		the x-coordinate
     * @param y
     * 		the y-coordinate
     */
    public void setCoordinates(int x, int y)
    {
        xCoordinate = x;
        yCoordinate = y;
    }
    
    /**
     * Gets the element's coordinates
     * @return
     * 		Contains the coordinates of this element.
     */
    public Point getCoordinates()
    {
        return new Point(xCoordinate,yCoordinate);
    }
    
    /**
     * Gets the element's name.
     * @return
     * 		the element name
     */
    public String getElementName()
    {
        return name;
    }
    
    /**
     * Gets the path of the element's image
     * @return
     * 		the image's path
     */
    public URL getImagePath()
    {
        return imagePath;
    }
    
    /**
     * Gets the image name
     * @return
     * 		the image name
     */
    public String getImageName()
    {
        return imageName;
    }
    
    /**
     * Draws the element.
     * If the rotation angle equals 0 the object just makes a translation, else a rotation and a translation 
     * make the element moving.
     * @param g
     * 		the Graphics context in which to paint.
     */
    public void paintElement(Graphics g)
    {
        if (rotationAngle==0)
        {
            int xImage = (int) (xCoordinate + shift.getWidth());
			int yImage = (int) (yCoordinate + shift.getHeight());
			g.drawImage(image, xImage, yImage, null);
        }
        else
        {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform transformation = new AffineTransform();
            transformation.rotate(Math.toRadians(rotationAngle),rotationCenter.x,rotationCenter.y);
            transformation.translate(xCoordinate+shift.getWidth(),yCoordinate+shift.getHeight());
            g2.drawImage(image,transformation,null);
        }
    }
    
    /**
     * Draws the element from the origin passed as parameters.
     * @param g
     * 		the Graphics context in which to paint.
     * @param origineX
     * 		the x-coordinate of the origin
     * @param origineY
     * 		the y-coordinate of the origin
     */
    public void paintElement(Graphics g, int origineX, int origineY)
    {
        if (rotationAngle==0)
        {
            int xImage = (int) (xCoordinate + origineX + shift.getWidth());
			int yImage = (int) (yCoordinate + origineY + shift.getHeight());
			g.drawImage(image, xImage, yImage, null);
        }
        else
        {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform transformation = new AffineTransform();
            transformation.translate(origineX, origineY);
            transformation.rotate(Math.toRadians(rotationAngle),rotationCenter.x,rotationCenter.y);
            transformation.translate(xCoordinate+shift.getWidth(),yCoordinate+shift.getHeight());
            g2.drawImage(image,transformation,null);
        }
    }

    /**
     * Changes the center and the angle for this element
     * @param p
     * 		the new center point
     * @param angle
     * 		the new angle for rotation
     */
    public void rotate(Point p, double angle)
    {
        rotationCenter.setLocation(p);
        rotationAngle = angle;
    }
    
    /**
     * Sets the dimension for the transaltion
     * @param d
     * 		the dimension of the translation
     */
    public void shift(Dimension d)
    {
        shift.setSize(d);
    }
    
    /**
     * Returns the height of this element
     * @return
     * 	the height
     */
    @Override
	public int getHeight()
    {
    	return image == null ? 0 : image.getHeight();
    }

    /**
     * Returns the width of this element
     * @return
     * 		the width
     */
    @Override
	public int getWidth()
    {
    	return image == null ? 0 : image.getWidth();
    }
}
