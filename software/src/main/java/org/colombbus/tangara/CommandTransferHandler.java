package org.colombbus.tangara;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class CommandTransferHandler extends TransferHandler implements ClipboardOwner {

	private static Logger LOG = Logger.getLogger(LogConsole.class);
	
	@Override
	public boolean canImport(TransferSupport info) {
		if (info.getComponent() instanceof TextPane)
			return true;
		return false;
    }
	
    @Override
	public boolean importData(TransferSupport info) {
    	if (canImport(info)) {
    		try {
	    		TextPane destination = (TextPane)info.getComponent();
	    		destination.insertLines((ArrayList<String>)info.getTransferable().getTransferData(info.getDataFlavors()[0]));
	    		return true;
    		} catch (Exception e) {
    			LOG.debug("error",e);
    		}
    	}
    	
        return false;
    }
    
    @Override
	public int getSourceActions(JComponent c) {
    	return COPY;
    }
    
    @Override
	public void exportToClipboard(JComponent c, Clipboard clip, int action) {
    	if (c instanceof LogConsole) {
    		ArrayList<String> commands = ((LogConsole)c).getSelectedCode();
    		String contents = "";
    		for (String command:commands) 
    		{
    			contents+=command;
        		if (!command.endsWith(";")) //$NON-NLS-1$
    				contents+=";"; //$NON-NLS-1$
        		contents+="\n";
    		}
    		clip.setContents(new StringSelection(contents), this);
    	}
    }
    
    @Override
	protected Transferable createTransferable(JComponent c) {
    	LogConsole console = (LogConsole) c;
    	return new CommandsTransferable(console.getSelectedCode());
    }

    
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		//  nothing...
	}
}
