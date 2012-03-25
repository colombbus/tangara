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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

/**
 * This class permits to display a window giving a library of all available objects.
 * @author Lionel
 *
 */
@SuppressWarnings("serial")
public class LibraryWindow extends JFrame{
	private static final Logger LOG = Logger.getLogger(LibraryWindow.class);
	
	private static final int MARGIN_X = 5;
	private static final int MARGIN_Y = 10;

	private static final int FONT_SIZE = 14;


    private JLabel helpText = null;
    private JPanel bottomPanel = null;
    private JButton exitButton = null;


    private boolean change = false;
    private Map<String,String> map = new HashMap<String, String>();

	private JList list = null;
	private DefaultListModel listModel = null;
	private JPanel mainPanel = null;
	private JScrollPane jscrollpane = null;
	private final List<MyObject> vector = new Vector<MyObject>();



	/**
	 * Creates an instance of this class.
	 * @param parent
	 */
	public LibraryWindow(EditorFrame parent)
	{		
		this.setTitle(Messages.getString("Library.title"));
		this.setSize(new Dimension(640, 480));
		this.setContentPane(getMainPanel());
	    this.setLocation(new Point((parent.getX()+(parent.getWidth()-getWidth())/2),(parent.getY()+(parent.getHeight()-getHeight())/2)));
	    list.setCellRenderer(new MyCellRenderer());
	    this.setResizable(false);
	}

    /**
     * This method initializes mainPanel
     *
     * @return javax.swing.JPanel
     */
	private JPanel getMainPanel()
	{
		if (mainPanel == null)
		{
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(getHelpText(), BorderLayout.NORTH);
			mainPanel.add(getJScrollPane(), BorderLayout.CENTER);
			mainPanel.add(getBottomPanel(), BorderLayout.SOUTH);
		}
		return mainPanel;
	}


	public void setChange(boolean aChange)
	{
		change = aChange;
	}

	public boolean getChange()
	{
		return change;
	}

    /**
     * This method initializes topPanel
     *
     * @return javax.swing.JPanel
     */
    private JLabel getHelpText()
    {
        if (helpText == null)
        {
            helpText = new JLabel();
            helpText.setText(Messages.getString("Library.helpText"));
            helpText.setBorder(new EmptyBorder(MARGIN_Y,MARGIN_X,0,MARGIN_X));
        }
        return helpText;
    }


    /**
     * This method initializes bottomPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getBottomPanel()
    {
        if (bottomPanel == null)
        {
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.X_AXIS));
            bottomPanel.add(Box.createHorizontalGlue());
            bottomPanel.add(getExitButton());
            bottomPanel.setBorder(new EmptyBorder(MARGIN_Y,MARGIN_X,MARGIN_Y,MARGIN_X));
        }
        return bottomPanel;
    }



    /**
     * This method initializes exitObject Button
     * @return JButton
     */
    private JButton getExitButton()
    {
    	if (exitButton == null)
    	{
    		exitButton = new JButton(Messages.getString("Library.exit"));
        	exitButton.addActionListener(new ActionListener(){
    			@Override
				public void actionPerformed(ActionEvent e)
    			{
    				if (change)
    				{
    					JOptionPane.showMessageDialog(null, Messages.getString("Library.change.content"),
								Messages.getString("Library.change.title"),
								JOptionPane.INFORMATION_MESSAGE);
    				}
    				setVisible(false);
    				dispose();
    			}
        	});
    	}
    	return exitButton;
    }


    /**
     * This method initializes JScrollPane
     *
     * @return javax.swing.JScrollPane
     */
	private JScrollPane getJScrollPane()
	{
		if (jscrollpane==null)
		{
			jscrollpane = new JScrollPane();
		}
		jscrollpane.setName("ScrollPane");
		jscrollpane.setViewportView(getList());
		jscrollpane.setBorder(new CompoundBorder(new EmptyBorder(MARGIN_Y, MARGIN_X,0,MARGIN_X), new LineBorder(new Color(127,157,185))));
		return jscrollpane;
	}

	/**
	 * Permits to get the JList component.
	 * @return JList
	 */
	private JList getList()
	{
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setBackground(Color.white);
		list.setForeground(Color.BLACK);
		list.setSelectionBackground(new Color(255,255,153));
		list.setSelectionForeground(Color.BLACK);

		List<String> toDelete = new ArrayList<String>();
		File logFile = new File(Configuration.instance().getTangaraPath().getParentFile().getAbsolutePath().replace("\\", "/") + "/objects/log.txt");
		if (logFile.exists())
		{
			try {
				BufferedReader reader = new BufferedReader(new FileReader(logFile));
				String ligne = null;
				while ((ligne = reader.readLine())!=null)
				{
					if (!ligne.equals(""))
						toDelete.add(ligne);
				}
				reader.close();
			}
			catch (Exception e) {
			LOG.error("Error while reading " + e);
			}
		}

		String path = Configuration.instance().getTangaraPath().getParentFile().getAbsolutePath().replace("\\", "/");
		path +="/objects/";
		File file = new File(path);
		File [] tab_jar = file.listFiles();
		for (int i = 0; i<tab_jar.length; i++)
		{
			if (tab_jar[i].getName().endsWith(".jar"))
			{
				boolean test = false;
				try {
					URL url = tab_jar[i].toURI().toURL();
					JarInputStream jarFile = new JarInputStream(url.openStream());
					JarEntry jarEntry = jarFile.getNextJarEntry();
				    while (jarEntry!=null)
				    {
				    	if (!jarEntry.isDirectory())
				    	{
				    		int last_slash = jarEntry.getName().lastIndexOf("/");
				    		String tmp = jarEntry.getName().substring(0, last_slash);
				    		int previous_slash = tmp.lastIndexOf("/");
				    		tmp = tmp.substring(previous_slash+1);

				    		if (tmp.equals(Configuration.instance().getLanguage()))
				    		{
				    			test = true;
				    			int point_index = jarEntry.getName().lastIndexOf(".");
				    			// we get the name in the spoken language
				    			String name = jarEntry.getName().substring(last_slash+1,point_index);
				    			int a = tab_jar[i].getName().lastIndexOf(".");
				    			String en_name = tab_jar[i].getName().substring(0, a);
				    			String description = " ";
				    			try{
				    				if (MessagesForObjects.containsMessage("org.colombbus.tangara.objects."+en_name,"description"))
					    				description = MessagesForObjects.getString("org.colombbus.tangara.objects."+en_name,"description");
				    			}
				    			catch(MissingResourceException e5)
				    			{ LOG.warn("Resource not found " + e5);} 

		  				        boolean q = false;
		  				        for (String s : toDelete)
		  				        {
		  				        	if (s.equals(en_name))
		  				        		q = true;
		  				        }

		  				        if (!q)
		  				        {
		  				        	MyObject o = new MyObject(name, description);
			  				        vector.add(o);
			  				        map.put(name, en_name);
		  				        }
				    		}
				    	}
				    	jarEntry = jarFile.getNextJarEntry();
				    }

				    if (!test)
				    {
						jarFile = new JarInputStream(url.openStream());
						jarEntry = jarFile.getNextJarEntry();
					    while (jarEntry!=null)
					    {
					    	if (!jarEntry.isDirectory())
					    	{
					    		int last_slash = jarEntry.getName().lastIndexOf("/");
					    		String tmp = jarEntry.getName().substring(0, last_slash);
					    		int previous_slash = tmp.lastIndexOf("/");
					    		tmp = tmp.substring(previous_slash+1);

					    		if (tmp.equals(Configuration.instance().getDefaultLanguage()))
					    		{
					    			int point_index = jarEntry.getName().lastIndexOf(".");
					    			// we get the name in the spoken language
					    			String name = jarEntry.getName().substring(last_slash+1,point_index);
					    			int a = tab_jar[i].getName().lastIndexOf(".");
					    			String en_name = tab_jar[i].getName().substring(0, a);
			  				        String description = MessagesForObjects.getString("org.colombbus.tangara.objects."+en_name,"description");

			  				      boolean q = false;
			  				        for (String s : toDelete)
			  				        {
			  				        	if (s.equals(en_name))
			  				        		q = true;
			  				        }

			  				        if (!q)
			  				        {
			  				        	MyObject o = new MyObject(name, description);
			  				        	vector.add(o);
			  				        	map.put(en_name, en_name);
			  				        }
					    		}
					    	}
					    	jarEntry = jarFile.getNextJarEntry();
					    }
				    }

				} catch (Exception e) {
					LOG.error("ERROR " + e);
				}

			}
		}
		Font f = new Font(list.getFont().getFontName(), list.getFont().getStyle(), FONT_SIZE);
		list.setFont(f);
		ComparatorComponent c = new ComparatorComponent();
		Collections.sort(vector,c);
		for(int x = 0; x < vector.size(); x++) listModel.addElement(new MyObject(vector.get(x).getName(), vector.get(x).getDescription()));
		return list;
	}


	/**
	 * Serves to compare two different objects.
	 * @author Lionel
	 *
	 */
	private class ComparatorComponent implements Comparator<MyObject>
	{
		public ComparatorComponent()
		{
		}

		@Override
		public int compare(MyObject arg0, MyObject arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	}

	private class MyObject
	{
		private String name = null;
		private String description = null;

		public MyObject(String aName, String aDescription)
		{
			name = aName;
			description = aDescription;

		}

		public String getName()
		{
			return name;
		}

		public String getDescription()
		{
			return description;
		}

	}

	/**
	 * Serves to customize the cell render.
	 * @author Lionel
	 *
	 */
	private class MyCellRenderer extends JPanel implements ListCellRenderer
	{

		private JLabel messagePane;


		public MyCellRenderer()
		{
			setLayout(new BorderLayout());

			messagePane = new JLabel();
			messagePane.setPreferredSize(null);
			messagePane.setFont(list.getFont());
			add(messagePane, BorderLayout.WEST);

		}

		private void setMessagePane(MyObject o)
		{

			messagePane.setText("");
			String toWrite = "<html>";
			toWrite += "<b>"+o.getName()+"</b><br>" + o.getDescription() + "</html>";
			messagePane.setText(toWrite);
		}

		/**
		 * Return a component that has been configured to display the specified value.
		 * That component's paint method is then called to "render" the cell.
		 * If it is necessary to compute the dimensions of a list because the list cells do not have a fixed size,
		 *  this method is called to generate a component on which getPreferredSize  can be invoked.
		 *  @param
		 *  		 The JList we're painting.
		 *  @param
    	 *			 The value returned by list.getModel().getElementAt(index).
    	 *  @param
    	 *  		The cells index.
    	 *  @param
    	 *  		True if the specified cell was selected.
    	 *  @param
    	 *  		True if the specified cell has the focus.
		 */

		@Override
		public Component getListCellRendererComponent(JList list,Object value, int index, boolean isSelected, boolean cellHasFocus)
	     {
				Border borderMatte = BorderFactory.createEmptyBorder(5, 10, 5, 0);
				Border borderline = BorderFactory.createMatteBorder(0,0,1,0,Color.LIGHT_GRAY);
				Border border = BorderFactory.createCompoundBorder(borderline, borderMatte);
		         if (isSelected)
		         {
		        	 	// Remove add/delete functionalities
		        	 	//removeObjectButton.setEnabled(true);
		        	    setBackground(list.getSelectionBackground());
				        setForeground(list.getSelectionForeground());
				        messagePane.setBackground(list.getSelectionBackground());
		        		messagePane.setForeground(list.getSelectionForeground());
		         }
		         else
		         {
		        	 	setBackground(list.getBackground());
				        setForeground(list.getForeground());
				        messagePane.setBackground(list.getBackground());
		        		messagePane.setForeground(list.getForeground());
		         }
		         setBorder(border);
		         setEnabled(list.isEnabled());
		         setFont(list.getFont());
		         setOpaque(true);
		         MyObject o = (MyObject)value;
	    	 	 setMessagePane(o);
		         revalidate();
		         return this;
	     }
	 }
}
