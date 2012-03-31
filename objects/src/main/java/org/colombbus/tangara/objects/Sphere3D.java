package org.colombbus.tangara.objects;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.vecmath.Point3d;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

@Localize(value = "Sphere3D", localizeParent = true)
public class Sphere3D extends Object3D {
	
	private Shape3D body;
	
	private double radius;
	
	@Localize(value="Sphere3D")
	public Sphere3D(double radius) {
		this(radius, "");
	}
	
	@Localize(value="Sphere3D")
	public Sphere3D(double radius, String colorName) {
		super();
		currentColor = TColor.translateColor(colorName, Color.WHITE);
		currentTransparency = 0;
		body = new Shape3D();
		body.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		body.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		tg.addChild(body);
		setRadius(radius);
		setColor(currentColor, currentTransparency);
		isExpandable = false;
	}
	
	@Localize(value="Sphere3D")
	public Sphere3D() {
		this(1);
	}
	
	@Override
	@Localize(value="Object3D.setTexture")
	public void setTexture(Texture imageTexture) {
		super.setTexture(imageTexture);
		Appearance appearance = imageTexture.getAppearance(lightingEnabled); 
		appearance.setTexCoordGeneration(new TexCoordGeneration(TexCoordGeneration.SPHERE_MAP, TexCoordGeneration.TEXTURE_COORDINATE_2));
		body.setAppearance(appearance);
	}
	
	@Override
	protected void setColor(Color color, double transparency) {
		super.setColor(color, transparency);
		body.setAppearance(Texture.getColorAppearance(color, transparency, lightingEnabled));
	}

	
	@Localize(value="Sphere3D.setRadius")
	public void setRadius(double radius) {
		this.radius = radius;
		Sphere sphere = new Sphere((float)this.radius, Primitive.GENERATE_TEXTURE_COORDS|Primitive.GENERATE_NORMALS, 50, null);
		body.removeAllGeometries();
		body.addGeometry(sphere.getShape().getGeometry());
		setBounds(new BoundingSphere(new Point3d(), this.radius));
		if(isTextured)
			setTexture(texture);
		else
			setColor(currentColor, currentTransparency);
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
