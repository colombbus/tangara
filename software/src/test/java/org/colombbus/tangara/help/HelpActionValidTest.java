/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2009 Colombbus (http://www.colombbus.org)
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
package org.colombbus.tangara.help;

import java.awt.BorderLayout;

import javax.swing.*;

import org.colombbus.helpengine.DefaultHelpEngine;
import org.colombbus.helpengine.HelpEngine;
import org.junit.*;

public class HelpActionValidTest {

	HelpEngine helpEngine;
	JFrame frame;

	@Before
	public void setup() {
		helpEngine = new DefaultHelpEngine();
		helpEngine.setPort(7777);
		helpEngine.startup();
	}

	@After
	public void teardown() {
		helpEngine.shutdown();
	}



	@Test
	public void testHelpAction() throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				frame = new JFrame();
				frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
				frame.setLayout(new BorderLayout());
				JButton button = new JButton(new HelpAction(helpEngine));
				frame.add(button, BorderLayout.CENTER);
				frame.setSize(200, 100);
				frame.setVisible(true);
			}
		});
		waitWhileFrameVisible();
	}

	private void waitWhileFrameVisible() throws InterruptedException {
		while (frame.isVisible()) {
			Thread.sleep(500);
		}
	}
}
