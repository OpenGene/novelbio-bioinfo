package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * 记录GffGene中的转录本信息
 * @author zong0jie
 *
 */
public class GffGeneIsoInfo {
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
	
	private static final Logger logger = Logger.getLogger(GffGeneIsoInfo.class);
	public GffGeneIsoInfo(String IsoName,boolean cis5to3, GffDetailGene gffDetailGene) {
		this.IsoName = IsoName;
		this.cis5to3 = cis5to3;
		this.gffDetailGene = gffDetailGene;
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
	/**
	 * 仅仅初始化给查找时用
	 * @param IsoName
	 * @param lsIsoform
	 */
	protected GffGeneIsoInfo(GffGeneIsoInfo gffGeneIsoInfo) {
		this.ATGsite = gffGeneIsoInfo.ATGsite;
		this.cis5to3 = gffGeneIsoInfo.cis5to3;
		this.IsoName = gffGeneIsoInfo.IsoName;
		this.lengthIso = gffGeneIsoInfo.lengthIso;
		this.lsIsoform = gffGeneIsoInfo.lsIsoform;
		this.UAGsite = gffGeneIsoInfo.UAGsite;
		this.mRNA = gffGeneIsoInfo.mRNA;
		this.gffDetailGene = gffGeneIsoInfo.gffDetailGene;
	}
	GffDetailGene gffDetailGene;
	public GffDetailGene getThisGffDetailGene() {
		return gffDetailGene;
	}
	boolean cis5to3 = true;
	public boolean isCis5to3() {
		return cis5to3;
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
	 * 该转录本的ATG的第一个字符坐标，从1开始计数
	 */
	protected int ATGsite = -10000000;
	/**
	 * 该转录本的Coding region end的最后一个字符坐标，从1开始计数
	 */
	protected int UAGsite = -10000000;
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
	protected int lengthIso = -100;
	/**
	 * 给转录本添加exon坐标，当基因为反向时UCSC的exon的格式是 <br>
	 * NM_021170	chr1	-	934341	935552	934438	935353	4	934341,934905,935071,935245,	934812,934993,935167,935552, <br>
	 * 那么exon为934341,934905,935071,935245和934812,934993,935167,935552, <br>
	 * 是从小到大排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 */
	protected void addExonUCSC(int locStart, int locEnd) {
		/**
		 * 添加外显子，添加在末尾
		 * 添加的时候必须按照基因方向添加，
		 * 正向从小到大添加 且 int0<int1
		 * 反向从大到小添加 且 int0>int1
		 */
		int[] tmpexon = new int[2];
		if (cis5to3) {
			tmpexon[0] = Math.min(locStart, locEnd);
			tmpexon[1] = Math.max(locStart, locEnd);
			lsIsoform.add(tmpexon);
		}
		else {
			tmpexon[0] = Math.max(locStart, locEnd);
			tmpexon[1] = Math.min(locStart, locEnd);
			lsIsoform.add(0, tmpexon);
		}
	}
	/**
	 * 这个要确认
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 */
	protected void addExonGFF(int locStart, int locEnd) {
		/**
		 * 添加外显子，添加在末尾
		 * 添加的时候必须按照基因方向添加，
		 * 正向从小到大添加 且 int0<int1
		 * 反向从大到小添加 且 int0>int1
		 */
		int[] tmpexon = new int[2];
		if (cis5to3) {
			tmpexon[0] = Math.min(locStart, locEnd);
			tmpexon[1] = Math.max(locStart, locEnd);
		}
		else {
			tmpexon[0] = Math.max(locStart, locEnd);
			tmpexon[1] = Math.min(locStart, locEnd);
		}
		lsIsoform.add(tmpexon);
	}
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
		if (cis5to3) {
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
	/**
	 * 获得5UTR的长度
	 * @return
	 */
	public int getLenUTR5() {
		int FUTR=0;
		int exonNum = lsIsoform.size();
		if (cis5to3) { //0    1     2     3     4     5   每个外显子中 1 > 0      0    atg   1
			for (int i = 0; i <exonNum; i++) 
			{
				if(lsIsoform.get(i)[1] < getATGSsite())    // 0       1   atg    
					FUTR = FUTR + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
				else if (lsIsoform.get(i)[0] < getATGSsite() && lsIsoform.get(i)[1] >= getATGSsite())  //     0    atg    1 
					FUTR = FUTR + getATGSsite() - lsIsoform.get(i)[0];
				else if (lsIsoform.get(i)[0] >= getATGSsite())  //     atg   0       1   
					break;
			}
		}
		else { //5  4   3   2   1   0    每个外显子中 0 > 1     1    gta   0
			for (int i = 0; i < exonNum; i++) 
			{
				if(lsIsoform.get(i)[1] > getATGSsite())  // gta   1      0
					FUTR = FUTR + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] + 1;
				else if (lsIsoform.get(i)[0] > getATGSsite()  && lsIsoform.get(i)[1] <= getATGSsite() ) //   1     gta      0
					FUTR = FUTR + lsIsoform.get(i)[0] - getATGSsite();
				else if (lsIsoform.get(i)[0] <= getATGSsite())   //   1        0      gta 
					break;
			}
		}
		return FUTR;
	}
	
	/**
	 * 获得3UTR的长度
	 * @return
	 */
	public int getLenUTR3()
	{
		int TUTR=0;
		int exonNum = lsIsoform.size();
		if (cis5to3) { //0    1     2     3     4     5   每个外显子中 0 < 1      0    uag   1
			for (int i = exonNum - 1; i >=0 ; i--) 
			{
				if(lsIsoform.get(i)[0] > getUAGsite())  //      uag     0      1
					TUTR = TUTR + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
				else if (lsIsoform.get(i)[1] > getUAGsite() && lsIsoform.get(i)[0] <= getUAGsite())  //     0     uag    1
					TUTR = TUTR + lsIsoform.get(i)[1] - getUAGsite();
				else if (lsIsoform.get(i)[1] <= getUAGsite())   //   0      1     uag   
					break;
			}
		}
		else { //5  4   3   2   1   0    每个外显子中 0 > 1      1    gau  0
			for (int i = exonNum-1; i >=0 ; i--) 
			{
				if(lsIsoform.get(i)[0] < getUAGsite())  //     1      0     gau
					TUTR = TUTR + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] + 1;
				else if (lsIsoform.get(i)[0] >= getUAGsite() && lsIsoform.get(i)[1] < getUAGsite())  //     1    gau    0     
					TUTR = TUTR + getUAGsite() - lsIsoform.get(i)[1];
				else if (lsIsoform.get(i)[1] >= getUAGsite())   //   gau   1      0     
					break;
			}
		}
		return TUTR;
	}
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

}
