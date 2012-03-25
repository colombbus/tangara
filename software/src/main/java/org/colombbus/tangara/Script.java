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

/**
 * This class brings together the command that has been made (in string form), 
 * the game area and the choice of viewing this command.
 * @author gwen
 *
 */
public class Script
{
	
	private String command;
	private GraphicsPane graphicsPane;
	private boolean displayResult;
	
	/**
	 * Creates a new script from the command in string form. The choice of viewing is true by default and it uses 
	 * the program graphics pane (Tangara game area).
	 * @param command
	 * 		the command in string form
	 */
	public Script(String command)
	{
		setCommand(command);
		setGraphicsPane(Program.instance().getDefaultGraphicsPane());
		displayResult = true;
	}

	/**
	 * Creates a new script from the command in string form and the choice of viewing. It uses the program 
	 * graphics pane (Tangara game area).
	 * @param command
	 * 		the command in string form
	 * @param display
	 * 		To choose if the command is displayed or not
	 */
	public Script(String command, boolean display)
	{
		this(command);
		displayResult = display;
	}
	
	/**
	 * Creates a new script from the command in string form and the graphics pane wanted.
	 * @param command
	 * 		the command in string form
	 * @param graphicsPane
	 * 		the graphics pane wanted
	 */
	public Script(String command, GraphicsPane graphicsPane)
	{
		setCommand(command);
		setGraphicsPane(graphicsPane);
		displayResult = true;
	}

	/**
	 * Creates a new script from the command in string form, the graphics pane wanted, and the choice of viewing.
	 * @param command
	 * 		the command in string form
	 * @param graphicsPane
	 * 		the graphics pane wanted
	 * @param display
	 * 		To choose if the command is displayed or not
	 */
	public Script(String command, GraphicsPane graphicsPane, boolean display)
	{
		this(command, graphicsPane);
		displayResult = display;
	}

	/**
	 * Sets the command in string form
	 * @param command
	 * 		the command in string form
	 */
	public void setCommand(String command)
	{
		this.command = command;
	}

	/**
	 * Sets the graphics pane
	 * @param graphicsPane
	 * 		the graphics pane wanted
	 */
	public void setGraphicsPane(GraphicsPane graphicsPane)
	{
		this.graphicsPane = graphicsPane;
	}

	/**
	 * Gets the command in string form
	 * @return
	 * 		the command in string form
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * Gets the graphics pane used
	 * @return
	 * 		the graphics pane used
	 */
	public GraphicsPane getGraphicsPane()
	{
		return graphicsPane;
	}
	
	/**
	 * Gets the boolean that determines if the command is displayed or not
	 * @return
	 * 		Determines if the command if displayed or not
	 */
	public boolean display()
	{
		return displayResult;
	}
}
