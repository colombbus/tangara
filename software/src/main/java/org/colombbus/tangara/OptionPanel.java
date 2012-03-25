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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;

/**
 * Represents the options bar located between the GameArea and the code editor pane
 */
@SuppressWarnings("serial")
public class OptionPanel extends JPanel
{
	/** the west panel of the options bar*/
	private JPanel modePanel = null;
	/** west of <code>modePanel</code> */
	private JPanel mainOptionsPanel = null;

	private JLabel commandLabel = null;
	/** east of <code>modePanel</code> */
	private JLabel endIcon = null;

	private JLabel programLabel = null;
	/** the east panel of the options bar */
	private JPanel secondaryOptionsPanel = null;
	/** Construction mode button */
	private JLabel designLabel = null;

	private boolean designMode;
	/** Tangara fram */
	private EditorFrame frame = null;

	/**
	 * Creates a new OptionPanel instance
	 *
	 */
	public OptionPanel(EditorFrame frame) {
		super();
		initialize();
        //javax.swing.ButtonGroup editModeGrp = new javax.swing.ButtonGroup();
        //editModeGrp.add(lineModeButton);
        //editModeGrp.add(textModeButton);
        this.frame = frame;
        designMode = false;
	}


	/**
	 * This method initializes the options bar
	 *
	 */
	private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(811, 34));
        this.setPreferredSize(new Dimension(84, 25));
        this.setBackground(Color.white);
        // modePanel in West and secondaryOptionsPanel in East
        this.add(getModePanel(), BorderLayout.WEST);
        this.add(getSecondaryOptionsPanel(), BorderLayout.EAST);
	}

	/**
	 * This method initializes modePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getModePanel()
	{
		// composed by mainOptionsPanel and endIcon
		if (modePanel == null)
		{
			endIcon = new JLabel();
			endIcon.setText("");
			endIcon.setBackground(Color.white);
			endIcon.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/main_end.png")));
			modePanel = new JPanel();
			modePanel.setLayout(new BorderLayout());
			modePanel.setBackground(new Color(230, 245, 248));
			modePanel.add(getMainOptionsPanel(), BorderLayout.WEST);
			modePanel.add(endIcon, BorderLayout.EAST);
		}
		return modePanel;
	}

	/**
	 * This method initializes mainOptionsPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getMainOptionsPanel()
	{
		if (mainOptionsPanel == null)
		{
			programLabel = new JLabel();
			programLabel.setText(Messages.getString("OptionPanel.programMode"));
			programLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
			programLabel.setForeground(new Color(60, 87, 174));
			programLabel.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/page_white_text.png")));
			programLabel.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					setProgramMode();
				}
				@Override
				public void mouseExited(java.awt.event.MouseEvent e) {
					OptionPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				@Override
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
					OptionPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			});
			commandLabel = new JLabel();
			commandLabel.setText(Messages.getString("OptionPanel.commandMode"));
			commandLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
			commandLabel.setForeground(new Color(153, 0, 0));
			commandLabel.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/computer.png")));
			commandLabel.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					setCommandMode();
				}
				@Override
				public void mouseExited(java.awt.event.MouseEvent e) {
					OptionPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				@Override
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
					OptionPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			});
			//composed by commandLabel (Commandes) and programLabel (Programmes)
			mainOptionsPanel = new JPanel();
			mainOptionsPanel.setLayout(new GridBagLayout());
			mainOptionsPanel.setBackground(new Color(240,240,240));//new Color(230, 245, 248));
			GridBagConstraints gbc1 = new GridBagConstraints();
			gbc1.insets = new Insets(0,10,0,0);
			mainOptionsPanel.add(commandLabel, gbc1);
			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.insets = new Insets(0,40,0,40);
			mainOptionsPanel.add(programLabel, gbc2);
		}
		return mainOptionsPanel;
	}

	/**
	 * This method initializes lineModeButton
	 *
	 * @return javax.swing.JRadioButton
	 *
	private JRadioButton getLineModeButton()
	{
		// button "Single ligne"
		if (lineModeButton == null)
		{
			lineModeButton = new JRadioButton();
			lineModeButton.setBackground(Color.white);
			lineModeButton.setSelected(true);
			lineModeButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
			lineModeButton.setText(Messages.getString("OptionPanel.singleLine")); //$NON-NLS-1$
			lineModeButton.setHorizontalAlignment(SwingConstants.LEFT);
			lineModeButton.setForeground(new Color(51, 51, 51));
			lineModeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					if (frame!=null)
						frame.setLineMode();
				}
			});
			lineModeButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseExited(java.awt.event.MouseEvent e) {
					//outside mouse with normal format
					OptionPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
					// inside mouse with hand format
					OptionPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			});
		}
		return lineModeButton;
	}
	*/

	/**
	 * This method initializes textModeButton
	 *
	 * @return javax.swing.JRadioButton
	 *
	private JRadioButton getTextModeButton()
	{
		//button Multi lines
		if (textModeButton == null)
		{
			textModeButton = new JRadioButton();
			textModeButton.setBackground(Color.white);
			textModeButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
			textModeButton.setText(Messages.getString("OptionPanel.multiLine")); //$NON-NLS-1$
			textModeButton.setHorizontalAlignment(SwingConstants.LEFT);
			textModeButton.setForeground(new Color(51, 51, 51));
			textModeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					if (frame!=null)
						frame.setTextMode();
				}
			});
			textModeButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseExited(java.awt.event.MouseEvent e) {
//					outside mouse with normal format
					OptionPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
//					 inside mouse with hand format
					OptionPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			});

		}
		return textModeButton;
	}
	*/

	/**
	 * Sets the state of <code>textModeButton</code> at true
	 *
	 */
	public void setTextMode()
	{
		//textModeButton.setSelected(true);
	}

	/**
	 * Sets the state of <code>lineModeButton</code> at true
	 *
	 */
	public void setLineMode()
	{
		//lineModeButton.setSelected(true);
	}


	/**
	 * This method initializes secondaryOptionsPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSecondaryOptionsPanel()
	{
		if (secondaryOptionsPanel == null)
		{
			designLabel = new JLabel();
			designLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
			designLabel.setText(Messages.getString("OptionPanel.designMode"));
			designLabel.setForeground(new Color(51, 51, 51));
			designLabel.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/cog_edit.png")));
			designLabel.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					changeDesignMode();
				}
				@Override
				public void mouseExited(java.awt.event.MouseEvent e) {
					OptionPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				@Override
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
					OptionPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			});
			secondaryOptionsPanel = new JPanel();
			secondaryOptionsPanel.setLayout(new GridBagLayout());
			secondaryOptionsPanel.setBackground(Color.white);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(0,0,0,40);
			secondaryOptionsPanel.add(designLabel, gbc);
			//secondaryOptionsPanel.add(getLineModeButton(), new GridBagConstraints());
			//secondaryOptionsPanel.add(getTextModeButton(), new GridBagConstraints());
		}
		return secondaryOptionsPanel;
	}

	/**
	 * Selects the UI command mode
	 *
	 */
	public void setCommandMode()
	{
		// pour mettre en mode commande
		if (frame !=null)
		{
            frame.setCommandMode();
            commandLabel.setForeground(new Color(153,0,0));
            programLabel.setForeground(new Color(60,87,174));
            designLabel.setVisible(true);
            OptionPanel.this.repaint();
			//lineModeButton.setVisible(true);
			//textModeButton.setVisible(true);
		}
	}

	/**
	 * Selects the UI program mode
	 *
	 */
	public void setProgramMode()
	{
		// pour mettre en mode programme
		if (frame !=null)
		{
            frame.setProgramMode();
            commandLabel.setForeground(new Color(60,87,174));
            programLabel.setForeground(new Color(153,0,0));
            designLabel.setVisible(false);
            this.repaint();
			//lineModeButton.setVisible(false);
			//textModeButton.setVisible(false);
			/*if (designMode)
				changeDesignMode();*/
		}
	}

	/**
	 * Changes the design which enables or not to drag objects and to check their name and their class.
	 *
	 */
	public void changeDesignMode()
	{
		designMode = !designMode;
		Program.instance().setDesignMode(designMode);
		if (frame !=null)
			frame.getGraphicsPane().repaint();
		if (designMode)
			designLabel.setForeground(new Color(153,0,0));
		else
			designLabel.setForeground(new Color(51,51,51));
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
