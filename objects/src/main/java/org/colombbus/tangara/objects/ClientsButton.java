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
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Configuration;
import org.colombbus.tangara.TGraphicalObject;

import org.apache.log4j.Logger;
/**
 * This class permits to create a box showing which clients are connected to an Internet component.
 * The subclass thread ClientsUpdate permits to update all the time the list of connected clients.
 * @author Benoit
 */
@SuppressWarnings("serial")
@Localize(value="ClientsButton",localizeParent=true)
public abstract class ClientsButton extends TGraphicalObject
{
	private static final int POLLING_INTERVAL = 5000;
	
	/** Class logger */
	private static Logger LOG = Logger.getLogger(ClientsButton.class);

	private ClientsUpdate update;

	private ImageIcon icon;

	private JComboBox list;

	/**
	* Creates an instance of this class.
	*/
    @Localize(value="ClientsButton")
    public ClientsButton()
    {
    	super();
        setSize(150,30);
        setOpaque(false);
        setLayout(new BorderLayout());
        loadIcon();
        list = new JComboBox();
        list.setRenderer(new MyCellRenderer());
        add(list,BorderLayout.CENTER);
        displayObject();
    }

    /**
     *
     * Creates an instance of this class with the given Internet Component.
     * @param internet
     */
    @Localize(value="ClientsButton")
    public ClientsButton(Internet internet)
    {
        this();
        setInternet(internet);
	}

    /**
     * Sets the Internet component of the class.
     * @param internet
     */
	@Localize(value="common.setInternet")
    public void setInternet(Internet internet)
    {
    	if ((update == null)&&(internet!=null))
    	{
    		update = new ClientsUpdate(internet);
    		update.start();
    	}
    	else
    	{
    		update.setInternet(internet);
    	}
    }

	/**
	 * Gets the selected client.
	 * @return String
	 */
	@Localize(value="ClientsButton.getClient")
    public String getClient()
    {
        return (String)list.getSelectedItem();
    }

	/**
	 * Deletes the instance of the class.
	 */
    @Override
	public void deleteObject()
    {
    	if (update!=null)
    	{
           	update.stopUpdate();
    	}
    	super.deleteObject();
    }

    /**
     * Loads the icon used to indicate a user.
     */
	private void loadIcon()
	{
        try
        {
        	String path = Configuration.instance().getTangaraPath().getParentFile().getAbsolutePath().replace("\\", "/");
        	path+="/objects/resources/ClientsButton/user.png";
        	URL imageURL = new URL("file:"+path);
        	BufferedImage image = ImageIO.read(imageURL);
			icon = new ImageIcon(image);
        }
        catch (Exception e)
        {
        	LOG.error("could not load user.png "+e.getMessage());
        }
	}



    /**
     * This subclass thread permits to update all the time which clients are connected.
     * @author Benoit
     */
    class ClientsUpdate extends Thread
    {
    	private boolean mustStop=false;
    	private Internet internet;

    	/**
    	 * Starts the loop in run() which permits to update the client list.
    	 * Moreover, the Internet component is removed. It has to be reseted.
    	 */
    	public ClientsUpdate()
    	{
    	}

    	/**
    	* Starts the loop in run() which permits to update the client list.
    	 * The Internet component is replaced with the one given.
    	*/
    	public ClientsUpdate(Internet internet)
    	{
	    	this.internet = internet;
	   	}

    	/**
    	 * The Internet component is replaced with the one given.
    	 * @param internet
    	 */
    	public void setInternet(Internet internet)
    	{
    		this.internet = internet;
    	}

    	/**
    	 * Stops the loop in run() which updates the client list.
    	 */
    	public void stopUpdate()
    	{
    		mustStop = true;
    	}

    	/**
    	 * This is the run() method of the subclass thread.
    	 * It serves to update in a loop the client list.
    	 */
    	@Override
		public void run()
    	{
    		while (!mustStop)
   		{
   			List<String> clients = internet.getClients();
   			if (clients!=null)
   			{
   				for (String client:clients)
   				{
  					boolean toBeInserted = true;
   					for (int i=0;i<list.getItemCount();i++)
   					{
   						if (client.compareTo((String)(list.getItemAt(i)))==0)
   						{
   							toBeInserted = false;
   							break;
   						}
   					}
   					if (toBeInserted)
    				{
	    					list.addItem(client);
	    				}
	    			}
	    			int max = list.getItemCount();
	    			for (int i=0;i<max;i++)
	    			{
	    				boolean toBeDeleted = true;
	    				for (String client:clients)
	    				{
	    					if (client.compareTo((String)(list.getItemAt(i)))==0)
	    					{
	    						toBeDeleted = false;
	    						break;
	    					}
	    				}
	    				if (toBeDeleted)
	    				{
	    					list.removeItemAt(i);
	    					i--;
	    					max--;
	    				}
	    			}
	    		}
	    		try
	    		{
	    			Thread.sleep(POLLING_INTERVAL);
	    		}
	    		catch (InterruptedException e)
	    		{

	    		}
	    	}
	    }
	}


    /**
     * This subclass permits to specify how will appear the JLabel of the selected user.
     * It is loaded in the list component by the constructor of the main class.
     * @author Benoit
     */
    private class MyCellRenderer extends JLabel implements ListCellRenderer
   	{
   

   		@Override
		public Component getListCellRendererComponent(JList list,Object value, int index, boolean isSelected, boolean cellHasFocus)
	    {
			String s = (String)value;
            setText(s);
	        if (isSelected)
	        {
		         setBackground(list.getSelectionBackground());
		         setForeground(list.getSelectionForeground());
	        }
	         else {
	             setBackground(list.getBackground());
	             setForeground(list.getForeground());
             }
	         setEnabled(list.isEnabled());
		     setFont(list.getFont());
		     setOpaque(true);
		     if (icon!=null)
			     setIcon(icon);
		     return this;
		 }
    }
}
