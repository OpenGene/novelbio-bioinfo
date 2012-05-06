package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * GffHashGene和SeqHash都是static，也就是一次只能对一个物种进行分析
 * MapReads不是static，也就是可以同时处理多个mapping文件
 * @author zong0jie
 *
 */
public class GffChrAbs {
private static final Logger logger = Logger.getLogger(GffChrAbs.class);
	
	private int taxID = 0;
	private int distanceMapInfo = 3000;
	static GffHashGene gffHashGene = null;
			
	static SeqHash seqHash = null;
	
	MapReads mapReads = null;
//	/**
//	 * 设定一系列的坐标位点
//	 */
//	ArrayList<MapInfo> lsMapInfos = null;
//	/**
//	 * 设定本次需要进行分析的MapInfo list
//	 * @param lsMapInfos
//	 */
//	public void setLsMapInfos(ArrayList<MapInfo> lsMapInfos) {
//		this.lsMapInfos = lsMapInfos;
//	}
//	/**
//	 * 获得本次需要分析的MapInfo list
//	 * @return
//	 */
//	public ArrayList<MapInfo> getLsMapInfos() {
//		return lsMapInfos;
//	}
	
	int[] tss = new int[]{-1500, 1500};
	int[] tes = null;
	boolean genebody = false;
	boolean UTR5 = false;
	boolean UTR3 = false;
	boolean exonFilter = false;
	boolean intronFilter = false;
	boolean filtertss = false;
	boolean filtertes = false;
	public void setFilterTssTes(int[] filterTss, int[] filterGenEnd) {
		if (filterTss != null)
			this.filtertss = true;
		else
			this.filtertss = false;
		
		if (filterGenEnd != null)
			this.filtertes = true;
		else
			this.filtertes = false;
		
		this.tss = filterTss;
		this.tes = filterGenEnd;
	}
	public void setFilterGeneBody(boolean filterGeneBody, boolean filterExon, boolean filterIntron)
	{
		this.genebody = filterGeneBody;
		this.exonFilter = filterExon;
		this.intronFilter = filterIntron;
	}
	
	public void setFilterUTR(boolean filter5UTR, boolean filter3UTR)
	{
		this.UTR5 = filter5UTR;
		this.UTR3 = filter3UTR;
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
	/**
	 * 主要用于画图
	 */
	int upBp = 5000;//tss和tes以及其他位点的上游长度，默认5000
	int downBp = 5000;//tss和tes以及其他位点的下游长度，默认5000
	
	int tssUpBp = 3000;
	int tssDownBp = 2000;
	int geneEnd3UTR = 100;
	
	
	static String chrFile = "";
	static String chrRegx = null;
	
	
	String equationsFile = "";
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
		loadChrFile();
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
		loadChrFile();
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
	 * 专门用于画图时的参数
	 * @param upBp tss和tes以及其他位点的上游长度，默认5000
	 * @param downBp tss和tes以及其他位点的下游长度，默认5000
	 */
	public void setPlotRegion(int upBp, int downBp) {
		this.upBp = upBp;
		this.downBp = downBp;
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
		loadChrFile();
	}
	
	public static void loadChrFile() {
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
			setMapCorrect();
		}
	}
	/**
	 * 给定一个文本来修正  没有文件则直接返回
	 * @param correctFile x第一列，y第二；列，从第一行开始读取
	 */
	public void setMapCorrect(String correctFile)
	{
		this.equationsFile = correctFile;
		setMapCorrect();
	}
	/**
	 * 设定用qpcr等参数校正mapping结果
	 */
	protected void setMapCorrect()
	{
		Equations equations = new Equations();
		equations.setXYFile(equationsFile);
		if (mapReads != null) {
			mapReads.setEquations(equations);
		}
	}
	public void loadMapReads() {
		try {
			mapReads.ReadMapFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public ArrayList<MapInfo> readFileRegionMapInfo(String txtExcel, int colChrID, int colStartLoc, int colEndLoc, int colScore,int rowStart)
	{
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID,colStartLoc,colEndLoc};
		}
		else {
			columnID = new int[]{colChrID,colStartLoc,colEndLoc, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			
			try {
				mapInfo.setStartEndLoc(Integer.parseInt(strings[1]),Integer.parseInt(strings[2]));
			} catch (Exception e) {
				logger.error("该坐标有问题："+mapInfo.getRefID());
				continue;
			}
			if (colScore > 0) {
				mapInfo.setScore(Double.parseDouble(strings[3]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
		return lsMapInfos;
	}
	/**
	 * 不用reads填充MapInfo
	 * 获得summit两端各region的区域，总共就是region*2+1的区域
	 * 如果两个位点终点的间距在distanceMapInfo以内，就会删除那个权重低的
	 * @param txtExcel
	 * @param region
	 * @param colChrID
	 * @param colSummit
	 * @param rowStart
	 */
	public ArrayList<MapInfo> readFileSiteMapInfo(String txtExcel,int region ,int colChrID, int colSummit, int colScore, int rowStart)
	{
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID, colSummit, colScore};
		}
		else {
			columnID = new int[]{colChrID, colSummit, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			try {
				mapInfo.setFlagLoc(Integer.parseInt(strings[1]));
			} catch (Exception e) {
				logger.error("该坐标有问题："+mapInfo.getRefID());
				continue;
			}
			mapInfo.setStartEndLoc(mapInfo.getFlagSite() - region, mapInfo.getFlagSite() + region);
			if (colScore > 0) {
				mapInfo.setScore(Double.parseDouble(strings[2]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
		return lsMapInfos;
	}
	
	/**
	 * 给定区域，自动获得基因
	 * 根据前面设定upBp和downBp
	 * @param lsMapInfos
	 * @param structure GffDetailGene.TSS等
	 * @param binNum 分成几块
	 * @return
	 */
	public ArrayList<MapInfo> getPeakCoveredGeneMapInfo(ArrayList<? extends MapInfo> lsMapInfos, String structure, int binNum)
	{
		HashMap<GffDetailGene,Double>  hashGffDetailGenes = getPeakGeneStructure( lsMapInfos, structure);
		 ArrayList<MapInfo> lsResult = getMapInfoFromGffGene(hashGffDetailGenes, structure);
		 mapReads.getRegionLs(binNum, lsResult, 0);
		 return lsResult;
	}

	/**
	 * 获得geneID以及相应权重，内部自动去冗余，保留权重高的那个，并且填充相应的reads
	 * 如果没有权重，就按照reads的密度进行排序
	 * 一般用于根据gene express 画heapmap图
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure 基因的哪个部分的结构 
	 * @param binNum 最后结果分成几块
	 */
	public ArrayList<MapInfo> readFileGeneMapInfo(String txtExcel,int colGeneID, int colScore, int rowStart, String Structure, int binNum)
	{
		////////////////////     读 文 件   ////////////////////////////////////////////
		int[] columnID = null;
		if (colScore <= 0 || colScore == colGeneID) {
			 columnID = new int[]{colGeneID};
		}
		else {
			columnID = new int[]{colGeneID, colScore};
		}	
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		return getLsGeneMapInfo(lstmp, Structure, binNum);
	}
	/**
	 * 获得geneID以及相应权重，内部自动去冗余，保留权重高的那个，并且填充相应的reads
	 * 如果没有权重，就按照reads的密度进行排序
	 * 一般用于根据gene express 画heapmap图
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure 基因的哪个部分的结构 
	 * @param binNum 最后结果分成几块
	 */
	public ArrayList<MapInfo> readGeneMapInfoAll(String Structure, int binNum)
	{
		ArrayList<String> lsGeneID = gffHashGene.getLOCChrHashIDList();
		ArrayList<String[]> lstmp = new ArrayList<String[]>();
		for (String string : lsGeneID) {
			lstmp.add(new String[]{string.split(GffDetailGene.SEP_GENE_NAME)[0]});
		}
		return getLsGeneMapInfo(lstmp, Structure, binNum);
	}
	/**
	 * 获得geneID以及相应权重，内部自动去冗余，保留权重高的那个，并且填充相应的reads
	 * 一般用于根据gene express 画heapmap图
	 * @param lsGeneValue string[2] 0:geneID 1:value 其中1 可以没有，那么就是string[1] 0:geneID
	 * @param rowStart
	 * @param Structure 基因的哪个部分的结构
	 * @param binNum 最后结果分成几块
	 * @return
	 */
	public ArrayList<MapInfo> getLsGeneMapInfo(ArrayList<String[]> lsGeneValue, String Structure, int binNum) {
		//有权重的就使用这个hash
 		HashMap<GffDetailGene, Double> hashGene2Value = new HashMap<GffDetailGene, Double>();

		for (String[] strings : lsGeneValue) {
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(strings[0]);
			if (gffDetailGene == null) {
				continue;
			}
			//have gene score, using the score as value, when the gene is same, add the score bigger one
			if (strings.length > 1) {
				if (hashGene2Value.containsKey(gffDetailGene)) {
					double score = Double.parseDouble(strings[1]);
					if (MapInfo.isMin2max()) {
						if (hashGene2Value.get(gffDetailGene) < score) {
							hashGene2Value.put(gffDetailGene, score);
						}
					}
					else {
						if (hashGene2Value.get(gffDetailGene) > score) {
							hashGene2Value.put(gffDetailGene, score);
						}
					}
				} else {
					hashGene2Value.put(gffDetailGene, Double.parseDouble(strings[1]));
				}
			}
			//didn't have score
			else {
				hashGene2Value.put(gffDetailGene, 0.0);
			}
		}
		ArrayList<MapInfo> lsMapInfoGene = getMapInfoFromGffGene(hashGene2Value, Structure);
		mapReads.getRegionLs(binNum, lsMapInfoGene, 0);
		if (lsGeneValue.get(0).length <= 1) {
			for (MapInfo mapInfo : lsMapInfoGene) {
				mapInfo.setScore(MathComput.mean(mapInfo.getDouble()));
			}
		}
		return lsMapInfoGene;
	}
	
	/**
	 * 根据前面设定upBp和downBp
	 * 给定一系列gffDetailGene，以及想要的部分，返回对应区域的LsMapInfo
	 * <b>注意里面没有填充reads的double[] value</b>
	 * @param setgffDetailGenes
	 * @param structure
	 * @return
	 */
	private ArrayList<MapInfo> getMapInfoFromGffGene(HashMap<GffDetailGene,Double> setgffDetailGenes, String structure)
	{
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (Entry<GffDetailGene, Double> gffDetailValue : setgffDetailGenes.entrySet()) {
			lsMapInfos.add(getStructureLoc(gffDetailValue.getKey(),gffDetailValue.getValue(), structure));
		}
		return lsMapInfos;
	}

	
	/**
	 * 给定peak的信息，chrID和起点终点，返回被peak覆盖到Tss的基因名和覆盖情况，用于做Tss图
	 * 自动去冗余基因
	 * @param lsPeakInfo mapInfo必须有 chrID 和 startLoc 和 endLoc 三项 
	 * @param structure GffDetailGene.TSS等
	 * @return
	 * 基因和权重的hash表
	 */
	private HashMap<GffDetailGene,Double> getPeakGeneStructure(ArrayList<? extends MapInfo> lsMapInfos, String structure) {
		//存储最后的基因和权重
		HashMap<GffDetailGene,Double> hashGffDetailGenes = new HashMap<GffDetailGene,Double>();
		for (MapInfo mapInfo : lsMapInfos) {
			Set<GffDetailGene> setGffDetailGene = getPeakStructureGene( mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), structure );
			for (GffDetailGene gffDetailGene : setGffDetailGene) {
				if (hashGffDetailGenes.containsKey(gffDetailGene)) {
					if (MapInfo.isMin2max()) {
						if (mapInfo.getScore() < hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
						}
					}
					else {
						if (mapInfo.getScore() > hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
						}
					}
				}
				else
					hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
			}
		}
		return hashGffDetailGenes;
	}
	/**
	 * 给定坐标区域，返回该peak所覆盖的GffDetailGene
	 * @param tsstesRange 覆盖度，tss或tes的范围
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure GffDetailGene.TSS等
	 * @return
	 */
	private Set<GffDetailGene> getPeakStructureGene(String chrID, int startLoc, int endLoc, String structure) {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(chrID, startLoc, endLoc);
		if (structure.equals(GffDetailGene.TSS)) {
			return gffCodGeneDU.getTSSGene(tss);
		}
		else if (structure.equals(GffDetailGene.TES)) {
			return gffCodGeneDU.getTESGene(tes);
		}
		else {
			logger.error("暂时没有除Tss和Tes之外的基因结构");
			return null;
		}
	}
	/**
	 * 前面设定upBp和downBp
	 * 给定gffDetailGene，以及想要的部分，返回对应区域的MapInfo
	 * <b>注意里面没有填充reads的double[] value</b>
	 * @param gffDetailGene
	 * @param value 该基因所对应的权重
	 * @param structure GffDetailGene.TSS等
	 * @return
	 */
	private MapInfo getStructureLoc(GffDetailGene gffDetailGene, Double value,String structure)
	{
		if (structure.equals(GffDetailGene.TSS)) {
			int tss = gffDetailGene.getLongestSplit().getTSSsite();
			MapInfo mapInfo = null;
			if (gffDetailGene.isCis5to3())
				mapInfo = new MapInfo(gffDetailGene.getParentName(), tss - Math.abs(upBp), tss + Math.abs(downBp), tss,0, gffDetailGene.getLongestSplit().getName());
			else 
				mapInfo = new MapInfo(gffDetailGene.getParentName(), tss - Math.abs(downBp), tss + Math.abs(upBp), tss, 0, gffDetailGene.getLongestSplit().getName());
			mapInfo.setCis5to3(gffDetailGene.isCis5to3());
			mapInfo.setScore(value);
			return mapInfo;
		}
		else if (structure.equals(GffDetailGene.TES)) {
			int tes = gffDetailGene.getLongestSplit().getTESsite();
			MapInfo mapInfo = null;
			if (gffDetailGene.isCis5to3())
				mapInfo = new MapInfo(gffDetailGene.getParentName(), tes - Math.abs(upBp), tes + Math.abs(downBp), tes, 0, gffDetailGene.getLongestSplit().getName());
			else 
				mapInfo = new MapInfo(gffDetailGene.getParentName(), tes - Math.abs(downBp), tes + Math.abs(upBp), tes, 0, gffDetailGene.getLongestSplit().getName());
			mapInfo.setCis5to3(gffDetailGene.isCis5to3());
			mapInfo.setScore(value);
			return mapInfo;
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
		
		String outFile = FileOperate.changeFileSuffix(FileOperate.deleteSep(chrFile), "_chrLen", "list");
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
