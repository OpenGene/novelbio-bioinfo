package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;

/**
 * 	��дhash����������������Ϣ����������taxID��chrID��atg��uag��tss�����ȣ��Լ�ÿһ��exon����Ϣ<br>
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
	/** ���codInExon��CDS�� */
	public static final int COD_LOCUTR_CDS = 7000;
	
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
	
	public static HashSet<GeneType> hashMRNA = new HashSet<GeneType>();
	static {
		hashMRNA.add(GeneType.mRNA);
		hashMRNA.add(GeneType.PSEU_TRANSCRIPT);
		hashMRNA.add(GeneType.mRNA_TE);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private GeneType flagTypeGene = GeneType.mRNA;
	/** �趨�����ת¼������γ��ȣ�Ĭ��Ϊ0 */
	protected int upTss = 0;
	/** �趨�����ת¼������γ��ȣ�Ĭ��Ϊ0  */
	protected int downTss=0;
	/**  �趨�����ת¼�յ�����γ��ȣ�Ĭ��Ϊ0 */
	protected int upTes=0;
	/** �趨�����β��������ĳ��ȣ�Ĭ��Ϊ0 */
	protected int downTes=100;
	/** ��ת¼����ATG�ĵ�һ���ַ����꣬��1��ʼ����  */
	protected int ATGsite = ListCodAbs.LOC_ORIGINAL;
	/** ��ת¼����Coding region end�����һ���ַ����꣬��1��ʼ���� */
	protected int UAGsite = ListCodAbs.LOC_ORIGINAL;
	/** ��ת¼���ĳ��� */
	protected int lengthIso = ListCodAbs.LOC_ORIGINAL;

	GffDetailGene gffDetailGeneParent;
	
	GeneID geneID;
	
	public GffGeneIsoInfo(String IsoName, GeneType geneType) {
		super.listName = IsoName;
		this.flagTypeGene = geneType;
	}
	
	public GffGeneIsoInfo(String IsoName, GffDetailGene gffDetailGene, GeneType geneType) {
		super.listName = IsoName;
		this.flagTypeGene = geneType;
		this.gffDetailGeneParent = gffDetailGene;
		setTssRegion(gffDetailGene.getTssRegion()[0], gffDetailGene.getTssRegion()[1]);
		setTesRegion(gffDetailGene.getTesRegion()[0], gffDetailGene.getTesRegion()[1]);
	}

	/**
	 * ���ظû��������
	 * @return
	 */
	public GeneType getGeneType() {
		return flagTypeGene;
	}
	public int getTaxID() {
		if (gffDetailGeneParent == null) {
			return 0;
		}
		return gffDetailGeneParent.getTaxID();
	}
	public void setGffDetailGeneParent(GffDetailGene gffDetailGeneParent) {
		this.gffDetailGeneParent = gffDetailGeneParent;
	}
	public GffDetailGene getParentGffDetailGene() {
		return gffDetailGeneParent;
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
	public boolean isCodInIsoTss(int coord) {
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
	public boolean isCodInIsoGenEnd(int coord) {
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
	public boolean isCodInAAregion(int coord) {
		if (!ismRNA() || getCodLoc(coord) != GffGeneIsoInfo.COD_LOC_EXON) {
			return false;
		}
		if (getCod2ATG(coord) < 0 || getCod2UAG(coord) > 0) {
			return false;
		}
		return true;
	}
	public String getChrID() {
		if (gffDetailGeneParent == null) {
			return "";
		}
		return gffDetailGeneParent.getRefID().toLowerCase();
	}
	/**
	 * �Ƿ���mRNA��atg��uag��
	 * @return
	 */
	public boolean ismRNA() {
		return Math.abs(ATGsite - UAGsite) > 10 ?  true : false;
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
		ExonInfo tmpexon = new ExonInfo(this, isCis5to3(), locStart, locEnd);
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
	 * @param atg ��1��ʼ����
	 * @param uag ��1��ʼ����
	 */
	public void setATGUAG(int atg, int uag) {
		if (Math.abs(atg - uag)<=1) {
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
		} else {
			if (ATGsite < 0 || ATGsite < Math.max(atg, uag)) {
				ATGsite = Math.max(atg, uag);
			}
			if (UAGsite < 0 || UAGsite > Math.min(atg, uag)) {
				UAGsite = Math.min(atg, uag);
			}
		}
	}
	/**
	 * <b>�������趨exon</b>
	 * ���ATGsite < 0 && UAGsite < 0������Ϊ�ǷǱ���RNA
	 * ��atg��uag����Ϊ���һλ
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
		return Math.abs(super.getLocDistmRNA(getTSSsite(), this.ATGsite) );
	}
	
	/**
	 * ���3UTR�ĳ���
	 * @return
	 */
	public int getLenUTR3() {
		return Math.abs(super.getLocDistmRNA(this.UAGsite, getTESsite() ) );
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
		else if (num == 0) {
			int allExonLength = 0;
			 // 0-0 0-1 1-0 1-1
			// 2-1 2-0 1-1 1-0 0-1 0-tss cood
			for (int i = 0; i < size(); i++) { 			
				allExonLength = allExonLength + get(i).Length();
			}
			return allExonLength;
		} else {
			num--;
			return get(num).Length();
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
		else if (num == 0) {
			int allIntronLength = 0;
			// 0-0 0-1 1-0 1-1
			// 2-1 2-0 1-1 1-0 0-1 0-tss cood
			for (int i = 1; i < size(); i++) { 
				allIntronLength = allIntronLength + Math.abs(get(i).getStartCis() - get(i-1).getEndCis()) - 1;
			}
			return allIntronLength;
		}
		num--;
		return Math.abs(get(num + 1).getStartCis() - get(num).getEndAbs()) - 1;
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
	 * ��COD_LOCUTR_5UTR��COD_LOCUTR_3UTR��COD_LOCUTR_CDS��
	 * @return
	 */
	public int getCodLocUTRCDS(int coord) {
		return getCodLocInfo(coord)[1];
	}
	/**
	 * ��ת¼�����ĸ�λ��
	 * 0: ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
	 * 1: ��COD_LOCUTR_5UTR��COD_LOCUTR_3UTR������
	 * @return
	 */
	private int[] getCodLocInfo(int coord) {
		int codLoc[] = new int[2];
		int ExIntronnum = getNumCodInEle(coord);
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
			} else {
				codLoc[1] = COD_LOCUTR_CDS; 
			}
		} 
		else {
			codLoc[0] = COD_LOC_INTRON;
		}
		return codLoc;
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
		int location = getCodLocUTRCDS(coord);
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
		int location = getCodLocUTRCDS(coord);
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
	 * ʹ��ǰ���ж���Exon�У����굽UAG���һ����ĸ�ľ��룬mRNAˮƽ
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
	 * ��þ���ı������У������������Ե���getSeqLoc�ķ�����ȡ����
	 * û�н���ͷ���new list-exonInfo
	 * @return
	 */
	public ArrayList<ExonInfo> getIsoInfoCDS() {
		if (Math.abs(ATGsite - UAGsite) <= 1) {
			return new ArrayList<ExonInfo>();
		}
		return getRangeIso(ATGsite, UAGsite);
	}
	/**
	 * ���3UTR����Ϣ
	 * @param startLoc
	 * @param endLoc
	 * @return
	 */
	public ArrayList<ExonInfo> getUTR3seq() {
		return getRangeIso(UAGsite, getTESsite());
	}
	/**
	 * ���5UTR����Ϣ
	 * @return
	 */
	public ArrayList<ExonInfo> getUTR5seq() {
		return getRangeIso(getTSSsite(), ATGsite);
	}
	/**
	 * ָ��һ������һ���յ����꣬��������������������������ȡ����������
	 * ���ջ���ķ�������
	 * ��С����ν����󷵻ز����� startLoc��EndLoc�Ĵ�С��ϵ
	 * ������������겻���������У��򷵻ؿյ�list
	 * @return
	 */
	public ArrayList<ExonInfo> getRangeIso(int startLoc, int EndLoc) {
		ArrayList<ExonInfo> lsresult = new ArrayList<ExonInfo>();
		int start = 0, end = 0;
		
		if (isCis5to3()) {
			start = Math.min(startLoc, EndLoc);
			end = Math.max(startLoc, EndLoc);
		}
		else {
			start = Math.max(startLoc, EndLoc);
			end = Math.min(startLoc, EndLoc);
		}
		int exonNumStart = getNumCodInEle(start) - 1;
		int exonNumEnd =getNumCodInEle(end) - 1;
		
		if (exonNumStart < 0 || exonNumEnd < 0) {
			return lsresult;
		}
		
		if (exonNumStart == exonNumEnd) {
			ExonInfo exonInfo = new ExonInfo(this, isCis5to3(), start, end);
			lsresult.add(exonInfo);
			return lsresult;
		}
		ExonInfo exonInfoStart = new ExonInfo(this, isCis5to3(), start, get(exonNumStart).getEndCis());
		lsresult.add(exonInfoStart);
		for (int i = exonNumStart+1; i < exonNumEnd; i++) {
			lsresult.add(get(i));
		}
		ExonInfo exonInfo2 = new ExonInfo(this, isCis5to3(), get(exonNumEnd).getStartCis(), end);
		lsresult.add(exonInfo2);
		return lsresult;
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
	public boolean isCodLocFilter(int coord, boolean filterTss, boolean filterGenEnd, 
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
		else if (filter5UTR && getCodLocUTRCDS(coord) == COD_LOCUTR_5UTR) {
			filter = true;
		}
		else if (filter3UTR && getCodLocUTRCDS(coord) == COD_LOCUTR_3UTR) {
			filter = true;
		}
		else if (filterExon && getCodLoc(coord) == COD_LOC_EXON) {
			filter = true;
		}
		else if (filterIntron && getCodLoc(coord) == COD_LOC_INTRON) {
			filter = true;
		}
		return filter;
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
		ExonInfo exonInfo = new ExonInfo(this,isCis5to3(), locStart, locEnd);
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
	 * @param geneID �û��������
	 * @param title ��GTF�ļ�������
	 * @return
	 */
	protected String getGTFformat(String geneID, String title) {
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
	protected String getGFFformat(String geneID, String title) {
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
	public ArrayList<int[]> getRegionNearTss(Collection<int[]> isList) {
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
	 * ���Intron��list��Ϣ����ǰ��������
	 * û�н���ͷ���new list-exonInfo
	 * @return
	 */
	public ArrayList<ExonInfo> getLsIntron() {
		ArrayList<ExonInfo> lsresult = new ArrayList<ExonInfo>();
		if (size() == 1) {
			return lsresult;
		}
		ExonInfo intronInfo = null;
		for (int i = 0; i < size(); i++) {
			ExonInfo exonInfo = get(i);
			if (i > 0) {
				if (exonInfo.isCis5to3()) {
					intronInfo.setEndCis(exonInfo.getStartCis() - 1);
				} else {
					intronInfo.setEndCis(exonInfo.getStartCis() + 1);
				}
			}
			if (i == size() - 1) {
				break;
			}
			intronInfo = new ExonInfo();
			lsresult.add(intronInfo);
			intronInfo.setParentListAbs(this);
			intronInfo.setCis5to3(exonInfo.isCis5to3());
			intronInfo.addItemName(exonInfo.getNameSingle());
			
			if (exonInfo.isCis5to3()) {
				intronInfo.setStartCis(exonInfo.getEndCis() + 1);
			} else {
				intronInfo.setStartCis(exonInfo.getEndCis() - 1);
			}
		}
		return lsresult;
	}
	/**
	 * ����nrλ�㣬����Ϊ����ATG����aaλ��
	 * ֱ�Ӹ���nr��ʵ��λ��
	 */
	public int getAAsiteNum(int codSite) {
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
	/**
	 * ���ظ�GeneIsoName����Ӧ��CopedID����Ϊ��NM�����Բ���Ҫָ��TaxID
	 * @return
	 */
	public GeneID getGeneID() {
		if (geneID == null) {
			geneID = new GeneID(getName(), gffDetailGeneParent.getTaxID());
		}
		return geneID;
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
	public String toStringCodLocStrFilter(int coord, boolean filterTss, boolean filterGenEnd, 
			boolean filterGeneBody,boolean filter5UTR, boolean filter3UTR,boolean filterExon, boolean filterIntron) {
		boolean filter = isCodLocFilter(coord, filterTss, filterGenEnd, filterGeneBody, filter5UTR, filter3UTR, filterExon, filterIntron);
		if (filter) {
			return toStringCodLocStr(coord);
		}
		else {
			return null;
		}
	}
	/**
	 * ������ʽ�Ķ�λ����
	 * @return
	 * null: ���ڸ�ת¼����
	 */
	public String toStringCodLocStr(int coord) {
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
				result = result + PROMOTER_INTERGENIC_STR;
			}
			else if (getCod2Tss(coord) > PROMOTER_DISTAL_MAMMUM) {
				result = result + PROMOTER_DISTAL_STR;
			}
			else {
				result = result + PROMOTER_PROXIMAL_STR;;
			}
		}
		else if (isCodInIsoTss(coord) && codLoc != COD_LOC_OUT) {
			result = result + PROMOTER_DOWNSTREAMTSS_STR;
		}
		
		result = result + "Distance_to_Tss_is:" + Math.abs(getCod2Tss(coord)) + " ";
		//UTR
		if (getCodLocUTRCDS(coord) == COD_LOCUTR_5UTR) {
			result = result + "5UTR_";
		}
		else if (getCodLocUTRCDS(coord) == COD_LOCUTR_3UTR) {
			result = result + "3UTR_";
		}
		//exon intron
		if (codLoc == COD_LOC_EXON) {
			result = result + "Exon:exon_Position_Number_is:" + getNumCodInEle(coord);
		}
		else if (codLoc == COD_LOC_INTRON) {
			result = result + "Intron_intron_Position_Number_is:" + Math.abs(getNumCodInEle(coord));
		}
		//gene end
		if (isCodInIsoGenEnd(coord)) {
			result = result + "Distance_to_GeneEnd: "+ getCod2Tes(coord);
		}
		return result;
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
		//���֣�����յ㣬ATG��UAG�������ӳ��� �ȶ�һ��
		boolean flag =  this.getTaxID() == otherObj.getTaxID() && this.getChrID().equals(otherObj.getChrID()) && this.getATGsite() == otherObj.getATGsite()
		&& this.getUAGsite() == otherObj.getUAGsite() && this.getTSSsite() == otherObj.getTSSsite()
		&& this.getListLen() == otherObj.getListLen();
		if (flag && equalsIso(otherObj) ) {
			return true;
		}
		return false;
	}
	/**
	 * ��дhash����������������Ϣ����������taxID��chrID��atg��uag��tss�����ȣ��Լ�ÿһ��exon����Ϣ
	 * @return
	 */
	public int hashCode() {
		String info = this.getTaxID() + "//" + this.getChrID() + "//" + this.getATGsite() + "//" + this.getUAGsite() + "//" + this.getTSSsite() + "//" + this.getListLen();
		for (ExonInfo exonInfo : this) {
			info = info + SepSign.SEP_INFO + exonInfo.getName();
		}
		return   info.hashCode();
	}
	/**
	 * ���ĸ�����Ҳ����gffDetailGene����������
	 */
	public GffGeneIsoInfo clone() {
		GffGeneIsoInfo result = null;
		result = (GffGeneIsoInfo) super.clone();
		result.ATGsite = ATGsite;
		result.gffDetailGeneParent = gffDetailGeneParent;
		result.downTes = downTes;
		result.downTss = downTss;
		result.flagTypeGene = flagTypeGene;
		result.lengthIso = lengthIso;
		result.UAGsite = UAGsite;
		result.upTes = upTes;
		result.upTss = upTss;
		result.geneID = geneID;
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
	
	public static GffGeneIsoInfo createGffGeneIso(String isoName, GffDetailGene gffDetailGene, GeneType geneType, boolean cis5to3) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(isoName, gffDetailGene, geneType);
		} else {
			gffGeneIsoInfo = new GffGeneIsoTrans(isoName, gffDetailGene, geneType);
		}
		return gffGeneIsoInfo;
	}
	public static GffGeneIsoInfo createGffGeneIso(String isoName, GeneType geneType, boolean cis5to3) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(isoName, geneType);
		} else {
			gffGeneIsoInfo = new GffGeneIsoTrans(isoName, geneType);
		}
		return gffGeneIsoInfo;
	}
	/** ��������iso�Ƚϵ���Ϣ
	 * 	double ratio = �ж���exon�ı߽�����ͬ�� / Math.min(gffGeneIsoInfo1.Size, gffGeneIsoInfo2.Size);
	 *  */
	public static double compareIsoRatio(GffGeneIsoInfo gffGeneIsoInfo1, GffGeneIsoInfo gffGeneIsoInfo2) {
		int[] compareInfo = compareIso(gffGeneIsoInfo1, gffGeneIsoInfo2);
		double ratio = (double)compareInfo[0]/Math.min(compareInfo[2], compareInfo[3]);
		return ratio;
	}
	/**
	 * ��������iso�Ƚϵ���Ϣ
	 * 0˵����ȫ����ͬ������ͬ��û�н�����ֱ�ӷ���0
	 * @param gffGeneIsoInfo1
	 * @param gffGeneIsoInfo2
	 * @return int[2] <br>
	 * 0:�ж���exon�ı߽�����ͬ��<br>
	 * 1:����߽���<br>
	 * 2: gffGeneIsoInfo1-Size<br>
	 * 3: gffGeneIsoInfo2-Size<br>
	 */
	public static int[] compareIso(GffGeneIsoInfo gffGeneIsoInfo1, GffGeneIsoInfo gffGeneIsoInfo2) {
		//��ȫû�н���
		if (!gffGeneIsoInfo1.isCis5to3().equals(gffGeneIsoInfo2.isCis5to3()) 
				|| gffGeneIsoInfo1.getEndAbs() <= gffGeneIsoInfo2.getStartAbs() 
				|| gffGeneIsoInfo1.getStartAbs() >= gffGeneIsoInfo2.getEndAbs()) {
			return new int[]{0,gffGeneIsoInfo1.size() * 2, gffGeneIsoInfo1.size()*2, gffGeneIsoInfo2.size()*2};
		}
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfo1); lsGffGeneIsoInfos.add(gffGeneIsoInfo2);
		ArrayList<ExonCluster> lsExonClusters = getExonCluster(gffGeneIsoInfo1.isCis5to3(), lsGffGeneIsoInfos);
		//��ͬ�ı߽�������һ����������������ͬ�߽�
		int sameBounds = 0;
		
		for (ExonCluster exonCluster : lsExonClusters) {
			sameBounds = sameBounds + getSameBoundsNum(exonCluster);
		}
		return new int[]{sameBounds, lsExonClusters.size()*2, gffGeneIsoInfo1.size()*2, gffGeneIsoInfo2.size()*2};
	}
	/**
	 * ��exoncluster�е�exon��һ��ʱ���鿴�����м���������ͬ�ġ�
	 * ��Ϊһ�µ�exonҲ����2����ͬ�ߣ����Է��ص�ֵΪ0��1��2
	 * @param exonCluster
	 * @return
	 */
	private static int getSameBoundsNum(ExonCluster exonCluster) {
		if (exonCluster.isSameExon()) {
			return 2;
		}

		ArrayList<ArrayList<ExonInfo>> lsExon = exonCluster.lsIsoExon;

		ArrayList<ExonInfo> lsExon1 = lsExon.get(0);
		ArrayList<ExonInfo> lsExon2 = lsExon.get(1);
		if (lsExon1.size() == 0 || lsExon2.size() == 0) {
			return 0;
		}
		if (lsExon1.get(0).getStartAbs() == lsExon2.get(0).getStartAbs()
			|| lsExon1.get(0).getEndAbs() == lsExon2.get(0).getEndAbs() ) {
			return 1;
		}
		
		if (lsExon1.get(lsExon1.size() - 1).getStartAbs() == lsExon2.get(lsExon2.size() - 1).getStartAbs()
				|| lsExon1.get(lsExon1.size() - 1).getEndAbs() == lsExon2.get(lsExon2.size() - 1).getEndAbs()) {
			return 1;
		}
		return 0;
	}
	/** ���շ���õı߽�exon����ÿ��ת¼�����л��֣����ֺõ�ExonCluster����ÿ���lsExon���ǿ����˷���Ȼ���շ���˳��װ��ȥ�� */
	public static ArrayList<ExonCluster> getExonCluster(Boolean cis5To3,  ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos) {
		String chrID = lsGffGeneIsoInfos.get(0).getChrID();
		ArrayList<ExonCluster> lsResult = new ArrayList<ExonCluster>();
		ArrayList<int[]> lsExonBound = ListAbs.getCombSep(cis5To3, lsGffGeneIsoInfos, false);
		ExonCluster exonClusterBefore = null;
		for (int[] exonBound : lsExonBound) {
			ExonCluster exonCluster = new ExonCluster(chrID, exonBound[0], exonBound[1]);
			
			exonCluster.setExonClusterBefore(exonClusterBefore);
			if (exonClusterBefore != null) {
				exonClusterBefore.setExonClusterAfter(exonCluster);
			}
			
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
				if (gffGeneIsoInfo.isCis5to3() != cis5To3) {
					continue;
				}
				
				ArrayList<ExonInfo> lsExonClusterTmp = new ArrayList<ExonInfo>();
				int beforeExonNum = 0;//�����isoform����û������bounder���е�exon����ô��Ҫ��¼��isoform��ǰ������exon��λ�ã����ڲ��ҿ����û�п����exon
				boolean junc = false;//�����isoform����û������bounder���е�exon����ô����Ҫ��¼������exon��λ�ã��ͽ����flag����Ϊtrue
				for (int i = 0; i < gffGeneIsoInfo.size(); i++) {
					ExonInfo exon = gffGeneIsoInfo.get(i);
					if (cis5To3) {
						if (exon.getEndAbs() < exonBound[0]) {
							junc = true;
							beforeExonNum = i;
							continue;
						}
						else if (exon.getStartAbs() >= exonBound[0] && exon.getEndAbs() <= exonBound[1]) {
							lsExonClusterTmp.add(exon);
							junc = false;
						}
						else if (exon.getStartAbs() > exonBound[1]) {
							break;
						}
					}
					else {
						if (exon.getStartAbs() > exonBound[1]) {
							junc = true;
							beforeExonNum = i;
							continue;
						}
						else if (exon.getEndAbs() <= exonBound[1] && exon.getStartAbs() >= exonBound[0]) {
							lsExonClusterTmp.add(exon);
							junc = false;
						}
						else if (exon.getEndAbs() < exonBound[0]) {
							break;
						}
					}
				}

				exonCluster.addExonCluster(gffGeneIsoInfo, lsExonClusterTmp);
				if (junc && beforeExonNum < gffGeneIsoInfo.size()-1) {
					exonCluster.setIso2ExonNumSkipTheCluster(gffGeneIsoInfo, beforeExonNum);
				}
			}
			lsResult.add(exonCluster);
			exonClusterBefore = exonCluster;
		}
		return lsResult;
	}
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
}
