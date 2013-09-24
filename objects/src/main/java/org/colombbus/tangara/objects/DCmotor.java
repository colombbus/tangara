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
	private boolean black;
	private boolean red;
	private SerialLink serial;
	
	@Localize(value = "DCmotor")
	public DCmotor() {
		Program.instance().writeMessage("usage:\nmyMotor = new DCmotor(mySerialLink)");
	}
	
	@Localize(value = "DCmotor")
	public DCmotor(SerialLink serial) {
		this.serial = serial;
	}
	
	@Localize(value = "DCmotor.connectBlackWire")
	public void connectBlackWire(int thePin) {
		blackPinNumber = thePin;
		black = false;
	}
	
	@Localize(value = "DCmotor.connectRedWire")
	public void connectRedWire(int thePin) {
		redPinNumber = thePin;
		red = false;
	}
	
	@Localize(value = "DCmotor.clockwiseRotation")
	public void clockwiseRotation(int timer) {
		if(black == false) {
			try {
				serial.send(String.valueOf(blackPinNumber));
				black = true;
			    Thread.sleep(timer);
			    serial.send(String.valueOf(blackPinNumber));
				black = false;
			} 
			catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
	}
	
	@Localize(value = "DCmotor.anticlockwiseRotation")
	public void anticlockwiseRotation(int timer) throws InterruptedException {
		if(red == false) {
			try {
				serial.send(String.valueOf(redPinNumber));
				black = true;
			    Thread.sleep(timer);
			    serial.send(String.valueOf(redPinNumber));
				black = false;
			} 
			catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
	}
	
}


