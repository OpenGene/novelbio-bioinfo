package com.novelBio.chIPSeq.peakAnnotation;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.chIPSeq.peakAnnotation.peakLoc.PeakLOC;
import com.novelBio.chIPSeq.peakAnnotation.symbolAnnotation.SymbolDesp;
import com.novelBio.generalConf.NovelBioConst;


public class PeakAnno {
	
	
	public static void main(String[] args) 
	{
		 int[] columnID=new int[3];
		columnID[0]=1;
		columnID[1]=2;
		columnID[2]=3;
		PeakLOC.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM,columnID, "TIGR", NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, "");
		System.out.println("prepare ok");
		annotation();

	}
	
	
	/**
	 * peak Annotation
	 */
	public static void  annotation() {
		//需要是excel文件
		String ParentFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/annotation/";
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=6;
		try {
			 String FpeaksFile=ParentFile+"FT_macsPeak_peaks.xls";
			 String FannotationFile=ParentFile+"FT5_annotation2.xls";
			 PeakLOC.locatDetail(FpeaksFile, "\t", columnID,2, -1, FannotationFile);
			 TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
				txtReadandWrite.setParameter(FannotationFile, false, true);
				int columnNum=0;
				try {
					columnNum = txtReadandWrite.ExcelColumns("\t");
				} catch (Exception e2) {
				}
				int columnRead=columnNum-1;
				int rowStart=2;
				SymbolDesp.getRefSymbDesp(FannotationFile, columnRead, rowStart, columnRead);
				SymbolDesp.getRefSymbDesp(FannotationFile, columnRead-2, rowStart, columnRead-2);
				SymbolDesp.getRefSymbDesp(FannotationFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok");
	}
}