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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.colombbus.tangara.io.ScriptReader;
import org.colombbus.tangara.io.ScriptWriter;
import org.gjt.sp.jedit.textarea.TextArea;


/**
 * The main framework of Tangara. It contains all panes of the UI when you run
 * Tangara in normal mode (with no arguments in main)
 * 
 * @author gwen
 * 
 */

@SuppressWarnings("serial")
public class EditorFrame extends TFrame {
	
	/**
	 * This class contains the informations of a tab.
	 * 
	 */
	
	private static final int optionType = JOptionPane.OK_CANCEL_OPTION;

	/** Class logger */
	private static Logger LOG = Logger.getLogger(EditorFrame.class);

	private static final double DIVIDER1_LOCATION = 0.65;
	private static final double DIVIDER2_LOCATION = 0.5;

	private int commandHistory = 0;
	
	private PopupManager popupManager;

	private boolean writingHelp = true;
	private boolean commandMode = false;
	private boolean displayLineNumbers = false;

	private int[] programDividerLocation = null;
	private int[] commandDividerLocation = null;

	private static final String ICON_PATH = "logo_tangara.png"; //$NON-NLS-1$

	private Font font;
	private int interfaceLevel;
	private Configuration configuration;
	public int commandModifier;
	
	private JSplitPane jSplitPane1 = null;
	private JPanel basePanel = null;
	private JPanel controlPanel = null;
	private JSplitPane jSplitPane2 = null;
	
	private JLabel toProgramLabel = null;
	private JLabel selectAllLabel = null;
	private JLabel deselectAllLabel = null;
	
	private JPanel pageEnd = null;
	
	private LogConsole console = null;
	
	private boolean noCodeSelected = true;
	
	private JPanel msgButtons = null;
	private JPanel msgButtonsPanel = null;
	private JPanel msgButtonsMainPanel = null;
	
	private JScrollPane scrollPane = null;
	private JPanel commandPanel = null;
	private JPanel cmdButtonPanel = null;
	private JButton cmdRunButton = null;
	private JButton refreshButton = null;
	private TextPane commandPane = null;
	private JPanel editionPanel = null;
	private GraphicsPane graphicsPane = null;
	private Banner banner = null;
	private JFileChooser fileChooser = null;
	private JFileChooser fileChooserWithoutFilter = null;
	private JPanel modePanel = null;
	private OptionPanel optionPanel = null;
    protected TextPaneManager programManager = null;
    private CommandTransferHandler commandHandler = null;
    private int programIndex = -1; 
        
	private static int tabSize = 0;

	private AbstractAction cutAction = null;
	private AbstractAction copyAction = null;
	private AbstractAction pasteAction = null;
	private AbstractAction undoAction = null;
    private AbstractAction redoAction = null;

    private Editable focusOwner = null;
    
    
	public static enum QuoteMode {
		// the old mode with many sharps before each quotation mark
		SHARP, 
		// the sharps are placed automatically. The sharps can still
		// be placed to force the level.
		INTUITIVE
	}
	
	public static QuoteMode quoteMode = QuoteMode.INTUITIVE;

	/**
	 * Creates a new instance of EditorFrame with the specified configuration
	 * 
	 * @param the
	 *            current configuration
	 */
	public EditorFrame(Configuration configuration) {
		super();
		this.configuration = configuration;
		loadParameters();
		initialize();
	}

	/**
	 * Loads all the required parameters that are written in the
	 * tangara.properties file.
	 */
	public void loadParameters() {
		// This parameter must be loaded here and not later. We take note of the
		// font to use.
		font = new Font(configuration.getProperty("editor.font"), Font.PLAIN, //$NON-NLS-1$
				Integer.parseInt(configuration.getProperty("editor.fontSize"))); //$NON-NLS-1$

		// We read the default difficulty level from the configuration file.
		// We get back the value of tangara.level (cf. tangara properties).
		interfaceLevel = Integer.parseInt(configuration
				.getProperty("tangara.level")); //$NON-NLS-1$

		// We read the default activation of writing help.
		writingHelp = configuration.getProperty("popup.display").equals("1"); //$NON-NLS-1$ //$NON-NLS-2$

		// We read the default activation of display line numbers.
		displayLineNumbers = configuration.getProperty("lineNumbers.display").equals("1"); //$NON-NLS-1$ //$NON-NLS-2$
		TextPane.setDisplayLineNumbers(displayLineNumbers);
		
		// We read the default quote mode from the configuration file.
		// We get back the value of tangara.quoteMode (cf. tangara properties).
		String mode = configuration.getProperty("quote.mode"); //$NON-NLS-1$
		setQuoteMode(mode);

		// We find which operating system is used.
		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (os.indexOf("mac") != -1) //$NON-NLS-1$
			commandModifier = java.awt.event.InputEvent.META_DOWN_MASK;
		else
			commandModifier = java.awt.event.InputEvent.CTRL_DOWN_MASK;

		tabSize = Integer.parseInt(configuration.getProperty("tab.size")); //$NON-NLS-1$
	}

	/**
	 * This method changes the standard size of a TAB for a given JTextPane.
	 * 
	 * @param the
	 *            JTextPane where to apply this method.
	 */
	public void setTabSizeOf(JTextPane textPane) {
		int spacesPerTab = tabSize;
		FontMetrics fm = textPane.getFontMetrics(textPane.getFont());
		int charWidth = fm.charWidth(' ');
		int tabWidth = charWidth * spacesPerTab;

		TabStop[] tabStops = new TabStop[200];

		for (int j = 0; j < tabStops.length; j++) {
			int tab = j + 1;
			tabStops[j] = new TabStop(tab * tabWidth);
		}

		TabSet tabSet = new TabSet(tabStops);

		Style style = textPane.getLogicalStyle();
		StyleConstants.setTabSet(style, tabSet);
		textPane.setLogicalStyle(style);
	}

	/**
	 * Gets the current font
	 * 
	 * @return the used font
	 */
	public Font getFontParameter() {
		return font;
	}

	/**
	 * Returns the current mode (command or program)
	 * 
	 * @return true if we are in command mode, false in program mode
	 */
	public boolean getMode() {
		return commandMode;
	}

	/**
	 * Returns the pane in command mode
	 * 
	 * @return the pane in command mode
	 */
	public TextPane catchEditorPane() {
		return commandPane;
	}

	/**
	 * Prints the "Welcome" message
	 * 
	 */
	private void showWelcomeMessage() {
		String message = Messages.getString("EditorFrame.welcome"); //$NON-NLS-1$
		Program.instance().writeMessage(message);
	}

	/**
	 * Clears the code editor pane
	 * 
	 */
	public void clearCommandPane() {
		commandPane.getBuffer().remove(0, commandPane.getBufferLength());
	}

	/**
	 * Gets the code in <code>commandPane</code> and launches its interpretation
	 * 
	 */
	public void executeInputScript() {
		// move to the beginning document
		commandPane.moveCaretPosition(0);

		String commands = ""; //$NON-NLS-1$
		switch (quoteMode) {
		case SHARP:
			commands = commandPane.getText();
			break;

		case INTUITIVE:
			commands = commandPane.getText();
			commands = StringParser.addQuoteDelimiters(commands);
			break;
		}

		// executes the commands with reference to the default GameArea object
		commands = commands.trim();
		programIndex = -1;
		Program.instance().executeScript(commands);
		clearCommandPane();
		// and get the focus
		commandPane.requestFocusInWindow();
		commandHistory = 0;
	}

	/**
	 * Gets the code in <code>programPane</code> and launches its interpretation
	 */
	public void executeProgram(String commands) {
		executeProgram(commands, -1);
	}
	
	public void executeProgram(String commands, int currentIndex) {
		programIndex = currentIndex;
		refresh();
		optionPanel.setCommandMode();
		
		if (quoteMode == QuoteMode.INTUITIVE)
			commands = StringParser.addQuoteDelimiters(commands);

		commands = commands.trim();
		if (Program.instance().getDesignMode())
			optionPanel.changeDesignMode();
		Program.instance().executeScript(commands);
		
	}

	/**
	 * Initializes the banner and the line mode before displaying the welcome
	 * message
	 */
	@Override
	public void afterInit() {
		// move back the separator of the basis panel
        jSplitPane1.setDividerLocation(DIVIDER1_LOCATION);
        banner.setCommandMode();
        ((java.awt.CardLayout) modePanel.getLayout()).show(modePanel,"commandMode"); //$NON-NLS-1$
        setInterfaceLevel(interfaceLevel);
        showWelcomeMessage();
        commandPane.requestFocusInWindow();
        commandMode = true;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// places the second separator.
				jSplitPane2.setDividerLocation(DIVIDER2_LOCATION);
                programDividerLocation = new int[2];
                // first time : set dividerLocation to fullScreen
                programDividerLocation[0] = 0;
                programDividerLocation[1] = jSplitPane2.getDividerLocation()+ jSplitPane1.getDividerLocation();
			}
		});
	}

	/**
	 * Selects the command mode, simpler than the program mode. It allows the
	 * user to see immediately the effect of the typed commands.
	 */
	public void setCommandMode() {
		// We check if we are not already in this mode.
       if (!commandMode) {
            if (programDividerLocation != null) {
                programDividerLocation[0] = jSplitPane1.getDividerLocation();
                programDividerLocation[1] = jSplitPane2.getDividerLocation();
            }
            Runnable commandModeRun = new Runnable() {
                @Override
				public void run() {
                	graphicsPane.freeze(false);
                    ((java.awt.CardLayout) modePanel.getLayout()).show(modePanel,"commandMode"); //$NON-NLS-1$
                    banner.setCommandMode();
                    commandPane.requestFocusInWindow();
                    commandMode = true;
                    console.disableDragAndDrop();
                    if (popupManager != null)
                        popupManager.closePopup();
                    if (commandDividerLocation != null) {
                        jSplitPane1.setDividerLocation(commandDividerLocation[0]);
                        // InvokeLater is required here, otherwise this second
                        // setDividerLocation is ignored
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
							public void run() {
                                jSplitPane2.setDividerLocation(commandDividerLocation[1]);
                            }
                        });
                    }
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                commandModeRun.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(commandModeRun);
                } catch (InterruptedException e) {
                    LOG.error(e);
                } catch (InvocationTargetException e) {
                    LOG.error(e);
                }
            }
        }
	}

	/**
	 * Selects program mode, more complex than the command mode. It allows the
	 * user to insert commands from the history list.
	 */
	public void setProgramMode() {
		// We check if we are not already in this mode.
		if (commandMode) {
		    if (commandDividerLocation == null) {
				commandDividerLocation = new int[2];
			}
			commandDividerLocation[0] = jSplitPane1.getDividerLocation();
			commandDividerLocation[1] = jSplitPane2.getDividerLocation();
			Runnable programModeRun = new Runnable() {
                @Override
				public void run() {
                	graphicsPane.freeze(true);
                    ((java.awt.CardLayout) modePanel.getLayout()).show(modePanel,"programMode"); //$NON-NLS-1$
                    banner.setProgramMode();
            		getProgramManager().requestFocus();
                    commandMode = false;
                    console.enableDragAndDrop();
                    if (popupManager != null)
                        popupManager.closePopup();
                    if (programDividerLocation != null) {
                        jSplitPane1.setDividerLocation(programDividerLocation[0]);
                        // InvokeLater is required here, otherwise this second
                        // setDividerLocation is ignored
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
							public void run() {
                                jSplitPane2.setDividerLocation(programDividerLocation[1]);
                            }
                        });
                    }
                }
			};
            if (SwingUtilities.isEventDispatchThread()) {
                programModeRun.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(programModeRun);
                } catch (InterruptedException e) {
                    LOG.error(e);
                } catch (InvocationTargetException e) {
                    LOG.error(e);
                }
            }
		}
	}
	
	/**
	 * Allows to choose which files to export
	 * @throws IOException 
	 * 
	 */
	public void exportProgram() throws IOException {
		graphicsPane.freeze(true);
		fileChooserWithoutFilter.setCurrentDirectory(getProgramManager().getCurrentDirectory());
		fileChooserWithoutFilter.setSelectedFile(new File("")); //$NON-NLS-1$
		fileChooserWithoutFilter.setDialogTitle(Messages.getString("EditorFrame.file.open.title")); //$NON-NLS-1$
		int returnVal = fileChooserWithoutFilter.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
        	File selected = fileChooserWithoutFilter.getSelectedFile();
    		(new FileSelection(this, true, selected)).setVisible(true);
        }
		graphicsPane.freeze(false);
    }

	/**
	 * Allows to choose which file to open
	 * 
	 */
	public void openFile() {
		graphicsPane.freeze(true);
        fileChooser.setSelectedFile(new File(getProgramManager().getCurrentDirectory(), ".tgr")); //$NON-NLS-1$
        fileChooser.setDialogTitle(Messages.getString("EditorFrame.file.open.title")); //$NON-NLS-1$
        int returnVal = fileChooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
        	String scriptPathname = fileChooser.getSelectedFile().getAbsolutePath();
        	if (!FileUtils.isTangaraFile(scriptPathname))	{
        		JOptionPane.showMessageDialog(this,MessageFormat.format(Messages.getString("EditorFrame.file.open.error.fileType"), scriptPathname), Messages.getString("EditorFrame.file.open.error.title"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
        		return;
        	}

        	try {
        		File scriptFile = new File(scriptPathname);
        		String script = loadScript(scriptFile);
        		
        		int index = getProgramManager().getFileIndex(scriptFile);
        		if (index > -1) {
        			// The file is already open : we just select the corresponding tab
        			getProgramManager().setSelectedIndex(index);
        		} else {
        			// The file is not already open : we add a pane
        			getProgramManager().addPane(scriptFile, script);
        		}
        		setCurrentDirectory(scriptFile.getParentFile());
        		// Reset undo manager
        		programResetUndo();
            	if (commandMode) {
					executeProgram(script);
            	}
        	}
        	catch(Throwable th)
        	{
        		JOptionPane.showMessageDialog(this,MessageFormat.format(Messages.getString("EditorFrame.file.open.error.fileOpen"), scriptPathname), Messages.getString("EditorFrame.file.open.error.title"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
        		LOG.error("Unable to read file " + scriptPathname, th); //$NON-NLS-1$
        	}
        }
        graphicsPane.freeze(false);
    }
	
	private String loadScript( File sourceFile) throws IOException {
		ScriptReader reader = new ScriptReader();
		String script = reader.readScript(sourceFile);
		return script;
	}

	/**
	 * Allows to create a new program when you choose "Make program" in the file
	 * menu from the command historic. Only in command mode
	 * 
	 */
	public void makeProgram() {
		graphicsPane.freeze(true);
		(new CommandSelection(this, true,
				CommandSelection.MODE_PROGRAM_CREATION)).setVisible(true);
		graphicsPane.freeze(false);
	}

	/**
	 * Only in program mode. Allows to save your program.
	 * 
	 */
	public void setConfiguration() {
		graphicsPane.freeze(true);
		(new ConfigurationWindow(this)).setVisible(true);
		graphicsPane.freeze(false);
	}

	/**
	 * Opens the Library Window and freezes temporarily the GraphicsPane.
	 */
	public void makeLibrary() {
		graphicsPane.freeze(true);
		(new LibraryWindow(this)).setVisible(true);
		graphicsPane.freeze(false);
	}

	/**
	 * Opens the About Window and freezes temporarily the GraphicsPane.
	 */
	public void makeAbout() {
		graphicsPane.freeze(true);
		Image backgroundImage = Main.getSplashScreenImage().getImage();
		AboutWindow aboutDlg = new AboutWindow(this,backgroundImage);
		aboutDlg.setVisible(true);
		graphicsPane.freeze(false);
	}

	/**
	 * Only in program mode. Allows to save your program.
	 * 
	 */
	public void saveProgram() {
		File currentFile = getProgramManager().getCurrentFile();
		if (currentFile == null)
			saveProgramAs();
		else
			saveProgram(currentFile);
	}

	/**
	 * Only in program mode. Saves the program according to the chosen
	 * destination file
	 * 
	 * @param destinationFile
	 *            the destination file
	 */
	private void saveProgram(File destinationFile) {
		try {
			saveBufferToFile(destinationFile);
			succeedToSaveFile(destinationFile);
			getProgramManager().setCurrentFile(destinationFile);
		} catch (Throwable th) {
			failToSaveFile(destinationFile, th);
		}
	}
	
	private void saveBufferToFile(File destinationFile) throws IOException {
		String script = getProgramManager().getCurrentContents();
		OutputStream out=null;
		try {
			out = new FileOutputStream(destinationFile);
			ScriptWriter writer = new ScriptWriter(Configuration.instance().getScriptHeader());
			writer.writeScript(script, out);
		} finally {
			IOUtils.closeQuietly(out);
		}
		
	}

	private void succeedToSaveFile(File destinationFile) {
		printInfo(MessageFormat.format(Messages
				.getString("EditorFrame.program.saveOk"), destinationFile //$NON-NLS-1$
				.getName()));
		//currentProgram = destinationFile;
		getProgramManager().setCurrentPaneModified(false);
		setCurrentDirectory(destinationFile.getParentFile());
	}

	private void failToSaveFile(File destinationFile, Throwable th) {
		LOG.error("Unable to write to program file \"" //$NON-NLS-1$
				+ destinationFile.getAbsolutePath() + "\"", th); //$NON-NLS-1$
		String msgPattern = Messages
				.getString("EditorFrame.program.saveError"); //$NON-NLS-1$
		printError(MessageFormat.format(msgPattern, destinationFile
				.getName()));
	}

	/**
	 * Only in program mode. Allows to save your program by selecting the
	 * filename
	 * 
	 */
	public void saveProgramAs() {
		graphicsPane.freeze(true);
		String dialogTitle = Messages.getString("EditorFrame.program.save.title");//$NON-NLS-1$
		
		if (getProgramManager().getCurrentFile()== null)
			fileChooser.setSelectedFile(new File(getProgramManager().getCurrentDirectory(),Messages.getString("EditorFrame.file.newFile"))); //$NON-NLS-1$
		else
			fileChooser.setSelectedFile(getProgramManager().getCurrentFile());
		fileChooser.setDialogTitle(dialogTitle);
		int returnVal = fileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File destinationFile = fileChooser.getSelectedFile();
			String fileName = destinationFile.getAbsolutePath();
			if (FileUtils.isTangaraFile(fileName) == false) {
				fileName = fileName + '.' + FileUtils.getTangaraFileExt();
				destinationFile = new File(fileName);
			}
			if (destinationFile.exists()) {
				if( userConfirmFileOverride(destinationFile)==false)
				{
					return;
				}
			}
			saveProgram(destinationFile);
		}
		graphicsPane.freeze(false);
	}
	
	private boolean userConfirmFileOverride(File destinationFile) {
		String messagePattern = Messages.getString("EditorFrame.program.override.message"); //$NON-NLS-1$
		String message = MessageFormat.format(messagePattern, destinationFile.getName());
		String title = Messages.getString("EditorFrame.program.override.title"); //$NON-NLS-1$
		Object[] options = { Messages.getString("tangara.yes"),Messages.getString("tangara.cancel") }; //$NON-NLS-1$ //$NON-NLS-2$
		Icon icon = null;
		int messageType = JOptionPane.QUESTION_MESSAGE;
		int answer = JOptionPane.showOptionDialog(EditorFrame.this, message,
				title, optionType, messageType, icon, options, options[0]);
		return answer == JOptionPane.OK_OPTION;
	}

	/**
	 * Creates a new tab and set the text as newContent.
	 * 
	 * @param newContent
	 *            the text to set in the new tab's programPane.
	 */
	public void newProgram(String newContent)
	{
		getProgramManager().addPane(Program.instance().getCurrentDirectory(), newContent);
		// Reset undo manager
		programResetUndo();
	}
	

	/**
	 * Inserts commands from history
	 * 
	 */
	public void insertCommandsFromHistory() {
		graphicsPane.freeze(true);
		(new CommandSelection(this, true,
				CommandSelection.MODE_COMMANDS_INSERTION)).setVisible(true);
		graphicsPane.freeze(false);
	}

	/**
	 * Inserts commands to the program (in program mode) (launched by command
	 * selection)
	 * 
	 * @param commands
	 *            the list of commands to insert
	 */
	public void insertCommands(String commands) {
		if (commands.length() == 0)
			return;
    	if(commandMode) {
    		optionPanel.setProgramMode();
    		newProgram(commands);
    	} else {
    		TextPane tp = getProgramManager().getCurrentPane();
    		String previousText = tp.getText();
    		int carretPosition = tp.getCaretPosition();
    		int firstLine = tp.getFirstLine();
    		
    		String textBefore = ""; //$NON-NLS-1$
    		for(int i = 0; i < carretPosition; i++) {
    			textBefore += previousText.charAt(i);
    		}
    		
    		String textAfter = ""; //$NON-NLS-1$
    		for(int i = carretPosition; i < previousText.length(); i++) {
    			textAfter += previousText.charAt(i);
    		}
    		if(carretPosition == 0 || previousText.charAt(carretPosition-1) == '\n') {
    			tp.setText(textBefore+commands+textAfter);
    			tp.moveCaretPosition(carretPosition+commands.length(),TextArea.NO_SCROLL);
    		}
    		else {
    			tp.setText(textBefore+"\n"+commands+textAfter); //$NON-NLS-1$
    			tp.moveCaretPosition(carretPosition+commands.length()+1,TextArea.NO_SCROLL);
    		}
    		int newLine = tp.getCaretLine();
    		if(newLine-firstLine+2 > tp.getVisibleLines()) {
    			tp.setFirstLine(newLine - tp.getVisibleLines() + 2);
    		} else {
    			tp.setFirstLine(firstLine);
    		}
    	}
    	setProgramMode();
	}

	
	public void closePane() {
		getProgramManager().closeCurrentPane();
	}

	public void newPane() {
		getProgramManager().addPane();
		programResetUndo();
	}
	
	/**
	 * Gets the file chooser of Tangara frame
	 * 
	 * @return javax.swing.JFileChooser
	 */
	public JFileChooser getFileChooser() {
		return fileChooser;
	}

	/**
	 * Copies the selected area (from command editor pane, program pane, or
	 * output pane).
	 * 
	 */
	public void copy() {
		if (focusOwner != null)
			focusOwner.copy();
	}

	/**
	 * Cuts the selected area (from command editor pane, program pane, or output
	 * pane).
	 * 
	 */
	public void cut() {
		if (focusOwner != null)
			focusOwner.cut();
	}

	/**
	 * Paste the content of the clipboard system in the selected pane (according
	 * to the mode)
	 * 
	 */
	public void paste() {
		if (focusOwner != null)
			focusOwner.paste();
	}
	
	
	public void enableEdit() {
        if (cutAction != null)
            cutAction.setEnabled(true);
        if (copyAction != null)
            copyAction.setEnabled(true);
	}
	
	public void disableEdit() {
        if (cutAction != null)
            cutAction.setEnabled(false);
        if (copyAction != null)
            copyAction.setEnabled(false);
	}

	/**
	 * Quit Tangara (close the window and the program).
	 * 
	 */
	public void exit() {
		graphicsPane.freeze(true);
		String title = Messages.getString("EditorFrame.exit.title"); //$NON-NLS-1$
		String message = Messages.getString("EditorFrame.exit.message"); //$NON-NLS-1$
		Object[] options = { Messages.getString("tangara.yes"), //$NON-NLS-1$
				Messages.getString("tangara.cancel") }; //$NON-NLS-1$
		int answer = JOptionPane.showOptionDialog(this, message, title,
				optionType, JOptionPane.QUESTION_MESSAGE,
				null, // do not use a custom Icon
				options, // the titles of buttons
				options[0]);

		if (answer == JOptionPane.OK_OPTION) {
			if (getProgramManager().checkProgramChanged()) {
				dispose();
				Program.instance().exit();
			}
		} else
			graphicsPane.freeze(false);
	}

	/**
	 * Prints the error passed as parameters (with a newline)
	 * 
	 * @param text
	 *            the error to print
	 */
	public void printError(String text) {
		Program.instance().printError(text);
	}

	/**
	 * Prints the message passed as parameters (with a newline)
	 * 
	 * @param text
	 *            the message to print
	 */
	public void printInfo(String text) {
		Program.instance().printOutputMessage(text);
	}

	/**
	 * Gets the current directory of the file chooser
	 * 
	 * @return java.io.File the path of the directory
	 */
	public File getCurrentDirectory() {
		return fileChooser.getCurrentDirectory();
	}

	/**
	 * Sets the current directory for the file chooser
	 * 
	 * @param directory
	 *            the new directory
	 */
	public void setCurrentDirectory(File directory) {
		fileChooser.setCurrentDirectory(directory);
		Program.instance().setCurrentDirectory(directory);
	}

	/**
	 * Sets the writing help value
	 * 
	 * @param value
	 */
	public void setWritingHelp(boolean value) {
		writingHelp = value;
	}

	/**
	 * Gets the writing help value
	 * 
	 * @return writingHelp
	 */
	public boolean getWritingHelp() {
		return writingHelp;
	}

	/**
	 * Displays the popup manager (to add methods) after "."
	 * 
	 * @param pane
	 *            the pane in Tangara where you are typing your code
	 * @param aClass
	 *            the class of the object that calls one of its methods
	 */
   private void setPopupDisplay(TextPane pane, Class<?> aClass) {
        if (popupManager != null) {
            popupManager.setVisible(false);
        }
        popupManager = new PopupManager(this, pane, aClass);
        popupManager.start();
    }

	
	/**
	 * Stop displaying the popup manager designed to choose methods.
	 */
	public void stopPopupDisplay() {
		if (popupManager != null) {
			popupManager.setVisible(false);
			popupManager = null;
		}
	}


	/**
	 * Sets the interface level in the options bar and the banner
	 * 
	 * @param level
	 *            advanced or basic
	 */
	public void setInterfaceLevel(int level) {
		if (level == Configuration.LEVEL_ADVANCED) {
			optionPanel.setVisible(true);
			msgButtonsMainPanel.setVisible(true);
			banner.setAdvancedLevel();
			console.setEnabled(true);
		} else // any other value : LEVEL_BASIC
		{
			//if (checkProgramChanged()) {
				optionPanel.setCommandMode();
				optionPanel.setVisible(false);
				msgButtonsMainPanel.setVisible(false);
				banner.setBasicLevel();
				console.setEnabled(false);
			//}
		}
		interfaceLevel = level;
	}

	/**
	 * Gets the interface level
	 * 
	 * @return the interface level (basic or advanced)
	 */
	public int getInterfaceLevel() {
		return interfaceLevel;
	}

	/**
	 * Changes the quote mode in accordance with the given string. There are
	 * three possible modes: SHARP, INTUITIVE, COLOR
	 * 
	 * @param newMode
	 */
	public void setQuoteMode(String newMode) {
		if (newMode.equals("SHARP")) { //$NON-NLS-1$
			quoteMode = QuoteMode.SHARP;
		} else if (newMode.equals("INTUITIVE")) { //$NON-NLS-1$
			quoteMode = QuoteMode.INTUITIVE;
		} else {
		    LOG.error("Unknown quote mode"); //$NON-NLS-1$
			Program.instance().writeMessage("Error - Unknown quote mode."); //$NON-NLS-1$
		}
	}

	/**
	 * Shows the program pane when you are in advanced level
	 * 
	 */
	public void showProgram() {
		if (getInterfaceLevel() > Configuration.LEVEL_BASIC) {
			optionPanel.setProgramMode();
		}
	}

	/**
	 * window listener to quit
	 */
	private WindowListener windowListener = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent evt) {
			exit(); // @jve:decl-index=0:
		}
	};

	// /////////// THE FRAME DESIGN STARTS HERE

	/**
	 * This method initializes the design of the frame.
	 * 
	 */
	private void initialize() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = graphicsEnvironment.getMaximumWindowBounds();
		setPreferredSize(bounds.getSize());

		this.setJMenuBar(getBanner());

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// Sets the main menu (the one separated by jsplitPane1)
		this.setContentPane(getBasePanel());
		this.setTitle(Messages.getString("EditorFrame.application.title")); //$NON-NLS-1$
		addWindowListener(this.windowListener);
		try {
			// Associates the icon (frame.icon = icon_tangara.png) to Tangara
			URL url = EditorFrame.class.getResource(ICON_PATH);
			MediaTracker attenteChargement = new MediaTracker(this);
			Image image = Toolkit.getDefaultToolkit().getImage(url);
			attenteChargement.addImage(image, 0);
			attenteChargement.waitForAll();
			setIconImage(image);
		} catch (InterruptedException e) {
			LOG.warn("Error while loading icon"); //$NON-NLS-1$
		}
		// fileChooser allows to easily choose a file
		// when you open (FILE->OPEN...) you have the choice between .txt or
		// .tgr
		fileChooser = new JFileChooser(Program.instance().getCurrentDirectory());

		// for TangaraFile ".tgr"
		fileChooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return FileUtils.isTangaraFile(f);
			}

			@Override
			public String getDescription() {
				return Messages
						.getString("EditorFrame.file.programFilesDescription"); //$NON-NLS-1$
			}
		});
		
		fileChooserWithoutFilter = new JFileChooser(Program.instance().getCurrentDirectory());
		pack();
		setVisible(true);
    	setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
	}

	/**
	 * Returns the cut action
	 * 
	 * @return
	 */
	public Action getCutAction() {
		if (cutAction == null) {
			cutAction = new AbstractAction(Messages
					.getString("Banner.menu.cut")) { //$NON-NLS-1$

				@Override
				public void actionPerformed(ActionEvent e) {
					cut();
				}
			};
			cutAction.setEnabled(false);
			cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
		}
		return cutAction;
	}

	/**
	 * Returns the copy action
	 * 
	 * @return
	 */
	public Action getCopyAction() {
		if (copyAction == null) {
			copyAction = new AbstractAction(Messages
					.getString("Banner.menu.copy")) { //$NON-NLS-1$

				@Override
				public void actionPerformed(ActionEvent e) {
					copy();
				}
			};
			copyAction.setEnabled(false);
			copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
		}
		return copyAction;
	}

	/**
	 * Returns the paste action
	 * 
	 * @return
	 */
	public Action getPasteAction() {
		if (pasteAction == null) {
			pasteAction = new AbstractAction(Messages
					.getString("Banner.menu.paste")) { //$NON-NLS-1$

				@Override
				public void actionPerformed(ActionEvent e) {
					paste();
				}
			};
			pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
		}

		return pasteAction;
	}

	/**
	 * Returns the undo action
	 * 
	 * @return
	 */
	public Action getUndoAction() {
		if (undoAction == null) {
			undoAction = new AbstractAction(Messages
					.getString("Banner.menu.undo")) { //$NON-NLS-1$

				@Override
				public void actionPerformed(ActionEvent e) {
					programUndo();
				}
			};
			undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
		}
		return undoAction;
	}

   /**
     * Returns the undo action
     * 
     * @return
     */
    public Action getRedoAction() {
        if (redoAction == null) {
            redoAction = new AbstractAction(Messages
                    .getString("Banner.menu.redo")) { //$NON-NLS-1$

                @Override
				public void actionPerformed(ActionEvent e) {
                    programRedo();
                }
            };
			redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
        }
        return redoAction;
    }

	

    
	/**
	 * This method initializes jSplitPane1
	 * 
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane1() {
		if (jSplitPane1 == null) {
			jSplitPane1 = new JSplitPane();
			jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane1.setTopComponent(getGraphicsPane());
			jSplitPane1.setBottomComponent(getControlPanel());
			jSplitPane1.setDividerSize(5);
		}
		return jSplitPane1;
	}

	/**
	 * This method initializes basePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getBasePanel() {
		if (basePanel == null) {
			basePanel = new JPanel();
			basePanel.setLayout(new BorderLayout());
			basePanel.add(getJSplitPane1(), BorderLayout.CENTER);
		}
		return basePanel;
	}

	/**
	 * This method initializes controlPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new JPanel();
			controlPanel.setLayout(new BorderLayout());
			controlPanel.add(getJSplitPane2(), BorderLayout.CENTER);
		}
		return controlPanel;
	}

	/**
	 * This method initializes jSplitPane2
	 * 
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane2() {
		if (jSplitPane2 == null) {
			jSplitPane2 = new JSplitPane();
			jSplitPane2.setDividerSize(5);
			jSplitPane2.setResizeWeight(1.0D);
			jSplitPane2.setTopComponent(getEditionPanel());
			
			jSplitPane2.setBottomComponent(getPageEnd());
			
			//jSplitPane2.setBottomComponent(getMsgScrollPane());
			jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		}
		return jSplitPane2;
	}
	
	/**
	 * This method initializes pageEnd
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPageEnd()
	{
		if(pageEnd == null)
		{
			pageEnd = new JPanel();
			pageEnd.setLayout(new BorderLayout());
			
			scrollPane = new JScrollPane(getConsole());
			
			pageEnd.add(scrollPane,BorderLayout.CENTER);
			
			pageEnd.add(getMsgButtonsMainPanel(),BorderLayout.NORTH);
		}
		return pageEnd;
	}
	
	/**
	 * This method initializes msgButtonsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMsgButtonsMainPanel()
	{
		if (msgButtonsMainPanel == null)
		{
			msgButtonsMainPanel = new JPanel();
			msgButtonsMainPanel.setLayout(new BorderLayout());

			msgButtonsMainPanel.setBackground(Color.white);

			msgButtonsMainPanel.setSize(new Dimension(811, 34));
			msgButtonsMainPanel.setPreferredSize(new Dimension(84, 25));
			msgButtonsMainPanel.add(getMsgButtonsPanel(), BorderLayout.WEST);
		}
		return msgButtonsMainPanel;
	}
	
	/**
	 * This method initializes msgButtonsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMsgButtonsPanel()
	{
		if (msgButtonsPanel == null)
		{
			msgButtonsPanel = new JPanel();
			msgButtonsPanel.setLayout(new BorderLayout());
			msgButtonsPanel.setBackground(Color.white);
			
			
			msgButtonsPanel.add(getMsgButtons(), BorderLayout.WEST);
			
			JLabel endIcon = new JLabel();
			endIcon.setText(""); //$NON-NLS-1$
			endIcon.setBackground(Color.white);
			endIcon.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/main_end.png"))); //$NON-NLS-1$
			msgButtonsPanel.add(endIcon, BorderLayout.EAST);
		}
		return msgButtonsPanel;
	}
	
	/**
	 * This method initializes console
	 * 
	 * @return org.colombbus.tangara.Console
	 */
	private LogConsole getConsole() {
		if (console == null) {
			console = new LogConsole(this);
			console.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (toProgramLabel != null) {
						if (console.isCodeSelected()) {
							if (!toProgramLabel.isEnabled()) {
								toProgramLabel.setEnabled(true);
					    		toProgramLabel.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/to_program.png"))); //$NON-NLS-1$
							}
						} else if (toProgramLabel.isEnabled()) {
							toProgramLabel.setEnabled(false);
			    	    	toProgramLabel.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/to_program_off.png"))); //$NON-NLS-1$
						}
					}
				}
			});
		}
		return console;
	}
	
	/**
	 * This method initializes msgButtons
	 * 	
	 * @return javax.swing.JPanel
	 */
	private JPanel getMsgButtons()
	{
		if (msgButtons == null)
		{
			toProgramLabel = new JLabel();
			toProgramLabel.setText(Messages.getString("EditorFrame.button.copyInProgramMode")); //$NON-NLS-1$
			toProgramLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 13)); //$NON-NLS-1$
			toProgramLabel.setForeground(new Color(60, 87, 174));
			toProgramLabel.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/to_program_off.png"))); //$NON-NLS-1$
			toProgramLabel.setEnabled(false);
			toProgramLabel.addMouseListener(new java.awt.event.MouseAdapter()
			{
				@Override
				public void mousePressed(java.awt.event.MouseEvent e)
				{
					console.insertCodeToProgram();
				}   
				@Override
				public void mouseExited(java.awt.event.MouseEvent e)
				{    
					if(!noCodeSelected)
					{
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						toProgramLabel.setForeground(new Color(60, 87, 174));
					}
				}
				@Override
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
					if(!noCodeSelected)
					{
						setCursor(new Cursor(Cursor.HAND_CURSOR));
						toProgramLabel.setForeground(new Color(100, 100, 255));
					}
				}
			});
			
			selectAllLabel = new JLabel();
			selectAllLabel.setText(Messages.getString("EditorFrame.button.selectAll")); //$NON-NLS-1$
			selectAllLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 13)); //$NON-NLS-1$
			selectAllLabel.setForeground(new Color(60, 87, 174));
			selectAllLabel.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/select_all.png"))); //$NON-NLS-1$
			selectAllLabel.addMouseListener(new java.awt.event.MouseAdapter()
			{
				@Override
				public void mousePressed(java.awt.event.MouseEvent e)
				{
					console.selectAll();
				}   
				@Override
				public void mouseExited(java.awt.event.MouseEvent e)
				{    
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					selectAllLabel.setForeground(new Color(60, 87, 174));
				}
				@Override
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
					setCursor(new Cursor(Cursor.HAND_CURSOR));
					selectAllLabel.setForeground(new Color(100, 100, 255));
				}
			});
			
			deselectAllLabel = new JLabel();
			deselectAllLabel.setText(Messages.getString("EditorFrame.button.deselectAll")); //$NON-NLS-1$
			deselectAllLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 13)); //$NON-NLS-1$
			deselectAllLabel.setForeground(new Color(60, 87, 174));
			deselectAllLabel.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/select_none.png"))); //$NON-NLS-1$
			deselectAllLabel.addMouseListener(new java.awt.event.MouseAdapter()
			{
				@Override
				public void mousePressed(java.awt.event.MouseEvent e)
				{
					console.clearSelection();
				}   
				@Override
				public void mouseExited(java.awt.event.MouseEvent e)
				{    
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					deselectAllLabel.setForeground(new Color(60, 87, 174));
				}
				@Override
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
					setCursor(new Cursor(Cursor.HAND_CURSOR));
					deselectAllLabel.setForeground(new Color(100, 100, 255));
				}
			});
			
			JLabel separatorLabel1 = new JLabel("        ");			 //$NON-NLS-1$
			JLabel separatorLabel2 = new JLabel("        "); //$NON-NLS-1$
			
			msgButtons = new JPanel();
			msgButtons.setLayout(new FlowLayout());
			msgButtons.setBackground(new Color(240,240,240));
			msgButtons.add(toProgramLabel);
			msgButtons.add(separatorLabel1);
			msgButtons.add(selectAllLabel);
			msgButtons.add(separatorLabel2);
			msgButtons.add(deselectAllLabel);
		}
		return msgButtons;
	}
	
	/**
	 * This method writes a message on the MsgTable
	 * 
	 */
	@Override
	public void addLogMsg(String message, int style, int lineNumber) {
		if (style == LogConsole.STYLE_ERROR && lineNumber>-1 && programIndex > -1) {
			message = MessageFormat.format(Messages.getString("EditorFrame.error.lineNumber"),message, lineNumber+1);
		}
		console.log(message, style, lineNumber, programIndex);			
	}
	
	@Override
	public int getCurrentLogIndex() {
		return console.getCurrentIndex();
	}
	
	@Override
	public void setErrorLines(int index, int number, int errorLineNumber) {
		console.setErrors(index, number, errorLineNumber, programIndex);
	}
	
	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getCommandPanel() {
		if (commandPanel == null) {
			commandPanel = new JPanel();
			commandPanel.setLayout(new BorderLayout());
			commandPanel.add(getCommandPane(), BorderLayout.CENTER);
			commandPanel.add(getCmdButtonPanel(), BorderLayout.EAST);
		}
		return commandPanel;
	}

	/**
	 * This method initializes cmdButtonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getCmdButtonPanel() {
		if (cmdButtonPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.insets = new Insets(4, 9, 18, 4);
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.ipadx = 60;
			gridBagConstraints1.ipady = -1;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(16, 9, 4, 4);
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipadx = 49;
			gridBagConstraints.ipady = -1;
			gridBagConstraints.gridx = 0;
			cmdButtonPanel = new JPanel();
			cmdButtonPanel.setBackground(new Color(197,214,219));//Color(156, 199, 213));
			cmdButtonPanel.setBorder(BorderFactory.createLineBorder(new Color(
					102, 102, 102), 1));
			cmdButtonPanel.setPreferredSize(new Dimension(150, 140));
			cmdButtonPanel.setLayout(new GridBagLayout());
			cmdButtonPanel.add(getCmdRunButton(), gridBagConstraints);
			cmdButtonPanel.add(getRefreshButton(), gridBagConstraints1);
		}
		return cmdButtonPanel;
	}

	/**
	 * This method initializes cmdRunButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getCmdRunButton() {
		if (cmdRunButton == null) {
			cmdRunButton = new JButton();
			cmdRunButton.setForeground(new Color(51, 51, 51));
			cmdRunButton.setBackground(new Color(156, 199, 213));
			cmdRunButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13)); //$NON-NLS-1$
			cmdRunButton.setIcon(new ImageIcon(getClass().getResource(
					"/org/colombbus/tangara/control_play_blue.png"))); //$NON-NLS-1$
			cmdRunButton.setPreferredSize(new Dimension(86, 30));
			cmdRunButton
					.setText(Messages.getString("EditorFrame.button.execute")); //$NON-NLS-1$
			cmdRunButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					executeInputScript();
				}
			});
		}
		return cmdRunButton;
	}

	/**
	 * This method initializes refreshButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getRefreshButton() {
		if (refreshButton == null) {
			refreshButton = new JButton();
			refreshButton.setBackground(new Color(156, 199, 213));
			refreshButton.setText(Messages.getString("EditorFrame.button.clean")); //$NON-NLS-1$
			refreshButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13)); //$NON-NLS-1$
			refreshButton.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/cross.png"))); //$NON-NLS-1$
			refreshButton.setPreferredSize(new Dimension(75, 30));
			refreshButton.setForeground(new Color(51, 51, 51));
			refreshButton.addActionListener(new java.awt.event.ActionListener() {
			    @Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					graphicsPane.freeze(true);
					String message = Messages.getString("EditorFrame.confirmClean.text"); //$NON-NLS-1$
					String title = Messages.getString("EditorFrame.confirmClean.title"); //$NON-NLS-1$
					Object[] options = {Messages.getString("tangara.yes"),Messages.getString("tangara.cancel") }; //$NON-NLS-1$ //$NON-NLS-2$
					int answer = JOptionPane.showOptionDialog(EditorFrame.this, message, title,optionType,JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (answer == JOptionPane.OK_OPTION) {
						refresh();
					}
					graphicsPane.freeze(false);
					commandPane.requestFocus();
				}
			});
		}
		return refreshButton;
	}

	/**
	 * This method refreshs the state of Tangara : delete every object and erase
	 * history.
	 * 
	 */
	private void refresh() {
		Program.instance().reset();
		clearCommandPane();
		console.clear();
		commandHistory = 0;
	}

	/**
	 * This method initializes and returns commandPane, for the command mode
	 * window.
	 * 
	 * @return javax.swing.JEditorPane
	 */
	public TextPane getCommandPane() {
		if (commandPane == null) {
		    commandPane = new TextPane(this, TextPane.getProperties(true), true);
		    commandPane.setBorder(BorderFactory.createLineBorder(new Color(102, 102, 102), 1));
	    }
		return commandPane;
    }

	public void historyUp() {
        commandHistory++;
        String previousCommand = Program.instance()
                .getCommandFromHistory(commandHistory);
        if (previousCommand != null) {
            clearCommandPane();
            commandPane.getBuffer().insert(0, previousCommand);
        } else // end of history has been reached
        {
            commandHistory--;
        }
	}
	
	public void historyDown() {
        if (commandHistory > 0) {
            commandHistory--;
            if (commandHistory == 0) {
                clearCommandPane();
            } else {
                String nextCommand = Program.instance()
                        .getCommandFromHistory(
                                commandHistory);
                if (nextCommand != null) {
                    clearCommandPane();
                    commandPane.getBuffer().insert(0, nextCommand);
                }
            }
        }
	}
	
	public void clearCommand() {
        if (commandHistory > 0) {
            commandHistory = 0;
            clearCommandPane();
        }
	}

	public void displayWritingHelp(Class<?> objectClass) {
	    if (writingHelp) {
	        if (commandMode)
                setPopupDisplay(commandPane, objectClass);
	        else
                setPopupDisplay(getProgramManager().getCurrentPane(), objectClass);
        }
	}
	
	/**
	 * Get panel containing the game panel
	 * 
	 * @return the panel containing the game area
	 */
	@Override
	public GraphicsPane getGraphicsPane() {
		if (graphicsPane == null) {
			try {
				try {
					String language = Configuration.instance().getLanguage();
					String className = "org.colombbus.tangara." + language //$NON-NLS-1$
							+ ".GraphicsPane_" + language; //$NON-NLS-1$
					Class<?> type = Class.forName(className);
					graphicsPane = (GraphicsPane) type.newInstance();

				} catch (ClassNotFoundException e) { 
					// If the language is unknown, the English version used.
					String className = "org.colombbus.tangara.en.GraphicsPane_en"; //$NON-NLS-1$
					Class<?> type;
					type = Class.forName(className);
					graphicsPane = (GraphicsPane) type.newInstance();
				}
			} catch (Exception e) {
				LOG.error("error get GraphisPane", e); //$NON-NLS-1$
			}

			graphicsPane.setLayout(null);
			graphicsPane.setBackground(Color.white);
		}
		return graphicsPane;
	}

	/**
	 * Initializes the banner of Tangara window
	 * 
	 * @return Tangara banner
	 */
	private Banner getBanner() {
		if (banner == null) {
			banner = new Banner(this.configuration, this);
		}
		return banner;
	}

	/**
	 * This method initializes editionPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getEditionPanel() {
		if (editionPanel == null) {
			editionPanel = new JPanel();
			editionPanel.setLayout(new BorderLayout());
			editionPanel.add(getOptionPanel(), BorderLayout.NORTH);
			editionPanel.add(getModePanel(), BorderLayout.CENTER);
		}
		return editionPanel;
	}

	/**
	 * This method initializes modePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getModePanel() {
		if (modePanel == null) {
			modePanel = new JPanel();
			modePanel.setLayout(new CardLayout());
			modePanel.setBackground(new Color(220,220,220));
			modePanel.add(getCommandPanel(), "commandMode"); //$NON-NLS-1$
			modePanel.add(getProgramManager(), "programMode"); //$NON-NLS-1$
		}
		return modePanel;
	}

	/**
	 * This method initializes optionPanel
	 * 
	 * @return org.colombbus.tangara.OptionPanel
	 */
	private OptionPanel getOptionPanel()
	{
		if (optionPanel == null) {
			optionPanel = new OptionPanel(this);
		}
		return optionPanel;
	}
	
    
    public TextPaneManager getProgramManager() {
    	if (programManager == null) {
    		programManager = new TextPaneManager(this);
    		programManager.setPreferredSize (new Dimension (410, 50));
    	}
    	return programManager;
    }
	
	/**
	 * Undo the last command
	 * 
	 */
	public void programUndo() {
		getProgramManager().getCurrentPane().undo();
	}

	/**
	 * Redo the last command
	 * 
	 */
	public void programRedo() {
		getProgramManager().getCurrentPane().redo();
	}

	public void enableUndo() {
	    if (undoAction != null)
	        undoAction.setEnabled(true);
	}
	
	public void disableUndo() {
        if (undoAction != null)
            undoAction.setEnabled(false);
	}
	
	public void enableRedo() {
        if (redoAction != null)
            redoAction.setEnabled(true);
	}
	
	public void disableRedo() {
        if (redoAction != null)
            redoAction.setEnabled(false);
	}
	
	/**
	 * Resets all undo managers
	 * 
	 */
	public void programResetUndo() {
		// Reset undo manager
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
	}

	@Override
	public boolean computeSize() {
		// Do not compute the size of the main frame
		return false;
	}
	
	public void displaySearch() {
		getProgramManager().displaySearch();
	}
	
	 public Action getSearchAction() {
		 return getProgramManager().getSearchAction();
	 }
	 
	 public CommandTransferHandler getCommandHandler() {
		 if (commandHandler == null) {
			 commandHandler = new CommandTransferHandler();
		 }
		 return commandHandler;
	 }
	 
	 public void setFocusOwner(Editable component) {
		 focusOwner = component;
		 if (component.mayCopy())
			 this.enableEdit();
		 else
			 this.disableEdit();
	 }
	 
	 public void setDisplayLineNumbers(boolean state) {
		 programManager.setDisplayLineNumbers(state);
		 displayLineNumbers = state;
	 }
	
	 public boolean getDisplayLineNumbers() {
		 return displayLineNumbers;
	 }
	 
	 public void selectLine(int programIndex, int lineNumber) {
		 // 1st set Program Mode
		 setProgramMode();
		 // 2nd ask the manager to select and show the required line
		 programManager.selectLine(programIndex, lineNumber);
	 }
}
