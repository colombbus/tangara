package org.colombbus.tangara.objects.sprite;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;

import org.colombbus.tangara.objects.Sprite;

public interface CollisionManager {

	void updateLocation(int x, int y);
	
	boolean testIntersection(Rectangle r);
	
	boolean testIntersection(Area a);
	
	void drawCollisionArea(Graphics g);
	
	void testCollision(int x, int y, List<Sprite> sprites, boolean forceProcessing);
	
	void init();
	
	int getAreaTop();
	
	int getAreaBottom();

	int getAreaLeft();

    int getAreaRight();

	Rectangle getRelativeBounds();
	
	Rectangle getAbsoluteBounds();
	
}
