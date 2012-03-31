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

package org.colombbus.tangara.objects.character;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;

/**
 *
 * @author benoit
 */
class JARUtils
{
    private JARUtils() {
	}

    public static File extractFileFromJar(URL jarPath, String entryName) throws Exception
    {
        JarInputStream jarFile = new JarInputStream(jarPath.openStream());
        JarEntry jarEntry = findJarEntry(entryName, jarFile);
        if (jarEntry==null)
        {
        	String msg = String
					.format(
							"The JAR entry name %s cannot be found in the JAR file \"%s\" ",
							entryName, jarPath.toString());
			LOG.error(msg);
			throw new Exception(msg);
        }

        int pointPosition = entryName.lastIndexOf(".");
        if (pointPosition<0)
        {
        	String msg = String
					.format(
							"The entry name %s of the JAR file \"%s\" should contain a dot character",
							entryName, jarPath.toString());
			LOG.error(msg);
			throw new Exception(msg);
        }
        String extension = entryName.substring(pointPosition);
        File tmpFile = File.createTempFile("skeleton",extension);
        tmpFile.deleteOnExit();
        FileOutputStream tmpFileOut = new FileOutputStream(tmpFile);
		copyStream(jarFile, tmpFileOut);
        tmpFileOut.close();
        return tmpFile;
    }

	private static JarEntry findJarEntry(String entryName,
			JarInputStream jarFile) throws IOException {
		JarEntry jarEntry = jarFile.getNextJarEntry();
        while(jarEntry!=null)
        {
            if (jarEntry.getName().equals(entryName))
            {
                break;
            }
            else
            {
                jarEntry = jarFile.getNextJarEntry();
            }
        }
		return jarEntry;
	}

    public static void addJAREntry(JarOutputStream jarOut,String entryName, File sourceFile) throws Exception
    {
        try
        {
            JarEntry entry = new JarEntry(entryName);
            entry.setSize(sourceFile.length());
            entry.setMethod(ZipEntry.DEFLATED);
            jarOut.putNextEntry(entry);
            FileInputStream sourceFileIn = new FileInputStream(sourceFile);
			copyStream(sourceFileIn, jarOut );
            jarOut.closeEntry();
        }
        catch (IOException e)
        {
        	String msg = "Access error while saving JAR file";
        	LOG.warn(msg,e);
            throw new Exception(msg,e);
        }
    }

	private static void copyStream(InputStream in,OutputStream out
			) throws IOException {
		BufferedInputStream source = new BufferedInputStream(in);
		int read;
		byte[] buffer = new byte[4096];
		while ((read = source.read(buffer)) != -1)
		{
			out.write(buffer, 0, read);
		}
	}

    /** Class logger */
    private static Logger LOG = Logger.getLogger(JARUtils.class);
}
