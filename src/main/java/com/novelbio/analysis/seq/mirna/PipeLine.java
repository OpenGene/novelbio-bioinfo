package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.MapBwa;
import com.novelbio.analysis.seq.mapping.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 小RNA比对流水线
 * @author zong0jie
 *
 */
public class PipeLine {
	String seqFile = "";
	String outPath = "";
	
	String rfamSeq = "";
	String miRNAseq = "";
	String ncRNAseq = "";
	String genome = "";
	
	String prix = "";
	
	public static void main(String[] args) {
		String rfamSeq = "/media/winE/Bioinformatics/GenomeData/pig/smallRNA/Rfam_pig.fasta";
		String miRNAseq = "/media/winE/Bioinformatics/GenomeData/pig/smallRNA/hairpin_pig_Final.fa";
		String ncRNAseq = "/media/winE/Bioinformatics/GenomeData/pig/smallRNA/RefSeq_ncRNA.txt";
		String genome = "/media/winE/Bioinformatics/GenomeData/pig/Index/bwa/chromAll.fa";
		PipeLine pipeLine = new PipeLine();
		pipeLine.setInfo(rfamSeq, miRNAseq, ncRNAseq, genome);
		
		String fastQfile = "";
		String outPath = "";
		String prix = "";
		pipeLine.setSample(fastQfile, outPath, prix);
		
	}
	/**
	 * @param rfamSeq
	 * @param miRNAseq
	 * @param ncRNAseq
	 * @param genome
	 */
	public void setInfo(String rfamSeq, String miRNAseq, String ncRNAseq, String genome) {
		this.rfamSeq = rfamSeq;
		this.miRNAseq = miRNAseq;
		this.ncRNAseq = ncRNAseq;
		this.genome = genome;
	}
	/**
	 * @param seqFile 序列
	 * @param outPath 输出文件夹
	 * @param prix 文件前缀
	 */
	public void setSample(String seqFile, String outPath, String prix) {
		this.seqFile = seqFile;
		this.outPath = outPath;
		this.prix = prix;
	}
	
	public void mappingPipeline() {
		outPath = FileOperate.addSep(outPath);
		
		String fqFile = seqFile;
		String samFile = outPath + prix + "_miRNA.sam";
		String bedFile = outPath + prix + "_miRNA.bed";
		String unMappedFq = outPath + prix + "_unMap2miRNA.fq";
		mapping(fqFile, miRNAseq, samFile, bedFile, unMappedFq);
		
		fqFile = unMappedFq;
		samFile = outPath + prix + "_rfam.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMap2rfam.fq";
		mapping(fqFile, rfamSeq, samFile, bedFile, unMappedFq);
		
		fqFile = unMappedFq;
		samFile = outPath + prix + "_ncRna.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMap2ncRna.fq";
		mapping(fqFile, ncRNAseq, samFile, bedFile, unMappedFq);
		
		fqFile = unMappedFq;
		samFile = outPath + prix + "_Genome.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMapped.fq";
		mapping(fqFile, genome, samFile, bedFile, unMappedFq);
		
		fqFile = seqFile;
		samFile = outPath + prix + "_GenomeAll.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMapped.fq";
		mapping(fqFile, genome, samFile, bedFile, unMappedFq);
		
	}
	
	
	private void mapping(String fqFile, String chrFile, String samFileName, String bedFile, String unMappedFq) {
		outPath = FileOperate.addSep(outPath);
		MapBwa mapBwa = new MapBwa(fqFile, samFileName, false);
		mapBwa.setExePath("", chrFile);
		SamFile samFile = mapBwa.mapReads();
		FastQ fastQ = samFile.getUnMappedReads(false, unMappedFq);
		samFile.sam2bedSingleEnd(TxtReadandWrite.TXT,  bedFile, false);
	}
}
