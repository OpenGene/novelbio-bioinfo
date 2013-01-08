package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ServSnpIndelRs;


/**
 * 对于单个位点的snp与indel的情况，可以保存多个不同的样本。
 * 在setSampleName()方法中可设定样本名，并获得该样本的信息。
 * @author zong0jie
 */
public abstract class SiteSnpIndelInfo {
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfo.class);
	
	String sampleName;
	
	RefSiteSnpIndel refSiteSnpIndelParent;
	/** snp所在refnr上的位置 */
	int snpOnReplaceLocStart = 0;
	int snpOnReplaceLocEnd = 0;

	String referenceSeq;
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	SiteInfo refSeqIntactAA = new SiteInfo();
	
	String thisSeq;
	
	/** 位点处在内含子还是外显子还是基因外，如果是deletion，那么优先看是否覆盖了exon */
	int codLocInfo = 0;
	boolean isInCDS = false;

	SplitType splitType = SplitType.NONE;
	SnpIndelRs snpIndelRs;
	ServSnpIndelRs servSnpIndelRs = new ServSnpIndelRs();
	/** 样本名对应该样本这类型snp的reads数量*/
	Map<String, SampleSnpReadsQuality> mapSample2thisBaseNum = new HashMap<String, SampleSnpReadsQuality>();
	/**
	 * @param refSiteSnpIndel 必须含有 GffIso 信息
	 * @param gffChrAbs
	 * @param refBase
	 * @param thisBase
	 */
	public SiteSnpIndelInfo(RefSiteSnpIndel refSiteSnpIndel, String refBase, String thisBase) {
		refSeqIntactAA.setRefID(refSiteSnpIndel.getRefID());
		this.refSiteSnpIndelParent = refSiteSnpIndel;
		this.thisSeq = thisBase;
		this.referenceSeq = refBase;
	}
	
	/** 根据parent，设定GffChrAbs */
	protected void setSitGffChrAbs() {
		if (refSiteSnpIndelParent == null || refSiteSnpIndelParent.gffChrAbs == null)
			return;
		
		if (refSiteSnpIndelParent.getGffIso() == null)
			return;

		setMapInfoRefSeqAAabs(refSiteSnpIndelParent.gffChrAbs);
	}
	/** 如果Iso不存在，该方法不会被调用。
	 * 如果Iso存在，并且snp位点在exon上，那么就设置ref序列的氨基酸的信息 */
	protected abstract void setMapInfoRefSeqAAabs(GffChrAbs gffChrAbs);
	/**
	 * 设定样本名，那么后面获取的都是该样本的信息
	 * @param sampleName
	 */
	public void setSampleName(String sampleName) {
		if (sampleName == null) {
			return;
		}
		this.sampleName = sampleName;
	}
	
	/** 样本名对应该样本这类型snp的reads数量*/
	public Map<String, SampleSnpReadsQuality> getMapSample2thisBaseNum() {
		return mapSample2thisBaseNum;
	}
	
	/** 获得其父节点
	 * 父节点的SampleName会被设置成与该site一样的名字
	 *  
	 *  */
	public RefSiteSnpIndel getRefSiteSnpIndelParent() {
		refSiteSnpIndelParent.setSampleName(sampleName);
		return refSiteSnpIndelParent;
	}
	public boolean isContainSample(String sampleName) {
		return mapSample2thisBaseNum.containsKey(sampleName);
	}
	public String getSampleName() {
		return sampleName;
	}
	
	/**
	 * 移码突变
	 * @param orfShift
	 */
	public abstract int getOrfShift();
	
	public String getThisSeq() {
		return thisSeq;
	}
	public String getSplitTypeEffected() {
		return splitType.toString();
	}
	/** 在该snp或indel情况下，相对的ref的序列 */
	public String getReferenceSeq() {
		return referenceSeq;
	}
	public abstract SnpIndelType getSnpIndelType();
	
	public void setThisReadsNum(int readsNum) {
		SampleSnpReadsQuality sampleSnpReadsQuality = mapSample2thisBaseNum.get(sampleName);
		if (sampleSnpReadsQuality == null) {
			return;
		}
		 sampleSnpReadsQuality.thisReadsNum = readsNum;
	}
	public int getReadsNum() {
		SampleSnpReadsQuality sampleSnpReadsQuality = mapSample2thisBaseNum.get(sampleName);
		if (sampleSnpReadsQuality == null) {
			return 0;
		}
		return sampleSnpReadsQuality.thisReadsNum;
	}
	public void setQuality(String Quality) {
		SampleSnpReadsQuality sampleSnpReadsQuality = mapSample2thisBaseNum.get(sampleName);
		if (sampleSnpReadsQuality == null) {
			return;
		}
		 sampleSnpReadsQuality.quality = Quality;
	}
	public String getQuality() {
		SampleSnpReadsQuality sampleSnpReadsQuality = mapSample2thisBaseNum.get(sampleName);
		if (sampleSnpReadsQuality == null) {
			return "";
		}
		return sampleSnpReadsQuality.quality;
	}
	public void setVcfFilterInfo(String Filter) {
		SampleSnpReadsQuality sampleSnpReadsQuality = mapSample2thisBaseNum.get(sampleName);
		if (sampleSnpReadsQuality == null) {
			return;
		}
		 sampleSnpReadsQuality.vcfFilterInfo = Filter;
	}
	
	public String getVcfInfoFilter() {
		SampleSnpReadsQuality sampleSnpReadsQuality = mapSample2thisBaseNum.get(sampleName);
		if (sampleSnpReadsQuality == null) {
			return "";
		}
		return sampleSnpReadsQuality.vcfFilterInfo;
	}
	/** snp计数添加addNum */
	protected void addThisBaseNum(int addNum) {
		SampleSnpReadsQuality sampleSnpReadsQuality = mapSample2thisBaseNum.get(sampleName);
		sampleSnpReadsQuality.addThisReadsNum(addNum);
	}
	protected void setOrAddSampleInfo(String sampleName) {
		if (!mapSample2thisBaseNum.containsKey(sampleName)) {
			mapSample2thisBaseNum.put(sampleName, new SampleSnpReadsQuality());
		}
		this.sampleName = sampleName;
	}
	/** 本snp占总reads的比例 */
	public double getThisBasePropss() {
		return (double)getReadsNum()/refSiteSnpIndelParent.getReadsNumAll();
	}
	/**
	 * 返回变化的AA的化学性质改变形式，不在cds中则返回""；
	 * @return
	 */
	public String getAAattrConvert() {
		if (isCDS() && referenceSeq.length() == 1 && thisSeq.length() == 1) {
			String refAA = getRefAAnr().toStringAA1();
			String thisAA = getThisAAnr().toStringAA1();
			try {
				return SeqFasta.cmpAAquality(refAA, thisAA);
			} catch (Exception e) {
				logger.error("变化的AA的化学性质出错");
				return "";
			}
		}
		return "";
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
		if ( refSeqIntactAA.isCis5to3() != null && !refSeqIntactAA.isCis5to3()) {
			seq = SeqFasta.reservecom(seq);
		}
		if (refSiteSnpIndelParent.getGffIso() == null)
			return new SeqFasta();
	
		return replaceSnpIndel(seq, snpOnReplaceLocStart, snpOnReplaceLocEnd);
	}
	
	public SeqFasta getRefAAnr() {
		return refSeqIntactAA.getSeqFasta();
	}
	public String getAAchamicalConvert() {
		String refaa =  refSeqIntactAA.getSeqFasta().toStringAA(false);
		String thisaa = getThisAAnr().toStringAA(false);
		return SeqFasta.cmpAAquality(refaa, thisaa);
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
		SeqFasta seqFasta = refSeqIntactAA.getSeqFasta().clone();
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
			//TODO snp信息去查找数据库
//			this.snpIndelRs = servSnpIndelRs.querySnpIndelRs(snpIndelRs);
			
			//临时方案
			this.snpIndelRs = snpIndelRs;
		}
	}
	/**
	 * 设定DBsnp的信息，有设定flag就当snp，没有设定flag就当indel
	 * @param snpIndelRs
	 */
	private void setSnpIndelRs() {
		SnpIndelRs snpIndelRs = new SnpIndelRs();
		snpIndelRs.setChrID(refSeqIntactAA.getRefID());
		snpIndelRs.setTaxID(refSiteSnpIndelParent.getTaxID());
		snpIndelRs.setLocStart(refSiteSnpIndelParent.getRefSnpIndelStart());
		//TODO 输入待查询的序列
//		snpIndelRs.setObserved(observed);
//		this.snpIndelRs = servSnpIndelRs.querySnpIndelRs(snpIndelRs);
		
		//临时方案
		this.snpIndelRs = snpIndelRs;
	}
	public boolean isExon() {
		if (codLocInfo != GffGeneIsoInfo.COD_LOC_EXON) {
			return false;
		}
		return true;
	}
	public boolean isCDS() {
		return isInCDS;
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
	/**
	 * 仅考虑位点信息
	 */
	@Override
	public int hashCode() {
		return getMismatchInfo().hashCode();
	}
	/**
	 * 仅比较位点，不比较里面的sample
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		SiteSnpIndelInfo otherObj = (SiteSnpIndelInfo)obj;
		if (
				refSeqIntactAA.equals(otherObj.refSeqIntactAA)
				&& thisSeq.equals(otherObj.thisSeq)
				&& refSiteSnpIndelParent.getRefID().equals(otherObj.refSiteSnpIndelParent.getRefID())
				&& refSiteSnpIndelParent.getRefSnpIndelStart() == otherObj.refSiteSnpIndelParent.getRefSnpIndelStart()
			)
		{
			return true;
		}
		return false;
	}
	public String toString() {
		//TODO 改装为list的方式来返回string数组
		String refnr =  refSeqIntactAA.getSeqFasta().toString();
		String refaa =  refSeqIntactAA.getSeqFasta().toStringAA(false);
		String thisnr =  getThisAAnr().toString();
		String thisaa = getThisAAnr().toStringAA(false);
		
		String result =  refSeqIntactAA.getRefID() + "\t" + refSiteSnpIndelParent.getRefSnpIndelStart() + "\t" + referenceSeq + "\t" + refSiteSnpIndelParent.getReadsNumRef() + "\t" + thisSeq + "\t" + 
		getReadsNum() + "\t" + getQuality() + "\t" + getVcfInfoFilter() + "\t" + isExon()+"\t" + refSiteSnpIndelParent.getProp() +"\t"+
		refnr +"\t"+refaa + "\t" + thisnr +"\t"+thisaa;
		if (refaa.length() ==3  && thisaa.length() == 3) {
			result = result + "\t" + SeqFasta.cmpAAquality(refaa, thisaa);
		}
		else {
			result = result + "\t" + "";
		}
		result = result + "\t" + this.getOrfShift();
		result = result + "\t" + snpIndelRs.getSnpRsID();
		if (refSiteSnpIndelParent.getGffIso() != null) {
			result = result + "\t" + refSiteSnpIndelParent.getGffIso().getName();
			GeneID copedID = new GeneID(refSiteSnpIndelParent.getGffIso().getName(), refSiteSnpIndelParent.getTaxID(), false);
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
		return (refSiteSnpIndelParent.getRefID() + SepSign.SEP_ID+ refSiteSnpIndelParent.getRefSnpIndelStart() 
				+ SepSign.SEP_ID + referenceSeq + SepSign.SEP_ID + thisSeq).toLowerCase();
	}
	/**
	 * 将另一个siteSnpIndelInfo中Snp的数量加到本类中来，相当于合并样本信息。
	 * 但是如果本SnpInfo已经有了某个样本信息，则跳过输入项目的样本信息
	 * @param siteSnpIndelInfo
	 */
	public void addSiteSnpIndelInfo(SiteSnpIndelInfo siteSnpIndelInfo) {
		if (!getMismatchInfo().equals(siteSnpIndelInfo.getMismatchInfo())) {
			return;
		}
		Map<String, SampleSnpReadsQuality> mapSample2SnpInfo = siteSnpIndelInfo.mapSample2thisBaseNum;
		for (Entry<String, SampleSnpReadsQuality> entry : mapSample2SnpInfo.entrySet()) {
			if (mapSample2thisBaseNum.containsKey(entry.getKey())) {
				continue;
			}
			mapSample2thisBaseNum.put(entry.getKey(), entry.getValue());
		}
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
	public void clean() {
		refSeqIntactAA.clear();
		mapSample2thisBaseNum.clear();
	}
	public static enum SnpIndelType {
		INSERT, DELETION, MISMATCH, CORRECT
	}

}
/**
 * 貌似与SiteSnpIndelInfoSnp一模一样
 * @author zong0jie
 *
 */
class SiteSnpIndelInfoInsert extends SiteSnpIndelInfo{
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfoInsert.class);
	
	public SiteSnpIndelInfoInsert(RefSiteSnpIndel mapInfoSnpIndel, String refBase, String thisBase) {
		super(mapInfoSnpIndel, refBase, thisBase);
		if (refBase.length() > 1) {
			logger.error("refBase 大于1，可能不是插入，请核对：" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
		}
	}
	@Override
	protected void setMapInfoRefSeqAAabs(GffChrAbs gffChrAbs) {
		GffGeneIsoInfo gffGeneIsoInfo = refSiteSnpIndelParent.getGffIso();
		codLocInfo = gffGeneIsoInfo.getCodLoc(refSiteSnpIndelParent.getRefSnpIndelStart());
		if (codLocInfo != GffGeneIsoInfo.COD_LOC_OUT) {
			setEffectSplitType(gffGeneIsoInfo, refSiteSnpIndelParent.getRefSnpIndelStart());
		}
		//mRNA层面
		//就算在外显子中，但是如果是非编码rna，或者在UTR区域中，也返回
		if (!gffGeneIsoInfo.isCodInAAregion(refSiteSnpIndelParent.getRefSnpIndelStart())) {
			isInCDS = false;
			return;
		}
		
		isInCDS = true;
		int LocStart = gffGeneIsoInfo.getLocAAbefore(refSiteSnpIndelParent.getRefSnpIndelStart());//该位点所在AA的第一个loc
		int LocEnd = gffGeneIsoInfo.getLocAAend(refSiteSnpIndelParent.getRefSnpIndelStart());
		if (LocEnd <0) {//如果不在转录本中
			if (gffGeneIsoInfo.isCis5to3()) {
				LocEnd = LocStart + 2;
			} else {
				LocEnd = LocStart - 2;
			}
		}
		SeqFasta NR = null;
		ArrayList<ExonInfo> lsTmp = gffGeneIsoInfo.getRangeIso(LocStart, LocEnd);
		if (lsTmp == null) {
			NR = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), refSiteSnpIndelParent.getRefID(), LocStart, LocEnd);
		}
		else if (lsTmp.size() == 0) {
			return;
		}
		else {
			try {
				NR = gffChrAbs.getSeqHash().getSeq(refSiteSnpIndelParent.getRefID(), lsTmp, false);
			} catch (Exception e) {
				NR = gffChrAbs.getSeqHash().getSeq(refSiteSnpIndelParent.getRefID(), lsTmp, false);
			}
		}
		refSeqIntactAA.setCis5to3(gffGeneIsoInfo.isCis5to3());
		refSeqIntactAA.setSeq(NR,false);
		snpOnReplaceLocStart = -gffGeneIsoInfo.getLocAAbeforeBias(refSiteSnpIndelParent.getRefSnpIndelStart()) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart;
	}
	private void setEffectSplitType(GffGeneIsoInfo gffGeneIsoInfo, int codLoc) {
		int cod2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(codLoc);
		int cod2UAGmRNA = gffGeneIsoInfo.getCod2UAGmRNA(codLoc);
		if (cod2ATGmRNA >= 0 && cod2ATGmRNA <= 2) {
			splitType = SplitType.ATG;
		}
		else if (cod2UAGmRNA <= 0 && cod2UAGmRNA >= -2) {
			splitType = SplitType.UAG;
		}
		else {
			int locNum = gffGeneIsoInfo.getNumCodInEle(codLoc);
			if ((locNum > 1 && gffGeneIsoInfo.getCod2ExInStart(codLoc) <= 1) ||
					(locNum < 0 && gffGeneIsoInfo.getCod2ExInEnd(codLoc) <= 1)) {
				splitType = SplitType.EXON_START;
			}
			else if ((locNum > 0 && locNum < gffGeneIsoInfo.size() && gffGeneIsoInfo.getCod2ExInEnd(codLoc) <= 1) ||
					(locNum < 0 && gffGeneIsoInfo.getCod2ExInStart(codLoc) <= 1) ) {
				splitType = SplitType.EXON_END;
			}
		}
	}
	public int getOrfShift() {
		return (3 - (thisSeq.length() - referenceSeq.length())%3) % 3;//待检查
	}
	@Override
	public SnpIndelType getSnpIndelType() {
		return SnpIndelType.INSERT;
	}
	
}

class SiteSnpIndelInfoSnp extends SiteSnpIndelInfoInsert {
	public SiteSnpIndelInfoSnp(RefSiteSnpIndel mapInfoSnpIndel, String refBase, String thisBase) {
		super(mapInfoSnpIndel, refBase, thisBase);
	}
	public int getOrfShift() {
		return 0;
	}
	@Override
	public SnpIndelType getSnpIndelType() {
		return SnpIndelType.MISMATCH;
	}
}
/**
 * 没有snp的位点，就是只返回ref了
 * @author zongjie
 *
 */
class SiteSnpIndelInfoNoSnp extends SiteSnpIndelInfoInsert {
	public SiteSnpIndelInfoNoSnp(RefSiteSnpIndel refSiteSnpIndel, String refBase, String thisBase) {
		super(refSiteSnpIndel, refBase, thisBase);
	}
	public int setOrfShift() {
		return 0;
	}
	@Override
	public SnpIndelType getSnpIndelType() {
		return SnpIndelType.CORRECT;
	}
}
/**
 * 必须很短的deletion，譬如在20bp以内的deletion
 * @author zong0jie
 *
 */
class SiteSnpIndelInfoDeletion extends SiteSnpIndelInfo {
	Logger logger = Logger.getLogger(SiteSnpIndelInfoInsert.class);
	int orfShift = 0;
	public SiteSnpIndelInfoDeletion(RefSiteSnpIndel refSiteSnpIndel, String refBase, String thisBase) {
		super(refSiteSnpIndel, refBase, thisBase);
		if (refBase.length() <= 1 || thisBase.length() > 1) {
			logger.error("本位点可能不是缺失，请核对：" + refSiteSnpIndel.getRefID() + "\t" + refSiteSnpIndel.getRefSnpIndelStart());
		}
	}

	@Override
	protected void setMapInfoRefSeqAAabs(GffChrAbs gffChrAbs) {
		int refStart = refSiteSnpIndelParent.getRefSnpIndelStart();
		
		int refEnd = refStart + referenceSeq.length() - 1;
		int refStartCis = refStart; int refEndCis = refEnd;

		GffGeneIsoInfo gffGeneIsoInfo = refSiteSnpIndelParent.getGffIso();
		if (!gffGeneIsoInfo.isCis5to3()) {
			refStartCis = refEnd; refEndCis = refStart;
		}
		codLocInfo = setLocationInfo(gffGeneIsoInfo, refStartCis, refEndCis);
		
		if (codLocInfo != GffGeneIsoInfo.COD_LOC_EXON) {
			return;
		}
		
		int[] bound = getLocOutOfExonToNearistExonBounder(gffGeneIsoInfo, refStartCis, refEndCis);
		refStartCis = bound[0]; refEndCis = bound[1];
		
		splitType = setEffectSplitType(gffGeneIsoInfo, refStartCis, refEndCis);
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
			refSeqIntactAA.setStartEndLoc(LocStart, LocEnd);
			ArrayList<ExonInfo> lsTmp = gffGeneIsoInfo.getRangeIso(LocStart, LocEnd);
			if (lsTmp == null) {
				logger.error("检查一下：" + refSiteSnpIndelParent.getRefID() + "\t" + refSiteSnpIndelParent.getRefSnpIndelStart());
				return;
			}
			setOrfShiftAndReplaceSite(gffGeneIsoInfo, refStartCis, refEndCis);
			SeqFasta NR = gffChrAbs.getSeqHash().getSeq(refSeqIntactAA.getRefID(), lsTmp, false);
			refSeqIntactAA.setSeq(NR,false);//因为上面已经反向过了
		}
		
		else if (!isInCDS && gffGeneIsoInfo.getNumCodInEle(refStartCis) != gffGeneIsoInfo.getNumCodInEle(refEndCis)) {
			logger.error("缺失外显子："  + refSiteSnpIndelParent.getRefID() + "\t" + refSiteSnpIndelParent.getRefSnpIndelStart());
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
	private static int[] getLocOutOfExonToNearistExonBounder(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		int[] bounder = new int[]{refStartCis, refEndCis};
		//将起点和终点转换到距离最近的exon上去
		if (gffGeneIsoInfo.getCodLoc(refStartCis) != GffGeneIsoInfo.COD_LOC_EXON) {
			int startExonNum = Math.abs(gffGeneIsoInfo.getNumCodInEle(refStartCis));
			//TODO check
			if (startExonNum == -1) {
				startExonNum = 0;
			}
			bounder[0] = gffGeneIsoInfo.get(startExonNum).getStartCis();
		}
		else if (gffGeneIsoInfo.getCodLoc(refEndCis) != GffGeneIsoInfo.COD_LOC_EXON) {
			int endExonNum = Math.abs(gffGeneIsoInfo.getNumCodInEle(refEndCis)) - 1;
			if (endExonNum == -1) {
				endExonNum = gffGeneIsoInfo.size() - 1;
			}
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
	private static SplitType setEffectSplitType(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		SplitType splitType = SplitType.NONE;
		int codStart2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(refStartCis);
		int codEnd2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(refEndCis);
		int codStart2UAGmRNA = gffGeneIsoInfo.getCod2UAGmRNA(refStartCis);
		int codEnd2UAGmRNA = gffGeneIsoInfo.getCod2UAGmRNA(refEndCis);
		
		if (codStart2ATGmRNA <=0 && codEnd2ATGmRNA >= 0 //横跨atg
			|| 	codStart2ATGmRNA >= 0 && codStart2ATGmRNA <= 2
		  ) {
			splitType = SplitType.ATG;
		}
		else if (codStart2UAGmRNA <= 0 && codEnd2UAGmRNA >= 0
				|| codEnd2ATGmRNA <= 0 && codEnd2ATGmRNA >= -2
		) {
			splitType = SplitType.UAG;
		}
		else if (gffGeneIsoInfo.getNumCodInEle(refStartCis) != gffGeneIsoInfo.getNumCodInEle(refEndCis)) {
			splitType = SplitType.EXON_START_END;
		}
		else {
			int locNum = gffGeneIsoInfo.getNumCodInEle(refStartCis);
			if (locNum != 1 && gffGeneIsoInfo.getCod2ExInStart(refStartCis) <= 1) {
				splitType = SplitType.EXON_START;
			}
			if (locNum != gffGeneIsoInfo.size() && gffGeneIsoInfo.getCod2ExInEnd(refEndCis) <= 1) {
				if (splitType == SplitType.EXON_END) {
					splitType = SplitType.EXON_START_END;
				}
				else {
					splitType = SplitType.EXON_END;
				}
			}
		}
		return splitType;
	}
	/**
	 * 设定该deletion处在哪个位置 ，或者说是否覆盖了exon
	 * @param gffGeneIsoInfo
	 * @param startCis
	 * @param endCis
	 */
	private static int setLocationInfo(GffGeneIsoInfo gffGeneIsoInfo, int startCis, int endCis) {
		int codLocInfo = GffGeneIsoInfo.COD_LOC_OUT;
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
		return codLocInfo;
	}
	/** 需要测试
	 * 这段代码的前提假设是ref很长，然后thisSeq一定为1
	 * 这个形式是deletion的一般形式。
	 * 那么这就排除了某段序列被完全不同的序列替换的情况
	 *  譬如  ref为 ATCG TC GT    this为 ATCG AACTG GT 这种情况会被拆分成两个错配和一个插入
	 *  那么这段代码就处理不了
	 *  */
	protected void setOrfShiftAndReplaceSite(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		int deletionLen = gffGeneIsoInfo.getLocDistmRNA(refStartCis, refEndCis) + 1 - thisSeq.length();
		orfShift = deletionLen%3;
		//TODO 这里有问题，如果一个deletion横跨了一整个intron，那么就会有错误
		snpOnReplaceLocStart = -gffGeneIsoInfo.getLocAAbeforeBias(refStartCis) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart + deletionLen;
	}

	@Override
	public SnpIndelType getSnpIndelType() {
		return SnpIndelType.DELETION;
	}
	
	public int getOrfShift() {
		return orfShift;
	}
}

class SampleSnpReadsQuality {
	/** 该snp的质量 */
	String quality = "";
	/** 是否符合标准 */
	String vcfFilterInfo = "";
	int thisReadsNum;
	public SampleSnpReadsQuality() {}
	public SampleSnpReadsQuality(int thisReadsNum) {
		this.thisReadsNum = thisReadsNum;
	}
	/**
	 * 添加snp位点的数量
	 * @param addNum
	 */
	public void addThisReadsNum(int addNum) {
		thisReadsNum = thisReadsNum + addNum;
	}
}

enum SplitType {
	ATG("atg"), UAG("uag"), EXON_START("exon start"), EXON_END("exon end"),
	/** deletion 跨过一个intron，这就影响了一个start和一个end */
	EXON_START_END("exon start and end"), NONE("none");
	String name;
	SplitType(String name) {
		this.name =name;
	}
	public String toString() {
		return name;
	}
}
