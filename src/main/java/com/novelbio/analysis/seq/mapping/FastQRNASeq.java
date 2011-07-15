package com.novelbio.analysis.seq.mapping;

import java.util.HashMap;
import java.util.Set;

import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.seq.BedSeq;

public class FastQRNASeq extends Mapping{
	String species = "";
	
	
	
	
	
	
	/**
	 * @param seqFile1
	 * @param seqFile2
	 * @param QUALITY
	 */
	public FastQRNASeq(String seqFile1, String seqFile2, int QUALITY, String species) {
		super(seqFile1, seqFile2, QUALITY);
		this.species = species;
	}

	/**
	 * 单端mapping
	 * @param seqFile1
	 * @param QUALITY
	 */
	public FastQRNASeq(String seqFile1, int QUALITY, String species) {
		super(seqFile1, QUALITY);
		this.species = species;
	}

	/**
	 * 双端mapping
	 * @param seqFile1
	 * @param seqFile2
	 * @param FastQFormateOffset 哪种fastQ格式，现在有FASTQ_SANGER_OFFSET，FASTQ_ILLUMINA_OFFSET两种 不知道就写0，程序会从文件中判断
	 * @param QUALITY
	 */
	public FastQRNASeq(String seqFile1,String seqFile2, int FastQFormateOffset,int QUALITY, String species) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		this.species = species;
	}
	
	/**
	 * 用tophat来进行reads的mapping
	 */
	@Override
	public BedSeq mapReads() {
		String cmd = "tophat";
		cmd = cmd + " -r " + (300 - getFirstReadsLen()*2);
		cmd = cmd + " -a 10 -m 1";
		cmd = cmd + " -i " + RnaSeqParam.getMinIntron(species) + " -I " + RnaSeqParam.getMaxIntron(species);
		cmd = cmd + " --solexa1.3-quals -F 0.15 -p 4 --coverage-search";
		cmd = cmd + " --min-coverage-intron " + RnaSeqParam.getMinIntron(species) + "  --max-coverage-intron " + RnaSeqParam.getMaxIntron(species);
		cmd = cmd + " --min-segment-intron "+ RnaSeqParam.getMinIntron(species) + "  --max-segment-intron " + RnaSeqParam.getMaxIntron(species);
		cmd = cmd + " -G /media/winE/Bioinformatics/GenomeData/Arabidopsis\\ TAIR9/TAIR10GFF/TAIR10_GTF3_genes.gtf ";
		return null;
	}
	
}

class RnaSeqParam
{
	static final String HUMAN = "human";
	static final String ARABIDOPSIS = "arabidopsis";
	static final String RICE = "rice";
	static final String MOUSE = "mouse";
	static boolean set = false;
	static HashMap<String, Integer> hashMinIntro = new HashMap<String, Integer>();
	static HashMap<String, Integer> hashMaxIntro = new HashMap<String, Integer>();
	private static void setInfo() {
		if (set) {
			return;
		}
		hashMaxIntro.put(ARABIDOPSIS, 6000);
		hashMinIntro.put(ARABIDOPSIS, 20);
		set = true;
	}
	/**
	 * 某物种最短Intron
	 * @param species
	 * @return
	 */
	public static int getMinIntron(String species) {
		setInfo();
		return hashMinIntro.get(species);
	}
	/**
	 * 某物种最长Intron
	 * @param species
	 * @return
	 */
	public static int getMaxIntron(String species) {
		setInfo();
		return hashMaxIntro.get(species);
	}


}