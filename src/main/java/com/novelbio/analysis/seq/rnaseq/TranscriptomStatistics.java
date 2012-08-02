package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqfastaStatisticsCDS;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.model.species.Species;

/**
 * 重建转录本的统计
 * @author zong0jie
 *
 */
public class TranscriptomStatistics {
	/** 全长aa至少长于50个氨基酸 */
	static int minAAlen = 50;
	
	/** 新基因的数量 */
	int allGeneNewNum = 0;
	/** 新转录本的数量 */
	int allIsoNewNum = 0;
	/** 修饰的exon的数量 */
	int allExonModifiedNum = 0;
	/** 新的非编码基因的数量 */
	int allNewNunCodingGeneNum = 0;
	/** 新的有全长CDS的基因数量 */
	int allNewCompleteCDSGeneNum = 0;
	/** 修饰过的Iso的数量 */
	int allModifiedIso = 0;
	/** 修饰过的Gene的数量 */
	int allModifiedGene = 0;
	
	SeqHash seqFastaHash;
	
	public int getAllCompleteCDSGeneNum() {
		return allNewCompleteCDSGeneNum;
	}
	public int getAllExonModifiedNum() {
		return allExonModifiedNum;
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
	public int getAllNunCodingGeneNum() {
		return allNewNunCodingGeneNum;
	}
	
	public void setSeqFastaHash(SeqHash seqFastaHash) {
		this.seqFastaHash = seqFastaHash;
	}

	
	public void addGeneCluster(GffGeneCluster gffGeneCluster) {
		//全新的转录本
		if (!gffGeneCluster.isContainsRef) {
			ArrayList<GffDetailGene> lsGene = gffGeneCluster.getThisGffGene();
			allGeneNewNum = allGeneNewNum + lsGene.size();
			for (GffDetailGene gffDetailGene : lsGene) {
				allIsoNewNum = allIsoNewNum + gffDetailGene.getLsCodSplit().size();
			}
			addCodingGeneNum(gffGeneCluster);
		}
		//已有的进行修正
		else {
			addNewIsoNum(gffGeneCluster);
			addModifiedIsoAndExon(gffGeneCluster);
		}
	}
	
	private void addCodingGeneNum(GffGeneCluster gffGeneCluster) {
		ArrayList<GffDetailGene> lsGeneThis = gffGeneCluster.getThisGffGene();
		for (GffDetailGene gffDetailGene : lsGeneThis) {
			boolean mRNAgene = false;
			
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				SeqFasta seqFasta = seqFastaHash.getSeq(gffGeneIsoInfo, false);
				SeqfastaStatisticsCDS seqfastaStatisticsCDS = new SeqfastaStatisticsCDS(seqFasta);
				seqfastaStatisticsCDS.calculateAAseqInfo();
				if (seqfastaStatisticsCDS.isFullCds() && seqfastaStatisticsCDS.getMstartAAlen() > minAAlen) {
					allNewCompleteCDSGeneNum++;
					mRNAgene = true;
					break;
				}
			}
			
			if (!mRNAgene)
				allNewNunCodingGeneNum++;
		}
	}
	
	/** 新转录本数量 */
	private void addNewIsoNum(GffGeneCluster gffGeneCluster) {
		ArrayList<GffDetailGene> lsGeneThis = gffGeneCluster.getThisGffGene();
		ArrayList<GffDetailGene> lsGeneRef = gffGeneCluster.getRefGffGene();
		int refIsoNum = 0, thisIsoNum = 0;
		for (GffDetailGene gffDetailGene : lsGeneRef) {
			refIsoNum = refIsoNum + gffDetailGene.getLsCodSplit().size();
		}
		for (GffDetailGene gffDetailGene : lsGeneThis) {
			thisIsoNum = thisIsoNum + gffDetailGene.getLsCodSplit().size();
		}
		if (thisIsoNum <= refIsoNum) {
			return;
		}
		allIsoNewNum = allIsoNewNum + (thisIsoNum - refIsoNum);
	}
	/** 修饰的转录本和修饰的exon */
	private void addModifiedIsoAndExon(GffGeneCluster gffGeneCluster) {
		for (GffDetailGene gffDetailGeneRefRaw : gffGeneCluster.getRefGffGene()) {//遍历每个GffDetail
			GffDetailGene gffDetailGeneRef = gffDetailGeneRefRaw.clone();
			HashSet<GffGeneIsoInfo> setGffIsoRefSelect = new HashSet<GffGeneIsoInfo>();//所有选中的Iso的名字，也就是与cufflink预测的转录本相似的转录本

			for (GffDetailGene gffDetailGeneCalculate : gffGeneCluster.getThisGffGene()) {//获得另一个GffHash里面的GffDetailGene
				for (GffGeneIsoInfo gffIsoThis : gffDetailGeneCalculate.getLsCodSplit()) {//遍历该GffDetailGene的转录本，并挑选出最接近的进行比较	
					GffGeneIsoInfo gffIsoRef = gffDetailGeneRef.getSimilarIso(gffIsoThis, gffGeneCluster.likelyhood);
					
					if (gffIsoRef == null || gffIsoRef.equalsIso(gffIsoThis) ) {
						continue;
					}
					setGffIsoRefSelect.add(gffIsoRef);
					gffGeneCluster.compareIso(gffIsoRef, gffIsoThis);
				}
			}
			allModifiedIso = allModifiedIso + setGffIsoRefSelect.size();
			if (setGffIsoRefSelect.size() > 0) {
				allModifiedGene ++;
			}
		}
		
		for (ArrayList<ExonClusterBoundInfo> lsexonClusterBound : gffGeneCluster.lsIso2ExonBoundInfoStatistics) {
			addTranscriptomStatistics(lsexonClusterBound);
		}
	}
	
	/**
	 * 添加一个exon组的比较组
	 * @param lsSelectExonStatistics
	 */
	private boolean addTranscriptomStatistics(ArrayList<ExonClusterBoundInfo> lsSelectExonStatistics) {
		boolean modified = false;
		for (ExonClusterBoundInfo exonClusterBoundInfo : lsSelectExonStatistics) {
			if (!exonClusterBoundInfo.isStartUnify() || !exonClusterBoundInfo.isEndUnify() ) {
				modified = true;
				allExonModifiedNum++;
			}
		}
		return modified;
	}
	
	public ArrayList<String[]> getStatisticsResult() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{"allExonModifiedNum", allExonModifiedNum + ""});
		lsResult.add(new String[]{"allGeneNewNum", allGeneNewNum + ""});
		lsResult.add(new String[]{"allIsoNewNum", allIsoNewNum + ""});
		lsResult.add(new String[]{"allModifiedIso", allModifiedIso + ""});
		lsResult.add(new String[]{"allModifiedGene", allModifiedGene + ""});
		lsResult.add(new String[]{"allNewCompleteCDSGeneNum", allNewCompleteCDSGeneNum + ""});
		lsResult.add(new String[]{"allNewNunCodingGeneNum", allNewNunCodingGeneNum + ""});
		return lsResult;
	}
}
