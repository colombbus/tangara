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

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * Manager of the method popup. A popup shows the list of the methods associated
 * to an object. It occurs when the user types the dot character after the object
 * instance name in the code editor pane and after a short time.
 * It occurs also if we are back to the above situation after a backspace.
 * @author ben
 * @author gwen
 */
public class PopupManager extends Thread
{
    /**
     * Class logger
     */
    private static Logger LOG = Logger.getLogger(PopupManager.class);

    /**
     * Duration without key pressed before displaying the popup
     */
	private static int popupDelayMs = -1;

	/**
	 * Default duration before displaying the popup
	 */
    private static final int DEFAULT_POPUP_DELAY = 500;

    /**
     * Pane of the editor
     */
    private TextPane pane;

	/**
	 * Popup displaying the list of methods
	 */
	private TPopup popup;

	private int x,y;

	/**
	 * The visibility status of the popup
	 */
	private boolean visible = true;


	/**
	 *
	 */
	private List<String> values = new Vector<String>();
    Map<String,String> prototypes = new Hashtable<String,String>();     

	private static Collator collator = null;


	
	public PopupManager(EditorFrame aFrame, TextPane pane,Class<?> targetClass)
    {   
        this.pane = pane;
        popup = new TPopup(aFrame, this);
        init(targetClass);
    }

	

	private void init(Class<?> targetClass)
	{
        for (Method m:targetClass.getMethods())
        {           
            Usage u = m.getAnnotation(Usage.class);
            if (u!=null)
            {
                if (values.contains(u.value()))
                {
                    if (u.first())
                        // this value has already been set, but the new one has the "first" flag : we replace the old one by this one
                        prototypes.put(u.value(), u.prototype());
                }
                else
                {
                    values.add(u.value());
                    prototypes.put(u.value(),u.prototype());
                }
            }
        }
                
        if (collator == null)
        {
            String languageToUse = Configuration.instance().getDefaultLanguage();;
            String tangaraLanguage = Configuration.instance().getLanguage();
            String[] languagesList = Locale.getISOLanguages();
            for (String s : languagesList)
                if (s.equals(tangaraLanguage))
                {
                    languageToUse = tangaraLanguage;
                    break;
                }
            collator = Collator.getInstance(new Locale(languageToUse));
        }

        if (values.size()>0)
        {
            Collections.sort(values, collator);

            for (String value:values)
            {
                popup.addValue(value);
            }
            Point relativeLocation = pane.offsetToXY(pane.getCaretPosition());
            Point coordinates = pane.getLocationOnScreen();
            x = relativeLocation.x + coordinates.x+20;
            y = relativeLocation.y + coordinates.y;
            
            Rectangle screenSize = pane.getRootPane().getBounds();
            
            int height = popup.getVisibleRowCount()*popup.getLineHeight();
            if(y + height >= screenSize.height)
                y -= height;
            
        } else {
            visible = false;
        }

        if (popupDelayMs == -1)
        {
            try
            {
                popupDelayMs = Integer.parseInt(Configuration.instance().getProperty("popup.delay"));
            }
            catch(Exception e)
            {
                LOG.warn("Failure when tryin to read 'command.popup-delay'", e);
                popupDelayMs = DEFAULT_POPUP_DELAY;
            }
        }
	}
	
	/**
	 * Hides the popup and fill the type zone with the selected method
	 */
	public void closePopup(String typing, int lastKeyCode)
	{
		popup.setVisible(false);
		if (typing.length()>0)
		{
	        pane.getBuffer().insert(pane.getCaretPosition(), typing);
		}
		try
		{
		    pane.requestFocus();
			(new Robot()).keyPress(lastKeyCode);
		}
		catch (AWTException awtEx)
		{
			LOG.warn("Failure when trying to reproduce KeyCode", awtEx);
		}

	}

    public void closePopup()
    {
        if ((popup != null)&&popup.isVisible()) {
            popup.setVisible(false);
        }
    }
    
	public void closePopup(String value)
    {
        popup.setVisible(false);
	    String prototype = prototypes.get(value);
        insertPrototype(prototype);
	    pane.requestFocus();
    }

	
	/**
	 * Set the visibility of the popup menu
	 * @param visible show the popup if it equals <code>true</code>, hide it otherwise.
	 */
	public synchronized void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	/**
	 * Get the visibility status of the popup
	 * @return <code>true</code> if the popup is visible, <code>false</code> otherwise
	 */
	public synchronized boolean isVisible()
	{
		return visible;
	}

	/**
	 * Insert a method call
	 *
	 * @param prototype
	 *            the method call prototype
	 */
	private void insertPrototype(String prototype)
	{
        int initialPosition = pane.getCaretPosition();
        
        int shift = prototype.indexOf("(");
        pane.getBuffer().insert(initialPosition, prototype);
        if (shift>-1)
        {
            pane.setCaretPosition(initialPosition+shift+1);
        }
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		try
		{
			Thread.sleep(popupDelayMs);
		}
		catch (InterruptedException intEx)
		{
			LOG.debug("Popup run interrupted", intEx);
		}

		if (isVisible())
		{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
				public void run() {
                    popup.setLocation(x, y);
                    popup.setVisible(true);
                }
            });
		}
	}
}
