package com.novelbio.base.multithread.txtreadcopewrite;

import com.novelbio.base.RunGetInfo;

public interface MTmulitCopeInfo<T> extends RunGetInfo<T> {
	/** ��read���󱣴���������<br>
	 * this.fastQRead = fastQRead; */
	public void setReadInfo(MTOneThreadReadFile mtOneThreadReadFile);
	
	public void setCollectionRecords () {
		
	}
}
