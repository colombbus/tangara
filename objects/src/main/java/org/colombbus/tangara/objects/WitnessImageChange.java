package org.colombbus.tangara.objects;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;

@Localize(value="WitnessImageChange",localizeParent=true)
public class WitnessImageChange extends Witness {

	@Localize(value="WitnessImageChange.getImageName")
	public String getImageName()
	{
		String name = getContextStringValue("newImageName");
		if (name == null)
		{
			Program.instance().writeMessage(getMessage("error.noImage"));
			return "";
		}
		return name;
	}

	@Localize(value="WitnessImageChange.getPreviousImageName")
	public String getPreviousImageName()
	{
		String name = getContextStringValue("oldImageName");
		if (name == null)
		{
			Program.instance().writeMessage(getMessage("error.noPreviousImage"));
			return "";
		}
		return name;
	}
	

}
