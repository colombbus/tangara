package org.colombbus.objectpackager;

import java.util.HashMap;
import java.util.Map;

class TObjectMetadata {
	private Map<Language, String> i18nObjectNames = new HashMap<Language, String>();
	private String packageName;

	public String getI18NObjectName(Language lang) {
		return i18nObjectNames.get(lang);
	}

	public void setI18NObjectName(Language lang, String objectName) {
		this.i18nObjectNames.put(lang, objectName);
	}

	public String getDefaultObjectName() {
		return this.i18nObjectNames.get(Language.ENGLISH);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packagename) {
		this.packageName = packagename;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((i18nObjectNames == null) ? 0 : i18nObjectNames.hashCode());
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
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
		TObjectMetadata other = (TObjectMetadata) obj;
		if (i18nObjectNames == null) {
			if (other.i18nObjectNames != null)
				return false;
		} else if (!i18nObjectNames.equals(other.i18nObjectNames))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}

}
