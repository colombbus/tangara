package org.colombbus.helpgenerator.jetty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

@SuppressWarnings("nls")
public class LocalFileHandler extends AbstractHandler {

	private static final String HTML_BASE_PATH = "src/test/data/html/";
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
		return path.toLowerCase().endsWith(".html");
	}

	private static void handleHtmlFile(String target, HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter writer = response.getWriter();
		String pageContent = getTextFile(target);
		writer.println(pageContent);
		System.out.println(" html fetch "+target);
	}

	private static String getTextFile(String path) {
		try {

			return loadTextFile(path);

		} catch (IOException ioEx) {
			return loadErrorPage();
		}
	}

	private static String loadTextFile(String path) throws IOException {
		File htmlFile = new File(HTML_BASE_PATH + path);
		return FileUtils.readFileToString(htmlFile);
	}

	private static String loadErrorPage() {
		InputStream in = null;
		try {

			in = LocalFileHandler.class.getResourceAsStream("/html/error.html"); //$NON-NLS-1$
			return IOUtils.toString(in);
		} catch (IOException ioEx) {
			System.err.println("Cannot find error page"); //$NON-NLS-1$
			return "<h1>Error page not found</h1>"; //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private static boolean isCssResourcePath(String path) {
		return path.toLowerCase().endsWith(".css");
	}

	private static void handleCssFile(String target, HttpServletResponse response) throws IOException {
		response.setContentType("text/css;charset=utf-8");
		PrintWriter writer = response.getWriter();
		String pageContent = getTextFile(target);
		writer.println(pageContent);
		System.out.println(" css fetch "+target);
	}


	private void binaryFile(String target, HttpServletResponse response) throws IOException {
		String contentType = contentTypeDictionary.getContentType(target);
		response.setContentType(contentType);

		byte[] content = loadBinaryResource(target);
		ServletOutputStream output = response.getOutputStream();
		output.write(content);
		System.out.println(" binary fetch "+target);
	}

	private static byte[] loadBinaryResource(String path) {
		try {
			File htmlFile = new File(HTML_BASE_PATH + path);
			return FileUtils.readFileToByteArray(htmlFile);
		} catch (IOException ioEx) {
			return new byte[0];
		}
	}
}
