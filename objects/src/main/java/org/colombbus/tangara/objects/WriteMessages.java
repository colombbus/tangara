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

package org.colombbus.tangara.objects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;
import java.util.regex.Matcher;


/**
 * This class provides a text zone inside a frame and with a button to send the
 * messages.
 * 
 * @author Benoit
 */
@SuppressWarnings("serial")
@Localize(value = "WriteMessages", localizeParent = true)
public abstract class WriteMessages extends TGraphicalObject {

	private JTextPane writingZone;

	private java.util.List<String> commands;


	/**
	 * Creates an instance of this class.
	 */
	@Localize(value = "WriteMessages")
	public WriteMessages() {
		super();
		setSize(300, 100);
		commands = new Vector<String>();
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
		setLayout(new BorderLayout());
		writingZone = new JTextPane();
		writingZone.setEditable(true);
		writingZone.setVisible(true);
		writingZone.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		writingZone.addKeyListener(new java.awt.event.KeyListener() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				keyStroke(evt);
			}

			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
			}

			@Override
			public void keyTyped(java.awt.event.KeyEvent evt) {
			}
		});
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(writingZone);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, BorderLayout.CENTER);
		JButton sendButton = new JButton(getMessage("button.send")); //$NON-NLS-1$
		sendButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonClick(evt);
			}

		});
		this.add(sendButton, BorderLayout.SOUTH);
		displayObject();
	}

	/**
	 * Executes the commands when ENTER is pressed on the keyboard
	 * 
	 * @param evt
	 */
	public void keyStroke(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
			executeCommands();
			evt.consume();
		}
	}

	/**
	 * Executes the commands when the button below the text is clicked
	 * 
	 * @param evt
	 */
	public void buttonClick(java.awt.event.ActionEvent evt) {
		executeCommands();
	}

	/**
	 * Executes the commands of the list and clears the writing zone.
	 */
	private void executeCommands() {
		String newCommand;
		for (String command : commands) {
			newCommand = command.replaceAll("%", Matcher.quoteReplacement(writingZone.getText()));
			Program.instance().executeScript(newCommand, getGraphicsPane());
		}
		writingZone.setText(""); //$NON-NLS-1$
	}

	/**
	 * Returns the text of the writing zone.
	 * 
	 * @return the text
	 * 
	 */
	@Localize(value = "WriteMessages.getText")
	public String getText() {
		return writingZone.getText();
	}

	/**
	 * Writes the given text in the writing zone.
	 * 
	 * @param text
	 */
	@Localize(value = "WriteMessages.write")
	public void write(String text) {
		writingZone.setText(text);
	}

	/**
	 * Add a command to the list of commands to execute.
	 * 
	 * @param cmd
	 */
	@Localize(value = "common.addCommand")
	public void addCommand(String cmd) {
		commands.add(cmd);
	}

	/**
	 * Clears the list of the commands to execute.
	 */
	@Localize(value = "common.removeCommands")
	public void removeCommands() {
		commands.clear();
	}

}
