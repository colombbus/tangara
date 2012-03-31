package org.colombbus.tangara.objects;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

@Localize(value = "Quadrilateral3D", localizeParent = true)
public class Quadrilateral3D extends Object3D {
	
	protected QuadArray quadrilateral;
	protected Shape3D shape;

	protected Point3d[] points = new Point3d[4];
	
	protected PolygonAttributes polygonAttributes;
	
	@Localize(value="Quadrilateral3D")
	public Quadrilateral3D() {
		this(new Point3d(-0.5, -0.5, 0), new Point3d(0.5, -0.5, 0), new Point3d(0.5, 0.5, 0), new Point3d(-0.5, 0.5, 0));
	}

	@Localize(value="Quadrilateral3D")
	public Quadrilateral3D(Point3D a, Point3D b, Point3D c, Point3D d) {
		this(a, b, c, d, "");
	}
	
	@Localize(value="Quadrilateral3D")
	public Quadrilateral3D(Point3D a, Point3D b, Point3D c, Point3D d, String colorName) {
		this(new Point3d(a.getX(), a.getY(), a.getZ()),new Point3d(b.getX(), b.getY(), b.getZ()),new Point3d(c.getX(), c.getY(), c.getZ()),new Point3d(d.getX(), d.getY(), d.getZ()), colorName);
	}
	
	@Localize(value="Quadrilateral3D")
	public Quadrilateral3D(Point3d a, Point3d b, Point3d c, Point3d d) {
		this(a, b, c, d, "");
	}
	
	@Localize(value="Quadrilateral3D")
	public Quadrilateral3D(Point3d a, Point3d b, Point3d c, Point3d d, String colorName) {
		super();
		Color color = TColor.translateColor(colorName, Color.WHITE);
		
		quadrilateral = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2 | GeometryArray.NORMALS);
		quadrilateral.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		quadrilateral.setCapability(GeometryArray.ALLOW_TEXCOORD_WRITE);
		quadrilateral.setCapability(GeometryArray.ALLOW_NORMAL_WRITE);
		
		points[0] = a;
		points[1] = b;
		points[2] = c;
		points[3] = d;
		
		quadrilateral.setCoordinate(0, points[0]);
		quadrilateral.setCoordinate(1, points[1]);
		quadrilateral.setCoordinate(2, points[2]);
		quadrilateral.setCoordinate(3, points[3]);

		quadrilateral.setTextureCoordinate(0, 0, new TexCoord2f(0f, 0f));
		quadrilateral.setTextureCoordinate(0, 1, new TexCoord2f(1f, 0f));
		quadrilateral.setTextureCoordinate(0, 2, new TexCoord2f(1f, 1f));
		quadrilateral.setTextureCoordinate(0, 3, new TexCoord2f(0f, 1f));

		
		updateCollisionBounds();

		generateNormals();
       
		shape = new Shape3D(quadrilateral);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		
		polygonAttributes = new PolygonAttributes();
		polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
		polygonAttributes.setBackFaceNormalFlip(true);
		polygonAttributes.setPolygonMode( PolygonAttributes.POLYGON_FILL );
		setColor(color, 0);
		tg.addChild(shape);
		
		isExpandable = false;
	}
	
	@Localize(value="Quadrilateral3D.setCoordinates2")
	public void setCoordinates(int index, Point3D p) {
		setCoordinates(index, p.getX(), p.getY(), p.getZ());
	}
	
	@Localize(value="Quadrilateral3D.setCoordinates")
	public void setCoordinates(int index, double x, double y, double z) {
		if(index > 0 && index < 5) {
			points[index-1].set(x, y, z);
			quadrilateral.setCoordinate(index-1, points[index-1]);
			updateCollisionBounds();
		}
	}
	
	@Localize(value="Quadrilateral3D.setVertices")
	public void setVertices(Point3D a, Point3D b, Point3D c, Point3D d) {
		setVertices(new Point3d(a.getX(), a.getY(), a.getZ()), new Point3d(b.getX(), b.getY(), b.getZ()),new Point3d(c.getX(), c.getY(), c.getZ()),new Point3d(d.getX(), d.getY(), d.getZ()));
	}

	public void setVertices(Point3d a, Point3d b, Point3d c, Point3d d) {
		points[0] = a;
		points[1] = b;
		points[2] = c;
		points[3] = d;
		quadrilateral.setCoordinate(0, points[0]);
		quadrilateral.setCoordinate(1, points[1]);
		quadrilateral.setCoordinate(2, points[2]);
		quadrilateral.setCoordinate(3, points[3]);
		updateCollisionBounds();
		generateNormals();
	}
	
	@Override
	protected void setColor(Color color, double transparency) {
		super.setColor(color, transparency);
		Appearance appearance = Texture.getColorAppearance(color, transparency,lightingEnabled); 
		appearance.setPolygonAttributes(polygonAttributes);
		shape.setAppearance(appearance);
	}
	
	@Override
	@Localize(value="Object3D.setTexture")
	public void setTexture(Texture imageTexture) {
		super.setTexture(imageTexture);
		Appearance appearance = imageTexture.getAppearance(lightingEnabled);
		shape.setAppearance(appearance);
	}

	@Localize(value="Quadrilateral3D.setTexture2")
	public void setTexture(Texture imageTexture, double repeatX, double repeatY) {
		texture = imageTexture;
		isTextured = true;
		quadrilateral.setTextureCoordinate(0, 0, new TexCoord2f(0f, 0f));
		quadrilateral.setTextureCoordinate(0, 1, new TexCoord2f((float) repeatX, 0f));
		quadrilateral.setTextureCoordinate(0, 2, new TexCoord2f((float)repeatX, (float) repeatY));
		quadrilateral.setTextureCoordinate(0, 3, new TexCoord2f(0f, (float) repeatY));
		setTexture(imageTexture);
	}
	
	protected void updateCollisionBounds() {
		double xMax = Math.max(points[0].getX(), Math.max(Math.max(points[2].getX(),points[3].getX()),points[1].getX()));
		double yMax = Math.max(points[0].getY(), Math.max(Math.max(points[2].getY(),points[3].getY()),points[1].getY()));
		double zMax = Math.max(points[0].getZ(), Math.max(Math.max(points[2].getZ(),points[3].getZ()),points[1].getZ()));

		double xMin = Math.min(points[0].getX(), Math.min(Math.min(points[2].getX(),points[3].getX()),points[1].getX()));
		double yMin = Math.min(points[0].getY(), Math.min(Math.min(points[2].getY(),points[3].getY()),points[1].getY()));
		double zMin = Math.min(points[0].getZ(), Math.min(Math.min(points[2].getZ(),points[3].getZ()),points[1].getZ()));
		
		setBounds(new BoundingBox(new Point3d(xMin-0.1, yMin-0.1, zMin-0.1), new Point3d(xMax+0.1, yMax+0.1, zMax+0.1)));
	}
	
	protected void generateNormals() {
		GeometryInfo info = new GeometryInfo(quadrilateral);
		NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(info);
        GeometryArray result = info.getGeometryArray();
        Vector3f temp = new Vector3f();
        result.getNormal(0, temp);
        quadrilateral.setNormal(0, temp); 
        result.getNormal(1, temp);
        quadrilateral.setNormal(1, temp); 
        result.getNormal(2, temp);
        quadrilateral.setNormal(2, temp); 
        result.getNormal(3, temp);
        quadrilateral.setNormal(3, temp); 
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
