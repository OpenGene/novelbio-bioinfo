package com.novelbio.other.pixiv.execute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.novelbio.base.dataOperate.WebFetch;

/** 并发下载pixiv的图片 */
public class PixivOperate {
	public static void main(String[] args) throws ParserException, InterruptedException, ExecutionException {
		
		PixivOperate pixivOperate = new PixivOperate();
		pixivOperate.setUrlAuther(1196643);
		pixivOperate.setSavePath("/Users/zongjie/Downloads/test");
		pixivOperate.running();
	}
	WebFetch webFetchPixiv = WebFetch.getInstance();
	String urlAuther;
	String autherName;
	
	String name = "facemun";
	String password = "f12344321n";
	
	String savePath;
	int retryGetPageNum = 10;
	
	/** 本作者有多少图片 */
	int allPictureNum = 0;
	/** 总共几页 */
	int allPages = 0;
	
	PixivOperate() {
		getcookies();
	}
	/**
	 * 获得pixiv的cookies
	 */
    private void getcookies() {
    	if (webFetchPixiv.getCookies() != null) {
    		return;
    	}
    	Map<String, String> mapPostKey2Value = new HashMap<String, String>();
    	mapPostKey2Value.put("mode", "login");
    	mapPostKey2Value.put("pixiv_id", name);
    	mapPostKey2Value.put("pass", password);
    	webFetchPixiv.setPostParam(mapPostKey2Value);
    	webFetchPixiv.setUrl("http://www.pixiv.net/index.php");
    	Iterable<String> itResult = webFetchPixiv.readResponse();
    	if (itResult == null) {
			getcookies();
		}
   }
    /**
     * @param urlAuther 的id
     */
	public void setUrlAuther(int urlAutherid) {
		this.urlAuther = "http://www.pixiv.net/member_illust.php?id=" + urlAutherid;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath.trim();
	}
	public void running() throws InterruptedException, ExecutionException {
		ArrayList<PixivGetPictureUrlToDownload> lsPrepareDownloads = getLsPrepareDownload();
		//等待要获得下载url的序列
		ThreadPoolExecutor executorGetUrlPrepToDownload = new ThreadPoolExecutor(1, 3, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
		//等待下载的类
		ThreadPoolExecutor executorDownload = new ThreadPoolExecutor(1, 3, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));

		LinkedList<Future<PixivGetPictureUrlToDownload>> lsUrlPrepToDownLoad = new LinkedList<Future<PixivGetPictureUrlToDownload>>();
		LinkedList<Future<PixivUrlDownLoad>> lsFutureDownLoad = new LinkedList<Future<PixivUrlDownLoad>>();

		for (PixivGetPictureUrlToDownload pixivGetPictureUrlToDownload : lsPrepareDownloads) {
			Future<PixivGetPictureUrlToDownload> result = executorGetUrlPrepToDownload.submit(pixivGetPictureUrlToDownload);
			lsUrlPrepToDownLoad.add(result);
		}
		
		while (executorGetUrlPrepToDownload.getActiveCount() > 0 || lsUrlPrepToDownLoad.size() > 0) {
			Future<PixivGetPictureUrlToDownload> futureToDownload = lsUrlPrepToDownLoad.poll();
			if (futureToDownload.isDone()) {
				PixivGetPictureUrlToDownload pictureUrlToDownload = futureToDownload.get();
				//失败了就放回去继续执行
				if (pictureUrlToDownload.getLsResult() == null) {
					Future<PixivGetPictureUrlToDownload> result = executorGetUrlPrepToDownload.submit(pictureUrlToDownload);
					lsUrlPrepToDownLoad.add(result);
				} else {//成功了就去下载
					for (PixivUrlDownLoad pixivUrlDownLoad : pictureUrlToDownload.getLsResult()) {
						Future<PixivUrlDownLoad> futureDownload = executorDownload.submit(pixivUrlDownLoad);
						lsFutureDownLoad.add(futureDownload);
					}
				}
			} else {//没执行成功就接着执行
				lsUrlPrepToDownLoad.add(futureToDownload);
			}
			Thread.sleep(100);
		}
		
		while (executorDownload.getActiveCount() > 0 || lsFutureDownLoad.size() > 0) {
			Future<PixivUrlDownLoad> futureDownload = lsFutureDownLoad.poll();
			if (futureDownload.isDone()) {
				PixivUrlDownLoad pixivUrlDownLoad = futureDownload.get();
				//失败了就放回去继续执行
				if (!pixivUrlDownLoad.isSaveSucess()) {
					Future<PixivUrlDownLoad> result = executorDownload.submit(pixivUrlDownLoad);
					lsFutureDownLoad.add(result);
				}
			} else {//没执行成功就接着执行
				lsFutureDownLoad.add(futureDownload);
			}
			Thread.sleep(100);
		}
		
		executorDownload.shutdown();
		executorGetUrlPrepToDownload.shutdown();
		
		webFetchPixiv.close();
		
		return;
	}
	
	
	
	private ArrayList<PixivGetPictureUrlToDownload> getLsPrepareDownload() {
		ArrayList<PixivGetPictureUrlToDownload> lsResult = new ArrayList<PixivGetPictureUrlToDownload>();
		try {
			if (!setPictureNum_And_PageNum_Auther()) {
				return null;
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		for (int i = 1; i <= allPages; i++) {
			String urlPage = urlAuther + "&p=" + i;
			PixivGetPageMidUrl pixivGetPageMidUrl = new PixivGetPageMidUrl();
			pixivGetPageMidUrl.setAllPageNum(allPages);
			pixivGetPageMidUrl.setAutherName(autherName);
			pixivGetPageMidUrl.setPageUrl(urlPage);
			pixivGetPageMidUrl.setThisPageNum(i);
			pixivGetPageMidUrl.setSavePath(savePath);
			pixivGetPageMidUrl.setWebFetch(webFetchPixiv);
			pixivGetPageMidUrl.setAllPictureNum(allPictureNum);
			ArrayList<PixivGetPictureUrlToDownload> lsPixivGetPictureUrlToDownloads = pixivGetPageMidUrl.getLsToDownloadUrl();
			while (true) {
				if (lsPixivGetPictureUrlToDownloads != null) {
					break;
				}
				lsPixivGetPictureUrlToDownloads = pixivGetPageMidUrl.getLsToDownloadUrl();
			}
			
			lsResult.addAll(lsPixivGetPictureUrlToDownloads);
		}
		return lsResult;
	}
	
	/**
	 * 获得总共几页
	 * @return
	 * @throws ParserException 
	 */
	private boolean setPictureNum_And_PageNum_Auther() throws ParserException {
		webFetchPixiv.setUrl(urlAuther);		
		String pixivAutherInfo = WebFetch.getResponseRetry(retryGetPageNum, webFetchPixiv);
		if (pixivAutherInfo == null) {
			return false;
		}
		Parser parser = new Parser(pixivAutherInfo);
		
		NodeFilter filterNum = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "active_gray"));
		NodeList nodeListNum = parser.parse(filterNum);
		allPictureNum = getNodeAllPicture(nodeListNum);
		
		parser = new Parser(pixivAutherInfo);
		NodeFilter filterName = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "avatar_m"));
		NodeList nodeAutherName = parser.parse(filterName);
		autherName = getAuterName(nodeAutherName);

		parser = new Parser(pixivAutherInfo);
		NodeFilter filterPage = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "pages"));
		NodeList nodeListPage = parser.parse(filterPage);
		allPages = getNodeAllPage(nodeListPage);
		return true;
	}
	/**
	 * @param nodeNumLsBefore
	 * @return
	 */
	private String getAuterName(NodeList getAuterName) {
		String autherName = null;
		Node nodeAuther = getAuterName.elementAt(0);
		String autherRaw = nodeAuther.getText();
		String[] ss = autherRaw.split(" ");
		for (String string : ss) {
			if (string.contains("title")) {
				autherName = string.replace("title=", "").replace("\"", "").trim();
				break;
			}
		}
		return autherName;
	}
	/**
	 * @param nodeNumLsBefore
	 * @return
	 */
	private int getNodeAllPicture(NodeList nodeNumLsBefore) {
        SimpleNodeIterator iteratorPages = nodeNumLsBefore.elements();
        Node nodeNumBefore = null;
        if (iteratorPages.hasMoreNodes()) {
        	nodeNumBefore = iteratorPages.nextNode();
		}
        Node nodeNum = nodeNumBefore.getNextSibling().getNextSibling();
        String pageRaw = nodeNum.toPlainTextString();
        String pageNum = pageRaw.replace("件", "").trim();
        return Integer.parseInt(pageNum);
	}
	private int getNodeAllPage(NodeList nodePage) {
		int pageNum = 1;
		//获得pages的子元素
        SimpleNodeIterator iteratorPages = nodePage.elements();
        NodeList nodePage1 = null;
        if (iteratorPages.hasMoreNodes()) {
        	nodePage1 = iteratorPages.nextNode().getChildren();
		}
        else {
			return 1;
		}
        //提取具体的page
		NodeFilter filterPage = new TagNameFilter("a");
        NodeList nodeListPages = nodePage1.extractAllNodesThatMatch(filterPage, true);
        SimpleNodeIterator iterator = nodeListPages.elements();  
        while (iterator.hasMoreNodes()) {  
            Node node = iterator.nextNode();
            String result = node.toPlainTextString();
            if (result.contains("下一个")) {
				break;
			}
            pageNum = Integer.parseInt(result);
        }
        return pageNum;
	}




}