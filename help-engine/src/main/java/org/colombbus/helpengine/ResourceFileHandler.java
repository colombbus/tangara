package org.colombbus.helpengine;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.io.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.*;

public class ResourceFileHandler extends AbstractHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceFileHandler.class);
	private static final String HTML_BASE_PATH = "/html/";//$NON-NLS-1$
	private final ContentTypeDictionary contentTypeDictionary = new ContentTypeDictionary();

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException {

		if (isHtmlResourcePath(target))
			handleHtmlFile(target, response);
		else if( isCssResourcePath(target))
			handleCssFile(target,response);
		else
			binaryFile(target, response);

		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}

	private static boolean isHtmlResourcePath(String path) {
		return path.toLowerCase().endsWith(".html");//$NON-NLS-1$
	}

	private static void handleHtmlFile(String resource, HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=utf-8"); //$NON-NLS-1$
		writeResponse(resource, response);
		LOG.trace("html fetch {}", resource); //$NON-NLS-1$
	}

	private static void writeResponse( String resource, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		String pageContent = getTextFile(resource);
		writer.println(pageContent);
	}

	private static String getTextFile(String path) {
		InputStream in=null;
		try {

			in = openStream(path);
			return IOUtils.toString(in);

		} catch (IOException ioEx) {
			LOG.warn("Text resource {} not found", path);//$NON-NLS-1$
			return errorPage();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private static InputStream openStream(String resource) throws IOException {
		String url = (HTML_BASE_PATH + resource).replace("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		InputStream in = ResourceFileHandler.class.getResourceAsStream(url);
		if (in == null) {
			String msg = String.format("Resource '%s' not found", url);//$NON-NLS-1$
			LOG.warn(msg);
			throw new IOException(msg);
		}
		return in;
	}

	private static String errorPage() {
		InputStream in=null;
		try {

			in = openStream("error.html"); //$NON-NLS-1$
			return IOUtils.toString(in);

		} catch (IOException ioEx) {
			LOG.error("Cannot find error page"); //$NON-NLS-1$
			return "<h1>Error page not found</h1>"; //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private static boolean isCssResourcePath(String path) {
		return path.toLowerCase().endsWith(".css"); //$NON-NLS-1$
	}

	private static void handleCssFile(String target, HttpServletResponse response) throws IOException {
		response.setContentType("text/css;charset=utf-8");//$NON-NLS-1$
		writeResponse(target, response);
		LOG.trace("fetch css {}",target);//$NON-NLS-1$
	}


	private void binaryFile(String target, HttpServletResponse response) throws IOException {
		String contentType = contentTypeDictionary.getContentType(target);
		response.setContentType(contentType);

		byte[] content = loadBinaryResource(target);
		ServletOutputStream output = response.getOutputStream();
		output.write(content);
		LOG.trace("fetch binary {}",target); //$NON-NLS-1$
	}

	private static byte[] loadBinaryResource(String resource) {
		InputStream in = null;
		try {

			in = openStream(resource);
			return IOUtils.toByteArray(in);

		} catch (IOException ioEx) {
			LOG.warn("Binary resource {} not found", resource);//$NON-NLS-1$
			return new byte[0];
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
