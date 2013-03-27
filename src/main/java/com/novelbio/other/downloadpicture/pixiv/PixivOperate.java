package com.novelbio.other.downloadpicture.pixiv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.other.downloadpicture.DownloadOperate;
import com.novelbio.other.downloadpicture.GetPictureUrl;
import com.novelbio.other.downloadpicture.UrlPictureDownLoad;

/** 并发下载pixiv的图片 */
public class PixivOperate extends DownloadOperate{
	private static Logger logger = Logger.getLogger(PixivOperate.class);

	String name = "facemun";
	String password = "f12344321n";
	
	public PixivOperate() {
		pixivGetPathExistPic = new PixivGetPathExistPic(PixivGetPathExistPic.SITE_PIXIV);
		getcookies();
	}
	/**
	 * 获得pixiv的cookies
	 */
    public void getcookies() {
    	if (webFetch == null) {
			webFetch = HttpFetch.getInstance();
		}
    	if (webFetch.getCookies() != null) {
    		return;
    	}
    	Map<String, String> mapPostKey2Value = new HashMap<String, String>();
    	mapPostKey2Value.put("mode", "login");
    	mapPostKey2Value.put("pixiv_id", name);
    	mapPostKey2Value.put("pass", password);
    	webFetch.setPostParam(mapPostKey2Value);
    	webFetch.setUri("http://www.pixiv.net/index.php");
    	if (!webFetch.query()) {
			getcookies();
		}
   }
    /**
     * @param urlAuther 的id
     */
	public void setUrlAuther(String urlAutherid) {
		this.urlAuther = "http://www.pixiv.net/member_illust.php?id=" + urlAutherid;
		autherID = urlAutherid + "";
		
	}
	
	/**
	 * 获得总共几页
	 * @return 返回是否设置成功
	 */
	protected boolean setPictureNum_And_PageNum_Auther_And_PixivGetPath() {
		try {
			webFetch.setUri(urlAuther);
			while (!webFetch.query(retryGetPageNum)) {
				Thread.sleep(500);
			}

			String pixivAutherInfo = webFetch.getResponse();
			Parser parser = new Parser(pixivAutherInfo);
			
			NodeFilter filterNum = new AndFilter(new TagNameFilter("span"), new HasAttributeFilter("class", "count-badge"));
			NodeList nodeListNum = parser.parse(filterNum);
			allPictureNum = getNodeAllPicture(nodeListNum);
			
			parser = new Parser(pixivAutherInfo);
			NodeFilter filterName = new AndFilter(new TagNameFilter("h1"), new HasAttributeFilter("class", "user"));
			NodeList nodeAutherName = parser.parse(filterName);
			autherName = getAuterName(nodeAutherName) + "_" + autherID;
			savePath = FileOperate.addSep(savePath) + UrlPictureDownLoad.generateoutName(autherName) + FileOperate.getSepPath();
			allPages = (int) Math.ceil((double)allPictureNum/20);
			webFetch.readResponse();
			return true;
		} catch (Exception e) {
			webFetch.readResponse();
		}
		return false;
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
        if (iteratorPages.hasMoreNodes()) {
        	nodeNumBefore = iteratorPages.nextNode();
		}
        String pageRaw = nodeNumBefore.toPlainTextString();
        String pageNum = pageRaw.replace("件", "").trim();
        return Integer.parseInt(pageNum);
	}
	/**
	 * @param nodeNumLsBefore
	 * @return
	 */
	private String getAuterName(NodeList getAuterName) {
		String autherName = null;
		Node nodeAuther = getAuterName.elementAt(0);
		autherName = nodeAuther.toPlainTextString();
//		String[] ss = autherRaw.split(" ");
//		for (String string : ss) {
//			if (string.contains("title")) {
//				autherName = string.replace("title=", "").replace("\"", "").trim();
//				break;
//			}
//		}
		return autherName;
	}
	
	protected ArrayList<PixivGetPictureUrlToDownload> getLsPrepareDownload() {
		ArrayList<PixivGetPictureUrlToDownload> lsResult = new ArrayList<PixivGetPictureUrlToDownload>();
		pixivGetPathExistPic.setSavePath(savePath);
		for (int i = 1; i <= allPages; i++) {
			String urlPage = urlAuther + "&p=" + i;
			PixivGetPageMidUrl pixivGetPageMidUrl = new PixivGetPageMidUrl();
			pixivGetPageMidUrl.setAllPageNum(allPages);
			pixivGetPageMidUrl.setPageUrl(urlPage);
			pixivGetPageMidUrl.setThisPageNum(i);
			pixivGetPageMidUrl.setSavePath(savePath);
			pixivGetPageMidUrl.setWebFetch(webFetch);
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



}
