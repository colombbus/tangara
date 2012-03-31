package org.colombbus.tangara.objects;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TGraphicalObject;

@SuppressWarnings("serial")
@Localize(value="InputArea",localizeParent=true)
public class InputArea extends TGraphicalObject
{
	private JTextField textArea; 
	private java.util.List<String> commandList = new Vector<String>();

	public InputArea()
	{
		textArea = new JTextField();
		textArea.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent evt)
			{
	            SwingUtilities.invokeLater(new Runnable(){
	            	@Override
					public void run()
	            	{
	                	executeCommands();
	            	}
	            });
			}
		});
		this.setLayout(new BorderLayout());
		this.add(textArea, BorderLayout.CENTER);
		setObjectWidth(50);
		setObjectHeight(20);
        displayObject();
	}

    @Localize(value="InputArea.setText")
	public void setText(String text)
	{
		textArea.setText(text);
	}
	
    @Localize(value="InputArea.getText")
	public String getText()
	{
    	return textArea.getText();
	}
    
	@Localize(value="common.addCommand")
	public void addCommand(String cmd)
    {
		commandList.add(cmd);
    }
	
	@Localize(value="common.removeCommands")
	public void removeCommands()
    {
    	commandList.clear();
    }
	
	private void executeCommands()
	{
        for (String command : commandList)
        {
            Program.instance().executeScript(command,getGraphicsPane());
        }
	}
}
