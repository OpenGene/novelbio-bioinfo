package com.novelbio.other.pixiv.execute;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
/** 获得本文件夹中已经存在的图片的ID
 * 方便以后如果看到该ID就可以不下载该图片了
 *  */
public class PixivGetPathExistPic {
	static PatternOperate patternOperate = new PatternOperate("\\d+", false);
	
	HashSet<String> setExistPictureID = new HashSet<String>();
	String savePath = "";
	
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
		ArrayList<String> lsFileName = FileOperate.getFoldFileNameLs(savePath, "*", "*");
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
