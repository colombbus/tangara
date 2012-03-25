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

import org.colombbus.build.Localize;

@Localize(value="Tools")
public class Tools {

	@Localize(value="Tools")
	public Tools()
	{
		super();
	}
	
	@Localize(value="Tools.stringToInteger")
	public int stringToInteger(String aText)
	{
		return Integer.parseInt(aText);
	}

	@Localize(value="Tools.integerToString")
	public String stringToInteger(int aNumber)
	{
		return Integer.toString(aNumber);
	}

	@Localize(value="Tools.compareClass")
	public boolean compareClass(Object object, bsh.ClassIdentifier aClass)
	{
		return (object.getClass()==aClass.getTargetClass());
	}
	
	@Localize(value="Tools.getObject")
	public Object getObject(String objectName)
	{
		return Program.instance().getObject(objectName);
	}

	@Localize(value="Tools.getObjectName")
	public String getObjectName(Object object)
	{
		return Program.instance().getObjectName(object);
	}
	
	@Localize(value="Tools.compareStrings")
	public boolean compareStrings(String string1, String string2) 
	{
		return (string1.compareTo(string2) == 0);
	}

}
