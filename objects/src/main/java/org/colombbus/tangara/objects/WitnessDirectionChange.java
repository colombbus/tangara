package org.colombbus.tangara.objects;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;

@Localize(value="WitnessDirectionChange",localizeParent=true)
public class WitnessDirectionChange extends Witness {

	@Localize(value="WitnessDirectionChange.getDirection")
	public String getDirection()
	{
		String name = getContextStringValue("newDirection");
		if (name == null)
		{
			Program.instance().writeMessage(getMessage("error.noDirection"));
			return "";
		}
		return name;
	}

	@Localize(value="WitnessDirectionChange.getPreviousDirection")
	public String getPreviousDirection()
	{
		String name = getContextStringValue("oldDirection");
		if (name == null)
		{
			Program.instance().writeMessage(getMessage("error.noPreviousDirection"));
			return "";
		}
		return name;
	}
	

}
