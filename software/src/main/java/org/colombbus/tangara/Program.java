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

import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.bsf.BSFEngine;
import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.io.ScriptReader;

/**
 * This class manages the shell BSFEngine and throws the execution of the
 * commands.
 *
 * @author gwen
 */
@Localize(value="Program")
public class Program {

	/** Tangara shell */
	private BSFEngine shell;

	/** Tangara frame */
	private TFrame frame;

	// error streams
	private ByteArrayOutputStream errContent;

	private PrintStream execErrorStream;

	private PrintStream prevErr;

	// output streams
	private ByteArrayOutputStream outContent;

	private PrintStream execOutStream;

	private PrintStream prevOut;

	private boolean systemStreamCachingEnable = false;

	private StringParser parser;

	private Vector<HistoryRecord> history;

	private List<TObject> nonGraphicalObjects;

	public Collection<TObject> getTObjectsVector()
	{
		return nonGraphicalObjects;
	}

	private Vector<TWindow> windows;

	private int historyDepth = DEFAULT_HISTORY_DEPTH;

	/** default : no limit */
	private static final int DEFAULT_HISTORY_DEPTH = -1;

	private File currentDirectory;

	/** Class logger */
	private static Logger LOG = Logger.getLogger(Program.class);

	private Pause pause = new Pause();

	private Execution execution;

	private static final String DFLT_SOURCE_NAME = Messages.getString("Program.defaultSource");

	/**
	 * Single instance
	 */
	public static Program INSTANCE;

	/**
	 * Gets the single instance of the program
	 *
	 * @return the program instance
	 */
	public static Program instance() {
		return INSTANCE;
	}

	/**
	 * Creates a new instance of Program
	 */
	@Localize(value="Program")
	public Program() {
		super();
		history = new Vector<HistoryRecord>();
		parser = new StringParser();
		windows = new Vector<TWindow>();
		execution = new Execution(this);
		nonGraphicalObjects = new Vector<TObject>();
	}

	/**
	 * Returns windows, the Vector of TWindows
	 * @return
	 */
	public Collection<TWindow> getWindows()
	{
		return windows;
	}

	/**
	 * Sets the main frame of the application
	 *
	 * @param frame
	 *            the Tangara Frame
	 */
	public void setFrame(TFrame frame)
	{
		this.frame = frame;
		execution.setCurrentGraphicsPane(frame.getGraphicsPane());
	}

	/**
	 * Gets the main frame of the application
	 *
	 * @return the Tangara Frame FIXME is it the right place to get this type of method
	 */
	public TFrame getFrame()
	{
		return frame;
	}

	/**
	 * Appends a message to the end of <code>msgPane</code> with the
	 * <code>attributes</code> associated
	 *
	 * @param message
	 *            message to print
	 * @param style
	 *            font information
	 */
	public void printMessage(String message, int style)
	{
		printMessage(message, style, -1);
	}

	/**
	 * Appends a message to the end of <code>msgPane</code> with the
	 * <code>attributes</code> associated
	 *
	 * @param message
	 *            message to print
	 * @param style
	 *            font information
	 * @param lineNumber
	 *            line number information
	 */
	public void printMessage(String message, int style, int lineNumber)
	{
		frame.addLogMsg(message, style, lineNumber);
	}


	/**
	 * Writes a message according to the output messages format
	 *
	 * @param message
	 *            message to print
	 */
	public void writeMessage(String message) {
		printMessage(message, LogConsole.STYLE_MESSAGE);
		execution.updateOutput(message);
	}

	/**
	 * Prints a code message according to the code message format with a newline
	 *
	 * @param code
	 *            code to print
	 */
	public void printCode(String code) {
		// split commands into single ones
		ArrayList<String> commands = parser.splitCommands(code, true);
		for (String command:commands)
			printMessage(command, LogConsole.STYLE_CODE);
	}

	/**
	 * Prints a message according to the error message format
	 *
	 * @param msg
	 *            msg to print
	 */
	public void printError(String msg) {
		printMessage(msg, LogConsole.STYLE_ERROR);
	}


	/**
	 * Prints an error message according linked to a program line
	 *
	 * @param msg
	 *            msg to print
	 */
	public void printError(String msg, int errorLine) {
		printMessage(msg, LogConsole.STYLE_ERROR, errorLine);
	}


	/**
	 * Prints a output message according to the output message format with a
	 * newline
	 *
	 * @param msg
	 *            msg to print
	 */
	public void printOutputMessage(String msg) {
		printMessage(msg, LogConsole.STYLE_MESSAGE);
		execution.updateOutput(msg);
	}

	/**
	 * Initialization of the programme.
	 * <p>
	 * It overrides the {@link System#err} and {@link System#out} attributes.
	 * </p>
	 *
	 */
	public static void init() {
		instance().activateSystemStreamCatching();
	}

	/**
	 * Disables used streams
	 *
	 */
	public static void end() {
		instance().deactivateSystemStreamCatching();
	}

	/**
	 * Activates new output and error streams for the system
	 *
	 */
	private void activateSystemStreamCatching() {
		if (systemStreamCachingEnable == false) {
			errContent = new ByteArrayOutputStream();
			outContent = new ByteArrayOutputStream();
			execErrorStream = new PrintStream(errContent);
			execOutStream = new PrintStream(outContent);
			System.setErr(execErrorStream);
			System.setOut(execOutStream);
			systemStreamCachingEnable = true;
		}
	}

	/**
	 * Disables used streams and restores the normal streams
	 *
	 */
	private void deactivateSystemStreamCatching() {
		if (systemStreamCachingEnable) {
			System.setErr(prevErr);
			System.setOut(prevOut);

			// clean error
			execErrorStream.close();
			try {
				errContent.close();
			} catch (IOException e) {
				LOG.error("Cannot close temporary error stream buffer", e); //$NON-NLS-1$
			}

			// clean output
			execOutStream.close();
			try {
				outContent.close();
			} catch (IOException e) {
				LOG.error("Cannot close temporary output stream buffer", e); //$NON-NLS-1$
			}
			systemStreamCachingEnable = false;
		}
	}

	/**
	 * Flush the output and error streams
	 *
	 */
	public void flushStreams() {
		execErrorStream.flush();
		execOutStream.flush();
	}


	/**
	 * Gets the GameArea object of the main frame
	 *
	 * @return the GameArea object of the main frame
	 */
	public GraphicsPane getDefaultGraphicsPane() {
		// default : gameArea is the GameArea object of the main frame
		if (frame != null)
			return frame.getGraphicsPane();
		else
			return null;
	}

	/**
	 * Execute the script in the graphics pane passed as parameters. The boolean
	 * parameter is to choose if you want to display or not the command
	 *
	 * @param script
	 *            the script to execute
	 * @param graphicsPane
	 *            the graphics pane to print
	 * @param displayResult
	 *            if you want to display the command or not.
	 */
	public void executeScript(String script, GraphicsPane graphicsPane,
			boolean displayResult) {
		execution.execute(script, graphicsPane, displayResult);
	}

	/**
	 * Execute the script in the graphics pane passed as parameters.
	 *
	 * @param script
	 *            the script to execute
	 * @param graphicsPane
	 *            the graphics pane to print
	 */
	public void executeScript(String script, GraphicsPane graphicsPane) {
		execution.execute(script, graphicsPane);
	}

	/**
	 * Executes the script passed as parameters in the default graphics pane
	 *
	 * @param script
	 *            the script to execute
	 */
	public void executeScript(String script) {
		executeScript(script, getDefaultGraphicsPane());
	}

	/**
	 * Executes the script passed as parameters in the default graphics pane. The
	 * boolean parameter is to choose if you want to display or not the command
	 *
	 * @param script
	 *            the script to execute
	 * @param displayResult
	 *            if you want to display the command or not.
	 */
	public void executeScript(String script, boolean displayResult) {
		executeScript(script, getDefaultGraphicsPane(), displayResult);
	}

	/**
	 * Try to execute the script in the graphics pane passed as paramaters and
	 * return a ScriptExecResult object that determines the output and the error
	 *
	 * @param script
	 *            the script to execute
	 * @param graphicsPane
	 *            the graphics pane to print
	 * @return a ScriptExecResult object that determines the output and the
	 *         error
	 */
	public ScriptExecResult executeScriptGetResult(String script, GraphicsPane graphicsPane) {
		ScriptExecResult result = execution.executeAndGetResult(script,	graphicsPane);
		return result;
	}

	/**
	 * Try to execute the script in the graphics pane passed as paramaters and
	 * return a ScriptExecResult object that determines the output and the error
	 *
	 * @param script
	 *            the script to execute
	 * @return a ScriptExecResult object that determines the output and the
	 *         error
	 */
	public ScriptExecResult executeScriptGetResult(String script) {
		return executeScriptGetResult(script, getDefaultGraphicsPane());
	}

	/**
	 * Sets the shell for the program and the execution threads
	 *
	 * @param shell
	 *            the shell to use
	 */
	public void setBSFEngine(BSFEngine shell) {
		this.shell = shell;
		execution.setBSFEngine(shell);
	}

	/**
	 * Converts the buffer's contents into a string, translating bytes into
	 * characters according to the platform's default character encoding.
	 *
	 * @return String translated from the buffer's contents, "" if the buffer is
	 *         null
	 */
	public String getOutContent() {
		if (outContent == null) {
			return ""; //$NON-NLS-1$
		}
		return outContent.toString();
	}

	/**
	 * Converts the buffer's contents into a string, translating bytes into
	 * characters according to the platform's default character encoding.
	 *
	 * @return String translated from the buffer's contents, "" if the buffer is
	 *         null
	 */
	public String getErrContent() {
		if (errContent == null) {
			return ""; //$NON-NLS-1$
		}
		return errContent.toString();
	}

	/**
	 * Installs the object passed as parameters in tangara
	 * @param
	 * 		the path to the .tgo file
	 */
	public void installObject(String fileName, LibraryWindow lw)
	{
		File f = new File(fileName);
		int point_index = f.getName().lastIndexOf(".");
		String object_name = f.getName().substring(0, point_index);
		File objects_dir = new File(Configuration.instance().getTangaraPath().getParentFile().getAbsolutePath().replace("\\", "/") + "/objects");
		File [] list_files = objects_dir.listFiles();
		boolean test = false;
		int a = 0;
		while (!test && a<list_files.length)
		{
			if (list_files[a].isFile() && list_files[a].getName().contains(object_name))
				test = true;
			a++;
		}
		if (test)
		{
			JOptionPane.showMessageDialog(null, MessageFormat.format(Messages.getString("Program.library.dialog.warning.content"),object_name),Messages.getString("Program.library.dialog.warning.title"), JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			Object[] options = {Messages.getString("tangara.yes"), Messages.getString("tangara.cancel")};
			int answer = JOptionPane.showOptionDialog(null,
						MessageFormat.format(Messages.getString("Program.library.dialog.sure.content"), object_name),
						Messages.getString("Program.library.dialog.sure.title"), JOptionPane.OK_CANCEL_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,     //do not use a custom Icon
					    options,  //the titles of buttons
					    options[0]);

			if (answer == JOptionPane.OK_OPTION)
			{
				  try {
					 ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
					 ZipEntry zen = null;
					 while((zen = zis.getNextEntry()) != null)
					 {
						 if (!zen.isDirectory())
						 {
							 if (zen.getName().equals(object_name+"/"+object_name+".jar"))
							 {
								 File jar = new File(objects_dir.getPath() + "/" + object_name +".jar");
								 FileOutputStream out = new FileOutputStream(jar);
								 byte[] buf = new byte[4096];
					                int offset;
					                while ((offset = zis.read(buf)) > 0)
					                    out.write(buf, 0, offset);
					                out.close();
							 }
							 else if (zen.getName().startsWith(object_name + "/resources") || zen.getName().startsWith(object_name + "/lib"))
							 {
								File aRessource = new File(objects_dir.getPath() + "/" + zen.getName().substring(object_name.length()+1));
								if (!aRessource.getParentFile().exists())
									aRessource.getParentFile().mkdirs();
								FileOutputStream out = new FileOutputStream(aRessource);
								 byte[] buf = new byte[4096];
					             int offset;
					             while ((offset = zis.read(buf)) > 0)
					                 out.write(buf, 0, offset);
					             out.close();
							 }
						 }
						 else
						 {
							 byte[] buf = new byte[4096];
				             while (zis.read(buf) > 0);
						 }
					 }
				lw.setChange(true);
				JOptionPane.showMessageDialog(null, MessageFormat.format(Messages.getString("Program.library.install.success.content"),object_name),
													Messages.getString("Program.library.install.success.title"),
													JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					LOG.error("Error while trying install tgo " + e);
					JOptionPane.showMessageDialog(null,
							MessageFormat.format(Messages.getString("Program.library.install.error.content"), object_name),
							Messages.getString("Program.library.install.error.title"),
						    JOptionPane.ERROR_MESSAGE
						   	);
				}
			}
		}
	}

	/**
	 * Uninstalls the selected object from tangara objects list
	 * @param name
	 * 		the name of the object in the spoken language
	 * @param en_name
	 * 		the english name
	 * @param lw
	 * 		the library
	 */
	public boolean removeObject(String name, String en_name, LibraryWindow lw)
	{
		Object[] options = {Messages.getString("tangara.yes"), Messages.getString("tangara.cancel")};
		int answer = JOptionPane.showOptionDialog(null,
					MessageFormat.format(Messages.getString("Program.library.remove.sure.content"), name),
					Messages.getString("Program.library.remove.sure.title"), JOptionPane.OK_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,     //do not use a custom Icon
				    options,  //the titles of buttons
				    options[0]);

		if (answer == JOptionPane.OK_OPTION)
		{

			try {
				File log = new File(Configuration.instance().getTangaraPath().getParentFile().getAbsolutePath().replace("\\", "/") + "/objects/log.txt");
				String previous = "";
				if (!log.exists())
				{
					log.createNewFile();
				}
				else
				{
					BufferedReader reader = new BufferedReader(new FileReader(log));
					String ligne = null;
					while ((ligne = reader.readLine())!=null)
					{
						previous += ligne + System.getProperty("line.separator");
					}
					reader.close();
				}


				PrintWriter print = new PrintWriter(new BufferedWriter(new FileWriter(log)));
				print.println(previous);
				print.println(en_name);
				print.close();
			} catch (Exception e) {
				LOG.error("Error while uninstall " + e);
				return false;
			}

			lw.setChange(true);
			JOptionPane.showMessageDialog(null, MessageFormat.format(Messages.getString("Program.library.uninstall.success.content"),name),
					Messages.getString("Program.library.uninstall.success.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		return false;
	}



	public Class<?> getTranslatedClassForName(String actualName)
	{
		Configuration conf = Configuration.instance();
		JarFile sourceFile = null;
		ZipEntry entry = null;
		boolean object = false;
		try {
			// we search in Tangara JAR file
			sourceFile = new JarFile(conf.getTangaraPath());
			entry = sourceFile.getEntry("org/colombbus/tangara/" + actualName+"_localization.txt");
			if (entry == null) {
				// could not find in Tangara JAR : try in the objects
				if (!conf.isExecutionMode()) {
					File objectsDirectory = new File(conf.getTangaraPath().getParentFile(), "objects");
					File objectFile = new File(objectsDirectory, actualName+".jar");
					if (objectFile.exists()) {
						sourceFile.close();
						sourceFile = new JarFile(objectFile);
					}
				}
				entry = sourceFile.getEntry("org/colombbus/tangara/objects/" + actualName+"_localization.txt");
				object = true;
			}
			if (entry == null ) {
				LOG.error("Could not find translated class name for " + actualName);
				sourceFile.close();
				return null;
			}

			// Read localization file
			BufferedReader reader = new BufferedReader(new InputStreamReader(sourceFile.getInputStream(entry)));
			String line = reader.readLine();
			while (line!= null) {
				StringTokenizer st = new StringTokenizer(line, " ");
				String language = st.nextToken();
				if (conf.getLanguage().compareTo(language)==0) {
					String className = st.nextToken();
					sourceFile.close();
					if (object) {
						className = "org.colombbus.tangara.objects." + language + "." + className;
						return Class.forName(className, false, conf.getObjectsClassLoader());
					} else {
						className = "org.colombbus.tangara." + language + "." + className;
						return Class.forName(className);
					}
				}
				line = reader.readLine();
			}

		} catch (Exception e) {
			LOG.error("Error while trying to get translated name for class '"+actualName+"'", e);
		} finally {
			if (sourceFile != null) {
				try {
					sourceFile.close();
				} catch (Exception e) {
					LOG.error("Error while trying to close JAR file ", e);
				}
			}
		}
		return null;
	}

	/**
	 * Sets the history depth
	 *
	 * @param limit
	 *            the new history depth
	 */
	public void setHistoryDepth(int limit) {
		historyDepth = limit;
	}

	/**
	 * Adds command into the history record vector
	 *
	 * @param commandText
	 *            a list of commands
	 * @param result
	 *            a boolean that represents the validity of the command
	 */
	public void addCommandToHistory(String commandText, boolean result) {
		// Split command into single ones
		ArrayList<String> commands = parser.splitCommands(commandText, false);
		int currentSize = history.size();
		int commandsToBeIgnored = 0;
		if (historyDepth > -1) {
			// historyDepth = limit of number of commands in history
			if (commands.size() > historyDepth) {
				// too many commands
				eraseHistory();
				commandsToBeIgnored = commands.size() - historyDepth;
			} else if (currentSize + commands.size() > historyDepth) {
				// commands + current = too many
				for (int i = 0; i < currentSize + commands.size() - historyDepth; i++) {
					history.remove(0);
				}
			}
		}
		// we take only the last ones
		for (int i = commandsToBeIgnored; i < commands.size(); i++) {
			String command = commands.get(i);
			history.add(new HistoryRecord(command, result));
		}
	}

	/**
	 * Retrieves the command located at the depth passed as parameters
	 *
	 * @param depth
	 *            the depth of the wanted command
	 * @return A string that represents the found command (null is the depth =0
	 *         or depth is too big)
	 */
	public String getCommandFromHistory(int depth) {
        if (depth == 0) {
        	return "";
        }
        if (depth <= history.size())
        	return history.get(history.size() - depth).getCommand();
        else
        	return null;
	}


	/**
	 * Clears the history records vector
	 *
	 */
	public void eraseHistory() {
		history.clear();
	}


	/**
	 * Gets the history record vector
	 *
	 * @return the history record vector
	 */
	public Vector<HistoryRecord> getHistory() {
		//Improvement made in order to split into commands the blocks that we have saved in the history.
		Vector<HistoryRecord> result = new Vector<HistoryRecord>();
		for (HistoryRecord element : history)
		{
			ArrayList<String> list = Program.instance().parser.splitCommands(element.getCommand(), false);
			boolean bool = element.getResult();
			for (String  string : list)
			{
				HistoryRecord new_element = new HistoryRecord(string, bool);
				result.add(new_element);
			}
		}
		return result;
	}



	/**
	 * This method removes all accents of a text.
	 */
	public static String removeAllAccents(String text)
	{
		String result = text;
		result = result.replace('é', 'e');
		result = result.replace('è', 'e');
		result = result.replace('ê', 'e');
		result = result.replace('à', 'a');
		result = result.replace('ù', 'u');
		result = result.replace('ç', 'c');
		return result;
	}

	/**
	 * Gets the object associated by its name in the variable field. If it
	 * doesn't exit, the method returns null
	 *
	 * @param name
	 *            the name of the variable
	 * @return the associated object
	 */
	public Object getObject(String name) {
		if (name.contains("["))
		{
			int crochetindex = name.lastIndexOf("[");
			String nameTab = name.substring(0, crochetindex);
			int crochetindex2 = name.lastIndexOf("]");
			String number = name.substring(crochetindex+1, crochetindex2);
			int nombre = Integer.parseInt(number);
			try {
				Object obj = shell.eval(DFLT_SOURCE_NAME, 1, 1,
						"this.interpreter.get(\"" + nameTab + "\")");
				return Array.get(obj, nombre);
			} catch (Exception e) {
				LOG.error("Fail to get an object");
				return null;
			}
		}
		else
		{
			try {
				Object obj = shell.eval(DFLT_SOURCE_NAME, 1, 1,
						"this.interpreter.get(\"" + name + "\")");
				return obj;
			} catch (Exception e) {
				LOG.error("Fail to get an object");
				return null;
			}
		}
	}

	/**
	 * Gets the object variable name
	 *
	 * @param source
	 *            the object whose we seek the variable name
	 * @return the associated string
	 */
	public String getObjectName(Object source) {
		String[] objects = null;
		String sourceName = null;
		try {
			objects = (String[]) (shell.eval(DFLT_SOURCE_NAME, 1, 1,"this.namespace.getVariableNames()"));
		} catch (Exception e) {
			LOG.error("Unable to get all variable names from bsh " + e.getMessage());
			return null;
		}
		if (objects != null) {
			try {
				for (String object : objects) {
					Object obj = shell.eval(DFLT_SOURCE_NAME, 1, 1, "this.interpreter.get(\"" + object + "\")");
					if (obj.toString().startsWith("["))
					{
						if (source.equals(shell.eval(DFLT_SOURCE_NAME, 1, 1, object)))
						{
							sourceName = object;
							break;
						}
						else
						{
							int size = Array.getLength(obj);
							for (int i = 0; i<size; i++)
							{
								if (source.equals(Array.get(obj, i)))
								{
									sourceName = object+"["+i+"]";
									break;
								}
							}
						}
					}
					else if (source.equals(shell
							.eval(DFLT_SOURCE_NAME, 1, 1, object))) {
						sourceName = object;
						break;
					}
				}
			} catch (Exception e) {
				LOG.error("Unable to evaluate variables from bsh " + e);
				return null;
			}
		}
		return sourceName;
	}

	/**
	 * Opens the file passed as parameters, and executes the commands that are
	 * in the file
	 *
	 * @param fileName
	 *            the file name (with path or not)
	 * @param graphicsPane
	 *            the graphics pane (to use method executeScript)
	 * @param waitForResult
	 *            if you want to generate a scriptExecResult object
	 */
	public void loadFile(String fileName, GraphicsPane graphicsPane,
			boolean waitForResult) {
		// TODO enhance, check isFile & read properties && more error cases
		if (FileUtils.isTangaraFile(fileName) == false) {
			// If it is not a tangara file, we try to add the tangara extension
			fileName = fileName+"."+FileUtils.getTangaraFileExt();
		}

		try {
			File file = new File(fileName);
			if (!file.isAbsolute()) {
				// 1st try the current directory
				file = new File(getCurrentDirectory(), fileName);
				// 2nd if file does not exist, try user home
				if (!file.exists())
					file = new File(Configuration.instance().getUserHome(),
							fileName);
			}
			if (!file.exists()) {
				LOG.error("Unable to read file " + fileName
						+ " (file not found)");
				printError(MessageFormat.format(Messages
						.getString("Program.loadFile.fileNotFound"), fileName));
				return;
			}
			setCurrentDirectory(file.getParentFile());

			ScriptReader scriptReader = new ScriptReader();
			String script = scriptReader.readScript(file);

			if (Configuration.instance().getProperty("quote.mode").equals("INTUITIVE"))
			{
				script = StringParser.addQuoteDelimiters(script);
			}

			if (waitForResult)
			{
				executeScriptGetResult(script, graphicsPane);
			}
			else
			{
				executeScript(script, graphicsPane);
			}
		} catch (Throwable th) {
			LOG.error("Unable to read file " + fileName, th);
			printError(MessageFormat.format(Messages
					.getString("Program.loadFile.error"), fileName));
		}
	}

	/**
	 * Loads the file passed as parameters in the specified window (open and
	 * execute)
	 *
	 * @param windowName
	 *            the window name
	 * @param fileName
	 *            the file name (with path or not)
	 */
	public void loadFile(TWindow window,String fileName)
    {
    	GraphicsPane graphicsPane;
    	if (window != null && windows.contains(window))
    		graphicsPane = window.getGraphicsPane();
    	else
    	{
    		graphicsPane = getCurrentGraphicsPane();
       	 	if (graphicsPane == null)
       	 	{
       	 		graphicsPane = getDefaultGraphicsPane();
       	 	}

    	}
    	loadFile(fileName,graphicsPane,false);
    }

	/**
	 * Adjust the window size whose the name is passed as parameters
	 *
	 * @param windowName
	 *            the window name
	 */
	public void adjustWindowSize(TWindow window) {
		boolean result = false;
		if (window!=null && windows.contains(window)) {
			result = window.computeSize();
			window.pack();
		} else {
			result = frame.computeSize();
			frame.pack();
		}
		if (!result)
			window.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
	}

	/**
	 * Executes on the object source the method passed as parameters.
	 *
	 * @param source
	 *            the object on which executes the method
	 * @param method
	 *            the method to execute on the object
	 */
	public void executeMethod(Object source, String method) {
		String sourceName = getObjectName(source);
		if (sourceName == null) {
			LOG.warn("Object " + source + " not found in bsh variables");
			return;
		}
		executeScript(sourceName + "." + method);
	}


	/**
	 * Allows to choose the design mode (command or program)
	 *
	 * @param value
	 *            true = command mode, false = program mode
	 */
	public void setDesignMode(boolean value) {
		getDefaultGraphicsPane().setDesignMode(value);
		for (TWindow w : windows) {
			w.getGraphicsPane().setDesignMode(value);
			w.getGraphicsPane().repaint();
		}
	}

	public boolean getDesignMode()
	{
		return getDefaultGraphicsPane().getDesignMode();
	}

	/**
	 * Gets the current graphics pane
	 *
	 * @return the current graphics pane
	 */
	public GraphicsPane getCurrentGraphicsPane() {
		if (execution != null) {
			GraphicsPane currentGraphicsPane = execution
					.getCurrentGraphicsPane();
			if (currentGraphicsPane == null) {
				// currentArea not yet initialized
				return getDefaultGraphicsPane();
			} else {
				return currentGraphicsPane;
			}
		} else {
			return getDefaultGraphicsPane();
		}
	}

	/**
	 * Executes the command in the specified window
	 *
	 * @param windowName
	 *            the window name where to execute the command
	 * @param command
	 *
	 */
	public void executeCommandInWindow(TWindow window, String command) {
		if (window !=null && windows.contains(window))
			executeScript(command, window.getGraphicsPane());
		else
			executeScript(command);
	}

	/**
	 * Gets the current directory
	 *
	 * @return java.io.File path of current directory
	 */
	public File getCurrentDirectory() {
		return currentDirectory;
	}

	/**
	 * Sets the current directory
	 *
	 * @param directory
	 *            java.io.File path of new directory
	 */
	public void setCurrentDirectory(File directory) {
		currentDirectory = directory;
	}

	/**
	 * Finds the object <code>source</code> and delete it from variable field.
	 *
	 * @param source
	 *            the object to delete
	 */
	public void deleteObject(Object source) {
		String objectName = getObjectName(source);
		if (objectName != null) {
			try {
				shell.eval(DFLT_SOURCE_NAME, 1, 1,
						"this.namespace.unsetVariable(\"" + objectName + "\")");
			} catch (Exception e) {
				LOG.error("Unable to unset variable name " + objectName
						+ " from bsh " + e.getMessage());
			}
		}
	}

	/**
	 * Checks whether or not the thread is in pause
	 *
	 */
	public void checkPause() {
		pause.checkPause();
	}

	/**
	 * Adds the object <code>obj</code> to the non graphical object list
	 *
	 * @param obj
	 *            the object to add
	 */
	public void addNonGraphicalObject(TObject obj) {
		synchronized (nonGraphicalObjects) {
			if (!nonGraphicalObjects.contains(obj))
				nonGraphicalObjects.add(obj);
		}
	}

	/**
	 * Removes all non graphical objects
	 *
	 */
	public void removeNonGraphicalObjects() {
		synchronized (nonGraphicalObjects) {
			for (TObject obj : nonGraphicalObjects) {
				try {
					obj.deleteObject();
				} catch (Exception e) {
					LOG.debug("Error while removing non graphical object: " + e);
				}
			}
			nonGraphicalObjects.clear();
		}
	}

    @Localize(value="Program.reset")
	public void reset()
	{
		try {
	        removeNonGraphicalObjects();
	        frame.getGraphicsPane().removeGraphicalObjects();
	        eraseHistory();
	        closeAllWindows();
	        Configuration.instance().getEngine().eval(DFLT_SOURCE_NAME, 1, 1,"this.namespace.clear()");
			Configuration.instance().load();
			Configuration.instance().getManager().declareBean(Messages.getString("Main.bean.program"), this, this.getClass());
			Configuration.instance().getManager().declareBean(Messages.getString("Main.bean.screen"), getCurrentGraphicsPane(), getCurrentGraphicsPane().getClass());
			Class<?> tools = Program.instance().getTranslatedClassForName("Tools");
			try {
				Tools a = (Tools)tools.newInstance();
				String toolsName = Messages.getString("Main.bean.tools");
				Configuration.instance().getManager().declareBean(toolsName, a, tools);
			} catch (Exception e) {
				LOG.error("Error while casting tools " + e);
			}
		} catch (Exception e) {
			LOG.debug("Error while clear " + e);
		}
	}

	/**
	 * Serves to know to which class corresponds a given object name..
	 * @param name
	 */
	public void identifyObject(String name)
	{
		Object obj = getObject(name);
		if (obj!=null)
		{
			String message = Messages.getString("Program.identify.result") + " ";
			message = MessageFormat.format(message, name);
			String class_name = obj.getClass().toString();
			int last_point = class_name.lastIndexOf(".");
			writeMessage( message + class_name.substring(last_point+1, class_name.length()));
		}
	}


	//------------------------------------------------------------------------------------
	//International methods:

		/**
		 * Exits the program
		 */
		@Localize(value="Program.exit")
		public void exit() {
			LOG.info("Exiting program");
	        frame.getGraphicsPane().removeGraphicalObjects();
	        removeNonGraphicalObjects();
			closeAllWindows();
			frame.setVisible(false);
			end();
			Main.exit();
		}

		public void closeAllWindows()
		{
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run()
				{
					for (TWindow window : windows)
					{
						window.delete();

					}
				}
			});
		}

		public void registerWindow(TWindow window)
		{
			windows.add(window);
		}

		/**
		 * Waits during several milliseconds
		 * @param milliseconds
		 *            the time to wait in milliseconds
		 */
		// removed : is not used?
		//@Localize(value="Program.wait")
		public void wait(int milliseconds) {
			pause.setPause(milliseconds);
		}

		@Localize(value="Program.pause")
		public void pause()
		{
			getCurrentGraphicsPane().freeze(true);
		}

		@Localize(value="Program.startAgain")
		public void startAgain()
		{
			getCurrentGraphicsPane().freeze(false);
		}


		/**
		 * Associate the .tgr files to Tangara application
		 * (Development function)
		 *
		 */
		public void associateFiles() {
			Configuration.instance().registerTangaraFileType();
		}

		/**
		 * Opens the file given in parameter.
		 * @param fileName
		 *            the file name
		 */
		@Localize(value="Program.loadFile")
		public void loadFile(String fileName) {
			loadFile(null, fileName);
		}

		/**
		 * Returns the hour and the date
		 *
		 * @return a string that defines the hour and the date
		 */
		//@Localize(value="Program.now")
		public String now() {
			Calendar now = Calendar.getInstance();
			String result = "" + now.get(Calendar.DAY_OF_MONTH) + "-"
					+ now.get(Calendar.MONTH) + "-" + now.get(Calendar.YEAR) + "_"
					+ now.get(Calendar.HOUR_OF_DAY) + "-"
					+ now.get(Calendar.MINUTE) + "-" + now.get(Calendar.SECOND);
			return result;
		}

		/**
		 * Enables to define the title of Tangara frame.
		 *
		 * @param title
		 *            the new title for the Tangara frame.
		 */
		@Localize(value="Program.defineTitle")
		public void defineTitle(String title) {
			Container top = getCurrentGraphicsPane().getTopLevelAncestor();
			if (top instanceof TFrame) {
				((TFrame) top).setTitle(title);
			}
		}

		/**
		 * Writes a message in the Tangara Console.
		 * @param texte
		 */
		@Localize(value="Program.write3")
		public void write(double a) {
			writeMessage(Double.toString(a));
		}

		@Localize(value="Program.write2")
		public void write(int a) {
			writeMessage(Integer.toString(a));
		}

		@Localize(value="Program.write")
		public void write(String text) {
			writeMessage(text);
		}

		public void setErrorInConsole(int offset, int length, int errorLineNumber) {
			frame.setErrorLines(offset, length, errorLineNumber);
		}

		public void setErrorInConsole(int offset, int length) {
			setErrorInConsole(offset, length, -1);
		}

		public int getCurrentLogIndex() {
			return frame.getCurrentLogIndex();
		}
}
