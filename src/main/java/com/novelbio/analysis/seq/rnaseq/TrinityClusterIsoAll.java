package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.denovo.ClusterCDhit;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 修正Trinity之后的结果，把trinity的所有转录本进行聚类，将相似的聚为一组
 * @author zong0jie
 *
 */
public class TrinityClusterIsoAll extends TrinityClusterIso {
	public static final String geneNamePrefix = "NovelBio";
	
	public static void main(String[] args) {
		TrinityClusterIsoAll trinityModify = new TrinityClusterIsoAll();
		trinityModify.setInFileName("/media/winE/NBC/Project/Project_WH/Trinity_tmp2013-07-150307-18080.fasta");
		trinityModify.setOutTrinityGeneFile("/media/winE/NBC/Project/Project_WH/Trinity_Out_Gene.fasta");
		trinityModify.setOutTrinityIsoFile("/media/winE/NBC/Project/Project_WH/Trinity_Out_Iso.fasta");
		trinityModify.cope();
	}
	
	/** 聚类并将结果写入文本 */
	protected void clusterAndWrite2File() {
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
		ClusterCDhit clusterSeq = new ClusterCDhit();
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

}
