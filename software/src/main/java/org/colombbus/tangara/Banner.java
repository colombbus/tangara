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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Logger;
import org.colombbus.tangara.help.HelpAction;

/**
 * This is the banner at the top of GameArea. It serves as a menu
 * 
 * @author gwen
 * 
 */
@SuppressWarnings("serial")
public class Banner extends JMenuBar {
	/** Class logger */
	private static final Logger LOG = Logger.getLogger(Banner.class);

	private JMenu fileMenu = null;
	private JMenu editMenu = null;
	private JMenuItem openFileItem = null;
	private JMenuItem exportProgramItem = null;
	private JMenuItem closeTabItem = null;
	private JMenuItem exitItem = null;
    private JMenuItem searchItem = null;

	private JSeparator separator1 = null;
	private JSeparator separator2 = null;
	private JSeparator separator3 = null;
    private JSeparator separator4 = null;
    private JSeparator separator5 = null;
    private JSeparator separator6 = null;

	private JMenuItem library = null;
	private JMenuItem about = null;
	private JMenuItem help = null;
	private JMenuItem checkUpdate = null;

	private EditorFrame parentFrame;

	private boolean menuVisible = false;


	// for painting buffering
	private BufferedImage bannerImageModel;
	private BufferedImage bannerImage;
	private Image rightImage;
	private Color titleColor;
	private Font titleFont;
	private String title;
	private boolean popupOpen = false;
	private boolean onMenu = false;

	private static final String[] BG_IMG_NAME_LIST = {
			Messages.getString("Banner.backgroundImage"), Messages.getString("Banner.colombbusIcon"), Messages.getString("Banner.tangaraIcon") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private JMenuItem makeProgramFileItem = null;

	private JMenu toolsMenu = null;

	private JMenu helpMenu = null;

	private JCheckBoxMenuItem rulersItem = null;

	private JCheckBoxMenuItem writingHelpItem = null;

	private JCheckBoxMenuItem displayLineNumbersItem = null;
	
	private JMenuItem newProgramItem = null;

	private JMenuItem saveProgramItem = null;

	private JMenuItem saveProgramAsItem = null;

	private JMenu interfaceLevelMenu = null;

	private List<Image> imageList;

	private JRadioButtonMenuItem basicLevelItem = null;
	private JRadioButtonMenuItem advancedLevelItem = null;

	private JMenuItem configurationMenu = null;


	private JMenuItem undoItem = null;

	private JMenuItem redoItem = null;


	private static final Font MENU_FONT = new Font("Lucida Grande", Font.PLAIN,
			14);

	/**
	 * Creates a new banner placed on the <code>EditorFrame</code> according to
	 * the configuration passed as parameters
	 * 
	 * @param configuration
	 *            the program configuration
	 * @param parentFrame
	 *            the Tangara frame
	 */
	public Banner(Configuration configuration, EditorFrame parentFrame) {
		this.parentFrame = parentFrame;
		initialize();
		initPaint();
	}

	/**
	 * Repaints the menu if the mouse is no longer located on
	 * 
	 */
	private void testMenu() {
		if ((!popupOpen) && (!onMenu)) {
			menuVisible = false;
			repaint();
		}
	}


	/**
	 * Defines and draws the design of Tangara banner
	 * 
	 */
	private void initPaint() {
		if (imageList == null) {
			imageList = new ArrayList<Image>();
			MediaTracker imageLoader = new MediaTracker(this);
			for (String imgName : BG_IMG_NAME_LIST) {
				Image image = Toolkit.getDefaultToolkit().getImage(EditorFrame.class.getResource(imgName));
				imageLoader.addImage(image, imageList.size());
				imageList.add(image);
			}
			try {
				imageLoader.waitForAll();
			} catch (InterruptedException e) {
				LOG.warn("Error while loading banner images"); //$NON-NLS-1$
			}
		}

		// the first image is the background with the exact height & width
		Image bgImg = imageList.get(0);

		
		int maxScreenWidth=Toolkit.getDefaultToolkit().getScreenSize().width;
		bannerImageModel = new BufferedImage(maxScreenWidth, bgImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics bannerGraphics = bannerImageModel.getGraphics();

		// draw left image
		bannerGraphics.drawImage(imageList.get(1), 0, 0, null);
		

		int left_shift = imageList.get(1).getWidth(null);

		Dimension screenSize = parentFrame.getSize();
		int screenWidth = (int) screenSize.getWidth();

		// In order to adapt the center bar to the screen size, we draw many
		// images of 10 pixel width.
		for (int i = 0; i < screenWidth / 20; i++)
			bannerGraphics.drawImage(imageList.get(0), left_shift + i * 20, 0,null);


		// The center right image can be painted only when window width is
		// known. See paint method below.
		rightImage = imageList.get(2);

		titleColor = Messages.getColor("Banner.title.color");
		titleFont = Messages.getFont("Banner.title.font");
		String version = Configuration.instance().getProperty("tangara.version");
		title = Messages.formatMessage("Banner.title", version);
	}

	/**
	 * Paints the banner. Here we override paint and not paintComponent, because
	 * when the banner is displayed we don't want children elements like menus
	 * to be displayed
	 * 
	 * @param g
	 *            the graphics context to use for painting
	 */
	@Override
	public void paint(Graphics g) {
		if (!menuVisible) {
			bannerImage = new BufferedImage(getWidth(), bannerImageModel
					.getHeight(), bannerImageModel.getType());
			Graphics2D bannerGraphics = (Graphics2D) bannerImage.getGraphics();
			bannerGraphics.drawImage(bannerImageModel, 0, 0, null);

			// draws the right image
			initPaint();
			int rightImageXPos = bannerImage.getWidth()
					- rightImage.getWidth(null);
			bannerGraphics.drawImage(rightImage, rightImageXPos, 0, null);

			// draws the title
			bannerGraphics.setColor(titleColor);
			bannerGraphics.setFont(titleFont);
			FontMetrics fontMetrics = bannerGraphics.getFontMetrics();
			int titleLength = fontMetrics.stringWidth(title);
			// Sets to on text anti-aliasing
			bannerGraphics.setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			bannerGraphics.drawString(title,
					((getWidth() - titleLength) / 2), 27);

			g.drawImage(bannerImage, 0, 0, null);
		} else
			super.paint(g);
	}

	/**
	 * Sets the banner to work for program mode
	 * 
	 */
	public void setProgramMode() {
		newProgramItem.setVisible(true);
		closeTabItem.setVisible(true);
		saveProgramItem.setVisible(true);
		saveProgramAsItem.setVisible(true);
		makeProgramFileItem.setVisible(false);
		separator2.setVisible(false);
		writingHelpItem.setVisible(true);
		separator5.setVisible(true);
		undoItem.setVisible(true);
		redoItem.setVisible(true);
		separator4.setVisible(true);
        searchItem.setVisible(true);
		separator6.setVisible(true);
        displayLineNumbersItem.setVisible(true);
	}

	/**
	 * Sets the banner to work for command mode
	 * 
	 */
	public void setCommandMode() {
		newProgramItem.setVisible(false);
		closeTabItem.setVisible(false);
		saveProgramItem.setVisible(false);
		saveProgramAsItem.setVisible(false);
		makeProgramFileItem.setVisible(true);
		separator2.setVisible(true);
		writingHelpItem.setVisible(true);
		newProgramItem.setVisible(false);
		separator5.setVisible(false);
		undoItem.setVisible(false);
		redoItem.setVisible(false);
		separator4.setVisible(false);
        searchItem.setVisible(false);
		separator6.setVisible(false);
        displayLineNumbersItem.setVisible(false);
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setSize(new Dimension(114, 42));
		this.setFont(MENU_FONT);
		this.setPreferredSize(new Dimension(114, 42));
		this.add(getFileMenu());
		this.add(getEditMenu());
		this.add(getToolsMenu());
		this.add(getHelpingMenu());

		// Add the MouseListener and the MenuListener to all menus
		MouseAdapter myMouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				onMenu = true;
				if (menuVisible == false) {
					menuVisible = true;
					repaint();
				}
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				onMenu = false;
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						testMenu();
					}
				});
			}
		};
		MenuListener myMenuListener = new MenuListener() {

			@Override
			public void menuCanceled(MenuEvent e) {
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				popupOpen = false;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						testMenu();
					}
				});
			}

			@Override
			public void menuSelected(MenuEvent e) {
				popupOpen = true;
			}
		};

		this.addMouseListener(myMouseListener);

		for (int i = 0; i < this.getMenuCount(); i++) {
			JMenu menu = this.getMenu(i);
			menu.addMenuListener(myMenuListener);
			menu.addMouseListener(myMouseListener);
		}

		this.addComponentListener(new ResizeBanner());

		ButtonGroup interfaceLevelGrp = new ButtonGroup();
		interfaceLevelGrp.add(basicLevelItem);
		interfaceLevelGrp.add(advancedLevelItem);
	}

	/**
	 * This method initializes fileMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText(Messages.getString("Banner.menu.file")); //$NON-NLS-1$
			fileMenu.setFont(MENU_FONT);
			fileMenu.add(getNewProgramItem());
			fileMenu.add(getOpenFileItem());
			fileMenu.add(getCloseTabItem());
			fileMenu.add(getSeparator3());
			fileMenu.add(getSaveProgramItem());
			fileMenu.add(getSaveProgramAsItem());
			fileMenu.add(getMakeProgramFileItem());
			fileMenu.add(getSeparator1());
			fileMenu.add(getExitItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes editMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getEditMenu() {
		if (editMenu == null) {
			editMenu = new JMenu();
			editMenu.setText(Messages.getString("Banner.menu.edit")); //$NON-NLS-1$
			editMenu.setFont(MENU_FONT);
			editMenu.add(getUndoItem());
			editMenu.add(getRedoItem());
			editMenu.add(getSeparator4());
			editMenu.add(parentFrame.getCopyAction()).setFont(MENU_FONT);
			editMenu.add(parentFrame.getCutAction()).setFont(MENU_FONT);
			editMenu.add(parentFrame.getPasteAction()).setFont(MENU_FONT);
			editMenu.add(getSeparator2());
			editMenu.add(getWritingHelpItem());
			editMenu.add(getSeparator5());
			editMenu.add(getSearchItem());
			editMenu.add(getSeparator6());
			editMenu.add(getDisplayLineNumbersItem());
		}
		return editMenu;
	}

	/**
	 * This method initializes exportProgramItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getExportProgramItem() {
		if (exportProgramItem == null) {
			exportProgramItem = new JMenuItem();
			exportProgramItem.setText(Messages.getString("Banner.menu.export")); //$NON-NLS-1$
			exportProgramItem.setFont(MENU_FONT); //$NON-NLS-1$
			exportProgramItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						parentFrame.exportProgram();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		return exportProgramItem;
	}
	
	/**
	 * This method initializes openFileItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenFileItem() {
		if (openFileItem == null) {
			openFileItem = new JMenuItem();
			openFileItem.setText(Messages.getString("Banner.menu.open")); //$NON-NLS-1$
			openFileItem.setFont(MENU_FONT); //$NON-NLS-1$
			openFileItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					parentFrame.openFile();
				}
			});
		}
		return openFileItem;
	}
	
	/**
	 * This method initializes closeTabItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getCloseTabItem() {
		if (closeTabItem == null) {
			closeTabItem = new JMenuItem();
			closeTabItem.setText(Messages.getString("Banner.menu.close")); //$NON-NLS-1$
			closeTabItem.setFont(MENU_FONT); //$NON-NLS-1$
			closeTabItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					parentFrame.closePane();
				}
			});
		}
		return closeTabItem;
	}

	/**
	 * This method initializes exitItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getExitItem() {
		if (exitItem == null) {
			exitItem = new JMenuItem();
			exitItem.setText(Messages.getString("Banner.menu.exit")); //$NON-NLS-1$
			exitItem.setFont(MENU_FONT);
			exitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					parentFrame.exit();
				}
			});
		}
		return exitItem;
	}




	/**
	 * This method initializes separator1
	 * 
	 * @return javax.swing.JSeparator
	 */
	private JSeparator getSeparator1() {
		if (separator1 == null) {
			separator1 = new JSeparator();
		}
		return separator1;
	}

	/**
	 * This method initializes separator2
	 * 
	 * @return javax.swing.JSeparator
	 */
	private JSeparator getSeparator2() {
		if (separator2 == null) {
			separator2 = new JSeparator();
		}
		return separator2;
	}

	
	/**
	 * This method initializes makeProgramFileItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMakeProgramFileItem() {
		if (makeProgramFileItem == null) {
			makeProgramFileItem = new JMenuItem();
			makeProgramFileItem.setText(Messages
					.getString("Banner.menu.makeProgram")); //$NON-NLS-1$
			makeProgramFileItem.setFont(MENU_FONT);
			makeProgramFileItem
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							parentFrame.makeProgram();
						}
					});
		}
		return makeProgramFileItem;
	}

	/**
	 * This method initializes toolsMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getToolsMenu() {
		if (toolsMenu == null) {
			toolsMenu = new JMenu();
			toolsMenu.setText(Messages.getString("Banner.menu.tools")); //$NON-NLS-1$
			toolsMenu.setFont(MENU_FONT);
			toolsMenu.add(getRulersItem());
			toolsMenu.add(getInterfaceLevelMenu());
			if (Configuration.instance()
					.getProperty("configuration.activation").equals("1"))
				toolsMenu.add(getConfigurationMenu());
			if (Configuration.instance().getProperty("library.activation")
					.equals("1"))
				toolsMenu.add(getLibrary());
			toolsMenu.addSeparator();
			toolsMenu.add(getExportProgramItem());

		}
		return toolsMenu;
	}

	/**
	 * This method initializes library
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenuItem getLibrary() {
		if (library == null) {
			library = new JMenuItem();
			library.setText(Messages.getString("Banner.menu.library"));
			library.setFont(MENU_FONT);
			library.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					parentFrame.makeLibrary();
				}
			});
		}
		return library;
	}

	/**
	 * This method initializes helpMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getHelpingMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			helpMenu.setText(Messages.getString("Banner.menu.help")); //$NON-NLS-1$
			helpMenu.setFont(MENU_FONT);
			helpMenu.add(getHelp());
			helpMenu.add(getCheckUpdate());
			helpMenu.add(getAbout());
		}
		return helpMenu;
	}


	/**
	 * Get help menu's item.
	 * 
	 * @return A help menu's item never <code>null</code>.
	 */
	private JMenuItem getHelp() {
		if (help == null) {
			help = new JMenuItem(new HelpAction());
			help.setFont(MENU_FONT);
		}

		return help;
	}
	
	private JMenuItem getCheckUpdate() {
		if( checkUpdate == null) {
			checkUpdate = new JMenuItem( new CheckUpdateAction());
			checkUpdate.setFont(MENU_FONT);
		}
		return checkUpdate;
	}
	
	/**
	 * This method initializes about
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenuItem getAbout() {
		if (about == null) {
			about = new JMenuItem();
			about.setText(Messages.getString("Banner.menu.about"));
			about.setFont(MENU_FONT);
			about.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					parentFrame.makeAbout();
				}
			});
		}
		return about;
	}
	
    private JMenuItem getSearchItem() {
        if (searchItem == null) {
            searchItem = new JMenuItem(parentFrame.getSearchAction());
            searchItem.setFont(MENU_FONT);
        }
        return searchItem;
    }

	/**
	 * This method initializes rulersItem
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getRulersItem() {
		if (rulersItem == null) {
			rulersItem = new JCheckBoxMenuItem();
			rulersItem.setText(Messages.getString("Banner.menu.rulers")); //$NON-NLS-1$
			rulersItem.setFont(MENU_FONT);
			rulersItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					Program.instance().getDefaultGraphicsPane().displayRulers(
							rulersItem.getState());
				}
			});
		}
		return rulersItem;
	}


	/**
	 * This method initializes separator4
	 * 
	 * @return javax.swing.JSeparator
	 */
	private JSeparator getSeparator3() {
		if (separator3 == null) {
			separator3 = new JSeparator();
		}
		return separator3;
	}

	/**
	 * This method initializes writingHelpItem
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getWritingHelpItem() {
		if (writingHelpItem == null) {
			writingHelpItem = new JCheckBoxMenuItem();
			writingHelpItem.setFont(MENU_FONT);
			writingHelpItem.setText(Messages
					.getString("Banner.menu.writingHelp")); //$NON-NLS-1$
			writingHelpItem.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					parentFrame.setWritingHelp(writingHelpItem.getState());
				}
			});
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					writingHelpItem.setState(parentFrame.getWritingHelp());
				}
			});
		}
		return writingHelpItem;
	}
	
	/**
	 * This method initializes writingHelpItem
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getDisplayLineNumbersItem() {
		if (displayLineNumbersItem == null) {
			displayLineNumbersItem = new JCheckBoxMenuItem();
			displayLineNumbersItem.setFont(MENU_FONT);
			displayLineNumbersItem.setText(Messages
					.getString("Banner.menu.displayLineNumbers")); //$NON-NLS-1$
			displayLineNumbersItem.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					parentFrame.setDisplayLineNumbers(displayLineNumbersItem.getState());
				}
			});
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					displayLineNumbersItem.setState(parentFrame.getDisplayLineNumbers());
				}
			});
		}
		return displayLineNumbersItem;
	}

	/**
	 * This method initializes newProgramItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getNewProgramItem() {
		if (newProgramItem == null) {
			newProgramItem = new JMenuItem();
			newProgramItem.setFont(MENU_FONT);
			newProgramItem
					.setText(Messages.getString("Banner.menu.newProgram")); //$NON-NLS-1$
			newProgramItem
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							parentFrame.newPane();
						}
					});
		}
		return newProgramItem;
	}

	/**
	 * This method initializes saveProgramItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveProgramItem() {
		if (saveProgramItem == null) {
			saveProgramItem = new JMenuItem();
			saveProgramItem.setFont(MENU_FONT);
			saveProgramItem.setText(Messages
					.getString("Banner.menu.saveProgram")); //$NON-NLS-1$
			saveProgramItem
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							parentFrame.saveProgram();
						}
					});
			saveProgramItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
		}
		return saveProgramItem;
	}

	/**
	 * This method initializes saveProgramAsItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveProgramAsItem() {
		if (saveProgramAsItem == null) {
			saveProgramAsItem = new JMenuItem();
			saveProgramAsItem.setFont(MENU_FONT);
			saveProgramAsItem.setText(Messages
					.getString("Banner.menu.saveProgramAs")); //$NON-NLS-1$
			saveProgramAsItem
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							parentFrame.saveProgramAs();
						}
					});
		}
		return saveProgramAsItem;
	}

	/**
	 * This method initializes interfaceLevelMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getInterfaceLevelMenu() {
		if (interfaceLevelMenu == null) {
			interfaceLevelMenu = new JMenu();
			interfaceLevelMenu.setFont(MENU_FONT);
			interfaceLevelMenu.setText(Messages
					.getString("Banner.menu.interfaceLevel")); //$NON-NLS-1$
			interfaceLevelMenu.add(getBasicLevelItem());
			interfaceLevelMenu.add(getAdvancedLevelItem());
		}
		return interfaceLevelMenu;
	}

	/**
	 * This method initializes configurationMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenuItem getConfigurationMenu() {
		if (configurationMenu == null) {
			configurationMenu = new JMenuItem();
			configurationMenu.setFont(MENU_FONT);
			configurationMenu.setText(Messages
					.getString("Banner.menu.configuration")); //$NON-NLS-1$
			configurationMenu
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							parentFrame.setConfiguration();
						}
					});
		}
		return configurationMenu;
	}

	/**
	 * This method initializes basicLevelItem
	 * 
	 * @return javax.swing.JRadioButtonMenuItem
	 */
	private JRadioButtonMenuItem getBasicLevelItem() {
		if (basicLevelItem == null) {
			basicLevelItem = new JRadioButtonMenuItem();
			basicLevelItem.setFont(MENU_FONT);
			basicLevelItem.setText(Messages
					.getString("Banner.menu.interfaceBasicLevel")); //$NON-NLS-1$
			basicLevelItem
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (basicLevelItem.isSelected())
								parentFrame
										.setInterfaceLevel(Configuration.LEVEL_BASIC);
						}
					});

		}
		return basicLevelItem;
	}

	/**
	 * This method initializes advancedLevelItem
	 * 
	 * @return javax.swing.JRadioButtonMenuItem
	 */
	private JRadioButtonMenuItem getAdvancedLevelItem() {
		if (advancedLevelItem == null) {
			advancedLevelItem = new JRadioButtonMenuItem();
			advancedLevelItem.setFont(MENU_FONT);
			advancedLevelItem.setText(Messages
					.getString("Banner.menu.interfaceAdvancedLevel")); //$NON-NLS-1$
			advancedLevelItem
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (advancedLevelItem.isSelected())
								parentFrame
										.setInterfaceLevel(Configuration.LEVEL_ADVANCED);
						}
					});
		}
		return advancedLevelItem;
	}

	/**
	 * Sets the level of display to basic
	 * 
	 */
	public void setBasicLevel() {
		basicLevelItem.setSelected(true);
	}

	/**
	 * Sets the level of display to advanced
	 * 
	 */
	public void setAdvancedLevel() {
		advancedLevelItem.setSelected(true);
	}

	/**
	 * This method initializes separator5
	 * 
	 * @return javax.swing.JSeparator
	 */
	private JSeparator getSeparator5() {
		if (separator5 == null) {
			separator5 = new JSeparator();
		}
		return separator5;
	}

	/**
	 * This method initializes separator5
	 * 
	 * @return javax.swing.JSeparator
	 */
	private JSeparator getSeparator6() {
		if (separator6 == null) {
			separator6 = new JSeparator();
		}
		return separator6;
	}

	/**
	 * This method initializes undoItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getUndoItem() {
       if (undoItem == null) {
            undoItem = new JMenuItem(parentFrame.getUndoAction());
            undoItem.setFont(MENU_FONT);
        }
		return undoItem;
	}

	/**
	 * This method initializes redoItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getRedoItem() {
		if (redoItem == null) {
			redoItem = new JMenuItem(parentFrame.getRedoAction());
			redoItem.setFont(MENU_FONT);
		}
		return redoItem;
	}

	/**
	 * This method initializes separator6
	 * 
	 * @return javax.swing.JSeparator
	 */
	private JSeparator getSeparator4() {
		if (separator4 == null) {
			separator4 = new JSeparator();
		}
		return separator4;
	}

	private class ResizeBanner extends ComponentAdapter {

		@Override
		public void componentResized(ComponentEvent e) {
			repaint();
		}
	}
}
