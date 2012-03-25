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
import java.awt.Graphics2D;
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
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

/**
 * This class is used when the child wants to export his program <br>
 *
 * @author hasan
 *
 */
@SuppressWarnings("serial")
public class FileSelection extends JDialog
{
	/** Class logger */
	private Logger LOG = Logger.getLogger(FileSelection.class);  //  @jve:decl-index=0:

	private JPanel mainPanel = null;
	
	private Vector<File> fileList = null;
	private Vector<String> fileNameList = null;
	
	private Vector<File> tangaraFilesList = null;
	private Vector<String> tangaraFilesNameList = null;
	
	private JList JFileNameList = null;
	private JList mainProgramSelectionList = null;
	private JScrollPane jScrollPane = null;
	private JPanel panelButtons = null;
	private JButton leftButton = null;
	private JButton rightButton = null;
	private JLabel helpText = null;
	private JPanel centralPanel = null;
	
	private File directory = null;
	private File selectedFile = null;
	private JFileChooser fileChooserWithoutFilter = null;
	private JFileChooser repertoryChooser = null;

	private boolean selecting = false;
	private int previousIndex = -1;
	private int currentState = -1;

	private static final int STATE_FILES_SELECTION = 1;
	private static final int STATE_CHOOSE_MAIN_FILE = 2;


	private JPanel panelFileSelection = null;
	private JPanel panelSelection = null;
	private JButton buttonAddFiles = null;
	private JButton buttonSelectAll = null;
	private JButton buttonSelectNone = null;
	private JPanel panelChooseMainFile = null;
	
	private static final int iconSize = 10;
	private static final Color selectedForegroundColor = Color.black;  //  @jve:decl-index=0:
	private static final Color selectedBackgroundColor = new Color(240,255,240);  //  @jve:decl-index=0:


	private ImageIcon iconNotSelected;  //  @jve:decl-index=0:
	private ImageIcon iconSelected;  //  @jve:decl-index=0:

	private static final int MARGIN_Y = 10;
	private static final int MARGIN_X = 5;
	private static final int MARGIN_BUTTON = 50;
	private static final int WIDTH_BUTTON = 180;
	private static final int HEIGHT_BUTTON = 30;

	private static final Comparator<File> alphabeticalOrder = new Comparator<File>() {
		@Override
		public int compare(File o1, File o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	};

	
	/**
	 * Creates a new instance of FileSelection from Tangara frame
	 * @throws IOException 
	 *
	 */
	public FileSelection(EditorFrame parent, boolean modal, File selected) throws IOException
	{
		super(parent, modal);
		this.selectedFile = selected;
		this.directory = selected.getParentFile();
		initializeIcons();
		initialize();
		changeState(STATE_FILES_SELECTION);
		this.setLocation(new Point((parent.getX()+(parent.getWidth()-getWidth())/2),(parent.getY()+(parent.getHeight()-getHeight())/2)));
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				JFileNameList.setSelectionInterval(0, JFileNameList.getModel().getSize()-1);
				countTangaraFilesSelected();
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
	 * This method initializes this
	 *
	 */
	private void initialize() {
        this.setSize(new Dimension(609, 427));
        this.setContentPane(getMainPanel());
        fileChooserWithoutFilter = new JFileChooser(directory);
        repertoryChooser = new JFileChooser(directory);
        repertoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileList = new Vector<File>();
        fileNameList = new Vector<String>();
        JFileNameList.setCellRenderer(new MyCellRenderer());
        fillFileList(directory.listFiles());
        JFileNameList.setListData(fileNameList);
        this.setTitle(Messages.getString("FileSelection.title"));
		centralPanel.setBorder(BorderFactory.createLineBorder(new Color(127,157,185)));
	}
	
	
	
	/**
	 * This method fills the fileList
	 *
	 */
	private void fillFileList(File[] list)	{
		Arrays.sort(list, alphabeticalOrder);
		for (int i = 0; i < list.length; i++)
		{
			if(list[i].isFile())
			{
				fileList.add(list[i]);
				fileNameList.add(list[i].getName());
				if(list[i].getName().endsWith(".tgr") || list[i].getName().endsWith(".txt")) {
				}
			}
		}
	}
	
	/**
	 * Counts the number of tangara files selected in the list
	 * if this number is 1, nextButton's text becomes "export".
	 */
	private void countTangaraFilesSelected() {
		int numberOfTangaraFilesSelected = 0;
		int[] selectedIndices = JFileNameList.getSelectedIndices();
		for(int i = 0; i < selectedIndices.length; i++) {
			if(fileNameList.get(selectedIndices[i]).endsWith(".tgr") || fileNameList.get(selectedIndices[i]).endsWith(".txt")) {
				numberOfTangaraFilesSelected++;
			}
		}
		if(numberOfTangaraFilesSelected == 1) {
			rightButton.setText(Messages.getString("FileSelection.export"));
		} else {
			rightButton.setText(Messages.getString("FileSelection.nextButton"));
		}
	}

	/**
	 * This method initializes mainPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
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
	 * This method initializes JFileNameList
	 *
	 * @return javax.swing.JList
	 */
	private JList getJFileNameList() {
		if (JFileNameList == null) {
			JFileNameList = new JList();
		}
		JFileNameList.setSelectionBackground(selectedBackgroundColor);
		JFileNameList.setSelectionForeground(selectedForegroundColor);
		JFileNameList.setBackground(Color.white);
		JFileNameList.setForeground(Color.gray);
		MouseListener[] mouseListeners = JFileNameList.getMouseListeners();
		for (MouseListener m:mouseListeners) {
			JFileNameList.removeMouseListener(m);
		}

		MouseMotionListener[] motionListeners = JFileNameList.getMouseMotionListeners();
		for (MouseMotionListener m:motionListeners) {
			JFileNameList.removeMouseMotionListener(m);
		}

		JFileNameList.addMouseListener(new MouseAdapter(){
         	@Override
			public void mousePressed(MouseEvent e) {
         		int index = JFileNameList.locationToIndex(e.getPoint());
    			if (JFileNameList.isSelectedIndex(index)) {
    				JFileNameList.removeSelectionInterval(index,index);
     				selecting = false;
     			} else {
     				JFileNameList.addSelectionInterval(index,index);
     				selecting = true;
     			}
     			previousIndex = index;
         	}

         	@Override
			public void mouseReleased(MouseEvent e) {
         		previousIndex = -1;
         		countTangaraFilesSelected();
         	}
		});
		JFileNameList.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int index = JFileNameList.locationToIndex(e.getPoint());
				if (index != previousIndex) {
					if (previousIndex>-1) {
						if (index>previousIndex) {
							if (selecting) {
								JFileNameList.addSelectionInterval(previousIndex+1,index);
							} else {
								JFileNameList.removeSelectionInterval(previousIndex+1,index);
							}
						} else {
							if (selecting) {
								JFileNameList.addSelectionInterval(index,previousIndex-1);
							} else {
								JFileNameList.removeSelectionInterval(index,previousIndex-1);
							}
						}
					} else {
						if (selecting) {
							JFileNameList.addSelectionInterval(index,index);
						} else {
							JFileNameList.removeSelectionInterval(index,index);
						}
					}
					JFileNameList.ensureIndexIsVisible(index);
					previousIndex = index;
				}
			}
		});


		return JFileNameList;
	}

	/**
	 * Identifies components that can be used as "rubber stamps" to paint the cells in JFileNameList
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
			String s = (String)value;
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
			jScrollPane.setViewportView(getJFileNameList());
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
			leftButton = new JButton(Messages.getString("FileSelection.cancelButton"));
			leftButton.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			leftButton.setMaximumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			leftButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try {
						doBack();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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
			rightButton = new JButton(Messages.getString("FileSelection.nextButton"));
			rightButton.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			rightButton.setMaximumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			rightButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try {
						doNext();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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
			centralPanel.add(getPanelFileSelection(), "JFileNameList");
			centralPanel.add(getPanelChooseMainFile(), "panelChooseMainFile");
		}
		return centralPanel;
	}

	/**
	 * Selects the state of the file selection
	 * @param state
	 * 			the state/step of file selection
	 * @throws IOException 
	 */
	private void changeState(int state) throws IOException {
		switch (state) {
			case STATE_FILES_SELECTION :
				helpText.setText(Messages.getString("FileSelection.filesSelection"));
				leftButton.setText(Messages.getString("FileSelection.cancelButton"));
				rightButton.setText(Messages.getString("FileSelection.nextButton"));
				((CardLayout)centralPanel.getLayout()).show(centralPanel,"JFileNameList");
				countTangaraFilesSelected();
				currentState = state;
				break;
			case STATE_CHOOSE_MAIN_FILE :
				fillMainProgramSelectionPanel();
				if(tangaraFilesList.size() > 1) {
					helpText.setText(Messages.getString("FileSelection.mainFileSelection"));
					leftButton.setText(Messages.getString("FileSelection.previousButton"));
					rightButton.setText(Messages.getString("FileSelection.export"));
					((CardLayout)centralPanel.getLayout()).show(centralPanel,"panelChooseMainFile");
					currentState = state;
				} else if(tangaraFilesList.size() == 1) {
					export();
				} else {
					JOptionPane.showMessageDialog(FileSelection.this, Messages.getString("FileSelection.mainProgramMissing.message"),
							Messages.getString("FileSelection.mainProgramMissing.title"), JOptionPane.ERROR_MESSAGE);
				}
				break;
		}
	}
	
	private void addFiles()
	{
		fileChooserWithoutFilter.setSelectedFile(new File(""));
		fileChooserWithoutFilter.setDialogTitle(Messages.getString("EditorFrame.file.open.title")); //$NON-NLS-1$
		int returnVal = fileChooserWithoutFilter.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
        	if(!fileChooserWithoutFilter.getSelectedFile().getParentFile().getAbsolutePath().equals(directory.getAbsolutePath()))
        	{
        		directory = fileChooserWithoutFilter.getSelectedFile().getParentFile();
            	fillFileList(directory.listFiles());
            	JFileNameList.setListData(fileNameList);
            	JFileNameList.setSelectionInterval(0, JFileNameList.getModel().getSize()-1);
            	countTangaraFilesSelected();
        	}
        }
	}

	private boolean userConfirmOverride(File exportFile) {
    	String title = Messages.getString("FileSelection.override.title");
    	String message = MessageFormat.format(Messages.getString("FileSelection.override.message"), exportFile.getName());
		Object[] options = {Messages.getString("tangara.yes"), Messages.getString("tangara.cancel")};
		int optionType = JOptionPane.OK_CANCEL_OPTION;
		int messageType = JOptionPane.QUESTION_MESSAGE;
		Icon icon = null;
		int answer = JOptionPane.showOptionDialog(this, message, title,
				optionType, messageType, icon, options, options[0]);
		return answer == JOptionPane.OK_OPTION;
	}

	
	
	private void export() throws IOException
	{
		String jarName = tangaraFilesNameList.get(mainProgramSelectionList.getSelectedIndex());
		jarName = jarName.substring(0,jarName.length()-4) + ".jar";
		fileChooserWithoutFilter.setSelectedFile(new File(jarName));
		int returnVal = fileChooserWithoutFilter.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
        	File exportFile = fileChooserWithoutFilter.getSelectedFile();
            if (exportFile.exists() && !userConfirmOverride(exportFile)) {
				return;
			}

			Configuration conf = Configuration.instance();
			File exportDirectory = FileUtils.createTempDirectory();
			File executionPropertiesFile = new File(exportDirectory, "execution.properties");
			if(executionPropertiesFile.createNewFile())
			{
				// Fills the execution.propeties File
				
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(executionPropertiesFile)));
				pw.println("main-program = " + tangaraFilesNameList.get(mainProgramSelectionList.getSelectedIndex()));
				
				pw.println("language = " + conf.getProperty("language"));
				
				String resources = "resources = ";
				for(int i = 0; i < JFileNameList.getSelectedIndices().length - 1; i++)
				{
					resources += fileNameList.get(JFileNameList.getSelectedIndices()[i]) + ",";
				}
				resources += fileNameList.get(JFileNameList.getSelectedIndices()[JFileNameList.getSelectedIndices().length-1]);
				pw.println(resources);
				
				pw.close();
				
				//Makes a copy of the base in the export directory
				
				File jar = conf.getBasePath();
				FileInputStream fis = new FileInputStream(jar);
				BufferedInputStream bis = new BufferedInputStream(fis);
				FileUtils.copyFile(bis, exportFile);
								
				//Adds the files to newBase
				
				Vector<File> filesToAdd = new Vector<File>();
				for(int i = 0; i < JFileNameList.getSelectedIndices().length; i++)
				{
					filesToAdd.add(fileList.get(JFileNameList.getSelectedIndices()[i]));
				}
				
				JarMaker maker = new JarMaker();
				maker.make(filesToAdd, exportFile, "resources", executionPropertiesFile, "org/colombbus/tangara");
				
			}
			else
			{
				LOG.error("Error trying to create file execution.properties");
			}
			exit();
        }
	}

	/**
	 * Closes the file selection window
	 *
	 */
	private void exit()
	{
		this.setVisible(false);
		this.dispose();
	}
	
	/**
	 * Fills the mainProgramSelectionPanel()
	 *
	 */
	private void fillMainProgramSelectionPanel()
	{
		tangaraFilesList = new Vector<File>();
		tangaraFilesNameList = new Vector<String>();
		int[] selectedIndices = JFileNameList.getSelectedIndices();
		int selectedIndex = 0;
		int index = 0;
		for(int i = 0; i < selectedIndices.length; i++) {
			String fileName = fileNameList.get(selectedIndices[i]);
			if(fileName.endsWith(".tgr") || fileName.endsWith(".txt")) {
				tangaraFilesNameList.add(fileName);
				tangaraFilesList.add(fileList.get(selectedIndices[i]));
				if (selectedFile.getName().compareTo(fileName)==0) {
					selectedIndex = index;
				}
				index++;
			}
		}
		mainProgramSelectionList.setListData(tangaraFilesNameList);
		mainProgramSelectionList.setSelectedIndex(selectedIndex);
		
	}
	
	/**
	 * This method initializes panelFileSelection
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelChooseMainFile()
	{
		if (panelChooseMainFile == null)
		{
			panelChooseMainFile = new JPanel();
			panelChooseMainFile.setLayout(new BorderLayout());
			panelChooseMainFile.setName("panelChooseMainFile");
			mainProgramSelectionList = new JList();
			mainProgramSelectionList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
			panelChooseMainFile.add(mainProgramSelectionList);
		}
		return panelChooseMainFile;
	}

	/**
	 * This method initializes panelFileSelection
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelFileSelection()
	{
		if (panelFileSelection == null)
		{
			panelFileSelection = new JPanel();
			panelFileSelection.setLayout(new BorderLayout());
			panelFileSelection.setName("panelFileSelection");
			panelFileSelection.add(getPanelSelection(), BorderLayout.NORTH);
			panelFileSelection.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return panelFileSelection;
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
			//panelSelection.add(Box.createHorizontalStrut(MARGIN_BUTTON));
			panelSelection.add(getButtonAddFiles());
			panelSelection.add(Box.createHorizontalGlue());
			panelSelection.add(getButtonSelectAll());
			panelSelection.add(Box.createHorizontalGlue());
			panelSelection.add(getButtonSelectNone());
			//panelSelection.add(Box.createHorizontalStrut(MARGIN_BUTTON));
		}
		return panelSelection;
	}
	
	/**
	 * This method initializes buttonAddFiles
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonAddFiles() {
		if (buttonAddFiles == null) {
			buttonAddFiles = new JButton();
			buttonAddFiles.setText(Messages.getString("FileSelection.addFilesButton"));
			buttonAddFiles.setMinimumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonAddFiles.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonAddFiles.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addFiles();
				}
			});
		}
		return buttonAddFiles;
	}


	/**
	 * This method initializes buttonSelectAll
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButtonSelectAll() {
		if (buttonSelectAll == null) {
			buttonSelectAll = new JButton();
			buttonSelectAll.setText(Messages.getString("FileSelection.selectAllButton"));
			buttonSelectAll.setMinimumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonSelectAll.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonSelectAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileNameList.setSelectionInterval(0, JFileNameList.getModel().getSize()-1);
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
	private JButton getButtonSelectNone() {
		if (buttonSelectNone == null) {
			buttonSelectNone = new JButton();
			buttonSelectNone.setText(Messages.getString("FileSelection.selectNoneButton"));
			buttonSelectNone.setPreferredSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonSelectNone.setMinimumSize(new Dimension(WIDTH_BUTTON, HEIGHT_BUTTON));
			buttonSelectNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileNameList.clearSelection();
				}
			});
		}
		return buttonSelectNone;
	}

	private void doBack() throws IOException {
		switch (currentState) {
			case STATE_FILES_SELECTION :
				exit();
				break;
			case STATE_CHOOSE_MAIN_FILE :
				changeState(STATE_FILES_SELECTION);
				break;
		}
	}

	private void doNext() throws IOException {
		switch (currentState) {
			case STATE_FILES_SELECTION :
				changeState(STATE_CHOOSE_MAIN_FILE);
				break;
			case STATE_CHOOSE_MAIN_FILE :
				export();
				break;
		}
	}


}
