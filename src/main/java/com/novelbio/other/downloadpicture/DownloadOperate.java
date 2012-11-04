package com.novelbio.other.downloadpicture;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.database.model.species.Species;
import com.novelbio.other.downloadpicture.donmai.DonmaiOperate;
import com.novelbio.other.downloadpicture.pixiv.PixivGetPathExistPic;
import com.novelbio.other.downloadpicture.pixiv.PixivGetPictureUrlToDownload;
import com.novelbio.other.downloadpicture.pixiv.PixivOperate;

public abstract class DownloadOperate {
	private static Logger logger = Logger.getLogger(DownloadOperate.class);
	
	public static void main(String[] args) {
//		PixivOperate pixivOperate = new PixivOperate();
//		pixivOperate.getcookies();
//		Set<String> setUrl = new LinkedHashSet<String>();
//		TxtReadandWrite txtReadUrl = new TxtReadandWrite("/home/zong0jie/ͼƬ/My Pictures/picture/pixivurl.txt", false);
//		for (String urlID : txtReadUrl.readlines()) {
//			urlID = urlID.trim();
//			if (urlID.equals("")) {
//				continue;
//			}
//			setUrl.add(urlID);
//		}
//		for (String urlID : setUrl) {			
//			pixivOperate.setUrlAuther(urlID);
//			pixivOperate.setSavePath("/home/zong0jie/ͼƬ/My Pictures/picture/pixivTest");
//			pixivOperate.running();
//			Thread.sleep(100);
//			logger.error("finished url:" + urlID);
//		}
		
		
//		PixivOperate pixivOperate = new PixivOperate();
//		pixivOperate.getcookies();
//		pixivOperate.setUrlAuther("3193378");
//		pixivOperate.setSavePath("/home/zong0jie/ͼƬ/My Pictures/picture/pixivTest");
//		pixivOperate.run();
		
		
		DonmaiOperate donmaiOperate = new DonmaiOperate();
		donmaiOperate.getcookies();
		donmaiOperate.setUrlAuther("gangbang");
		donmaiOperate.setSavePath("/home/zong0jie/ͼƬ/My Pictures/picture/donmai");
		donmaiOperate.run();
		
	}
	
	protected WebFetch webFetch;
	protected String urlAuther;
	protected String autherName;
	protected String autherID;
	
	protected String savePath;
	protected int retryGetPageNum = 10;
	
	/** �������ж���ͼƬ */
	protected int allPictureNum = 0;
	/** �ܹ���ҳ */
	protected int allPages = 0;
	
	protected PixivGetPathExistPic pixivGetPathExistPic;
	
	/** ���ĳ����վ��cookies */
    protected abstract void getcookies();
    
    public void setWebFetchPixiv(WebFetch webFetchPixiv) {
		this.webFetch = webFetchPixiv;
	}
    public WebFetch getWebFetchPixiv() {
		return webFetch;
	}
    /** @param urlAuther ��id 
     * @return */
	public abstract void setUrlAuther(String urlAutherid);
	
	public void setSavePath(String savePath) {
		this.savePath = savePath.trim();
	}
	public void run() {
		try {
			running();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void running() throws InterruptedException, ExecutionException {
		if(!setPictureNum_And_PageNum_Auther_And_PixivGetPath()) {
			return;
		}
		ArrayList<? extends GetPictureUrl> lsPrepareDownloads = getLsPrepareDownload();
		ArrayList<UrlPictureDownLoad> lsDownLoads = new ArrayList<UrlPictureDownLoad>();
		logger.info("��ô�����ȫ��midurl����");
		//�ȴ�Ҫ�������url������
		ThreadPoolExecutor executorGetUrlPrepToDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
		//�ȴ����ص���
		ThreadPoolExecutor executorDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));

		LinkedList<Future<GetPictureUrl>> lsUrlPrepToDownLoad = new LinkedList<Future<GetPictureUrl>>();
		LinkedList<Future<UrlPictureDownLoad>> lsFutureDownLoad = new LinkedList<Future<UrlPictureDownLoad>>();

		for (GetPictureUrl pixivGetPictureUrlToDownload : lsPrepareDownloads) {
			Future<GetPictureUrl> result = executorGetUrlPrepToDownload.submit(pixivGetPictureUrlToDownload);
			lsUrlPrepToDownLoad.add(result);
		}
		//��executorGetUrlPrepToDownload�м����������ֱ�����
		while (executorGetUrlPrepToDownload.getActiveCount() > 0 || lsUrlPrepToDownLoad.size() > 0) {
			Future<GetPictureUrl> futureToDownload = lsUrlPrepToDownLoad.poll();
			if (futureToDownload.isDone()) {
				GetPictureUrl pictureUrlToDownload = futureToDownload.get();
				//ʧ���˾ͷŻ�ȥ����ִ��
				if (pictureUrlToDownload.getLsResult() == null) {
					Future<GetPictureUrl> result = executorGetUrlPrepToDownload.submit(pictureUrlToDownload);
					lsUrlPrepToDownLoad.add(result);
				} else {//�ɹ��˾�ȥ����
					lsDownLoads.addAll(pictureUrlToDownload.getLsResult());
				}
			} else {//ûִ�гɹ��ͽ���ִ��
				lsUrlPrepToDownLoad.add(futureToDownload);
			}
			Thread.sleep(100);
		}
		
		for (UrlPictureDownLoad pixivUrlDownLoad : lsDownLoads) {
			Future<UrlPictureDownLoad> futureDownload = executorDownload.submit(pixivUrlDownLoad);
			lsFutureDownLoad.add(futureDownload);
		}
		logger.info("��ô�����ȫ��bigurl����");
		//��executorDownload�м����������ֱ�����
		while (executorDownload.getActiveCount() > 0 || lsFutureDownLoad.size() > 0) {
			Future<UrlPictureDownLoad> futureDownload = lsFutureDownLoad.poll();
			if (futureDownload.isDone()) {
				UrlPictureDownLoad pixivUrlDownLoad = futureDownload.get();
				//ʧ���˾ͷŻ�ȥ����ִ��
				if (!pixivUrlDownLoad.isSaveSucess()) {
					Future<UrlPictureDownLoad> result = executorDownload.submit(pixivUrlDownLoad);
					lsFutureDownLoad.add(result);
				}
			} else {//ûִ�гɹ��ͽ���ִ��
				lsFutureDownLoad.add(futureDownload);
			}
			Thread.sleep(100);
		}
		
		executorDownload.shutdown();
		executorGetUrlPrepToDownload.shutdown();
		
		webFetch.close();
		
		return;
	}
	 protected abstract boolean setPictureNum_And_PageNum_Auther_And_PixivGetPath();
	/** ����һϵ�п��Ի�������������ӵĶ���
	 * ʵ������Ϊ������ֱ�ӻ��������ص�url�����Զ�Ҫ����������ת����ô����м���ת���̶�����д�ڸ÷�������
	 */
	protected abstract ArrayList<? extends GetPictureUrl> getLsPrepareDownload();
	
}
