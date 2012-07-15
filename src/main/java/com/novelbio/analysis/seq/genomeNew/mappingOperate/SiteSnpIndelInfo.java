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
 * 对于单个位点的snp与indel的情况
 * @author zong0jie
 */
public abstract class SiteSnpIndelInfo implements Comparable<SiteSnpIndelInfo> {
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfo.class);
	//TODO 添加是否引起剪接位点变化的flag
	public static final int SPLIT_ATG = 12;
	public static final int SPLIT_UAG = 24;
	public static final int SPLIT_SPLIT_START = 36;
	public static final int SPLIT_SPLIT_END = 48;
	/** deletion 跨过一个intron，这就影响了一个start和一个end */
	public static final int SPLIT_SPLIT_START_END = SPLIT_SPLIT_START + SPLIT_SPLIT_END;
	public static final int SPLIT_SPLIT_NONE = 0;
	
	MapInfoSnpIndel mapInfoSnpIndel;
	/** snp所在refnr上的位置 */
	int snpOnReplaceLocStart = 0;
	int snpOnReplaceLocEnd = 0;
	/** 移码，0，1，2三种 */
	int orfShift = 0; 
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	MapInfo mapinfoRefSeqIntactAA = new MapInfo();
	String referenceSeq;
	String thisSeq;
	/** 位点处在内含子还是外显子还是基因外，如果是deletion，那么优先看是否覆盖了exon */
	int codLocInfo = 0;
	boolean isInCDS = false;
	/** 用MapInfoSnpIndel的type */
	int snpType = MapInfoSnpIndel.TYPE_CORRECT;
	int thisBaseNum = 0;
	int splitType = SPLIT_SPLIT_NONE;
	SnpIndelRs snpIndelRs;
	ServSnpIndelRs servSnpIndelRs = new ServSnpIndelRs();
	/**
	 * @param mapInfoSnpIndel 必须含有 GffIso 信息
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
	/** 如果snp位点在exon上，那么就设置ref序列的氨基酸的信息 */
	protected abstract void setMapInfoRefSeqAAabs(GffChrAbs gffChrAbs);
	
	public void setThisBaseNum(int thisBaseNum) {
		this.thisBaseNum = thisBaseNum;
	}
	/**计数加一 */
	protected void addThisBaseNum() {
		this.thisBaseNum++;
	}
	/**
	 * 移码突变
	 * @param orfShift
	 */
	public int getOrfShift() {
		return orfShift;
	}
	public String getThisSeq() {
		return thisSeq;
	}
	/** 在该snp或indel情况下，相对的ref的序列 */
	public String getReferenceSeq() {
		return referenceSeq;
	}
	public int getSnpIndelType() {
		return snpType;
	}
	public int getThisBaseNum() {
		return thisBaseNum;
	}
	/** 本snp占总snp的比例 */
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
	 * 如果一个位点有两个以上的snp，就可能会出错
	 * 获得本snp位置最多变异的AA序列
	 * 注意要通过{@link #setCis5to3(Boolean)}来设定 插入序列在基因组上是正向还是反向
	 * 还要通过{@link #setReplaceLoc(int)}来设定插入在refnr上的位置
	 * @return 没有的话就返回一个空的seqfasta
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
	 * 跟方向相关
	 * 给定序列和起始位点，用snp位点去替换序列，同时将本次替换是否造成移码写入orfshift
	 * @param thisSeq 给定序列--该序列必须是正向，然后
	 * @param cis5to3 给定序列的正反向
	 * @param startLoc  实际位点 在序列的哪一个点开始替换，替换包括该位点 0表示插到最前面。1表示从第一个开始替换
	 * 如果ref为""，则将序列插入在startBias那个碱基的后面
	 * @param endLoc 实际位点 在序列的哪一个点结束替换，替换包括该位点
	 * @return
	 */
	private SeqFasta replaceSnpIndel(String replace, int startLoc, int endLoc) {
		SeqFasta seqFasta = mapinfoRefSeqIntactAA.getSeqFasta().clone();
		if (seqFasta.toString().equals("")) {
			return new SeqFasta();
		}
		seqFasta.modifySeq(startLoc, endLoc, replace, false, false);
		//修改移码
		return seqFasta;
	}
	/**
	 * 设定snpID，自动获得对应的DBsnp信息
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
	 * 设定DBsnp的信息，有设定flag就当snp，没有设定flag就当indel
	 * @param snpIndelRs
	 */
	private void setSnpIndelRs() {
		SnpIndelRs snpIndelRs = new SnpIndelRs();
		snpIndelRs.setChrID(mapinfoRefSeqIntactAA.getRefID());
		snpIndelRs.setTaxID(mapInfoSnpIndel.getTaxID());
		snpIndelRs.setLocStart(mapInfoSnpIndel.getRefSnpIndelStart());
		//TODO 输入待查询的序列
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
	 * 如果在SNPDB中有记载，获得记载的信息
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
		//TODO 需要修正
		return (mapinfoRefSeqIntactAA.getSeqFasta().toString().hashCode() + SepSign.SEP_INFO + thisSeq).hashCode() + thisBaseNum;
	}
	/** 返回snp类型
	 * (referenceSeq + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + snpType).toLowerCase()
	 * @return
	 */
	public String getSiteTypeInfo() {
		return (referenceSeq + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + snpType).toLowerCase();
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
	 * 必须与	public static String getMismatchInfo(String referenceSeq, String thisSeq)一致
	 * 返回一个string，记录snp的位置信息
	 * chrid + SepSign.SEP_ID+ locstart + SepSign.SEP_ID + referenceSeq + SepSign.SEP_ID + thisSeq
	 * @return
	 */
	public String getMismatchInfo() {
		return (mapInfoSnpIndel.getRefID() + SepSign.SEP_ID+ mapInfoSnpIndel.getRefSnpIndelStart() 
				+ SepSign.SEP_ID + referenceSeq + SepSign.SEP_ID + thisSeq).toLowerCase();
	}
	/////////////////////////////////////// 静态方法，获得所有指定区域的位点的信息 ///////////////////////////////
	public static String getMyTitle() {
		String result = "ChrID\tSnpLoc\tRefBase\tAllelic_depths_Ref\tThisBase\tAllelic_depths_Alt \tQuality\tFilter\tAllele_Balance_Hets()\tIsInExon\tDistance_To_Start\t" + 
		"RefAAnr\tRefAAseq\tThisAAnr\tThisAASeq\tAA_chemical_property\tOrfShift\tSnpDB_ID\tGeneAccID\tGeneSymbol\tGeneDescription";
		return result;
	}
	/**
	 * 必须与 public String getMismatchInfo() 一致
	 * 返回一个string，记录snp的位置信息
	 * chrid + SepSign.SEP_ID+ locstart + SepSign.SEP_ID + referenceSeq + SepSign.SEP_ID + thisSeq
	 * @return
	 */
	public static String getMismatchInfo(String chrID, int Loc, String referenceSeq, String thisSeq) {
		return (chrID + SepSign.SEP_ID + Loc + SepSign.SEP_ID + referenceSeq 
				+ SepSign.SEP_ID + thisSeq).toLowerCase();
	}
}
/**
 * 貌似与SiteSnpIndelInfoSnp一模一样
 * @author zong0jie
 *
 */
class SiteSnpIndelInfoInsert extends SiteSnpIndelInfo{
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfoInsert.class);
	
	public SiteSnpIndelInfoInsert(MapInfoSnpIndel mapInfoSnpIndel, GffChrAbs gffChrAbs, String refBase, String thisBase) {
		super(mapInfoSnpIndel, gffChrAbs, refBase, thisBase);
		if (refBase.length() > 1) {
			logger.error("refBase 大于1，可能不是插入，请核对：" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
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
		//mRNA层面
		//就算在外显子中，但是如果是非编码rna，或者在UTR区域中，也返回
		if (!gffGeneIsoInfo.isCodInAAregion(mapInfoSnpIndel.getRefSnpIndelStart())) {
			isInCDS = false;
			return;
		}
		setOrfShift();
		
		isInCDS = true;
		int LocStart = gffGeneIsoInfo.getLocAAbefore(mapInfoSnpIndel.getRefSnpIndelStart());//该位点所在AA的第一个loc
		int LocEnd = gffGeneIsoInfo.getLocAAend(mapInfoSnpIndel.getRefSnpIndelStart());
		if (LocEnd <0) {//如果不在转录本中
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
		orfShift = (3 - (thisSeq.length() - referenceSeq.length())%3) % 3;//待检查
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
 * 必须很短的deletion，譬如在20bp以内的deletion
 * @author zong0jie
 *
 */
class SiteSnpIndelInfoDeletion extends SiteSnpIndelInfo {
	Logger logger = Logger.getLogger(SiteSnpIndelInfoInsert.class);
	public SiteSnpIndelInfoDeletion(MapInfoSnpIndel mapInfoSnpIndel, GffChrAbs gffChrAbs, String refBase, String thisBase) {
		super(mapInfoSnpIndel, gffChrAbs, refBase, thisBase);
		if (refBase.length() <= 1 || thisBase.length() > 1) {
			logger.error("本位点可能不是缺失，请核对：" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
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
				logger.error("检查一下：" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
				return;
			}
			setOrfShiftAndReplaceSite(gffGeneIsoInfo, refStartCis, refEndCis);
			SeqFasta NR = gffChrAbs.getSeqHash().getSeq(mapinfoRefSeqIntactAA.getRefID(), lsTmp, false);
			mapinfoRefSeqIntactAA.setSeq(NR,false);//因为上面已经反向过了
		}
		
		else if (!isInCDS && gffGeneIsoInfo.getNumCodInEle(refStartCis) != gffGeneIsoInfo.getNumCodInEle(refEndCis)) {
			logger.error("缺失外显子："  + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
			isInCDS = true;
			return;
		}
		else {
			isInCDS = false;
			return;
		}
	}
	/**
	 * 如果deletion覆盖了exon--这个在前面必须首先判定
	 * 然后如果deletion的位点落到了exon外，则将该位点定位到所包含的最近的exon的边界上，如下图<br>
	 * 0--1----------------2---3---------cod1-------------4---5----------------6---7---------------cod2---------------8----9, 修正为<br>
	 * 0--1----------------2---3---------------------cod1(4)---5---------------6---(7)cod2----------------------------8----9<br>
	 * @param gffGeneIsoInfo
	 * @param refStartCis
	 * @param refEndCis
	 * @return
	 */
	private int[] getLocOutOfExonToNearistExonBounder(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		int[] bounder = new int[]{refStartCis, refEndCis};
		//将起点和终点转换到距离最近的exon上去
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
	 * 必须在{@link#setLocationInfo}结束后调用
	 * @param gffGeneIsoInfo
	 * @param refStartCis 必须在exon中
	 * @param refEndCis 必须在exon中
	 */
	private void setEffectSplitType(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		splitType = 0;
		int codStart2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(refStartCis);
		int codEnd2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(refEndCis);
		int codStart2UAGmRNA = gffGeneIsoInfo.getCod2UAG(refStartCis);
		int codEnd2UAGmRNA = gffGeneIsoInfo.getCod2ATG(refEndCis);
		
		if (codStart2ATGmRNA <=0 && codEnd2ATGmRNA >= 0 //横跨atg
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
	 * 设定该deletion处在哪个位置 ，或者说是否覆盖了exon
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
		//TODO 这里没有考虑一头在基因前一头在基因尾的情况
		else if (gffGeneIsoInfo.getCodLoc(startCis) == GffGeneIsoInfo.COD_LOC_OUT && gffGeneIsoInfo.getCodLoc(endCis) == GffGeneIsoInfo.COD_LOC_OUT) {
			codLocInfo = GffGeneIsoInfo.COD_LOC_OUT;
		}
		else if (gffGeneIsoInfo.getCodLoc(startCis) == GffGeneIsoInfo.COD_LOC_INTRON && gffGeneIsoInfo.getCodLoc(endCis) == GffGeneIsoInfo.COD_LOC_INTRON
				&& gffGeneIsoInfo.getNumCodInEle(startCis) == gffGeneIsoInfo.getNumCodInEle(endCis)
				) {
			codLocInfo = GffGeneIsoInfo.COD_LOC_INTRON;
		}
	}
	/** 需要测试 */
	protected void setOrfShiftAndReplaceSite(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		int deletionLen = gffGeneIsoInfo.getLocDistmRNA(refStartCis, refEndCis) + 1 - thisSeq.length();
		orfShift = deletionLen%3;

		snpOnReplaceLocStart = -gffGeneIsoInfo.getLocAAbeforeBias(refStartCis) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart + deletionLen;
	}
}