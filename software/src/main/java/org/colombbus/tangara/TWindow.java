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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public abstract class TWindow extends TFrame {
	/** Class logger */
	private static Logger LOG = Logger.getLogger(TWindow.class);

	/** Minimal dimension of a Tangara window */
	private static final Dimension minimalSize = new Dimension(200, 200);

	// String windowName = null;
	private String initFileName;

	/** Graphical part of the Tangara environnement */
	private GraphicsPane graphicsPane;

	/** Main panel of the window */
	private JPanel jContentPane;

	/**
	 * Creates an instance of this class.
	 */
	public TWindow() {
		initialize();
		setLocationRelativeTo(Program.instance().getDefaultGraphicsPane().getTopLevelAncestor());
		Program.instance().registerWindow(this);
		this.setVisible(true);
	}

	/**
	 * Creates a new window and loads the file in
	 * 
	 * @param fileName
	 */
	public TWindow(String fileName) {
		initFileName = fileName;
		initialize();
		Program.instance().loadFile(initFileName, graphicsPane, true);
		setLocationRelativeTo(Program.instance().getDefaultGraphicsPane().getTopLevelAncestor());
		Program.instance().registerWindow(this);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				afterInit();
			}
		});
	}

	/**
	 * Initializes the window
	 */
	private void initialize() {
		setContentPane(getJContentPane());
		GraphicsPane gp = getGraphicsPane();
		gp.setPreferredSize(minimalSize);
		pack();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				delete();
			}
		});
	}

	/**
	 * Deletes the window
	 */
	public void delete() {
		setVisible(false);
		graphicsPane.removeGraphicalObjects();
		Program.instance().getWindows().remove(this);
		Program.instance().deleteObject(this);
		this.dispose();
	}

	/**
	 * Adjusts the size of the window
	 */
	public void adjustSize() {
		if (Program.instance().getWindows().contains(this)) {
			boolean result = computeSize();
			pack();
			if (!result) {
				setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
			}
		}
	}

	/**
	 * Shows the window
	 */
	public void newShow() {
		if (Program.instance().getWindows().contains(this))
			this.toFront();
	}

	/**
	 * Runs a command in this window
	 * 
	 * @param command
	 */
	public void executeCommand(String command) {
		if (this != null) {
			Program.instance().executeScript(command, graphicsPane);
		}
	}

	/**
	 * Loads a file in this window
	 * 
	 * @param fileName
	 */
	public void loadFile(String fileName) {
		if (this != null)
			Program.instance().loadFile(fileName, graphicsPane, false);
	}

	/**
	 * Gets the main pane
	 * 
	 * @return
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getGraphicsPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * Gets the window graphicsPane
	 */
	@Override
	public GraphicsPane getGraphicsPane() {
		if (graphicsPane == null) {
			try {
				try {
					String language = Configuration.instance().getLanguage();
					String className = "org.colombbus.tangara." + language
							+ ".GraphicsPane_" + language;
					Class<?> type = Class.forName(className);
					graphicsPane = (GraphicsPane) type.newInstance();

				} catch (ClassNotFoundException e) { // If the language is
														// unknown, the English
														// version used.
					String className = "org.colombbus.tangara.en.GraphicsPane_en";
					Class<?> type;
					type = Class.forName(className);
					graphicsPane = (GraphicsPane) type.newInstance();
				}
			} catch (Exception e) {
				LOG.error("error getting GraphicsPane", e);
			}
			graphicsPane.setLayout(null);
			graphicsPane.setBackground(Color.white);
		}
		return graphicsPane;
	}

	@Override
	public void afterInit() {
		boolean result = computeSize();
		pack();
		setVisible(true);
		if (!result) {
			setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		}
	}
}
