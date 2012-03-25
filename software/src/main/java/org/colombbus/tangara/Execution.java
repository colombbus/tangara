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

import java.awt.Desktop;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.bsf.BSFEngine;
import org.apache.log4j.Logger;

/**
 * This class creates the thread that will run the execution of the scripts.
 * Furthermore, if the thread which is running is not ExecutionThread's one,
 * we can store the scripts in a stack, to execute them later.
 * @author gwen
 *
 */
public class Execution {

	private ScriptExecResult scriptResult = new ScriptExecResult();
	private GraphicsPane currentGraphicsPane;
	private BSFEngine shell;
	private StringParser parser = new StringParser();
	private Program program;

	public static ErrorResult lastError;

    private static final String  DFLT_SOURCE_NAME = Messages.getString("Program.defaultSource");

	/** Class logger */
    private static Logger LOG = Logger.getLogger(Execution.class);

	public Execution(Program program)
	{
		this.program = program;
	}


	/**
	 * Sets the current graphics pane
	 * @param graphicsPane
	 * 		the graphics pane wanted
	 */
	public void setCurrentGraphicsPane(GraphicsPane graphicsPane)
	{
		currentGraphicsPane = graphicsPane;
		currentGraphicsPane.declareBeanForTheScreen();
	}

	/**
	 * Gets the current graphics pane
	 * @return
	 * 		the graphics pane used
	 */
	public GraphicsPane getCurrentGraphicsPane()
	{
		return currentGraphicsPane;
	}

	/**
	 * Sets the shell
	 * @param shell
	 * 		the bsfEngine shell wanted
	 */
    public void setBSFEngine(BSFEngine shell) {
        this.shell = shell;
    }


	/**
	 * Executes the command in the graphics pane passed as parameters
	 * @param command
	 * 		the command in string form
	 * @param graphicsPane
	 * 		the graphics pane used
	 */
	public void execute(String command, GraphicsPane graphicsPane)
	{
		execute(command, graphicsPane,true);
	}

	/**
	 * Executes the command in the graphics pane passed as parameters.
	 * 	If the thread  hand, it executes the command immediately, otherwise its puts in the stack
	 *  of orders to be executed
	 * @param command
	 * 		the command in string form
	 * @param graphicsPane
	 * 		the graphics pane used
	 * @param isDisplayed
	 * 		To choose whether or not the command is displayed
	 */
	public void execute(String command, GraphicsPane graphicsPane, boolean isDisplayed) {
		if (SwingUtilities.isEventDispatchThread())
		processScript(new Script(command, graphicsPane, isDisplayed));
		else
		{
			final String aCommand = command;
			final GraphicsPane aGP = graphicsPane;
			final boolean aB = isDisplayed;
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						processScript(new Script(aCommand,aGP, aB));
					}
				});
			} catch (Exception e) {
				LOG.warn("Error processScript", e);
			}
		}
	}

	/**
	 * Throws the other "executeAndGetResult" with the boolean that determines the display at true
	 * @param command
	 * 		the command in string form
	 * @param graphicsPane
	 * 		the graphics pane used
	 * @return
	 * 	 a ScriptExecResult to see the output or the error
	 */
	public ScriptExecResult executeAndGetResult(String command, GraphicsPane graphicsPane)
	{
		return executeAndGetResult(command, graphicsPane, true);
	}


	/**
	 * If the thread takes hand, it executes the command, otherwise it creates a new script from the command and
	 * adds it to the scripts stack.
	 * @param command
	 * 		the command in string form
	 * @param graphicsPane
	 * 		the graphics pane used
	 * @param isDisplayed
	 * 		to choose if the command is displayed or not
	 * @return
	 * 		a ScriptExecResult to see the output or the error
	 */
	public ScriptExecResult executeAndGetResult(String command, GraphicsPane graphicsPane, boolean isDisplayed)
	{
		if (SwingUtilities.isEventDispatchThread())
			processScript(new Script(command, graphicsPane, isDisplayed));
		else
		{
			final String aCommand = command;
			final GraphicsPane aGP = graphicsPane;
			final boolean aB = isDisplayed;
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run(){
						processScript(new Script(aCommand,aGP, aB));
					}
				});
			} catch (InterruptedException e) {
				Program.instance().writeMessage("Error 1 in executeAndGetResult");
			} catch (InvocationTargetException e) {
				Program.instance().writeMessage("Error 2 in executeAndGetResult");
			}
		}

		return new ScriptExecResult(scriptResult.getOuput(),scriptResult.getError());
	}

	/**
	 * Parse the script and throws its assessment by the shell (and execution if it's good).
	 * @param script
	 * 		the script to execute
	 */
	private void processScript(Script script)
	{
    	scriptResult.reset();
    	String scriptCommand = script.getCommand();
    	boolean display = script.display();

    	if (scriptCommand.length() != 0) {
		    // Splits the command into blocks
		    ArrayList<String> commands = parser.splitBlocks(scriptCommand);
		    for(String command:commands) {
		    	program.checkPause();
		    	int startIndex = 0;
		    	if (display) {
			        startIndex = program.getCurrentLogIndex();
			        program.printCode(command);
		    	}
		    	
		       	final String parsedScript = parser.parseQuotes(command);

		       	// Saves the previous GraphicsPane object (in case of recursive call to processScript)
	    		GraphicsPane savedGP = getCurrentGraphicsPane();
	    		
	    		setCurrentGraphicsPane(script.getGraphicsPane());

		       	//We execute the command.
		       	if (SwingUtilities.isEventDispatchThread())
	        	{
        			try {
	        			shell.eval(DFLT_SOURCE_NAME, 1, 1, parsedScript);
   					} catch (Throwable t) {
   		   				Execution.lastError = ErrorHelper.process(t);
   					}
		       	} else
		       		LOG.warn("Error - processScript() called from outside of the EDT");

		       	// Restore the previous GraphicsPane
		       	setCurrentGraphicsPane(savedGP);

		       	if (lastError != null) {
		            //The code contains some compilation errors.
	            	ErrorResult errorCopy = new ErrorResult(lastError);
		            lastError = null;
		            scriptResult.setError(errorCopy.getText());
		            if (display)
		            {
			            printError(command, errorCopy, startIndex);
		            	// adds the command to the command history
			            program.addCommandToHistory(command, false);
		            	break;
		            }
		        }
		       	else if (display)
		       	{
		       		// Adds the command to the command history
		       	    program.addCommandToHistory(command, true);
		       	}
		   	}
		   	program.flushStreams();
        }
	}

	/**
	 * Prints the error in the console or in a popup.
	 * @param command
	 *                   The executed command that caused the error.
	 * @param error
	 *                   The execution text and line of the error
	 */
	private void printError(String commandText, ErrorResult error, int startIndex) {
    	String errorMessage = MessageFormat.format(Messages.getString("Program.error"),error.getText());
    	if (Main.isProgramMode()) {
    		// display error in a popup
    		if (error.hasLink()) {
    			Object[] possibleValues = { "OK", error.getLinkText()};
    			int selectedValue = JOptionPane.showOptionDialog(Program.instance().getFrame(),
    					errorMessage, Messages.getString("error.title"),
    			        JOptionPane.NO_OPTION,JOptionPane.ERROR_MESSAGE, null,
    			        possibleValues, possibleValues[0]);
    			if (selectedValue == 1) {
    				try {
    					Desktop.getDesktop().browse((new URL(error.getLink())).toURI());
    				} catch (Exception e) {
    	    			JOptionPane.showMessageDialog(Program.instance().getFrame(), e.getMessage(), Messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
    				}
    			}
    		} else {
    			JOptionPane.showMessageDialog(Program.instance().getFrame(), errorMessage, Messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
    		}
		} else {
			if (error.hasLine()) {
				// error line could be detected
		    	program.setErrorInConsole(startIndex+error.getLine()-1, 1, startIndex+error.getLine()-1);
	
			} else {
				// Count line number
				StringTokenizer lineCounter = new StringTokenizer(commandText, "\n");
				program.setErrorInConsole(startIndex, lineCounter.countTokens(), startIndex);
	
			}
	    	program.printError(errorMessage, startIndex);
		}
	}



	public void updateOutput(String message) {
		if (scriptResult.getOuput() == null)
			scriptResult.setOuput(message);
		else
			scriptResult.setOuput(scriptResult.getOuput()+"\n"+message);
	}
	
}


