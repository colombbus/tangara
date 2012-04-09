package org.colombbus.helpgenerator.model;

public class Css extends AbstractRemoteResource {

	private String media;

	public Css(String remoteUrl) {
		super(remoteUrl);
	}

	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	@Override
	protected String computeLocalUrl() {
		String externalLink = getRemoteUrl();
		if (externalLink.startsWith("?s=")) { //$NON-NLS-1$
			// /wiki/lib/exe/css.php?s=all&amp;t=tangara&amp;tseed=1331738090
			// /wiki/lib/exe/css.php?s=print&amp;t=tangara&amp;tseed=1331738090
			int startPos = externalLink.indexOf("css.php?s="); //$NON-NLS-1$
			int endPos = externalLink.lastIndexOf("&amp;t=tangara&amp;tseed="); //$NON-NLS-1$
			return externalLink.substring(startPos, endPos) + "_tangara.css"; //$NON-NLS-1$
		} else {
			// /wiki/lib/exe/css.php?t=tangara&amp;tseed=1331738090
			return "default_tangara.css"; //$NON-NLS-1$
		}
	}
}
