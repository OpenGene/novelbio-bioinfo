package com.novelbio.bioinfo.tools.compare;

import java.util.List;

import com.novelbio.base.StringOperate;
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
		List<String> lsFileName = FileOperate.getLsFoldFileName(thisFilePath, "*", "xls|txt");
		String FileA=lsFileName.get(0);
		String FileB=lsFileName.get(1);
		System.out.println(FileA);
		String outputFilePath="IntersectionResults";
		FileOperate.createFolders(thisFilePath+"/"+outputFilePath);
		try {
			CompareListSimple.getFileToList(thisFilePath,outputFilePath, FileA, FileB, false,
					"InterSection.xls", FileOperate.changeFileSuffix(FileA, "_only", "xls"), FileOperate.changeFileSuffix(FileB, "_only", "xls"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 public static String getProjectPath() {
		 java.net.URL url = runCompSimple.class.getProtectionDomain().getCodeSource().getLocation();
		 String filePath = null;
		 filePath = StringOperate.decode(url.getPath());
		 if (filePath.endsWith(".jar"))
		 filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
		 java.io.File file = new java.io.File(filePath);
		 filePath = file.getAbsolutePath();
		 return filePath;
	 }
}
