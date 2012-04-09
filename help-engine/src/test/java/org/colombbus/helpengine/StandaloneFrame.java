package org.colombbus.helpengine;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;

import javax.swing.*;

@SuppressWarnings("nls")
class StandaloneFrame {
	private Semaphore semaphore ;
	private HelpEngine engine;
	private JFrame frame;

	public StandaloneFrame( HelpEngine engine, Semaphore semaphore) {
		this.engine = engine;
		this.semaphore = semaphore;

		buildUI();
		frame.setVisible(true);
	}

	private void buildUI() {
		frame = new JFrame("Standalone test");
		frame.setBounds(50, 50, 100, 100);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(
				new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						release();
					}
				});
		frame.getContentPane().setLayout(new BorderLayout(15, 15));
		JButton button = createButton();
		frame.getContentPane().add(button, BorderLayout.CENTER);
	}

	void release() {
		semaphore.release();
	}

	private JButton createButton() {
		@SuppressWarnings("serial")
		Action helpAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				help();
			}
		};

		JButton button = new JButton();
		button.setAction(helpAction);
		button.setText("Help !"); //$NON-NLS-1$
		return button;
	}

	void help() {
		engine.openHelp();
	}
}