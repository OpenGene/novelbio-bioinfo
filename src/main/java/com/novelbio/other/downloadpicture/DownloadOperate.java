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
		
		
//		PixivOperate pixivOperate = new PixivOperate();
//		pixivOperate.getcookies();
//		pixivOperate.setUrlAuther("3193378");
//		pixivOperate.setSavePath("/home/zong0jie/图片/My Pictures/picture/pixivTest");
//		pixivOperate.run();
		
		
		DonmaiOperate donmaiOperate = new DonmaiOperate();
		donmaiOperate.getcookies();
		donmaiOperate.setUrlAuther("gangbang");
		donmaiOperate.setSavePath("/home/zong0jie/图片/My Pictures/picture/donmai");
		donmaiOperate.run();
		
	}
	
	protected WebFetch webFetch;
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
    
    public void setWebFetchPixiv(WebFetch webFetchPixiv) {
		this.webFetch = webFetchPixiv;
	}
    public WebFetch getWebFetchPixiv() {
		return webFetch;
	}
    /** @param urlAuther 的id 
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
		logger.info("获得待下载全部midurl连接");
		//等待要获得下载url的序列
		ThreadPoolExecutor executorGetUrlPrepToDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
		//等待下载的类
		ThreadPoolExecutor executorDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));

		LinkedList<Future<GetPictureUrl>> lsUrlPrepToDownLoad = new LinkedList<Future<GetPictureUrl>>();
		LinkedList<Future<UrlPictureDownLoad>> lsFutureDownLoad = new LinkedList<Future<UrlPictureDownLoad>>();

		for (GetPictureUrl pixivGetPictureUrlToDownload : lsPrepareDownloads) {
			Future<GetPictureUrl> result = executorGetUrlPrepToDownload.submit(pixivGetPictureUrlToDownload);
			lsUrlPrepToDownLoad.add(result);
		}
		//将executorGetUrlPrepToDownload中间的内容运行直到完毕
		while (executorGetUrlPrepToDownload.getActiveCount() > 0 || lsUrlPrepToDownLoad.size() > 0) {
			Future<GetPictureUrl> futureToDownload = lsUrlPrepToDownLoad.poll();
			if (futureToDownload.isDone()) {
				GetPictureUrl pictureUrlToDownload = futureToDownload.get();
				//失败了就放回去继续执行
				if (pictureUrlToDownload.getLsResult() == null) {
					Future<GetPictureUrl> result = executorGetUrlPrepToDownload.submit(pictureUrlToDownload);
					lsUrlPrepToDownLoad.add(result);
				} else {//成功了就去下载
					lsDownLoads.addAll(pictureUrlToDownload.getLsResult());
				}
			} else {//没执行成功就接着执行
				lsUrlPrepToDownLoad.add(futureToDownload);
			}
			Thread.sleep(100);
		}
		
		for (UrlPictureDownLoad pixivUrlDownLoad : lsDownLoads) {
			Future<UrlPictureDownLoad> futureDownload = executorDownload.submit(pixivUrlDownLoad);
			lsFutureDownLoad.add(futureDownload);
		}
		logger.info("获得待下载全部bigurl连接");
		//将executorDownload中间的内容运行直到完毕
		while (executorDownload.getActiveCount() > 0 || lsFutureDownLoad.size() > 0) {
			Future<UrlPictureDownLoad> futureDownload = lsFutureDownLoad.poll();
			if (futureDownload.isDone()) {
				UrlPictureDownLoad pixivUrlDownLoad = futureDownload.get();
				//失败了就放回去继续执行
				if (!pixivUrlDownLoad.isSaveSucess()) {
					Future<UrlPictureDownLoad> result = executorDownload.submit(pixivUrlDownLoad);
					lsFutureDownLoad.add(result);
				}
			} else {//没执行成功就接着执行
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
	/** 返回一系列可以获得最终下载连接的对象、
	 * 实际上因为不可能直接获得最后下载的url，所以都要经过几次跳转，那么这个中间跳转过程都可以写在该方法里面
	 */
	protected abstract ArrayList<? extends GetPictureUrl> getLsPrepareDownload();
	
}
