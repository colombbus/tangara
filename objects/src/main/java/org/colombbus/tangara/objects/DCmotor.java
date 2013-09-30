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

import javax.swing.Timer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;
import org.colombbus.tangara.objects.SerialLink;


/*
 * @author marc
 */

@Localize(value = "DCmotor", localizeParent = true)
public class DCmotor extends TObject
{
	private int blackPinNumber;
	private int redPinNumber;
	/*private boolean black;
	private boolean red;*/
	private SerialLink serial;
	private StopTask task = new StopTask();
	private Timer timer;
	
	@Localize(value = "DCmotor")
	public DCmotor() {
		Program.instance().writeMessage("usage:\nmyMotor = new DCmotor(mySerialLink)");
	}
	
	@Localize(value = "DCmotor")
	public DCmotor(SerialLink serial) {
		timer = new Timer(0,task);
		timer.setRepeats(false);
		this.serial = serial;
	}

	@Localize(value = "DCmotor.connectBlackWire")
	public void connectBlackWire(int thePin) {
		blackPinNumber = thePin;
		//black = false;
	}
	
	@Localize(value = "DCmotor.connectRedWire")
	public void connectRedWire(int thePin) {
		redPinNumber = thePin;
		//red = false;
	}
	
	@Localize(value = "DCmotor.clockwiseRotation")
	public void clockwiseRotation(int duration) {
		if (timer.isRunning()){
			timer.stop();
		}
		serial.send(String.valueOf(blackPinNumber));
		timer.setDelay(duration);
		task.setBlack(true);
		timer.start();
	}
	
	@Localize(value = "DCmotor.anticlockwiseRotation")
	public void anticlockwiseRotation(int duration){
		if (timer.isRunning()){
			timer.stop();
		}
		serial.send(String.valueOf(redPinNumber));
		timer.setDelay(duration);
		task.setBlack(false);
		timer.start();
	}
	
	@Localize(value = "DCmotor.clockwiseRun")
	public void clockwiseRun() {
		serial.modifyDigital(blackPinNumber,true);
	}
	
	@Localize(value = "DCmotor.anticlockwiseRun")
	public void anticlockwiseRun() {
		serial.modifyDigital(redPinNumber,true);
	}
	
	@Localize(value = "DCmotor.stopMotor")
	public void stopMotor() {
		serial.modifyDigital(blackPinNumber,false);
		serial.modifyDigital(redPinNumber,false);
	}
	
	private class StopTask implements ActionListener
	{
		boolean black;
		
		public StopTask() {
			super();
		}
		
		public void setBlack(boolean value) {
			this.black = value;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if (black)
				serial.send(String.valueOf(blackPinNumber));
			else
				serial.send(String.valueOf(redPinNumber));
		}
	}
	
}


