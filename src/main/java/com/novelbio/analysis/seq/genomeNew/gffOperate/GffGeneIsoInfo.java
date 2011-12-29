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
	/**
	 * ���鶯������ΪTss����5000bp
	 */
	public static int PROMOTER_INTERGENIC_MAMMUM = 5000;
	/**
	 * �趨DISTAL Promoter������TSS���εĶ���bp�⣬Ĭ��1000
	 * Ŀǰ����annotation�������й�
	 * 1000bp����Ϊ Proximal Promoter_
	 * @param pROMOTER_DISTAL_MAMMUM
	 */
	public static void setPROMOTER_DISTAL_MAMMUM(int pROMOTER_DISTAL_MAMMUM) {
		PROMOTER_DISTAL_MAMMUM = pROMOTER_DISTAL_MAMMUM;
	}
	/**
	 * �趨intergeneic������TSS���εĶ���bp�⣬Ĭ��5000
	 * Ŀǰ����annotation�������й�
	 * @param pROMOTER_INTERGENIC_MAMMUM
	 */
	public static void setPROMOTER_INTERGENIC_MAMMUM(
			int pROMOTER_INTERGENIC_MAMMUM) {
		PROMOTER_INTERGENIC_MAMMUM = pROMOTER_INTERGENIC_MAMMUM;
	}
	/**
	 * ���鶯��ΪDistal Promoter Tss����1000bp�����ڵľ�ΪProximal Promoter
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
	 * ���ظû��������
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
	
	/**
	 * coord�Ƿ���promoter����ķ�Χ�ڣ���Tss����UpStreamTSSbp��Tss����DownStreamTssbp
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
	 * coord�Ƿ���gene�⣬������geneEnd�ӳ�����ķ�Χ��
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
	 * coord�Ƿ��ڸ�ת¼������promoter��geneEnd�ӳ�����ķ�Χ��
	 * @return
	 */
	public boolean isCodInIsoExtend() {
		return (codLoc != COD_LOC_OUT) || isCodInIsoTss() || isCodInIsoGenEnd();
	}
	/**
	 * cod�Ƿ��ڱ�����
	 * �����ת¼���ǷǱ���RNA��ֱ�ӷ���false��
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
	public int getExonNum()
	{
		return lsIsoform.size();
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
	protected int numExIntron = 0;
	
	/**
	 * ������5UTR��3UTR���ǲ���
	 */
	protected int codLocUTR = COD_LOCUTR_OUT;
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
	 * ��COD_LOCUTR_5UTR��COD_LOCUTR_3UTR������
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
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ0
	 * ʵ����Ŀ����1��ʼ����
	 * @return
	 */
	public int getCodExInNum() {
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
	 * ��Ϊcod���������У����Կ϶���tES���Σ����Ը�ֵʼ��Ϊ����
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
	 * ��ʼ������
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
			if((coord < ATGsite && isCis5to3()) || (coord > ATGsite && !isCis5to3())){        //����С��atg����5��UTR��,Ҳ������������
				codLocUTR = COD_LOCUTR_5UTR;
			}
			else if((coord > UAGsite && isCis5to3()) || (coord < UAGsite && !isCis5to3())){       //����cds��ʼ������3��UTR��
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
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * Ϊʵ����Ŀ
	 * ������Ϊ0
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
	 * @param location ����
	 * @param mRNAnum
	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
	 */
	public abstract int getLocDistmRNASite(int location, int mRNAnum);
	
	/**
	 * �����ܺͱ�loc���һ���������ͷ��nr�����꣬��1��ʼ����
	 * @param location
	 * @return
	 */
	public int getLocAAbefore(int location) {
		int startLen = getLocDistmRNA(location,ATGsite);
		return  getLocDistmRNASite(location, -startLen%3);
	}
	/**
	 * �����ܺͱ�loc���һ���������ͷ��nr��ƫ�ƣ�Ҳ������ǰƫ�Ƽ������
	 * ��Ϊ����
	 * @param location
	 * @return
	 */
	public int getLocAAbeforeBias(int location) {
		int startLen = getLocDistmRNA(location,ATGsite);
		return   -startLen%3;
	}
	/**
	 * �����ܺͱ�loc���һ���������β��nr�����꣬��1��ʼ����
	 * @param location
	 * @return
	 */
	public int getLocAAend(int location)
	{
		int startLen = getLocDistmRNA(location,ATGsite);
		return  getLocDistmRNASite(location, 2 - startLen%3);
	}
	/**
	 * ��������֮��ľ��룬mRNA���棬��loc1��loc2����ʱ�����ظ�������loc1��loc2����ʱ����������
	 * Ҫ�����������궼��exon��.��������ϣ��򷵻�GffCodAbs.LOC_ORIGINAL
	 * @param loc1 ��һ������
	 * @param loc2 �ڶ�������
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
	 * ������ʽ�Ķ�λ����
	 * @return
	 * null: ���ڸ�ת¼����
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
	 * ������ʽ�Ķ�λ����
	 * null: ���ڸ�ת¼����
	 * 
	 * ָ��������������������peakץ��������ע�ͣ���Ҫ��ɸѡ�����ʵ�peakȻ���������ȽϹ���
	 * �����ϵĻ�����
	 * @param filterTss �Ƿ����tssɸѡ��null�����У�������У���ô������int[2],0��tss���ζ���bp  1��tss���ζ���bp����Ϊ���� <b>ֻ�е�filterGeneBodyΪfalseʱ��tss���βŻᷢ������</b>
	 * @param filterGenEnd �Ƿ����geneEndɸѡ��null�����У�������У���ô������int[2],0��geneEnd���ζ���bp  1��geneEnd���ζ���bp����Ϊ����<b>ֻ�е�filterGeneBodyΪfalseʱ��geneEnd���βŻᷢ������</b>
	 * @param filterGeneBody �Ƿ���geneBody��true��������geneBody�Ļ���ȫ��ɸѡ������false��������geneBody��ɸѡ<br>
	 * <b>��������ֻ�е�filterGeneBodyΪfalseʱ���ܷ�������</b>
	 * @param filter5UTR �Ƿ���5UTR��
	 * @param filter3UTR �Ƿ���3UTR��
	 * @param filterExon �Ƿ�����������
	 * @param filterIntron �Ƿ����ں�����
	 * 0-n:�����loc��Ϣ<br>
	 * n+1: ������<br>
	 * n+2: ������Ϣ<br>
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
	 * ��lsIsoҲ����
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
	//�����м�״̬
	HashMap<String, ArrayList<CompSubArrayCluster>> hashTmp = new HashMap<String, ArrayList<CompSubArrayCluster>>();
	/**
	 * �Ƚ�����ת¼��֮��Ĳ���ж��
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
	 * �Ƚ�����ת¼��֮��Ĳ���ж��
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
	 * ר�����cufflink�Ľ��
	 * @param locStart
	 * @param locEnd
	 */
	protected abstract void addExonCufflinkGTF(int locStart, int locEnd);
	
	
	/**
	 * ��ø�ת¼������㣬�����Ƿ���
	 * @return
	 */
	public abstract int getStartAbs();
	/**
	 * ��ø�ת¼�����յ㣬�����Ƿ���
	 * @return
	 */
	public abstract int getEndAbs();
	/**
	 * ���ظû����GTF��ʽ�ļ���ĩβ�л��з�
	 * @param title ��GTF�ļ�������
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
	 * ���ظû����GTF��ʽ�ļ���ĩβ�л��з�
	 * @param title ��GTF�ļ�������
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
	 * ������������ӵĳ���֮��
	 */
	public int getIsoLen() {
		int isoLen = 0;
		for (int[] exons : lsIsoform) {
			isoLen = isoLen + Math.abs(exons[1] - exons[0]);
		}
		return isoLen;
	}
	
	/**
	 * ���ܲ��Ǻܾ�ȷ
	 * ���ؾ���Tss��һϵ�������ʵ������
	 * @param is ����� TSS��������Ϣ��Ʃ��-200��-100��-100��100��100��200�ȣ�ÿһ�����һ��int[2]��ע��int[1]����С��int[2]
	 * @return
	 * ����õĽ������int[0] < int[1]
	 * ���� int[0] > int[1]
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
	 * �Ƚ��Ƿ�Ϊͬһ��ת¼��
	 */
	public boolean equals(Object obj) {

		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		GffGeneIsoInfo otherObj = (GffGeneIsoInfo)obj;
		//���֣�����յ㣬ATG��UAG�������ӳ��ȣ�ת¼�����ֵȶ�һ��
		boolean flag =  this.getTaxID() == otherObj.getTaxID() && this.getChrID().equals(otherObj.getChrID()) && this.getATGSsite() == otherObj.getATGSsite()
		&& this.getUAGsite() == otherObj.getUAGsite() && this.getTSSsite() == otherObj.getTSSsite()
		&& this.getIsoLen() == otherObj.getIsoLen() && this.getIsoName().equals(otherObj.getIsoName());
		
		if (flag && compIso(otherObj.getIsoInfo()) ) {
			return true;
		}
		return false;
	}
	

	
	/**
	 * �����ӱȽ����һģһ���򷵻�true��
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
	 * ��þ���ı�������
	 * û�н���ͷ���new list-int[]
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
