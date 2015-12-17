package com.novelbio.other.downloadpicture.pixiv;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.other.downloadpicture.GetPictureUrl;
import com.novelbio.other.downloadpicture.UrlPictureDownLoad;
/** 找到每个midUrl所对应的页面，然后通过本类获得bigurl的download类*/
public class PixivGetPictureUrlToDownload extends GetPictureUrl {
	private static Logger logger = Logger.getLogger(PixivGetPictureUrlToDownload.class);
	String pixivUrl = "http://www.pixiv.net/";
		
	int retryNum = 10;
	String midUrl;
	HttpFetch webFetch;
	
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
	public void setWebFetch(HttpFetch webFetch) {
		this.webFetch = HttpFetch.getInstance(webFetch);
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
		webFetch.setUri("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=43680130");
//		if (midUrl.contains("43680130")) {
//			logger.debug("");
//		}
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
		System.out.println(info);
		Parser parser = new Parser(info);
		NodeFilter filterPicture = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "works_display"));
		Node nodePicture = parser.parse(filterPicture).elementAt(0);
		lsResult = getLsDownloads(nodePicture);
		webFetch.close();
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
			pixivUrlDownLoad.setWebFetch(webFetch);
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
				resultUrl = string.split("href=")[1].replace("\"", "").trim();
				break;
			}
		}
		return StringOperate.decode(resultUrl);
	}
	private String getPictureUrlBigAbs(String bigUrl) throws ParserException {
		String resultUrl = "";
		webFetch.setRefUri(midUrl);
		webFetch.setUri(bigUrl);
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
				resultUrl = string.split("src=")[1].replace("\"", "").trim();
			}
		}
		resultUrl = StringOperate.decode(resultUrl);
		return resultUrl;
	}
	/** 获取连环画的url 
	 * @throws ParserException */
	private ArrayList<String> getPictureUrlManga(String mangaUrl) throws ParserException {
		webFetch.setRefUri(midUrl);
		webFetch.setUri(mangaUrl);
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
			e.printStackTrace();
			lsResult = null;
		}
		return this;
	}

	

}
