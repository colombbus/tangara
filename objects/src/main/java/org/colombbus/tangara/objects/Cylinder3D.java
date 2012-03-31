package org.colombbus.tangara.objects;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.vecmath.Point3d;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;

import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;

import static com.sun.j3d.utils.geometry.Cylinder.*;

@Localize(value = "Cylinder3D", localizeParent = true)
public class Cylinder3D extends Object3D {
	
	Shape3D body;
	Shape3D top;
	Shape3D bottom;

	private double height, radius;
	
	@Localize(value="Cylinder3D")
	public Cylinder3D(double radius, double height) {
		this(radius, height, "");
	}
	
	@Localize(value="Cylinder3D")
	public Cylinder3D(double radius, double height, String colorName) {
		super();
		currentColor = TColor.translateColor(colorName, Color.WHITE);
		currentTransparency = 0;
		this.radius = radius;
		this.height = height;
		body = new Shape3D();
		top = new Shape3D();
		bottom = new Shape3D();
		body.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		top.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		bottom.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		body.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		top.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		bottom.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		tg.addChild(body);
		tg.addChild(top);
		tg.addChild(bottom);
		createCylinder();
		setColor(currentColor, currentTransparency);
		isExpandable = false;
	}
	
	@Localize(value="Cylinder3D")
	public Cylinder3D() {
		this(0.5,1);
	}
	
	@Override
	@Localize(value="Object3D.setTexture")
	public void setTexture(Texture imageTexture) {
		super.setTexture(imageTexture);
		Appearance textureApp = imageTexture.getAppearance(lightingEnabled);
		textureApp.setTexCoordGeneration(new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2));
		body.setAppearance(textureApp);
		top.setAppearance(textureApp);
		bottom.setAppearance(textureApp);
	}
	
	@Override
	protected void setColor(Color color, double transparency) {
		super.setColor(color,transparency);
		Appearance colorApp = Texture.getColorAppearance(color, transparency, lightingEnabled);
		body.setAppearance(colorApp);
		top.setAppearance(colorApp);
		bottom.setAppearance(colorApp);
	}

	protected void createCylinder() {
		Cylinder cylinderProv = new Cylinder((float)radius, (float)height, Primitive.GENERATE_TEXTURE_COORDS|Primitive.GENERATE_NORMALS, 20, 20, null);
		body.removeAllGeometries();
		body.addGeometry(cylinderProv.getShape(BODY).getGeometry());
		top.removeAllGeometries();
		top.addGeometry(cylinderProv.getShape(TOP).getGeometry());
		bottom.removeAllGeometries();
		bottom.addGeometry(cylinderProv.getShape(TOP).getGeometry());
		setBounds(new BoundingBox(new Point3d(-radius, -height/2, -radius), new Point3d(radius, height/2, radius)));
	}

	@Localize(value="Cylinder3D.setRadius")
	public void setRadius(double radius) {
		this.radius  = radius;
		createCylinder();
	}

	@Localize(value="Cylinder3D.setHeight")
	public void setHeight(double height) {
		this.height  = height;
		createCylinder();
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
