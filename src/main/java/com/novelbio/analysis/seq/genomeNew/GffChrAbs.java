package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * GffHashGene和SeqHash都是static，也就是一次只能对一个物种进行分析
 * MapReads不是static，也就是可以同时处理多个mapping文件
 * @author zong0jie
 *
 */
public class GffChrAbs {
private static final Logger logger = Logger.getLogger(GffChrGene.class);
	
	private int taxID = 0;
	private int distanceMapInfo = 3000;
	static GffHashGene gffHashGene = null;
			
	static SeqHash seqHash = null;
	
	MapReads mapReads = null;
	/**
	 * 设定一系列的坐标位点
	 */
	ArrayList<MapInfo> lsMapInfos = null;
	/**
	 * 设定本次需要进行分析的MapInfo list
	 * @param lsMapInfos
	 */
	public void setLsMapInfos(ArrayList<MapInfo> lsMapInfos) {
		this.lsMapInfos = lsMapInfos;
	}
	/**
	 * 获得本次需要分析的MapInfo list
	 * @return
	 */
	public ArrayList<MapInfo> getLsMapInfos() {
		return lsMapInfos;
	}
	public static GffHashGene getGffHashGene() {
		return gffHashGene;
	}
	public static SeqHash getSeqHash() {
		return seqHash;
	}
	public MapReads getMapReads() {
		return mapReads;
	}
	
	/**
	 * 标准化方法，默认为不标准化
	 */
	int mapNormType = MapReads.NORMALIZATION_NO;
	int upBp = 5000;//tss和tes以及其他位点的上游长度，默认5000
	int downBp = 5000;//tss和tes以及其他位点的下游长度，默认5000
	
	int tssUpBp = 3000;
	int tssDownBp = 2000;
	int geneEnd3UTR = 100;
	
	
	static String chrFile = "";
	static String chrRegx = "";
	
	/**
	 * 
	 * @param gffType
	 * @param gffFile
	 * @param chrFile
	 * @param readsBed
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 */
	public GffChrAbs(String gffType, String gffFile, String chrFile,String readsBed ,int binNum)
	{
		setGffFile(gffType, gffFile);
		setChrFile(chrFile, null);
		this.setMapReads(readsBed, binNum);
	}
	/**
	 * @param gffType
	 * @param gffFile
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，
	   则用该正则表达式提取含有该文件名的文件 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param readsBed
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确

	 */
	public GffChrAbs(String gffType, String gffFile, String chrFile, String regx, String readsBed, int binNum)
	{
		setGffFile(gffType, gffFile);
		setChrFile(chrFile, regx);
		this.setMapReads(readsBed, binNum);
	}
	/**
	 * 设定mapreads的标准化方法
	 * @param mapNormType 在 MapReads.NORMALIZATION_ALL_READS 中选择，默认MapReads.NORMALIZATION_ALL_READS
	 */
	public void setMapNormType(int mapNormType) {
		this.mapNormType = mapNormType;
		if (mapReads != null) {
			mapReads.setNormalType(mapNormType);
		}
	}
	/**
	 * @param upBp tss和tes以及其他位点的上游长度，默认5000
	 * @param downBp tss和tes以及其他位点的下游长度，默认5000
	 */
	public void region(int upBp, int downBp) {
		this.upBp = upBp;
		this.downBp = downBp;
	}
	/**
	 * 设定基因的定位区域信息
	 * @param tssUpBp 设定基因的转录起点上游长度，默认为3000bp
	 * @param tssDownBp 设定基因的转录起点下游长度，默认为2000bp
	 * @param geneEnd3UTR 设定基因结尾向外延伸的长度，默认为100bp
	 */
	public void setGeneRange(int tssUpBp, int tssDownBp, int geneEnd3UTR) {
		this.tssUpBp = tssDownBp;
		this.tssDownBp = tssDownBp;
		this.geneEnd3UTR = geneEnd3UTR;
		GffDetailGene.setCodLocation(tssUpBp, tssDownBp, geneEnd3UTR);
	}


	
	public static void setGffFile(String gffType, String gffFile)
	{
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
		}
	}
	
	/**
	 chrFile 序列文件或序列文件夹
    regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，
    则用该正则表达式提取含有该文件名的文件 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param chrFile
	 * @param regx
	 */
	public static void setChrFile(String chrFile, String regx) {
		GffChrAbs.chrFile = chrFile;
		GffChrAbs.chrRegx = regx;
	}
	
	protected void loadChrFile() {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, chrRegx);
		}
	}
	
	/**
	 * @param readsFile mapping的结果文件，必须排过序，一般为bed格式
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			mapReads = new MapReads(binNum, readsFile);
			mapReads.setChrLenFile(getRefLenFile());
			mapReads.setNormalType(mapNormType);
		}
	}
	
	protected void loadMapReads() {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, chrRegx);
		}
	}
	
	
	/**
	 * 获得指定文件内的坐标信息
	 * 如果两个位点终点的间距在distanceMapInfo以内，就会删除那个权重低的
	 * @param txtExcel
	 * @param colChrID
	 * @param colStartLoc
	 * @param colEndLoc
	 * @param colScore 打分，也就是权重，没有该列的话，就设置为0
	 * @param rowStart
	 */
	public void getFileRegion(String txtExcel, int colChrID, int colStartLoc, int colEndLoc, int colScore,int rowStart)
	{
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID,colStartLoc,colEndLoc};
		}
		else {
			columnID = new int[]{colChrID,colStartLoc,colEndLoc, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			mapInfo.setStartLoc(Integer.parseInt(strings[1]));
			mapInfo.setEndLoc(Integer.parseInt(strings[2]));
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStartLoc(0);;
			}
			if (colScore > 0) {
				mapInfo.setWeight(Double.parseDouble(strings[3]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
	}
	/**
	 * 获得summit两端各region的区域，总共就是region*2+1的区域
	 * 如果两个位点终点的间距在distanceMapInfo以内，就会删除那个权重低的
	 * @param txtExcel
	 * @param region
	 * @param colChrID
	 * @param colSummit
	 * @param rowStart
	 */
	public void getFileSite(String txtExcel,int region ,int colChrID, int colSummit, int colScore, int rowStart)
	{
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID, colSummit, colScore};
		}
		else {
			columnID = new int[]{colChrID, colSummit, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			int start = Integer.parseInt(strings[1]);
			mapInfo.setFlagLoc(Integer.parseInt(strings[1]));
			mapInfo.setStartLoc(mapInfo.getFlagSite() - region);
			mapInfo.setEndLoc(mapInfo.getFlagSite() + region);
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStartLoc(0);;
			}
			if (colScore > 0) {
				mapInfo.setWeight(Double.parseDouble(strings[2]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
	}
	
	/**
	 * 
	 * @param structure GffDetailGene.TSS等
	 * @return
	 */
	public ArrayList<MapInfo> getPeakCoveredGeneMapInfo(String structure)
	{
		Set<GffDetailGene> setGffDetailGenes = getPeakGeneStructure(structure);
		 ArrayList<MapInfo> lsResult = getMapInfoFromGffGene(setGffDetailGenes, structure);
		 return lsResult;
	}
	
	
	/**
	 * 根据前面设定upBp和downBp
	 * 给定一系列gffDetailGene，以及想要的部分，返回对应区域的LsMapInfo
	 * @param setgffDetailGenes
	 * @param structure
	 * @return
	 */
	protected ArrayList<MapInfo> getMapInfoFromGffGene(Set<GffDetailGene> setgffDetailGenes, String structure)
	{
		lsMapInfos = new ArrayList<MapInfo>();
		for (GffDetailGene gffDetailGene : setgffDetailGenes) {
			lsMapInfos.add(getStructureLoc(gffDetailGene, structure));
		}
		return lsMapInfos;
	}

	
	/**
	 * 给定peak的信息，chrID和起点终点，返回被peak覆盖到Tss的基因名和覆盖情况，用于做Tss图
	 * 自动去冗余基因
	 * @param lsPeakInfo mapInfo必须有 chrID 和 startLoc 和 endLoc 三项
	 */
	protected HashSet<GffDetailGene> getPeakGeneStructure(String structure) {
		//存储最后基因的数量
		HashSet<GffDetailGene> hashGffDetailGenes = new HashSet<GffDetailGene>();
		for (MapInfo mapInfo : lsMapInfos) {
			if (mapInfo.getStart() <0 && mapInfo.getStart() > -1000) {
				mapInfo.setStartLoc(0);;
			}
			hashGffDetailGenes.addAll(getPeakStructureGene(mapInfo.getChrID(), mapInfo.getStart(), mapInfo.getEnd(), structure ) );
		}
		return hashGffDetailGenes;
	}
	/**
	 * 给定坐标区域，返回该peak所覆盖的GffDetailGene
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure
	 * @return
	 */
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
	/**
	 * 前面设定upBp和downBp
	 * 给定gffDetailGene，以及想要的部分，返回对应区域的MapInfo
	 * @param gffDetailGene
	 * @param structure GffDetailGene.TSS等
	 * @return
	 */
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
	 * 获得每条转录本的长度，如果outFile不存在，那么必须给出一个outFile的文件名
	 * 并且SeqHash对象是存在的
	 * @param RefSeqFile
	 * @return
	 */
	public String getRefLenFile()
	{
		String outFile = FileOperate.changeFileSuffix(chrFile, "_chrLen", "list");
		if (FileOperate.isFileExist(outFile)) {
			return outFile;
		}
		 loadChrFile();
		ArrayList<String[]> lsChrLen = seqHash.getChrLengthInfo();
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outFile, true);
		txtReadandWrite.ExcelWrite(lsChrLen, "\t", 1, 1);
		return outFile;
	}
	
}
