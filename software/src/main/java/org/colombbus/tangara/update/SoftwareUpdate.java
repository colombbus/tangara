/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2009-2012 Colombbus (http://www.colombbus.org)
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
package org.colombbus.tangara.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.colombbus.tangara.core.Version;

public class SoftwareUpdate {

	private static final String UPDATE_SITE = "http://tangara.colombbus.org"; //$NON-NLS-1$
	private static final Logger LOG = Logger.getLogger(SoftwareUpdate.class);

	private SoftwareUpdate() {
	}

	public static void launchVersionCheck() {
		try {
			UpdateCheckRunner runner = createUpdateCheckRunner();
			Thread th = new Thread(runner);
			th.start();
		} catch (IOException ioEx) {
			LOG.error("Fail to load version from " + UPDATE_SITE); //$NON-NLS-1$
		}
	}

	private static UpdateCheckRunner createUpdateCheckRunner() throws IOException {
		Version currentVersion = loadCurrentVersion();
		return new UpdateCheckRunner(currentVersion, UPDATE_SITE);
	}

	public static void launchVersionCheckAndShowNotFound() {
		try {
			UpdateCheckRunner runner = createUpdateCheckRunner();
			runner.setShowNotFoundMessage(true);
			Thread th = new Thread(runner);
			th.start();
		} catch (IOException ioEx) {
			LOG.error("Fail to load version from " + UPDATE_SITE); //$NON-NLS-1$
		}
	}

	private static Version loadCurrentVersion() throws IOException {
		InputStream in = SoftwareUpdate.class.getResourceAsStream("/org/colombbus/tangara/fixed.properties"); //$NON-NLS-1$
		Properties configuration = new Properties();
		configuration.load(in);
		String versionTxt = configuration.getProperty("tangara.version"); //$NON-NLS-1$
		return new Version(versionTxt);
	}

	public static final void main(String[] args) {
		SoftwareUpdate.launchVersionCheck();
	}

}
