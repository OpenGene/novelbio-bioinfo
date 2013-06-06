package com.novelbio.analysis.tools.arrayTools;



public class runSepFile {

	/**
	 * @param args
	 * 将excel分割成指定的格式，方便导入arraytools
	 */
	public static void main(String[] args) {
//		String thisFilePath=null;
//		try {
//			thisFilePath = runSepFile.class.getResource("/").toURI().getPath();
//			//thisFilePath=thisFilePath.substring(1);
//		} catch (URISyntaxException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();/
//		}//这个不乱码;
//		System.out.println(thisFilePath);
//		System.out.println("test");
//		ArrayList<String[]> lsFileName=FileOperate.getFoldFileName(thisFilePath, "*", "xls");
//		String excelFile=lsFileName.get(0)[0]+"."+lsFileName.get(0)[1];
//		System.out.println(lsFileName.get(0)[0]+"."+lsFileName.get(0)[1]);
		
		String thisFilePath = "/home/zong0jie/desktop/";
		String excelFile = "aa.xls";
		try {
			ArrayTools.seperateFile(thisFilePath,excelFile,"ArrayToolsInput");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
