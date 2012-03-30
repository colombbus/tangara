package org.colombbus.objectpackager;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;

class JarEntryDesc {

	public static JarEntryDesc createDirectoryEntry(String name) {
		return new JarEntryDesc(name, null);
	}

	public static JarEntryDesc createFileEntry(String name, File file) {
		return new JarEntryDesc(name, file);
	}

	private String name;
	private File file;

	private JarEntryDesc(String name, File file) {
		this.name = name;
		this.file = file;
	}

	public void writeTo(JarOutputStream out) throws IOException {
		JarEntry entry = new JarEntry(name);
		entry.setMethod(ZipEntry.DEFLATED);
		out.putNextEntry(entry);

		if( file != null)
			writeFile(out);

		out.closeEntry();
	}

	private void writeFile( JarOutputStream out ) throws IOException {
		byte[] fileContent = FileUtils.readFileToByteArray(file);
		out.write(fileContent);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JarEntryDesc other = (JarEntryDesc) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
