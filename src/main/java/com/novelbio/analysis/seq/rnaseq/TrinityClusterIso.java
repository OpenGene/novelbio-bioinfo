package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.fileOperate.FileOperate;

public abstract class TrinityClusterIso {
	public static final String geneNamePrefix = "NovelBio";
	/** trinity得到的fasta文件 */
	String inTrinityFile;
	/** 输出的基因文件名，仅包含最长Iso的序列，可用于做blast */
	String outTrinityGeneFile;
	/** 输出的Iso文件名，包含全体Iso序列 */
	String outTrinityIsoFile;
	/** 输出GeneName对IsoName的对照表，给Rsem用的 */
	String outTrinityGeneName2Iso;
	
	/** 将输入的trinity文件按照iso分成小组，放在这个里面 */
	ArrayListMultimap<String, SeqFasta> mapGeneID2LsSeqFasta = ArrayListMultimap.create();
	
	List<String> lsTmpFileName = new ArrayList<String>();
	/** 两个iso的最低相似度，小于这个相似度就认为是两个不同的基因 */
	int identity = 95;
	int geneNum = 0;
	
	public void setOutTrinityGeneFile(String outTrinityGeneFile) {
		this.outTrinityGeneFile = outTrinityGeneFile;
	}
	public void setOutTrinityIsoFile(String outTrinityIsoFile) {
		this.outTrinityIsoFile = outTrinityIsoFile;
	}
	/** 给Rsem用的 */
	public void setOutTrinityGeneName2Iso(String outTrinityGeneName2Iso) {
		this.outTrinityGeneName2Iso = outTrinityGeneName2Iso;
	}
	public void setInFileName(String inFileName) {
		this.inTrinityFile = inFileName;
	}
	
	public void cope() {
		copeFileToGene2LsIso();
		clusterAndWrite2File();
		removeTmpFile();
	}
	
	/** 将trinity的结果读取并整理成GeneID2LsIso的形式 */
	private void copeFileToGene2LsIso() {
		lsTmpFileName.clear();
		SeqFastaHash seqFastaHash = new SeqFastaHash(inTrinityFile);
		for (SeqFasta seqFasta : seqFastaHash.getSeqFastaAll()) {
			seqFasta.setName(seqFasta.getSeqName().split(" ")[0]);
			String[] ss = seqFasta.getSeqName().split("_");
			String geneName = ss[0] + "_" + ss[1];
			mapGeneID2LsSeqFasta.put(geneName, seqFasta);
		}
		seqFastaHash.close();
	}
	
	/** 聚类并将结果写入文本 */
	protected abstract void clusterAndWrite2File();

	public void removeTmpFile() {
		for (String fileName : lsTmpFileName) {
			FileOperate.DeleteFileFolder(fileName);
		}
	}
	

}
