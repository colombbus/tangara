/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008-2012 Colombbus (http://www.colombbus.org)
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
package org.colombbus.build;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A dictionary containing the localization information.
 *
 * <pre>
 * Localization information is the information used to generate the localized class from the localizable class.
 * </pre>
 */
public class LocalizeDictionary {

	private String language;
	private Map<String, String> localizedValues = new HashMap<String, String>();

	public LocalizeDictionary(String language, Properties props) {
		this.language = language;
		initLocalizedValues(props);
	}

	private void initLocalizedValues(Properties props) {
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			String standard = (String) entry.getKey();
			String localized = (String) entry.getValue();
			localizedValues.put(standard, localized);
		}
	}

	public String getLanguage() {
		return language;
	}

	/**
	 * Register a new 'translation' in the dictionary
	 *
	 * @param standard
	 *            the value in 'standard' language
	 * @param localized
	 *            the value in the localized language
	 */
	public void register(String standard, String localized) {
		localizedValues.put(standard, localized);
	}

	public boolean isLocalized(String standard) {
		return localizedValues.containsKey(standard);
	}

	public String localize(String standard) {
		return localizedValues.get(standard);
	}

	/**
	 * Check if a method is the default call among a set of methods with the
	 * same name.
	 *
	 * @param standard
	 *            the method identifier
	 * @return
	 */
	public boolean isDefaultCall(String standard) {
		String defaultCallStandard = standard + ".f"; //$NON-NLS-1$
		return localizedValues.containsKey(defaultCallStandard);
	}

	public boolean isCallUsageDefined(String standard) {
		String prototypeCallStandard = prototypeCallKey(standard);
		String simpleCallStandard = simpleCallKey(standard);
		return localizedValues.containsKey(prototypeCallStandard) && localizedValues.containsKey(simpleCallStandard);
	}

	private static String prototypeCallKey(String standard) {
		return standard + ".p"; //$NON-NLS-1$
	}

	private static String simpleCallKey(String standard) {
		return standard + ".v"; //$NON-NLS-1$
	}

	public String getSimpleCallCode(String standard) {
		String simpleCallStandard = simpleCallKey(standard);
		return localizedValues.get(simpleCallStandard);
	}

	public String getPrototypeCallCode(String standard) {
		String prototypeCallStandard = prototypeCallKey(standard);
		return localizedValues.get(prototypeCallStandard);
	}

}
