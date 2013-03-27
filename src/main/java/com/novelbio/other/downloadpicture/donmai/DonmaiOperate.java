package com.novelbio.other.downloadpicture.donmai;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.other.downloadpicture.DownloadOperate;
import com.novelbio.other.downloadpicture.UrlPictureDownLoad;
import com.novelbio.other.downloadpicture.pixiv.PixivGetPathExistPic;

public class DonmaiOperate extends DownloadOperate {

	public DonmaiOperate() {
		pixivGetPathExistPic = new PixivGetPathExistPic(PixivGetPathExistPic.SITE_DONMAI);
	}
	/** 不需要获得cookies */
	@Override
	public void getcookies() {
		if (webFetch == null) {
			webFetch = HttpFetch.getInstance();
		}
		return;
	}

	@Override
	public void setUrlAuther(String urlAutherid) {
		this.urlAuther = "http://www.donmai.us/post/index?tags=" + urlAutherid;
		autherID = urlAutherid + "";    
	}

	@Override
	protected boolean setPictureNum_And_PageNum_Auther_And_PixivGetPath() {
		try {
			webFetch.setUri(urlAuther);
			while (!webFetch.query(retryGetPageNum)) {
			}
			String pixivAutherInfo = webFetch.getResponse();
			Parser parser = new Parser(pixivAutherInfo);
			NodeFilter filterNum = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "paginator"));
			NodeList nodeListNum = parser.parse(filterNum);
			
			allPages = getNodeAllPages(nodeListNum);
			autherName = autherID;
			allPictureNum = getAllPicNum(allPages);
			savePath = FileOperate.addSep(savePath) + UrlPictureDownLoad.generateoutName(autherName) + FileOperate.getSepPath();
			pixivGetPathExistPic.setSavePath(savePath);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
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
		while (true) {
			donmaiGetPictureUrl.call();
			 ArrayList<UrlPictureDownLoad> lsResult = donmaiGetPictureUrl.getLsResult();
			 if (lsResult != null ) {
				 if (lsResult.size() > 0) {
					 allPictureNum = 20 * (allPageNum - 1) + lsResult.size();
					 break;
				}
				else {
					this.allPages = allPages - 1;
					return getAllPicNum(allPages);
				}
			}
		}
		return allPictureNum;
	}
	
	@Override
	protected ArrayList<DonmaiGetPictureUrl> getLsPrepareDownload() {
		ArrayList<DonmaiGetPictureUrl> lsResult = new ArrayList<DonmaiGetPictureUrl>();
		for (int i = 1; i <= allPages; i++) {
			DonmaiGetPictureUrl donmaiGetPictureUrl = new DonmaiGetPictureUrl();
			donmaiGetPictureUrl.setUrlPage(urlAuther, i);
			donmaiGetPictureUrl.setAllPictureNum(allPictureNum);
			donmaiGetPictureUrl.setSavePath(savePath);
			donmaiGetPictureUrl.setPixivGetPathExistPic(pixivGetPathExistPic);
			donmaiGetPictureUrl.setWebFetch(HttpFetch.getInstance(webFetch));
			lsResult.add(donmaiGetPictureUrl);
		}
		return lsResult;
	}

}
