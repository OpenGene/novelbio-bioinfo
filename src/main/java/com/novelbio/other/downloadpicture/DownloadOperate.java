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
import com.novelbio.base.dataOperate.HttpFetch;
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
//		pixivOperate.setUrlAuther("213435");
//		pixivOperate.setSavePath("/home/zong0jie/ͼƬ/My Pictures/picture/pixivTest");
//		pixivOperate.run();
		
		
		DonmaiOperate donmaiOperate = new DonmaiOperate();
		donmaiOperate.getcookies();
		donmaiOperate.setUrlAuther("3d");
		donmaiOperate.setDownloadFast(true);
		donmaiOperate.setSavePath("/home/zong0jie/ͼƬ/My Pictures/picture/donmai");
		donmaiOperate.run();
		
	}
	
	protected HttpFetch webFetch;
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
    
	DownloadPictureFile downloadFile = new DownloadPictureFile();
	
    public void setWebFetchPixiv(HttpFetch webFetchPixiv) {
		this.webFetch = webFetchPixiv;
	}
    public HttpFetch getWebFetchPixiv() {
		return webFetch;
	}
    /** @param urlAuther ��id 
     * @return */
	public abstract void setUrlAuther(String urlAutherid);
	
	public void setSavePath(String savePath) {
		this.savePath = savePath.trim();
	}
    /** ������Ӻ��Ƿ���������
     * ���ǵ�ȫ�����Ӷ�����˲�����
     *  */
	public void setDownloadFast(boolean downloadFast) {
		downloadFile.setDownloadFast(downloadFast);
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
		downloadFile.setLsPrepareDownloads(lsPrepareDownloads);
		downloadFile.running();
		return;
	}
	
	public void closeWebFetch() {
		webFetch.close();
	}
	protected abstract boolean setPictureNum_And_PageNum_Auther_And_PixivGetPath();
	/** ����һϵ�п��Ի�������������ӵĶ���
	 * ʵ������Ϊ������ֱ�ӻ��������ص�url�����Զ�Ҫ����������ת����ô����м���ת���̶�����д�ڸ÷�������
	 */
	protected abstract ArrayList<? extends GetPictureUrl> getLsPrepareDownload();
	
}

/**
 * �õ����ص�url����֮��ʼ��������ͼƬ
 * @author zong0jie
 *
 */
class DownloadPictureFile {
	boolean downloadFast = false;
	
	ArrayList<UrlPictureDownLoad> lsDownLoads = new ArrayList<UrlPictureDownLoad>();
	//�ȴ�Ҫ�������url������
	ThreadPoolExecutor executorGetUrlPrepToDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
	//�ȴ����ص���
	ThreadPoolExecutor executorDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
	
	LinkedList<Future<GetPictureUrl>> lsUrlPrepToDownLoad = new LinkedList<Future<GetPictureUrl>>();
	LinkedList<Future<UrlPictureDownLoad>> lsFutureDownLoad = new LinkedList<Future<UrlPictureDownLoad>>();
	
	/**
	 * ����ͼƬurl����
	 * @param lsPrepareDownloads
	 */
	public void setLsPrepareDownloads(ArrayList<? extends GetPictureUrl> lsPrepareDownloads) {
		for (GetPictureUrl pixivGetPictureUrlToDownload : lsPrepareDownloads) {
			Future<GetPictureUrl> result = executorGetUrlPrepToDownload.submit(pixivGetPictureUrlToDownload);
			lsUrlPrepToDownLoad.add(result);
		}
	}
	
    /** ������Ӻ��Ƿ���������
     * ���ǵ�ȫ�����Ӷ�����˲�����
     *  */
	public void setDownloadFast(boolean downloadFast) {
		this.downloadFast = downloadFast;
	}
	
	public void running() throws InterruptedException, ExecutionException {
		//��executorGetUrlPrepToDownload�м����������ֱ�����
		while (executorGetUrlPrepToDownload.getActiveCount() > 0 || lsUrlPrepToDownLoad.size() > 0) {
			Future<GetPictureUrl> futureToDownload = lsUrlPrepToDownLoad.poll();
			if (futureToDownload.isDone() ) {
				doneGetPicUrl(futureToDownload);
			} else {//ûִ�гɹ��ͽ���ִ��
				lsUrlPrepToDownLoad.add(futureToDownload);
			}
			Thread.sleep(100);
		}
		addToPoolWhileSlowMode();
		executDownLoadPicture();
		closeThreadPool();
	}
	/**
	 * �����ȡ�����Ѿ������ˣ����ж��Ƿ�ɹ����ɹ��˽�����һ����װ��list����pool
	 * ʧ��������
	 * @param futureToDownload
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private void doneGetPicUrl(Future<GetPictureUrl> futureToDownload) throws InterruptedException, ExecutionException {
		GetPictureUrl pictureUrlToDownload = futureToDownload.get();
		if (pictureUrlToDownload.isSuccess()) {
			sucessGetUrl(pictureUrlToDownload);
		} else {
			//ʧ���˾ͷŻ�ȥ����ִ��
			Future<GetPictureUrl> result = executorGetUrlPrepToDownload.submit(pictureUrlToDownload);
			lsUrlPrepToDownLoad.add(result);
		}
	}
	
	/**
	 * �ɹ����أ�Ȼ��ʼ������һ����
	 * ���������ģʽ����װ��lsDownLoads
	 * ����ǿ���ģʽ����װ��executorDownload
	 * @param pictureUrlToDownload
	 */
	private void sucessGetUrl(GetPictureUrl pictureUrlToDownload) {
		if (!downloadFast) {
			lsDownLoads.addAll(pictureUrlToDownload.getLsResult());
		} else {
			for (UrlPictureDownLoad urlPictureDownLoad : pictureUrlToDownload.getLsResult()) {
				Future<UrlPictureDownLoad> futureDownload = executorDownload.submit(urlPictureDownLoad);
				lsFutureDownLoad.add(futureDownload);
			}
		}
	}
	
	/**
	 * ֻ������ģʽ�Ż�ִ��
	 */
	private void addToPoolWhileSlowMode() {
		if (!downloadFast) {
			for (UrlPictureDownLoad pixivUrlDownLoad : lsDownLoads) {
				Future<UrlPictureDownLoad> futureDownload = executorDownload.submit(pixivUrlDownLoad);
				lsFutureDownLoad.add(futureDownload);
			}
		}
	}
	
	/**
	 * ��ʽ����ͼƬ 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private void executDownLoadPicture() throws InterruptedException, ExecutionException {
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
	}
	
	private void closeThreadPool() {
		executorDownload.shutdown();
		executorGetUrlPrepToDownload.shutdown();
	}
}
