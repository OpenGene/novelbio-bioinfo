package com.novelbio.base.multithread.txtreadcopewrite;

import com.novelbio.base.RunGetInfo;

public interface MTmulitCopeInfo<T> extends RunGetInfo<T> {
	/** 将read对象保存起来，如<br>
	 * this.fastQRead = fastQRead; */
	public void setReadInfo(MTOneThreadReadFile mtOneThreadReadFile);
	
	public void setCollectionRecords () {
		
	}
}
