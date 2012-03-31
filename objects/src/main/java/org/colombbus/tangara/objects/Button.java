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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class creates a labeled button.
 * An action can happen when the button is clicked on.
 * @author benoit
 */
@SuppressWarnings("serial")
@Localize(value="Button",localizeParent=true)
public abstract class Button extends TGraphicalObject {

	private static final String[] DEFAULT_EXTENSIONS = {"png","bmp","gif","jpg","jpeg"}; //$NON-NLS-1$

	private static final int PADDING_HORIZONTAL = 40;
	private static final int PADDING_VERTICAL = 10;
	
	private JButton button;
	private java.util.List<String> commands = new Vector<String>();
	   
    private boolean isColored=false;
    private boolean isWidthFixed=false;
    private Color color;
    private ComponentAdapter colorFill;
    
    
    /** Creates a new instance of Button */
	@Localize(value="Button")
    public Button() {
        setSize(50,30);
        setOpaque(false);
        setLayout(new BorderLayout());
        createButton();
        colorFill = new ComponentAdapter(){
        	@Override
			public void  componentResized(ComponentEvent e)
        	{
        		setFillColor();
        	}
        };
        displayObject();
    }

	private void createButton() {
		button = new JButton();
        String lFName = UIManager.getLookAndFeel().getName();
        if ((lFName.toLowerCase().contains("mac")))
        {
        	button.setContentAreaFilled(false);
        }        
        button.addActionListener(new java.awt.event.ActionListener() {
	        @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
	            SwingUtilities.invokeLater(new Runnable(){
	            	@Override
					public void run()
	            	{
	                	executeCommands();
	            	}
	            });
	        }
        });
        add(button,BorderLayout.CENTER);
	}

    @Override
	@Localize(value="common.setObjectWidth")
	public void setObjectWidth(int width)
	{
    	isWidthFixed = true;
		super.setObjectWidth(width);
	}


	/**
	 * Creates a new instance of Button and sets the button's text
	 * @param text
	 * 		the string used to set the text
	 */
	@Localize(value="Button")
    public Button(String text)
    {
        this();
        setText(text);
    }

	/**
	 * Sets the button's text
	 * @param text
	 * 		the string used to set the text
	 */
	@Localize(value="common.setText")
    public void setText(String text)
    {
		// if width has not been fixed through direct calls to setWidth, 
		// we compute the width from the size of the text
		if (!isWidthFixed)
		{
	    	FontMetrics fontMetrics = button.getFontMetrics(button.getFont());
	        int length = fontMetrics.stringWidth(text);
	        int height = fontMetrics.getHeight();
	        super.setObjectWidth(length+PADDING_HORIZONTAL);
	        super.setObjectHeight(height+PADDING_VERTICAL);
		}
        if (isColored)
        {
	    	button.removeComponentListener(colorFill);
	    	button.setIcon(null);
	    	isColored = false;
        }
		button.setText(text);
    }	

	/**
	 * Adds a command to the button's commands list.
	 * @param cmd
	 * 		the command to add.
	 */
	@Localize(value="common.addCommand")
	public void addCommand(String cmd)
    {
		commands.add(cmd);
    }

	/**
	 * Removes all commands associated with the button
	 *
	 */
	@Localize(value="common.removeCommands")
	public void removeCommands()
    {
    	commands.clear();
    }

	/**
	 * Enables the button
	 *
	 */
    @Localize(value="common.activate")
	public void activate()
    {
    	button.setEnabled(true);
    }

    /**
     * Disables the button.
     *
     */
    @Localize(value="common.deactivate")
    public void deactivate()
    {
    	button.setEnabled(false);
    }
    
    private void setFillColor()
    {
		BufferedImage i = new BufferedImage(getObjectWidth(),getObjectHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics g = i.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, i.getWidth(), i.getHeight());
		button.setIcon(new ImageIcon(i));
    }

    /**
     * Enables to fill the button with the color passed as parameters
     * @param colorName
     * 		the color name (in the language selected for Tangara)
     */
    @Localize(value="Button.fillColor")
    public void fillColor(String colorName)
    {
    	this.color = TColor.translateColor(colorName, Color.black);
    	setFillColor();
    	if (!isColored)
    	{
        	button.setText("");
	    	// add the component listener
	    	button.addComponentListener(colorFill);
	    	isColored = true;
    	}
    }
    
    /**
     * Runs all commands associated with the button.
     *
     */
    private void executeCommands()
    {
        for (String command:commands)
        {
            Program.instance().executeScript(command,getGraphicsPane());
        }
    }

    /**
     * Sets the button's default icon.
     * @param fileName
     * 		the file name of the icon used as the default image.
     */
    @Localize(value="Button.setPicture")
    public void setPicture(String fileName)
    {
    	try
    	{
    		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
    		if (file == null) {
    			throw new Exception("file not found");
    		}
    		if (isColored)
    		{
    			button.removeComponentListener(colorFill);
    			isColored = false;
    		}
    		button.setIcon(new ImageIcon(file.getAbsolutePath()));
    	}
    	catch (Exception e)
    	{
            String message = MessageFormat.format(getMessage("icon.error")+" ("+e.getMessage()+")", fileName);
            Program.instance().writeMessage(message);
    	}
    }

    /**
     * Hides the component     
     */
    @Override
	@Localize(value="Button.hide")
    public void hide()
    {
        button.setVisible(false);
        repaint();
    }
    
    /**
     * Shows the component
     */
    @Localize(value="Button.display")
    public void display()
    {                   
        button.setVisible(true);
        repaint();
    }
    
    /**
     * Sets the size of the text.
     * @param sizeValue
     */
    @Localize(value="Button.setTextSize")
    public void setTextSize(int value) {
        Font currentFont = button.getFont();
        button.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), value));
        String currentText = button.getText();
        if (currentText!=null)
        	setText(currentText);
    }
    
}
