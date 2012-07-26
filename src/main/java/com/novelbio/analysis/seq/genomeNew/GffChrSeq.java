package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class GffChrSeq {
	private static Logger logger = Logger.getLogger(GffChrSeq.class);
	GffChrAbs gffChrAbs = null;
	
	GeneStructure geneStructure;
	/** true,��ȡ��ת¼����false����ȡ��gene�µ��ת¼�� */
	boolean absIso;
	/** �Ƿ���ȡ�ں��� */
	boolean getIntron;
	/** ��ȡȫ���������е�ʱ����ÿ��LOC��ȡһ�����л�����ȡȫ�� */
	boolean getAllIso;
	
	public GffChrSeq() {}
	
	public GffChrSeq(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** ��ȡ���������ʱ��<br>
	 * true����ȡ�û����Ӧ��ת¼��<br>
	 * false ��ȡ�û������ڻ�����ת¼��<br>
	 * @param absIso
	 */
	public void setAbsIso(boolean absIso) {
		this.absIso = absIso;
	}
	/** ��ȡȫ���������е�ʱ����ÿ��LOC��ȡһ�����л�����ȡȫ�� */
	public void setGetAllIso(boolean getAllIso) {
		this.getAllIso = getAllIso;
	}
	/**
	 * ��ȡ�����ʱ�������ں��ӣ�����ȡ������������ȥ
	 * @param getIntron
	 */
	public void setGetIntron(boolean getIntron) {
		this.getIntron = getIntron;
	}
	/**
	 * ��GffChrAbs���룬����gffChrAbs��س�ʼ��chrSeq��gffhashgene������
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * �������꣬��ø���������Ӧ������
	 * @return
	 */
	public void getSeq(SiteInfo mapInfo) {
		gffChrAbs.getSeqHash().getSeq(mapInfo);
	}
	/**
	 * �������꣬��ȡ����
	 * @param IsoName
	 * @param absIso
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeq(boolean cis5to3,String chrID, int startLoc, int endLoc) {
		return gffChrAbs.getSeqHash().getSeq(chrID, (long)startLoc, (long)endLoc);
	}
	
	public void setGeneStructure(GeneStructure geneStructure) {
		this.geneStructure = geneStructure;
	}
	/**
	 * �趨�����ӷ�Χ����þ�������
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param startExon ����ĳ��exon ���
	 * @param endExon ����ĳ��Intron �յ�
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeq(String IsoName, int startExon, int endExon, boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), startExon, endExon, gffGeneIsoInfo, getIntron);
		seqFasta.setName(IsoName);
		return seqFasta;
	}
	public SeqFasta getSeq(String IsoName) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		return getSeq(gffGeneIsoInfo);
	}
	/**
	 * ����genestructure�ж���Ľṹ��ȡ����
	 * ���genestructure�趨����tss������gffchrabs���趨��tss��ȡ����
	 * @param IsoName
	 * @param absIso true,��ȡ��ת¼����false����ȡ��gene�µ��ת¼��
	 * @return
	 */
	public SeqFasta getSeq(GffGeneIsoInfo gffGeneIsoInfo) {
		ArrayList<ExonInfo> lsExonInfos = null;
		if (geneStructure.equals(GeneStructure.ALLLENGTH) || geneStructure.equals(GeneStructure.EXON)) {
			lsExonInfos = gffGeneIsoInfo;
		}
		else if (geneStructure.equals(GeneStructure.CDS)) {
			lsExonInfos = gffGeneIsoInfo.getIsoInfoCDS();
		}
		else if (geneStructure.equals(GeneStructure.INTRON)) {
			lsExonInfos = gffGeneIsoInfo.getLsIntron();
		}
		else if (geneStructure.equals(GeneStructure.UTR3)) {
			lsExonInfos = gffGeneIsoInfo.getUTR3seq();
		}
		else if (geneStructure.equals(GeneStructure.UTR5)) {
			lsExonInfos = gffGeneIsoInfo.getUTR5seq();
		}
		else if (geneStructure.equals(GeneStructure.TSS)) {
			getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),gffChrAbs.tss[0], gffChrAbs.tss[1]);
		}
		else if (geneStructure.equals(GeneStructure.TES)) {
			getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),gffChrAbs.tes[0], gffChrAbs.tes[1]);
		}
		SeqFasta seqFastaResult = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsExonInfos, getIntron);
		seqFastaResult.setName(gffGeneIsoInfo.getName());
		return seqFastaResult;
	}
	/**
	 * ��ȡĳ��λ����ܱ����У����ݷ��򷵻غ��ʵ�����
	 * ������ȡTss��Tes�ܱ����е�
	 * @param cis5to3 ����
	 * @param site λ��
	 * @param upBp ��λ�����Σ�����������
	 * @param downBp ��λ�����Σ�����������
	 * @return
	 */
	private SeqFasta getSiteRange(GffGeneIsoInfo gffGeneIsoInfo, int site, int upBp, int downBp) {
		int startlocation = 0; int endlocation = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			startlocation = site + upBp;
			endlocation = site + downBp;
		}
		else {
			startlocation = site - upBp;
			endlocation = site - downBp;
		}
		int start = Math.min(startlocation, endlocation);
		int end = Math.max(startlocation, endlocation);
		SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), gffGeneIsoInfo.getChrID(), start, end);
		if (seq == null) {
			logger.error("û����ȡ�����У�" + " "+ gffGeneIsoInfo.getChrID() + " " + start + " " + end);
			return null;
		}
		seq.setName(gffGeneIsoInfo.getName());
		return seq;
	}
	/**
	 * ��ȡȫ�������promoter����������
	 * @param upBp tss���ζ���bp�����������������������
	 * @param downBp tss���ζ���bp�����������������������
	 * @return
	 */
	public ArrayList<SeqFasta> getGenomeWideSeq() {
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffDetailGene gffDetailGene = null;
		for (String geneID : lsID) {
			gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
			if (getAllIso) {
				lsResult.addAll(getGeneSeqAllIso(gffDetailGene));
			}
			else {
				lsResult.add(getGeneSeqLongestIso(gffDetailGene));
			}
		}
		return lsResult;
	}
	private LinkedList<SeqFasta> getGeneSeqAllIso(GffDetailGene gffDetailGene) {
		LinkedList<SeqFasta> lsResult = new LinkedList<SeqFasta>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			SeqFasta seqFasta = getSeq(gffGeneIsoInfo);
			if (seqFasta == null || seqFasta.getLength() < 3) {
				continue;
			}
			lsResult.add(seqFasta);
		}
		return lsResult;
	}
	private SeqFasta getGeneSeqLongestIso(GffDetailGene gffDetailGene) {
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		return getSeq(gffGeneIsoInfo);
	}
	/**
	 * ��ָ��motif����ȫ����������promoter���򣬷��صõ���motif
	 * ��д���ı�
	 * @param outTxtFile
	 * @param regex
	 * @param upBp
	 * @param downBp
	 */
	public void motifPromoterScan(String outTxtFile, String regex, int upBp, int downBp) {
		ArrayList<SeqFasta> lsPromoterSeq = getGenomePromoterSeq(upBp, downBp);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(SeqFasta.getMotifScanTitle());
		for (SeqFasta seqFasta : lsPromoterSeq) {
			lsResult.addAll(seqFasta.getMotifScanResult(regex));
		}
		TxtReadandWrite txtMotifOut = new TxtReadandWrite(outTxtFile, true);
		txtMotifOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	/**
	 * ���ĳ�����ֵ�ȫ��cds���У���refseq����ȡ���Ӿ�ȷ
	 * ÿ������ֻѡȡ����һ������
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqCDSAll() {
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffGeneIsoInfo gffGeneIsoInfo = null;
		for (String geneID : lsID) {
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(geneID);
			ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.getIsoInfoCDS();
			if (lsCDS.size() > 0) {
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsCDS, false);
				if (seq == null || seq.getLength() < 3) {
					continue;
				}
				seq.setName(geneID);
				lsResult.add(seq);
			}
		}
		return lsResult;
	}
	//TODO �����½�һ���ཫ��Щ5UTR��3UTR��Promoter��ȫ��װ��ȥ
	/**
	 * ���ĳ�����ֵ�ȫ��cds��Ҳ���Ǵ�ATG��UAG��ÿ��ISO���У���refseq����ȡ���Ӿ�ȷ
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqCDSAllIso() {
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameAll();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffDetailGene gffDetailGene = null;
		for (String geneID : lsID) {
			gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
			gffDetailGene.removeDupliIso();
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.getIsoInfoCDS();
				if (lsCDS.size() > 0) {
					SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsCDS, false);
					if (seq == null || seq.getLength() < 3) {
						continue;
					}
					seq.setName(gffGeneIsoInfo.getName());
					lsResult.add(seq);
				}
			}
		}
		return lsResult;
	}
	/**
	 * ���ĳ�����ֵ�ȫ��3UTR���У�Ϊ��Ԥ��novel miRNA�л���
	 */
	public ArrayList<SeqFasta> getSeq3UTRAll() {
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffDetailGene gffDetailGene = null;
		for (String geneID : lsID) {
			gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
			gffDetailGene.removeDupliIso();
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.getUTR3seq();
				if (lsCDS.size() > 0) {
					SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsCDS, false);
					if (seq == null || seq.getLength() < 3) {
						continue;
					}
					seq.setName(gffGeneIsoInfo.getName());
					lsResult.add(seq);
				}
			}
		}
		return lsResult;
	}
	/**
	 * ���ĳ�����ֵ�ȫ��RNAȫ�����е�ÿ��ISO���У���refseq����ȡ���Ӿ�ȷ
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param FilteredStrand �����������������ת¼���Ļ����ϣ��Ƿ���Ҫ���򻥲�
	 * @param startExon ����ĳ��exon
	 * @param endExon ����ĳ��Intron
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqAllIso() {
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getLocHashtable().values()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, false);
				if (seq == null || seq.getLength() < 3) {
					continue;
				}
				seq.setName(gffGeneIsoInfo.getName());
				lsResult.add(seq);
			}
		}
		return lsResult;
	}
	/**
	 * ���ĳ�����ֵ�ȫ��RNAȫ�����е�ÿ��gene������У���refseq����ȡ���Ӿ�ȷ
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param FilteredStrand �����������������ת¼���Ļ����ϣ��Ƿ���Ҫ���򻥲�
	 * @param startExon ����ĳ��exon
	 * @param endExon ����ĳ��Intron
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqAll() {
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getLocHashtable().values()) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, false);
				if (seq == null || seq.getLength() < 3) {
					continue;
				}
				seq.setName(gffGeneIsoInfo.getName());
				lsResult.add(seq);
		}
		return lsResult;
	}
	/**
	 * ���Ը�rsemʹ��
	 * �ڲ��Զ�close
	 * @param seqFastaTxt
	 * @return
	 */
	public void writeIsoFasta(String seqFastaTxt) {
		HashSet<String> setRemoveRedundent = new HashSet<String>();
		TxtReadandWrite txtFasta = new TxtReadandWrite(seqFastaTxt, true);
		ArrayList<GffDetailGene> lsGffDetailGenes = gffChrAbs.getGffHashGene().getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (setRemoveRedundent.contains(gffGeneIsoInfo.getName())) {
					continue;
				}
				setRemoveRedundent.add(gffGeneIsoInfo.getName());
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, false);
				seq.setName(gffGeneIsoInfo.getName());
				txtFasta.writefileln(seq.toStringNRfasta());
			}
		}
		txtFasta.close();
	}
}
