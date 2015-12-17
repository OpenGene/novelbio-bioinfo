package com.novelbio.other.downloadpicture.pixiv;

import java.util.ArrayList;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.HttpFetch;

public class PixivGetPageMidUrl {
	HttpFetch webFetch;
	/** 判断该文件是否已经存在了，存在了就不下载了 */
	PixivGetPathExistPic pixivGetPathExistPic;
	
	String pixivUrl = "http://www.pixiv.net/";
	String pageUrl;
	int allPictureNum;
	int allPageNum;
	int thisPageNum;

	int retryNum = 100;
	
	String savePath;
	
	ArrayList<String[]> lsNameAndUrl;
	
	boolean isAlreadyHaveFile = false;
	
	public void setWebFetch(HttpFetch webFetch) {
		this.webFetch = HttpFetch.getInstance(webFetch);
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	public void setAllPageNum(int allPageNum) {
		this.allPageNum = allPageNum;
	}
	public void setThisPageNum(int thisPageNum) {
		this.thisPageNum = thisPageNum;
	}
	public void setAllPictureNum(int allPictureNum) {
		this.allPictureNum = allPictureNum;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	public void setPixivGetPathExistPic(PixivGetPathExistPic pixivGetPathExistPic) {
		this.pixivGetPathExistPic = pixivGetPathExistPic;
	}
	
	public boolean isAlreadyHaveFile() {
		return isAlreadyHaveFile;
	}
	/** 返回null 表示没有成功 */
	public ArrayList<PixivGetPictureUrlToDownload> getLsToDownloadUrl() {
		try {
			if(!getPictures()) {
				webFetch.readResponse();
				return null;
			}
		} catch (ParserException e) {
			webFetch.readResponse();
			return null;
		}
		webFetch.close();
		return getLsPixivGetPictureUrlToDownloads();
	}
	/**
	 * 获得待下载的信息
	 * 没有成功则返回null
	 * @return
	 * @throws ParserException 
	 */
	private boolean getPictures() throws ParserException {
		webFetch.setUri(pageUrl);
		if (!webFetch.query(retryNum)) {
			return false;
		}
		String pageInfo = webFetch.getResponse();
		Parser parser = new Parser(pageInfo);
		
		NodeFilter filterPicture = new AndFilter(new TagNameFilter("ul"), new HasAttributeFilter("class", "_image-items"));
		NodeList nodeListPicture = parser.parse(filterPicture);
		lsNameAndUrl = getPictureMidUrl(nodeListPicture);
		return true;
	}
	/** 获得中等图片的url */
	private ArrayList<String[]> getPictureMidUrl(NodeList nodeListPicture) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		NodeFilter filterPicture = new AndFilter(new TagNameFilter("li"), new HasAttributeFilter("class", "image-item"));
		//包含有全部图片的nodelist
		NodeList nodeLsPicture = nodeListPicture.extractAllNodesThatMatch(filterPicture, true);
		
		SimpleNodeIterator iterator = nodeLsPicture.elements();
        while (iterator.hasMoreNodes()) {
        	//每个图片的node
            Node nodePicture = iterator.nextNode();
            String name = getPictureName(nodePicture);
            String url = getPictureUrl(nodePicture);
            if (pixivGetPathExistPic.isPicAlreadyHave(url)) {
				isAlreadyHaveFile = true;
				continue;
			}
            lsResult.add(new String[]{name, url});
        }
        return lsResult;
	}
	/** 获得每个图片的url */
	private String getPictureUrl(Node nodePicture) {
		String urlAll = nodePicture.getFirstChild().getText();
		 urlAll = StringOperate.decode(urlAll).trim();
		 urlAll = urlAll.split("href=")[1].replace("\"", "").trim();
		 urlAll = urlAll.split(" ")[0];
		 if (urlAll.startsWith("/")) {
			urlAll = urlAll.substring(1);
		}
		return urlAll;
	}
	/** 获得每个图片的名字 */
	private String getPictureName(Node nodePicture) {
		String name = nodePicture.getLastChild().toPlainTextString();
		return name;
//		NodeFilter filterUrl = new TagNameFilter("img");
//		//只可能有一个url
//		NodeList nodelsUrl = nodePicture.getChildren().extractAllNodesThatMatch(filterUrl);
//		String allName = nodelsUrl.elementAt(0).getText();
//		String[] ss = allName.split(" ");
//		for (String string : ss) {
//			if (string.contains("title")) {
//				name = string.replace("title=", "").replace("\"", "");
//				name = name.substring(0,name.length() - 1);
//				break;
//			}
//		}
//		return name;
	}
	
	/**
	 * 返回设定好midurl的类，等待每个深入下去查找具体的bigurl
	 * @return
	 */
	private ArrayList<PixivGetPictureUrlToDownload> getLsPixivGetPictureUrlToDownloads() {
		ArrayList<PixivGetPictureUrlToDownload> lsResult = new ArrayList<PixivGetPictureUrlToDownload>();
		int i = 0;
		for (String[] nameAndUrl : lsNameAndUrl) {
			PixivGetPictureUrlToDownload pictureUrlToDownload = new PixivGetPictureUrlToDownload();
			pictureUrlToDownload.setMidUrl(pixivUrl + nameAndUrl[1]);
			pictureUrlToDownload.setName(nameAndUrl[0]);
			//每页20张图
			int pictureNum = allPictureNum - (thisPageNum - 1) * 20 - i;
			pictureUrlToDownload.setPictureNum(pictureNum);
			pictureUrlToDownload.setWebFetch(webFetch);
			pictureUrlToDownload.setSavePath(savePath);
			lsResult.add(pictureUrlToDownload);
			i ++;
		}
		return lsResult;
	}

}
