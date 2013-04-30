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

package org.colombbus.tangara;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;

/**
 * This abstract class is the super class for all graphical Tangara objects (like Characters, Buttons ....).
 * It defines the main characteristics of these objects and their listeners.
 * @author gwen
 *
 */
@SuppressWarnings("serial")
public abstract class TGraphicalObject extends JPanel
{
	protected static final Color DEFAULT_BACKGROUND = new Color(240,240,240);

	private boolean resizeW = false;
    private boolean resizeE = false;
	private boolean resizeN = false;
	private boolean resizeS = false;
	private boolean dragging = false;
    private boolean designMode = false;
    private int originX;
    private int originY;
    private int previousX;
    private int previousY;
	private int displayHintPopups;
    protected int shiftX;
    protected int shiftY;

    
	private Map<String,List<String>> handlers = new HashMap<String,List<String>>();
	private Map<String,List<TWitness>> witnesses = new HashMap<String,List<TWitness>>();

    private MouseAdapter designMouseAdapter;
    private MouseMotionAdapter designMouseMotionAdapter;

    private Map<Component, MouseListener[]> actualMouseListeners;
    private Map<Component, MouseMotionListener[]> actualMouseMotionListeners;
    protected boolean displayEvents = true;
    
    private GraphicsPane graphicsPane;

	/** Class logger */
    protected static Logger LOG = Logger.getLogger(TGraphicalObject.class);

    /**
     * Creates a new Tangara graphical object. It initializes the position, the color and the (mouse)listeners.
     *
     */
    public TGraphicalObject()
    {
    	setSize(0,0);
		graphicsPane = Program.instance().getCurrentGraphicsPane();

		
		displayHintPopups = Configuration.instance().getInteger("popup.display");
    	designMouseAdapter = new MouseAdapter()
    	{
    		//Improvement made to display a hint popup that indicates the name and the class of the objects:
    		@Override
			public void mouseEntered(MouseEvent e)
    		{
    			if (displayHintPopups == 1)
    			{
    				String className = TGraphicalObject.this.getClass().getSimpleName();
    				String objectName = Program.instance().getObjectName(TGraphicalObject.this);
    				String toolTipText = objectName + "  (" + className +")";
    				changeToolTip(TGraphicalObject.this, toolTipText);
    			}
    		}

			@Override
			public void mousePressed(MouseEvent e)
			{
				originX = getX();
				originY = getY();
				previousX = e.getX()+originX;
				previousY = e.getY()+originY;
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				int deltaX = e.getX()+getX()-previousX;
				int deltaY = e.getY()+getY()-previousY;
				if (resizeE)
				{
					String command = MessageFormat.format(Messages.getString("GraphicalObject.command.setWidth"), (TGraphicalObject.super.getWidth()+deltaX)); //$NON-NLS-1$
					executeMethod(command);
				}
				else if (resizeW)
				{
					String command = MessageFormat.format(Messages.getString("GraphicalObject.command.setWidth"), (TGraphicalObject.super.getWidth()-deltaX)); //$NON-NLS-1$
					executeMethod(command);
					int value = getX()+deltaX-originX;
					// move back the component in order to execute the moving command
					TGraphicalObject.super.setLocation(originX,getY());
					if (value>0)
					{
						command=MessageFormat.format(Messages.getString("GraphicalObject.command.moveForward"), value);
						executeMethod(command);
					}
					else if (value<0)
					{
						command=MessageFormat.format(Messages.getString("GraphicalObject.command.moveBackward"), -value);
						executeMethod(command);
					}
				}
				if (resizeS)
				{
					String command = MessageFormat.format(Messages.getString("GraphicalObject.command.setHeight"), (TGraphicalObject.super.getHeight()+deltaY)); //$NON-NLS-1$
					executeMethod(command);
				}
				else if (resizeN)
				{
					String command = MessageFormat.format(Messages.getString("GraphicalObject.command.setHeight"), (TGraphicalObject.super.getHeight()-deltaY)); //$NON-NLS-1$
					executeMethod(command);
					int value = getY()+deltaY-originY;
					// move back the component in order to execute the moving command
					TGraphicalObject.super.setLocation(getX(),originY);
					if (value>0)
					{
						command=MessageFormat.format(Messages.getString("GraphicalObject.command.moveDown"), value);
						executeMethod(command);
					}
					else if (value<0)
					{
						command=MessageFormat.format(Messages.getString("GraphicalObject.command.moveUp"), -value);
						executeMethod(command);
					}
				}
				if (dragging)
				{
					String command = MessageFormat.format(Messages.getString("GraphicalObject.command.setPosition"), (getX()+deltaX+shiftX),(getY()+deltaY+shiftY)); //$NON-NLS-1$
					executeMethod(command);
				}
			}
		};

    	designMouseMotionAdapter = new MouseMotionAdapter(){
			@Override
			public void mouseMoved(MouseEvent e)
			{
				int x = retrieveXCoordinate((Component)e.getSource(),e.getX());
				int y = retrieveYCoordinate((Component)e.getSource(),e.getY());
				resizeN = false;
				resizeS = false;
				resizeE = false;
				resizeW = false;
				dragging = false;
				if (x<5)
				{
					resizeW = true;
					if (y<5)
					{
						resizeN = true;
						setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
					}
					else if (y>TGraphicalObject.super.getHeight()-5)
					{
						resizeS = true;
						setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
					}
					else
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
					}
				}
				else if (x>TGraphicalObject.super.getWidth()-5)
				{
					resizeE = true;
					if (y<5)
					{
						resizeN = true;
						setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
					}
					else if (y>TGraphicalObject.super.getHeight()-5)
					{
						resizeS = true;
						setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
					}
					else
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					}
				}
				else if (y<5)
				{
					resizeN = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				}
				else if (y>TGraphicalObject.super.getHeight()-5)
				{
					resizeS = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				}
				else
				{
					dragging = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}

			@Override
			public void mouseDragged(MouseEvent e)
			{
				int newX = e.getX()+getX();
				int newY = e.getY()+getY();
				int deltaX = newX-previousX;
				int deltaY = newY-previousY;
				if (!dragging)
				{
					if (resizeE)
					{
						setObjectWidth(TGraphicalObject.super.getWidth()+deltaX);
					}
					else if (resizeW)
					{
						setObjectWidth(TGraphicalObject.super.getWidth()- deltaX);
						TGraphicalObject.super.setLocation(getX()+deltaX,getY());
					}
					if (resizeS)
					{
						setObjectHeight(TGraphicalObject.super.getHeight()+ deltaY);
					}
					else if (resizeN)
					{
						setObjectHeight(TGraphicalObject.super.getHeight()-deltaY);
						TGraphicalObject.super.setLocation(getX(),getY()+deltaY);
					}
				}
				else
				{
					TGraphicalObject.super.setLocation(getX()+deltaX,getY()+deltaY);
				}
				previousX = newX;
				previousY = newY;
			}
		};
    }


   protected BufferedImage loadPicture(String fileName)
    {
		URI file = getResource(fileName);
		try {
			if (file == null)
				throw new Exception("file not found");
			else {
				BufferedImage newImage = ImageIO.read(new File(file));
				return newImage;
	    	} 
		}
		catch (Exception e) {
            String message = MessageFormat.format(getMessage("load.error")+" ("+e.getMessage()+")", fileName);
            Program.instance().writeMessage(message);
		}
		return null;
    }
    
    /**
     * Gets a message from the message_language.properties of the object's resources.
     * @param key
     * @return
     */
    public String getMessage(String key) {
    	String result = null;
    	Class<?> currentClass = this.getClass();
    	while(result==null &&  !currentClass.getName().equals(TGraphicalObject.class.getName())) {
    		result = MessagesForObjects.getString(currentClass.getName(), key);
    		currentClass = currentClass.getSuperclass();
    	}
		if (result == null) {
			result = '!'+key+'!';
		}
    	return result;
    }

	/**
	 * Returns if the .properties contains the key
	 * @param key
	 * 		the string to check
	 * @return
	 * 		a boolean
	 */
    public boolean containsMessage(String key)
    {
    	boolean result = false;
    	Class<?> currentClass = this.getClass();
    	while(!( result ||  currentClass.getName().equals(TGraphicalObject.class.getName()))) {
    		result = MessagesForObjects.containsMessage(currentClass.getName(), key);
    		currentClass = currentClass.getSuperclass();
    	}
    	return result;
    }

     /**
     * Changes the hint popup with the given String.
     * @param comp
     * @param tooltipText
     */
    private void changeToolTip(JComponent comp, String tooltipText)
    {
    	comp.setToolTipText(tooltipText);
    	Component[] children = comp.getComponents();
    	if (children!=null)
    		for (Component child:children)
    	    	if (child instanceof JComponent)
    	    		changeToolTip((JComponent)child, tooltipText);
    }

   /**
    * Retrieves the horizontal coordinate of this graphical object from one of its children (components).
    * @param childComp
    * 		one child (component) of this Graphical object
    * @param x
    * 		the horizontal origin
    * @return
    * 		the horizontal coordinate
    */
    private int retrieveXCoordinate(final Component childComp, final int x)
    {
    	int xCoord = x;
    	for (Component currentComp = childComp; currentComp != this;)
    	{
    		xCoord+=currentComp.getX();
    		currentComp = currentComp.getParent();
    	}
    	return xCoord;
    }

    /**
     * Retrieves the vertical coordinate of this graphical object from one of its children (components)
     * @param childComp
     * 		one child (component) of this graphical objects
     * @param y
     * 		the vertical origin
     * @return
     * 		the vertical coordinate
     */
    private int retrieveYCoordinate(final Component childComp, final int y)
    {
    	int yCoord = y;
    	for(Component currentComp = childComp; currentComp!=this;)
    	{
    		yCoord+=currentComp.getY();
    		currentComp = currentComp.getParent();
    	}
    	return yCoord;
    }

    /**
     * Moves forward this graphical object of <code>value</code>.
     * @param value
     * 		RepreseS=snts the value of the forward step according to the x-axis.
     */
    @Localize(value="common.moveForward")
	public void moveForward(int value)
    {
    	super.setLocation(getX()+value,getY());
    }

    /**
     * Moves backward this graphical object of <code>value</code>.
     * @param value
     * 		Represents the value of the backward step according to the x-axis.
     */
    @Localize(value="common.moveBackward")
	public void moveBackward(int value)
    {
    	super.setLocation(getX()-value,getY());
    }

    /**
     * Moves back this graphical object of <code>value</code>.
     * @param value
     * 		Represents the value of the backward step according to the y-axis.
     */
    @Localize(value="common.moveDown")
	public void moveDown(int value)
    {
    	super.setLocation(getX(),getY()+value);
    }

    /**
     * Moves up this graphical object of <code>value</code>.
     * @param value
     * 		Represents the value of the forward step according to the y-axis.
     */
    @Localize(value="common.moveUp")
	public void moveUp(int value)
    {
    	super.setLocation(getX(),getY()-value);
    }

    /**
     * Moves this component to a new location.
     * The top-left corner of the new location is specified by the x and y  parameters in the coordinate space of this component's parent.
     * @param x
     * 		the x-coordinate of the new location's top-left corner in the parent's coordinate space
     * @param y
     * 		the y-coordinate of the new location's top-left corner in the parent's coordinate space
     */
    @Localize(value="common.setObjectLocation1")
	public void setObjectLocation(int x, int y)
    {
    	super.setLocation(x-shiftX, y-shiftY);
    }
    
    @Localize(value="common.setObjectLocation2")
	public void setObjectLocation(double x, double y)
    {
    	super.setLocation((int)x-shiftX, (int)y-shiftY);
    }
    
    @Localize(value="common.setObjectLocation1")
    public void setObjectLocation(Point p) {
    	setObjectLocation(p.x, p.y);
    }
    
    @Localize(value="common.setObjectXCoordinate")
	public void setObjectXCoordinate(double x)
    {
    	super.setLocation((int)x-shiftX, getLocation().y);
    }
    
    @Localize(value="common.setObjectXCoordinate")
	public void setObjectXCoordinate(int x)
    {
    	super.setLocation(x-shiftX, getLocation().y);
    }
    
    @Localize(value="common.setObjectYCoordinate")
	public void setObjectYCoordinate(double y)
    {
    	super.setLocation(getLocation().x, (int)y-shiftY);
    }
    
    @Localize(value="common.setObjectYCoordinate")
	public void setObjectYCoordinate(int y)
    {
    	super.setLocation(getLocation().x, y-shiftY);
    }

    /**
     * Sets the width of this graphical object.
     * @param value
     * 		the new width value.
     */
    @Localize(value="common.setObjectWidth")
	public void setObjectWidth(int value)
    {
    	super.setSize(value,super.getHeight());
    	revalidate();
    }

    /**
     * Sets the height of this graphical object.
     * @param value
     * 		the new height value.
     */
    @Localize(value="common.setObjectHeight")
	public void setObjectHeight(int value)
    {
    	super.setSize(super.getWidth(),value);
    	revalidate();
    }

    /**
     * Gets the height of this graphical object.
     * @return
     * 		the graphical object's height.
     */
    @Localize(value="common.getObjectHeight")
	public int getObjectHeight()
    {
    	return super.getHeight();
    }

    /**
     * Gets the width of this graphical object.
     * @return
     * 		the graphical object's width.
     */
    @Localize(value="common.getObjectWidth")
	public int getObjectWidth()
    {
    	return super.getWidth();
    }

    /**
     * Translates the delete command to delete this object
     *
     */
    @Localize(value="common.delete")
	public void delete()
    {
    	deleteObject();
    }

    /**
     * Deletes this graphical object of its container if the container is a JComponent.
     *
     */
    public void deleteObject()
    {
        handlers.clear();
        witnesses.clear();
    	Container parent = getParent();
    	getParent().remove(this);
    	if (parent instanceof JComponent)
    	{
    		((JComponent)parent).revalidate();
    	}
    	parent.repaint();
        Program.instance().deleteObject(this);
    }

    /**
     * Runs the execution of the method passed as parameters.
     * @param method
     * 		the method to execute.
     */
    private void executeMethod(String method)
    {
    	Program.instance().executeMethod(this, method);
    }


    /**
     * Removes mouse and motion listeners from component and its children (if there are).
     * We keep mouse and motion listeners for design mode.
     * @param comp
     * 		the component to remove mouse and motion listeners.
     */
    private void recRemoveListeners(Component comp)
    {
		// 1st remove original mouse and motion listeners from component
    	//			and save them in actualMouseListeners and actualMouseMotionListeners
		MouseListener[] mouseListeners = comp.getMouseListeners();
		if (mouseListeners!=null)
		{
			for (MouseListener listener:mouseListeners)
			{
	    		comp.removeMouseListener(listener);
			}
			actualMouseListeners.put(comp,mouseListeners);
		}
		MouseMotionListener[] motionListeners = comp.getMouseMotionListeners();
		if (motionListeners!=null)
		{
			for (MouseMotionListener listener:motionListeners)
			{
	    		comp.removeMouseMotionListener(listener);
			}
			actualMouseMotionListeners.put(comp,motionListeners);
		}

		// 2nd add mouse and motion listeners for design mode
		comp.addMouseListener(designMouseAdapter);
		comp.addMouseMotionListener(designMouseMotionAdapter);

		// 3rd do the same operation for children components if any
		if (comp instanceof Container) {
			Component[] componentChildren = ((Container)comp).getComponents();
			if (componentChildren!=null)
			{
				for (Component child:componentChildren)
				{
					recRemoveListeners(child);
				}
			}
		}
    }

    /**
     * Adds saved mouse and motion listeners to the component and its children (if there are).
     * We remove design mouse and motion listeners from component.
     * @param comp
     * 		the component to add the saved listeners.
     */
    private void recRetrieveListeners(Component comp)
    {
    	// 1st remove design mouse and motion listeners from component
		comp.removeMouseListener(designMouseAdapter);
		changeToolTip(this, null);
		comp.removeMouseMotionListener(designMouseMotionAdapter);

		// 2nd add original mouse and motion listeners to component and its children if any
		if (actualMouseListeners!=null)
		{
			MouseListener[] mouseListeners = actualMouseListeners.get(comp);
			if (mouseListeners!=null)
			{
				for (MouseListener listener:mouseListeners)
				{
		    		comp.addMouseListener(listener);
				}
			}
		}
		if (actualMouseMotionListeners != null)
		{
			MouseMotionListener[] motionListeners = actualMouseMotionListeners.get(comp);
			if (motionListeners!=null)
			{
				for (MouseMotionListener listener:motionListeners)
				{
		    		comp.addMouseMotionListener(listener);
				}
			}
		}

		// 3rd do the same operation for children components if any
		if (comp instanceof Container) {
			Component[] children = ((Container)comp).getComponents();
			if (children!=null)
			{
				for (Component child:children)
				{
					recRetrieveListeners(child);
				}
			}
		}
    }

    /**
     * Selects if we are in design mode or not.
     * Design mode is used to move the object and to set its size.
     * In design mode, the object has only  design mouse and motion listeners.
     * @param value
     * 		true = in design mode
     */
    public void setDesignMode(boolean value)
    {
    	freeze(value);
    	designMode = value;
    	if (designMode)
    	{
    		actualMouseListeners = new Hashtable<Component,MouseListener[]>();
    		actualMouseMotionListeners = new Hashtable<Component,MouseMotionListener[]>();
    		// Remove mouse and mouse motion listeners from the component and its children if any
    		// and add mouse and motion listeners for design
    		recRemoveListeners(this);
    	}
    	else
    	{
    		// Remove design listeners and retrieve original listeners for component and its children
    		recRetrieveListeners(this);

    		// delete the original listeners list
			actualMouseListeners = null;
			actualMouseMotionListeners = null;

    		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	    resizeW = false;
    	    resizeE = false;
    	    resizeN = false;
    	    resizeS = false;
    	    dragging = false;
    	}
    }

    /**
     * This method can be overloaded in order to stop the activity of the object.
     * @param value
     */
    public void freeze(boolean value)
    {
    }

    /**
     * Gets the graphics pane where the graphical object is added
     * @return
     * 		the used graphics pane
     */
    public GraphicsPane getGraphicsPane()
    {
    	return graphicsPane;
    }

    /**
     * Adds this graphical object to the graphics pane list and display it
     *
     */
    protected void displayObject()
    {
    	setDesignMode(graphicsPane.getDesignMode());
    	if (SwingUtilities.isEventDispatchThread()) {
    		graphicsPane.addGraphicalObject(this);
    	}
    	else {
    		SwingUtilities.invokeLater(new Runnable(){
    			@Override
				public void run() {
    	    		graphicsPane.addGraphicalObject(TGraphicalObject.this);
    			}
    		});
    	}
    }


    /**
     * Loads a file in the resource folder of this object.
     * @param fileName
     * @return URI
     */
    public URI getResource(String fileName)
    {
    	String objectName = this.getClass().getSuperclass().getName();
    	objectName = objectName.substring(objectName.lastIndexOf(".")+1);
    	Configuration conf = Configuration.instance();
    	if (conf.isExecutionMode()) {
    		try {
    			return conf.getObjectsClassLoader().getResource("org/colombbus/tangara/objects/resources/"+objectName+"/"+fileName).toURI();
    		}
    		catch (Exception e) {
    			LOG.error("error trying to find resource '"+fileName+"'", e);
    			return null;
    		}
    	} else {
    		File resourceDirectory = new File(conf.getTangaraPath().getParentFile(), "objects/resources/"+objectName); 
    		File resourceFile = new File(resourceDirectory, fileName);
    		return resourceFile.toURI();
    	}

    }

    // EVENT MANAGEMENT
    protected boolean isEventRegistered(String eventName)
    {
    	Set<String> keys = handlers.keySet();
    	return keys.contains(eventName);
    }
    
    protected void registerEvent(String eventName)
    {
    	if (isEventRegistered(eventName))
    	{
    		LOG.error("Event '"+eventName+"' already registered");
    	}
    	else
    	{
    		List<String> newHandler = new ArrayList<String>();
    		handlers.put(eventName, newHandler);
    		List<TWitness> newWitness = new ArrayList<TWitness>();
    		witnesses.put(eventName, newWitness);
    	}
    }
    
    protected void unregisterEvent(String eventName)
    {
    	if (!isEventRegistered(eventName))
    	{
    		LOG.error("Cannot unregister event '"+eventName+"': this event is not registered");
    	}
    	else
    	{
    		handlers.remove(eventName);
    		witnesses.remove(eventName);
    	}
    }
    
    protected void addHandler(String eventName, String handler)
    {
    	if (!isEventRegistered(eventName))
    	{
    		LOG.error("Cannot add handler to event '"+eventName+"': this event is not registered");
    	}
    	else
    	{
    		List<String> list = handlers.get(eventName);
    		list.add(handler);
    	}
    }    

    protected void addWitness(String eventName, TWitness witness)
    {
    	if (!isEventRegistered(eventName))
    	{
    		LOG.error("Cannot add witness to event '"+eventName+"': this event is not registered");
    	}
    	else
    	{
    		List<TWitness> list = witnesses.get(eventName);
    		list.add(witness);
    	}
    }
    
    protected void clearHandlers(String eventName)
    {
    	if (!isEventRegistered(eventName))
    	{
    		LOG.error("Cannot clear handlers for event '"+eventName+"': this event is not registered");
    	}
    	else
    	{
    		List<String> list = handlers.get(eventName);
    		list.clear();
    	}
    }    
    
    protected void clearWitnesses(String eventName)
    {
    	if (!isEventRegistered(eventName))
    	{
    		LOG.error("Cannot clear witnesses for event '"+eventName+"': this event is not registered");
    	}
    	else
    	{
    		List<TWitness> list = witnesses.get(eventName);
    		list.clear();
    	}
    }   
    
    protected void clearEvent(String eventName)
    {
    	if (!isEventRegistered(eventName))
    	{
    		LOG.error("Cannot clear event '"+eventName+"': this event is not registered");
    	}
    	else
    	{
    		clearWitnesses(eventName);
    		clearHandlers(eventName);
    	}
    }
    
    protected void witnessEvent(String eventName, HashMap<String,Object> data)
    {
		List<TWitness> witnessesList = witnesses.get(eventName);
		for (TWitness witness:witnessesList)
		{
			witness.setObject(this);
			if (data != null)
				witness.setContext(data);
		}
    }
    
    protected void processEvent(String eventName)
    {
    	processEvent(eventName, null);
    }
    
    protected void processEvent(String eventName, HashMap<String,Object> data)
    {
    	if (!isEventRegistered(eventName))
    	{
    		LOG.error("Cannot process event '"+eventName+"': this event is not registered");
    	}
    	else
    	{
    		// 1st retrieve the corresponding data
    		//  making a copy, because execution of handlers may alter this list (e.g. in case of a "clearEvent")
    		List<String> handlersList = new ArrayList<String>(handlers.get(eventName));
    		if (handlersList.size()>0)
    		{
	    		// 2nd set the witnesses according to the info, if provided
	    		if (data !=null)
	    			witnessEvent(eventName, data);
	    		// 3rd execute handlers
	    		for (String handler:handlersList)
	    		{
	    			Program.instance().executeScript(handler, displayEvents);
	    		}
    		}
    	}
    }
    
    protected List<TWitness> getWitnesses(String eventName)
    {    	
    	if (!isEventRegistered(eventName))
    	{
    		LOG.error("Cannot get witnesses for event '"+eventName+"': this event is not registered");
    		return null;
    	}
    	else
    	{
    		return witnesses.get(eventName);
    	}
    }
    
    public void shiftLocation(int x, int y)
    {
    	shiftX = x;
    	shiftY = y;
    	TGraphicalObject.super.setLocation(getX()-shiftX,getY()-shiftY);
    }
    
    public Point getObjectLocation()
    {
    	return new Point(getObjectX(),getObjectY());
    }

    public int getObjectX()
    {
    	return shiftX + super.getX();
    }

    public int getObjectY()
    {
    	return shiftY + super.getY();
    }
    
    public Rectangle getObjectBounds()
    {
    	return new Rectangle(getObjectX(),getObjectY(),super.getWidth(),super.getHeight());
    }

    private void recAddMouseListener(Component comp, MouseListener ml) {
    	comp.addMouseListener(ml);
    	if (comp instanceof Container) {
    		for (Component child:((Container)comp).getComponents())
    			recAddMouseListener(child, ml);
    	}
    }

    private void recRemoveMouseListener(Component comp, MouseListener ml) {
    	comp.removeMouseListener(ml);
    	if (comp instanceof Container) {
    		for (Component child:((Container)comp).getComponents())
    			recRemoveMouseListener(child, ml);
    	}
    }
    
    public void addGlobalMouseListener(MouseListener ml) {
    	recAddMouseListener(this, ml);
    }

    public void removeGlobalMouseListener(MouseListener ml) {
    	recRemoveMouseListener(this, ml);
    }

    private void recAddKeyListener(Component comp, KeyListener kl) {
    	comp.addKeyListener(kl);
    	if (comp instanceof Container) {
    		for (Component child:((Container)comp).getComponents())
    			recAddKeyListener(child, kl);
    	}
    }

    private void recRemoveKeyListener(Component comp, KeyListener kl) {
    	comp.removeKeyListener(kl);
    	if (comp instanceof Container) {
    		for (Component child:((Container)comp).getComponents())
    			recRemoveKeyListener(child, kl);
    	}
    }
    
    public void addGlobalKeyListener(KeyListener kl) {
    	recAddKeyListener(this, kl);
    }

    public void removeGlobalKeyListener(KeyListener kl) {
    	recRemoveKeyListener(this, kl);
    }

    
    
}
