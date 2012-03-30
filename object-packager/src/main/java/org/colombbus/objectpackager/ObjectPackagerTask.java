package org.colombbus.objectpackager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ObjectPackagerTask extends Task {

	private File summaryFile;
	private File classDir;
	private File targetDir;
	private List<TObjectMetadata> objectMetadatas = new ArrayList<TObjectMetadata>();

	public void setSummaryFile(File summaryFile) {
		this.summaryFile = summaryFile;
	}

	public void setClassDir(File classDir) {
		this.classDir = classDir;
	}

	public void setTarget(File target) {
		this.targetDir = target;
	}

	@Override
	public void execute() throws BuildException {
		validateState();

		loadObjectMetadatas();
		packageAllObjects();
	}

	private void validateState() {
		validateSummaryFile();
		validateClassDir();
		validateTarget();
	}

	private void validateSummaryFile() {
		if (summaryFile == null)
			throw new BuildException("summaryFile not set"); //$NON-NLS-1$

		if (summaryFile.exists() == false) {
			String msg = String.format("summaryFile '%s' does not exist", summaryFile.getPath()); //$NON-NLS-1$
			throw new BuildException(msg);
		}

		if (summaryFile.isDirectory()) {
			String msg = String.format("summaryFile '%s' is not a file", summaryFile.getPath()); //$NON-NLS-1$
			throw new BuildException(msg);
		}
	}

	private void validateClassDir() {
		if (classDir == null)
			throw new BuildException("classDir not set"); //$NON-NLS-1$

		if (classDir.exists() == false) {
			String msg = String.format("classDir '%s' does not exist", classDir.getPath()); //$NON-NLS-1$
			throw new BuildException(msg);
		}

		if (classDir.isFile()) {
			String msg = String.format("classDir '%s' is not a directory", classDir.getPath()); //$NON-NLS-1$
			throw new BuildException(msg);
		}
	}

	private void validateTarget() {
		if (targetDir == null)
			throw new BuildException("target not set"); //$NON-NLS-1$

		if (targetDir.exists() && targetDir.isFile()) {
			String msg = String.format("target '%s' is not a directory", targetDir.getPath()); //$NON-NLS-1$
			throw new BuildException(msg);
		}

		targetDir.mkdirs();
		if (targetDir.exists() == false) {
			String msg = String.format("Cannot create target directory '%s'", targetDir.getPath()); //$NON-NLS-1$
			throw new BuildException(msg);
		}
	}

	private void loadObjectMetadatas() {
		try {

			TObjectMetadataLoader loader = new TObjectMetadataLoader();
			loader.load(summaryFile);
			this.objectMetadatas = loader.getAllMetadata();

		} catch (IllegalArgumentException ex) {
			String msg = String.format("The content of summaryFile '%s' is invalid", summaryFile.getPath()); //$NON-NLS-1$
			throw new BuildException(msg, ex);
		}
	}

	private void packageAllObjects() {
		for (TObjectMetadata objectMetadata : objectMetadatas) {
			packageObject(objectMetadata);
		}
	}

	private void packageObject(TObjectMetadata metadata) {
		try {

			List<File> objectFiles = collectObjectFiles(metadata);
			File targetJarFile = buildJarPath(metadata);
			packageFilesToJar(objectFiles, targetJarFile);

		} catch (IOException ioEx) {
			String msg = String.format("The content of summaryFile '%s' is invalid", summaryFile.getPath()); //$NON-NLS-1$
			throw new BuildException(msg, ioEx);
		}
	}

	private List<File> collectObjectFiles(TObjectMetadata metadata) {
		FileObjectCollector collector = new FileObjectCollector(classDir);
		collector.collect(metadata);
		List<File> objectFiles = collector.getFiles();
		return objectFiles;
	}

	private File buildJarPath(TObjectMetadata metadata) {
		String defaultObjectName = metadata.getDefaultObjectName();
		String jarFilename = String.format("%s.jar", defaultObjectName).toLowerCase(); //$NON-NLS-1$
		File targetJarFile = new File(targetDir, jarFilename);
		return targetJarFile;
	}

	private void packageFilesToJar(List<File> objectFiles, File targetJarFile) throws IOException {
		JarMaker jarMaker = new JarMaker();
		jarMaker.setSourceDir(classDir);
		jarMaker.addInputFiles(objectFiles);
		jarMaker.writeTo(targetJarFile);
	}
}
