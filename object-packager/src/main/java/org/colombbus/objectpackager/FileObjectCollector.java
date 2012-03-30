package org.colombbus.objectpackager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Collect all the classes relatives to a Tangara objects.
 *
 * <pre>
 * It includes all translated classes, inner classes and dedicated packages.
 * </pre>
 */
class FileObjectCollector {

	private List<File> objectFiles = new ArrayList<File>();
	private File basePackage;

	private TObjectMetadata metadata;

	public FileObjectCollector(File baseDir) {
		validateBaseDirArgument(baseDir);
		basePackage = new File(baseDir, "org/colombbus/tangara/objects/");
	}

	private static void validateBaseDirArgument(File baseDir) {
		if (baseDir == null)
			throw new IllegalArgumentException("baseDir argument is null"); //$NON-NLS-1$
		if (baseDir.exists() == false) {
			String msg = String.format("Directory '%s' does not exist", baseDir.getPath()); //$NON-NLS-1$
			throw new IllegalArgumentException(msg);
		}
		if (baseDir.isFile()) {
			String msg = String.format("baseDir '%s' shall be a directory", baseDir.getPath()); //$NON-NLS-1$
			throw new IllegalArgumentException(msg);
		}
	}

	public void collect(TObjectMetadata objectMetadata) {
		if (objectMetadata == null)
			throw new IllegalArgumentException("metadata argument is null"); //$NON-NLS-1$
		this.metadata = objectMetadata;

		collectBasePackageFiles();
		collectDedicatedPackageFiles();
		for (Language lang : Language.values()) {
			collectLanguagePackageFiles(lang);
		}
	}

	private void collectBasePackageFiles() {
		String objectName = metadata.getDefaultObjectName();
		collectByFilteringDirectory(objectName, basePackage);
	}

	private void collectByFilteringDirectory(String objectName, File directory) {
		for (File file : directory.listFiles()) {
			if (fileBelongsObject(file, objectName)) {
				objectFiles.add(file);
			}
		}
	}

	private static boolean fileBelongsObject(File file, String objectName) {
		String filename = file.getName();
		if( filename.equals(objectName+".class")) //$NON-NLS-1$
			return true;
		if( filename.startsWith(objectName+"$")) //$NON-NLS-1$
			return true;
		return false;
	}

	private void collectDedicatedPackageFiles() {
		String dedicatedPackagename = metadata.getPackageName();
		if (dedicatedPackagename == null)
			return;

		File dedicatedPackage = new File(basePackage, dedicatedPackagename);
		collectDirectoryFiles(dedicatedPackage);
	}

	private void collectDirectoryFiles(File directory) {
		for (File file : directory.listFiles()) {
			objectFiles.add(file);
		}
	}

	private void collectLanguagePackageFiles(Language lang) {
		String langPackage = lang.getPackageName();
		File directory = new File( basePackage, langPackage);

		String objectName = metadata.getI18NObjectName(lang);
		collectByFilteringDirectory(objectName,directory);
	}

	public List<File> getFiles() {
		return objectFiles;
	}
}
