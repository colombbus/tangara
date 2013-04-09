package org.colombbus.tangara.objects;


import java.awt.Color;

import java.awt.Image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;

import java.net.MalformedURLException;
import java.net.URI;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;
import org.colombbus.tangara.objects.character.Movement;


@SuppressWarnings("serial")
@Localize(value="MotorA",localizeParent=true)
public abstract class MotorA extends TGraphicalObject
{
  private BufferedImage monImage;
  private Movement movement;
  private Timer timer;
  private Point destination=new Point(0,0);
  private int motorId;
  private int xCoordinate=0;
  private int yCoordinate=0;
  
  private final Dimension shift = new Dimension();
	
	@Localize(value="MotorA")
    public MotorA()
    {
		super();
		initialize();
	}
	
	public void initialize()
    {
		motorId=1;
		movement = new Movement();
		movement.setMotor(this);
		try
		{
			monImage = loadPicture("moteur1.png");
			setSize(615,259);
			timer = new Timer(50, movement);
			timer.start();
		}
		catch(Exception e)
		{
			LOG.error("Picture display error", e);
		}
		displayObject();
    }
	
	public BufferedImage loadPicture(String fileName)
    {
		URI file = getResource(fileName);
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
    
	
    public void paintComponent(Graphics g)
    {
    	int xImage = (int) (xCoordinate + destination.x + shift.getWidth());
		int yImage = (int) (yCoordinate + destination.y + shift.getHeight());
    	g.drawImage(monImage, xImage, yImage, null);
    }
	
	/*
	public void paintElement(Graphics g, int origineX, int origineY)
    {
		int xImage = (int) (0 + origineX + shift.getWidth());
		int yImage = (int) (0 + origineY + shift.getHeight());
		g.drawImage(monImage, xImage, yImage, null);
    }*/
    
	public void turnAction() {
		Point position = getObjectLocation();
		this.setObjectLocation(position);
		xCoordinate=position.x;
		yCoordinate=position.y;
		//destination.x=position.x;
		//destination.y=position.y;
		
		LOG.debug("Turn action " + motorId);
		try {
			Thread.sleep(25);
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		if(motorId==0)
		{
			monImage = loadPicture("moteur1.png");
			//movement.setLocation((int)xCoordinate, (int)yCoordinate);
			//setObjectLocation(xCoordinate,yCoordinate);
			setSize(615,259);
			motorId++;
		}
		else if(motorId==1)
		{
			monImage = loadPicture("moteur2.png");
			setSize(615,259);
			motorId++;
		}
		else if(motorId==2)
		{
			monImage = loadPicture("moteur3.png");
			setSize(615,259);
			motorId++;
		}
		else if(motorId==3)
		{
			monImage = loadPicture("moteur4.png");
			setSize(615,259);
			motorId++;
		}
		else
		{
			monImage = loadPicture("moteur5.png");
			setSize(615,259);
			motorId=0;
		}
		displayObject();
	}
     
}
