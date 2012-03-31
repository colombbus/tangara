package org.colombbus.tangara.objects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

@SuppressWarnings("serial")
@Localize(value="Chronometer",localizeParent=true)
public class Chronometer extends TGraphicalObject {
	
	private Timer timer;
	private long beginingTime;
	private String text;
	private JLabel label;
	private long stoppedTime;
	private boolean started = false;
	private long correctionTime;
	
	@Localize(value="Chronometer")
	public Chronometer() {
		createTextArea();
		timer = new Timer(10, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateClock();
			}
		});
		displayObject();
	}
	
	private void createTextArea() {
        setSize(50,20);
        setOpaque(false);
		setLayout(new BorderLayout());
		label = new JLabel();
        label.setSize(new Dimension(getObjectWidth(),getObjectHeight()));
        add(label,BorderLayout.CENTER);
        setTime(0); //$NON-NLS-1$
	}
	
	private void updateClock() {
		setTime(getTime());
	}
	
	private void setTime(long time) {
		this.text = formateTime(time);
    	computeSize();
		label.setText(text);
    }

    private void computeSize() {
        FontMetrics fontMetrics = label.getFontMetrics(label.getFont());
        int length = fontMetrics.stringWidth(text);
        setSize(length+10,fontMetrics.getHeight());
    }
    
    private String formateTime(long time) {
    	long minutes, seconds, hundredths;
    	
    	minutes = time/60000;
    	seconds = (time - (minutes * 60000))/1000;
    	hundredths = (time - (minutes * 60000) - seconds*1000)/10; 
    	
    	return String.format("%02d:%02d:%02d", minutes, seconds, hundredths);
    }
    
	@Localize(value = "common.setColor")
	public void setColor(String colorName) {
		Color c = TColor.translateColor(colorName, Color.black);
		label.setForeground(c);
	}
	
	@Localize(value="Chronometer.setTextSize")
    public void setTextSize(int value) {
        Font currentFont = label.getFont();
        label.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), value));
        computeSize();
    }

	@Localize(value="Chronometer.start")
	public void start() {
		beginingTime = System.currentTimeMillis();
		correctionTime = 0;
		timer.start();
	}
	
	@Localize(value="Chronometer.initialize")
	public void initialize() {
		beginingTime = System.currentTimeMillis();
		correctionTime = 0;
        setTime(0); //$NON-NLS-1$
	}

	@Localize(value="Chronometer.stop")
	public void stop() {
		stoppedTime = getTime();
		timer.stop();
		started = false;
	}

	@Localize(value="Chronometer.getTime")
	public long getTime() {
		if(timer.isRunning())
			return System.currentTimeMillis() - beginingTime + correctionTime;
		else
			return stoppedTime;
	}

	@Localize(value="Chronometer.getFormattedTime")
	public String getFormattedTime() {
		return formateTime(getTime());
	}
	
    @Override
	public void freeze(boolean shallFreeze)
    {
    	super.freeze(shallFreeze);
    	if (shallFreeze) {
    		started = timer.isRunning();
    		correctionTime = getTime();
    		timer.stop();
    	} else {
    		if (started) {
    			beginingTime = System.currentTimeMillis();
        		timer.restart();
    		}
    	}
    }
	
}
