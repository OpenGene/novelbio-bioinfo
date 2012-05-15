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
 * ��¼��ת¼���ľ���������Ϣ,
 * ��һ�ʼ��exon����Ϣ��exon�ɶԳ��֣�Ϊint[2] 
 * 0: ����������㣬�����䣬��1��ʼ����<br>
 * 1: ���������յ㣬�����䣬��1��ʼ����<br>
 * ���ջ���ķ����������
 * ����������С�������У���int0&lt;int1
 * ���������Ӵ�С���У���int0&gt;int1
 * @return
 */
public abstract class GffGeneIsoInfo extends ListAbsSearch<ExonInfo, ListCodAbs<ExonInfo>, ListCodAbsDu<ExonInfo, ListCodAbs<ExonInfo>>>{
	private static final Logger logger = Logger.getLogger(GffGeneIsoInfo.class);
	private static final long serialVersionUID = -6015332335255457620L;
	/** ���codInExon������������ */
	public static final int COD_LOC_EXON = 100;
	/** ���codInExon�����ں����� */
	public static final int COD_LOC_INTRON = 200;
	/** ���codInExon����ת¼����  */
	public static final int COD_LOC_OUT = 300;
	/**  ���codInExon����5UTR��  */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**  ���codInExon����3UTR�� */
	public static final int COD_LOCUTR_3UTR = 3000;
	/** ���codInExon����UTR�� */
	public static final int COD_LOCUTR_OUT = 0;	
	/** ���鶯������ΪTss����5000bp */
	public static int PROMOTER_INTERGENIC_MAMMUM = 5000;
	/**  ���鶯��ΪDistal Promoter Tss����1000bp�����ڵľ�ΪProximal Promoter */
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
	/** �趨�����ת¼������γ��ȣ�Ĭ��Ϊ0 */
	protected int upTss = 0;
	/** �趨�����ת¼������γ��ȣ�Ĭ��Ϊ0  */
	protected int downTss=0;
	/**  �趨�����ת¼�յ�����γ��ȣ�Ĭ��Ϊ0 */
	protected int upTes=0;
	/** �趨�����β��������ĳ��ȣ�Ĭ��Ϊ0 */
	protected int downTes=100;
	String chrID = "";
	private int taxID = 0;
	/** ��ת¼����ATG�ĵ�һ���ַ����꣬��1��ʼ����  */
	protected int ATGsite = ListCodAbs.LOC_ORIGINAL;
	/** ��ת¼����Coding region end�����һ���ַ����꣬��1��ʼ���� */
	protected int UAGsite = ListCodAbs.LOC_ORIGINAL;
	/** ��ת¼���ĳ��� */
	protected int lengthIso = ListCodAbs.LOC_ORIGINAL;
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
	public GffGeneIsoInfo(String IsoName, GffDetailGene gffDetailGene, String geneType) {
		super.listName = IsoName;
		this.flagTypeGene = geneType;
		this.chrID = gffDetailGene.getParentName();
		setTssRegion(gffDetailGene.getTssRegion()[0], gffDetailGene.getTssRegion()[1]);
		setTesRegion(gffDetailGene.getTesRegion()[0], gffDetailGene.getTesRegion()[1]);
	}
	/**
	 * ���ظû��������
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
	 * ����gffDetailGene���趨
	 * ����Tss��Χ����Ϊ����������Ϊ����
	 * @param upTss
	 * @param downTss
	 */
	protected void setTssRegion(int upTss, int downTss) {
		this.upTss = upTss;
		this.downTss = downTss;
	}
	/**
	 * ����gffDetailGene���趨
	 * @param upTes
	 * @param downTes
	 */
	protected void setTesRegion(int upTes, int downTes) {
		this.upTes = upTes;
		this.downTes = downTes;
	}
	/**
	 * coord�Ƿ���promoter����ķ�Χ�ڣ���Tss����UpStreamTSSbp��Tss����DownStreamTssbp
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
	 * coord�Ƿ���geneEnd����ķ�Χ��
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
	 * coord�Ƿ��ڸ�ת¼������promoter��geneEnd�ӳ�����ķ�Χ��
	 * @return
	 */
	public boolean isCodInIsoExtend(int coord) {
		int codLoc = getCodLoc(coord);
		return (codLoc != COD_LOC_OUT) || isCodInIsoTss(coord) || isCodInIsoGenEnd(coord);
	}
	/**
	 * cod�Ƿ��ڱ�����
	 * �����ת¼���ǷǱ���RNA��ֱ�ӷ���false��
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
	 * û���趨tss��tes�ķ�Χ
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
	 * �Ƿ���mRNA��atg��uag��
	 * ��ʱֻ��ʹ��UCSCgene
	 * @return
	 */
	public boolean ismRNA() {
		return mRNA;
	}
	/**
	 * ֻ����������õ�ˮ�������Ͻ�GFF�ļ���
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
	 * ����ת¼�����ATG��UAG���꣬<br>
	 * ������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 * ���Զ��ж����������Ƿ�С�����е�atg���յ��Ƿ�������е�uag
	 * �ǵĻ����Ż��趨������Ͳ��趨
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
	 * ����ǷǱ���RNA����atg��uag����Ϊ���һλ
	 */
	public void setATGUAGncRNA() {
		if (ATGsite < 0 && UAGsite <0) {
			ATGsite = get(size() - 1).getEndCis();
			UAGsite = get(size() - 1).getEndCis();
		}
	}	
	/**
	 * ��ת¼����ATG�ĵ�һ���ַ����꣬��1��ʼ�������Ǳ�����
	 * @return
	 */
	public int getATGsite() {
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
		return (int)get(0).getStartCis();
	}
	/**
	 * ��ת¼����Coding region end�����һ���ַ����꣬��1��ʼ�������Ǳ�����
	 * @return
	 */
	public int getTESsite() {
		return get(size() -1).getEndCis();
	}
	public int getExonNum() {
		return size();
	}
	/**
	 * ���5UTR�ĳ���
	 * @return
	 */
	public int getLenUTR5() {
		return Math.abs(super.getLocDistmRNA(getTSSsite(), this.ATGsite) ) + 1;
	}
	
	/**
	 * ���3UTR�ĳ���
	 * @return
	 */
	public int getLenUTR3() {
		return Math.abs(super.getLocDistmRNA(this.UAGsite, getTESsite() )) + 1;
	}
	 /**
     * @param num ָ���ڼ���������������򷵻�-1000000000, 
     * num Ϊʵ�ʸ��������num=0�򷵻�ȫ��Exon�ĳ��ȡ�
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
     * @param num ָ���ڼ���������������򷵻�-1000000000, 
     * num Ϊʵ�ʸ��������num=0�򷵻�ȫ��Intron�ĳ��ȡ�
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
	 * ��ת¼�����ĸ�λ��
	 * 0: ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
	 * 1: ��COD_LOCUTR_5UTR��COD_LOCUTR_3UTR������
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
			if((coord < ATGsite && isCis5to3()) || (coord > ATGsite && !isCis5to3())){        //����С��atg����5��UTR��,Ҳ������������
				codLoc[1] = COD_LOCUTR_5UTR;
			}
			else if((coord > UAGsite && isCis5to3()) || (coord < UAGsite && !isCis5to3())){       //����cds��ʼ������3��UTR��
				codLoc[1] = COD_LOCUTR_3UTR; 
			}
		}
		else {
			codLoc[0] = COD_LOC_INTRON;
		}
		return codLoc;
	}
	/**
	 * ��ת¼�����ĸ�λ��
	 * ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
	 * @return
	 */
	public int getCodLoc(int coord) {
		return getCodLocInfo(coord)[0];
	}
	
	/**
	 * ��ת¼�����ĸ�λ��
	 * ��COD_LOCUTR_5UTR��COD_LOCUTR_3UTR������
	 * @return
	 */
	public int getCodLocUTR(int coord) {
		return getCodLocInfo(coord)[1];
	}
	/**
	 * ���굽��ת¼�����ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
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
	 * ���굽��ת¼���յ�ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
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
	 * ���굽ATG�ľ��룬����������. 
	 * ��ATG����Ϊ����������Ϊ����
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
	 * ���굽UAG�����һ������ľ��룬����������.
	 * ��UAG����Ϊ����������Ϊ����
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
	 * ʹ��ǰ���ж���UTR��<br>
	 * ���������UTR�У��������UTR����㣬ע�������ȥ���ں��� <br>
	 */
	public int getCod2UTRstartmRNA(int coord) {
		int location = getCodLocUTR(coord);
		if (location == COD_LOCUTR_5UTR) {
			return getLocDistmRNA(getTSSsite(), coord);
		}
		else if (location == COD_LOCUTR_3UTR) {
			return getLocDistmRNA(getUAGsite(), coord);
		}
		logger.error("����UTR��");
		return 0;
	}
	/**
	 * ʹ��ǰ���ж���UTR��<br>
	 * ���������UTR�У��������UTR���յ㣬ע�������ȥ���ں���<br>
	 */
	public int getCod2UTRendmRNA(int coord) {
		int location = getCodLocUTR(coord);
		if (location == COD_LOCUTR_5UTR) {
			return getLocDistmRNA(coord, getATGsite());
		}
		else if (location == COD_LOCUTR_3UTR) {
			return getLocDistmRNA(coord, getTESsite());
		}
		logger.error("����UTR��");
		return 0;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽��ת¼��atg�ľ���
	 * ��ȥ���ں��ӵ�ֱ����cod2atg/cod2End
	 * ��������ں����У���Ϊ�ܴ�ĸ��������-10000000
	 */
	public int getCod2ATGmRNA(int coord) {
		return getLocDistmRNA(ATGsite, coord);
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽UAG�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * �������յ�����Ϊ����������Ϊ����<br>
	 * ��������ں����У���Ϊ�ܴ�ĸ��������-10000000
	 */
	public int getCod2UAGmRNA(int coord) {
		return getLocDistmRNA(UAGsite, coord);
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽TSS�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * ֻ�е����괦���������в��о��룬�������ں���\
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	public int getCod2TSSmRNA(int coord) {
		return getLocDistmRNA(getTSSsite(), coord);
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽TES�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * ��Ϊcod���������У����Կ϶���tES���Σ����Ը�ֵʼ��Ϊ����
	 */
	public int getCod2TESmRNA(int coord) {
		return getLocDistmRNA(getTESsite(), coord);
	}
	/**
	 * �����ܺͱ�loc���һ���������ͷ��nr�����꣬��1��ʼ����
	 * @param location
	 * @return
	 */
	public int getLocAAbefore(int location) {
		int startLen = getLocDistmRNA(ATGsite, location);
		return  getLocDistmRNASite(location, -startLen%3);
	}
	/**
	 * �����ܺͱ�loc���һ���������ͷ��nr��ƫ�ƣ�Ҳ������ǰƫ�Ƽ���������������ں���
	 * ��Ϊ������������˵��������
	 * @param location
	 * @return �����λ�����һ��������ĵ�һ��λ�㣬�򷵻�0
	 */
	public int getLocAAbeforeBias(int location) {
		int startLen = getLocDistmRNA(ATGsite,location);
		return   -startLen%3;
	}
	/**
	 * �����ܺͱ�loc���һ���������β��nr�����꣬��1��ʼ����
	 * @param location
	 * @return
	 */
	public int getLocAAend(int location) {
		int startLen = getLocDistmRNA(ATGsite, location);
		return  getLocDistmRNASite(location, 2 - startLen%3);
	}
	/**
	 * �����ܺͱ�loc���һ���������β��nr��ƫ�ƣ�Ҳ�������ƫ�Ƽ���������������ں���
	 * ��Ϊ������������˵��������
	 * @param location
	 * @return �����λ�����һ������������һ��λ�㣬Ҳ���ǵ���λ���򷵻�0
	 */
	public int getLocAAendBias(int location) {
		int startLen = getLocDistmRNA(ATGsite,location);
		return 2 - startLen%3;
	}
	/**
	 * ָ��һ������һ���յ����꣬��������������������������ȡ����������
	 * ���ջ���ķ�������
	 * ��С����ν����󷵻ز����� startLoc��EndLoc�Ĵ�С��ϵ
	 * ������������겻���������У��򷵻�null
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
	 * ���ظ�GeneIsoName����Ӧ��CopedID����Ϊ��NM�����Բ���Ҫָ��TaxID
	 * @return
	 */
	public CopedID getCopedID() {
		return new CopedID(getName(), taxID);
	}
	/**
	 * ������ʽ�Ķ�λ����
	 * @return
	 * null: ���ڸ�ת¼����
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
	 * ������ʽ�Ķ�λ����, <b>������gffDetailGene���趨tss��tes������</b><br>
	 * null: ���ڸ�ת¼����
	 * 
	 * ָ��������������������peakץ��������ע�ͣ���Ҫ��ɸѡ�����ʵ�peakȻ���������ȽϹ���
	 * �����ϵĻ�����
	 * @param filterTss �Ƿ����tssɸѡ<b>ֻ�е�filterGeneBodyΪfalseʱ��tss���βŻᷢ������</b>
	 * @param filterGenEnd �Ƿ����geneEndɸѡ<b>ֻ�е�filterGeneBodyΪfalseʱ��geneEnd���βŻᷢ������</b>
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
	 * �������ת¼������һ�£����ܽ��бȽ�
	 * �Ƚ�����ת¼��֮��Ĳ���ж��
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
	 * �����ǰ�˳����ӵ�ID
	 * ���Ҫȷ��
	 * ��ת¼�����exon���꣬GFF3��exon�ĸ�ʽ�� <br>
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ� <br>
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж� <br>
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
			logger.error("NCBI��Gff�ļ������⣬��exon���λ���������exon������check: " + locStart + " " + locEnd);
		}
	}
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
		String genetitle = getGFFformatExonMISO(geneID, title,strand);
		return genetitle;
	}
	protected abstract String getGTFformatExon(String geneID, String title, String strand);
	protected abstract String getGFFformatExonMISO(String geneID, String title, String strand);

	
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
	 * ��дequal
	 * �Ƚ��Ƿ�Ϊͬһ��ת¼��
	 * ���Ƚ�����ת¼�������֣�Ҳ���Ƚ�coord
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		GffGeneIsoInfo otherObj = (GffGeneIsoInfo)obj;
		//���֣�����յ㣬ATG��UAG�������ӳ��ȣ�ת¼�����ֵȶ�һ��
		boolean flag =  this.getTaxID() == otherObj.getTaxID() && this.getChrID().equals(otherObj.getChrID()) && this.getATGsite() == otherObj.getATGsite()
		&& this.getUAGsite() == otherObj.getUAGsite() && this.getTSSsite() == otherObj.getTSSsite()
		&& this.getListLen() == otherObj.getListLen();
		if (flag && compIso(otherObj) ) {
			return true;
		}
		return false;
	}
	
	/**
	 * ��дhash
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
	 * ��þ���ı�������
	 * û�н���ͷ���new list-exonInfo
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
	 * ����nrλ�㣬����Ϊ����ATG����aaλ��
	 * ֱ�Ӹ���nr��ʵ��λ��
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