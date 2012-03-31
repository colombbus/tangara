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

import java.util.Vector;

import javax.swing.JOptionPane;

import org.colombbus.build.Localize;
import org.colombbus.tangara.GraphicsPane;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;

/**
 * This class provides an object able to display a window with a Message written.
 * After a click on this windows' button, a command can be executed.
 * @author Benoit
 *
 */
@Localize(value="Information")
public abstract class Information extends TObject
{
	private String text;
	private java.util.List<String> commands= new Vector<String>();
	private GraphicsPane graphicsPane;

	/**
	 * Creates a new instance of Information
	 */
	@Localize(value="Information")
	public Information()
	{
		graphicsPane = Program.instance().getCurrentGraphicsPane();
	}

	/**
	 * Creates a new instance of Information and initializes its text
	 * @param text
	 */
	@Localize(value="Information")
	public Information(String text)
	{
		this();
		this.text = text;
	}

	/**
	 * Sets the text of the window to display.
	 * @param text
	 */
	@Localize(value="common.setText")
	public void setText(String text)
	{
		this.text = text;
	}

    /**
     * Adds a command to the command list.
     * @param command
     */
    @Localize(value="common.addCommand")
	public void addCommand(String command)
	{
        commands.add(command);
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
     * Executes the commands of the command list.
     */
    private void executeCommands()
    {
        for (String command:commands)
        {
            Program.instance().executeScript(command,graphicsPane);
        }
    }

    /**
     * Displays the popup.
     */
    @Localize(value="Information.show")
    public void show()
    {
    	if (text!=null)
    	{
    		JOptionPane.showMessageDialog(graphicsPane,text, getMessage("title"), JOptionPane.INFORMATION_MESSAGE);
			executeCommands();
    	}
    	else
    	{
    		Program.instance().printError(getMessage("textError"));
    	}
    }

    /**
     * Deletes the object.
     */
    @Override
	@Localize(value="common.delete")
    public void delete()
    {
    	Program.instance().deleteObject(this);
    }

}
