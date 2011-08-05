package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * ��¼GffGene�е�ת¼����Ϣ
 * @author zong0jie
 *
 */
public class GffGeneIsoInfo {
	/**
	 * ���codInExon������������
	 */
	public static final int COD_LOC_EXON = 100;
	/**
	 * ���codInExon�����ں�����
	 */
	public static final int COD_LOC_INTRON = 200;
	/**
	 * ���codInExon����ת¼����
	 */
	public static final int COD_LOC_OUT = 300;
	/**
	 * ���codInExon����5UTR��
	 */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**
	 * ���codInExon����3UTR��
	 */
	public static final int COD_LOCUTR_3UTR = 3000;
	/**
	 * ���codInExon����UTR��
	 */
	public static final int COD_LOCUTR_OUT = 0;	
	
	private static final Logger logger = Logger.getLogger(GffGeneIsoInfo.class);
	public GffGeneIsoInfo(String IsoName,boolean cis5to3, GffDetailGene gffDetailGene) {
		this.IsoName = IsoName;
		this.cis5to3 = cis5to3;
		this.gffDetailGene = gffDetailGene;
	}
	
//	/**
//	 * ������ʼ��������ʱ��
//	 * @param IsoName
//	 * @param lsIsoform
//	 */
//	protected GffGeneIsoInfo(String IsoName, ArrayList<int[]> lsIsoform, boolean cis5to3) {
//		this.IsoName = IsoName;
//		this.lsIsoform = lsIsoform;
//		this.cis5to3 = cis5to3;
//	}
	/**
	 * ������ʼ��������ʱ��
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
	 * �Ƿ���mRNA��atg��uag��
	 * ��ʱֻ��ʹ��UCSCgene
	 * @return
	 */
	public boolean ismRNA() {
		return mRNA;
	}
	
	/**
	 * ��ת¼����ATG�ĵ�һ���ַ����꣬��1��ʼ����
	 */
	protected int ATGsite = -10000000;
	/**
	 * ��ת¼����Coding region end�����һ���ַ����꣬��1��ʼ����
	 */
	protected int UAGsite = -10000000;
	/**
	 * ת¼��������
	 */
	protected String IsoName = "";

	  /**
     * ת¼���������ӵľ�����Ϣ<br>
     * exon�ɶԳ��֣���һ��exon�����Ǹ�ת¼������㣬���һ��exon�����Ǹ�ת¼�����յ㣬�����С����������int[0]<int[1]<br>
     * ����Ӵ�С������int[0]>int[1]<br>
     */
	protected ArrayList<int[]> lsIsoform = new ArrayList<int[]>();

	/**
	 * ��ת¼���ĳ���
	 */
	protected int lengthIso = -100;
	/**
	 * ��ת¼�����exon���꣬������Ϊ����ʱUCSC��exon�ĸ�ʽ�� <br>
	 * NM_021170	chr1	-	934341	935552	934438	935353	4	934341,934905,935071,935245,	934812,934993,935167,935552, <br>
	 * ��ôexonΪ934341,934905,935071,935245��934812,934993,935167,935552, <br>
	 * �Ǵ�С�������е�<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ� <br>
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж� <br>
	 */
	protected void addExonUCSC(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ
		 * ��ӵ�ʱ����밴�ջ�������ӣ�
		 * �����С������� �� int0<int1
		 * ����Ӵ�С��� �� int0>int1
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
	 * ���Ҫȷ��
	 * ��ת¼�����exon���꣬GFF3��exon�ĸ�ʽ�� <br>
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ� <br>
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж� <br>
	 */
	protected void addExonGFF(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ
		 * ��ӵ�ʱ����밴�ջ�������ӣ�
		 * �����С������� �� int0<int1
		 * ����Ӵ�С��� �� int0>int1
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
	 * ���Ҫȷ��
	 * ��ת¼�����exon���꣬GFF3��exon�ĸ�ʽ�� <br>
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�<br>
	 * �����exon��ʱ�������CDS��UTR֮�������ŵģ���ô�ͽ���CDS��UTR����һ�𣬷���һ��exon��
	 * ����������Ͱ�ԭ������
	 */
	protected void addExonGFFCDSUTR(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ
		 * ��ӵ�ʱ����밴�ջ�������ӣ�
		 * �����С������� �� int0<int1
		 * ����Ӵ�С��� �� int0>int1
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
	 * ���ظ�ת¼��������
	 * @return
	 */
	public String getIsoName() {
		return IsoName;
	}
	/**
	 * ���ظ�ת¼���ľ���������Ϣ,
	 * ��һ�ʼ��exon����Ϣ��exon�ɶԳ��֣�Ϊint[2] 
	 * 0: ����������㣬�����䣬��1��ʼ����<br>
	 * 1: ���������յ㣬�����䣬��1��ʼ����<br>
	 * ���ջ���ķ����������
	 * ����������С�������У���int0&lt;int1
	 * ���������Ӵ�С���У���int0&gt;int1
	 * @return
	 */
	public  ArrayList<int[]> getIsoInfo() {
		return lsIsoform;
	}
	/**
	 * ��ת¼����ATG�ĵ�һ���ַ����꣬��1��ʼ�������Ǳ�����
	 * @return
	 */
	public int getATGSsite() {
		return ATGsite;
	}
	/**
	 * ��ת¼����Coding region end�����һ���ַ����꣬��1��ʼ�������Ǳ�����
	 * @return
	 */
	public int getUAGsite() {
		return UAGsite;
	}
	/**
	 * ����������
	 * ��ת¼����TSS�ĵ�һ���ַ����꣬��1��ʼ�������Ǳ�����
	 * @return
	 */
	public int getTSSsite() {
		return lsIsoform.get(0)[0];
	}
	/**
	 * ��ת¼����Coding region end�����һ���ַ����꣬��1��ʼ�������Ǳ�����
	 * @return
	 */
	public int getTESsite() {
		return lsIsoform.get(lsIsoform.size() -1)[1];
		
	}
	/**
	 * ���5UTR�ĳ���
	 * @return
	 */
	public int getLenUTR5() {
		int FUTR=0;
		int exonNum = lsIsoform.size();
		if (cis5to3) { //0    1     2     3     4     5   ÿ���������� 1 > 0      0    atg   1
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
		else { //5  4   3   2   1   0    ÿ���������� 0 > 1     1    gta   0
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
	 * ���3UTR�ĳ���
	 * @return
	 */
	public int getLenUTR3()
	{
		int TUTR=0;
		int exonNum = lsIsoform.size();
		if (cis5to3) { //0    1     2     3     4     5   ÿ���������� 0 < 1      0    uag   1
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
		else { //5  4   3   2   1   0    ÿ���������� 0 > 1      1    gau  0
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
     * @param num ָ���ڼ���������������򷵻�-1000000000, 
     * num Ϊʵ�ʸ��������num=0�򷵻�ȫ��Exon�ĳ��ȡ�
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
     * @param num ָ���ڼ���������������򷵻�-1000000000, 
     * num Ϊʵ�ʸ��������num=0�򷵻�ȫ��Intron�ĳ��ȡ�
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
