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
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class provides an object able to display a sound.
 * @author Dominique
 *
 */
@SuppressWarnings("serial")
@Localize(value="DisplaySound",localizeParent=true)
public abstract class DisplaySound extends TGraphicalObject
{
	private static final int REFRESH_INTERVAL = 40;
	private static final int DEFAULT_RATIO = 61; // i.e. number of pixels corresponding to a second

	private Sound sound;
    private int[] data;
    private int max;
    private int min;
    private int ratio = DEFAULT_RATIO; // i.e. number of pixels corresponding to a second
    private int zoomFactor = 0; // i.e. number of samples per pixel
	private BufferedImage buffer;
	private boolean play = false;
	private int playLocation = 0;
    private Object updatePlay = new Object();
    private float frameRate;
    private static int maxXCursor;
	private static int xCursor;

	/**
	 * Creates a new instance of displaySound
	 */
    @Localize(value="DisplaySound")
	public DisplaySound()
	{
		setSize(400,100);
    	setBackground(Color.BLACK);
		addMouseListener(new MouseAdapter() {
            @Override
			public void mousePressed(MouseEvent evt) {
        		xCursor = evt.getX();
            	if (xCursor>maxXCursor)
            		xCursor = maxXCursor;
        		repaint();
            }
		});
    	displayObject();

	}

    /**
     * Creates a new instance of DisplaySound and sets the sound associated with.
     * @param s
     */
    @Localize(value="DisplaySound")
	public DisplaySound(Sound s)
    {
    	this();
    	setSound(s);
    }

    /**
     * Sets the current sound.
     * @param mySound
     */
    @Localize(value="DisplaySound.setSound")
	public void setSound(Sound mySound)
	{
		sound = mySound;
		AudioInputStream input = sound.getInputStream();
		if (input !=null)
		{
			AudioFormat format = input.getFormat();
			frameRate = format.getFrameRate();
			int bytesPerSample = (format.getSampleSizeInBits()/8);
			boolean bigEndian = format.isBigEndian();
			long frameLength = input.getFrameLength();
			data = new int[(int)frameLength];
			// Read data. If stream is multi-channels (e.g. stereo), we take only the first channel
			// Handle only 8bits and 16bits encoding
			try
			{
			    int size = format.getFrameSize();
			    byte[] bytes = new byte[size];
				switch (bytesPerSample)
				{
					case 1 : // 8bits
					    for (int i=0;i<frameLength;i++)
						{
							if (input.read(bytes)==size)
							    data[i] = 0x00FF&bytes[0]; //unsigned value
						}
						// set min and max values
						min = 0;
						max = 255;
						break;
					case 2 : // 16 bits
						for (int i=0;i<frameLength;i++)
						{
							if (input.read(bytes)==size)
							{
    							if (bigEndian)
    								data[i] = bytes[0]*256 + bytes[1]; // signed values
    							else
    								data[i] = bytes[1]*256 + bytes[0]; // signed values
							}
						}
						// set min and max values
						min = -32768;
						max  = 32767;
						break;
				}
			}
			catch (IOException e)
			{
				Program.instance().writeMessage(getMessage("readingError") + e.getMessage());
			}
		}
		sound.registerDisplay(this);
		updateGraphics();
	}

    /**
     * Updates Graphics.
     */
	private void updateGraphics()
	{

		if (data !=null)
		{
			// compute zoomFactor
			zoomFactor = (int)(frameRate/ratio);
			int width = getSize().width;
			int height = getSize().height;
			buffer = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			Graphics g = buffer.getGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.green);
			if (data.length>0)
			{
    			int previousY = (((max - data[0])*height)/(max - min));
    			for (int x = 1;x<width;x++)
    			{
    				int sample = x*zoomFactor;
    				if (sample<data.length)
    				{
    					int y = (((max - data[sample])*height)/(max - min));
    					g.drawLine(x-1,previousY,x,y);
    					previousY = y;
    				}
    			}
			}
			xCursor=0;
			maxXCursor = Math.min(width,data.length/zoomFactor);
			repaint();
		}
	}

	/**
	 * Sets the height
	 * @param value
	 * 		the new height
	 */
    @Override
	@Localize(value="common.setObjectHeight")
	public void setObjectHeight(int value)
	{
		super.setObjectHeight(value);
		updateGraphics();
	}

    /**
     * Sets the width
     * @param width
     * 		the new width
     */
    @Override
	@Localize(value="common.setObjectWidth")
	public void setObjectWidth(int value)
	{
		super.setObjectWidth(value);
		updateGraphics();
	}

    /**
     * Zooms
     */
    @Localize(value="DisplaySound.zoom")
	public void zoom()
	{
		ratio = Math.min(1001,ratio+20);
		updateGraphics();
	}

    /**
     * Dezooms
     */
    @Localize(value="DisplaySound.dezoom")
	public void dezoom()
	{
		ratio = Math.max(1, ratio-20);
		updateGraphics();
	}

    /**
     * Sets the location of the reading
     * @param position
     * 		the new position
     */
	private void setPlayLocation(long position)
	{
		synchronized(updatePlay)
		{
			playLocation = (int)(position/zoomFactor);
			repaint();
		}
	}

	/**
	 * Paints the current sound.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (data == null||buffer==null)
			return;
		g.drawImage(buffer, 0, 0, null);
		if (play)
		{
			g.setColor(Color.red);
			synchronized(updatePlay)
			{
				g.fillRect(playLocation, 0, 2,getSize().height);
			}
		}
		if (xCursor>0)
		{
			g.setColor(Color.white);
			g.drawLine(xCursor, 0, xCursor, getObjectHeight());
		}
	}

	/**
	 * Returns the cursor location in milliseconds.
	 */
    @Localize(value="DisplaySound.getCursorLocation")
	public long getCursorLocation()
	{
		return (xCursor*1000/ratio);
	}

	public void start()
	{
		play = true;
		(new PlayThread()).start();
	}

	/**
	 * The thread used to play a sound.
	 * @author Lionel.
	 *
	 */
	class PlayThread extends Thread
	{
		@Override
		public void run()
		{
			while (sound.isPlaying())
			{
				setPlayLocation(sound.getCurrentPosition());
				try
				{
					sleep(REFRESH_INTERVAL);
				}
				catch (InterruptedException e)
				{
				}
			}
			play = false;
			repaint();
		}
	}

}
