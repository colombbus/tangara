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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

/**
 *  This object enables to create a connection on an external server
 */
@SuppressWarnings("serial")
@Localize(value="Connection",localizeParent=true)
public abstract class Connection extends TGraphicalObject
{
	private JTextField alias;
	private JButton connect;
	private JLabel message;
	private JPanel innerPane;
	private Internet internet;
	private String serverName;
	private java.util.List<String> commands = new Vector<String>();


	/**
	 * Creates a new instance of connection
	 */
	@Localize(value="Connection")
	public Connection()
	{
		super();
		this.setSize(180, 90);
		this.setLayout(new BorderLayout());
		this.add(new JLabel(getMessage("aliasText")),BorderLayout.NORTH);
		innerPane = new JPanel();
		innerPane.setBackground(DEFAULT_BACKGROUND);
		innerPane.setLayout(new BorderLayout());
		message = new JLabel(" ");
		innerPane.add(message,BorderLayout.SOUTH);
		
		alias = new JTextField();
		alias.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
	            if (evt.getKeyCode()==java.awt.event.KeyEvent.VK_ENTER)
	            {
	            	connect();
	            	evt.consume();
	            }
			}
            @Override
			public void keyReleased(java.awt.event.KeyEvent evt)
            {}
            @Override
			public void keyTyped(java.awt.event.KeyEvent evt)
            {}
		});
		innerPane.add(alias,BorderLayout.CENTER);
		this.add(innerPane,BorderLayout.CENTER);
		connect = new JButton(getMessage("connectText"));
		connect.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				connect();
			}
		});
		this.add(connect,BorderLayout.SOUTH);
        this.setBackground(DEFAULT_BACKGROUND);
        displayObject();
	}


	/**
	 * Connects the Internet object with the server.
	 * The attributes internet and server have to be specified before.
	 */
	private void connect()
	{
		if ((internet !=null)&&(serverName!=null))
		{
			if (alias.getText().trim().length()==0)
			{
				error(getMessage("error.aliasEmpty"));
				return;
			}
			info(getMessage("message.inProgress"));

			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run()
				{
					int result = internet.connectServer(serverName, alias.getText());
					switch(result)
					{
						case 0:
						case Internet.ERROR_USER_ALREADY_CONNECTED :
							info(getMessage("message.connected"));
							executeCommands();
							break;
						case Internet.ERROR_CONNECTION_FAILURE :
							error(getMessage("error.connectionFailure"));
							break;
						case Internet.ERROR_USER_ALREADY_EXISTS :
							error(getMessage("error.alreadyExists"));
							break;
						default :
							error(getMessage("error.unknown"));
							break;
					}
				}
			});
		}
		else
		{
			if (internet==null)
				Program.instance().printError(getMessage("error.noInternet"));
			if (serverName==null)
				Program.instance().printError(getMessage("error.noServer"));
		}
	}

	/**
	 * Sets the given text into the JLabel message, with a green background.
	 * @param text
	 */
	private void info(String text)
	{
		message.setForeground(Color.green);
		message.setText(text);
	}

	/**
	 * Sets the given text into the JLabel message, with a red background.
	 * @param text
	 */
	private void error(String text)
	{
		message.setForeground(Color.red);
		message.setText(text);
	}

	/**
	 * execute the commands of the command list.
	 */
    private void executeCommands()
    {
        for (String command:commands)
        {
            Program.instance().executeScript(command,getGraphicsPane());
        }
    }

    /**
     * Sets the background color.
     * @param colorName
     */
	@Localize(value="common.setColor")
    public void setColor(String colorName)
    {
		Color c = TColor.translateColor(colorName, Color.black);
        this.setBackground(c);

        innerPane.setBackground(c);
        repaint();
    }

	/**
	 * Sets the internet attribute.
	 * @param internet
	 */
	@Localize(value="common.setInternet")
    public void setInternet(Internet internet)
    {
		this.internet = internet;
    }

	/**
	 * Sets the serverName attribute.
	 * @param serverName
	 */
	@Localize(value="Connection.setServer")
	public void setServer(String serverName)
    {
		this.serverName = serverName;
    }
	
	/**
	 * Adds the given command to the command list.
	 * @param cmd
	 */
	@Localize(value="common.addCommand")
	public void addCommand(String cmd)
    {
		commands.add(cmd);
    }

	/**
	 * Clears the command list.
	 */
	@Localize(value="common.removeCommands")
	public void removeCommands()
    {
    	commands.clear();
    }

}
