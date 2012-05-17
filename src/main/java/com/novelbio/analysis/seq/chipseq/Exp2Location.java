package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;

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
 * 研究表达差异和位点差异的
 * 也就是读取一个差异基因表，然后研究两组的甲基化差异情况
 * x轴：差异表达的ratio
 * y轴：差异表达的甲基化，用sicer-dif获得
 * @author zong0jie
 *
 */
public class Exp2Location {
	public static void main2(String[] args) {
		BedSeq bedSeq = new BedSeq("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/K4.KO.D0.sorted-1-removed.bed.gz");
		bedSeq.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		bedSeq = bedSeq.extend(240);
		bedSeq.sortBedFile();
		
		bedSeq = new BedSeq("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/K4.KO.D4.sorted-1-removed.bed.gz");
		bedSeq.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		bedSeq = bedSeq.extend(240);
		bedSeq.sortBedFile();
	}
	public static void main(String[] args) {
		String gffFile = NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ;
		String sicerFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/sicer-df-K4/K4_K4-WT4_sorted-1-removed-W200-G600-summary";
		int colChrID = 1;
		int colPeakStart = 2;
		int colPeakEnd = 3;
		int colScore = 11;
		String mapFile1 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/K4.WT.D4.sorted-1-removed_extend_sorted.bed";
		String mapFile2 = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/yulufile/K4.KO.D4.sorted-1-removed_extend_sorted.bed";
		String geneExpFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/expression/TKO-D4 vs FH-D4-XLYdif-ratio-noredudent.xls";
		int[] colGeneID = new int[]{1,7};
		
		String txtOutTss = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/difexpdifmethy/tssK4_Peak";
		String txtOutGeneBody = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/difexpdifmethy/genebodyK4_Peak";
		
		ArrayList<String[]> lsGeneInfo = ExcelTxtRead.readLsExcelTxt(geneExpFile, colGeneID, 2, -1);
		ArrayList<String[]> lsInput = new ArrayList<String[]>();
		for (String[] strings : lsGeneInfo) {
			if (Double.parseDouble(strings[1]) < 1) {
				continue;
			}
			lsInput.add(strings);
		}
		
		Exp2Location exp2Location = new Exp2Location();
		exp2Location.setReadPeak(true);
		exp2Location.setGffFile(gffFile);
//		exp2Location.setMapInfo(mapFile1, mapFile2);
		exp2Location.setSicerScore(sicerFile, colChrID, colPeakStart, colPeakEnd, colScore);
		exp2Location.readDifExpGene(lsInput, 2, txtOutTss, txtOutGeneBody);
	}
	/** 正负2K */
	int tssRegion = 2000;
	GffHashGene gffHashGene = new GffHashGene();
	MapReads mapReads1 = null;
	MapReads mapReads2 = null;
	/** 保存sicerdif的信息 */
	ListHashBin listHashBin = null;
	/** 默认读取sicer的结果, false则读取mapbed文件的结果 */
	boolean readPeak = true;
	/** 默认读取sicer的结果, false则读取mapbed文件的结果 */
	public void setReadPeak(boolean readPeak) {
		this.readPeak = readPeak;
	}
	/**
	 * 默认gff是ucsc的gff文件
	 * @param gffFile
	 */
	public void setGffFile( String gffFile) {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, gffFile);
	}
	public void setMapInfo(String mapFile1, String mapFile2) {
		mapReads1 = new MapReads(20, mapFile1);
		mapReads1.setChrLenFile("/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/ChromFa_chrLen.list");
		mapReads1.ReadMapFile();
		mapReads2 = new MapReads(20, mapFile2);
		mapReads2.setChrLenFile("/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/ChromFa_chrLen.list");
		mapReads2.ReadMapFile();
	}
	
	private void setSicerScore( String sicerFile, int colChrID, int colPeakStart, int colPeakEnd, int colScore) {
		listHashBin = new ListHashBin(true, colChrID, colPeakStart, colPeakEnd, 2);
		listHashBin.setColScore(colScore);
		listHashBin.ReadGffarray(sicerFile);
	}
	
	/**
	 * @param lsGene2Ratio 0: geneID 1：ratio
	 * @param rowStart
	 * @param txtOutTss
	 * @param txtOutGeneBody
	 */
	public void readDifExpGene(ArrayList<String[]> lsGene2Ratio, int rowStart, String txtOutTss, String txtOutGeneBody) {
		ArrayList<String[]> lsOutTss = new ArrayList<String[]>();
		ArrayList<String[]> lsOutGeneBody = new ArrayList<String[]>();
		for (String[] strings : lsGene2Ratio) {
			Double tssInfo = null,geneBodyInfo = null;
			if (readPeak) {
				tssInfo = getGeneTssPeak(strings[0]);
				geneBodyInfo = getGeneBodyPeak(strings[0]);
			}
			else {
				tssInfo = getGeneTssMap(strings[0]);
				geneBodyInfo = getGeneBodyMap(strings[0]);
			}
			if (tssInfo != null) {
				String[] strTss = new String[]{strings[0], strings[1], tssInfo + ""};
				lsOutTss.add(strTss);
			}
			if (geneBodyInfo != null) {
				String[] strGeneBody = new String[]{strings[0], strings[1], geneBodyInfo + ""};
				lsOutGeneBody.add(strGeneBody);
			}
		}
		
		TxtReadandWrite txtTss = new TxtReadandWrite(txtOutTss, true);
		TxtReadandWrite txtGeneBody = new TxtReadandWrite(txtOutGeneBody, true);
		
		txtTss.ExcelWrite(lsOutTss, "\t", 1, 1);
		txtGeneBody.ExcelWrite(lsOutGeneBody, "\t", 1, 1);
		
		txtTss.close();
		txtGeneBody.close();
		
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
			start = gffGeneIsoInfo.getTSSsite() + tssRegion;
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion;
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
		ListCodAbsDu<ListDetailBin, ListCodAbs<ListDetailBin>> lsDu = listHashBin.searchLocation(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getTSSsite() - tssRegion, gffGeneIsoInfo.getTSSsite() + tssRegion);
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
	private Double getGeneBodyMap(String geneID) {
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
		mapReads1.getRegion(mapInfo, 20, 0);
		double score1 = mapInfo.getMean();
		mapReads2.getRegion(mapInfo, 20, 0);
		double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
	}
	
	/**
	 * 给定基因，获得该基因tss附近sicer-dif的分数
	 * @param geneID
	 * @return 所有包含该基因tss区域甲基化均值
	 */
	private Double getGeneTssMap(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),gffGeneIsoInfo.getTSSsite() - tssRegion, gffGeneIsoInfo.getTSSsite() + tssRegion);
		mapReads1.getRegion(mapInfo, 20, 0);
		double score1 = mapInfo.getMean();
		mapReads2.getRegion(mapInfo, 20, 0);
		double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
	}
	
	
}
