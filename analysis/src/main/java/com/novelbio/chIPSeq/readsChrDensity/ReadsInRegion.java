package com.novelbio.chIPSeq.readsChrDensity;

import java.util.ArrayList;

import com.novelBio.base.dataStructure.MathComput;
import com.novelBio.base.genome.GffChrUnion;
import com.novelBio.base.genome.GffPeakOverlap;
import com.novelBio.base.genome.gffOperate.GffDetail;
import com.novelBio.base.genome.gffOperate.GffHashPeak;
import com.novelBio.base.genome.mappingOperate.MapReads;




public class ReadsInRegion  
{
	/**
	 * 给定一组坐标的bed数组信息，譬如bivalent区域等，计算所有这些区域内累计reads的密度情况，类似TSS图
	 *@param mapFile mapping的结果文件，一般为bed格式
     *@param   chrFilePath 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，文件夹最后无所谓加不加"/"或"\\"
     *@param sep 
     *@param colChrID ChrID在第几列，从1开始
     *@param colStartNum mapping起点在第几列，从1开始
     *@param colEndNum mapping终点在第几列，从1开始
     *@param invNum 每隔多少位计数
     *@param tagLength 设定双端readsTag拼起来后长度的估算值，大于20才会进行设置。目前solexa双端送样长度大概是200-400bp，不用太精确 ,默认是400
	* @param RegInfo 一组坐标的bed数组信息，譬如bivalent区域等 包含有Region的数组信息，string[3]：0： chrID，1：startNum  2:endNum 
     * @param binNum 最后返回的区域被分为多少块
	 * @param RegInfo
	 * @throws Exception 
	 */
	public double[] getRegionReads(String mapFile,String chrFilePath,String sep,int colChrID,int colStartNum,int colEndNum,int invNum,int tagLength,String[][] RegInfo,int binNum) throws Exception 
	{
		GffChrUnion gffChrUnion=new GffChrUnion();
		gffChrUnion.loadMap(mapFile, chrFilePath, sep, colChrID, colStartNum, colEndNum, invNum, tagLength);
		
		
		GffHashPeak gffHashRegion=new GffHashPeak();
		gffHashRegion.ReadGffarray(RegInfo);
		ArrayList<String> chrIDlist=gffHashRegion.getLOCChrHashIDList();
		double[] regionReads=new double[binNum];
		for (int i = 0; i < chrIDlist.size(); i++) {
			String tmpRegion=chrIDlist.get(i).split("/")[0];
			GffDetail gffRegionDetail=gffHashRegion.LOCsearch(tmpRegion);
			double[] tmpReads=gffChrUnion.getRangReadsDist(binNum,gffRegionDetail.ChrID, gffRegionDetail.numberstart, gffRegionDetail.numberend);
			MathComput.addArray(regionReads, tmpReads);
		}
		return regionReads;
	}
	

	
}
