package com.novelbio.other.pixiv.execute;

import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.fileOperate.FileOperate;
import com.sun.xml.internal.rngom.util.Uri;

public class PixivUrlDownLoad implements Callable<PixivUrlDownLoad>{
	WebFetch webFetch;
	/** 我们给每个pixiv的编号，从小到大排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	int pictureNum;
	/** pixiv的图片ID */
	String pictureID;
	
	String pictureUrl;
	String refUrl;
	
	String savePath;

	String name;
	String auther;
		
	public void setWebFetch(WebFetch webFetch) {
		this.webFetch = webFetch;
	}
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
	/** pixiv的图片ID */
	public void setPictureID(String pictureID) {
		this.pictureID = pictureID;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	/** 我们给每个pixiv的编号，从小到大排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}
	/** 下载成功就什么都不返回，失败就返回自身 */
	@Override
	public PixivUrlDownLoad call() throws Exception {
		return downloadPicture();
	}
	/** 成功就返回null */
    public PixivUrlDownLoad downloadPicture() {
    	webFetch.setUrl(pictureUrl);
    	webFetch.setRefUrl(refUrl);
    	if(webFetch.download(getSaveName())) {
    		return null;
    	}
    	return this;
    }


    private String getSaveName() {
    	String[] ss = pictureUrl.split("/");
    	String suffix = ss[ss.length - 1];
		String saveName = pictureNum + "_" + name + "_" + suffix;
		return getSavePath() + saveName;
    }
    private String getSavePath() {
		String downLoadPath = FileOperate.addSep(savePath) + generateoutName(auther) + FileOperate.getSepPath();
		FileOperate.createFolders(downLoadPath);
		return downLoadPath;
    }
	 /**
	  * 因为pixiv中的作者名或文件名里面总是有各种奇怪的字符，有些不能成为文件夹名，所以要将他们替换掉
     * 输入旧文件名，将其转变为新文件名
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

}
