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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JLabel;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class permits to have a text zone
 * @author Benoit
 *
 */
@SuppressWarnings("serial")
@Localize(value="Text",localizeParent=true)
public abstract class Text  extends TGraphicalObject
{

	private java.util.List<String> commands = new Vector<String>();
	private String text;
    private JLabel label;


    /** Creates a new instance of Texte */
    @Localize(value="Text")
    public Text()
    {
        setSize(50,20);
        setOpaque(false);
        setLayout(new BorderLayout());
        label = new JLabel();
        label.setSize(new Dimension(getObjectWidth(),getObjectHeight()));
        label.addMouseListener(new MouseAdapter(){
        	@Override
			public void mousePressed(MouseEvent e)
        	{
        		executeCommands();
        	}
        });
	   	add(label,BorderLayout.CENTER);
	   	displayObject();
    }

    @Localize(value="Text")
    public Text(String text)
    {
        this();
        setText(text);
    }

    /**
     * Sets the text to show.
     * @param text
     */
    @Localize(value="Text.setText")
    public void setText(String text)
    {
    	this.text = text;
    	computeSize();
		label.setText(Text.this.text);
    }
    
    @Localize(value="Text.setText2")
    public void setText(int text)
    {
    	this.text = String.valueOf(text);
    	computeSize();
		label.setText(Text.this.text);
    }
    
    @Localize(value="Text.setText3")
    public void setText(double text)
    {
    	this.text = String.valueOf(text);
    	computeSize();
		label.setText(Text.this.text);
    }

    private void computeSize()
    {
        FontMetrics fontMetrics = label.getFontMetrics(label.getFont());
        int length = fontMetrics.stringWidth(text);
        setSize(length+10,fontMetrics.getHeight());
    }
    
    /**
     * Gets the shown text.
     * @return the text
     */
    @Localize(value="Text.getText")
    public String getText()
    {
    	return label.getText();
    }

    /**
     * Sets the color of the text.
     * @param colorName
     */
    @Localize(value="common.setColor")
    public void setColor(String colorName)
    {
		Color c = TColor.translateColor(colorName, Color.black);
		label.setForeground(c);
   }

    /**
     * Adds the given command to the command list.
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
     * Executes all the commands of the command list.
     */
    private void executeCommands()
    {
        for (String command:commands)
        {
            Program.instance().executeScript(command,getGraphicsPane());
        }
    }

    /**
     * Sets the cursor of the text.
     * @param cursorName
     */
    @Localize(value="Text.setCursor")
    public void setCursor(String cursorName)
    {
    	if (cursorName.equals(getMessage("cursor.hand"))) //$NON-NLS-1$
    	{
    		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    	}
    	else
    	{
    		label.setCursor(Cursor.getDefaultCursor());
    	}
    }
    
    /**
     * Sets the size of the text.
     * @param sizeValue
     */
    @Localize(value="Text.setTextSize")
    public void setTextSize(int value) 
    {
        Font currentFont = label.getFont();
        label.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), value));
        if(text != null)
        	computeSize();
    }

}
