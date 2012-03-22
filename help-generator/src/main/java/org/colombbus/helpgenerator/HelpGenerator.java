package org.colombbus.helpgenerator;

import java.io.*;

import org.apache.commons.io.*;
import org.apache.commons.lang3.Validate;
import org.colombbus.helpgenerator.model.*;
import org.colombbus.helpgenerator.net.*;
import org.slf4j.*;

public class HelpGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(HelpGenerator.class);
	private static final String BASE_URL = "http://tangara.colombbus.org/"; //$NON-NLS-1$
	private HtmlPageGenerator pageGenerator = new HtmlPageGeneratorImpl();
	private WebDownloader downloader = new WebDownloaderImpl(BASE_URL);
	private SiteDumper siteCollector = new SiteDumper();
	private File generationPath;

	public void setGenerationPath(File generationPath) {
		validateWriteableDirectory(generationPath);

		this.generationPath = generationPath;
	}

	private static void validateWriteableDirectory(File directory) {
		Validate.notNull(directory);
		Validate.isTrue(directory.exists());
		Validate.isTrue(directory.isDirectory());
		Validate.isTrue(directory.canExecute());
		Validate.isTrue(directory.canRead());
		Validate.isTrue(directory.canWrite());
	}

	public void run() throws IOException {
		verifyGenerationPath();

		collectSite();
		generateLocalHtmlFiles();
		generateLocalResourceFiles();
		copyPreBuiltResources();
		LOG.info("Generation done"); //$NON-NLS-1$
	}

	private void verifyGenerationPath() {
		Validate.validState(generationPath != null, "generationPath not set"); //$NON-NLS-1$
	}

	private void collectSite() {
		siteCollector.setDownloader(downloader);

		siteCollector.collect();
		int pageCount = siteCollector.getPageCount();
		LOG.info("Page count: " + pageCount); //$NON-NLS-1$
	}

	private void generateLocalHtmlFiles() throws IOException {
		Toc toc = siteCollector.getToc();
		this.pageGenerator.setToc(toc);

		for (Page page : siteCollector.getAllPages()) {
			writePage(page);
		}
	}

	private void writePage(Page page) throws IOException {
		String filename = page.getFilename();
		File htmlFile = new File(generationPath, filename);
		String htmlContent = this.pageGenerator.generatePage(page);

		try {

			FileUtils.write(htmlFile, htmlContent);

		} catch (IOException ioEx) {
			LOG.error("Fail to generate page " + filename, ioEx); //$NON-NLS-1$
			throw ioEx;
		}
	}

	private void generateLocalResourceFiles() {
		for (RemoteResource resource : siteCollector.getRemoteResources()) {
			generateLocalResource(resource);
		}
	}

	private void generateLocalResource(RemoteResource resource) {
		String filename = resource.getLocalUrl();
		File resourceFile = new File(generationPath, filename);
		LOG.info("writing local resource " + filename + " to " + resourceFile.getPath()); //$NON-NLS-1$ //$NON-NLS-2$

		try {

			byte[] content = resource.getContent();
			FileUtils.writeByteArrayToFile(resourceFile, content);

		} catch (IOException ioEx) {
			LOG.error("Fail to write resource " + resourceFile.getPath(), ioEx); //$NON-NLS-1$
		}
	}

	private void copyPreBuiltResources() {
		exportResourceAsFile("main.css"); //$NON-NLS-1$
	}

	private void exportResourceAsFile(String resourceName) {
		InputStream in = null;
		try {
			in = HelpGenerator.class.getResourceAsStream("/html/" + resourceName); //$NON-NLS-1$
			File file = new File(generationPath, resourceName);
			FileUtils.copyInputStreamToFile(in, file);
		} catch (IOException ioEx) {
			LOG.error("Fail to export resource " + resourceName);//$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public static void main(String[] args) {
		File target = null;
		if (args.length == 0) {
			target = new File(".");
		} else if (args.length == 1) {
			target = new File(args[0]);
		} else {
			System.err.println("Invalid argument count");
			printUsage();
			return;
		}

		try {
			HelpGenerator generator = new HelpGenerator();
			generator.setGenerationPath(target);
			generator.run();
		} catch (Exception ex) {
			System.err.println("Error during help generation "+ex.getMessage());
			printUsage();
		}
	}

	private static final void printUsage() {
		System.out.println("Usage: <classname> <target-directory>");
	}

}
