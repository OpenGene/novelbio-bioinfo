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

import org.apache.log4j.Logger;
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
import com.novelbio.base.fileOperate.FileOperate;

/** 并发下载pixiv的图片 */
public class PixivOperate {
	private static Logger logger = Logger.getLogger(PixivOperate.class);
	
	public static void main(String[] args) throws ParserException, InterruptedException, ExecutionException {
		
		PixivOperate pixivOperate = new PixivOperate();
		pixivOperate.setUrlAuther(648014);
		pixivOperate.setSavePath("/home/zong0jie/图片/My Pictures/picture/pixivTest");
		pixivOperate.running();
	}
	WebFetch webFetchPixiv;
	String urlAuther;
	String autherName;
	String autherID;
	
	String name = "facemun";
	String password = "f12344321n";
	
	String savePath;
	int retryGetPageNum = 10;
	
	/** 本作者有多少图片 */
	int allPictureNum = 0;
	/** 总共几页 */
	int allPages = 0;
	
	PixivGetPathExistPic pixivGetPathExistPic = new PixivGetPathExistPic();
	
	PixivOperate() {
		getcookies();
	}
	/**
	 * 获得pixiv的cookies
	 */
    private void getcookies() {
    	if (webFetchPixiv == null) {
			webFetchPixiv = WebFetch.getInstance();
		}
    	if (webFetchPixiv.getCookies() != null) {
    		return;
    	}
    	Map<String, String> mapPostKey2Value = new HashMap<String, String>();
    	mapPostKey2Value.put("mode", "login");
    	mapPostKey2Value.put("pixiv_id", name);
    	mapPostKey2Value.put("pass", password);
    	webFetchPixiv.setPostParam(mapPostKey2Value);
    	webFetchPixiv.setUrl("http://www.pixiv.net/index.php");
    	if (!webFetchPixiv.query()) {
			getcookies();
		}
   }
    public void setWebFetchPixiv(WebFetch webFetchPixiv) {
		this.webFetchPixiv = webFetchPixiv;
	}
    public WebFetch getWebFetchPixiv() {
		return webFetchPixiv;
	}
    /**
     * @param urlAuther 的id
     */
	public void setUrlAuther(int urlAutherid) {
		this.urlAuther = "http://www.pixiv.net/member_illust.php?id=" + urlAutherid;
		autherID = urlAutherid + "";
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath.trim();
	}
	public void running() throws InterruptedException, ExecutionException {
		ArrayList<PixivGetPictureUrlToDownload> lsPrepareDownloads = getLsPrepareDownload();
		ArrayList<PixivUrlDownLoad> lsDownLoads = new ArrayList<PixivUrlDownLoad>();
		logger.info("获得待下载全部midurl连接");
		//等待要获得下载url的序列
		ThreadPoolExecutor executorGetUrlPrepToDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));
		//等待下载的类
		ThreadPoolExecutor executorDownload = new ThreadPoolExecutor(3, 4, 5000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(1000));

		LinkedList<Future<PixivGetPictureUrlToDownload>> lsUrlPrepToDownLoad = new LinkedList<Future<PixivGetPictureUrlToDownload>>();
		LinkedList<Future<PixivUrlDownLoad>> lsFutureDownLoad = new LinkedList<Future<PixivUrlDownLoad>>();

		for (PixivGetPictureUrlToDownload pixivGetPictureUrlToDownload : lsPrepareDownloads) {
			Future<PixivGetPictureUrlToDownload> result = executorGetUrlPrepToDownload.submit(pixivGetPictureUrlToDownload);
			lsUrlPrepToDownLoad.add(result);
		}
		//将executorGetUrlPrepToDownload中间的内容运行直到完毕
		while (executorGetUrlPrepToDownload.getActiveCount() > 0 || lsUrlPrepToDownLoad.size() > 0) {
			Future<PixivGetPictureUrlToDownload> futureToDownload = lsUrlPrepToDownLoad.poll();
			if (futureToDownload.isDone()) {
				PixivGetPictureUrlToDownload pictureUrlToDownload = futureToDownload.get();
				//失败了就放回去继续执行
				if (pictureUrlToDownload.getLsResult() == null) {
					Future<PixivGetPictureUrlToDownload> result = executorGetUrlPrepToDownload.submit(pictureUrlToDownload);
					lsUrlPrepToDownLoad.add(result);
				} else {//成功了就去下载
					lsDownLoads.addAll(pictureUrlToDownload.getLsResult());
				}
			} else {//没执行成功就接着执行
				lsUrlPrepToDownLoad.add(futureToDownload);
			}
			Thread.sleep(100);
		}
		
		for (PixivUrlDownLoad pixivUrlDownLoad : lsDownLoads) {
			Future<PixivUrlDownLoad> futureDownload = executorDownload.submit(pixivUrlDownLoad);
			lsFutureDownLoad.add(futureDownload);
		}
		logger.info("获得待下载全部bigurl连接");
		//将executorDownload中间的内容运行直到完毕
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
		savePath = FileOperate.addSep(savePath) + generateoutName(autherName) + FileOperate.getSepPath();
		pixivGetPathExistPic.setSavePath(savePath);

		
		for (int i = 1; i <= allPages; i++) {
			String urlPage = urlAuther + "&p=" + i;
			PixivGetPageMidUrl pixivGetPageMidUrl = new PixivGetPageMidUrl();
			pixivGetPageMidUrl.setAllPageNum(allPages);
			pixivGetPageMidUrl.setPageUrl(urlPage);
			pixivGetPageMidUrl.setThisPageNum(i);
			pixivGetPageMidUrl.setSavePath(savePath);
			pixivGetPageMidUrl.setWebFetch(webFetchPixiv);
			pixivGetPageMidUrl.setAllPictureNum(allPictureNum);
			pixivGetPageMidUrl.setPixivGetPathExistPic(pixivGetPathExistPic);
			ArrayList<PixivGetPictureUrlToDownload> lsPixivGetPictureUrlToDownloads = pixivGetPageMidUrl.getLsToDownloadUrl();
			while (true) {
				if (lsPixivGetPictureUrlToDownloads != null) {
					break;
				}
				lsPixivGetPictureUrlToDownloads = pixivGetPageMidUrl.getLsToDownloadUrl();
			}
			lsResult.addAll(lsPixivGetPictureUrlToDownloads);
			logger.error("总共" + allPages + "页，已经读取完第" + i + "页" );
			//如果文件夹里面已经有该图片了，那么就返回，实际上有第一张图片，基本上后面的图都会有了，所以判断第一张图片就行了
//			if (pixivGetPageMidUrl.isAlreadyHaveFile()) {
//				break;
//			}
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
		if (!webFetchPixiv.query(retryGetPageNum)) {
			return false;
		}
		String pixivAutherInfo = webFetchPixiv.getResponse();
		Parser parser = new Parser(pixivAutherInfo);
		
		NodeFilter filterNum = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "active_gray"));
		NodeList nodeListNum = parser.parse(filterNum);
		allPictureNum = getNodeAllPicture(nodeListNum);
		
		parser = new Parser(pixivAutherInfo);
		NodeFilter filterName = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "avatar_m"));
		NodeList nodeAutherName = parser.parse(filterName);
		autherName = getAuterName(nodeAutherName) + "_" + autherID;
		
		allPages = (int) Math.ceil((double)allPictureNum/20);
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


	
	 /**
	  * 因为pixiv中的作者名或文件名里面总是有各种奇怪的字符，有些不能成为文件夹名，所以要将他们替换掉
   * 输入旧文件名，将其转变为新文件名
   * @param filepath
   * @param newPath
   */
  protected static String generateoutName(String name) {
  		String outName;
  		outName = name.replace("\\", "");
  		outName = outName.replace("/", "");
  		outName= outName.replace("\"", "");
  		outName = outName.replace("*", "");
  		outName = outName.replace("?", "");
  		outName = outName.replace("<", "");
  		outName = outName.replace(">", "");
  		outName = outName.replace("|", "");
  		return outName;
  }

}