package org.colombbus.helpgenerator.model;

import java.util.ArrayList;
import java.util.List;

public class TocLevel1 {
	private String title;
	private String header;
	private String footer;
	private List<TocLevel2> level2List = new ArrayList<TocLevel2>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public List<TocLevel2> getLevel2List() {
		return level2List;
	}

	public void addLevel2(TocLevel2 level2) {
		level2List.add(level2);
	}

}
