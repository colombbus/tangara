package org.colombbus.tangara.objects;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;

@Localize(value = "Triangle3D", localizeParent = true)
public class Triangle3D extends Object3D {
	
	TriangleArray triangle;
	Shape3D shape;
	
	private Point3d[] points = new Point3d[3];
	
	private PolygonAttributes polygonAttributes;
	
	@Localize(value="Triangle3D")
	public Triangle3D() {
		this(new Point3d(-0.5, -0.5, 0), new Point3d(0.5, -0.5, 0), new Point3d(0, 0.5, 0), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Localize(value="Triangle3D")
	public Triangle3D(Point3D a, Point3D b, Point3D c) {
		this(a, b, c, ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Localize(value="Triangle3D")
	public Triangle3D(Point3D a, Point3D b, Point3D c, String colorName) {
		this(a, b, c, TColor.translateColor(colorName, Color.WHITE));
	}

	@Localize(value="Triangle3D")
	public Triangle3D(Point3d a, Point3d b, Point3d c, String colorName) {
		this(a, b, c, TColor.translateColor(colorName, Color.WHITE));
	}

	@Localize(value="Triangle3D")
	public Triangle3D(Point3D a, Point3D b, Point3D c, Color color) {
		this(new Point3d(a.getX(), a.getY(), a.getZ()), new Point3d(b.getX(), b.getY(), b.getZ()), new Point3d(c.getX(), c.getY(), c.getZ()), color);
	}

	@Localize(value="Triangle3D")
	public Triangle3D(Point3d a, Point3d b, Point3d c, Color color) {
		super();
		triangle = new TriangleArray(3, GeometryArray.COORDINATES | GeometryArray.COLOR_3 | GeometryArray.TEXTURE_COORDINATE_2);
		triangle.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		triangle.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
		
		points[0] = a;
		points[1] = b;
		points[2] = c;
		
		triangle.setCoordinate(0, points[0]);
		triangle.setCoordinate(1, points[1]);
		triangle.setCoordinate(2, points[2]);
		triangle.setTextureCoordinate(0, 0, new TexCoord2f(0f, 0f));
		triangle.setTextureCoordinate(0, 1, new TexCoord2f(1f, 0f));
		triangle.setTextureCoordinate(0, 2, new TexCoord2f(0.5f, 1f));
		
		updateCollisionBounds();

		shape = new Shape3D(triangle);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		
		polygonAttributes = new PolygonAttributes();
		polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
		polygonAttributes.setBackFaceNormalFlip(true);
		polygonAttributes.setPolygonMode( PolygonAttributes.POLYGON_FILL );

		setColor(color,0);
				
		tg.addChild(shape);
		
		isExpandable = false;
	}

	
	@Localize(value="Triangle3D.setCoordinates")
	public void setCoordinates(int index, double x, double y, double z) {
		if(index == 1 || index == 2 || index == 3) {
			points[index-1].set(x, y, z);
			triangle.setCoordinate(index-1, points[index-1]);
			updateCollisionBounds();
		}
	}

	@Localize(value="Triangle3D.setCoordinates2")
	public void setCoordinates(int index, Point3D p) {
		setCoordinates(index, p.getX(), p.getY(), p.getZ());
	}
	
	@Localize(value="Triangle3D.setVertices")
	public void setVertices(Point3D a, Point3D b, Point3D c) {
		points[0].set(a.getX(), a.getY(), a.getZ());
		points[1].set(b.getX(), b.getY(), b.getZ());
		points[2].set(c.getX(), c.getY(), c.getZ());
		triangle.setCoordinate(0, points[0]);
		triangle.setCoordinate(1, points[1]);
		triangle.setCoordinate(2, points[2]);
		updateCollisionBounds();
	}
	
	@Override
	protected void setColor(Color color, double transparency) {
		super.setColor(color,transparency);
		Appearance appearance = Texture.getColorAppearance(color, transparency,lightingEnabled);
		appearance.setPolygonAttributes(polygonAttributes);
		shape.setAppearance(appearance);
	}
	
	@Override
	@Localize(value="Object3D.setTexture")
	public void setTexture(Texture imageTexture) {
		super.setTexture(imageTexture);
		shape.setAppearance(imageTexture.getAppearance(lightingEnabled));
	}
	
	
	protected void updateCollisionBounds() {
		Point3d a = new Point3d();
		Point3d b = new Point3d();
		Point3d c = new Point3d();
		triangle.getCoordinate(0, a);
		triangle.getCoordinate(1, b);
		triangle.getCoordinate(2, c);

		double xMax = Math.max(a.getX(), Math.max(c.getX(),b.getX()));
		double yMax = Math.max(a.getY(), Math.max(c.getY(),b.getY()));
		double zMax = Math.max(a.getZ(), Math.max(c.getZ(),b.getZ()));

		double xMin = Math.min(a.getX(), Math.min(c.getX(),b.getX()));
		double yMin = Math.min(a.getY(), Math.min(c.getY(),b.getY()));
		double zMin = Math.min(a.getZ(), Math.min(c.getZ(),b.getZ()));
		
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
