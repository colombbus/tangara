package org.colombbus.helpgenerator;

import java.util.*;

import org.colombbus.helpgenerator.model.*;
import org.colombbus.helpgenerator.net.WebDownloader;

/**
 * Collect the HTML pages on the Tangara web site and build a local site
 */
class SiteDumper {

	private static final String OBJECT_BASE_URL = "/wiki/doku.php?id="; //$NON-NLS-1$
	private static final String INDEX_PAGE_URL = OBJECT_BASE_URL+"objet:les_objets_de_tangara"; //$NON-NLS-1$
	private static final String COMMAND_PAGE_URL=OBJECT_BASE_URL+"logiciel:commandes"; //$NON-NLS-1$

	private List<String> objectIdList = new ArrayList<String>();
	private List<Page> pages = new ArrayList<Page>();
	private Set<Image> imageSet = new HashSet<Image>();
	private Set<Css> cssSet = new HashSet<Css>();
	private WebDownloader downloader ;
	private Toc toc;

	public void setDownloader(WebDownloader downloader) {
		this.downloader = downloader;
	}

	public void collect() {
		analyzeIndexPage();
		dumpObjectPages();
		dumpCommandPages();
		dumpCssFiles();
		dumpImages();
	}

	private void analyzeIndexPage() {
		byte[] remoteContent = downloader.download(INDEX_PAGE_URL);
		String remoteHtml = new String(remoteContent);

		IndexPageAnalyzer analyzer = new IndexPageAnalyzer();
		analyzer.setRemoteHtml(remoteHtml);
		analyzer.analyze();
		objectIdList = analyzer.getObjectIdList();
		toc = analyzer.getToc();
		imageSet.addAll( analyzer.getImageSet() );
	}

	private void dumpObjectPages() {
		for (String objectId : objectIdList) {
			dumpObjectPage(objectId);
		}
	}

	private void dumpObjectPage(String objectId) {
		String pageUri = OBJECT_BASE_URL + objectId;
		System.out.println("Parsing object page " + objectId + " at " + pageUri); //$NON-NLS-1$ //$NON-NLS-2$

		byte[] binaryContent = downloader.download(pageUri);
		String remoteHtml = new String(binaryContent);

		ObjectPageDumper pageDumper = new ObjectPageDumper();
		pageDumper.setObjectId(objectId);
		pageDumper.setRemoteHtml(remoteHtml);
		pageDumper.buildPage();

		Page page = pageDumper.getPage();
		pages.add(page);

		imageSet.addAll(pageDumper.getImageSet());
		cssSet.addAll(pageDumper.getCssSet());
	}

	private void dumpCommandPages() {
		String pageUri = COMMAND_PAGE_URL;
		System.out.println("Parsing command page at " + pageUri); //$NON-NLS-1$

		byte[] binaryContent = downloader.download(pageUri);
		String remoteHtml = new String(binaryContent);

		CommandPageDumper pageDumper = new CommandPageDumper();
		pageDumper.setPageUrl(pageUri);
		pageDumper.setRemoteHtml(remoteHtml);
		pageDumper.buildPage();

		Page page = pageDumper.getPage();
		pages.add(page);

		imageSet.addAll(pageDumper.getImageSet());
		cssSet.addAll(pageDumper.getCssSet());
	}

	private void dumpCssFiles() {
		dumpRemoteResources(cssSet);
	}

	private void dumpRemoteResources(Set<? extends RemoteResource> remoteResources) {
		for (RemoteResource remoteResource : remoteResources) {
			dumpRemoteResource(remoteResource);
		}
	}

	private void dumpRemoteResource(RemoteResource remoteResource) {
		String url = remoteResource.getRemoteUrl();
		byte[] content = downloader.download(url);
		remoteResource.setContent( content );
	}

	private void dumpImages() {
		dumpRemoteResources(imageSet);
	}

	public int getPageCount() {
		return pages.size();
	}

	public List<Page> getAllPages() {
		return pages;
	}

	public Set<RemoteResource> getRemoteResources() {
		Set<RemoteResource> remoteResources = new HashSet<RemoteResource>();
		remoteResources.addAll(cssSet);
		remoteResources.addAll(imageSet);
		return remoteResources;
	}

	public Toc getToc() {
		return toc;
	}
}
