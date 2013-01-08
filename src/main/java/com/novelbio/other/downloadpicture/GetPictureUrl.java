package com.novelbio.other.downloadpicture;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.HttpFetch;

/** 给定某个url，用该类来获得该mid url下属所有big picture的url */
public abstract class GetPictureUrl implements Callable<GetPictureUrl> {
	/** null说明失败 */
	public abstract ArrayList<UrlPictureDownLoad> getLsResult();
	
	public boolean isSuccess() {
		if (getLsResult() == null) {
			return false;
		}
		return true;
	}
	
	public abstract void setWebFetch(HttpFetch webFetch);
	
}
