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
  private Point destination;
  private int motorId;
  
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
		destination = new Point(0,0);
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
    
	@Override
    public void paintComponent(Graphics g)
    {
    	int xImage = (int) (destination.x + shift.getWidth());
		int yImage = (int) (destination.y + shift.getHeight());
    	g.drawImage(monImage, xImage, yImage, null);
    }
    
    @Localize(value="MotorA.reverseAction")
    public void reverseAction()
    {
        movement.reverseMove();
    }
    
    @Localize(value="MotorA.slowAction")
    public void slowAction(int value)
    {
    	int a = timer.getDelay();
    	timer.setDelay(a*value);
    }
    
    @Localize(value="MotorA.fastAction")
    public void fastAction(int value)
    {
    	int a = timer.getDelay();
    	timer.setDelay(a/value);
    }
    
    @Override
   	public void deleteObject()
    {
    	timer.stop();
    	movement = null;
    	super.deleteObject();
    }
    
	public void turnAction(boolean mode) {
		timer.stop();
		Point position = this.getObjectLocation();
		super.setObjectLocation(position);
		movement.move(new Dimension(position.x, position.y));
		movement.setLocation(position.x, position.y);
		//shift = new Dimension(position.x, position.y);
		//destination = new Point(position.x, position.y);
		timer.restart();
		LOG.debug("Turn action " + motorId);
		if(motorId==0)
		{
			monImage = loadPicture("moteur1.png");
			setSize(615,259);
			if(mode)
				motorId = 4;
			else
				motorId++;
		}
		else if(motorId==1)
		{
			monImage = loadPicture("moteur2.png");
			setSize(615,259);
			if(mode)
				motorId--;
			else
				motorId++;
		}
		else if(motorId==2)
		{
			monImage = loadPicture("moteur3.png");
			setSize(615,259);
			if(mode)
				motorId--;
			else
				motorId++;
		}
		else if(motorId==3)
		{
			monImage = loadPicture("moteur4.png");
			setSize(615,259);
			if(mode)
				motorId--;
			else
				motorId++;
		}
		else
		{
			monImage = loadPicture("moteur5.png");
			setSize(615,259);
			if(mode)
				motorId--;
			else
				motorId=0;
		}
		displayObject();
	}
     
}
