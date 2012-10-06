package com.novelbio.other.pixiv.execute;

import java.util.AbstractQueue;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.fileOperate.FileOperate;

public class PixivUrlDownLoad {
	WebFetch webFetch;
	/** ���Ǹ�ÿ��pixiv�ı�ţ��Ӵ�С���У�Ϊ�������ͼƬ��ʱ����Է��㰴��˳����ʾͼƬ */
	int pictureNum;
	/** pixiv��ͼƬID */
	int pictureID;
	/** �Ƿ���������������ͼƬ */
	boolean subPicture;
	/** ���������������ͼƬ��ID*/
	int subID;
	
	String pictureUrl;
	String refUrl;
	
	String savePath;

	String name;
	String auther;
	
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}
	public void setAuther(String auther) {
		this.auther = auther;
	}
	public void setName(String name) {
		this.name = name;
	}
	/** pixiv��ͼƬID */
	public void setPictureID(int pictureID) {
		this.pictureID = pictureID;
	}
	/** �Ƿ���������������ͼƬ */
	public void setSubPicture(boolean subPicture) {
		this.subPicture = subPicture;
	}
	/** ���Ǹ�ÿ��pixiv�ı�ţ��Ӵ�С���У�Ϊ�������ͼƬ��ʱ����Է��㰴��˳����ʾͼƬ */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}
	/** ���������������ͼƬ��ID*/
	public void setSubID(int subID) {
		this.subID = subID;
	}
	
	 /**
	  * ��Ϊpixiv�е����������ļ������������и�����ֵ��ַ�����Щ���ܳ�Ϊ�ļ�����������Ҫ�������滻��
     * ������ļ���������ת��Ϊ���ļ���
     * @param filepath
     * @param newPath
     */
    private String generateoutName(String name) {
    		String outName;
    		outName = name.replace("\\", "");
    		outName = outName.replace("/", "");
    		outName= outName.replace("\"", "");
    		outName = outName.replace("*", "");
    		outName = outName.replace("?", "");
    		outName = outName.replace("<", "");
    		outName = outName.replace(">", "");
    		outName = outName.replace("|", "");
    		return outName;
    }
    private String getSavePath() {
		String downLoadPath = FileOperate.addSep(savePath) + generateoutName(auther) + FileOperate.getSepPath();
		FileOperate.createFolders(downLoadPath);
		return downLoadPath;
    }
    private String getSaveName() {
    		String saveName = pictureNum + "_" + name + "_" + pictureID;
    		if (subPicture) {
    			saveName = saveName + "_" + subID;
    		}
    		return getSavePath() + saveName;
    }
    public boolean downloadPicture(WebFetch webFetch) {
    		webFetch.setUrl(pictureUrl);
    		webFetch.setRefUrl(refUrl);
    		boolean sucessQuery = webFetch.query();
    		boolean sucessSave = webFetch.download(getSaveName());
    		return sucessQuery && sucessSave;
    }
    
    

}
