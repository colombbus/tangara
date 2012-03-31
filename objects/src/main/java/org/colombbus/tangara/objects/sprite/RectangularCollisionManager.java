package org.colombbus.tangara.objects.sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import org.colombbus.tangara.objects.Sprite;

public class RectangularCollisionManager implements CollisionManager {

	private Rectangle collisionRectangle;
	private Rectangle absoluteCollisionRectangle;
	private Sprite managedSprite;
	protected ArrayList<Sprite> lastEncounteredSprites;
    protected boolean init;

	public RectangularCollisionManager(int x, int y, int width, int height, Sprite aSprite)
	{
		collisionRectangle = new Rectangle(x,y,width,height);
		absoluteCollisionRectangle = new Rectangle(collisionRectangle);
		managedSprite = aSprite;
		lastEncounteredSprites = new ArrayList<Sprite>();
		init = false;
	}
	
	@Override
	public void drawCollisionArea(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.RED);
		g2.draw(collisionRectangle);
	}

	@Override
	public void testCollision(int x, int y, List<Sprite> spriteList, boolean forceProcessing) {
	    if (init) {
	        lastEncounteredSprites.clear();
	        init = false;
	    }
		List<Sprite> copyList = new ArrayList<Sprite>(spriteList);
		ArrayList<Sprite> encounteredSprites = new ArrayList<Sprite>();

		Rectangle testRectangle;
		testRectangle = new Rectangle(collisionRectangle);
		testRectangle.x+=x;
		testRectangle.y+=y;
		for (Sprite sprite:copyList)
		{
			if (sprite!=null&& sprite!=managedSprite && !sprite.isHidden())
			{
				if (sprite.intersects(testRectangle))
				{
                    encounteredSprites.add(sprite);
                    if (!lastEncounteredSprites.contains(sprite)) {
                        managedSprite.processCollision(x,y,sprite);
                        sprite.processCollision(x,y,managedSprite);
                    } else if (forceProcessing) {
				        managedSprite.processCollision(x,y,sprite);
				    }
				}
			}
		}
        lastEncounteredSprites = encounteredSprites;
	}

	@Override
	public boolean testIntersection(Rectangle r) {
		return r.intersects(absoluteCollisionRectangle);
	}

	@Override
	public boolean testIntersection(Area a) {
		return (a.intersects(absoluteCollisionRectangle));
	}

	@Override
	public void updateLocation(int x, int y) {
		absoluteCollisionRectangle = new Rectangle(collisionRectangle);
		absoluteCollisionRectangle.x+=x;
		absoluteCollisionRectangle.y+=y;
	}
	
	@Override
	public int getAreaTop() 
	{
		return absoluteCollisionRectangle.y;
	}

	@Override
	public int getAreaBottom() 
	{
		return absoluteCollisionRectangle.y+absoluteCollisionRectangle.height;
	}

    @Override
    public int getAreaLeft() 
    {
        return absoluteCollisionRectangle.x;
    }

    @Override
    public int getAreaRight() 
    {
        return absoluteCollisionRectangle.x+absoluteCollisionRectangle.width;
    }
	
	@Override
	public Rectangle getRelativeBounds()
	{
		return new Rectangle(collisionRectangle);
	}
	
    @Override
    public Rectangle getAbsoluteBounds()
    {
        Rectangle bounds = new Rectangle(absoluteCollisionRectangle);
        return bounds;
    }
    
    @Override
    public void init()
    {
        init = true;
    }   

}
