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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

//import javax.help.CSH;
//import javax.help.HelpBroker;
//import javax.help.HelpSet;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

//import com.sun.java.help.search.Indexer;

/**
 * This class enables to create a new Help window
 */
public class HelpWindow {

	private static Logger LOG = Logger.getLogger(HelpWindow.class); // @jve:decl-index=0:

	private static final String HELPSET_NAME_BASE="structure_";
	private static final String HELPSET_NAME_SUFFIX=".hs";
	private static final String MAP_NAME_BASE = "Map_";
	private static final String MAP_NAME_SUFFIX = ".jhm";
	private static final String TAM_NAME_BASE = "TAM_";
	private static final String TAM_NAME_SUFFIX = ".xml";
	private static final String FIRST_PAGE="00_intro";
	private static final String LOG_FILE="log.txt";
	private static final String SEARCH_DIR="JavaHelpSearch";
	private static final String CONFIG_FILE="Config.txt";

	private PrintWriter print;

	private PrintWriter print2;

	/**
	 * Creates a new Help window associated with the JMenuItem passed as parameters
	 * @param help
	 */
	public HelpWindow(JMenuItem help)
	{
		File path = new File(Configuration.instance().getTangaraPath().getParentFile(),"Help");

		File log = new File(path,LOG_FILE);

		if(!log.exists())
		{
			// Log file does not exist : we create the help set
			createJavaHelp(path, Configuration.instance().getLanguage());
		}
		else
		{
			// Log file exists : we check that help set correspond to the current language
			try {
				BufferedReader reader = new BufferedReader(new FileReader(log));
				String ligne = reader.readLine();
				if (ligne!=null)
				{
					StringTokenizer st = new StringTokenizer(ligne);
					if (st.nextToken().equals("Language") && !st.nextToken().equals(Configuration.instance().getLanguage()))
						// Language has changed: we re-create the help set
						createJavaHelp(path, Configuration.instance().getLanguage());
				}
				reader.close();
			} catch (Exception e) {
				LOG.error("Error while reading log file " + e);
			}
		}

		// Set up the help viewer
//FIXME help desactivated
//		try {
//			URL [] list = new URL[1];
//			list[0] = path.toURI().toURL();
//
//			ClassLoader cl = new URLClassLoader(list);
//			URL url = HelpSet.findHelpSet(cl, HELPSET_NAME, new Locale(Configuration.instance().getLanguage()));
//			HelpSet hs = new HelpSet(cl, url);
//
//			HelpBroker hb = hs.createHelpBroker();
//
//			CSH.setHelpIDString(help, FIRST_PAGE);
//
//			help.addActionListener(new CSH.DisplayHelpFromSource(hb));
//		} catch (Exception e1) {
//			LOG.error("Error1 " + e1);
//		}
	}


	/**
	 * Copies a file or directory
	 * @param src
	 * 		the file or directory to copy
	 * @param dest
	 * 		where copy
	 * @throws IOException
	 */
	public static void copyFile(File src, File dest) throws IOException {
	      if (!src.exists()) throw new IOException(
	         "File not found '" + src.getAbsolutePath() + "'");
	      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
	      BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));

	      byte[] read = new byte[4096];
	      int len;
	      while ((len = in.read(read)) > 0)
	         out.write(read, 0, len);

	      out.flush();
	      out.close();
	      in.close();
	   }

	/**
	 * Deletes the directory passed as parameters
	 * @param dir
	 * 		the directory to delete
	 */
	public static void deleteDir(File dir)
	{
		File [] list = dir.listFiles();
		for (int i = 0; i<list.length; i++)
		{
			if (list[i].isDirectory())
				deleteDir(list[i]);
			else
				list[i].delete();
		}
		dir.delete();
	}


	private String getTitle(File f)
	{
		String result = "";
		if (!f.getName().endsWith(".html"))
			return "not HTML";
		else
		{
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String ligne = reader.readLine();
				while (ligne!=null && !ligne.contains("<TITLE>") && !ligne.contains("<title>"))
				{
					ligne = reader.readLine();
				}
				reader.close();
				if (ligne==null)
					return "No TITLE in page " + f.getName();
				else
				{
					String fin = "";
					String debut = "";
					if (ligne.contains("</TITLE>"))
					{
						debut = "<TITLE>";
						fin = "</TITLE>";
					}

					else if (ligne.contains("</title>"))
					{
						debut = "<title>";
						fin = "</title>";
					}

					else
						return "No title in page " + f.getName();

					int fin_index = ligne.lastIndexOf(fin);
					result = ligne.substring(ligne.indexOf(debut) + 7, fin_index);
					return result;
				}
			} catch (Exception e) {
				LOG.error("Error while reading file " + e);
				return "Error for file " +f.getName();
 			}
		}
	}


	private void mapAndTocForFile(File f, File path, String underscore)
	{
		// we get the single name of the file and the urlname
		int under_index2 = f.getName().indexOf("_");
		int point_index2 = f.getName().indexOf(".");
		String named = f.getName().substring(under_index2+1, point_index2);
		String tmp = path.getAbsolutePath()+"Main_pages/fr/";
		String url_name = f.getPath().replace("\\", "/").substring(tmp.length());
		String targetName = url_name.substring(0, url_name.lastIndexOf(".html"));

		//now we will add into the map and the toc
		print.println("<mapID target=\""+ targetName + "\" url=\"pages/" + url_name + "\"/>");
		File dir_associated = new File(f.getParent(), named);


		if ((dir_associated.exists() && dir_associated.isDirectory()) || named.equals("objects"))
		{
			print2.println("<tocitem text=\"" + getTitle(f) + "\" target=\"" + targetName + "\" image=\"tamicon\">");
			if (dir_associated.exists())
			{
				// Apres on fait pareil pour tous les sous fichiers
				File [] sub_files = dir_associated.listFiles();
				for (int i = 0; i<sub_files.length; i++)
				{
					if (!sub_files[i].getName().equals("CVS") && sub_files[i].isFile())
					{
						if (sub_files[i].getName().endsWith(".html"))
								mapAndTocForFile(sub_files[i], path, underscore);
						else
						{
							try {
								copyFile(sub_files[i], new File(path,"pages/"+named+"/"+sub_files[i].getName()));
							} catch (IOException e) {
								LOG.error("Error while copying normal files in help " + e);
							}
						}
					}
				}
			}
			if (named.equals("objects"))
			{
				// Specialement pour les objets on les rajoute tous
				File objects_dir = new File(Configuration.instance().getTangaraPath().getParentFile().getAbsolutePath().replace("\\","/") + "/objects/" );
				File [] listfiles = objects_dir.listFiles();
				Vector<String> list_names = new Vector<String>();
				HashMap<String, String> map = new HashMap<String, String>();
				for (int i = 0 ; i <listfiles.length; i++)
				{
					try {
						if (listfiles[i].getName().endsWith(".jar"))
						{
							int point_index = listfiles[i].getName().lastIndexOf(".");
							String name = listfiles[i].getName().substring(0, point_index);

							// Copy the pages in the right directory
							File object_dir = new File(path , "pages/" + name );
							object_dir.mkdir();
							File object_ressource = new File(Configuration.instance().getTangaraPath().getParentFile().getAbsolutePath().replace("\\","/") + "/objects/resources/"+name+"/Help");
							if (object_ressource.exists())
							{
								File [] list_html_object = object_ressource.listFiles();
								for (int e = 0; e <list_html_object.length; e++)
								{
									if (list_html_object[e].getName().endsWith(".html"))
									{
										int under_index = list_html_object[e].getName().lastIndexOf("_");
										if (underscore.equals("") && under_index==-1)
											copyFile(list_html_object[e], new File(path,"pages/"+name+"/"+list_html_object[e].getName()));
										else if(!underscore.equals(""))
										{
											if (list_html_object[e].getName().contains(underscore))
												copyFile(list_html_object[e], new File(path,"pages/"+name+"/"+list_html_object[e].getName()));
										}
									}
									else
										copyFile(list_html_object[e], new File(path,"pages/" + name + "/" + list_html_object[e].getName()));
								}
								// Gets the name of the object in the selected language
								String name_lang = null;
								if (underscore.equals(""))
									name_lang = name;
								else
								{
									name_lang = getLangName(listfiles[i]);
								}
								if (name_lang!=null)
								{
									list_names.add(name_lang);
									map.put(name_lang, name);
									//Add to the map file
									print.println("<mapID target=\""+ name + "\" url=\"pages/"+name+"/index"+underscore+".html\" />");
								}
							}
						}
					}catch (Exception e2) {
						LOG.error("Error2 getHelp " + e2);
					}
				}
				// Add to the tam file
				Collections.sort(list_names);
				for (String s : list_names)
				{
					print2.println("<tocitem text=\"" + s + "\" target=\""+map.get(s)+"\" image=\"fileicon\" />" );
				}
			}
			print2.println("</tocitem>");
		}
		else
		{
			// pas de sous fichiers
			print2.println("<tocitem text=\"" + getTitle(f)+ "\" target=\"" + targetName + "\" image=\"fileicon\"/>");
		}

		File parent = new File(path,"pages/" + url_name.substring(0, url_name.lastIndexOf(named)-3));
		if (!parent.exists())
			parent.mkdirs();
		File in_pages = new File (path,"pages/" + url_name);
		try {
			in_pages.createNewFile();
			copyFile(f, in_pages);
		} catch (IOException e3) {
			LOG.error("Error 3 getHelp " + e3 +" " + f.getName());
		}
	}

	/**
	 * Creates the java help
	 * @param path
	 * 		the path where to create the java help
	 * @param lang
	 * 		the spoken language
	 */
	private void createJavaHelp(File path, String lang)
	{
		File pages = new File(path, "pages");
		if (pages.exists())
			deleteDir(pages);
		pages.mkdir();

		File javahelpsearch = new File(path,SEARCH_DIR);
		if (javahelpsearch.exists())
			deleteDir(javahelpsearch);

		File helpset_file = new File(path,HELPSET_NAME_BASE + lang + HELPSET_NAME_SUFFIX);
		File config_file = null;
		File map_file = null;
		File tam_file = null;

		String underscore = "";

		if (helpset_file.exists())
		{
			// There is a helpset corresponding to the current language
			map_file = new File(path, MAP_NAME_BASE + lang + MAP_NAME_SUFFIX);
			tam_file = new File(path, TAM_NAME_BASE+ lang + TAM_NAME_SUFFIX);
			underscore = "_" + lang;
		}
		else
		{
			// There is no helpset : we use the default one
			map_file = new File(path + "Map.jhm");
			tam_file = new File(path+ "TAM.xml");
		}

		config_file = new File(path, CONFIG_FILE);

		if (config_file.exists())
			config_file.delete();
		if (map_file.exists())
			map_file.delete();
		if (tam_file.exists())
			tam_file.delete();

		boolean test = true;

		try {
			// Generates the jhm and copy the file for objects
			// Generates the TAM.xml file
			test = test && map_file.createNewFile();
			test = test && tam_file.createNewFile();
			File lang_dir = new File(path, "Main_pages/" + lang);
			test = test && lang_dir.exists();
			if (test)
			{
				print = new PrintWriter(new BufferedWriter(new FileWriter(map_file)));
				// debut  + images
				print.println("<?xml version='1.0' encoding='ISO-8859-1' ?>\n " +
						"<!DOCTYPE map\n" +
				  "PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN\"\n" +
				         "\"http://java.sun.com/products/javahelp/map_1_0.dtd\">\n" +
				         "\n"+
				"<map version=\"1.0\">\n" +
					"<mapID target=\"image\" url=\"Main_pages/logo_cbs_petit.gif\" />\n" +
					"<mapID target=\"tamicon\" url=\"Main_pages/tam.gif\" />\n" +
					"<mapID target=\"fileicon\" url=\"Main_pages/file.gif\" />\n" +
					"<mapID target=\""+FIRST_PAGE+"\" url=\"pages/"+FIRST_PAGE+".html\" />");

				print2 = new PrintWriter(new BufferedWriter(new FileWriter(tam_file)));
				print2.println(" <?xml version='1.0' encoding='ISO-8859-1'  ?> \n "+
							"<!DOCTYPE toc \n" +
							"PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN\"\n" +
							"\"../dtd/toc_2_0.dtd\">\n" +
							"<toc version=\"2.0\"> \n<tocitem text=\""+getTitle(new File(path,"Main_pages/" + lang + "/"+FIRST_PAGE+".html")) + "\" target=\""+FIRST_PAGE+"\" image=\"image\">" );
				copyFile(new File(path,"Main_pages/" + lang + "/"+FIRST_PAGE+".html"), new File(path , "pages/"+FIRST_PAGE+".html"));


				// generation of the tree
				File [] list_lang_dir = lang_dir.listFiles();
				for (int a = 0; a<list_lang_dir.length; a++)
				{
					if (!list_lang_dir[a].getName().equals("CVS") && list_lang_dir[a].isFile() && !list_lang_dir[a].getName().equals(FIRST_PAGE+".html"))
					{
						mapAndTocForFile(list_lang_dir[a], path, underscore);
					}
				}

				// Ends
				print.println("</map>");
				print.close();

				print2.println("</tocitem>\n</toc>");
				print2.close();

				test = test && config_file.createNewFile();
				if (test)
				{
					//Generates the config file
					print = new PrintWriter(new BufferedWriter(new FileWriter(config_file)));
					print.print("IndexRemove " + path.getAbsolutePath());
					print.close();
				}
			}
		} catch (Exception e) {
			LOG.error("Error getHelp " + e);
		}
		// Generates the search database
		String[] args=new String[] { pages.getAbsolutePath(), "-c", config_file.getAbsolutePath() ,"-db", javahelpsearch.getAbsolutePath(), "-locale", lang  };
		//Indexer indexer=new Indexer();
//FIXME help desactived
//		Indexer.main(args);

		Calendar c = GregorianCalendar.getInstance();
		c.setTime(new Date());
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH)+1;
		int year = c.get(Calendar.YEAR);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int seconds = c.get(Calendar.SECOND);

		File log = new File(path, LOG_FILE);
		try {
			log.createNewFile();
			PrintWriter printlog = new PrintWriter(new BufferedWriter(new FileWriter(log)));
			printlog.println("Language " + lang);
			printlog.println("Date : " + year + " " + month + " " + day + " " + hour + " " + minute + " " + seconds );
			printlog.close();
		} catch (Exception e) {
			LOG.error("Error while creating log " + e);
		}
	}


	/**
	 * Enables to get the name of an object in the spoken language thanks to the jar file
	 * @param jarName
	 * 		the file that contains the object classes
	 * @return
	 * 		the object name in the spoken language
	 */
	private String getLangName(File jarName)
	{
		String name = null;

		try {
			URL url = jarName.toURI().toURL();
			JarInputStream jarFile = new JarInputStream(url.openStream());
	        JarEntry jarEntry = jarFile.getNextJarEntry();
	        while (jarEntry!=null)
	        {
	        	if (!jarEntry.isDirectory() && jarEntry.getName().contains(Configuration.instance().getLanguage()))
	        	{
	        		int lang_index = jarEntry.getName().lastIndexOf(Configuration.instance().getLanguage());
	        		name = jarEntry.getName().substring(lang_index+3, jarEntry.getName().length()-6);
	        	}
	        	jarEntry = jarFile.getNextJarEntry();
	        }
		} catch (Exception e) {
			LOG.error("Error getLangName " + jarName +" " + e);
		}
		return name;
	}
}
