package com.novelbio.analysis.seq.genomeNew;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 给定基因的区域，画出各种统计图
 * @author zong0jie
 *
 */
public class GffChrMap extends GffChrAbs{
	/**
	 * 
	 */
	boolean HanYanFstrand =false;
	/**
	 * 
	 * @param gffType
	 * @param gffFile
	 * @param readsBed
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 * @param HanYanFstrand 是否选择韩燕模式，根据reads是否与基因的方向相一致而进行过滤工作，这个是专门针对韩燕的项目做的分析。
	 */
	public GffChrMap(String gffType, String gffFile, String chrFile,String readsBed, int binNum, boolean HanYanFstrand) {
		super(gffType, gffFile, chrFile, readsBed, binNum);
		this.HanYanFstrand = HanYanFstrand;
	}
	
	/**
	 * @param readsFile mapping的结果文件，必须排过序，一般为bed格式
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			if (HanYanFstrand) {
				mapReads = new MapReadsHanyanChrom(binNum, readsFile);
				mapReads.setChrLenFile(getRefLenFile());
				mapReads.setNormalType(mapNormType);
			}
			else {
				mapReads = new MapReads(binNum, readsFile);
				mapReads.setChrLenFile(getRefLenFile());
				mapReads.setNormalType(mapNormType);
			}
		}
	}
	
	/**
	 * 读取bed文件
	 */
	public void readMapBed() {
		try {
			mapReads.ReadMapFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique Unique的reads在哪一列 novelbio的标记在第七列，从1开始计算
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param cis5to3 是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique, boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, colUnique, booUniqueMapping, cis5to3);
	}
	
	/**
	 * 返回某条染色体上的reads情况，不是密度图，只是简单的计算reads在一个染色体上的情况
	 * 主要用于RefSeq时，一个基因上的reads情况
	 * @param chrID
	 * @param thisInvNum 每个区间几bp
	 * @parm type 取样方法 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 没有的话就返回null
	 */
	public double[] getChrInfo(String chrID, int thisInvNum, int type)
	{
		double[] tmpResult = mapReads.getRengeInfo(thisInvNum, chrID, 0, 0, type);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}
	

	
	int maxresolution =10000;
	
	/**
	 * 画出所有染色体上密度图
	 * @param gffChrMap2 是否有第二条染色体，没有的话就是null
	 * @throws Exception
	 */
	public void getAllChrDist(GffChrMap gffChrMap2) 
	{
		ArrayList<String[]> chrlengthInfo=seqHash.getChrLengthInfo();
		for (int i = chrlengthInfo.size()-1; i>=0; i--) {
			try {
				getChrDist(chrlengthInfo.get(i)[0], maxresolution, gffChrMap2);
			} catch (Exception e) { 	e.printStackTrace();			}
		}
	}
	
	
	/**
	 * 
	 * 给定染色体，返回该染色体上reads分布
	 * @param chrID 第几个软色体
	 * @param maxresolution 最长分辨率
	 * @param gffChrMap2 如果需要画第二条染色体的图，也就是对称了画
	 * @param 输出文件名，带后缀"_chrID"
	 * @throws Exception
	 */
	private void getChrDist(String chrID,int maxresolution, GffChrMap gffChrMap2) throws Exception
	{
		int[] resolution=seqHash.getChrRes(chrID, maxresolution);
		double[] chrReads=getChrDensity(chrID.toLowerCase(),resolution.length);
		long chrLength =seqHash.getChrLength(chrID);
		if (chrReads!=null)
		{
			TxtReadandWrite txtRparamater=new TxtReadandWrite();
			////////// 参 数 设 置 /////////////////////
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM,true, false);
			txtRparamater.writefile("Item"+"\t"+"Info"+"\r\n");//必须要加上的，否则R读取会有问题
			txtRparamater.writefile("tihsresolution"+"\t"+chrLength+"\r\n");
			txtRparamater.writefile("maxresolution"+"\t"+seqHash.getChrLenMax()+"\r\n");
			txtRparamater.writefile("ChrID"+"\t"+chrID+"\r\n");
			
			////////// 数 据 输 入 ///////////////////////
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X, true,false);
			txtRparamater.Rwritefile(resolution);
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y, true,false);
			txtRparamater.Rwritefile(chrReads);
			
			///////////如果第二条染色体上有东西，那么也写入文本/////////////////////////////////////////
			if (gffChrMap2!=null) 
			{
				double[] chrReads2=gffChrMap2.getChrDensity(chrID.toLowerCase(), resolution.length);
				txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_2Y, true,false);
				txtRparamater.Rwritefile(chrReads2);
			}
			hist();
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X,chrID+"readsx");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y,chrID+"readsy");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_2Y,chrID+"reads2y");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM,chrID+"parameter");
		}
	}
	
	/**
	 * 返回某条染色体上的reads情况，是密度图
	 * 主要用于基因组上，一条染色体上的reads情况
	 * @param chrID
	 * @param binNum 分成几个区间
	 * @parm type 取样方法 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 没有的话就返回null
	 */
	private double[] getChrDensity(String chrID, int binNum)
	{
		double[] tmpResult = mapReads.getReadsDensity(chrID, 0, 0, binNum);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}
	
	/**
	 * 调用R画图
	 * @throws Exception
	 */
	private void hist() throws Exception
	{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+ NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
	
	
	
	/**
	 * 指定一个区域范围的list，返回每个list的reads信息
	 * 默认加权平均
	 * @param lsMapInfo 直接修改本List
	 * @param binNum
	 */
	private void getRegionReads( int binNum) {
		int type = 0;
		mapReads.getRegionLs(binNum, lsMapInfos, type);
	}
	
	private void getTssMapInfo()
	{
		super.getPeakGeneStructure(lsPeakInfo, structure);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 专为韩燕设计<br>
	 * 当为refseq时，获得的某个基因的分布情况，按照3个barcode划分
	 * @return
	 * 没有该基因则返回null
	 */
	public double[] getGeneReadsHYRefseq(String geneID) {
		double[] tmpResult = getChrInfo(geneID, 1, 0);
		if (tmpResult == null) {
			return null;
		}
		//获得具体转录本的信息
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return combineLoc(tmpResult, gffGeneIsoInfoOut.getLenUTR5()+1);
	}
	/**
	 * 给定atg位点，获得该atg位点在合并后的序列中应该是第几个，从1开始
	 * @param atgSite
	 * @return
	 */
	public int getCombAtgSite(String geneID)
	{
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		int atgSite = gffGeneIsoInfoOut.getLenUTR5()+1;
		//除以3是指3个碱基
		return (int)Math.ceil((double)(atgSite -  1)/3);
	}
	
	/**
	 * 给定atg位点，获得该atg位点在合并后的序列中应该是第几个，从1开始
	 * @param atgSite
	 * @return
	 */
	public int getAtgSite(String geneID)
	{
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return gffGeneIsoInfoOut.getLenUTR5()+1;
		//除以3是指3个碱基
	}
	/**
	 * 专为韩燕设计
	 * 将三个碱基合并为1个coding，取3个的最后一个碱基对应的reads数
	 * @param geneReads 该基因的reads信息，必须是单碱基精度
	 * @param AtgSite 该基因的atg位点，从1开始计算
	 * @return
	 * 返回经过合并的结果，譬如
	 * {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
	 * atg位点为6
	 * 结果{ 2,5,8,11,14,17};
	 */
	private double[] combineLoc(double[] geneReads, int AtgSite)
	{
		//此时的SeqInfo第一位就是实际的第一位，不是atgsite了		
		return MathComput.mySplineHY(geneReads, 3, AtgSite, 3);
	}

	/**
	 * 仅给<b>韩燕</b>使用<br>
	 * 获得基因的信息，然后排序，可以从里面挑选出最大的几个然后画图
	 * 返回经过排序的mapinfo的list，每一个mapInfo包含了该基因的核糖体信息
	 */
	public ArrayList<MapInfo> getChrInfo() {
		ArrayList<String> lsChrID = mapReads.getChrIDLs();
		ArrayList<MapInfo> lsMapInfo = new ArrayList<MapInfo>();
		for (String string : lsChrID) {
			mapReads.setNormalType(MapReads.NORMALIZATION_NO);
			GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(string);
			if (!gffGeneIsoInfo.getGeneType().equals( GffGeneIsoInfo.TYPE_GENE_MRNA)
					&& !gffGeneIsoInfo.getGeneType().equals( GffGeneIsoInfo.TYPE_GENE_MRNA_TE)
			) {
				continue;
			}
			
			double[] tmp = mapReads.getRengeInfo(mapReads.getBinNum(), string, 0, 0,0);
			mapReads.setNormalType(super.mapNormType);
			double[] tmp2 = mapReads.getRengeInfo(mapReads.getBinNum(), string, 0, 0,0);
			///////////////////  异 常 处 理 /////////////////////////////////////////////////////////////////////
			if (tmp == null && tmp2 == null) {
				continue;
			}
			else if (tmp == null) {
				tmp = new double[tmp2.length];
			}
			else if (tmp2 == null) {
				tmp2 = new double[tmp.length];
			}
			////////////////////////////////////////////////////////////////////////////////////////
			int combatgSite = getCombAtgSite(string);
			tmp2 = combineLoc(tmp2, getAtgSite(string));

			
			
			double weight = MathComput.sum(tmp);
			MapInfo mapInfo = new MapInfo(string);
			mapInfo.setWeight(weight);
			mapInfo.setDouble(tmp2);
			mapInfo.setFlagLoc(combatgSite);
			CopedID copedID = new CopedID(string, 0, false);
			mapInfo.setTitle(copedID.getSymbo());
			lsMapInfo.add(mapInfo);
		}
		Collections.sort(lsMapInfo);
		return lsMapInfo;
	}
	
	
}
