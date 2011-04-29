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
		PeakLOC.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM,columnID, NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, "");
		System.out.println("prepare ok");
		annotation();

	}
	
	
	/**
	 * peak Annotation
	 */
	public static void  annotation() {
		//需要是excel文件
		String ParentFile="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/";
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=6;
		//定位区域
		int[] region = new int[3];//0:UpstreamTSSbp 1:DownStreamTssbp 2:GeneEnd3UTR
		region[0] = 3000; region[1] = 3000; region[2] = 100;
		try {
			 String FpeaksFile=ParentFile+"CSA sepis peak Filter.xls";
			 String FannotationFile=ParentFile+"CSA sepis peak Filter_annotation.xls";
			 String FPeakHist = ParentFile; String resultPrix ="CSA";
			 String statistics = ParentFile+ "statistics.txt";
			 PeakLOC.histTssGeneEnd(FpeaksFile, "\t", columnID, 2, -1, FPeakHist, resultPrix);
			 
			 PeakLOC.locatstatistic(FannotationFile, "\t", columnID, 2, -1, statistics);
			 
			 PeakLOC.locatDetail(FpeaksFile, "\t", columnID,2, -1, FannotationFile,region);
			 
			 TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
				txtReadandWrite.setParameter(FannotationFile, false, true);
				int columnNum=0;
				try {
					columnNum = txtReadandWrite.ExcelColumns("\t");
				} catch (Exception e2) {
				}
				int columnRead=columnNum-1;
				int rowStart=2;
				SymbolDesp.getRefSymbDesp(39947,FannotationFile, columnRead, rowStart, columnRead);
				SymbolDesp.getRefSymbDesp(39947,FannotationFile, columnRead-2, rowStart, columnRead-2);
				SymbolDesp.getRefSymbDesp(39947,FannotationFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("ok");
	}
	
	

		

	
	
}