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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Timer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;


/**
 * This object permits to create an event that happen randomly.
 * The event can execute a list of commands.
 * The delay of the event is chosen randomly within the bounds (Min and Max) that are given.
 */
@Localize(value="Random",localizeParent=true)
public abstract class Random extends TObject
{

    private Vector<String> commands;

    private Timer timer;
    private Task task;
    private int delay;
    private int Min = 1000;
    private int Max = 10000;
    private boolean periodic = true;
    private boolean running = false;
    
    private boolean displayEvents = true;
    
    /** Creates a new instance of Button */
    @Localize(value="Random")
    public Random()
    {
        super();
        task = new Task();
        timer = new Timer(1000, task);
        timer.setRepeats(false);
        commands = new Vector<String>();
    }


    /**
     * Deletes the Random object.
     */
    @Override
	public void deleteObject()
    {
    	running = false;
    	timer.stop();
    	task = null;
        super.deleteObject();
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
     * Activates a random event
     * @param delay
     */
    @Localize(value="Random.start")
    public void start()
    {
    	if (!running)
    	{
	    	running = true;
	    	setupNextStep();
    	}
    }

    private void setupNextStep()
    {
    	setDelay();
    	timer.start();
    }
    
    /**
     * Activates a random event
     * @param delay
     */
    @Localize(value="Random.stop")
    public void stop()
    {
    	running = false;
        timer.stop();
    }

    /**
     * Serves to set the bounds within which the random even will occur.
	 * The bounds parameters must be given in milliseconds.
     * @param Min
     * @param Max
     */
    @Localize(value="Random.setBounds")
    public void setBounds(int Min, int Max)
    {
    	this.Min = Min;
    	this.Max = Max;
    }

    /**
     * Serves to make a random delay within the bounds.
     */
    public void setDelay()
    {
      	double dice = Math.random();
    	delay = (int)((Max-Min)*dice) + Min;
    	timer.setInitialDelay(delay);
    }

    /**
     * Activates a random event
     * @param delay
     */
    @Localize(value="Random.setPeriodic")
    public void setPeriodic(boolean value)
    {
    	periodic = value;
    }

   
    /**
     * Plays randomly heads or tails
     * @param delay
     */
    @Localize(value="Random.throwDice")
    public int throwDice(int max)
    {
    	int value = (int) Math.floor(Math.random()*max + 1);
    	return value;
    }

    
    public class Task implements ActionListener
    {
        public Task()
        {
            super();
        }

        @Override
		public void actionPerformed(ActionEvent e1)
        {
        	if (running)
        	{
	        	executeCommands();
	        	if (periodic)
	        		setupNextStep();
        	}
        }
    }

    /**
     * Runs all commands of the commands list.
     *
     */
    private void executeCommands()
    {
        for (String command:commands)
        {
            Program.instance().executeScript(command, getGraphicsPane(), displayEvents);
        }
    }

    /**
     * Freezes the random's movements or not
     * @param shallFreeze
     * 		true = freeze the movement
     */
    @Override
	public void freeze(boolean shallFreeze)
    {        
        if (shallFreeze)
        {
        	if (running)
        		timer.stop();
        }        	
        else
        {
        	if (running)
        		timer.restart();
        }            
    }
    
    @Localize(value="Random.displayCommands")
    public void displayCommands(boolean value)
    {
		displayEvents  = value;
    }

}
