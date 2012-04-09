package org.colombbus.helpgenerator.net;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class WebDownloaderImpl implements WebDownloader {

	private String baseUrl;
	private HttpClient httpclient = new DefaultHttpClient();

	public WebDownloaderImpl() {
	}

	public WebDownloaderImpl(String baseUrl) {
		setBaseUrl(baseUrl);
	}

	@Override
	public void setBaseUrl(String baseUrl) {
		Validate.notNull(baseUrl);

		this.baseUrl = baseUrl;
		if (this.baseUrl.endsWith("/") == false) //$NON-NLS-1$
			this.baseUrl += "/"; //$NON-NLS-1$
	}

	@Override
	public byte[] download(String pagePath) {
		HttpGet httpget = createGetRequest(pagePath);
		try {

			HttpResponse response = httpclient.execute(httpget);
			return responseAsBinary(response);

		} catch (IOException ioEx) {
			throw new RuntimeException("Fail to read " + httpget.getURI(), ioEx); //$NON-NLS-1$
		}
	}

	private HttpGet createGetRequest(String pagePath) {
		Validate.validState(baseUrl!=null,"baseUrl not set"); //$NON-NLS-1$
		Validate.notNull(pagePath,"pagePath argument is null"); //$NON-NLS-1$

		String fullUrl = baseUrl + pagePath;
		return new HttpGet(fullUrl);
	}

	private static byte[] responseAsBinary(HttpResponse response) throws IOException {
		InputStream in = null;
		try {

			HttpEntity entity = response.getEntity();
			in = entity.getContent();
			return IOUtils.toByteArray(in);

		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
