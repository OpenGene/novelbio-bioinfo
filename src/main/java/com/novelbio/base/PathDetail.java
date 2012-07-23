package com.novelbio.base;

import com.novelbio.base.fileOperate.FileOperate;


public class PathDetail {
	 public static String getProjectPath() {
		 java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		 String filePath = null;
		 try {
		 filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		 if (filePath.endsWith(".jar"))
		 filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
		 java.io.File file = new java.io.File(filePath);
		 filePath = file.getAbsolutePath();
		 return FileOperate.addSep(filePath);
		 }
}
