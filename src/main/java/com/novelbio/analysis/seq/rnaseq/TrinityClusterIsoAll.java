package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.denovo.ClusterSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 修正Trinity之后的结果，把trinity的所有转录本进行聚类，将相似的聚为一组
 * @author zong0jie
 *
 */
public class TrinityClusterIsoAll {
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
	
	public static void main(String[] args) {
		TrinityClusterIsoAll trinityModify = new TrinityClusterIsoAll();
		trinityModify.setInFileName("/media/winE/NBC/Project/Project_WH/Trinity_tmp2013-07-150307-18080.fasta");
		trinityModify.setOutTrinityGeneFile("/media/winE/NBC/Project/Project_WH/Trinity_Out_Gene.fasta");
		trinityModify.setOutTrinityIsoFile("/media/winE/NBC/Project/Project_WH/Trinity_Out_Iso.fasta");
		trinityModify.cope();
	}
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
	private void clusterAndWrite2File() {
		geneNum = 1;
		TxtReadandWrite txtWriteGene = new TxtReadandWrite(outTrinityGeneFile, true);
		TxtReadandWrite txtWriteIso = new TxtReadandWrite(outTrinityIsoFile, true);
		TxtReadandWrite txtWriteGene2Iso = new TxtReadandWrite(outTrinityGeneName2Iso, true);
		
		List<SeqFasta> lsSeqFastaToCluster = new ArrayList<>(mapGeneID2LsSeqFasta.values());
		ArrayListMultimap<String, SeqFasta> mapName2LsIso = getClusteredGene2LsIso(lsSeqFastaToCluster);
		for (String geneName : mapName2LsIso.keySet()) {
			List<SeqFasta> lsSeqFastasNew = mapName2LsIso.get(geneName);
			for (SeqFasta seqFasta : lsSeqFastasNew) {
				txtWriteIso.writefileln(seqFasta.toStringNRfasta());
				txtWriteGene2Iso.writefileln(geneName + "\t" + seqFasta.getSeqName());
			}
			SeqFasta seqLongestIso = getLongestIso(lsSeqFastasNew);
			seqLongestIso.setName(geneName);
			txtWriteGene.writefileln(seqLongestIso.toStringNRfasta());
		}
		txtWriteGene.close();
		txtWriteIso.close();
		txtWriteGene2Iso.close();
	}
	
	/** 获得通过聚类后得到的gene和iso
	 * SeqFasta的名字随便起的
	 */
	private ArrayListMultimap<String, SeqFasta> getClusteredGene2LsIso(List<SeqFasta> lsSeqFastas) {
		//cluster
		String txtFile = PathDetail.getRworkspaceTmp() + "tmpTrinityCluster" + DateUtil.getDateAndRandom();
		ClusterSeq clusterSeq = new ClusterSeq();
		clusterSeq.setSeqHash(lsSeqFastas);
		clusterSeq.setOutFileName(txtFile);
		clusterSeq.setIdentityThrshld(identity);
		clusterSeq.setThreadNum(2);//一个线程就够啦
		clusterSeq.run();
		Map<String, SeqFasta> mapNameOld2Seq = getMapName2Seq(lsSeqFastas);
		List<List<String>> lsResult = clusterSeq.getLsCluster();
		ArrayListMultimap<String, SeqFasta> mapGeneName2LsSeqFasta = ArrayListMultimap.create();
		int allNum = lsResult.size();
		for (int i = 0; i < lsResult.size(); i++) {
			List<String> lsSeqName = lsResult.get(i);
			String geneName = geneNamePrefix + "_" + getSeqId(i, allNum);
			for (int j = 0; j < lsSeqFastas.size(); j++) {
				String geneNameOld = lsSeqName.get(j);
				SeqFasta seqFasta = mapNameOld2Seq.get(geneNameOld);
				seqFasta.setName(geneName+"."+j);
				mapGeneName2LsSeqFasta.put(geneName, seqFasta);
			}
		}
		return mapGeneName2LsSeqFasta;
	}
	
	private Map<String, SeqFasta> getMapName2Seq(List<SeqFasta> lsSeqFastas) {
		Map<String, SeqFasta> mapName2Seq = new HashMap<>();
		for (SeqFasta seqFasta : lsSeqFastas) {
			mapName2Seq.put(seqFasta.getSeqName(), seqFasta);
		}
		return mapName2Seq;
	}
	
	private String getSeqId(int num, int allNum) {
		int fold10Num = 1;
		while((allNum = allNum/10) > 0) {
			fold10Num++;
		}
		String numString = num+"";
		for (int i = 0; i < fold10Num - numString.length(); i++) {
			numString = 0+numString;
		}
		return numString;
	}
	
	private SeqFasta getLongestIso(List<SeqFasta> lsSeqFastas) {
		SeqFasta seqFastaMaxLen = null;
		int len = 0;
		for (SeqFasta seqFasta : lsSeqFastas) {
			if (seqFasta.Length() > len) {
				seqFastaMaxLen = seqFasta;
			}
		}
		return seqFastaMaxLen;
	}
	
	public void removeTmpFile() {
		for (String fileName : lsTmpFileName) {
			FileOperate.DeleteFileFolder(fileName);
		}
	}
	
}
