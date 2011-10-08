package com.novelbio.analysis.seq.genomeNew2.gffOperate;

/**
 * 针对UCSC的CpGall文件设计的Detai信息
 * @包含超类信息
 * CpG名 locString<br>
 * CpG起点 numberstart<br>
 * CpG终点 numberend<br>
 * CpG所在染色体编号 ChrID<br>
 * CpG方向 cis5to3，文件内没有CpG方向，所以方向通通为true
 * 
 * @包含CpG特有信息
 * CpG长度
 * CpG数量
 * GC数量
 * CpG百分比
 * GC百分比
 * CpG Island显著性
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
	 * CpG长度
	 */
	protected int lengthCpG=0;
	/**
	 * CpG数量
	 */
	protected int numCpG=0;
	/**
	 * GC数量
	 */
	protected int numGC=0;
	/**
	 * CpG百分比
	 */
	protected double perCpG=0;
	/**
	 * GC百分比
	 */
	protected double perGC=0;
	/**
	 * 显著性
	 */
	protected double obsExp=0;
	/**
	 * CpG长度
	 * @return
	 */
	public int getLength() {
		return lengthCpG;
	}
	/**
	 * GC数量
	 * @return
	 */
	public int getNumCG() {
		return numGC;
	}
	/**
	 * GC数量
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
