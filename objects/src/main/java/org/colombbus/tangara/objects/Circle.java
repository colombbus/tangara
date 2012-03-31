package org.colombbus.tangara.objects;

import java.awt.Color;
import java.awt.Graphics;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

@SuppressWarnings("serial")
@Localize(value="Circle",localizeParent=true)
public class Circle extends TGraphicalObject
{
	private static final Color DRAW_COLOR = Color.BLACK;

	private Color fillColor = Color.WHITE;
	
    @Localize(value="Circle")
    public Circle()
	{
		super();
		setObjectLocation(0, 0);
		setObjectWidth(50);
		setObjectHeight(50);
        setOpaque(false);
		displayObject();
	}
	
    @Localize(value="Circle")
    public Circle(int x1, int y1, int r)
	{
		this();
		int x = x1-r;
		int y = y1-r;
		int width = 2*r;
		int height = 2*r;
		setObjectLocation(x, y);
		setObjectWidth(width);
		setObjectHeight(height);
	}

    /**
     * Sets the pen color.
     * @param colorName
     */
    @Localize(value="common.setColor")
    public void setColor(String colorName)
    {
    	fillColor = TColor.translateColor(colorName, Color.BLACK);
    	repaint();
    }
    
    @Localize(value="Circle.setRadius")
    public void setRadius(int value)
    {
    	int diameter = value*2+1;
    	setObjectWidth(diameter);
    	setObjectHeight(diameter);

    	int x = getObjectX()+getObjectWidth()/2;
    	int y = getObjectY()+getObjectHeight()/2;
    	setObjectLocation(x-value,y-value);
    }
    
    @Localize(value="Circle.setCenterLocation")
    public void setCenterLocation(int x, int y)
    {
    	setObjectLocation(x-(getObjectWidth()-1)/2, y-(getObjectHeight()-1)/2);
    }
    
    @Override
	public void paintComponent(Graphics g)
    {
    	g.setColor(fillColor);
    	g.fillOval(0, 0, getObjectWidth()-1, getObjectHeight()-1);
    	g.setColor(DRAW_COLOR);
    	g.drawOval(0, 0, getObjectWidth()-1, getObjectHeight()-1);
    }
}
