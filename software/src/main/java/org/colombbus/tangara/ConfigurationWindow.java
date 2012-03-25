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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;


/**
 * This class permits to display from the Tools menu a window in which all Tangara properties can be chosen.
 * @author Thierry
 *
 */
@SuppressWarnings("serial")
public class ConfigurationWindow extends JDialog
{
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(ConfigurationWindow.class);
	public static final int MARGIN_X = 5;
	public static final int MARGIN_Y = 10;
    
    private EditorFrame parent;

    private JTable propertiesTable;
    private Properties defaultProperties;
    private Properties properties;
    private Object[][] modelData;

    private JPanel mainPanel;
    private JLabel helpText;
    private JScrollPane scrollPane;
    private JPanel bottomPanel;
    private JButton restoreButton;
    private JButton cancelButton;
    private JButton saveButton;
    private String password;

    private boolean cancelWindow = false;
    
    
	/**
	 * Creates a new configuration window from the main frame.
	 * @param parent
	 * 		the frame of Tangara.
	 */
    public ConfigurationWindow(EditorFrame parent)
    {  	
    	password = Configuration.instance().getProperty("configuration.password");    	
    	if (password!=null&&!password.equals(""))
        	displayPasswordWindow();    	
    	if (!cancelWindow)
    	{
    		this.parent = parent;
    		this.setSize(new Dimension(780, 427));
    		this.setTitle(Messages.getString("ConfigurationWindow.title"));    		
    		initialize();
    		this.setLocation(new Point((parent.getX()+(parent.getWidth()-getWidth())/2),(parent.getY()+(parent.getHeight()-getHeight())/2)));
    		this.setResizable(false);
    		this.setModal(true);
    	}
    }

    /**
     * This method launches a password window allowing to display the ConfigurationWindow.
     */
    private void displayPasswordWindow()
    {
    	String passwordWindow = Messages.getString("ConfigurationWindow.password.window");
    	String enterPassword = Messages.getString("ConfigurationWindow.password.passwordMessage");
    	String typedPassword = (String)JOptionPane.showInputDialog(
    						parent,
    						enterPassword,
    	                    passwordWindow,
    	                    JOptionPane.PLAIN_MESSAGE,
    	                    null,
    	                    null,
    	                    null);
    	if (typedPassword == null)
    	{
    		cancelWindow = true;
    		try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						exit();
					}
				});
			} catch (InterruptedException e) {
				LOG.warn("displayPasswordWindow", e);
			} catch (InvocationTargetException e) {
				LOG.warn("displayPasswordWindow", e);
			}
    	}
    	else if (!typedPassword.equals(password))
    	{
	    	String wrongPasswordTitle = Messages.getString("ConfigurationWindow.password.wrongPasswordTitle");
	    	String wrongPasswordMessage = Messages.getString("ConfigurationWindow.password.wrongPasswordMessage");
			JOptionPane.showMessageDialog(null, wrongPasswordMessage, wrongPasswordTitle, JOptionPane.WARNING_MESSAGE);
			displayPasswordWindow();
    	}
    }

    /**
     * This method initializes the components to put in this JFrame.
     *
     */
    private void initialize()
    {    	
    	//We load the default properties.
    	loadDefaultProperties();
    	
        //We load the table contents.
        loadProperties();
        String[] columnNames = {Messages.getString("ConfigurationWindow.propertyColumn"),
                                Messages.getString("ConfigurationWindow.valueColumn")};        
        SpecialTableModel model = new SpecialTableModel(modelData, columnNames);        
        propertiesTable = new JTable(model);        
        propertiesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);        


        //We set the double click listener on the table.
        propertiesTable.addMouseListener(new MouseAdapter() {
            @Override
			public void mouseClicked(MouseEvent e) {
              if (e.getClickCount() == 2) {
                Point p = e.getPoint();
                int row = propertiesTable.rowAtPoint(p);
                ConfigurationPropertyWindow confPropertyWindow = new ConfigurationPropertyWindow(ConfigurationWindow.this, row, (String) propertiesTable.getValueAt(row, 0), (String) propertiesTable.getValueAt(row, 1));
				confPropertyWindow.setVisible(true);
              }
            }
          });                
                
        //We set the content pane of the window.
        this.setContentPane(getMainPanel());
    }

    /**
     * Loads the list of Tangara properties from the configuration class.
     *
     */
    private void loadDefaultProperties()
    {
        defaultProperties = new Properties();
		InputStream inStream = null;
		try {
			inStream = Configuration.instance().getClass().getResourceAsStream(Configuration.DEFAULT_PROPERTIES_FILENAME);
			defaultProperties.load(inStream);
		} catch (IOException ioEx) {
			String errMsg = "Failed to load default configuration"; //$NON-NLS-1$
			LOG.fatal(errMsg, ioEx);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
    }

    /**
     * Loads the list of Tangara properties from the configuration class.
     *
     */
    private void loadProperties()
    {
        properties = Configuration.instance().getProperties();

        List<String> listProp = new ArrayList<String>();
        for( Object key : properties.keySet()) {
        	listProp.add((String)key);
        }
        Collections.sort(listProp);
        
        modelData = new Object[listProp.size()][2];
        
        for (int dataIdx = 0; dataIdx<listProp.size(); dataIdx++)
        {
        	String key = listProp.get(dataIdx);
			modelData[dataIdx][0] = key;
        	modelData[dataIdx][1] = properties.getProperty(key);
        }
    }

    /**
     *  This class permits to deactivate the possibility to edit the columns.
     * @author Thierry
     *
     */
    class SpecialTableModel extends DefaultTableModel
    {

        SpecialTableModel(Object[][] rowData, Object[] columnNames)
        {
            super(rowData, columnNames);
        }
        @Override
		public boolean isCellEditable(int row, int column)
        {
            return false; // We deactivate the possibility to edit the columns.
        }
    }

    //---------------------------------------------------------------------------------
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
            mainPanel.add(getScrollPane(), BorderLayout.CENTER);
            mainPanel.add(getBottomPanel(), BorderLayout.SOUTH);
        }
        return mainPanel;
    }

    /**
     * This method initializes topPanel
     *
     * @return javax.swing.JPanel
     */
    private JLabel getHelpText()
    {
        if (helpText == null)
        {        	
            helpText = new JLabel();
            helpText.setText(Messages.getString("ConfigurationWindow.helpText"));
            helpText.setBorder(createEmptyBorder());
        }
        return helpText;
    }

    /**
     * This method initializes scrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getScrollPane()
    {
        if (scrollPane == null)
        {        	
            scrollPane = new JScrollPane(propertiesTable);
            EmptyBorder borderPart1 = createEmptyBorder();
            LineBorder borderPart2 = createLineBorder();
			CompoundBorder border = new CompoundBorder(borderPart1, borderPart2);
			scrollPane.setBorder(border);
    	}
        return scrollPane;
    }

	private EmptyBorder createEmptyBorder() {
		return new EmptyBorder(MARGIN_Y, MARGIN_X,0,MARGIN_X);
	}
	
	public LineBorder createLineBorder() {
		return new LineBorder(new Color(127,157,185));
	}


    /**
     * This method initializes bottomPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getBottomPanel()
    {
        if (bottomPanel == null)
        {        	
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.X_AXIS));
            bottomPanel.add(getRestoreButton());
            bottomPanel.add(Box.createHorizontalStrut(10));
            bottomPanel.add(getSaveButton());
            bottomPanel.add(Box.createHorizontalGlue());
            bottomPanel.add(getCancelButton());
            bottomPanel.setBorder(new EmptyBorder(MARGIN_Y,MARGIN_X,MARGIN_X,MARGIN_Y));            
        }
        return bottomPanel;
    }

    /**
     * This method initializes leftButton
     *
     * @return javax.swing.JButton
     */
    private JButton getRestoreButton()
    {
        if (restoreButton == null)
        {
        	restoreButton = new JButton();
        	restoreButton.setText(Messages.getString("ConfigurationWindow.restore"));
        	restoreButton.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent e) {
	                restoreConfiguration();
                }
            });
        }
        return restoreButton;
    }

    /**
     * This method initializes centerButton
     *
     * @return javax.swing.JButton
     */
    private JButton getCancelButton()
    {
        if (cancelButton == null)
        {
        	cancelButton = new JButton();
        	cancelButton.setText(Messages.getString("ConfigurationWindow.cancel"));
            cancelButton.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent e) {
                    exit();
                }
            });
        }
        return cancelButton;
    }

    /**
     * This method initializes rightButton
     *
     * @return javax.swing.JButton
     */
    private JButton getSaveButton()
    {
        if (saveButton == null)
        {
        	saveButton = new JButton();
        	saveButton.setText(Messages.getString("ConfigurationWindow.save"));
        	saveButton.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent e)
                {
                    saveConfiguration();
                }

            });
        }
        return saveButton;
    }

	private void saveConfiguration() {
		readUserUpdates();

        FileOutputStream outStream = null;
        try {
			File baseDir = new File(System.getProperty("user.home"), Configuration.PROPERTIES_DIRECTORY_NAME);
			if (!baseDir.exists()) {
				if (baseDir.mkdir()) {
					LOG.debug("Property directory created: '"+baseDir.getAbsolutePath()+"'");
				} else {
					LOG.error("Could not create property directory '"+baseDir.getAbsolutePath()+"'");
				}
			}
			File confFile = new File(baseDir,Configuration.PROPERTIES_FILENAME);
			outStream = new FileOutputStream(confFile);
			properties.store(outStream, Configuration.PROPERTIES_FILENAME);
		} catch (Exception e1) {
			LOG.warn("Exception when trying to access output file", e1);
		} finally {
			IOUtils.closeQuietly(outStream);
		}
    	String saveWindow = Messages.getString("ConfigurationWindow.save.saveWindow");
    	String saveMessage = Messages.getString("ConfigurationWindow.save.saveMessage");
		JOptionPane.showMessageDialog(null, saveMessage, saveWindow, JOptionPane.INFORMATION_MESSAGE);
        exit();
	}

	private void readUserUpdates() {
		for(int i=0; i<propertiesTable.getRowCount(); i++) {
        	String name = (String)propertiesTable.getValueAt(i,0);
			String value = (String)propertiesTable.getValueAt(i,1);
			properties.setProperty(name, value);
		}
	}

    //-----------------------------------------------------------------

    /**
     * Closes the configuration window
     *
     */
    private void exit()
    {
        this.setVisible(false);
        this.dispose();
    }

    public String getDefaultProperty(String name) {
    	return defaultProperties.getProperty(name);
    }
    
    public void setProperty(int row, String value) {
        propertiesTable.setValueAt(value, row, 1);
    }
    
    private void restoreConfiguration() {
		for(int rowIdx=0; rowIdx<propertiesTable.getRowCount(); rowIdx++)
		{
		  	String name = (String)propertiesTable.getValueAt(rowIdx,0)	;
		  	String value = getDefaultProperty(name);
			properties.setProperty(name, value);
		  	propertiesTable.setValueAt(value, rowIdx, 1);
		}
	}

	
}
