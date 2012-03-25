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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;


/**
 * This class enables to create a thread that displays the splash image of Tangara. It enables the software to load
 * during this time
 * @author gwen
 *
 */
@SuppressWarnings("serial")
public class SplashScreen extends JWindow {

	private static final String IMPORT_DEVELOPERS = "starting.developers";
	private static final String IMPORT_DEVELOPERS_NAMES = "starting.developersNames";
	private static final String IMPORT_COLOMBBUS = "starting.colombbus";
	private static final String IMPORT_WEBSITE = "starting.webSite";
	
	private static final int marginLeft = 40;
	private static final int marginText = 40;
	private static final int lineHeight = 22;
	
	/**
	 * Creates a new SplashScreen object by specifying the image to use and the time to wait
	 * @param image
	 * 		the image to use
	 * @param waitTime
	 * 		the time to wait
	 */
	public SplashScreen(ImageIcon imageIcon, int waitTime)
	{
		super();
		int width = imageIcon.getIconWidth();
		int height = imageIcon.getIconHeight();
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D drawingGraphics  = (Graphics2D) bufferedImage.getGraphics();
	    drawingGraphics.drawImage(imageIcon.getImage(), 0, 0, null);

        Color titleColor = Configuration.instance().getColor("tangara.title.color");//$NON-NLS-1$
		String title = Configuration.instance().getString("tangara.title");//$NON-NLS-1$
		Font titleFont = Configuration.instance().getFont("tangara.title.font");//$NON-NLS-1$
		
        // draws the title
	    drawingGraphics.setColor(titleColor);
	    drawingGraphics.setFont(titleFont);
        // Sets to on text anti-aliasing
        drawingGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawingGraphics.drawString(title, marginLeft, height/2-marginText);

        drawingGraphics.setFont(new Font("Arial", Font.PLAIN, 19));
		String developersNames = Messages.getString(IMPORT_DEVELOPERS_NAMES);
		String tokenSeparator  = ",";
		String developers = Messages.getString(IMPORT_DEVELOPERS);
		String colombbus = Messages.getString(IMPORT_COLOMBBUS);
		String webSite = Messages.getString(IMPORT_WEBSITE);


		drawingGraphics.drawString(developers, marginLeft, height/2 + lineHeight);
		int i = 1;
		for (StringTokenizer namesTokenizer = new StringTokenizer(
				developersNames, tokenSeparator); namesTokenizer.hasMoreTokens();)
		{
			String packageName = namesTokenizer.nextToken();
			drawingGraphics.drawString(packageName, marginLeft+150, height/2 + lineHeight*i);
			i++;
		}
		i++;
		drawingGraphics.drawString(colombbus, marginLeft, height/2 + lineHeight*i);
		i++;
		drawingGraphics.setColor(new Color(88, 2, 7));
		drawingGraphics.drawString(webSite, marginLeft, height/2 + lineHeight*i);

		JLabel l = new JLabel(new ImageIcon(bufferedImage) );
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,Color.DARK_GRAY, Color.BLACK);
		l.setBorder(border);
		getContentPane().add(l, BorderLayout.CENTER);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = l.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
					screenSize.height / 2 - (labelSize.height / 2));

		final int pause = waitTime;
		final Runnable closerRunner = new Runnable() {
			@Override
			public void run() {
				setVisible(false);
				dispose();
			}
		};
		Runnable waitRunner = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(pause);
					SwingUtilities.invokeAndWait(closerRunner);
				} catch (Exception e) {
					e.printStackTrace();
					// can catch InvocationTargetException
					// can catch InterruptedException
				}
			}
		};
		setVisible(true);
		Thread splashThread = new Thread(waitRunner, "SplashThread");
		splashThread.start();
	}

}
