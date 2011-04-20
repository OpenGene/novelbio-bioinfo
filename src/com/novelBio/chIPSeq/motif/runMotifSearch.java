package com.novelBio.chIPSeq.motif;

import com.novelBio.generalConf.NovelBioConst;

public class runMotifSearch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getMotifDensity();   
		
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
		String motifRex="CT[GC]TCC\\w\\wGGT|CAG\\wACC\\w\\wGGA[GC]AG";
		String FileParent = "/media/winE/NBC/Project/ChIPSeq_CDG110330/result/annotation/";
		String txtpeakFile = FileParent + "FT5_macsPeak_peaks.xls";
		int[] columnID = new int[]{1,6};
		int rowStart = 2;
		int rowEnd = -1;
		String resultPath = "/media/winE/NBC/Project/ChIPSeq_CDG110330/result/motif/";
		String resultPrix = "MEME_FT5MEME";
		int peakLength = 2000;
		try { 
			String gfffilename = "";//NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ;
			String chrPah = NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM;
			int condition = 1;//0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻��������� 1: ͨͨ��ȡ���� 2: ͨͨ��ȡ����
			String sep = "\t";
			
			cdg.getSummitMotifDensity(NovelBioConst.GENOME_GFF_TYPE_UCSC, gfffilename, chrPah, peakLength/2, condition, txtpeakFile, sep, columnID, rowStart, rowEnd, motifRex, resultPath, resultPrix);
		} catch (Exception e) { 	e.printStackTrace(); }
	}
	
	
}
