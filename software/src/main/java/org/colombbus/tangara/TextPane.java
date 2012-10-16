package org.colombbus.tangara;

import org.apache.log4j.Logger;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.ScrollLayout;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.gjt.sp.jedit.textarea.TextArea;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.jedit.JEditBeanShellAction;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.TextUtilities;
import org.gjt.sp.util.IOUtilities;


@SuppressWarnings("serial")
class TextPane extends StandaloneTextArea implements Editable {

    static Properties props;
    static boolean displayLineNumbers = false;
    protected EditorFrame parentFrame;
    protected static Logger LOG  = Logger.getLogger(TextPane.class);;
    protected static final String AUTHORIZED_CHARACTERS = "[]_"; //$NON-NLS-1$
    protected KeyListener keyListener;
    protected JPanel leftPane, topPane;
	private final IndicatorLine glassPane = new IndicatorLine();
    private final Rectangle lineRect  = new Rectangle(0,0,0,0);
    private int numberOfLines = 0;
    private int insertedLine = 0;
    private boolean mayCopy = false;

    private Vector<Rectangle> rectangles = new Vector<Rectangle>();
    private int previousRect = -1;

    long beginTime;
    long currentTime;
    boolean scrolling = false;

    static {
        props = new Properties();
        props.putAll(loadProperties("/org/gjt/sp/jedit/jedit.props"));
        props.putAll(loadProperties("/org/colombbus/tangara/jedit.props"));
        props.setProperty("view.font", Configuration.instance().getProperty("editor.font"));
        props.setProperty("view.fontsize", Configuration.instance().getProperty("editor.fontSize"));
        String indentWith = Configuration.instance().getProperty("tab.char");
        if (indentWith.equals("TAB")) {
            props.setProperty("buffer.noTabs", "false");
            props.setProperty("buffer.indentSize", Configuration.instance().getProperty("tab.size"));
            props.setProperty("buffer.tabSize", Configuration.instance().getProperty("tab.size"));
        } else {
            props.setProperty("buffer.noTabs", "true");
            props.setProperty("buffer.indentSize", Configuration.instance().getProperty("tab.spaces"));
            props.setProperty("buffer.tabSize", Configuration.instance().getProperty("tab.spaces"));
        }
        props.setProperty("buffer.undoCount", Configuration.instance().getProperty("undo.limit"));
        // view.gutter.fgColor=#AAAAAA
        // view.gutter.bgColor=#FAFAFA
        // view.gutter.currentLineColor=#7F0055

    }

    public TextPane(EditorFrame parentFrame, IPropertyManager propertyManager, boolean command) {
        super(propertyManager);
        this.parentFrame = parentFrame;
        if (Configuration.instance().getInteger("editor.color")==1) {
            Mode mode = new Mode("java");
            try {
                File javaProperties = new File(Configuration.instance().getTangaraPath().getParentFile(), "modes/java.xml");
                mode.setProperty("file",javaProperties.getAbsolutePath());
            } catch (Exception e) {
                LOG.error(e);
            }
            ModeProvider.instance.addMode(mode);
            getBuffer().setMode(mode);
        }
        this.setMinimumSize(new Dimension(0,0));
        Color background = Color.decode(propertyManager.getProperty("view.bgColor"));
        leftPane = new JPanel();
        leftPane.setPreferredSize(new Dimension(5,5));
        leftPane.setMinimumSize(new Dimension(5,5));
        leftPane.setBackground(background);
        topPane = new JPanel();
        topPane.setPreferredSize(new Dimension(5,5));
        topPane.setMinimumSize(new Dimension(5,5));
        topPane.setBackground(background);
        if (command) {
        	// we do not need any gutter : we place a pane instead
        	add(ScrollLayout.LEFT,leftPane);
            add(ScrollLayout.TOP,topPane);
        } else {
        	updateGutter();
        }
        if (command)
            keyListener = new CommandKeyListener();
        else
            keyListener = new ProgramKeyListener();

        this.addCaretListener(new EditCaretListener());
        getBuffer().addBufferListener(new EditBufferListener());

        initDragAndDrop();

        this.addFocusListener(new FocusAdapter() {
        	@Override
			public void focusGained(FocusEvent e) {
        		TextPane.this.parentFrame.setFocusOwner(TextPane.this);
        	}
        });
    }

    private static Properties loadProperties(String fileName) {
        Properties loadedProps = new Properties();
        InputStream in = TextArea.class.getResourceAsStream(fileName);
        try {
            loadedProps.load(in);
        }
        catch (IOException e) {
            LOG.error(e);
        }
        finally {
            IOUtilities.closeQuietly(in);
        }
        return loadedProps;
    }

    @Override
	public void processKeyEvent(KeyEvent evt)
    {
        if (evt.getID() == KeyEvent.KEY_PRESSED) {
            keyListener.keyPressed(evt);
        } else if (evt.getID() == KeyEvent.KEY_TYPED) {
            keyListener.keyTyped(evt);
        }
        if(!evt.isConsumed())
            super.processKeyEvent(evt);

    }

    @Override
	public void copy() {
        JEditBeanShellAction copyAction = getActionContext().getAction("copy");
        if (copyAction != null) {
            copyAction.invoke(this);
        }
    }

    @Override
	public void paste() {
        JEditBeanShellAction pasteAction = getActionContext().getAction("paste");
        if (pasteAction != null) {
            pasteAction.invoke(this);
        }
    }

    @Override
	public void cut() {
        JEditBeanShellAction cutAction = getActionContext().getAction("cut");
        if (cutAction != null) {
            cutAction.invoke(this);
        }
    }

    public void undo() {
        getBuffer().undo(this);
    }

    public void redo() {
        getBuffer().redo(this);
    }

    private boolean recursiveSearch(String text, boolean way, boolean caseSensitive, boolean recursive) {
        int nextSearchPos = getCaretPosition();

        if (!caseSensitive)
            text = text.toLowerCase();

        String currentSelection = getSelectedText();

        if (currentSelection !=null) {
            // Search already running, go to next searched item
            if (!caseSensitive)
                currentSelection = currentSelection.toLowerCase();
            if (way &&currentSelection.equals(text))
                nextSearchPos += 1;
        }


        if (way && nextSearchPos >= buffer.getLength()) {
            if (recursive) {
                setCaretPosition(0);
                return recursiveSearch(text, way, caseSensitive, false);
            } else
                return false;
        }

        String remainingText;
        int nextOccurrence;
        if (way) {
            remainingText = getText(nextSearchPos, buffer.getLength() - nextSearchPos);
            if (!caseSensitive) {
                remainingText = remainingText.toLowerCase();
            }
            nextOccurrence = remainingText.indexOf(text);
        } else {
            remainingText = getText(0, nextSearchPos);
            if (!caseSensitive) {
                remainingText = remainingText.toLowerCase();
            }
            nextOccurrence = remainingText.lastIndexOf(text);
        }

        if (nextOccurrence < 0) {
            if (recursive&&(buffer.getLength()>0)) {
                if (way) {
                    setCaretPosition(0);
                    return recursiveSearch(text, way, caseSensitive, false);
                } else {
                    setCaretPosition(buffer.getLength()-1);
                    return recursiveSearch(text, way, caseSensitive, false);
                }
            } else {
                return false;
            }
        }

        if (way)
            nextOccurrence+= nextSearchPos ;

        int endSelection = nextOccurrence + text.length();
        setCaretPosition(nextOccurrence);
        Selection.Range selection = new Selection.Range(nextOccurrence,endSelection);
        setSelection(selection);
        return true;
    }

    public boolean search(String text, boolean way, boolean caseSensitive) {
        return recursiveSearch(text, way, caseSensitive, true);
    }

    private class CommandKeyListener extends KeyAdapter {
        @Override
		public void keyTyped(KeyEvent evt) {
            if ((evt.getKeyChar() == '.') || (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
                // Find the object name
                int line = getCaretLine();
                if(getLineLength(line) == 0)
                    return;
                int lineStart = getLineStartOffset(line);
                int position = getCaretPosition() - lineStart -1;
                String lineText = getLineText(line);
                if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                    if (lineText.charAt(position)!='.') {
                        parentFrame.stopPopupDisplay();
                        return;
                    }
                    position--;
                }
                int wordStart = TextUtilities.findWordStart(lineText,position,AUTHORIZED_CHARACTERS);
                int wordEnd = TextUtilities.findWordEnd(lineText,wordStart+1, AUTHORIZED_CHARACTERS);
                String objectName = lineText.substring(wordStart, wordEnd);
                boolean inList = false;
                if (objectName.endsWith("]")) {
                    // List element: get actual name
                    objectName = objectName.substring(0,objectName.lastIndexOf("["));
                    inList = true;
                }
                objectName = StringUtils.removeAccents(objectName);

                Object obj = Program.instance().getObject(objectName);

                if (obj != null) {
                    Class<?> objectClass = obj.getClass();
                    if (inList&&objectClass.isArray()) {
                        objectClass = objectClass.getComponentType();
                    }
                    parentFrame.displayWritingHelp(objectClass);
                } else {
                    LOG.warn("Error trying to retrieve class of object '"+objectName+"'");
                }
            } else {
                parentFrame.stopPopupDisplay();
            }
        }

        @Override
		public void keyPressed(KeyEvent evt) {
            if ((evt.getModifiersEx() & parentFrame.commandModifier) == parentFrame.commandModifier) {
                switch (evt.getKeyCode()) {
                case KeyEvent.VK_A:
                    TextPane.this.selectAll();
                }
            } else {
                switch (evt.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    parentFrame.executeInputScript();
                    evt.consume();
                    break;
                case KeyEvent.VK_UP:
                    parentFrame.historyUp();
                    evt.consume();
                    break;
                case KeyEvent.VK_DOWN:
                    parentFrame.historyDown();
                    evt.consume();
                    break;
                case KeyEvent.VK_ESCAPE:
                    parentFrame.clearCommand();
                    evt.consume();
                    break;
                }
            }
        }
    }

    private class ProgramKeyListener extends KeyAdapter {
        @Override
		public void keyTyped(KeyEvent evt) {
            if ((evt.getKeyChar() == '.') || (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
            	try {
	            	// Find the object name
	                int line = getCaretLine();
	                if(getLineLength(line) == 0)
	                    return;
	                int lineStart = getLineStartOffset(line);
	                int position = getCaretPosition() - lineStart -1;
	                String lineText = getLineText(line);
	                if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
	                    if (lineText.charAt(position)!='.') {
	                        parentFrame.stopPopupDisplay();
	                        return;
	                    }
	                    position--;
	                }
	                int wordStart = TextUtilities.findWordStart(lineText,position,AUTHORIZED_CHARACTERS);
	                int wordEnd = TextUtilities.findWordEnd(lineText,wordStart+1, AUTHORIZED_CHARACTERS);
	                String objectName = lineText.substring(wordStart, wordEnd);
	                Pattern toFind = null;
	                if (objectName.endsWith("]")) {
	                    // List element: get actual name
	                    objectName = objectName.substring(0,objectName.lastIndexOf("["));
	                    toFind = Pattern.compile("\\b"+ objectName+ "\\s*=\\s*new\\s*([\\p{L}0-9_]*)\\s*\\[");
	                } else {
                		toFind = Pattern.compile("\\b"+ objectName+ "\\s*=\\s*new\\s*([\\p{L}0-9_]*)\\s*\\(");
	                }
	                Matcher m = toFind.matcher(getBuffer().getText(0, getBuffer().getLength()));
	                Class<?> objectClass = null;
	                Configuration configuration = Configuration.instance();
	                if (m.find()) {
	                    String className = m.group(1);
	                    className = StringUtils.removeAccents(className);
	                    try {
	                        URLClassLoader cl = configuration.getObjectsClassLoader();
	                        if (cl != null) {
	                            objectClass = Class.forName("org.colombbus.tangara.objects."+ configuration.getLanguage()+ "."+ className,false, cl);
	                        } else {
	                            LOG.warn("Unable to find class " + className+ " : class loader null");
	                        }
	                    }
	                    catch (ClassNotFoundException ex) {
	                        throw new Exception("Class " + className+ " not found");
	                    }
	                } else {
	                    String objectNameNoAccents = StringUtils.removeAccents(objectName);
	                    Object object = configuration.getManager().lookupBean(objectNameNoAccents);
	                    if (object != null) {
	                        objectClass = object.getClass();
	                    } else {
	                        throw new Exception("bean " + objectName+ " not found");
	                    }
	                }
	                if (objectClass != null) {
	                    parentFrame.displayWritingHelp(objectClass);
	                }
	                else {
	                	throw new Exception("Error trying to display help for object '"+objectName+"'");
	                }
            	}
            	catch (Exception e) {
                    LOG.debug(e.getMessage());
            	}
            } else {
                parentFrame.stopPopupDisplay();
            }
        }

        @Override
		public void keyPressed(KeyEvent evt) {
            if ((evt.getModifiersEx() & parentFrame.commandModifier) == parentFrame.commandModifier) {
                switch (evt.getKeyCode()) {
                case KeyEvent.VK_A:
                    TextPane.this.selectAll();
                    break;
                }
            }
        }
    }

    @Override
    public void createPopupMenu(MouseEvent evt)
    {
        popup = new JPopupMenu();
        popup.add(parentFrame.getCopyAction());
        popup.add(parentFrame.getCutAction());
        popup.add(parentFrame.getPasteAction());
        if (!parentFrame.getMode()) {
            popup.addSeparator();
            popup.add(parentFrame.getUndoAction());
            popup.add(parentFrame.getRedoAction());
        }
    } //}}}

    public boolean canUndo() {
        return getBuffer().canUndo();
    }

    public boolean canRedo() {
        return getBuffer().canRedo();
    }

    public void insertLines(ArrayList<String> lines) {
    	String text = "";
    	for (String line: lines) {
    		text+="\n"+line;
    		if (!line.endsWith(";")) //$NON-NLS-1$
				text+=";"; //$NON-NLS-1$
    	}
    	this.insertText(text);
    }

    public void insertText(String text) {
    	int startInsertion = getCaretPosition();
		getBuffer().insert(startInsertion, text);
		this.setSelection(new Selection.Range(startInsertion+1,startInsertion+text.length()));
		requestFocusInWindow();
    }

	@SuppressWarnings("unused")
   private void initDragAndDrop() {
    	setTransferHandler(parentFrame.getCommandHandler());

    	//TODO : à améliorer !!
    	for(int i = 0; i < 40; i++) {
        	rectangles.add(new Rectangle(-10, 98+23*i, 1550, 23));
        }

		final DropTarget initialDropTarget = getDropTarget();

		new DropTarget(this, new DropTargetListener() {

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {}

			@Override
			public void drop(DropTargetDropEvent dtde) {
				initialDropTarget.drop(dtde);
				lineRect.setRect(0,0,0,0);
				parentFrame.setGlassPane(glassPane);
		        glassPane.setVisible(false);
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde)
			{
				int textLength = getText().length();
				if(textLength > 0)
				{
					Point p = dtde.getLocation();
					p = SwingUtilities.convertPoint(TextPane.this,p,getPainter());
					int pos = xyToOffset(p.x,p.y,!(getPainter().isBlockCaretEnabled() || isOverwriteEnabled()));
					if(pos != -1)
						moveCaretPosition(pos,TextArea.NO_SCROLL);
					boolean contains = false;

					for(int i = 0; i < numberOfLines-1; i++) {
						if(rectangles.get(i).contains(new Point(p.x, p.y+110))) {
							Rectangle r = new Rectangle(rectangles.get(i).x, rectangles.get(i).y + 10, getBounds().width, 3);
							lineRect.setRect(r);
							insertedLine = i;
							contains = true;
							break;
						}
					}

					if(!contains) {
						Rectangle r = new Rectangle(rectangles.get(numberOfLines-1).x, rectangles.get(numberOfLines-1).y + 10, getBounds().width, 3);
						lineRect.setRect(r);
						insertedLine = numberOfLines-1;
					}

					if(getCaretLine()-getFirstLine()+1 > insertedLine) {
						int currentLine = getCaretLine();
						while (getCaretLine() == currentLine && getCaretPosition() != 0) {
							moveCaretPosition(getCaretPosition()-1,TextArea.NO_SCROLL);
						}
					} else {
						int currentLine = getCaretLine();
						while (getCaretLine() == currentLine && getCaretPosition() != textLength) {
							moveCaretPosition(getCaretPosition()+1,TextArea.NO_SCROLL);
						}
						if(getCaretPosition() != textLength) {
							moveCaretPosition(getCaretPosition()-1,TextArea.NO_SCROLL);
						}
						else if(getText().charAt(textLength-1) == '\n' && insertedLine != numberOfLines - 1) {
							moveCaretPosition(getCaretPosition()-1,TextArea.NO_SCROLL);
						}
					}

					if(insertedLine != previousRect) {
						parentFrame.setGlassPane(glassPane);
				        glassPane.setVisible(true);
				        beginTime = System.currentTimeMillis();
					} else {
						currentTime = System.currentTimeMillis();
						if(scrolling) {
							if((currentTime - beginTime) > 40) {
								beginTime = System.currentTimeMillis();
								if(getCaretLine()-getFirstLine() > getVisibleLines()-5) {
									setFirstLine(getFirstLine()+1);
								} else if(getCaretLine()-getFirstLine() < 3) {
									setFirstLine(getFirstLine()-1);
								} else {
									scrolling = false;
								}
							}
						} else {
							if((currentTime - beginTime) > 1500) {
								beginTime = System.currentTimeMillis()-1460;
								if(getCaretLine()-getFirstLine() > getVisibleLines()-5) {
									setFirstLine(getFirstLine()+1);
									scrolling = true;
								} else if(getCaretLine()-getFirstLine() < 3) {
									setFirstLine(getFirstLine()-1);
									scrolling = true;
								}
							}
						}
					}
					previousRect = insertedLine;
				}
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
				lineRect.setRect(0,0,0,0);
				parentFrame.setGlassPane(glassPane);
		        glassPane.setVisible(false);
		        previousRect = -1;
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				initialDropTarget.dragEnter(dtde);
				int firstLine = getFirstLine();
				int currentCarretPosition = getCaretPosition();
				String content = getText();
				setCaretPosition(content.length());
				int lastLine = getCaretLine();
				numberOfLines = lastLine - firstLine + 2;
				setCaretPosition(currentCarretPosition);
				setFirstLine(firstLine);
			}
		});

    }


    private class IndicatorLine extends JPanel
    {
		private final AlphaComposite IndicatorLine;

		public IndicatorLine() {
            setOpaque(false);
            IndicatorLine = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        }

        @Override
		public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(IndicatorLine);
            g2.setPaint(Color.BLUE);
            g2.fill(lineRect);
        }
    }

    private class EditCaretListener implements CaretListener {

            @Override
			public void caretUpdate(CaretEvent e) {
                int dot = e.getDot();
                int mark = e.getMark();
                if (dot == mark) {
                    parentFrame.disableEdit();
                    mayCopy = false;
                } else {
                    parentFrame.enableEdit();
                    mayCopy = true;
                }
            }
    }

    public static IPropertyManager getProperties(boolean commandMode) {
        IPropertyManager propertyManager;
        if (commandMode) {
            propertyManager = new IPropertyManager() {
                @Override
				public String getProperty(String name) {
                    if (name.compareTo("view.selectionColor")==0)
                        return "#9999aa";
                    return props.getProperty(name);
                }
            };
        } else {
            propertyManager = new IPropertyManager() {
                @Override
				public String getProperty(String name) {
                    if (name.compareTo("view.bgColor")==0)
                        return "#FAFAFA";
                    return props.getProperty(name);
                }
            };

        }
        return propertyManager;

    }

    private class EditBufferListener extends BufferAdapter {

        private void updateState(JEditBuffer arg0) {
            if (arg0.canUndo()) {
                parentFrame.enableUndo();
            } else {
                parentFrame.disableUndo();
            }
            if (arg0.canRedo()) {
                parentFrame.enableRedo();
            } else {
                parentFrame.disableRedo();
            }
        }

        @Override
        public void bufferLoaded(JEditBuffer arg0) {
            updateState(arg0);
        }

        @Override
        public void contentInserted(JEditBuffer arg0, int arg1, int arg2,
                int arg3, int arg4) {
            updateState(arg0);
        }

        @Override
        public void contentRemoved(JEditBuffer arg0, int arg1, int arg2,
                int arg3, int arg4) {
            updateState(arg0);
        }

        @Override
		public void transactionComplete(JEditBuffer buffer) {
            updateState(buffer);
        }
    }

    @Override
	public boolean mayCopy() {
    	return mayCopy;
    }

    public static void setDisplayLineNumbers(boolean value) {
    	displayLineNumbers = value;
    }

    public void updateGutter() {
    	if (displayLineNumbers) {
    		remove(leftPane);
    		remove(topPane);
    		getGutter().setEnabled(true);
        	add(ScrollLayout.LEFT,getGutter());
        	revalidate();
    	} else {
    		getGutter().setEnabled(false);
    		remove(getGutter());
        	add(ScrollLayout.LEFT,leftPane);
        	add(ScrollLayout.TOP,topPane);
        	revalidate();
    	}
    }

    public void selectLine(int lineNumber) {
    	setCaretPosition(getBuffer().getLineStartOffset(lineNumber));
    	selectLine();
    }

}