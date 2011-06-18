package com.novelbio.analysis.seq.chipseq.readsChrDensity;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;


public class runReadsInRegion {

	/**
	 * 读取包含有 chrID startLoc, endLoc 信息的文件，定制化制作reads在某个区域中的密度图，和Tss类似
	 * @param args
	 */
	public static void main(String[] args) 
	{
		///**mouse
		String mapFFile="/media/winG/NBC/Project/ChIP-SeqCDG20100911/SIPeS_Peaks_CDG100923/46/mapping/Rfragment.fasta";
		String mapRFile="/media/winG/NBC/Project/ChIP-SeqCDG20100911/SIPeS_Peaks_CDG100923/NP/mapping/Rfragment.fasta";
		
		String chrFilePath="/media/winG/bioinformation/GenomeData/ucsc_mm9/chromFa";
		String txtFile="";//包含有 chrID startLoc, endLoc 信息的文件，这个也就是定制化制作reads在某个区域中的密度图，和Tss类似
		int[] txtcolumnID=new int[3];txtcolumnID[0]=1;txtcolumnID[1]=2;txtcolumnID[2]=3;
		String RworkSpace="";
		
		int binNum=50;//将区域分割为多少份
		
		//**/
		/**human
		String mapFile="/media/winG/NBC/Program/ChIP-Seq-WJK100909/mapping/fragment_tab.fasta";
		String chrFilePath="/media/winG/bioinformation/GenomeData/HumanUCSChg19/ChromFa";
		**/
		String sep=" "; 
		int invNum=10;
		int tagLength=300;
		String[][] regionInfo=null;
		try {
			regionInfo = ExcelTxtRead.readtxtExcel(txtFile, "\t", txtcolumnID, 2, -1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ReadsInRegion readsInRegion=new ReadsInRegion();
		try {
			double[] result=readsInRegion.getRegionReads(mapFFile, chrFilePath, sep, 0, 1, 2, 10, 400,regionInfo, binNum);
			TxtReadandWrite tssReadandWrite=new TxtReadandWrite();
			tssReadandWrite.setParameter("/media/winG/bioinformation/R/practice_script/platform/tmp/tss.txt", true,false);
			
			try { tssReadandWrite.Rwritefile(result); 	} catch (Exception e) { 	e.printStackTrace(); }
			try {density(RworkSpace);	} catch (Exception e) {	e.printStackTrace();}
//			FileOperate.changeFileName("/media/winG/bioinformation/R/practice_script/platform/tmp/tss.txt", newName);
			//FileOperate.DeleteFolder("/media/winG/bioinformation/R/practice_script/platform/tmp/tss.txt");
 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	private static void density(String RworkSpace) throws Exception{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+RworkSpace+ "MyRegionReads.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		System.out.println("ok");
	}
}
