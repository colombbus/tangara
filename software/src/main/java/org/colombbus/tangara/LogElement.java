package org.colombbus.tangara;

import javax.swing.text.AttributeSet;

public class LogElement {

	private int type;
	private String text;
	private int lineNumber;
	private int programIndex;
	
	LogElement(String text, int type)
	{
		this(text, type, -1, -1);
	}

	LogElement(String text, int type, int lineNumber, int programIndex)
	{
		this.type = type;
		this.text = text;
		this.lineNumber = lineNumber;
		this.programIndex = programIndex;
	}

	
	public String getText() {
		return text;
	}

	public int getType() {
		return type;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	public int getProgramIndex() {
		return programIndex;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setType(int type) {
		this.type= type ;
	}

	public void setLineNumber(int number) {
		this.lineNumber= number;
	}

	public void setProgramIndex(int index) {
		this.programIndex= index;
	}
	
	public AttributeSet getAttributes() {
		return LogConsole.getAttributes(type);
	}

	public boolean isCode() {
		return type == LogConsole.STYLE_CODE;
	}

	public boolean isError() {
		return type == LogConsole.STYLE_ERROR;
	}
	
	public boolean isSelecteable() {
		return isCode();
	}
	
    public boolean isLinkable() {
    	return (lineNumber>-1)&&(programIndex>-1);
    }
    


}
