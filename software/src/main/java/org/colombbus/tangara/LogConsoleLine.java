package org.colombbus.tangara;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;

@SuppressWarnings("serial")
public class LogConsoleLine extends JLabel implements ListCellRenderer {

	private final Color selectedBackgroudColor = new Color(170,170,250);


    public LogConsoleLine() {
        setOpaque(true);
    }

	@Override
	public Component getListCellRendererComponent(JList list, Object line,
			int index, boolean isSelected, boolean cellHasFocus) {
    	LogElement element = (LogElement)line;
    	AttributeSet attributes = element.getAttributes();
    	Font font = new Font(StyleConstants.getFontFamily(attributes), Font.PLAIN, StyleConstants.getFontSize(attributes));
    	if (StyleConstants.isBold(attributes))
    		font = font.deriveFont(Font.BOLD);
    	if (StyleConstants.isItalic(attributes))
    		font = font.deriveFont(Font.ITALIC);
    	setFont(font);
        setBackground(StyleConstants.getBackground(attributes));
        setForeground(StyleConstants.getForeground(attributes));
        String text = element.getText();
        // management of tabs
        text = text.replace("\t", "    ");
        // management of CR (empty lines)
        //text = text.replace("\n", "[CR]");
        setText(text);
        if ((isSelected)&&(element.isSelecteable())) {
    		setBackground(selectedBackgroudColor);
        }
        return this;
	}
}
