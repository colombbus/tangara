/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2009-2012 Colombbus (http://www.colombbus.org)
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
package org.colombbus.tangara.update;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
class HyperLinkLabel extends JLabel implements MouseListener {

	private static final Logger LOG = Logger.getLogger(HyperLinkLabel.class);

	private String url;
	private Color overForeground = Color.RED;
	private Color defaultForeground = Color.BLUE;

	public HyperLinkLabel(String text, String url) {
		super("<html><u>" + text + "</u></html"); //$NON-NLS-1$ //$NON-NLS-2$
		this.url = url;
		setForeground(defaultForeground);
		// defaultForeground = getForeground();
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseListener(this);
	}

	/**
	 * @param text
	 * @param horizontalAlignment
	 */
	public HyperLinkLabel(String text, String url, int horizontalAlignment) {
		super(text, horizontalAlignment);
		this.url = url;
		addMouseListener(this);
	}

	public void setOverForeground(Color overForeground) {
		this.overForeground = overForeground;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		browseLink();
	}

	private void browseLink() {
		try {
			BrowserLauncher.browse(url);
		} catch (Exception ex) {
			LOG.warn("Fail to browse url", ex); //$NON-NLS-1$
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setForeground(overForeground);
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setForeground(defaultForeground);
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
