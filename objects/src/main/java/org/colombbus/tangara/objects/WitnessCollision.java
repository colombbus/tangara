package org.colombbus.tangara.objects;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;

@Localize(value="WitnessCollision",localizeParent=true)
public class WitnessCollision extends Witness {

	@Localize(value="WitnessCollision.getEncounteredObject")
	public TGraphicalObject getEncounteredObject()
	{
		Object encountered = getContextValue("lastCollision");
		if ((encountered == null)||!(encountered instanceof TGraphicalObject))
		{
			Program.instance().writeMessage(getMessage("error.noEncounteredObject"));
			return null;
		}
		return (TGraphicalObject)encountered;
	}

	@Localize(value="WitnessCollision.getEncounteredObjectName")
	public String getEncounteredObjectName()
	{
		String name = getContextStringValue("lastCollisionName");
		if (name == null)
		{
			Program.instance().writeMessage(getMessage("error.noEncounteredObject"));
			return "";
		}
		return name;
	}
	

}
