package com.novelBio.tools.compare;

import java.net.URISyntaxException;
import java.util.ArrayList;

import com.novelBio.base.fileOperate.FileOperate;

public class runCompSimple {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String thisFilePath=null;
		try {
			thisFilePath = runCompSimple.class.getResource("/").toURI().getPath();
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

}
