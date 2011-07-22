package com.novelbio.analysis.seq.genomeNew.gffOperate;

/**
 * ������д��equal���룬���ڱȽ�����loc�Ƿ�һ��
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
public abstract class GffDetailAbs {

	/**
	 * ����Ŀ��List-GffDetail�еľ���λ��
	 */
	protected int itemNum = -10;
	/**
	 * ����Ŀ��List-GffDetail�еľ���λ��
	 */
	public int getItemNum() {
		return this.itemNum;
	}
	
	/**
	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
	 */
	protected String locString = ""; //loc name
	
	
	/**
	 * @GffHashGene
	 * ���������,���λ������С���յ㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	protected int numberstart = -1000000000; // loc start number 
	
	/**
	 * @GffHashGene
	 * �������յ㣬�յ�λ�����Ǵ�����㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ�յ㣬�յ�λ�����Ǵ�����㣬������Ŀ����
	 */
	protected int numberend = -1000000000; //loc end number
	
	/**
	 * Ⱦɫ���ţ���Сд
	 */
	protected String ChrID="";
	/**
	 * ת¼���򣬼���ͬһ���򲻹ܶ���ת¼����ͬһת¼����
	 */
	protected boolean cis5to3 = true; 
	/**
	 * 
	 * û�о��趨Ϊ""��null
	 * @param chrID Ⱦɫ���ţ���Сд
	 * @param locString 	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
	 * @param cis5to3
	 */
	public GffDetailAbs(String chrID, String locString,boolean cis5to3)
	{
		this.ChrID = chrID;
		this.locString = locString;
		this.cis5to3 = cis5to3;
	}
	///////////////////////////////////////////////  �� coord �йص����Ժͷ���  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 */
	protected int coord = -100;
	/**
	 * ������������ľ��룬����������
	 */
	protected Integer cod2Start = null;
	/**
	 * ����������յ�ľ��룬����������
	 */
	protected Integer cod2End = null;

	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 */
	protected void setCoord(int coord) {
		this.coord = coord;
	}
	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 * @return
	 */
	public int getCoord() {
		return this.coord;
	}
	/**
	 * Ⱦɫ���ţ���Сд
	 */
	public String getChrID() {
		return this.ChrID;
	}
    /**
 	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
     */
	public String getLocString()
	{
		return this.locString;
	}
	/**
	 * ת¼���򣬼���ͬһ���򲻹ܶ���ת¼����ͬһת¼����
	 */
	public boolean getCis5to3() {
		return this.cis5to3;
	}
	/**
	 * @GffHashGene
	 * ���������,���λ������С���յ㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	public int getNumEnd()
	{
		return this.numberend;
	}
	/**
	 * @GffHashGene
	 * ���������,���λ������С���յ㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	public int getNumStart()
	{
		return this.numberstart;
	}

	/**
	 * ֻ�Ƚ�locString��numberstart��numberend��ChrID��cis5to3
	 * ���Ƚ�coord
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		GffDetailAbs otherObj = (GffDetailAbs)obj;
		
		return locString.equals(otherObj.locString) && 
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		ChrID.equals(otherObj.ChrID) &&
		cis5to3 == otherObj.cis5to3;
	}
	/**
	 * ������굽��ItemEnd�ľ���,���coordС��0˵�������⣬�򷵻�null
	 * ��֮ǰ���趨coord
	 * ����item������
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * ���û����� >--------5start>--------->3end------->������
	 * ���������end��5������Ϊ����
	 * ���������end��3������Ϊ����
	 * @return
	 */
	public Integer getCod2End() {
		if (cod2End != null) {
			return cod2End;
		}
		if (coord < 0) {
			return null;
		}
		if (cis5to3) {
			cod2End =  coord -numberend;
		}
		else {
			cod2End = numberstart- coord;
		}
		return cod2End;
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
	public Integer getCod2Start() {
		if (cod2Start != null) {
			return cod2Start;
		}
		if (coord < 0) {
			return null;
		}
		if (cis5to3) {
			cod2Start =  coord -numberstart;
		}
		else {
			cod2Start = numberend - coord;
		}
		return cod2Start;
	}

	/**
	 * �����Ƿ��ڻ�����
	 * @return
	 */
	public boolean getCodInSide() {
		if (coord < 0) {
			return false;
		}
		if (coord >= numberstart && coord <= numberend) {
			return true;
		}
		else {
			return false;
		}
	}
		
}
