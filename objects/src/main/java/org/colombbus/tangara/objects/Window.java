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

import org.colombbus.build.Localize;
import org.colombbus.tangara.TWindow;

@SuppressWarnings("serial")
@Localize(value="Window")
public abstract class Window extends TWindow {

    /**
     * Creates an instance of this class.
     */
    @Localize(value="Window")
	public Window()
	{
	}

    /**
     * Creates a new window and loads the file in
     * @param fileName
     */
    @Localize(value="Window")
	public Window(String fileName)
    {
    	super(fileName);

    }


   /**
    * Deletes the window
    */
    @Override
	@Localize(value="common.delete")
    public void delete()
    {
    	super.delete();
    }

    /**
     * Adjusts the size of the window
     */
    @Override
	@Localize(value="Window.adjustSize")
	public void adjustSize()
	{
    	super.adjustSize();
	}

    /**
     * Shows the window
     */
    @Override
	@Localize(value="Window.show")
    public void newShow()
	{
    	super.newShow();
	}

    /**
     * Runs a command in this window
     * @param command
     */
    @Override
	@Localize(value="Window.executeCommand")
	public void executeCommand(String command)
	{
		super.executeCommand(command);
	}

    /**
     * Loads a file in this window
     * @param fileName
     */
    @Override
	@Localize(value="Window.loadFile")
    public void loadFile(String fileName)
	{
    	super.loadFile(fileName);
	}


}
