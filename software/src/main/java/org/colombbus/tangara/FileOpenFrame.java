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
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

/**
 * This class gives the frame containing the program Tangara is run from a tgr file.
 * @author gwen
 */
@SuppressWarnings("serial")
public class FileOpenFrame extends TFrame
{

	/** Class logger */
    private static Logger LOG = Logger.getLogger(FileOpenFrame.class);

	private GraphicsPane graphicsPane;
	private JPanel jContentPane;

	private static final String ICON_PATH = "logo_tangara.png";

	public FileOpenFrame() {
		super();
		initialize();
	}

	private void initialize()
	{
		this.setContentPane(getJContentPane());
        this.setTitle(Messages.getString("ProgramFrame.application.title"));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
        	@Override
			public void windowClosing(WindowEvent e)
        	{
        		exit();
        	}
        });
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
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane()
	{
		if (jContentPane == null)
		{
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getGraphicsPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	public GraphicsPane getCurrentGraphicsPane()
	{
		return graphicsPane;
	}

    /**
     * Get panel containing the game panel
     *
     * @return graphicsPane
     */
	@Override
	public GraphicsPane getGraphicsPane()
	{
		if (graphicsPane == null) {
			try {
				loadGraphicsPane();
				graphicsPane.setLayout(null);
				graphicsPane.setBackground(Color.white);
			} catch(Exception e) {
   				LOG.error("error get GraphisPane", e);
			}
		}
		return graphicsPane;
	}

	private void loadGraphicsPane() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		try {
			String language = Configuration.instance().getLanguage();
			String className = "org.colombbus.tangara."+ language + ".GraphicsPane_" + language;
			graphicsPane = createGraphicsPaneInstance(className);
		} catch (ClassNotFoundException e) { // If the language is unknown, the English version used.
			String className = "org.colombbus.tangara.en.GraphicsPane_en";
			graphicsPane = createGraphicsPaneInstance(className);
		}
	}

	private GraphicsPane createGraphicsPaneInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> type = Class.forName(className);
		GraphicsPane gp = (GraphicsPane) type.newInstance();
		return gp;
	}

	/**
	 * Only computes size
	 */
	@Override
	public void afterInit() {
		boolean result = computeSize();
		// If size is set according to its content, the frame should not be resizable
		if (result)
			setResizable(false);
		pack();
		if (result)
			setLocationRelativeTo(null);
		setVisible(true);
		if (!result)
			setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
	}

	/**
	 * Exit from tangara
	 */
	public void exit() {
		LOG.info("Prepare exiting application");
		String title = Messages.getString("EditorFrame.exit.title");
		String message = Messages.getString("EditorFrame.exit.message");
		graphicsPane.freeze(true);
		Object[] options = {Messages.getString("tangara.yes"), Messages.getString("tangara.cancel")};
		int answer = JOptionPane.showOptionDialog(this,
					message,
					title, JOptionPane.OK_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,     //do not use a custom Icon
				    options,  //the titles of buttons
				    options[0]);
		if (answer == JOptionPane.OK_OPTION) {
			dispose();
			Program.instance().exit();
		}
		else
			graphicsPane.freeze(false);
	}

}
