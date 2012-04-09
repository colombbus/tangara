package org.colombbus.helpengine;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentTypeDictionary {

	private static final Logger LOG = LoggerFactory.getLogger(ContentTypeDictionary.class);
	private static final String TYPE_MAP_PATH = "content-type.properties"; //$NON-NLS-1$

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
			LOG.warn(msg,th);
			throw new RuntimeException(msg, th);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public String getContentType(String uri) {
		String extension = FilenameUtils.getExtension(uri);
		String contentType = props.getProperty(extension);
		return contentType == null ?  "application/binary" : contentType; //$NON-NLS-1$
	}

}
