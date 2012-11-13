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

/** ��������pixiv��ͼƬ */
public class PixivOperate extends DownloadOperate{
	private static Logger logger = Logger.getLogger(PixivOperate.class);

	String name = "facemun";
	String password = "f12344321n";
	
	public PixivOperate() {
		pixivGetPathExistPic = new PixivGetPathExistPic(PixivGetPathExistPic.SITE_PIXIV);
		getcookies();
	}
	/**
	 * ���pixiv��cookies
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
    	webFetch.setUrl("http://www.pixiv.net/index.php");
    	if (!webFetch.query()) {
			getcookies();
		}
   }
    /**
     * @param urlAuther ��id
     */
	public void setUrlAuther(String urlAutherid) {
		this.urlAuther = "http://www.pixiv.net/member_illust.php?id=" + urlAutherid;
		autherID = urlAutherid + "";
		
	}
	
	/**
	 * ����ܹ���ҳ
	 * @return �����Ƿ����óɹ�
	 */
	protected boolean setPictureNum_And_PageNum_Auther_And_PixivGetPath() {
		try {
			webFetch.setUrl(urlAuther);
			while (!webFetch.query(retryGetPageNum)) {
				Thread.sleep(500);
			}

			String pixivAutherInfo = webFetch.getResponse();
			Parser parser = new Parser(pixivAutherInfo);
			
			NodeFilter filterNum = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "count-badge"));
			NodeList nodeListNum = parser.parse(filterNum);
			allPictureNum = getNodeAllPicture(nodeListNum);
			
			parser = new Parser(pixivAutherInfo);
			NodeFilter filterName = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "avatar_m"));
			NodeList nodeAutherName = parser.parse(filterName);
			autherName = getAuterName(nodeAutherName) + "_" + autherID;
			savePath = FileOperate.addSep(savePath) + UrlPictureDownLoad.generateoutName(autherName) + FileOperate.getSepPath();
			allPages = (int) Math.ceil((double)allPictureNum/20);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
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
        String pageRaw = nodeNumBefore.toPlainTextString();
        String pageNum = pageRaw.replace("��", "").trim();
        return Integer.parseInt(pageNum);
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
			logger.error("�ܹ�" + allPages + "ҳ���Ѿ���ȡ���" + i + "ҳ" );
			//����ļ��������Ѿ��и�ͼƬ�ˣ���ô�ͷ��أ�ʵ�����е�һ��ͼƬ�������Ϻ����ͼ�������ˣ������жϵ�һ��ͼƬ������
//			if (pixivGetPageMidUrl.isAlreadyHaveFile()) {
//				break;
//			}
		}
		return lsResult;
	}



}