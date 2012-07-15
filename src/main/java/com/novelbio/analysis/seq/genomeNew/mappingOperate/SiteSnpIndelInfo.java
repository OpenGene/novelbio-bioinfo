package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.database.service.servgeneanno.ServSnpIndelRs;


/**
 * ���ڵ���λ���snp��indel�����
 * @author zong0jie
 */
public abstract class SiteSnpIndelInfo implements Comparable<SiteSnpIndelInfo> {
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfo.class);
	//TODO ����Ƿ��������λ��仯��flag
	public static final int SPLIT_ATG = 12;
	public static final int SPLIT_UAG = 24;
	public static final int SPLIT_SPLIT_START = 36;
	public static final int SPLIT_SPLIT_END = 48;
	/** deletion ���һ��intron�����Ӱ����һ��start��һ��end */
	public static final int SPLIT_SPLIT_START_END = SPLIT_SPLIT_START + SPLIT_SPLIT_END;
	public static final int SPLIT_SPLIT_NONE = 0;
	
	MapInfoSnpIndel mapInfoSnpIndel;
	/** snp����refnr�ϵ�λ�� */
	int snpOnReplaceLocStart = 0;
	int snpOnReplaceLocEnd = 0;
	/** ���룬0��1��2���� */
	int orfShift = 0; 
	/** ���snp������exon�ϣ������������ref��Ӱ�쵽�İ���������� */
	MapInfo mapinfoRefSeqIntactAA = new MapInfo();
	String referenceSeq;
	String thisSeq;
	/** λ�㴦���ں��ӻ��������ӻ��ǻ����⣬�����deletion����ô���ȿ��Ƿ񸲸���exon */
	int codLocInfo = 0;
	boolean isInCDS = false;
	/** ��MapInfoSnpIndel��type */
	int snpType = MapInfoSnpIndel.TYPE_CORRECT;
	int thisBaseNum = 0;
	int splitType = SPLIT_SPLIT_NONE;
	SnpIndelRs snpIndelRs;
	ServSnpIndelRs servSnpIndelRs = new ServSnpIndelRs();
	/**
	 * @param mapInfoSnpIndel ���뺬�� GffIso ��Ϣ
	 * @param gffChrAbs
	 * @param refBase
	 * @param thisBase
	 */
	public SiteSnpIndelInfo(MapInfoSnpIndel mapInfoSnpIndel, GffChrAbs gffChrAbs, String refBase, String thisBase) {
		mapinfoRefSeqIntactAA.setRefID(mapInfoSnpIndel.getRefID());
		this.mapInfoSnpIndel = mapInfoSnpIndel;
		this.thisSeq = thisBase;
		this.referenceSeq = refBase;
		setMapInfoRefSeqAA(gffChrAbs);
	}
	
	private void setMapInfoRefSeqAA(GffChrAbs gffChrAbs) {
		if (gffChrAbs == null)
			return;
		
		if (mapInfoSnpIndel.getGffIso() == null)
			return;

		setMapInfoRefSeqAAabs(gffChrAbs);
	}
	/** ���snpλ����exon�ϣ���ô������ref���еİ��������Ϣ */
	protected abstract void setMapInfoRefSeqAAabs(GffChrAbs gffChrAbs);
	
	public void setThisBaseNum(int thisBaseNum) {
		this.thisBaseNum = thisBaseNum;
	}
	/**������һ */
	protected void addThisBaseNum() {
		this.thisBaseNum++;
	}
	/**
	 * ����ͻ��
	 * @param orfShift
	 */
	public int getOrfShift() {
		return orfShift;
	}
	public String getThisSeq() {
		return thisSeq;
	}
	/** �ڸ�snp��indel����£���Ե�ref������ */
	public String getReferenceSeq() {
		return referenceSeq;
	}
	public int getSnpIndelType() {
		return snpType;
	}
	public int getThisBaseNum() {
		return thisBaseNum;
	}
	/** ��snpռ��snp�ı��� */
	public double getThisBaseProp() {
		return (double)thisBaseNum/mapInfoSnpIndel.getRead_Depth_Filtered();
	}
	/**
	 * Allele Balance for hets
	 * (ref/(ref+alt))
	 * @return
	 */
	public double getAllele_Balance_Hets() {
		return (double)mapInfoSnpIndel.getAllelic_depths_Ref()/(mapInfoSnpIndel.getAllelic_depths_Ref()+thisBaseNum);
	}
	/**
	 * ���һ��λ�����������ϵ�snp���Ϳ��ܻ����
	 * ��ñ�snpλ���������AA����
	 * ע��Ҫͨ��{@link #setCis5to3(Boolean)}���趨 ���������ڻ��������������Ƿ���
	 * ��Ҫͨ��{@link #setReplaceLoc(int)}���趨������refnr�ϵ�λ��
	 * @return û�еĻ��ͷ���һ���յ�seqfasta
	 */
	public SeqFasta getThisAAnr() {
		String seq = thisSeq;
		if ( mapinfoRefSeqIntactAA.isCis5to3() != null && !mapinfoRefSeqIntactAA.isCis5to3()) {
			seq = SeqFasta.reservecom(seq);
		}
		if (mapInfoSnpIndel.getGffIso() == null)
			return new SeqFasta();
	
		return replaceSnpIndel(seq, snpOnReplaceLocStart, snpOnReplaceLocEnd);
	}
	
	public String getRefAAnr() {
		return mapinfoRefSeqIntactAA.getSeqFasta().toStringAA();
	}
	/**
	 * ���������
	 * �������к���ʼλ�㣬��snpλ��ȥ�滻���У�ͬʱ�������滻�Ƿ��������д��orfshift
	 * @param thisSeq ��������--�����б���������Ȼ��
	 * @param cis5to3 �������е�������
	 * @param startLoc  ʵ��λ�� �����е���һ���㿪ʼ�滻���滻������λ�� 0��ʾ�嵽��ǰ�档1��ʾ�ӵ�һ����ʼ�滻
	 * ���refΪ""�������в�����startBias�Ǹ�����ĺ���
	 * @param endLoc ʵ��λ�� �����е���һ��������滻���滻������λ��
	 * @return
	 */
	private SeqFasta replaceSnpIndel(String replace, int startLoc, int endLoc) {
		SeqFasta seqFasta = mapinfoRefSeqIntactAA.getSeqFasta().clone();
		if (seqFasta.toString().equals("")) {
			return new SeqFasta();
		}
		seqFasta.modifySeq(startLoc, endLoc, replace, false, false);
		//�޸�����
		return seqFasta;
	}
	/**
	 * �趨snpID���Զ���ö�Ӧ��DBsnp��Ϣ
	 * @param snpRsID
	 */
	public void setDBSnpID(String snpRsID) {
		if (snpRsID != null && !snpRsID.trim().equals("")) {
			SnpIndelRs snpIndelRs = new SnpIndelRs();
			snpIndelRs.setSnpRsID(snpRsID);
			this.snpIndelRs = servSnpIndelRs.querySnpIndelRs(snpIndelRs);
		}
	}
	/**
	 * �趨DBsnp����Ϣ�����趨flag�͵�snp��û���趨flag�͵�indel
	 * @param snpIndelRs
	 */
	private void setSnpIndelRs() {
		SnpIndelRs snpIndelRs = new SnpIndelRs();
		snpIndelRs.setChrID(mapinfoRefSeqIntactAA.getRefID());
		snpIndelRs.setTaxID(mapInfoSnpIndel.getTaxID());
		snpIndelRs.setLocStart(mapInfoSnpIndel.getRefSnpIndelStart());
		//TODO �������ѯ������
//		snpIndelRs.setObserved(observed);
		this.snpIndelRs = servSnpIndelRs.querySnpIndelRs(snpIndelRs);
	}
	public boolean isExon() {
		if (codLocInfo != GffGeneIsoInfo.COD_LOC_EXON) {
			return false;
		}
		return true;
	}
	/**
	 * �����SNPDB���м��أ���ü��ص���Ϣ
	 * @return
	 */
	public SnpIndelRs getSnpIndelRs() {
		if (snpIndelRs != null) {
			return snpIndelRs;
		}
		setSnpIndelRs();
		return snpIndelRs;
	}

	@Override
	public int hashCode() {
		return getMismatchInfo().hashCode() + thisBaseNum;
	}
	@Override
	public int compareTo(SiteSnpIndelInfo o) {
		Integer thisNum = thisBaseNum;
		Integer otherNum = o.thisBaseNum;
		return thisNum.compareTo(otherNum);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		SiteSnpIndelInfo otherObj = (SiteSnpIndelInfo)obj;
		if (
				mapinfoRefSeqIntactAA.equals(otherObj.mapinfoRefSeqIntactAA)
				&& thisSeq.equals(otherObj.thisSeq)
				&& thisBaseNum == otherObj.thisBaseNum
				&& snpType == otherObj.snpType
			)
		{
			return true;
		}
		return false;
	}
	public String toString() {
		String refnr =  mapinfoRefSeqIntactAA.getSeqFasta().toString();
		String refaa =  mapinfoRefSeqIntactAA.getSeqFasta().toStringAA(false);
		String thisnr =  getThisAAnr().toString();
		String thisaa = getThisAAnr().toStringAA(false);
		
		String result =  mapinfoRefSeqIntactAA.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart() + "\t" + referenceSeq + "\t" + mapInfoSnpIndel.getAllelic_depths_Ref() + "\t" + thisSeq + "\t" + 
		getThisBaseNum() + "\t" + mapInfoSnpIndel.quality + "\t" + mapInfoSnpIndel.Filter + "\t" + "\t" + getAllele_Balance_Hets() + "\t" + isExon()+"\t" + mapInfoSnpIndel.getProp() +"\t"+
		refnr +"\t"+refaa + "\t" + thisnr +"\t"+thisaa;
		if (refaa.length() ==3  && thisaa.length() == 3) {
			result = result + "\t" + SeqFasta.cmpAAquality(refaa, thisaa);
		}
		else {
			result = result + "\t" + "";
		}
		result = result + "\t" + this.getOrfShift();
		result = result + "\t" + snpIndelRs.getSnpRsID();
		if (mapInfoSnpIndel.getGffIso() != null) {
			result = result + "\t" + mapInfoSnpIndel.getGffIso().getName();
			GeneID copedID = new GeneID(mapInfoSnpIndel.getGffIso().getName(), mapInfoSnpIndel.getTaxID(), false);
			result = result + "\t" + copedID.getSymbol() +"\t"+copedID.getDescription();
		}
		else
			result = result + "\t \t \t " ;
		return result;
	}
	/**
	 * ������	public static String getMismatchInfo(String referenceSeq, String thisSeq)һ��
	 * ����һ��string����¼snp��λ����Ϣ
	 * chrid + SepSign.SEP_ID+ locstart + SepSign.SEP_ID + referenceSeq + SepSign.SEP_ID + thisSeq
	 * @return
	 */
	public String getMismatchInfo() {
		return (mapInfoSnpIndel.getRefID() + SepSign.SEP_ID+ mapInfoSnpIndel.getRefSnpIndelStart() 
				+ SepSign.SEP_ID + referenceSeq + SepSign.SEP_ID + thisSeq).toLowerCase();
	}
	/////////////////////////////////////// ��̬�������������ָ�������λ�����Ϣ ///////////////////////////////
	public static String getMyTitle() {
		String result = "ChrID\tSnpLoc\tRefBase\tAllelic_depths_Ref\tThisBase\tAllelic_depths_Alt \tQuality\tFilter\tAllele_Balance_Hets()\tIsInExon\tDistance_To_Start\t" + 
		"RefAAnr\tRefAAseq\tThisAAnr\tThisAASeq\tAA_chemical_property\tOrfShift\tSnpDB_ID\tGeneAccID\tGeneSymbol\tGeneDescription";
		return result;
	}
	/**
	 * ������ public String getMismatchInfo() һ��
	 * ����һ��string����¼snp��λ����Ϣ
	 * chrid + SepSign.SEP_ID+ locstart + SepSign.SEP_ID + referenceSeq + SepSign.SEP_ID + thisSeq
	 * @return
	 */
	public static String getMismatchInfo(String chrID, int Loc, String referenceSeq, String thisSeq) {
		return (chrID + SepSign.SEP_ID + Loc + SepSign.SEP_ID + referenceSeq 
				+ SepSign.SEP_ID + thisSeq).toLowerCase();
	}
}
/**
 * ò����SiteSnpIndelInfoSnpһģһ��
 * @author zong0jie
 *
 */
class SiteSnpIndelInfoInsert extends SiteSnpIndelInfo{
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfoInsert.class);
	
	public SiteSnpIndelInfoInsert(MapInfoSnpIndel mapInfoSnpIndel, GffChrAbs gffChrAbs, String refBase, String thisBase) {
		super(mapInfoSnpIndel, gffChrAbs, refBase, thisBase);
		if (refBase.length() > 1) {
			logger.error("refBase ����1�����ܲ��ǲ��룬��˶ԣ�" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
		}
		super.snpType = MapInfoSnpIndel.TYPE_INSERT;
	}
	@Override
	protected void setMapInfoRefSeqAAabs(GffChrAbs gffChrAbs) {
		GffGeneIsoInfo gffGeneIsoInfo = mapInfoSnpIndel.getGffIso();
		codLocInfo = gffGeneIsoInfo.getCodLoc(mapInfoSnpIndel.getRefSnpIndelStart());
		if (codLocInfo == GffGeneIsoInfo.COD_LOC_EXON) {
			setEffectSplitType(gffGeneIsoInfo, mapInfoSnpIndel.getRefSnpIndelStart());
		}
		//mRNA����
		//�������������У���������ǷǱ���rna��������UTR�����У�Ҳ����
		if (!gffGeneIsoInfo.isCodInAAregion(mapInfoSnpIndel.getRefSnpIndelStart())) {
			isInCDS = false;
			return;
		}
		setOrfShift();
		
		isInCDS = true;
		int LocStart = gffGeneIsoInfo.getLocAAbefore(mapInfoSnpIndel.getRefSnpIndelStart());//��λ������AA�ĵ�һ��loc
		int LocEnd = gffGeneIsoInfo.getLocAAend(mapInfoSnpIndel.getRefSnpIndelStart());
		if (LocEnd <0) {//�������ת¼����
			if (gffGeneIsoInfo.isCis5to3()) {
				LocEnd = LocStart + 2;
			}
			else {
				LocEnd = LocStart - 2;
			}
		}
		SeqFasta NR = null;
		ArrayList<ExonInfo> lsTmp = gffGeneIsoInfo.getRangeIso(LocStart, LocEnd);
		if (lsTmp == null) {
			NR = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), mapInfoSnpIndel.getRefID(), LocStart, LocEnd);
		}
		else {
			NR = gffChrAbs.getSeqHash().getSeq(mapInfoSnpIndel.getRefID(), lsTmp, false);
		}
		mapinfoRefSeqIntactAA.setCis5to3(gffGeneIsoInfo.isCis5to3());
		mapinfoRefSeqIntactAA.setSeq(NR,false);
		snpOnReplaceLocStart = -gffGeneIsoInfo.getLocAAbeforeBias(mapInfoSnpIndel.getRefSnpIndelStart()) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart;
	}
	private void setEffectSplitType(GffGeneIsoInfo gffGeneIsoInfo, int codLoc) {
		int cod2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(codLoc);
		int cod2UAGmRNA = gffGeneIsoInfo.getCod2UAGmRNA(codLoc);
		if (cod2ATGmRNA >= 0 && cod2ATGmRNA <= 2) {
			splitType = SPLIT_ATG;
		}
		else if (cod2UAGmRNA <= 0 && cod2UAGmRNA >= -2) {
			splitType = SPLIT_UAG;
		}
		else {
			int locNum = gffGeneIsoInfo.getNumCodInEle(codLoc);
			if (locNum != 1 && gffGeneIsoInfo.getCod2ExInStart(codLoc) <= 1) {
				splitType = SPLIT_SPLIT_START;
			}
			else if (locNum != gffGeneIsoInfo.size() && gffGeneIsoInfo.getCod2ExInEnd(codLoc) <= 1) {
				splitType = SPLIT_SPLIT_END;
			}
		}
	}
	protected void setOrfShift() {
		orfShift = (3 - (thisSeq.length() - referenceSeq.length())%3) % 3;//�����
	}
	
}

class SiteSnpIndelInfoSnp extends SiteSnpIndelInfoInsert {
	public SiteSnpIndelInfoSnp(MapInfoSnpIndel mapInfoSnpIndel, GffChrAbs gffChrAbs, String refBase, String thisBase) {
		super(mapInfoSnpIndel, gffChrAbs, refBase, thisBase);
		super.snpType = MapInfoSnpIndel.TYPE_MISMATCH;
	}
	protected void setOrfShift() {
		orfShift = 0;
	}
}
/**
 * ����̵ܶ�deletion��Ʃ����20bp���ڵ�deletion
 * @author zong0jie
 *
 */
class SiteSnpIndelInfoDeletion extends SiteSnpIndelInfo {
	Logger logger = Logger.getLogger(SiteSnpIndelInfoInsert.class);
	public SiteSnpIndelInfoDeletion(MapInfoSnpIndel mapInfoSnpIndel, GffChrAbs gffChrAbs, String refBase, String thisBase) {
		super(mapInfoSnpIndel, gffChrAbs, refBase, thisBase);
		if (refBase.length() <= 1 || thisBase.length() > 1) {
			logger.error("��λ����ܲ���ȱʧ����˶ԣ�" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
		}
		super.snpType = MapInfoSnpIndel.TYPE_DELETION;
	}

	@Override
	protected void setMapInfoRefSeqAAabs(GffChrAbs gffChrAbs) {
		int refStart = mapInfoSnpIndel.getRefSnpIndelStart();
		int refEnd = refStart + referenceSeq.length() - 1;
		int refStartCis = refStart; int refEndCis = refEnd;
		
		GffGeneIsoInfo gffGeneIsoInfo = mapInfoSnpIndel.getGffIso();
		if (gffGeneIsoInfo.isCis5to3()) {
			refStartCis = refEnd; refEndCis = refStart;
		}
		setLocationInfo(gffGeneIsoInfo, refStartCis, refEndCis);
		
		if (codLocInfo != GffGeneIsoInfo.COD_LOC_EXON) {
			return;
		}
		
		int[] bound = getLocOutOfExonToNearistExonBounder(gffGeneIsoInfo, refStartCis, refEndCis);
		refStartCis = bound[0]; refEndCis = bound[1];
		
		setEffectSplitType(gffGeneIsoInfo, refStartCis, refEndCis);
		isInCDS = false;
		if (gffGeneIsoInfo.isCodInAAregion(refStartCis) || gffGeneIsoInfo.isCodInAAregion(refEndCis)) {
			isInCDS = true;
			if (!gffGeneIsoInfo.isCodInAAregion(refStartCis)) {
				refStartCis = gffGeneIsoInfo.getATGsite();
			}
			else if (!gffGeneIsoInfo.isCodInAAregion(refEndCis)) {
				refEndCis = gffGeneIsoInfo.getUAGsite();
			}
		}
		if (isInCDS) {
			int LocStart = gffGeneIsoInfo.getLocAAbefore(refStartCis);
			int LocEnd =gffGeneIsoInfo.getLocAAend(refEndCis);
			mapinfoRefSeqIntactAA.setStartEndLoc(LocStart, LocEnd);
			ArrayList<ExonInfo> lsTmp = gffGeneIsoInfo.getRangeIso(LocStart, LocEnd);
			if (lsTmp == null) {
				logger.error("���һ�£�" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
				return;
			}
			setOrfShiftAndReplaceSite(gffGeneIsoInfo, refStartCis, refEndCis);
			SeqFasta NR = gffChrAbs.getSeqHash().getSeq(mapinfoRefSeqIntactAA.getRefID(), lsTmp, false);
			mapinfoRefSeqIntactAA.setSeq(NR,false);//��Ϊ�����Ѿ��������
		}
		
		else if (!isInCDS && gffGeneIsoInfo.getNumCodInEle(refStartCis) != gffGeneIsoInfo.getNumCodInEle(refEndCis)) {
			logger.error("ȱʧ�����ӣ�"  + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
			isInCDS = true;
			return;
		}
		else {
			isInCDS = false;
			return;
		}
	}
	/**
	 * ���deletion������exon--�����ǰ����������ж�
	 * Ȼ�����deletion��λ���䵽��exon�⣬�򽫸�λ�㶨λ���������������exon�ı߽��ϣ�����ͼ<br>
	 * 0--1----------------2---3---------cod1-------------4---5----------------6---7---------------cod2---------------8----9, ����Ϊ<br>
	 * 0--1----------------2---3---------------------cod1(4)---5---------------6---(7)cod2----------------------------8----9<br>
	 * @param gffGeneIsoInfo
	 * @param refStartCis
	 * @param refEndCis
	 * @return
	 */
	private int[] getLocOutOfExonToNearistExonBounder(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		int[] bounder = new int[]{refStartCis, refEndCis};
		//�������յ�ת�������������exon��ȥ
		if (gffGeneIsoInfo.getCodLoc(refStartCis) != GffGeneIsoInfo.COD_LOC_EXON) {
			int startExonNum = gffGeneIsoInfo.getNumCodInEle(refStartCis);
			bounder[0] = gffGeneIsoInfo.get(startExonNum).getStartCis();
		}
		else if (gffGeneIsoInfo.getCodLoc(refEndCis) != GffGeneIsoInfo.COD_LOC_EXON) {
			int endExonNum = gffGeneIsoInfo.getNumCodInEle(refEndCis) - 1;
			bounder[1] = gffGeneIsoInfo.get(endExonNum).getEndCis();
		}
		return bounder;
	}
	/**
	 * ������{@link#setLocationInfo}���������
	 * @param gffGeneIsoInfo
	 * @param refStartCis ������exon��
	 * @param refEndCis ������exon��
	 */
	private void setEffectSplitType(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		splitType = 0;
		int codStart2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(refStartCis);
		int codEnd2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(refEndCis);
		int codStart2UAGmRNA = gffGeneIsoInfo.getCod2UAG(refStartCis);
		int codEnd2UAGmRNA = gffGeneIsoInfo.getCod2ATG(refEndCis);
		
		if (codStart2ATGmRNA <=0 && codEnd2ATGmRNA >= 0 //���atg
			|| 	codStart2ATGmRNA >= 0 && codStart2ATGmRNA <= 2
		  ) {
			splitType = SPLIT_ATG;
		}
		else if (codStart2UAGmRNA <= 0 && codEnd2UAGmRNA >= 0
				|| codEnd2ATGmRNA <= 0 && codEnd2ATGmRNA >= -2
		) {
			splitType = SPLIT_UAG;
		}
		else if (gffGeneIsoInfo.getNumCodInEle(refStartCis) != gffGeneIsoInfo.getNumCodInEle(refEndCis)) {
			splitType = SPLIT_SPLIT_START_END;
		}
		else {
			int locNum = gffGeneIsoInfo.getNumCodInEle(refStartCis);
			if (locNum != 1 && gffGeneIsoInfo.getCod2ExInStart(refStartCis) <= 1) {
				splitType = SPLIT_SPLIT_START;
			}
			if (locNum != gffGeneIsoInfo.size() && gffGeneIsoInfo.getCod2ExInEnd(refEndCis) <= 1) {
				splitType = splitType + SPLIT_SPLIT_END;
			}
		}
	}
	/**
	 * �趨��deletion�����ĸ�λ�� ������˵�Ƿ񸲸���exon
	 * @param gffGeneIsoInfo
	 * @param startCis
	 * @param endCis
	 */
	private void setLocationInfo(GffGeneIsoInfo gffGeneIsoInfo, int startCis, int endCis) {
		if (gffGeneIsoInfo.getCodLoc(startCis) == GffGeneIsoInfo.COD_LOC_EXON || gffGeneIsoInfo.getCodLoc(endCis) == GffGeneIsoInfo.COD_LOC_EXON
				|| gffGeneIsoInfo.getNumCodInEle(startCis) != gffGeneIsoInfo.getNumCodInEle(endCis)
				) {
			codLocInfo = GffGeneIsoInfo.COD_LOC_EXON;
		}
		//TODO ����û�п���һͷ�ڻ���ǰһͷ�ڻ���β�����
		else if (gffGeneIsoInfo.getCodLoc(startCis) == GffGeneIsoInfo.COD_LOC_OUT && gffGeneIsoInfo.getCodLoc(endCis) == GffGeneIsoInfo.COD_LOC_OUT) {
			codLocInfo = GffGeneIsoInfo.COD_LOC_OUT;
		}
		else if (gffGeneIsoInfo.getCodLoc(startCis) == GffGeneIsoInfo.COD_LOC_INTRON && gffGeneIsoInfo.getCodLoc(endCis) == GffGeneIsoInfo.COD_LOC_INTRON
				&& gffGeneIsoInfo.getNumCodInEle(startCis) == gffGeneIsoInfo.getNumCodInEle(endCis)
				) {
			codLocInfo = GffGeneIsoInfo.COD_LOC_INTRON;
		}
	}
	/** ��Ҫ���� */
	protected void setOrfShiftAndReplaceSite(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		int deletionLen = gffGeneIsoInfo.getLocDistmRNA(refStartCis, refEndCis) + 1 - thisSeq.length();
		orfShift = deletionLen%3;

		snpOnReplaceLocStart = -gffGeneIsoInfo.getLocAAbeforeBias(refStartCis) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart + deletionLen;
	}
}