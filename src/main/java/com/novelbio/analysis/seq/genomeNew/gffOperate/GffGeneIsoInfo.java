package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.dataStructure.listOperate.ListComb;
import com.novelbio.database.model.modcopeid.CopedID;

/**
 * 记录该转录本的具体坐标信息,
 * 第一项开始是exon的信息，exon成对出现，为int[2] 
 * 0: 该外显子起点，闭区间，从1开始记数<br>
 * 1: 该外显子终点，闭区间，从1开始记数<br>
 * 按照基因的方向进行排列
 * 如果正向则从小到大排列，且int0&lt;int1
 * 如果反向则从大到小排列，且int0&gt;int1
 * @return
 */
public abstract class GffGeneIsoInfo extends ListAbsSearch<ExonInfo, ListCodAbs<ExonInfo>, ListCodAbsDu<ExonInfo, ListCodAbs<ExonInfo>>>{
	private static final Logger logger = Logger.getLogger(GffGeneIsoInfo.class);
	private static final long serialVersionUID = -6015332335255457620L;
	/** 标记codInExon处在外显子中 */
	public static final int COD_LOC_EXON = 100;
	/** 标记codInExon处在内含子中 */
	public static final int COD_LOC_INTRON = 200;
	/** 标记codInExon不在转录本中  */
	public static final int COD_LOC_OUT = 300;
	/**  标记codInExon处在5UTR中  */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**  标记codInExon处在3UTR中 */
	public static final int COD_LOCUTR_3UTR = 3000;
	/** 标记codInExon不在UTR中 */
	public static final int COD_LOCUTR_OUT = 0;	
	/** 哺乳动物基因间为Tss上游5000bp */
	public static int PROMOTER_INTERGENIC_MAMMUM = 5000;
	/**  哺乳动物为Distal Promoter Tss上游1000bp，以内的就为Proximal Promoter */
	public static int PROMOTER_DISTAL_MAMMUM = 1000;
	/** InterGenic_ */
	public static final String PROMOTER_INTERGENIC_STR = "InterGenic_";
	/**  Distal Promoter_ */
	public static final String PROMOTER_DISTAL_STR = "Distal Promoter_";
	/**  Proximal Promoter_ */
	public static final String PROMOTER_PROXIMAL_STR = "Proximal Promoter_";
	/**  Proximal Promoter_  */
	public static final String PROMOTER_DOWNSTREAMTSS_STR = "Promoter DownStream Of Tss_";
	public static final String TYPE_GENE_MRNA = "mRNA";
	public static final String TYPE_GENE_MIRNA = "miRNA";
	public static final String TYPE_GENE_PSEU_TRANSCRIPT = "pseudogenic_transcript";
	public static final String TYPE_GENE_MRNA_TE = "mRNA_TE_gene";
	public static final String TYPE_GENE_TRNA = "tRNA";
	public static final String TYPE_GENE_SNORNA = "snoRNA";
	public static final String TYPE_GENE_SNRNA = "snRNA";
	public static final String TYPE_GENE_RRNA = "rRNA";
	public static final String TYPE_GENE_NCRNA = "ncRNA";
	public static final String TYPE_GENE_MISCRNA = "miscRNA";
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private String flagTypeGene = TYPE_GENE_MRNA;
	/** 设定基因的转录起点上游长度，默认为0 */
	protected int upTss = 0;
	/** 设定基因的转录起点下游长度，默认为0  */
	protected int downTss=0;
	/**  设定基因的转录终点点上游长度，默认为0 */
	protected int upTes=0;
	/** 设定基因结尾向外延伸的长度，默认为0 */
	protected int downTes=100;
	String chrID = "";
	private int taxID = 0;
	/** 该转录本的ATG的第一个字符坐标，从1开始计数  */
	protected int ATGsite = ListCodAbs.LOC_ORIGINAL;
	/** 该转录本的Coding region end的最后一个字符坐标，从1开始计数 */
	protected int UAGsite = ListCodAbs.LOC_ORIGINAL;
	/** 该转录本的长度 */
	protected int lengthIso = ListCodAbs.LOC_ORIGINAL;
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
	public GffGeneIsoInfo(String IsoName, GffDetailGene gffDetailGene, String geneType) {
		super.listName = IsoName;
		this.flagTypeGene = geneType;
		this.chrID = gffDetailGene.getParentName();
		setTssRegion(gffDetailGene.getTssRegion()[0], gffDetailGene.getTssRegion()[1]);
		setTesRegion(gffDetailGene.getTesRegion()[0], gffDetailGene.getTesRegion()[1]);
	}
	/**
	 * 返回该基因的类型
	 * @return
	 */
	public String getGeneType() {
		return flagTypeGene;
	}
	protected void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	/**
	 * 跟随gffDetailGene的设定
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	protected void setTssRegion(int upTss, int downTss) {
		this.upTss = upTss;
		this.downTss = downTss;
	}
	/**
	 * 跟随gffDetailGene的设定
	 * @param upTes
	 * @param downTes
	 */
	protected void setTesRegion(int upTes, int downTes) {
		this.upTes = upTes;
		this.downTes = downTes;
	}
	/**
	 * coord是否在promoter区域的范围内，从Tss上游UpStreamTSSbp到Tss下游DownStreamTssbp
	 * @return
	 */
	public boolean isCodInIsoTss(int coord)
	{
		int cod2tss = getCod2Tss(coord);
		if (cod2tss >= upTss && cod2tss <= downTss) {
			return true;
		}
		return false;
	}
	/**
	 * coord是否在geneEnd区域的范围内
	 * @return
	 */
	public boolean isCodInIsoGenEnd(int coord)
	{
		int cod2tes = getCod2Tes(coord);
		if (cod2tes >= upTes && cod2tes <= downTes) {
			return true;
		}
		return false;
	}
	/**
	 * coord是否在该转录本包括promoter和geneEnd延长区域的范围内
	 * @return
	 */
	public boolean isCodInIsoExtend(int coord) {
		int codLoc = getCodLoc(coord);
		return (codLoc != COD_LOC_OUT) || isCodInIsoTss(coord) || isCodInIsoGenEnd(coord);
	}
	/**
	 * cod是否在编码区
	 * 如果本转录本是非编码RNA，直接返回false；
	 * @return
	 */
	public boolean isCodInAAregion(int coord)
	{
		if (!ismRNA() || getCodLoc(coord) != GffGeneIsoInfo.COD_LOC_EXON) {
			return false;
		}
		if (getCod2ATG(coord) < 0 || getCod2UAG(coord) > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 没有设定tss和tes的范围
	 * @param IsoName
	 * @param chrID
	 * @param geneType
	 */
	public GffGeneIsoInfo(String IsoName, String chrID, String geneType) {
		super.listName = IsoName;
		this.flagTypeGene = geneType;
		this.chrID = chrID;
	}
	public String getChrID() {
		return chrID;
	}
	protected boolean mRNA = true;
	/**
	 * 是否是mRNA有atg和uag，
	 * 暂时只能使用UCSCgene
	 * @return
	 */
	public boolean ismRNA() {
		return mRNA;
	}
	/**
	 * 只能用于排序好的水稻和拟南芥GFF文件中
	 * 这个要确认
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 在添加exon的时候，如果本CDS与UTR之间是连着的，那么就将本CDS和UTR连在一起，放在一个exon中
	 * 如果不连，就按原来的来
	 */
	protected void addExonGFFCDSUTR(int locStart, int locEnd) {
		/**
		 * 添加外显子，添加在末尾
		 * 添加的时候必须按照基因方向添加，
		 * 正向从小到大添加 且 int0<int1
		 * 反向从大到小添加 且 int0>int1
		 */
		ExonInfo tmpexon = new ExonInfo(getName(), isCis5to3(), locStart, locEnd);
		if (size() > 0) {
			ExonInfo exon = get(size() - 1);
			if (Math.abs(exon.getEndCis() - tmpexon.getStartCis()) == 1) {
				exon.setEndCis(tmpexon.getEndCis());
				return;
			}
		}
		add(tmpexon);
	}
	/**
	 * 给该转录本添加ATG和UAG坐标，<br>
	 * 加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定
	 * 会自动判定输入的起点是否小于已有的atg，终点是否大于已有的uag
	 * 是的话，才会设定，否则就不设定
	 */
	public void setATGUAG(int atg, int uag) {
		if (Math.abs(atg - uag)<=1) {
			mRNA = false;
			atg = Math.min(atg, uag);
			uag = Math.min(atg, uag);
		}
		if (isCis5to3()) {
			if (ATGsite < 0 || ATGsite > Math.min(atg, uag)) {
				ATGsite = Math.min(atg, uag);
			}
			if (UAGsite < 0 || UAGsite < Math.max(atg, uag)) {
				UAGsite = Math.max(atg, uag);
			}
		}
		else {
			if (ATGsite < 0 || ATGsite < Math.max(atg, uag)) {
				ATGsite = Math.max(atg, uag);
			}
			if (UAGsite < 0 || UAGsite > Math.min(atg, uag)) {
				UAGsite = Math.min(atg, uag);
			}
		}
	}
	
	
	/**
	 * 如果是非编码RNA，则将atg和uag设置为最后一位
	 */
	public void setATGUAGncRNA() {
		if (ATGsite < 0 && UAGsite <0) {
			ATGsite = get(size() - 1).getEndCis();
			UAGsite = get(size() - 1).getEndCis();
		}
	}	
	/**
	 * 该转录本的ATG的第一个字符坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getATGsite() {
		return ATGsite;
	}
	/**
	 * 该转录本的Coding region end的最后一个字符坐标，从1开始计数，是闭区间
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
		return (int)get(0).getStartCis();
	}
	/**
	 * 该转录本的Coding region end的最后一个字符坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getTESsite() {
		return get(size() -1).getEndCis();
	}
	public int getExonNum() {
		return size();
	}
	/**
	 * 获得5UTR的长度
	 * @return
	 */
	public int getLenUTR5() {
		return Math.abs(super.getLocDistmRNA(getTSSsite(), this.ATGsite) ) + 1;
	}
	
	/**
	 * 获得3UTR的长度
	 * @return
	 */
	public int getLenUTR3() {
		return Math.abs(super.getLocDistmRNA(this.UAGsite, getTESsite() )) + 1;
	}
	 /**
     * @param num 指定第几个，如果超出，则返回-1000000000, 
     * num 为实际个数。如果num=0则返回全部Exon的长度。
     * @return 
     */
	public int getLenExon(int num) {
		if (num < 0 || num > size()) {
			return -1000000000;
		}
		else if (num == 0) 
		{
			int allExonLength = 0;
			for (int i = 0; i < size(); i++) // 0-0 0-1 1-0 1-1
			{ // 2-1 2-0 1-1 1-0 0-1 0-tss cood
				allExonLength = allExonLength + get(i).getLen();
			}
			return allExonLength;
		}
		else {
			num--;
			return get(num).getLen();
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
		else if (num == 0) 
		{
			int allIntronLength = 0;
			for (int i = 1; i < size(); i++) // 0-0 0-1 1-0 1-1
			{ // 2-1 2-0 1-1 1-0 0-1 0-tss cood
				allIntronLength = allIntronLength + Math.abs(get(i).getStartCis() - get(i-1).getEndCis()) - 1;
			}
		}
		num--;
		return Math.abs(get(num + 1).getStartCis() - get(num).getEndAbs()) - 1;
	}
	/**
	 * 在转录本的哪个位置
	 * 0: 有COD_LOC_EXON，COD_LOC_INTRON，COD_LOC_OUT三种
	 * 1: 有COD_LOCUTR_5UTR，COD_LOCUTR_3UTR，两种
	 * @return
	 */
	private int[] getCodLocInfo(int coord) {
		int codLoc[] = new int[2];
		int ExIntronnum = getLocInEleNum(coord);
		if (ExIntronnum == 0) {
			codLoc[0] = COD_LOC_OUT;
		}
		else if (ExIntronnum > 0) {
			codLoc[0] = COD_LOC_EXON;
			if((coord < ATGsite && isCis5to3()) || (coord > ATGsite && !isCis5to3())){        //坐标小于atg，在5‘UTR中,也是在外显子中
				codLoc[1] = COD_LOCUTR_5UTR;
			}
			else if((coord > UAGsite && isCis5to3()) || (coord < UAGsite && !isCis5to3())){       //大于cds起始区，在3‘UTR中
				codLoc[1] = COD_LOCUTR_3UTR; 
			}
		}
		else {
			codLoc[0] = COD_LOC_INTRON;
		}
		return codLoc;
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
	 * 在转录本的哪个位置
	 * 有COD_LOCUTR_5UTR，COD_LOCUTR_3UTR，两种
	 * @return
	 */
	public int getCodLocUTR(int coord) {
		return getCodLocInfo(coord)[1];
	}
	/**
	 * 坐标到该转录本起点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 * @return
	 */
	public int getCod2Tss(int coord) {
		if (isCis5to3()) {
			return coord - getTSSsite();
		}
		else {
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
		}
		else {
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
		}
		else {
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
		}
		else {
			return -(coord - getUAGsite());
		}
	}
	/**
	 * 使用前先判定在UTR中<br>
	 * 如果坐标在UTR中，坐标距离UTR的起点，注意这个会去除内含子 <br>
	 */
	public int getCod2UTRstartmRNA(int coord) {
		int location = getCodLocUTR(coord);
		if (location == COD_LOCUTR_5UTR) {
			return getLocDistmRNA(getTSSsite(), coord);
		}
		else if (location == COD_LOCUTR_3UTR) {
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
		int location = getCodLocUTR(coord);
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
	 * 不去除内含子的直接用cod2atg/cod2End
	 * 如果不在内含子中，则为很大的负数，大概-10000000
	 */
	public int getCod2ATGmRNA(int coord) {
		return getLocDistmRNA(ATGsite, coord);
	}
	/**
	 * 使用前先判定在Exon中，坐标到UAG的距离，mRNA水平
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
	/**
	 * 返回能和本loc组成一个氨基酸的头部nr的坐标，从1开始计算
	 * @param location
	 * @return
	 */
	public int getLocAAbefore(int location) {
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
		return   -startLen%3;
	}
	/**
	 * 返回能和本loc组成一个氨基酸的尾部nr的坐标，从1开始计算
	 * @param location
	 * @return
	 */
	public int getLocAAend(int location) {
		int startLen = getLocDistmRNA(ATGsite, location);
		return  getLocDistmRNASite(location, 2 - startLen%3);
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
	 * 指定一个起点和一个终点坐标，将这两个坐标间的外显子区域提取出来并返回
	 * 按照基因的方向排序
	 * 大小无所谓，最后返回不依赖 startLoc和EndLoc的大小关系
	 * 如果这两个坐标不在外显子中，则返回null
	 * @return
	 */
	public ArrayList<ExonInfo> getRangeIso(int startLoc, int EndLoc)
	{
		ArrayList<ExonInfo> lsresult = new ArrayList<ExonInfo>();
		int start = 0;
		int end = 0;
		if (isCis5to3()) {
			start = Math.min(startLoc, EndLoc);
			end = Math.max(startLoc, EndLoc);
		}
		else {
			start = Math.max(startLoc, EndLoc);
			end = Math.min(startLoc, EndLoc);
		}
		int exonNumStart = getLocInEleNum(start) - 1;
		int exonNumEnd =getLocInEleNum(end) - 1;
		
		if (exonNumStart < 0 || exonNumEnd < 0) {
			return null;
		}
		
		if (exonNumStart == exonNumEnd) {
			ExonInfo exonInfo = new ExonInfo(getName(), isCis5to3(), start, end);
			lsresult.add(exonInfo);
			return lsresult;
		}
		ExonInfo exonInfo = new ExonInfo(getName(), isCis5to3(), start, get(exonNumStart).getEndCis());
		lsresult.add(exonInfo);
		for (int i = exonNumStart+1; i < exonNumEnd; i++) {
			lsresult.add(get(i));
		}
		ExonInfo exonInfo2 = new ExonInfo(getName(), isCis5to3(), get(exonNumEnd).getStartCis(), end);
		lsresult.add(exonInfo2);
		return lsresult;
	}
	/**
	 * 返回该GeneIsoName所对应的CopedID，因为是NM号所以不需要指定TaxID
	 * @return
	 */
	public CopedID getCopedID() {
		return new CopedID(getName(), taxID);
	}
	/**
	 * 文字形式的定位描述
	 * @return
	 * null: 不在该转录本内
	 */
	public String getCodLocStr(int coord) {
		String result = "gene_position:";
		if ( isCis5to3()) {
			result = result + "forward ";
		}
		else {
			result = result + "reverse ";
		}
		int codLoc = getCodLoc(coord);
		//promoter\
		if (isCodInIsoTss(coord) && codLoc == COD_LOC_OUT) {
			if (getCod2Tss(coord) > PROMOTER_INTERGENIC_MAMMUM) {
				result = PROMOTER_INTERGENIC_STR;
			}
			else if (getCod2Tss(coord) > PROMOTER_DISTAL_MAMMUM) {
				result = PROMOTER_DISTAL_STR;
			}
			else {
				result = PROMOTER_PROXIMAL_STR;;
			}
		}
		else if (isCodInIsoTss(coord) && codLoc != COD_LOC_OUT) {
			result = PROMOTER_DOWNSTREAMTSS_STR;
		}
		
		result = result + "Distance_to_Tss_is:" + Math.abs(getCod2Tss(coord)) + " ";
		//UTR
		if (getCodLocUTR(coord) == COD_LOCUTR_5UTR) {
			result = result + "5UTR_";
		}
		else if (getCodLocUTR(coord) == COD_LOCUTR_3UTR) {
			result = result + "3UTR_";
		}
		//exon intron
		if (codLoc == COD_LOC_EXON) {
			result = result + "Exon:exon_Position_Number_is:" + getLocInEleNum(coord);
		}
		else if (codLoc == COD_LOC_INTRON) {
			result = result + "Intron_intron_Position_Number_is:" + getLocInEleNum(coord);
		}
		//gene end
		if (isCodInIsoGenEnd(coord)) {
			result = result + "Distance_to_GeneEnd: "+ getCod2Tes(coord);
		}
		return result;
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
	public String getCodLocStrFilter(int coord, boolean filterTss, boolean filterGenEnd, 
			boolean filterGeneBody,boolean filter5UTR, boolean filter3UTR,boolean filterExon, boolean filterIntron) {
		boolean filter = false;
		if (filterTss == true) {
			if (isCodInIsoTss(coord)) {
				filter = true;
			}
		}
		if (filterGenEnd == true) {
			if (isCodInIsoGenEnd(coord)) {
				filter = true;
			}
		}
		if (filterGeneBody && getCodLoc(coord) != COD_LOC_OUT) {
			filter = true;
		}
		else if (filter5UTR && getCodLocUTR(coord) == COD_LOCUTR_5UTR) {
			filter = true;
		}
		else if (filter3UTR && getCodLocUTR(coord) == COD_LOCUTR_3UTR) {
			filter = true;
		}
		else if (filterExon && getCodLoc(coord) == COD_LOC_EXON) {
			filter = true;
		}
		else if (filterIntron && getCodLoc(coord) == COD_LOC_INTRON) {
			filter = true;
		}
		
		if (filter) {
			return getCodLocStr(coord);
		}
		else {
			return null;
		}
	}
	/**
	 * 如果两个转录本方向不一致，则不能进行比较
	 * 比较两个转录本之间的差距有多大
	 * @param gffGeneIsoInfo
	 * @return
	 */
	public  ListComb<ExonInfo> compIsoLs(GffGeneIsoInfo gffGeneIsoInfo) {
		if (this.isCis5to3() != gffGeneIsoInfo.isCis5to3()) {
			return null;
		}
		ListComb<ExonInfo> lsResult = new ListComb<ExonInfo>();
		lsResult.addListAbs(this);
		lsResult.addListAbs(gffGeneIsoInfo);
		return lsResult;
	}
	
	/**
	 * 假设是安顺序添加的ID
	 * 这个要确认
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 */
	protected void addExon(int locStart, int locEnd) {
		ExonInfo exonInfo = new ExonInfo(getName(),isCis5to3(), locStart, locEnd);
		if (size() == 0) {
			add(exonInfo);
			return;
		}
		if ((isCis5to3() && exonInfo.getStartAbs() >= get(size() - 1).getEndAbs())
		|| 
		(!isCis5to3() && exonInfo.getEndAbs() <= get(size() - 1).getStartAbs())
		) {
			add(exonInfo);
		}
		else if ((isCis5to3() && exonInfo.getEndAbs() <= get(0).getStartAbs())
				|| 
				(!isCis5to3() && exonInfo.getStartAbs() >= get(size() - 1).getEndAbs())
		){
			add(0,exonInfo);
		}
		else {
			logger.error("NCBI的Gff文件有问题，其exon会窜位，本次添加exon出错，请check: " + locStart + " " + locEnd);
		}
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
	 * 返回该基因的GTF格式文件，末尾有换行符
	 * @param title 该GTF文件的名称
	 * @return
	 */
	protected String getGTFformat(String geneID, String title)
	{
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		String genetitle = getGTFformatExon(geneID, title,strand);
		return genetitle;
	}
	/**
	 * 返回该基因的GTF格式文件，末尾有换行符
	 * @param title 该GTF文件的名称
	 * @return
	 */
	protected String getGFFformat(String geneID, String title)
	{
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		String genetitle = getGFFformatExonMISO(geneID, title,strand);
		return genetitle;
	}
	protected abstract String getGTFformatExon(String geneID, String title, String strand);
	protected abstract String getGFFformatExonMISO(String geneID, String title, String strand);

	
	/**
	 * 可能不是很精确
	 * 返回距离Tss的一系列坐标的实际坐标
	 * @param is 相对于 TSS的坐标信息，譬如-200到-100，-100到100，100到200等，每一组就是一个int[2]，注意int[1]必须小于int[2]
	 * @return
	 * 最后获得的结果正向int[0] < int[1]
	 * 反向 int[0] > int[1]
	 */
	public ArrayList<int[]> getRegionNearTss(Collection<int[]> isList)
	{
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
		//物种，起点终点，ATG，UAG，外显子长度，转录本名字等都一致
		boolean flag =  this.getTaxID() == otherObj.getTaxID() && this.getChrID().equals(otherObj.getChrID()) && this.getATGsite() == otherObj.getATGsite()
		&& this.getUAGsite() == otherObj.getUAGsite() && this.getTSSsite() == otherObj.getTSSsite()
		&& this.getListLen() == otherObj.getListLen();
		if (flag && compIso(otherObj) ) {
			return true;
		}
		return false;
	}
	
	/**
	 * 重写hash
	 * @return
	 */
	public int hashcode()
	{
		String info = this.getTaxID() + "//" + this.getChrID() + "//" + this.getATGsite() + "//" + this.getUAGsite() + "//" + this.getTSSsite() + "//" + this.getListLen();
		for (ExonInfo exonInfo : this) {
			info = info + "@@"+exonInfo.getName();
		}
		return   info.hashCode();
	}
	
	/**
	 * 获得具体的编码序列
	 * 没有结果就返回new list-exonInfo
	 * @return
	 */
	public ArrayList<ExonInfo> getIsoInfoCDS()
	{
		if (ATGsite == UAGsite) {
			return new ArrayList<ExonInfo>();
		}

		ArrayList<ExonInfo> lsresult = new ArrayList<ExonInfo>();
		int numAtg = getLocInEleNum(ATGsite) - 1;
		int numUag = getLocInEleNum(UAGsite) - 1;
		for (int i = 0; i < size(); i++) {
			ExonInfo exonTmp = get(i);
			if (i < numAtg) {
				continue;
			}
			else if (i > numUag) {
				break;
			}
			else if (i == numAtg) {
				ExonInfo exonFinalTmp = new ExonInfo();
				exonFinalTmp.setParentName(getName());
				exonFinalTmp.setCis5to3(isCis5to3());
				exonFinalTmp.setStartCis(ATGsite);
				if (numAtg == numUag) {
					exonFinalTmp.setEndCis(UAGsite);
					lsresult.add(exonFinalTmp);
					break;
				}
				else {
					exonFinalTmp.setEndCis(exonTmp.getEndCis());
					lsresult.add(exonFinalTmp);
				}
			}
			else if (i == numUag) {
				ExonInfo exonFinalTmp = new ExonInfo(getName(), isCis5to3(), exonTmp.getStartCis(), UAGsite);
				lsresult.add(exonFinalTmp);
				break;
			}
			else {
				lsresult.add(exonTmp);
			}
		}
		return lsresult;
	}
	
	/**
	 * 给定nr位点，换算为距离ATG多少aa位置
	 * 直接给定nr的实际位点
	 */
	public int getAAsiteNum(int codSite)
	{
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
	public GffGeneIsoInfo clone()
	{
		GffGeneIsoInfo result = null;
		result = (GffGeneIsoInfo) super.clone();
		result.ATGsite = ATGsite;
		result.chrID = chrID;
		result.downTes = downTes;
		result.downTss = downTss;
		result.flagTypeGene = flagTypeGene;
		result.lengthIso = lengthIso;
		result.mRNA = mRNA;
		result.taxID = taxID;
		result.UAGsite = UAGsite;
		result.upTes = upTes;
		result.upTss = upTss;
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
}