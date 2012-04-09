package org.colombbus.helpgenerator.model;

public class Image extends AbstractRemoteResource {

	private String cssClass;
	private String align;
	private String alt;

	public Image(String remoteUrl) {
		super(remoteUrl);
	}

	@Override
	protected String computeLocalUrl() {
		String externalUrl = getRemoteUrl();
		int lastDoubleDotPos = externalUrl.lastIndexOf(':');
		int lastEqualPos = externalUrl.lastIndexOf('=');
		int separatorPos = Math.max(lastDoubleDotPos, lastEqualPos);
		if (separatorPos == -1)
			throw new IllegalArgumentException("Separator not found in " + externalUrl); //$NON-NLS-1$
		return externalUrl.substring(separatorPos + 1);
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public String getCssClass() {
		return cssClass;
	}

	public String getAlign() {
		return align;
	}

	public String getAlt() {
		return alt;
	}
}
