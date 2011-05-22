package com.novelbio.base.genome.gffOperate;

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
public class GffDetail {
	/**
	 * LOCID��<br>
	 * ˮ����LOC_Os01g01110<br>
	 * ���Ͻ棺AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG��107_chr1_CpG_36568608: 27 ����107��CpG gff�ļ��е�����,36568608�Ǹ�CpG��Ⱦɫ���ϵ����
	 * peak: peak���_peak�յ�
	 */
	public String locString=""; //loc name
	
	/**
	 * @GffHashGene
	 * ���������,���λ������С���յ㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ���,���λ������С���յ㣬������Ŀ����
	 */
	public int numberstart=-1000000000; // loc start number 
	
	/**
	 * @GffHashGene
	 * �������յ㣬�յ�λ�����Ǵ�����㣬���ӻ�����
	 * @GffHashItem
	 * ��Ŀ�յ㣬�յ�λ�����Ǵ�����㣬������Ŀ����
	 */
	public int numberend=-1000000000; //loc end number
	
	/**
	 * Ⱦɫ���ţ���Сд
	 */
	public String ChrID="";
	
	/**
	 * ת¼���򣬼���ͬһ���򲻹ܶ���ת¼����ͬһת¼����
	 */
	public boolean cis5to3=true; 
	
	
	/**
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		GffDetail otherObj = (GffDetail)obj;
		
		return locString.equals(otherObj.locString) && 
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		ChrID.equals(otherObj.ChrID) &&
		cis5to3 == otherObj.cis5to3;
	}
	
	
	
	
	
	
	
}
