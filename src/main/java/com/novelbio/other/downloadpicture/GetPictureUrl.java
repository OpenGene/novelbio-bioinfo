package com.novelbio.other.downloadpicture;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.HttpFetch;

/** ����ĳ��url���ø�������ø�mid url��������big picture��url */
public interface GetPictureUrl extends Callable<GetPictureUrl> {
	/** null˵��ʧ�� */
	public ArrayList<UrlPictureDownLoad> getLsResult();

	void setWebFetch(HttpFetch webFetch);
	
}
