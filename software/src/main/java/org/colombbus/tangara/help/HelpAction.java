/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2009 Colombbus (http://www.colombbus.org)
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
package org.colombbus.tangara.help;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import javax.help.HelpBroker;
//import javax.help.HelpSet;
//import javax.help.HelpSetException;
import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.colombbus.tangara.Configuration;
import org.colombbus.tangara.Messages;

/**
 * Help action which show a JavaHelp application.
 *
 * @version $Id: HelpAction.java,v 1.1.2.7 2009-07-19 08:20:38 gwenael.le_roux Exp $
 */
@SuppressWarnings("serial")
public class HelpAction extends AbstractAction {

	/** Logging system */
	private static Logger LOG = Logger.getLogger(HelpAction.class);

	//FIXME help desactived
	//private static HelpBroker helpBroker;
	private static final Lock brokerInitlock = new ReentrantLock();

	/**
	 * Add properties to the action.
	 */
	public HelpAction() {
		String actionName = Messages.getString("HelpAction.name"); //$NON-NLS-1$
		putValue(NAME, actionName);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		try {
			createHelpBrokerIfNecessary();
			displayHelpBroker();
		} catch (Exception e) {
			LOG.fatal(e.getMessage());
		}
	}

	private void createHelpBrokerIfNecessary() throws Exception {
		//FIXME help desactived
//		if (helpBroker != null)
//			return;
//		brokerInitlock.lock();
//		try {
//			if (helpBroker != null)
//				return;
//			createHelpBroker();
//		} finally {
//			brokerInitlock.lock();
//		}
	}

	//FIXME help desactived
//	private void createHelpBroker() throws Exception {
//		HelpSet helpSet = createHelpSet();
//		helpBroker = helpSet.createHelpBroker();
//	}
//
//	private HelpSet createHelpSet() throws FileNotFoundException,
//			MalformedURLException, HelpSetException {
//		URL helpLocation = buildHelpBaseLocation();
//
//		URL[] helpLocationList = new URL[] { helpLocation };
//		ClassLoader classLoader = new URLClassLoader(helpLocationList);
//
//		String helpSetName = Messages.getString("HelpAction.helpsetName"); //$NON-NLS-1$
//		URL helpSetLocation = HelpSet.findHelpSet(classLoader, helpSetName);
//		HelpSet helpSet = new HelpSet(classLoader, helpSetLocation);
//		return helpSet;
//	}

	private URL buildHelpBaseLocation() throws FileNotFoundException,
			MalformedURLException {
		File tangaraRootDir = Configuration.instance().getTangaraPath()
				.getParentFile();
		String helpDirRelativePath = Messages.getString("HelpAction.directory"); //$NON-NLS-1$
		File helpDir = new File(tangaraRootDir, helpDirRelativePath);

		if (!helpDir.exists() || !helpDir.canRead())
			throw new FileNotFoundException("helpset directory does not exist");

		URL helpBaseURL = helpDir.toURI().toURL();
		return helpBaseURL;
	}

	/**
	 * Display the help broker
	 */
	private void displayHelpBroker() {
		//FIXME help desactived
//		helpBroker.setDisplayed(true);
	}
}
