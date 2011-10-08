package com.novelbio.analysis.seq.genomeNew2.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections15.map.Flat3Map;
import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.copeID.CopedID;

/**
 * ��¼GffGene�е�ת¼����Ϣ
 * @author zong0jie
 *
 */
public abstract class GffGeneIsoInfo {
	
	/**
	 * ���鶯������ΪTss����5000bp
	 */
	public static final int PROMOTER_INTERGENIC_MAMMUM = 5000;
	/**
	 * ���鶯��ΪDistal Promoter Tss����1000bp�����ڵľ�ΪProximal Promoter
	 */
	public static final int PROMOTER_DISTAL_MAMMUM = 1000;
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
	
	
	
	
	private int taxID = 0;
	protected void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * �趨�����ת¼������γ��ȣ�Ĭ��Ϊ3000bp
	 */
	protected static int UpStreamTSSbp = 3000;
	
	/**
	 * �趨�����ת¼������γ��ȣ�Ĭ��Ϊ2000bp
	 */
	protected static int DownStreamTssbp=2000;
	/**
	 * �趨�����β��������ĳ��ȣ�Ĭ��Ϊ100bp
	 * ����˵������������������100bp����Ϊ��3��UTR
	 * ��ô��ͳ��peak�����ʱ����������������û�б�peak�����ǣ���ͳ�Ƹ�������reads�����
	 */
	protected static int GeneEnd3UTR=100;
	/**
	 * �趨�����ת¼����յ�λ����Ϣ
	 * @param UpStreamTSSbp �趨�����ת¼������γ��ȣ�Ĭ��Ϊ3000bp
	 * @param DownStreamTssbp �趨�����ת¼������γ��ȣ�Ĭ��Ϊ2000bp
	 * @param GeneEnd3UTR �趨�����β��������ĳ��ȣ�Ĭ��Ϊ100bp
	 */
	protected static void setCodLocation(int upStreamTSSbp, int downStreamTssbp, int geneEnd3UTR) {
		UpStreamTSSbp = upStreamTSSbp;
		DownStreamTssbp = downStreamTssbp;
		GeneEnd3UTR = geneEnd3UTR;
	}
	


	
	
	
	private static final Logger logger = Logger.getLogger(GffGeneIsoInfo.class);
	
	public GffGeneIsoInfo(String IsoName, GffDetailGene gffDetailGene) {
		this.IsoName = IsoName;
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
	public abstract boolean isCis5to3();
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
	protected abstract void addExonGFFCDSUTR(int locStart, int locEnd);
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
	 * ����ĳ���������ڵ��ں���/�����ӵ���Ŀ
	 */
	HashMap<Integer, Integer> hashLocExInNum;
	/**
	 * ����ĳ�����굽���ڵ��ں���/���������ľ���
	 */
	HashMap<Integer, Integer> hashLocExInStart;
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
	 * ���ظ�GeneIsoName����Ӧ��CopedID����Ϊ��NM�����Բ���Ҫָ��TaxID
	 * @return
	 */
	public CopedID getCopedID()
	{
		return new CopedID(getIsoName(), taxID, false);
	}
	/**
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * Ϊʵ����Ŀ
	 * ������Ϊ0
	 * @return
	 */
	protected abstract int getLocExInNum(int location);
	
	public abstract GffGeneIsoInfoCod setCod(int coord);
	
}
