package org.colombbus.tangara;

import java.util.ArrayList;

import javax.swing.AbstractListModel;


@SuppressWarnings("serial")
public class LogConsoleModel extends AbstractListModel {
	
    private ArrayList<LogElement> data = null;

    public LogConsoleModel() {
    	data = new ArrayList<LogElement>();
    }
    
    public void addLine(final Object object) {
    	data.add((LogElement)object);
    	int lastIndex = data.size()-1;
    	fireIntervalAdded(this, lastIndex, lastIndex); 
    }
    
    public void setError(int index, int errorLineNumber, int programIndex) {
    	LogElement element = data.get(index);
    	element.setType(LogConsole.STYLE_ERROR);
    	element.setLineNumber(errorLineNumber);
    	element.setProgramIndex(programIndex);
    	fireContentsChanged(this, index, index);
    }
    
    public void setErrors(int index, int number, int errorLineNumber, int programIndex) {
    	for (int i = 0; i<number ; i++) {
    		if (index+i < data.size()) {
    	    	setError(index+i, errorLineNumber, programIndex);
    		}
    	}
    }
    
    public void clear() {
    	int lastIndex = data.size() - 1;
    	data.clear();
    	if (lastIndex > 0)
    	fireIntervalRemoved(this, 0, lastIndex);
	}
    
    public boolean isCode(int index) {
    	LogElement element = data.get(index);
    	return element.isCode();
    }

    public boolean isError(int index) {
    	LogElement element = data.get(index);
    	return element.isError();
    }
    
    public boolean isSelecteable(int index) {
    	LogElement element = data.get(index);
    	return element.isSelecteable();
    }

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public Object getElementAt(int index) {
    	return data.get(index);
	}

}
