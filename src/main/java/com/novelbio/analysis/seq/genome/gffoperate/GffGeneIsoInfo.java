package com.novelbio.analysis.seq.genome.gffoperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

import com.novelbio.analysis.seq.genome.ExceptionNbcGFF;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffoperate.exoncluster.ExonCluster;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.listoperate.ListAbs;
import com.novelbio.listoperate.ListAbsSearch;
import com.novelbio.listoperate.ListCodAbs;
import com.novelbio.listoperate.ListCodAbsDu;

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
public abstract class GffGeneIsoInfo extends ListAbsSearch<ExonInfo, ListCodAbs<ExonInfo>, ListCodAbsDu<ExonInfo, ListCodAbs<ExonInfo>>> {
	private static final Logger logger = LoggerFactory.getLogger(GffGeneIsoInfo.class);
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

	@Id
	String id;
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public GeneType flagTypeGene = GeneType.ncRNA;
//	/** 设定基因的转录起点上游长度，默认为0 */
//	@Transient
//	protected int upTss = 0;
//	/** 设定基因的转录起点下游长度，默认为0  */
//	@Transient
//	protected int downTss=0;
//	/**  设定基因的转录终点点上游长度，默认为0 */
//	@Transient
//	protected int upTes=0;
//	/** 设定基因结尾向外延伸的长度，默认为0 */
//	@Transient
//	protected int downTes=100;
	
	/** 该转录本的ATG的第一个字符坐标，从1开始计数  */
	protected int ATGsite = ListCodAbs.LOC_ORIGINAL;
	/** 该转录本的UAG的最后一个字符坐标，从1开始计数 */
	protected int UAGsite = ListCodAbs.LOC_ORIGINAL;
	/** 该转录本的长度 */
	protected int lengthIso = ListCodAbs.LOC_ORIGINAL;
	
	@Transient
	GffDetailGene gffDetailGeneParent;
	@Indexed(unique = false)
	String gffFileId;
	/**
	 * 该名字为实际上的iso所在的基因名字，不一定为其 gffDetailGeneParent 的gene name
	 * 因为可能会有多个gffDetailGene合并为一个gffDetailGene，这时候直接用gffDetailGeneParent的名字就无法进行区分
	 */
	String geneParentName;
	
	@Transient
	GeneID geneID;
	
	//TODO 考虑兼容这种特性的exon
	/**
	 * 是否为错乱的exon
	 * 目前只看到叶绿体的基因是错乱的exon
	 */
	@Transient
	private boolean isUnorderedExon = false;	
	
	/** 给mongodb使用 */
	public GffGeneIsoInfo() {	}
	
	public GffGeneIsoInfo(String IsoName, String geneParentName, GeneType geneType) {
		super.listName = IsoName;
		this.flagTypeGene = geneType;
		this.geneParentName = geneParentName;
	}
	
	public GffGeneIsoInfo(String IsoName, String geneParentName, GffDetailGene gffDetailGene, GeneType geneType) {
		super.listName = IsoName;
		this.flagTypeGene = geneType;
		this.gffDetailGeneParent = gffDetailGene;
		this.geneParentName = geneParentName;
	}
	public void setParentGeneName(String geneParentName) {
		this.geneParentName = geneParentName;
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
		if (gffDetailGeneParent == null) {
			return 0;
		}
		return gffDetailGeneParent.getTaxID();
	}
	public void setGffDetailGeneParent(GffDetailGene gffDetailGeneParent) {
		if (gffDetailGeneParent == null) {
			return;
		}
		this.gffDetailGeneParent = gffDetailGeneParent;
	}
	public GffDetailGene getParentGffDetailGene() {
		return gffDetailGeneParent;
	}
	
	/** 返回同一组的GffDetailGene，效率略低 */
	public GffDetailGene getParentGffGeneSame() {
		GffDetailGene gffDetailGene = gffDetailGeneParent.clone();
		gffDetailGene.setStartAbs(-100);
		gffDetailGene.setEndAbs(-100);
		gffDetailGene.lsGffGeneIsoInfos.clear();
		for (GffGeneIsoInfo iso : gffDetailGeneParent.lsGffGeneIsoInfos) {
			if (iso.getParentGeneName().equals(getParentGeneName())) {
				gffDetailGene.addIsoSimple(iso);
			}
		}		
		return gffDetailGene;
	}
	
	/**
	 * 该名字为实际上的iso所在的基因名字，不一定为其 gffDetailGeneParent 的gene name<br>
	 * 因为可能会有多个gffDetailGene合并为一个gffDetailGene，这时候直接用gffDetailGeneParent的名字就无法进行区分
	 */
	public String getParentGeneName() {
		if (!StringOperate.isRealNull(geneParentName)) {
			return geneParentName;
		}
		return gffDetailGeneParent.getNameSingle();
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
		if (!ismRNA() || getCodLoc(coord) != GffGeneIsoInfo.COD_LOC_EXON) {
			return false;
		}
		if (getCod2ATG(coord) < 0 || getCod2UAG(coord) > 0) {
			return false;
		}
		return true;
	}
	public String getRefIDlowcase() {
		if (gffDetailGeneParent == null) {
			return "";
		}
		return gffDetailGeneParent.getRefID().toLowerCase();
	}
	public String getRefID() {
		if (gffDetailGeneParent == null) {
			return "";
		}
		return gffDetailGeneParent.getRefID();
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
	public void extendUtr(GffGeneIsoInfo gffGeneIsoInfo) {
		extend5Utr(gffGeneIsoInfo);
		extend3Utr(gffGeneIsoInfo);
	}
	
	
	private void extend5Utr(GffGeneIsoInfo gffGeneIsoInfo) {
		ListCodAbs<ExonInfo> codStart = gffGeneIsoInfo.searchLocation(getStart());
		if (!codStart.isInsideLoc()) {
			return;
		}
		int itemNum = codStart.getItemNumThis();
		List<ExonInfo> lsExonInfos = new ArrayList<>();
		for (int i = 0; i <= itemNum; i++) {
			lsExonInfos.add(gffGeneIsoInfo.get(i));
		}
		ExonInfo exonInfo = lsExonInfos.get(lsExonInfos.size() - 1);
		getLsElement().get(0).setStartCis(exonInfo.getStartCis());
		lsExonInfos = lsExonInfos.subList(0, lsExonInfos.size() - 1);
		getLsElement().addAll(0, lsExonInfos);
	}
	
	private void extend3Utr(GffGeneIsoInfo gffGeneIsoInfo) {
		ListCodAbs<ExonInfo> codStart = gffGeneIsoInfo.searchLocation(getEnd());
		if (!codStart.isInsideLoc()) {
			return;
		}
		
		int itemNum = codStart.getItemNumThis();
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
			if (ATGsite < 0 || ATGsite > Math.min(atg, uag)) {
				ATGsite = Math.min(atg, uag);
			}
			if (UAGsite < 0 || UAGsite < Math.max(atg, uag)) {
				UAGsite = Math.max(atg, uag);
			}
		} else {
			if (ATGsite < 0 || ATGsite < Math.max(atg, uag)) {
				ATGsite = Math.max(atg, uag);
			}
			if (UAGsite < 0 || UAGsite > Math.min(atg, uag)) {
				UAGsite = Math.min(atg, uag);
			}
		}
	}
	/** 如果是GTF文件指定了atg位点，就用这个设定，是ATG的第一个位点 */
	public void setATG(int atg) {
		ATGsite = atg;
	}
	/** 如果是GTF文件指定了uag位点，就用这个设定，是UAG的最后一个位点 */
	public void setUAG(int uag) {
		UAGsite = uag;
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
	protected int[] getATGLoc() {
		int[] atginfo = null;
		if (ATGsite > 0) {
			atginfo = new int[2];
			if (isCis5to3()) {
				atginfo[0] = ATGsite;
				atginfo[1] = ATGsite + 2;
			} else {
				atginfo[0] = ATGsite - 2;
				atginfo[1] = ATGsite;
			}
		}
		return atginfo;
	}
	protected int[] getUAGLoc() {
		int[] atginfo = null;
		if (UAGsite > 0) {
			atginfo = new int[2];
			if (isCis5to3()) {
				atginfo[0] = UAGsite - 2;
				atginfo[1] = UAGsite;
			} else {
				atginfo[0] = UAGsite;
				atginfo[1] = UAGsite + 2;
			}
		}
		return atginfo;
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
		return super.getStart();
	}
	/**
	 * 该转录本的Coding region end的最后一个字符坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getTESsite() {
		return super.getEnd();
	}
	public int getExonNum() {
		return size();
	}
	/**
	 * 获得5UTR的长度
	 * @return
	 */
	public int getLenUTR5() {
		if (ismRNA()) {
			return Math.abs(super.getLocDistmRNA(getTSSsite(), this.ATGsite) );
		}
		return 0;
	}
	
	/**
	 * 获得3UTR的长度
	 * @return
	 */
	public int getLenUTR3() {
		if (ismRNA()) {
			return Math.abs(super.getLocDistmRNA(this.UAGsite, getTESsite() ) );
		}
		return 0;
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
//		if (startCoord == getATGsite()) {
//			throw new ExceptionNbcGFF("cannot get the next coord while coord on stop code");
//		}
		return getLocDistmRNASite(startCoord, -3);
	}
	/** 获取上一个aa的终点的坐标 */
	public int getLocAALastEnd(int coord) {
		if (!isSiteInCds(coord)) {
			throw new ExceptionNbcGFF("cannot get coord not on CDS");
		}
		int startCoord = getLocAAbefore(coord);
//		if (startCoord == getATGsite()) {
//			throw new ExceptionNbcGFF("cannot get the next coord while coord on stop code");
//		}
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
	public ArrayList<ExonInfo> getIsoInfoCDS() {
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
	public ArrayList<ExonInfo> getUTR3seq() {
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
	public ArrayList<ExonInfo> getUTR5seq() {
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
	public ArrayList<ExonInfo> getRangeIsoOnExon(int startLoc, int endLoc) {
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
		boolean mycis5to3 = getExonCis5To3(cis5to3);
		
		ExonInfo exonInfo = new ExonInfo(mycis5to3, locStart, locEnd);
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
		boolean mycis5to3 = getExonCis5To3(cis5to3);
		ExonInfo exonInfo = new ExonInfo(mycis5to3, locStart, locEnd);
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
			logger.info("The Gff file may have error on gene: " + getName() + " and exon: " + getRefID() + " " + locStart + " " + locEnd + "  please check");

		}
	}
	
	/**
	 * @param cis5to3 exon的方向
	 * @param locStart
	 * @param locEnd
	 */
	protected void addFirstExon(Boolean cis5to3, int locStart, int locEnd) {
		boolean mycis5to3 = getExonCis5To3(cis5to3);
		ExonInfo exonInfo = new ExonInfo(mycis5to3, locStart, locEnd);
		clearElements();
		add(exonInfo);
	}
	
	/**
	 * 获得该exon的方向
	 * @param cis5to3 该exon的方向，如果为null，则需要返回该iso的方向，如果iso的方向也为null，则返回true
	 * @return
	 */
	private boolean getExonCis5To3(Boolean cis5to3) {
		boolean mycis5to3 = true;
		if (this.isCis5to3() != null) {
			mycis5to3 = this.isCis5to3();
		} else if (this.isCis5to3() != null) {
			mycis5to3 = cis5to3;
		} else if (getParentGffDetailGene() != null && getParentGffDetailGene().isCis5to3Real() != null) {
			mycis5to3 = getParentGffDetailGene().isCis5to3Real();
		} else {
			mycis5to3 = true;
		}
		return mycis5to3;
	}
	
	/**
	 * 获得该转录本的起点，不考虑方向
	 * @return
	 */
	public abstract int getStartAbs();
	/**
	 * 获得该转录本的终点，不考虑方向
	 * @return
	 */
	public abstract int getEndAbs();
	
	/**
	 * 如果输入的是GffPlant的类型，
	 * 那么可能UTR和CDS会错位。这时候就需要先将exon排序，然后合并overlap或者是挨着的(exon相邻，只相差1bp)exon
	 */
	protected void combineExon() {
		if (isEmpty()) {
			return;
		}
		super.sort();
		List<ExonInfo> lsExoninfo = new ArrayList<ExonInfo>();
		lsExoninfo.add(get(0));
		
		boolean combine = false;
		for (int i = 1; i < this.size(); i++) {
			ExonInfo exon = get(i);
			ExonInfo exonLast = lsExoninfo.get(lsExoninfo.size() - 1);
			if (isCis5to3() && exon.getStartCis() <= exonLast.getEndCis()+1) {
				combine = true;
				if (exon.getEndCis() > exonLast.getEndCis()) {
					exonLast.setEndCis(exon.getEndCis());
				}
			} else if (!isCis5to3() && exon.getStartCis() >= exonLast.getEndCis()-1) {
				combine = true;
				if (exon.getEndCis() < exonLast.getEndCis()) {
					exonLast.setEndCis(exon.getEndCis());
				}
			} else {
				lsExoninfo.add(exon);
			}
		}
		
		if (combine) {
			clearElements();
			for (ExonInfo exonInfo : lsExoninfo) {
				add(exonInfo);
			}
		}
		lsExoninfo.clear();
		lsExoninfo = null;
	}
	
	/**
	 * 排序并去除重复exon
	 */
	public void sort() {
		super.sort();
		LinkedHashSet<ExonInfo> setExonInfos = new LinkedHashSet<ExonInfo>();
		boolean haveDuplicateExon = false;
		for (ExonInfo exonInfo : this) {
			if (setExonInfos.contains(exonInfo)) {
				haveDuplicateExon = true;
				continue;
			}
			setExonInfos.add(exonInfo);
		}
		if (haveDuplicateExon) {
			clearElements();
			for (ExonInfo exonInfo : setExonInfos) {
				add(exonInfo);
			}
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
			for (ExonInfo exonInfo : this) {
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
			for (ExonInfo exonInfo : this) {
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
		lsHead.add(chrID); lsHead.add(title);
		
		List<String> lsSuffixInfo = new ArrayList<>();
		lsSuffixInfo.add("."); lsSuffixInfo.add(strand); lsSuffixInfo.add(".");
		lsSuffixInfo.add("gene_id \"" + getParentGeneName() + "\"; transcript_id " + "\"" + getName()+"\"; genetype " + "\"" + getGeneType()+"\"");
		
		int[] atg = getATGLoc();
		int[] uag = getUAGLoc();
		boolean ismRNA = ismRNAFromCds();
		for (ExonInfo exons : this) {
			List<String> lsTmpResult = new ArrayList<>();
			ExonInfo cds = getCds(exons);
			if (ismRNA && atg != null && ATGsite >= exons.getStartAbs() && ATGsite <= exons.getEndAbs()) {
				lsTmpResult.clear();
				lsTmpResult.addAll(lsHead);
				lsTmpResult.add(GffHashGTF.startCodeFlag);
				lsTmpResult.add(atg[0] + ""); lsTmpResult.add(atg[1] + "");
				lsSuffixInfo.set(2, ".");
				lsTmpResult.addAll(lsSuffixInfo);
				lsResult.add(ArrayOperate.cmbString(lsTmpResult.toArray(new String[0]), "\t"));
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
			if (ismRNA && uag != null && UAGsite >= exons.getStartAbs() && UAGsite <= exons.getEndAbs()) {
				lsTmpResult.clear();
				lsTmpResult.addAll(lsHead);
				lsTmpResult.add(GffHashGTF.stopCodeFlag);
				lsTmpResult.add(uag[0] + "");
				lsTmpResult.add(uag[1] + "");
				lsSuffixInfo.set(2, ".");
				lsTmpResult.addAll(lsSuffixInfo);
				lsResult.add(ArrayOperate.cmbString(lsTmpResult.toArray(new String[0]), "\t"));
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
	
	/** 根据输入的exon，判断是否存在cds，并返回<br>
	 * null表示没有cds
	 * @param exons
	 * @return
	 */
	private ExonInfo getCds(ExonInfo exons) {
		if (!ismRNAFromCds()) {
			return null;
		}
		
		ExonInfo cdsInfo = exons.clone();
		ExonInfo atgUag = new ExonInfo(isCis5to3(), ATGsite, UAGsite);
		if (cdsInfo.getEndAbs() < atgUag.getStartAbs() || cdsInfo.getStartAbs() > atgUag.getEndAbs()) {
			return null;
		}

		if (cdsInfo.isCodInSide(ATGsite)) {
			cdsInfo.setStartCis(ATGsite);
		}
		if (cdsInfo.isCodInSide(UAGsite)) {
			if (isCis5to3()) {
				cdsInfo.setEndCis(UAGsite-3);
			} else {
				cdsInfo.setEndCis(UAGsite+3);
			}
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
	
	public GffGeneIsoInfo getSubGffGeneIso(int startLoc, int endLoc) {
		int startAbs = Math.min(startLoc, endLoc);
		int endAbs = Math.max(startLoc, endLoc);
		GffGeneIsoInfo gffGeneIsoInfoResult = this.clone();
		gffGeneIsoInfoResult.clearElements();
		for (ExonInfo exonInfo : this) {
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
			intronInfo.setParentListAbs(this);
			intronInfo.setCis5to3(exonInfo.isCis5to3());
			intronInfo.addItemName(exonInfo.getNameSingle());
			
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
	 * 返回该GeneIsoName所对应的CopedID，因为是NM号所以不需要指定TaxID
	 * @return
	 */
	public GeneID getGeneID() {
		if (geneID == null) {
			geneID = new GeneID(getName(), gffDetailGeneParent.getTaxID());
		}
		return geneID;
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
		StringBuilder stringBuilder = new StringBuilder(geneParentName);
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
		
		GffGeneIsoInfo otherObj = (GffGeneIsoInfo)obj;
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
	public boolean equalsIso(ListAbs<ExonInfo> lsOther) {
		if (lsOther.size() != size() ) {
			return false;
		}
		for (int i = 0; i < lsOther.size(); i++) {
			ExonInfo otherT = lsOther.get(i);
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
		for (ExonInfo exonInfo : this) {
			info = info + SepSign.SEP_INFO + exonInfo.getStartAbs() + SepSign.SEP_ID + exonInfo.getEndAbs();
		}
		return   info.hashCode();
	}
	/**
	 * 它的父级，也就是gffDetailGene，只是地址传递
	 */
	public GffGeneIsoInfo clone() {
		GffGeneIsoInfo result = null;
		result = (GffGeneIsoInfo) super.clone();
		result.ATGsite = ATGsite;
		result.gffDetailGeneParent = gffDetailGeneParent;
		result.geneParentName = geneParentName;
		result.flagTypeGene = flagTypeGene;
		result.lengthIso = lengthIso;
		result.UAGsite = UAGsite;
		result.geneID = geneID;
		return result;
	}
	@Override
	protected ListCodAbs<ExonInfo> creatGffCod(String listName, int Coordinate) {
		ListCodAbs<ExonInfo> result = new ListCodAbs<ExonInfo>(listName, Coordinate);
		return result;
	}

	@Override
	protected ListCodAbsDu<ExonInfo, ListCodAbs<ExonInfo>> creatGffCodDu(
			ListCodAbs<ExonInfo> gffCod1, ListCodAbs<ExonInfo> gffCod2) {
		ListCodAbsDu<ExonInfo, ListCodAbs<ExonInfo>> result = new ListCodAbsDu<ExonInfo, ListCodAbs<ExonInfo>>(gffCod1, gffCod2);
		return result;
	}
	
	public static GffGeneIsoInfo createGffGeneIso(String isoName, String parentName, GffDetailGene gffDetailGene, GeneType geneType, boolean cis5to3) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(isoName, parentName, gffDetailGene, geneType);
		} else {
			gffGeneIsoInfo = new GffGeneIsoTrans(isoName, parentName, gffDetailGene, geneType);
		}
		return gffGeneIsoInfo;
	}
	public static GffGeneIsoInfo createGffGeneIso(String isoName, String parentName, GeneType geneType, boolean cis5to3) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(isoName, parentName, geneType);
		} else {
			gffGeneIsoInfo = new GffGeneIsoTrans(isoName, parentName, geneType);
		}
		return gffGeneIsoInfo;
	}
	/** 返回两个iso比较的信息
	 * 	double ratio = 有多少exon的边界是相同的 / Math.min(gffGeneIsoInfo1.Size, gffGeneIsoInfo2.Size);
	 *  */
	public static double compareIsoRatio(GffGeneIsoInfo gffGeneIsoInfo1, GffGeneIsoInfo gffGeneIsoInfo2) {
		int[] compareInfo = compareIso(gffGeneIsoInfo1, gffGeneIsoInfo2);
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
	public static int[] compareIso(GffGeneIsoInfo gffGeneIsoInfo1, GffGeneIsoInfo gffGeneIsoInfo2) {
		//完全没有交集
		if (!gffGeneIsoInfo1.isCis5to3().equals(gffGeneIsoInfo2.isCis5to3()) 
				|| gffGeneIsoInfo1.getEndAbs() <= gffGeneIsoInfo2.getStartAbs() 
				|| gffGeneIsoInfo1.getStartAbs() >= gffGeneIsoInfo2.getEndAbs()) {
			return new int[]{0,gffGeneIsoInfo1.size() * 2, gffGeneIsoInfo1.size()*2, gffGeneIsoInfo2.size()*2};
		} else if (gffGeneIsoInfo1.equals(gffGeneIsoInfo2)) {
			int edgeNum = gffGeneIsoInfo1.size();
			return new int[]{edgeNum, edgeNum*2, edgeNum, edgeNum};
		}
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfo1); lsGffGeneIsoInfos.add(gffGeneIsoInfo2);
		ArrayList<ExonCluster> lsExonClusters = getExonCluster(gffGeneIsoInfo1.isCis5to3(), lsGffGeneIsoInfos);
		//相同的边界数量，一个外显子有两个相同边界
		int sameBounds = 0;
		
		for (ExonCluster exonCluster : lsExonClusters) {
			sameBounds = sameBounds + getSameBoundsNum(exonCluster);
		}
		return new int[]{sameBounds, lsExonClusters.size()*2, gffGeneIsoInfo1.size()*2, gffGeneIsoInfo2.size()*2};
	}
	
	//TODO 该方法待测试
	/**
	 * 返回两个iso比较的信息
	 * 看是否除了头尾边界外，其他边界都相同。
	 * 头尾边界一致也返回true
	 */
	public static boolean isExonEdgeSame_NotConsiderBound(GffGeneIsoInfo gffGeneIsoInfo1, GffGeneIsoInfo gffGeneIsoInfo2) {
		//完全没有交集
		if (!gffGeneIsoInfo1.isCis5to3().equals(gffGeneIsoInfo2.isCis5to3()) 
				|| gffGeneIsoInfo1.getEndAbs() <= gffGeneIsoInfo2.getStartAbs() 
				|| gffGeneIsoInfo1.getStartAbs() >= gffGeneIsoInfo2.getEndAbs()) {
			return false;
		}
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfo1); lsGffGeneIsoInfos.add(gffGeneIsoInfo2);
		ArrayList<ExonCluster> lsExonClusters = getExonCluster(gffGeneIsoInfo1.isCis5to3(), lsGffGeneIsoInfos);
		//相同的边界数量，一个外显子有两个相同边界
		//第一个overlap的exon
		for (ExonCluster exonCluster : lsExonClusters) {
			if (exonCluster.getLsIsoExon().size() == 1) {
				continue;
			}
			if (!exonCluster.isSameExon() && !exonCluster.isEdgeSmaller(gffGeneIsoInfo1)) {
				return false;
			}
		}
		int[] compareInfo = GffGeneIsoInfo.compareIso(gffGeneIsoInfo1, gffGeneIsoInfo2);
		double ratio = (double)compareInfo[0]/Math.min(compareInfo[2], compareInfo[3]);
		int exonNumSmall = Math.min(gffGeneIsoInfo1.size(), gffGeneIsoInfo2.size()), exonNumBig = Math.min(gffGeneIsoInfo1.size(), gffGeneIsoInfo2.size());
		if (ratio > 0.75 && exonNumSmall/exonNumBig > 0.75) {
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
	private static int getSameBoundsNum(ExonCluster exonCluster) {
		if (exonCluster.isSameExon()) {
			return 2;
		}

		List<List<ExonInfo>> lsExon = exonCluster.getLsIsoExon();
		if (lsExon.size() < 2) {
			return 0;
		}
		List<ExonInfo> lsExon1 = lsExon.get(0);
		List<ExonInfo> lsExon2 = lsExon.get(1);
		if (lsExon1.size() == 0 || lsExon2.size() == 0) {
			return 0;
		}
		if (lsExon1.get(0).getStartAbs() == lsExon2.get(0).getStartAbs()
			|| lsExon1.get(0).getEndAbs() == lsExon2.get(0).getEndAbs() ) {
			return 1;
		}
		
		if (lsExon1.get(lsExon1.size() - 1).getStartAbs() == lsExon2.get(lsExon2.size() - 1).getStartAbs()
				|| lsExon1.get(lsExon1.size() - 1).getEndAbs() == lsExon2.get(lsExon2.size() - 1).getEndAbs()) {
			return 1;
		}
		return 0;
	}
	/**
	 * 按照分组好的边界exon，将每个转录本进行划分，
	 * 划分好的ExonCluster里面每组的lsExon都是考虑
	 * 了方向然后按照方向顺序装进去的 
	 */
	public static ArrayList<ExonCluster> getExonClusterSingle(Boolean cis5To3, List<GffGeneIsoInfo> lsGffGeneIsoInfos) {
		String chrID = lsGffGeneIsoInfos.get(0).getRefIDlowcase();
		ArrayList<ExonCluster> lsResult = new ArrayList<ExonCluster>();
		ArrayList<int[]> lsExonBound = ListAbs.getCombSep(cis5To3, lsGffGeneIsoInfos, false);
		ExonCluster exonClusterBefore = null;
		for (int[] exonBound : lsExonBound) {
			ExonCluster exonCluster = new ExonCluster(chrID, exonBound[0], exonBound[1], lsGffGeneIsoInfos, cis5To3);
			
			exonCluster.setExonClusterBefore(exonClusterBefore);
			if (exonClusterBefore != null) {
				exonClusterBefore.setExonClusterAfter(exonCluster);
			}
			
			exonCluster.initail();
			lsResult.add(exonCluster);
			exonClusterBefore = exonCluster;
		}
		return lsResult;
	}
	
	/**
	 * 按照分组好的边界exon，将每个转录本进行划分，
	 * 划分好的ExonCluster里面每组的lsExon都是考虑
	 * 了方向然后按照方向顺序装进去的 
	 */
	public static ArrayList<ExonCluster> getExonCluster(Boolean cis5To3, List<GffGeneIsoInfo> lsGffGeneIsoInfos) {
		String chrID = lsGffGeneIsoInfos.get(0).getRefIDlowcase();

		ArrayList<ExonCluster> lsExonClusters = getExonClusterSingle(cis5To3, lsGffGeneIsoInfos);
		List<ExonCluster> lsExonClustersNew = new ArrayList<>();
		ExonCluster exonClusterBefore = null;
		ExonCluster exonClusterBeforeReal = null;
		int[] exonMultiSE = null;
		
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			if (exonClusterBefore == null) {
				exonClusterBefore = exonCluster;
				exonClusterBeforeReal = exonClusterBefore;
				continue;
			}
			if (!exonClusterBefore.getMapIso2ExonIndexSkipTheCluster().isEmpty() 
					&& exonClusterBefore.getMapIso2ExonIndexSkipTheCluster().equals(exonCluster.getMapIso2ExonIndexSkipTheCluster())) {
				if (exonMultiSE == null) {
					int start = Math.min(exonClusterBefore.getStartAbs(), exonCluster.getStartAbs());
					int end = Math.max(exonClusterBefore.getEndAbs(), exonCluster.getEndAbs());
					exonMultiSE = new int[]{start, end};
				} else {
					exonMultiSE[0] = Math.min(exonMultiSE[0], exonCluster.getStartAbs());
					exonMultiSE[1] = Math.max(exonMultiSE[1], exonCluster.getEndAbs());
				}
				
//				if (exonMultiSE[0] > exonMultiSE[1]) {
//					System.out.println();
//					logger.debug("");
//				}
			} else {
				if (exonMultiSE != null) {
					ExonCluster exonClusterNew = new ExonCluster(chrID, exonMultiSE[0], exonMultiSE[1], lsGffGeneIsoInfos, cis5To3);
					exonClusterNew.setExonClusterBefore(exonClusterBeforeReal);
					exonClusterNew.setExonClusterAfter(lsExonClusters.get(i));
					exonClusterNew.initail();
					lsExonClustersNew.add(exonClusterNew);
					exonMultiSE = null;
				}
				if ( i >= 1) {
					exonClusterBeforeReal = lsExonClusters.get(i - 1);
				}
			}
			exonClusterBefore = exonCluster;
		}
		
		//不是multise不需要统计这个
//		if (exonMultiSE != null) {
//			ExonCluster exonClusterNew = new ExonCluster(chrID, exonMultiSE[0], exonMultiSE[1], lsGffGeneIsoInfos, cis5To3);
//			exonClusterNew.setExonClusterBefore(exonClusterBeforeReal);
//			exonClusterNew.initail();
//			lsExonClustersNew.add(exonClusterNew);
//			exonMultiSE = null;
//		}
		
		lsExonClusters.addAll(lsExonClustersNew);
		
		return lsExonClusters;
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