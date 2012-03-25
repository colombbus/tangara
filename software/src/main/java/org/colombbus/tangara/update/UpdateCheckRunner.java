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

import java.awt.Component;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.colombbus.tangara.core.Version;

class UpdateCheckRunner implements Runnable {

	private static final Logger LOG = Logger.getLogger(UpdateCheckRunner.class);

	private String updateSite;
	private Version currentVersion;
	private SoftwareUpdateInfo updateInfo;
	private boolean showNotFoundMsg=false;

	public UpdateCheckRunner(Version currentVersion, String updateSite) {
		Validate.notNull(currentVersion, "currentVersion argument is null"); //$NON-NLS-1$
		Validate.notNull(updateSite,"updateSite argument is null"); //$NON-NLS-1$

		this.currentVersion = currentVersion;
		this.updateSite = updateSite;
	}

	public void setShowNotFoundMessage(boolean showNotFoundMsg) {
		this.showNotFoundMsg = showNotFoundMsg;
	}


	@Override
	public void run() {
		try {
			updateInfo = readSoftwareUpdateInfo();
			if (updateInfo.moreRecentThan(currentVersion)) {
				showUpdateSoftwareWindow();
			} else if( showNotFoundMsg){
				showNoUpdateWindow();
			}
		} catch (IOException ioEx) {
			LOG.error("Fail to connect to the web", ioEx); //$NON-NLS-1$
		} catch (Throwable th) {
			LOG.error("An error occurs", th); //$NON-NLS-1$
		}

	}

	static class DisplayWindowRunner implements Runnable {

		private SoftwareUpdateInfo info;

		public DisplayWindowRunner(SoftwareUpdateInfo info) {
			this.info = info;
		}


		@Override
		public void run() {
			JDialog dialog = new SoftwareUpdateDialog(info);
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
		}
	}

	private void showUpdateSoftwareWindow() {
		Runnable displayer = new DisplayWindowRunner(updateInfo);
		runInEventDispatcherThread(displayer);
	}


	private static void runInEventDispatcherThread(Runnable displayer) {
		if (SwingUtilities.isEventDispatchThread()) {
			displayer.run();
		} else {
			SwingUtilities.invokeLater(displayer);
		}
	}

	static class NoUpdateFoundRunner implements Runnable {

		public NoUpdateFoundRunner() {
		}


		@Override
		public void run() {
			Component parentComponent = null;
			String message = Messages.getString("NoUpdateFound.message"); //$NON-NLS-1$
			String title = Messages.getString("NoUpdateFound.title"); //$NON-NLS-1$
			int messageType = JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
		}
	}

	private static void showNoUpdateWindow() {
		Runnable displayer = new NoUpdateFoundRunner();
		runInEventDispatcherThread(displayer);
	}

	private SoftwareUpdateInfo readSoftwareUpdateInfo() throws IOException {
		UpdateRequester requester = new UpdateRequester();
		requester.setUpdateSite(updateSite);
		return requester.requestSoftwareInfo();
	}


}
