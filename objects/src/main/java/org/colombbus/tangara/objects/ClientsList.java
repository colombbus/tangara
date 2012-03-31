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
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class provides a component that enables to display a client list.
 *
 * @author Benoit
 */
@SuppressWarnings("serial")
@Localize(value = "ClientsList", localizeParent = true)
public abstract class ClientsList extends TGraphicalObject {
	private static final int POLLING_INTERVAL = 5000;

	/** Class logger */
	private static Logger LOG = Logger.getLogger(ClientsList.class);

	private ClientsUpdate update;
	private JList myList;
	private DefaultListModel myModel;
	private Vector<String> commands = new Vector<String>();
	private ImageIcon icon;
	private String lastClientSelected = "";
	private List<String> toBeInserted = new Vector<String>();
	private List<String> toBeDeleted = new Vector<String>();

	/**
	 * Creates a new clients list
	 */
	@Localize(value = "ClientsList")
	public ClientsList() {
		super();
		loadIcon();
		setSize(50, 30);
		setLayout(new BorderLayout());

		createList();
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(myList);
		scroll.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		scroll.setSize(new Dimension(getObjectWidth(), getObjectHeight()));
		add(scroll, BorderLayout.CENTER);
		displayObject();
	}

	private void createList() {
		myList = new JList();
		myList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		myList.setCellRenderer(new MyCellRenderer());
		myModel = new DefaultListModel();
		myList.setModel(myModel);
		myList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				String client = getClient();
				if ((client != null) && (client.compareTo(lastClientSelected) != 0)) {
					executeCommands();
					lastClientSelected = client;
				}
			}
		});
	}

	/**
	 * Loads an icon.
	 */
	private void loadIcon() {
		try {
			URL imageURL = ClientsButton.class.getResource("user.png");
			BufferedImage image = ImageIO.read(imageURL);
			icon = new ImageIcon(image);
		} catch (Exception e) {
			LOG.error("could not load user.png " + e.getMessage());
		}
	}

	/**
	 * Returns the size of the DefaultListModel myModel.
	 *
	 * @return int
	 */
	public int getItemCount() {
		if (myModel == null)
			return 0;
		return myModel.getSize();
	}

	/**
	 * Returns the string of a given position of myModel.
	 *
	 * @param position
	 * @return
	 */
	public String getItemAt(int position) {
		if (myModel == null)
			return null;
		return (String) myModel.elementAt(position);
	}

	/**
	 * Adds an item to the list of items to insert.
	 *
	 * @param item
	 */
	public void addItem(String item) {
		synchronized (toBeInserted) {
			toBeInserted.add(item);
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				insertItem();
			}
		});
	}

	/**
	 * Inserts in the myModel the top element of the item list toBeInserted.
	 */
	private void insertItem() {
		synchronized (toBeInserted) {
			myModel.addElement(toBeInserted.get(0));
			toBeInserted.remove(0);
		}
	}

	/**
	 * Removes the given item from the item list. It adds the element to delete
	 * to a delete list.
	 *
	 * @param item
	 */
	public void removeItem(String item) {
		synchronized (toBeDeleted) {
			toBeDeleted.add(item);
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				deleteItem();
			}
		});
	}

	/**
	 * Removes actually the element at the top of the deleted list.
	 */
	private void deleteItem() {
		synchronized (toBeDeleted) {
			myModel.removeElement(toBeDeleted.get(0));
			toBeDeleted.remove(0);
		}
	}

	/**
	 * Executes the commands of the command list.
	 */
	private void executeCommands() {
		String client = getClient();
		for (String command : commands) {
			String newCommand = command;
			if (client != null)
				newCommand = command.replaceAll("%", client);
			Program.instance().executeScript(newCommand, getGraphicsPane());
		}
	}

	/**
	 * Sets the Internet component with the given one.
	 *
	 * @param internet
	 */
	@Localize(value = "common.setInternet")
	public void setInternet(Internet internet) {
		if (internet != null) {
			if (update == null) {
				update = new ClientsUpdate(internet);
				update.start();
			} else {
				update.setInternet(internet);
			}
		}
	}

	/**
	 * Adds a command to the command list.
	 *
	 * @param cmd
	 */
	@Localize(value = "common.addCommand")
	public void addCommand(String cmd) {
		commands.add(cmd);
	}

	/**
	 * Clears the command list.
	 */
	@Localize(value = "common.removeCommands")
	public void removeCommands() {
		commands.clear();
	}

	/**
	 * Returns String of the selected client.
	 *
	 * @return String
	 */
	@Localize(value = "ClientsList.getClient")
	public String getClient() {
		if (myList == null) {
			return null;
		}
		return (String) myList.getSelectedValue();
	}

	/**
	 * Deletes the instance of this class.
	 */
	@Override
	public void deleteObject() {
		if (update != null)
			update.stopUpdate();
		super.deleteObject();
	}

	/**
	 * This thread serves to update constantly the client list
	 *
	 */
	private class ClientsUpdate extends Thread {
		private boolean mustStop = false;
		private Internet internet;

		public ClientsUpdate(Internet internet) {
			this.internet = internet;
		}

		/**
		 * The Internet component is replaced with the one given.
		 *
		 * @param internet
		 */
		public void setInternet(Internet internet) {
			this.internet = internet;
		}

		/**
		 * Stops the loop in run() which updates the client list.
		 */
		public void stopUpdate() {
			mustStop = true;
		}

		/**
		 * Starts the thread.
		 */
		@Override
		public void start() {
			super.start();
		}

		/**
		 * This is the run() method of the subclass thread. It serves to update
		 * in a loop the client list.
		 */
		@Override
		public void run() {
			while (!mustStop) {
				List<String> clients = internet.getClients();
				if (clients != null) {
					for (String client : clients) {
						boolean toBeInserted = true;
						for (int i = 0; i < getItemCount(); i++) {
							if (client.compareTo((getItemAt(i))) == 0) {
								toBeInserted = false;
								break;
							}
						}
						if (toBeInserted) {
							addItem(client);
						}
					}
					int max = getItemCount();
					for (int i = 0; i < max; i++) {
						String checkedItem = (getItemAt(i));
						boolean toBeDeleted = true;
						for (String client : clients) {
							if (client.compareTo(checkedItem) == 0) {
								toBeDeleted = false;
								break;
							}
						}
						if (toBeDeleted) {
							removeItem(checkedItem);
						}
					}
				}
				try {
					Thread.sleep(POLLING_INTERVAL);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * This subclass permits to specify how will appear the JLabel of the
	 * selected user. It is loaded in the list component by the constructor of
	 * the main class.
	 *
	 * @author Benoit
	 */
	private class MyCellRenderer extends JLabel implements ListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String s = (String) value;
			setText(s);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			if (icon != null) {
				setIcon(icon);
			}
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return this;
		}
	}
}
