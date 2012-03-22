package org.colombbus.helpgenerator.model;

public class Page {

	private String filename;
	private String title;
	private String content;

	public Page(String filename, String title, String content) {
		this.filename = filename;
		this.title = title;
		this.content = content;
	}

	public String getFilename() {
		return filename;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}
}
