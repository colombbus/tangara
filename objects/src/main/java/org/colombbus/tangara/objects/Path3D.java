/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008 Colombbus (http://www.colombbus.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara.objects;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.text.MessageFormat;

import javax.imageio.ImageIO;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;

@Localize(value="Path3D",localizeParent=true)
public class Path3D extends Quadrilateral3D {

	private static final String[] DEFAULT_EXTENSIONS = {"png","bmp","gif","jpg","jpeg"}; //$NON-NLS-1$
	private BufferedImage actualPicture;
	private BufferedImage usedPicture;
	private Vector3d x, y;
	private double dimX, dimY;
	private Texture trueTexture;
	private String pathFile = null;
	private boolean pathShown = false;
	
	
	@Localize(value="Path3D")
	public Path3D() {
		this(new Point3d(-0.5, -0.5, 0), new Point3d(0.5, -0.5, 0), new Point3d(0.5, 0.5, 0), new Point3d(-0.5, 0.5, 0));
	}

	@Localize(value="Path3D")
	public Path3D(Point3D a, Point3D b, Point3D c, Point3D d) {
		this(a, b, c, d, "");
	}
	
	@Localize(value="Path3D")
	public Path3D(Point3d a, Point3d b, Point3d c, Point3d d) {
		this(a, b, c, d, "");
	}
	
	@Localize(value="Path3D")
	public Path3D(Point3D a, Point3D b, Point3D c, Point3D d, String fileName) {
		this(new Point3d(a.getX(), a.getY(), a.getZ()),new Point3d(b.getX(), b.getY(), b.getZ()),new Point3d(c.getX(), c.getY(), c.getZ()),new Point3d(d.getX(), d.getY(), d.getZ()), "");
		loadPathPicture(fileName);
	}

	@Localize(value="Path3D")
	public Path3D(Point3d a, Point3d b, Point3d c, Point3d d, String colorName) {
		super(a,b,c,d, colorName);
		// compute relative base
		computeBase();
	}

	
	/**
	 * Loads an image from a file.
	 * @param fileName
	 */
    private void loadPictureFile(String fileName) {
    	try {
    		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
			if (file == null)
				throw new Exception("file not found"); //$NON-NLS-1$
			BufferedImage newImage = ImageIO.read(file);
			// Make use of Toolkit rather than ImageIO, to manage transparent images
			if (newImage !=null) {
	    		int width = newImage.getWidth();
	    		int height = newImage.getHeight();
	    		actualPicture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    		actualPicture.getGraphics().drawImage(newImage,0,0,null);
	    		usedPicture = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	    		usedPicture.getGraphics().drawImage(newImage,0,0,null);

			}
			pathFile = fileName;
    	}
    	catch (Exception e) {
            String message = MessageFormat.format(getMessage("error.load")+" ("+e.getMessage()+")", fileName);
            Program.instance().writeMessage(message);
    	}
    }
    
    private void computeBase() {
    	x = new Vector3d(points[2].getX()-points[3].getX(), points[2].getY()-points[3].getY(), points[2].getZ()-points[3].getZ());
    	y = new Vector3d(points[0].getX()-points[3].getX(), points[0].getY()-points[3].getY(), points[0].getZ()-points[3].getZ());
    	dimX = x.length();
    	dimY = y.length();
    	x.normalize();
    	y.normalize();
    }
    
    public boolean isOnPath(Point3d p) {
    	// 1st transform point to be in the relative base
    	Transform3D invertTransform = new Transform3D();
    	invertTransform.invert(t3D);
    	invertTransform.transform(p);
    	// 2nd compute coordinates in relative base
    	Vector3d position = new Vector3d(p.getX()-points[3].getX(), p.getY()-points[3].getY(), p.getZ()-points[3].getZ());
    	double xCoord = Math.abs(x.dot(position)/dimX);
    	double yCoord = Math.abs(y.dot(position)/dimY);
    	// 3rd find picture coordinates 
    	if ((xCoord > 1) || (yCoord>1))
    		return false;
    	int xPicture = (int)(xCoord * usedPicture.getWidth());
    	int yPicture = (int)(yCoord * usedPicture.getHeight());
    	return (usedPicture.getRGB(xPicture, yPicture) != -1);
    }
	
    
	@Override
	@Localize(value="Quadrilateral3D.setCoordinates")
	public void setCoordinates(int index, double x, double y, double z) {
		super.setCoordinates(index, x, y, z);
		computeBase();
	}
	
	@Override
	public void setVertices(Point3d a, Point3d b, Point3d c, Point3d d) {
		super.setVertices(a, b, c, d);
		computeBase();
	}
    
	@Localize(value="Path3D.loadPathPicture")
	public void loadPathPicture(String fileName) {
		loadPictureFile(fileName);
		if (pathShown)
			setTexture(new Texture(pathFile));
	}
	
	@Localize(value="Path3D.showPath")
	public void showPath(boolean value) {
		pathShown = value;
		if (value && pathFile != null) {
			trueTexture = texture; 
	    	setTexture(new Texture(pathFile));
		} else {
			if (trueTexture != null) {
				setTexture(trueTexture);
			}
		}
	}
	
    /**
     * Sets a transparent color.
     * @param colorName
     */
    @Localize(value="Path3D.transparentColor")
    public void transparentColor(String colorName) {
    	Color c = TColor.translateColor(colorName, Color.black);
    	TransparentFilter filter = new TransparentFilter(c);    
    	Image newPicture = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(usedPicture.getSource(),filter));
    	usedPicture = new BufferedImage(actualPicture.getWidth(),actualPicture.getHeight(),BufferedImage.TYPE_INT_ARGB);
    	usedPicture.getGraphics().drawImage(newPicture,0,0,null);
    }
    
	 class TransparentFilter extends RGBImageFilter
	 {
		 private Color transparent;

		 public TransparentFilter(Color target)
		 {
			 transparent = new Color(target.getRGB());
		 }

		 @Override
		public int filterRGB(int x, int y, int rgb)
		 {
			 Color c = new Color(rgb);
			 if (TColor.testCloseColor(transparent, c))
			 {
				 return (rgb & 0x00ffffff);
			 }
			 else
			 {
				 return rgb;
			 }
		 }
	 }

    
}
