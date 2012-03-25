package org.colombbus.tangara;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class BaseFrame extends JFrame {
	protected JProgressBar progress = null;
	protected JLabel label = null;
	
	public BaseFrame() {
		label = new JLabel();
		label.setBackground(Color.white);
		label.setForeground(Color.black);
		label.setOpaque(true);
		label.setBorder(new EmptyBorder(5,5,0,5));
		//label.setPreferredSize(new Dimension(400,10));
		progress = new JProgressBar();
		progress.setBackground(Color.white);
		progress.setValue(0);
		label.setPreferredSize(new Dimension(400,50));
		this.setBackground(Color.white);
		this.setLayout(new BorderLayout());
		this.add(label, BorderLayout.NORTH);
		this.add(progress, BorderLayout.CENTER);
		JPanel leftPane = new JPanel();
		leftPane.setPreferredSize(new Dimension(5,5));
		JPanel rightPane = new JPanel();
		rightPane.setPreferredSize(new Dimension(5,5));
		JPanel bottomPane = new JPanel();
		bottomPane.setPreferredSize(new Dimension(5,5));
		this.add(leftPane, BorderLayout.WEST);
		this.add(rightPane, BorderLayout.EAST);
		this.add(bottomPane, BorderLayout.SOUTH);

		this.pack();
    	this.setResizable(false);
    	this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    	this.setLocationRelativeTo(null);
		this.setTitle(Messages.getString("EditorFrame.application.title"));
	}
	
	public BaseFrame(String text) {
		this();
		setText(text);
	}
	
	public void setText(String text) {
		label.setText(text);
	}
	
	public void setMaximumProgress(int maxValue) {
		int currentValue = progress.getValue();
		int currentMax = progress.getMaximum();
		progress.setMaximum(maxValue);
		if (currentValue > 0) {
			progress.setValue((int)Math.floor(currentValue*maxValue/currentMax));
		}
	}
	
	public void setProgress(int value) {
		progress.setValue(value);
	}
	
	public void setIndeterminate(boolean value) {
		progress.setIndeterminate(value);
	}
	
}
