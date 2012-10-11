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

import com.novelbio.base.dataOperate.WebFetch;

public class PixivGetPageMidUrl {
	WebFetch webFetch;
	/** �жϸ��ļ��Ƿ��Ѿ������ˣ������˾Ͳ������� */
	PixivGetPathExistPic pixivGetPathExistPic;
	
	String pixivUrl = "http://www.pixiv.net/";
	String pageUrl;
	int allPictureNum;
	int allPageNum;
	int thisPageNum;
	/** ��ҳ�ж���ͼƬ */
	int thisPagePictureNum;
	int retryNum = 100;
	
	String savePath;
	
	ArrayList<String[]> lsNameAndUrl;
	
	boolean isAlreadyHaveFile = false;
	
	public void setWebFetch(WebFetch webFetch) {
		this.webFetch = webFetch;
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
	/** ����null ��ʾû�гɹ� */
	public ArrayList<PixivGetPictureUrlToDownload> getLsToDownloadUrl() {
		try {
			if(!getPictures()) {
				return null;
			}
		} catch (ParserException e) {
			return null;
		}
		return getLsPixivGetPictureUrlToDownloads();
	}
	/**
	 * ��ô����ص���Ϣ
	 * û�гɹ��򷵻�null
	 * @return
	 * @throws ParserException 
	 */
	private boolean getPictures() throws ParserException {
		webFetch.setUrl(pageUrl);
		if (!webFetch.query(retryNum)) {
			return false;
		}
		Parser parser = new Parser(webFetch.getResponse());
		NodeFilter filterPicture = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "display_works linkStyleWorks"));
		NodeList nodeListPicture = parser.parse(filterPicture);
		lsNameAndUrl = getPictureMidUrl(nodeListPicture);
		return true;
	}
	/** ����е�ͼƬ��url */
	private ArrayList<String[]> getPictureMidUrl(NodeList nodeListPicture) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		NodeFilter filterPicture = new TagNameFilter("a");
		//������ȫ��ͼƬ��nodelist
		NodeList nodeLsPicture = nodeListPicture.extractAllNodesThatMatch(filterPicture, true);
		thisPagePictureNum = nodeListPicture.size();
		
		SimpleNodeIterator iterator = nodeLsPicture.elements();
        while (iterator.hasMoreNodes()) {
        	//ÿ��ͼƬ��node
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
	/** ���ÿ��ͼƬ��url */
	private String getPictureUrl(Node nodePicture) {
		String urlAll = nodePicture.getText();
		 urlAll = urlAll.replace("a", "").replace("href=", "").replace("\"", "").trim();
		 urlAll = WebFetch.decode(urlAll);
		return urlAll;
	}
	/** ���ÿ��ͼƬ������ */
	private String getPictureName(Node nodePicture) {
		String name = "";
		NodeFilter filterUrl = new TagNameFilter("img");
		//ֻ������һ��url
		NodeList nodelsUrl = nodePicture.getChildren().extractAllNodesThatMatch(filterUrl);
		String allName = nodelsUrl.elementAt(0).getText();
		String[] ss = allName.split(" ");
		for (String string : ss) {
			if (string.contains("title")) {
				name = string.replace("title=", "").replace("\"", "");
				name = name.substring(0,name.length() - 1);
				break;
			}
		}
		return name;
	}
	
	/**
	 * �����趨��midurl���࣬�ȴ�ÿ��������ȥ���Ҿ����bigurl
	 * @return
	 */
	private ArrayList<PixivGetPictureUrlToDownload> getLsPixivGetPictureUrlToDownloads() {
		ArrayList<PixivGetPictureUrlToDownload> lsResult = new ArrayList<PixivGetPictureUrlToDownload>();
		int i = 0;
		for (String[] nameAndUrl : lsNameAndUrl) {
			PixivGetPictureUrlToDownload pictureUrlToDownload = new PixivGetPictureUrlToDownload();
			pictureUrlToDownload.setMidUrl(pixivUrl + nameAndUrl[1]);
			pictureUrlToDownload.setName(nameAndUrl[0]);
			//ÿҳ20��ͼ
			int pictureNum = allPictureNum - (thisPageNum - 1) * 20 - i;
			pictureUrlToDownload.setPictureNum(pictureNum);
			pictureUrlToDownload.setWebFetch(WebFetch.getInstance(webFetch));
			pictureUrlToDownload.setSavePath(savePath);
			lsResult.add(pictureUrlToDownload);
			i ++;
		}
		return lsResult;
	}

}
