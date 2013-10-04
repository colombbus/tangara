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

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.bsf.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.colombbus.tangara.core.Version;
import org.colombbus.tangara.io.ScriptHeader;
import org.colombbus.tangara.io.ScriptHeaderImpl;

/**
 * Configuration of the application.
 * <p>
 * Use this singleton to access to the configuration of the application. Before
 * using it, call the {@link Configuration#load()} method to load the
 * configuration from files. The configuration is loaded in the following order:
 * <ol>
 * <li><code>org/colombbus/tangara/tangara.properties</code> file in the
 * classpath or the jar file</li>
 * <li>the file pointed by the java system property
 * <code>org.colombbus.tangaraConfiguration</code></li>
 * <li>the file <code>tangara.properties</code> contained in the same directory
 * of the jar file</li>
 * </ol>
 * </p>
 */
public class Configuration {

	private static final ScriptHeader SCRIPT_HEADER = new ScriptHeaderImpl(Charset.forName("UTF-8"), new Version("1.0"));

	/** Jar extension */
	private static final String JAR_FILE_EXT = "jar";

	/** Defines the user home path cf tangara.properties */
	private static final String USER_HOME_P = "user.home";//$NON-NLS-1$
	// userHome is in comment in tangara.properties

	/** Related to Mes Documents (pour le francais) in tangara.properties */
	private static final String USER_DIRNAME_P = "user.dir";//$NON-NLS-1$

	/** History depth for keeping track of commands */
	private static final String HISTORY_DEPTH_P = "history.depth";//$NON-NLS-1$

	/** Related to "bean-shell" in tangara.properties */
	private static final String SCRIPT_ENGINE_LIST_P = "scripting-engine-list";//$NON-NLS-1$

	/** Related to "BeanShell" in tangara.properties */
	private static final String DFLT_SCRIPT_ENGINE_P = "scripting-engine.default";//$NON-NLS-1$

	/** Packages list */
	private static final String IMPORT_PKG_LST = "org.colombbus.tangara.*, org.colombbus.tangara.net.*";
	private static final String IMPORT_PKG_LST_SEP = ",";//$NON-NLS-1$
	private static final String IMPORT_PKG_OBJECTS = "org.colombbus.tangara.objects.";

	// The import command for single package.
	// The position of the package name is identified by the tag defined by the
	// property
	// command.parameter.tag
	/** Related to "%package%" in tangara.properties */
	private static final String PARAMETER_TAG_P = "command.parameter.tag";//$NON-NLS-1$
	/** Related to "import %package% ;" in tangara.properties */
	private static final String IMPORT_PKG_CMD_P = "command.import-package";//$NON-NLS-1$

	private static final String ADD_CLASSPATH_CMD_P = "command.add-classpath";//$NON-NLS-1$

	/** The property referring to the log level */
	public static final String LOG_LEVEL_P = "log.level";//$NON-NLS-1$

	/** Related to "language" (fr) in tangara.properties */
	private static final String LANGUAGE_P = "language";

	/** Defines the default language "en" */
	public static final String DEFAULT_LANGUAGE = "en";

	private static final String fontPlain = "plain";
	private static final String fontBold = "bold";
	private static final String fontItalic = "italic";

	/** The property referring to the configuration file path */
	public static final String CONF_SYS_P = "org.colombbus.tangara.Configuration";//$NON-NLS-1$

	/** The name of the configuration file for fixed properties */
	public static final String FIXED_PROPERTIES_FILENAME = "fixed.properties"; //$NON-NLS-1$

	/** The name of the default configuration file for changeable parameters */
	public static final String DEFAULT_PROPERTIES_FILENAME = "default.properties"; //$NON-NLS-1$

	/** The name of the user configuration file */
	public static final String PROPERTIES_FILENAME = "tangara.properties"; //$NON-NLS-1$

	/** The name of the directory in user.home containing configuration file */
	public static final String PROPERTIES_DIRECTORY_NAME = "tangara";

	/** The name of the redirection file for user configuration */
	public static final String REDIRECT_PROPERTIES_FILENAME = "redirect.properties"; //$NON-NLS-1$

	/**
	 * The name of the file contained in an execution jar holding execution
	 * parameters
	 */
	public static final String EXECUTION_PROPERTIES_FILENAME = "org/colombbus/tangara/execution.properties"; //$NON-NLS-1$

	/** The name of the property holding the version number of the base file */
	public static final String BASE_VERSION_PROPERTY = "Tangara-version"; //$NON-NLS-1$

	/** The property referring to the path containing the configuration file */
	public static final String REDIRECT_PATH_P = "path";

	/** The default font */
	public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12); //$NON-NLS-1$

	public static final int LEVEL_BASIC = 0;
	public static final int LEVEL_ADVANCED = 1;

	private URLClassLoader objectsClassLoader;

	/** The user home directory */
	private File userHome;

	/** Path to tangara jar file or main class path */
	private File tangaraPath;

	/** The manager of BSF script engine */
	private BSFManager bsfManager = new BSFManager();

	/** The BSF script engine */
	private BSFEngine engine;

	/** List of the packages loaded during shell initialization */
	private String[] scriptImportPkgList = new String[0];

	/**
	 * Script language command used to import package
	 * <p>
	 * The command shall contain a tag that will be replaced by the name of the
	 * package.
	 * </p>
	 */
	private String importPackageCmd;

	/**
	 * Tag used in commands to show the place of the parameter
	 */
	private String parameterTag;

	private String addClassPathCmd;

	/** The name of the script engine */
	private String defaultEngineName;

	/** Changeable properties */
	private Properties properties = new Properties();

	/** Fixed properties */
	private Properties fixedProperties = new Properties();

	/** Determines if the language is the default language or not */
	private boolean defaultLanguage = false;

	/** Path to the base **/
	private File basePath = null;

	/** Class logger */
	private static Logger LOG = null;

	private boolean executionMode = false;

	/** Singleton instance */
	private static final Configuration instance = new Configuration();

	/**
	 * Gets the single instance of the configuration
	 *
	 * @return the configuration instance
	 */
	public static Configuration instance() {
		return instance;
	}

	/**
	 * Creates a new configuration instance.
	 * <p>
	 * Initializes the tangara path and the user home variables.
	 * </p>
	 */
	private Configuration() {
		super();
		try {
			tangaraPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (Exception ex) {
			System.err.println("Error when trying to get the path to tangara.jar : " + ex);
		}

		userHome = new File(System.getProperty("user.dir"));
	}

	/**
	 * Cheks if the system is windows.
	 *
	 * @return true if the system is windows.
	 */
	public static boolean isOsWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	/**
	 * Gets the value of a property
	 *
	 * @param name
	 *            name of the property
	 * @return the property value or <code>null</code> if the property does not
	 *         exist
	 */
	public String getProperty(String name) {
		if (properties.containsKey(name))
			return properties.getProperty(name);
		else if (fixedProperties.containsKey(name))
			return fixedProperties.getProperty(name);
		displayError("Could not find property " + name);
		return null;
	}

	private void displayError(String message) {
		if (LOG != null) {
			LOG.error(message);
		} else {
			System.out.println(message);
		}
	}

	/**
	 * Gets the urlClassLoader that enables to access to the objects jar files.
	 *
	 * @return an URLClassLoader
	 */
	public URLClassLoader getObjectsClassLoader() {
		if (objectsClassLoader == null) {
			try {
				if (isExecutionMode()) {
					URL[] listUrl = new URL[1];
					listUrl[0] = instance.getTangaraPath().toURI().toURL();
					objectsClassLoader = new URLClassLoader(listUrl);
				} else {
					File f = new File(instance.getTangaraPath().getParentFile(), "objects");
					File[] list = f.listFiles();
					Vector<URL> vector = new Vector<URL>();
					for (int i = 0; i < list.length; i++) {
						if (list[i].getName().endsWith(".jar"))
							vector.add(list[i].toURI().toURL());
					}
					File flib = new File(instance.getTangaraPath().getParentFile().getAbsolutePath().replace("\\", "/") + "/objects/lib");
					File[] listflib = flib.listFiles();
					for (int j = 0; j < listflib.length; j++) {
						if (listflib[j].getName().endsWith(".jar"))
							vector.add(listflib[j].toURI().toURL());
					}
					URL[] listUrl = new URL[vector.size()];
					for (int j = 0; j < vector.size(); j++)
						listUrl[j] = vector.get(j);
					objectsClassLoader = new URLClassLoader(listUrl);
				}
			} catch (Exception e1) {
				displayError("URL MAL FORMED " + e1);
				return null;
			}
		}
		return objectsClassLoader;

	}

	/**
	 * Loads the configuration
	 *
	 * @throws ConfigurationException
	 */
	public void load() throws ConfigurationException {
		System.out.println("Loading configuration..."); //$NON-NLS-1$
		properties.clear();
		fixedProperties.clear();

		System.out.println("-> loading default properties");
		loadDefaultCfg();
		System.out.println("-> loading fixed properties");
		loadFixedCfg();
		System.out.println("-> loading system properties");
		loadSystemCfgFile();
		System.out.println("-> loading local properties");
		loadLocalCfgFile();
		// Try to define userHome from the config file
		System.out.println("-> setting user home");
		userHome = new File(System.getProperty("user.home"));
		String confHome = getProperty(USER_HOME_P);
		if ((confHome != null) && !confHome.equals(""))
			userHome = new File(confHome);
		String myDocString = getProperty(USER_DIRNAME_P);
		if ((myDocString != null) && (!myDocString.equals(""))) {
			File myDocDir = new File(userHome, myDocString);
			if (myDocDir.exists())
				userHome = myDocDir;
		}
		// Check if we are in execution mode
		System.out.println("-> checking execution mode");
		testExecutionMode();
		System.out.println("-> loading scripting languages");
		loadScriptingLanguages(fixedProperties);
		System.out.println("-> langage " + getLanguage() + " is used");
		defaultLanguage = (getLanguage().compareTo(getDefaultLanguage()) == 0);
		System.out.println("-> loading localized messages");
		// Load localized messages
		try {
			// loads the language
			Messages.loadLocalizedResource(getLanguage());
			org.colombbus.tangara.net.Messages.loadLocalizedResource(getLanguage());
		} catch (Throwable th) {
			System.err.println("error while loading language configuration: " + th);
		}
		System.out.println("-> loading package info");
		loadPackageInfo(fixedProperties);
		System.out.println("-> loading engine");
		loadEngine(defaultEngineName);
		System.out.println("-> loading objects");
		loadObjects();
		System.out.println("Configuration loaded"); //$NON-NLS-1$
	}

	private void loadObjects() throws ConfigurationException {
		if (!isExecutionMode()) {
			// Import objects from file system
			File objectsDirectory = new File(tangaraPath.getParentFile(), "objects");
			System.out.println("using path " + objectsDirectory.getAbsolutePath());

			// 1st Load libraries
			File libDirectory = new File(objectsDirectory, "lib");
			File libraries[] = libDirectory.listFiles();
			for (int i = 0; i < libraries.length; i++) {
				if (libraries[i].getName().endsWith(".jar")) {
					System.out.println("Loading library " + libraries[i].getName());
					addClassPathToScriptEngine(libraries[i]);
				}
			}

			// 2nd load objects
			File objects[] = objectsDirectory.listFiles();
			for (int i = 0; i < objects.length; i++) {
				if (objects[i].getName().endsWith(".jar")) {
					System.out.println("Loading object " + objects[i].getName());
					addClassPathToScriptEngine(objects[i]);
				}
			}
		}
		// import the localized objects package
		importLocalizedObjectsPackage();
	}

	/**
	 * Returns the properties attribute.
	 *
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Sets the current properties
	 *
	 * @param properties
	 *            the new properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Loads the default properties from the current jar file
	 *
	 * @throws ConfigurationException
	 *             the properties cannot be loaded
	 */
	public void loadDefaultCfg() throws ConfigurationException {
		InputStream inStream = null;
		try {
			inStream = getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILENAME);
			properties.load(inStream);
		} catch (IOException ioEx) {
			String errMsg = "Failed to load default configuration "; //$NON-NLS-1$
			System.err.println(errMsg + ioEx);
			throw new ConfigurationException(errMsg, ioEx);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
	}

	/**
	 * Loads the default properties from the current jar file
	 *
	 * @throws ConfigurationException
	 *             the properties cannot be loaded
	 */
	public void loadFixedCfg() throws ConfigurationException {
		InputStream inStream = null;
		try {
			inStream = getClass().getResourceAsStream(FIXED_PROPERTIES_FILENAME);
			fixedProperties.load(inStream);
		} catch (Throwable th) {
			String errMsg = "Failed to load default configuration "; //$NON-NLS-1$
			System.err.println(errMsg + th);
			throw new ConfigurationException(errMsg, th);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
	}

	/**
	 * Loads a default file, located in the same directory as the JAR file 1st
	 * we look for a valid local redirection file (in
	 * user.home/PROPERTIES_DIRECTORY_NAME) 2nd we look for a valid redirection
	 * file located with tangara binary 2nd we look for a local config file 4th
	 * we look for a config file located with tangara binary
	 */
	public void loadLocalCfgFile() {
		File propertiesDirectory, binaryDirectory, configFile;
		// Initialize directories
		propertiesDirectory = new File(System.getProperty("user.home"), PROPERTIES_DIRECTORY_NAME);
		binaryDirectory = getTangaraPath().getParentFile();
		configFile = null;

		// 1st look for a local redirection file
		if (propertiesDirectory.exists()) {
			File configDirectory = getRedirectionPath(new File(propertiesDirectory, REDIRECT_PROPERTIES_FILENAME));
			if (configDirectory != null) {
				// we could find a redirection path: test if there is a config
				// file there
				File testFile = new File(configDirectory, PROPERTIES_FILENAME);
				if (testFile.exists()) {
					// we could find a config file: set configFile accordingly
					System.out.println("Reading configuration from path: '" + configDirectory.getAbsolutePath() + "'");
					configFile = testFile;
				}
			}
		}

		// 2nd look for a valid redirection file located with tangara binary
		if (configFile == null) {
			File configDirectory = getRedirectionPath(new File(binaryDirectory, REDIRECT_PROPERTIES_FILENAME));
			if (configDirectory != null) {
				// we could find a redirection path: test if there is a config
				// file there
				File testFile = new File(configDirectory, PROPERTIES_FILENAME);
				if (testFile.exists()) {
					// we could find a config file: set configFile accordingly
					System.out.println("Reading configuration from path: '" + configDirectory.getAbsolutePath() + "'");
					configFile = testFile;
				}
			}
		}

		// 3dr look for a local config file
		if (configFile == null) {
			File testFile = new File(propertiesDirectory, PROPERTIES_FILENAME);
			if (testFile.exists()) {
				// we could find a config file: set configFile accordingly
				System.out.println("Reading configuration from path: '" + propertiesDirectory.getAbsolutePath() + "'");
				configFile = testFile;
			}
		}

		// 4th look for a config file located with tangara binary
		if (configFile == null) {
			File testFile = new File(binaryDirectory, PROPERTIES_FILENAME);
			if (testFile.exists()) {
				// we could find a config file: set configFile accordingly
				System.out.println("Reading configuration from path: '" + binaryDirectory.getAbsolutePath() + "'");
				configFile = testFile;
			}
		}

		// Finally read config file
		if (configFile != null) {
			InputStream configStream = null;
			try {
				configStream = new FileInputStream(configFile);
				properties.load(configStream);
			} catch (FileNotFoundException ex) {
				System.err.println("Could not find configuration file '" + configFile.getAbsolutePath() + "'\n" + ex);
			} catch (IOException ioEx) {
				System.err.println("Failed to load configuration file '" + configFile.getAbsolutePath() + "'\n" + ioEx);
			} finally {
				IOUtils.closeQuietly(configStream);
			}
		}
	}

	private File getRedirectionPath(File redirectFile) {
		File path = null;
		if (redirectFile.exists()) {
			InputStream redirectStream = null;
			try {
				Properties redirectProperties = new Properties();
				redirectStream = new FileInputStream(redirectFile);
				redirectProperties.load(redirectStream);
				if (redirectProperties.containsKey(REDIRECT_PATH_P)) {
					path = new File(redirectProperties.getProperty(REDIRECT_PATH_P));
				}
			} catch (Exception e) {
				System.err.println("Error while reading redirect file '" + redirectFile.getAbsolutePath() + "'\n" + e);
			} finally {
				IOUtils.closeQuietly(redirectStream);
			}
		}
		return path;
	}

	/**
	 * Loads the configuration file declared in the system property
	 * {@link #CONF_SYS_P} (org.colombbus.tangara.Configuration)
	 */
	private void loadSystemCfgFile() {
		String globalProp = System.getProperty(CONF_SYS_P, null);
		if (globalProp != null) {
			InputStream configStream = null;
			try {
				configStream = new FileInputStream(globalProp);
				properties.load(configStream);
			} catch (FileNotFoundException ex) {
				String msg = String.format("Could not find configuration file %s ", globalProp);//$NON-NLS-1$
				System.err.println(msg + ex);
			} catch (IOException ioEx) {
				String errMsg = "Failed to load configuration file " + globalProp + " "; //$NON-NLS-1$
				System.err.println(errMsg + ioEx);
			} finally {
				IOUtils.closeQuietly(configStream);
			}
		}
	}

	public static String ext;

	/**
	 * Loads the declarations of the script engines declared in a property set
	 *
	 * @param props
	 *            property set containing the configuration
	 * @throws ConfigurationException
	 *             if the scripting languages cannot be loaded
	 */
	private void loadScriptingLanguages(Properties props) throws ConfigurationException {
		String strEngineList = loadProperty(props, SCRIPT_ENGINE_LIST_P);

		for (StringTokenizer engineTokenizer = new StringTokenizer(strEngineList); engineTokenizer.hasMoreTokens();) {
			String engineBaseItem = engineTokenizer.nextToken();
			String engineName = props.getProperty(engineBaseItem + ".name"); //$NON-NLS-1$
			String engineClass = props.getProperty(engineBaseItem + ".class"); //$NON-NLS-1$
			String engineExtProps = props.getProperty(engineBaseItem + ".extensions"); //$NON-NLS-1$
			String[] extArray = null;
			if (engineExtProps != null) {
				List<String> extList = new ArrayList<String>();
				for (StringTokenizer extTokenizer = new StringTokenizer(engineExtProps); extTokenizer.hasMoreTokens();) {
					String extension = extTokenizer.nextToken();
					ext = extension;
					extList.add(extension);
				}
				extArray = extList.toArray(new String[0]);
			}
			BSFManager.registerScriptingEngine(engineName, engineClass, extArray);
			System.out.println("Script " + engineName + " loaded"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		defaultEngineName = loadProperty(props, DFLT_SCRIPT_ENGINE_P);
	}

	/**
	 * Loads the package information from configuration file and builds the list
	 * of the package to load by default
	 *
	 * @param props
	 *            the property set containing the package information
	 */
	private void loadPackageInfo(Properties props) throws ConfigurationException {
		importPackageCmd = loadProperty(props, IMPORT_PKG_CMD_P);
		addClassPathCmd = loadProperty(props, ADD_CLASSPATH_CMD_P);
		parameterTag = loadProperty(props, PARAMETER_TAG_P);

		List<String> pkgList = new ArrayList<String>();
		for (StringTokenizer pkgTokenizer = new StringTokenizer(IMPORT_PKG_LST, IMPORT_PKG_LST_SEP); pkgTokenizer.hasMoreTokens();) {
			String packageName = pkgTokenizer.nextToken();
			pkgList.add(packageName);
		}
		if (defaultLanguage)
			pkgList.add("org.colombbus.tangara.en.*");
		else
			pkgList.add("org.colombbus.tangara." + getLanguage() + ".*");
		scriptImportPkgList = pkgList.toArray(scriptImportPkgList);
	}

	/**
	 * Loads a property and throws an exception if it cannot be found
	 *
	 * @param props
	 *            property set containing the property to load
	 * @param name
	 *            name of the property to get
	 * @return the value of the property <code>name</code>
	 * @throws ConfigurationException
	 *             if the property cannot be found
	 */
	private static String loadProperty(Properties props, String name) throws ConfigurationException {
		String value = props.getProperty(name, null);
		if (value == null) {
			String msg = String.format("Property %s cannot be found", name);//$NON-NLS-1$
			System.err.println(msg);
			throw new ConfigurationException(msg);
		}
		return value;
	}

	/**
	 * Gets the value of a property in a string format
	 *
	 * @param name
	 *            name of the property
	 * @param defaultValue
	 *            the value to return if the property cannot be found
	 * @return the value of the property, or <code>defaultValue</code> if the
	 *         property is not found.
	 */
	public String getString(String name, String defaultValue) {
		String strValue = getProperty(name);
		if (strValue == null)
			return defaultValue;
		else
			return strValue;
	}

	/**
	 * Gets the value of a property in a string format
	 *
	 * @param name
	 *            name of the property
	 * @return the value of the property, or <code>null</code> if the property
	 *         is not found.
	 */
	public String getString(String name) {
		return getProperty(name);
	}

	/**
	 * Gets the value of a property in an integer format
	 *
	 * @param defaultValue
	 *            the value to return if the property cannot be found
	 * @return the value of the property, or <code>defaultValue</code> if the
	 *         property is not found.
	 */
	public int getInteger(String property, int defaultValue) {
		int intValue = defaultValue;
		try {
			String strValue = getProperty(property);
			intValue = Integer.parseInt(strValue);
		} catch (Exception ex) {
			displayError("Failed to load integer property " + property);
		}
		return intValue;
	}

	/**
	 * Gets the value of a property in an integer format
	 *
	 * @param property
	 *            name of the property
	 * @return the value of the property, or {@link Integer#MIN_VALUE} if the
	 *         property is not found.
	 */
	public int getInteger(String property) {
		return getInteger(property, Integer.MIN_VALUE);
	}

	/**
	 * Gets the value of a property in a long format
	 *
	 * @param defaultValue
	 *            the value to return if the property cannot be found
	 * @return the value of the property, or <code>defaultValue</code> if the
	 *         property is not found.
	 */
	public long getLong(String property, long defaultValue) {
		long longValue = defaultValue;
		try {
			String strValue = getProperty(property);
			longValue = Long.parseLong(strValue);
		} catch (Exception ex) {
			displayError("Failed to load long property " + property);
		}
		return longValue;
	}

	/**
	 * Gets the value of a property in a long format
	 *
	 * @param property
	 *            name of the property
	 * @return the value of the property, or {@link Long#MIN_VALUE} if the
	 *         property is not found.
	 */
	public long getLong(String property) {
		return getLong(property, Long.MIN_VALUE);
	}

	/**
	 * Gets the value of a property in a float format
	 *
	 * @param property
	 *            name of the property
	 * @param defaultValue
	 *            the value to return if the property cannot be found
	 * @return the value of the property, or <code>defaultValue</code> if the
	 *         property is not found.
	 */
	public float getFloat(String property, float defaultValue) {
		float floatValue = defaultValue;
		try {
			String strValue = getProperty(property);
			floatValue = Float.parseFloat(strValue);
		} catch (Exception ex) {
			displayError("Failed to load float property " + property);
		}
		return floatValue;
	}

	/**
	 * Gets the value of a property in a long format
	 *
	 * @param property
	 *            name of the property
	 * @return the value of the property, or {@link Float#MIN_VALUE} if the
	 *         property is not found.
	 */
	public float getFloat(String property) {
		return getFloat(property, Float.MIN_VALUE);
	}

	/**
	 * Gets the value of a property in a double format
	 *
	 * @param property
	 *            name of the property
	 * @param defaultValue
	 *            the value to return if the property cannot be found
	 * @return the value of the property, or <code>defaultValue</code> if the
	 *         property is not found.
	 */
	public double getDouble(String property, double defaultValue) {
		double doubleValue = defaultValue;
		try {
			String value = getProperty(property);
			return Double.parseDouble(value);
		} catch (Exception ex) {
			displayError("Failed to load double property " + property);
		}
		return doubleValue;
	}

	/**
	 * Gets the value of a property in a long format
	 *
	 * @param property
	 *            name of the property
	 * @return the value of the property, or {@link Double#MIN_VALUE} if the
	 *         property is not found.
	 */
	public double getDouble(String property) {
		return getDouble(property, Double.MIN_VALUE);
	}

	/**
	 * Loads the BSF engine.
	 *
	 * @param engineName
	 *            name of the engine
	 *
	 * @see #getEngine()
	 */
	private void loadEngine(String engineName) throws ConfigurationException {
		System.out.println("Loading script engine " + engineName);
		try {
			engine = bsfManager.loadScriptingEngine(engineName.trim());
		} catch (BSFException bsfEx) {
			String msg = "Could not find script engine " + engineName;//$NON-NLS-1$
			System.err.println(msg + " " + bsfEx);
			throw new ConfigurationException(msg, bsfEx);
		}

		// import the initial packages
		for (String pkgName : scriptImportPkgList) {
			System.out.println("Loading package " + pkgName);
			importPkgToScriptEngine(pkgName);
		}
		System.out.println("Script engine " + engineName + " loaded");
	}

	/**
	 * Imports a package into the script engine
	 *
	 * @param pkgName
	 *            the name of the package
	 * @throws ConfigurationException
	 *             if the package cannot be imported
	 */
	private void importPkgToScriptEngine(String pkgName) throws ConfigurationException {
		try {
			StringBuilder cmd = new StringBuilder(importPackageCmd);
			int tagStartPos = cmd.indexOf(parameterTag);
			int tageEndPos = tagStartPos + parameterTag.length();
			cmd.replace(tagStartPos, tageEndPos, pkgName);
			// System.out.println("cmd " + cmd.toString());
			engine.eval("load-packages", 1, 1, cmd.toString()); //$NON-NLS-1$
		} catch (Exception ex) {
			String msg = String.format("Failed to import package %s", pkgName); //$NON-NLS-1$
			System.err.println(msg + " " + ex);
			throw new ConfigurationException(msg, ex);
		}
	}

	private void addClassPathToScriptEngine(File jarFile) throws ConfigurationException {
		try {
			StringBuilder cmd = new StringBuilder(addClassPathCmd);
			int tagStartPos = cmd.indexOf(parameterTag);
			int tageEndPos = tagStartPos + parameterTag.length();
			cmd.replace(tagStartPos, tageEndPos, jarFile.getAbsolutePath().replace("\\", "/"));
			// System.out.println("cmd " + cmd.toString());
			engine.eval("add-classpath", 1, 1, cmd.toString()); //$NON-NLS-1$
		} catch (Exception ex) {
			String msg = String.format("Failed to load class path %s", jarFile.getName()); //$NON-NLS-1$
			System.err.println(msg + " " + ex);
			throw new ConfigurationException(msg, ex);
		}
	}

	/**
	 * Get the engine currently loaded
	 *
	 * @return the BSF engine
	 */
	public BSFEngine getEngine() {
		return engine;
	}

	/**
	 * Get the BSF manager
	 *
	 * @return a BSF manager
	 */
	public BSFManager getManager() {
		return bsfManager;
	}

	/**
	 * Get a font from the configuration
	 *
	 * @param prefix
	 *            property prefix
	 * @param defaultValue
	 *            default font used if an error occurs
	 * @return the font associated to the property <code>prefix</code>, or
	 *         <code>defaultValue</code> if no font is found or if an error
	 *         occurs.
	 */
	public Font getFont(String key, Font defaultValue) {
		String fontDesc = null;
		try {
			fontDesc = getProperty(key);
		} catch (MissingResourceException e) {
			LOG.warn("Could not find font resource " + key);//$NON-NLS-1$
		}
		if (fontDesc != null) {
			fontDesc = fontDesc.trim();
		}
		return Font.decode(fontDesc);
	}

	/**
	 * Get a font from the configuration
	 *
	 * @param prefix
	 *            the prefix of the property key declaring a font
	 *
	 * @return the font associated to the properties starting with
	 *         <code>prefix</code>
	 */
	public Font getFont(String prefix) {
		return getFont(prefix, DEFAULT_FONT);
	}

	/** List of the font styles */
	private static final Map<String, Integer> FONT_STYLE = new Hashtable<String, Integer>();
	static {
		FONT_STYLE.put(fontPlain, Font.PLAIN);
		FONT_STYLE.put(fontBold, Font.BOLD);
		FONT_STYLE.put(fontItalic, Font.ITALIC);
	}

	public Color getColor(String property) {
		return getColor(property, Color.black);
	}

	/**
	 * A color is a set of 3 values separated by spaces
	 *
	 * @param property
	 *            the property key associated to the color
	 * @param defaultValue
	 *            the color to return if the property key is not associated to a
	 *            color
	 * @return the color associated to the property key, or
	 *         <code>defaultValue</code> if there is no color associated to the
	 *         property
	 */
	public Color getColor(String property, Color defaultValue) {
		// TODO enhance error handling
		Color color = defaultValue;
		String colorValue = getProperty(property);
		if (colorValue == null) {
			displayError("Property " + property + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			color = null;
			try {
				color = TColor.translateColor(colorValue, null);
			} catch (Exception e) {
			}
			if (color == null) {
				try {
					StringTokenizer tokenizer = new StringTokenizer(colorValue);
					int red = Integer.parseInt(tokenizer.nextToken());
					int green = Integer.parseInt(tokenizer.nextToken());
					int blue = Integer.parseInt(tokenizer.nextToken());
					color = new Color(red, green, blue);
				} catch (Exception ex) {
					displayError("Failed loading color " + property); //$NON-NLS-1$
				}
			}
		}
		return color;
	}

	/**
	 * Get the command history depth
	 *
	 * @return the command history depth
	 */
	public int getHistoryDepth() {
		return getInteger(HISTORY_DEPTH_P);
	}

	/**
	 * Get the user home directory path
	 *
	 * @return a path
	 */
	public File getUserHome() {
		return userHome;
	}

	/**
	 * Get the path to the tangara JAR file
	 *
	 * @return the path to the
	 */
	public File getTangaraPath() {
		return tangaraPath;
	}

	/**
	 * Check if the Tangara application is executed from a jar file.
	 *
	 * @return <code>true</code> if the application is executed from a jar file,
	 *         <code>false</code> otherwise.
	 */
	public boolean isExecutedFromJAR() {
		return FileUtils.isExtension(getTangaraPath(), JAR_FILE_EXT);
	}

	private boolean testExecutionMode() {
		executionMode = false;
		JarFile file = null;
		try {
			file = new JarFile(getTangaraPath());
			ZipEntry entry = file.getEntry(EXECUTION_PROPERTIES_FILENAME);
			if (entry != null) {
				executionMode = true;
				System.out.println("execution mode detected");
				Properties executionProperties = new Properties();
				InputStream ips = ClassLoader.getSystemResourceAsStream(EXECUTION_PROPERTIES_FILENAME);
				executionProperties.load(ips);
				if (executionProperties.containsKey("main-program")) {
					String mainTangaraFile = executionProperties.getProperty("main-program");
					System.out.println("main tangara file: " + mainTangaraFile);
					properties.setProperty("main-program", mainTangaraFile);
				} else {
					System.err.println("error : main program not specified");
				}
				if (executionProperties.containsKey("language")) {
					String language = executionProperties.getProperty("language");
					properties.setProperty("language", language);
					System.out.println("language: " + language);
				} else {
					System.err.println("error : language not specified");
				}
				if (executionProperties.containsKey("resources")) {
					String resources = executionProperties.getProperty("resources");
					properties.setProperty("program.resources", resources);
					System.out.println("resources: " + resources);
				} else {
					System.err.println("error : resources not specified");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					System.err.println("error while closing tangara JAR file");
				}
			}
		}
		return executionMode;
	}

	public boolean isExecutionMode() {
		return executionMode;
	}

	/**
	 * Associate the *.tgr files to the Tangara application
	 */
	public void registerTangaraFileType() {
		if (isOsWindows()) {
			File tangaraPath = getTangaraPath();
			WindowsConfiguration.registerTgrFiles(tangaraPath);
		}
	}

	/**
	 * Gets the configured language
	 *
	 * @return A string that determines the language
	 */
	public String getLanguage() {
		return getProperty(LANGUAGE_P);
	}

	/**
	 * Gets the default language
	 *
	 * @return the default string "en"
	 */
	public String getDefaultLanguage() {
		return DEFAULT_LANGUAGE;
	}

	/**
	 * Gets the comparator between the default language and the configured one
	 *
	 * @return A boolean that compares default language and the configured one
	 */
	public boolean defaultLanguage() {
		return defaultLanguage;
	}

	/**
	 * Determines the objects package according to the language. <br>
	 * org.colombbus.tangara.objects.* for default language. <br>
	 * org.colombbus.tangara.objects.fr.* for french. <br>
	 * etc ...
	 *
	 * @throws ConfigurationException
	 */
	public void importLocalizedObjectsPackage() throws ConfigurationException {
		String lang = getLanguage();
		String objectsPackage = IMPORT_PKG_OBJECTS;
		if (lang.equals(getDefaultLanguage())) {
			// it will be org.colombbus.tangara.objects.en.*
			objectsPackage += "en.*";
		} else {
			// it will be org.colombbus.tangara.objects.#language.*
			objectsPackage += lang + ".*";
		}
		importPkgToScriptEngine(objectsPackage);
	}

	/**
	 * Get the logging level from the configuration files
	 *
	 * @return the log level
	 * @throws ConfigurationException
	 */
	public String getLogLevel() {
		return getProperty(LOG_LEVEL_P);
	}

	/**
	 * Loads the configuration for log4j from the file log4j.properties
	 */
	public void configureLogging() {
		// BasicConfigurator is used to quickly configure the package log4j
		// Add a ConsoleAppender that uses PatternLayout using the
		// PatternLayout.TTCC_CONVERSION_PATTERN
		// and prints to System.out to the root category.
		BasicConfigurator.configure();
		// Configure log4j by the url : log4j.properties.
		String fileName = "log4j_" + Configuration.instance().getLogLevel() + ".properties";
		System.out.println("Loading logging configuration file: " + fileName);
		URL url = Main.class.getResource(fileName);
		if (url == null) {
			System.out.println("Logging configuration file not found - loading file: log4j_off.properties");
			url = Main.class.getResource("log4j_off.properties"); //$NON-NLS-1$
			if (url == null) {
				System.err.println("No logging configuration found");
				return;
			}
		}
		// PropertyConfigurator allows the configuration of log4j from an
		// external file
		// It will read configuration options from URL url.
		PropertyConfigurator.configure(url);
		LOG = Logger.getLogger(Configuration.class);
	}

	public ScriptHeader getScriptHeader() {
		return SCRIPT_HEADER;
	}

	public Properties loadUpdateProperties() {
		File propertiesDirectory;
		Properties updateProperties = new Properties();
		// First we look at local configuration directory
		propertiesDirectory = new File(System.getProperty("user.home"), PROPERTIES_DIRECTORY_NAME);
		if (!propertiesDirectory.exists()) {
			// Second we look at tangara binary path
			propertiesDirectory = getTangaraPath().getParentFile();
		}
		BufferedInputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(new File(propertiesDirectory, getProperty("checkUpdate.fileName"))));
			updateProperties.load(input);
			input.close();
			return updateProperties;
		} catch (IOException e) {
			LOG.warn("Error trying to load update properties");
		} finally {
			IOUtils.closeQuietly(input);
		}
		return null;
	}

	public void saveUpdateProperties(Properties updateProperties) {
		File propertiesDirectory;
		propertiesDirectory = new File(System.getProperty("user.home"), PROPERTIES_DIRECTORY_NAME);
		if (!propertiesDirectory.exists()) {
			propertiesDirectory.mkdir();
		}
		BufferedOutputStream outStream = null;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(new File(propertiesDirectory, getProperty("checkUpdate.fileName"))));
			updateProperties.store(outStream, "Tangara update properties");
		} catch (Exception ex) {
			LOG.error("Failed to write lastlaunch property", ex);
		} finally {
			IOUtils.closeQuietly(outStream);
		}
	}

	private File getLocalBasePath() {
		File propertiesDirectory = new File(System.getProperty("user.home"), PROPERTIES_DIRECTORY_NAME);
		File baseFile = new File(propertiesDirectory, getProperty("base.fileName"));
		return baseFile;
	}

	private File getBinaryBasePath() {
		File binaryDirectory = getTangaraPath().getParentFile();
		File baseFile = new File(binaryDirectory, getProperty("base.fileName"));
		return baseFile;
	}

	private boolean isValidBase(File basePath) {
		if (basePath.exists()) {
			String baseVersion = JarUtils.getManifestProperty(basePath, BASE_VERSION_PROPERTY);
			if ((baseVersion != null) && (baseVersion.compareTo(getString("tangara.version")) == 0)) {
				return true;
			}
		}
		return false;
	}

	private void initBasePath() {
		// First we look at local configuration directory
		File baseFile = getLocalBasePath();
		if (!baseFile.exists()) {
			// file does not exist, check if base exists in binary path
			File baseFile2 = getBinaryBasePath();
			if (isValidBase(baseFile2)) {
				// base was found and base is valid: we use this one
				baseFile = baseFile2;
			}
		}
		this.basePath = baseFile;
	}

	public File getBasePath() {
		if (basePath == null) {
			initBasePath();
		}
		return basePath;
	}

	public boolean baseExists() {
		File basePath = getBasePath();
		return isValidBase(basePath);
	}

}