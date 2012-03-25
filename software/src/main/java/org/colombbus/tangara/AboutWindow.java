/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008-2012 Colombbus (http://www.colombbus.org)
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Display an about window that indicates some information about Tangara
 * (version, developers names, license, etc).
 */
@SuppressWarnings("serial")
public class AboutWindow extends JDialog {
	private JTextPane credits;
	private JScrollPane scroll;
	private JButton closeButton;
	private ImageComponent imagePanel;
	private int windowHeight;
	private int windowWidth;
	private static final int marginLeft = 40;
	private static final int marginBottom = 20;
	private static final int marginText = 40;
	private static final int marginButton = 20;
	private static final String ICON_IMAGE = Messages.getString("AboutWindow.iconImage"); //$NON-NLS-1$

	private static final Logger LOG = Logger.getLogger(AboutWindow.class);

	public AboutWindow(JFrame frame, Image baseHeaderImage) {
		super(frame, true);
		windowWidth = baseHeaderImage.getWidth(null);
		windowHeight = baseHeaderImage.getHeight(null);
		setLocationAndSize();
		initialize(baseHeaderImage);
	}

	private void setLocationAndSize() {
		Insets insets = getToolkit().getScreenInsets(getGraphicsConfiguration());
		this.setSize(new Dimension(windowWidth + insets.left + insets.right, windowHeight + insets.top + insets.bottom));
		this.setResizable(false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
	}

	private JScrollPane getTextZone() {
		if (scroll == null) {
			credits = new JTextPane();
			String text = creditText();
			credits.setContentType("text/html");
			credits.setText(text);
			credits.setFont(new Font("Arial", Font.PLAIN, 20));
			credits.setEditable(false);
			credits.setOpaque(false);

			scroll = new JScrollPane(credits);
			scroll.getViewport().setOpaque(false);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setPreferredSize(new Dimension((windowWidth * 3 / 4) - marginLeft, windowHeight / 2 - marginBottom));

			scroll.setOpaque(false);
			scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					scroll.getVerticalScrollBar().setValue(0);
				}
			});
		}
		return scroll;
	}

	private String creditText() {
		String creditText = "";
		try {
			InputStream in = creditResourceStream();
			String rawText = IOUtils.toString(in);
			String version = Configuration.instance().getProperty("tangara.version");
			creditText = rawText.replace("${tangara.version}", version);
		} catch (IOException e1) {
			LOG.warn("About Window - Error while reading HTML File", e1);
		}
		return creditText;
	}

	private InputStream creditResourceStream() {
		InputStream in = null;
		if (Configuration.instance().defaultLanguage() == false) {
			String language = Configuration.instance().getLanguage();
			String resourceName = "about_" + language + ".html";
			in = getClass().getResourceAsStream(resourceName);
		}

		if (in == null)
			in = getClass().getResourceAsStream("about.html");

		return in;
	}

	/**
	 * Sets the close button of the about window.
	 *
	 * @return the close button
	 */
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setText(Messages.getString("AboutWindow.close"));
			closeButton.setPreferredSize(new Dimension(250, 29));
			closeButton.setSize(new Dimension(250, 29));
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
			closeButton.setPreferredSize(new Dimension(windowWidth / 4 - 2 * marginButton, windowHeight / 8));
			closeButton.setOpaque(false);
		}
		return closeButton;
	}

	/**
	 * Sets the image zone of the about window,
	 *
	 * @param backgroundImg
	 */
	private void initialize(Image backgroundImg) {
		updateWindowIcon();

		BufferedImage headerImage = createBackgroundImage(backgroundImg);
		imagePanel = new ImageComponent(headerImage);
		imagePanel.setBounds(0, 0, headerImage.getWidth(), headerImage.getHeight());
		imagePanel.setOpaque(true);
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));

		imagePanel.add(Box.createVerticalStrut(windowHeight / 2));

		JPanel panel = bodyPanel();
		imagePanel.add(panel);
		imagePanel.add(Box.createVerticalStrut(marginBottom));
		setContentPane(imagePanel);
	}

	private void updateWindowIcon() {
		Image tangaraIcon = new ImageIcon(ICON_IMAGE).getImage();
		if (tangaraIcon == null)
			LOG.error("tangara image icon not defined");
		else
			setIconImage(tangaraIcon);
	}

	private BufferedImage createBackgroundImage(Image baseBackgroundImg) {
		BufferedImage newImg = new BufferedImage(baseBackgroundImg.getWidth(null), baseBackgroundImg.getHeight(null),
				BufferedImage.TYPE_INT_RGB);
		newImg.getGraphics().drawImage(baseBackgroundImg, 0, 0, null);
		Graphics2D drawingGraphics = (Graphics2D) newImg.getGraphics();
		Color titleColor = Configuration.instance().getColor("tangara.title.color");
		String titleText = Configuration.instance().getString("tangara.title");
		Font titleFont = Configuration.instance().getFont("tangara.title.font");
		drawingGraphics.setFont(titleFont);
		drawingGraphics.setColor(titleColor);
		drawingGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		drawingGraphics.drawString(titleText, marginLeft, windowHeight / 2 - marginText);
		return newImg;
	}

	private JPanel bodyPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setOpaque(false);
		panel.add(Box.createHorizontalStrut(marginLeft));
		panel.add(getTextZone());
		panel.add(Box.createHorizontalStrut(marginButton));
		panel.add(getCloseButton());
		panel.add(Box.createHorizontalStrut(marginButton));
		return panel;
	}

	/**
	 * Serves to create JPanel in which an image can be drawn.
	 */
	private class ImageComponent extends JPanel {
		private Image image;

		ImageComponent(BufferedImage image) {
			super();
			this.image = image;
		}

		/**
		 * Overwrites the paintComponent method.
		 *
		 * @param the
		 *            graphic context.
		 */
		@Override
		public void paintComponent(Graphics g) {
			g.drawImage(image, 0, 0, this);
			this.paintChildren(g);
		}
	}

}
