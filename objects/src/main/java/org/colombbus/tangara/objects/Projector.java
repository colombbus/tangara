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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;

import javax.media.*;
import javax.media.control.FramePositioningControl;
import javax.swing.Timer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Configuration;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class provides a box able to display a video.
 * @author Benoit
 */
@SuppressWarnings("serial")
@Localize(value="Projector",localizeParent=true)
public abstract class Projector extends TGraphicalObject {

	private static final String DEFAULT_EXTENSION="mov";


	private static double DELAY = 50;

	private Player player;
	private ControllerListener loopController;
	private boolean looping=false;
	private boolean playing=false;
	private boolean first=false;

	private Timer timer;

	private ChangeVideoSpeed cvs;

	private double currentSpeed;

	/**
	 * Creates an instance of this class.
	 */
    @Localize(value="Projector")
    public Projector()
    {
        super();
    	setSize(300,200);
    	setLayout(new BorderLayout());
    	setBackground(Color.BLACK);
        try
        {
        	Manager.setHint( Manager.LIGHTWEIGHT_RENDERER, true );
        	loopController = new ControllerListener(){
                @Override
				public void controllerUpdate(ControllerEvent controllerEvent)
                {
                	if (controllerEvent instanceof EndOfMediaEvent)
                    {
                		if (timer==null || !timer.isRunning())
                		{
                			player.setMediaTime(new Time(0));
                        	player.start();
                		}
                    }
                }
        	};
        }
        catch (Exception e)
        {
        	Program.instance().writeMessage("Exception "+e.getMessage());
        }

        cvs = new ChangeVideoSpeed();
        currentSpeed = 1;
        displayObject();
    }


    /**
     * Creates an instance of this class.
     * @param fileName
     */
    @Localize(value="Projector")
    public Projector(String fileName)
    {
    	this();
    	loadFile(fileName);
    }

    private void actuallyStart()
    {
		if (!first)
		{
			player.setMediaTime(new Time(0));
			first = true;
		}
		if (currentSpeed==1)
			player.start();
		else
		{
			timer = new Timer((int)(DELAY/Math.abs(currentSpeed)), cvs);
			timer.start();
		}
		playing = true;
    }

    /**
     * Starts the video.
     */
    @Localize(value="Projector.start")
    public void start()
	{
		if (player!=null)
		{
			if ((player.getState()==Controller.Prefetched)||(player.getState()==Controller.Realized))
			{
				actuallyStart();
			}
			else
			{
	        	player.addControllerListener(new ControllerListener() {
	                @Override
					public void controllerUpdate(ControllerEvent controllerEvent)
	                {
	                	if((controllerEvent instanceof PrefetchCompleteEvent)||(controllerEvent instanceof RealizeCompleteEvent))
		                {
	                		actuallyStart();
            				player.removeControllerListener(this);
		                }
	                }
	        	});
			}
		}
	}

    /**
     * Stops the video.
     */
    @Localize(value="Projector.stop")
    public void stop()
	{
		if (player!=null)
		{
			player.stop();
			if (timer!=null && timer.isRunning())
				timer.stop();
		}
		playing = false;
	}


   /**
    * Sets the loop state of the video projector.
    * @param value
    */
    @Localize(value="Projector.loop")
    public void loop(boolean value)
    {
    	if (player !=null)
    	{
    		if (value!=looping)
    		{
    			if (value)
    				player.addControllerListener(loopController);
    			else
    				player.removeControllerListener(loopController);
    			looping = value;
    		}
    	}
    }

    @Localize(value="Projector.getSpeed")
    public double getSpeed()
    {
    	return currentSpeed;
    }


    /**
     * Sets the speed value of the video projector.
     * @param value
     */
    @Localize(value="Projector.setSpeed")
    public void setSpeed(double value)
    {
    	boolean hasToPlay = playing;
    	if (playing)
    		stop();
		if (value!=currentSpeed)
		{
	    	if (Double.compare(value, 0)==0)
			{
	    		hasToPlay = false;
			}
	    	else if (Double.compare(value, 1)!=0)
	    	{
				Double seconds = player.getMediaTime().getSeconds();
				Time duration = player.getDuration();
	    		double totalseconds =  duration.getSeconds();
	    		FramePositioningControl fpc = (FramePositioningControl)player.getControl("javax.media.control.FramePositioningControl");
	    		double rate = fpc.mapTimeToFrame(duration)/totalseconds;
				cvs.setCompteur( (int)(rate*seconds)+ 3);
	    	}
		}
		currentSpeed = value;
		if (hasToPlay)
		{
			start();
		}
    }



    /**
     * Deletes the object.
     */
    @Override
	public void deleteObject()
    {
    	if (player!=null)
    	{
    		player.close();
    		player = null;
    	}
    	if (loopController!=null)
    	{
    		loopController = null;
    	}
    	if (timer!=null)
    	{
    		if (timer.isRunning())
    			timer.stop();
    		timer = null;
    	}
    	super.deleteObject();
    }

    /**
     * Loads a video from a file.
     * @param fileName
     */
	@Localize(value="Projector.loadFile")
    public void loadFile(String fileName)
    {
    	String extension = "";
    	try
    	{
    		//1st stop current player if existing
        	if (player!=null)
        	{
    			if (player.getState()!=Controller.Unrealized)
    			{
    				player.stop();
    				player.deallocate();
    			}
        	}
        	looping =  false;
    		// 2nd try to load the new file
	    	int pointPosition = fileName.lastIndexOf('.');
	    	if (pointPosition>-1)
	    	{
	    		extension = fileName.substring(pointPosition+1);
	    		if (extension.length()==0)
	    		{
	    			extension = DEFAULT_EXTENSION;
	    			fileName += DEFAULT_EXTENSION;
	    		}
	    	}
	    	else
	    	{
				extension = DEFAULT_EXTENSION;
				fileName += "." + DEFAULT_EXTENSION;
	    	}
	    	File file = new File(fileName);
	    	if (!file.isAbsolute())
	    	{
	    		// the name does not contain any directory reference : add the current directory
	    		file = new File(Program.instance().getCurrentDirectory(),fileName);
	    		// if file does not exist, try with user home directory
	    		if (!file.exists())
	    			file = new File(Configuration.instance().getUserHome(),fileName);
	    		fileName = file.getAbsolutePath();
	    	}
    		if (!(file.exists()))
    			throw new Exception("file not found");
            MediaLocator video = new MediaLocator("file://"+file.getAbsolutePath());
        	player = Manager.createPlayer(video);
        	if (player != null)
        	{
	        	player.addControllerListener(new ControllerListener() {
	                @Override
					public void controllerUpdate(ControllerEvent controllerEvent)
	                {
	                	try
	                	{
		                	if (controllerEvent instanceof ResourceUnavailableEvent)
		                	{
		                		if (player!=null)
		                			player.deallocate();
		                		throw new Exception(getMessage("load.error.unsupportedFormat"));
		                	}
		                	else if(controllerEvent instanceof RealizeCompleteEvent)
		                	{
		                		Component playerComponent = player.getVisualComponent();
		                		if(playerComponent != null)
		                		{
		                        	removeAll();
		                			add(playerComponent,BorderLayout.CENTER);
		                			revalidate();
		                			player.removeControllerListener(this);
		                		}
		                		else
		                		{
		                			throw new Exception("visual component null");
		                		}
		                	}
	                	}
	                	catch (Exception e)
	                	{
                            Program.instance().writeMessage(getMessage("load.error2")+" ("+e.getMessage()+")");

	                	}
	                }
	        	});
	        	player.prefetch();
        	}
        	else
        	{
        		throw new Exception("Player could not be created");
        	}
    	}
    	catch (Exception e)
    	{
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", fileName);
            Program.instance().writeMessage(message);
    	}

    }

	/**
	 * Enables to rewind the video
	 */
	@Localize(value="Projector.rewind")
	public void rewind()
	{
		if (playing)
		{
			if (timer!=null && timer.isRunning())
				timer.stop();
			else
				player.stop();
		}
		cvs.setCompteur(0);
		player.setMediaTime(new Time(0));
		if (playing)
			start();
	}

	@Override
	public void freeze(boolean value)
	{
		if (!value)
		{
			if (playing)
			{
				if (currentSpeed==1)
					player.start();
				else
					timer.start();
			}
		}
		else
		{
			if (playing)
			{
				if (currentSpeed==1)
					player.stop();
				else
					timer.stop();
			}
		}
	}

	/**
	 * This class enables to change the speed of the video
	 */
	private class ChangeVideoSpeed implements ActionListener
	{
		private int compteur=0;

		public ChangeVideoSpeed()
		{
		}

		public void setCompteur(int newPos)
		{
			compteur = newPos;
		}


		@Override
		public void actionPerformed(ActionEvent e)
		{
			double totalSeconds = player.getDuration().getSeconds();
	    	int totalFrames = FramePositioningControl.FRAME_UNKNOWN;
	    	FramePositioningControl fpc = (FramePositioningControl)player.getControl("javax.media.control.FramePositioningControl");
	    	totalFrames = fpc.mapTimeToFrame(player.getDuration());
	    	double rateDouble = totalFrames/(totalSeconds*10); // number of frames each 100ms
	    	double diff = rateDouble - (int) rateDouble;
	    	int rate;
	    	if (diff>0.5)
	    		rate = (int)rateDouble + 1;
	    	else
	    		rate = (int)rateDouble;
	    	//  -> number of frames each 100ms*/
    		if (currentSpeed>0)
    		{
    			if (compteur < totalFrames)
    			{
    				fpc.seek(compteur);
    				compteur = compteur + rate;
    			}
    			else
        		{
        			fpc.seek(totalFrames);
        			if (looping)
        				compteur = 0;
        			else
        				timer.stop();
        		}
    		}
    		else
    		{
    			if (compteur > 0)
    			{
    				fpc.seek(compteur);
    				compteur = compteur - rate;
    			}
    			else
    			{
    				fpc.seek(0);
    				if (looping)
    					compteur = totalFrames;
    				else
    					timer.stop();
    			}
    		}
    	}
	}
}
