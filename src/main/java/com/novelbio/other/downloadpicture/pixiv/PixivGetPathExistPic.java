package com.novelbio.other.downloadpicture.pixiv;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
/** 获得本文件夹中已经存在的图片的ID
 * 方便以后如果看到该ID就可以不下载该图片了
 *  */
public class PixivGetPathExistPic {
	public final static int SITE_PIXIV = 2;
	public final static int SITE_DONMAI = 4;
	
	PatternOperate patternOperate;
	
	HashSet<String> setExistPictureID = new HashSet<String>();
	String savePath = "";
	/**
	 * SITE_PIXIV等
	 * @param siteType
	 */
	public PixivGetPathExistPic(int siteType) {
		if (siteType == SITE_PIXIV) {
			patternOperate = new PatternOperate("\\d+", false);
		} else if (siteType == SITE_DONMAI) {
			patternOperate = new PatternOperate("[a-z0-9A-Z]+", false);
		}
		
	}
	
	public void setSavePath(String savePath) {
		if (this.savePath.equalsIgnoreCase(savePath)) {
			return;
		}
		this.savePath = savePath;
		getFileID();
	}
	private void getFileID() {
		if (!FileOperate.isFileDirectory(savePath)) {
			return;
		}
		ArrayList<String> lsFileName = FileOperate.getLsFoldFileName(savePath, "*", "*");
		for (String string : lsFileName) {
			ArrayList<String> lsID = patternOperate.getPat(string);
			setExistPictureID.add(getLongID(lsID));
		}
	}
	
	public boolean isPicAlreadyHave(String pictureUrl) {
		ArrayList<String> lsID = patternOperate.getPat(pictureUrl);
		String pictureID = getLongID(lsID);
		if (pictureID == null) {
			return false;
		}
		if (setExistPictureID.contains(pictureID)) {
			return true;
		}
		return false;
	}
	/** 获得尾部比较长的ID */
	private String getLongID(ArrayList<String> lsID) {
		String result = null;
		if (lsID.size() > 1) {
			String id1 = lsID.get(lsID.size() - 1);
			String id2 = lsID.get(lsID.size() - 2);
			if (id1.length() > id2.length()) {
				result = id1;
			}
			else {
				result = id2;
			}
		}
		else {
			result = lsID.get(0);
		}
		return result;
	}
}
