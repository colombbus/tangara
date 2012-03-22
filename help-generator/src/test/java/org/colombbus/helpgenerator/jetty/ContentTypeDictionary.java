package org.colombbus.helpgenerator.jetty;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

class ContentTypeDictionary {

	private static final String TYPE_MAP_PATH = "content-type.properties";
	private Properties props = new Properties();

	public ContentTypeDictionary() {
		loadDictionaryResource();
	}

	private void loadDictionaryResource() {
		InputStream in = null;
		try {

			in = ContentTypeDictionary.class.getResourceAsStream(TYPE_MAP_PATH);
			props.load(in);

		} catch (Throwable th) {
			String msg = String.format("Resource %s cannot be found", TYPE_MAP_PATH); //$NON-NLS-1$
			throw new RuntimeException(msg, th);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public String getContentType(String uri) {
		String extension = FilenameUtils.getExtension(uri);

		String contentType = props.getProperty(extension);
		return contentType == null ?  "application/binary" : contentType;
	}

}
