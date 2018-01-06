package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.EnumMrnaLocate;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;


/**
 * 对于单个位点的snp与indel的情况，可以保存多个不同的样本。
 * 在setSampleName()方法中可设定样本名，并获得该样本的信息。
 * @author zong0jie
 */
public abstract class SnpRefAltInfo {
	private static final Logger logger = Logger.getLogger(SnpRefAltInfo.class);

	
	/** ref的坐标区间 */
	Align alignRef;
	/** reference的序列 */
	String seqRef;
	
	/** 改变之后的序列 */
	String seqAlt;

	
	
	/** 影响到了哪几个转录本 */
	List<GffGeneIsoInfo> lsIsos = new ArrayList<>();
	
	public SnpRefAltInfo(String refId, int position, String seqRef, String seqAlt) {
		int positionEnd = position + seqRef.length() - 1;
		alignRef = new Align(refId, position, positionEnd);
		this.seqRef = seqRef;
		this.seqAlt = seqAlt;
	}
	
	/** 根据parent，设定GffChrAbs */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		GffCodGeneDU gffCodGeneDu = gffChrAbs.getGffHashGene().searchLocation(alignRef.getRefID(), alignRef.getStartAbs(), alignRef.getEndAbs());
		if (gffCodGeneDu == null) {
			return;
		}
		Set<GffDetailGene> setGenes = gffCodGeneDu.getCoveredOverlapGffGene();
		setMapInfoRefSeqAAabs(gffChrAbs.getSeqHash());
	}
	/** 如果Iso不存在，该方法不会被调用。
	 * 如果Iso存在，并且snp位点在exon上，那么就设置ref序列的氨基酸的信息 */
	protected abstract void setMapInfoRefSeqAAabs(SeqHash seqHash);
	
	public String getRefId() {
		return alignRef.getRefID();
	}
	public int getStartPosition() {
		return alignRef.getStartAbs();
	}
	public int getEndPosition() {
		return alignRef.getEndAbs();
	}
	public String getSeqAlt() {
		return seqAlt;
	}
	public String getSeqRef() {
		return seqRef;
	}
	public Align getAlignRef() {
		return alignRef;
	}
	private EnumVariantType getVariantType() {
		if (seqRef.length() ==seqAlt.length()) {
			if (seqRef.length() == 1) {
				return EnumVariantType.SNP;
			} else if (seqRef.length() == 2) {
				return EnumVariantType.DNP;
			} else if (seqRef.length() == 3) {
				return EnumVariantType.TNP;
			} else if (seqRef.length() > 3) {
				return EnumVariantType.ONP;
			}
		} else if (seqRef.length() == 1 && seqAlt.length() > 1) {
			return EnumVariantType.INS;
		} else if (seqRef.length() > 1 && seqAlt.length() == 1) {
			return EnumVariantType.DEL;
		} else {
			throw new exceptionnbcvar();
		}
	}
	
	public String getSplitTypeEffected() {
		if (splitType == SplitType.NONE) {
			return "";
		}
		String splitString = splitType.toString();
		
		if (splitDistance >= 0) {
			splitString = splitString +  " Distance_To_Splice_Site_Is:_" + splitDistance + "_bp";
		} else if (splitType == SplitType.ATG) {
			splitString = splitString +  " Cover_ATG_Site";
		} else if (splitType == SplitType.UAG) {
			splitString = splitString +  " Cover_UAG_Site";
		} else {
			splitString = splitString +  " Cover_Splice_Site";
		}
		return splitString;
	}
	
	public abstract SnpIndelType getSnpIndelType();
	
	public abstract String getAffectCdsInfo();
	public abstract String getAffectAAInfo();

	/**
	 * 返回变化的AA的化学性质改变形式，不在cds中则返回""；
	 * @return
	 */
	public String getAAattrConvert() {
		if (isCDS() && seqRef.length() == 1 && seqAlt.length() == 1) {
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
			seq = SeqFasta.reverseComplement(seq);
		}
		if (refSiteSnpIndelParent.getGffIso() == null)
			return new SeqFasta();
	
		return replaceSnpIndel(seq, snpOnReplaceLocStart, snpOnReplaceLocEnd);
	}
	
	public SeqFasta getRefAAnr() {
		return refSeqIntactAA.getSeqFasta();
	}
	public String getAAchamicalConvert() {
		if (this instanceof SiteSnpIndelInfoSnp) {
			String refaa =  refSeqIntactAA.getSeqFasta().toStringAA(false);
			String thisaa = getThisAAnr().toStringAA(false);
			return SeqFasta.cmpAAquality(refaa, thisaa);
		}
		return "";
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

	public boolean isExon() {
		if (enumMrnaLocate == EnumMrnaLocate.intron || enumMrnaLocate == enumMrnaLocate.intergenic) {
			return false;
		}
		return true;
	}
	public boolean isCDS() {
		return enumMrnaLocate == EnumMrnaLocate.cds;
	}
	
	public EnumMrnaLocate getEnumMrnaLocate() {
		return enumMrnaLocate;
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
		SnpRefAltInfo otherObj = (SnpRefAltInfo)obj;
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
		return (alignRef.getRefID() + SepSign.SEP_ID+ alignRef.getStartAbs() 
				+ SepSign.SEP_ID + seqRef + SepSign.SEP_ID + seqAlt).toLowerCase();
	}
	

	/**
	 * 返回该类涉及到的展示信息为linkedlist<br>
	 * lsTitle.add("RefNr");<br>
		lsTitle.add("RefAA");<br>
		lsTitle.add("ThisNr");<br>
		lsTitle.add("ThisAA");<br>
		lsTitle.add("ConvertType");<br>
		lsTitle.add("ChamicalConvert");<br>
	 * @return
	 */
	public List<String> toStrings() {
		List<String> lsInfo = new LinkedList<String>();
		if (refSiteSnpIndelParent.getGffIso() == null) {
			for (int i = 0; i < 6; i++) {
				lsInfo.add("");
			}
			return lsInfo;
		}
		
		lsInfo.add(getRefAAnr().toString());
		lsInfo.add(getRefAAnr().toStringAA3());
		lsInfo.add(getThisAAnr().toString());
		lsInfo.add(getThisAAnr().toStringAA3());
		String aaChange = "";
		if (this instanceof SiteSnpIndelInfoSnp && this.refSiteSnpIndelParent.getAffectAANum() > 0) {
			aaChange = getRefAAnr().toStringAA3() + this.refSiteSnpIndelParent.getAffectAANum() + getThisAAnr().toStringAA3();
		}
		if (isCDS() && getOrfShift() > 0) {
			aaChange = "orf_shift-" +getOrfShift(); 
		}
		lsInfo.add(aaChange);
		lsInfo.add(getSplitTypeEffected());
		lsInfo.add(getAAchamicalConvert());
		return lsInfo;
	}
	
	/**
	 * 获得标题，为linkedlist<br>
	 * lsTitle.add("RefNr");<br>
		lsTitle.add("RefAA");<br>
		lsTitle.add("ThisNr");<br>
		lsTitle.add("ThisAA");<br>
		lsTitle.add("ConvertType");<br>
		lsTitle.add("ChamicalConvert");<br>
	 * @return
	 */
	public static List<String> getTitle() {
		List<String> lsTitle = new LinkedList<String>();
		lsTitle.add("RefNr");
		lsTitle.add("RefAA");
		lsTitle.add("ThisNr");
		lsTitle.add("ThisAA");
		lsTitle.add("ConvertType");
		lsTitle.add("SplitType");
		lsTitle.add("ChamicalConvert");
		
		return lsTitle;

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


	public static enum SnpIndelType {
		INSERT, DELETION, MISMATCH, CORRECT
	}
}

enum SplitType {
	ATG("Near_ATG"), UAG("Near_UAG"), EXON_START("Near_Exon_Start"), EXON_END("Near_Exon_End"),
	/** deletion 跨过一个intron，这就影响了一个start和一个end */
	Cover_Exon_Intron("Cover_Exon_Intron"), 
	Exon_From_Start_To_End("Exon_Start_To_End"), 
	
	NONE("None");
	String name;
	SplitType(String name) {
		this.name =name;
	}
	public String toString() {
		return name;
	}
}
