package org.colombbus.objectpackager;

import java.util.jar.JarFile;

class JarUtil {

	private JarUtil() {
	}

	public static void closeQuietly(JarFile jarFile) {
		try {

			if (jarFile != null)
				jarFile.close();

		} catch (Throwable ignored) {
		}
	}

}
