package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.CompSubArray;
import com.novelbio.base.dataStructure.CompSubArrayCluster;
import com.novelbio.database.model.modcopeid.CopedID;

/**
 * 记录GffGene中的转录本信息
 * @author zong0jie
 *
 */
public abstract class GffGeneIsoInfo {
	/**
	 * 标记codInExon处在外显子中
	 */
	public static final int COD_LOC_EXON = 100;
	/**
	 * 标记codInExon处在内含子中
	 */
	public static final int COD_LOC_INTRON = 200;
	/**
	 * 标记codInExon不在转录本中
	 */
	public static final int COD_LOC_OUT = 300;
	/**
	 * 标记codInExon处在5UTR中
	 */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**
	 * 标记codInExon处在3UTR中
	 */
	public static final int COD_LOCUTR_3UTR = 3000;
	/**
	 * 标记codInExon不在UTR中
	 */
	public static final int COD_LOCUTR_OUT = 0;	
	/**
	 * 哺乳动物基因间为Tss上游5000bp
	 */
	public static int PROMOTER_INTERGENIC_MAMMUM = 5000;
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
	/**
	 * 哺乳动物为Distal Promoter Tss上游1000bp，以内的就为Proximal Promoter
	 */
	public static int PROMOTER_DISTAL_MAMMUM = 1000;
	/**
	 * InterGenic_
	 */
	public static final String PROMOTER_INTERGENIC_STR = "InterGenic_";
	/**
	 * Distal Promoter_
	 */
	public static final String PROMOTER_DISTAL_STR = "Distal Promoter_";
	/**
	 * Proximal Promoter_
	 */
	public static final String PROMOTER_PROXIMAL_STR = "Proximal Promoter_";
	/**
	 * Proximal Promoter_
	 */
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
	
	private String flagTypeGene = TYPE_GENE_MRNA;
	
	String chrID = "";
	/**
	 * 返回该基因的类型
	 * @return
	 */
	public String getGeneType() {
		return flagTypeGene;
	}
	private int taxID = 0;
	protected void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	/**
	 * 设定基因的转录起点上游长度，默认为3000bp
	 */
	protected static int UpStreamTSSbp = 3000;
	
	/**
	 * 设定基因的转录起点下游长度，默认为2000bp
	 */
	protected static int DownStreamTssbp=2000;
	/**
	 * 设定基因结尾向外延伸的长度，默认为100bp
	 * 就是说将基因结束点向后延伸100bp，认为是3’UTR
	 * 那么在统计peak区域的时候，如果这段区域里面没有被peak所覆盖，则不统计该区域内reads的情况
	 */
	protected static int GeneEnd3UTR=100;
	/**
	 * 设定基因的转录起点终点位置信息
	 * @param UpStreamTSSbp 设定基因的转录起点上游长度，默认为3000bp
	 * @param DownStreamTssbp 设定基因的转录起点下游长度，默认为2000bp
	 * @param GeneEnd3UTR 设定基因结尾向外延伸的长度，默认为100bp
	 */
	protected static void setCodLocation(int upStreamTSSbp, int downStreamTssbp, int geneEnd3UTR) {
		UpStreamTSSbp = upStreamTSSbp;
		DownStreamTssbp = downStreamTssbp;
		GeneEnd3UTR = geneEnd3UTR;
	}
	
	/**
	 * coord是否在promoter区域的范围内，从Tss上游UpStreamTSSbp到Tss下游DownStreamTssbp
	 * @return
	 */
	public boolean isCodInIsoTss()
	{
		if (codLoc == COD_LOC_OUT && getCod2Tss() < 0 && Math.abs(getCod2Tss()) <= UpStreamTSSbp ) {
			return true;
		}
		else if ( codLoc != COD_LOC_OUT && getCod2Tss() > 0 && Math.abs(getCod2Tss()) <= DownStreamTssbp ) {
			return true;
		}
		return false;
	}
	/**
	 * coord是否在gene外，并且在geneEnd延长区域的范围内
	 * @return
	 */
	public boolean isCodInIsoGenEnd()
	{
		if (codLoc == COD_LOC_OUT && getCod2Tes() > 0 && Math.abs(getCod2Tes()) <= GeneEnd3UTR ) {
			return true;
		}
		return false;
	}
	/**
	 * coord是否在该转录本包括promoter和geneEnd延长区域的范围内
	 * @return
	 */
	public boolean isCodInIsoExtend() {
		return (codLoc != COD_LOC_OUT) || isCodInIsoTss() || isCodInIsoGenEnd();
	}
	/**
	 * cod是否在编码区
	 * 如果本转录本是非编码RNA，直接返回false；
	 * @return
	 */
	public boolean isCodInAAregion()
	{
		if (!ismRNA() || getCodLoc() != GffGeneIsoInfo.COD_LOC_EXON) {
			return false;
		}
		if (cod2ATG < 0 || cod2UAG > 0) {
			return false;
		}
		return true;
		
		
	}
	
	private static final Logger logger = Logger.getLogger(GffGeneIsoInfo.class);
	
	public GffGeneIsoInfo(String IsoName, GffDetailGene gffDetailGene, String geneType) {
		this.IsoName = IsoName;
		this.flagTypeGene = geneType;
		this.coord = gffDetailGene.getCoord();
		if (this.coord > GffCodAbs.LOC_ORIGINAL) {
			searchCoord();
		}
		this.chrID = gffDetailGene.getChrID();
	}
	public GffGeneIsoInfo(String IsoName, String chrID, int coord, String geneType) {
		this.IsoName = IsoName;
		this.flagTypeGene = geneType;
		this.coord = coord;
		if (this.coord > GffCodAbs.LOC_ORIGINAL) {
			searchCoord();
		}
		this.chrID = chrID;
	}
	
	public GffGeneIsoInfo(String IsoName, String chrID, String geneType) {
		this.IsoName = IsoName;
		this.flagTypeGene = geneType;
		this.chrID = chrID;
	}
	
//	/**
//	 * 仅仅初始化给查找时用
//	 * @param IsoName
//	 * @param lsIsoform
//	 */
//	protected GffGeneIsoInfo(String IsoName, ArrayList<int[]> lsIsoform, boolean cis5to3) {
//		this.IsoName = IsoName;
//		this.lsIsoform = lsIsoform;
//		this.cis5to3 = cis5to3;
//	}

	GffDetailGene gffDetailGene;
//	public GffDetailGene getThisGffDetailGene() {
//		return gffDetailGene;
//	}
	public String getChrID()
	{
//		return gffDetailGene.getChrID();
		return chrID;
	}
	public abstract boolean isCis5to3();
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
	 * 该转录本的ATG的第一个字符坐标，从1开始计数
	 */
	protected int ATGsite = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 该转录本的Coding region end的最后一个字符坐标，从1开始计数
	 */
	protected int UAGsite = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 转录本的名字
	 */
	protected String IsoName = "";

	  /**
     * 转录本中外显子的具体信息<br>
     * exon成对出现，第一个exon坐标是该转录本的起点，最后一个exon坐标是该转录本的终点，正向从小到大排列且int[0]<int[1]<br>
     * 反向从大到小排列且int[0]>int[1]<br>
     */
	protected ArrayList<int[]> lsIsoform = new ArrayList<int[]>();

	/**
	 * 该转录本的长度
	 */
	protected int lengthIso = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 给转录本添加exon坐标，当基因为反向时UCSC的exon的格式是 <br>
	 * NM_021170	chr1	-	934341	935552	934438	935353	4	934341,934905,935071,935245,	934812,934993,935167,935552, <br>
	 * 那么exon为934341,934905,935071,935245和934812,934993,935167,935552, <br>
	 * 是从小到大排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 */
	protected abstract void addExonUCSC(int locStart, int locEnd);
	/**
	 * 这个要确认
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 */
	protected abstract void addExonGFF(int locStart, int locEnd);
	/**
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
		int[] tmpexon = new int[2];
		if (isCis5to3()) {
			tmpexon[0] = Math.min(locStart, locEnd);
			tmpexon[1] = Math.max(locStart, locEnd);
		}
		else {
			tmpexon[0] = Math.max(locStart, locEnd);
			tmpexon[1] = Math.min(locStart, locEnd);
		}
		if (lsIsoform.size() > 0) {
			int[] exon = lsIsoform.get(lsIsoform.size() - 1);
			if (Math.abs(exon[1] - tmpexon[0]) == 1) {
				exon[1] = tmpexon[1];
				return;
			}
		}
		lsIsoform.add(tmpexon);
	}
	/**
	 * 返回该转录本的名称
	 * @return
	 */
	public String getIsoName() {
		return IsoName;
	}
	/**
	 * 返回该转录本的具体坐标信息,
	 * 第一项开始是exon的信息，exon成对出现，为int[2] 
	 * 0: 该外显子起点，闭区间，从1开始记数<br>
	 * 1: 该外显子终点，闭区间，从1开始记数<br>
	 * 按照基因的方向进行排列
	 * 如果正向则从小到大排列，且int0&lt;int1
	 * 如果反向则从大到小排列，且int0&gt;int1
	 * @return
	 */
	public  ArrayList<int[]> getIsoInfo() {
		return lsIsoform;
	}
	
 
	
	
	
	
	/**
	 * 该转录本的ATG的第一个字符坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getATGSsite() {
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
		return lsIsoform.get(0)[0];
	}
	/**
	 * 该转录本的Coding region end的最后一个字符坐标，从1开始计数，是闭区间
	 * @return
	 */
	public int getTESsite() {
		return lsIsoform.get(lsIsoform.size() -1)[1];
		
	}
	public int getExonNum()
	{
		return lsIsoform.size();
	}
	/**
	 * 获得5UTR的长度
	 * @return
	 */
	public abstract int getLenUTR5();
	
	/**
	 * 获得3UTR的长度
	 * @return
	 */
	public abstract int getLenUTR3();
	 /**
     * @param num 指定第几个，如果超出，则返回-1000000000, 
     * num 为实际个数。如果num=0则返回全部Exon的长度。
     * @return 
     */
	public int getLenExon(int num)
	{
		if (num < 0 || num > lsIsoform.size()) {
			return -1000000000;
		}
		else if (num == 0) 
		{
			int allExonLength = 0;
			for (int i = 0; i < lsIsoform.size(); i++) // 0-0 0-1 1-0 1-1
			{ // 2-1 2-0 1-1 1-0 0-1 0-tss cood
				allExonLength = allExonLength + Math.abs(lsIsoform.get(i)[1] - lsIsoform.get(i)[0]) + 1;
			}
			return allExonLength;
		}
		else {
			num--;
			return Math.abs(lsIsoform.get(num)[1] - lsIsoform.get(num)[0]) + 1;
		}
	}
	 /**
     * @param num 指定第几个，如果超出，则返回-1000000000, 
     * num 为实际个数。如果num=0则返回全部Intron的长度。
     * @return 
     */
	public int getLenIntron(int num)
	{
		if (num < 0 || num > lsIsoform.size()) {
			return -1000000000;
		}
		else if (num == 0) 
		{
			int allIntronLength = 0;
			for (int i = 1; i < lsIsoform.size(); i++) // 0-0 0-1 1-0 1-1
			{ // 2-1 2-0 1-1 1-0 0-1 0-tss cood
				allIntronLength = allIntronLength + Math.abs(lsIsoform.get(i)[1] - lsIsoform.get(i)[0]) - 1;
			}
		}
		num--;
		return Math.abs(lsIsoform.get(num + 1)[0] - lsIsoform.get(num)[1]) - 1;
	}
	
	/**
	 * 坐标
	 */
	protected int coord = GffCodAbs.LOC_ORIGINAL;
	
	/**
	 * 坐标到该转录本起点的距离，考虑正反向
	 * 坐标在起点上游为负数，下游为正数
	 */
	protected int cod2TSS = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 坐标到该转录本终点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 */
	protected int cod2TES = GffCodAbs.LOC_ORIGINAL;
	
	/**
	 * 只有当坐标处在外显子中才有距离，不包含内含子
	 * 坐标到该转录本起点的距离，只看mRNA水平，考虑正反向
	 * 只有当坐标处在外显子中才有距离，不包含内含子\
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
	 */
	protected int cod2TSSmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 只有当坐标处在外显子中才有距离，不包含内含子
	 * 坐标到该转录本终点的距离，只看mRNA水平，考虑正反向
	 * 不去除内含子的直接用getCod2UAG
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
	 */
	protected int cod2TESmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 只有当坐标处在外显子中才有距离，不包含内含子<br>
	 * 坐标到该转录本atg的距离，只看mRNA水平，考虑正反向<br>
	 * 坐标在起点上游为负数，下游为正数<br>
	 */
	protected int cod2ATGmRNA= GffCodAbs.LOC_ORIGINAL;
	/**
	 * 只有当坐标处在外显子中才有距离，不包含内含子<br>
	 * 坐标到该转录本uag的距离，只看mRNA水平，考虑正反向<br>
	 * 坐标在终点上游为负数，下游为正数<br>
	 * Cnn nn  nuaG 距离为8
	 */
	protected int cod2UAGmRNA = GffCodAbs.LOC_ORIGINAL;
	
	
	/**
	 * 如果坐标在外显子/内含子中，
	 * 坐标与该外显子/内含子起点的距离
	 * 都为正数
	 */
	protected int cod2ExInStart = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 如果坐标在外显子/内含子中，
	 * 坐标与该外显子/内含子终点的距离
	 * 都为正数
	 */
	protected int cod2ExInEnd = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 包含内含子
	 * 坐标到ATG的距离，考虑正反向.
	 * 在ATG上游为负数，下游为正数
	 * @return
	 */
	protected int cod2ATG = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 包含内含子
	 * 坐标到UAG的距离，考虑正反向.
	 * 在UAG上游为负数，下游为正数
	 * @return
	 */
	protected int cod2UAG = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 坐标在第几个外显子或内含子中，如果不在就为负数
	 * 实际数目，从1开始记数
	 */
	protected int numExIntron = 0;
	
	/**
	 * 坐标在5UTR、3UTR还是不在
	 */
	protected int codLocUTR = COD_LOCUTR_OUT;
	/**
	 * 使用前先判定在UTR中
	 * 如果坐标在UTR中，坐标距离UTR的起点，注意这个会去除内含子
	 * 不去除内含子的直接用cod2start/cod2cdsEnd
	 */
	protected int cod2UTRstartmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 使用前先判定在UTR中
	 * 如果坐标在UTR中，坐标距离UTR的终点，注意这个会去除内含子
	 * 不去除内含子的直接用cod2atg/cod2End
	 */
	protected int cod2UTRendmRNA = GffCodAbs.LOC_ORIGINAL;

	/**
	 * 坐标在外显子、内含子还是在该转录本外
	 * 与codLocExon和codLocIntron比较即可
	 */
	protected int codLoc = 0;
	
	/**
	 * 在转录本的哪个位置
	 * 有COD_LOC_EXON，COD_LOC_INTRON，COD_LOC_OUT三种
	 * @return
	 */
	public int getCodLoc() {
		return codLoc;
	}
	/**
	 * 在转录本的哪个位置
	 * 有COD_LOCUTR_5UTR，COD_LOCUTR_3UTR，两种
	 * @return
	 */
	public int getCodLocUTR() {
		return codLocUTR;
	}
	/**
	 * 坐标到该转录本起点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 * @return
	 */
	public int getCod2Tss() {
		return cod2TSS;
	}
	/**
	 * 坐标到该转录本终点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 * @return
	 */
	public int getCod2Tes() {
		return cod2TES;
	}
	public int getCoord() {
		return coord;
	}
	public void setCoord(int coord) {
	
		this.coord = coord;
		searchCoord();
	}
	/**
	 * 坐标在第几个外显子或内含子中，如果不在就为0
	 * 实际数目，从1开始记数
	 * @return
	 */
	public int getCodExInNum() {
		return numExIntron;
	}
	/**
	 * 坐标到该外显子/内含子起点的距离，考虑正反向
	 * @return
	 */
	public int getCod2ExInStart() {
		return cod2ExInStart;
	}
	/**
	 * 坐标到该外显子/内含子终点的距离，考虑正反向
	 * @return
	 */
	public int getCod2ExInEnd() {
		return cod2ExInEnd;
	}
	/**
	 * 坐标到ATG的距离，考虑正反向.
	 * 在ATG上游为负数，下游为正数
	 * @return
	 */
	public int getCod2ATG() {
		return cod2ATG;
	}
	/**
	 * 坐标到UAG的最后一个碱基的距离，考虑正反向.
	 * 在UAG上游为负数，下游为正数
	 * @return
	 */
	public int getCod2UAG() {
		return cod2UAG;
	}
	/**
	 * 使用前先判定在UTR中<br>
	 * 如果坐标在UTR中，坐标距离UTR的起点，注意这个会去除内含子 <br>
	 */
	public int getCod2UTRstartmRNA() {
		return cod2UTRstartmRNA;
	}
	/**
	 * 使用前先判定在UTR中<br>
	 * 如果坐标在UTR中，坐标距离UTR的终点，注意这个会去除内含子<br>
	 */
	public int getCod2UTRendmRNA() {
		return cod2UTRendmRNA;
	}
	/**
	 * 使用前先判定在Exon中，坐标到该转录本atg的距离
	 * 不去除内含子的直接用cod2atg/cod2End
	 * 如果不在内含子中，则为很大的负数，大概-10000000
	 */
	public int getCod2ATGmRNA() {
		return cod2ATGmRNA;
	}
	/**
	 * 使用前先判定在Exon中，坐标到UAG的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 坐标在终点上游为负数，下游为正数<br>
	 * 如果不在内含子中，则为很大的负数，大概-10000000
	 */
	public int getCod2UAGmRNA() {
		return cod2UAGmRNA;
	}
	/**
	 * 使用前先判定在Exon中，坐标到TSS的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 只有当坐标处在外显子中才有距离，不包含内含子\
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
	 */
	public int getCod2TSSmRNA() {
		return cod2TSSmRNA;
	}
	/**
	 * 使用前先判定在Exon中，坐标到TES的距离，mRNA水平
	 * 不去除内含子的直接用getCod2UAG
	 * 因为cod在外显子中，所以肯定在tES上游，所以该值始终为负数
	 */
	public int getCod2TESmRNA() {
		return cod2TESmRNA;
	}

	
	/**
	 */
	private void searchCoord()
	{
		init();
		codSearchNum();
		if (codLocUTR == COD_LOCUTR_5UTR) {
			setCod2UTR5();
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			setCod2UTR3();
		}
		if (codLoc == COD_LOC_EXON) {
			setCod2StartEndmRNA();
			setCod2StartEndCDS();
		}
	}
	/**
	 * 初始化变量
	 */
	private void init()
	{
//		 coord = GffCodAbs.LOC_ORIGINAL;
		 cod2TSS = GffCodAbs.LOC_ORIGINAL;
		 cod2TES = GffCodAbs.LOC_ORIGINAL;
		 cod2TSSmRNA = GffCodAbs.LOC_ORIGINAL;
		 cod2TESmRNA = GffCodAbs.LOC_ORIGINAL;
		 cod2ATGmRNA= GffCodAbs.LOC_ORIGINAL;
		 cod2UAGmRNA = GffCodAbs.LOC_ORIGINAL;
		 cod2ExInStart = GffCodAbs.LOC_ORIGINAL;
		 cod2ExInEnd = GffCodAbs.LOC_ORIGINAL;
		 cod2ATG = GffCodAbs.LOC_ORIGINAL;
		 cod2UAG = GffCodAbs.LOC_ORIGINAL;
		 numExIntron = -1;
		 codLocUTR = COD_LOCUTR_OUT;
		 cod2UTRstartmRNA = GffCodAbs.LOC_ORIGINAL;
		 cod2UTRendmRNA = GffCodAbs.LOC_ORIGINAL;
		 codLoc = 0;
	}
	/**
	 * 第一个计算的，计算坐标与本 外显子/内含子 的 起点/终点 的距离
	 */
	protected abstract void setCod2ExInStartEnd();
	/**
	 * 查找坐标在第几个外显子或内含子中
	 * 并且指出在是在外显子还是内含子
	 * 是否在UTR中
	 * 同时填充		
	 * cod2ATG
		cod2cdsEnd 
		cod2start 
		cod2end 
		等
	 */
	protected void codSearchNum()
	{
		int ExIntronnum = getLocExInNum(coord);
		if (ExIntronnum == 0) {
			codLoc = COD_LOC_OUT;
		}
		else if (ExIntronnum > 0) {
			codLoc = COD_LOC_EXON;
			if((coord < ATGsite && isCis5to3()) || (coord > ATGsite && !isCis5to3())){        //坐标小于atg，在5‘UTR中,也是在外显子中
				codLocUTR = COD_LOCUTR_5UTR;
			}
			else if((coord > UAGsite && isCis5to3()) || (coord < UAGsite && !isCis5to3())){       //大于cds起始区，在3‘UTR中
				codLocUTR = COD_LOCUTR_3UTR; 
			}
		}
		else {
			codLoc = COD_LOC_INTRON;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		setCod2SiteAbs();
		cod2ExInStart = getLoc2ExInStart(coord);
		cod2ExInEnd = getLoc2ExInEnd(coord);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		numExIntron = Math.abs(ExIntronnum);
	}
	protected abstract void setCod2SiteAbs();
	
	/**
	 * 当坐标在5UTR的外显子中时使用
	 * 方向为正向
	 */
	protected abstract void setCod2UTR5();
	
	/**
	 * 当坐标在3UTR的外显子中时使用
	 * 方向为正向
	 */
	protected abstract void setCod2UTR3();
	/**
	 * 当在EXON中时才使用，看cod与本mRNA的起点和终点的距离
	 * 不包括内含子
	 */
	protected abstract void setCod2StartEndmRNA();
	/**
	 * 当在EXON中时才使用，看cod与本mRNA的atg和uag的距离
	 * 不包括内含子
	 */
	protected abstract void setCod2StartEndCDS();

	/**
	 * 保存某个坐标和所在的内含子外显子数目
	 */
	HashMap<Integer, Integer> hashLocExInNum;
	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 为实际数目
	 * 都不在为0
	 * @return
	 */
	protected abstract int getLocExInNum(int location);
	/**
	 * 保存某个坐标到所在的内含子/外显子起点的距离
	 */
	HashMap<Integer, Integer> hashLocExInStart;
	/**
	 * 坐标到外显子/内含子 起点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	protected abstract int getLoc2ExInStart(int location);
	/**
	 * 保存某个坐标到所在的内含子/外显子终点的距离
	 */
	HashMap<Integer, Integer> hashLocExInEnd;
	/**
	 * 坐标到外显子/内含子 终点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	protected abstract int getLoc2ExInEnd(int location);
	/**
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param location 坐标
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 */
	public abstract int getLocDistmRNASite(int location, int mRNAnum);
	
	/**
	 * 返回能和本loc组成一个氨基酸的头部nr的坐标，从1开始计算
	 * @param location
	 * @return
	 */
	public int getLocAAbefore(int location) {
		int startLen = getLocDistmRNA(location,ATGsite);
		return  getLocDistmRNASite(location, -startLen%3);
	}
	/**
	 * 返回能和本loc组成一个氨基酸的头部nr的偏移，也就是向前偏移几个碱基
	 * 恒为负数
	 * @param location
	 * @return
	 */
	public int getLocAAbeforeBias(int location) {
		int startLen = getLocDistmRNA(location,ATGsite);
		return   -startLen%3;
	}
	/**
	 * 返回能和本loc组成一个氨基酸的尾部nr的坐标，从1开始计算
	 * @param location
	 * @return
	 */
	public int getLocAAend(int location)
	{
		int startLen = getLocDistmRNA(location,ATGsite);
		return  getLocDistmRNASite(location, 2 - startLen%3);
	}
	/**
	 * 两个坐标之间的距离，mRNA层面，当loc1在loc2上游时，返回负数，当loc1在loc2下游时，返回正数
	 * 要求这两个坐标都在exon上.如果不符合，则返回GffCodAbs.LOC_ORIGINAL
	 * @param loc1 第一个坐标
	 * @param loc2 第二个坐标
	 */
	public int getLocDistmRNA(int loc1, int loc2)
	{
		int locSmall = 0; int locBig = 0;
		if (isCis5to3()) {
			locSmall = Math.min(loc1, loc2);  locBig = Math.max(loc1, loc2);
		}
		else {
			locSmall = Math.max(loc1, loc2);  locBig = Math.min(loc1, loc2);
		}
		int locSmallExInNum = getLocExInNum(locSmall); int locBigExInNum = getLocExInNum(locBig);
		
		int distance = GffCodAbs.LOC_ORIGINAL;
		
		if (locSmallExInNum <= 0 || locBigExInNum <= 0) 
			return distance;
		
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locBig - locSmall;
		}
		else {
			distance = getLoc2ExInEnd(locSmall) + getLoc2ExInStart(locBig) + 1;
			for (int i = locSmallExInNum + 1; i <= locBigExInNum - 1; i++) {
				distance = distance + Math.abs(lsIsoform.get(i)[0] -lsIsoform.get(i)[1]) + 1;
			}
		}
		
		if ((isCis5to3() && loc1 < loc2) || (!isCis5to3() && loc1 > loc2)) {
			return -Math.abs(distance);
		}
		return Math.abs(distance);
	}
	
	/**
	 * 指定一个起点和一个终点坐标，将这两个坐标间的外显子区域提取出来并返回
	 * 按照基因的方向排序
	 * 大小无所谓，最后返回不依赖 startLoc和EndLoc的大小关系
	 * 如果这两个坐标不在外显子中，则返回null
	 * @return
	 */
	public ArrayList<int[]> getRangeIso(int startLoc, int EndLoc)
	{

		ArrayList<int[]> lsresult = new ArrayList<int[]>();
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
		
		int exonNumStart = getLocExInNum(start) - 1;
		int exonNumEnd =getLocExInNum(end) - 1;
		
		if (exonNumStart < 0 || exonNumEnd < 0) {
			return null;
			
		}
		
		if (exonNumStart == exonNumEnd) {
			int[] exonSub = new int[2];
			exonSub[0] = start; exonSub[1] = end;
			lsresult.add(exonSub);
			return lsresult;
		}
		
		int[] exonSub = new int[2];
		exonSub[0] = start; exonSub[1] = lsIsoform.get(exonNumStart)[1];
		lsresult.add(exonSub);
		for (int i = exonNumStart+1; i < exonNumEnd; i++) {
			lsresult.add(lsIsoform.get(i));
		}
		exonSub = new int[2];
		exonSub[0] = lsIsoform.get(exonNumEnd)[0]; exonSub[1] = end; 
		lsresult.add(exonSub);
		return lsresult;
	}
	/**
	 * 返回该GeneIsoName所对应的CopedID，因为是NM号所以不需要指定TaxID
	 * @return
	 */
	public CopedID getCopedID()
	{
		return new CopedID(getIsoName(), taxID, false);
	}
	
	/**
	 * 文字形式的定位描述
	 * @return
	 * null: 不在该转录本内
	 */
	public String getCodLocStr() {
		String result = "gene_position:";
		if ( isCis5to3()) {
			result = result + "forward ";
		}
		else {
			result = result + "reverse ";
		}
		if (!isCodInIsoExtend()) {
			return null;
		}
		//promoter
		if (isCodInIsoTss() && getCodLoc() == COD_LOC_OUT) {
			if (cod2TSS > PROMOTER_INTERGENIC_MAMMUM) {
				result = PROMOTER_INTERGENIC_STR;
			}
			else if (cod2TSS > PROMOTER_DISTAL_MAMMUM) {
				result = PROMOTER_DISTAL_STR;
			}
			else {
				result = PROMOTER_PROXIMAL_STR;;
			}
		}
		else if (isCodInIsoTss() && getCodLoc() != COD_LOC_OUT) {
			result = PROMOTER_DOWNSTREAMTSS_STR;
		}
		
		result = result + "Distance_to_Tss_is:" + Math.abs(cod2TSS) + " ";
		//UTR
		if (codLocUTR == COD_LOCUTR_5UTR) {
			result = result + "5UTR_";
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			result = result + "3UTR_";
		}
		//exon intron
		if (codLoc == COD_LOC_EXON) {
			result = result + "Exon:exon_Position_Number_is:" + getCodExInNum();
		}
		else if (codLoc == COD_LOC_INTRON) {
			result = result + "Intron_intron_Position_Number_is:" + getCodExInNum();
		}
		//gene end
		if (isCodInIsoGenEnd()) {
			result = result + "Distance_to_GeneEnd: "+ getCod2Tes();
		}
		return result;
	}
	
	/**
	 * 文字形式的定位描述
	 * null: 不在该转录本内
	 * 
	 * 指定条件，将符合条件的peak抓出来并做注释，主要是筛选出合适的peak然后做后续比较工作
	 * 不符合的会跳过
	 * @param filterTss 是否进行tss筛选，null不进行，如果进行，那么必须是int[2],0：tss上游多少bp  1：tss下游多少bp，都为正数 <b>只有当filterGeneBody为false时，tss下游才会发会作用</b>
	 * @param filterGenEnd 是否进行geneEnd筛选，null不进行，如果进行，那么必须是int[2],0：geneEnd上游多少bp  1：geneEnd下游多少bp，都为正数<b>只有当filterGeneBody为false时，geneEnd上游才会发会作用</b>
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
	public String getCodLocStrFilter(int[] filterTss, int[] filterGenEnd, 
			boolean filterGeneBody,boolean filter5UTR, boolean filter3UTR,boolean filterExon, boolean filterIntron) {
		boolean filter = false;
		if (filterTss != null) {
			if (cod2TSS >= -filterTss[0] && cod2TSS <= filterTss[1]) {
				filter = true;
			}
		}
		if (filterGenEnd != null) {
			if (cod2TES >= -filterGenEnd[0] && cod2TES <= filterGenEnd[1]) {
				filter = true;
			}
		}
		
		if (filterGeneBody && getCodLoc() != COD_LOC_OUT) {
			filter = true;
		}
		else if (filter5UTR && getCodLocUTR() == COD_LOCUTR_5UTR) {
			filter = true;
		}
		else if (filter3UTR && getCodLocUTR() == COD_LOCUTR_3UTR) {
			filter = true;
		}
		else if (filterExon && getCodLoc() == COD_LOC_EXON) {
			filter = true;
		}
		else if (filterIntron && getCodLoc() == COD_LOC_INTRON) {
			filter = true;
		}
		
		if (filter) {
			return getCodLocStr();
		}
		else {
			return null;
		}
	}
	
	
	public abstract GffGeneIsoInfo clone();
	/**
	 * 连lsIso也复制
	 * @return
	 */
	public abstract GffGeneIsoInfo cloneDeep();
	
	
	protected void clone(GffGeneIsoInfo gffGeneIsoInfo)
	{
		gffGeneIsoInfo.ATGsite = ATGsite;
//		gffGeneIsoInfo.coord = coord;
		if (gffDetailGene != null) {
			gffGeneIsoInfo.gffDetailGene = gffDetailGene.clone();
		}
		gffGeneIsoInfo.hashLocExInEnd = hashLocExInEnd;
		gffGeneIsoInfo.hashLocExInNum = hashLocExInNum;
		gffGeneIsoInfo.hashLocExInStart =hashLocExInStart;
//		gffGeneIsoInfo.IsoName = IsoName;
		gffGeneIsoInfo.lsIsoform = lsIsoform;
		gffGeneIsoInfo.lengthIso = lengthIso;
		gffGeneIsoInfo.mRNA = mRNA;
		gffGeneIsoInfo.taxID = taxID;
		gffGeneIsoInfo.UAGsite = UAGsite;
//		gffGeneIsoInfo.flagTypeGene = flagTypeGene;
	}
	protected void cloneDeep(GffGeneIsoInfo gffGeneIsoInfo)
	{
		gffGeneIsoInfo.ATGsite = ATGsite;
//		gffGeneIsoInfo.coord = coord;
		if (gffDetailGene != null) {
			gffGeneIsoInfo.gffDetailGene = gffDetailGene.clone();
		}
		gffGeneIsoInfo.hashLocExInEnd = new HashMap<Integer, Integer>();
		gffGeneIsoInfo.hashLocExInNum = new HashMap<Integer, Integer>();
		gffGeneIsoInfo.hashLocExInStart = new HashMap<Integer, Integer>();
//		gffGeneIsoInfo.IsoName = IsoName;
		ArrayList<int[]> lsIso = new ArrayList<int[]>();
		for (int[] is : lsIsoform) {
			int[] isTmp = new int[2];
			isTmp[0] = is[0];
			isTmp[1] = is[1];
			lsIso.add(isTmp);
		}
		gffGeneIsoInfo.lsIsoform = lsIso;
		gffGeneIsoInfo.lengthIso = lengthIso;
		gffGeneIsoInfo.mRNA = mRNA;
		gffGeneIsoInfo.taxID = taxID;
		gffGeneIsoInfo.UAGsite = UAGsite;
//		gffGeneIsoInfo.flagTypeGene = flagTypeGene;
	}
	//保存中间状态
	HashMap<String, ArrayList<CompSubArrayCluster>> hashTmp = new HashMap<String, ArrayList<CompSubArrayCluster>>();
	/**
	 * 比较两个转录本之间的差距有多大
	 * @param gffGeneIsoInfo
	 * @return
	 */
	public double compIso(GffGeneIsoInfo gffGeneIsoInfo) {
		ArrayList<CompSubArrayCluster> lsCompResult = null;
		if (hashTmp.containsKey(gffGeneIsoInfo.getIsoName())) {
			lsCompResult =  hashTmp.get(gffGeneIsoInfo.getIsoName());
		}
		else {
			ArrayList<ExonInfo> lsThis = new ArrayList<ExonInfo>();
			ArrayList<ExonInfo> lsComp = new ArrayList<ExonInfo>();
			
			ArrayList<int[]> lsIsoComp = gffGeneIsoInfo.getIsoInfo();
			
			for (int[] is : lsIsoform) {
				ExonInfo exonInfo = new ExonInfo(is, gffGeneIsoInfo.isCis5to3());
				lsThis.add(exonInfo);
			}
			for (int[] is : lsIsoComp) {
				ExonInfo exonInfo = new ExonInfo(is, gffGeneIsoInfo.isCis5to3());
				lsComp.add(exonInfo);
			}
			lsCompResult = ArrayOperate.compLs2(lsThis, lsComp, gffGeneIsoInfo.isCis5to3());
			hashTmp.put(gffGeneIsoInfo.getIsoName(), lsCompResult);
		}
		return CompSubArrayCluster.getCompScore(lsCompResult);
	}
	/**
	 * 比较两个转录本之间的差距有多大
	 * @param gffGeneIsoInfo
	 * @return
	 */
	public  ArrayList<CompSubArrayCluster> compIsoLs(GffGeneIsoInfo gffGeneIsoInfo) {
		ArrayList<CompSubArrayCluster> lsCompResult = null;
		if (hashTmp.containsKey(gffGeneIsoInfo.getIsoName())) {
			lsCompResult = hashTmp.get(gffGeneIsoInfo.getIsoName());
		}
		else {
			ArrayList<ExonInfo> lsThis = new ArrayList<ExonInfo>();
			ArrayList<ExonInfo> lsComp = new ArrayList<ExonInfo>();
			
			ArrayList<int[]> lsIsoComp = gffGeneIsoInfo.getIsoInfo();
			
			for (int[] is : lsIsoform) {
				ExonInfo exonInfo = new ExonInfo(is, gffGeneIsoInfo.isCis5to3());
				lsThis.add(exonInfo);
			}
			for (int[] is : lsIsoComp) {
				ExonInfo exonInfo = new ExonInfo(is, gffGeneIsoInfo.isCis5to3());
				lsComp.add(exonInfo);
			}
			lsCompResult = ArrayOperate.compLs2(lsThis, lsComp, gffGeneIsoInfo.isCis5to3());
			hashTmp.put(gffGeneIsoInfo.getIsoName(), lsCompResult);
		}
		return lsCompResult;
	}
	
	
	public void setLsIsoform(ArrayList<int[]> lsIsoform) {
		this.lsIsoform = lsIsoform;
	}
	public void setIsoName(String isoName) {
		IsoName = isoName;
	}
	/**
	 * 专门添加cufflink的结果
	 * @param locStart
	 * @param locEnd
	 */
	protected abstract void addExonCufflinkGTF(int locStart, int locEnd);
	
	
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
//		String genetitle = getChrID() + "\t" +title + "\ttranscript\t" +getStartAbs() +
//		"\t" + getEndAbs() + "\t"+"0.000000"+"\t" +strand+"\t.\t"+ "gene_id \""+geneID+"\"; transcript_id \""+getIsoName()+"\"; \r\n";
//		genetitle = genetitle + getGTFformatExon(geneID, title,strand);
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
//		String genetitle = getChrID() + "\t" +title + "\ttranscript\t" +getStartAbs() +
//		"\t" + getEndAbs() + "\t"+"0.000000"+"\t" +strand+"\t.\t"+ "gene_id \""+geneID+"\"; transcript_id \""+getIsoName()+"\"; \r\n";
//		genetitle = genetitle + getGTFformatExon(geneID, title,strand);
		String genetitle = getGFFformatExonMISO(geneID, title,strand);
		return genetitle;
	}
	protected abstract String getGTFformatExon(String geneID, String title, String strand);
	protected abstract String getGFFformatExonMISO(String geneID, String title, String strand);
	/**
	 * 获得所有外显子的长度之和
	 */
	public int getIsoLen() {
		int isoLen = 0;
		for (int[] exons : lsIsoform) {
			isoLen = isoLen + Math.abs(exons[1] - exons[0]);
		}
		return isoLen;
	}
	
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
	 * 比较是否为同一个转录本
	 */
	public boolean equals(Object obj) {

		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		GffGeneIsoInfo otherObj = (GffGeneIsoInfo)obj;
		//物种，起点终点，ATG，UAG，外显子长度，转录本名字等都一致
		boolean flag =  this.getTaxID() == otherObj.getTaxID() && this.getChrID().equals(otherObj.getChrID()) && this.getATGSsite() == otherObj.getATGSsite()
		&& this.getUAGsite() == otherObj.getUAGsite() && this.getTSSsite() == otherObj.getTSSsite()
		&& this.getIsoLen() == otherObj.getIsoLen() && this.getIsoName().equals(otherObj.getIsoName());
		
		if (flag && compIso(otherObj.getIsoInfo()) ) {
			return true;
		}
		return false;
	}
	

	
	/**
	 * 外显子比较如果一模一样则返回true；
	 * @param lsOtherExon
	 * @return
	 */
	protected boolean compIso(ArrayList<int[]> lsOtherExon)
	{
		if (lsOtherExon.size() != getIsoInfo().size()) {
			return false;
		}
		for (int i = 0; i < lsOtherExon.size(); i++) {
			int exonOld[] = lsOtherExon.get(i);
			int exonThis[] = getIsoInfo().get(i);
			if (exonOld[0] != exonThis[0] || exonOld[1] != exonThis[1]) {
				return false;
			}
		}
		return true;
	}
	protected abstract void sortIso();
	protected abstract void sortIsoRead();
	
	

	/**
	 * 获得具体的编码序列
	 * 没有结果就返回new list-int[]
	 * @return
	 */
	public ArrayList<int[]> getIsoInfoCDS()
	{
		if (ATGsite == UAGsite) {
			return new ArrayList<int[]>();
		}

		ArrayList<int[]> lsresult = new ArrayList<int[]>();
		int numAtg = getLocExInNum(ATGsite) - 1;
		int numUag = getLocExInNum(UAGsite) - 1;
		for (int i = 0; i < lsIsoform.size(); i++) {
			int[] exonTmp = lsIsoform.get(i);
			if (i < numAtg) {
				continue;
			}
			else if (i > numUag) {
				break;
			}
			else if (i == numAtg) {
				int[] exonFinalTmp = new int[2];
				exonFinalTmp[0] = ATGsite;
				if (numAtg == numUag) {
					exonFinalTmp[1] = UAGsite;
					lsresult.add(exonFinalTmp);
					break;
				}
				else {
					exonFinalTmp[1] = exonTmp[1];
					lsresult.add(exonFinalTmp);
				}
			}
			else if (i == numUag) {
				int[] exonFinalTmp = new int[2];
				exonFinalTmp[0] = exonTmp[0];
				exonFinalTmp[1] = UAGsite;
				lsresult.add(exonFinalTmp);
				break;
			}
			else {
				int[] exonFinalTmp = new int[2];
				exonFinalTmp[0] = exonTmp[0];
				exonFinalTmp[1] = exonTmp[1];
				lsresult.add(exonFinalTmp);
			}
		}
		return lsresult;
	}
}




class ExonInfo implements CompSubArray
{
	boolean cis;
	int[] exon;
	String flag = "";
	@Override
	public double[] getCell() {
		if (cis) {
			return new double[]{exon[0],exon[1]};
		}
		else {
			return new double[]{exon[1],exon[0]};
		}
	}
	public ExonInfo(int[] exon, boolean cis) {
		this.exon = exon;
		this.cis = cis;
	}
	@Override
	public Boolean isCis5to3() {
		return cis;
	}
	@Override
	public double getStartCis() {
		return exon[0];
	}
	@Override
	public void setStartCis(double startLoc)
	{
		exon[0] = (int)startLoc;
	}
	@Override
	public void setEndCis(double endLoc)
	{
		exon[1] = (int)endLoc;
	}
	@Override
	public double getEndCis() {
		return exon[1];
	}
	@Override
	public double getStartAbs() {
		return Math.min(exon[0], exon[1]);
	}
	@Override
	public double getEndAbs() {
		return Math.max(exon[0], exon[1]);
	}
	@Override
	public String getFlag() {
		return flag;
	}
	@Override
	public void setFlag(String flag) {
		this.flag = flag;
	}
	@Override
	public double getLen()
	{
		return Math.abs(exon[0] - exon[1]);
	}
	

}
