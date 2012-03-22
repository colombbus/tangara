package org.colombbus.helpgenerator.model;

public enum Language {

	FR, ES;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
