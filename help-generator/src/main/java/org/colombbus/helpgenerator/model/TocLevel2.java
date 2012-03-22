package org.colombbus.helpgenerator.model;

import java.util.ArrayList;
import java.util.List;

public class TocLevel2 {
	private String title;
	private List<TocLevel3> level3List = new ArrayList<TocLevel3>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<TocLevel3> getLevel3List() {
		return level3List;
	}

	public void addLevel3(TocLevel3 level3) {
		level3List.add(level3);
	}
}
