package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.species.Species;
/**
 * GffHashGene和SeqHash都是static，也就是一次只能对一个物种进行分析
 * MapReads不是static，也就是可以同时处理多个mapping文件
 * @author zong0jie
 *
 */
public class GffChrAbs {
	private static final Logger logger = Logger.getLogger(GffChrAbs.class);
	private int distanceMapInfo = 3000;
	GffHashGene gffHashGene = null;
	SeqHash seqHash = null;
	MapReads mapReads = null;
	Species species;
	
	int[] tss = new int[]{-1500, 1500};
	int[] tes = null;
	boolean genebody = false;
	boolean UTR5 = false;
	boolean UTR3 = false;
	boolean exonFilter = false;
	boolean intronFilter = false;
	boolean filtertss = false;
	boolean filtertes = false;

	boolean HanYanFstrand = false;
	
	/** 标准化方法，默认为不标准化 */
	int mapNormType = MapReads.NORMALIZATION_NO;
	/** 主要用于画图 */
	int upBp = 5000;//tss和tes以及其他位点的上游长度，默认5000
	int downBp = 5000;//tss和tes以及其他位点的下游长度，默认5000
	int tssUpBp = 3000;
	int tssDownBp = 2000;
	int geneEnd3UTR = 100;
	
	String chrRegx = null;
	String equationsFile = "";

	public GffChrAbs() {}
	public GffChrAbs(Species species) {
		setSpecies(species);
	}	
	public GffChrAbs(int taxID) {
		setTaxID(taxID);
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
	public GffChrAbs(String gffType, String gffFile, String chrFile, String regx, String readsBed, int binNum) {
		setGffFile(0, gffType, gffFile);
		setChrFile(chrFile, regx);
		this.setMapReads(readsBed, binNum);
	}
	/**
	 * @param gffType
	 * @param gffFile
	 * @param chrFile
	 * @param readsBed
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 */
	public GffChrAbs(String gffType, String gffFile, String chrFile,String readsBed ,int binNum) {
		setGffFile(0, gffType, gffFile);
		setChrFile(chrFile, null);
		this.setMapReads(readsBed, binNum);
	}
	public void setTaxID(int taxID) {
		this.species = new Species(taxID);
		setGffFile(species.getTaxID(), species.getGffFile()[0], species.getGffFile()[1]);
		setChrFile(species.getChrRegxAndPath()[1], species.getChrRegxAndPath()[0]);
	}
	public void setSpecies(Species species) {
		if (this.species != null && this.species.equals(species)) {
			return;
		}
		this.species = species;
		setGffFile(species.getTaxID(), species.getGffFile()[0], species.getGffFile()[1]);
		setChrFile(species.getChrRegxAndPath()[1], species.getChrRegxAndPath()[0]);
	}
	public void setGffHash(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	public void set(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	/** 如果没有设定species，就返回一个全新的species，并且其taxID == 0 */
	public Species getSpecies() {
		if (species == null) {
			return new Species();
		}
		return species;
	}
	public int getTaxID() {
		if (species == null) {
			return 0;
		}
		return species.getTaxID();
	}
	public void setHanYanFstrand(boolean hanYanFstrand) {
		HanYanFstrand = hanYanFstrand;
	}
	public GffHashGene getGffHashGene() {
		return gffHashGene;
	}
	public SeqHash getSeqHash() {
		return seqHash;
	}
	public MapReads getMapReads() {
		return mapReads;
	}

	/**
	 * 对于Tss和GeneEnd的定义
	 * @param filterTss
	 * @param filterGenEnd
	 */
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

	public void setFilterGeneBody(boolean filterGeneBody, boolean filterExon, boolean filterIntron) {
		this.genebody = filterGeneBody;
		this.exonFilter = filterExon;
		this.intronFilter = filterIntron;
	}
	public void setFilterUTR(boolean filter5UTR, boolean filter3UTR) {
		this.UTR5 = filter5UTR;
		this.UTR3 = filter3UTR;
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
		this.upBp = Math.abs(upBp);
		this.downBp = downBp;
	}
	public void setGffFile(int taxID, String gffType, String gffFile) {
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
			gffHashGene.setTaxID(taxID);
		}
	}
	/**
	 chrFile 序列文件或序列文件夹
    regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，
    则用该正则表达式提取含有该文件名的文件 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param chrFile
	 * @param regx
	 */
	public void setChrFile(String chrFile, String regx) {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, chrRegx);
		}
	}
	/** 输入已经配置好的mapReads对象，但是标准化和校正都由GffChrAbs提供 */
	public void setMapReads(MapReads mapReads) {
		this.mapReads = mapReads;
		setMapCorrect();
		mapReads.setNormalType(mapNormType);
		mapReads.setMapChrID2Len(species.getMapChromInfo());
	}
	/**
	 * @param readsFile mapping的结果文件，必须排过序，一般为bed格式
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			if (HanYanFstrand) {
				mapReads = new MapReadsHanyanChrom();
			} else {
				mapReads = new MapReads();
			}
			mapReads.setBedSeq(readsFile);
			mapReads.setInvNum(binNum);
			mapReads.setMapChrID2Len(species.getMapChromInfo());
			mapReads.setNormalType(mapNormType);

			setMapCorrect();
		}
	}
	/**
	 * 给定一个文本来修正  没有文件则直接返回
	 * @param correctFile x第一列，y第二；列，从第一行开始读取
	 */
	public void setMapCorrect(String correctFile) {
		this.equationsFile = correctFile;
		setMapCorrect();
	}
	/** 设定用qpcr等参数校正mapping结果 */
	protected void setMapCorrect() {
		Equations equations = new Equations();
		equations.setXYFile(equationsFile);
		if (mapReads != null) {
			mapReads.setFormulatToCorrectReads(equations);
		}
	}
	public void loadMapReads() {
		try { mapReads.running(); }
		catch (Exception e) {
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
	 * @param colScore 打分，也就是权重，没有该列的话，就设置为 <= 0
	 * @param rowStart
	 */
	public ArrayList<MapInfo> readFileRegionMapInfo(String txtExcel, int colChrID, int colStartLoc, int colEndLoc, int colScore,int rowStart) {
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
	public ArrayList<MapInfo> readFileSiteMapInfo(String txtExcel,int region ,int colChrID, int colSummit, int colScore, int rowStart) {
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
	public ArrayList<MapInfo> getPeakCoveredGeneMapInfo(ArrayList<? extends MapInfo> lsMapInfos, GeneStructure structure, int binNum) {
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
	public ArrayList<MapInfo> readFileGeneMapInfo(String txtExcel,int colGeneID, int colScore, int rowStart, GeneStructure Structure, int binNum)
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
	public ArrayList<MapInfo> readGeneMapInfoAll(GeneStructure Structure, int binNum) {
		ArrayList<String> lsGeneID = gffHashGene.getLsNameAll();
		ArrayList<String[]> lstmp = new ArrayList<String[]>();
		for (String string : lsGeneID) {
			lstmp.add(new String[]{string.split(SepSign.SEP_ID)[0]});
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
	public ArrayList<MapInfo> getLsGeneMapInfo(ArrayList<String[]> lsGeneValue, GeneStructure Structure, int binNum) {
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
	private ArrayList<MapInfo> getMapInfoFromGffGene(HashMap<GffDetailGene,Double> setgffDetailGenes, GeneStructure structure) {
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
	private HashMap<GffDetailGene,Double> getPeakGeneStructure(ArrayList<? extends MapInfo> lsMapInfos, GeneStructure structure) {
		//存储最后的基因和权重
		HashMap<GffDetailGene,Double> hashGffDetailGenes = new HashMap<GffDetailGene,Double>();
		for (MapInfo mapInfo : lsMapInfos) {
			Set<GffDetailGene> setGffDetailGene = getPeakStructureGene( mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), structure );
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
	private Set<GffDetailGene> getPeakStructureGene(String chrID, int startLoc, int endLoc, GeneStructure structure) {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(chrID, startLoc, endLoc);
		if (gffCodGeneDU == null) {
			return new HashSet<GffDetailGene>();
		}
		if (structure.equals(GeneStructure.TSS)) {
			return gffCodGeneDU.getTSSGene(tss);
		}
		else if (structure.equals(GeneStructure.TES)) {
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
	private MapInfo getStructureLoc(GffDetailGene gffDetailGene, Double value,GeneStructure structure)
	{
		if (structure.equals(GeneStructure.TSS)) {
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
		else if (structure.equals(GeneStructure.TES)) {
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

	
}
