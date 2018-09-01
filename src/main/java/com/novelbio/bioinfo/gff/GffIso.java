package com.novelbio.bioinfo.gff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Alignment.CompM2S;
import com.novelbio.bioinfo.base.Alignment.CompS2M;
import com.novelbio.bioinfo.base.binarysearch.BinarySearch;
import com.novelbio.bioinfo.base.binarysearch.BsearchSite;
import com.novelbio.bioinfo.base.binarysearch.BsearchSiteDu;
import com.novelbio.bioinfo.base.binarysearch.ListEle;
import com.novelbio.bioinfo.gff.GffGene.GeneStructure;
import com.novelbio.database.domain.modgeneid.GeneType;

/**
 * 	重写hash，不包含基因名信息，包含基因taxID，chrID，atg，uag，tss，长度，以及每一个exon的信息<br>
 * 记录该转录本的具体坐标信息,
 * 第一项开始是exon的信息，exon成对出现，为int[2] 
 * 0: 该外显子起点，闭区间，从1开始记数<br>
 * 1: 该外显子终点，闭区间，从1开始记数<br>
 * 按照基因的方向进行排列
 * 如果正向则从小到大排列，且int0&lt;int1
 * 如果反向则从大到小排列，且int0&gt;int1
 * @return
 */
@Document(collection="gffiso")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "fileId_name", def = "{'gffFileId': 1, 'listName': 1}")
})
public abstract class GffIso extends ListEle<ExonInfo> {
	private static final Logger logger = LoggerFactory.getLogger(GffIso.class);
	private static final long serialVersionUID = -6015332335255457620L;
	/** 标记codInExon处在外显子中 */
	public static final int COD_LOC_EXON = 100;
	/** 标记codInExon处在内含子中 */
	public static final int COD_LOC_INTRON = 200;
	/** 标记codInExon不在转录本中  */
	public static final int COD_LOC_OUT = 300;
	
	/**  标记codInExon不在UTR或CDS中，譬如该基因是none coding rna，那么就没有UTR和CDS */
	public static final int COD_LOCUTR_NONE = 1000;
	/**  标记codInExon处在5UTR中  */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**  标记codInExon处在3UTR中 */
	public static final int COD_LOCUTR_3UTR = 3000;
	/** 标记codInExon不在UTR中 */
	public static final int COD_LOCUTR_OUT = 0;
	/** 标记codInExon在CDS中 */
	public static final int COD_LOCUTR_CDS = 7000;
	
	/** 哺乳动物基因间为Tss上游5000bp */
	public static int PROMOTER_INTERGENIC_MAMMUM = 5000;
	/**  哺乳动物为Distal Promoter Tss上游1000bp，以内的就为Proximal Promoter */
	public static int PROMOTER_DISTAL_MAMMUM = 1000;
	/** InterGenic_ */
	public static final String PROMOTER_INTERGENIC_STR = "InterGenic";
	/**  Distal Promoter_ */
	public static final String PROMOTER_DISTAL_STR = "Distal_Promoter";
	/**  Proximal Promoter_ */
	public static final String PROMOTER_PROXIMAL_STR = "Proximal_Promoter";
	/**  Proximal Promoter_  */
	public static final String PROMOTER_DOWNSTREAMTSS_STR = "Promoter DownStream Of Tss";

	/** 所有坐标的起始信息  */
	public static final int LOC_ORIGINAL = -1000000000;
	
	@Id
	String id;
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public GeneType flagTypeGene = GeneType.ncRNA;
	
	/** 该转录本的ATG的第一个字符坐标，从1开始计数  */
	protected int ATGsite = LOC_ORIGINAL;
	/** 该转录本的UAG的最后一个字符坐标，从1开始计数 */
	protected int UAGsite = LOC_ORIGINAL;
	/** 该转录本的长度 */
	protected int lengthIso = LOC_ORIGINAL;
	
	@Transient
	GffGene gffGene;
	@Indexed(unique = false)
	String gffFileId;
	/**
	 * 该名字为实际上的iso所在的基因名字，不一定为其 gffDetailGeneParent 的gene name
	 * 因为可能会有多个gffDetailGene合并为一个gffDetailGene，这时候直接用gffDetailGeneParent的名字就无法进行区分
	 */
	String geneName;
	
	/**
	 * 是否为错乱的exon
	 * 目前只看到叶绿体的基因是错乱的exon
	 */
	@Transient
	private boolean isUnorderedExon = false;	
	
	/** 给mongodb使用 */
	public GffIso() {	}
	
	public GffIso(String isoName, String geneName, GeneType geneType) {
		this.name = isoName;
		this.flagTypeGene = geneType;
		this.geneName = geneName;
	}
	
	public GffIso(String isoName, String geneName, GffGene gffGene, GeneType geneType) {
		this.name = isoName;
		this.flagTypeGene = geneType;
		this.gffGene = gffGene;
		this.geneName = geneName;
	}
	public void setParentGeneName(String geneName) {
		this.geneName = geneName;
	}
	/** 仅供数据库使用 */
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	/** 仅用于数据库 */
	public String getGffFileId() {
		return gffFileId;
	}
	/** 仅用于数据库 */
	public void setGffFileId(String gffFileId) {
		this.gffFileId = gffFileId;
	}

	/**
	 * 返回该基因的类型
	 * @return
	 */
	public GeneType getGeneType() {
		if (ismRNA()) {
			return GeneType.mRNA;
		}
		if (flagTypeGene == null || flagTypeGene == GeneType.mRNA) {
			return GeneType.ncRNA;
		}
		return flagTypeGene;
	}
	
	public void setGeneType(GeneType flagTypeGene) {
		this.flagTypeGene = flagTypeGene;
	}
		
	public int getTaxID() {
		if (gffGene == null) {
			return 0;
		}
		return gffGene.getTaxID();
	}
	public void setGffDetailGeneParent(GffGene gffGene) {
		if (gffGene == null) {
			return;
		}
		this.gffGene = gffGene;
	}
	public GffGene getParentGffDetailGene() {
		return gffGene;
	}
	
	/** 返回同一组的GffDetailGene，效率略低 */
	public GffGene getParentGffGeneSame() {
		GffGene gffGeneResult = gffGene.clone();
		gffGeneResult.setStartAbs(-100);
		gffGeneResult.setEndAbs(-100);
		gffGeneResult.lsGffGeneIsoInfos.clear();
		for (GffIso iso : gffGene.lsGffGeneIsoInfos) {
			if (iso.getParentGeneName().equals(getParentGeneName())) {
				gffGeneResult.addIsoSimple(iso);
			}
		}		
		return gffGeneResult;
	}
	
	/**
	 * 该名字为实际上的iso所在的基因名字，不一定为其 gffDetailGeneParent 的gene name<br>
	 * 因为可能会有多个gffDetailGene合并为一个gffDetailGene，这时候直接用gffDetailGeneParent的名字就无法进行区分
	 */
	public String getParentGeneName() {
		if (!StringOperate.isRealNull(geneName)) {
			return geneName;
		}
		return gffGene.getNameSingle();
	}
	/**
	 * coord是否在promoter区域的范围内，从Tss上游UpStreamTSSbp到Tss下游DownStreamTssbp
	 * @return
	 */
	public boolean isCodInIsoTss(int[] tss, int coord) {
		if (tss == null) {
			return false;
		}
		int cod2tss = getCod2Tss(coord);
		if (cod2tss >= tss[0] && cod2tss <= tss[1]) {
			return true;
		}
		return false;
	}

	/**
	 * coord是否在geneEnd区域的范围内
	 * @return
	 */
	public boolean isCodInIsoGenEnd(int[] geneEnd, int coord) {
		if (geneEnd == null) {
			return false;
		}
		int cod2tes = getCod2Tes(coord);
		if (cod2tes >= geneEnd[0] && cod2tes <= geneEnd[1]) {
			return true;
		}
		return false;
	}
	/**
	 * coord是否在该转录本包括promoter和geneEnd延长区域的范围内
	 * @return
	 */
	public boolean isCodInIsoExtend(int[] tss, int[] geneEnd, int coord) {
		int codLoc = getCodLoc(coord);
		return (codLoc != COD_LOC_OUT) || isCodInIsoTss(tss, coord) || isCodInIsoGenEnd(geneEnd, coord);
	}
	/**
	 * cod是否在编码区
	 * 如果本转录本是非编码RNA，直接返回false；
	 * @return
	 */
	public boolean isCodInAAregion(int coord) {
		if (!ismRNA() || getCodLoc(coord) != GffIso.COD_LOC_EXON) {
			return false;
		}
		if (getCod2ATG(coord) < 0 || getCod2UAG(coord) > 0) {
			return false;
		}
		return true;
	}
	public String getRefIDlowcase() {
		if (gffGene == null) {
			return "";
		}
		return gffGene.getRefID().toLowerCase();
	}
	public String getRefID() {
		if (gffGene == null) {
			return "";
		}
		return gffGene.getRefID();
	}
	/**
	 * 是否是mRNA有atg和uag，
	 * @return
	 */
	public boolean ismRNA() {
		if (ATGsite < 0 || UAGsite < 0) {
			return false;
		}
		return Math.abs(ATGsite - UAGsite) > 10 || flagTypeGene == GeneType.mRNA;
	}
	/**
	 * 根据atg和uag的位置来判断是否为mRNA
	 * @return
	 */
	public boolean ismRNAFromCds() {
		if (ATGsite < 0 || UAGsite < 0) {
			return false;
		}
		return Math.abs(ATGsite - UAGsite) > 10;

	}
	/**
	 * 根据输入的gffIso延长两端
	 * @param gffGeneIsoInfo
	 */
	public void extendUtr(GffIso gffGeneIsoInfo) {
		extend5Utr(gffGeneIsoInfo);
		extend3Utr(gffGeneIsoInfo);
	}
	
	
	private void extend5Utr(GffIso gffGeneIsoInfo) {
		BsearchSite<ExonInfo> codStart = gffGeneIsoInfo.searchLocation(getStart());
		if (!codStart.isInsideLoc()) {
			return;
		}
		int itemNum = codStart.getIndexAlignThis();
		List<ExonInfo> lsExonInfos = new ArrayList<>();
		for (int i = 0; i <= itemNum; i++) {
			lsExonInfos.add(gffGeneIsoInfo.get(i));
		}
		ExonInfo exonInfo = lsExonInfos.get(lsExonInfos.size() - 1);
		getLsElement().get(0).setStartCis(exonInfo.getStartCis());
		lsExonInfos = lsExonInfos.subList(0, lsExonInfos.size() - 1);
		getLsElement().addAll(0, lsExonInfos);
	}
	
	private void extend3Utr(GffIso gffGeneIsoInfo) {
		BsearchSite<ExonInfo> codEnd = gffGeneIsoInfo.searchLocation(getEnd());

		if (!codEnd.isInsideLoc()) {
			return;
		}
		
		int itemNum = codEnd.getIndexAlignThis();
		List<ExonInfo> lsExonInfos = new ArrayList<>();
		for (int i = itemNum; i < gffGeneIsoInfo.size(); i++) {
			lsExonInfos.add(gffGeneIsoInfo.get(i));
		}
		ExonInfo exonInfo = lsExonInfos.get(0);
		getLsElement().get(getLsElement().size() - 1).setEndCis(exonInfo.getEndCis());
		lsExonInfos = lsExonInfos.subList(1, lsExonInfos.size());
		getLsElement().addAll(lsExonInfos);
	}
	
	/**
	 * 给该转录本添加ATG和UAG坐标，<br>
	 * 加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 * 会自动判定输入的起点是否小于已有的atg，终点是否大于已有的uag
	 * 是的话，才会设定，否则就不设定
	 * @param atg 从1开始记数
	 * @param uag 从1开始记数
	 */
	public void setATGUAGauto(int atg, int uag) {
		if (!ismRNA()) {
			ATGsite = -1;
			UAGsite = -1;
		}
		if (Math.abs(atg - uag)<=1) {
			atg = Math.min(atg, uag);
			uag = Math.min(atg, uag);
		}
//		flagTypeGene = GeneType.mRNA;
		if (isCis5to3()) {
			setATG(Math.min(atg, uag));
			setUAG(Math.max(atg, uag));
		} else {
			setATG(Math.max(atg, uag));
			setUAG(Math.min(atg, uag));
		}
	}
	/** 如果是GTF文件指定了atg位点，就用这个设定，是ATG的第一个位点 */
	public void setATG(int atg) {
		if (ATGsite <= 0) {
			ATGsite = atg;
		} else {
			if (isCis5to3() && atg < ATGsite) {
				ATGsite = atg;
			} else if (!isCis5to3() && atg > ATGsite) {
				ATGsite = atg;
			}
		}
	}
	/** 如果是GTF文件指定了uag位点，就用这个设定，是UAG的最后一个位点 */
	public void setUAG(int uag) {
		if (UAGsite <= 0) {
			UAGsite = uag;
		} else {
			if (isCis5to3() && uag > UAGsite) {
				UAGsite = uag;
			} else if (!isCis5to3() && uag < UAGsite) {
				UAGsite = uag;
			}
		}
	}
	
	/**
	 * <b>必须先设定exon</b>
	 * 如果ATGsite < 0 && UAGsite < 0，则认为是非编吗RNA
	 * 则将atg和uag设置为最后一位
	 */
	public void setATGUAGncRNA() {
		if (ATGsite < 0 && UAGsite <0) {
			ATGsite = get(size() - 1).getEndCis();
			UAGsite = get(size() - 1).getEndCis();
		}
	}
	@VisibleForTesting
	protected List<ExonInfo> getATGLoc() {
		List<ExonInfo> lsAtgInfo = new ArrayList<>();
		if (ATGsite > 0) {
			int atgEnd = getLocAAend(ATGsite);
			lsAtgInfo = getSubExon(ATGsite, atgEnd);
		}
		return lsAtgInfo;
	}
	@VisibleForTesting
	protected List<ExonInfo> getUAGLoc() {
		List<ExonInfo> lsUagInfo = new ArrayList<>();
		if (UAGsite > 0) {
			int uagStart = getLocAAbefore(UAGsite);
			lsUagInfo = getSubExon(uagStart, UAGsite);
		}
		return lsUagInfo;
	}
	/**
	 * 该转录本的ATG的第一个字符坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getATGsite() {
		return ATGsite;
	}
	/**
	 * 该转录本的UAG最后一个字符G的坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getUAGsite() {
		return UAGsite;
	}
	/**
	 * 考虑正反向
	 * 该转录本的TSS的第一个字符坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getTSSsite() {
		return getStart();
	}
	/**
	 * 该转录本的Coding region end的最后一个字符坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getTESsite() {
		return getEnd();
	}
	public int getExonNum() {
		return size();
	}
	
	/**
	 * 获得5UTR的长度
	 * @return
	 */
	public int getLenUTR5() {
		if (!ismRNA()) return 0;
		return Math.abs(getLocDistmRNA(getTSSsite(), this.ATGsite) );
	}
	
	/**
	 * 获得3UTR的长度
	 * @return
	 */
	public int getLenUTR3() {
		if (!ismRNA()) return 0;
		return Math.abs(getLocDistmRNA(this.UAGsite, getTESsite() ) );
	}
	
	/**
	 * 获取全长exon长度
	 * @return
	 */
	public int getLenExon() {
		return getLenExon(0);
	}
	 /**
     * @param num 指定第几个，如果超出或小于0，则返回-1000000000, 
     * num 为实际个数。如果num=0则返回全部Exon的长度。
     * @return 
     */
	public int getLenExon(int num) {
		if (num < 0 || num > size()) {
			return -1000000000;
		}
		else if (num == 0) {
			int allExonLength = 0;
			 // 0-0 0-1 1-0 1-1
			// 2-1 2-0 1-1 1-0 0-1 0-tss cood
			for (int i = 0; i < size(); i++) { 			
				allExonLength += get(i).getLength();
			}
			return allExonLength;
		} else {
			num--;
			return get(num).getLength();
		}
	}
	 /**
     * @param num 指定第几个，如果超出，则返回-1000000000, 
     * num 为实际个数。如果num=0则返回全部Intron的长度。
     * @return 
     */
	public int getLenIntron(int num) {
		if (num < 0 || num > size()) {
			return -1000000000;
		}
		else if (num == 0) {
			int allIntronLength = 0;
			// 0-0 0-1 1-0 1-1
			// 2-1 2-0 1-1 1-0 0-1 0-tss cood
			for (int i = 1; i < size(); i++) { 
				allIntronLength += Math.abs(get(i).getStartCis() - get(i-1).getEndCis()) - 1;
			}
			return allIntronLength;
		}
		num--;
		return Math.abs(get(num + 1).getStartCis() - get(num).getEndAbs()) - 1;
	}
	/**
	 * 在转录本的哪个位置
	 * 有COD_LOC_EXON，COD_LOC_INTRON，COD_LOC_OUT三种
	 * @return
	 */
	public int getCodLoc(int coord) {
		return getCodLocInfo(coord)[0];
	}
	
	/**
	 * 返回该位点在基因的哪些结构内
	 * @param coord
	 * @param tssRange 如果不指定，就不返回tss的区域
	 * @param tesRange 如果不指定，就不返回tes的区域
	 * @return
	 */
	public Set<GeneStructure> getLsCoordOnGeneStructure(int coord, int[] tssRange, int[] tesRange) {
		Set<GeneStructure> lsGeneStructures = new HashSet<>();
		if (isCodInIsoTss(tssRange, coord)) {
			lsGeneStructures.add(GeneStructure.TSS);
		}
		if (isCodInIsoGenEnd(tesRange, coord)) {
			lsGeneStructures.add(GeneStructure.TES);
		}
		int ExIntronnum = getNumCodInEle(coord);
		if (ExIntronnum > 0) {
			lsGeneStructures.add(GeneStructure.EXON);
			
			if (ismRNA()) {
				if ((coord < ATGsite && isCis5to3()) || (coord > ATGsite && !isCis5to3())) {
					lsGeneStructures.add(GeneStructure.UTR5);
				} else if((coord > UAGsite && isCis5to3()) || (coord < UAGsite && !isCis5to3())){       //大于cds起始区，在3‘UTR中
					lsGeneStructures.add(GeneStructure.UTR3);
				} else {
					lsGeneStructures.add(GeneStructure.CDS);
					if (isCis5to3() && coord >= ATGsite && coord <= ATGsite + 2 || !isCis5to3() && coord <= ATGsite && coord >= ATGsite - 2) {
						lsGeneStructures.add(GeneStructure.ATG);
					} else if (isCis5to3() && coord <= UAGsite && coord >= UAGsite - 2 || !isCis5to3() && coord >= UAGsite && coord <= UAGsite + 2) {
						lsGeneStructures.add(GeneStructure.UAG);
					}
				}
			}
		} else {
			lsGeneStructures.add(GeneStructure.INTRON);
		}
		return lsGeneStructures;
	}
	
	/**
	 * 在转录本的哪个位置
	 * 有COD_LOCUTR_5UTR，COD_LOCUTR_3UTR，COD_LOCUTR_CDS等
	 * @return
	 */
	public int getCodLocUTRCDS(int coord) {
		return getCodLocInfo(coord)[1];
	}
	/**
	 * 在转录本的哪个位置
	 * 0: 有COD_LOC_EXON，COD_LOC_INTRON，COD_LOC_OUT三种
	 * 1: 有COD_LOCUTR_5UTR，COD_LOCUTR_3UTR，COD_LOCUTR_NONE,COD_LOCUTR_CDS四种
	 * @return
	 */
	private int[] getCodLocInfo(int coord) {
		int codLoc[] = new int[2];
		int ExIntronnum = getNumCodInEle(coord);
		if (ExIntronnum == 0) {
			codLoc[0] = COD_LOC_OUT;
			codLoc[1] = COD_LOCUTR_NONE;
		} 
		else if (ExIntronnum > 0) {
			codLoc[0] = COD_LOC_EXON;
			if (!ismRNA()) {
				codLoc[1] = COD_LOCUTR_NONE;
				return codLoc;
			}
			//只有当该iso为mRNA时才进行判定
			if((coord < ATGsite && isCis5to3()) || (coord > ATGsite && !isCis5to3())){        //坐标小于atg，在5‘UTR中,也是在外显子中
				codLoc[1] = COD_LOCUTR_5UTR;
			} else if((coord > UAGsite && isCis5to3()) || (coord < UAGsite && !isCis5to3())){       //大于cds起始区，在3‘UTR中
				codLoc[1] = COD_LOCUTR_3UTR; 
			} else {
				codLoc[1] = COD_LOCUTR_CDS; 
			}
		} 
		else {
			codLoc[0] = COD_LOC_INTRON;
		}
		return codLoc;
	}

	/**
	 * 在转录本的哪个位置
	 * @return
	 */
	public EnumMrnaLocate getCodLocate(int coord) {
		EnumMrnaLocate enumMrnaLocate = null;
		int ExIntronnum = getNumCodInEle(coord);
		if (ExIntronnum == 0) {
			enumMrnaLocate = EnumMrnaLocate.intergenic;
		} 
		else if (ExIntronnum > 0) {
			enumMrnaLocate = EnumMrnaLocate.exon;
			if (!ismRNA()) {
				return enumMrnaLocate;
			}
			//只有当该iso为mRNA时才进行判定
			if((coord < ATGsite && isCis5to3()) || (coord > ATGsite && !isCis5to3())) {        //坐标小于atg，在5‘UTR中,也是在外显子中
				enumMrnaLocate = EnumMrnaLocate.utr5;
			} else if((coord > UAGsite && isCis5to3()) || (coord < UAGsite && !isCis5to3())) {       //大于cds起始区，在3‘UTR中
				enumMrnaLocate = EnumMrnaLocate.utr3;
			} else {
				enumMrnaLocate = EnumMrnaLocate.cds;
			}
		} 
		else {
			enumMrnaLocate = EnumMrnaLocate.intron;
		}
		return enumMrnaLocate;
	}
	
	/**
	 * 坐标到该转录本起点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 * @return
	 */
	public int getCod2Tss(int coord) {
		if (isCis5to3()) {
			return coord - getTSSsite();
		} else {
			return -(coord - getTSSsite());
		}
	}
	/**
	 * 坐标到该转录本终点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 * @return
	 */
	public int getCod2Tes(int coord) {
		if (isCis5to3()) {
			return coord - getTESsite();
		} else {
			return -(coord - getTESsite());
		}
	}
	/**
	 * 坐标到ATG的距离，考虑正反向. 
	 * 在ATG上游为负数，下游为正数
	 * @return
	 */
	public int getCod2ATG(int coord) {
		if (isCis5to3()) {
			return coord - getATGsite();
		} else {
			return -(coord - getATGsite());
		}
	}
	/**
	 * 坐标到UAG的最后一个碱基的距离，考虑正反向.
	 * 在UAG上游为负数，下游为正数
	 * @return
	 */
	public int getCod2UAG(int coord) {
		if (isCis5to3()) {
			return coord - getUAGsite();
		} else {
			return -(coord - getUAGsite());
		}
	}
	/**
	 * 使用前先判定在UTR中<br>
	 * 如果坐标在UTR中，坐标距离UTR的起点，注意这个会去除内含子 <br>
	 */
	public int getCod2UTRstartmRNA(int coord) {
		int location = getCodLocUTRCDS(coord);
		if (location == COD_LOCUTR_5UTR) {
			return getLocDistmRNA(getTSSsite(), coord);
		} else if (location == COD_LOCUTR_3UTR) {
			return getLocDistmRNA(getUAGsite(), coord);
		}
		logger.error("不在UTR中");
		return 0;
	}
	/**
	 * 使用前先判定在UTR中<br>
	 * 如果坐标在UTR中，坐标距离UTR的终点，注意这个会去除内含子<br>
	 */
	public int getCod2UTRendmRNA(int coord) {
		int location = getCodLocUTRCDS(coord);
		if (location == COD_LOCUTR_5UTR) {
			return getLocDistmRNA(coord, getATGsite());
		}
		else if (location == COD_LOCUTR_3UTR) {
			return getLocDistmRNA(coord, getTESsite());
		}
		logger.error("不在UTR中");
		return 0;
	}
	/**
	 * 使用前先判定在Exon中，坐标到该转录本atg的距离
	 * 当坐标为atg的a时，返回0。以此类推。
	 * 不去除内含子的直接用cod2atg/cod2End
	 * 如果不在内含子中，则为很大的负数，大概-10000000
	 */
	public int getCod2ATGmRNA(int coord) {
		return getLocDistmRNA(ATGsite, coord);
	}
	/**
	 * 使用前先判定在Exon中，坐标到UAG最后一个字母的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 坐标在终点上游为负数，下游为正数<br>
	 * 如果不在内含子中，则为很大的负数，大概-10000000
	 */
	public int getCod2UAGmRNA(int coord) {
		return getLocDistmRNA(UAGsite, coord);
	}
	/**
	 * 使用前先判定在Exon中，坐标到TSS的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 只有当坐标处在外显子中才有距离，不包含内含子\
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
	 */
	public int getCod2TSSmRNA(int coord) {
		return getLocDistmRNA(getTSSsite(), coord);
	}
	/**
	 * 使用前先判定在Exon中，坐标到TES的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 因为cod在外显子中，所以肯定在tES上游，所以该值始终为负数
	 */
	public int getCod2TESmRNA(int coord) {
		return getLocDistmRNA(getTESsite(), coord);
	}
	
	private boolean isSiteInCds(int location) {
		if (location < Math.min(ATGsite, UAGsite) || location > Math.max(ATGsite, UAGsite)) {
			return false;
		}
		if (getNumCodInEle(location) <= 0) {
			return false;
		}
		return true;
	}
	/**
	 * 返回能和本loc组成一个氨基酸的头部nr的坐标，从1开始计算
	 * @param location
	 * @return 如果location不在cds中，则返回-1
	 */
	public int getLocAAbefore(int location) {
		if (!isSiteInCds(location)) {
			return -1;
		}
		int startLen = getLocDistmRNA(ATGsite, location);
		
		return  getLocDistmRNASite(location, -startLen%3);
	}
	/**
	 * 返回能和本loc组成一个氨基酸的头部nr的偏移，也就是向前偏移几个碱基，不考虑内含子
	 * 恒为负数，正数就说明出错了
	 * @param location
	 * @return 如果本位点就是一个氨基酸的第一个位点，则返回0
	 */
	public int getLocAAbeforeBias(int location) {
		int startLen = getLocDistmRNA(ATGsite,location);
		return -startLen%3;
	}
	/**
	 * 返回能和本loc组成一个氨基酸的尾部nr的坐标，从1开始计算
	 * @param location
	 * @return 如果location不在cds中，则返回-1
	 */
	public int getLocAAend(int location) {
		if (!isSiteInCds(location)) {
			return -1;
		}
		int startLen = getLocDistmRNA(ATGsite, location);
		return  getLocDistmRNASite(location, 2 - startLen%3);
	}
	/** 获取下一个aa的终点的坐标 */
	public int getLocAANextEnd(int coord) {
		if (!isSiteInCds(coord)) {
			throw new ExceptionNbcGFF("cannot get coord not on CDS");
		}
		int endCoord = getLocAAend(coord);
		if (endCoord == getUAGsite()) {
			throw new ExceptionNbcGFF("cannot get the next coord while coord on stop code");
		}
		return getLocDistmRNASite(endCoord, 3);
	}
	/** 获取上一个aa的起点的坐标 */
	public int getLocAALastStart(int coord) {
		if (!isSiteInCds(coord)) {
			throw new ExceptionNbcGFF("cannot get coord not on CDS");
		}
		int startCoord = getLocAAbefore(coord);
		return getLocDistmRNASite(startCoord, -3);
	}
	
	/** 获取上一个aa的终点的坐标 */
	public int getLocAALastEnd(int coord) {
		if (!isSiteInCds(coord)) {
			throw new ExceptionNbcGFF("cannot get coord not on CDS");
		}
		int startCoord = getLocAAbefore(coord);
		return getLocDistmRNASite(startCoord, -1);
	}
	
	/**
	 * 返回能和本loc组成一个氨基酸的尾部nr的偏移，也就是向后偏移几个碱基，不考虑内含子
	 * 恒为正数，负数就说明出错了
	 * @param location
	 * @return 如果本位点就是一个氨基酸的最后一个位点，也就是第三位，则返回0
	 */
	public int getLocAAendBias(int location) {
		int startLen = getLocDistmRNA(ATGsite,location);
		return 2 - startLen%3;
	}
	/**
	 * 获得具体的编码序列，待修正，可以调用getSeqLoc的方法提取序列
	 * 没有结果就返回new list-exonInfo
	 * @return
	 */
	public List<ExonInfo> getIsoInfoCDS() {
		if (Math.abs(ATGsite - UAGsite) <= 1) {
			return new ArrayList<ExonInfo>();
		}
		return getRangeIsoOnExon(ATGsite, UAGsite);
	}
	/**
	 * 获得3UTR的信息
	 * @param startLoc
	 * @param endLoc
	 * @return
	 */
	public List<ExonInfo> getUTR3seq() {
		if (Math.abs(ATGsite - UAGsite) <= 1) {
			return new ArrayList<ExonInfo>();
		}
		int i = isCis5to3()? 1 : -1;
		return getRangeIsoOnExon(UAGsite + i, getTESsite());
	}
	/**
	 * 获得5UTR的信息
	 * @return
	 */
	public List<ExonInfo> getUTR5seq() {
		if (Math.abs(ATGsite - UAGsite) <= 1) {
			return new ArrayList<ExonInfo>();
		}
		int i = isCis5to3()? 1 : -1;
		return getRangeIsoOnExon(getTSSsite(), ATGsite - i);
	}
	/**
	 * 指定一个起点和一个终点坐标，将这两个坐标间的外显子区域提取出来并返回
	 * 按照基因的方向排序
	 * 大小无所谓，最后返回不依赖 startLoc和EndLoc的大小关系
	 * 如果这两个坐标不在外显子中，则返回空的list
	 * @return
	 */
	public List<ExonInfo> getRangeIsoOnExon(int startLoc, int endLoc) {
		int exonNumStart = getNumCodInEle(startLoc) - 1;
		int exonNumEnd =getNumCodInEle(endLoc) - 1;
		
		if (exonNumStart < 0 || exonNumEnd < 0) {
			return new ArrayList<>();
		}
		return getSubGffGeneIso(startLoc, endLoc).getLsElement();
	}

	/**
	 * 文字形式的定位描述, <b>首先在gffDetailGene中设定tss，tes这两项</b><br>
	 * null: 不在该转录本内
	 * 
	 * 指定条件，将符合条件的peak抓出来并做注释，主要是筛选出合适的peak然后做后续比较工作
	 * 不符合的会跳过
	 * @param filterTss 是否进行tss筛选<b>只有当filterGeneBody为false时，tss下游才会发会作用</b>
	 * @param filterGenEnd 是否进行geneEnd筛选<b>只有当filterGeneBody为false时，geneEnd上游才会发会作用</b>
	 * @param filterGeneBody 是否处于geneBody，true，将处于geneBody的基因全部筛选出来，false，不进行geneBody的筛选<br>
	 * <b>以下条件只有当filterGeneBody为false时才能发挥作用</b>
	 * @param filter5UTR 是否处于5UTR中
	 * @param filter3UTR 是否处于3UTR中
	 * @param filterExon 是否处于外显子中
	 * @param filterIntron 是否处于内含子中
	 * 0-n:输入的loc信息<br>
	 * n+1: 基因名<br>
	 * n+2: 基因信息<br>
	 **/
	public boolean isCodLocFilter(int coord, boolean filterTss, int[] tss, boolean filterGenEnd, int[] geneEnd,  
			boolean filterGeneBody,boolean filter5UTR, boolean filter3UTR,boolean filterExon, boolean filterIntron) {
		boolean filter = false;
		if (filterTss == true) {
			if (isCodInIsoTss(tss, coord)) filter = true;
		}
		if (filterGenEnd == true) {
			if (isCodInIsoGenEnd(geneEnd, coord)) filter = true;
		}
		
		if (filterGeneBody && getCodLoc(coord) != COD_LOC_OUT) {
			filter = true;
		} else if (filter5UTR && getCodLocUTRCDS(coord) == COD_LOCUTR_5UTR) {
			filter = true;
		} else if (filter3UTR && getCodLocUTRCDS(coord) == COD_LOCUTR_3UTR) {
			filter = true;
		} else if (filterExon && getCodLoc(coord) == COD_LOC_EXON) {
			filter = true;
		} else if (filterIntron && getCodLoc(coord) == COD_LOC_INTRON) {
			filter = true;
		}
		return filter;
	}
	
	/** 最常规的添加exon，不做任何判定 */
	protected void addExonNorm(Boolean cis5to3, int locStart, int locEnd) {
		if (cis5to3 != null && isCis5to3() != cis5to3) {
			throw new ExceptionNbcGFF("add exon error " + cis5to3 + " " + locStart + " " + locEnd);
		}
		
		ExonInfo exonInfo = new ExonInfo(isCis5to3(), locStart, locEnd);
		add(exonInfo);
	}
	/**
	 * 假设是安顺序添加的ID
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 * @param cis5to3 exon的方向，null表示选取和iso一样的方向
	 * @param locStart exon的起点，程序会将其与locEnd中自动选择起点
	 * @param locEnd exon的终点，程序会将其与locStart中自动选择起点
	 */
	protected void addCDS(Boolean cis5to3, int locStart, int locEnd) {
		addExon(cis5to3, locStart, locEnd, true);
	}
	/**
	 * 假设是安顺序添加的ID
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 * @param cis5to3 exon的方向，null表示选取和iso一样的方向
	 * @param locStart exon的起点，程序会将其与locEnd中自动选择起点
	 * @param locEnd exon的终点，程序会将其与locStart中自动选择起点
	 */
	protected void addExon(Boolean cis5to3, int locStart, int locEnd) {
		addExon(cis5to3, locStart, locEnd, false);
	}
	
	/**
	 * 假设是安顺序添加的ID
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 * @param cis5to3 exon的方向，null表示选取和iso一样的方向
	 * @param locStart exon的起点，程序会将其与locEnd中自动选择起点
	 * @param locEnd exon的终点，程序会将其与locStart中自动选择起点
	 */
	private void addExon(Boolean cis5to3, int locStart, int locEnd, boolean isCds) {
		if (cis5to3 != null && isCis5to3() != cis5to3) {
			throw new ExceptionNbcGFF("add exon error " + cis5to3 + " " + locStart + " " + locEnd);
		}
		ExonInfo exonInfo = new ExonInfo(isCis5to3(), locStart, locEnd);
		if (size() == 0) {
			add(exonInfo);
			return;
		}
		ExonInfo exonLast = get(size() - 1);
		ExonInfo exonFirst = get(0);
		if (isCis5to3() && exonInfo.getStartAbs() >= exonLast.getEndAbs()) {
			add(exonInfo);
		} else if (!isCis5to3() && exonInfo.getStartAbs() >= exonFirst.getEndAbs()) {
			add(0,exonInfo);
		} else if (isCis5to3() && exonInfo.getEndAbs() <= exonFirst.getStartAbs()) {
			add(0, exonInfo);
		} else if (!isCis5to3() && exonInfo.getEndAbs() <= exonLast.getStartAbs()) {
			add(exonInfo);
		}else if (isCis5to3() && exonInfo.equalsLoc(exonLast) || !isCis5to3() && exonInfo.equalsLoc(exonFirst)) {
			return;
		} else {
			if (isCds && ( Math.min(locStart, locEnd) > getStartAbs() && Math.max(locStart, locEnd) < getEndAbs())) {
				return;
			}
			add(exonInfo);
		}
	}
	
	/**
	 * @param cis5to3 exon的方向
	 * @param locStart
	 * @param locEnd
	 */
	protected void addFirstExon(Boolean cis5to3, int locStart, int locEnd) {
		if (cis5to3 != null && isCis5to3() != cis5to3) {
			throw new ExceptionNbcGFF("add exon error " + cis5to3 + " " + locStart + " " + locEnd);
		}
		ExonInfo exonInfo = new ExonInfo(isCis5to3(), locStart, locEnd);
		clearElements();
		add(exonInfo);
	}

	public BsearchSiteDu<ExonInfo> searchLocationDu(int startLoc, int endLoc) {
		BinarySearch<ExonInfo> binarySearch = new BinarySearch<>(lsElement, isCis5to3());
		return binarySearch.searchLocationDu(startLoc, endLoc);
	}
	
	public BsearchSite<ExonInfo> searchLocation(int loc) {
		BinarySearch<ExonInfo> binarySearch = new BinarySearch<>(lsElement, isCis5to3());
		return binarySearch.searchLocation(loc);
	}

	/**
	 * 排序并去除重复exon
	 * 如果输入的是GffPlant的类型，
	 * 那么可能UTR和CDS会错位。这时候就需要先将exon排序，然后合并overlap或者是挨着的(exon相邻，只相差1bp)exon
	 */
	public void sortAndCombine() {
		if (lsElement.isEmpty()) {
			return;
		}
		sortOnly();
		
		ArrayList<ExonInfo> lsExonsResult = new ArrayList<>();
		lsExonsResult.add(lsElement.get(0));
		
		ExonInfo exonInfoLast = lsExonsResult.get(0);
		boolean isNeedReset = false;
		
		for (int i = 1; i < lsElement.size(); i++) {
			ExonInfo exonInfo = lsElement.get(i);
			if (isCis5to3() && exonInfoLast.getEndAbs() >= exonInfo.getStartAbs()-1) {
				exonInfoLast.setEndAbs(Math.max(exonInfoLast.getEndAbs(), exonInfo.getEndAbs()));
				isNeedReset = true;
			} else if (!isCis5to3() && exonInfoLast.getStartAbs()<=exonInfo.getEndAbs()+1) {
				exonInfoLast.setStartAbs(Math.min(exonInfoLast.getStartAbs(), exonInfo.getStartAbs()));
				isNeedReset = true;
			} else {
				lsExonsResult.add(exonInfo);
				exonInfoLast = exonInfo;
			}
		}
		if (isNeedReset) {
			lsElement = lsExonsResult;
		}
	}
	
	/**
	 * 将list中的元素进行排序，如果反向，那么就从大到小排序
	 * 如果正向，那么就从小到大排序
	 * 内部有flag，排完后就不会再排第二次了
	 */
	public void sortOnly() {
		if (isCis5to3()) {
			Collections.sort(lsElement, new CompS2M());
		} else {
			Collections.sort(lsElement, new CompM2S());
		}
	}
	

	
	protected String getBedFormat(String chrID, String title) {
		StringBuilder bed = new StringBuilder();
		bed.append(chrID).append("\t")
		.append(getStartAbs() - 1).append("\t")
		.append(getEndAbs()).append("\t")
		.append(getName()).append("\t")
		.append("0").append("\t");
		if (isCis5to3()) {
			bed.append("+").append("\t");
		} else {
			bed.append("-").append("\t");
		}
		if (ismRNA()) {
			bed.append(Math.min(getATGsite(), getUAGsite()) - 1).append("\t");
			bed.append(Math.max(getATGsite(), getUAGsite())).append("\t");
		} else {
			bed.append(getEndAbs()).append("\t");
			bed.append(getEndAbs()).append("\t");
		}
		bed.append(0).append("\t");
		bed.append(size()).append("\t");
		
		StringBuilder len = new StringBuilder();
		if (isCis5to3()) {
			for (ExonInfo exonInfo : lsElement) {
				len.append(exonInfo.getLength());
					len.append(",");
			}
		} else {
			for (int i = size() - 1; i >= 0; i--) {
				ExonInfo exonInfo = get(i);
				len.append(exonInfo.getLength());
				len.append(",");
			}
		}
		bed.append(len.toString()).append("\t");
		
		StringBuilder site = new StringBuilder();
		if (isCis5to3()) {
			for (ExonInfo exonInfo : lsElement) {
				site.append(exonInfo.getStartAbs() - getStartAbs());
				site.append(",");
			}
		} else {
			for (int i = size() - 1; i >= 0; i--) {
				ExonInfo exonInfo = get(i);
				site.append(exonInfo.getStartAbs() - getStartAbs());
				site.append(",");
			}
		}
		bed.append(site.toString());
		return bed.toString();
	}
	
	/**
	 * 返回该基因的GTF格式文件，末尾有换行符
	 * @param chrID 染色体名，主要是为了大小写问题，null表示走默认
	 * @param geneID 该基因的名字
	 * @param title 该GTF文件的名称
	 * @return
	 */
	public String getGTFformat(String chrID, String title) {
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		String genetitle = getGTFformatExon(chrID, title,strand);
		return genetitle;
	}
	/**
	 * 返回该基因的GTF格式文件，末尾有换行符
	 * @param title 该GTF文件的名称
	 * @return
	 */
	public String getGFFformat(String title) {
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		String genetitle = getGFFformatExonMISO(title,strand);
		return genetitle;
	}

	protected String getGTFformatExon(String chrID, String title, String strand) {
		if (chrID == null) chrID = getRefID();

		List<String> lsResult = new ArrayList<>();
		
		List<String> lsHead = new ArrayList<>();
		if (StringOperate.isRealNull(title)) {
			title = GffHashGene.GFFDBNAME;
		}
		lsHead.add(chrID); lsHead.add(title);
		
		List<String> lsSuffixInfo = new ArrayList<>();
		lsSuffixInfo.add("."); lsSuffixInfo.add(strand); lsSuffixInfo.add(".");
		lsSuffixInfo.add("gene_id \"" + getParentGeneName() + "\"; transcript_id " + "\"" + getName()+"\"; genetype " + "\"" + getGeneType()+"\"");
		
		List<ExonInfo> lsAtg = getATGLoc();
		List<ExonInfo> lsUag = getUAGLoc();
		boolean ismRNA = ismRNAFromCds();
		//UAG之前的最后一个位点
		int uagBeforeSite = getLocAALastEnd(UAGsite);

		for (ExonInfo exons : lsElement) {
			List<String> lsTmpResult = new ArrayList<>();
			ExonInfo cds = getCds(exons, ATGsite, uagBeforeSite);
			if (ismRNA && !lsAtg.isEmpty() && ATGsite >= exons.getStartAbs() && ATGsite <= exons.getEndAbs()) {
				for (ExonInfo atg : lsAtg) {
					lsTmpResult.clear();
					lsTmpResult.addAll(lsHead);
					lsTmpResult.add(GffHashGTF.startCodeFlag);
					lsTmpResult.add(atg.getStartAbs() + "");
					lsTmpResult.add(atg.getEndAbs() + "");
					lsSuffixInfo.set(2, getCdsCodeNum(exons.getStartCis())+"");
					lsTmpResult.addAll(lsSuffixInfo);
					lsResult.add(ArrayOperate.cmbString(lsTmpResult.toArray(new String[0]), "\t"));
				}
			}
			lsTmpResult.clear();
			lsTmpResult.addAll(lsHead);
			lsTmpResult.add("exon");
			lsTmpResult.add(exons.getStartAbs() + "");
			lsTmpResult.add(exons.getEndAbs() + "");
			lsSuffixInfo.set(2, ".");
			lsTmpResult.addAll(lsSuffixInfo);
			lsResult.add(ArrayOperate.cmbString(lsTmpResult.toArray(new String[0]), "\t"));
			
			if (cds != null) {
				lsTmpResult.clear();
				lsTmpResult.addAll(lsHead);
				lsTmpResult.add("CDS");
				lsTmpResult.add(cds.getStartAbs() + "");
				lsTmpResult.add(cds.getEndAbs() + "");
				lsSuffixInfo.set(2, getCdsCodeNum(cds.getStartCis()) + "");
				lsTmpResult.addAll(lsSuffixInfo);
				lsResult.add(ArrayOperate.cmbString(lsTmpResult.toArray(new String[0]), "\t"));
			}
			if (ismRNA && !lsUag.isEmpty()&& uagBeforeSite >= exons.getStartAbs() && uagBeforeSite <= exons.getEndAbs()) {
				for (ExonInfo uag : lsUag) {
					lsTmpResult.clear();
					lsTmpResult.addAll(lsHead);
					lsTmpResult.add(GffHashGTF.stopCodeFlag);
					lsTmpResult.add(uag.getStartAbs() + "");
					lsTmpResult.add(uag.getEndAbs() + "");
					lsSuffixInfo.set(2, getCdsCodeNum(exons.getStartCis())+"");
					lsTmpResult.addAll(lsSuffixInfo);
					lsResult.add(ArrayOperate.cmbString(lsTmpResult.toArray(new String[0]), "\t"));
				}
			}
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		for (String content : lsResult) {
	        stringBuilder.append(content + TxtReadandWrite.ENTER_LINUX);
        }
		return stringBuilder.toString();
	}
	
	private int getCdsCodeNum(int cdsStart) {
		int codNum = getCod2ATGmRNA(cdsStart)%3;
		if (codNum == 1) {
			codNum = 2;
		} else if (codNum == 2) {
			codNum = 1;
		}
		return codNum;
	}
	
	/** 
	 * 根据输入的exon，判断是否存在cds，并返回<br>
	 * null表示没有cds
	 * @param exons
	 * @param atg
	 * @param uagBeforeSite GTF的cds结尾是在uag之前结束的
	 * @return
	 */
	private ExonInfo getCds(ExonInfo exons, int atg, int uagBeforeSite) {
		if (!ismRNAFromCds()) {
			return null;
		}
		
		ExonInfo cdsInfo = exons.clone();
		ExonInfo atgUag = new ExonInfo(isCis5to3(), atg, uagBeforeSite);
		if (cdsInfo.getEndAbs() < atgUag.getStartAbs() || cdsInfo.getStartAbs() > atgUag.getEndAbs()) {
			return null;
		}

		if (cdsInfo.isCodInSide(atg)) {
			cdsInfo.setStartCis(atg);
		}
		if (cdsInfo.isCodInSide(uagBeforeSite)) {
			cdsInfo.setEndCis(uagBeforeSite);
		}
		return cdsInfo;
	}
	
	protected String getGFFformatExonMISO(String title, String strand) {
		String geneExon = "";
		for (int i = 0; i < size(); i++) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getRefID() + "\t" +title + "\texon\t" + exons.getStartAbs() + "\t" + exons.getEndAbs()
		     + "\t.\t" +strand+"\t.\t"+ "ID=exon:" + getName()  + ":" + (i+1) +";Parent=" + getName() + " "+TxtReadandWrite.ENTER_LINUX;
		}
		return geneExon;
	}

	/**
	 * 可能不是很精确
	 * 返回距离Tss的一系列坐标的实际坐标
	 * @param is 相对于 TSS的坐标信息，譬如-200到-100，-100到100，100到200等，每一组就是一个int[2]，注意int[1]必须小于int[2]
	 * @return
	 * 最后获得的结果正向int[0] < int[1]
	 * 反向 int[0] > int[1]
	 */
	public ArrayList<int[]> getRegionNearTss(Collection<int[]> isList) {
		ArrayList<int[]> lsTmp = new ArrayList<int[]>();
		int tsssite = getTSSsite();
		for (int[] is : isList) {
			int[] tmp = new int[2];
			if (isCis5to3()) {
				tmp[0] = tsssite + is[0];
				tmp[1] = tsssite + is[1];
			}
			else {
				tmp[0] = tsssite - is[0];
				tmp[1] = tsssite - is[1];
			}
			lsTmp.add(tmp);
		}
		return lsTmp;
	}
	
	public GffIso getSubGffGeneIso(int startLoc, int endLoc) {
		int startAbs = Math.min(startLoc, endLoc);
		int endAbs = Math.max(startLoc, endLoc);
		GffIso gffGeneIsoInfoResult = this.clone();
		gffGeneIsoInfoResult.clearElements();
		for (ExonInfo exonInfo : lsElement) {
			ExonInfo exonInfoResult = exonInfo.clone();
			if (exonInfo.getEndAbs() < startAbs || exonInfo.getStartAbs() > endAbs) {
				continue;
			}
			
			if (exonInfo.getStartAbs() <= startAbs && exonInfo.getEndAbs() >= startAbs) {
				exonInfoResult.setStartAbs(startAbs);
			}
			if (exonInfo.getStartAbs() <= endAbs && exonInfo.getEndAbs() >= endAbs) {
				exonInfoResult.setEndAbs(endAbs);
			}
			gffGeneIsoInfoResult.add(exonInfoResult);
		}
		return gffGeneIsoInfoResult;
	}
	
	public List<ExonInfo> getSubExon(int startLoc, int endLoc) {
		int startAbs = Math.min(startLoc, endLoc);
		int endAbs = Math.max(startLoc, endLoc);
		List<ExonInfo> lsExonsResult = new ArrayList<>();
		for (ExonInfo exonInfo : lsElement) {
			ExonInfo exonInfoResult = exonInfo.clone();
			if (exonInfo.getEndAbs() < startAbs || exonInfo.getStartAbs() > endAbs) {
				continue;
			}
			
			if (exonInfo.getStartAbs() <= startAbs && exonInfo.getEndAbs() >= startAbs) {
				exonInfoResult.setStartAbs(startAbs);
			}
			if (exonInfo.getStartAbs() <= endAbs && exonInfo.getEndAbs() >= endAbs) {
				exonInfoResult.setEndAbs(endAbs);
			}
			exonInfoResult.setParent(this);
			lsExonsResult.add(exonInfoResult);
		}
		return lsExonsResult;
	}
	/**
	 * 获得Intron的list信息，从前到后排序
	 * 没有结果就返回new list-exonInfo
	 * @return
	 */
	public ArrayList<ExonInfo> getLsIntron() {
		ArrayList<ExonInfo> lsresult = new ArrayList<ExonInfo>();
		if (size() == 1) {
			return lsresult;
		}
		ExonInfo intronInfo = null;
		for (int i = 0; i < size(); i++) {
			ExonInfo exonInfo = get(i);
			if (i > 0) {
				if (exonInfo.isCis5to3()) {
					intronInfo.setEndCis(exonInfo.getStartCis() - 1);
				} else {
					intronInfo.setEndCis(exonInfo.getStartCis() + 1);
				}
			}
			if (i == size() - 1) {
				break;
			}
			intronInfo = new ExonInfo();
			lsresult.add(intronInfo);
			intronInfo.setParent(this);
			intronInfo.setCis5to3(exonInfo.isCis5to3());
			
			if (exonInfo.isCis5to3()) {
				intronInfo.setStartCis(exonInfo.getEndCis() + 1);
			} else {
				intronInfo.setStartCis(exonInfo.getEndCis() - 1);
			}
		}
		return lsresult;
	}
	/**
	 * 给定nr位点，换算为距离ATG多少aa位置
	 * 直接给定nr的实际位点
	 */
	public int getAAsiteNum(int codSite) {
		if (Math.abs(ATGsite-UAGsite) < 2) {
			return 0;
		}
		int aaNum = getLocDistmRNA( ATGsite, codSite);
		if (aaNum < 0) {
			return 0;
		}
		aaNum = aaNum + 1;
		return (aaNum+2)/3;
	}

	/**
	 * 文字形式的定位描述, <b>首先在gffDetailGene中设定tss，tes这两项</b><br>
	 * null: 不在该转录本内
	 * 
	 * 指定条件，将符合条件的peak抓出来并做注释，主要是筛选出合适的peak然后做后续比较工作
	 * 不符合的会跳过
	 * @param filterTss 是否进行tss筛选<b>只有当filterGeneBody为false时，tss下游才会发会作用</b>
	 * @param filterGenEnd 是否进行geneEnd筛选<b>只有当filterGeneBody为false时，geneEnd上游才会发会作用</b>
	 * @param filterGeneBody 是否处于geneBody，true，将处于geneBody的基因全部筛选出来，false，不进行geneBody的筛选<br>
	 * <b>以下条件只有当filterGeneBody为false时才能发挥作用</b>
	 * @param filter5UTR 是否处于5UTR中
	 * @param filter3UTR 是否处于3UTR中
	 * @param filterExon 是否处于外显子中
	 * @param filterIntron 是否处于内含子中
	 * 0-n:输入的loc信息<br>
	 * n+1: 基因名<br>
	 * n+2: 基因信息<br>
	 **/
	public String toStringCodLocStrFilter(int coord, boolean filterTss, int[] tss, boolean filterGenEnd, int[] geneEnd, 
			boolean filterGeneBody,boolean filter5UTR, boolean filter3UTR,boolean filterExon, boolean filterIntron) {
		boolean filter = isCodLocFilter(coord, filterTss,tss, filterGenEnd, geneEnd, filterGeneBody, filter5UTR, filter3UTR, filterExon, filterIntron);
		if (filter) {
			return toStringCodLocStr(tss, coord);
		}
		else {
			return null;
		}
	}
	/**
	 * 文字形式的定位描述
	 * @return
	 * null: 不在该转录本内
	 */
	public String toStringCodLocStr(int[] tss, int coord) {
		String result = "gene_position:";
		if ( isCis5to3()) {
			result = result + "forward ";
		} else {
			result = result + "reverse ";
		}
		int codLoc = getCodLoc(coord);
		//promoter\
		if (isCodInIsoTss(tss, coord) && codLoc == COD_LOC_OUT) {
			if (getCod2Tss(coord) > PROMOTER_INTERGENIC_MAMMUM) {
				result = result + PROMOTER_INTERGENIC_STR;
			} else if (getCod2Tss(coord) > PROMOTER_DISTAL_MAMMUM) {
				result = result + PROMOTER_DISTAL_STR;
			} else {
				result = result + PROMOTER_PROXIMAL_STR;
			}
		} else if (isCodInIsoTss(tss, coord) && codLoc != COD_LOC_OUT) {
			result = result + PROMOTER_DOWNSTREAMTSS_STR;
		}
		
		result = result + " Distance_to_Tss_is:" + getCod2Tss(coord) + " ";
		//UTR
		if (getCodLocUTRCDS(coord) == COD_LOCUTR_5UTR) {
			result = result + "5UTR_";
		} else if (getCodLocUTRCDS(coord) == COD_LOCUTR_3UTR) {
			result = result + "3UTR_";
		}
		//exon intron
		if (codLoc == COD_LOC_EXON) {
			result = result + "Exon:exon_Position_Number_is:" + getNumCodInEle(coord);
		} else if (codLoc == COD_LOC_INTRON) {
			result = result + "Intron_intron_Position_Number_is:" + Math.abs(getNumCodInEle(coord));
		}
		//gene end
//		if (isCodInIsoGenEnd(coord)) {
			result = result + " Distance_to_GeneEnd: "+ getCod2Tes(coord);
//		}
		return result;
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder(geneName);
		stringBuilder.append("\t"); stringBuilder.append(getName() + ":");
		for (ExonInfo exonInfo : lsElement) {
			stringBuilder.append( exonInfo.getStartCis() + "-" + exonInfo.getEndCis() + ",");
		}
		return stringBuilder.toString();
	}
	
	/**
	 * 重写equal
	 * 比较是否为同一个转录本
	 * 不比较两个转录本的名字，也不比较coord
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		GffIso otherObj = (GffIso)obj;
		//物种，起点终点，ATG，UAG，外显子长度 等都一致
		boolean flag = this.getRefIDlowcase().equals(otherObj.getRefIDlowcase()) && this.getATGsite() == otherObj.getATGsite()
		&& this.getUAGsite() == otherObj.getUAGsite() && this.getTSSsite() == otherObj.getTSSsite()
		&& this.getListLen() == otherObj.getListLen();
		if (flag && equalsIso(otherObj) ) {
			return true;
		}
		return false;
	}
	
	/**
	 * 依次比较两个list中的元素是否一致。内部调用每个元素的equals方法
	 * 不比较name，如果需要比较name，那么就用equal
	 * 暂时还没重写equal
	 * 外显子比较如果一模一样则返回true；
	 * @param lsOtherExon
	 * @return
	 */
	public boolean equalsIso(GffIso iso) {
		if (iso.size() != size() ) {
			return false;
		}
		for (int i = 0; i < iso.size(); i++) {
			ExonInfo otherT = iso.get(i);
			ExonInfo thisT = get(i);
			if (otherT.getStartAbs() != thisT.getStartAbs() || otherT.getEndAbs() != thisT.getEndAbs() ) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 重写hash，不包含基因名信息，包含基因taxID，chrID，atg，uag，tss，长度，以及每一个exon的信息
	 * @return
	 */
	public int hashCode() {
		String info = this.getTaxID() + "//" + this.getRefIDlowcase() + "//" + this.getATGsite() + "//" + this.getUAGsite() + "//" + this.getTSSsite() + "//" + this.getListLen();
		for (ExonInfo exonInfo : lsElement) {
			info = info + SepSign.SEP_INFO + exonInfo.getStartAbs() + SepSign.SEP_ID + exonInfo.getEndAbs();
		}
		return   info.hashCode();
	}
	/**
	 * 它的父级，也就是gffDetailGene，只是地址传递
	 */
	public GffIso clone() {
		GffIso result = null;
		result = (GffIso) super.clone();
		result.lsElement = new ArrayList<>(lsElement);
		result.gffGene = gffGene;
		result.ATGsite = ATGsite;
		result.gffGene = gffGene;
		result.geneName = geneName;
		result.flagTypeGene = flagTypeGene;
		result.lengthIso = lengthIso;
		result.UAGsite = UAGsite;
		return result;
	}
	
	public static GffIso createGffGeneIso(String isoName, String parentName, GffGene gffDetailGene, GeneType geneType, boolean cis5to3) {
		GffIso gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffIsoCis(isoName, parentName, gffDetailGene, geneType);
		} else {
			gffGeneIsoInfo = new GffIsoTrans(isoName, parentName, gffDetailGene, geneType);
		}
		return gffGeneIsoInfo;
	}
	public static GffIso createGffGeneIso(String isoName, String parentName, GeneType geneType, boolean cis5to3) {
		GffIso gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffIsoCis(isoName, parentName, geneType);
		} else {
			gffGeneIsoInfo = new GffIsoTrans(isoName, parentName, geneType);
		}
		return gffGeneIsoInfo;
	}
	/** 返回两个iso比较的信息
	 * 	double ratio = 有多少exon的边界是相同的 / Math.min(gffGeneIsoInfo1.Size, gffGeneIsoInfo2.Size);
	 *  */
	public static double compareIsoRatio(GffIso gffGeneIsoInfo1, GffIso gffGeneIsoInfo2) {
		int[] compareInfo = compareIsoBorder(gffGeneIsoInfo1, gffGeneIsoInfo2);
		double ratio = (double)compareInfo[0]/Math.min(compareInfo[2], compareInfo[3]);
		return ratio;
	}
	/**
	 * 返回两个iso比较的信息
	 * 0说明完全不相同。方向不同或没有交集则直接返回0
	 * @param gffGeneIsoInfo1
	 * @param gffGeneIsoInfo2
	 * @return int[2] <br>
	 * 0:有多少exon的边界是相同的<br>
	 * 1:总体边界数<br>
	 * 2: gffGeneIsoInfo1-Size<br>
	 * 3: gffGeneIsoInfo2-Size<br>
	 */
	public static int[] compareIsoBorder(GffIso gffGeneIsoInfo1, GffIso gffGeneIsoInfo2) {
		//完全没有交集
		if (gffGeneIsoInfo1.isCis5to3() != gffGeneIsoInfo2.isCis5to3() 
				|| gffGeneIsoInfo1.getEndAbs() <= gffGeneIsoInfo2.getStartAbs() 
				|| gffGeneIsoInfo1.getStartAbs() >= gffGeneIsoInfo2.getEndAbs()) {
			return new int[]{0,gffGeneIsoInfo1.size() * 2, gffGeneIsoInfo1.size()*2, gffGeneIsoInfo2.size()*2};
		} else if (gffGeneIsoInfo1.equals(gffGeneIsoInfo2)) {
			int edgeNum = gffGeneIsoInfo1.size();
			return new int[]{edgeNum, edgeNum*2, edgeNum, edgeNum};
		}
		List<GffIso> lsGffGeneIsoInfos = new ArrayList<GffIso>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfo1); lsGffGeneIsoInfos.add(gffGeneIsoInfo2);
		List<ExonCluster> lsExonClusters = ExonClusterOperator.getExonClusterSingle(gffGeneIsoInfo1.isCis5to3(), lsGffGeneIsoInfos);
		//相同的边界数量，一个外显子有两个相同边界
		int sameBounds = 0;
		
		for (ExonCluster exonCluster : lsExonClusters) {
			sameBounds = sameBounds + ExonClusterOperator.getSameBoundsNum(exonCluster);
		}
		return new int[]{sameBounds, lsExonClusters.size()*2, gffGeneIsoInfo1.size()*2, gffGeneIsoInfo2.size()*2};
	}
	
	/**
	 * 返回两个iso比较的信息，外显子有重合就认为相同
	 * 0说明完全不相同。方向不同或没有交集则直接返回0
	 * @param gffGeneIsoInfo1
	 * @param gffGeneIsoInfo2
	 * @return int[2] <br>
	 * 0:有多少exon是overlap的<br>
	 * 1:lsExonClusters number<br>
	 * 2: gffGeneIsoInfo1-在lsExonClusters中的数量，如果其中的exonCluster中含有该转录本两个exon，仅计算1次<br>
	 * 3: gffGeneIsoInfo2-Size<br>
	 */
	public static int[] compareIso(GffIso gffGeneIsoInfo1, GffIso gffGeneIsoInfo2) {
		//完全没有交集
		if (gffGeneIsoInfo1.isCis5to3() != gffGeneIsoInfo2.isCis5to3() 
				|| gffGeneIsoInfo1.getEndAbs() <= gffGeneIsoInfo2.getStartAbs() 
				|| gffGeneIsoInfo1.getStartAbs() >= gffGeneIsoInfo2.getEndAbs()) {
			return new int[]{0,gffGeneIsoInfo1.size() * 2, gffGeneIsoInfo1.size()*2, gffGeneIsoInfo2.size()*2};
		} else if (gffGeneIsoInfo1.equals(gffGeneIsoInfo2)) {
			int edgeNum = gffGeneIsoInfo1.size();
			return new int[]{edgeNum, edgeNum*2, edgeNum, edgeNum};
		}
		List<GffIso> lsGffGeneIsoInfos = new ArrayList<GffIso>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfo1); lsGffGeneIsoInfos.add(gffGeneIsoInfo2);
		List<ExonCluster> lsExonClusters = ExonClusterOperator.getExonClusterSingle(gffGeneIsoInfo1.isCis5to3(), lsGffGeneIsoInfos);
		//相同的边界数量，一个外显子有两个相同边界
		int overlapExon = 0;
		int iso1Num = 0;
		int iso2Num = 0;
		for (ExonCluster exonCluster : lsExonClusters) {
			if (isExonOverlap(exonCluster)) {
				overlapExon++;
			}
			List<ExonInfo> lsExon1 = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo1);
			if (!ArrayOperate.isEmpty(lsExon1)) {
				iso1Num++;
			}
			
			List<ExonInfo> lsExon2 = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfo2);
			if (!ArrayOperate.isEmpty(lsExon2)) {
				iso2Num++;
			}
		}
		return new int[]{overlapExon, lsExonClusters.size(), iso1Num, iso2Num};
	}
	
	//TODO 该方法待测试
	/**
	 * 返回两个iso比较的信息
	 * 看是否除了头尾边界外，其他边界都相同。
	 * 头尾边界一致也返回true
	 */
	public static boolean isExonEdgeSame_NotConsiderBound(GffIso gffGeneIsoInfo1, GffIso gffGeneIsoInfo2) {
		//完全没有交集
		if (gffGeneIsoInfo1.isCis5to3() != gffGeneIsoInfo2.isCis5to3() 
				|| gffGeneIsoInfo1.getEndAbs() <= gffGeneIsoInfo2.getStartAbs() 
				|| gffGeneIsoInfo1.getStartAbs() >= gffGeneIsoInfo2.getEndAbs()) {
			return false;
		}
		ArrayList<GffIso> lsGffGeneIsoInfos = new ArrayList<GffIso>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfo1); lsGffGeneIsoInfos.add(gffGeneIsoInfo2);
		List<ExonCluster> lsExonClusters = ExonClusterOperator.getExonClusterSingle(gffGeneIsoInfo1.isCis5to3(), lsGffGeneIsoInfos);
		//相同的边界数量，一个外显子有两个相同边界
		//第一个overlap的exon
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getLsIsoExon().size() == 1) {
				continue;
			}
			if (!exonCluster.isSameExon() && !exonCluster.isEdgeDifferent(gffGeneIsoInfo1)) {
				return false;
			}
		}
		int[] compareInfo = GffIso.compareIsoBorder(gffGeneIsoInfo1, gffGeneIsoInfo2);
		double ratio = (double)compareInfo[0]/Math.min(compareInfo[2], compareInfo[3]);
		int minus = compareInfo[0]-Math.min(compareInfo[2], compareInfo[3]);
		int exonNumSmall = Math.min(gffGeneIsoInfo1.size(), gffGeneIsoInfo2.size()), exonNumBig = Math.min(gffGeneIsoInfo1.size(), gffGeneIsoInfo2.size());
		if ((ratio > 0.75 || ratio > 0.5 && minus <= 2) && exonNumSmall/exonNumBig > 0.75) {
			return true;
		}
		return false;
	}
	
	/**
	 * 当exoncluster中的exon不一样时，查看具体有几条边是相同的。
	 * 因为一致的exon也仅有2条相同边，所以返回的值为0，1，2
	 * @param exonCluster
	 * @return
	 */
	private static boolean isExonOverlap(ExonCluster exonCluster) {
		if (exonCluster.isSameExon()) {
			return true;
		}

		List<List<ExonInfo>> lsExon = exonCluster.getLsIsoExon();
		if (lsExon.size() < 2) {
			return false;
		}
		List<ExonInfo> lsExon1 = lsExon.get(0);
		List<ExonInfo> lsExon2 = lsExon.get(1);
		return lsExon1.size() > 0 && lsExon2.size() > 0;
	}
	
	/**
	 * 设定DISTAL Promoter区域在TSS上游的多少bp外，默认1000
	 * 目前仅和annotation的文字有关
	 * 1000bp以内为 Proximal Promoter_
	 * @param pROMOTER_DISTAL_MAMMUM
	 */
	public static void setPROMOTER_DISTAL_MAMMUM(int pROMOTER_DISTAL_MAMMUM) {
		PROMOTER_DISTAL_MAMMUM = pROMOTER_DISTAL_MAMMUM;
	}
	/**
	 * 设定intergeneic区域在TSS上游的多少bp外，默认5000
	 * 目前仅和annotation的文字有关
	 * @param pROMOTER_INTERGENIC_MAMMUM
	 */
	public static void setPROMOTER_INTERGENIC_MAMMUM(
			int pROMOTER_INTERGENIC_MAMMUM) {
		PROMOTER_INTERGENIC_MAMMUM = pROMOTER_INTERGENIC_MAMMUM;
	}

}
