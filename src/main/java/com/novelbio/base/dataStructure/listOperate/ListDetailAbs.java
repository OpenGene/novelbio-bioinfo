package com.novelbio.base.dataStructure.listOperate;

import org.apache.log4j.Logger;

/**
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
public class ListDetailAbs{
	/**
	 * ����cis���������ζ���bp���ڴ˷�Χ������Ϊ��tss����
	 */
	int upTss = 0;
	/**
	 * ����cis���������ζ���bp���ڴ˷�Χ������Ϊ��tss����
	 */
	int downTss = 0;
	/**
	 * ����cis���յ�����ζ���bp���ڴ˷�Χ������Ϊ��tes����
	 */
	int upGeneEnd3UTR = 0;
	/**
	 * ����cis���յ�����ζ���bp���ڴ˷�Χ������Ϊ��tes����
	 */
	int downGeneEnd3UTR = 0;
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
	 * ����Tes��Χ����Ϊ����������Ϊ����
	 * @param upTes
	 * @param downTes
	 */
	public void setTesRegion(int upTes, int downTes) {
		this.upGeneEnd3UTR = upTes;
		this.downGeneEnd3UTR = downTes;
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
	/**
	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
	 */
	private String ItemName = ""; //loc name
	/**
	 * Ⱦɫ���ţ���Сд
	 */
	protected String parentName="";
	/**
	 * ת¼���򣬼���ͬһ���򲻹ܶ���ת¼����ͬһת¼����
	 */
	protected Boolean cis5to3 = null; 
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
	public ListDetailAbs(String chrID, String ItemName,Boolean cis5to3)
	{
		this.parentName = chrID.toLowerCase();
		this.ItemName = ItemName;
		this.cis5to3 = cis5to3;
	}
	
	private static Logger logger = Logger.getLogger(ListDetailAbs.class);
	
	int number = 0;
	/**
	 * ������һ
	 */
	public void addNumber() {
		number++;
	}
	/**
	 * �������ڳ��ֶ��ٵ�Ԫ�أ�����ǰ�����addNumber���
	 * @return
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * ����Ŀ��List-GffDetail�еľ���λ��
	 */
	protected int itemNum = ListCodAbs.LOC_ORIGINAL;
	/**
	 * ����Ŀ��List-GffDetail�еľ���λ��
	 */
	public int getItemNum() {
		return this.itemNum;
	}
    /**
     * Item������
 	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
     */
	public String getName() {
		return this.ItemName;
	}
    /**
 	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
     */
	public void setName(String locString) {
		this.ItemName = locString;
	}
	/**
	 * Ⱦɫ���ŵ���Ϣ����ID
	 * @param parentName
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	/**
	 * @GffHashGene
	 * ����Ŀ���,���λ������С���յ㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	protected int numberstart = ListCodAbs.LOC_ORIGINAL; // loc start number 
	
	/**
	 * @GffHashGene
	 * ����Ŀ�յ㣬�յ�λ�����Ǵ�����㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ�յ㣬�յ�λ�����Ǵ�����㣬������Ŀ����
	 */
	protected int numberend = ListCodAbs.LOC_ORIGINAL; //loc end number
	/**
	 * ��������㵽��һ������߽�ľ���
	 */
	protected int tss2UpGene = ListCodAbs.LOC_ORIGINAL;
	/**
	 * ��������㵽��һ������߽�ľ���
	 */
	public void setTss2UpGene(int tss2UpGene) {
		this.tss2UpGene = tss2UpGene;
	}
	/**
	 * �������յ㵽��һ������߽�ľ���
	 */
	protected int tes2DownGene = ListCodAbs.LOC_ORIGINAL;
	/**
	 * �������յ㵽��һ������߽�ľ���
	 */
	public void setTes2DownGene(int tes2DownGene) {
		this.tes2DownGene = tes2DownGene;
	}

	/**
	 * �������յ㵽��һ������߽�ľ���
	 */
	public int getTes2DownGene() {
		return tes2DownGene;
	}
	/**
	 * ��������㵽��һ������߽�ľ���
	 */
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

	/**
	 * �����Ƿ��ڻ�����
	 * @return
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
		
		return ItemName.equals(otherObj.ItemName) && 
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		parentName.equals(otherObj.parentName) &&
		getItemNum() == otherObj.getItemNum() &&
		cis5to3 == otherObj.cis5to3;
	} 

	
	/**
	 * ��дhashcode
	 */
	public int hashCode(){
		String hash = "";
		hash = parentName + "//" + ItemName + "//" + numberstart + "//" + numberstart;
		return hash.hashCode();
	}
	
	/**
	 * ���������Ϣȫ�����Ƶ�gffDetailAbs��ȥ
	 * locString��ChrID��cis5to3������
	 * cod2Start,cod2End������
	 * @param gffDetailAbs1
	 * @param gffDetailAbs2
	 */
	protected void clone(ListDetailAbs gffDetailAbs)
	{
		gffDetailAbs.parentName = parentName;
		gffDetailAbs.cis5to3 = cis5to3;
		gffDetailAbs.ItemName = ItemName;
		gffDetailAbs.itemNum = itemNum;
		gffDetailAbs.numberstart = numberstart;
		gffDetailAbs.numberend = numberend;
		gffDetailAbs.tes2DownGene = tes2DownGene;
		gffDetailAbs.tss2UpGene = tss2UpGene;
		gffDetailAbs.number = number;
	}
	public int getStartCis() {
		if (isCis5to3()) {
			return numberstart;
		}
		return numberend;
	}
	
	public int getEndCis() {
		if (isCis5to3()) {
			return numberend;
		}
		return numberstart;
	}
	
	public int getLen() {
		return Math.abs(numberend-numberstart) + 1;
	}
}
