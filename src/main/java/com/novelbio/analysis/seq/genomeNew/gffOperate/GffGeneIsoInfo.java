package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * ��¼GffGene�е�ת¼����Ϣ
 * @author zong0jie
 *
 */
public abstract class GffGeneIsoInfo {
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
		this.coord = gffDetailGene.getCoord();
		if (this.coord > GffCodAbs.LOC_ORIGINAL) {
			searchCoord();
		}
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
	protected int ATGsite = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ��ת¼����Coding region end�����һ���ַ����꣬��1��ʼ����
	 */
	protected int UAGsite = GffCodAbs.LOC_ORIGINAL;
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
	protected int lengthIso = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ��ת¼�����exon���꣬������Ϊ����ʱUCSC��exon�ĸ�ʽ�� <br>
	 * NM_021170	chr1	-	934341	935552	934438	935353	4	934341,934905,935071,935245,	934812,934993,935167,935552, <br>
	 * ��ôexonΪ934341,934905,935071,935245��934812,934993,935167,935552, <br>
	 * �Ǵ�С�������е�<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ� <br>
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж� <br>
	 */
	protected abstract void addExonUCSC(int locStart, int locEnd);
	/**
	 * ���Ҫȷ��
	 * ��ת¼�����exon���꣬GFF3��exon�ĸ�ʽ�� <br>
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ� <br>
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж� <br>
	 */
	protected abstract void addExonGFF(int locStart, int locEnd);
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
	public abstract int getLenUTR5();
	
	/**
	 * ���3UTR�ĳ���
	 * @return
	 */
	public abstract int getLenUTR3();
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
	
	/**
	 * ����
	 */
	protected int coord = GffCodAbs.LOC_ORIGINAL;
	
	/**
	 * ���굽��ת¼�����ľ��룬����������
	 * �������������Ϊ����������Ϊ����
	 */
	protected int cod2TSS = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ���굽��ת¼���յ�ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
	 */
	protected int cod2TES = GffCodAbs.LOC_ORIGINAL;
	
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���
	 * ���굽��ת¼�����ľ��룬ֻ��mRNAˮƽ������������
	 * ֻ�е����괦���������в��о��룬�������ں���\
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	protected int cod2TSSmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���
	 * ���굽��ת¼���յ�ľ��룬ֻ��mRNAˮƽ������������
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	protected int cod2TESmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���<br>
	 * ���굽��ת¼��atg�ľ��룬ֻ��mRNAˮƽ������������<br>
	 * �������������Ϊ����������Ϊ����<br>
	 */
	protected int cod2ATGmRNA= GffCodAbs.LOC_ORIGINAL;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���<br>
	 * ���굽��ת¼��uag�ľ��룬ֻ��mRNAˮƽ������������<br>
	 * �������յ�����Ϊ����������Ϊ����<br>
	 * Cnn nn  nuaG ����Ϊ8
	 */
	protected int cod2UAGmRNA = GffCodAbs.LOC_ORIGINAL;
	
	
	/**
	 * ���������������/�ں����У�
	 * �������������/�ں������ľ���
	 * ��Ϊ����
	 */
	protected int cod2ExInStart = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ���������������/�ں����У�
	 * �������������/�ں����յ�ľ���
	 * ��Ϊ����
	 */
	protected int cod2ExInEnd = GffCodAbs.LOC_ORIGINAL;
	/**
	 * �����ں���
	 * ���굽ATG�ľ��룬����������.
	 * ��ATG����Ϊ����������Ϊ����
	 * @return
	 */
	protected int cod2ATG = GffCodAbs.LOC_ORIGINAL;
	/**
	 * �����ں���
	 * ���굽UAG�ľ��룬����������.
	 * ��UAG����Ϊ����������Ϊ����
	 * @return
	 */
	protected int cod2UAG = GffCodAbs.LOC_ORIGINAL;
	/**
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ����
	 * ʵ����Ŀ����1��ʼ����
	 */
	protected int numExIntron = -1;
	
	/**
	 * ������5UTR��3UTR���ǲ���
	 */
	protected int codLocUTR = 0;
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR����㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2start/cod2cdsEnd
	 */
	protected int cod2UTRstartmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR���յ㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2atg/cod2End
	 */
	protected int cod2UTRendmRNA = GffCodAbs.LOC_ORIGINAL;

	/**
	 * �����������ӡ��ں��ӻ����ڸ�ת¼����
	 * ��codLocExon��codLocIntron�Ƚϼ���
	 */
	protected int codLoc = 0;
	
	/**
	 * ��ת¼�����ĸ�λ��
	 * ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
	 * @return
	 */
	public int getCodLoc() {
		return codLoc;
	}
	/**
	 * ��ת¼�����ĸ�λ��
	 * ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
	 * @return
	 */
	public int getCodLocUTR() {
		return codLocUTR;
	}
	/**
	 * ���굽��ת¼�����ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
	 * @return
	 */
	public int getCod2Tss() {
		return cod2TSS;
	}
	/**
	 * ���굽��ת¼���յ�ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
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
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ����
	 * ʵ����Ŀ����1��ʼ����
	 * @return
	 */
	public int getExInNum() {
		return numExIntron;
	}
	/**
	 * ���굽��������/�ں������ľ��룬����������
	 * @return
	 */
	public int getCod2ExInStart() {
		return cod2ExInStart;
	}
	/**
	 * ���굽��������/�ں����յ�ľ��룬����������
	 * @return
	 */
	public int getCod2ExInEnd() {
		return cod2ExInEnd;
	}
	/**
	 * ���굽ATG�ľ��룬����������.
	 * ��ATG����Ϊ����������Ϊ����
	 * @return
	 */
	public int getCod2ATG() {
		return cod2ATG;
	}
	/**
	 * ���굽UAG�����һ������ľ��룬����������.
	 * ��UAG����Ϊ����������Ϊ����
	 * @return
	 */
	public int getCod2UAG() {
		return cod2UAG;
	}
	/**
	 * ʹ��ǰ���ж���UTR��<br>
	 * ���������UTR�У��������UTR����㣬ע�������ȥ���ں��� <br>
	 */
	public int getCod2UTRstartmRNA() {
		return cod2UTRstartmRNA;
	}
	/**
	 * ʹ��ǰ���ж���UTR��<br>
	 * ���������UTR�У��������UTR���յ㣬ע�������ȥ���ں���<br>
	 */
	public int getCod2UTRendmRNA() {
		return cod2UTRendmRNA;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽��ת¼��atg�ľ���
	 * ��ȥ���ں��ӵ�ֱ����cod2atg/cod2End
	 * ��������ں����У���Ϊ�ܴ�ĸ��������-10000000
	 */
	public int getCod2ATGmRNA() {
		return cod2ATGmRNA;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽UAG�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * �������յ�����Ϊ����������Ϊ����<br>
	 * ��������ں����У���Ϊ�ܴ�ĸ��������-10000000
	 */
	public int getCod2UAGmRNA() {
		return cod2UAGmRNA;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽TSS�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * ֻ�е����괦���������в��о��룬�������ں���\
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	public int getCod2TSSmRNA() {
		return cod2TSSmRNA;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽TES�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	public int getCod2TESmRNA() {
		return cod2TESmRNA;
	}
	
	/**
	 */
	private void searchCoord()
	{
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
	 * ��һ������ģ����������뱾 ������/�ں��� �� ���/�յ� �ľ���
	 */
	protected abstract void setCod2ExInStartEnd();
	/**
	 * ���������ڵڼ��������ӻ��ں�����
	 * ����ָ�������������ӻ����ں���
	 * �Ƿ���UTR��
	 * ͬʱ���		
	 * cod2ATG
		cod2cdsEnd 
		cod2start 
		cod2end 
		��
	 */
	protected void codSearchNum()
	{
		int ExIntronnum = getLocExInNum(coord);
		if (ExIntronnum == 0) {
			codLoc = COD_LOC_OUT;
		}
		else if (ExIntronnum > 0) {
			codLoc = COD_LOC_EXON;
			if((coord < ATGsite && cis5to3) || (coord > ATGsite && !cis5to3)){        //����С��atg����5��UTR��,Ҳ������������
				codLocUTR = COD_LOCUTR_5UTR;
			}
			else if((coord > UAGsite && cis5to3) || (coord < UAGsite && !cis5to3)){       //����cds��ʼ������3��UTR��
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
	 * ��������5UTR����������ʱʹ��
	 * ����Ϊ����
	 */
	protected abstract void setCod2UTR5();
	
	/**
	 * ��������3UTR����������ʱʹ��
	 * ����Ϊ����
	 */
	protected abstract void setCod2UTR3();
	/**
	 * ����EXON��ʱ��ʹ�ã���cod�뱾mRNA�������յ�ľ���
	 * �������ں���
	 */
	protected abstract void setCod2StartEndmRNA();
	/**
	 * ����EXON��ʱ��ʹ�ã���cod�뱾mRNA��atg��uag�ľ���
	 * �������ں���
	 */
	protected abstract void setCod2StartEndCDS();

	/**
	 * ����ĳ����������ڵ��ں�����������Ŀ
	 */
	HashMap<Integer, Integer> hashLocExInNum;
	/**
	 * @param location �õ������ڵڼ��������ӻ��ں�����
	 * @return
	 */
	protected abstract int getLocExInNum(int location);
	/**
	 * ����ĳ�����굽���ڵ��ں���/���������ľ���
	 */
	HashMap<Integer, Integer> hashLocExInStart;
	/**
	 * ���굽������/�ں��� ������
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
	 */
	protected abstract int getLoc2ExInStart(int location);
	/**
	 * ����ĳ�����굽���ڵ��ں���/�������յ�ľ���
	 */
	HashMap<Integer, Integer> hashLocExInEnd;
	/**
	 * ���굽������/�ں��� �յ����
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
	 */
	protected abstract int getLoc2ExInEnd(int location);
	/**
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
	 */
	public abstract int getLocdistanceSite(int location, int mRNAnum);
	/**
	 * ��������֮��ľ��룬mRNA���棬��loc1��loc2����ʱ�����ظ�������loc1��loc2����ʱ����������
	 * Ҫ�����������궼��exon��.��������ϣ��򷵻�GffCodAbs.LOC_ORIGINAL
	 * @param loc1 ��һ������
	 * @param loc2 �ڶ�������
	 */
	public int getLocDistance(int loc1, int loc2)
	{
		int locSmall = 0; int locBig = 0;
		if (cis5to3) {
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
		
		if ((cis5to3 && loc1 < loc2) || (!cis5to3 && loc1 > loc2)) {
			return -Math.abs(distance);
		}
		return Math.abs(distance);
	}
	
	/**
	 * ָ��һ������һ���յ����꣬��������������������������ȡ����������
	 * ���ջ���ķ�������
	 * ��С����ν����󷵻ز����� startLoc��EndLoc�Ĵ�С��ϵ
	 * ������������겻���������У��򷵻�null
	 * @return
	 */
	public ArrayList<int[]> getRangeIso(int startLoc, int EndLoc)
	{

		ArrayList<int[]> lsresult = new ArrayList<int[]>();
		int start = 0;
		int end = 0;
		if (cis5to3) {
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
	
	
	
	
}
