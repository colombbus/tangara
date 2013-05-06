package org.colombbus.tangara.objects;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;

import java.net.URI;
import java.awt.Graphics;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;


@SuppressWarnings("serial")
@Localize(value="MotorA",localizeParent=true)
public abstract class MotorA extends TGraphicalObject implements ActionListener
{
  private BufferedImage monImage;
  private boolean reverseMode = false;
  private Timer t;
  private int motorId;
	
	@Localize(value="MotorA")
    public MotorA()
    {
		super();
		initialize();
	}
	
	public void initialize()
    {
		motorId = 1;
		try	{
			monImage = loadPicture("moteur4.png");
			setSize(214,135);

			t = new Timer(50, this);
			t.start();
		}
		catch(Exception e) {
			LOG.error("Picture display error", e);
		}
		displayObject();
    }
	
   
	//@Override
    public void paintComponent(Graphics g)
    {
    	g.drawImage(monImage, 0, 0, null);
    }
    
	
    @Localize(value="MotorA.reverseAction")
    public void reverseAction()
    {
        if(reverseMode)
        	reverseMode = false;
        else
        	reverseMode = true;
    }
    
    @Localize(value="MotorA.slowAction")
    public void slowAction(int value)
    {
    	int a = t.getDelay();
    	t.setDelay(a*value);
    }
    
    @Localize(value="MotorA.fastAction")
    public void fastAction(int value)
    {
    	int a = t.getDelay();
    	t.setDelay(a/value);
    }
       
    /**
     * Moves forward this character of <code>value</code>.
     * @param value
     * 		Represents the value of the forward step according to the x-axis.
     */
	@Override
	@Localize(value="common.moveForward")
    public void moveForward(int value)
    {
		super.moveForward(value);		
    }

	/**
     * Moves backward this character of <code>value</code>.
     * @param value
     * 		Represents the value of the backward step according to the x-axis.
     */
	@Override
	@Localize(value="common.moveBackward")
    public void moveBackward(int value)
    {
		super.moveBackward(value);
    }

	/**
     * Moves up this character of <code>value</code>.
     * @param value
     * 		Represents the value of the forward step according to the y-axis.
     */
	@Override
	@Localize(value="common.moveUp")
    public void moveUp(int value)
    {
		super.moveUp(value);
    }

	 /**
     * Moves back this character of <code>value</code>.
     * @param value
     * 		Represents the value of the backward step according to the y-axis.
     */
	@Override
	@Localize(value="common.moveDown")
    public void moveDown(int value)
    {
		super.moveDown(value);
    }
    
    @Override
   	public void deleteObject()
    {
    	super.deleteObject();
    }
    
	public void turnAction(boolean mode) {
		LOG.debug("Turn action " + motorId);
		if(motorId==0)
		{
			monImage = loadPicture("moteur1.png");
			setSize(214,135);
			if(mode)
				motorId = 4;
			else
				motorId++;
		}
		else if(motorId==1)
		{
			monImage = loadPicture("moteur2.png");
			setSize(214,135);
			if(mode)
				motorId--;
			else
				motorId++;
		}
		else if(motorId==2)
		{
			monImage = loadPicture("moteur3.png");
			setSize(214,135);
			if(mode)
				motorId--;
			else
				motorId++;
		}
		else if(motorId==3)
		{
			monImage = loadPicture("moteur4.png");
			setSize(214,135);
			if(mode)
				motorId--;
			else
				motorId++;
		}
		else
		{
			monImage = loadPicture("moteur5.png");
			setSize(214,135);
			if(mode)
				motorId--;
			else
				motorId=0;
		}
		repaint();
	}
	
	public void actionPerformed(ActionEvent e1)
	{
		turnAction(reverseMode);
	}
     
}
