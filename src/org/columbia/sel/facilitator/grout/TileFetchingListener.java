package org.columbia.sel.facilitator.grout;

public interface TileFetchingListener {
	public void onTileDownloaded();
	
	public void onFetchingStart(FetchingStartEvent fse);
	
	public void onFetchingStop();
	
	public void onFetchingComplete();
	
	public void onFetchingProgress(FetchingProgressEvent fpe);
}
