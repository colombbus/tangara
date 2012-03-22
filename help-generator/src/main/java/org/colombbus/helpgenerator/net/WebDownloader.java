package org.colombbus.helpgenerator.net;

public interface WebDownloader {

	void setBaseUrl(String baseUrl);

	byte[] download(String pagePath);

	//TODO add download as text

}