package com.novelbio.test;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.SeqComb;
import com.novelbio.analysis.seq.blastZJ.Cell;
import com.novelbio.analysis.seq.blastZJ.LongestCommonSubsequence;
import com.novelbio.analysis.seq.blastZJ.SmithWaterman;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashPlantGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashUCSCgene;
import com.novelbio.analysis.seq.genome.gffOperate.GffsearchUCSCgene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashPeak;
import com.novelbio.analysis.seq.rnaseq.SplitCope;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.plot.Rplot;
import com.novelbio.test.testextend.b;



public class mytest {
	
	private static Logger logger = Logger.getLogger(mytest.class);  
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		int a = 2;
		if (a > 3) {
			System.out.println("a<3");
		}
		else if ( a == 2) {
			System.out.println("a == 2");
		}
		
	}
	
	
	public static void AthIntron() throws Exception {
		
		
		
	}
	
	public static void blast() {
		String sequence1 = "ATGAACAGCGTGGGCGAGGCGTGCACCGAGCTCAAGCGCGTAAGAGCACTAGAAGTAGGTAACTGATG";
		String sequence2 = "ATGAAAATGGGCGAGGCGTGCACCGAGCTCTTAAACCCTGAATCCTTTCATTGTTTTAAAACATTCTTACTATGAACAGGGCGAGGCGTGCTGCGAGCTC";
		SmithWaterman smithWaterman = new SmithWaterman(sequence1, sequence2, 1, -2, -2,1.1);
		String[] reStrings = smithWaterman.getAlignment();
		System.out.println("ok");
		try {
			smithWaterman.printScoreTable("/media/winE/matrix2");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void WZFfastq() {
		String parentPath = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/ÖíÁ´Çò¾ú-2003/ÖíÁ´Çò¾ú-2003/raw_reads/s_3_fastq.txt/";
		String seq1 = parentPath + "s_3_sep_filter_high.fasta_1";
		String seq2 = parentPath + "s_3_sep_filter_high.fasta_2";
		FastQ fastQ = new FastQ(seq1, seq2,FastQ.FASTQ_ILLUMINA_OFFSET,FastQ.QUALITY_HIGM);
		try {
			fastQ.filterBarcode("/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/ÖíÁ´Çò¾ú-2003/ÖíÁ´Çò¾ú-2003/raw_reads/s_3_fastq.txt/barcod.fastq", 1, "GTCAT","GTCAT","CATGT","CATGT","TGACT","TGACT");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
