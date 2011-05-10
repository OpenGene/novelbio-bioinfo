package com.novelbio.base.genome.gffOperate;

/**
 * ���UCSC��CpGall�ļ���Ƶ�Detai��Ϣ
 * @����������Ϣ
 * CpG�� locString<br>
 * CpG��� numberstart<br>
 * CpG�յ� numberend<br>
 * CpG����Ⱦɫ���� ChrID<br>
 * CpG���� cis5to3���ļ���û��CpG�������Է���ͨͨΪtrue
 * 
 * @����CpG������Ϣ
 * CpG����
 * CpG����
 * GC����
 * CpG�ٷֱ�
 * GC�ٷֱ�
 * CpG Island������
 * 
 * 
 * @author zong0jie
 *
 */
public class GffDetailCG extends GffDetail
{
	/**
	 * CpG����
	 */
	public int lengthCpG=0;
	
	/**
	 * CpG����
	 */
	public int numCpG=0;
	
	/**
	 * GC����
	 */
	public int numGC=0;
	
	/**
	 * CpG�ٷֱ�
	 */
	public double perCpG=0;
	
	/**
	 * GC�ٷֱ�
	 */
	public double perGC=0;
	
	/**
	 * ������
	 */
	public double obsExp=0;
	
	
	
	
	
	
	
	
}
