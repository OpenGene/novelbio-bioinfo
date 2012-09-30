package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
/**
 * ��GffChrAbs���趨Tss��Tes�ķ�Χ
 * setGetSeqIso �� setGetSeqSite��˭���趨����ȡ˭
 * @author zong0jie
 *
 */
public class GffChrSeq extends RunProcess<GffChrSeq.GffChrSeqProcessInfo>{
	private static Logger logger = Logger.getLogger(GffChrSeq.class);
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	GeneStructure geneStructure = GeneStructure.ALLLENGTH;
	/** �Ƿ���ȡ�ں��� */
	boolean getIntron;
	/** ��ȡȫ���������е�ʱ����ÿ��LOC��ȡһ�����л�����ȡȫ�� */
	boolean getAllIso;
	/** �Ƿ���ȡ������ */
	boolean getAAseq = false;
	/** �Ƿ����ȡmRNA���� */
	boolean getOnlyMRNA = false;
	
	boolean getGenomWide = false;
	
	/** ����ȡλ�㻹����ȡ���� */
	boolean booGetIsoSeq = false;
	LinkedHashSet<GffGeneIsoInfo> setIsoToGetSeq = new LinkedHashSet<GffGeneIsoInfo>();
	ArrayList<SiteInfo> lsSiteInfos = new ArrayList<SiteInfo>();
	
	/** Ĭ�ϴ����ļ������򷵻�һ��listSeqFasta */
	boolean saveToFile = true;
	ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
	TxtReadandWrite txtOutFile;
	String outFile = "";
	
	int[] tssRange;
	int[] tesRange; 
	
	public GffChrSeq() {}
	
	public GffChrSeq(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setTssRange(int[] tssRange) {
		this.tssRange = tssRange;
	}
	public void setTesRange(int[] tesRange) {
		this.tesRange = tesRange;
	}
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}
	/** Ĭ����ture����ʾ����output�ļ�
	 * ������������lsResult��
	 */
	public void setSaveToFile(boolean saveToFile) {
		this.saveToFile = saveToFile;
	}
	/** ��ȡȫ���������е�ʱ����ÿ��Gene��ȡһ��Iso������ȡȫ��Iso <br>
	 * true����ȡ�û����Ӧ��ת¼��<br>
	 * false ��ȡ�û������ڻ�����ת¼��<br>
	 */
	public void setGetAllIso(boolean getAllIso) {
		this.getAllIso = getAllIso;
	}
	/** �Ƿ����ȡmRNA��Ҳ�����б����RNA */
	public void setIsGetOnlyMRNA(boolean getOnlyMRNA) {
		this.getOnlyMRNA = getOnlyMRNA;
	}
	/**
	 * ��ȡ�����ʱ�������ں��ӣ�����ȡ������������ȥ
	 * @param getIntron
	 */
	public void setGetIntron(boolean getIntron) {
		this.getIntron = getIntron;
	}
	public void setGetAAseq(boolean getAAseq) {
		this.getAAseq = getAAseq;
	}
	/**
	 * ��GffChrAbs���룬����gffChrAbs��س�ʼ��chrSeq��gffhashgene������
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setOutPutFile(String outPutFile) {
		this.outFile = outPutFile;
	}
	/** ����ȡ�������һ������ */
	public void setGeneStructure(GeneStructure geneStructure) {
		this.geneStructure = geneStructure;
	}
	/**
	 * ����������ȡ���У��ڲ���ȥ���ظ�����
	 * @param lsIsoName
	 */
	public void setGetSeqIso(ArrayList<String> lsIsoName) {
		setIsoToGetSeq.clear();
		for (String string : lsIsoName) {
			GffGeneIsoInfo gffGeneIsoInfo = getIso(string);
			if (getOnlyMRNA && !gffGeneIsoInfo.ismRNA()) {
				continue;
			}
			if (gffGeneIsoInfo != null) {
				setIsoToGetSeq.add(gffGeneIsoInfo);
			}
		}
		booGetIsoSeq = true;
	}
	public void setGetSeqIsoGenomWide() {
		getGenomWide = true;
	}
	/**
	 * ����������ȡ���У��ڲ���ȥ���ظ�����
	 * @param lsListGffName
	 */
	private void getSeqIsoGenomWide() {
		setIsoToGetSeq.clear();
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		GffDetailGene gffDetailGene = null;
		for (String geneID : lsID) {
			gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
			if (getOnlyMRNA && !gffDetailGene.isMRNA()) {
				continue;
			}
			if (getAllIso) {
				setIsoToGetSeq.addAll(getGeneSeqAllIso(gffDetailGene));
			}
			else {
				setIsoToGetSeq.addAll(getGeneSeqLongestIso(gffDetailGene));
			}
		}
		booGetIsoSeq = true;
	}
	public int getNumOfQuerySeq() {
		if (booGetIsoSeq) {
			return setIsoToGetSeq.size();
		} else {
			return lsSiteInfos.size();
		}
	}
	private LinkedList<GffGeneIsoInfo> getGeneSeqAllIso(GffDetailGene gffDetailGene) {
		LinkedList<GffGeneIsoInfo> lsResult = new LinkedList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			lsResult.add(gffGeneIsoInfo);
		}
		return lsResult;
	}
	private LinkedList<GffGeneIsoInfo> getGeneSeqLongestIso(GffDetailGene gffDetailGene) {
		LinkedList<GffGeneIsoInfo> lsResult = new LinkedList<GffGeneIsoInfo>();
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		lsResult.add(gffGeneIsoInfo);
		return lsResult;
	}
	/**
	 * ����λ����ȡ����
	 * @param lsListGffName
	 */
	public void setGetSeqSite(ArrayList<SiteInfo> lsSiteName) {
		lsSiteInfos = lsSiteName;
		booGetIsoSeq = false;
	}
	/** ������Ǳ������ļ��У��Ϳ���ͨ���������ý�� */
	public ArrayList<SeqFasta> getLsResult() {
		return lsResult;
	}
	/**
	 * ��ָ��motif����ָ�������ָ�����򣬷��صõ���motif
	 * ��д���ı�
	 * @param regex
	 * 	����motif����ȫ�������ָ�������ϲ�����Ӧ��������ʽ<br>
	 * �����������кͷ������в��ҵĽ��<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: �����motif����<br>
	 * 3: motif���һ������뱾�����յ�ľ���
	 */
	public ArrayList<String[]> motifPromoterScan(String regex) {
		ArrayList<String[]> lsMotifResult = new ArrayList<String[]>();
		if (booGetIsoSeq) {
			for (GffGeneIsoInfo gffGeneIsoInfo : setIsoToGetSeq) {
				SeqFasta seqFasta = getSeq(gffGeneIsoInfo);
				if (seqFasta == null || seqFasta.Length() < 3) {
					continue;
				}
				lsMotifResult.addAll(seqFasta.getMotifScan().getMotifScanResult(regex));
			}
		}
		return lsMotifResult;
	}

	@Override
	protected void running() {
		getSeq();
	}
	/**
	 * ���趨��ȡ�����У�Ȼ��������ȡ��д���ı�
	 * @return
	 */
	public void getSeq() {
		if (getGenomWide) {
			getSeqIsoGenomWide();
		}
		if (saveToFile)
			txtOutFile = new TxtReadandWrite(outFile, true);
		
		int num = 0;
		boolean isGetSeq = false;
		if (booGetIsoSeq) {
			for (GffGeneIsoInfo gffGeneIsoInfo : setIsoToGetSeq) {
				num++;
				SeqFasta seqFasta = getSeq(gffGeneIsoInfo);

				isGetSeq = copeSeqFasta(seqFasta);
				
				suspendCheck();
				if (flagStop) {
					break;
				}
				setTmpInfo(isGetSeq, seqFasta, num);
			}
		}
		else {
			for (SiteInfo siteInfo : lsSiteInfos) {
				num++;
				getSeq(siteInfo);
				SeqFasta seqFasta = siteInfo.getSeqFasta();
				isGetSeq = copeSeqFasta(seqFasta);
				
				suspendCheck();
				if (flagStop) {
					break;
				}
				setTmpInfo(isGetSeq, seqFasta, num);
			}
		}
		if (saveToFile)
			txtOutFile.close();
	}
	/** �趨�м���� */
	private void setTmpInfo(boolean isGetSeq, SeqFasta seqFasta, int number) {
		if (!isGetSeq) {
			return;
		}
		GffChrSeqProcessInfo gffChrSeqProcessInfo = new GffChrSeqProcessInfo(number);
		if (getAAseq) {
			gffChrSeqProcessInfo.setSeqFasta(seqFasta.toStringAAfasta());
		}
		else {
			gffChrSeqProcessInfo.setSeqFasta(seqFasta.toStringNRfasta());
		}
		setRunInfo(gffChrSeqProcessInfo);
	}
	
	private GffGeneIsoInfo getIso(String IsoName) {
		if (getAllIso)
			return gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			return gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
	}
	
	
	/** �����Ƿ��ȡ������ */
	private boolean copeSeqFasta(SeqFasta seqFasta) {
		if (seqFasta == null || seqFasta.Length() < 3) {
			return false;
		}
		if (saveToFile) {
			if (getAAseq) {
				txtOutFile.writefileln(seqFasta.toStringAAfasta());
			}
			else {
				txtOutFile.writefileln(seqFasta.toStringNRfasta());
			}
		}
		else {
			lsResult.add(seqFasta);
		}
		return true;
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
		GffGeneIsoInfo gffGeneIsoInfo = getIso(IsoName);
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), startExon, endExon, gffGeneIsoInfo, getIntron);
		if (seqFasta == null) {
			return null;
		}
		seqFasta.setName(IsoName);
		return seqFasta;
	}
	public SeqFasta getSeq(String IsoName) {
		GffGeneIsoInfo gffGeneIsoInfo = getIso(IsoName);
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
		if (gffGeneIsoInfo == null) {
			return null;
		}
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
			return getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),tssRange[0], tssRange[1]);
		}
		else if (geneStructure.equals(GeneStructure.TES)) {
			return getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),tesRange[0], tesRange[1]);
		}
		if (lsExonInfos.size() == 0) {
			return null;
		}
		SeqFasta seqFastaResult = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsExonInfos, getIntron);
		if (seqFastaResult == null) {
			return null;
		}
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
				if (seq == null) {
					continue;
				}
				seq.setName(gffGeneIsoInfo.getName());
				txtFasta.writefileln(seq.toStringNRfasta());
			}
		}
		txtFasta.close();
	}
	
	public static class GffChrSeqProcessInfo {
		int number;
		ArrayList<String> lsTmpInfo = new ArrayList<String>();
		public GffChrSeqProcessInfo(int number) {
			this.number = number;
		}
		public void setSeqFasta(String string) {
			lsTmpInfo.add(string);
		}
		public int getNumber() {
			return number;
		}
		public ArrayList<String> getLsTmpInfo() {
			return lsTmpInfo;
		}
	}

}

