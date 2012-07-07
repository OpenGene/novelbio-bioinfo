package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;


import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

/**
 * 读取samtools产生的snp文件，获得全基因组范围内的snp分布情况
 * @author zong0jie
 *
 */
public class MapReadsSnp {
	public static void main(String[] args) {
		String outFile = "/media/winF/NBC/Project/Project_ZDB_Lab/HY/BZ_20120521/mappingresult/DifSnp_testMutvsWT";
		MapReadsSnp mapReadsSnp = new MapReadsSnp();
		
		Species species = new Species(39947);
		mapReadsSnp.setMapChrID2Len(species.getMapChromInfo());
		mapReadsSnp.readSamPileUpFileFirstScan("WT", "/media/winF/NBC/Project/Project_ZDB_Lab/HY/BZ_20120521/mappingresult/BZ171-9522detailmpileup");
		mapReadsSnp.readSamPileUpFileFirstScan("Mut", "/media/winF/NBC/Project/Project_ZDB_Lab/HY/BZ_20120521/mappingresult/BZ171-269detailmpileup");
//		mapReadsSnp.readSamPileUpFileFirstScan("Mut", "/media/winF/NBC/Project/Project_ZDB_Lab/HY/BZ_20120521/mappingresult/Muttest");
//		mapReadsSnp.readSamPileUpFileFirstScan("WT", "/media/winF/NBC/Project/Project_ZDB_Lab/HY/BZ_20120521/mappingresult/WTtest");

		HashMap<String, ArrayList<int[]>> mapChrID2SnpInfo = mapReadsSnp.getSampleDetailSnp("Mut", "WT");
		for (Entry<String, ArrayList<int[]>> entry : mapChrID2SnpInfo.entrySet()) {
			String outTxt = FileOperate.changeFilePrefixReal(outFile, entry.getKey() + "_", null);
			TxtReadandWrite txtOut = new TxtReadandWrite(outTxt, true);
			ArrayList<int[]> lsSnpInfo = entry.getValue();
			for (int[] is : lsSnpInfo) {
				txtOut.writefileln(is[0] + "\t" + is[1]);
			}
			txtOut.close();
		}
	}
	
	private static Logger logger = Logger.getLogger(MapReadsSnp.class);
	
	int taxID = 0;
	int foldPlus = 10000;
	
	HashMap<String, String> mapSample2PileUpFile = new HashMap<String, String>();
	
	/**
	 * 第一步扫描，找出所有与genome有差异的snp
	 *  key: sampleID to value: snpInfo<br>
	 * key: chrID
	 * value: site num
	 *   chrID都为小写
	 */
	HashMap<String, HashMap<String, int[]>> mapSample_To_Chr2SnpSiteInfo = new HashMap<String, HashMap<String,int[]>>();
	/** 第一步扫描，snp覆盖小于该reads就认为没有snp */
	int minSnpReadsFirstScan = 3;
	/** 第一步扫描，snp比例小于该比例就认为没有snp */
	double minSnpProp = 0.1;
	
	
	/** 第一步扫描获得的，貌似差异的snp位点
	 * 接下来就要用该信息继续扫描两个样本，确定差异snp
	 *  */
	HashMap<String, ArrayList<MapInfoSnpIndel>> mapChrID2LsMapInfoSnpIndell = new HashMap<String, ArrayList<MapInfoSnpIndel>>();
	
	/** 检查差异snp时，wt所必须要有的覆盖数*/
	int minMutReadsCoverage = 5;
	/** 检查差异snp时，wt所必须要有的覆盖数*/
	int minMutSnpReadsCoverage = 3;
	/** 检查差异snp时，wt所必须要有的覆盖数*/
	int minWTReadsCoverage = 8;
	/** 检查差异snp时，wt所能容许的最大reads数，理论上为0，考虑错配，可以设置为1*/
	int maxWTSnpReadsCount = 2;

	/** 序列信息,名字都为小写 */
	HashMap<String, Long> mapChrID2Len = new HashMap<String, Long>();
	/** 保存mapping文件中出现过的每个chr 的长度 */
	ArrayList<String[]> lsChrLength=new ArrayList<String[]>();

	public MapReadsSnp() {}
	
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	 /**
	  * 返回所有chrID的list
	  * @return
	  */
	public ArrayList<String> getChrIDLs() {
		return ArrayOperate.getArrayListKey(mapChrID2Len);
	}
	public void readChrLenFile(ArrayList<String[]> lsChrLen) {
		for (String[] string : lsChrLen) {
			mapChrID2Len.put(string[0].toLowerCase(), Long.parseLong(string[1]));
		}
	}
	public void setMapChrID2Len(HashMap<String, Long> mapChrID2Len) {
		this.mapChrID2Len = mapChrID2Len;
	}
	
	public HashMap<String, ArrayList<int[]>> getSampleDetailSnp(String sampleMut, String sampleWT) {
		compareSnpInfoFirstScanInToMapChrID2SnpDetail(sampleMut, sampleWT);
		String samToolsPleUpFile = mapSample2PileUpFile.get(sampleMut);
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapChrID2LsMapInfoSnpIndelMut = MapInfoSnpIndel.getSiteInfo(mapChrID2LsMapInfoSnpIndell, samToolsPleUpFile, null);
		samToolsPleUpFile = mapSample2PileUpFile.get(sampleWT);
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapChrID2LsMapInfoSnpIndelWT = MapInfoSnpIndel.getSiteInfo(mapChrID2LsMapInfoSnpIndell, samToolsPleUpFile, null);
		return compareDifSnpMut2WT(mapChrID2LsMapInfoSnpIndelMut, mapChrID2LsMapInfoSnpIndelWT);
	}
	/**
	 * 输入的两个map应该是一模一样，除了里面的snp信息有不同，挑选出Mut相对于WT差异的snp
	 * @param mapChrID2LsMapInfoSnpIndelMut
	 * @param mapChrID2LsMapInfoSnpIndelWT
	 * @return chrID--lsSnpInfo
	 * 0：坐标
	 * 1：坐标信息
	 */
	private HashMap<String, ArrayList<int[]>> compareDifSnpMut2WT(HashMap<String, ArrayList<MapInfoSnpIndel>> mapChrID2LsMapInfoSnpIndelMut, 
			HashMap<String, ArrayList<MapInfoSnpIndel>> mapChrID2LsMapInfoSnpIndelWT) {
		HashMap<String, ArrayList<int[]>> mapChrID2LsSnpInfo = new HashMap<String, ArrayList<int[]>>();
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(mapChrID2LsMapInfoSnpIndelMut);
		for (String chrID : lsChrID) {
			ArrayList<int[]> lsResult = new ArrayList<int[]>();
			ArrayList<MapInfoSnpIndel> lsMut = mapChrID2LsMapInfoSnpIndelMut.get(chrID);
			ArrayList<MapInfoSnpIndel> lsWT = mapChrID2LsMapInfoSnpIndelWT.get(chrID);
			for (int i = 0; i < lsMut.size(); i++) {
				MapInfoSnpIndel mapInfoSnpIndelMut = lsMut.get(i);
				MapInfoSnpIndel mapInfoSnpIndelWT = lsWT.get(i);
				int[] snpInfo = compareSnpDif(mapInfoSnpIndelMut, mapInfoSnpIndelWT);
				if (snpInfo != null) {
					lsResult.add(snpInfo);
				}
			}
			mapChrID2LsSnpInfo.put(chrID, lsResult);
		}
		return mapChrID2LsSnpInfo;
	}
	/**
	 * 返回比较的结果，用int[] 表示
	 * @param mapInfoSnpIndelMut
	 * @param mapInfoSnpIndelWT
	 * @return
	 * 0：坐标
	 * 1：该坐标snp的比例
	 */
	private int[] compareSnpDif(MapInfoSnpIndel mapInfoSnpIndelMut, MapInfoSnpIndel mapInfoSnpIndelWT) {
		int[] result = new int[2];
		
		SiteSnpIndelInfo siteSnpIndelInfoMut = mapInfoSnpIndelMut.getBigAllenInfo();
		SiteSnpIndelInfo siteSnpIndelInfoWT = mapInfoSnpIndelWT.getBigAllenInfo();
		//mut没有snp
		if (siteSnpIndelInfoMut == null || siteSnpIndelInfoMut.getThisBaseNum() == 0) {
			return null;
		}
		//mut覆盖度要到位，并且snp位点的reads小于指定的最小容许错配
		//添加一句 
		if (mapInfoSnpIndelWT.getRead_Depth_Filtered() > minWTReadsCoverage 
				&& (siteSnpIndelInfoWT == null
				      || (siteSnpIndelInfoWT.getThisBaseNum() <= maxWTSnpReadsCount
						 && (siteSnpIndelInfoWT.getThisBaseNum() *100 / mapInfoSnpIndelWT.getRead_Depth_Filtered() < 1 
								 || !siteSnpIndelInfoWT.getThisSeq().equals(siteSnpIndelInfoMut.getThisSeq())
							   )
						 )
					 )
				&& mapInfoSnpIndelMut.getRead_Depth_Filtered() > minMutReadsCoverage
				&& siteSnpIndelInfoMut.getThisBaseNum() > minMutSnpReadsCoverage
			)
		 {
			result[0] = (int)((double)siteSnpIndelInfoMut.getThisBaseNum() * foldPlus/mapInfoSnpIndelMut.getRead_Depth_Filtered());
			result[1] = mapInfoSnpIndelMut.getRefSnpIndelStart();
			return result;
		}
		return null;
	}
	
	/**
	 * 第一次scan，挑选出差异的snp，装入mapChrID2LsMapInfoSnpIndell
	 * @param sampleMut
	 * @param sampleWt
	 * @param outFile
	 */
	private void compareSnpInfoFirstScanInToMapChrID2SnpDetail(String sampleMut, String sampleWt) {
		HashMap<String, int[]> mapChrID2SnpSiteInfoMut = mapSample_To_Chr2SnpSiteInfo.get(sampleMut);
		HashMap<String, int[]> mapChrID2SnpSiteInfoWt = mapSample_To_Chr2SnpSiteInfo.get(sampleWt);
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(mapChrID2SnpSiteInfoMut);
		for (String chrID : lsChrID) {
			int[] snpSiteMut = mapChrID2SnpSiteInfoMut.get(chrID);
			int[] snpSiteWT = mapChrID2SnpSiteInfoWt.get(chrID);
			ArrayList<MapInfoSnpIndel> lsDifSnpInfo = compareChrSnpInfo(chrID, snpSiteMut, snpSiteWT);
			mapChrID2LsMapInfoSnpIndell.put(chrID, lsDifSnpInfo); 
		}
	}
	/**
	 * 比较两个样本的snp情况，将不同的位点挑选出来，方便后续第二次筛选
	 * 本步为初筛
	 */
	private ArrayList<MapInfoSnpIndel> compareChrSnpInfo(String chrID, int[] snpSiteMut, int[] snpSiteWT) {
		ArrayList<MapInfoSnpIndel> lsSnpInfoResult = new ArrayList<MapInfoSnpIndel>();
		int MutSite = 0, WTSite = 0;
		//MUT --------1st---0---------------2nd--0------------0--------------------------3th--------0-------------------------------------------0-------------------End----0--0--0--0--0-----
		//WT   --------1st---0---------------2nd-------------------------------0-----------3th---0-----------------------0---------------------------------------0---End--------------------------
		while (MutSite < snpSiteMut.length) {
			//End之后的情况
			if (WTSite >= snpSiteWT.length) {
				for (int i = MutSite; i < snpSiteMut.length; i++) {
					MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(chrID, snpSiteMut[MutSite]);
					lsSnpInfoResult.add(mapInfoSnpIndel);
				}
				break;
			}
			int MutLoc = snpSiteMut[MutSite];
			int WTLoc = snpSiteWT[WTSite];
			//1st之后的情况
			if (MutLoc == WTLoc) {
				MutSite++; WTSite++;
				continue;
			}
			//2nd之后的情况
			else if (MutLoc < WTLoc) {
				MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(chrID, snpSiteMut[MutSite]);
				lsSnpInfoResult.add(mapInfoSnpIndel);
				MutSite++;
				continue;
			}
			//3th之后的情况
			else if (MutLoc > WTLoc) {
				WTSite++;
				continue;
			}
		}
		return lsSnpInfoResult;
	}
	
	public void readSamPileUpFileFirstScan(String sampleID, String fileSamPileUp) {
		mapSample2PileUpFile.put(sampleID, fileSamPileUp);
		
		HashMap<String, int[]> mapChrID2SnpSiteInfo = new HashMap<String, int[]>();
		mapSample_To_Chr2SnpSiteInfo.put(sampleID, mapChrID2SnpSiteInfo);
		
		TxtReadandWrite txtReadPileUp = new TxtReadandWrite(fileSamPileUp, false);
		String lastChr = "";
		int[] chrBpReads = null;//保存每个bp的snp信息，其中chrBpReads[0] 留空，然后chrBpReads[1] 就是实际第一个位点的snp
		
		boolean flagExistChrID = true;// 当没有该染色体时标记为false并且跳过所有该染色体上的坐标
		int numSite = 0;
		for (String string : txtReadPileUp.readlines()) {
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(taxID, null, string);
			if (!mapInfoSnpIndel.getRefID().toLowerCase().equals(lastChr) ) {
				if (!lastChr.equals("") && flagExistChrID) {
					addSumArrayToMapResult(sampleID, lastChr, chrBpReads);
				}
				lastChr = mapInfoSnpIndel.getRefID().toLowerCase();// 实际这是新出现的ChrID
				logger.info(lastChr);
				int chrLength = 0;
				try {
					chrLength =  mapChrID2Len.get(lastChr.toLowerCase()).intValue();
					addToLsChrLength(lastChr, chrLength);
					flagExistChrID = true;
				} catch (Exception e) {
					logger.error("出现未知chrID "+lastChr);
					flagExistChrID = false;
					continue;
				}
				chrBpReads = new int[chrLength+1];// 同样为方便，0位记录总长度。这样实际bp就是实际长度
			}
			numSite = mapInfoSnpIndel.getRefSnpIndelStart();
			//因为这个染色体数组只能保存int，所以将double的小数乘以10000，然后保存
			if (mapInfoSnpIndel.getBigAllenInfo() == null
					|| (mapInfoSnpIndel.getBigAllenInfo().getThisBaseNum() < minSnpReadsFirstScan
					     && (double)mapInfoSnpIndel.getBigAllenInfo().getThisBaseNum()/mapInfoSnpIndel.getRead_Depth_Filtered() < minSnpProp )) {
				chrBpReads[numSite] = 0;
				continue;
			}
			chrBpReads[numSite] = (int) (mapInfoSnpIndel.getBigAllenInfo().getThisBaseProp() * foldPlus);
		}
		if (flagExistChrID) {
			addSumArrayToMapResult(sampleID, lastChr, chrBpReads);
		}
		sortLsChrLength();
	}
	/**
	 * 	SumChrBpReads设定，并装入map表
	 * @param chrID
	 * @param chrLength
	 * @return
	 */
	private void addSumArrayToMapResult(String sampleID, String chrID, int[] chrSnpProp) {
		HashMap<String, int[]> mapChrID2SnpSiteInfo = mapSample_To_Chr2SnpSiteInfo.get(sampleID);
		LinkedList<Integer> lsSnpSite = new LinkedList<Integer>();
		for (int i = 0; i < chrSnpProp.length; i++) {
			if(chrSnpProp[i] > minSnpProp * foldPlus) {
				lsSnpSite.add(i);
			}
		}
		int[] sumSnpProp = new int[lsSnpSite.size()];
		int mm = 0;
		for (int num : lsSnpSite) {
			sumSnpProp[mm] = num;
			mm++;
		}

		mapChrID2SnpSiteInfo.put(chrID, sumSnpProp);		
	}
	/**
	 * 将每一条序列长度装入lsChrLength
	 * @param chrID
	 * @param chrLength
	 */
	private void addToLsChrLength(String chrID, int chrLength) {
		String[] tmpChrLen = new String[2];
		tmpChrLen[0] = chrID;
		tmpChrLen[1] = chrLength + "";
		lsChrLength.add(tmpChrLen);
	}
	/** 把lsChrLength按照chrLen从小到大进行排序 */
	private void sortLsChrLength() {
		Collections.sort(lsChrLength,new Comparator<String[]>(){
			public int compare(String[] arg0, String[] arg1) {
				Integer chr1 = Integer.parseInt(arg0[1]);
				Integer chr2 = Integer.parseInt(arg1[1]);
				return chr1.compareTo(chr2);
			}
		});
	}
}
