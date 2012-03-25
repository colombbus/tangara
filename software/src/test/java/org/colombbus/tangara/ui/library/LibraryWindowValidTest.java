/**
 *
 */
package org.colombbus.tangara.ui.library;

import java.awt.HeadlessException;

import javax.swing.JFrame;

import org.junit.Ignore;

@SuppressWarnings("serial")
@Ignore
public class LibraryWindowValidTest extends JFrame {

	private JFrame secondaryFrame;
	public LibraryWindowValidTest() throws HeadlessException {
		super("main");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(400,400);
		setLocation(200, 200);
	}

	public void showSecondaryFrame() {
		secondaryFrame = new JFrame("secondary");
		secondaryFrame.setSize(150,150);
		secondaryFrame.setLocationRelativeTo(this);
		secondaryFrame.setVisible(true);
	}


	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                createAndShowGUI();
            }

       });

	}

	private static void createAndShowGUI() {
		LibraryWindowValidTest primaryFrame = new LibraryWindowValidTest();
		primaryFrame.setVisible(true);
		primaryFrame.showSecondaryFrame();
	}



}
