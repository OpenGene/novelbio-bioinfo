package com.novelbio.analysis.seq.genomeNew2.gffOperate;

import javax.servlet.jsp.tagext.TryCatchFinally;

import com.novelbio.analysis.annotation.copeID.CopedID;

/**
 * ������д��equal���룬���ڱȽ�����loc�Ƿ�һ��
 * ��д��hashcode
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
	protected String locString = ""; //loc name
	
	
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
		this.ChrID = chrID;
		this.locString = locString;
		this.cis5to3 = cis5to3;
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
	
	public abstract GffDetailAbsCod setCood(int coord);
	///////////////////////////////////////////////  �� coord �йص����Ժͷ���  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
/////////////////////////////  ��дequals��  ////////////////////////////////////

	

	/**
	 * ֻ�Ƚ�locString��numberstart��numberend��ChrID��cis5to3
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
	
	
}
