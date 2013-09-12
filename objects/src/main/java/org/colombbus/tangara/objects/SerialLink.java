package org.colombbus.tangara.objects;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TGraphicalObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;

@SuppressWarnings("serial")
@Localize(value = "SerialLink", localizeParent = true)
public class SerialLink extends TGraphicalObject implements
		SerialPortEventListener {

	SerialPort serialPort;
	private static final String PORT_NAMES[] = { "/dev/tty.usbserial-A4017AB6",
			"/dev/ttyUSB0", "COM3" , "COM4"};
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 9600;
	private BufferedReader input;
	private BufferedWriter output;
	private String command;

	@Localize(value = "SerialLink")
	public SerialLink() {
		initialize();
	}

	public void initialize() {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		try {
			while (portEnum.hasMoreElements()) {
				CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
						.nextElement();
				for (String portName : PORT_NAMES) {
					if (currPortId.getName().equals(portName)) {
						portId = currPortId;
						break;
					}
				}
			}
			if (portId == null) {
				System.out.println("Port non trouve.");
				return;
			}
		} catch (Exception e) {
			LOG.debug("Erreur d'enumeration des ports.");
		}

		try {
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			input = new BufferedReader(new InputStreamReader(
					serialPort.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(
					serialPort.getOutputStream()));

			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			LOG.debug("Erreur d'initialisation du port.");
		}
	}

	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	@Localize(value = "SerialLink.send")
	public void send(String action) {
		command = action;
	}

	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				//System.out.println(command);
				//Thread.sleep(1500);
				output.write(command);
				output.flush();
				command = null;
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		} else {
			System.out.println("No data available");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
		}
	}

}