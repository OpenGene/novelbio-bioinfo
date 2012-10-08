package com.novelbio.other.pixiv.execute;

import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.fileOperate.FileOperate;

public class PixivUrlDownLoad implements Callable<PixivUrlDownLoad> {
	WebFetch webFetch;
	/** 我们给每个pixiv的编号，从小到大排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	int pictureNum;
	/** pixiv的图片ID */
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
	/** pixiv的图片ID */
	public void setPictureID(String pictureID) {
		this.pictureID = pictureID;
	}
	/** savePath里面包含作者 */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	/** 我们给每个pixiv的编号，从小到大排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}

	public PixivUrlDownLoad call() {
		downloadPicture();
		return this;
	}
	/** 成功就返回null */
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
