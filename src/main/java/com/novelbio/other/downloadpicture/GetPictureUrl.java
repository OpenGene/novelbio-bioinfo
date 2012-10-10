package com.novelbio.other.downloadpicture;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.WebFetch;

/** 给定某个url，用该类来获得该mid url下属所有big picture的url */
public interface GetPictureUrl extends Callable<GetPictureUrl> {
	/** null说明失败 */
	public ArrayList<UrlPictureDownLoad> getLsResult();

	void setWebFetch(WebFetch webFetch);
	
}
