package org.columbia.sel.facilitator.grout;

import org.columbia.sel.facilitator.grout.event.FetchingErrorEvent;
import org.columbia.sel.facilitator.grout.event.FetchingProgressEvent;
import org.columbia.sel.facilitator.grout.event.FetchingStartEvent;

public interface TileFetchingListener {
	public void onTileDownloaded();
	
	public void onFetchingStart(FetchingStartEvent fse);
	
	public void onFetchingStop();
	
	public void onFetchingComplete();
	
	public void onFetchingProgress(FetchingProgressEvent fpe);
	
	public void onFetchingError(FetchingErrorEvent fee);
}
