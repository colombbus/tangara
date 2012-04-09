package org.colombbus.helpgenerator.model;

import java.util.ArrayList;
import java.util.List;

public class Toc {

	private List<TocLevel1> level1List = new ArrayList<TocLevel1>();

	public List<TocLevel1> getLevel1List() {
		return level1List;
	}

	public void addLevel1(TocLevel1 level1) {
		level1List.add(level1);
	}
}