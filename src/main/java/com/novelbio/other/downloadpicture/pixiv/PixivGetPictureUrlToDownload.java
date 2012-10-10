package com.novelbio.other.downloadpicture.pixiv;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.RegexFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.other.downloadpicture.GetPictureUrl;
import com.novelbio.other.downloadpicture.UrlPictureDownLoad;
/** 找到每个midUrl所对应的页面，然后通过本类获得bigurl的download类*/
public class PixivGetPictureUrlToDownload implements GetPictureUrl {
	private static Logger logger = Logger.getLogger(PixivGetPictureUrlToDownload.class);
	String pixivUrl = "http://www.pixiv.net/";
		
	int retryNum = 10;
	String midUrl;
	WebFetch webFetch;
	
	String name;
	String savePath;
	int pictureNum;
	/** null说明执行失败，需要重新执行 */
	ArrayList<UrlPictureDownLoad> lsResult;
	
	public void setName(String name) {
		this.name = name;
	}
	public void setMidUrl(String midUrl) {
		this.midUrl = midUrl;
	}
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}
	public void setWebFetch(WebFetch webFetch) {
		this.webFetch = webFetch;
	}
	/** 保存的路径里面应该要包含作者名 */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	public ArrayList<UrlPictureDownLoad> getLsResult() {
		return lsResult;
	}
	/** 返回null表示失败，就需要重跑
	 * @throws ParserException */
	private void getLsPicture() throws ParserException {
		webFetch.setUrl(midUrl);
		if (!webFetch.query(retryNum)) {
			lsResult = null;
			return;
		}
		String info = webFetch.getResponse();
		if (info == null) {
			logger.error("没抓到东西：" + midUrl);
			lsResult = new ArrayList<UrlPictureDownLoad>();
			return;
		}
		Parser parser = new Parser(info);
		NodeFilter filterPicture = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "works_display"));
		Node nodePicture = parser.parse(filterPicture).elementAt(0);
		lsResult = getLsDownloads(nodePicture);
	}
	
	private ArrayList<UrlPictureDownLoad> getLsDownloads(Node nodePicture) throws ParserException {
		ArrayList<UrlPictureDownLoad> lsPixivUrlDownLoads = new ArrayList<UrlPictureDownLoad>();
		
		ArrayList<String> lsResultUrl = new ArrayList<String>();
		String urlBig = getPictureUrlBig(nodePicture);
		urlBig = pixivUrl + urlBig;
		if (urlBig.contains("mode=manga")) {
			lsResultUrl = getPictureUrlManga(urlBig);
			if (lsResultUrl == null) {
				return null;
			}
		} else {
			lsResultUrl.add(getPictureUrlBigAbs(urlBig));
		}
		for (String pictureUrl : lsResultUrl) {
			UrlPictureDownLoad pixivUrlDownLoad = new UrlPictureDownLoad();
			pixivUrlDownLoad.setName(name);
			pixivUrlDownLoad.setPictureNum(pictureNum);
			pixivUrlDownLoad.setPictureUrl(pictureUrl);
			pixivUrlDownLoad.setRefUrl(midUrl);
			pixivUrlDownLoad.setWebFetch(WebFetch.getInstance(webFetch));
			pixivUrlDownLoad.setSavePath(savePath);
			lsPixivUrlDownLoads.add(pixivUrlDownLoad);
		}
		return lsPixivUrlDownLoads;
	}
	
	private String getPictureUrlBig(Node nodePicture) {
		String resultUrl = null;
		NodeFilter filterPictureUrl = new TagNameFilter("a");
		Node nodeUrl = nodePicture.getChildren().extractAllNodesThatMatch(filterPictureUrl,true).elementAt(0);
		String url = nodeUrl.getText();
		String[] ss = url.split(" ");
		for (String string : ss) {
			if (string.contains("href=")) {
				resultUrl = string.replace("href=", "").replace("\"", "").trim();
				break;
			}
		}
		return WebFetch.decode(resultUrl);
	}
	private String getPictureUrlBigAbs(String bigUrl) throws ParserException {
		String resultUrl = "";
		webFetch.setRefUrl(midUrl);
		webFetch.setUrl(bigUrl);
		if (!webFetch.query(retryNum)) {
			return null;
		}
		Parser parser = new Parser(webFetch.getResponse());
		NodeFilter filterPicture = new TagNameFilter("img");
		Node nodePictureManga = parser.parse(filterPicture).elementAt(0);
		String rawUrl = nodePictureManga.toHtml();
		String[] ss = rawUrl.split(" ");
		for (String string : ss) {
			if (string.contains("src")) {
				resultUrl = string.replace("src=", "").replace("\"", "").trim();
			}
		}
		resultUrl = WebFetch.decode(resultUrl);
		return resultUrl;
	}
	/** 获取连环画的url 
	 * @throws ParserException */
	private ArrayList<String> getPictureUrlManga(String mangaUrl) throws ParserException {
		webFetch.setRefUrl(midUrl);
		webFetch.setUrl(mangaUrl);
		if (!webFetch.query(retryNum)) {
			return null;
		}

		return getLsPictureUrlManga(webFetch.getResponse());
	}
	
	private ArrayList<String> getLsPictureUrlManga(String pageInfo) throws ParserException {
		Parser parser = new Parser(pageInfo);
		NodeFilter filterPicture = new RegexFilter(".unshift");
		NodeList nodeLsPicture = parser.extractAllNodesThatMatch(filterPicture);
		
		ArrayList<String> lsMangaUrl = new ArrayList<String>();
		SimpleNodeIterator iterator = nodeLsPicture.elements();
        while (iterator.hasMoreNodes()) {
        	//每个图片的node
            Node nodePicture = iterator.nextNode();
            String url = getPictureUrl(nodePicture);
            lsMangaUrl.add(url);
        }
        return lsMangaUrl;
	}
	/** 获得每个图片的url 
	 * @throws UnsupportedEncodingException */
	private String getPictureUrl(Node nodePicture) {
		String outUrl= null;
		String url = nodePicture.getText();
		PatternOperate patternOperate = new PatternOperate("(http://.+?)'\\)", false);
		outUrl = patternOperate.getPatFirst(url, 1);

		return outUrl;
	}
	@Override
	public PixivGetPictureUrlToDownload call() {
		try {
			getLsPicture();
		} catch (Exception e) {
			lsResult = null;
		}
		return this;
	}

	

}
