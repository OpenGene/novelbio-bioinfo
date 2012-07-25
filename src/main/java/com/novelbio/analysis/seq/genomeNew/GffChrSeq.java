package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class GffChrSeq {
	private static Logger logger = Logger.getLogger(GffChrSeq.class);
	GffChrAbs gffChrAbs = null;
	
	public GffChrSeq() {}
	
	public GffChrSeq(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * ��GffChrAbs���룬����gffChrAbs��س�ʼ��chrSeq��gffhashgene������
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * ��������������ø�ת¼������Ϣ
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @return
	 */
	public SeqFasta getSeq(String IsoName, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		return gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
	}
	/**
	 * �������꣬��ø���������Ӧ������
	 * @return
	 */
	public void getSeq(MapInfo mapInfo) {
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
	
	/**
	 * ��������������ø�ת¼������Ϣ
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param cis5to3 �����������������ת¼���Ļ����ϣ��Ƿ���Ҫ���򻥲���
	 * @param startExon ����ĳ��exon
	 * @param endExon ����ĳ��Intron
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeq(String IsoName, boolean cis5to3,int startExon, int endExon, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), cis5to3, startExon, endExon, gffGeneIsoInfo, getIntron);
		seqFasta.setName(IsoName);
		return seqFasta;
	}
	
	
	
	/**
	 * ��������������ø�ת¼������Ϣ
	 * ����ת¼���ķ������Ǵӻ������5����3����ȡ�� ������Ҫ�˹��趨cisseq
	 * @param cisseq ��������Ƿ������У�����ڻ�������˵��
	 * @param IsoName ת¼��������
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron �Ƿ����ں���
	 * @return
	 */
	public SeqFasta getSeq(boolean cisseq, String IsoName, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(cisseq, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
		seqFasta.setName(IsoName);
		return seqFasta;
	}

	/**
	 * ��������������ø�ת¼������Ϣ
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param cis5to3 �����������������ת¼���Ļ����ϣ��Ƿ���Ҫ���򻥲���
	 * @param startExon ����ĳ��exon
	 * @param endExon ����ĳ��Intron
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeqCDS(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
		seqFasta.setName(IsoName);
		return seqFasta;
	}
	
	/**
	 * ��������������ø�ת¼������Ϣ
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param cis5to3 �����������������ת¼���Ļ����ϣ��Ƿ���Ҫ���򻥲�
	 * @param startExon ����ĳ��exon
	 * @param endExon ����ĳ��Intron
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron
	 * @return
	 */
	public String getSeqProtein(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
		seq.setName(IsoName);
		return seq.toStringAA();
	}
	/**
	 * ��ȡ����promoter����������
	 * @param IsoName ������
	 * @param upBp tss���ζ���bp�����������������������
	 * @param downBp tss���ζ���bp�����������������������
	 * @return
	 */
	public SeqFasta getPromoter(String IsoName, int upBp, int downBp) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		int TssSite = gffGeneIsoInfo.getTSSsite();
		int startlocation = 0; int endlocation = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			startlocation = TssSite + upBp;
			endlocation = TssSite + downBp;
		}
		else {
			startlocation = TssSite - upBp;
			endlocation = TssSite - downBp;
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
	public ArrayList<SeqFasta> getGenomePromoterSeq(int upBp, int downBp) {
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		for (String geneID : lsID) {
			SeqFasta seqFasta = getPromoter(geneID, upBp, downBp);
			if (seqFasta == null) {
				logger.error("û����ȡ������"+geneID);
				continue;
			}
			lsResult.add(seqFasta);
		}
		return lsResult;
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
				ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.get3UTRseq();
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
