package com.novelbio.other.pixiv.execute;

import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.fileOperate.FileOperate;

public class PixivUrlDownLoad implements Callable<PixivUrlDownLoad> {
	WebFetch webFetch;
	/** ���Ǹ�ÿ��pixiv�ı�ţ���С�������У�Ϊ�������ͼƬ��ʱ����Է��㰴��˳����ʾͼƬ */
	int pictureNum;
	/** pixiv��ͼƬID */
	String pictureID;
	
	String pictureUrl;
	String refUrl;
	
	String savePath;

	String name;
	boolean saveSucess = false;
	public void setWebFetch(WebFetch webFetch) {
		this.webFetch = webFetch;
	}
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isSaveSucess() {
		return saveSucess;
	}
	/** pixiv��ͼƬID */
	public void setPictureID(String pictureID) {
		this.pictureID = pictureID;
	}
	/** savePath����������� */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	/** ���Ǹ�ÿ��pixiv�ı�ţ���С�������У�Ϊ�������ͼƬ��ʱ����Է��㰴��˳����ʾͼƬ */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}

	public PixivUrlDownLoad call() {
		downloadPicture();
		return this;
	}
	/** �ɹ��ͷ���null */
    public void downloadPicture() {
    	if (pictureUrl == null) {
			saveSucess = true;
			return;
		}
    	webFetch.setUrl(pictureUrl);
    	webFetch.setRefUrl(refUrl);
    	webFetch.query();
    	if(webFetch.download(getSaveName())) {
    		saveSucess = true;
    	} else {
    		saveSucess = false;
    	}
    }

    private String getSaveName() {
    	String[] ss = pictureUrl.split("/");
    	String suffix = ss[ss.length - 1];
    	if (suffix.contains("?")) {
			suffix = suffix.split("?")[0];
		}
		String saveName = pictureNum + "_" + PixivOperate.generateoutName(name) + "_" + suffix;
		return getSavePath() + saveName;
    }
    private String getSavePath() {
		String downLoadPath = FileOperate.addSep(savePath) + FileOperate.getSepPath();
		FileOperate.createFolders(downLoadPath);
		return downLoadPath;
    }


}
