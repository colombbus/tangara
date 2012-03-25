/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008 Colombbus (http://www.colombbus.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * This class can check if a file can be used by
 * tangara (file. Tgr or. Txt) from the path (in form java.io.File or string).
 * @author gwen
 *
 */
public class FileUtils {

	private static final String TEXT_FILE_EXTENSION = "txt";

	private static String tangaraFileExt = null;

	private static String tangaraObjectExt = null;

    /**  Class logger */
    private static Logger LOG =Logger.getLogger(FileUtils.class);

    private static JarUtils jar = null;

    private static Vector<File> tempDirectories = null;

    static {
    	tempDirectories = new Vector<File>();
    }

	private FileUtils() {
		super();
	}

	public static final boolean isExtension(File file, String extension)
	{
		return file.getName().toLowerCase().endsWith("."+extension.toLowerCase());
	}

	public static final boolean isExtension(String fileName, String extension)
	{
		return fileName.toLowerCase().endsWith("."+extension.toLowerCase());
	}


	/**
	 * Returns if the extension of path is "tgr" or not
	 * @param path
	 * 		java.io.File the path file
	 * @return
	 * 		true if the extension is "tgr"
	 */
	public static final boolean isTangaraFile(File path) {
		return isExtension(path, getTangaraFileExt());
	}

	/**
	 * Returns if the extension of path is "tgr" or not
	 * @param path
	 * 		java.lang.String the path file
	 * @return
	 * 		true if the extension is "tgr"
	 */
	public static final boolean isTangaraFile(String path) {
		return isExtension(path,getTangaraFileExt());
	}

	/**
	 * Returns if the extension of path is "tgo" or not
	 * @param path
	 * 		java.io.File the path file
	 * @return
	 * 		true if the extension is "tgo"
	 */
	public static final boolean isTangaraObjectFile(File path) {
		return isExtension(path, getTangaraObjectExt());
	}

	/**
	 * Returns if the extension of path is "tgr" or not
	 * @param path
	 * 		java.lang.String the path file
	 * @return
	 * 		true if the extension is "tgr"
	 */
	public static final boolean isTangaraObjectFile(String path) {
		return isExtension(path, getTangaraObjectExt());
	}

	/**
	 * Returns if the extension of path is "txt" or not
	 * @param path
	 * 		java.io.File the path file
	 * @return
	 * 		true if the extension is "txt"
	 */
	public static final boolean isTextFile(File path) {
		return isExtension(path, getTextFileExt());
	}

	/**
	 * Returns if the extension of path is "txt" or not
	 * @param path
	 * 		java.lang.String the path file
	 * @return
	 * 		true if the extension is "txt"
	 */
	public static final boolean isTextFile(String path) {
		return isExtension(path, getTextFileExt());
	}

	/**
	 * Returns a string corresponding to the extension of Tangara files "tgr"
	 * @return
	 * 		"tgr"
	 */
	public static final String getTangaraFileExt() {
		if (tangaraFileExt == null) {
			// we don't care if the content is overriden
			tangaraFileExt = Configuration.instance().getProperty("tangara.file.extension").toLowerCase();
		}
		return tangaraFileExt;
	}

	/**
	 * Returns a string corresponding to the extension of Tangara files "tgo"
	 * @return
	 * 		"tgo"
	 */
	public static final String getTangaraObjectExt() {
		if (tangaraObjectExt == null) {
			// we don't care if the content is overriden
			tangaraObjectExt = Configuration.instance().getProperty("tangara.object.extension").toLowerCase();
		}
		return tangaraObjectExt;
	}

	/**
	 * Returns a string corresponding to the extension of text files "txt"
	 * @return
	 * 		"txt"
	 */
	public static final String getTextFileExt() {
		return TEXT_FILE_EXTENSION;
	}

	/**
	 * Try to find a file from its filename.
	 * <p>
	 * The algorithm checks if the filename is a full path, a path relative to
	 * the current directory or a path relative to home directory.<br/> If no
	 * file has been found, the extension is append to its name and the same
	 * directories are parsed in the same order.
	 * </p>
	 *
	 * @param filename
	 *            the filename of the file to found
	 * @param fileExt
	 *            the default extension of the file. May be <code>null</code>.
	 *            No dot is added during aggregation with the filename, so think
	 *            to add it if it is required.
	 * @return the <code>java.lang.File</code> instance associated to the
	 *         filename if it was found, <code>null</code> otherwise.
	 */
	public static File findPath(String filename, String fileExt) {
		File path = findPath(filename);
		if (path == null && fileExt != null) {
			String filenameWithExt = filename + fileExt;
			path = findPath(filenameWithExt);
		}
		return path;
	}

	/**
	 * Try to find a file from its filename.
	 * <p>
	 * The algorithm checks if the filename is a full path, a path relative to
	 * the current directory or a path relative to home directory.
	 * </p>
	 *
	 * @param filename
	 *            the filename of the file to found
	 * @return the <code>java.lang.File</code> instance associated to the
	 *         filename if it was found, <code>null</code> otherwise.
	 */
	public static File findPath(String filename) {
		// case 1: full path
		File path = new File(filename);
		if (path.exists()) {
			return path;
		}

		// case 2: path relative to current directory
		File currentDir = Program.instance().getCurrentDirectory();
		path = new File(currentDir, filename);
		if (path.exists()) {
			return path;
		}

		// case 3: path relative to home directory
		File homeDir = Configuration.instance().getUserHome();
		path = new File(homeDir, filename);
		if (path.exists()) {
			return path;
		}
		return null;
	}

	public static void copyFile(BufferedInputStream source, File destination) {
    	BufferedOutputStream output = null;
    	try
    	{
    		output =  new BufferedOutputStream(new FileOutputStream(destination));
	        byte buffer[] = new byte[2048];
	        while (true) {
	        	int n = source.read(buffer);
	        	if (n <= 0)
	        		break;
	        	output.write(buffer, 0, n);
	    	}
    	}
        catch (IOException e) {
        	LOG.error("could not copy file '"+destination.getName()+"'", e);
    	} finally {
        	if (output != null) {
        		try {
        			output.close();
        		} catch (IOException e) {
        		}
        	}
        }
	}

	public static void copyFile(File source, File destination) {
		BufferedInputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(source));
			copyFile(input, destination);
		} catch (IOException e) {
			IOUtils.closeQuietly(input);
		}
	}

	public static File createTempDirectory() {
		try {
			File tempDirectory = File.createTempFile("resources", "");
			tempDirectory.delete();
			tempDirectory.mkdir();
			tempDirectories.add(tempDirectory);
			return tempDirectory;
		} catch (IOException e) {
			LOG.error("Error while creating temp directory", e);
			return null;
		}
	}

	public static int extractJar(File jar, File directory) {
    	JarEntry entry = null;
    	File currentFile = null;
    	BufferedInputStream input = null;
    	JarFile jarFile = null;
    	int filesCount = 0;
		try {
	    	jarFile = new JarFile(jar);
	    	Enumeration<JarEntry> entries = jarFile.entries();

	    	while(entries.hasMoreElements()) {
	    		entry = entries.nextElement();
	    		currentFile = new File(directory, entry.getName());
	    		if (entry.isDirectory()) {
	    			currentFile.mkdir();
	    		} else {
	        		currentFile.createNewFile();
		    		input = new BufferedInputStream(jarFile.getInputStream(entry));
		    		copyFile(input, currentFile);
		    		input.close();
		    		filesCount++;
	    		}
	    	}
		} catch (IOException e) {
			LOG.error("Error extracting JAR file "+jar.getAbsolutePath(),e);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
				if (jarFile != null) {
					jarFile.close();
				}
			} catch (IOException e) {
			}
		}
		return filesCount;
	}

	private static void addDirectoryToJar(File directory, JarOutputStream output, String prefix, PropertyChangeListener listener) throws IOException {
		try {
			File files[] = directory.listFiles();
	        JarEntry entry = null;
	        for (int i=0; i<files.length; i++) {
	        	if (files[i].isDirectory()) {
	        		if (prefix != null) {
	        			entry = new JarEntry(prefix+"/"+files[i].getName()+"/");
		            	output.putNextEntry(entry);
	        			addDirectoryToJar(files[i], output, prefix+"/"+files[i].getName(), listener);
	        		} else {
	        			entry = new JarEntry(files[i].getName()+"/");
		            	output.putNextEntry(entry);
	        			addDirectoryToJar(files[i], output, files[i].getName(), listener);
	        		}
	        	} else {
	        		addFileToJar(files[i], output, prefix);
	    	        if (listener != null) {
	    	        	listener.propertyChange(new PropertyChangeEvent(directory, "fileAdded", null, null));
	    	        }
	        	}
	        }
        } catch (IOException e) {
        	LOG.error("Error while adding directory '"+directory.getAbsolutePath()+"'",e);
        	throw e;
        }
	}

	private static void addFileToJar(File fileToAdd, JarOutputStream output, String prefix) throws IOException {
        BufferedInputStream input = null;
        JarEntry entry = null;
        try {
			if (prefix != null)
				entry = new JarEntry(prefix+"/"+fileToAdd.getName());
			else
				entry = new JarEntry(fileToAdd.getName());
	    	output.putNextEntry(entry);
	    	input =  new BufferedInputStream(new FileInputStream(fileToAdd));
	        byte buffer[] = new byte[2048];
	        while (true) {
	        	int n = input.read(buffer);
	        	if (n <= 0)
	        		break;
	        	output.write(buffer, 0, n);
	    	}
	        input.close();
        } catch (IOException e) {
        	LOG.error("Error trying to add file '"+fileToAdd.getAbsolutePath()+"' to jar", e);
        	if (input != null) {
        		try {
        			input.close();
        		} catch (IOException e2) {
        		}
        	}
        	throw e;
        }
	}

	public static void startJarEdit(File jarFile) {
		jar = new JarUtils(jarFile);
	}

	public static boolean stopJarEdit(File jarFile) {
		try {
			jar.save();
			jar = null;
			return true;
		} catch (IOException e) {
			LOG.error("Error while trying to save jar edits",e);
		}
		return false;
	}


	public static boolean addFileToJar(File fileToAdd, File jarFile, String prefix) {
		if (fileToAdd.isDirectory())
			return false;
		if (jar == null)
			startJarEdit(jarFile);
		try {
			jar.addFile(fileToAdd, prefix);
			return true;
		} catch (IOException e) {
			LOG.error("Error while adding file '"+fileToAdd.getAbsolutePath()+"' to jar file '"+jarFile.getAbsolutePath()+"'",e);
			return false;
		}
	}

	public static boolean addDirectoryToJar(File directoryToAdd, File jarFile, String prefix) {
		if (!directoryToAdd.isDirectory())
			return false;
		if (jar == null)
			startJarEdit(jarFile);
		try {
			jar.addDirectory(directoryToAdd, prefix);
			return true;
		} catch (IOException e) {
			LOG.error("Error while trying to umount files",e);
			return false;
		}
	}

	public static void makeJar(File directory, File jar, String mainClass, PropertyChangeListener listener,Hashtable<String, String> manifestAttributes) {
		JarOutputStream jarOutput = null;
		try {
	    	jarOutput = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jar)));
	    	addDirectoryToJar(directory, jarOutput, null, listener);
	    	StringBuffer sbuf = new StringBuffer();
	    	sbuf.append("Manifest-Version: 1.0\n");
	    	sbuf.append("Built-By: Colombbus\n");
	    	if (mainClass != null)
		    	sbuf.append("Main-Class: "+mainClass+"\n");
	    	if (manifestAttributes != null) {
	    		for (Enumeration<String> keys = manifestAttributes.keys(); keys.hasMoreElements();) {
	    			String name = keys.nextElement();
	    			sbuf.append(name+": "+manifestAttributes.get(name)+"\n");
	    		}
	    	}
	    	InputStream is = new ByteArrayInputStream(sbuf.toString().getBytes("UTF-8"));

	    	JarEntry manifestEntry = new JarEntry("META-INF/MANIFEST.MF");
	    	jarOutput.putNextEntry(manifestEntry);

	    	byte buffer[] = new byte[2048];
	        while (true) {
	        	int n = is.read(buffer);
	        	if (n <= 0)
	        		break;
	        	jarOutput.write(buffer, 0, n);
	    	}
	        is.close();
	        jarOutput.close();
		} catch(Exception e) {
			LOG.error("Error while creating JAR file '"+jar.getAbsolutePath()+"'",e);
		} finally {
			try {
				if (jarOutput!=null)
					jarOutput.close();
			} catch (IOException e) {
			}
		}
	}

	public static int copyDirectory(File source, File destination) {
		int filesCount = 0;
		File[] sourceFiles = source.listFiles();
		for(File file:sourceFiles) {
			if (file.isDirectory()) {
				File destinationDirectory = new File(destination, file.getName());
				destinationDirectory.mkdir();
				filesCount+= copyDirectory(file, destinationDirectory);
			} else {
				File destinationFile = new File(destination, file.getName());
				copyFile(file, destinationFile);
				filesCount++;
			}
		}
		return filesCount;
	}

	public static boolean deleteDirectory(File source) {
		if (!source.isDirectory())
			return false;
		File[] elements = source.listFiles();
		for (int i=0; i<elements.length; i++) {
			if (elements[i].isDirectory()) {
				if (!deleteDirectory(elements[i]))
					return false;
			} else {
				if (!elements[i].delete())
					return false;
			}
		}
		return source.delete();
	}

	public static void clean() {
		for (File tempDirectory:tempDirectories) {
			deleteDirectory(tempDirectory);
		}
	}

	public static File findFile(String fileName) {
    	File file = new File(fileName);
    	if (!file.isAbsolute()) {
    		// the name does not contain any directory reference : add the current directory
    		file = new File(Program.instance().getCurrentDirectory(),fileName);
    		// if file does not exist, try with user home directory
    		if (!file.exists())
    			file = new File(Configuration.instance().getUserHome(),fileName);
    		fileName = file.getAbsolutePath();
    	}
		if (!(file.exists()))
			return null;
		else
			return file;
    }

    /**
     * Creates a BufferedImage from a file.
     * @param fileName
     * @return
     * @throws Exception
     */
	public static File findFile(String fileName, String[] possibleExtensions)
    {
    	int pointPosition = fileName.lastIndexOf('.');
    	boolean extensionFound = false;
    	if (pointPosition>-1)
    	{
    		// dot Found
    		String extension = fileName.substring(pointPosition+1);
    		if (extension.length()>=0)
    		{
    			extensionFound = true;
    		}
    	}
    	else
    	{
    		// no dot found
			fileName += "."; //$NON-NLS-1$
    	}
    	if (extensionFound) {
    		return findFile(fileName);
    	} else {
    		File file = new File(fileName);
	    	if (!file.isAbsolute()) {
	    		// the name does not contain any directory reference : add the current directory
	    		File currentDirectory = Program.instance().getCurrentDirectory();
	    		for (String extension:possibleExtensions) {
	    			file = new File(currentDirectory,fileName+extension);
	    			if (file.exists())
	    				return file;
	    		}
	    		File userHome = Configuration.instance().getUserHome();
	    		LOG.debug("USER HOME : "+userHome);
	    		for (String extension:possibleExtensions) {
	    			file = new File(userHome,fileName+extension);
	    			if (file.exists())
	    				return file;
	    		}
	    	} else {
	    		// File is absolute: we simply try to find the file with possible extensions
	    		for (String extension:possibleExtensions) {
	    			file = new File(fileName+extension);
	    			if (file.exists())
	    				return file;
	    		}
	    	}
    	}
    	return null;
    }

}
