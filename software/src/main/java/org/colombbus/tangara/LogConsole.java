package org.colombbus.tangara;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class LogConsole extends JList implements ListDataListener, Editable {
	
	/** The format for the output messages */
	public static SimpleAttributeSet outMsgTextAtt = new SimpleAttributeSet();
	/** The format for the error messages */
	public static SimpleAttributeSet errorMsgTextAtt = new SimpleAttributeSet();
	/** The format for the code messages */
	public static SimpleAttributeSet codeMsgTextAtt = new SimpleAttributeSet();

	public static final int STYLE_MESSAGE = 1; 
	public static final int STYLE_ERROR = 2;
	public static final int STYLE_CODE = 3;
	
	private boolean selecting = false;
	private int previousIndex = -1;
	private boolean dragging = false;
	private Point selectionPoint;
	private int selectionIndex;
	private boolean dragAndDropEnabled = false;
	
	private LogConsoleModel model = null;
	private EditorFrame frame = null;
	private JPopupMenu popup;
	
	
	private static Logger LOG = Logger.getLogger(LogConsole.class);
	
	static {
		/**
		 * Sets the formats for output, error and code messages
		 *
		 */
		// output messages : blue
		StyleConstants.setForeground(outMsgTextAtt, Color.blue);
		StyleConstants.setBackground(outMsgTextAtt, new Color(200,200,200));
        StyleConstants.setFontFamily(outMsgTextAtt, "Lucida Grande"); //$NON-NLS-1$
		StyleConstants.setFontSize(outMsgTextAtt, 13);
		StyleConstants.setBold(outMsgTextAtt, false);
		// error messages : red
		StyleConstants.setForeground(errorMsgTextAtt, Color.red);
		StyleConstants.setBackground(errorMsgTextAtt, new Color(200,200,200));
        StyleConstants.setFontFamily(errorMsgTextAtt, "Lucida Grande"); //$NON-NLS-1$
		StyleConstants.setFontSize(errorMsgTextAtt, 13);
		StyleConstants.setBold(errorMsgTextAtt, false);
		// code messages : green
		StyleConstants.setForeground(codeMsgTextAtt, new Color(0, 153, 0));
		StyleConstants.setBackground(codeMsgTextAtt, new Color(200,200,200));
        StyleConstants.setFontFamily(codeMsgTextAtt, "Lucida Grande"); //$NON-NLS-1$
		StyleConstants.setFontSize(codeMsgTextAtt, 13);
		StyleConstants.setBold(codeMsgTextAtt, false);
	}
	
	public LogConsole(EditorFrame frame) {
		this.frame = frame;
		setBackground(new Color(200,200,200));
		
		setFixedCellHeight(18);

		model = new LogConsoleModel();
		model.addListDataListener(this);
		setModel(model);
		
		//Set up renderer.
		setCellRenderer(new LogConsoleLine());
		
		this.setTransferHandler(frame.getCommandHandler());
		
		MouseListener[] mouseListeners = getMouseListeners();
		MouseMotionListener[] motionListeners = getMouseMotionListeners();
		for (MouseListener ml:mouseListeners)
			removeMouseListener(ml);
		for (MouseMotionListener mml:motionListeners)
			removeMouseMotionListener(mml);
        
		addMouseListener(new MouseAdapter(){
			
         	@Override
			public void mousePressed(MouseEvent e)
         	{
         		selectionPoint = e.getLocationOnScreen();
         		selectionIndex = locationToIndex(e.getPoint());
         		dragging = false;
         		int index = locationToIndex(e.getPoint());
         		if (index > -1) {
	         		// 1st test if line is code
	         		if (model.isCode(index)) {
	         			// 2nd test if line was previously selected 
	         			if  (isSelectedIndex(index)) {
	         				// Row was selected
	         				selecting = false;
	         			} else {
	         				// Row was not selected: we add the index to the selection and start selection
	         				addSelectionInterval(index, index);
	         				selecting = true;
	         			}
	         		} else {
	         			// line is not code: we start selecting anyway
	     				selecting = true;
	         		}
	     			previousIndex = index;
         		}
         		// Get the keyboard focus
         		LogConsole.this.requestFocus();
         	}

         	@Override
			public void mouseReleased(MouseEvent e)
         	{
         		int index = locationToIndex(e.getPoint());
         		if (!selecting && !dragging) {
         			// no selection and no dragging: we just deselect the row
         			if (index == previousIndex)
         				removeSelectionInterval(index, index);
         		}
         		previousIndex = -1;
         		setCursor(null);
         		setDragEnabled(false);
         		dragging = false;
         		selecting = false;
         	}
         	
         	@Override
			public void mouseClicked(MouseEvent e)
         	{
         		if (e.getClickCount()>1) {
         			// double click occured
         			
	         		// get the clicked element
	         		selectionPoint = e.getLocationOnScreen();
	         		selectionIndex = locationToIndex(e.getPoint());
	         		dragging = false;
	         		int index = locationToIndex(e.getPoint());
	         		if (index > -1) {
		         		if (model.isError(index)) {
		         			LogElement errorElement = (LogElement)model.getElementAt(index);
		         			if (errorElement.isLinkable()) {
			         			int errorLine = errorElement.getLineNumber();
			         			int programIndex = errorElement.getProgramIndex();
		         				LogConsole.this.frame.selectLine(programIndex, errorLine);
		         			}
		         		}
	         		}
         		}
         	}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (selecting) {
					// We are selecting items
					int index = locationToIndex(e.getPoint());
					if(index > -1) {
						// 1st scroll to make item visible
						scrollRectToVisible(getCellBounds(index,index));
						// 2nd select new indexes
						if (index != previousIndex) {
							if (index>previousIndex) {
								addSelectionInterval(previousIndex+1, index);
							} else {
								addSelectionInterval(index, previousIndex-1);
							}
							previousIndex = index;
						}
					}
				} else if (!dragging ){
					// We are not selecting items : we may be dragging them
					Rectangle r = new Rectangle(selectionPoint.x-10,selectionPoint.y-10,20,20);
					if(!r.contains(e.getLocationOnScreen()) || locationToIndex(e.getPoint()) != selectionIndex) {
						dragging = true;
						if (dragAndDropEnabled) {
							try {
								getTransferHandler().exportAsDrag(LogConsole.this, e, TransferHandler.COPY);
							} catch (Exception e2) {
								LOG.error("Error while exporting commands",e2); //$NON-NLS-1$
							}
						}
					}
				}
			}
		});
		
		this.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (isCodeSelected())
					LogConsole.this.frame.enableEdit();
				else
					LogConsole.this.frame.disableEdit();
			}
		});
		
        popup = new JPopupMenu();
        popup.add(frame.getCopyAction());
        this.setComponentPopupMenu(popup);
        
        this.addFocusListener(new FocusAdapter() {
        	@Override
			public void focusGained(FocusEvent e) {
        		LogConsole.this.frame.setFocusOwner(LogConsole.this);
        	}
        });
    }
	
	public void enableDragAndDrop() {
		dragAndDropEnabled = true;
	}

	public void disableDragAndDrop() {
		dragAndDropEnabled = false;
	}
	
	public boolean isCodeSelected() {
		int[] indexes = getSelectedIndices();
		for (int index:indexes) {
			if (model.isCode(index))
				return true;
		}
		return false;
	}
	
	public static SimpleAttributeSet getAttributes(int type) {
		switch (type) {
			case STYLE_CODE :
				return codeMsgTextAtt;
			case STYLE_ERROR : 
				return errorMsgTextAtt;
			case STYLE_MESSAGE :
				return outMsgTextAtt;
		}
		return outMsgTextAtt;
	}
	
    public void insertCodeToProgram()
    {
    	String insertedCode = ""; //$NON-NLS-1$
    	int[] indexes = getSelectedIndices();
    	Arrays.sort(indexes);
    	for (int index:indexes) {
			LogElement element = (LogElement)model.getElementAt(index);
			if (element.isCode()) {
				String codeLine = element.getText();
				if (!codeLine.endsWith(";")) //$NON-NLS-1$
					codeLine+=";"; //$NON-NLS-1$
				insertedCode+=codeLine+"\n"; //$NON-NLS-1$
			}
    	}
    	if (insertedCode.length()>0) {
    		frame.insertCommands(insertedCode);
    	}
    }
    
    public void log(String text, int style, int lineNumber, int programIndex) {
    	model.addLine(new LogElement(text, style, lineNumber, programIndex));
    }
    
    public void setErrors(int index, int number, int errorLineNumber, int programIndex) {
    	model.setErrors(index, number, errorLineNumber, programIndex);
    }
    
    public void clear() {
    	model.clear();
    }

    public int getCurrentIndex() {
    	return model.getSize();
    }
    
	@Override
	public void intervalAdded(ListDataEvent e) {
		// scroll to the last line inserted, if console is not currently used
		if (!this.hasFocus())
			scrollRectToVisible(getCellBounds(e.getIndex1(),e.getIndex1()));
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		
	}
	
	public void selectAll() {
		if (model.getSize()>0)
			setSelectionInterval(0,model.getSize()-1);
	}
	
	public ArrayList<String> getSelectedCode() {
		ArrayList<String> result = new ArrayList<String>();
		int[] indexes = getSelectedIndices();
    	Arrays.sort(indexes);
		for (int index:indexes) {
			LogElement element = (LogElement)model.getElementAt(index);
			//if(!element.getText().endsWith(";")) element.setText(element.getText() + ";"); //$NON-NLS-1$ //$NON-NLS-2$
			if (element.isCode())
				result.add(element.getText());
		}
		return result;
	}
	
	@Override
	public void copy() {
		TransferHandler.getCopyAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
	}

	@Override
	public void cut() {
		// do nothing
	}

	@Override
	public void paste() {
		// do nothing
	}
	
	@Override
	public boolean mayCopy() {
		return isCodeSelected();
	}
	
}
