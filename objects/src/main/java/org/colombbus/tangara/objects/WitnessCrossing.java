package org.colombbus.tangara.objects;


import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;

@Localize(value="WitnessCrossing",localizeParent=true)
public class WitnessCrossing extends Witness {

	@Localize(value="WitnessCrossing.getEncounteredPath")
	public Path getEncounteredPath()
	{
		Object path = getContextValue("pathCrossed");
		if ((path == null)||!(path instanceof Path))
		{
			Program.instance().writeMessage(getMessage("error.noEncouteredPath"));
			return null;
		}
		return (Path)path;
	}

	@Localize(value="WitnessCrossing.getEncounteredPathName")
	public String getEncounteredPathName()
	{
		String name = getContextStringValue("pathCrossedName");
		if (name == null)
		{
			Program.instance().writeMessage(getMessage("error.noEncouteredPath"));
			return "";
		}
		return name;
	}
	

}
