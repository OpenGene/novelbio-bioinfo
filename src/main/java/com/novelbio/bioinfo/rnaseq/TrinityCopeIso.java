package com.novelbio.bioinfo.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.PathDetail;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.rnaseq.cluster.ClusterCDhit;

/** 修正Trinity之后的结果
 * 把trinity的结果整理成iso和单独基因的，并且基因名用我们的id
 * @author zong0jie
 *
 */
public class TrinityCopeIso extends TrinityClusterIso {
	public static final String geneNamePrefix = "NovelBio";
	
	public static void main(String[] args) {
		TrinityCopeIso trinityModify = new TrinityCopeIso();
		trinityModify.setInFileName("/home/novelbio/下载/Dip-1trinity.Trinity.fasta");
		trinityModify.setOutTrinityGeneFile("/home/novelbio/下载/Dip_Gene.fasta");
		trinityModify.setOutTrinityIsoFile("/home/novelbio/下载/Dip_Iso.fasta");
		trinityModify.setOutTrinityGeneName2Iso("/home/novelbio/下载/Dip_gene2Iso.txt");
		trinityModify.cope();
		
		
		
	}
	
	/** 聚类并将结果写入文本 */
	protected void clusterAndWrite2File() {
		geneNum = 1;
		int geneNumToCluster = 500;//每500个基因做一次聚类
		TxtReadandWrite txtWriteGene = new TxtReadandWrite(outTrinityGeneFile, true);
		TxtReadandWrite txtWriteIso = new TxtReadandWrite(outTrinityIsoFile, true);
		TxtReadandWrite txtWriteGene2Iso = new TxtReadandWrite(outTrinityGeneName2Iso, true);

		List<SeqFasta> lsSeqFastaToCluster = new ArrayList<>();
		for (String geneName : mapGeneID2LsSeqFasta.keySet()) {
			List<SeqFasta> lsSeqFastas = mapGeneID2LsSeqFasta.get(geneName);
			//只有一个iso
			if (lsSeqFastas.size() == 1) {
				String geneNameNew = getGeneName(geneNum); geneNum++;
				SeqFasta seqFasta = lsSeqFastas.get(0);
				seqFasta.setName(geneNameNew);
				txtWriteGene.writefileln(seqFasta.toStringNRfasta());
				seqFasta.setName(geneNameNew + "." + 1);
				txtWriteIso.writefileln(seqFasta.toStringNRfasta());
				txtWriteGene2Iso.writefileln(geneNameNew + "\t" + geneNameNew + "." + 1);
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
					seqFasta.setName(geneNameNew + "." + isoNum);
					txtWriteIso.writefileln(seqFasta.toStringNRfasta());
					txtWriteGene2Iso.writefileln(geneNameNew + "\t" + geneNameNew + "." + isoNum);
					isoNum++;
				}
				SeqFasta seqLongestIso = getLongestIso(lsSeqFastasNew);
				seqLongestIso.setName(geneNameNew);
				txtWriteGene.writefileln(seqLongestIso.toStringNRfasta());
			}
			lsSeqFastaToCluster.clear();
		}
		txtWriteGene.close();
		txtWriteIso.close();
		txtWriteGene2Iso.close();
	}
	
	/**
	 * 将若干组iso group进行聚类。实际上这只是为了降低io
	 * 本质上是将一组iso group进行聚类，如果本组iso没法聚到一起，说明本组iso来源于不同的gene，就要将其根据聚类结果切分成多个gene  
	 * 获得通过聚类后得到的gene和iso
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
					geneNameLast = seqFastaName.split(SepSign.SEP_INFO_SIMPLE)[0];					
					i++;
					mapGeneName2LsSeqFasta.put(i+" ", seqFasta);
				}				
			}
		}
		//清空文件
		clusterSeq.clearTmpFile();
		FileOperate.deleteFileFolder(clusterSeq.getOutClusterInfo());
		FileOperate.deleteFileFolder(clusterSeq.getOutClusterSeq());
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
