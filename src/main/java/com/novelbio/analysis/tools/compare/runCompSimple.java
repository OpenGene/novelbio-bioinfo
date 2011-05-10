package com.novelbio.analysis.tools.compare;

import java.net.URISyntaxException;
import java.util.ArrayList;

import com.novelbio.base.fileOperate.FileOperate;

public class runCompSimple {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String thisFilePath=null;
		try {
			thisFilePath = getProjectPath();
			//thisFilePath = runCompSimple.class.getResource("/").toURI().getPath();
			System.out.println(thisFilePath);
			//thisFilePath= "/home/zong0jie/桌面/CDG/Compare/XYLCompare/eee/";
			//thisFilePath=thisFilePath.substring(1);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//这个不乱码;

		ArrayList<String[]> lsFileName=FileOperate.getFoldFileName(thisFilePath, "*", "xls|txt");
		String FileA=lsFileName.get(0)[0]+"."+lsFileName.get(0)[1];
		String FileB=lsFileName.get(1)[0]+"."+lsFileName.get(1)[1];
		System.out.println(lsFileName.get(0)[0]+"."+lsFileName.get(0)[1]);
		String outputFilePath="IntersectionResults";
		FileOperate.createFolder(thisFilePath+"/"+outputFilePath);
		try {
			CompareListSimple.getFileToList(thisFilePath,outputFilePath, FileA, FileB, false,"InterSection.xls",lsFileName.get(0)[0]+"Only.xls", lsFileName.get(1)[0]+"Only.xls");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 public static String getProjectPath() {
		 java.net.URL url = runCompSimple.class.getProtectionDomain().getCodeSource().getLocation();
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
		 return filePath;
		 }
}
