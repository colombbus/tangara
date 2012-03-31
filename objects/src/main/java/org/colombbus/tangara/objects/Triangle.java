package org.colombbus.tangara.objects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

@SuppressWarnings("serial")
@Localize(value="Triangle",localizeParent=true)
public class Triangle extends TGraphicalObject
{
	private static final Color DRAW_COLOR = Color.BLACK;
	private Color fillColor = Color.WHITE;
	private Point[] summit = new Point[3];
	private int initialWidth=0;
	private int initialHeight=0;
	
    @Localize(value="Triangle")
    public Triangle()
	{
		setObjectLocation(0, 0);
        this.setOpaque(false);
		setSummitsLocations(0,0,25,25,50,0);
        displayObject();
	}
	
    @Localize(value="Triangle")
    public Triangle(int x1, int y1, int x2, int y2, int x3, int y3)
	{
		this();
		setSummitsLocations(x1,y1,x2,y2,x3,y3);
	}

    /**
     * Sets the pen color.
     * @param colorName
     */
    @Localize(value="common.setColor")
    public void setColor(String colorName)
    {
    	fillColor = TColor.translateColor(colorName, Color.black);
    	repaint();
    }
    
    @Localize(value="Triangle.setSummitsLocations")
    public void setSummitsLocations(int x1,int y1, int x2, int y2, int x3, int y3)
    {
    	int xMin = Math.min(x1, x2);
    	xMin = Math.min(xMin, x3);
    	int xMax = Math.max(x1, x2);
    	xMax = Math.max(xMax, x3);
    	int yMin = Math.min(y1, y2);
    	yMin = Math.min(yMin, y3);
    	int yMax = Math.max(y1, y2);
    	yMax = Math.max(yMax, y3);
    	initialWidth = xMax-xMin;
    	initialHeight = yMax-yMin;
    	summit[0] = new Point(x1-xMin, y1-yMin);
    	summit[1] = new Point(x2-xMin, y2-yMin);
    	summit[2] = new Point(x3-xMin, y3-yMin);
    	setObjectLocation(xMin,yMin);
    	setObjectWidth(initialWidth+1);
    	setObjectHeight(initialHeight+1);
    	//repaint();
    }
    
    @Override
	public void paintComponent(Graphics g)
    {
    	Polygon triangle = new Polygon();
    	float xCoef = (float)(getObjectWidth()-1.0)/initialWidth;
    	float yCoef = (float)(getObjectHeight()-1.0)/initialHeight;
    	triangle.addPoint((int)(summit[0].x*xCoef), (int)(summit[0].y*yCoef));
    	triangle.addPoint((int)(summit[1].x*xCoef), (int)(summit[1].y*yCoef));
    	triangle.addPoint((int)(summit[2].x*xCoef), (int)(summit[2].y*yCoef));
    	g.setColor(fillColor);
    	g.fillPolygon(triangle);
    	g.setColor(DRAW_COLOR);
    	g.drawPolygon(triangle);
    }
}
