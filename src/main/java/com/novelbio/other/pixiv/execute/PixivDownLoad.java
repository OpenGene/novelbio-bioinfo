package com.novelbio.other.pixiv.execute;

import java.util.AbstractQueue;

import com.novelbio.base.dataOperate.WebFetch;

public class PixivDownLoad {
	AbstractQueue<PixivPicture> queuePicture;
	WebFetch webFetchPixivDownload = new WebFetch();
	
	public void setQueuePicture(AbstractQueue<PixivPicture> queuePicture) {
		this.queuePicture = queuePicture;
	}
	public void getPicture() {
		if (queuePicture.isEmpty()) {
			return;
		}
		PixivPicture picture = queuePicture.poll();
		picture
	}
}
