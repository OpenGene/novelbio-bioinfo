package com.novelBio.chIPSeq.prepare;

import java.util.ArrayList;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.genome.GffChrUnion;
import com.novelBio.base.genome.GffLocatCod;
import com.novelBio.chIPSeq.cGIsland.CpG;


public class GenomeBasePrepare {
	protected static GffLocatCod gffLocatCod=new GffLocatCod();
	static int invNum=10;
	public static String sep="\t";
	/**
	 * 设定读取的最小片段间隔，默认为10，也就是10bp精度
	 * @param invNum
	 */
	public void setInvNum(int invNum) {
		this.invNum = invNum;
	}
	
	/**
	 * 如果某个值设置为""，则该值不设置
	 * @param chrFilePath
	 * @param colMap mapping 文件中 chr 起点 终点的位置,如果 mapFilePath没有，那么就设为null
	 * 常规bed文件 1，2，3
	 * 王从茂的文件，0，1，2
	 * @param gffClass 待实例化的Gffhash子类，只能有 "TIGR","CG","UCSC","Peak","Repeat"这几种
	 * @param gffFilePath
	 * @param mapFilePath 默认10bp的间隔计数
	 */
	public static void  prepare(String chrFilePath,int[] colMap,String gffClass,String gffFilePath,String mapFilePath) {
		if (chrFilePath != null && !chrFilePath.trim().equals("")) {
			gffLocatCod.loadChr(chrFilePath);
		}
		if (gffFilePath != null && !gffFilePath.trim().equals("")) {
			gffLocatCod.loadGff(gffClass,gffFilePath);
		}
		if (mapFilePath != null && !mapFilePath.trim().equals("")) {
			gffLocatCod.loadMap(mapFilePath, chrFilePath, sep, colMap[0], colMap[1], colMap[2], invNum, 0);
		}
	}
	
	/**
	 * 计算染色体统计结果
	 * @throws Exception 
	 */
	public static void getChrStatistic(String txtchrInfo) throws Exception
	{
		//ArrayList<Long> lsGeneStructure = gffLocatCod.getGeneStructureLength();
		ArrayList<String[]> lsChrLength =gffLocatCod.getChrLengthInfo();
		TxtReadandWrite txtChrInfo = new TxtReadandWrite();
		txtChrInfo.setParameter(txtchrInfo, true, false);
		for (String[] strings : lsChrLength) {
			txtChrInfo.writefile(strings[0]+"\t"+strings[1]+"\n");
		}
	}
	
}
