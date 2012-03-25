package org.colombbus.tangara;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.buffer.JEditBuffer;

@SuppressWarnings("serial")
public class TextPaneManager extends JTabbedPane {

    private final IndicatorLine glassPane = new IndicatorLine();
    private final Rectangle lineRect  = new Rectangle();
    private int dragTabIndex = -1;
    private EditorFrame parentFrame;
    private int newIndex = 1;
    protected JTextField searchText = null;
    protected JCheckBox searchCaseSensitive = null;
	private Box searchPane = null;
    private AbstractAction searchAction = null;
    private AbstractAction searchNextAction = null;
    private AbstractAction searchPreviousAction = null;
    private AbstractAction cancelSearchAction = null;
	private boolean displayLineNumbers = false;

	/** Class logger */
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(TextPaneManager.class);



	//The information on all the panes are in this Vector.
	private Vector<TextPaneTab> panes = new Vector<TextPaneTab>();

    public TextPaneManager(EditorFrame parent) {
        super();
        this.parentFrame = parent;
        this.setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
        initTabsDragAndDrop();
		addChangeListener(new ChangeListener() {
            @Override
			public void stateChanged(ChangeEvent e) {
            	File directory = getCurrentDirectory();
            	if (directory != null)
            		Program.instance().setCurrentDirectory(directory);
                SwingUtilities.invokeLater(new Runnable() {
        			@Override
					public void run() {
                    	getCurrentPane().requestFocusInWindow();
                	}
                });
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
        	@Override
			public void run() {
        		initTabs();
        	}
        });
    }

    private void initTabs() {
        // Add the "new" tab
		JPanel newTab = new JPanel();
		addTab("", newTab); //$NON-NLS-1$
		setTabComponentAt(0, new NewPaneButton());
		setEnabledAt(0, false);
		// Add a new pane
		addPane();
    }

    private void initTabsDragAndDrop() {

        final DragSourceAdapter dsl = new DragSourceAdapter() {
            @Override
			public void dragOver(DragSourceDragEvent e) {
                Point glassPt = e.getLocation();
                SwingUtilities.convertPointFromScreen(glassPt, glassPane);
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                glassPane.setCursor(DragSource.DefaultMoveDrop);
            }
            @Override
			public void dragDropEnd(DragSourceDropEvent e) {
                lineRect.setRect(0,0,0,0);
                dragTabIndex = -1;
                glassPane.setVisible(false);
            }
        };

        final Transferable t = new Transferable() {
            private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "tab"); //$NON-NLS-1$

            @Override
			public Object getTransferData(DataFlavor flavor) {
                return TextPaneManager.this;
            }

            @Override
			public DataFlavor[] getTransferDataFlavors() {
                DataFlavor[] f = new DataFlavor[1];
                f[0] = this.FLAVOR;
                return f;
            }

            @Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.getHumanPresentableName().equals("tab"); //$NON-NLS-1$
            }
        };


        final DragGestureListener dgl = new DragGestureListener() {

        	@Override
			public void dragGestureRecognized(DragGestureEvent e) {
                if(getTabCount()<=1) return;
                Point tabPt = e.getDragOrigin();
                dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
                //"disabled tab problem".
                if(dragTabIndex<0 || !isEnabledAt(dragTabIndex)) return;
                initGlassPane();
                try {
                    e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
                }
                catch(InvalidDnDOperationException idoe) {
                    idoe.printStackTrace();
                }
            }
        };
        new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
    }


    @Override
	public void setSelectedIndex(int index) {
    	super.setSelectedIndex(index);
    	File directory = getDirectory(index);
    	if (directory != null)
    		Program.instance().setCurrentDirectory(directory);
    }

	public void closePane(int index) {
		if(checkProgramChanged(index)) {
			if(paneCount() == 1)
				addPane();
			removePane(index);
			if(index == paneCount()) {
				setSelectedIndex(index-1);
			}
		}
	}

	public void closeCurrentPane() {
		closePane(getSelectedIndex());
	}

    public boolean isPaneModified(int index) {
		try {
			return panes.get(index).isModified();
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
    }

    public boolean isPaneFree(int index) {
		try {
			return panes.get(index).isFree();
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
    }

    public void setPaneModified(int index, boolean value) {
    	TextPaneTab tab = panes.get(index);
    	tab.setModified(value);
    }

    public void setCurrentPaneModified(boolean value) {
    	setPaneModified(getSelectedIndex(), value);
    }

    public int getPanesCount() {
    	return panes.size();
    }

	public void removePane(int index) {
		super.remove(index);
		panes.remove(index);
		// Fire state changed to update selected directory
		this.fireStateChanged();
	}

	public int getFileIndex(File file) {
		for (int i=0;i<getPanesCount();i++) {
			if (file.equals(getFile(i))) {
				return i;
			}
		}
		return -1;
	}

	public void addPane(File file, String contents) {
		String title;
		if (file.isDirectory()) {
			// new File
			title = Messages.getString("EditorFrame.file.newFile")+newIndex; //$NON-NLS-1$
			newIndex++;
		} else {
			title = file.getName();
		}

		if ((contents != null) && isPaneFree(getSelectedIndex())) {
			modifyTab(getSelectedIndex(), title, file, contents);
		} else {
	    	addTab(title, file, contents);
			setSelectedIndex(paneCount()-1);
		}
	}

	public void addPane(File file) {
		addPane(file, null);
	}

	public void addPane() {
		addPane(Program.instance().getCurrentDirectory());
	}

	public int paneCount() {
		return panes.size();
	}

	public TextPane getPane(int index) {
		try {
			return panes.get(index).getTextPane();
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public File getDirectory(int index) {
		try {
			return panes.get(index).getDirectory();
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public File getFile(int index) {
		try {
			File file =panes.get(index).getFile();
			if (file.isDirectory())
				return null;
			else
				return file;
		} catch (Exception e) {
				return null;
			}
	}

	/**
	 * Checks whether the specified programme has changed or not
	 *
	 * @return boolean
	 */
	private boolean checkProgramChanged(int i)
	{
		Boolean bool = true;
		if (isPaneModified(i)) {
			parentFrame.showProgram();
			setSelectedIndex(i);
			String message = Messages
				.getString("EditorFrame.tab.discardChanges.confirm"); //$NON-NLS-1$
			String title = Messages
				.getString("EditorFrame.program.discardChanges.confirmTitle"); //$NON-NLS-1$
			Object[] options = { Messages.getString("tangara.yes"), //$NON-NLS-1$
				Messages.getString("tangara.cancel") }; //$NON-NLS-1$
			int answer = JOptionPane.showOptionDialog(this, message, title,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null, // do not use a custom Icon
				options, // the titles of buttons
				options[0]);
			if (answer != JOptionPane.OK_OPTION)
				bool = false;
		}
		return bool;
	}

	/**
	 * Checks whether one of the programs at least has changed or not
	 *
	 * @return boolean
	 */
	public boolean checkProgramChanged(){
		for(int i = 0; i < paneCount(); i++) {
			if (!checkProgramChanged(i))
				return false;
		}
		return true;
	}

	public TextPane getCurrentPane() {
		return getPane(getSelectedIndex());
	}

	public File getCurrentDirectory() {
		return getDirectory(getSelectedIndex());
	}

	public File getCurrentFile() {
		return getFile(getSelectedIndex());
	}

	public String getContents(int index) {
		return getPane(index).getText();
	}

	public String getCurrentContents() {
		return getContents(getSelectedIndex());
	}

	public void setCurrentFile(File file) {
		String title;
		if (file.isDirectory()) {
			// new File
			title = Messages.getString("EditorFrame.file.newFile")+newIndex; //$NON-NLS-1$
			newIndex++;
		} else {
			title = file.getName();
		}
		modifyTab(getSelectedIndex(), title, file, null);
	}

	public void clearPane(int index) {
		TextPane tp = getPane(index);
		tp.getBuffer().remove(0, tp.getBufferLength());
	}

	@Override
	public void requestFocus() {
		getCurrentPane().requestFocusInWindow();
	}

    public void displaySearch() {
    	JPanel currentPane = (JPanel) getSelectedComponent();
    	currentPane.add(BorderLayout.SOUTH,getSearchPane());
    	currentPane.add(BorderLayout.SOUTH,getSearchPane());
    	currentPane.revalidate();
        searchText.requestFocus();
        if (searchText.getText().length()>0) {
            searchText.selectAll();
        }
    }

    public void hideSearch() {
    	JPanel currentPane = (JPanel) getSelectedComponent();
    	currentPane.remove(getSearchPane());
    	currentPane.revalidate();
    	requestFocus();
    }

    public void performSearch(boolean way) {
    	TextPane tp = getCurrentPane();
        if (!tp.search(searchText.getText(), way, searchCaseSensitive.isSelected())) {
            searchText.setBackground(Color.RED);
        } else {
            searchText.setBackground(Color.WHITE);
        }
    }

	private void addTab(String title, File file, String contents) {
    	// 1st create a new TextPane
    	TextPane newTextPane = new TextPane(parentFrame,TextPane.getProperties(false), false);

    	// 2nd create the tab
    	StyledTab tab = new StyledTab(title);
    	TextPaneTab paneTab = new TextPaneTab(file, newTextPane, tab);

    	// 3rd add contents if any
    	if (contents != null) {
    		newTextPane.getBuffer().insert(0, contents);
    		newTextPane.setCaretPosition(0);
    		paneTab.setModified(false);
    	}

    	// 4th add new TextPaneTab
    	panes.add(paneTab);

    	// 5th create the pane and add it to manager
    	JPanel pane = new JPanel();
    	pane.setLayout(new BorderLayout());
		JPanel textPaneContainer = new JPanel();
		textPaneContainer.setBorder(BorderFactory.createLineBorder(new Color(102, 102, 102), 1));
		textPaneContainer.setLayout(new BorderLayout());
		textPaneContainer.add(newTextPane, BorderLayout.CENTER);
    	pane.add(textPaneContainer, BorderLayout.CENTER);
    	pane.add(makePrgButtonPanel(), BorderLayout.EAST);
    	int index = getTabCount()-1;
    	insertTab(title, null, pane, null, index);
    	setTabComponentAt(index,tab);
    	if (!file.isDirectory())
    		setToolTipTextAt(index, file.getAbsolutePath());
    }

    private void modifyTab(int index, String title, File file, String contents) {
    	TextPaneTab paneTab = panes.get(index);
    	TextPane tp = paneTab.getTextPane();
    	if (contents != null) {
    		tp.getBuffer().insert(0, contents);
    		tp.setCaretPosition(0);
    	}
    	paneTab.setFile(file);
    	StyledTab tab = paneTab.getTab();
    	tab.setTitle(title);
    	paneTab.setModified(false);
    	paneTab.setFree(false);
    	if (!file.isDirectory())
    		setToolTipTextAt(index, file.getAbsolutePath());
    }

	/**
	 * This method creates and returns a JPanel for a PrgRunButton
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel makePrgButtonPanel() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.ipady = 0;
		gridBagConstraints2.gridheight = 1;
		gridBagConstraints2.gridwidth = 1;
		gridBagConstraints2.gridx = 0;
		JPanel tprgButtonPanel = new JPanel();
		tprgButtonPanel.setLayout(new GridBagLayout());
		tprgButtonPanel.setBorder(BorderFactory.createLineBorder(new Color(102, 102, 102), 1));
		tprgButtonPanel.setBackground(new Color(250, 250, 250));
		tprgButtonPanel.setPreferredSize(new Dimension(150, 140));
		tprgButtonPanel.add(makePrgRunButton(), gridBagConstraints2);
		return tprgButtonPanel;
	}

	/**
	 * This method creates and returns a button
	 *
	 * @return javax.swing.JButton
	 */
	private JButton makePrgRunButton() {
		JButton tprgRunButton = new JButton();
		tprgRunButton.setBackground(new Color(250, 250, 250));
		tprgRunButton.setForeground(new Color(51, 51, 51));
		tprgRunButton.setIcon(new ImageIcon(getClass().getResource("/org/colombbus/tangara/control_play_blue.png"))); //$NON-NLS-1$
		tprgRunButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13)); //$NON-NLS-1$
		tprgRunButton.setPreferredSize(new Dimension(120, 30));
		tprgRunButton.setText(Messages.getString("EditorFrame.button.execute")); //$NON-NLS-1$
		tprgRunButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					parentFrame.executeProgram(getCurrentPane().getText(), getSelectedIndex());
				}
			});
		return tprgRunButton;
	}

    private int getTargetTabIndex(Point glassPt)
    {
        Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, TextPaneManager.this);
        for(int i=0;i<getTabCount()-1;i++) {
            Rectangle r = getBoundsAt(i);
            r.setRect(r.x-r.width/2, r.y-200,  r.width, r.height+400);
            if(r.contains(tabPt)) return i;
        }
        Rectangle r = getBoundsAt(getTabCount()-2);
        r.setRect(r.x+r.width/2, r.y-200,  3000, r.height+400);
        return   r.contains(tabPt)?getTabCount()-1:-1;
    }

    private void convertTab(int oldIndex, int newIndex) {
        if(newIndex<0 || oldIndex==newIndex || getPanesCount()==1)
        {
            return;
        }
        if(newIndex>1 && !isEnabledAt(newIndex-1))
        {
        	return;
        }
        Component tComponent = getComponentAt(oldIndex);
        Component tTabComponent = getTabComponentAt(oldIndex);
        String tTitle = getTitleAt(oldIndex);
        String tToolTipText = getToolTipTextAt(oldIndex);
        TextPaneTab tTextPaneTab = panes.get(oldIndex);

        newIndex  = oldIndex > newIndex ? newIndex : newIndex-1;

        setSelectedIndex(0);

        panes.remove(oldIndex);
        remove(oldIndex);

        panes.insertElementAt(tTextPaneTab, newIndex);
        insertTab(tTitle, null, tComponent, tToolTipText, newIndex);
        setTabComponentAt(newIndex, tTabComponent);

        setSelectedIndex(newIndex);
    }

    private void initTargetLeftRightLine(int next)
    {
        if(next<0 || dragTabIndex==next || next-dragTabIndex==1)
        {
            lineRect.setRect(0,0,0,0);
        }
        else if(next==0)
        {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
            lineRect.setRect(r.x-3/2,r.y,3,r.height);
        }
        else
        {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next-1), glassPane);
            lineRect.setRect(r.x+r.width-3/2,r.y,3,r.height);
        }
    }

    private void initGlassPane()
    {
        getRootPane().setGlassPane(glassPane);
        glassPane.setVisible(true);
    }

    private Box getSearchPane() {
        if (searchPane == null) {
            searchPane = Box.createHorizontalBox();
            searchPane.add(new JLabel(Messages.getString("EditorFrame.button.search"))); //$NON-NLS-1$
            searchPane.add(Box.createHorizontalStrut(5));
            searchText = new JTextField();
            searchText.addKeyListener(new KeyAdapter() {
                @Override
				public void keyPressed(KeyEvent evt) {
                    searchText.setBackground(Color.WHITE);
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_ENTER:
                            performSearch(true);
                            break;
                        case KeyEvent.VK_ESCAPE:
                            hideSearch();
                            break;
                    }
                }
            });
            Dimension size = new Dimension(200,searchText.getPreferredSize().height);
            searchText.setPreferredSize(size);
            searchText.setMaximumSize(size);
            searchPane.add(searchText);
            searchPane.add(Box.createHorizontalStrut(10));
            searchCaseSensitive = new JCheckBox();
            searchCaseSensitive.setText(Messages.getString("EditorFrame.button.search.caseSensitive")); //$NON-NLS-1$
            searchPane.add(searchCaseSensitive);
            searchPane.add(Box.createHorizontalStrut(10));
            JButton bNext = new JButton(getSearchNextAction());
            searchPane.add(bNext);
            searchPane.add(Box.createHorizontalStrut(10));
            JButton bPrevious = new JButton(getSearchPreviousAction());
            searchPane.add(bPrevious);
            searchPane.add(Box.createHorizontalStrut(10));
            JButton bCancel = new JButton(getCancelSearchAction());
            searchPane.add(bCancel);
            searchPane.add(Box.createHorizontalGlue());
            searchPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
        return searchPane;
    }

    public Action getCancelSearchAction() {
        if (cancelSearchAction == null) {
            cancelSearchAction = new AbstractAction(Messages.getString("EditorFrame.button.search.cancel"), new ImageIcon(getClass().getResource( //$NON-NLS-1$
            "/org/colombbus/tangara/cancel.png"))) { //$NON-NLS-1$
                @Override
				public void actionPerformed(ActionEvent e) {
                    hideSearch();
                }
            };
        }
        return cancelSearchAction;
    }

    public Action getSearchAction() {
        if (searchAction == null) {
            searchAction = new AbstractAction(Messages.getString("Banner.menu.search")) { //$NON-NLS-1$
                @Override
				public void actionPerformed(ActionEvent e) {
                    displaySearch();
                }
            };
            searchAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));
        }
        return searchAction;
    }

    public Action getSearchNextAction() {
        if (searchNextAction == null) {
            searchNextAction = new AbstractAction(Messages.getString("EditorFrame.button.search.next"), new ImageIcon(getClass().getResource( //$NON-NLS-1$
            "/org/colombbus/tangara/arrow_down.png"))) { //$NON-NLS-1$
                @Override
				public void actionPerformed(ActionEvent e) {
                    performSearch(true);
                }
            };
        }
        return searchNextAction;
    }

    public Action getSearchPreviousAction() {
        if (searchPreviousAction == null) {
            searchPreviousAction = new AbstractAction(Messages.getString("EditorFrame.button.search.previous"), new ImageIcon(getClass().getResource( //$NON-NLS-1$
            "/org/colombbus/tangara/arrow_up.png"))) { //$NON-NLS-1$
                @Override
				public void actionPerformed(ActionEvent e) {
                    performSearch(false);
                }
            };
        }
        return searchPreviousAction;
    }

    private class IndicatorLine extends JPanel
    {
		private final AlphaComposite IndicatorLine;
        public IndicatorLine()
        {
            setOpaque(false);
            IndicatorLine = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        }
        @Override
		public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(IndicatorLine);
            g2.setPaint(Color.BLUE);
            g2.fill(lineRect);
        }
    }



	private class TextPaneTab {
		private File file;
		private TextPane textPane;
		private StyledTab tab;
		private boolean isFree = true;
		private boolean isModified = false;

		TextPaneTab(File file, TextPane textPane, StyledTab tab) {
			this.file = file;
			this.textPane = textPane;
			this.tab = tab;
			setModified(false);
	        textPane.getBuffer().addBufferListener(new BufferAdapter() {
				@Override
				public void contentRemoved(JEditBuffer arg0, int arg1, int arg2, int arg3,int arg4) {
					if(!isModified()) {
						setModified(true);
					}
					if (isFree()) {
						setFree(false);
					}
				}
				@Override
				public void contentInserted(JEditBuffer arg0, int arg1, int arg2, int arg3,int arg4) {
					if(!isModified()) {
						setModified(true);
					}
					if (isFree()) {
						setFree(false);
					}
				}
			});
		}


		public void setModified(boolean value) {
			isModified = value;
			textPane.getBuffer().setDirty(value);
			tab.setModified(value);
		}

		public void setFree(boolean value) {
			isFree = value;
		}

		public void setFile(File file) {
			this.file = file;
		}

		public boolean isFree() {
			return isFree;
		}

		public boolean isModified() {
			return isModified;
		}

		public TextPane getTextPane() {
			return textPane;
		}

		public File getFile() {
			return file;
		}

		public StyledTab getTab() {
			return tab;
		}

		public File getDirectory() {
			if (file == null)
				return null;
			if (file.isDirectory())
				return file;
			else
				return file.getParentFile();
		}

		public void propertiesChanged() {
			textPane.updateGutter();
		}
	}

	private class StyledTab extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private JLabel label;
		private String title;
		public Icon iconOff = new ImageIcon(getClass().getResource("/org/colombbus/tangara/x_off.png")); //$NON-NLS-1$
		public Icon iconOn = new ImageIcon(getClass().getResource("/org/colombbus/tangara/x_on.png")); //$NON-NLS-1$

	    public StyledTab(String title)
	    {
	        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
	        setOpaque(false);
	        this.title = title;
	        label = new JLabel(title);
	        add(label);
	        final JLabel CrossIcon = new JLabel();
	        CrossIcon.setText(""); //$NON-NLS-1$
	        CrossIcon.setBackground(Color.white);
	        CrossIcon.setIcon(iconOff);

	        CrossIcon.addMouseListener(new java.awt.event.MouseAdapter()
	        {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e)
				{
					closePane(TextPaneManager.this.indexOfTabComponent(StyledTab.this));
				}
				@Override
				public void mouseExited(java.awt.event.MouseEvent e)
				{
					CrossIcon.setIcon(iconOff);
				}
				@Override
				public void mouseEntered(java.awt.event.MouseEvent e)
				{
					CrossIcon.setIcon(iconOn);
				}
			});
	        add(new JLabel("     ")); //$NON-NLS-1$
	        add(CrossIcon);
	    }

	    public void setModified(boolean value) {
	    	if (value)
	    		label.setText("* "+title); //$NON-NLS-1$
	    	else
	    		label.setText(title);
	    	repaint();
	    }

	    public void setTitle(String title) {
	    	this.title = title;
	    }

	}

    class CDropTargetListener implements DropTargetListener
    {
        @Override
		public void dragEnter(DropTargetDragEvent e)
        {
            if(isDragAcceptable(e)) e.acceptDrag(e.getDropAction());
            else e.rejectDrag();
        }
        @Override
		public void dragExit(DropTargetEvent e){}
        @Override
		public void dropActionChanged(DropTargetDragEvent e){}

        private Point pt_ = new Point();
        @Override
		public void dragOver(final DropTargetDragEvent e) {
            Point pt = e.getLocation();
            initTargetLeftRightLine(getTargetTabIndex(pt));
            if(!pt_.equals(pt)) glassPane.repaint();
        }

        @Override
		public void drop(DropTargetDropEvent e) {
            if(isDropAcceptable(e)) {
                convertTab(dragTabIndex, getTargetTabIndex(e.getLocation()));
                e.dropComplete(true);
            }else{
                e.dropComplete(false);
            }
            repaint();
        }
        public boolean isDragAcceptable(DropTargetDragEvent e) {
            Transferable t = e.getTransferable();
            if(t==null) return false;
            DataFlavor[] f = e.getCurrentDataFlavors();
            if(t.isDataFlavorSupported(f[0]) && dragTabIndex>=0) {
                return true;
            }
            return false;
        }
        public boolean isDropAcceptable(DropTargetDropEvent e) {
            Transferable t = e.getTransferable();
            if(t==null) return false;
            DataFlavor[] f = t.getTransferDataFlavors();
            if(t.isDataFlavorSupported(f[0]) && dragTabIndex>=0) {
                return true;
            }
            return false;
        }
    }

	/**
	 * This is a class for the plusButton.
	 *
	 */
	private class NewPaneButton extends JPanel {
		private static final long serialVersionUID = 1L;
		public Icon iconOff = new ImageIcon(getClass().getResource("/org/colombbus/tangara/+_off.png")); //$NON-NLS-1$
		public Icon iconOn = new ImageIcon(getClass().getResource("/org/colombbus/tangara/+_on.png")); //$NON-NLS-1$
		private JLabel label;


		public NewPaneButton() {
			setOpaque(false);
			label = new JLabel();
			add(label);
			off();
	        addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					addPane();
				}
				@Override
				public void mouseExited(java.awt.event.MouseEvent e) {
					off();
				}
				@Override
				public void mouseEntered(java.awt.event.MouseEvent e) {
					on();
				}
			});

		}

		private void off() {
	        label.setIcon(iconOff);
        }

		private void on() {
	        label.setIcon(iconOn);
        }
    }

	public boolean getDisplayLineNumbers() {
		return displayLineNumbers;
	}

	public void setDisplayLineNumbers(boolean state) {
		displayLineNumbers = state;
		TextPane.setDisplayLineNumbers(state);
		for (TextPaneTab tab:panes ){
			tab.propertiesChanged();
		}
	}

	public void selectLine(int programIndex, int lineNumber) {
		if (programIndex < getPanesCount()) {
			setSelectedIndex(programIndex);
			TextPane pane = getCurrentPane();
			pane.selectLine(lineNumber);
			pane.requestFocusInWindow();
		}
	}

}
