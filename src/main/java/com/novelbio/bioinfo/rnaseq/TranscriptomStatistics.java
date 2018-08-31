package com.novelbio.bioinfo.rnaseq;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.fasta.SeqfastaStatisticsCDS;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;

/**
 * 重建转录本的统计
 * @author zong0jie
 *
 */
public class TranscriptomStatistics {
	private static Logger logger = Logger.getLogger(TranscriptomStatistics.class);
	
	/** 全长aa至少长于50个氨基酸 */
	static int minAAlen = 50;
	
	/** 新基因的数量 */
	int allGeneNewNum = 0;
	/** 新转录本的数量 */
	int allIsoNewNum = 0;

	/** 新的有CDS的基因数量 */
	int allNewCDSGeneNum = 0;
	/** 新的有全长CDS的基因数量 */
	int allNewCompleteCDSGeneNum = 0;
	
	/** 修饰过的Iso的数量 */
	int allModifiedIso = 0;
	/** 修饰过的Gene的数量 */
	int allModifiedGene = 0;
	
	int allNoModifiedGene = 0;
	
	int totalGenes;
	
	SeqHash seqFastaHash;
	
	public int getAllCompleteCDSGeneNum() {
		return allNewCompleteCDSGeneNum;
	}
	public int getAllGeneNewNum() {
		return allGeneNewNum;
	}
	public int getAllIsoNewNum() {
		return allIsoNewNum;
	}
	public int getAllModifiedIso() {
		return allModifiedIso;
	}
	
	public void setSeqFastaHash(SeqHash seqFastaHash) {
		this.seqFastaHash = seqFastaHash;
	}

	public void addGeneCluster(GffGeneCluster gffGeneCluster) {
		totalGenes += gffGeneCluster.getThisGffGene().size();
		//全新的转录本
		if (!gffGeneCluster.isContainsRef) {
			ArrayList<GffGene> lsGene = gffGeneCluster.getThisGffGene();
			allGeneNewNum = allGeneNewNum + lsGene.size();
			for (GffGene gffDetailGene : lsGene) {
				allIsoNewNum = allIsoNewNum + gffDetailGene.getLsCodSplit().size();
			}
			if (seqFastaHash != null) {
				addCodingGeneNum(gffGeneCluster);
			}
		}
		//已有的进行修正
		else {
			addNewIsoNum(gffGeneCluster);
			noModifiedGene(gffGeneCluster);
			addModifiedIsoAndExon(gffGeneCluster);
		}
	}
	
	private void addCodingGeneNum(GffGeneCluster gffGeneCluster) {
		ArrayList<GffGene> lsGeneThis = gffGeneCluster.getThisGffGene();
		for (GffGene gffDetailGene : lsGeneThis) {			
			for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				SeqFasta seqFasta = null;
				try {
					seqFasta = seqFastaHash.getSeq(gffGeneIsoInfo, false);
				} catch (Exception e) {
					logger.error(e);
					continue;
				}
				if (seqFasta == null) {
					logger.error("can not find sequence on " + gffGeneIsoInfo.toString());
					continue;
				}
				SeqfastaStatisticsCDS seqfastaStatisticsCDS = seqFasta.statisticsCDS();
				seqfastaStatisticsCDS.calculateAAseqInfo();
				if (seqfastaStatisticsCDS.getMstartAAlen() > minAAlen) {
					allNewCDSGeneNum++;
					if (seqfastaStatisticsCDS.isFullCds()) {
						allNewCompleteCDSGeneNum++;
					}
					break;
				}
			}
		}
	}
	
	/** 新转录本数量 */
	private void addNewIsoNum(GffGeneCluster gffGeneCluster) {
		ArrayList<GffGene> lsGeneThis = gffGeneCluster.getThisGffGene();
		ArrayList<GffGene> lsGeneRef = gffGeneCluster.getRefGffGene();
		int newGeneNum = lsGeneThis.size() - lsGeneRef.size();
		if (newGeneNum > 0) {
			allGeneNewNum = allGeneNewNum + newGeneNum;
		}
		
		int refIsoNum = 0, thisIsoNum = 0;
		for (GffGene gffDetailGene : lsGeneRef) {
			refIsoNum = refIsoNum + gffDetailGene.getLsCodSplit().size();
		}
		for (GffGene gffDetailGene : lsGeneThis) {
			thisIsoNum = thisIsoNum + gffDetailGene.getLsCodSplit().size();
		}
		if (thisIsoNum <= refIsoNum) {
			return;
		}
		allIsoNewNum = allIsoNewNum + (thisIsoNum - refIsoNum);
	}
	
	/** 没有修饰的基因数量 */
	private void noModifiedGene(GffGeneCluster gffGeneCluster) {
		ArrayList<GffGene> lsGeneThis = gffGeneCluster.getThisGffGene();
		ArrayList<GffGene> lsGeneRef = gffGeneCluster.getRefGffGene();
	
		for (GffGene gffDetailGeneRef : lsGeneRef) {
			double[] regionRef = {gffDetailGeneRef.getStartAbs(), gffDetailGeneRef.getEndAbs()};
			boolean sameGene = false;
			for (GffGene gffDetailGeneThis : lsGeneThis) {
				double[] regionThis = {gffDetailGeneThis.getStartAbs(), gffDetailGeneThis.getEndAbs()};
				double[] compare = ArrayOperate.cmpArray(regionRef, regionThis);
				if (compare[2] > 0.5 || compare[3] > 0.5) {
					for (GffIso gffIsoThis : gffDetailGeneThis.getLsCodSplit()) {
						GffIso gffIsoRef = gffDetailGeneRef.getSimilarIso(gffIsoThis, gffGeneCluster.likelyhood);
						if (gffIsoRef != null && !gffIsoRef.equalsIso(gffIsoThis)) {
							sameGene = false;
							break;
						}
						sameGene = true;
					}
				}
			}
			if (sameGene) {
				allNoModifiedGene++;
			}
		}
	}
	
	/** 修饰的转录本 */
	private void addModifiedIsoAndExon(GffGeneCluster gffGeneCluster) {
		for (GffGene gffDetailGeneRefRaw : gffGeneCluster.getRefGffGene()) {//遍历每个GffDetail
			GffGene gffDetailGeneRef = gffDetailGeneRefRaw.clone();
			HashSet<GffIso> setGffIsoRefSelect = new HashSet<GffIso>();//所有选中的Iso的名字，也就是与cufflink预测的转录本相似的转录本

			for (GffGene gffDetailGeneThis : gffGeneCluster.getThisGffGene()) {//获得另一个GffHash里面的GffDetailGene
				for (GffIso gffIsoThis : gffDetailGeneThis.getLsCodSplit()) {//遍历该GffDetailGene的转录本，并挑选出最接近的进行比较	
					GffIso gffIsoRef = gffDetailGeneRef.getSimilarIso(gffIsoThis, gffGeneCluster.likelyhood);
					
					if (gffIsoRef == null || gffIsoRef.equalsIso(gffIsoThis) ) {
						continue;
					}
					setGffIsoRefSelect.add(gffIsoRef);
				}
			}
			allModifiedIso = allModifiedIso + setGffIsoRefSelect.size();
			if (setGffIsoRefSelect.size() > 0) {
				allModifiedGene ++;
			}
		}
	}

	
	public ArrayList<String[]> getStatisticsResult() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{"allGeneNewNum", allGeneNewNum + ""});
		lsResult.add(new String[]{"allIsoNewNum", allIsoNewNum + ""});
		lsResult.add(new String[]{"allModifiedIso", allModifiedIso + ""});
		lsResult.add(new String[]{"allModifiedGene", allModifiedGene + ""});
		lsResult.add(new String[]{"allNewCDSGeneNum", allNewCDSGeneNum + ""});
		lsResult.add(new String[]{"allNewCompleteCDSGeneNum", allNewCompleteCDSGeneNum + ""});
		lsResult.add(new String[]{"allNoModifiedGene", allNoModifiedGene + ""});
		lsResult.add(new String[]{"totalGenes", totalGenes + ""});
		return lsResult;
	}
}
