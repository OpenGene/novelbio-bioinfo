package com.novelbio.other.downloadpicture.donmai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.record.cont.ContinuableRecord;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.other.downloadpicture.DownloadOperate;
import com.novelbio.other.downloadpicture.UrlPictureDownLoad;
import com.novelbio.other.downloadpicture.pixiv.PixivGetPageMidUrl;
import com.novelbio.other.downloadpicture.pixiv.PixivGetPathExistPic;
import com.novelbio.other.downloadpicture.pixiv.PixivGetPictureUrlToDownload;
import com.novelbio.other.downloadpicture.pixiv.PixivOperate;

public class DonmaiOperate extends DownloadOperate {
	private static Logger logger = Logger.getLogger(DonmaiOperate.class);
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		DonmaiOperate donmaiOperate = new DonmaiOperate();
		donmaiOperate.getcookies();
		donmaiOperate.setUrlAuther("buriki");
		donmaiOperate.setSavePath("/home/zong0jie/ͼƬ/My Pictures/picture/donmai");
		donmaiOperate.running();
		
	}
	DonmaiOperate() {
		pixivGetPathExistPic = new PixivGetPathExistPic(PixivGetPathExistPic.SITE_DONMAI);
	}
	/** ����Ҫ���cookies */
	@Override
	protected void getcookies() {
		if (webFetch == null) {
			webFetch = WebFetch.getInstance();
		}
		return;
	}

	@Override
	public void setUrlAuther(String urlAutherid) {
		this.urlAuther = "http://www.donmai.us/post/index?tags=" + urlAutherid;
		autherID = urlAutherid + "";
	}

	@Override
	protected boolean setPictureNum_And_PageNum_Auther() {
		try {
			webFetch.setUrl(urlAuther);
			while (!webFetch.query(retryGetPageNum)) {
			}
			String pixivAutherInfo = webFetch.getResponse();
			Parser parser = new Parser(pixivAutherInfo);
			
			NodeFilter filterNum = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "pagination"));
			NodeList nodeListNum = parser.parse(filterNum);
			
			allPages = getNodeAllPages(nodeListNum);
			autherName = autherID;
			allPictureNum = getAllPicNum(allPages);
			savePath = FileOperate.addSep(savePath) + UrlPictureDownLoad.generateoutName(autherName) + FileOperate.getSepPath();
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
	private int getNodeAllPages(NodeList nodeListNum) {
		int num = 1;
		if (nodeListNum.size() == 0) {
			return num;
		}
		SimpleNodeIterator iteratorPages = nodeListNum.elements().nextNode().getChildren().elements();
        Node nodePage = null;
        while (iteratorPages.hasMoreNodes()) {
        	nodePage = iteratorPages.nextNode();
        	  String pageRaw = nodePage.toPlainTextString();
        	  try {
        		  int page = Integer.parseInt(pageRaw);
        		  if (page > num) {
        			  num = page;
        		  }
        	  } catch (Exception e) {
        		  // TODO: handle exception
        	  }
        }
        return num;
	}
	
	private int getAllPicNum(int allPageNum) throws Exception {
		int allPictureNum = 0;
		DonmaiGetPictureUrl donmaiGetPictureUrl = new DonmaiGetPictureUrl();
		donmaiGetPictureUrl.setUrlPage(urlAuther, allPageNum);
		donmaiGetPictureUrl.setWebFetch(webFetch);
		while (!donmaiGetPictureUrl.query()) { }
		 ArrayList<DonmaiGetDownloadUrl> lsResult = donmaiGetPictureUrl.getLsResult();
		 if (lsResult != null && lsResult.size() > 0) {
			allPictureNum = 20 * (allPageNum - 1) + lsResult.size();
		}
		return allPictureNum;
	}
	
	@Override
	protected ArrayList<DonmaiGetDownloadUrl> getLsPrepareDownload() {
		ArrayList<DonmaiGetDownloadUrl> lsResult = new ArrayList<DonmaiGetDownloadUrl>();
		pixivGetPathExistPic.setSavePath(savePath);
		for (int i = 1; i <= allPages; i++) {
			DonmaiGetPictureUrl donmaiGetPictureUrl = new DonmaiGetPictureUrl();
			donmaiGetPictureUrl.setUrlPage(urlAuther, i);
			donmaiGetPictureUrl.setSavePath(savePath);
			donmaiGetPictureUrl .setAllPictureNum(allPictureNum);
			donmaiGetPictureUrl.setWebFetch(WebFetch.getInstance(webFetch));
			donmaiGetPictureUrl.setPixivGetPathExistPic(pixivGetPathExistPic);
			while (!donmaiGetPictureUrl.query()) { }
			ArrayList<DonmaiGetDownloadUrl> lsPixivGetPictureUrlToDownloads = donmaiGetPictureUrl.getLsResult();
			lsResult.addAll(lsPixivGetPictureUrlToDownloads);
			logger.info("�ܹ�" + allPages + "ҳ���Ѿ���ȡ���" + i + "ҳ" );
			//����ļ��������Ѿ��и�ͼƬ�ˣ���ô�ͷ��أ�ʵ�����е�һ��ͼƬ�������Ϻ����ͼ�������ˣ������жϵ�һ��ͼƬ������
//			if (pixivGetPageMidUrl.isAlreadyHaveFile()) {
//				break;
//			}
		}
		return lsResult;
	}

}
