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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.Configuration;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class permits to have a Text box designed to display the messages of
 * each person in a chat service.
 *
 * @author Benoit
 *
 */
@SuppressWarnings("serial")
@Localize(value = "DisplayMessages", localizeParent = true)
public abstract class DisplayMessages extends TGraphicalObject {

	/** Class logger */
	private static Logger LOG = Logger.getLogger(DisplayMessages.class);
	public final String DEFAULT_EXTENSION = getMessage("defaultExtension");

	private Map<String, SimpleAttributeSet> smileys = new Hashtable<String, SimpleAttributeSet>();

	private JList myList;
	private DefaultListModel myModel;
	private ImageIcon defaultIcon;
	private ImageIcon meIcon;
	private int previouslySelected = -1;
	private java.util.List<Message> newMessages = new Vector<Message>();

	private java.util.List<String> commands = new Vector<String>();

	/**
	 * Creates an instance of this class.
	 */
	@Localize(value = "DisplayMessages")
	public DisplayMessages() {
		super();
		setSize(300, 100);
		setLayout(new BorderLayout());
		loadIcons();
		createList();
		JScrollPane scroll = createScrollPane();
		add(scroll, BorderLayout.CENTER);
		displayObject();
	}

	private void createList() {
		myList = new JList();
		myList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		myList.setCellRenderer(new MyCellRenderer());
		myModel = new DefaultListModel();
		myList.setFixedCellWidth(getObjectWidth() - 20);
		myList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selected = myList.getSelectedIndex();
				if ((selected != -1) && (selected != previouslySelected)) {
					previouslySelected = selected;
					executeCommands();
				}
			}
		});
		myList.setModel(myModel);
	}

	private JScrollPane createScrollPane() {
		JScrollPane scroll = new JScrollPane();
		scroll.setSize(getObjectWidth(), getObjectHeight());
		scroll.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		scroll.setViewportView(myList);
		scroll.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				myList.setFixedCellWidth(getObjectWidth() - 20);
			}
		});
		return scroll;
	}

	/**
	 * Adds a message from the given user.
	 *
	 * @param user
	 * @param text
	 */
	@Localize(value = "DisplayMessages.addMessage")
	public void addMessage(String user, String text) {
		addMessage(user, text, false);
	}

	/**
	 * Adds our message sent to the given user.
	 *
	 * @param user
	 * @param text
	 */
	@Localize(value = "DisplayMessages.addMyMessage")
	public void addMyMessage(String user, String text) {
		addMessage(user, text, true);
	}

	/**
	 * Adds the message to the list of messages to insert. The message can be
	 * ours or not.
	 *
	 * @param user
	 * @param text
	 * @param mine
	 */
	public void addMessage(String user, String text, boolean mine) {
		if (myModel != null) {
			synchronized (newMessages) {
				newMessages.add(new Message(user, text, mine));
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					insertNextMessage();
				}
			});
		}
	}

	/**
	 * Inserts the next message of the list.
	 */
	private void insertNextMessage() {
		synchronized (newMessages) {
			Message message = newMessages.get(0);
			myModel.addElement(message);
			myList.ensureIndexIsVisible(myModel.getSize() - 1);
			newMessages.remove(0);
		}
	}

	/**
	 * Updates the list.
	 */
	public void repaintList() {
		DefaultListModel newModel = new DefaultListModel();
		myList.clearSelection();
		for (int i = 0; i < myModel.getSize(); i++) {
			newModel.addElement(myModel.elementAt(i));
		}
		myList.setModel(newModel);
		myModel = newModel;
		myList.repaint();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				myList.ensureIndexIsVisible(myModel.getSize() - 1);
			}
		});
	}

	/**
	 * Adds a smiley.
	 *
	 * @param symbol
	 * @param fileName
	 */
	@Localize(value = "DisplayMessages.addSmiley")
	public void addSmiley(String symbol, String fileName) {
		String extension = "";
		int pointPosition = fileName.lastIndexOf('.');
		if (pointPosition > -1) {
			extension = fileName.substring(pointPosition + 1);
			if (extension.length() == 0) {
				extension = DEFAULT_EXTENSION;
				fileName += DEFAULT_EXTENSION;
			}
		} else {
			extension = DEFAULT_EXTENSION;
			fileName += "." + DEFAULT_EXTENSION;
		}
		File file = new File(fileName);
		if (!file.isAbsolute()) {
			file = new File(Program.instance().getCurrentDirectory(), fileName);
			// if file does not exist, try with user home directory
			if (!file.exists())
				file = new File(Configuration.instance().getUserHome(), fileName);
		}
		if (file.exists()) {
			SimpleAttributeSet style = new SimpleAttributeSet();
			StyleConstants.setIcon(style, new ImageIcon(file.getAbsolutePath()));
			smileys.put(symbol, style);
			repaintList();
		} else {
			Program.instance().printError(MessageFormat.format(getMessage("smileyNotFound"), fileName));
		}
	}

	/**
	 * Add a new command
	 *
	 * @param cmd
	 */
	@Localize(value = "common.addCommand")
	public void addCommand(String cmd) {
		commands.add(cmd);
	}

	/**
	 * Removes all commands
	 */
	@Localize(value = "common.removeCommands")
	public void removeCommands() {
		commands.clear();
	}

	/**
	 * Runs all commands
	 */
	private void executeCommands() {
		Message currentMessage = (Message) myList.getSelectedValue();
		if ((currentMessage != null) && (!currentMessage.isMyMessage())) {
			String user = currentMessage.getUser();
			for (String command : commands) {
				String newCommand = command;
				if (user != null)
					newCommand = command.replaceAll("%", user);
				Program.instance().executeScript(newCommand, getGraphicsPane());
			}
		}
	}

	/**
	 * Loads special icons for this object
	 */
	private void loadIcons() {
		try {
			URL imageURL = getResource("user.png").toURL();
			BufferedImage image = ImageIO.read(imageURL);
			defaultIcon = new ImageIcon(image);
			imageURL = getResource("user_red.png").toURL();
			image = ImageIO.read(imageURL);
			meIcon = new ImageIcon(image);
		} catch (Exception e) {
			LOG.error("could not load user.png " + e.getMessage());
		}
	}

	private class Message {
		private String text;
		private String user;
		private boolean isMine;

		public Message(String user, String text, boolean isMine) {
			this.user = user;
			this.text = text;
			this.isMine = isMine;
		}

		public String getText() {
			return text;
		}

		public String getUser() {
			return user;
		}

		public boolean isMyMessage() {
			return isMine;
		}
	}

	private class MyCellRenderer extends JPanel implements ListCellRenderer {
		private JTextPane messagePane;
		private JLabel iconLabel;

		public MyCellRenderer() {
			setLayout(new BorderLayout());
			iconLabel = new JLabel();
			iconLabel.setPreferredSize(new Dimension(50, 50));
			add(iconLabel, BorderLayout.WEST);
			messagePane = new JTextPane();
			messagePane.setPreferredSize(null);
			messagePane.setEditable(false);
			add(messagePane, BorderLayout.CENTER);
		}

		private void setMessage(Message m) {
			if (m.isMyMessage())
				messagePane.setText(getMessage("me") + m.getUser() + "\n");
			else
				messagePane.setText(m.getUser() + "\n");
			DefaultStyledDocument doc = (DefaultStyledDocument) messagePane.getDocument();
			int startPos = doc.getLength();
			String text = m.getText() + " "; // because of a bug when text is
												// only made of a smiley: this
												// one is not displayed
			try {
				doc.insertString(startPos, text, null);
			} catch (BadLocationException e) {
				LOG.warn("Error while inserting message");
			}
			if (smileys != null) {
				int pos;
				for (String key : smileys.keySet()) {
					pos = 0;
					while (pos >= 0) {
						pos = text.indexOf(key, pos);
						if (pos >= 0) {
							doc.setCharacterAttributes(startPos + pos, key.length(), smileys.get(key), true);
							pos++;
						}
					}
				}
			}
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Message m = (Message) value;
			setMessage(m);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				messagePane.setBackground(list.getSelectionBackground());
				messagePane.setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				messagePane.setBackground(list.getBackground());
				messagePane.setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			if (m.isMyMessage()) {
				if (meIcon != null)
					iconLabel.setIcon(meIcon);
			} else {
				if (defaultIcon != null) {
					iconLabel.setIcon(defaultIcon);
				}
			}
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return this;
		}
	}

}
