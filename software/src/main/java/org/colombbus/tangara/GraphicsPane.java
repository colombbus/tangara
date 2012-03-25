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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import org.apache.log4j.Logger;
import org.colombbus.build.Localize;

/**
 * The pane that represents the Game Area (between the banner and the options bar)
 * @author Lionel
 *
 */
@SuppressWarnings("serial")
@Localize(value="GraphicsPane")
public abstract class GraphicsPane extends JLayeredPane {
    
	private static final Logger LOG = Logger.getLogger(GraphicsPane.class);
    
	/** The programme object */
	private Program programme = Program.instance();
	
	private boolean rulers = false;
	private boolean designMode = false;
	
	private ArrayList<MouseListener> globalMouseListeners;
	private ArrayList<KeyListener> globalKeyListeners;

	/**
	 * Creates a new instance of Graphics Pane
	 *
	 */
	public GraphicsPane() {	
		setDoubleBuffered(true);
		globalMouseListeners = new ArrayList<MouseListener>();
		globalKeyListeners = new ArrayList<KeyListener>();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (designMode)
			drawDesignMode(g);

		if (rulers)
			drawRulers(g);
	}

	/**
	 * Adds the specified component to this container at the first position.
	 * @param c
	 * 		the component to be added
	 */
	public void addToTop(Component c) {
		add(c, 0);
		revalidate();
		repaint();
	}

	/**
	 * Decides to display the rulers or not
	 * @param value
	 * 		a boolean to decide
	 */
	public void displayRulers(boolean value)
	{
		rulers = value;
		repaint();
	}

	/**
	 * Draws the lines when you use the construction mode in commands mode
	 * @param g
	 * 		the Graphics context in which to paint
	 */
	private void drawDesignMode(Graphics g)
	{
		int width = this.getWidth();
		int height = this.getHeight();
		g.setColor(new Color(156,199,213));
		for (int i=20;i<width+height;i+=20)
		{
			g.drawLine(i, 0, 0, i);
		}
	}

	/**
	 * Draws the rulers when you choose to display it
	 * @param g
	 */
	private void drawRulers(Graphics g)
	{
		int width = this.getWidth();
		int height = this.getHeight();
		g.setColor(Color.black);
		g.setFont(new Font("Monospaced",Font.PLAIN,10));
		for (int i=0;i<width;i+=100)
		{
			g.drawLine(i, 0, i, 3);
			for (int j=i+10;j<i+100;j+=10)
			{
				g.drawLine(j, 0, j, 1);
			}
			if (i>0)
			{
				String value = String.valueOf(i);
	            FontMetrics fontMetrics = g.getFontMetrics();
	            int shift = (fontMetrics.stringWidth(value)/2);
	            g.drawString(value, i-shift, 3+fontMetrics.getHeight());

			}
		}
		for (int i=0;i<height;i+=100)
		{
			g.drawLine(0, i, 3, i);
			for (int j=i+10;j<i+100;j+=10)
			{
				g.drawLine(0, j, 1, j);
			}
			if (i>0)
			{
				String value = String.valueOf(i);
	            FontMetrics fontMetrics = g.getFontMetrics();
	            int shift = (fontMetrics.getHeight()/2);
	            g.drawString(value, 4, i+shift);
			}
		}
	}



	//-----------------------------------------------------------------------------------------

	//Internationalization of the methods:

    /**
     * Sets the language of the Graphics Pane so that the appropriate commands
     * are available for the BSFEngine.
     */
	public void declareBeanForTheScreen()
	{
		try {
			String beanName = Messages.getString("Main.bean.screen"); //$NON-NLS-1$

			try {
				String language = Configuration.instance().getLanguage();
				String className = "org.colombbus.tangara."+ language + ".GraphicsPane_" + language;
				Class<?> type = Class.forName(className);

				//If there is a current bean, we will declare only a new one, not the same one.
				//TODO However, with a command on another TWindow than the main one, the bean is redeclared twice at each command.
				GraphicsPane currentBean = (GraphicsPane) Configuration.instance().getManager().lookupBean(beanName);
				if (currentBean != null)
				{
					if ( ! currentBean.equals(this) )
					{ //We have a new bean so we must declare it.
						Configuration.instance().getManager().undeclareBean(beanName);
						Configuration.instance().getManager().declareBean(beanName, this, type);
					}
				} else //The first bean for the screen is declared in the Main, at the starting of Tangara.
					Configuration.instance().getManager().declareBean(beanName, this, type);
			} catch (ClassNotFoundException e) { // If the language is unknown, the English version is declared.
				Configuration.instance().getManager().declareBean(beanName, this, GraphicsPane.class);
			}
		} catch (Exception e) {
			LOG.error("Fail to declare screen bean", e);
		}

	}


	//International methods:

	/**
	 * Returns the width of the Graphics Pane
	 * @return
	 * 		the width
	 */
	@Override
	@Localize(value="GraphicsPane.getWidth")
	public int getWidth()
	{
		if (!isShowing())
		{
			Toolkit tk = Toolkit.getDefaultToolkit();
			JFrame frame = (JFrame)getTopLevelAncestor();
			return tk.getScreenSize().width-tk.getScreenInsets(getGraphicsConfiguration()).left-tk.getScreenInsets(getGraphicsConfiguration()).left-frame.getInsets().left-frame.getInsets().right;
		}
		return super.getWidth();
	}

	/**
	 * Returns the height of the Graphics Pane.
	 * @return
	 * 		the height
	 */
	@Override
	@Localize(value="GraphicsPane.getHeight")
	public int getHeight()
	{
		if (!isShowing())
		{
			Toolkit tk = Toolkit.getDefaultToolkit();
			JFrame frame = (JFrame)getTopLevelAncestor();
			return getGraphicsConfiguration().getBounds().height- tk.getScreenInsets(getGraphicsConfiguration()).top-tk.getScreenInsets(getGraphicsConfiguration()).bottom-frame.getInsets().top-frame.getInsets().bottom;
		}
		return super.getHeight();
	}

	/**
	 * Adjusts the size of Graphics Pane depending on the size of the window
	 *
	 */
	@Localize(value="GraphicsPane.adjustSize")
	public void adjustSize()
	{
    	Container top = this.getTopLevelAncestor();
    	if (top instanceof TFrame)
    	{
    		boolean result = ((TFrame)top).computeSize();
    		((TFrame)top).pack();
    		if (!result)
    			((TFrame)top).setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
    	}
	}

	/**
	 * Deletes the Graphics Pane.
	 *
	 */
	@Localize(value="GraphicsPane.clear")
	public void clear()
	{
		removeGraphicalObjects();
	}

    //end of international methods
//--------------------------------------------------------------------------------------------


    /**
     * Removes all the children components of the graphics pane
     *
     */
	public void removeGraphicalObjects()
	{
		Component[] children = getComponents();
    	for (Component child:children)
    	{
    		if (child instanceof TGraphicalObject)
    		{
    			((TGraphicalObject)child).deleteObject();
    		}
    	}
		removeAll();
		revalidate();
		repaint();
	}



    /**
     * Writes a message in the output pane
     * @param message
     * 		the message to print
     */
	public void showMessage(String message) {
		if (programme != null) {
			programme.writeMessage(message);
		}
	}


	/**
	 * 	Writes a message according to the attributes associated
	 * @param message
	 * 			the message to print
	 * @param attr
	 * 			the format
	 */
	public void showMessage(String message, int style) {
		if (programme != null) {
			programme.printMessage(message, style);
		}
	}

	/**
	 * Allows to choose the design mode (command or program)
	 * @param value
	 * 		true = command mode, false = program mode
	 */
	public void setDesignMode(boolean value)
	{
		designMode = value;
    	Component[] children = getComponents();
    	for (Component child:children)
    	{
    		if (child instanceof TGraphicalObject)
    		{
    			((TGraphicalObject)child).setDesignMode(value);
    		}
    	}
    	Collection<TObject> TObjectVector =  Program.instance().getTObjectsVector();
    	for (TObject object : TObjectVector)
    	{
    		object.freeze(value);
    	}
		repaint();
	}

	/**
	 * Gets the Tangara editing mode
	 * @return
	 * 		a boolean that represents Tangara editing mode
	 */
	public boolean getDesignMode()
	{
		return designMode;
	}


	/**
	 * Freezes or defreezes all the graphicsPanes with the given value.
	 */
    public void freeze(boolean value)
    {
    	if (!getDesignMode())
    	{
	    	Collection<TWindow> windows = Program.instance().getWindows();
	    	for (TWindow window : windows)
	    	{
	    		window.getGraphicsPane().freezesThisGraphicsPane(value);
	    	}
	    	Program.instance().getFrame().getGraphicsPane().freezesThisGraphicsPane(value);
    	}
    }

    /**
	 * Freezes or gameAreaPanel all children of the graphics pane (TObject or TGraphicalObject)
	 * @param value
	 * 		a boolean that represents if you choose to freeze or not
	 */
    public void freezesThisGraphicsPane(boolean value)
    {
    	Component[] children = getComponents();
    	for (Component child:children)
    	{
    		if (child instanceof TGraphicalObject)
    		{
    			((TGraphicalObject)child).freeze(value);
    		}
    	}
    	Collection<TObject> TObjectVector =  Program.instance().getTObjectsVector();
    	for (TObject object : TObjectVector)
    	{
    		object.freeze(value);
    	}
    }

    /**
     * This method returns all graphical elements at position (x,y)
     *     
     */
    public Component [] getComponentsAt(int x, int y)
    {
    	ArrayList <Component> list = new ArrayList<Component>();
    	Component [] children = getComponents();
    	for (Component child : children)
    	{
    		if (child.contains(x-child.getX(), y-child.getY()))
    			list.add(child);
    	}
    	Component [] retour = new Component[list.size()];
    	return list.toArray(retour);
    }

    public void addGlobalMouseListener(MouseListener ml) {
    	if (!globalMouseListeners.contains(ml)) {
    		Component[] children = getComponents();
        	for (Component child:children) {
        		if (child instanceof TGraphicalObject) {
        			((TGraphicalObject)child).addGlobalMouseListener(ml);
        		}
        	}
    		globalMouseListeners.add(ml);
    		this.addMouseListener(ml);
    	}
    }
    

    public void removeGlobalMouseListener(MouseListener ml) {
    	if (globalMouseListeners.contains(ml)) {
    		Component[] children = getComponents();
        	for (Component child:children) {
        		if (child instanceof TGraphicalObject) {
        			((TGraphicalObject)child).removeGlobalMouseListener(ml);
        		}
        	}
    		globalMouseListeners.remove(ml);
    		this.removeMouseListener(ml);
    	}
    	
    }
    
    public void addGlobalKeyListener(KeyListener kl) {
    	if (!globalKeyListeners.contains(kl)) {
    		Component[] children = getComponents();
        	for (Component child:children) {
        		if (child instanceof TGraphicalObject) {
        			((TGraphicalObject)child).addGlobalKeyListener(kl);
        		}
        	}
        	globalKeyListeners.add(kl);
    		this.addKeyListener(kl);
    	}
    }
    

    public void removeGlobalKeyListener(KeyListener kl) {
    	if (globalKeyListeners.contains(kl)) {
    		Component[] children = getComponents();
        	for (Component child:children) {
        		if (child instanceof TGraphicalObject) {
        			((TGraphicalObject)child).removeGlobalKeyListener(kl);
        		}
        	}
        	globalKeyListeners.remove(kl);
    		this.removeKeyListener(kl);
    	}
    	
    }

    public void addGraphicalObject(TGraphicalObject object) {
		for (MouseListener ml:globalMouseListeners) {
			object.addMouseListener(ml);
		}
		for (KeyListener kl:globalKeyListeners) {
			object.addKeyListener(kl);
		}
    	add(object,new Integer(1),0);
    	object.setObjectLocation(0,0);
    	revalidate();
    }

    
}
