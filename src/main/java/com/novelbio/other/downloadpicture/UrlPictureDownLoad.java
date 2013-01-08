package com.novelbio.other.downloadpicture;

import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.other.downloadpicture.pixiv.PixivOperate;

public class UrlPictureDownLoad implements Callable<UrlPictureDownLoad> {
	HttpFetch webFetch;
	/** 我们给每个pixiv的编号，从小到大排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	int pictureNum;
	
	String pictureUrl;
	String refUrl;
	
	String savePath;

	String name;
	boolean saveSucess = false;
	public void setWebFetch(HttpFetch webFetch) {
		this.webFetch = webFetch;
	}
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}
	/** 图片名字，没有可以不填，这样就直接用连接名做名字 */
	public void setName(String name) {
		this.name = name;
	}
	public boolean isSaveSucess() {
		return saveSucess;
	}
	/** savePath里面包含作者 */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	/** 我们给每个pixiv的编号，从小到大排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}

	public UrlPictureDownLoad call() {
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
    	String savePath = getSaveName();
    	
    	if(download(webFetch, savePath)) {
    		saveSucess = true;
    	} else {
    		saveSucess = false;
    	}
    }
    /** 下载图片 */
    private boolean download(HttpFetch webFetch, String savePath) {
    	while (webFetch.download(savePath)) {
    		if (FileOperate.getFileSize(savePath) > 2) {
    			return true;
			} else {
				FileOperate.delFile(savePath);;
				pictureUrl = FileOperate.changeFileSuffix(pictureUrl, "", "png").replace("http:/", "http://");
				webFetch.setUrl(pictureUrl);
				webFetch.query();
				savePath = FileOperate.changeFileSuffix(savePath, "", "png");
				while (webFetch.download(savePath)) {
					if (FileOperate.getFileSize(savePath) > 2) {
						return true;
					} else {
						FileOperate.delFile(savePath);
						pictureUrl = FileOperate.changeFileSuffix(pictureUrl, "", "gif").replace("http:/", "http://");;
						webFetch.setUrl(pictureUrl);
						webFetch.query();
						savePath = FileOperate.changeFileSuffix(savePath, "", "gif");
						while (webFetch.download(savePath)) {
							if (FileOperate.getFileSize(savePath) > 2) {
								return true;
							} else {
								FileOperate.delFile(savePath);
								pictureUrl = FileOperate.changeFileSuffix(pictureUrl, "", "jpeg").replace("http:/", "http://");;
								webFetch.setUrl(pictureUrl);
								webFetch.query();
								savePath = FileOperate.changeFileSuffix(savePath, "", "jpeg");
								while (webFetch.download(savePath)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
    	return false;
    }
    
    private String getSaveName() {
    	String[] ss = pictureUrl.split("/");
    	String suffix = ss[ss.length - 1];
    	if (suffix.contains("?")) {
			suffix = suffix.split("\\?")[0];
		}
    	String outName = generateoutName(name);
    	if (!outName.equals("")) {
			outName = "_" + outName;
		}
		String saveName = pictureNum + outName + "_" + suffix;
		return getSavePath() + saveName;
    }
    private String getSavePath() {
		String downLoadPath = FileOperate.addSep(savePath);
		FileOperate.createFolders(downLoadPath);
		return downLoadPath;
    }
	
	 /**
	  * 没有就返回""
	  * 有文件名就返回下划线+文件名："_name"
	  * 因为pixiv中的作者名或文件名里面总是有各种奇怪的字符，有些不能成为文件夹名，所以要将他们替换掉
  * 输入旧文件名，将其转变为新文件名
  * @param filepath
  * @param newPath
  */
 public static String generateoutName(String name) {
	  if (name == null || name.trim().equals("")) {
		return "";
	  }
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
