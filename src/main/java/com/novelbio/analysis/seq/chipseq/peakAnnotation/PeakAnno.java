package com.novelbio.analysis.seq.chipseq.peakAnnotation;

import com.novelbio.analysis.seq.chipseq.peakAnnotation.peakLoc.PeakLOC;
import com.novelbio.analysis.seq.chipseq.peakAnnotation.symbolAnnotation.SymbolDesp;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;


public class PeakAnno {
	
	
	public static void main(String[] args) 
	{
//		 int[] columnID=new int[3];
//		columnID[0]=1;
//		columnID[1]=2;
//		columnID[2]=3;
//		PeakLOC.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM,null,NovelBioConst.GENOME_GFF_TYPE_UCSC,
//				NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, "");
		PeakLOC.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM,null,NovelBioConst.GENOME_GFF_TYPE_TIGR,
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, "");
//		PeakLOC.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM,null,NovelBioConst.GENOME_GFF_TYPE_UCSC,
//				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, "");
//		PeakLOC.prepare("/media/winE/Bioinformatics/GenomeData/Arabidopsis/ChromFaRaw",null,NovelBioConst.GENOME_GFF_TYPE_TAIR,
//				"/media/winE/Bioinformatics/GenomeData/Arabidopsis/TAIR9_GFF3_genes.gff", "");
		System.out.println("prepare ok");
		annotation();
	}
	
	/**
	 * peak Annotation
	 */
	public static void  annotation() {
		//��Ҫ��excel�ļ�
		String ParentFile="/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/compareSICER/single/";
		String geneStructurePath = FileOperate.createFolders(FileOperate.getParentPathName(ParentFile), "genestructure") + "/";
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=5;
		int taxID = 39947;
		//��λ����
		int[] region = new int[3];//0:UpstreamTSSbp 1:DownStreamTssbp 2:GeneEnd3UTR
		region[0] = 1500; region[1] = 1500; region[2] = 100;
		
		int upBp = region[0];
//		region[0] = 1000; region[1] = 1000; region[2] = 100000;
//		int upBp = 100000;
		try {
			 String FpeaksFile=ParentFile+"3NseSort-W200-G200-E100.scoreisland.xls";
			 String resultPrix ="3N";

			 String FannotationFile= FileOperate.changeFileSuffix(FpeaksFile, "_annotation", null);
			 String FPeakHist = ParentFile; 
			 PeakLOC.histTssGeneEnd(FpeaksFile, "\t", columnID, 2, -1, FPeakHist, resultPrix);
			 
			 statisticNum(upBp,FpeaksFile, columnID,geneStructurePath, resultPrix);
			 
//			 PeakLOC.locatDetail(FpeaksFile, "\t", columnID,2, -1, FannotationFile,region);
//			 
//			 TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
//				txtReadandWrite.setParameter(FannotationFile, false, true);
//				int columnNum=0;
//				try {
//					columnNum = txtReadandWrite.ExcelColumns("\t");
//				} catch (Exception e2) {
//				}
//				int columnRead=columnNum-1;
//				int rowStart=2;
//				SymbolDesp.getRefSymbDesp(taxID,FannotationFile, columnRead, rowStart, columnRead);
//				SymbolDesp.getRefSymbDesp(taxID,FannotationFile, columnRead-2, rowStart, columnRead-2);
//				SymbolDesp.getRefSymbDesp(taxID,FannotationFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok");
	}
	
	
	/**
	 * �ں�������������ͳ��
	 * @throws Exception
	 */
	public static void statisticNum(int upBp,String peaksFile, int[] columnID, String resultParentFile,String prix) throws Exception 
	{
		String[][] intronExonStatistic;
		String genestructureBar = prix + "bar.jpg";
		String genestructureStatistic = prix + "geneStructure";
		intronExonStatistic = PeakLOC.getPeakStaticInfo(upBp,peaksFile, "\t",columnID, 2, -1);
		TxtReadandWrite txtstatistic = new TxtReadandWrite();
		txtstatistic.setParameter(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_FILE, true, false);
		txtstatistic.ExcelWrite(intronExonStatistic, "\t");
		barPlot();
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_RESULT_PIC,resultParentFile, genestructureBar, true);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_FILE,resultParentFile, genestructureStatistic, true);

	}
	/**
	 * ����R��ͼ
	 * @throws Exception
	 */
	private static void barPlot() throws Exception
	{
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+ NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		System.out.println("ok");
	}
	
	

		

	
	
}