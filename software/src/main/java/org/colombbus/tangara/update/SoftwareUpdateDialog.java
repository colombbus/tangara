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

import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.*;

import org.apache.commons.lang.Validate;

@SuppressWarnings("serial")
class SoftwareUpdateDialog extends JDialog implements ActionListener {

	private final ResourceBundle bundle = ResourceBundle.getBundle(SoftwareUpdateDialog.class.getPackage().getName() + ".messages"); //$NON-NLS-1$

	private JButton closeButton;
	private SoftwareUpdateInfo info;
	private Font font = new Font("sans-serif", Font.PLAIN, 12);; //$NON-NLS-1$

	public SoftwareUpdateDialog(SoftwareUpdateInfo info) {
		Validate.notNull(info == null, "info argument is null"); //$NON-NLS-1$
		this.info = info;
		init();
	}

	private void init() {
		initDialog();
		addIntroductionPane();
		addButtonPane();
		addCenterPane();

		pack();
	}

	private void addCenterPane() {
		JPanel centerPane = new JPanel();
		BoxLayout layout = new BoxLayout(centerPane, BoxLayout.Y_AXIS);
		centerPane.setLayout(layout);

		JTextArea descPane = new JTextArea(info.getDescription());
		descPane.setFont(font);
		descPane.setEditable(false);
		centerPane.add(descPane);

		JPanel linkPanel = new JPanel();
		linkPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		String linkTitle = bundle.getString("SoftwareUpdateDialog.linkTitle"); //$NON-NLS-1$
		JLabel linkTitleLabel = new JLabel(linkTitle);
		linkTitleLabel.setFont(font);
		linkPanel.add(linkTitleLabel);
		HyperLinkLabel linkLabel = new HyperLinkLabel(info.getLink(), info
				.getLink());
		linkLabel.setFont(font);
		linkPanel.add(linkLabel);
		linkPanel.setBackground(Color.white);
		linkPanel.setOpaque(true);
		centerPane.add(linkPanel);
		add(centerPane, BorderLayout.CENTER);
	}

	private void initDialog() {
		getRootPane().setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		setLayout(new BorderLayout(5, 5));
		setSize(200, 500);
		String title = bundle.getString("SoftwareUpdateDialog.title"); //$NON-NLS-1$
		setTitle(title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void addButtonPane() {
		Box box = Box.createHorizontalBox();
		box.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		box.add(Box.createGlue());
		String closeLabel = bundle.getString("SoftwareUpdateDialog.close"); //$NON-NLS-1$
		closeButton = new JButton(closeLabel);
		closeButton.addActionListener(this);
		box.add(closeButton);
		add(box, BorderLayout.SOUTH);
	}

	private void addIntroductionPane() {
		String introFormat = bundle
				.getString("SoftwareUpdateDialog.introduction"); //$NON-NLS-1$
		String introduction = MessageFormat.format(introFormat, info
				.getVersion());

		String labelText = String.format("<html><b>%s</b></html>",introduction); //$NON-NLS-1$
		JLabel introLabel = new JLabel(labelText);
		introLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		introLabel.setFont(font);
		add(introLabel, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			setVisible(false);
		}
	}

}
