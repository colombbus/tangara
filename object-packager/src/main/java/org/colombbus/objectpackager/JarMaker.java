package org.colombbus.objectpackager;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;
import java.util.*;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.FileUtils;

class JarMaker {

	private String sourcePath;
	private List<JarEntryDesc> entries = new ArrayList<JarEntryDesc>();
	private File jarFile;
	private JarOutputStream jarOut;

	public void setSourceDir(File sourceDir) {
		if (sourceDir == null)
			throw new IllegalArgumentException("sourceDir argument is null");//$NON-NLS-1$
		if (sourceDir.exists() == false || sourceDir.isFile())
			throw new IllegalArgumentException(sourceDir.getPath() + " is not an existing directory");//$NON-NLS-1$

		this.sourcePath = sourceDir.getAbsolutePath();
	}

	public void addInputFiles(Collection<File> files) {
		if (files == null)
			throw new IllegalArgumentException("files argument is null");//$NON-NLS-1$

		for (File file : files)
			addInputFile(file);
	}

	public void addInputFile(File file) {
		if (sourcePath == null)
			throw new IllegalStateException("sourceDir not set"); //$NON-NLS-1$
		if (file == null)
			throw new IllegalArgumentException("entryFile argument is null");//$NON-NLS-1$
		if (file.exists() == false || file.isDirectory())
			throw new IllegalArgumentException(file.getPath() + " is not an existing file");//$NON-NLS-1$

		// TODO check entry file in base directory
		appendFileEntries(file);
	}

	private void appendFileEntries(File file) {
		String filePath = file.getAbsolutePath();
		validateInsideSourcePath(filePath);

		String relativePath = filePath.substring(sourcePath.length() + 1);
		relativePath = relativePath.replace(File.separatorChar, '/');

		int separatorPos = 0;
		do {

			separatorPos = relativePath.indexOf('/', separatorPos);
			JarEntryDesc entry;
			if (separatorPos == -1) {
				String filename = relativePath;
				entry = JarEntryDesc.createFileEntry(filename, file);
			} else {
				String dirname = relativePath.substring(0, separatorPos + 1);
				entry = JarEntryDesc.createDirectoryEntry(dirname);
				separatorPos++;
			}
			appendEntry(entry);

		} while (separatorPos > -1);
	}

	private void validateInsideSourcePath(String filePath) {
		if (filePath.startsWith(sourcePath) == false) {
			String msg = String.format("File '%s' shall be in sourceDir directory '%s'", filePath, sourcePath);//$NON-NLS-1$
			throw new IllegalArgumentException(msg);
		}
	}

	private void appendEntry(JarEntryDesc entry) {
		if (entries.contains(entry) == false)
			entries.add(entry);
	}

	public void writeTo(File file) throws IOException {
		if (sourcePath == null)
			throw new IllegalStateException("sourceDir not set"); //$NON-NLS-1$
		if (file == null)
			throw new IllegalArgumentException("file argument is null");//$NON-NLS-1$
		if (file.exists() && file.isDirectory())
			throw new IllegalArgumentException(file.getPath() + " is not a file");//$NON-NLS-1$

		this.jarFile = file;
		writeJarFile();
	}

	private void writeJarFile() throws IOException {
		FileOutputStream fileOut = null;
		try {

			fileOut = FileUtils.openOutputStream(jarFile);
			jarOut = new JarOutputStream(fileOut);
			writeJarContent();

		} finally {
			closeQuietly(jarOut);
			closeQuietly(fileOut);
		}
	}

	private void writeJarContent() throws IOException {
		for (JarEntryDesc entry : entries) {
			entry.writeTo(jarOut);
		}
	}

}