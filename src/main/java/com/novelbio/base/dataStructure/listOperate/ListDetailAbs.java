package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.mappingOperate.Alignment;

/**
 * compare�ıȽ�ȡ���ڸ��ڵ�ķ���������ڵ�ķ���Ϊnull�����վ���ֵ���������cis����ô�Ͱ�cis���������Ϊtrans�Ͱ���trans�ķ�ʽ����
 * ������д��equal���룬���ڱȽ�����loc�Ƿ�һ��
 * ��д��hashcode ���Ƚ�ChrID + "//" + locString + "//" + numberstart + "//" + numberstart;
 * �洢Gff�ļ���ÿ����Ŀ�ľ�����Ϣ��ֱ������GffPeak�ļ�
 * ����<br>
 * ��Ŀ�� locString<br>
 * ��Ŀ��� numberstart<br>
 * ��Ŀ�յ� numberend<br>
 * ��Ŀ����Ⱦɫ���� ChrID<br>
 * ��Ŀ���� cis5to3
 * @author zong0jie
 *
 */
public class ListDetailAbs implements Cloneable, Comparable<ListDetailAbs>, Alignment {
	/** ���� */
	ListAbs<? extends ListDetailAbs> listAbs;
	
	/** ����cis���������ζ���bp���ڴ˷�Χ������Ϊ��tss����  */
	protected int upTss = 0;
	/** ����cis���������ζ���bp���ڴ˷�Χ������Ϊ��tss���� */
	protected int downTss = 0;
	/** ����cis���յ�����ζ���bp���ڴ˷�Χ������Ϊ��tes���� */
	protected int upGeneEnd3UTR = 0;
	/** ����cis���յ�����ζ���bp���ڴ˷�Χ������Ϊ��tes���� */
	protected int downGeneEnd3UTR = 0;
	/**
	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
	 */
	private ArrayList<String> lsItemName = new ArrayList<String>(); //loc name
	/**  Ⱦɫ���ţ���Сд */
	protected String parentName="";
	/** ת¼���򣬼���ͬһ���򲻹ܶ���ת¼����ͬһת¼���� */
	protected Boolean cis5to3 = null;
	/** ���������ж�����reads */
	int readsInElementNumber = 0;
	
	/** ����Ŀ���,���λ������С���յ㣬���ӻ����� */
	protected int numberstart = ListCodAbs.LOC_ORIGINAL; // loc start number 
	/** ����Ŀ�յ㣬�յ�λ�����Ǵ�����㣬���ӻ����� */
	protected int numberend = ListCodAbs.LOC_ORIGINAL; //loc end number
	/** ��������㵽��һ������߽�ľ��� */
	protected int tss2UpGene = ListCodAbs.LOC_ORIGINAL;
	/** �������յ㵽��һ������߽�ľ��� */
	protected int tes2DownGene = ListCodAbs.LOC_ORIGINAL;
	/** ����Ŀ��List-GffDetail�еľ���λ�� */
	protected int itemNum = ListCodAbs.LOC_ORIGINAL;
	
	public ListDetailAbs() {}
	/**
	 * û�о��趨Ϊ""��null
	 * @param chrID Ⱦɫ���ţ��Զ����Сд
	 * @param locString 	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
	 * @param cis5to3 ��ȷ��������null
	 */
	public ListDetailAbs(String chrID, String ItemName, Boolean cis5to3) {
		if (chrID != null) {
			this.parentName = chrID.toLowerCase();
		}
		this.lsItemName.add(ItemName);
		this.cis5to3 = cis5to3;
	}
	/**
	 * û�о��趨Ϊ""��null
	 * @param chrID Ⱦɫ���ţ��Զ����Сд
	 * @param locString 	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
	 * @param cis5to3 ��ȷ��������null
	 */
	public ListDetailAbs(ListAbs<? extends ListDetailAbs> listAbs, String ItemName, Boolean cis5to3) {
		this.listAbs = listAbs;
		this.parentName = listAbs.getName().toLowerCase();
		this.lsItemName.add(ItemName);
		this.cis5to3 = cis5to3;
	}
	public void setParentListAbs(ListAbs<? extends ListDetailAbs> listAbs) {
		this.listAbs = listAbs;
	}
	/**
	 * ����Tss��Χ����Ϊ����������Ϊ����
	 * @param upTss
	 * @param downTss
	 */
	public void setTssRegion(int upTss, int downTss) {
		this.upTss = upTss;
		this.downTss = downTss;
	}
	/**
	 * ����Tss��Χ����Ϊ����������Ϊ����
	 * @param upTss
	 * @param downTss
	 */
	public void setTssRegion(int[] Tss) {
		if (Tss != null)
			setTssRegion(Tss[0], Tss[1]);
	}
	
	/**
	 * ����Tes��Χ����Ϊ����������Ϊ����
	 * @param upTes
	 * @param downTes
	 */
	public void setTesRegion(int upTes, int downTes) {
		this.upGeneEnd3UTR = upTes;
		this.downGeneEnd3UTR = downTes;
	}
	/**
	 * ����Tss��Χ����Ϊ����������Ϊ����
	 * @param upTss
	 * @param downTss
	 */
	public void setTesRegion(int[] Tes) {
		if (Tes != null)
			setTesRegion(Tes[0], Tes[1]);
	}
	/**
	 * 0��uptss
	 * 1��downtss
	 * @return
	 */
	public int[] getTssRegion() {
		return new int[]{upTss, downTss};
	}
	/**
	 * 0��uptes
	 * 1��downtes
	 * @return
	 */
	public int[] getTesRegion() {
		return new int[]{upGeneEnd3UTR, downGeneEnd3UTR};
	}
	private static Logger logger = Logger.getLogger(ListDetailAbs.class);
	
	/** ������һ */
	public void addReadsInElementNum() {
		readsInElementNumber++;
	}
	/**
	 * �������ڳ��ֶ��ٵ�Ԫ�أ�����ǰ�����addNumber���
	 * @return
	 */
	public int getReadsInElementNum() {
		return readsInElementNumber;
	}
	/**
	 * ��0��ʼ��λ��list�ĵڼ���λ��
	 * @param itemNum
	 */
	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}
	/** 
	 * <b>����û��</b>
	 * ����Ŀ��List-GffDetail�еľ���λ�� */
	public int getItemNum() {
		return this.itemNum;
	}
    /**
     * Item�����֣����ص�һ��
 	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
     */
	public String getNameSingle() {
		return this.lsItemName.get(0);
	}
	/** ȫ��item������ */
	public ArrayList<String > getName() {
		return this.lsItemName;
	}
    /**
 	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
     */
	public void addItemName(String itemName) {
		this.lsItemName.add(itemName);
	}
	/**
	 * Ⱦɫ���ŵ���Ϣ����ID
	 * @param parentName
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	/** ��������㵽��һ������߽�ľ���  */
	public void setTss2UpGene(int tss2UpGene) {
		this.tss2UpGene = tss2UpGene;
	}
	/** �������յ㵽��һ������߽�ľ��� */
	public void setTes2DownGene(int tes2DownGene) {
		this.tes2DownGene = tes2DownGene;
	}
	/** �������յ㵽��һ������߽�ľ��� */
	public int getTes2DownGene() {
		return tes2DownGene;
	}
	/** ��������㵽��һ������߽�ľ��� */
	public int getTss2UpGene() {
		return tss2UpGene;
	}
	/**
	 * @GffHashGene
	 * �������յ㣬�յ�λ�����Ǵ�����㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ�յ㣬�յ�λ�����Ǵ�����㣬������Ŀ����
	 */
	public int getEndAbs() {
		return numberend;
	}
	/**
	 * @GffHashGene
	 * ���������,���λ������С���յ㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	public int getStartAbs() {
		return numberstart;
	}
	/**
	 * @param numberend ��Ŀ�յ�,�յ�λ�����Ǵ�����㣬���ӻ�����
	 */
	public void setEndAbs(int numberend) {
		this.numberend = numberend;
	}
	/**
	 * @param numberstart ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	public void setStartAbs(int numberstart) {
		this.numberstart = numberstart;
	}
	/**
	 * @param numberend ��Ŀ�յ�,���ݻ�����ȷ��
	 */
	public void setEndCis(int numberend) {
		if (isCis5to3() == null || isCis5to3()) {
			this.numberend = numberend;
		}
		else {
			this.numberstart = numberend;
		}
	}
	/**
	 * @param numberstart ��Ŀ���,���ݻ�����ȷ��
	 */
	public void setStartCis(int numberstart) {
		if (isCis5to3() == null || isCis5to3()) {
			this.numberstart = numberstart;
		}
		else {
			this.numberend = numberstart;
		}
	}
	/**
	 * �����Ƿ��ڻ�����ڲ�������Tss��GeneEnd����չ����
	 */
	public boolean isCodInGeneExtend(int coord) {
		return isCodInGene(coord) || isCodInPromoter(coord) || isCodInGenEnd(coord);
	}
	
	/**
	 * �Ƿ�����ν��Tss��,�ȿ�������Ҳ������
	 * ���������Ҫֻ�ڻ������tss����Ҫͬʱ����isCodInside==false�ж�
	 * @return
	 */
	public boolean isCodInPromoter(int coord) {
		if (getCod2Start(coord) == null) {
			return false;
		}
		int cod2start = getCod2Start(coord);
		if (cod2start >= upTss && cod2start <= downTss) {
			return true;
		}
		return false;
	}
	
	/**
	 * �Ƿ�����ν��GeneEnd��,�ȿ�������Ҳ��������
	 * ���������Ҫֻ�ڻ������geneEnd����Ҫͬʱ����isCodInside==false�ж�
	 * Ҳ����β���㣬������չgeneEnd3UTR���ȵ�bp
	 * @return
	 */
	public boolean isCodInGenEnd(int coord) {
		if (getCod2End(coord) == null) {
			return false;
		}
		int cod2end = getCod2End(coord);
		if (cod2end >= upGeneEnd3UTR && cod2end <= downGeneEnd3UTR ) {
			return true;
		}
		return false;
	}
	/**
	 * �Ƿ��ڻ����ڣ�����չ
	 * @return
	 */
	public boolean isCodInGene(int coord) {
		if (coord >= numberstart && coord <= numberend) {
			return true;
		}
		return false;
	}
	/**
	 * ����listAbs��ţ���Сд
	 */
	public String getParentName() {
		return this.parentName;
	}

	/**
	 * ת¼���򣬼���ͬһ���򲻹ܶ���ת¼����ͬһת¼����
	 * һ��ת¼�������������Ҳ�з���ѡ���������Ǹ�
	 */
	public Boolean isCis5to3() {
		return this.cis5to3;
	}
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	/**
	 * ������굽��ItemEnd�ľ���
	 * ��֮ǰ���趨coord
	 * ����item������
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * ���û����� >--------5start>--------->3end------->������
	 * ���������end��5������Ϊ����
	 * ���������end��3������Ϊ����
	 * @return
	 */
	public Integer getCod2End(int coord) {
		if (cis5to3 == null) {
			logger.error("����ȷ����Item�ķ���");
			return null;
		}
		if (cis5to3) {
			return coord -numberend;
		}
		else {
			return numberstart- coord;
		}
	}
	/**
	 * ������굽��ItemStart�ľ���,���coordС��0˵�������⣬�򷵻�null
	 * ��֮ǰ���趨coord
	 * ����item������
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * ���û����� >--------5start>--------->3end------->������
	 * ���������start��5������Ϊ����
	 * ���������start��3������Ϊ����
	 * @return
	 */
	public Integer getCod2Start(int coord) {
		if (cis5to3 == null) {
			logger.error("����ȷ����Item�ķ���");
			return null;
		}
		if (cis5to3) {
			return coord -numberstart;
		}
		else {
			return numberend - coord;
		}
	}
	/** �����Ƿ��ڻ�����
	 */
	public boolean isCodInSide(int coord) {
		if (coord >= numberstart && coord <=  numberend) {
			return true;
		}
		return false;
	}
	
/////////////////////////////  ��дequals��  ////////////////////////////////////
	/**
	 * ֻ�Ƚ�locString��numberstart��numberend��ChrID��cis5to3
	 * ���Ƚ�coord
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		ListDetailAbs otherObj = (ListDetailAbs)obj;
		
		return lsItemName.equals(otherObj.lsItemName) && 
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		parentName.equals(otherObj.parentName) &&
		getItemNum() == otherObj.getItemNum() &&
		cis5to3 == otherObj.cis5to3;
	}
	/** ��дhashcode */
	public int hashCode(){
		String hash = "";
		hash = parentName + "//" + getName().hashCode() + "//" + numberstart + "//" + numberstart;
		return hash.hashCode();
	}
	/** û�з����򷵻�startAbs */
	public int getStartCis() {
		if (isCis5to3() == null || isCis5to3()) {
			return numberstart;
		}
		return numberend;
	}
	/** û�з����򷵻�endAbs */
	public int getEndCis() {
		if (isCis5to3() == null || isCis5to3()) {
			return numberend;
		}
		return numberstart;
	}
	
	public int Length() {
		return Math.abs(numberend-numberstart) + 1;
	}
	
	public ListDetailAbs clone() {
		ListDetailAbs result = null;
		try {
			result = (ListDetailAbs) super.clone();
			result.cis5to3 = cis5to3;
			result.downGeneEnd3UTR = downGeneEnd3UTR;
			result.downTss = downTss;
			result.lsItemName = (ArrayList<String>) lsItemName.clone();
			result.itemNum = itemNum;
			result.readsInElementNumber = readsInElementNumber;
			result.numberend = numberend;
			result.numberstart = numberstart;
			result.parentName = parentName;
			result.tes2DownGene = tes2DownGene;
			result.tss2UpGene = tss2UpGene;
			result.upGeneEnd3UTR = upGeneEnd3UTR;
			result.upTss = upTss;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}
	@Override
	public int compareTo(ListDetailAbs o) {
		Integer o1startCis = getStartCis(); Integer o1endCis = getEndCis();
		Integer o2startCis = o.getStartCis(); Integer o2endCis = o.getEndCis();
		
		Integer o1startAbs = getStartAbs(); Integer o1endAbs = getEndAbs();
		Integer o2startAbs = o.getStartAbs(); Integer o2endAbs = o.getEndAbs();
		
		if (listAbs.isCis5to3() == null) {
			int result = o1startAbs.compareTo(o2startAbs);
			if (result == 0) {
				return o1endAbs.compareTo(o2endAbs);
			}
			return result;
		}
		
		else if (listAbs.isCis5to3()) {
			int result = o1startCis.compareTo(o2startCis);
			if (result == 0) {
				return o1endCis.compareTo(o2endCis);
			}
			return result;
		}
		else {
				int result = - o1startCis.compareTo(o2startCis);
				if (result == 0) {
					return - o1endCis.compareTo(o2endCis);
				}
				return result;
			}
	}
}
