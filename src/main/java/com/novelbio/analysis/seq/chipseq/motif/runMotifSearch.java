package com.novelbio.analysis.seq.chipseq.motif;

import com.novelbio.analysis.generalConf.NovelBioConst;

public class runMotifSearch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getMotifDetail();   
		
	}

	public static void getChrMotif() {
		Motifsearch cdg=new Motifsearch();
		String chrFilePath="/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/chromFa";
		String Rworkspace="/media/winE/Bioinformatics/R/practice_script/platform";
		String motifRex="tcctcc";
		cdg.maxChrLength = 3000;
		try { cdg.getChrMoitfDensity(chrFilePath, Rworkspace, motifRex); } catch (Exception e) { 	e.printStackTrace(); }
	}
	
	/**
	 * ѡ��peak�ļ���ָ��chrID��summit�㣬���peak��Ϣ
	 */
	public static void getMotifDensity() {
		Motifsearch cdg=new Motifsearch();
		String motifRex="CCAAA";
		String FileParent = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/";
		String txtpeakFile = FileParent + "CSAnovelbio_annotationFilteredNew.txt";
		int[] columnID = new int[]{2,7};
		int rowStart = 2;
		int rowEnd = -1;
		String resultPath = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/motif/";
		String resultPrix = "MEME_CSA_CCAAA";
		int peakLength = 2000;
		try { 
			String gfffilename = "";//NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ;
			String chrPah = NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM;
			int condition = 1;//0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻��������� 1: ͨͨ��ȡ���� 2: ͨͨ��ȡ����
			String sep = "\t";
			
			cdg.getMotifSummitDensity(NovelBioConst.GENOME_GFF_TYPE_TIGR, gfffilename, chrPah, peakLength/2, condition, txtpeakFile, sep, columnID, rowStart, rowEnd, motifRex, resultPath, resultPrix);
		} catch (Exception e) { 	e.printStackTrace(); }
	}
	
	/**
	 * ѡ��OC�ļ���ָ��Tss���γ��ȣ����motif��Ϣ
	 */
	public static void getMotifDetail() {
		Motifsearch cdg=new Motifsearch();
		String motifRex="\\w{5}GCCGCC|GGCGGC\\w{5}";
		String FileParent = "/media/winE/NBC/Project/Project_ZDB_Lab/GXC/";
		String excelLoc = FileParent + "quidongzitique2.xls";
		int[] columnID = new int[]{2};
		int rowStart = 2;
		int rowEnd = -1;
		int writeCol = 4;
		int Length = 2000;
		try { 
			String gfffilename = NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE;//NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ;
			String chrPah = NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM;
			cdg.getMotifDetail(NovelBioConst.GENOME_GFF_TYPE_TIGR, gfffilename, chrPah, Length, excelLoc, columnID, writeCol, rowStart, rowEnd, motifRex);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}
