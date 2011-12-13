package com.novelbio.analysis.project.fy;

import java.util.ArrayList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.SAMtools;
import com.novelbio.analysis.seq.rnaseq.SplitCope;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class Splicer {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		TxtReadandWrite txtRead1 = new TxtReadandWrite(TxtReadandWrite.GZIP, "/media/新加卷/NBC/Project/Project_FY/chicken/compress/DT40_KO0h_L1_1.fq.gz");
//		TxtReadandWrite txtRead2 = new TxtReadandWrite(TxtReadandWrite.GZIP, "/media/新加卷/NBC/Project/Project_FY/chicken/compress/DT40_KO0h_L1_2.fq.gz");
//		ArrayList<String> lsInfo1 = txtRead1.readFirstLines(104);
//		ArrayList<String> lsInfo2 = txtRead2.readFirstLines(104);
//		
//		TxtReadandWrite txtRead_1 = new TxtReadandWrite("/media/winE/NBC/Project/Project_FY_Lab/clean_reads/DT40_KO0h_trimEnd_min_70_1",false);
//		TxtReadandWrite txtRead_2 = new TxtReadandWrite( "/media/winE/NBC/Project/Project_FY_Lab/clean_reads/DT40_KO0h_trimEnd_min_70_2",false);
//		ArrayList<String> lsInfo_1 = txtRead_1.readFirstLines(100);
//		ArrayList<String> lsInfo_2 = txtRead_2.readFirstLines(100);
//		for (int i = 0; i < 100; i++) {
//			System.out.println(lsInfo1.get(i+4));
//			System.out.println(lsInfo_1.get(i));
//			System.out.println(lsInfo2.get(i+4));
//			System.out.println(lsInfo_2.get(i));
//		}	
		copeInfo();
		
		
	}
	
	
	
	public static void copeInfo()
	{
		SplitCope splitCope = new SplitCope(
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/heartK0vsWT0/splicing.diff",
				"/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/heartK0vsWT0/isoforms.fpkm_tracking");
		try {
			
			splitCope
					.copeSplit("/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/heartK0vsWT0/splicing.Out5.xls", 2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changeGeneID(String inFile, String outFile)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(inFile, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		for (String string : txtRead.readlines()) {
			String geneID = string.split("\t")[8].split(";")[3].split("\"")[1];
			String geneIDold = string.split("\t")[8].split(";")[0].split("\"")[1];
			String result = string.replace(geneIDold, geneID);
			txtWrite.writefileln(result);
		}
		txtWrite.close();
	}
	
	
}
