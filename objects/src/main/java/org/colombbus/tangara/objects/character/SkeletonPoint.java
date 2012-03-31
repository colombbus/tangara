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
 * PointSquelette.java
 *
 * Created on 25 novembre 2006, 23:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.colombbus.tangara.objects.character;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.AffineTransform;

/**
 * This enables to create skeleton points. These points are used as centers of rotation (for instance shoulders).
 * @author benoit
 */
class SkeletonPoint
{
	private String name;
	private int xCoordinate;
    private int yCoordinate;
    private boolean visible = true;
    private final Point rotationCenter = new Point();
    private double rotationAngle;
    
    /**
     * Creates a new instance of SkeletonPoint and sets its name
     * @param name
     * 		the skeleton point name
     */
    public SkeletonPoint(String name)
    {
    	this.name = name;
    }
    
    /**
     * Creates a new instance of SkeletonPoint, sets its name and its coordinates.
     * @param name
     * 		the skeleton point name
     * @param x
     * 		the x-coordinate
     * @param y
     * 		the y-coordinate
     */
    public SkeletonPoint(String name, int x, int y)
    {
    	this.name = name;
        xCoordinate = x;
        yCoordinate = y;
    }
    
    /**
     * Gets the skeleton point coordinates after rotation
     * @return
     * 		the skeleton point coordinates
     */
    public Point getCoordinates()
    {
    	Point ptSrc = new Point(xCoordinate,yCoordinate);
		if (rotationAngle!=0)
    	{
    		AffineTransform transformation = new AffineTransform();
            transformation.rotate(Math.toRadians(rotationAngle),rotationCenter.x,rotationCenter.y);
    		Point coordinates = new Point();
    		transformation.transform(ptSrc,coordinates);
    		return coordinates;
    	}
    	else
    		return ptSrc;
    }
    
    
    /**
     * Draws this skeleton point
     * @param g
     * 		the Graphics context in which to paint.
     */
    public void paint(Graphics g)
    {
        if (visible)
        {
            g.setColor(Color.RED);
            g.drawLine(xCoordinate-5,yCoordinate,xCoordinate+5,yCoordinate);
            g.drawLine(xCoordinate,yCoordinate-5,xCoordinate,yCoordinate+5);
            g.setColor(Color.BLACK);
            g.drawLine(xCoordinate-5,yCoordinate+1,xCoordinate+5,yCoordinate+1);
            g.drawLine(xCoordinate+1,yCoordinate-5,xCoordinate+1,yCoordinate+5);
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

	public String getName() {
		return name;
	}
    
}   
