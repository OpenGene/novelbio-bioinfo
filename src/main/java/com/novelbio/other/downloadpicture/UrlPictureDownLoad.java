package com.novelbio.other.downloadpicture;

import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.other.downloadpicture.pixiv.PixivOperate;

public class UrlPictureDownLoad implements Callable<UrlPictureDownLoad> {
	WebFetch webFetch;
	/** ���Ǹ�ÿ��pixiv�ı�ţ���С�������У�Ϊ�������ͼƬ��ʱ����Է��㰴��˳����ʾͼƬ */
	int pictureNum;
	
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
	/** ͼƬ���֣�û�п��Բ��������ֱ���������������� */
	public void setName(String name) {
		this.name = name;
	}
	public boolean isSaveSucess() {
		return saveSucess;
	}
	/** savePath����������� */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	/** ���Ǹ�ÿ��pixiv�ı�ţ���С�������У�Ϊ�������ͼƬ��ʱ����Է��㰴��˳����ʾͼƬ */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}

	public UrlPictureDownLoad call() {
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
	  * û�оͷ���""
	  * ���ļ����ͷ����»���+�ļ�����"_name"
	  * ��Ϊpixiv�е����������ļ������������и�����ֵ��ַ�����Щ���ܳ�Ϊ�ļ�����������Ҫ�������滻��
  * ������ļ���������ת��Ϊ���ļ���
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
