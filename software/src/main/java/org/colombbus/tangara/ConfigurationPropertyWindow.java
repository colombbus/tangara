package org.colombbus.tangara;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ToolTipManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

@SuppressWarnings("serial")
public class ConfigurationPropertyWindow extends JDialog {

    private JPanel propertyMainPanel;
    private JButton propertyButtonSave;
    private JButton propertyButtonCancel;
    private JButton propertyButtonDefault;
    private String propertyName;
    private String propertyValue;
    private JTextArea jTextArea;
    private JScrollPane jScrollPane;
    private JScrollPane jScrollPane2;
    private int row;
    private JTextArea propertyHelpText;
    private ConfigurationWindow parentWindow;
    private AbstractAction copyAction;
    private AbstractAction cutAction;
    private AbstractAction pasteAction;
    private JPopupMenu popup;
    
    public ConfigurationPropertyWindow(ConfigurationWindow aWindow, int row, String propertyName, String propertyValue) {
    	parentWindow = aWindow;
    	this.row = row;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        setModal(true);
        setSize(new Dimension(400, 260));
        setTitle(Messages.getString("ConfigurationWindow.setPropertyWindow") + " "+propertyName);
        setLocation(new Point((parentWindow.getX()+(parentWindow.getWidth()-this.getWidth())/2),(parentWindow.getY()+(parentWindow.getHeight()-this.getHeight())/2)));
        setResizable(false);
        setContentPane(this.getPropertyMainPanel());
        addPopupMenu();
    }


    /**
     * Closes the set property window.
     */
    private void exit()
    {
    	ToolTipManager.sharedInstance().setInitialDelay(750);
        this.setVisible(false);
        this.dispose();
    }

    
	private MatteBorder createMatteBorder() {
		return new MatteBorder(ConfigurationWindow.MARGIN_Y, ConfigurationWindow.MARGIN_X,ConfigurationWindow.MARGIN_Y,ConfigurationWindow.MARGIN_X,parentWindow.getBackground());
	}
    
    /**
     * This method initializes propertyMainPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPropertyMainPanel()
    {
        propertyMainPanel = new JPanel();
        propertyMainPanel.setLayout(new BorderLayout());

        propertyHelpText = new JTextArea( Messages.getString("ConfigurationWindow." + propertyName) );
        propertyHelpText.setTabSize(4);
        propertyHelpText.setLineWrap(true);
        propertyHelpText.setWrapStyleWord(true);
        propertyHelpText.setEditable(false);
        propertyHelpText.setBackground(parentWindow.getBackground());
        propertyHelpText.setSelectionColor(parentWindow.getBackground());
        jScrollPane2 = new JScrollPane(propertyHelpText);
        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
        jScrollPane2.setPreferredSize(new Dimension (380, 80));
        jScrollPane2.setBorder(new CompoundBorder(new EmptyBorder(ConfigurationWindow.MARGIN_Y, ConfigurationWindow.MARGIN_X,0,ConfigurationWindow.MARGIN_X), parentWindow.createLineBorder()));
        propertyMainPanel.add(jScrollPane2, BorderLayout.NORTH);
        
        //We add a JTextArea to edit the property's value.
        jTextArea = new JTextArea();
        jTextArea.setMargin(new Insets(5,5,5,5));
        jTextArea.setText(propertyValue);
        jScrollPane = new JScrollPane(jTextArea);
        jScrollPane.setBackground(Color.white);
        jScrollPane.setPreferredSize(new Dimension(380, 60));
        jScrollPane.setBorder(new CompoundBorder(createMatteBorder(), parentWindow.createLineBorder()));
	    propertyMainPanel.add(jScrollPane, BorderLayout.CENTER);

	    //We add a default button to restore the default value.
        ToolTipManager.sharedInstance().setInitialDelay(150);
        propertyButtonDefault = new JButton(Messages.getString("ConfigurationWindow.default"));
        propertyButtonDefault.setToolTipText(parentWindow.getDefaultProperty(propertyName));
        propertyButtonDefault.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent e)
            {
            	jTextArea.setText(parentWindow.getDefaultProperty(propertyName));
            }
        });

        //We add a save button to save the change.
        propertyButtonSave = new JButton(Messages.getString("ConfigurationWindow.modify"));
        propertyButtonSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent e)
            {
            	parentWindow.setProperty(row, jTextArea.getText());
                exit();
            }
        });

        //We add a cancel button to exit without saving.
        propertyButtonCancel = new JButton(Messages.getString("ConfigurationWindow.cancel"));
        propertyButtonCancel.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e)
            {
                exit();
            }
        });
                    
        JPanel propertyButtonsPanel = new JPanel();
        propertyButtonsPanel.setLayout(new BoxLayout(propertyButtonsPanel,BoxLayout.X_AXIS));
        propertyButtonsPanel.setBorder(new EmptyBorder(ConfigurationWindow.MARGIN_Y,ConfigurationWindow.MARGIN_X,ConfigurationWindow.MARGIN_Y, ConfigurationWindow.MARGIN_X));
        propertyButtonsPanel.add(propertyButtonDefault);
        propertyButtonsPanel.add(Box.createHorizontalStrut(10));
        propertyButtonsPanel.add(propertyButtonSave);
        propertyButtonsPanel.add(Box.createHorizontalGlue());
        propertyButtonsPanel.add(propertyButtonCancel);
        
        propertyMainPanel.add(propertyButtonsPanel, BorderLayout.SOUTH);
        
        return propertyMainPanel;
    }
    
    private void addPopupMenu() {
    	copyAction = new AbstractAction(Messages.getString("Banner.menu.copy")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				jTextArea.copy();
			}
		};
    	cutAction = new AbstractAction(Messages.getString("Banner.menu.cut")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				jTextArea.cut();
			}
		};
    	pasteAction = new AbstractAction(Messages.getString("Banner.menu.paste")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				jTextArea.paste();
			}
		};
    	popup = new JPopupMenu();
    	popup.add(copyAction);
    	popup.add(cutAction);
    	popup.add(pasteAction);
    	jTextArea.addMouseListener(new PopupListener());
    	jTextArea.addCaretListener(new CaretListener() {
    		@Override
			public void caretUpdate(CaretEvent e) {
    			if (e.getDot()==e.getMark()) {
    				copyAction.setEnabled(false);
    				cutAction.setEnabled(false);
    			} else {
    				copyAction.setEnabled(true);
    				cutAction.setEnabled(true);
    			}
    		}
    	});
    }
    
	private class PopupListener extends MouseAdapter {
	    @Override
		public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    @Override
		public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),e.getX(),e.getY());
	        }
	    }
	}

    
    
}
