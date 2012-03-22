package org.colombbus.helpgenerator.jetty;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.colombbus.helpgenerator.HelpGenerator;
import org.eclipse.jetty.server.Server;

public class ServerSample {

	private static final File HTML_BASE_PATH = new File("temp/html"); //$NON-NLS-1$

	public static void main(String[] args) {
		try {

			initHtmlBasePath();
			generateFiles();
			launchServer();

		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	private static void initHtmlBasePath() throws IOException {
		if( HTML_BASE_PATH.exists())
			FileUtils.cleanDirectory(HTML_BASE_PATH);
		else
			HTML_BASE_PATH.mkdirs();
	}

	private static void generateFiles() throws Exception {
		HelpGenerator generator = new HelpGenerator();
		generator.setGenerationPath(HTML_BASE_PATH);
		generator.run();
	}

	private static void launchServer() throws Exception {
		Server server = new Server(8080);
		server.setHandler(new LocalFileHandler());

		server.start();
		server.join();
	}
}
