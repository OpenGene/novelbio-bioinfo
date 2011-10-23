package com.novelbio.analysis.seq.genomeNew.gffOperate;

import javax.servlet.jsp.tagext.TryCatchFinally;

import com.novelbio.analysis.annotation.copeID.CopedID;

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
public abstract class GffDetailAbs {
	
	
	
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
	 * ����Ŀ��List-GffDetail�еľ���λ��
	 */
	protected int itemNum = GffCodAbs.LOC_ORIGINAL;
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
	private String locString = ""; //loc name
	public void setLocString(String locString) {
		this.locString = locString;
	}
	/**
	 * @GffHashGene
	 * ���������,���λ������С���յ㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	protected int numberstart = GffCodAbs.LOC_ORIGINAL; // loc start number 
	
	/**
	 * @GffHashGene
	 * �������յ㣬�յ�λ�����Ǵ�����㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ�յ㣬�յ�λ�����Ǵ�����㣬������Ŀ����
	 */
	protected int numberend = GffCodAbs.LOC_ORIGINAL; //loc end number
	/**
	 * ��������㵽��һ������߽�ľ���
	 */
	protected int tss2UpGene = GffCodAbs.LOC_ORIGINAL;
	/**
	 * �������յ㵽��һ������߽�ľ���
	 */
	protected int tes2DownGene = GffCodAbs.LOC_ORIGINAL;
	/**
	 * Ⱦɫ���ţ���Сд
	 */
	protected String ChrID="";
	/**
	 * ת¼���򣬼���ͬһ���򲻹ܶ���ת¼����ͬһת¼����
	 */
	protected boolean cis5to3 = true; 
	/**
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
		this.ChrID = chrID.toLowerCase();
		this.locString = locString;
		this.cis5to3 = cis5to3;
	}
	///////////////////////////////////////////////  �� coord �йص����Ժͷ���  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 */
	protected int coord = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ������������ľ��룬����������
	 */
	protected Integer cod2Start = null;
	/**
	 * ����������յ�ľ��룬����������
	 */
	protected Integer cod2End = null;
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
	public int getNumberend() {
		return numberend;
	}
	/**
	 * @GffHashGene
	 * ���������,���λ������С���յ㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	public int getNumberstart() {
		return numberstart;
	}
	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 */
	public void setCoord(int coord) {
		this.coord = coord;
		cod2End = null;
		cod2Start = null;
	}
	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 * @return
	 */
	public int getCoord() {
		return this.coord;
	}
	
	/**
	 * �����Ƿ��ڻ�����ڲ�������Tss��GeneEnd����չ����
	 */
	public boolean isCodInGenExtend() {
		return isCodInGene() || isCodInPromoter() || isCodInGenEnd();
	}
	
	/**
	 * �Ƿ�����ν��Tss��
	 * @return
	 */
	public boolean isCodInPromoter() {
		if (getCod2Start() == null) {
			return false;
		}
		if (getCod2Start() < 0 && Math.abs(getCod2Start()) <= UpStreamTSSbp) {
			return true;
		}
		else if (getCod2Start() >= 0 && Math.abs(getCod2Start()) <= DownStreamTssbp) {
			return true;
		}
		return false;
	}
	
	/**
	 * �Ƿ�����ν��GeneEnd��
	 * @return
	 */
	public boolean isCodInGenEnd() {
		if (getCod2End() == null) {
			return false;
		}
		if (getCod2End() > 0 && Math.abs(getCod2End()) <= GeneEnd3UTR) {
			return true;
		}
		return false;
	}
	
	/**
	 * �Ƿ��ڻ����ڣ�����չ
	 * @return
	 */
	public boolean isCodInGene() {
		if (coord >= numberstart && coord <= numberend) {
			return true;
		}
		return false;
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
	public boolean isCis5to3() {
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
		
		GffDetailAbs otherObj = (GffDetailAbs)obj;
		
		return locString.equals(otherObj.locString) && 
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		ChrID.equals(otherObj.ChrID) &&
		getItemNum() == otherObj.getItemNum() &&
		cis5to3 == otherObj.cis5to3;
	} 

	
	/**
	 * ��дhashcode
	 */
	public int hashCode(){
		String hash = "";
		hash = ChrID + "//" + locString + "//" + numberstart + "//" + numberstart;
		return hash.hashCode();
	}
	
	public abstract GffDetailAbs clone();
	/**
	 * ���������Ϣȫ�����Ƶ�gffDetailAbs��ȥ
	 * locString��ChrID��cis5to3������
	 * cod2Start,cod2End������
	 * @param gffDetailAbs1
	 * @param gffDetailAbs2
	 */
	protected void clone(GffDetailAbs gffDetailAbs)
	{
//		gffDetailAbs1.ChrID = gffDetailAbs2.ChrID;
//		gffDetailAbs1.cis5to3 = gffDetailAbs2.cis5to3
//		gffDetailAbs1.locString = gffDetailAbs2.locString;
		gffDetailAbs.coord = coord;
		gffDetailAbs.itemNum = itemNum;
		gffDetailAbs.numberstart = numberstart;
		gffDetailAbs.numberend = numberend;
		gffDetailAbs.tes2DownGene = tes2DownGene;
		gffDetailAbs.tss2UpGene = tss2UpGene;
	}
}
