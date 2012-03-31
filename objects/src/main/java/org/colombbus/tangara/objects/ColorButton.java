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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class permits to have a Button displaying a list of colors.
 * @author Benoit
 */
@SuppressWarnings("serial")
@Localize(value="ColorButton", localizeParent=true)
public abstract class ColorButton extends TGraphicalObject {

	private List<String> commands = new Vector<String>();
	private JComboBox list;

	/**
	 * Creates a new color button.
	 */
	@Localize(value="ColorButton")
    public ColorButton()
    {
    	super();
    	setSize(300,30);
    	setLayout(new BorderLayout());
        list = new JComboBox(TColor.getColors());
        list.addActionListener(new java.awt.event.ActionListener() {
        @Override
		public void actionPerformed(java.awt.event.ActionEvent evt) {
            SwingUtilities.invokeLater(new Runnable(){
            	@Override
				public void run()
            	{
            		executeCommand();
            	}
            });
        }
        });
        list.setRenderer(new MyCellRenderer());
        
        SwingUtilities.invokeLater(new Runnable(){
        	@Override
			public void run()
        	{
        		computeWidth();
        	}
        });
        
        add(list,BorderLayout.CENTER);
        displayObject();
    }

	/**
	 * Creates a new color button and sets the main color
	 * @param colorName
	 */
	@Localize(value="ColorButton")
    public ColorButton(String colorName)
    {
        this();
        setColor(colorName);
    }

   /**
    * Sets the selected color.
    * @param colorName
    */
    @Localize(value="common.setColor")
	public void setColor(String colorName)
    {
    	list.setSelectedItem(colorName);
    }
    
    /**
     * Add a command to the command list.
     * @param cmd
     */
    @Localize(value="common.addCommand")
	public void addCommand(String cmd)
    {
    	commands.add(cmd);
    }

    /**
     * Clears the command list.
     */
    @Localize(value="common.removeCommands")
	public void removeCommands()
    {
    	commands.clear();
    }

    /**
     * Clears the command list.
     */
    @Localize(value="ColorButton.getColor")
	public String getColor()
    {
    	return (String)list.getSelectedItem();
    }
    
    /**
     * Computes the width.
     */
    private void computeWidth()
    {
    	Graphics g = list.getGraphics();
        FontMetrics fontMetrics = g.getFontMetrics();
        int max = 0;
    	for (int i=0;i<list.getItemCount();i++)
    	{
    		max = Math.max(max, fontMetrics.stringWidth((String)list.getItemAt(i)));
    	}
    	setObjectWidth(max+60);
    }

    /**
     * Executes the commans of the command list.
     */
    private void executeCommand()
	{
		String colorName = (String)list.getSelectedItem();
    	for (String command:commands)
        {
        	String newCommand = command.replaceAll("%", colorName);
            Program.instance().executeScript(newCommand,getGraphicsPane());
        }
	}

    /**
     * This subclass permits to show the different labels of each color.
     * @author Benoit
     *
     */
	private class MyCellRenderer extends JLabel implements ListCellRenderer
	{
		/**
		 *   Implementation of ListCellRenderer interface
		 *   Returns a component that has been configured to display the specified value.
		 */
		@Override
		public Component getListCellRendererComponent(JList list,Object value, int index, boolean isSelected, boolean cellHasFocus)
	     {
			String s = (String)value;
	         setText(s);
	           if (isSelected) {
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
	         BufferedImage img = new BufferedImage(12,12,BufferedImage.TYPE_INT_RGB);
	         Graphics g = img.getGraphics();
	         g.setColor(TColor.translateColor(s));
	         g.fillRect(0, 0, 12, 12);
	         setIcon(new ImageIcon(img));
	         return this;
	     }
	 }


}
