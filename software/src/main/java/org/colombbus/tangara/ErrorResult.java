package org.colombbus.tangara;


public class ErrorResult {

	String errorText;
	String link;
	String linkText;
	int errorLine;
	
	public ErrorResult() {
		errorText = new String();
		errorLine = -1;
		link = null;
		linkText = null;
	}
	
	public ErrorResult(String text) {
		this();
		setText(text);
	}

	public ErrorResult(String text, int line) {
		this(text);
		setLine(line);
	}

	public ErrorResult(ErrorResult copy) {
		this(copy.getText(),copy.getLine());
		if (copy.hasLink())
			setLink(copy.getLink(),copy.getLinkText());
	}

	
	public void setText(String text) {
		errorText = new String(text);
	}

	public void setLine(int line) {
		errorLine = line;
	}
	
	public void setLink(String aLink, String text) {
		link = new String(aLink);
		linkText = new String(text);
	}
	
	public String getText() {
		return errorText;
	}

	public int getLine() {
		return errorLine;
	}
	
	public String getLink() {
		return link;
	}

	public String getLinkText() {
		return linkText;
	}

	public boolean hasLine() {
		return (errorLine>-1);
	}

	public boolean hasLink() {
		return (link != null);
	}

}
