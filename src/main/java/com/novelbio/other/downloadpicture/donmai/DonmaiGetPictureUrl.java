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
import org.htmlparser.util.SimpleNodeIterator;

import net.sf.picard.annotation.Gene.Transcript.Exon;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.other.downloadpicture.GetPictureUrl;
import com.novelbio.other.downloadpicture.UrlPictureDownLoad;
import com.novelbio.other.downloadpicture.pixiv.PixivGetPathExistPic;

/** ����һ��ͼƬ���ӣ����ظ�ͼƬ�Ĵ�ͼ���� */
public class DonmaiGetPictureUrl implements GetPictureUrl {
	private static Logger logger = Logger.getLogger(DonmaiGetPictureUrl.class);
	protected HttpFetch webFetch;
	int retryNum = 10;
	String urlpage;
	int page;
	String savePath;
	
	int allPictureNum;
	
	PixivGetPathExistPic pixivGetPathExistPic;
	
	ArrayList<UrlPictureDownLoad> lsResult;
	@Override
	public void setWebFetch(HttpFetch webFetch) {
		this.webFetch = webFetch;
	}
	public void setPixivGetPathExistPic(PixivGetPathExistPic pixivGetPathExistPic) {
		this.pixivGetPathExistPic = pixivGetPathExistPic;
	}
	public void setUrlPage(String url, int pageNum) {
		this.page = pageNum;
		if (pageNum == 1) {
			this.urlpage = url;
			return;
		}
		else {
			this.urlpage = url + "&page=" + pageNum;
		}
	}
	public void setAllPictureNum(int allPictureNum) {
		this.allPictureNum = allPictureNum;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	@Override
	public GetPictureUrl call() throws Exception {
		webFetch.setUrl(urlpage);
		if (!webFetch.query(retryNum)) {
			lsResult = null;
			return this;
		}
		String info = webFetch.getResponse();
		if (info == null) {
			logger.error("ûץ��������" + urlpage);
			lsResult = new ArrayList<UrlPictureDownLoad>();
			return this;
		}
		Parser parser = new Parser(info);
		NodeFilter filterPicture = new AndFilter(new TagNameFilter("img"), new HasAttributeFilter("class"));
		NodeList nodeLsPicture = parser.parse(filterPicture);
		lsResult = getLsDownloads(nodeLsPicture);
		return this;
	}
	private ArrayList<UrlPictureDownLoad> getLsDownloads(NodeList nodeLsPicture) {
		ArrayList<UrlPictureDownLoad> lsResult = new ArrayList<UrlPictureDownLoad>();
		SimpleNodeIterator nodelist = nodeLsPicture.elements();
		int i = 0;
	    while (nodelist.hasMoreNodes()) {
	    	String info = nodelist.nextNode().toHtml();
	    	String[] ss = info.split(" ");
	    	for (String string : ss) {
				if (string.contains("src=")) {
					String[] ss2 = string.replace("src=", "").replace("\"", "").trim().split("/");
					String pictureUrl = "http://hijiribe.donmai.us/data/" + ss2[ss2.length - 1];
					if (pixivGetPathExistPic != null && pixivGetPathExistPic.isPicAlreadyHave(pictureUrl)) {
						continue;
					}
					UrlPictureDownLoad urlPictureDownLoad = new UrlPictureDownLoad();
					int thisPictureNum = allPictureNum - (page - 1) * 20 - i;
					urlPictureDownLoad.setPictureNum(thisPictureNum);
					urlPictureDownLoad.setPictureUrl(pictureUrl);
					urlPictureDownLoad.setSavePath(savePath);
					urlPictureDownLoad.setWebFetch(HttpFetch.getInstance(webFetch));
					lsResult.add(urlPictureDownLoad);
				}
			}
	    	i ++;
        }
	    logger.info("������ϣ�" + urlpage);
	    return lsResult;
	}
	@Override
	public ArrayList<UrlPictureDownLoad> getLsResult() {
		return lsResult;
	}

}