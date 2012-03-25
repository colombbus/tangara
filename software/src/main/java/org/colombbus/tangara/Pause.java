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

import org.apache.log4j.Logger;


/** 
 * This class enables in Tangara software to wait during a certain period.
 * 
 */
public class Pause
{
	/** Class logger */    
    private static final Logger LOG = Logger.getLogger(Pause.class);
    
	/** Indicates if the pause is running or not */
	private boolean hold = false;
	
    /** Creates an instance of Pause */
	public Pause()
	{
	}
	
	/** Creates a PauseThread instance to wait during <code> value </code>
	 * @param duration
	 * 			Time in milliseconds
	 */
	public synchronized void setPause(long duration)
	{
		synchronized (this) {
			if (!hold)
			{
				hold = true;
    			PauseThread pt = new PauseThread(duration);
    			pt.start();
			}
		}
	}

	/**
	 * Ends the pause and notifies the other threads
	 *
	 */
	public synchronized void endPause()
	{
		synchronized (this) {
			if (hold)
			{
				hold = false;
				notifyAll();
			}
			
		}
	}
	
	/**
	 * Checks wether or not the thread is in pause
	 *
	 */
	public void checkPause()
	{
		synchronized (this) {
			if (hold)
			{
				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
					LOG.debug("Pause checking interrupted",e);
				}
			}
		}
	}

	/**
	 * This class throws threads to wait during a certain period
	 * @author gwen
	 *
	 */
	class PauseThread extends Thread
	{
		/** Value of wait time */
		private long milliseconds=0;
		
		/** Creates an instance of PauseThread where the waiting period is equal to <code> value </code>
		 * 
		 * @param value
		 * 			the waiting period
		 */
		public PauseThread(long value)
		{
			milliseconds = value;
		}
		/**
		 * Runs the thread which sleep during a certain period.
		 */
		@Override
		public void run()
		{
	    	try
	    	{
	    		Thread.sleep(milliseconds);
	    	}
	    	catch(InterruptedException e)
	    	{
	    		LOG.debug("Pause thread interrupted",e);
	    	}
	    	Pause.this.endPause();
		}
	}
}

