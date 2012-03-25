package org.colombbus.tangara;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

public class CommandsTransferable implements Transferable {

	ArrayList<String> commands;
	
	public CommandsTransferable(ArrayList<String> commandArrays) {
		commands = commandArrays;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[1];
		flavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "tangara.command");
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.getHumanPresentableName().compareTo("tangara.command") == 0);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) {
		if (isDataFlavorSupported (flavor))
			return commands;
		return null;
	}

}
