package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.fileOperate.FileOperate;

public class GffChrGene {
	private static final Logger logger = Logger.getLogger(GffChrGene.class);
	
	
	private static GffHashGene gffHashGene = null;
			
	private static SeqHash seqHash = null;
	
	private MapReads mapReads = null;
	
	/**
	 * 每个区域内所含的bp数，大于等于binNum，最好是binNum的倍数 如果binNum ==1 && binNum == 1，结果会很精确
	 */
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	int upBp = 5000;//tss和tes以及其他位点的上游长度，默认5000
	int downBp = 5000;//tss和tes以及其他位点的下游长度，默认5000
	/**
	 * 
	 * 设定mapreads的一系列参数
	 * @param mapNormType 在 MapReads.NORMALIZATION_ALL_READS 中选择，默认MapReads.NORMALIZATION_ALL_READS
	 * @param upBp tss和tes以及其他位点的上游长度，默认5000
	 * @param downBp tss和tes以及其他位点的下游长度，默认5000
	 */
	public void setMapNormType(int mapNormType, int upBp, int downBp) {
		this.mapNormType = mapNormType;
		this.upBp = upBp;
		this.downBp = downBp;
	}
	
	
	public GffChrGene(String gffType, String gffFile, String chrFile, String readsBed, int binNum)
	{
		this.setGffFile(gffType, gffFile);
		this.setChrFile(chrFile, null);
		this.setMapReads(readsBed, binNum);
	}
	public GffChrGene(String gffType, String gffFile, String chrFile, String regx, String readsBed, int binNum)
	{
		this.setGffFile(gffType, gffFile);
		this.setChrFile(chrFile, regx);
		this.setMapReads(readsBed, binNum);
	}
	
	public void setGffFile(String gffType, String gffFile)
	{
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
		}
	}
	
	public void setChrFile(String chrFile, String regx) {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, regx);
		}
	}
	/**
	 * @param readsFile mapping的结果文件，必须排过序，一般为bed格式
	 * @param binNum 每隔多少位计数
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			mapReads = new MapReads(binNum, readsFile);
		}
	}
	
	
	
	/**
	 * 
	 * 给定一系列位点然后获得信息
	 * @param lssummit summit信息 0:chrID 1:summit 2:weight
	 * @param range
	 * @param sortmin2max
	 * @param thisBinNum 待分割的区域数目
	 * @return
	 */
	protected List<MapInfo> getLocInfo(ArrayList<String[]> lssummit, int range, boolean sortmin2max, int thisBinNum) {
		ArrayList<MapInfo> lsTmp = new ArrayList<MapInfo>();
		for (String[] strings : lssummit) {
			int locSummit = Integer.parseInt(strings[1]);
			double weight = Double.parseDouble(strings[2]);
			MapInfo mapInfo = new MapInfo(strings[0], locSummit - range, locSummit + range, locSummit, weight,strings[0]+ locSummit);
		}
		MapInfo.sortPath(sortmin2max);
		List<MapInfo> lsResult= MapInfo.sortLsMapInfo(lsTmp, range);
		mapReads.getRegionLs(thisBinNum, lsResult, 0, mapNormType);
		return lsResult;
	}
	
	/**
	 * 设定每个区域的bp，然后获得结果
	 * @param lsPeakInfo peak信息 0:chrID 1:startLoc 2:endLoc
	 * @param upBp
	 * @param downBp
	 * @param thisInvNum 每个区域内所含的bp数，大于等于binNum，最好是binNum的倍数 如果binNum ==1 && binNum == 1，结果会很精确
	 */
	protected ArrayList<MapInfo> getPeakStructure(ArrayList<String[]> lsPeakInfo, int thisInvNum, String structure) {
		Set<GffDetailGene> setgffDetailGenes = getPeakGeneStructure(lsPeakInfo, structure);
		ArrayList<MapInfo> lsMapInfos = getMapInfoFromGffGene(setgffDetailGenes, structure);
		mapReads.getRegionLs(lsMapInfos, thisInvNum, 0, mapNormType);
		return lsMapInfos;
	}
	
	/**
	 * 指定待分割的区域，然后获得结果
	 * @param thisBinNum 待分割的区域数目
	 * @param lsPeakInfo 0:chrID 1:startLoc 2:endLoc
	 * @param upBp
	 * @param downBp
	 */
	protected ArrayList<MapInfo> getPeakStructure(int thisBinNum,ArrayList<String[]> lsPeakInfo, String structure) {
		Set<GffDetailGene> setgffDetailGenes = getPeakGeneStructure(lsPeakInfo, structure);
		ArrayList<MapInfo> lsMapInfos = getMapInfoFromGffGene(setgffDetailGenes, structure);
		mapReads.getRegionLs(thisBinNum, lsMapInfos, 0, mapNormType);
		return lsMapInfos;
	}
	
	private ArrayList<MapInfo> getMapInfoFromGffGene(Set<GffDetailGene> setgffDetailGenes, String structure)
	{
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (GffDetailGene gffDetailGene : setgffDetailGenes) {
			lsMapInfos.add(getStructureLoc(gffDetailGene, structure));
		}
		return lsMapInfos;
	}
	
	private MapInfo getStructureLoc(GffDetailGene gffDetailGene, String structure)
	{
		if (structure.equals(GffDetailGene.TSS)) {
			int tss = gffDetailGene.getLongestSplit().getTSSsite();
			return new MapInfo(gffDetailGene.getChrID(), tss - upBp, tss + downBp, tss,0, gffDetailGene.getLongestSplit().getIsoName());
		}
		else if (structure.equals(GffDetailGene.TES)) {
			int tes = gffDetailGene.getLongestSplit().getTSSsite();
			return new MapInfo(gffDetailGene.getChrID(), tes - upBp, tes + downBp, tes, 0, gffDetailGene.getLongestSplit().getIsoName());
		}
		else {
			logger.error("还没添加该种类型的structure");
			return null;
		}
	}
	
	/**
	 * 给定peak的信息，chrID和起点终点，返回被peak覆盖到Tss的基因名和覆盖情况，用于做Tss图
	 * @param lsPeakInfo 0:chrID 1:startLoc 2:endLoc
	 */
	private HashSet<GffDetailGene> getPeakGeneStructure(ArrayList<String[]> lsPeakInfo, String structure) {
		//存储最后基因的数量
		HashSet<GffDetailGene> hashGffDetailGenes = new HashSet<GffDetailGene>();
		for (String[] strings : lsPeakInfo) {
			int startLoc = Integer.parseInt(strings[1]);
			if (startLoc <0 && startLoc > -1000) {
				startLoc = 0;
			}
			int endLoc = Integer.parseInt(strings[2]);
			hashGffDetailGenes.addAll(getPeakStructureGene(strings[0], startLoc, endLoc, structure ) );
		}
		return hashGffDetailGenes;
	}
	
	private Set<GffDetailGene> getPeakStructureGene(String chrID, int startLoc, int endLoc, String structure) {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(chrID, startLoc, endLoc);
		if (structure.equals(GffDetailGene.TSS)) {
			return gffCodGeneDU.getTSSGene();
		}
		else if (structure.equals(GffDetailGene.TES)) {
			return gffCodGeneDU.getTESGene();
		}
		else {
			logger.error("暂时没有除Tss和Tes之外的基因结构");
			return null;
		}
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
	
	
	
	
	


