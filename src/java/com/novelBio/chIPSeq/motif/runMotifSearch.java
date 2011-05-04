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
	 * 选定peak文件，指定chrID和summit点，获得peak信息
	 */
	public static void getMotifDensity() {
		Motifsearch cdg=new Motifsearch();
		String motifRex="CCCCCCCC";
		String FileParent = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/";
		String txtpeakFile = FileParent + "CSAnovelbio_annotationFiltered.txt";
		int[] columnID = new int[]{1,6};
		int rowStart = 2;
		int rowEnd = -1;
		String resultPath = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/motif/";
		String resultPrix = "MEME_CSA_CCCCCCCCCC...";
		int peakLength = 2000;
		try { 
			String gfffilename = "";//NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ;
			String chrPah = NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM;
			int condition = 1;//0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向 1: 通通提取正向 2: 通通提取反向
			String sep = "\t";
			
			cdg.getSummitMotifDensity(NovelBioConst.GENOME_GFF_TYPE_TIGR, gfffilename, chrPah, peakLength/2, condition, txtpeakFile, sep, columnID, rowStart, rowEnd, motifRex, resultPath, resultPrix);
		} catch (Exception e) { 	e.printStackTrace(); }
	}
	
	
}
