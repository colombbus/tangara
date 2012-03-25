package org.colombbus.tangara;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

public class BaseMaker implements PropertyChangeListener {

	/**  Class logger */
    private Logger LOG =Logger.getLogger(BaseMaker.class);
    private BaseFrame frame = null;
    private int currentStep = 1;
    
    private int extractJARFromDirectory(File JarDirectory, File destination, boolean recursive) {
    	File[] elements = JarDirectory.listFiles();
    	int filesCount = 0;
    	for (int i=0;i<elements.length;i++) {
    		if (elements[i].isDirectory()) {
    			if (recursive) {
    				filesCount += extractJARFromDirectory(elements[i], destination, true);
    			}
    		} else {
    			if (elements[i].getName().endsWith(".jar")) {
    	    		FileUtils.extractJar(elements[i], destination);
    	    		filesCount++;
    			}
    		}
    	}
    	return filesCount;
    }

    private int extractJARFromDirectory(File JarDirectory, File destination, Vector<String> toSkip, boolean recursive) {
    	File[] elements = JarDirectory.listFiles();
    	int filesCount = 0;
    	for (int i=0;i<elements.length;i++) {
    		if (elements[i].isDirectory()) {
    			if (recursive) {
    				filesCount += extractJARFromDirectory(elements[i], destination, toSkip, true);
    			}
    		} else {
    			if ((elements[i].getName().endsWith(".jar"))&&(!toSkip.contains(elements[i].getName()))){
    	    		FileUtils.extractJar(elements[i], destination);
    	    		filesCount++;
    			}
    		}
    	}
    	return filesCount;
    }

    public void make() {
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
    	frame.setText(Messages.getString("Base.generationText"));
    	frame.setVisible(true);
    	generateBase();
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
    
	private void generateBase() {
		(new BaseGeneration()).start();
	}
	
	private void end() {
    	frame.setVisible(false);
    	frame.dispose();
    	Main.launchGUI();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		updateProgress();
	}
	
	private class BaseGeneration extends Thread {
		@Override
		public void run() {
			int filesCount = 0;
			
	    	LOG.info("Generating base");
	    	Configuration conf = Configuration.instance();
	    	
	    	File directory = FileUtils.createTempDirectory();
	    	
	    	LOG.info("Unziping files");
	    	
	    	frame.setMaximumProgress(14);

	    	// 1st unzip libs
	    	LOG.debug("Unziping libs");
	    	File libDirectory = new File(conf.getTangaraPath().getParentFile(),"lib");
	    	StringTokenizer skipLibs = new StringTokenizer(conf.getProperty("base.skipLibs"),",");
	    	Vector<String> skipValues = new Vector<String>();
	    	while(skipLibs.hasMoreTokens()) {
	    		skipValues.add(skipLibs.nextToken());
	    	}
	    	filesCount += extractJARFromDirectory(libDirectory, directory, skipValues, false);
	    	updateProgress();
	    	
	    	// 2nd unzip objects
	    	LOG.debug("Unziping objects");
	    	File objectsDirectory = new File(conf.getTangaraPath().getParentFile(),"objects");
	    	filesCount += extractJARFromDirectory(objectsDirectory, directory, false);
	    	updateProgress();
	    	
	    	// 3rd unzip objects libs
	    	LOG.debug("Unziping objects libs");
	    	File objectsLibrariesDirectory = new File(objectsDirectory,"lib");
	    	filesCount += extractJARFromDirectory(objectsLibrariesDirectory, directory, false);
	    	updateProgress();

	    	// 4th copy objects resources
	    	LOG.debug("Copying objects resources");
	    	File resourcesDirectory = new File(objectsDirectory,"resources");
	    	File resourcesDestination = new File(directory, "org/colombbus/tangara/objects/resources");
	    	resourcesDestination.mkdir();
	    	filesCount += FileUtils.copyDirectory(resourcesDirectory, resourcesDestination);
	    	updateProgress();
	    	
	    	// 5th unzip tangara.jar
	    	LOG.debug("Unziping tangara.jar");
	    	filesCount += FileUtils.extractJar(conf.getTangaraPath(), directory);
	    	updateProgress();
			
			// 6th remove META-INF directory
	    	LOG.debug("removing META-INF");
	    	File metaDirectory = new File(directory, "META-INF");
	    	FileUtils.deleteDirectory(metaDirectory);
	    	updateProgress();

			// 7th remove every file under the root directory
	    	LOG.debug("removing files in root directory");
	    	File[] files = directory.listFiles();
	    	for (int i=0;i<files.length;i++) {
	    		if (!files[i].isDirectory())
	    			files[i].delete();
	    	}
	    	updateProgress();
	    	
	    	frame.setMaximumProgress(filesCount*2);
	    	currentStep = filesCount;
	    	
	    	// Make JAR
			LOG.info("Making JAR");
			File basePath = conf.getBasePath();
			File baseDir = basePath.getParentFile(); 
			if (!baseDir.exists())
				baseDir.mkdir();
			Hashtable<String, String> manifestAttributes = new Hashtable<String, String>();
			manifestAttributes.put(Configuration.BASE_VERSION_PROPERTY, Configuration.instance().getString("tangara.version"));
	    	FileUtils.makeJar(directory, basePath, "org.colombbus.tangara.Main", BaseMaker.this, manifestAttributes);
	    	end();
		}
	}
}
