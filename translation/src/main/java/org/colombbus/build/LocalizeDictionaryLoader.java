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

import java.io.*;
import java.util.*;

/**
 * Load {@link LocalizeDictionary} from the processor configuration options
 *
 * <pre>
 * Usage:
 * 	{@link LocalizeDictionaryLoader} loader = new {@link LocalizeDictionaryLoader}();
 * 	loader.{@link #setConfigurationDirectory(File)}
 * 	loader.{@link #setLogger(ProcessingLogger)}
 * 	loader.{@link #loadDictionaries()}
 * 	List&lt;{@link LocalizeDictionary}&gt; dictionaries = loader.{@link #getDictionaries()}
 * </pre>
 */
public class LocalizeDictionaryLoader {
	private static final String LANGUAGE_LIST_PROP = "packages.list"; //$NON-NLS-1$
	private static final String TRANSLATION_FILES_LIST_PROP = "translationFiles.list"; //$NON-NLS-1$
	private static final String LIST_SEPARATOR_PROP = "list.separator"; //$NON-NLS-1$

	private static final String PROPERTIES_FILENAME = "translation.properties"; //$NON-NLS-1$

	private ProcessingLogger pLogger;
	private List<LocalizeDictionary> dictionaries = new ArrayList<LocalizeDictionary>();
	private Properties configuration;
	private List<String> filenameList;
	private List<String> languageList;
	private File confDirectory;

	public void setLogger(ProcessingLogger logger) {
		this.pLogger = logger;
	}

	public void setConfigurationDirectory(File directory) {
		this.confDirectory = directory;
	}

	public void loadDictionaries() throws IOException {
		loadConfigurationFile();
		readConfiguration();
		buildDictionaries();
	}

	private void loadConfigurationFile() throws IOException {
		File configurationFile = new File(confDirectory, PROPERTIES_FILENAME);
		try {

			configuration = PropertiesUtils.load(configurationFile);

		} catch (FileNotFoundException notFoundEx) {
			pLogger.error("Could not find translation config file %s ", configurationFile.getAbsolutePath()); //$NON-NLS-1$
			throw notFoundEx;
		} catch (IOException ioEx) {
			pLogger.error("Error while reading translation config file %s", configurationFile.getAbsolutePath()); //$NON-NLS-1$
			throw ioEx;
		}
	}

	private void readConfiguration() {
		String separator = configuration.getProperty(LIST_SEPARATOR_PROP);

		String filesListProp = configuration.getProperty(TRANSLATION_FILES_LIST_PROP);
		filenameList = splitToStringList(filesListProp, separator);

		String languageListProp = configuration.getProperty(LANGUAGE_LIST_PROP);
		languageList = splitToStringList(languageListProp, separator);
	}

	private static List<String> splitToStringList(String string, String delim) {
		String[] strArray = string.split(delim);
		return Arrays.asList(strArray);
	}

	private void buildDictionaries() throws IOException {
		dictionaries = new ArrayList<LocalizeDictionary>();
		Iterator<String> languageIt = languageList.iterator();
		for (String filename : filenameList) {
			String language = languageIt.next();
			computeDictionary(language, filename);
		}
	}

	private void computeDictionary(String language, String fileName) throws IOException {
		File dictionaryFile = new File(confDirectory, fileName);
		Properties content = loadDictionaryFromFile(dictionaryFile);
		LocalizeDictionary dictionary = new LocalizeDictionary(language, content);
		dictionaries.add(dictionary);
	}

	private Properties loadDictionaryFromFile(File dictionaryFile) throws IOException {
		try {

			return PropertiesUtils.load(dictionaryFile);

		} catch (FileNotFoundException ex) {
			pLogger.error("Could not find dictionary file %s", dictionaryFile.getAbsolutePath()); //$NON-NLS-1$
			throw ex;
		} catch (IOException ioEx) {
			pLogger.error("Error while reading dictionery file %s", dictionaryFile.getAbsolutePath()); //$NON-NLS-1$
			throw ioEx;
		}
	}

	/**
	 * Get the loaded dictionaries
	 *
	 * @return a non <code>null</code> list of dictionaries
	 */
	public List<LocalizeDictionary> getDictionaries() {
		return dictionaries;
	}

}
