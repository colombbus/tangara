package org.colombbus.helpgenerator.model;

public interface RemoteResource {

	String getRemoteUrl();

	String getLocalUrl();

	void setContent(byte[] content);

	byte[] getContent();

}
