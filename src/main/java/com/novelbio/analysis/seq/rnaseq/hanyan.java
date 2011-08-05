package com.novelbio.analysis.seq.rnaseq;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.base.fileOperate.FileOperate;


public class hanyan {
	
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		fileterFile("/media/winE/NBC/Project/Project_HY_Lab/TSC2_WT/TSC2 WTfilterPolyA.txt", "/media/winE/NBC/Project/Project_HY_Lab/TSC2_WT/Hanyan", "AG","small","TG","medium","AC","large");
	}
	
	private static void fileterFile(String fileName,String outFilePath,String...barcodeAndName) throws Exception {
		FastQ fastQ = new FastQ(fileName, FastQ.QUALITY_MIDIAN);
//		fastQ = fastQ.filterReads(FileOperate.getParentPathName(fileName)+"/"+FileOperate.getFileNameSep(fileName)[0] +"filter." +FileOperate.getFileNameSep(fileName)[1]);
//		fastQ = fastQ.trimPolyA(22,FileOperate.getParentPathName(fileName)+"/"+FileOperate.getFileNameSep(fileName)[0] +"PolyA." +FileOperate.getFileNameSep(fileName)[1]);
		fastQ.filterBarcode(outFilePath, 0, barcodeAndName);
	}
}
