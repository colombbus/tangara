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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.log4j.Logger;

/**
 * This class permits to associate the .tgr files with Tangara.
 */
class WindowsConfiguration {

	private static final Logger LOG = Logger.getLogger(WindowsConfiguration.class);

	/**
	 * Works only on Windows systems: insert in registry the data required to
	 * open tgr files with Tangara
	 */
	public static void registerTgrFiles(File tangaraPath) {
		String jarPath = getRegistryValue("HKCR\\jarfile\\shell\\Open\\command"); //$NON-NLS-1$
		if (jarPath == null) {
			LOG.warn("Unable to access jarFile open command"); //$NON-NLS-1$
			return;
		}
		if (tangaraPath == null) {
			LOG.warn("Unable to find Tangara path"); //$NON-NLS-1$
			return;
		}
		jarPath = jarPath.replace("%1", tangaraPath.getAbsolutePath()); //$NON-NLS-1$
		jarPath = jarPath.replace("%*", "\"%1\""); //$NON-NLS-1$ //$NON-NLS-2$
		jarPath = jarPath.replace("\"", "\\\""); //$NON-NLS-1$//$NON-NLS-2$
		jarPath = "\"" + jarPath + "\""; //$NON-NLS-1$//$NON-NLS-2$

		setRegistryValue("HKCR\\Colombbus.Tangara.1", "\"Programme Tangara\""); //$NON-NLS-1$//$NON-NLS-2$
		setRegistryValue("HKCR\\Colombbus.Tangara.1\\shell\\Open\\command", jarPath); //$NON-NLS-1$
		setRegistryValue("HKCR\\.tgr", "Colombbus.Tangara.1"); //$NON-NLS-1$//$NON-NLS-2$
	}

	// Works only on Windows systems: get the default REG_SZ value of a registry
	// key
	private static String getRegistryValue(String key) {
		String command = String.format("reg query %s /ve", key); //$NON-NLS-1$
		if (LOG.isDebugEnabled()) {
			String msg = String.format("Getting Registry key %s : %s", key, command); //$NON-NLS-1$
			LOG.debug(msg);
		}

		try {
			Process process = Runtime.getRuntime().exec(command);
			StreamReader reader = new StreamReader(process.getInputStream());

			reader.start();
			process.waitFor();
			reader.join();

			String result = reader.getResult();

			String regsz = "REG_SZ"; //$NON-NLS-1$

			int p = result.indexOf(regsz);

			if (p == -1) {
				return null;
			}

			return result.substring(p + regsz.length()).trim();
		} catch (Exception ex) {
			LOG.warn("Error when trying to read registry key " + key, ex); //$NON-NLS-1$
			return null;
		}
	}

	// Works only on Windows systems: set the default REG_SZ value of a registry
	// key
	private static void setRegistryValue(String key, String value) {
		// REG ADD HKCR\Colombbus.Tangara.1 /ve /t REG_SZ /d "Programme Tangara"
		// /f
		String command = String.format("reg add %s /ve /t REG_SZ /d %s /f", key, value); //$NON-NLS-1$
		try {
			LOG.debug("Setting Registry key " + key + " : " + command); //$NON-NLS-1$//$NON-NLS-2$
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
		} catch (Exception ex) {
			LOG.warn("Error when trying to set registry key " + key + " to " + value, ex); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	static class StreamReader extends Thread {

		private InputStream is;

		private StringWriter sw = new StringWriter();

		StreamReader(InputStream is) {
			this.is = is;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1)
					sw.write(c);
			} catch (IOException e) {
				LOG.warn("Fail reading stream", e); //$NON-NLS-1$
			}
		}

		String getResult() {
			return sw.toString();
		}
	}
}
