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
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.other.downloadpicture.pixiv.PixivGetPathExistPic;

/** 给定一个图片链接，返回该图片的大图链接，有问题 */
public class DonmaiGetPictureUrl {
	private static Logger logger = Logger.getLogger(DonmaiGetPictureUrl.class);
	protected WebFetch webFetch;
	int retryNum = 10;
	String urlpage;
	int page;
	String savePath;
	
	int allPictureNum;
	/** 判断有没有类似文件名的 */
	PixivGetPathExistPic pixivGetPathExistPic;
	
	ArrayList<DonmaiGetDownloadUrl> lsResult;

	public void setWebFetch(WebFetch webFetch) {
		this.webFetch = webFetch;
	}
	public void setUrlPage(String url, int pageNum) {
		this.page = pageNum;
		if (pageNum == 1) {
			this.urlpage = url;
			return;
		}
		this.urlpage = url + "&page=" + pageNum;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	public void setAllPictureNum(int allPictureNum) {
		this.allPictureNum = allPictureNum;
	}
	/** false 表示失败 */
	public boolean query() {
		try {
			return getLsDownloads();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	private boolean getLsDownloads() throws ParserException {
		webFetch.setUrl(urlpage);
		if (!webFetch.query(retryNum)) {
			lsResult = null;
			return false;
		}
		String info = webFetch.getResponse();
		if (info == null) {
			logger.info("没抓到东西：" + urlpage);
			lsResult = new ArrayList<DonmaiGetDownloadUrl>();
			return true;
		}
		Parser parser = new Parser(info);
		NodeFilter filterPicture = new AndFilter(new TagNameFilter("img"), new HasAttributeFilter("class"));
		NodeList nodeLsPicture = parser.parse(filterPicture);
		lsResult = getLsDownloads(nodeLsPicture);
		return true;
	}
	
	private ArrayList<DonmaiGetDownloadUrl> getLsDownloads(NodeList nodeLsPicture) {
		ArrayList<DonmaiGetDownloadUrl> lsResult = new ArrayList<DonmaiGetDownloadUrl>();
		SimpleNodeIterator nodelist = nodeLsPicture.elements();
		int i = 0;
	    while (nodelist.hasMoreNodes()) {
	    	Node nodeUrl = nodelist.nextNode();
	    	if (isPictureExist(nodeUrl)) {
				continue;
			}
	    	
	    	String info = nodeUrl.getParent().toHtml();
	    	String[] ss = info.split(" ");
	    	for (String string : ss) {
				if (string.contains("href=")) {
					String ss2 = string.replace("href=", "").replace("\"", "").replace(">", "").trim();
					String pictureUrl = "http://www.donmai.us" + ss2;
					DonmaiGetDownloadUrl donmaiGetDownloadUrl = new DonmaiGetDownloadUrl();
					int thisPictureNum = allPictureNum - (page - 1) * 20 - i;
					donmaiGetDownloadUrl.setSavePath(savePath);
					donmaiGetDownloadUrl.setUrl(pictureUrl, page, thisPictureNum);
					donmaiGetDownloadUrl.setWebFetch(WebFetch.getInstance(webFetch));
					lsResult.add(donmaiGetDownloadUrl);
				}
			}
	    	i ++;
        }

	    return lsResult;
	}
	private boolean isPictureExist(Node nodeUrl) {
		String info = nodeUrl.toHtml();
    	String[] ss = info.split(" ");
    	for (String string : ss) {
			if (string.contains("src=")) {
				String[] ss2 = string.replace("src=", "").replace("\"", "").trim().split("/");
				String pictureUrl = "http://hijiribe.donmai.us/data/" + ss2[ss2.length - 1];
				if (pixivGetPathExistPic != null) {
					return pixivGetPathExistPic.isPicAlreadyHave(pictureUrl);
				} else {
					return false;
				}
				
			}
		}
    	return false;
	}
	public ArrayList<DonmaiGetDownloadUrl> getLsResult() {
		return lsResult;
	}
	public void setPixivGetPathExistPic(
			PixivGetPathExistPic pixivGetPathExistPic) {
		this.pixivGetPathExistPic = pixivGetPathExistPic;
		
	}

}
