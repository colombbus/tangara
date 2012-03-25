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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;

@Localize(value = "TWitness", localizeParent = true)
public class TWitness extends TObject {
	private static final Logger LOG = Logger.getLogger(TWitness.class);

	private Map<String, Object> context = new HashMap<String, Object>();
	private TGraphicalObject source;

	public TWitness() {
		context.put("x", "0");
		context.put("y", "0");
	}

	public Object getContextValue(String key) {
		synchronized (context) {
			if (context.containsKey(key))
				return context.get(key);
			else {
				LOG.error("Key '" + key + "' cannot be found in this event's context");  //$NON-NLS-1$//$NON-NLS-2$
				return null;
			}
		}
	}

	public String getContextStringValue(String key) {
		Object value = getContextValue(key);
		if ((value != null) && (value instanceof String)) {
			return (String) value;
		}
		return null;
	}

	public int getContextIntValue(String key) {
		Object value = getContextValue(key);
		if ((value != null) && (value instanceof Integer)) {
			return ((Integer) value).intValue();
		}
		return -1;
	}

	public void setContextValue(String key, Object value) {
		synchronized (context) {
			context.put(key, value);
		}
	}

	@Localize(value = "TWitness.getObject")
	public TGraphicalObject getObject() {
		return source;
	}

	public void setObject(TGraphicalObject object) {
		source = object;
	}

	@Localize(value = "TWitness.getObjectName")
	public String getObjectName() {
		return Program.instance().getObjectName(source);
	}

	public void setContext(Map<String, Object> data) {
		synchronized (context) {
			context.putAll(data);
		}
	}

	@Localize(value = "TWitness.getXCoordinate")
	public int getXCoordinate() {
		return getContextIntValue("x"); //$NON-NLS-1$
	}

	@Localize(value = "TWitness.getYCoordinate")
	public int getYCoordinate() {
		return getContextIntValue("y"); //$NON-NLS-1$
	}

	@Override
	public void deleteObject() {
		context.clear();
		super.deleteObject();
	}

}
