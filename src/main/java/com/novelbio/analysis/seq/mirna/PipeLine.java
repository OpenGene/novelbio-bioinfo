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
		genome = "/media/winE/Bioinformatics/GenomeData/pig/Index/bwasus9/pigChromSus9.fa";
		PipeLine pipeLine = new PipeLine();
		pipeLine.setInfo(rfamSeq, miRNAseq, ncRNAseq, genome);
		
		String parentPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/Rawdata/";
		String fastQfile = parentPath + "s_6_IDX7_1_h_filtered.fq";
		String outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX7/";
		String prix = "H12";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX6_1_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX6/";
		prix = "C";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX8_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/";
		prix = "H36";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX3_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX3/";
		prix = "H6";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX9_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX9/";
		prix = "N12";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX5_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX5/";
		prix = "N36";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
		
		fastQfile = parentPath + "s_6_IDX4_1_h_filtered.fq";
		outPath = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX4/";
		prix = "N6";
		pipeLine.setSample(fastQfile, outPath, prix);
		pipeLine.mappingRepeat();
	
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
	
	public void mappingRepeat() {
		outPath = FileOperate.addSep(outPath);
		String fqFile = "";
		String samFile = "";
		String bedFile = "";
		String unMappedFq= "";
		
		fqFile = outPath + prix + "_unMap2ncRna.fq";
		samFile = outPath + prix + "_GenomeSus9.sam";
		bedFile = FileOperate.changeFileSuffix(samFile, null, "bed");
		unMappedFq = outPath + prix + "_unMappedSus9.fq";
		mapping(fqFile, genome, samFile, bedFile, unMappedFq);
	
		
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
