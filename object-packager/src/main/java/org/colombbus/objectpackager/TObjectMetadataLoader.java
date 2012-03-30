package org.colombbus.objectpackager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import static org.colombbus.objectpackager.TObjectMetadataFactory.createMetadata;

class TObjectMetadataLoader {

	private List<TObjectMetadata> metadatas = new ArrayList<TObjectMetadata>();

	public void load(File file) {
		if (file == null)
			throw new IllegalArgumentException("null file argument"); //$NON-NLS-1$

		try {
			List<String> descriptions = FileUtils.readLines(file);
			appendDescriptions(descriptions);
		} catch (IOException ioEx) {
			throw new IllegalArgumentException("Fail to read file " + file.getPath(), ioEx); //$NON-NLS-1$
		}
	}

	private void appendDescriptions(List<String> descriptions) {
		for (String description : descriptions) {
			appendDescription(description);
		}
	}

	private void appendDescription(String description) {
		if (description.trim().isEmpty())
			return;

		TObjectMetadata metadata = createMetadata(description);
		if (metadatas.contains(metadata)) {
			String defaultClassname = metadata.getI18NObjectName(Language.ENGLISH);
			String msg = String.format("Metadata of object '%s' defined twice", defaultClassname); //$NON-NLS-1$
			throw new IllegalArgumentException(msg);
		}

		metadatas.add(metadata);
	}

	public List<TObjectMetadata> getAllMetadata() {
		return metadatas;
	}

}
