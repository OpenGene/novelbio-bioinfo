package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.SepSign;
import com.novelbio.base.fileOperate.FileOperate;

/** 用CD-HIT的来对 trinity 的结果进行聚类 */
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
			String geneName = seqFasta.getSeqName().split(" ")[0];
			String[] geneName2IsoName = getGeneName2IsoName(geneName);
			seqFasta.setName(geneName2IsoName[1]);
			
			mapGeneID2LsSeqFasta.put(geneName2IsoName[0], seqFasta);
		}
		seqFastaHash.close();
	}
	
	/** 给定trinityname，如 TRINITY_DN27976_c2_g9_i18<br>
	 * 返回 genename和isoname的数组，如 DN27976_c2_g9 和 DN27976_c2_g9@i18
	 * @param trinityName
	 * @return
	 */
	protected static String[] getGeneName2IsoName(String trinityName) {
		String geneName = trinityName;
		String isoName = "";
		if (geneName.toLowerCase().startsWith("trinity_")) {
			geneName = geneName.substring("trinity_".length());
		}
		String[] names = geneName.split("_");
		geneName = names[0];
		isoName = names[names.length - 1];
		for (int i = 1; i < names.length - 1; i++) {
			geneName =geneName + "_" + names[i];
		}
		return new String[]{geneName, geneName + SepSign.SEP_INFO_SIMPLE + isoName};
	}
	
	/** 聚类并将结果写入文本 */
	protected abstract void clusterAndWrite2File();

	public void removeTmpFile() {
		for (String fileName : lsTmpFileName) {
			FileOperate.deleteFileFolder(fileName);
		}
	}
	

}
