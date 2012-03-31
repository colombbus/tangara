package org.colombbus.tangara.objects;

import java.awt.Color;

import javax.media.j3d.BoundingBox;
import javax.vecmath.Point3d;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;

@Localize(value = "Box3D", localizeParent = true)
public class Box3D extends Object3D {
	
	private Quadrilateral3D bottom;
	private Quadrilateral3D left;
	private Quadrilateral3D front;
	private Quadrilateral3D right;
	private Quadrilateral3D back;
	private Quadrilateral3D top;
	
	@Localize(value="Box3D")
	public Box3D(double sizeX, double sizeY, double sizeZ, String colorName) {
		super();
		Point3d p1 = new Point3d(-sizeX/2, -sizeY/2, sizeZ/2);
		Point3d p2 = new Point3d(sizeX/2, -sizeY/2, sizeZ/2);
		Point3d p3 = new Point3d(sizeX/2, sizeY/2, sizeZ/2);
		Point3d p4 = new Point3d(-sizeX/2, sizeY/2, sizeZ/2);
		Point3d p5 = new Point3d(-sizeX/2, -sizeY/2, -sizeZ/2);
		Point3d p6 = new Point3d(sizeX/2, -sizeY/2, -sizeZ/2);
		Point3d p7 = new Point3d(sizeX/2, sizeY/2, -sizeZ/2);
		Point3d p8 = new Point3d(-sizeX/2, sizeY/2, -sizeZ/2);
		front = new Quadrilateral3D(p1, p2, p3, p4);
		back = new Quadrilateral3D(p5, p8, p7, p6);
		bottom = new Quadrilateral3D(p1, p5, p6, p2);
		left = new Quadrilateral3D(p1, p4, p8, p5);
		right = new Quadrilateral3D(p2, p6, p7, p3);
		top = new Quadrilateral3D(p3, p7, p8, p4);
		addObject(bottom);
		addObject(left);
		addObject(front);
		addObject(right);
		addObject(back);
		addObject(top);
		setColor(colorName);
		setBounds(new BoundingBox(new Point3d(-sizeX/2, -sizeY/2, -sizeZ/2), new Point3d(sizeX/2, sizeY/2, sizeZ/2)));
		isExpandable = false;
	}
	
	@Localize(value="Box3D")
	public Box3D(double sizeX, double sizeY, double sizeZ) {
		this(sizeX, sizeY, sizeZ, "");
	}

	@Localize(value="Box3D")
	public Box3D() {
		this(1, 1, 1);
	}
	
	@Override
	@Localize(value="Object3D.setTexture")
	public void setTexture(Texture imageTexture) {
		setTextures(imageTexture, imageTexture, imageTexture, imageTexture, imageTexture, imageTexture);
	}

	@Localize(value="Box3D.setTexture2")
	public void setTexture(Texture imageTexture, double repeatX, double repeatY) {
		setTextures(imageTexture, imageTexture, imageTexture, imageTexture, imageTexture, imageTexture, repeatX, repeatY);
	}
	
	@Override
	@Localize(value="Object3D.removeTexture")
	public void removeTexture() {
		front.removeTexture();
		back.removeTexture();
		bottom.removeTexture();
		left.removeTexture();
		right.removeTexture();
	}
	
	@Localize(value="Box3D.setTextures")
	public void setTextures(Texture front, Texture back, Texture right, Texture left, Texture top, Texture bottom) {
		setTopTexture(top);
		setRightTexture(right);
		setLeftTexture(left);
		setFrontTexture(front);
		setBackTexture(back);
		setBottomTexture(bottom);
	}

	@Localize(value="Box3D.setTextures2")
	public void setTextures(Texture front, Texture back, Texture right, Texture left, Texture top, Texture bottom, double repeatX, double repeatY) {
		setTopTexture(top, repeatX, repeatY);
		setRightTexture(right, repeatX, repeatY);
		setLeftTexture(left, repeatX, repeatY);
		setFrontTexture(front, repeatX, repeatY);
		setBackTexture(back, repeatX, repeatY);
		setBottomTexture(bottom, repeatX, repeatY);
	}
	
	@Localize(value="Box3D.setTopTexture")
	public void setTopTexture(Texture imageTexture) {
		setTopTexture(imageTexture, 1, 1);
	}

	@Localize(value="Box3D.setTopTexture2")
	public void setTopTexture(Texture imageTexture, double repeatX, double repeatY) {
		top.setTexture(imageTexture, repeatX, repeatY);
	}
	
	@Localize(value="Box3D.setRightTexture")
	public void setRightTexture(Texture imageTexture) {
		setRightTexture(imageTexture,1,1);
	}

	@Localize(value="Box3D.setRightTexture2")
	public void setRightTexture(Texture imageTexture, double repeatX, double repeatY) {
		right.setTexture(imageTexture, repeatX, repeatY);
	}
	
	@Localize(value="Box3D.setLeftTexture")
	public void setLeftTexture(Texture imageTexture) {
		setLeftTexture(imageTexture, 1,1);
	}

	@Localize(value="Box3D.setLeftTexture2")
	public void setLeftTexture(Texture imageTexture, double repeatX, double repeatY) {
		left.setTexture(imageTexture, repeatX, repeatY);
	}
	
	@Localize(value="Box3D.setFrontTexture")
	public void setFrontTexture(Texture imageTexture) {
		setFrontTexture(imageTexture, 1,1);
	}

	@Localize(value="Box3D.setFrontTexture2")
	public void setFrontTexture(Texture imageTexture, double repeatX, double repeatY) {
		front.setTexture(imageTexture, repeatX, repeatY);
	}
	
	@Localize(value="Box3D.setBackTexture")
	public void setBackTexture(Texture imageTexture) {
		setBackTexture(imageTexture, 1, 1);
	}
	
	@Localize(value="Box3D.setBackTexture2")
	public void setBackTexture(Texture imageTexture, double repeatX, double repeatY) {
		back.setTexture(imageTexture, repeatX, repeatY);
	}

	@Localize(value="Box3D.setBottomTexture")
	public void setBottomTexture(Texture imageTexture) {
		setBottomTexture(imageTexture, 1, 1);
	}
	
	@Localize(value="Box3D.setBottomTexture2")
	public void setBottomTexture(Texture imageTexture, double repeatX, double repeatY) {
		bottom.setTexture(imageTexture, repeatX, repeatY);
	}

	@Override
	@Localize(value="Object3D.setColor2")
	public void setColor(String colorName, double transparency) {
		setTopColor(colorName, transparency);
		setRightColor(colorName, transparency);
		setLeftColor(colorName, transparency);
		setFrontColor(colorName, transparency);
		setBackColor(colorName, transparency);
		setBottomColor(colorName, transparency);
	}
	
	@Localize(value="Box3D.setColors")
	public void setColors(String front, String back, String right, String left, String top, String bottom) {
		setTopColor(top, 0);
		setRightColor(right, 0);
		setLeftColor(left, 0);
		setFrontColor(front, 0);
		setBackColor(back, 0);
		setBottomColor(bottom, 0);
	}

	@Localize(value="Box3D.setColors2")
	public void setColors(String front, String back, String right, String left, String top, String bottom, float transparency) {
		setTopColor(top, transparency);
		setRightColor(right, transparency);
		setLeftColor(left, transparency);
		setFrontColor(front, transparency);
		setBackColor(back, transparency);
		setBottomColor(bottom, transparency);
	}
	
	@Localize(value="Box3D.setTopColor2")
	public void setTopColor(String colorName, double transparency) {
		top.setColor(TColor.translateColor(colorName, Color.RED), transparency);
	}
	
	@Localize(value="Box3D.setTopColor")
	public void setTopColor(String colorName) {
		setTopColor(colorName, 0);
	}
	
	@Localize(value="Box3D.setRightColor2")
	public void setRightColor(String colorName, double transparency) {
		right.setColor(TColor.translateColor(colorName, Color.YELLOW), transparency);
	}
	
	@Localize(value="Box3D.setRightColor")
	public void setRightColor(String colorName) {
		setRightColor(colorName, 0);
	}
	
	@Localize(value="Box3D.setLeftColor2")
	public void setLeftColor(String colorName, double transparency) {
		left.setColor(TColor.translateColor(colorName, Color.BLUE), transparency);
	}
	
	@Localize(value="Box3D.setLeftColor")
	public void setLeftColor(String colorName) {
		setLeftColor(colorName, 0);
	}
	
	@Localize(value="Box3D.setFrontColor2")
	public void setFrontColor(String colorName, double transparency) {
		front.setColor(TColor.translateColor(colorName, Color.GREEN), transparency);
	}
	
	@Localize(value="Box3D.setFrontColor")
	public void setFrontColor(String colorName) {
		setFrontColor(colorName, 0);
	}
	
	@Localize(value="Box3D.setBackColor2")
	public void setBackColor(String colorName, double transparency) {
		back.setColor(TColor.translateColor(colorName, Color.WHITE), transparency);
	}
	
	@Localize(value="Box3D.setBackColor")
	public void setBackColor(String colorName) {
		setBackColor(colorName, 0);
	}
	
	@Localize(value="Box3D.setBottomColor2")
	public void setBottomColor(String colorName, double transparency) {
		bottom.setColor(TColor.translateColor(colorName, Color.ORANGE), transparency);
	}
	
	@Localize(value="Box3D.setBottomColor")
	public void setBottomColor(String colorName) {
		setBottomColor(colorName, 0);
	}
	
	@Localize(value="Box3D.setDimensions")
	public void setDimensions(double x, double y, double z) {
		Point3d p1 = new Point3d(-x/2, -y/2, z/2);
		Point3d p2 = new Point3d(x/2, -y/2, z/2);
		Point3d p3 = new Point3d(x/2, y/2, z/2);
		Point3d p4 = new Point3d(-x/2, y/2, z/2);
		Point3d p5 = new Point3d(-x/2, -y/2, -z/2);
		Point3d p6 = new Point3d(x/2, -y/2, -z/2);
		Point3d p7 = new Point3d(x/2, y/2, -z/2);
		Point3d p8 = new Point3d(-x/2, y/2, -z/2);
		front.setVertices(p1, p2, p3, p4);
		back.setVertices(p5, p8, p7, p6);
		bottom.setVertices(p1, p5, p6, p2);
		left.setVertices(p1, p4, p8, p5);
		right.setVertices(p2, p6, p7, p3);
		top.setVertices(p3, p7, p8, p4);
		setBounds(new BoundingBox(new Point3d(-x/2, -y/2, -z/2), new Point3d(x/2, y/2, z/2)));
	}
	
	@Override
	public void deleteObject() {
		if (bottom != null)
			bottom.deleteObject();
		if (top != null)
			top.deleteObject();
		if (right!=null)
			right.deleteObject();
		if (left!=null)
			left.deleteObject();
		if (back != null)
			back.deleteObject();
		if (front != null)
			front.deleteObject();
		super.deleteObject();
	}
	
	@Override
	public void enableLighting() {
		if (bottom != null)
			bottom.enableLighting();
		if (top != null)
			top.enableLighting();
		if (right!=null)
			right.enableLighting();
		if (left!=null)
			left.enableLighting();
		if (back != null)
			back.enableLighting();
		if (front != null)
			front.enableLighting();
	}

	@Override
	public void disableLighting() {
		if (bottom != null)
			bottom.disableLighting();
		if (top != null)
			top.disableLighting();
		if (right!=null)
			right.disableLighting();
		if (left!=null)
			left.disableLighting();
		if (back != null)
			back.disableLighting();
		if (front != null)
			front.disableLighting();
	}
	
	/* parent methods that should not be marked with "usage" annotations
	 */
	
	@Override
	public void loadFile(String fileName) {
		super.loadFile(fileName);
	}
	
	@Override
	public void loadFile(String name, double angle, double scale) {
		super.loadFile(name, angle, scale);
	}
	
	@Override
	public void addObject(Object3D obj) {
		super.addObject(obj);
	}
}
