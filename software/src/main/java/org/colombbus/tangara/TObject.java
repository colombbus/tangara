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

package org.colombbus.tangara;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;

/**
 * This is the super class for non graphical Tangara objects. It enables to
 * define their structure.
 */
public class TObject {
	private GraphicsPane graphicsPane;

	protected static Logger LOG = Logger.getLogger(TObject.class);

	private Map<String, List<String>> handlers = new HashMap<String, List<String>>();

	protected boolean displayEvents = true;

	
	/**
	 * Creates a new tangara non graphical object and adds it to tangara non
	 * graphical objects list.
	 */
	public TObject() {
		graphicsPane = Program.instance().getCurrentGraphicsPane();
		Program.instance().addNonGraphicalObject(this);
	}

	/**
	 * Gets the graphics pane
	 *
	 * @return the used graphics pane
	 */
	public GraphicsPane getGraphicsPane() {
		return graphicsPane;
	}

	/**
	 * Translates the delete command to delete this object
	 *
	 */
	@Localize(value = "common.delete")
	public void delete() {
		deleteObject();
	}

	public String getMessage(String key) {
		String result = null;
		Class<?> currentClass = this.getClass();
		while (result == null && !currentClass.getName().equals(TObject.class.getName())) {
			result = MessagesForObjects.getString(currentClass.getName(), key);
			currentClass = currentClass.getSuperclass();
		}
		if (result == null) {
			result = '!' + key + '!';
		}
		return result;
	}

	/**
	 * Returns if the .properties contains the key
	 *
	 * @param key
	 *            the string to check
	 * @return a boolean
	 */
	public boolean containsMessage(String key) {
		boolean result = false;
		Class<?> currentClass = this.getClass();
		while (!(result || currentClass.getName().equals(TObject.class.getName()))) {
			result = MessagesForObjects.containsMessage(currentClass.getName(), key);
			currentClass = currentClass.getSuperclass();
		}
		return result;
	}

	/**
	 * Delete the object in the tangara non graphical objects list
	 */
	public void deleteObject() {
		Program.instance().deleteObject(this);
	}

	/**
	 * Finds a resource from the resource folder of this object.
	 *
	 * @param fileName
	 * @return URI
	 */
	public URI getResource(String fileName) {
		String objectName = this.getClass().getSuperclass().getName();
		objectName = objectName.substring(objectName.lastIndexOf(".") + 1);
		Configuration conf = Configuration.instance();
		if (conf.isExecutionMode()) {
			try {
				return conf.getObjectsClassLoader().getResource("org/colombbus/tangara/objects/resources/" + objectName + "/" + fileName)
						.toURI();
			} catch (Exception e) {
				LOG.error("error trying to find resource '" + fileName + "'", e);
				return null;
			}
		} else {
			File resourceDirectory = new File(conf.getTangaraPath().getParentFile(), "objects/resources/" + objectName);
			File resourceFile = new File(resourceDirectory, fileName);
			return resourceFile.toURI();
		}
	}

	/**
	 * This method can be overloaded in order to stop the activity of the
	 * object.
	 *
	 * @param value
	 */
	public void freeze(boolean value) {
	}

	// EVENT MANAGEMENT
	protected boolean isEventRegistered(String eventName) {
		Set<String> keys = handlers.keySet();
		return keys.contains(eventName);
	}

	protected void registerEvent(String eventName) {
		if (isEventRegistered(eventName)) {
			LOG.error("Event '" + eventName + "' already registered");
		} else {
			List<String> newHandler = new ArrayList<String>();
			handlers.put(eventName, newHandler);
		}
	}

	protected void unregisterEvent(String eventName) {
		if (!isEventRegistered(eventName)) {
			LOG.error("Cannot unregister event '" + eventName + "': this event is not registered");
		} else {
			handlers.remove(eventName);
		}
	}

	protected void addHandler(String eventName, String handler) {
		if (!isEventRegistered(eventName)) {
			LOG.error("Cannot add handler to event '" + eventName + "': this event is not registered");
		} else {
			List<String> list = handlers.get(eventName);
			list.add(handler);
		}
	}

	protected void clearHandlers(String eventName) {
		if (!isEventRegistered(eventName)) {
			LOG.error("Cannot clear handlers for event '" + eventName + "': this event is not registered");
		} else {
			List<String> list = handlers.get(eventName);
			list.clear();
		}
	}

	protected void clearEvent(String eventName) {
		if (!isEventRegistered(eventName)) {
			LOG.error("Cannot clear event '" + eventName + "': this event is not registered");
		} else {
			clearHandlers(eventName);
		}
	}

	protected void processEvent(String eventName) {
		processEvent(eventName, null);
	}

	protected void processEvent(String eventName, HashMap<String, String> data) {
		if (!isEventRegistered(eventName)) {
			LOG.error("Cannot process event '" + eventName + "': this event is not registered");
		} else {
			// 1st retrieve the corresponding data
			// making a copy, because execution of handlers may alter this list
			// (e.g. in case of a "clearEvent")
			List<String> handlersList = new ArrayList<String>(handlers.get(eventName));
			if (handlersList.size() > 0) {
				// 2nd execute handlers
				for (String handler : handlersList) {
					Program.instance().executeScript(handler, displayEvents);
				}
			}
		}
	}
}
