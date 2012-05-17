package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
import com.novelbio.generalConf.NovelBioConst;

/**
 * 及其消耗内存
 * 研究位点差异和位点差异的
 * 譬如读取一个差异的miRNA和一个差异的甲基化，然后研究两组的相关性
 * x轴：差异表达的ratio
 * y轴：差异表达的甲基化，用sicer-dif获得
 * @author zong0jie
 *
 */
public class DifLoc2DifLoc {
	public static void main(String[] args) {
		String gffFile = NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ;
		String bedSrna1 = "";
		String bedSrna2 = "";
		String bedMethy1 = "";
		String bedMethy2 = "";
		String outFile = "";
		DifLoc2DifLoc exp2Location = new DifLoc2DifLoc();
		exp2Location.setGffFile(gffFile);
		exp2Location.addMapInfo("sRNA", bedSrna1, bedSrna2);
		exp2Location.addMapInfo("methy", bedMethy1, bedMethy2);
		exp2Location.readDifExpGene(typeTss, "sRNA", "methy", outFile);
	}
	/** 正负2K */
	int tssRegion = 2000;
	GffHashGene gffHashGene = new GffHashGene();
	String chrLenFile = "";
	int binNum = 20;
	LinkedHashMap<String, ArrayList<MapReads>> hashMapReads = new LinkedHashMap<String, ArrayList<MapReads>>();
	static int typeTss = 2;
	static int typeGeneAll = 4;
	static int typeGeneBody = 8;
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
	public void setGffFile( String gffFile) {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, gffFile);
	}
	/**
	 * 按照顺序加入，两组
	 * @param prefix
	 * @param mapFile1
	 * @param mapFile2
	 */
	public void addMapInfo(String prefix, String mapFile1, String mapFile2) {
		MapReads mapReads1 = new MapReads(chrLenFile, binNum, mapFile1);
		MapReads mapReads2 = new MapReads(chrLenFile, binNum, mapFile2);
		ArrayList<MapReads> lsMapReads = new ArrayList<MapReads>();
		lsMapReads.add(mapReads1);
		lsMapReads.add(mapReads2);
		hashMapReads.put(prefix, lsMapReads);
	}
	/**
	 * 全体基因作分析
	 * @param type 类型，是tss还是geneEnd
	 * @param prefix1 第一个类型，如sRNA
	 * @param prefix2 第二个类型，如methylation
	 * @param lsGeneID 
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
				tssInfo1 = getGeneAll(prefix1, strings);
				tssInfo2 = getGeneAll(prefix2, strings);
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
			start = gffGeneIsoInfo.getTSSsite() + tssRegion;
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion;
		}
		end = gffGeneIsoInfo.getTESsite();
		mapInfo.setStartEndLoc(start, end);
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		lsMapReads.get(0).getRegion(mapInfo, 20, 0);
		double score1 = mapInfo.getMean();
		lsMapReads.get(1).getRegion(mapInfo, 20, 0);
		double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
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
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),gffGeneIsoInfo.getTSSsite() - tssRegion, gffGeneIsoInfo.getTSSsite() + tssRegion);
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		lsMapReads.get(0).getRegion(mapInfo, 20, 0);
		double score1 = mapInfo.getMean();
		lsMapReads.get(1).getRegion(mapInfo, 20, 0);
		double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
	}
	
	/**
	 * 给定基因，获得该基因全部区域的分数
	 * @param geneID
	 * @return 所有包含该基因tss区域甲基化均值
	 */
	private Double getGeneAll(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),gffGeneIsoInfo.getTSSsite(), gffGeneIsoInfo.getTESsite());
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		lsMapReads.get(0).getRegion(mapInfo, 20, 0);
		double score1 = mapInfo.getMean();
		lsMapReads.get(1).getRegion(mapInfo, 20, 0);
		double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
	}
}
