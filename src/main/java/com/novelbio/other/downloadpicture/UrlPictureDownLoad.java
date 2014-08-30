package com.novelbio.other.downloadpicture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.other.downloadpicture.pixiv.PixivOperate;

public class UrlPictureDownLoad implements Callable<UrlPictureDownLoad> {
	HttpFetch httpFetch;
	/** 我们给每个pixiv的编号，从小到大排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	int pictureNum;
	
	URI pictureUrl;
	URI refUrl;
	
	String savePath;

	String name;
	boolean saveSucess = false;
	public void setWebFetch(HttpFetch webFetch) {
		this.httpFetch = HttpFetch.getInstance(webFetch);
	}
	public void setPictureUrl(String pictureUrl) {
		try {
			this.pictureUrl = new URI(pictureUrl);
		} catch (URISyntaxException e) {}
	}
	public void setRefUrl(String refUrl) {
		try {
			this.refUrl = new URI(refUrl);
		} catch (URISyntaxException e) {}
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
    	httpFetch.setUri(pictureUrl);
    	httpFetch.setRefUri(refUrl);
    	httpFetch.query();
    	String savePath = getSaveName();
    	
    	try {
			if(download(httpFetch, savePath)) {
				saveSucess = true;
		    	httpFetch.close();
			} else {
				saveSucess = false;
			}
		} catch (Exception e) {
			saveSucess = false;
		}
    }
    /** 下载图片 
     * @throws URISyntaxException 
     * @throws IOException */
    private boolean download(HttpFetch webFetch, String savePath) throws URISyntaxException, IOException {
    	String picuri = "";
    	boolean sucess = false;
    	LinkedList<String> lsSuffix = new LinkedList<String>();
    	lsSuffix.add("png"); lsSuffix.add("gif"); lsSuffix.add("jpeg");
    	while (webFetch.download(savePath)) {
    		if (FileOperate.getFileSizeLong(savePath) > 2000) {
    			sucess = true;
    			break;
			} else {
				FileOperate.delFile(savePath);
				if (lsSuffix.isEmpty()) {
					break;
				}
				String suffix = lsSuffix.poll();
				picuri = FileOperate.changeFileSuffix(pictureUrl.toString(), "", suffix);
				pictureUrl = new URI(picuri);
				webFetch.setUri(pictureUrl);
				webFetch.query();
				savePath = FileOperate.changeFileSuffix(savePath, "", suffix);
			}
		}
    	if (sucess && savePath.endsWith(".png")) {
			BufferedImage bufferedImage = ImageUtils.read(savePath);
			if (bufferedImage == null) {
				return false;
			}
			if (bufferedImage.getWidth() > 5000) {
				int w = (int) (bufferedImage.getWidth() * 0.6); 
				int h = (int) (bufferedImage.getHeight() * 0.6);
				bufferedImage = ImageUtils.resizeImage(bufferedImage, w, h);
			} else if (bufferedImage.getWidth() > 4000) {
				int w = (int) (bufferedImage.getWidth() * 0.7); 
				int h = (int) (bufferedImage.getHeight() * 0.7);
				bufferedImage = ImageUtils.resizeImage(bufferedImage, w, h);
			} else if (bufferedImage.getWidth() > 3000) {
				int w = (int) (bufferedImage.getWidth() * 0.9); 
				int h = (int) (bufferedImage.getHeight() * 0.9);
				bufferedImage = ImageUtils.resizeImage(bufferedImage, w, h);
			}
			String outSavePath = FileOperate.changeFileSuffix(savePath, null, "jpg");
			ImageUtils.saveBufferedImage(bufferedImage, outSavePath);
			FileOperate.delFile(savePath);
		}
    	
    	return sucess;
    }
    
    private String getSaveName() {
    	String[] ss = pictureUrl.toString().split("/");
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
