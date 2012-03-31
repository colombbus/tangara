package org.colombbus.tangara.objects;

import java.awt.Color;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;

@Localize(value = "Segment3D", localizeParent = true)
public class Segment3D extends Object3D {
	
	private LineArray segment;
	private Shape3D shape;
	
	private Point3d[] points = new Point3d[2];

	@Localize(value="Segment3D")
	public Segment3D() {
		this(new Point3d(-0.5, 0, 0), new Point3d(0.5, 0, 0), ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Localize(value="Segment3D")
	public Segment3D(Point3D a, Point3D b) {
		this(a, b, ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Localize(value="Segment3D")
	public Segment3D(Point3D a, Point3D b, String colorName) {
		this(a, b, TColor.translateColor(colorName, Color.WHITE));
	}

	@Localize(value="Segment3D")
	public Segment3D(Point3d a, Point3d b, String colorName) {
		this(a, b, TColor.translateColor(colorName, Color.WHITE));
	}
	
	@Localize(value="Segment3D")
	public Segment3D(Point3D a, Point3D b, Color color) {
		this(new Point3d(a.getX(), a.getY(), a.getZ()),new Point3d(b.getX(), b.getY(), b.getZ()), color);
	}
	
	@Localize(value="Segment3D")
	public Segment3D(Point3d a, Point3d b, Color color) {
		super();
		segment = new LineArray(2, GeometryArray.COORDINATES | GeometryArray.COLOR_3 | GeometryArray.TEXTURE_COORDINATE_2);
		segment.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		segment.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
		
		points[0] = a;
		points[1] = b;
		
		segment.setCoordinate(0, points[0]);
		segment.setCoordinate(1, points[1]);
		segment.setTextureCoordinate(0, 0, new TexCoord2f(0f, 0.5f));
		segment.setTextureCoordinate(0, 1, new TexCoord2f(1f, 0.5f));

		updateCollisionBounds();
		
		shape = new Shape3D(segment);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		setColor(color,0);
		tg.addChild(shape);
		
		isExpandable = false;
	}
	
	@Localize(value="Segment3D.setCoordinates")
	public void setCoordinates(int index, double x, double y, double z) {
		if(index == 1 || index == 2) {
			points[index-1].set(x, y, z);
			segment.setCoordinate(index-1, points[index-1]);
			updateCollisionBounds();
		}
	}

	@Localize(value="Segment3D.setCoordinates2")
	public void setCoordinates(int index, Point3D p) {
		setCoordinates(index, p.getX(), p.getY(), p.getZ());
	}

	@Localize(value="Segment3D.setVertices")
	public void setVertices(Point3D a, Point3D b) {
		points[0].set(a.getX(), a.getY(), a.getZ());
		points[1].set(b.getX(), b.getY(), b.getZ());
		segment.setCoordinate(0, points[0]);
		segment.setCoordinate(1, points[1]);
		updateCollisionBounds();
	}

	@Override
	protected void setColor(Color color, double transparency) {
		super.setColor(color,transparency);
		Color3f c = new Color3f(color.getRed(), color.getGreen(), color.getBlue());
		segment.setColor(0, c);
		segment.setColor(1, c);
		shape.setAppearance(Texture.getColorAppearance(color, transparency));
	}
	
	@Override
	@Localize(value="Objet3D.setTexture")
	public void setTexture(Texture imageTexture) {
		super.setTexture(imageTexture);
		shape.setAppearance(imageTexture.getAppearance());
	}
	
	protected void updateCollisionBounds() {
		Point3d a = new Point3d();
		Point3d b = new Point3d();
		segment.getCoordinate(0, a);
		segment.getCoordinate(1, b);
		double xMax = Math.max(a.getX(), b.getX());
		double yMax = Math.max(a.getY(), b.getY());
		double zMax = Math.max(a.getZ(), b.getZ());

		double xMin = Math.min(a.getX(), b.getX());
		double yMin = Math.min(a.getY(), b.getY());
		double zMin = Math.min(a.getZ(), b.getZ());
		
		setBounds(new BoundingBox(new Point3d(xMin-0.1, yMin-0.1, zMin-0.1), new Point3d(xMax+0.1, yMax+0.1, zMax+0.1)));
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
