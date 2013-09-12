
package org.colombbus.tangara.objects;

/*import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.text.MessageFormat;
import java.net.MalformedURLException;
import java.net.URI;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JOptionPane;
import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
*/

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import org.colombbus.tangara.TGraphicalObject;

public class Description extends TGraphicalObject
{

	private int[] numeroPort;
	private String[] nomPort;	
	private int[] xPort;		
	private int[] yPort;		
	private boolean[] usedPort;
	private BufferedImage monImage;
	
	public Description() {
	}
	
	public Description(BufferedImage usedImage) {
		super();
		monImage = usedImage;
		setSize(255,516);
		displayObject();
		//initializeDesc();
	}
	
	public void paintComponent(Graphics g) {
    	g.drawImage(monImage,0,0,null);
    }
	
	public void initializeDesc() { //tableau pour carte arduino Mega uniquement
    	int i, ref=13, ref2=3, ref3=0, cX=6, cY=105;
    	numeroPort = new int[82];
    	nomPort = new String[82];
    	xPort = new int[82];
    	yPort = new int[82];
    	usedPort = new boolean[82];
    	for(i=0; i<82; i++) {
    		numeroPort[i] = i;
    		usedPort[i] = false;
    		if(i == 0) {
    			nomPort[i] = "AREF";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 1) {
    			nomPort[i] = "GND (1)";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i >= 2 && i <= 13) {
    			nomPort[i] = "PWM " + ref;
    			xPort[i] = cX;
    			yPort[i] = cY;
    			if(ref == 8)
    				cX += 18;
    			else
    				cX += 9;
    			ref--;
    		}
    		else if(i == 14) {
    			nomPort[i] = "TX >1";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 15) {
    			nomPort[i] = "RX >0";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 18;
    		}
    		else if(i >= 16 && i <= 21) {
    			if(i%2 == 0)
    				nomPort[i] = "TX" + ref2 + " " + (i-2);
    			else {
    				nomPort[i] = "RX" + ref2 + " " + (i-2);
    				ref2--;
    			}
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 22) {
    			nomPort[i] = "SOA 20";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 23) {
    			nomPort[i] = "SCL 21";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 27;
    		}
    		else if(i == 24) {
    			nomPort[i] = "5V (1)";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 25) {
    			nomPort[i] = "5V (2)";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX -= 9;
    			cY += 9;
    		}
    		else if(i >= 26 && i <= 57) {
    			nomPort[i] = "DIGITAL " + (i-4);
    			xPort[i] = cX;
    			yPort[i] = cY;
    			if(i%2 == 0)
    				cX += 9;
    			else {
    				cX -= 9;
    				cY += 9;
    			}
    		}
    		else if(i == 58) {
    			nomPort[i] = "GND (2)";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 59) {
    			nomPort[i] = "GND (3)";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX = 141;
    			cY = 177;
    		}
    		else if(i == 60) {
    			nomPort[i] = "RESET";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 61) {
    			nomPort[i] = "3V3";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 62) {
    			nomPort[i] = "5V";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 63) {
    			nomPort[i] = "GND (4)";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 64) {
    			nomPort[i] = "GND (5)";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 9;
    		}
    		else if(i == 65) {
    			nomPort[i] = "VIN";
    			xPort[i] = cX;
    			yPort[i] = cY;
    			cX += 16;
    		}
    		else {
    			nomPort[i] = "ANALOG IN " + ref3;
    			xPort[i] = cX;
    			yPort[i] = cY;
    			if(ref == 7)
    				cX += 18;
    			else
    				cX += 9;
    			ref3++;
    		}
    	}
    }
	
}
