package com.novelbio.other.downloadpicture;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.HttpFetch;

/** ����ĳ��url���ø�������ø�mid url��������big picture��url */
public abstract class GetPictureUrl implements Callable<GetPictureUrl> {
	/** null˵��ʧ�� */
	public abstract ArrayList<UrlPictureDownLoad> getLsResult();
	
	public boolean isSuccess() {
		if (getLsResult() == null) {
			return false;
		}
		return true;
	}
	
	public abstract void setWebFetch(HttpFetch webFetch);
	
}
