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

import java.awt.MediaTracker;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.bsf.BSFException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.colombbus.helpengine.DefaultHelpEngine;
import org.colombbus.helpengine.HelpEngine;
import org.colombbus.tangara.io.ScriptReader;
import org.colombbus.tangara.update.SoftwareUpdate;

/**
 * Contains the Main method of the program. <br>
 * It loads the log4j configuration, starts the program by creating the GUI and
 * displays it
 *
 * @author gwen
 */
public class Main {

	/** Defines the mode (normal or by a .tgr file) */
	static boolean programMode = false;
	/** The name of the .tgr file */
	static String programName;
	/** The frame of Tangara */
	private static TFrame frame;
	/** Class logger */
	private static Logger LOG;

	private static String mainTangaraFile;
	static String language;
	private static File tempDirectory;

	private static Main INSTANCE = new Main();

	private static int HELP_SERVER_PORT = 7777;
	private static HelpEngine helpEngine;
	private static final Lock initializationLock = new ReentrantLock();

	private final static String RESOURCES_DIRECTORY = "resources/";

	/**
	 * Gets the single instance of the program
	 *
	 * @return the program instance
	 */
	public static Main instance() {
		return INSTANCE;
	}

	/**
	 * Creates the GUI and shows it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		Class<?> typeClass = Program.class;
		try {
			try {
				String lang = Configuration.instance().getLanguage();
				String className = "org.colombbus.tangara." + lang + ".Program_" + lang;
				typeClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				// If the class in this language doesn't exist, we load the
				// English version of Program.
				typeClass = Class.forName("org.colombbus.tangara.en.Program_en");
			}
			Program.INSTANCE = (Program) typeClass.newInstance();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			exit();
		}

		// Forces the program initialization for error and output stream
		// initalization
		Program.init();
		LOG.info("Application starting..."); //$NON-NLS-1$
		// Try to set the System Look&Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			LOG.error("Unable to load native look and feel"); //$NON-NLS-1$
		}
		Configuration conf = Configuration.instance();
		// binds the system program
		Program programme = Program.instance();
		programme.setHistoryDepth(conf.getHistoryDepth());
		programme.setCurrentDirectory(conf.getUserHome());

		// bean permits to translate to Program the commands in the chosen
		// language.
		String beanName = Messages.getString("Main.bean.program");
		try {
			// We declare the bean for Program.
			Configuration.instance().getManager().declareBean(beanName, programme, typeClass);
		} catch (BSFException e) {
			LOG.error(e.getMessage(), e);
			exit();
		}

		Class<?> tools = Program.instance().getTranslatedClassForName("Tools");
		try {
			Tools a = (Tools) tools.newInstance();
			String toolsName = Messages.getString("Main.bean.tools");
			Configuration.instance().getManager().declareBean(toolsName, a, tools);
		} catch (Exception e) {
			LOG.error("Error while casting tools " + e);
		}

		// -------------------------------------------------------------------------------------------

		// In normal execution of Tangara, we use an EditorFrame
		// In jar file mode we use a ProgramFrame
		// programMode is false for normal mode
		if (!programMode) {
			// Creates the frame
			frame = new EditorFrame(conf,helpEngine);
			programme.setFrame(frame);
			// binds the game area and sets in which language will be the
			// GraphicsPane
			GraphicsPane graphicsPane = frame.getGraphicsPane();
			graphicsPane.declareBeanForTheScreen();

			// sets the shell for Program
			programme.setBSFEngine(conf.getEngine());

			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// allows to divide the main panel and makes the GUI ready
					frame.afterInit();
				}
			});
			checkForUpdate();

		} else { // .jar file mode
			frame = new FileOpenFrame();
			programme.setFrame(frame);

			// bind the game area
			GraphicsPane graphicsPane = frame.getGraphicsPane();
			graphicsPane.declareBeanForTheScreen();

			// sets the shell for Program and sets in which language will be the
			// GraphicsPane
			programme.setBSFEngine(conf.getEngine());

			executeProgram();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// allows to divide the main panel and makes the GUI ready
					frame.afterInit();
				}
			});
		}
	}

	public static void copyFilesInTempDirectory() {
		try {
			// Creation of a temp directory
			tempDirectory = FileUtils.createTempDirectory();
			Configuration conf = Configuration.instance();
			StringTokenizer resources = new StringTokenizer(conf.getProperty("program.resources"), ",");
			String resource = null;

			JarFile jarFile = new JarFile(conf.getTangaraPath());

			while (resources.hasMoreTokens()) {
				resource = resources.nextToken();
				ZipEntry entry = jarFile.getEntry(RESOURCES_DIRECTORY + resource);
				if (entry == null) {
					jarFile.close();
					throw new Exception("Resource '" + resource + "' not found");
				}
				BufferedInputStream input = new BufferedInputStream(jarFile.getInputStream(entry));
				File destinationFile = new File(tempDirectory, resource);
				destinationFile.createNewFile();
				FileUtils.copyFile(input, destinationFile);
			}
		} catch (Exception e) {
			LOG.error("error while copying program files: ", e);
		}
	}

	/**
	 * The main method of the software. <br>
	 * It loads the log4j configuration, determines the program mode and throws
	 * the program
	 *
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 */
	public static void main(String[] args) {
		// First: uninstall objects deleted during the previous execution of
		// Tangara
		uninstallObjects();

		// Second: load the configuration
		try {
			// creates a configuration object (initializing the tangara path (My
			// Documents) and the user home variables)
			// and loads the configuration component. (package, script, system,
			// path, BSF, language)
			Configuration.instance().load();
		} catch (ConfigurationException initEx) {
			System.err.println("Configuration initialization failed " + initEx);
			exit();
		}
		// create a configuration object (initializing the tangara path (My
		// Documents)and the user home variables)
		// and load the configuration (package, script, system, path, BSF,
		// language)

		// Third: initialize logging functionalities.
		Configuration.instance().configureLogging();
		LOG = Logger.getLogger(Main.class);
		LOG.info("Configuration loaded");

		initializeHelpEngine();

		if (Configuration.instance().isExecutionMode()) {
			mainTangaraFile = Configuration.instance().getProperty("main-program");
			System.out.println("-> Copying files");
			copyFilesInTempDirectory();
			programMode = true;
			programName = (new File(tempDirectory, mainTangaraFile)).getAbsolutePath();
		} else {
			programMode = false;
			if (args.length == 1) {
				File path = new File(args[0]);
				// Testing the parameter to determine the program mode
				if (path.exists() == false) {
					LOG.warn("parameter file " + path.getAbsolutePath() + " does not exist.");
				} else if (path.isFile() == false) {
					LOG.warn("parameter path " + path.getAbsolutePath() + " is not a file");
				} else if (path.canRead() == false) {
					LOG.warn("parameter path " + path.getAbsolutePath() + " cannot be read");
				} else if (FileUtils.isTangaraFile(path) == false) {
					LOG.warn("parameter path " + path.getAbsolutePath() + " is not a tangara file or a tangara object file");
				} else {
					programName = args[0];
					programMode = true;
				}
			}
		}

		if (!programMode) {
			if (checkForBase())
				launchGUI();
		} else {
			launchGUI();
		}
	}


	private static void initializeHelpEngine() {
		LOG.info("Initializing help engine");//$NON-NLS-1$

		helpEngine = new DefaultHelpEngine();
		helpEngine.setPort(HELP_SERVER_PORT);
		helpEngine.startup();

		LOG.info("Help engine initialized");//$NON-NLS-1$
	}


	public static void launchGUI() {
		if (!programMode)
			displaySplashScreen();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void checkForUpdate() {
		Configuration conf = Configuration.instance();
		// Check for updates if the feature is enabled in conf
		if (conf.getInteger("checkUpdate.activation") == 1) {
			boolean checkUpdate = false;
			GregorianCalendar today = new GregorianCalendar();
			GregorianCalendar lastLaunch = new GregorianCalendar();
			lastLaunch.setTimeInMillis(0);
			Properties properties = conf.loadUpdateProperties();

			// Read last Launch date
			if ((properties != null) && (properties.containsKey("checkUpdate.lastLaunch"))) {
				String strValue = properties.getProperty("checkUpdate.lastLaunch");
				long longValue = Long.parseLong(strValue);
				lastLaunch.setTimeInMillis(longValue);
			} else {
				LOG.warn("Failed to load lastLaunch property");
				properties = new Properties();
			}

			// Write last Launch date
			properties.setProperty("checkUpdate.lastLaunch", Long.toString(today.getTimeInMillis()));
			conf.saveUpdateProperties(properties);

			String intervalText = Configuration.instance().getProperty("checkUpdate.interval");
			if (intervalText.equals("ALWAYS")) {
				checkUpdate = true;
			} else if (intervalText.equals("WEEK")) {
				lastLaunch.add(GregorianCalendar.DAY_OF_MONTH, 7);
				checkUpdate = (today.after(lastLaunch));
			} else if (intervalText.equals("MONTH")) {
				lastLaunch.add(GregorianCalendar.MONTH, 1);
				checkUpdate = (today.after(lastLaunch));
			}
			if (checkUpdate) {
				SoftwareUpdate.launchVersionCheck();
			}
		}
	}

	private static void uninstallObjects() {
		String homePath = Configuration.instance().getTangaraPath().getParentFile().getAbsolutePath();
		File logFile = new File(homePath.replace("\\", "/") + "/objects/log.txt");
		if (logFile.exists()) {
			Reader fileReader = null;
			try {
				fileReader = new FileReader(logFile);
				BufferedReader reader = new BufferedReader(fileReader);
				String ligne = null;
				while ((ligne = reader.readLine()) != null) {
					if (!ligne.equals("")) {
						File jar = new File(homePath.replace("\\", "/") + "/objects/" + ligne + ".jar");
						jar.delete();
						File ressource = new File(homePath.replace("\\", "/") + "/objects/resources/" + ligne);
						HelpWindow.deleteDir(ressource);
					}
				}
				reader.close();
			} catch (Exception e) {
				System.err.println("Error while reading log file " + e);
			} finally {
				IOUtils.closeQuietly(fileReader);
			}
			logFile.delete();
		}
	}

	private static ImageIcon splashScreenImage;

	public static ImageIcon getSplashScreenImage() {
		return splashScreenImage;
	}

	/**
	 * Displays splash.png during the loading
	 */
	private static void displaySplashScreen() {
		URL url = SplashScreen.class.getResource("splash.png");
		splashScreenImage = new ImageIcon(url);
		// while the image is not on screen
		while (splashScreenImage.getImageLoadStatus() == MediaTracker.LOADING) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.debug("splash screen loading interrupted", e); //$NON-NLS-1$
			}
		}
		// should normally be run in the EDT, but launched at once in order to
		// display
		// the screen as soon as possible
		new SplashScreen(splashScreenImage, 3000);
	}

	/**
	 * Changes the currentDirectory and throws the program passed in parameters
	 * of main. <br>
	 * It throws only if there is one parameter in the main
	 */
	private static void executeProgram() {
		File programFile = null;
		try {
			if (programName == null)
				throw new Exception("programName = null");

			programFile = new File(programName);
			if (!programFile.exists()) {
				throw new Exception("File not found : " + programName);
			}
			Program.instance().setCurrentDirectory(programFile.getParentFile());

			String commands = loadScript(programFile);

			if (Configuration.instance().getProperty("quote.mode").equals("INTUITIVE")) {
				commands = StringParser.addQuoteDelimiters(commands);
			}
			Program.instance().executeScriptGetResult(commands);
		} catch (Throwable th) {
			LOG.error("Unable to execute program " + programName, th);
		}
	}

	/**
	 * Returns a boolean that represents the mode of the program
	 *
	 * @return the parameter programMode of Main class
	 */
	public static boolean isProgramMode() {
		return programMode;
	}

	private static String loadScript(File sourceFile) throws IOException {
		ScriptReader reader = new ScriptReader();
		return  reader.readScript(sourceFile);
	}

	/**
	 * Enables to quit the program
	 */
	public static void exit() {
		LOG.info("Shutdown help engine ");
		helpEngine.shutdown();
		LOG.info("Cleaning temp directories"); //$NON-NLS-1$
		FileUtils.clean();
		LOG.info("Exiting application"); //$NON-NLS-1$

		System.exit(0);
	}

	public static boolean checkForBase() {
		if (!Configuration.instance().baseExists()) {
			BaseMaker maker = new BaseMaker();
			maker.make();
			return false;
		}
		return true;
	}

}
