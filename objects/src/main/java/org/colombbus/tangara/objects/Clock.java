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
import java.util.ArrayList;

import javax.swing.Timer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;

@Localize(value="Clock",localizeParent=true)
public abstract class Clock extends TObject{
	
	private Task task = new Task();
	private java.util.List<String> list_commands = new ArrayList<String>();	
	private boolean display = false;
	private boolean playing = false;
	private boolean initialDelaySet = false;
	private Timer timer;
	
		
	@Localize(value="Clock")
	public Clock()
	{
		timer = new Timer(0, task);
	}

	@Localize(value="Clock.addCommand")
	public void addCommand(String command)
	{
		list_commands.add(command);		
	}
	
	@Localize(value="Clock.removeCommands")
	public void removeCommands()
	{
		list_commands.clear();
	}
	
	@Localize(value="Clock.start")
	public void start()
	{
		if (!timer.isRunning())
		{
			timer.start();
			playing = true;
		}
	}

	@Localize(value="Clock.setInitialDelay")
	public void setInitialDelay(int aDelay)
	{
		timer.setInitialDelay(aDelay);
		initialDelaySet = true;
	}

	
	@Localize(value="Clock.setDelay")
	public void setDelay(int aDelay)
	{
		timer.setDelay(aDelay);
		if (!initialDelaySet)
			timer.setInitialDelay(aDelay);
	}
		
	@Localize(value="Clock.stop")
	public void stop()
	{
		timer.stop();
		playing = false;
	}
	
	@Localize(value="Clock.displayCommands")
	public void displayCommands(boolean value)
	{
		display = value;
	}
	
	@Override
	public void deleteObject()
	{
		timer.stop();
		task = null;
		super.deleteObject();
	}
	
	@Override
	public void freeze(boolean value)
	{
		if (value)
		{
			if (playing)
				timer.stop();
		}
		else
		{
			if (playing)
				timer.start();
		}
	}
	
    @Localize(value="Clock.setPeriodic")
    public void setPeriodic(boolean value)
    {
   		timer.setRepeats(value);
    }
	
	private class Task implements ActionListener
	{
		public Task()
		{
			super();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{					
			for (String command : list_commands)
				Program.instance().executeScript(command, display);
		}
	}
}
