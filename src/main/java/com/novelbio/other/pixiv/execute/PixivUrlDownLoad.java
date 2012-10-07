package com.novelbio.other.pixiv.execute;

import java.util.concurrent.Callable;

import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.fileOperate.FileOperate;
import com.sun.xml.internal.rngom.util.Uri;

public class PixivUrlDownLoad implements Callable<PixivUrlDownLoad>{
	WebFetch webFetch;
	/** ���Ǹ�ÿ��pixiv�ı�ţ���С�������У�Ϊ�������ͼƬ��ʱ����Է��㰴��˳����ʾͼƬ */
	int pictureNum;
	/** pixiv��ͼƬID */
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
	/** pixiv��ͼƬID */
	public void setPictureID(String pictureID) {
		this.pictureID = pictureID;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	/** ���Ǹ�ÿ��pixiv�ı�ţ���С�������У�Ϊ�������ͼƬ��ʱ����Է��㰴��˳����ʾͼƬ */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}
	/** ���سɹ���ʲô�������أ�ʧ�ܾͷ������� */
	@Override
	public PixivUrlDownLoad call() throws Exception {
		return downloadPicture();
	}
	/** �ɹ��ͷ���null */
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

}
