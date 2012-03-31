package org.colombbus.tangara.objects;

import java.awt.Color;
import java.awt.Graphics;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

@SuppressWarnings("serial")
@Localize(value="Rectangle",localizeParent=true)
public class Rectangle extends TGraphicalObject
{
	private static final Color DRAW_COLOR = Color.BLACK;
	private Color fillColor = Color.WHITE;
	
    @Localize(value="Rectangle")
    public Rectangle()
	{
		super();
		setObjectLocation(0, 0);
		setObjectWidth(50);
		setObjectHeight(50);
		displayObject();
	}
	
    @Localize(value="Rectangle")
    public Rectangle(int x1, int y1, int x2, int y2)
	{
		this();
		setSummitsLocations(x1,y1,x2,y2);
	}

    /**
     * Sets the pen color.
     * @param colorName
     */
    @Localize(value="common.setColor")
    public void setColor(String colorName)
    {
		Color c = TColor.translateColor(colorName, Color.black);
    	fillColor = c;
    	repaint();
    }
    
    @Localize(value="Rectangle.setSummitsLocations")
    public void setSummitsLocations(int x1,int y1, int x2, int y2)
    {
		int x = Math.min(x1, x2);
		int y = Math.min(y1, y2);
		int width = Math.abs(x1-x2);
		int height = Math.abs(y1-y2);
		setObjectLocation(x, y);
		setObjectWidth(width+1);
		setObjectHeight(height+1);
    }
    
    @Override
	public void paintComponent(Graphics g)
    {
    	g.setColor(fillColor);
    	g.fillRect(0, 0, getObjectWidth(), getObjectHeight());
    	g.setColor(DRAW_COLOR);
    	g.drawRect(0, 0, getObjectWidth()-1, getObjectHeight()-1);
    }
}
