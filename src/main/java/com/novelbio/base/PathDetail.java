package com.novelbio.base;

import com.novelbio.base.fileOperate.FileOperate;


public class PathDetail {
	public static void main(String[] args) {
		System.out.println(getProjectPath());
	}
	/** 返回jar所在的路径 */
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
}
