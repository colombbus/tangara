package org.colombbus.helpgenerator.model;

import org.colombbus.helpgenerator.util.WikiPageHelper;

public class TocLevel3 {

	private String title;
	private String objectId;
	private boolean graphical;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isGraphical() {
		return graphical;
	}

	public void setGraphical(boolean graphical) {
		this.graphical = graphical;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getLocalUrl() {
		return WikiPageHelper.objectIdToFilename(objectId);
	}
}
