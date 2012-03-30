package org.colombbus.objectpackager;

enum Language {

	ENGLISH("en"), FRENCH("fr"), SPANISH("es"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


	private String packageName;

	private Language(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageName() {
		return packageName;
	}
}
