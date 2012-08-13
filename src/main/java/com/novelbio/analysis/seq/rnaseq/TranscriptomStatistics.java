package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.SeqfastaStatisticsCDS;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.model.species.Species;

/**
 * �ؽ�ת¼����ͳ��
 * @author zong0jie
 *
 */
public class TranscriptomStatistics {
	private static Logger logger = Logger.getLogger(TranscriptomStatistics.class);
	
	/** ȫ��aa���ٳ���50�������� */
	static int minAAlen = 50;
	
	/** �»�������� */
	int allGeneNewNum = 0;
	/** ��ת¼�������� */
	int allIsoNewNum = 0;
	/** ���ε�exon������ */
	int allExonModifiedNum = 0;
	/** �µķǱ����������� */
	int allNewNunCodingGeneNum = 0;

	/** �µ���CDS�Ļ������� */
	int allNewCDSGeneNum = 0;
	/** �µ���ȫ��CDS�Ļ������� */
	int allNewCompleteCDSGeneNum = 0;
	
	/** ���ι���Iso������ */
	int allModifiedIso = 0;
	/** ���ι���Gene������ */
	int allModifiedGene = 0;
	
	int allNoModifiedGene = 0;
	
	int totalGenes;
	
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
		//ȫ�µ�ת¼��
		if (!gffGeneCluster.isContainsRef) {
			ArrayList<GffDetailGene> lsGene = gffGeneCluster.getThisGffGene();
			allGeneNewNum = allGeneNewNum + lsGene.size();
			totalGenes = totalGenes + lsGene.size();
			for (GffDetailGene gffDetailGene : lsGene) {
				allIsoNewNum = allIsoNewNum + gffDetailGene.getLsCodSplit().size();
			
			}
			addCodingGeneNum(gffGeneCluster);
		}
		//���еĽ�������
		else {
			addNewIsoNum(gffGeneCluster);
			noModifiedGene(gffGeneCluster);
			addModifiedIsoAndExon(gffGeneCluster);
		}
	}
	
	private void addCodingGeneNum(GffGeneCluster gffGeneCluster) {
		ArrayList<GffDetailGene> lsGeneThis = gffGeneCluster.getThisGffGene();
		for (GffDetailGene gffDetailGene : lsGeneThis) {
			boolean mRNAgene = false;
			
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				SeqFasta seqFasta = seqFastaHash.getSeq(gffGeneIsoInfo, false);
				SeqfastaStatisticsCDS seqfastaStatisticsCDS = seqFasta.statisticsCDS();
				seqfastaStatisticsCDS.calculateAAseqInfo();
				if (seqfastaStatisticsCDS.getMstartAAlen() > minAAlen) {
					allNewCDSGeneNum++;
					mRNAgene = true;
					if (seqfastaStatisticsCDS.isFullCds()) {
						allNewCompleteCDSGeneNum++;
					}
					break;
				}
			}
			
			if (!mRNAgene)
				allNewNunCodingGeneNum++;
		}
	}
	
	/** ��ת¼������ */
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
	/** û�����εĻ������� */
	private void noModifiedGene(GffGeneCluster gffGeneCluster) {
		ArrayList<GffDetailGene> lsGeneThis = gffGeneCluster.getThisGffGene();
		ArrayList<GffDetailGene> lsGeneRef = gffGeneCluster.getRefGffGene();
		boolean sameGene = false;
		if (lsGeneRef.size() == 1 && lsGeneRef.size() == 1) {
			GffDetailGene gffDetailGeneRef = lsGeneRef.get(0);
			GffDetailGene gffDetailGeneThis = lsGeneThis.get(0);
			if (gffDetailGeneRef.getLsCodSplit().size() == gffDetailGeneThis.getLsCodSplit().size()) {
				for (GffGeneIsoInfo gffIsoThis : gffDetailGeneThis.getLsCodSplit()) {
					GffGeneIsoInfo gffIsoRef = gffDetailGeneRef.getSimilarIso(gffIsoThis, gffGeneCluster.likelyhood);
					if (!gffIsoRef.equalsIso(gffIsoThis)) {
						sameGene = false;
						break;
					}
					sameGene = true;
				}
			}
		}
		if (sameGene) {
			allNoModifiedGene++;
			totalGenes++;
		}
	}
	/** ���ε�ת¼�������ε�exon */
	private void addModifiedIsoAndExon(GffGeneCluster gffGeneCluster) {
		for (GffDetailGene gffDetailGeneRefRaw : gffGeneCluster.getRefGffGene()) {//����ÿ��GffDetail
			GffDetailGene gffDetailGeneRef = gffDetailGeneRefRaw.clone();
			HashSet<GffGeneIsoInfo> setGffIsoRefSelect = new HashSet<GffGeneIsoInfo>();//����ѡ�е�Iso�����֣�Ҳ������cufflinkԤ���ת¼�����Ƶ�ת¼��

			for (GffDetailGene gffDetailGeneThis : gffGeneCluster.getThisGffGene()) {//�����һ��GffHash�����GffDetailGene
				for (GffGeneIsoInfo gffIsoThis : gffDetailGeneThis.getLsCodSplit()) {//������GffDetailGene��ת¼��������ѡ����ӽ��Ľ��бȽ�	
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
				totalGenes++;
			}
		}
		
		for (ArrayList<ExonClusterBoundInfo> lsexonClusterBound : gffGeneCluster.lsIso2ExonBoundInfoStatistics) {
			addTranscriptomStatistics(lsexonClusterBound);
		}
	}
	
	/**
	 * ���һ��exon��ıȽ���
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
		lsResult.add(new String[]{"allNewCDSGeneNum", allNewCDSGeneNum + ""});
		lsResult.add(new String[]{"allNewCompleteCDSGeneNum", allNewCompleteCDSGeneNum + ""});
		lsResult.add(new String[]{"allNewNunCodingGeneNum", allNewNunCodingGeneNum + ""});
		lsResult.add(new String[]{"allNoModifiedGene", allNoModifiedGene + ""});
		lsResult.add(new String[]{"totalGenes", totalGenes + ""});
		return lsResult;
	}
}
