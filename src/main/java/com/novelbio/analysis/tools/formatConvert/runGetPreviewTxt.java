package com.novelbio.analysis.tools.formatConvert;


public class runGetPreviewTxt 
{
	public static void main(String[] args) {
		try {
			PreviewTxt.getFileHead("/Volumes/DATA/myData/Desktop/tmp/mGL.mapped", "/Volumes/DATA/myData/Desktop/tmp/soap.txt", 1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
