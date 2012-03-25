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
 * This class enables to set the output and the error to see the result of your scripts
 * @author gwen
 *
 */
public class ScriptExecResult {
	
	/**
	 * Creates an empty ScriptExecResult instance
	 *
	 */
	public ScriptExecResult() {
		super();
	}

	/**
	 * Creates a new instance of ScriptExecResult from the selected output and error
	 * @param output
	 * 		the output stream
	 * @param error
	 * 		the error stream
	 */
	public ScriptExecResult(String output, String error) {
		super();
		this.output = output;
		this.error = error;
	}
	
	/**
	 * Gets the output
	 * @return
	 * 		the output 
	 */
	public String getOuput() {
		return output;
	}
	
	/**
	 * Sets the output
	 * @param output
	 * 		the output wanted
	 */
	public void setOuput( String output) {
		this.output = output;
	}
	
	/**
	 * Gets the error
	 * @return
	 * 		the output
	 */
	public String getError() {
		return error;
	}
	
	/**
	 * Sets the error
	 * @param error
	 * 		the error wanted
	 */
	public void setError( String error ) {
		this.error = error;
	}

	/**
	 * Resets output and error strings
	 *
	 */
	public void reset()
	{
		this.output = null;
		this.error = null;
	}
	
	
	private String output;
		private String error;
}
