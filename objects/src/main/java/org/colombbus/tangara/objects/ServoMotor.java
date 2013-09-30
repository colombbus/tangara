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

//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;

//import javax.swing.Timer;

import org.colombbus.build.Localize;
//import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;
import org.colombbus.tangara.objects.SerialLink;


/*
 * @author marc
 */

@Localize(value = "ServoMotor", localizeParent = true)
public class ServoMotor extends TObject
{
	/*
	 * Unfortunately, the ServoMotor can only be used on the pin number 9
	 * Moreover, the angle is necessarly between 0 and 360
	 * This is because of the form of the Arduino source code,
	 * and might be change... someday
	 */
	
	private int pinNumber = 9;
	private SerialLink serial;
	
	@Localize(value = "ServoMotor")
	public ServoMotor() {
	}
	
	@Localize(value = "ServoMotor")
	public ServoMotor(SerialLink serial) {
		this.serial = serial;
		serial.modifyDigital(pinNumber, false);
	}
	
	/*
	@Localize(value = "ServoMotor.connect")
	public void connect(int thePin) {
		pinNumber = thePin;
	}
	*/
	
	@Localize(value = "ServoMotor.angle")
	public void angle(int angle) {
		if(angle < 0) {
			angle = 0;
		}
		else if(angle > 360) {
			angle = 360;
		}
		angle += 1000;
		String a = String.valueOf(angle);
		serial.send(a);
	}
	
}




