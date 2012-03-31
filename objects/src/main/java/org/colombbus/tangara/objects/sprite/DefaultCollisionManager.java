package org.colombbus.tangara.objects.sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import org.colombbus.tangara.objects.Sprite;

public class DefaultCollisionManager implements CollisionManager {

	protected Sprite managedSprite;
	protected ArrayList<Sprite> lastEncounteredSprites;
	protected boolean init;
	
	
	public DefaultCollisionManager(Sprite aSprite)
	{
		managedSprite = aSprite;
		lastEncounteredSprites = new ArrayList<Sprite>();
		init = false;
	}
	
	@Override
	public void drawCollisionArea(Graphics g) {
		Rectangle r = new Rectangle(0,0,managedSprite.getObjectWidth()-1,managedSprite.getObjectHeight()-1);
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.RED);
		g2.draw(r);
	}

	@Override
	public synchronized void testCollision(int x, int y, List<Sprite> sprites, boolean forceProcessing) {
	    if (init) {
	        lastEncounteredSprites.clear();
	        init = false;
	    }
		List<Sprite> copyList = new ArrayList<Sprite>(sprites);
		ArrayList<Sprite> encounteredSprites = new ArrayList<Sprite>();
		Rectangle testRectangle = managedSprite.getObjectBounds();
		testRectangle.x = x;
		testRectangle.y = y;
		for (Sprite s:copyList)
		{
			if ( s!=null && s!=managedSprite && !s.isHidden())
			{
				if (s.intersects(testRectangle))
				{
				    encounteredSprites.add(s);
				    if (!lastEncounteredSprites.contains(s)) {
				        managedSprite.processCollision(x,y,s);
				        s.processCollision(x,y,managedSprite);
				    } else if (forceProcessing) {
				        managedSprite.processCollision(x,y,s);
				    }
				}
			}
		}
		lastEncounteredSprites = encounteredSprites;
	}

	@Override
	public boolean testIntersection(Rectangle r) {
		return r.intersects(managedSprite.getObjectBounds());
	}

	@Override
	public boolean testIntersection(Area a) {
		return (a.intersects(managedSprite.getObjectBounds()));
	}

	@Override
	public void updateLocation(int x, int y) {
		// nothing has to be done
	}
	
	@Override
	public int getAreaTop() 
	{
		return managedSprite.getObjectY();
	}

    @Override
    public int getAreaLeft() 
    {
        return managedSprite.getObjectX();
    }

    @Override
    public int getAreaRight() 
    {
        return managedSprite.getObjectX()+managedSprite.getObjectWidth();
    }
    
	@Override
	public int getAreaBottom() 
	{
		return managedSprite.getObjectY()+managedSprite.getObjectHeight();
	}

	@Override
	public Rectangle getRelativeBounds()
	{
		return new Rectangle(0,0,managedSprite.getObjectWidth(), managedSprite.getObjectHeight());
	}

    @Override
    public Rectangle getAbsoluteBounds()
    {
        return managedSprite.getObjectBounds();
    }

    @Override
    public synchronized void init()
    {
        init = true;
    }
}
