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
//		TxtReadandWrite txtReadUrl = new TxtReadandWrite("/home/zong0jie/图片/My Pictures/picture/pixivurl.txt", false);
//		for (String urlID : txtReadUrl.readlines()) {
//			urlID = urlID.trim();
//			if (urlID.equals("")) {
//				continue;
//			}
//			setUrl.add(urlID);
//		}
//		for (String urlID : setUrl) {			
//			pixivOperate.setUrlAuther(urlID);
//			pixivOperate.setSavePath("/home/zong0jie/图片/My Pictures/picture/pixivTest");
//			pixivOperate.running();
//			Thread.sleep(100);
//			logger.error("finished url:" + urlID);
//		}
		
		
		PixivOperate pixivOperate = new PixivOperate();
		pixivOperate.getcookies();
		pixivOperate.setUrlAuther("403278");
		pixivOperate.setDownloadFast(true);
		pixivOperate.setSavePath("D:/Picture/pixiv");
		pixivOperate.run();
		
		
		DonmaiOperate donmaiOperate = new DonmaiOperate();
		donmaiOperate.getcookies();
		donmaiOperate.setUrlAuther("kishida_mel");
		donmaiOperate.setDownloadFast(true);
		donmaiOperate.setSavePath("D:/Picture/donmai");
		donmaiOperate.run();
		
	}
	
	protected HttpFetch webFetch;
	protected String urlAuther;
	protected String autherName;
	protected String autherID;
	
	protected String savePath;
	protected int retryGetPageNum = 10;
	
	/** 本作者有多少图片 */
	protected int allPictureNum = 0;
	/** 总共几页 */
	protected int allPages = 0;
	
	protected PixivGetPathExistPic pixivGetPathExistPic;
	
	/** 获得某个网站的cookies */
    protected abstract void getcookies();
    
	DownloadPictureFile downloadFile = new DownloadPictureFile();
	
    public void setWebFetchPixiv(HttpFetch webFetchPixiv) {
		this.webFetch = webFetchPixiv;
	}
    public HttpFetch getWebFetchPixiv() {
		return webFetch;
	}
    /** @param urlAuther 的id 
     * @return */
	public abstract void setUrlAuther(String urlAutherid);
	
	public void setSavePath(String savePath) {
		this.savePath = savePath.trim();
	}
    /** 获得连接后是否立即下载
     * 还是等全部连接都获得了才下载
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
	/** 返回一系列可以获得最终下载连接的对象、
	 * 实际上因为不可能直接获得最后下载的url，所以都要经过几次跳转，那么这个中间跳转过程都可以写在该方法里面
	 */
	protected abstract ArrayList<? extends GetPictureUrl> getLsPrepareDownload();
	
}

/**
 * 得到下载的url的类之后开始具体下载图片
 * @author zong0jie
 *
 */
class DownloadPictureFile {
	private static Logger logger = Logger.getLogger(DownloadPictureFile.class);
	boolean downloadFast = false;
	
	ArrayList<UrlPictureDownLoad> lsDownLoads = new ArrayList<UrlPictureDownLoad>();
	//等待要获得下载url的序列
	ThreadPoolExecutor executorGetUrlPrepToDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
	//等待下载的类
	ThreadPoolExecutor executorDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
	
	LinkedList<Future<GetPictureUrl>> lsUrlPrepToDownLoad = new LinkedList<Future<GetPictureUrl>>();
	LinkedList<Future<UrlPictureDownLoad>> lsFutureDownLoad = new LinkedList<Future<UrlPictureDownLoad>>();
	
	/**
	 * 下载图片url的类
	 * @param lsPrepareDownloads
	 */
	public void setLsPrepareDownloads(ArrayList<? extends GetPictureUrl> lsPrepareDownloads) {
		for (GetPictureUrl pixivGetPictureUrlToDownload : lsPrepareDownloads) {
			Future<GetPictureUrl> result = executorGetUrlPrepToDownload.submit(pixivGetPictureUrlToDownload);
			lsUrlPrepToDownLoad.add(result);
		}
	}
	
    /** 获得连接后是否立即下载
     * 还是等全部连接都获得了才下载
     *  */
	public void setDownloadFast(boolean downloadFast) {
		this.downloadFast = downloadFast;
	}
	
	public void running() throws InterruptedException, ExecutionException {
		//将executorGetUrlPrepToDownload中间的内容运行直到完毕
		while (executorGetUrlPrepToDownload.getActiveCount() > 0 || lsUrlPrepToDownLoad.size() > 0) {
			Future<GetPictureUrl> futureToDownload = lsUrlPrepToDownLoad.poll();
			if (futureToDownload.isDone() ) {
				doneGetPicUrl(futureToDownload);
			} else {//没执行成功就接着执行
				lsUrlPrepToDownLoad.add(futureToDownload);
			}
			Thread.sleep(100);
		}
		addToPoolWhileSlowMode();
		executDownLoadPicture();
		closeThreadPool();
	}
	/**
	 * 如果获取序列已经跑完了，则判断是否成功，成功了进行下一步，装入list或者pool
	 * 失败了重跑
	 * @param futureToDownload
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private void doneGetPicUrl(Future<GetPictureUrl> futureToDownload) throws InterruptedException, ExecutionException {
		GetPictureUrl pictureUrlToDownload = futureToDownload.get();
		if (pictureUrlToDownload.isSuccess()) {
			sucessGetUrl(pictureUrlToDownload);
		} else {
			//失败了就放回去继续执行
			Future<GetPictureUrl> result = executorGetUrlPrepToDownload.submit(pictureUrlToDownload);
			lsUrlPrepToDownLoad.add(result);
		}
	}
	
	/**
	 * 成功下载，然后开始进行下一步。
	 * 如果是慢速模式，则装入lsDownLoads
	 * 如果是快速模式，则装入executorDownload
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
	 * 只有慢速模式才会执行
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
	 * 正式下载图片 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private void executDownLoadPicture() throws InterruptedException, ExecutionException {
		//将executorDownload中间的内容运行直到完毕
		while (executorDownload.getActiveCount() > 0 || lsFutureDownLoad.size() > 0) {
			Future<UrlPictureDownLoad> futureDownload = lsFutureDownLoad.poll();
			if (futureDownload.isDone()) {
				UrlPictureDownLoad pixivUrlDownLoad = futureDownload.get();
				//失败了就放回去继续执行
				if (!pixivUrlDownLoad.isSaveSucess()) {
					Future<UrlPictureDownLoad> result = executorDownload.submit(pixivUrlDownLoad);
					lsFutureDownLoad.add(result);
				} else {
					logger.error("finish " + pixivUrlDownLoad.name);
				}
			} else {//没执行成功就接着执行
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
