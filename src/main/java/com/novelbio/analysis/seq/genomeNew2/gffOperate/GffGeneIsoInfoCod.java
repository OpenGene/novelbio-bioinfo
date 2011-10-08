package com.novelbio.analysis.seq.genomeNew2.gffOperate;

public abstract class GffGeneIsoInfoCod {
	GffGeneIsoInfo gffGeneIso = null;
	public GffGeneIsoInfoCod(GffGeneIsoInfo gffgeneIso, int coord)
	{
		this.gffGeneIso = gffgeneIso;
		this.coord = coord;
		searchCoord();
	}
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
	protected int numExIntron = -1;
	
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
	public GffGeneIsoInfo getGffGeneIso() {
		return gffGeneIso;
	}
	/**
	 * coord是否在promoter区域的范围内，从Tss上游UpStreamTSSbp到Tss下游DownStreamTssbp
	 * @return
	 */
	public boolean isCodInIsoTss()
	{
		if (codLoc == COD_LOC_OUT && getCod2Tss() < 0 && Math.abs(getCod2Tss()) <= GffGeneIsoInfo.UpStreamTSSbp ) {
			return true;
		}
		else if ( codLoc != COD_LOC_OUT && getCod2Tss() > 0 && Math.abs(getCod2Tss()) <= GffGeneIsoInfo.DownStreamTssbp ) {
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
		if (codLoc == COD_LOC_OUT && getCod2Tes() > 0 && Math.abs(getCod2Tes()) <= GffGeneIsoInfo.GeneEnd3UTR ) {
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
	 * 坐标在第几个外显子或内含子中，如果不在就为负数
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
	 * 因为cod在外显子中，所以肯定在tss下游，所以该值始终为正数
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
		 coord = GffCodAbs.LOC_ORIGINAL;
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
		int ExIntronnum = gffGeneIso.getLocExInNum(coord);
		if (ExIntronnum == 0) {
			codLoc = COD_LOC_OUT;
		}
		else if (ExIntronnum > 0) {
			codLoc = COD_LOC_EXON;
			if((coord < gffGeneIso.ATGsite && gffGeneIso.isCis5to3()) || (coord > gffGeneIso.ATGsite && !gffGeneIso.isCis5to3())){        //坐标小于atg，在5‘UTR中,也是在外显子中
				codLocUTR = COD_LOCUTR_5UTR;
			}
			else if((coord > gffGeneIso.UAGsite && gffGeneIso.isCis5to3()) || (coord < gffGeneIso.UAGsite && !gffGeneIso.isCis5to3())){       //大于cds起始区，在3‘UTR中
				codLocUTR = COD_LOCUTR_3UTR; 
			}
		}
		else {
			codLoc = COD_LOC_INTRON;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		setCod2SiteAbs();
		cod2ExInStart = gffGeneIso.getLoc2ExInStart(coord);
		cod2ExInEnd = gffGeneIso.getLoc2ExInEnd(coord);
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

//	/**
//	 * 保存某个坐标和所在的内含子外显子数目
//	 */
//	HashMap<Integer, Integer> hashLocExInNum;
//
//	/**
//	 * 保存某个坐标到所在的内含子/外显子起点的距离
//	 */
//	HashMap<Integer, Integer> hashLocExInStart;

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
		if (filter5UTR && getCodLocUTR() == COD_LOCUTR_5UTR) {
			filter = true;
		}
		if (filter3UTR && getCodLocUTR() == COD_LOCUTR_3UTR) {
			filter = true;
		}
		if (filterExon && getCodLoc() == COD_LOC_EXON) {
			filter = true;
		}
		if (filterIntron && getCodLoc() == COD_LOC_INTRON) {
			filter = true;
		}
		if (filter) {
			return getCodLocStr();
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
	public String getCodLocStr() {
		String result = "";
		if (isCodInIsoExtend()) {
			return null;
		}
		//promoter
		if (isCodInIsoTss() && getCodLoc() == COD_LOC_OUT) {
			if (cod2TSS > GffGeneIsoInfo.PROMOTER_INTERGENIC_MAMMUM) {
				result = GffGeneIsoInfo.PROMOTER_INTERGENIC_STR;
			}
			else if (cod2TSS > GffGeneIsoInfo.PROMOTER_DISTAL_MAMMUM) {
				result = GffGeneIsoInfo.PROMOTER_DISTAL_STR;
			}
			else {
				result = GffGeneIsoInfo.PROMOTER_PROXIMAL_STR;;
			}
		}
		else if (isCodInIsoTss() && getCodLoc() != COD_LOC_OUT) {
			result = GffGeneIsoInfo.PROMOTER_DOWNSTREAMTSS_STR;
		}
		
		result = result + "Distance to Tss is: " + Math.abs(cod2TSS) + " ";
		//UTR
		if (codLocUTR == COD_LOCUTR_5UTR) {
			result = result + "5UTR_";
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			result = result + "3UTR_";
		}
		//exon intron
		if (codLoc == COD_LOC_EXON) {
			result = result + "Exon_Exon Position Number is:" + getCodExInNum();
		}
		else if (codLoc == COD_LOC_INTRON) {
			result = result + "Intron_Intron Position Number is:" + getCodExInNum();
		}
		//gene end
		if (isCodInIsoGenEnd()) {
			result = result + "Distance to GeneEnd: "+ getCod2Tes();
		}
		return result;
	}
}
