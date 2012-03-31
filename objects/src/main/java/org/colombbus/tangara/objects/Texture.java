package org.colombbus.tangara.objects;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.text.MessageFormat;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.Material;
import javax.vecmath.Color3f;


import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;

import com.sun.j3d.utils.image.TextureLoader;

@Localize(value = "Texture", localizeParent = true)
public class Texture extends TObject {

	protected Appearance appearance;
	protected Appearance appearanceWithLight;
	
	private static final String[] DEFAULT_EXTENSIONS = {"png","bmp","gif","jpg","jpeg"}; //$NON-NLS-1$
	private double width;
	private double height;

	@Localize(value = "Texture")
	public Texture(String fileName) {
		appearance = new Appearance();
		appearanceWithLight = new Appearance();
		try {
			Image sourceImage = java.awt.Toolkit.getDefaultToolkit().getImage(getCompleteName(fileName));
			TextureLoader loader = new TextureLoader(sourceImage, null);
			ImageComponent2D image = loader.getImage();
			width = image.getWidth();
			height = image.getHeight();
	
			Texture2D texture = new Texture2D(javax.media.j3d.Texture.BASE_LEVEL,
					javax.media.j3d.Texture.RGBA, image.getWidth(),
					image.getHeight());
			texture.setImage(0, image);
			texture.setEnable(true);
			texture.setMagFilter(javax.media.j3d.Texture.BASE_LEVEL_LINEAR);
			texture.setMinFilter(javax.media.j3d.Texture.BASE_LEVEL_LINEAR);
	
			PolygonAttributes polyAttributes = new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0f);
			appearance.setPolygonAttributes(polyAttributes);
			appearance.setTexture(texture);
	
			ColoringAttributes coloringAttributes = new ColoringAttributes();
			coloringAttributes.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
			appearance.setColoringAttributes(coloringAttributes);

			/*RenderingAttributes ra = new RenderingAttributes();
			ra.setAlphaTestFunction(RenderingAttributes.NOT_EQUAL);
			ra.setAlphaTestValue(0);
			appearance.setRenderingAttributes(ra);*/
			
			appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
			appearance.setCapability(Appearance.ALLOW_TEXGEN_WRITE);
			
			// Appearance with light
			appearanceWithLight.duplicateNodeComponent(appearance, false);
			Material material = new Material();
			material.setShininess(50f);
			material.setAmbientColor(1f,1f,1f);
			material.setEmissiveColor(0f,0f,0f);
			material.setDiffuseColor(1f,1f,1f);
			material.setSpecularColor(0.8f, 0.8f, 0.8f);
			appearanceWithLight.setMaterial(material);

			TextureAttributes textureAttributes = new TextureAttributes();
			textureAttributes.setTextureMode(TextureAttributes.MODULATE);
			appearanceWithLight.setTextureAttributes(textureAttributes);
			appearanceWithLight.setCapability(Appearance.ALLOW_TEXGEN_WRITE);
		
		} catch (Exception e) {
            String message = MessageFormat.format(getMessage("creation.error")+" ("+e.getMessage()+")", fileName);
            Program.instance().writeMessage(message);
		}
	}
	
	public double getHeight() {
		return height;
	}
	
	public double getWidth() {
		return width;
	}

	public String getCompleteName(String fileName) throws Exception {
		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
		if (file == null)
			throw new Exception("file not found"); //$NON-NLS-1$
		return file.getAbsolutePath();		
	}
	
	public Appearance getAppearance() {
		return getAppearance(false);
	}
	
	public Appearance getAppearance(boolean lightingEnabled) {
		if (lightingEnabled)
			return appearanceWithLight;
		else
			return appearance;
	}

	static Appearance getColorAppearance(Color color, double transparency) {
		return getColorAppearance(color, transparency, false);
	}

	static Appearance getColorAppearance(Color color, double transparency, boolean lighting) {
		if(transparency < 0 || transparency > 1) 
			transparency = 0;
		Appearance appearance = new Appearance();
		Color3f c3f = new Color3f(color);
		ColoringAttributes coloringAttributes = new ColoringAttributes();
		coloringAttributes.setColor(c3f);
		coloringAttributes.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
		appearance.setColoringAttributes(coloringAttributes);
		appearance.setTransparencyAttributes(new TransparencyAttributes (TransparencyAttributes.BLENDED, (float)transparency));
		if (lighting) {
			Material material = new Material();
			material.setAmbientColor(c3f);
			material.setDiffuseColor(c3f);
			material.setEmissiveColor(0f,0f,0f);
			material.setSpecularColor(0.8f, 0.8f, 0.8f);
			material.setShininess(50f);
			appearance.setMaterial(material);
		}
		return appearance;
	}

	
}
