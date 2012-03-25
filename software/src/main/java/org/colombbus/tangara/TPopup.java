package org.colombbus.tangara;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class TPopup extends JWindow {

	protected JList list;
	protected PopupManager manager;
	protected DefaultListModel model = new DefaultListModel();
	protected int lastKeyCode;
	protected String currentTyping = "";

	public TPopup(Frame owner, PopupManager manager) {
		super(owner);
		this.manager = manager;
		list = new JList(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.setCellRenderer(new DefaultListCellRenderer());
		list.addKeyListener(new PopupKeyListener());
		list.addMouseListener(new PopupMouseListener());

		JPanel content = new JPanel(new BorderLayout());
		JScrollPane scroller = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(scroller, BorderLayout.CENTER);
		setContentPane(content);
		list.addFocusListener(new PopupFocusListener());

	}

	public TPopup(Frame owner, PopupManager manager, Point location) {
		this(owner, manager);
		if (location != null) {
			setLocation(location);
		}
	}

	public void select(int index) {
		list.setSelectedIndex(index);
		list.ensureIndexIsVisible(index);
	}

	public void addValue(String value) {
		model.addElement(value);
	}

	@Override
	public void setVisible(boolean value) {
		if (value) {
			list.setVisibleRowCount(Math.min(model.size(), 10));
			pack();
			super.setVisible(value);
			toFront();
			if (model.size() > 0)
				select(0);
			list.requestFocus();
		} else {
			super.setVisible(value);
		}
	}

	private class PopupFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			TPopup.this.setVisible(false);
			TPopup.this.dispose();
		}
	}

	protected void close(boolean valid) {
		if (valid)
			manager.closePopup((String) list.getSelectedValue());
		else
			manager.closePopup(currentTyping, lastKeyCode);
	}

	private class PopupMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			close(true);
		}
	}

	public int getLineHeight() {
		return (list.getFontMetrics(list.getFont()).getHeight());
	}

	public int getVisibleRowCount() {
		return list.getVisibleRowCount();
	}

	private class PopupKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent evt) {
			lastKeyCode = evt.getKeyCode();
			if (lastKeyCode == KeyEvent.VK_UP || lastKeyCode == KeyEvent.VK_DOWN) {
				currentTyping = "";
				int size = model.size();
				if (size > 0) {
					if (lastKeyCode == KeyEvent.VK_UP && list.getSelectedIndex() == 0) {
						select(size - 1);
						evt.consume();
					} else if (lastKeyCode == KeyEvent.VK_DOWN && list.getSelectedIndex() == size - 1) {
						select(0);
						evt.consume();
					}
				}
			} else if ((lastKeyCode == KeyEvent.VK_ENTER) || (lastKeyCode == KeyEvent.VK_SPACE)) {
				close(true);
			} else if (evt.isActionKey()) {
				close(false);
			}
		}

		@Override
		public void keyTyped(KeyEvent evt) {
			char character = evt.getKeyChar();
			if (Character.isLetterOrDigit(character)) {
				String newTyping = currentTyping + character;

				for (int i = 0; i < model.getSize(); i++) {
					String value = (String) model.getElementAt(i);
					if (value.startsWith(newTyping)) {
						select(i);
						currentTyping = newTyping;
						return;
					}
				}
			}
			close(false);
		}
	}

}
