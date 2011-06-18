package com.novelbio.analysis.seq.chipseq.peakAnnotation;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.chipseq.peakAnnotation.peakLoc.PeakLOC;
import com.novelbio.analysis.seq.chipseq.peakAnnotation.symbolAnnotation.SymbolDesp;
import com.novelbio.base.dataOperate.TxtReadandWrite;


public class PeakAnno {
	
	
	public static void main(String[] args) 
	{
		 int[] columnID=new int[3];
		columnID[0]=1;
		columnID[1]=2;
		columnID[2]=3;
		PeakLOC.prepare("",null,NovelBioConst.GENOME_GFF_TYPE_UCSC,
				"/media/winE/Bioinformatics/GenomeData/human/hg18refseqUCSCsortUsing.txt", "");
		System.out.println("prepare ok");
		annotation();

	}
	
	
	/**
	 * peak Annotation
	 */
	public static void  annotation() {
		//需要是excel文件
		String ParentFile="/media/winE/NBC/Project/Microarray_WFL110423/王凤良 基因芯片/王凤良 基因芯片/chip result/annotation/";
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=4;
		int taxID = 9606;
		//定位区域
		int[] region = new int[3];//0:UpstreamTSSbp 1:DownStreamTssbp 2:GeneEnd3UTR
		region[0] = 5000; region[1] = 3000; region[2] = 100;
		try {
			 String FpeaksFile=ParentFile+"C_vs_N_635_ratio_peaks.txt";
			 String FannotationFile=ParentFile+"C_vs_N_635_ratio_peaks_annotation.xls";
			 String FPeakHist = ParentFile; String resultPrix ="";
			 String statistics = ParentFile+ "statistics.txt";
//			 PeakLOC.histTssGeneEnd(FpeaksFile, "\t", columnID, 2, -1, FPeakHist, resultPrix);
			 
//			 PeakLOC.locatstatistic(FannotationFile, "\t", columnID, 2, -1, statistics);
			 
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
				SymbolDesp.getRefSymbDesp(taxID,FannotationFile, columnRead, rowStart, columnRead);
				SymbolDesp.getRefSymbDesp(taxID,FannotationFile, columnRead-2, rowStart, columnRead-2);
				SymbolDesp.getRefSymbDesp(taxID,FannotationFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("ok");
	}
	
	

		

	
	
}