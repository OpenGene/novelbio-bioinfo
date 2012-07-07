package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.impl.ExplicitGroupImpl;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 及其消耗内存
 * 研究位点差异和位点差异的
 * 譬如读取一个差异的miRNA和一个差异的甲基化，然后研究两组的相关性
 * 也可读基因表达与甲基化表达
 * x轴：差异表达的ratio
 * y轴：差异表达的甲基化，用sicer-dif获得
 * @author zong0jie
 *
 */
public class DifLoc2DifLoc {
	Logger logger = Logger.getLogger(DifLoc2DifLoc.class);
	public static void main2(String[] args) {
		
		String bedSrna1 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_3N/bgi_3N.bed";
		String bedSrna2 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_2N/bgi_2N.bed";
		
		
		BedSeq bedSeq = new BedSeq(bedSrna1);
		bedSeq = bedSeq.extend(240);
		bedSeq.sortBedFile();
		
//		 bedSeq = new BedSeq(bedSrna2);
//		 bedSeq = bedSeq.extend(240);
//		 bedSeq.sortBedFile();
	}
	public static void main(String[] args) {
		String gffFile = NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ;
		String bedSrna1 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_N/bgi_N_extend_sorted.bed";
		String bedSrna2 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_3N/bgi_3N_extend_sorted.bed";
		String bedMethy1 = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/Nextend_sort.bed";
		String bedMethy2 = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/3Nextend_sort.bed";
		String outFile = "/media/winE/NBC/Project/Project_ZHY_Lab/sRNAvsMethylation/N-3N.txt";
		DifLoc2DifLoc exp2Location = new DifLoc2DifLoc();
//		exp2Location.setGffFile(gffFile);
		exp2Location.addMapInfo("sRNA", bedSrna1, bedSrna2);
		exp2Location.addMapInfo("methy", bedMethy1, bedMethy2);
		exp2Location.readDifExpGene(typeTss, "sRNA", "methy", FileOperate.changeFileSuffix(outFile, "_tss", null));

		exp2Location.readDifExpGene(typeGeneAll, "sRNA", "methy", FileOperate.changeFileSuffix(outFile, "_geneAll", null));

		exp2Location.readDifExpGene(typeGeneBody, "sRNA", "methy", FileOperate.changeFileSuffix(outFile, "_geneBody", null));
	}
	/** 正负2K */
	int[] tssRegion = new int[]{-2000,2000};
	GffHashGene gffHashGene = new GffHashGene();
	String chrLenFile = "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/ChromFa_chrLen.list";
	int binNum = 20;
	LinkedHashMap<String, ArrayList<MapReads>> hashMapReads = new LinkedHashMap<String, ArrayList<MapReads>>();
	public static int typeTss = 2;
	public static int typeGeneAll = 4;
	public static int typeGeneBody = 8;
	/** 保存sicerdif的信息 */
	ListHashBin listHashBin = null;
	/** 默认读取bed文件的结果, false则读取peak文件的结果 */
	boolean readPeak = false;
	/** 默认读取sicer的结果, false则读取mapbed文件的结果 */
	public void setReadPeak(boolean readPeak) {
		this.readPeak = readPeak;
	}
	public void setTssRegion(int[] tssRegion) {
		this.tssRegion = tssRegion;
	}
	/**
	 * 设定染色体长度文件
	 * @param chrLenFile
	 */
	public void setChrLenFile(String chrLenFile) {
		this.chrLenFile = chrLenFile;
	}
	/**
	 * 默认gff是ucsc的gff文件
	 * @param gffFile
	 */
	public void setGffFile(String gffType, String gffFile) {
		gffHashGene = new GffHashGene(gffType, gffFile);
	}
	/**
	 * 按照顺序加入，两组
	 * 最后是mapFile1除以mapFile2
	 * @param prefix
	 * @param mapFile1
	 * @param mapFile2
	 */
	public void addMapInfo(String prefix, String mapFile1, String mapFile2) {
		ArrayList<MapReads> lsMapReads = new ArrayList<MapReads>();
		MapReads mapReads1 = null;
		if (FileOperate.isFileExist(mapFile1)) {
			mapReads1 = new MapReads(chrLenFile, binNum, mapFile1);
			mapReads1.ReadMapFile();
			lsMapReads.add(mapReads1);
		}
		
		MapReads mapReads2 = null;
		if (FileOperate.isFileExist(mapFile2)) {
			mapReads2 = new MapReads(chrLenFile, binNum, mapFile2);
			mapReads2.ReadMapFile();
			lsMapReads.add(mapReads2);
		}
		hashMapReads.put(prefix, lsMapReads);
	}
	/**
	 * 读取sicer文件的score分数，如果不是用sicer的score去做分析，那么就不用该方法
	 * @param sicerFile
	 * @param colChrID
	 * @param colPeakStart
	 * @param colPeakEnd
	 * @param colScore 比值或表达等信息
	 */
	public void setSicerScore( String sicerFile, int colChrID, int colPeakStart, int colPeakEnd, int colScore) {
		listHashBin = new ListHashBin(true, colChrID, colPeakStart, colPeakEnd, 2);
		listHashBin.setColScore(colScore);
		listHashBin.ReadGffarray(sicerFile);
	}
	/**
	 * 
	 * @param excelTxtFile
	 * @param colGeneID geneID列
	 * @param colExp score列
	 * @param rowStart
	 * @param txtOutTss
	 * @param txtOutGeneBody
	 * @param mapPrix
	 */
	public void readExpGeneTxt(int type, String excelTxtFile, int colGeneID, int colExp, int rowStart, String txtOut, String mapPrix) {
		ArrayList<String[]> lsGene2Ratio = ExcelTxtRead.readLsExcelTxt(excelTxtFile, new int[]{colGeneID, colExp}, 1, -1);
		readDifExpGene(type, lsGene2Ratio, rowStart, txtOut, mapPrix);
	}
	
	/**
	 * @param lsGene2Ratio 0: geneID 1：ratio/exp
	 * @param rowStart
	 * @param txtOutTss
	 * @param txtOutGeneBody
	 */
	public void readDifExpGene(int type, ArrayList<String[]> lsGene2Ratio, int rowStart, String txtOut, String mapPrix) {
		ArrayList<String[]> lsOut = new ArrayList<String[]>();
		lsOut.add(new String[]{"geneID", mapPrix, "methylation"});
		for (String[] strings : lsGene2Ratio) {
			Double Info = null;
			if (readPeak) {
				if (type == typeTss) {
					Info = getGeneTssPeak(strings[0]);
				}
				else if (type == typeGeneAll) {
					Info = getGeneAllPeak(strings[0]);
				}
				else if (type == typeGeneBody) {
					Info = getGeneBodyPeak(strings[0]);
				}
			}
			else {
				if (type == typeTss) {
					Info = getGeneTssMap(mapPrix, strings[0]);
				} else if (type == typeGeneAll) {
					Info = getGeneAllMap(mapPrix, strings[0]);
				} else if (type == typeGeneBody) {
					Info = getGeneBodyMap(mapPrix, strings[0]);
				}
			}
			if (Info != null) {
				String[] strTss = new String[]{strings[0], strings[1], Info + ""};
				lsOut.add(strTss);
			}
		}
		TxtReadandWrite txt = new TxtReadandWrite(txtOut, true);
		txt.ExcelWrite(lsOut, "\t", 1, 1);
		txt.close();
	}
	/**
	 * 全体基因作分析
	 * @param type 类型，是tss还是geneEnd
	 * @param prefix1 第一个类型，如sRNA
	 * @param prefix2 第二个类型，如methylation
	 * @param lsAccID 
	 * @param txtOutInfo
	 */
	public void readDifExpGene(int type, String prefix1, String prefix2,  String txtOutInfo) {
		ArrayList<String> lsGeneID = gffHashGene.getLOCIDList();
		readDifExpGene(type, prefix1, prefix2, lsGeneID, txtOutInfo);
	}
	/**
	 * @param type 类型，是tss还是geneEnd
	 * @param prefix1 第一个类型，如sRNA
	 * @param prefix2 第二个类型，如methylation
	 * @param lsGeneID 
	 * @param txtOutInfo
	 */
	public void readDifExpGene(int type, String prefix1, String prefix2, ArrayList<String> lsGeneID, String txtOutInfo) {
		ArrayList<String[]> lsOutGeneInfo = new ArrayList<String[]>();
		for (String strings : lsGeneID) {
			Double tssInfo1 = null,tssInfo2 = null;
			if (type == typeTss) {
				tssInfo1 = getGeneTssMap(prefix1, strings);
				tssInfo2 = getGeneTssMap(prefix2, strings);
			}
			else if (type == typeGeneBody) {
				tssInfo1 = getGeneBodyMap(prefix1, strings);
				tssInfo2 = getGeneBodyMap(prefix2, strings);
			}
			else if (type == typeGeneAll) {
				tssInfo1 = getGeneAllMap(prefix1, strings);
				tssInfo2 = getGeneAllMap(prefix2, strings);
			}
			if (tssInfo1 != null && tssInfo2 != null) {
				String[] strTss = new String[]{strings, tssInfo1 + "", tssInfo2 + ""};
				lsOutGeneInfo.add(strTss);
			}
		}
		TxtReadandWrite txtInfo = new TxtReadandWrite(txtOutInfo, true);
		txtInfo.ExcelWrite(lsOutGeneInfo, "\t", 1, 1);
		txtInfo.close();
	}
	
	/**
	 * 给定基因，获得该基因全部区域的分数
	 * @param geneID
	 * @return 所有包含该基因tss区域甲基化均值
	 */
	private Double getGeneAllPeak(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int startTmp = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			startTmp = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
		}
		else {
			startTmp = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
		}
		int start = Math.min(startTmp, gffGeneIsoInfo.getTESsite());
		int end = Math.max(startTmp, gffGeneIsoInfo.getTESsite());

		ListCodAbsDu<ListDetailBin, ListCodAbs<ListDetailBin>> lsDu = listHashBin.searchLocation(gffGeneIsoInfo.getChrID(), start, end);
		ArrayList<ListDetailBin> lsBin = lsDu.getAllGffDetail();
		if (lsBin.size() == 0) {
			return 1.0;
		}
		Double score = 0.0;
		for (ListDetailBin listDetailBin : lsBin) {
			score = score + listDetailBin.getScore();
		}
		return score/lsBin.size();
	}
	
	
	/**
	 * 给定基因，获得该基因tss附近sicer-dif的分数
	 * @param geneID
	 * @return 所有包含该基因tss区域甲基化均值
	 */
	private Double getGeneBodyPeak(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[1];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[1];
		}
		end = gffGeneIsoInfo.getTESsite();
		
		ListCodAbsDu<ListDetailBin, ListCodAbs<ListDetailBin>> lsDu = listHashBin.searchLocation(gffGeneIsoInfo.getChrID(), Math.min(start, end), Math.max(start, end));
		ArrayList<ListDetailBin> lsBin = lsDu.getAllGffDetail();
		if (lsBin.size() == 0) {
			return 1.0;
		}
		double score = 0;
		for (ListDetailBin listDetailBin : lsBin) {
			score = score + listDetailBin.getScore();
		}
		return score/lsBin.size();
	}
	/**
	 * 给定基因，获得该基因tss附近sicer-dif的分数
	 * @param geneID
	 * @return 所有包含该基因tss区域甲基化均值
	 */
	private Double getGeneTssPeak(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
			end = gffGeneIsoInfo.getTSSsite() + tssRegion[1];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
			end = gffGeneIsoInfo.getTSSsite() - tssRegion[1];
		}
		ListCodAbsDu<ListDetailBin, ListCodAbs<ListDetailBin>> lsDu = listHashBin.searchLocation(gffGeneIsoInfo.getChrID(), start, end);
		ArrayList<ListDetailBin> lsBin = lsDu.getAllGffDetail();
		if (lsBin.size() == 0) {
			return 1.0;
		}
		double score = 0;
		for (ListDetailBin listDetailBin : lsBin) {
			score = score + listDetailBin.getScore();
		}
		return score/lsBin.size();
	}
	/**
	 * 给定基因，获得该基因tss附近sicer-dif的分数
	 * @param geneID
	 * @return 所有包含该基因tss区域甲基化均值
	 */
	private Double getGeneBodyMap(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID());
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
		}
		end = gffGeneIsoInfo.getTESsite();
		mapInfo.setStartEndLoc(start, end);
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		return getInfo(mapInfo, lsMapReads);
	}
	
	/**
	 * 给定基因，获得该基因tss附近sicer-dif的分数
	 * @param geneID
	 * @return 所有包含该基因tss区域甲基化均值
	 */
	private Double getGeneTssMap(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
			end = gffGeneIsoInfo.getTSSsite() + tssRegion[1];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
			end = gffGeneIsoInfo.getTSSsite() - tssRegion[1];
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),start, end);
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		return getInfo(mapInfo, lsMapReads);
	}
	
	/**
	 * 给定基因，获得该基因全部区域的分数
	 * @param geneID
	 * @return 所有包含该基因tss区域甲基化均值
	 */
	private Double getGeneAllMap(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		int start = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion[0];
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion[0];
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),start, gffGeneIsoInfo.getTESsite());
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		return getInfo(mapInfo, lsMapReads);
	}
	
	private Double getInfo(MapInfo mapInfo, ArrayList<MapReads> lsMapReads) {
		lsMapReads.get(0).getRegion(mapInfo, 20, 0);
		if (mapInfo.getDouble() == null) {
			logger.error("发现了未知ID：" + mapInfo.getRefID() + " " + mapInfo.getStart() + " " + mapInfo.getEnd());
			return null;
		}
		Double score1 = mapInfo.getMean();
		if (score1 == null) {
			return null;
		}
		if (lsMapReads.size() == 1) {
			return score1;
		}
		lsMapReads.get(1).getRegion(mapInfo, 20, 0);
		Double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
	}
	
}
