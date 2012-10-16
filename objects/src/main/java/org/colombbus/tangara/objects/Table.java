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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TGraphicalObject;

/**
 * This class permits to have a text zone
 * @author Benoit
 *
 */
@SuppressWarnings("serial")
@Localize(value="Table",localizeParent=true)
public abstract class Table  extends TGraphicalObject {

	private java.util.List<String> commands = new Vector<String>();
    private JTable table;
    private CustomTableModel tableModel;


    /** Creates a new instance of Texte */
    @Localize(value="Table")
    public Table() {
        setSize(100,100);
        setOpaque(false);
        setLayout(new BorderLayout());
        tableModel = new CustomTableModel();
        table = new JTable(tableModel);
        table.setCellSelectionEnabled(false);
        JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(table);
		scroll.getViewport().setBackground(Color.white);
		scroll.setSize(new Dimension(getObjectWidth(),getObjectHeight()));
        add(scroll, BorderLayout.CENTER);
	   	displayObject();
	   	
    }

    @Localize(value="Table")
    public Table(int cols, int rows) {
        this();
        setColumnNumber(cols);
        setRowNumber(rows);
    }

    @Localize(value="Table")
    public Table(int cols) {
        this();
        setColumnNumber(cols);
    }
    
    @Localize(value="Table.setColumnNumber")
    public void setColumnNumber(int number) {
    	tableModel.setColumnCount(number);
    }

    @Localize(value="Table.setRowNumber")
    public void setRowNumber(int number) {
    	tableModel.setRowCount(number);
    }

    @Localize(value="Table.addColumn")
    public void addColumn() {
    	tableModel.addColumn(null);
    }
    
    @Localize(value="Table.addColumn2")
    public void addColumn(String title) {
    	tableModel.addColumn(title);
    }
    
    @Localize(value="Table.setColumnTitle")
    public void setColumnTitle(int index, String title) {
    	if (index<1 || index>tableModel.getColumnCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.columnOutOfBounds"), index)); //$NON-NLS-1$
    	} else {
    		tableModel.setColumnIdentifier(index-1, title);
    	}
    }
    
    @Localize(value="Table.setColumnTitles")
    public void setColumnTitles(org.colombbus.tangara.objects.List<String> titles) {
    	Vector<String> cells = new Vector<String>();
    	for(String title:titles) {
    		cells.add(title);
    	}
    	tableModel.setColumnIdentifiers(cells);
    }
    
    @Localize(value="Table.addRow")
    public void addRow() {
		Object[] emptyData = null;
    	tableModel.addRow(emptyData);
    }
    
    @Localize(value="Table.addRow2")
    public void addRow(org.colombbus.tangara.objects.List<String> texts) {
    	Vector<String> cells = new Vector<String>();
    	for(String text:texts) {
    		cells.add(text);
    	}
    	tableModel.addRow(cells);
    }
    
    @Localize(value="Table.removeRow")
    public void removeRow(int index) {
    	if (index<1 || index>tableModel.getRowCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.rowOutOfBounds"), index)); //$NON-NLS-1$
    	} else {
    		tableModel.removeRow(index-1);
    	}
    }
    
    @Localize(value="Table.removeColumn")
    public void removeColumn(int index) {
    	if (index<1 || index>tableModel.getColumnCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.columnOutOfBounds"), index)); //$NON-NLS-1$
    	} else {
    		tableModel.removeColumn(index-1);
    	}
    }

    @Localize(value="Table.insertColumn")
    public void insertColumn(int index) {
    	insertColumn(index, null);
    }

    @Localize(value="Table.insertColumn2")
    public void insertColumn(int index, String title) {
    	if (index<1 || index>tableModel.getColumnCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.columnOutOfBounds"), index)); //$NON-NLS-1$
    	} else {
    		tableModel.insertColumn(index-1, title);
		}    	
    }
    
    @Localize(value="Table.insertRow")
    public void insertRow(int index) {
    	insertRow(index, null);
    }

    @Localize(value="Table.insertRow2")
    public void insertRow(int index, org.colombbus.tangara.objects.List<String> texts) {
    	if (index<1 || index>tableModel.getRowCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.rowOutOfBounds"), index)); //$NON-NLS-1$
    	} else {
    		Vector<String> cells = null;
    		if (texts!=null) {
	        	cells = new Vector<String>();
	        	for(String text:texts) {
	        		cells.add(text);
	        	}
    		}
    		tableModel.insertRow(index-1, cells);
    	}
    }
    
    @Localize(value="Table.setText")
    public void setText(int rowIndex, int columnIndex, String value) {
    	if (rowIndex<1 || rowIndex>tableModel.getRowCount()) {
    		String message =getMessage("error.rowOutOfBounds"); 
    		LOG.debug(message);
    		LOG.debug(MessageFormat.format(message, "test"));
    		LOG.debug(MessageFormat.format(message, 123));
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.rowOutOfBounds"), rowIndex));
    	} else if (columnIndex<1 || columnIndex>tableModel.getColumnCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.columnOutOfBounds"), columnIndex)); //$NON-NLS-1$
    	} else {
    		tableModel.setValueAt(value, rowIndex-1, columnIndex-1);
    	}
    }

    @Localize(value="Table.setRow")
    public void setRow(int rowIndex, org.colombbus.tangara.objects.List<String> texts) {
    	if (rowIndex<1 || rowIndex>tableModel.getRowCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.rowOutOfBounds"), rowIndex));
    	} else {
    		int columnIndex = 0;
        	for(String text:texts) {
        		tableModel.setValueAt(text, rowIndex-1, columnIndex);
        		columnIndex++;
    		}
    	}
    }
    
    @Localize(value="Table.getText")
    public String getText(int rowIndex, int columnIndex) {
    	if (rowIndex<1 || rowIndex>tableModel.getRowCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.rowOutOfBounds"), rowIndex));
    	} else if (columnIndex<1 || columnIndex>tableModel.getColumnCount()) {
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.columnOutOfBounds"), columnIndex)); //$NON-NLS-1$
    	} else {
    		return (String)tableModel.getValueAt(rowIndex-1, columnIndex-1);
    	}
    	return "";
    }
    
    @Localize(value="Table.getRowCount")
    public int getRowCount() {
    	return tableModel.getRowCount();
    }

    @Localize(value="Table.getColumnCount")
    public int getColumnCount() {
    	return tableModel.getColumnCount();
    }
    
    @Localize(value="Table.displayGrid")
    public void displayGrid(boolean value) {
    	table.setShowGrid(value);
    }
    
    @Localize(value="Table.displayHeader")
    public void displayHeader(boolean value) {
    	table.getTableHeader().setVisible(value);
    	table.getTableHeader().setPreferredSize(value ? null : new Dimension(-1, 0));
    }

    /**
     * Sets the color of the text.
     * @param colorName
     */
    @Localize(value="common.setColor")
    public void setColor(String colorName) {
		Color c = TColor.translateColor(colorName, Color.black);
		table.setForeground(c);
   }

    
    /**
     * Sets the color of the borders.
     * @param colorName
     */
    @Localize(value="Table.setGridColor")
    public void setGridColor(String colorName) {
		Color c = TColor.translateColor(colorName, Color.black);
		table.setGridColor(c);
   }
    
    /**
     * Sets the size of the text.
     * @param sizeValue
     */
    @Localize(value="Table.setTextSize")
    public void setTextSize(int value) {
        Font currentFont = table.getFont();
        table.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), value));
        FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(fontMetrics.getHeight());
    }

    class CustomTableModel extends DefaultTableModel {
        
    	public void removeColumn(int column) {
            columnIdentifiers.remove(column);
            for (Object row: dataVector) {
                ((Vector)row).remove(column);
            }
            fireTableStructureChanged();
        }
    	
    	public void insertColumn(int column, Object columnIdentifier) {
    		columnIdentifiers.insertElementAt(columnIdentifier, column);
            for (Object row: dataVector) {
                ((Vector)row).insertElementAt(null, column);
            }
            fireTableStructureChanged();
    	}
    	
    	public void setColumnIdentifier(int column, Object columnIdentifier) {
    		columnIdentifiers.setElementAt(columnIdentifier, column);
    		fireTableStructureChanged();
    	}
    	
    	public boolean isCellEditable(int rowIndex, int columnIndex) {
    		return false;
    	}
    	
    }

    
}
