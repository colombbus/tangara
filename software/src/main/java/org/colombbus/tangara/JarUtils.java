package org.colombbus.tangara;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import de.schlichtherle.io.File;

public class JarUtils {
	
	private File jarFile = null;
	
	public JarUtils() {
		File.setDefaultArchiveDetector(new de.schlichtherle.io.DefaultArchiveDetector("jar"));
	}
	
	public JarUtils(java.io.File file) {
		this();
		this.openJar(file);
	}
	
	public void openJar(java.io.File file) {
		jarFile = new File(file);
	}
	
	public void save() throws IOException {
		File.umount();
	}

	public void addFile(java.io.File fileToAdd, String prefix)throws IOException {
		File newFile = new File(fileToAdd);
		File destinationFile = null;
		if (prefix != null)
			destinationFile = new File(jarFile.getAbsolutePath()+"/"+prefix+"/"+fileToAdd.getName());
		else
			destinationFile = new File(jarFile.getAbsolutePath()+"/"+fileToAdd.getName());

		File.cp(newFile, destinationFile);
	}

	public boolean addDirectory(java.io.File directoryToAdd, String prefix)throws IOException {
		File newDirectory = new File(directoryToAdd);
		File destinationDirectory = null;
		if (prefix != null)
			destinationDirectory = new File(jarFile.getAbsolutePath()+"/"+prefix+"/"+directoryToAdd.getName());
		else
			destinationDirectory = new File(jarFile.getAbsolutePath()+"/"+directoryToAdd.getName());

		return newDirectory.copyAllTo(destinationDirectory);
	}

	public static String getManifestProperty(java.io.File jarFile, String property) {
		try {
			JarFile jar = new JarFile(jarFile);
			Manifest manifest = jar.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			return attributes.getValue(property);
		} catch (IOException e) {
			return null;
		}
	}
	
}
