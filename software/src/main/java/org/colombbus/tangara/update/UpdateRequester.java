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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.colombbus.tangara.core.Version;

class UpdateRequester {

	private URL updateSite;

	public void setUpdateSite(String site) throws MalformedURLException {
		Validate.notNull(site, "site argument is null");//$NON-NLS-1$
		URI updateSiteURI = URI.create(site + "/update.properties");//$NON-NLS-1$
		this.updateSite = updateSiteURI.toURL();
	}

	public SoftwareUpdateInfo requestSoftwareInfo() throws IOException {
		URLConnection connect = updateSite.openConnection();

		Properties updateProp = new Properties();
		updateProp.load(connect.getInputStream());

		SoftwareUpdateInfo softwareInfo = extractSoftwareInfo(updateProp);
		return softwareInfo;
	}

	private static SoftwareUpdateInfo extractSoftwareInfo(Properties updateProp) {
		SoftwareUpdateInfo info = new SoftwareUpdateInfo();

		String versionTxt = updateProp.getProperty("version"); //$NON-NLS-1$
		Version version = new Version(versionTxt);
		info.setVersion(version);

		String link = updateProp.getProperty("link"); //$NON-NLS-1$
		info.setLink(link);

		String description = updateProp.getProperty("description"); //$NON-NLS-1$
		info.setDescription(description);

		return info;
	}

}
