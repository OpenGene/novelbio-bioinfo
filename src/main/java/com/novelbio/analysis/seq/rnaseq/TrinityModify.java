package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.denovo.ClusterSeq;
import com.novelbio.analysis.seq.denovo.N50AndSeqLen;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 修正Trinity之后的结果 */
public class TrinityModify {
	public static final String geneNamePrefix = "NovelBio";
	/** trinity得到的fasta文件 */
	String inTrinityFile;
	/** 输出的基因文件名，仅包含最长Iso的序列，可用于做blast */
	String outTrinityGeneFile;
	/** 输出的Iso文件名，包含全体Iso序列 */
	String outTrinityIsoFile;
	
	
	/** 将输入的trinity文件按照iso分成小组，放在这个里面 */
	ArrayListMultimap<String, SeqFasta> mapGeneID2LsSeqFasta = ArrayListMultimap.create();
	
	List<String> lsTmpFileName = new ArrayList<String>();
	/** 两个iso的最低相似度，小于这个相似度就认为是两个不同的基因 */
	int identity = 85;
	int geneNum = 0;
	
	public static void main(String[] args) {
		TrinityModify trinityModify = new TrinityModify();
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
		int geneNumToCluster = 500;//每500个基因做一次聚类
		TxtReadandWrite txtWriteGene = new TxtReadandWrite(outTrinityGeneFile, true);
		TxtReadandWrite txtWriteIso = new TxtReadandWrite(outTrinityIsoFile, true);
		List<SeqFasta> lsSeqFastaToCluster = new ArrayList<>();
		for (String geneName : mapGeneID2LsSeqFasta.keySet()) {
			List<SeqFasta> lsSeqFastas = mapGeneID2LsSeqFasta.get(geneName);
			//只有一个iso
			if (lsSeqFastas.size() == 1) {
				String geneNameNew = getGeneName(geneNum); geneNum++;
				SeqFasta seqFasta = lsSeqFastas.get(0);
				seqFasta.setName(geneNameNew);
				txtWriteGene.writefileln(seqFasta.toStringNRfasta());
				seqFasta.setName(geneNameNew + "_iso_" + 1);
				txtWriteIso.writefileln(seqFasta.toStringNRfasta());
				continue;
			}
			lsSeqFastaToCluster.addAll(lsSeqFastas);
			if (lsSeqFastaToCluster.size() < geneNumToCluster) {
				continue;
			}
			//有多个iso，通过聚类将不相似的iso分成两个不同的基因
			ArrayListMultimap<String, SeqFasta> mapName2LsIso = null;
			try {
				mapName2LsIso = getClusteredGene2LsIso(lsSeqFastaToCluster);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if (mapName2LsIso == null) {
				continue;
			}
			for (String name : mapName2LsIso.keySet()) {
				List<SeqFasta> lsSeqFastasNew = mapName2LsIso.get(name);
				String geneNameNew = getGeneName(geneNum); geneNum++;
				int isoNum = 1;
				for (SeqFasta seqFasta : lsSeqFastasNew) {
					seqFasta.setName(geneNameNew + "_iso_" + isoNum);
					isoNum++;
					txtWriteIso.writefileln(seqFasta.toStringNRfasta());
				}
				SeqFasta seqLongestIso = getLongestIso(lsSeqFastasNew);
				seqLongestIso.setName(geneNameNew);
				txtWriteGene.writefileln(seqLongestIso.toStringNRfasta());
			}
			lsSeqFastaToCluster.clear();
		}
		txtWriteGene.close();
		txtWriteIso.close();
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
		List<List<String>> lsResult = clusterSeq.getLsCluster();
		
		//modifyResult
		Map<String, SeqFasta> mapName2Seq = getMapName2Seq(lsSeqFastas);
		ArrayListMultimap<String, SeqFasta> mapGeneName2LsSeqFasta = ArrayListMultimap.create();
		int i = 0;
		for (List<String> list : lsResult) {
			i++;
			Collections.sort(list);
			String geneNameLast = null;
			for (String seqFastaName : list) {
				SeqFasta seqFasta = mapName2Seq.get(seqFastaName);
				if (geneNameLast != null && seqFastaName.startsWith(geneNameLast)) {
					mapGeneName2LsSeqFasta.put(i+" ", seqFasta);
				} else {
					String[] ss = seqFastaName.split("_");
					try {
						geneNameLast = ss[0] + "_" + ss[1];
					} catch (Exception e) {
						geneNameLast = ss[0] + "_" + ss[1];
					}
					
					i++;
					mapGeneName2LsSeqFasta.put(i+" ", seqFasta);
				}				
			}
		}
		//清空文件
		clusterSeq.clearTmpFile();
		FileOperate.DeleteFileFolder(clusterSeq.getOutClusterInfo());
		FileOperate.DeleteFileFolder(clusterSeq.getOutClusterSeq());
		return mapGeneName2LsSeqFasta;
	}
	
	private Map<String, SeqFasta> getMapName2Seq(List<SeqFasta> lsSeqFastas) {
		Map<String, SeqFasta> mapName2Seq = new HashMap<>();
		for (SeqFasta seqFasta : lsSeqFastas) {
			mapName2Seq.put(seqFasta.getSeqName(), seqFasta);
		}
		return mapName2Seq;
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
	
	/** 返回计算好的N50等信息 */
	public N50AndSeqLen getN50info() {
		N50AndSeqLen n50AndSeqLen = new N50AndSeqLen(inTrinityFile);
		return n50AndSeqLen;
	}
	
	/** 给定geneNum，返回该基因的名字 */
	private String getGeneName(int geneNum) {
		String geneNumFinal = geneNum + "";
		String prefix = geneNamePrefix;
		if (geneNumFinal.length() < 6) {
			for (int i = 0; i < 6 - geneNumFinal.length(); i++) {
				prefix = prefix + "0";
			}
		}
		return prefix + geneNumFinal;
	}
	
}
