package org.colombbus.tangara;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;

import javax.swing.SwingUtilities;

public class JarMaker implements PropertyChangeListener {

    private BaseFrame frame = null;
    private int currentStep = 1;

    Vector<File> filesToAdd;
    File newBase;
    String prefix;
    File executionPropertiesFile;
    String execPrefix;

    public void make(Vector<File> filesToAdd, File newBase, String prefix, File executionPropertiesFile, String execPrefix) {
    	this.filesToAdd = filesToAdd;
    	this.newBase = newBase;
    	this.prefix = prefix;
    	this.executionPropertiesFile = executionPropertiesFile;
    	this.execPrefix = execPrefix;
    	// Create graphics
    	SwingUtilities.invokeLater(new Runnable() {
    		@Override
			public void run() {
    			createAndShowGUI();
    		}
    	});
    }

    private void createAndShowGUI() {
    	frame = new BaseFrame();
    	frame.setText(Messages.getString("Jar.generationText"));
    	frame.setVisible(true);
    	generateJar();
    }

    private void updateProgress() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				frame.setProgress(currentStep);
			}
		});
    	currentStep++;
    }

	private void generateJar() {
		(new BaseGeneration()).start();
	}

	private void end() {
    	frame.setVisible(false);
    	frame.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		updateProgress();
	}

	private class BaseGeneration extends Thread {
		@Override
		public void run() {
			FileUtils.startJarEdit(newBase);
	    	frame.setMaximumProgress(filesToAdd.size());
	    	FileUtils.addFileToJar(executionPropertiesFile, newBase, execPrefix);
	    	executionPropertiesFile.delete();
			for (int i = 0; i < filesToAdd.size(); i++)
			{
				FileUtils.addFileToJar(filesToAdd.get(i), newBase, prefix);
				updateProgress();
			}
			FileUtils.stopJarEdit(newBase);
	    	end();
		}
	}
}
