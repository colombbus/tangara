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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.colombbus.tangara.io.ScriptWriter;

/**
 * This class is used when the child wants to save his program (only in command mode) or to create a new one.
 * It is composed of several steps according to the mode. <br>
 * First mode : when you choose Save in the file menu. This is composed of two steps. First you choose the command
 * you want to import. Then you can modify, add, remove commands before saving the program. <br>
 * Second mode : when you choose Create a program in the file menu. This is composed of 3 steps. The first two
 * are the same as before. Third, you can choose to display your program in the program window (so, in program mode)
 * or you can choose to save your program
 *
 * @author gwen
 *
 */
@SuppressWarnings("serial")
public class CommandSelection extends JDialog
{
	/** Class logger */
	private Logger LOG = Logger.getLogger(CommandSelection.class);  //  @jve:decl-index=0:

	private JPanel mainPanel = null;
	private JList commandList = null;
	private JScrollPane jScrollPane = null;
	private JPanel panelButtons = null;
	private JButton leftButton = null;
	private JButton rightButton = null;
	private JLabel helpText = null;
	private JPanel centralPanel = null;
	private JTextPane fileContents = null;

	private boolean selecting = false;
	private int previousIndex = -1;
	private int currentState = -1;
	private int mode = -1;
	private EditorFrame myParent = null;

	public static final int MODE_TEXTFILE_CREATION = 1;
	public static final int MODE_PROGRAM_CREATION = 2;
	public static final int MODE_COMMANDS_INSERTION = 3;

	private static final int STATE_COMMANDS_SELECTION = 1;
	private static final int STATE_FILE_EDITION = 2;
	private static final int STATE_PROGRAM_DESTINATION=3;


	private JPanel panelCommandSelection = null;
	private JPanel panelSelection = null;
	private JButton buttonSelectAll = null;
	private JButton buttonSelectNone = null;
	private JScrollPane panelFileContents = null;

	private static final int iconSize = 10;
	private static final Color selectedForegroundColor = Color.black;  //  @jve:decl-index=0:
	private static final Color selectedBackgroundColor = new Color(240,255,240);  //  @jve:decl-index=0:


	private ImageIcon iconNotSelected;  //  @jve:decl-index=0:
	private ImageIcon iconSelected;  //  @jve:decl-index=0:
	private JPanel panelProgramDestination = null;
	private JRadioButton buttonSaveProgram = null;
	private JRadioButton buttonDisplayProgram = null;

	private static final int MARGIN_Y = 10;
	private static final int MARGIN_X = 5;
	private static final int MARGIN_BUTTON = 50;
	private static final int WIDTH_BUTTON = 200;
	private static final int HEIGHT_BUTTON = 30;

	/**
	 * Creates a new instance of CommandSelection from Tangara frame
	 *
	 */
	public CommandSelection(EditorFrame parent, boolean modal, int mode)
	{
		super(parent, modal);
		this.mode = mode;
		myParent = parent;
		initializeIcons();
		initialize();
        ButtonGroup programDestinationGrp = new ButtonGroup();
        programDestinationGrp.add(buttonSaveProgram);
        programDestinationGrp.add(buttonDisplayProgram);
		changeState(STATE_COMMANDS_SELECTION);
		this.setLocation(new Point((parent.getX()+(parent.getWidth()-getWidth())/2),(parent.getY()+(parent.getHeight()-getHeight())/2)));
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run()
			{
				commandList.setSelectionInterval(0, commandList.getModel().getSize()-1);
			}
		});
	}

	/**
	 * Initializes the icons
	 *
	 */
	private void initializeIcons()
	{
		BufferedImage imageSelected = new BufferedImage(iconSize,iconSize,BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)imageSelected.getGraphics();
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(selectedBackgroundColor);
    	g.fillRect(0, 0, iconSize, iconSize);
    	g.setColor(Color.green);
		g.fillOval(0, 0, iconSize-1, iconSize-1);
		iconSelected = new ImageIcon(imageSelected);
		BufferedImage imageNotSelected = new BufferedImage(iconSize,iconSize,BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D)imageNotSelected.getGraphics();
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(0, 0, iconSize, iconSize);
		g.setColor(Color.lightGray);
		g.fillOval(0, 0, iconSize-1, iconSize-1);
		iconNotSelected = new ImageIcon(imageNotSelected);
	}


	/**
	 * Inserts commands into Tangara frame to execute them
	 *
	 */
	private void insertCommands()
	{
		String commands;
		// we compute commands which are then written in the fileContents component which is hidden
		computeFileContents();
		commands = fileContents.getText();
		myParent.insertCommands(commands);
		exit();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
        this.setSize(new Dimension(609, 427));
        this.setContentPane(getMainPanel());
        commandList.setCellRenderer(new MyCellRenderer());
       	commandList.setListData(Program.instance().getHistory());
        switch (mode)
        {
        	case MODE_TEXTFILE_CREATION:
        		// file saving
            	this.setTitle(Messages.getString("CommandSelection.fileCreation.title"));
        		break;
        	case MODE_PROGRAM_CREATION:
        		// program saving
            	this.setTitle(Messages.getString("CommandSelection.programCreation.title"));
        		break;
        	case MODE_COMMANDS_INSERTION:
        		// command insertion in program mode
            	this.setTitle(Messages.getString("CommandSelection.commandsInsertion.title"));
        		break;
        }
		centralPanel.setBorder(BorderFactory.createLineBorder(new Color(127,157,185)));
	}

	/**
	 * This method initializes mainPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getMainPanel()
	{
		if (mainPanel == null)
		{
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(getHelpText(), BorderLayout.NORTH);
			mainPanel.add(Box.createHorizontalStrut(MARGIN_X), BorderLayout.WEST);
			mainPanel.add(getCentralPanel(), BorderLayout.CENTER);
			mainPanel.add(Box.createHorizontalStrut(MARGIN_X), BorderLayout.EAST);
			mainPanel.add(getPanelButtons(), BorderLayout.SOUTH);
		}
		return mainPanel;
	}

	/**
	 * This method initializes commandList
	 *
	 * @return javax.swing.JList
	 */
	private JList getCommandList()
	{
		if (commandList == null)
		{
			commandList = new JList();
		}
		commandList.setSelectionBackground(selectedBackgroundColor);
		commandList.setSelectionForeground(selectedForegroundColor);
		commandList.setBackground(Color.white);
		commandList.setForeground(Color.gray);
		MouseListener[] mouseListeners = commandList.getMouseListeners();
		for (MouseListener m:mouseListeners)
		{
			commandList.removeMouseListener(m);
		}

		MouseMotionListener[] motionListeners = commandList.getMouseMotionListeners();
		for (MouseMotionListener m:motionListeners)
		{
			commandList.removeMouseMotionListener(m);
		}

		commandList.addMouseListener(new MouseAdapter(){
         	@Override
			public void mousePressed(MouseEvent e)
         	{
         		int index = commandList.locationToIndex(e.getPoint());
    			if (commandList.isSelectedIndex(index))
     			{
     				commandList.removeSelectionInterval(index,index);
     				selecting = false;
     			}
     			else
     			{
     				commandList.addSelectionInterval(index,index);
     				selecting = true;
     			}
     			previousIndex = index;
         	}

         	@Override
			public void mouseReleased(MouseEvent e)
         	{
         		previousIndex = -1;
         	}
		});
		commandList.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e)
			{
				int index = commandList.locationToIndex(e.getPoint());
				if (index != previousIndex)
				{
					if (previousIndex>-1)
					{
						if (index>previousIndex)
						{
							if (selecting)
							{
			     				commandList.addSelectionInterval(previousIndex+1,index);
							}
							else
							{
			     				commandList.removeSelectionInterval(previousIndex+1,index);
							}
						}
						else
						{
							if (selecting)
							{
			     				commandList.addSelectionInterval(index,previousIndex-1);
							}
							else
							{
			     				commandList.removeSelectionInterval(index,previousIndex-1);
							}
						}
					}
					else
					{
						if (selecting)
						{
		     				commandList.addSelectionInterval(index,index);
						}
						else
						{
		     				commandList.removeSelectionInterval(index,index);
						}
					}
					commandList.ensureIndexIsVisible(index);
					previousIndex = index;
				}
			}
		});


		return commandList;
	}

	/**
	 * Identifies components that can be used as "rubber stamps" to paint the cells in commandList
	 * @author gwen
	 *
	 */
	private class MyCellRenderer extends JLabel implements ListCellRenderer
	{

		/**
		 * Return a component that has been configured to display the specified value.
		 * That component's paint method is then called to "render" the cell.
		 * If it is necessary to compute the dimensions of a list because the list cells do not have a fixed size,
		 *  this method is called to generate a component on which getPreferredSize  can be invoked.
		 *  @param
		 *  		 The JList we're painting.
		 *  @param
    	 *			 The value returned by list.getModel().getElementAt(index).
    	 *  @parma
    	 *  		The cells index.
    	 *  @param
    	 *  		True if the specified cell was selected.
    	 *  @param
    	 *  		True if the specified cell has the focus.
		 */
		@Override
		public Component getListCellRendererComponent(JList list,Object value, int index, boolean isSelected, boolean cellHasFocus)
	     {
	    	 if (!((HistoryRecord)value).getResult())
	    	 {
	    		 String s = ((HistoryRecord)value).getCommand();
		         setText(s);
		         setForeground(Color.red);
		         this.setPreferredSize(new Dimension(0,0));
		         return this;
	    	 }
	    	 else
	    	 {
		         String s = ((HistoryRecord)value).getCommand();
		         setText(s);
		           if (isSelected) {
		        	   setBackground(list.getSelectionBackground());
		               setForeground(list.getSelectionForeground());
		               setIcon(iconSelected);
		           }
		         else {
		               setBackground(list.getBackground());
		               setForeground(list.getForeground());
		               setIcon(iconNotSelected);
		           }
		           setEnabled(list.isEnabled());
		           setFont(list.getFont());
		         setOpaque(true);
		         this.setPreferredSize(new Dimension(1000,15));
		         return this;
	    	 }

	     }
	 }

	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane()
	{
		if (jScrollPane == null)
		{
			jScrollPane = new JScrollPane();
			jScrollPane.setName("jScrollPane");
			jScrollPane.setViewportView(getCommandList());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes panelButtons
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelButtons()
	{
		if (panelButtons == null)
		{
			panelButtons = new JPanel();
			panelButtons.setBorder(new EmptyBorder(MARGIN_Y, MARGIN_X, MARGIN_Y, MARGIN_X));
			panelButtons.setLayout(new BoxLayout(panelButtons,BoxLayout.X_AXIS));
			panelButtons.add(Box.createHorizontalStrut(MARGIN_BUTTON));
			panelButtons.add(getLeftButton(), null);
			panelButtons.add(Box.createHorizontalGlue());
			panelButtons.add(getRightButton(), null);
			panelButtons.add(Box.createHorizontalStrut(MARGIN_BUTTON));
		}
		return panelButtons;
	}

	/**
	 * This method initializes leftButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getLeftButton()
	{
		if (leftButton == null)
		{
			leftButton = new JButton();
			leftButton.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			leftButton.setMaximumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			leftButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					doBack();
				}
			});
		}
		return leftButton;
	}

	/**
	 * This method initializes rightButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getRightButton()
	{
		if (rightButton == null)
		{
			rightButton = new JButton();
			rightButton.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			rightButton.setMaximumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			rightButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					doNext();
				}
			});
		}
		return rightButton;
	}


	/**
	 * This method initializes jPanel5
	 *
	 * @return javax.swing.JPanel
	 */
	private JLabel getHelpText()
	{
		if (helpText == null)
		{
			helpText = new JLabel();
			helpText.setText(" ");
			EmptyBorder border = new EmptyBorder(MARGIN_Y,MARGIN_X,MARGIN_Y,MARGIN_X);
			helpText.setBorder(border);
		}
		return helpText;
	}

	/**
	 * This method initializes centralPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getCentralPanel()
	{
		if (centralPanel == null)
		{
			centralPanel = new JPanel();
			centralPanel.setLayout(new CardLayout());
			centralPanel.add(getPanelCommandSelection(), "commandList");
			centralPanel.add(getPanelFileContents(), "fileEdition");
			centralPanel.add(getPanelProgramDestination(), "programDestination");
		}
		return centralPanel;
	}

	/**
	 * This method initializes fileContents
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextPane getFileContents()
	{
		if (fileContents == null)
		{
			fileContents = new JTextPane();
			fileContents.setName("fileContents");
		}
		return fileContents;
	}

	/**
	 * Selects the state of the command selection
	 * @param state
	 * 			the state/step of command selection
	 */
	private void changeState(int state)
	{
		switch (state)
		{
			case STATE_COMMANDS_SELECTION :
				//command selection step 1
				switch (mode)
				{
					case MODE_PROGRAM_CREATION:
						// creates program
						helpText.setText(Messages.getString("CommandSelection.programCreation.commandsSelection"));
					case MODE_TEXTFILE_CREATION:
						// saves
						helpText.setText(Messages.getString("CommandSelection.fileCreation.commandsSelection"));
						leftButton.setText(Messages.getString("CommandSelection.cancelButton"));
						rightButton.setText(Messages.getString("CommandSelection.nextButton"));
						((CardLayout)centralPanel.getLayout()).show(centralPanel,"commandList");
						break;
					case MODE_COMMANDS_INSERTION:
						//  insertion in program mode
						helpText.setText(Messages.getString("CommandSelection.commandsInsertion.commandsSelection"));
						leftButton.setText(Messages.getString("CommandSelection.cancelButton"));
						rightButton.setText(Messages.getString("CommandSelection.insertButton"));
						((CardLayout)centralPanel.getLayout()).show(centralPanel,"commandList");
						break;
				}
				break;
			case STATE_FILE_EDITION :
				// command edit step 2
				if (currentState == STATE_COMMANDS_SELECTION)
				{
					computeFileContents();
				}
				if (mode == MODE_PROGRAM_CREATION)
					// create program
					helpText.setText(Messages.getString("CommandSelection.programCreation.fileEdition"));
				else
					// save program
					helpText.setText(Messages.getString("CommandSelection.fileCreation.fileEdition"));
				leftButton.setText(Messages.getString("CommandSelection.previousButton"));
				if ((mode == MODE_PROGRAM_CREATION)&&(myParent.getInterfaceLevel()==Configuration.LEVEL_ADVANCED))
				{
					//create program ->third step
					rightButton.setText(Messages.getString("CommandSelection.nextButton"));
				}
				else
				{
					// save program -> to save
					rightButton.setText(Messages.getString("CommandSelection.saveButton"));
				}
				((CardLayout)centralPanel.getLayout()).show(centralPanel,"fileEdition");
				break;
			case STATE_PROGRAM_DESTINATION :
				// third step only in create program
				helpText.setText(Messages.getString("CommandSelection.programCreation.programDestination"));
				leftButton.setText(Messages.getString("CommandSelection.previousButton"));
				rightButton.setText(Messages.getString("CommandSelection.nextButton"));
				((CardLayout)centralPanel.getLayout()).show(centralPanel,"programDestination");
				break;
		}
		currentState = state;
	}

	/**
	 * Gets each command from history
	 *
	 */
	private void computeFileContents()
	{
		fileContents.setText("");
		boolean firstLine = true;
		Object[] selected = commandList.getSelectedValues();
		boolean first = true;
		for (Object o :selected)
		{
			// last check not to add erroneous commands
			HistoryRecord command = (HistoryRecord)o;
			if (command.getResult())
			{
				String string = command.getCommand();
				string = string.replace("\r", "");
				if ((!string.endsWith(";"))&&(!string.endsWith("{"))&&(!string.endsWith("}")))
						string+=";";
				if (!first)
				    string="\n"+string;
				if (firstLine)
				{
					fileContents.setText(fileContents.getText() + string);
					firstLine = false;
				}
				else
					fileContents.setText(fileContents.getText() + string);
				first = false;
			}
		}
		fileContents.setFont(myParent.getFontParameter());
		myParent.setTabSizeOf(fileContents);
	}

	/**
	 * Closes the command selection window
	 *
	 */
	private void exit()
	{
		this.setVisible(false);
		this.dispose();
	}

	/**
	 * Enables to save the program  in a file (step 2 of save or step 3 of create program)
	 * @return
	 * 		the path of the saved file
	 */
	private File saveFile()
	{
		File destinationFile = null;
		JFileChooser fileChooser = createFileChooser();
		int userChoice = fileChooser.showSaveDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION)
        {
        	File selectedFile = fileChooser.getSelectedFile();
        	destinationFile = buildDestFile(selectedFile);
        	
            if (destinationFile.exists()
					&& userConfirmOverride(destinationFile) == false) {
				return null;
			}
            
            try {
				saveBufferToFile(destinationFile);
				succeedSavingFile(fileChooser, destinationFile);
			} catch (Throwable th) {
				failSavingFile(destinationFile, th);
			}
        }
    	return destinationFile;
	}

	private void succeedSavingFile(JFileChooser fileChooser,
			File destinationFile) {
		String message;
		if (mode == MODE_PROGRAM_CREATION)
			message = MessageFormat.format(Messages.getString("CommandSelection.programCreation.saveOk"), destinationFile.getName());
		else
			message = MessageFormat.format(Messages.getString("CommandSelection.fileCreation.saveOk"), destinationFile.getName());
		myParent.printInfo(message);		
		myParent.setCurrentDirectory(fileChooser.getCurrentDirectory());
	}

	private void failSavingFile(File destinationFile, Throwable th) {
		LOG.error("Unable to write to file \""+destinationFile.getAbsolutePath()+"\"", th);
		myParent.printError(MessageFormat.format(Messages.getString("CommandSelection.saveError"), destinationFile.getName()));
	}
	
	private void saveBufferToFile( File destFile) throws IOException {
		OutputStream out=null;
		try {
			out = new FileOutputStream(destFile);
        	String script = fileContents.getText();
			ScriptWriter writer = new ScriptWriter(Configuration.instance().getScriptHeader());
			writer.writeScript(script, out);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private JFileChooser createFileChooser() {
		String title;
		FileFilter fileFilter;
		if (mode == MODE_PROGRAM_CREATION) {
			title = Messages
					.getString("CommandSelection.programCreation.save.title");
			fileFilter = createTangaraFileFilter();
		} else {
			title = Messages
					.getString("CommandSelection.fileCreation.save.title");
			fileFilter = createTextFileFilter();
		}

		JFileChooser fileChooser = new JFileChooser(myParent
				.getCurrentDirectory());
		fileChooser.setDialogTitle(title);
		fileChooser.addChoosableFileFilter(fileFilter);
		return fileChooser;
	}

	private FileFilter createTangaraFileFilter() {
		return new FileFilter() {
		    @Override
			public boolean accept(File file)
		    {
		        if (file.isDirectory())
		            return true;
		        return FileUtils.isTangaraFile(file);
		    }

		    @Override
			public String getDescription()
		    {
		        return Messages.getString("EditorFrame.file.programFilesDescription");
		    }
		};
	}

	private FileFilter createTextFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File file)
		    {
		        if (file.isDirectory())
		            return true;
		        return FileUtils.isTextFile(file);
		    }

		    @Override
			public String getDescription()
		    {
		        return Messages.getString("EditorFrame.file.commandFilesDescription");
		    }
		};
	}

	private File buildDestFile(File destinationFile) {
		String fileName = destinationFile.getAbsolutePath();
		if (mode == MODE_PROGRAM_CREATION)
		{
			if( FileUtils.isTangaraFile(fileName)==false)
			{
				// .tgr
				fileName = fileName + '.' + FileUtils.getTangaraFileExt();
				destinationFile = new File(fileName);
			}
		}
		else if (FileUtils.isTextFile(fileName)==false)
		{
			fileName = fileName + '.' + FileUtils.getTextFileExt();
			destinationFile = new File(fileName);
		}
		return destinationFile;
	}
	
	private boolean userConfirmOverride(File destinationFile) {
    	String title;
    	String pattern;
    	if (mode == MODE_PROGRAM_CREATION)
    	{
        	title = Messages.getString("CommandSelection.programCreation.override.title");
			pattern = Messages.getString("CommandSelection.programCreation.override.message");
    	}
    	else
    	{
        	title = Messages.getString("CommandSelection.fileCreation.override.title");
			pattern = Messages.getString("CommandSelection.fileCreation.override.message");
    	}
		String message = MessageFormat.format(pattern, destinationFile.getName());
		Object[] options = {Messages.getString("tangara.yes"), Messages.getString("tangara.cancel")};
		int optionType = JOptionPane.OK_CANCEL_OPTION;
		int messageType = JOptionPane.QUESTION_MESSAGE;
		Icon icon = null;
		int answer = JOptionPane.showOptionDialog(this, message, title,
				optionType, messageType, icon, options, options[0]);
		return answer == JOptionPane.OK_OPTION;
	}

	/**
	 * This method initializes panelCommandSelection
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelCommandSelection()
	{
		if (panelCommandSelection == null)
		{
			panelCommandSelection = new JPanel();
			panelCommandSelection.setLayout(new BorderLayout());
			panelCommandSelection.setName("panelCommandSelection");
			panelCommandSelection.add(getPanelSelection(), BorderLayout.NORTH);
			panelCommandSelection.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return panelCommandSelection;
	}

	/**
	 * This method initializes panelSelection
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelSelection()
	{
		if (panelSelection == null)
		{
			panelSelection = new JPanel();
			panelSelection.setBorder(new EmptyBorder(MARGIN_Y/2,MARGIN_X,MARGIN_Y/2,MARGIN_X));
			panelSelection.setLayout(new BoxLayout(panelSelection,BoxLayout.X_AXIS));
			panelSelection.add(Box.createHorizontalStrut(MARGIN_BUTTON));
			panelSelection.add(getButtonSelectAll());
			panelSelection.add(Box.createHorizontalGlue());
			panelSelection.add(getButtonSelectNone());
			panelSelection.add(Box.createHorizontalStrut(MARGIN_BUTTON));
		}
		return panelSelection;
	}


	/**
	 * This method initializes buttonSelectAll
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonSelectAll()
	{
		if (buttonSelectAll == null)
		{
			buttonSelectAll = new JButton();
			buttonSelectAll.setText(Messages.getString("CommandSelection.selectAllButton"));
			buttonSelectAll.setMinimumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonSelectAll.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonSelectAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					commandList.setSelectionInterval(0, commandList.getModel().getSize()-1);
				}
			});
		}
		return buttonSelectAll;
	}

	/**
	 * This method initializes buttonSelectNone
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonSelectNone()
	{
		if (buttonSelectNone == null)
		{
			buttonSelectNone = new JButton();
			buttonSelectNone.setText(Messages.getString("CommandSelection.selectNoneButton"));
			buttonSelectNone.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonSelectNone.setMinimumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonSelectNone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					commandList.clearSelection();
				}
			});
		}
		return buttonSelectNone;
	}


	/**
	 * This method initializes panelFileContents
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getPanelFileContents()
	{
		if (panelFileContents == null)
		{
			panelFileContents = new JScrollPane();
			panelFileContents.setName("jScrollPane1");
			panelFileContents.setViewportView(getFileContents());
		}
		return panelFileContents;
	}

	/**
	 * This method initializes panelProgramDestination
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelProgramDestination()
	{
		if (panelProgramDestination == null)
		{
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.insets = new Insets(0, 20, 0, 0);
			gridBagConstraints2.gridy = 2;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.insets = new Insets(0, 20, 0, 0);
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.insets = new Insets(50, 0, 0, 0);
			gridBagConstraints1.gridy = 3;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridy = 0;
			panelProgramDestination = new JPanel();
			panelProgramDestination.setLayout(new GridBagLayout());
			panelProgramDestination.setName("panelProgramDestination");
			panelProgramDestination.add(getButtonSaveProgram(), gridBagConstraints);
			panelProgramDestination.add(getButtonDisplayProgram(), gridBagConstraints1);
		}
		return panelProgramDestination;
	}

	/**
	 * This method initializes buttonSaveProgram
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getButtonSaveProgram()
	{
		if (buttonSaveProgram == null)
		{
			buttonSaveProgram = new JRadioButton();
			buttonSaveProgram.setText(Messages.getString("CommandSelection.programCreation.saveProgram"));
			buttonSaveProgram.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
			buttonSaveProgram.setSelected(true);
		}
		return buttonSaveProgram;
	}

	/**
	 * This method initializes buttonDisplayProgram
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getButtonDisplayProgram()
	{
		if (buttonDisplayProgram == null)
		{
			buttonDisplayProgram = new JRadioButton();
			buttonDisplayProgram.setText(Messages.getString("CommandSelection.programCreation.displayProgram"));
			buttonDisplayProgram.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		}
		return buttonDisplayProgram;
	}

	private void saveFileAndExit() {
		File savedFile = saveFile();
		if (savedFile!=null) {
			exit();
		}
	}

	private void doBack() {
		switch (currentState)
		{
			case STATE_COMMANDS_SELECTION :
				exit();
				break;
			case STATE_FILE_EDITION :
				changeState(STATE_COMMANDS_SELECTION);
				break;
			case STATE_PROGRAM_DESTINATION :
				changeState(STATE_FILE_EDITION);
				break;
		}
	}

	private void doNext() {
		switch (currentState)
		{
			case STATE_COMMANDS_SELECTION :
				if (mode==MODE_COMMANDS_INSERTION)
				{
					insertCommands();
				}
				else
					changeState(STATE_FILE_EDITION);
				break;
			case STATE_FILE_EDITION :
				if (mode == MODE_PROGRAM_CREATION && myParent.getInterfaceLevel()==Configuration.LEVEL_ADVANCED)
					changeState(STATE_PROGRAM_DESTINATION);
				else
				{
					saveFileAndExit();
				}
				break;
			case STATE_PROGRAM_DESTINATION :
				if (buttonDisplayProgram.isSelected())
				{
					String commands = fileContents.getText();
					myParent.newProgram(commands);
					myParent.showProgram();
					exit();
				}
				else
				{
					saveFileAndExit();
				}
				break;
		}
	}


}
