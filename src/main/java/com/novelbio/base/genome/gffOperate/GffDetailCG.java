package com.novelbio.base.genome.gffOperate;

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
public class GffDetailCG extends GffDetail
{
	/**
	 * CpG长度
	 */
	public int lengthCpG=0;
	
	/**
	 * CpG数量
	 */
	public int numCpG=0;
	
	/**
	 * GC数量
	 */
	public int numGC=0;
	
	/**
	 * CpG百分比
	 */
	public double perCpG=0;
	
	/**
	 * GC百分比
	 */
	public double perGC=0;
	
	/**
	 * 显著性
	 */
	public double obsExp=0;
	
	
	
	
	
	
	
	
}
