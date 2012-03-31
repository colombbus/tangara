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
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Configuration;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;


/**
 * This object permits to create some states with images. One can change the state by dragging the mouse.
 * Some actions can be executed when a state is set.
 * There are two modes: rotation or translation. In the latter, an orientation can be set.
 * @author Thierry
 *
 */
@SuppressWarnings("serial")
@Localize(value="Dimmer",localizeParent=true)
public abstract class Dimmer extends TGraphicalObject
{
    private static final String DEFAULT_EXTENSION = "png";	
	
    private Map<Double, State> stateValues = new Hashtable<Double, State>();    
    private BufferedImage currentImage;    
    private State currentState;

    private MouseAdapter mouseAdapter;
    private MouseMotionAdapter mouseMotionAdapter;

    /**
     * Creates an instance of this class.
     */
    @Localize(value="Dimmer")
    public Dimmer()
    {
        super();
        setSize(50,50);
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false); // in order to handle transparent images.
        
        addMouseAdapter();
        displayObject();
        centerOrigin();
    }

    /**
     * Draws the current image if there is one.
     */
    @Override
	public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (currentImage!=null)
        {
        	g.drawImage(currentImage, 0, 0, null);
            if (displayOrigin)
            {
            	g.setColor(Color.red);
            	g.drawLine((int)origin.getX()-5, (int)origin.getY()-5, (int)origin.getX()+5, (int)origin.getY()+5);
            	g.drawLine((int)origin.getX()-5, (int)origin.getY()+5, (int)origin.getX()+5, (int)origin.getY()-5);
            	g.setColor(Color.black);
            }
        }
    }

    /**
     * Makes a color transparent.
     * @param colorName
     */
    @Localize(value="Dimmer.transparentColor")
    public void transparentColor(String colorName)
    {
        Color transparentColor = TColor.translateColor(colorName, Color.black);
        TransparentFilter filter = new TransparentFilter(transparentColor);
    	if (currentImage!=null)
    	{
    		Image newPicture = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(currentImage.getSource(), filter));
    		currentImage = new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_ARGB);
    		currentImage.getGraphics().drawImage(newPicture,0,0,null);
    	}
        Iterator<Map.Entry<Double, State>> set = stateValues.entrySet().iterator();
        while (set.hasNext())
        {
            Map.Entry<Double, State> entry = set.next();
            BufferedImage stateImage = entry.getValue().stateImage;
            Image img2 =Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(stateImage.getSource(), filter));
            BufferedImage img3 = new BufferedImage(stateImage.getWidth(),stateImage.getHeight(),BufferedImage.TYPE_INT_ARGB);
            img3.getGraphics().drawImage(img2,0,0,null);
            entry.getValue().stateImage = img3;
        }
        repaint();
    }

    /**
     * Serves to make transparent a certain color.
     * @author Thierry
     *
     */
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


    /**
     * Creates a BufferedImage from a file.
     * @param fileName
     * @return
     * @throws Exception
     */
    private BufferedImage loadFile(String fileName) throws Exception
    {
        String extension = "";
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
            file = new File(Program.instance().getCurrentDirectory(), fileName);
            // if file does not exist, try with user home directory
            if (!file.exists())
                file = new File(Configuration.instance().getUserHome(),fileName);
            fileName = file.getAbsolutePath();
        }
        if (!(file.exists()))
            throw new Exception("file not found");
         BufferedImage newImage = ImageIO.read(file);
         return newImage;
    }

    /**
     * Sets the image of a state.
     * @param path
     */
    //@Localize(value="Dimmer.setImage")
    public void setImage(double stateValue, String path)
    {
    	if(stateValues.containsKey(stateValue))
    	{
        	State state = stateValues.get(stateValue);
            try
            {
            	state.stateImage = loadFile(path);
                repaint();
            }
            catch (Exception e)
            {
                String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", path);
                Program.instance().printError(message);
            }
    	}
    	else
    	{
    		String message = MessageFormat.format(getMessage("stateValue.error"), stateValue);
            Program.instance().writeMessage(message);
    	}
    }

    //----------------------------------------------------------------------------------------------
    

    /**
     * Adds a command to the command list.
     * @param stateName
     * @param cmd
     */
    @Localize(value="Dimmer.addCommand")
    public void addCommand(double stateValue, String cmd)
    {
    	if(stateValues.containsKey(stateValue))
    		stateValues.get(stateValue).stateCommands.add(cmd);
    	else
    	{
    		String message = MessageFormat.format(getMessage("stateValue.error"), stateValue);
            Program.instance().writeMessage(message);
    	}
    }

    /**
     * Removes all commands from the command list
     * @param stateName
     */
    @Localize(value="Dimmer.removeCommands")
    public void removeCommands(double stateValue)
    {
    	if(stateValues.containsKey(stateValue))
    		stateValues.get(stateValue).stateCommands.clear();
    	else
    	{
    		String message = MessageFormat.format(getMessage("stateValue.error"), stateValue);
            Program.instance().writeMessage(message);
    	}
    }

    /**
     * Runs all commands
     * @param stateName
     */
    //@Localize(value="Dimmer.executeCommands")
    public void executeCommands(double stateValue)
    {
    	if(stateValues.containsKey(stateValue))
    	{
	    	State state = stateValues.get(stateValue);
	        for (String command : state.stateCommands)
	        {
	            Program.instance().executeScript(command, getGraphicsPane());
	        }
    	}
    	else
    	{
    		String message = MessageFormat.format(getMessage("stateValue.error"), stateValue);
            Program.instance().writeMessage(message);
    	}
    }

    /**
     * Prepares the Mouse adapter used to specify what to do with the mouse.
     */
    private void addMouseAdapter()
    {
        mouseAdapter = new MouseAdapter()
        {
            @Override
			public void mouseClicked(MouseEvent e)
            {
            }

            @Override
			public void mousePressed(MouseEvent e)
            {
                computeMousePosition(e);
            }
        };

        mouseMotionAdapter = new MouseMotionAdapter()
        {
            @Override
			public void mouseDragged(MouseEvent e)
            {
            	computeMousePosition(e);
            }
        };

        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseMotionAdapter);
    }

    /**
     * Computes the mouse position and executes updateState() with that position.
     */
    private void computeMousePosition(MouseEvent e)
    {
    	newMousePosition = e.getPoint();
        double centerX = getObjectWidth()/2;
        double centerY = getObjectHeight()/2;
        double Dx = newMousePosition.getX() - centerX;
        double Dy = -newMousePosition.getY() + centerY;
        double distanceToCenter = Math.sqrt(Dx*Dx + Dy*Dy);
        double halfDiagonal = Math.sqrt(getObjectWidth()*getObjectWidth() + getObjectHeight()*getObjectHeight())/2;

        //If the mouse is close enough to the center of the Dimmer image, we have an action.
        //The distance must be inferior than half diagonal + 20.
        if (distanceToCenter < halfDiagonal + 20)
        {
        	double DeltaX = newMousePosition.getX() - origin.getX();
        	double DeltaY = - (newMousePosition.getY() - origin.getY());
            double orientedAngle = Math.atan2(DeltaY, DeltaX);
        	double newStatePosition = 0;
            if (rotationMode)
            {
                newStatePosition = orientedAngle*180/Math.PI;
                int finalAngle = (int)newStatePosition + 360;
                newStatePosition = finalAngle % 360;
            } else
            {
                double distance = Math.sqrt(DeltaX*DeltaX + DeltaY*DeltaY);
                orientedAngle -= orientation;
            	orientedAngle = 180/Math.PI*orientedAngle + 360;
                orientedAngle = (int) orientedAngle % 360;
                orientedAngle *= Math.PI/180;
            	newStatePosition = Math.cos(orientedAngle)*distance;
            }
            updateState(newStatePosition);
        }
    }

    private Point newMousePosition;
    private double currentStateValue=0;
    private double orientation=0;
    private boolean rotationMode = false;
    private boolean displayOrigin = false;
    private Point origin;

    /**
     * Updates the current state according to the new position of the mouse.
     * @param distance
     */
    private void updateState(double newPosition)
    {
        double newValue = newPosition;
        double newStateValue = currentStateValue;
        double distanceMin = Math.abs(currentStateValue - newValue);;
        double distance;
        Set <Double> values = stateValues.keySet();
        for (Double value : values)
        {
        	distance = Math.abs(value - newValue);
        	if ( distance < distanceMin)
        	{
        		distanceMin = distance;
        		newStateValue = value;
            }
        }
        if (rotationMode)
        {
        	Set <Double> values2 = stateValues.keySet();
            for (Double value : values2)
            {
            	distance = Math.abs(value - (newValue - 360));
            	if ( distance < distanceMin)
            	{
            		distanceMin = distance;
            		newStateValue = value;
                }
            }
        }
        setState(newStateValue);
    }

    /**
     * Activates or deactivates the mode in which the position of the origin is shown with a red cross.
     * @param value
     */
    @Localize(value="Dimmer.setDisplayOrigin")
    public void setDisplayOrigin(boolean value)
    {
    	displayOrigin = value;
    	repaint();
    }

    /**
     * Sets the origin with the given parameters. And shows this origin with a red cross.
     * @param X
     * @param Y
     */
    @Localize(value="Dimmer.setOrigin")
    public void setOrigin(int X, int Y)
    {
        origin = new Point(X, Y);
        repaint();
    }

    /**
     * Sets the origin at the center of the Dimmer.
     */
    //@Localize(value="Dimmer.centerOrigin")
    public void centerOrigin()
    {
    	setOrigin(getObjectWidth()/2, getObjectHeight()/2);
    }

    /**
     * Sets the orientation reference of the image (in degrees).
     * @param orientation
     */
    @Localize(value="Dimmer.setOrientation")
    public void setOrientation(double orientation)
    {
        this.orientation = orientation/180*Math.PI;
    }

    /**
     * Sets the mode of the Dimmer, either translation or rotation.
     * @param value
     */
    @Localize(value="Dimmer.setMode")
    public void setMode(String mode)
    {
    	boolean modeFound = false;
    	
        String translationModeList = getMessage("translationModeList");
        StringTokenizer translationStringTokenizer = new StringTokenizer(translationModeList, ",");
        while (translationStringTokenizer.hasMoreTokens())
        {
        	String token = translationStringTokenizer.nextToken();
            if (token.equals(mode))
            {
            	rotationMode = false;
            	modeFound = true;
            }
        }

        if (!modeFound)
        {
	    	String rotationModeList = getMessage("rotationModeList");
	    	StringTokenizer rotationStringTokenizer = new StringTokenizer(rotationModeList, ",");
	        while (rotationStringTokenizer.hasMoreTokens())
	        {
	        	String token = rotationStringTokenizer.nextToken();
	            if (token.equals(mode))
	            {
	            	rotationMode = true;
	            	return;
	            }
	        }
        }
        if (!modeFound)
        {
    		String message = MessageFormat.format(getMessage("mode.error"), mode);
            Program.instance().writeMessage(message);
        }
    }

    /**
     * Shows the list of states with their parameters.
     */
    //@Localize(value="Dimmer.writeStates")
    public void writeStates()
    {
    	String valueWord = getMessage("value");
    	String imageWord = getMessage("image");

    	Program.instance().writeMessage("");
    	Set <Double> values = stateValues.keySet();
    	for (Double value: values)
    	{
    		String toWrite = valueWord + " " + stateValues.get(value).stateValue;
    		toWrite += ", " + imageWord + " " + stateValues.get(value).stateImagePath;    		
    		Program.instance().writeMessage(toWrite);
    	}
    }    

    /**
     * Sets the new current state and execute the commands of this state.
     * @param stateName
     */
    @Localize(value="Dimmer.setState")
    public void setState(double stateValue)
    {
    	if (stateValues.containsKey(stateValue))
    	{
	        State newState = stateValues.get(stateValue);
	        if (newState != null && newState != currentState)
	        {
	            currentStateValue = stateValue;
	        	currentState = newState;
	            currentImage = currentState.stateImage;
				setObjectWidth(currentImage.getWidth());
			 	setObjectHeight(currentImage.getHeight());
	            repaint();
	            executeCommands(stateValue);
	        }
    	}
    	else
    	{
    		String message = MessageFormat.format(getMessage("stateValue.error"), stateValue);
            Program.instance().writeMessage(message);
    	}
    }

    /**
     * Removes a state from the states list.
     * @param imageName
     */
    @Localize(value="Dimmer.removeState")
    public void removeState(double stateValue)
    {
    	if (stateValues.containsKey(stateValue))
    	{
	    	stateValues.remove(stateValue);
	    	if ((currentState!=null)&&(currentState.stateValue == stateValue))
	    		currentState=null;
	    	repaint();
    	}
    	else
    	{
    		String message = MessageFormat.format(getMessage("stateValue.error"), stateValue);
            Program.instance().writeMessage(message);
    	}
    }

    /**
     * Creates a new state.
     * @param stateName
     * @param stateImage
     * @param stateValue
     */
    @Localize(value="Dimmer.addState")
    public void addState(double stateValue, String stateImagePath)
    {
    	if (!(stateValues.containsKey(stateValue)))
    	{
    		stateValues.put(stateValue,new State(stateValue, stateImagePath));
    		setImage(stateValue, stateImagePath);
    	}
    	else
    	{
    		String message = MessageFormat.format(getMessage("stateValue.alreadyExists.error"), stateValue);
            Program.instance().writeMessage(message);
    	}
    }

    /**
     * This subclass serves to represent a state.
     * @author Thierry
     *
     */
    public class State
    {        
        private BufferedImage stateImage;
        private String stateImagePath;
        private double stateValue;
        private java.util.List<String> stateCommands= new Vector<String>();

        public State(double stateValue, String stateImagePath)
        {
            this.stateImagePath = stateImagePath;
            this.stateValue = stateValue;
        }
        
    }

}

