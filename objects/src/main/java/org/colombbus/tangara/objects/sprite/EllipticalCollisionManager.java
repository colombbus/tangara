package org.colombbus.tangara.objects.sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import org.colombbus.tangara.objects.Sprite;

public class EllipticalCollisionManager implements CollisionManager {

	private Ellipse2D.Double collisionEllipse;
	private Area absoluteCollisionEllipse;
	private Sprite managedSprite;
	protected ArrayList<Sprite> lastEncounteredSprites;
	protected boolean init;

	public EllipticalCollisionManager(int x, int y, int width, int height, Sprite aSprite)
	{
		collisionEllipse = new Ellipse2D.Double(x,y,width,height);
		absoluteCollisionEllipse = new Area(collisionEllipse);
		managedSprite = aSprite;
		lastEncounteredSprites = new ArrayList<Sprite>();
		init = false;
	}
	
	@Override
	public void drawCollisionArea(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.RED);
		g2.draw(collisionEllipse);
	}

	@Override
	public void testCollision(int x, int y, List<Sprite> sprites, boolean forceProcessing) {
       if (init) {
            lastEncounteredSprites.clear();
            init = false;
        }
		ArrayList<Sprite> copyList = new ArrayList<Sprite>(sprites);
		ArrayList<Sprite> encounteredSprites = new ArrayList<Sprite>();
		Area testEllipse = new Area(new Ellipse2D.Double(collisionEllipse.x+x,collisionEllipse.y+y,collisionEllipse.width,collisionEllipse.height));
		for (Sprite s:copyList)
		{
			if ( s!=null && s!=managedSprite && !s.isHidden())
			{
				if (s.intersects(testEllipse))
				{
                    encounteredSprites.add(s);
                    if (!lastEncounteredSprites.contains(s)) {
                        managedSprite.processCollision(x,y,s);
                        s.processCollision(x,y,managedSprite);
                    }  else if (forceProcessing) {
				        managedSprite.processCollision(x,y,s);
				    }
				}
			}
		}
        lastEncounteredSprites = encounteredSprites;
	}

	@Override
	public boolean testIntersection(Rectangle r) {
		return absoluteCollisionEllipse.intersects(r);
	}

	@Override
	public boolean testIntersection(Area a) {
		Area collisionArea = new Area(absoluteCollisionEllipse);
		collisionArea.intersect(a);
		return !collisionArea.isEmpty();
	}

	@Override
	public void updateLocation(int x, int y) {
		absoluteCollisionEllipse = new Area(new Ellipse2D.Double(x+collisionEllipse.x,y+collisionEllipse.y,collisionEllipse.width,collisionEllipse.height));
	}

	@Override
	public int getAreaTop() 
	{
		return absoluteCollisionEllipse.getBounds().y;
	}
	
	@Override
	public int getAreaBottom() 
	{
		Rectangle r = absoluteCollisionEllipse.getBounds();
		return r.y+r.height;
	}
	
    @Override
    public int getAreaLeft() 
    {
        Rectangle r = absoluteCollisionEllipse.getBounds();
        return r.x;
    }

    @Override
    public int getAreaRight() 
    {
        Rectangle r = absoluteCollisionEllipse.getBounds();
        return r.x+r.width;
    }

	@Override
	public Rectangle getRelativeBounds()
	{
		return new Area(collisionEllipse).getBounds();
	}

    @Override
    public Rectangle getAbsoluteBounds()
    {
        Rectangle bounds = new Area(absoluteCollisionEllipse).getBounds();
        return bounds;
    }
	
    @Override
    public void init()
    {
        init = true;
    }
    
}
