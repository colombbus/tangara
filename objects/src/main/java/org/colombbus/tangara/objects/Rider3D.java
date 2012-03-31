package org.colombbus.tangara.objects;

import java.util.HashMap;
import org.colombbus.build.Localize;


@Localize(value = "Rider3D", localizeParent = true)
public class Rider3D extends Walker3D {

	/**
	 * The Path3D which this Rider3D is on.
	 * It's null if this Rider3D is not on
	 * a Path3D.
	 */
	private Path3D path;
	
	/**
	 * Giving a path to an Rider3D is for giving him
	 * two actions, the first for when it's on the path,
	 * the second for when it's not. To differentiate those
	 * two states, this boolean is used.
	 */
	private boolean wasOnPath = false;
	
	@Localize(value = "Rider3D")
	public Rider3D() {
		super();
		registerEvent("onPath");
		registerEvent("outOfPath");
	}
	
	/**
	 * Sets the path of this Rider3D.
	 * 
	 * @param p - The path to be set to this Rider3D.
	 */
	@Localize(value = "Rider3D.setPath")
	public void setPath(Path3D p) {
		path = p;
	}

	private boolean isOnPath() {
		if (path == null) 
			return false;
		if (!lastEncounteredObjects.contains(path)) {
			return false;
		}
		return (path.isOnPath(getPosition()));
	}
	
	/**
	 * Executes the commands related to the path of this Rider3D.
	 */
	private void processPathCommands() {
		if (isOnPath()) {
			if (!wasOnPath) {
				HashMap <String,String> info = new HashMap<String,String>();							
	            info.put("x", Double.toString(translation.getX()));
	            info.put("y", Double.toString(translation.getY()));
	            info.put("z", Double.toString(translation.getZ()));
				processEvent("onPath",info);
				wasOnPath = true;
			}
		} else if (wasOnPath) {
			HashMap <String,String> info = new HashMap<String,String>();							
            info.put("x", Double.toString(translation.getX()));
            info.put("y", Double.toString(translation.getY()));
            info.put("z", Double.toString(translation.getZ()));
			processEvent("outOfPath",info);
			wasOnPath = false;
		}
	}

	/**
	 * Add a command to this Rider3D that is executed
	 * when it's entering in its path.
	 * 
	 * @param command - The command to execute when this Rider3D
	 * enters in its path.
	 */
	@Localize(value = "Rider3D.ifOnPath")
	public void ifOnPath(String command) {
    	addHandler("onPath",command); //$NON-NLS-1$
	}

	/**
	 * Add a command to this Rider3D that is executed
	 * when it's going out of its path.
	 * 
	 * @param command - The command to execute when this Rider3D
	 * goes out of its path.
	 */
	@Localize(value = "Rider3D.ifOutOfPath")
	public void ifOutOfPath(String command) {
    	addHandler("outOfPath",command); //$NON-NLS-1$
	}

	@Override
	protected void updatePosition() {
		super.updatePosition();
		processPathCommands();
	}
	
}