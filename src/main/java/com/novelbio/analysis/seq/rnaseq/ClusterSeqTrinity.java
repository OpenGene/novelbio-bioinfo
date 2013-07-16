package com.novelbio.analysis.seq.rnaseq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.denovo.ClusterSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 专门用来处理trinity结果的类 */
public class ClusterSeqTrinity extends ClusterSeq {
	public static final String geneNamePrefix = "NovelBio";
	public static void main(String[] args) {
		ClusterSeqTrinity clusterSeqTrinity = new ClusterSeqTrinity();
		clusterSeqTrinity.setOutFileName("/media/winE/NBC/Project/Project_WH/Trinity_cluster.fasta.clstr");
		clusterSeqTrinity.setInFileName("/media/winE/NBC/Project/Project_WH/Trinity_tmp2013-07-150307-18080.fasta");
		clusterSeqTrinity.setOutGeneSeqFile("/media/winE/NBC/Project/Project_WH/Trinity_Gene.fa");
		clusterSeqTrinity.setOutIsoSeqFile("/media/winE/NBC/Project/Project_WH/Trinity_Iso.fa");
		clusterSeqTrinity.modifyGeneName2Iso();
	}
	
	ArrayListMultimap<String, String> mapGene2Iso = ArrayListMultimap.create();
	String outIsoSeqFile;
	String outGeneSeqFile;
	
	TxtReadandWrite txtWriteIsoSeq;
	TxtReadandWrite txtWriteGeneSeq;
	SeqFastaHash seqFastaHash;
	
	int geneNum = 1;
	
	public void setOutIsoSeqFile(String outIsoSeqFile) {
		this.outIsoSeqFile = outIsoSeqFile;
	}
	public void setOutGeneSeqFile(String outGeneSeqFile) {
		this.outGeneSeqFile = outGeneSeqFile;
	}
	
	private void initalTxt() {
		txtWriteGeneSeq = new TxtReadandWrite(outGeneSeqFile, true);
		txtWriteIsoSeq = new TxtReadandWrite(outIsoSeqFile, true);
	}
	
	/** 返回修正后的geneID和IsoID对照表 */
	public ArrayListMultimap<String, String> getMapGene2Iso() {
		return mapGene2Iso;
	}
	
	public void modifyGeneName2Iso() {
		initalTxt();
		seqFastaHash = new SeqFastaHash(inFileName, null, true);
		List<List<String>> lsCluster = getLsCluster();
		for (List<String> list : lsCluster) {
			copeAndWriteCluster(list);
		}
		txtWriteGeneSeq.close();
		txtWriteIsoSeq.close();
	}
	
	private void copeAndWriteCluster(List<String> lsCluster) {
		Map<String, String> mapSeqID2GeneID = new HashMap<String, String>();
		ArrayListMultimap<String, String> mapGeneID2LsGeneIsoID = ArrayListMultimap.create();
		Map<String, String> mapGeneIsoID2SeqIsoID = new HashMap<String, String>();
		for (String string : lsCluster) {
			String SeqName = string.split("_seq")[0];
			if (!mapSeqID2GeneID.containsKey(SeqName)) {
				String geneName = getGeneName(geneNum);
				geneNum++;
				mapSeqID2GeneID.put(SeqName, geneName);
			}
			String geneID = mapSeqID2GeneID.get(SeqName);
			String geneIsoID = "";
			
			if (mapGeneID2LsGeneIsoID.containsKey(geneID)) {
				List<String> lsIsoName = mapGeneID2LsGeneIsoID.get(geneID);
				geneIsoID = geneID + "_iso_" + (lsIsoName.size() + 1);
				mapGeneID2LsGeneIsoID.put(geneID, geneIsoID);
			} else {
				geneIsoID = geneID + "_iso_" + 1;
				mapGeneID2LsGeneIsoID.put(geneID, geneIsoID);
			}
			mapGeneIsoID2SeqIsoID.put(geneIsoID, string);
		}
		
		for (String geneID : mapGeneID2LsGeneIsoID.keySet()) {
			List<String> lsGeneIsoID = mapGeneID2LsGeneIsoID.get(geneID);
			String seqGene = "";
			for (String isoID : lsGeneIsoID) {
				mapGene2Iso.put(geneID, isoID);
				String seqIsoID = mapGeneIsoID2SeqIsoID.get(isoID);
				SeqFasta seqTmpGene = seqFastaHash.getSeqFasta(seqIsoID);
				if (seqTmpGene.Length() > seqGene.length()) {
					seqGene = seqTmpGene.toString();
				}
				seqTmpGene.setName(isoID);
				txtWriteIsoSeq.writefileln(seqTmpGene.toStringNRfasta());
			}
			SeqFasta seqFasta = new SeqFasta(seqGene);
			seqFasta.setName(geneID);
			txtWriteGeneSeq.writefileln(seqFasta.toStringNRfasta());
		}
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
