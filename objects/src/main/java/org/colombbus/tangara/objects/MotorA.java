package org.colombbus.tangara.objects;


import java.awt.Color;

import java.awt.Image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;

import java.net.MalformedURLException;
import java.net.URI;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JOptionPane;

import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;


@SuppressWarnings("serial")
@Localize(value="MotorA",localizeParent=true)
public abstract class MotorA extends TGraphicalObject
{
  private BufferedImage monImage;
	
	@Localize(value="MotorA")
    public MotorA()
    {
			super();
    	monImage = loadPicture();
    	setSize(70,70);
    	displayObject();
			}
	
	public BufferedImage loadPicture()
    {
		URI file = getResource("red.png");
		try {
			if (file == null) {
				throw new Exception("file not found");
			} else {
					BufferedImage newImage = ImageIO.read(new File(file));
					return newImage;
	    	} 
		} catch (Exception e) {
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", "red.png");
            Program.instance().writeMessage(message);
		}
			return null;
    }
    
    public void paintComponent(Graphics g) {
    	g.drawImage(monImage,0,0,null);
    }
     
}
