package org.colombbus.tangara.objects;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

@SuppressWarnings("serial")
@Localize(value="Curve",localizeParent=true)
public class Curve extends TGraphicalObject
{
	protected static final int STYLE_POINTS = 0;
	protected static final int STYLE_LINES = 1;
	protected static final int STYLE_LOOP = 2;
	protected static final int STYLE_LINES_AND_POINTS = 3;
	
	Color currentColor = Color.RED;
	
	public class Point
	{
		Point(double x, double y)
		{
			this.x = x;
			this.y = y;
			this.color = currentColor;
		}
		double x;
		double y;
		Color color;
	}
	private Vector<Point> Points = new Vector<Point>();
	
	private static final Color DRAW_COLOR = Color.BLACK;

	private int style = STYLE_LINES;
	private boolean axes = true;
	private int pointSize = 4;
	private double coordMax = 0.0;
	
    @Localize(value="Curve")
    public Curve()
	{
		super();
		setObjectWidth(500);
		setObjectHeight(500);
        setOpaque(false);
		displayObject();
	}

    /**
     * Sets the pen color.
     * @param colorName
     */
    @Localize(value="Curve.setCurrentColor")
    public void setCurrentColor(String colorName)
    {
    	currentColor = TColor.translateColor(colorName, Color.RED);
    	repaint();
    }
    
    /*@Localize(value="Curve.setCenterLocation")
    public void setCenterLocation(int x, int y)
    {
    	setObjectLocation(x-(getObjectWidth()-1)/2, y-(getObjectHeight()-1)/2);
    }*/
    
    @Localize(value="Curve.setPointSize")
    public void setPointSize(int pointSize)
    {
    	pointSize = 2*pointSize;
    	repaint();
    }
    
    @Localize(value="Curve.setStyle")
    public void setStyle(String styleName)
    {
    	styleName = styleName.toLowerCase();
    	if(styleName.equals(this.getMessage("style.points")))
    	{
    		style = STYLE_POINTS;
    	}
    	else if(styleName.equals(this.getMessage("style.lines")))
    	{
    		style = STYLE_LINES;
    	}
    	else if(styleName.equals(this.getMessage("style.loop")))
    	{
    		style = STYLE_LOOP;
    	}
    	else if(styleName.equals(this.getMessage("style.lines_and_points")))
    	{
    		style = STYLE_LINES_AND_POINTS;
    	}
    	else
    	{
    		style = STYLE_LINES;
    	}
    	repaint();
    }
    
    @Localize(value="Curve.showAxes")
    public void showAxes(boolean Axes)
    {
    	this.axes = Axes;
    	repaint();
    }
    
    @Localize(value="Curve.addPoint")
    public void addPoint(double x, double y)
    {
    	Points.add(new Point(x,y));
    	repaint();
    }
    
    @Localize(value="Curve.insertPoint")
    public void insertPoint(double x, double y, int index)
    {
    	Points.insertElementAt(new Point(x,y), index-1);
    	repaint();
    }
    
    @Localize(value="Curve.getNumberOfPoint")
    public int getNumberOfPoint()
    {
    	return Points.size();
    }
    
    @Localize(value="Curve.getXCoordinate")
    public double getXCoordinate(int index)
    {
    	if (index<=0 || index>Points.size()) {
            Program.instance().writeMessage(getMessage("error.incorrectIndex"));
    		return -1;
    	}
    	return Points.get(index-1).x;
    }
    
    @Localize(value="Curve.getYCoordinate")
    public double getYCoordinate(int index)
    {
    	if (index<=0 || index>Points.size()) {
            Program.instance().writeMessage(getMessage("error.incorrectIndex"));
    		return -1;
    	}
    	return Points.get(index-1).y;
    }
    
    @Localize(value="Curve.clearPoints")
    public void clearPoints()
    {
    	Points = new Vector<Point>();
    	coordMax = 10;
    	repaint();
    }
    
    @Localize(value="Curve.clearLastPoint")
    public void clearLastPoint()
    {
    	if(Points.size() > 0)
    	{
    		int lastPoint = Points.size()-1;
        	Points.remove(lastPoint);
        	coordMax = 10;
        	repaint();
    	}
    }
    
    @Override
	public void paintComponent(Graphics g)
    {
    	g.setColor(DRAW_COLOR);
    	
    	// Shows the axes if axes is true;
    	if(axes)
    	{
    		g.drawLine(getObjectWidth()/2, getObjectHeight(), getObjectWidth()/2, 0);
        	g.drawLine(0, getObjectHeight()/2, getObjectWidth(), getObjectHeight()/2);
    	}
    	
    	// Gets the highest coordinate and draws the curve consequently.
    	coordMax = 0.0;
		for (int j = 0; j < Points.size(); j++)
		{
			if(Math.abs(Points.get(j).x) > Math.abs(Points.get(j).y))
			{
				if(Math.abs(Points.get(j).x) > coordMax)
				{
					coordMax = Math.floor(Math.abs(Points.get(j).x))+1;
				}
			}
			else
			{
				if(Math.abs(Points.get(j).y) > coordMax)
				{
					coordMax = Math.floor(Math.abs(Points.get(j).y))+1;
				}
			}
		}
    	
    	if(Points.size() == 1) // If there is only one point, the choice of the style is not given.
    	{
    		g.setColor(Points.get(0).color);
    		g.fillOval((int)(getObjectWidth()*(Points.get(0).x+coordMax)/(2*coordMax))-pointSize/2, getObjectHeight()-(int)(getObjectHeight()*(Points.get(0).y+coordMax)/(2*coordMax))-pointSize/2, pointSize, pointSize);
    	}
    	
    	else if(Points.size() > 1)
    	{    		
    		if(style == STYLE_POINTS)
    		{
    			for (int i = 0; i < Points.size(); i++)
    			{
    				g.setColor(Points.get(i).color);
    				g.fillOval((int)(getObjectWidth()*(Points.get(i).x+coordMax)/(2*coordMax))-pointSize/2, getObjectHeight()-(int)(getObjectHeight()*(Points.get(i).y+coordMax)/(2*coordMax))-pointSize/2, pointSize, pointSize);
    			}
    		}
    		else if (style == STYLE_LINES)
    		{
    			int Abscisse1 = (int)(getObjectWidth()*(Points.get(0).x+coordMax)/(2*coordMax));
    			int Ordonnee1 = (int)(getObjectHeight()*(Points.get(0).y+coordMax)/(2*coordMax));
    			int Abscisse2;
    			int Ordonnee2;
    			for (int i = 1; i < Points.size(); i++)
    			{
    				Abscisse2 = (int)(getObjectWidth()*(Points.get(i).x+coordMax)/(2*coordMax));
            		Ordonnee2 = (int)(getObjectHeight()*(Points.get(i).y+coordMax)/(2*coordMax));
            		g.setColor(Points.get(i).color);
            		g.drawLine(Abscisse1, getObjectHeight()-Ordonnee1, Abscisse2, getObjectHeight()-Ordonnee2);
            		Abscisse1 = Abscisse2;
            		Ordonnee1 = Ordonnee2;
    			}
    		}
    		else if (style == STYLE_LINES_AND_POINTS)
    		{
    			g.setColor(Points.get(0).color);
    			g.fillOval((int)(getObjectWidth()*(Points.get(0).x+coordMax)/(2*coordMax))-pointSize/2, getObjectHeight()-(int)(getObjectHeight()*(Points.get(0).y+coordMax)/(2*coordMax))-pointSize/2, pointSize, pointSize);
    			int Abscisse1 = (int)(getObjectWidth()*(Points.get(0).x+coordMax)/(2*coordMax));
    			int Ordonnee1 = (int)(getObjectHeight()*(Points.get(0).y+coordMax)/(2*coordMax));
    			int Abscisse2;
    			int Ordonnee2;
    			for (int i = 1; i < Points.size(); i++)
    			{
    				g.setColor(Points.get(i).color);
    				g.fillOval((int)(getObjectWidth()*(Points.get(i).x+coordMax)/(2*coordMax))-pointSize/2, getObjectHeight()-(int)(getObjectHeight()*(Points.get(i).y+coordMax)/(2*coordMax))-pointSize/2, pointSize, pointSize);
    				Abscisse2 = (int)(getObjectWidth()*(Points.get(i).x+coordMax)/(2*coordMax));
            		Ordonnee2 = (int)(getObjectHeight()*(Points.get(i).y+coordMax)/(2*coordMax));
            		g.drawLine(Abscisse1, getObjectHeight()-Ordonnee1, Abscisse2, getObjectHeight()-Ordonnee2);
            		Abscisse1 = Abscisse2;
            		Ordonnee1 = Ordonnee2;
    			}
    		}
    		else if (style == STYLE_LOOP)
    		{
    			int Abscisse1 = (int)(getObjectWidth()*(Points.get(0).x+coordMax)/(2*coordMax));
    			int Ordonnee1 = (int)(getObjectHeight()*(Points.get(0).y+coordMax)/(2*coordMax));
    			int TempX = Abscisse1;
    			int TempY = Ordonnee1;
    			int Abscisse2;
    			int Ordonnee2;
    			for (int i = 1; i < Points.size(); i++)
    			{
    				Abscisse2 = (int)(getObjectWidth()*(Points.get(i).x+coordMax)/(2*coordMax));
            		Ordonnee2 = (int)(getObjectHeight()*(Points.get(i).y+coordMax)/(2*coordMax));
            		g.setColor(Points.get(i).color);
            		g.drawLine(Abscisse1, getObjectHeight()-Ordonnee1, Abscisse2, getObjectHeight()-Ordonnee2);
            		Abscisse1 = Abscisse2;
            		Ordonnee1 = Ordonnee2;
    			}
    			g.setColor(Points.get(0).color);
    			g.drawLine(Abscisse1, getObjectHeight()-Ordonnee1, TempX, getObjectHeight()-TempY);
    		}
    	}
    }
}

