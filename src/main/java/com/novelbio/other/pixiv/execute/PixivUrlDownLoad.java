package com.novelbio.other.pixiv.execute;

import java.util.AbstractQueue;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.fileOperate.FileOperate;

public class PixivUrlDownLoad {
	WebFetch webFetch;
	/** 我们给每个pixiv的编号，从大到小排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	int pictureNum;
	/** pixiv的图片ID */
	int pictureID;
	/** 是否是连环画，有子图片 */
	boolean subPicture;
	/** 如果是连环画，子图片的ID*/
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
	/** pixiv的图片ID */
	public void setPictureID(int pictureID) {
		this.pictureID = pictureID;
	}
	/** 是否是连环画，有子图片 */
	public void setSubPicture(boolean subPicture) {
		this.subPicture = subPicture;
	}
	/** 我们给每个pixiv的编号，从大到小排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}
	/** 如果是连环画，子图片的ID*/
	public void setSubID(int subID) {
		this.subID = subID;
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
