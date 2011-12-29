package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;
/**
 * 名字通通小写
 * 计算距离时，SnnnC<br>
 * S距离C为5，S和C重合时距离为0<br>
 * CnnnATG<br>
 * C到UTRend的距离: ATGsite - coord - 1;//CnnnATG<br>
 * C到ATG的距离: coord - ATGsite//CnnnATG<br>
 * 距离本外显子起始 nnnCnn为3个碱基，距离终点为2个碱基<br>
 * 距离本外显子起始 Cnn为0个碱基<br>
 * @author zong0jie
 *
 */
public class GffGeneIsoCis extends GffGeneIsoInfo {
	private static final Logger logger = Logger.getLogger(GffGeneIsoCis.class);

	public GffGeneIsoCis(String IsoName, GffDetailGene gffDetailGene, String geneType) {
		super(IsoName, gffDetailGene, geneType);
	}

	public GffGeneIsoCis(String IsoName, String ChrID, int coord, String geneType) {
		super(IsoName, ChrID, coord, geneType);
	}
	public GffGeneIsoCis(String IsoName, String ChrID, String geneType) {
		super(IsoName, ChrID, geneType);
	}
	/**
	 * 第一个计算的，计算坐标与本 外显子/内含子 的 起点/终点 的距离
	 */
	@Override
	protected void setCod2ExInStartEnd() {}
	
	/**
	 * 当坐标在5UTR的外显子中时使用
	 * 方向为正向
	 */
	@Override
	protected void setCod2UTR5() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		//             tss             0-0   0-1        1-0 cood 1-1           2-0  2-1               3-0  atg  3-1               4               5
		for (int i = 0; i < NumExon; i++) {
			cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
		}
		cod2UTRstartmRNA = cod2UTRstartmRNA + cod2ExInStart;
		//  tss             0        1-0    cood  atg  1-1      2               3               4               5
		if (ATGsite <= lsIsoform.get(NumExon)[1]) //一定要小于等于
		{
			cod2UTRendmRNA = ATGsite - coord - 1;//CnnnnnnATG
		}
		// tss             0        1-0    cood   1-1      2               3-0   atg   3-1                 4               5
		else
		{
			cod2UTRendmRNA = cod2ExInEnd;  ///Cnnnnnn
			int m = NumExon+1;
			while ( lsIsoform.get(m)[1] < ATGsite  ) 
			{
				cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(m)[1] - lsIsoform.get(m)[0] + 1;
				m++;
			}
			cod2UTRendmRNA = cod2UTRendmRNA + ATGsite - lsIsoform.get(m)[0];//nnnnnnnATG
			
			if (ATGsite < lsIsoform.get(m)[0]) {
				logger.error("setCod2UTR5Cis error: coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
			}
		}
	}
	
	/**
	 * 当坐标在3UTR的外显子中时使用
	 * 方向为正向
	 */
	@Override
	protected void setCod2UTR3() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag  cood  2-1               3-0      3-1                 4               5
		if ( UAGsite >= lsIsoform.get(NumExon)[0])//一定要大于等于 
		{
			cod2UTRstartmRNA = coord - UAGsite - 1;  //UAGnnnnnnnC
		}
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag    2-1               3-0      3-1                 4-0    cood    4-1               5
		else 
		{
			cod2UTRstartmRNA = cod2ExInStart; //nnnnnnnC
			int m = NumExon-1;
			while (m >= 0 && lsIsoform.get(m)[0] > UAGsite) 
			{
				cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(m)[1] - lsIsoform.get(m)[0] + 1;
				m--;
			}
			cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(m)[1] - UAGsite; //UAGnnnnn
		}
		/////////////////////utrend//////////////////
		// tss             0        1-0    atg   1-1      2-0    uag    2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		for (int i = lsIsoform.size() - 1; i > NumExon; i--) {
			cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2UTRendmRNA = cod2UTRendmRNA + cod2ExInEnd;
	}
	
	/**
	 * 当在EXON中时才使用，看cod与本mRNA的起点和终点的距离
	 * 不包括内含子
	 */
	protected void setCod2StartEndmRNA() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		cod2TSSmRNA = 0; cod2TESmRNA = 0;
		// tss             0-0 0-1        1-0 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		for (int i = 0; i < NumExon; i++) {
			cod2TSSmRNA = cod2TSSmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2TSSmRNA = cod2TSSmRNA + cod2ExInStart;
		
		for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
			cod2TESmRNA = cod2TESmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2TESmRNA = cod2TESmRNA + cod2ExInEnd;
	}
	
	/**
	 * 当在EXON中时才使用，看cod与本mRNA的atg和uag的距离
	 * 先计算setCod2StartEndmRNA
	 * 不包括内含子
	 */
	protected void setCod2StartEndCDS() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		cod2ATGmRNA = 0; cod2UAGmRNA = 0;
		// tss             0-0 0-1        1-0 atg 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		if (codLocUTR == COD_LOCUTR_5UTR) {
			cod2ATGmRNA = -(cod2UTRendmRNA + 1);
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			cod2UAGmRNA = cod2UTRstartmRNA + 1;
		}
		//当coord在ATG下游时,为正数
		if (codLocUTR != COD_LOCUTR_5UTR) {
			//当coord在UAG上游时,为负数
			if (NumExon  == getLocExInNum(ATGsite) - 1) {
				cod2ATGmRNA = (coord - ATGsite);
			}
			else {
				// tss             0-0 0-1        1-0 atg 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 uag 5-1
				for (int i = 0; i < NumExon; i++) {
					if (lsIsoform.get(i)[1] < ATGsite) {
						continue;
					}
					if (lsIsoform.get(i)[0] <= ATGsite && lsIsoform.get(i)[1] >= ATGsite) {
						cod2ATGmRNA = lsIsoform.get(i)[1] - ATGsite + 1; // Atgnn   nnnnC
						continue;
					}
					cod2ATGmRNA = cod2ATGmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
				}
				cod2ATGmRNA = cod2ATGmRNA + cod2ExInStart;
			}
		
		}
		//当coord在UAG上游时,为负数
		if (codLocUTR != COD_LOCUTR_3UTR) {
			if (NumExon  == getLocExInNum(UAGsite) - 1) {
				cod2UAGmRNA = (coord - UAGsite);
				return;
			}
			
			
			
			// tss             0-0 0-1        1-0 1-1      2-0 2-1               3-0   cood    3-1       4-0 4-1       5-0 uag 5-1       6-0 6-1
			for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
				if (lsIsoform.get(i)[0] > UAGsite) {
					break;
				}
				if (lsIsoform.get(i)[0] <= UAGsite && lsIsoform.get(i)[1] >= UAGsite) {
					cod2UAGmRNA = cod2UAGmRNA + UAGsite - lsIsoform.get(i)[0] + 1; // nCnnn nnn  nnuaG
					break;
				}
				cod2UAGmRNA = cod2UAGmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
			}
			cod2UAGmRNA = -(cod2UAGmRNA + cod2ExInEnd);
		}
	}
	

	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 为实际数目
	 * 都不在为0
	 * @return
	 */
	@Override
	protected int getLocExInNum(int location) {
		if (hashLocExInNum == null) {
			hashLocExInNum = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInNum.containsKey(location)) {
			return hashLocExInNum.get(location);
		}

		if (    location < lsIsoform.get(0)[0] || 
				location > lsIsoform.get(lsIsoform.size()-1)[1]  )  	{
//			hashLocExInNum.put(location, 0);  //不在转录本内的坐标不用理会
			return 0;
		}
		for(int i = 0; i < lsIsoform.size(); i++)  //一个一个Exon的检查
		{
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 cood 3-1                 4-0  4-1               5
			if(location <= lsIsoform.get(i)[1] && location >= lsIsoform.get(i)[0]) {
				hashLocExInNum.put(location, i + 1);
				return i + 1;
			}
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 3-1        cood         4-0  4-1               5
			else if(i<= lsIsoform.size() - 2 && location > lsIsoform.get(i)[1] && location < lsIsoform.get(i+1)[0]) {
				hashLocExInNum.put(location, -(i + 1));
				return -(i + 1);
			}
		}
		hashLocExInNum.put(location, 0);
		return 0;
	}

	/**
	 * 坐标到外显子/内含子 起点距离
	 * @param location 坐标
	 */
	@Override
	protected int getLoc2ExInStart(int location) {
		if (hashLocExInStart == null) {
			hashLocExInStart = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInStart.containsKey(location)) {
			return hashLocExInStart.get(location);
		}
		int loc2ExInStart = -1000000000;
		int exIntronNum = getLocExInNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			loc2ExInStart = location - lsIsoform.get(NumExon)[0];//距离本外显子起始 nnnnnnnnC
			hashLocExInStart.put(location, loc2ExInStart);
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			loc2ExInStart = location - lsIsoform.get(NumExon)[1] -1;// 距前一个外显子 NnnnCnnnn
			hashLocExInStart.put(location, loc2ExInStart);
		}
		return loc2ExInStart;
	}

	/**
	 * 坐标到外显子/内含子 终点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	@Override
	protected int getLoc2ExInEnd(int location) {
		if (hashLocExInEnd == null) {
			hashLocExInEnd = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInEnd.containsKey(location)) {
			return hashLocExInEnd.get(location);
		}
		int loc2ExInEnd = -1000000000;
		int exIntronNum = getLocExInNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			 loc2ExInEnd = lsIsoform.get(NumExon)[1] - location;//距离本外显子终止  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			 loc2ExInEnd = lsIsoform.get(NumExon)[0] - location - 1;// 距后一个外显子 nnCnnnnN
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	
	
	/**
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 */
	@Override
	public int getLocDistmRNASite(int location, int mRNAnum) {
		if (getLocExInNum(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getLoc2ExInStart(location)) {
				return location + mRNAnum;
			} 
			else {
				int exonNum = getLocExInNum(location) - 1;
				int remain = Math.abs(mRNAnum) - getLoc2ExInStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					int[] tmpExon = lsIsoform.get(i);
					// 一个一个外显子的向前遍历
					if (remain - (tmpExon[1] - tmpExon[0] + 1) > 0) {
						remain = remain - (tmpExon[1] - tmpExon[0] + 1);
						continue;
					}
					else {
						return tmpExon[1] - remain + 1;
					}
				}
				return -1;
			}
		} 
		else {
			if (mRNAnum <= getLoc2ExInEnd(location)) {
				return location + mRNAnum;
			} 
			else {
				int exonNum = getLocExInNum(location) - 1;
				int remain = mRNAnum - getLoc2ExInEnd(location);
				for (int i = exonNum + 1; i < lsIsoform.size(); i++) {
					int[] tmpExon = lsIsoform.get(i);
					// 一个一个外显子的向前遍历
					if (remain - (tmpExon[1] - tmpExon[0] + 1) > 0) {
						remain = remain - (tmpExon[1] - tmpExon[0] + 1);
						continue;
					}
					else {
						return tmpExon[0] + remain - 1;
					}
				}
				return -1;
			}
		}
	}


	@Override
	protected void setCod2SiteAbs() {
		cod2ATG = coord - ATGsite; //CnnnATG    AtgnC
		cod2UAG = coord - UAGsite; //CnuaG    UAGnnnC
		cod2TSS = coord - getTSSsite();
		cod2TES = coord - getTESsite();
		
	}
	
	
	protected void addExonUCSC(int locStart, int locEnd) {
		/**
		 * 添加外显子，添加在末尾 添加的时候必须按照基因方向添加， 正向从小到大添加 且 int0<int1 反向从大到小添加 且
		 * int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);
		lsIsoform.add(tmpexon);
	}
	
	/**
	 * 这个要确认
	 * 给转录本添加exon坐标，GFF3的exon的格式是 <br>
	 * 当gene为反方向时，exon是从大到小排列的<br>
	 * 只需要注意按照次序装，也就是说如果正向要从小到大的加，反向从大到小的加 <br>
	 * 然而具体加入这一对坐标的时候，并不需要分别大小，程序会根据gene方向自动判定 <br>
	 */
	protected void addExonCufflinkGTF(int locStart, int locEnd) {
		addExonGFF( locStart,  locEnd);
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
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);

		lsIsoform.add(tmpexon);
	}
	
	/**
	 * 获得5UTR的长度
	 * @return
	 */
	public int getLenUTR5() {
		int FUTR=0;
		int exonNum = lsIsoform.size();
		 //0    1     2     3     4     5   每个外显子中 1 > 0      0    atg   1
			for (int i = 0; i <exonNum; i++) 
			{
				if(lsIsoform.get(i)[1] < getATGSsite())    // 0       1   atg    
					FUTR = FUTR + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
				else if (lsIsoform.get(i)[0] < getATGSsite() && lsIsoform.get(i)[1] >= getATGSsite())  //     0    atg    1 
					FUTR = FUTR + getATGSsite() - lsIsoform.get(i)[0];
				else if (lsIsoform.get(i)[0] >= getATGSsite())  //     atg   0       1   
					break;
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
		 //0    1     2     3     4     5   每个外显子中 0 < 1      0    uag   1
		for (int i = exonNum - 1; i >=0 ; i--) 
		{
			if(lsIsoform.get(i)[0] > getUAGsite())  //      uag     0      1
				TUTR = TUTR + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
			else if (lsIsoform.get(i)[1] > getUAGsite() && lsIsoform.get(i)[0] <= getUAGsite())  //     0     uag    1
				TUTR = TUTR + lsIsoform.get(i)[1] - getUAGsite();
			else if (lsIsoform.get(i)[1] <= getUAGsite())   //   0      1     uag   
				break;
		}
		return TUTR;
	}


	@Override
	public GffGeneIsoCis clone() {
		GffGeneIsoCis gffGeneIsoCis = new GffGeneIsoCis(IsoName, chrID,coord, getGeneType());
		this.clone(gffGeneIsoCis);
		gffGeneIsoCis.setCoord(getCoord());
		return gffGeneIsoCis;
	}


	@Override
	public boolean isCis5to3() {
		return true;
	}
	
	@Override
	public GffGeneIsoCis cloneDeep() {
		GffGeneIsoCis gffGeneIsoCis = new GffGeneIsoCis(IsoName, chrID,coord, getGeneType());
		this.cloneDeep(gffGeneIsoCis);
		gffGeneIsoCis.setCoord(getCoord());
		return gffGeneIsoCis;
	}

	@Override
	public int getStartAbs() {
		return lsIsoform.get(0)[0];
	}

	@Override
	public int getEndAbs() {
		return lsIsoform.get(lsIsoform.size() - 1)[1];
	}
	@Override
	protected String getGFFformatExonMISO(String geneID, String title,
			String strand) {
		String geneExon = "";
		for (int i = 0; i < getIsoInfo().size(); i++) {
			int[] exons = getIsoInfo().get(i);
			geneExon = geneExon + getChrID() + "\t" +title + "\texon\t" +exons[0] + "\t" + exons[1]
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=exon:" + getIsoName()  + ":" + (i+1) +";Parent=" + getIsoName() + " \r\n";
		}
		return geneExon;
	
	}
	@Override
	protected String getGTFformatExon(String geneID, String title, String strand) {
		String geneExon = "";
		for (int[] exons : getIsoInfo()) {
			geneExon = geneExon + getChrID() + "\t" +title + "\texon\t" +exons[0] + "\t" + exons[1]
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "gene_id \""+geneID+"\"; transcript_id \""+getIsoName()+"\"; \r\n";
		}
		return geneExon;
	}
	/**
	 * 从大到小排序
	 */
	@Override
	protected void sortIso() {
		for (int i = 0; i < lsIsoform.size(); i++) {
			int[] is = lsIsoform.get(i);
			if (is[0] > is[1]) {
				int tmp = is[1];
				is[1] = is[0];
				is[0] = tmp;
				logger.error("exon坐标有问题：" + is[0]+"\t" + is[1]);
			}
		}
		
		Collections.sort(lsIsoform, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				if (o1[0] < o2[0]) {
					return -1;
				}
				else if (o1[0] > o2[0]) {
					return 1;
				}
				else {
					return 0;
				}
			}
		});
	}

	@Override
	protected void sortIsoRead() {
		Collections.sort(lsIsoform, new Comparator<int[]>() {

			@Override
			public int compare(int[] o1, int[] o2) {
				Integer a = o1[0];
				Integer b = o2[0];
				return a.compareTo(b);
			}
		});
	}
	
}
