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
 * This class provides a classical Window popup displaying
 * a message with two programmable buttons Yes and No underneath.
 * @author Benoit
 *
 */
@Localize(value="Question")
public abstract class Question extends TObject
{
	String questionText;
	Vector<String> commandsYes;
	Vector<String> commandsNo;
	GraphicsPane graphicsPane;

	/**
	 * Creates a new instance of Question
	 */
	@Localize(value="Question")
	public Question()
	{
		questionText = null;
		commandsYes = new Vector<String>();
		commandsNo = new Vector<String>();
		graphicsPane = Program.instance().getCurrentGraphicsPane();
	}

	/**
	 * Creates a new instance of Question and sets its text
	 * @param questionText
	 */
	@Localize(value="Question")
	public Question(String questionText)
	{
		this();
		this.questionText = questionText;
	}

	/**
	 * Sets the text
	 * @param questionText
	 */
	@Localize(value="common.setText")
	public void setText(String questionText)
	{
		this.questionText = questionText;
	}


	/**
	 * Add a command "yes"
	 * @param command
	 */
	@Localize(value="Question.addCommandYes")
	public void addCommandYes(String command)
	{
        commandsYes.add(command);
	}

	/**
	 * Removes all commands "yes"
	 */
	@Localize(value="Question.removeCommandsYes")
    public void removeCommandsYes()
    {
    	commandsYes.clear();
    }

	/**
	 * Runs all commands "yes"
	 */
    private void executeCommandsYes()
    {    	
        for (String command:commandsYes)
        {
            Program.instance().executeScript(command);
        }        
    }

    /**
     * Adds a command "no"
     * @param command
     */
	@Localize(value="Question.addCommandNo")
	public void addCommandNo(String command)
	{
        commandsNo.add(command);
	}

	/**
	 * Removes all commands "no"
	 */
	@Localize(value="Question.removeCommandsNo")
    public void removeCommandsNo()
    {
    	commandsNo.clear();
    }

	/**
	 * Executes all commands "no"
	 */
    private void executeCommandsNo()
    {    	
        for (String command:commandsNo)
        {
            Program.instance().executeScript(command,graphicsPane);
        }        
    }

    /**
     * Creates the frame
     */
	@Localize(value="Question.ask")
    public void ask()
    {
    	if (questionText!=null)
    	{    		
			Object[] options = {getMessage("yes"), getMessage("no")};
			int answer = JOptionPane.showOptionDialog(graphicsPane,
						questionText,
						getMessage("title"), JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE,
					    null,     //do not use a custom Icon
					    options,  //the titles of buttons
					    options[0]);
	    	if (answer == JOptionPane.OK_OPTION)
    		{
	    		executeCommandsYes();
	    	}
	        else
	        {
	        	executeCommandsNo();
	        }
    	}
    	else
    	{
    		Program.instance().printError(getMessage("textError"));
    	}
    }

	/**
	 * Deletes the question
	 */
	@Localize(value="common.delete")
    public void supprimer()
    {
    	Program.instance().deleteObject(this);
    }

}
