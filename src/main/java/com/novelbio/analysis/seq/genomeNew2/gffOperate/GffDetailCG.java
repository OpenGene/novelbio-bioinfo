package com.novelbio.analysis.seq.genomeNew2.gffOperate;

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
public class GffDetailCG extends GffDetailAbs
{
	public GffDetailCG(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}

	/**
	 * CpG����
	 */
	protected int lengthCpG=0;
	/**
	 * CpG����
	 */
	protected int numCpG=0;
	/**
	 * GC����
	 */
	protected int numGC=0;
	/**
	 * CpG�ٷֱ�
	 */
	protected double perCpG=0;
	/**
	 * GC�ٷֱ�
	 */
	protected double perGC=0;
	/**
	 * ������
	 */
	protected double obsExp=0;
	/**
	 * CpG����
	 * @return
	 */
	public int getLength() {
		return lengthCpG;
	}
	/**
	 * GC����
	 * @return
	 */
	public int getNumCG() {
		return numGC;
	}
	/**
	 * GC����
	 * @return
	 */
	public double getPerCpG() {
		return perCpG;
	}
	@Override
	public GffDetailCGCod setCood(int coord) {
		GffDetailCGCod gffDetailCGCod = new GffDetailCGCod(this, coord);
		return gffDetailCGCod;
	}
	
}