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

package org.colombbus.tangara.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.colombbus.tangara.Configuration;

/**
 * @author gwen
 * 
 */
public class FileReceptionDialog {

	private String sourceFilename;

	private byte[] sourceFileContent;

	private JFrame owner;

	private String message;

	private static Logger LOG = Logger.getLogger(FileReceptionDialog.class);

	/**
	 * @param owner
	 * @param contextMsg
	 */
	public FileReceptionDialog(JFrame owner, String contextMsg,
			String filename, byte[] content) {
		this.owner = owner;
		this.message = contextMsg;
		this.sourceFilename = filename;
		this.sourceFileContent = content;
	}

	/**
	 * This method initializes this
	 * 
	 */
	public void setVisible() {
		final String[] options = {
				Messages.getString("FileReceptionDialog.saveButton"),
				Messages.getString("FileReceptionDialog.cancelButton") };
		final String title = Messages.getString("FileReceptionDialog.title");
		final int optionType = JOptionPane.OK_CANCEL_OPTION;
		final int messageType = JOptionPane.QUESTION_MESSAGE;
		final Icon icon = null;
		
		int choosenOption = JOptionPane.showOptionDialog(owner, message, title,
				optionType, messageType, icon, options, options[0]);
		if (choosenOption == JOptionPane.OK_OPTION)
			setTargetFile();
	}

	private void setTargetFile() {
		File targetDir = Configuration.instance().getUserHome();
		JFileChooser chooserDlg = new JFileChooser(targetDir);

		String filterMsg = Messages
				.getString("FileReceptionDialog.filter.allFiles");
		SimpleFileFilter filter = new SimpleFileFilter(filterMsg);
		chooserDlg.setFileFilter(filter);

		File originalSelFile = new File(targetDir, sourceFilename);
		File curSelFile = originalSelFile;
		boolean showDialog = true;
		while (showDialog) {
			chooserDlg.setSelectedFile(curSelFile);
			int userAction = chooserDlg.showSaveDialog(owner);
			curSelFile = chooserDlg.getSelectedFile();
			switch (userAction) {
			case JFileChooser.CANCEL_OPTION:
				showDialog = false;
				break;
			case JFileChooser.APPROVE_OPTION:
				if (curSelFile.exists()) {
					String title = Messages
							.getString("FileReceptionDialog.overwrite.title");
					String msgPattern = Messages
							.getString("FileReceptionDialog.overwrite.message");
					String overwriteMsg = MessageFormat.format(msgPattern,
							curSelFile.getName());
					Object[] options = {
							Messages.getString("FileReceptionDialog.yes"),
							Messages.getString("FileReceptionDialog.no") };
					int userChoice = JOptionPane.showOptionDialog(owner,
							overwriteMsg, title, JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, // do not use a
																// custom Icon
							options, // the titles of buttons
							options[0]);
					if (userChoice == JOptionPane.OK_OPTION) {
						if (saveFileAs(curSelFile)) {
							showDialog = false;
						}
					}
				} else if (saveFileAs(curSelFile)) {
					showDialog = false;
				}
				break;
			case JFileChooser.ERROR_OPTION:
				LOG.error("Error in file chooser dialog");
				// TODO what to do in case of error ? Retry ?
				showDialog = false;
				break;
			}
		}
	}

	private boolean saveFileAs(File targetFile) {
		try {
			createNewFile(targetFile);
			writeFile(targetFile);
			return true;
		} catch (IOException ioEx) {
			return false;
		}
	}
	
	private void createNewFile( File targetFile) throws IOException {
		try {
			targetFile.createNewFile();
		} catch (IOException ioEx) {
			failToCreateNewFile(targetFile, ioEx);
			throw ioEx;
		}		
	}

	private void failToCreateNewFile(File targetFile, IOException ioEx) {
		LOG.error("Failed to create target file "
				+ targetFile.getAbsolutePath(), ioEx);

		File errorPath = null;
		if (targetFile.exists() && targetFile.canWrite() == false) {
			errorPath = targetFile;
		} else if (targetFile.getParentFile().canWrite() == false) {
			errorPath = targetFile.getParentFile();
		}

		String message = null;
		if (errorPath != null) {
			String msgPattern = Messages
					.getString("FileReceptionDialog.error.noWriteAccess");
			message = MessageFormat.format(msgPattern, errorPath
					.getAbsolutePath());
		} else {// undefined error
			message = Messages
					.getString("FileReceptionDialog.error.undefined");
		}

		String title = Messages.getString("FileReceptionDialog.error.title");

		int messageType = JOptionPane.WARNING_MESSAGE;
		JOptionPane.showMessageDialog(owner, message, title, messageType);
	}
	
	private void writeFile( File targetFile) throws IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(targetFile);
			out.write(sourceFileContent);
		} catch (IOException ioEx) {
			failToWriteFile(targetFile, ioEx);
			throw ioEx;
		} finally {
			IOUtils.closeQuietly(out);
		}

	}

	private void failToWriteFile(File targetFile, IOException ioEx) {
		LOG.error("Fail to fill file " + targetFile.getAbsolutePath(), ioEx);
		String title = Messages.getString("FileReceptionDialog.error.title");
		String message = null;
		if (targetFile.canWrite()) {
			String msgPattern = Messages
					.getString("FileReceptionDialog.error.writeFailed");
			message = MessageFormat.format(msgPattern, targetFile
					.getAbsolutePath());
		} else {
			String msgPattern = Messages
					.getString("FileReceptionDialog.error.noWriteAccess");
			message = MessageFormat.format(msgPattern, targetFile
					.getAbsolutePath());
		}
		int messageType = JOptionPane.WARNING_MESSAGE;
		JOptionPane.showMessageDialog(owner, message, title, messageType);
	}


}
