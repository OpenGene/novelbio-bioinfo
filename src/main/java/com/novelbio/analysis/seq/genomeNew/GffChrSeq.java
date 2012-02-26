package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.AminoAcid;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.listOperate.ListAbs;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;

public class GffChrSeq extends GffChrAbs{
	private static Logger logger = Logger.getLogger(GffChrSeq.class);
	public GffChrSeq(String gffType, String gffFile, String chrFile, String regx) {
		super(gffType, gffFile, chrFile, regx, null, 0);
		loadChrFile();
	}
	
	public GffChrSeq(String gffType, String gffFile, String chrFile) {
		this(gffType, gffFile, chrFile, null);
		loadChrFile();
	}

	public static void main(String[] args) {
//		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,  
//				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,  
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);

	}
	
	/**
	 * ��������������ø�ת¼������Ϣ
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @return
	 */
	public SeqFasta getSeq(String IsoName, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		return seqHash.getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
	}
	
	/**
	 * �������꣬��ȡ����
	 * @param IsoName
	 * @param absIso
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeq(boolean cis5to3,String chrID, int startLoc, int endLoc)
	{
		return seqHash.getSeq(chrID, (long)startLoc, (long)endLoc);
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
	public SeqFasta getSeq(String IsoName, boolean cis5to3,int startExon, int endExon, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, startExon, endExon, gffGeneIsoInfo, getIntron);
		seqFasta.setSeqName(IsoName);
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
	public SeqFasta getSeq(boolean cisseq, String IsoName, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = seqHash.getSeq(cisseq, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
		seqFasta.setSeqName(IsoName);
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
	public SeqFasta getSeqCDS(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
		seqFasta.setSeqName(IsoName);
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
	public String getSeqProtein(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seq = seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
		seq.setSeqName(IsoName);
		return seq.toStringAA(true, 0);
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
		gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
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
		SeqFasta seq = seqHash.getSeq(gffGeneIsoInfo.isCis5to3(), gffGeneIsoInfo.getChrID(), start, end);
		if (seq == null) {
			logger.error("û����ȡ�����У�" + " "+ gffGeneIsoInfo.getChrID() + " " + start + " " + end);
			return null;
		}
		seq.setSeqName(gffGeneIsoInfo.getIsoName());
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
		ArrayList<String> lsID = gffHashGene.getLOCChrHashIDList();
		for (String string : lsID) {
			String geneID = string.split(ListAbs.SEP)[0];
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
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param cis5to3 �����������������ת¼���Ļ����ϣ��Ƿ���Ҫ���򻥲�
	 * @param startExon ����ĳ��exon
	 * @param endExon ����ĳ��Intron
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqCDSAll()
	{
		ArrayList<String> lsID = gffHashGene.getLOCChrHashIDList();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffGeneIsoInfo gffGeneIsoInfo = null;
		for (String string : lsID) {
			gffGeneIsoInfo = gffHashGene.searchISO(string.split(ListAbs.SEP)[0]);
			ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.getIsoInfoCDS();
			if (lsCDS.size() > 0) {
				SeqFasta seq = seqHash.getSeq(gffGeneIsoInfo.getChrID(), lsCDS, false);
				if (seq == null || seq.length() < 3) {
					continue;
				}
				seq.setSeqName(string.split(ListAbs.SEP)[0]);
				lsResult.add(seq);
			}
		}
		return lsResult;
		
	}
}
