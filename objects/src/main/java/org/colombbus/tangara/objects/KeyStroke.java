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

package org.colombbus.tangara.objects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.Timer;

import org.colombbus.build.Localize;
import org.colombbus.tangara.GraphicsPane;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TKey;
import org.colombbus.tangara.TObject;

/**
 * This class provides an object capable of adding commands on certain keyboard
 * keys. The user has to click on the background of Tangara to be able to use
 * the key commands.
 * 
 * @author Benoit
 * 
 */
@Localize(value = "KeyStroke", localizeParent = true)
public abstract class KeyStroke extends TObject {
	
	private MouseManager mouseManager = new MouseManager();
	private KeyPressedAction keyPressedAction = new KeyPressedAction();
	private KeyReleasedAction keyReleasedAction = new KeyReleasedAction();

	private boolean display = true;

	public Map<Integer, java.util.List<String>> commands = new Hashtable<Integer, java.util.List<String>>();
	public Map<Integer, java.util.List<String>> releaseCommands = new Hashtable<Integer, java.util.List<String>>();

	private Map<Integer, Boolean> keyStates = new HashMap<Integer, Boolean>();
	
	private boolean active = false;
	private KeysProcessor processor = new KeysProcessor();
	private Timer t;
	
	private static final int SLEEP_TIME = 30;
	private static final int ALL_KEYS = -10;
	
	
	/**
	 * Creates a new instance of KeyStroke
	 */
	@Localize(value = "KeyStroke")
	public KeyStroke() {
		super();
		activate();
	}

	/**
	 * Adds a Key command to a certain Key.
	 * 
	 * @param key
	 * @param command
	 */
	@Localize(value = "KeyStroke.addCommand")
	public void addCommand(String key, String command) {
		try {
			Integer keyCode;
			if (key.length() > 1) {
				keyCode = new Integer(TKey.getKeyCode(key));
			} else if (key.length() == 1) {
				keyCode = new Integer(TKey.getKeyCode(key.charAt(0)));
			} else {
				throw new Exception("key empty"); //$NON-NLS-1$
			}
			synchronized(commands) {
				java.util.List<String> keyCommands = commands.get(keyCode);
				if (keyCommands == null)
					keyCommands = new ArrayList<String>();
				keyCommands.add(command);
				commands.put(keyCode, keyCommands);
				javax.swing.KeyStroke ksPressed = javax.swing.KeyStroke.getKeyStroke(keyCode.intValue(), 0);
				javax.swing.KeyStroke ksReleased = javax.swing.KeyStroke.getKeyStroke(keyCode.intValue(), 0, true);
				GraphicsPane gp = getGraphicsPane();
				gp.registerKeyboardAction(keyPressedAction, keyCode.toString(),ksPressed, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				gp.registerKeyboardAction(keyReleasedAction, keyCode.toString(),ksReleased, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			}
		} catch (Exception e) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.addCommand"), key) + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * Removes the commands of a given key.
	 * 
	 * @param key
	 */
	@Localize(value = "KeyStroke.removeCommands")
	public void removeCommands(String key) {
		try {
			Integer keyCode;
			if (key.length() > 1) {
				keyCode = new Integer(TKey.getKeyCode(key));
			} else if (key.length() == 1) {
				keyCode = new Integer(TKey.getKeyCode(key.charAt(0)));
			} else {
				throw new Exception("key empty"); //$NON-NLS-1$
			}
			synchronized(commands) {
				commands.remove(keyCode);
				javax.swing.KeyStroke ksPressed = javax.swing.KeyStroke.getKeyStroke(keyCode.intValue(), 0);
				javax.swing.KeyStroke ksReleased = javax.swing.KeyStroke.getKeyStroke(keyCode.intValue(), 0, true);
				GraphicsPane gp = getGraphicsPane();
				gp.unregisterKeyboardAction(ksPressed);
				gp.unregisterKeyboardAction(ksReleased);
			}
			//releaseCommands.remove(keyCode);
		} catch (Exception e) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.removeCommand"), key) + " (" + e.getMessage() + ")");
		}
	}

	/**
	 * Activates the object.
	 */
	@Localize(value = "KeyStroke.activate")
	public void activate() {
		if (!active) {
			active = true;
			getGraphicsPane().addGlobalMouseListener(mouseManager);
			t = new Timer(SLEEP_TIME, processor);
			t.start();
		}
	}

	/**
	 * Deactivates the object.
	 */
	@Localize(value = "KeyStroke.deactivate")
	public void deactivate() {
		if (active) {
			active = false;
			keyStates.clear();
			getGraphicsPane().removeGlobalMouseListener(mouseManager);
			if (t != null)
				t.stop();
			processor.stopProcessor();
		}

	}

	/**
	 * Deletes the object.
	 */
	@Override
	public void deleteObject() {
		deactivate();
		removeCommandsRelease();
		commands.clear();
		super.deleteObject();
	}

	/**
	 * Sets the display status.
	 * 
	 * @param value
	 */
	@Localize(value = "KeyStroke.displayCommands")
	public void displayCommands(boolean value) {
		display = value;
	}

	@Localize(value = "KeyStroke.addCommandRelease")
	public void addCommandRelease(String command) {
		try {
			synchronized (releaseCommands) {
				java.util.List<String> commands = releaseCommands.get(ALL_KEYS);
				if (commands == null)
					commands = new ArrayList<String>();
				commands.add(command);
				releaseCommands.put(ALL_KEYS, commands);
			}
		} catch (Exception e) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.addCommand"), "release") + " (" + e.getMessage() + ")");
		}
	}

	
	@Localize(value = "KeyStroke.addCommandRelease2")
	public void addCommandRelease(String key, String command) {
		try {
			Integer keyCode;
			if (key.length() > 1) {
				keyCode = new Integer(TKey.getKeyCode(key));
			} else if (key.length() == 1) {
				keyCode = new Integer(TKey.getKeyCode(key.charAt(0)));
			} else {
				throw new Exception("key empty"); //$NON-NLS-1$
			}
			synchronized (releaseCommands) {
				java.util.List<String> keyCommands = releaseCommands.get(keyCode);
				if (keyCommands == null)
					keyCommands = new ArrayList<String>();
				keyCommands.add(command);
				releaseCommands.put(keyCode, keyCommands);
				javax.swing.KeyStroke ksReleased = javax.swing.KeyStroke.getKeyStroke(keyCode.intValue(), 0, true);
				GraphicsPane gp = getGraphicsPane();
				gp.registerKeyboardAction(keyReleasedAction, keyCode.toString(), ksReleased, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			}
		} catch (Exception e) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.addCommand"), key) + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}


	@Localize(value = "KeyStroke.removeCommandsRelease")
	public void removeCommandsRelease() {
		try {
			synchronized (releaseCommands) {
				releaseCommands.remove(ALL_KEYS);
			}
		} catch (Exception e) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.removeCommand"), "release") + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	private class MouseManager extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getSource() instanceof JComponent)
				((JComponent) e.getSource()).requestFocusInWindow();
		}
	}
	
	class KeysProcessor implements ActionListener {
		private ArrayList<Integer> activeKeys;
		private ArrayList<Integer> deactivatedKeys;
		private boolean keysActive;
		
		public KeysProcessor() {
			super();
			activeKeys = new ArrayList<Integer>();
			deactivatedKeys = new ArrayList<Integer>();
			keysActive = false;
		}

		public synchronized void activateKey(Integer code) {
			synchronized (activeKeys) {
				if (!activeKeys.contains(code)) {
					activeKeys.add(code);
				}
			}
		}

		public synchronized void deactivateKey(Integer code) {
			synchronized (activeKeys) {
				activeKeys.remove(code);
			}
			synchronized (deactivatedKeys) {
				deactivatedKeys.add(code);
			}
		}

		public synchronized void stopProcessor() {
			activeKeys.clear();
			deactivatedKeys.clear();
		}

		@Override
		public void actionPerformed(ActionEvent azerty) {
			int cpt = -1;
			synchronized (activeKeys) {
				cpt = activeKeys.size();
			}
			if (cpt > 0) {
				synchronized (activeKeys) {
					keysActive = true;
					for (Integer i : activeKeys) {
						java.util.List<String> keyCommands = commands.get(i);
						if (keyCommands!=null) {
							for (String command : keyCommands) {
								Program.instance().executeScript(command, display);
							}
						}
					}
				}
			} else if (keysActive) {
				java.util.List<String> releaseAllKeysCommands = releaseCommands.get(ALL_KEYS);
				if (releaseAllKeysCommands != null && releaseAllKeysCommands.size() > 0) {
					for (String command : releaseAllKeysCommands) {
						Program.instance().executeScript(command,display);
					}
				}
				keysActive = false;
			}
			synchronized (deactivatedKeys) {
				for (Integer i : deactivatedKeys) {
					java.util.List<String> keyCommands = releaseCommands.get(i);
					if (keyCommands!=null) {
						for (String command : keyCommands) {
							Program.instance().executeScript(command, display);
						}
					}
				}
				deactivatedKeys.clear();
			}

		}
	}

	private class KeyPressedAction implements ActionListener {
		public KeyPressedAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (active) {
				synchronized (keyStates) {
					Integer code = new Integer(e.getActionCommand());
					if (commands.containsKey(code)) {
						if (keyStates.containsKey(code)) {
							if (!keyStates.get(code)) {
								keyStates.put(code, true);
								processor.activateKey(code);
							}
						} else {
							keyStates.put(code, true);
							processor.activateKey(code);
						}
					}
				}
			}
		}
	}

	private class KeyReleasedAction implements ActionListener {
		public KeyReleasedAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (active) {
				synchronized (keyStates) {
					Integer code = new Integer(e.getActionCommand());
					processor.deactivateKey(code);
					keyStates.put(code, false);
				}
			}
		}
	}
	
	
}
