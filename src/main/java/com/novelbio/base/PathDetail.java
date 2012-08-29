package com.novelbio.base;

import java.io.File;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class PathDetail {
	public static void main(String[] args) {
		File file = new File("");
		System.out.println(file.getAbsolutePath());
		System.out.println(getProjectPathInside());
		
		
		System.out.println(getRscript());
	}
	static HashMap<String, String> mapID2Path = new HashMap<String, String>();
	static {
		TxtReadandWrite txtRead = new TxtReadandWrite(getRworkspace() + "NBCPath.txt", false);
		for (String string : txtRead.readlines()) {
			string = string.trim();
			if (string.startsWith("#")) {
				continue;
			}
			String[] ss = string.split("\t");
			if (ss.length < 2) {
				continue;
			}
			mapID2Path.put(ss[0], ss[1]);
		}
	}
	/** ����jar���ڵ�·�� */
	public static String getProjectPath() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		filePath = FileOperate.getParentPathName(filePath);
		return FileOperate.addSep(filePath);
	}
	/** ����jar�ڲ�·�� */
	public static String getProjectPathInside() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return FileOperate.addSep(filePath);
	}
	/** ����jar���ڵ�·����·���ָ�����Ϊ"/" */
	public static String getProjectPathLinux() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		filePath = FileOperate.getParentPathName(filePath);
		return FileOperate.addSep(filePath).replace("\\", "/");
	}
	/** ��ʱ�ļ����ļ��У�û�оʹ���һ�� */
	public static String getProjectConfPath() {
		String fold = PathDetail.getProjectPath() + "ConfFold" + FileOperate.getSepPath();
		if (!FileOperate.isFileFoldExist(fold)) {
			FileOperate.createFolders(fold);
		}
		return fold;
	}
	
	public static String getRworkspace() {
		return getProjectPath() + "rscript"  + FileOperate.getSepPath();
	}
	/** �ļ��ָ��Ϊ"/" */
	public static String getRworkspaceLinux() {
		return getProjectPath() + "rscript"  + FileOperate.getSepPath();
	}
	public static String getRworkspaceTmp() {
		return getRworkspace() + "tmp"  + FileOperate.getSepPath();
	}
	/** �ļ��ָ��Ϊ"/" */
	public static String getRworkspaceTmpLinux() {
		return getRworkspace() + "tmp"  + FileOperate.getSepPath();
	}
	
	public static String getRscript() {
		return mapID2Path.get("R_SCRIPT") + " ";
	}
}
