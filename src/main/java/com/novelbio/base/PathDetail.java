package com.novelbio.base;

import com.novelbio.base.fileOperate.FileOperate;


public class PathDetail {
	public static void main(String[] args) {
		System.out.println(getProjectPath());
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
	
	
}
