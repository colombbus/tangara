package org.colombbus.tangara.objects;

import static javax.media.j3d.Text3D.*;

import java.awt.Color;
import java.awt.Font;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.vecmath.Point3f;
import org.colombbus.build.Localize;

@Localize(value = "Text3D", localizeParent = true)
public class Text3D extends Object3D {
	
	private Shape3D shape;
	private javax.media.j3d.Text3D text3D;
	
	@Localize(value="Text3D")
	public Text3D(String text) {
		this(text, "", 1); //$NON-NLS-1$
	}

	@Localize(value="Text3D")
	public Text3D() {
		this("text", "", 1); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Localize(value="Text3D")
	public Text3D(String text, String colorName) {
		this(text, colorName, 1);
	}

	@Localize(value="Text3D")
	public Text3D(String text, double scale) {
		this(text, "", scale); //$NON-NLS-1$
	}

	@Localize(value="Text3D")
	public Text3D(String text, String colorName, double scale) {
		Font3D font3D = new Font3D(new Font("Arial", Font.PLAIN, 1), new FontExtrusion()); //$NON-NLS-1$
		
		text3D = new javax.media.j3d.Text3D(font3D, "", new Point3f(),ALIGN_FIRST, PATH_RIGHT);
		text3D.setCapability(ALLOW_STRING_WRITE);
		shape = new Shape3D();
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		shape.setGeometry(text3D);
		tg.addChild(shape);

		setColor(colorName);
		setScale(scale);
		setText(text);
		
		isExpandable = false;
	}

	@Localize(value="common.setText")
	public void setText(String texte) {
		text3D.setString(texte);
		BoundingBox box = new BoundingBox();
		text3D.getBoundingBox(box);
		setBounds(box);
	}
	
	@Override
	protected void setColor(Color color, double transparency) {
		Appearance appearance = Texture.getColorAppearance(color, transparency,lightingEnabled);
		shape.setAppearance(appearance);
	}
	
	@Override
	@Localize(value="Object3D.setTexture")
	public void setTexture(Texture imageTexture) {
		super.setTexture(imageTexture);
		Appearance appearance = imageTexture.getAppearance(lightingEnabled);
		appearance.setTexCoordGeneration(new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2));
		shape.setAppearance(appearance);
	}

	@Localize(value="Text3D.setTexture2")
	public void setTexture(Texture imageTexture, double repeatX, double repeatY) {
		// repeatX and repeatY not supported
		setTexture(imageTexture);
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
