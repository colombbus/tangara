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
 * This class enables to save each command. It is composed of a string that represents the command and a boolean 
 * that determines whether or not the command was valid
 * @author gwen
 *
 */
public class HistoryRecord
{
	private String command;
	private boolean result = false;
	
	/**
	 * Creates a new instance of HistoryRecord without initializing
	 *
	 */
	public HistoryRecord()
	{
	}
	
	/**
	 * Creates a new instance of HistoryRecord according the name of the command and its validity
	 * @param command
	 * 			a string that represents the command
	 * @param result
	 * 			a boolean that represents the validity of the command
	 */
	public HistoryRecord(String command, boolean result)
	{
		this.command = command;
		this.result = result;		
	}
	
	
	
	/**
	 * Gets the command string 
	 * @return
	 * 		the command string
	 */
	public String getCommand()
	{
		return command;
	}
	
	/**
	 * Gets the command validity
	 * @return
	 * 		a boolean that represents the validity of the command
	 */	
	public boolean getResult()
	{
		return result;
	}		
}
