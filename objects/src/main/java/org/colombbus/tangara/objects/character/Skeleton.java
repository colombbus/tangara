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
 * Squelette.java
 *
 * Created on 25 novembre 2006, 23:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.colombbus.tangara.objects.character;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * 
 * @author benoit
 */
public class Skeleton
{
    private static final String Y_COORD_A = "coordinateY";
	private static final String X_COORD_A = "coordinateX";
	private static final String IMAGE_A = "image";
	private static final String NAME_A = "name";
	public static final String LEFT_SHOULDER = "leftShoulder";
    public static final String RIGHT_SHOULDER = "rightShoulder";
    public static final String LEFT_ARM = "leftArm";
    public static final String RIGHT_ARM = "rightArm";
    public static final String CHEST = "chest";
    public static final String TAIL = "tail";
    public static final String TAIL_BASE = "tailBasis";
    public static final String LEFT_HAND = "leftHand";
    public static final String RIGHT_HAND = "rightHand";
    
    private final List<SkeletonElement> orderedElemList = new ArrayList<SkeletonElement>();
    private final Map<String, SkeletonElement> elemMap = new HashMap<String, SkeletonElement>();
    private final Map<String, SkeletonPoint> pointMap = new HashMap<String, SkeletonPoint>();
    

    private int height = 0;
    private int width = 0;
   
    private boolean rightArm = false;
    private boolean leftArm = false;
    private boolean tail = false;
    private boolean chest = false;
    
    public Skeleton()
    {
    }
    
    
 
    public void addElement(String name, URL resourcePath, String image, int x,
			int y) throws Exception {
		SkeletonElement elem = new SkeletonElement(name, resourcePath, image,
				x, y);
		addElement(elem);
	}
    
    private void addElement( SkeletonElement elem) throws Exception {
    	String name = elem.getElementName();
		if (elemMap.containsKey(name)) {
			throw new Exception("Skeleton already contains an element named \""
					+ name + "\"");
		} else {
			orderedElemList.add(elem);
			elemMap.put(name, elem);
		}    	
    }
 
    public void addElement(String name, URL resourcePath, String image) throws Exception
    {
		SkeletonElement elem = new SkeletonElement(name, resourcePath, image);
		addElement(elem);
    }
    
    public void addPoint(String name) throws Exception
    {
    	SkeletonPoint point = new SkeletonPoint(name);
    	addPoint( point );
    }
    
    private void addPoint( SkeletonPoint point ) throws Exception {
    	String name = point.getName();
    	if( pointMap.containsKey(name)) {
            throw new Exception("Skeleton already contains a point named \""+name+"\"");    		
    	}
    	pointMap.put(name, point);    	
    }
    
    public void addPoint(String name, int x, int y) throws Exception
    {    	
    	SkeletonPoint point = new SkeletonPoint(name, x,y);
    	addPoint(point);
    }
    

    public void paintAt(Graphics g, Point origine)
    {
    	for( SkeletonElement elem : orderedElemList) {
    		elem.paintElement(g, origine.x, origine.y);
    	}
   }
    
    public void readSkeleton(String skeletonName, URL url) throws Exception
    {
        try
        {
            // read the JAR file
            loadSkeletonJar(skeletonName, url);
            computeSize();
            updateState();
        }
        catch(ParserConfigurationException e)
        {
            throw new Exception("Configuration error for DOM parser while calling factory.newDocumentBuilder();");
        }
        catch(SAXException se)
        {
            throw new Exception("Parsing error while calling builder.parse(xml)");
        }
        catch(IOException ioEx)
        {
            throw new Exception("IO error while calling builder.parse(xml)", ioEx);
        }
    }

	private void reset() {
		elemMap.clear();
		pointMap.clear();
		orderedElemList.clear();
	}

	private void loadSkeletonJar(String skeletonName, URL url) throws Exception,
			ParserConfigurationException, SAXException, IOException {
		File tmpSkeleton = JARUtils.extractFileFromJar(url,"skeleton.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(tmpSkeleton);
		tmpSkeleton.delete();
		Element root = document.getDocumentElement();
		NodeList elements = root.getElementsByTagName("element");
		if (elements.getLength()<=0)
		{
		    throw new Exception("No element found in "+skeletonName);
		}
		reset();

		for (int i=0;i<elements.getLength();i++)
		{
		    Element element = (Element)elements.item(i);
		    if (element.hasAttribute(NAME_A) == false
					|| element.hasAttribute(IMAGE_A) == false)
		    {
		        throw new Exception("file "+skeletonName+" corrupted");
		    }
		    String name = element.getAttribute(NAME_A);
		    String image = element.getAttribute(IMAGE_A);
		    if (element.hasAttribute(X_COORD_A)
					&& element.hasAttribute(Y_COORD_A))
		    {
		    	String xStr = element.getAttribute(X_COORD_A);
				int x = Integer.parseInt(xStr);
				String yStr = element.getAttribute(Y_COORD_A);
				int y = Integer.parseInt(yStr);
				addElement(name, url, image, x, y);
		    }
		    else
		    {
		        addElement(name, url, image);
		    }
		}
		NodeList points = root.getElementsByTagName("point");
		for (int i=0;i<points.getLength();i++)
		{
		    Element element = (Element)points.item(i);
		    if (element.hasAttribute(NAME_A)==false)
		    {
		        throw new Exception("file "+skeletonName+" corrupted");
		    }
		    String nom = element.getAttribute(NAME_A);
		    if (element.hasAttribute(X_COORD_A)
					&& element.hasAttribute(Y_COORD_A))
		    {
		        int x = Integer.parseInt(element.getAttribute(X_COORD_A));
		        int y = Integer.parseInt(element.getAttribute(Y_COORD_A));
		        addPoint(nom,x,y);
		    }
		    else
		    {
		        addPoint(nom);
		    }
		}
	}

    public void rotateElement(String pointName, String elementName, double angle) throws Exception
    {
        SkeletonPoint point = pointMap.get(pointName);
        Point center = point.getCoordinates();

        SkeletonElement element = elemMap.get(elementName);
        element.rotate(center, angle);
    }
    
    public void rotatePoint(String centerName, String pointName, double angle) throws Exception
    {
        SkeletonPoint p = pointMap.get(centerName);
        Point center = p.getCoordinates();
        SkeletonPoint point = pointMap.get(pointName);
        point.rotate(center, angle);
    }

    public void shilftElement(String elementName, Dimension shiftValue) throws Exception
    {
    	SkeletonElement e = elemMap.get(elementName);
        e.shift(shiftValue);
    }
    
    
    
    private void computeSize()
    {
    	int maxX = 0;
    	int maxY = 0;
        for (SkeletonElement elem : elemMap.values())
        {
        	Point elementOrigin = elem.getCoordinates();
        	maxX = Math.max(maxX,elementOrigin.x+elem.getWidth());
        	maxY = Math.max(maxY,elementOrigin.y+elem.getHeight());
        }
        width = maxX;
        height = maxY;
    }
    
    private int[] computeMarginsForAnElement(int[] max, String elementName, String pointName) throws Exception
    {
		SkeletonElement arm = elemMap.get(elementName);
		Point center = pointMap.get(pointName).getCoordinates();
		int left = Math.min(arm.getCoordinates().x,center.x);
		int right = Math.max(arm.getCoordinates().x+arm.getWidth(),center.x);
		int top = Math.min(arm.getCoordinates().y,center.y);
		int bottom = Math.max(arm.getCoordinates().y+arm.getHeight(),center.y);
		int hSide = Math.max(center.x-left, right-center.x);
		int vSide = Math.max(center.y-top, bottom-center.y);
		int maxSide = (int)Math.sqrt(hSide*hSide+vSide*vSide);

		int[] newMax = new int[4];
		newMax[0] = Math.min(center.y-maxSide,max[0]);
		newMax[1] = Math.min(center.x-maxSide,max[1]);
		newMax[2] = Math.max(center.x+maxSide, max[2]);
		newMax[3] = Math.max(center.y+maxSide, max[3]);
		return newMax;
    }
    
    // Returns : top, left, right, bottom
    // We assume that chest won't go outside the initial box
    public int[] computeMargins() throws Exception
    {
    	int[] max = new int[4];
    	max[0] = 0;
    	max[1] = 0;
    	max[2] = getWidth();
    	max[3] = getHeight();
    	
    	if (hasLeftArm())
    		max = computeMarginsForAnElement(max,LEFT_ARM,LEFT_SHOULDER);
    	if (hasRightArm())
    		max = computeMarginsForAnElement(max,RIGHT_ARM,RIGHT_SHOULDER);
    	if (hasTail())
    		max = computeMarginsForAnElement(max,TAIL,TAIL_BASE);

    	int[] margins = new int[4];
    	margins[0] = -max[0];
    	margins[1] = -max[1];
    	margins[2] = max[2]-getWidth();
    	margins[3] = max[3]-getHeight();
    	return margins;
    }
    
    //
    private void updateState()
    {
    	leftArm = elementPresent(LEFT_ARM) && pointPresent(LEFT_SHOULDER);
    	rightArm = elementPresent(RIGHT_ARM) && pointPresent(RIGHT_SHOULDER);
    	chest = elementPresent(CHEST);
    	tail = elementPresent(TAIL) && pointPresent(TAIL_BASE);
    }
    
    private boolean elementPresent(String elementName) {
    	return elemMap.containsKey(elementName);
    }
    
    private boolean pointPresent( String pointName ) {
    	return pointMap.containsKey(pointName);
    }
    
    /**
     * @return <code>true</code> if there is a left arm and a left shoulder
     */
    public boolean hasLeftArm()
    {
    	return leftArm;
    }

    /**
     * @return <code>true</code> if there is a right arm and a right shoulder
     */
    public boolean hasRightArm()
    {
    	return rightArm;
    }

    /** 
     * @return true if there is a tail and a tail base
     */
    public boolean hasTail()
    {
    	return tail;
    }
    
    /**
     * @return <code>true</code> if there is a "body" element
     */
    public boolean hasChest()
    {
    	return chest;
    }

    public int getHeight()
    {
    	return height;
    }
    
    public int getWidth()
    {
    	return width;
    }
    
    public Point getPointCoordinates(String pointName) throws Exception
    {
        SkeletonPoint p = pointMap.get(pointName);
    	return p.getCoordinates();
    }
    
}
