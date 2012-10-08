package com.novelbio.other.pixiv.execute;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
/** ��ñ��ļ������Ѿ����ڵ�ͼƬ��ID
 * �����Ժ����������ID�Ϳ��Բ����ظ�ͼƬ��
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
	/** ���β���Ƚϳ���ID */
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
