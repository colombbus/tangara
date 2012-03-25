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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * This class translates messages for objects (new Configuration)
 * @author ESIEE
 *
 */

public class MessagesForObjects {
	
	private static HashMap<String,ResourceBundle> map = new HashMap<String,ResourceBundle>();
	
	private static Logger LOG = Logger.getLogger(MessagesForObjects.class);
	
	private MessagesForObjects()
	{
		
	}

	/**
	 * Creates a resourcebundle according to the language passed as parameters 
	 * @param classe
	 * 	the name of the class who will use this resource bundle
	 * @param language
	 * 	the spoken language
	 */
	public static void loadLocalizedResource(String pack,String language)
	{
		ClassLoader loader = null;
		Configuration conf = Configuration.instance();
		String bundleName = null;
		int index = pack.lastIndexOf(".");
		String className = pack.substring(index+1);
		if (conf.isExecutionMode()) {
			loader = Configuration.instance().getObjectsClassLoader();
			bundleName = "org/colombbus/tangara/objects/resources/"+className+".messages";
		} else {
			try {
				File resourcesDirectory = new File(conf.getTangaraPath().getParentFile(), "objects/resources/");
				URL url = new URL("file:" + resourcesDirectory.getAbsolutePath()+"/");
				URL [] list = new URL[1];
				list[0] = url;
				loader = new URLClassLoader(list);
			} catch (MalformedURLException e) {
				LOG.error("Error trying to get localized resource", e);
				return;
			}
			bundleName = className+".messages";
		}
		Locale defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
		try {
			map.put(pack, ResourceBundle.getBundle(bundleName, new Locale(language), loader) );
		} catch (MissingResourceException e) {
		}
		Locale.setDefault(defaultLocale);
	}
	
	public static String getString(String classbinaryname, String key) {			
		ResourceBundle toFind = map.get(classbinaryname);		
		if (toFind!=null)
		{			
			try {
				return toFind.getString(key);
			}
			 catch (MissingResourceException e) {
					return null;
				}
		}		
		else 
		{
			loadLocalizedResource(classbinaryname, Configuration.instance().getLanguage());
			toFind = map.get(classbinaryname);
			if (toFind == null) {
				return null;
			}
			try {
				return toFind.getString(key);
			}
			 catch (MissingResourceException e) {
					return null;
				}
		}
	}
	/**
	 * Returns if the .properties contains the key
	 * @param key
	 * 		the string to check
	 * @return
	 * 		a boolean
	 */
	public static boolean containsMessage(String classbinaryname, String key)
	{
		ResourceBundle toFind = map.get(classbinaryname);
		if (toFind!=null)
		{						
			if (System.getProperty("java.version").startsWith("1.6"))
				return toFind.containsKey(key);
			else
			{
				Enumeration <String> enume = toFind.getKeys();				
				String s;				
				while (enume.hasMoreElements())
				{
					s = enume.nextElement();
					if (s.equals(key))
						return true;
				}
				return false;
			}
		}
		else
		{
			loadLocalizedResource(classbinaryname, Configuration.instance().getLanguage());
			toFind = map.get(classbinaryname);
			if (toFind == null)
				return false;
			if (System.getProperty("java.version").startsWith("1.6"))
				return toFind.containsKey(key);
			else
			{
				Enumeration <String> enume = toFind.getKeys();				
				String s;				
				while (enume.hasMoreElements())
				{
					s = enume.nextElement();
					if (s.equals(key))
						return true;
				}
				return false;
			}
		}
	}
}
