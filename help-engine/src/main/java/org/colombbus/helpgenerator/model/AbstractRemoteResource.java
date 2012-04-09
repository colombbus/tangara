package org.colombbus.helpgenerator.model;



public abstract class AbstractRemoteResource implements RemoteResource {

	private byte[] content;
	private String remoteUrl;
	private String localUrl;

	public AbstractRemoteResource(String remoteUrl) {
		this.remoteUrl = remoteUrl;
		this.localUrl = computeLocalUrl();
	}

	protected abstract String computeLocalUrl();

	@Override
	public String getLocalUrl() {
		return localUrl;
	}

	@Override
	public String getRemoteUrl() {
		return remoteUrl;
	}

	@Override
	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public byte[] getContent() {
		return content;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((remoteUrl == null) ? 0 : remoteUrl.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractRemoteResource other = (AbstractRemoteResource) obj;
		if (remoteUrl == null) {
			if (other.remoteUrl != null)
				return false;
		} else if (!remoteUrl.equals(other.remoteUrl))
			return false;
		return true;
	}

}
