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

@Localize(value = "Led", localizeParent = true)
public class Led extends TObject
{
	private int pinNumber;
	private boolean isOn;
	private SerialLink serial;
	
	@Localize(value = "Led")
	public Led() {
		Program.instance().writeMessage("usage:\nmyLed = new Led(mySerialLink)");
	}
	
	@Localize(value = "Led")
	public Led(SerialLink serial) {
		this.serial = serial;
		isOn = false;
	}
	
	@Localize(value = "Led.connect")
	public void connect(int thePin) {
		pinNumber = thePin;
	}
	
	@Localize(value = "Led.turnOn")
	public void turnOn() {
		if(isOn == false) {
			serial.send(String.valueOf(pinNumber));
			isOn = true;
		}
	}
	
	@Localize(value = "Led.turnOff")
	public void turnOff() {
		if(isOn == true) {
			serial.send(String.valueOf(pinNumber));
			isOn = false;
		}
	}
	
}


