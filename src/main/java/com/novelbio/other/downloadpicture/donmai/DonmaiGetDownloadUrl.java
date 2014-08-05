package com.novelbio.other.downloadpicture.donmai;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.other.downloadpicture.GetPictureUrl;
import com.novelbio.other.downloadpicture.UrlPictureDownLoad;

public class DonmaiGetDownloadUrl  extends GetPictureUrl {
	private static Logger logger = Logger.getLogger(DonmaiGetPictureUrl.class);
	protected HttpFetch webFetch;
	int retryNum = 10;
	String url;
	String savePath;
	
	int thisPictureNum;
	
	ArrayList<UrlPictureDownLoad> lsResult;
	@Override
	public void setWebFetch(HttpFetch webFetch) {
		this.webFetch = HttpFetch.getInstance(webFetch);
	}
	public void setUrl(String url, int page, int thisPictureNum) {
		this.url = url;
		this.thisPictureNum = thisPictureNum;
	}
	
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	@Override
	public GetPictureUrl call() throws Exception {
		webFetch.setUri(url);
		if (!webFetch.query(retryNum)) {
			lsResult = null;
			return this;
		}
		String info = webFetch.getResponse();
		if (info == null) {
			logger.error("没抓到东西：" + url);
			lsResult = new ArrayList<UrlPictureDownLoad>();
			return this;
		}
		Parser parser = new Parser(info);
		NodeFilter filterPicture = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("id", "highres"));
		NodeList nodeLsPicture = parser.parse(filterPicture);
		lsResult = getLsDownloads(nodeLsPicture);
		webFetch.close();
		return this;
	}
	
	private ArrayList<UrlPictureDownLoad> getLsDownloads(NodeList nodeLsPicture) {
		ArrayList<UrlPictureDownLoad> lsResult = new ArrayList<UrlPictureDownLoad>();
		Node nodeUrldownload = nodeLsPicture.elementAt(0);
		String info = nodeUrldownload.toHtml();
    	String[] ss = info.split(">")[0].split(" ");
    	for (String string : ss) {
			if (string.contains("href=")) {
				String pictureUrl = string.split("href=")[1].replace("\"", "").trim();
				UrlPictureDownLoad urlPictureDownLoad = new UrlPictureDownLoad();
				urlPictureDownLoad.setPictureNum(thisPictureNum);
				urlPictureDownLoad.setPictureUrl(pictureUrl);
				urlPictureDownLoad.setSavePath(savePath);
				urlPictureDownLoad.setWebFetch(webFetch);
				lsResult.add(urlPictureDownLoad);
				break;
			}
    	}
	    logger.info("抓取下载链接完毕：" + url);
	    return lsResult;
	}
	@Override
	public ArrayList<UrlPictureDownLoad> getLsResult() {
		return lsResult;
	}

}
