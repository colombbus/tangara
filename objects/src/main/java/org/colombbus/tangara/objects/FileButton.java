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
import java.awt.FontMetrics;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class provides an Button able to save a file after clicking on it.
 * @author benoit
 */
@SuppressWarnings("serial")
@Localize(value="FileButton",localizeParent=true)
public abstract class FileButton extends TGraphicalObject
{
	private static final boolean MODE_LOAD = true;
    private static final boolean MODE_SAVE = false;

	private JFileChooser fileChooser;
	private boolean mode = MODE_LOAD;
	private java.util.List<String> commands= new Vector<String>();
	private String text;

    private JButton button;

    /**
     * Creates a new instance of FileButton
     */
    @Localize(value="FileButton")
    public FileButton() {
    	super();
    	setSize(50,30);
    	setOpaque(false);
    	setLayout(new BorderLayout());
    	createButton();
        createFileChooser();
    	displayObject();
    }

	private void createFileChooser() {
		fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(Program.instance().getCurrentDirectory());
	}

	private void createButton() {
		button = new JButton();
        button.setSize(getObjectWidth(),getObjectHeight());
        String lFName = UIManager.getLookAndFeel().getName();
        if (!(lFName.toLowerCase().contains("windows")))
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
	                	executeCommande();
	            	}
	            });
	        }
        });
    	add(button,BorderLayout.CENTER);
	}

    /**
     * Creates a new instance of FileButton and initializes its text
     * @param text
     */
    @Localize(value="FileButton")
    public FileButton(String text)
    {
        this();
        setText(text);
    }

    /**
     * Executes the command, either saving or loading.
     */
    private void executeCommande()
    {
    	int returnVal;
    	if (mode==MODE_LOAD)
    	{
    		returnVal = fileChooser.showOpenDialog(getGraphicsPane());
    	}
    	else
    	{
    		returnVal = fileChooser.showSaveDialog(getGraphicsPane());
    	}
        if (returnVal==JFileChooser.APPROVE_OPTION)
        {

        	String fileName = fileChooser.getSelectedFile().getAbsolutePath();
            for (String command:commands)
            {
            	String newCommand = command;
        		newCommand = command.replace("%",formatString(fileName));
                Program.instance().executeScript(newCommand,getGraphicsPane());
            }
        }
    }

    /**
     * Sets the right number of slashes in a text.
     * @param text
     * @return
     */
    private String formatString(String text)
    {
    	return text.replace("\\", "\\\\");
    }

    /**
     * Sets the text.
     * @param text
     */
    @Localize(value="common.setText")
    public void setText(String text)
    {
    	this.text = text;
    	FontMetrics fontMetrics = button.getFontMetrics(getFont());
        int length = fontMetrics.stringWidth(text);
		setObjectWidth(length+40);
		button.setText(FileButton.this.text);
    }

    /**
     * Adds a command to the command list.
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
     * Sets the Saving status.
     */
    @Localize(value="FileButton.save")
	public void save()
    {
    	mode = MODE_SAVE;
    }

    /**
     * Sets the loading status.
     */
    @Localize(value="FileButton.load")
	public void load()
    {
    	mode = MODE_LOAD;
    }

}
