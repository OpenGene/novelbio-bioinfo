package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;
import com.novelbio.generalConf.TitleFormatNBC;

public class CtrlMiRNApredict implements IntCmdSoft {
	GffChrAbs gffChrAbs;
	Species species;
	String outPath;
	String outPathSample;
	String outPathTmp;
	String samStatisticsPath;
	
	Map<String, ? extends AlignSeq> mapPrefix2SamFile;
	Map<String, String> mapPrefix2UnmapFq = new HashMap<>();
	
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
//	SoftWareInfo softWareInfo = new SoftWareInfo();
	MiRNACount miRNACount = new MiRNACount();
	
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.miRNAName);
	
	/** 新miRNA的注释，比对到哪些物种上去 */
	List<Species> lsBlastTo = new ArrayList<>();
	
	boolean isUseOldResult = true;
	String novelMiRNAmrd;
	List<String> lsCmd = new ArrayList<>();
	int threadNum = 8;
	int lenMin = 17, lenMax = 32;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (this.gffChrAbs != null && gffChrAbs != null && this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies())) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
	}
	public void setSpecies(Species species) {
		this.species = species;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	public void setExpMir(GeneExpTable expMirPre, GeneExpTable expMirMature) {
		this.expMirPre = expMirPre;
		this.expMirMature = expMirMature;
	}
	public void setNovelMiRNAmrd(String novelMiRNAmrd) {
		this.novelMiRNAmrd = novelMiRNAmrd;
	}
	/** 新miRNA的注释，比对到哪些物种上去 */
	public void setLsSpeciesBlastTo(List<Species> lsBlastTo) {
		this.lsBlastTo = lsBlastTo;
	}
	/**
	 *  输出文件夹
	 * @param outPath 输入汇总结果路径
	 * @param outPathSample 输出样本文件夹
	 * @param outPathTmp 临时文件夹
	 * @param samStatisticsPath 统计报告文件夹
	 */
	public void setOutPath(String outPath, String outPathSample, String outPathTmp, String samStatisticsPath) {
		this.outPath = outPath;
		this.outPathSample = outPathSample;
		this.outPathTmp = outPathTmp;
		this.samStatisticsPath = samStatisticsPath;
	}
	public void setMapPrefix2GenomeSamFile(Map<String, ? extends AlignSeq> mapPrefix2SamFile) {
		this.mapPrefix2SamFile = mapPrefix2SamFile;
	}

	public void runMiRNApredict(SamMapRate samMapMiRNARate) {
		lsCmd.clear();
		if (gffChrAbs == null) {
			gffChrAbs = new GffChrAbs(species);
		}
		if (mapPrefix2SamFile.size() <= 0) {
			return;
		}
		String novelMiRNAPathDeep = outPath + "miRNApredictDeep/";
		FileOperate.createFolders(novelMiRNAPathDeep);
		
		novelMiRNADeep.setSeqInput(mapPrefix2SamFile.values());
		novelMiRNADeep.setSpeciesChrIndex(species);
		novelMiRNADeep.setGffChrAbs(gffChrAbs);
		novelMiRNADeep.setMiRNASeq(species.getMiRNAmatureFile(), null, species.getMiRNAhairpinFile());
		novelMiRNADeep.setSpeciesName(species.getCommonName());
		novelMiRNADeep.setOutPath(novelMiRNAPathDeep);
		novelMiRNADeep.setNovelMiRNAdeepMrdFile(novelMiRNAmrd);
		novelMiRNADeep.predict();
		lsCmd.addAll(novelMiRNADeep.getCmdExeStr());
		setMiRNACount_And_Anno();
		calculateExp(samMapMiRNARate);
	}
	
	/**
	 * @param samMapMiRNARate null表示不统计
	 */
	protected void calculateExp(SamMapRate samMapMiRNARate) {
		samMapMiRNARate.setNovelMiRNAInfo();
		miRNACount.setExpTable(expMirPre, expMirMature);
		 for (String prefix : mapPrefix2SamFile.keySet()) {
			 getMirPredictCount(prefix, mapPrefix2SamFile.get(prefix), samMapMiRNARate);
		}
	}
	
	private void getMirPredictCount(String prefix, AlignSeq alignSeq, SamMapRate samMapMiRNARate) {
		FastQ fastQ = null;
		if (isUseOldResult && FileOperate.isFileExistAndBigThanSize(FileOperate.changeFileSuffix(alignSeq.getFileName(), "", "fastq.gz"), 0)) {
			fastQ = new FastQ(FileOperate.changeFileSuffix(alignSeq.getFileName(), "", "fastq.gz"));
		} else {
			fastQ = alignSeq.getFastQ();
		}
		alignSeq.close();
		String novelMiRNAsam = outPathTmp + prefix + "novelMiRNAmapping.sam";
		String unmappedFq = outPathTmp + prefix + "novelMiRNAunmapped.fq.gz";
		SamFileStatistics samFileNovelMiRNA = new SamFileStatistics(prefix);
		String mirHairp = novelMiRNADeep.getNovelMiRNAhairpin();
		String mirHairpNew = outPathTmp + FileOperate.getFileName(mirHairp);
		FileOperate.copyFile(mirHairp, mirHairpNew, false);
		novelMiRNAsam = MiRNAmapPipline.mappingDNA(lsCmd, isUseOldResult, samFileNovelMiRNA, threadNum, fastQ.getReadFileName(), 
				mirHairpNew, novelMiRNAsam, unmappedFq);
		if (samFileNovelMiRNA.getReadsNum(MappingReadsType.Mapped) > 0) {
			SamFileStatistics.saveExcel(samStatisticsPath + FileOperate.getFileName(novelMiRNAsam), samFileNovelMiRNA);
		}
		mapPrefix2UnmapFq.put(prefix, unmappedFq);
		samMapMiRNARate.setCurrentCondition(prefix);
		miRNACount.setSamMapRate(samMapMiRNARate);
		miRNACount.initial();
		AlignSeqReading alignSeqReading = getAlignSeqReading(new SamFile(novelMiRNAsam), miRNACount);
		alignSeqReading.running();
		
		expMirMature.setCurrentCondition(prefix);
		expMirMature.addAllReads(miRNACount.getCountMatureAll());
		expMirMature.addGeneExp(miRNACount.getMapMirMature2Value());
		
		expMirPre.setCurrentCondition(prefix);
		expMirPre.addAllReads(miRNACount.getCountPreAll());
		expMirPre.addGeneExp(miRNACount.getMapMiRNApre2Value());
		
		expMirPre.writeFile(false, outPathSample + prefix + FileOperate.getSepPath() + prefix + ".NovelMirPre.Counts.txt", EnumExpression.Counts);
		expMirMature.writeFile(false, outPathSample + prefix + FileOperate.getSepPath() + prefix + ".NovelMirMature.Counts.txt", EnumExpression.Counts);
	}
	
	private AlignSeqReading getAlignSeqReading(AlignSeq alignSeq, AlignmentRecorder alignmentRecorder) {
		AlignSeqReading alignSeqReading = new AlignSeqReading();
		alignSeqReading.addSeq(alignSeq);
		alignSeqReading.setLenMin(lenMin);
		alignSeqReading.setLenMax(lenMax);
		alignSeqReading.addAlignmentRecorder(alignmentRecorder);
		return alignSeqReading;
	}
	
	public Map<String, String> getMapPrefix2UnmapFq() {
		return mapPrefix2UnmapFq;
	}
	
	public GeneExpTable getExpMirPre() {
		return expMirPre;
	}
	public GeneExpTable getExpMirMature() {
		return expMirMature;
	}
	
	public void writeToFile() {
		String outPathNovel = outPath + "novelMiRNA/";
		expMirPre.writeFile(true, outPathNovel + "NovelMirPreAll.Counts.txt", EnumExpression.Counts);
		expMirMature.writeFile(true, outPathNovel + "NovelMirMatureAll.Counts.txt", EnumExpression.Counts);
		expMirPre.writeFile(true, outPathNovel + "NovelMirPreAll.UQTPM.txt", EnumExpression.UQ);
		expMirMature.writeFile(true, outPathNovel + "NovelMirMatureAll.UQTPM.txt", EnumExpression.UQ);
	}
	
	private void setMiRNACount_And_Anno() {
		Map<String, String> mapID2Blast = null;
		MiRNAnovelAnnotaion miRNAnovelAnnotaion = null;
		if (lsBlastTo != null && !lsBlastTo.isEmpty()) {
			miRNAnovelAnnotaion = new MiRNAnovelAnnotaion();
			miRNAnovelAnnotaion.setMiRNAthis(novelMiRNADeep.getNovelMiRNAmature());
			String blastPath = outPathTmp + "blastAnnoMiRNA";
			miRNAnovelAnnotaion.setLsMiRNAblastTo(lsBlastTo, blastPath);
			miRNAnovelAnnotaion.setIsUseOldResult(isUseOldResult);
			miRNAnovelAnnotaion.annotation();
			mapID2Blast = miRNAnovelAnnotaion.getMapID2Blast();
			lsCmd.addAll(miRNAnovelAnnotaion.getCmdExeStr());
		}

		ListMiRNAdeep listMiRNAdeep = new ListMiRNAdeep();
		listMiRNAdeep.setBlastMap(mapID2Blast);
//		listMiRNAdeep.setSetMiRNApredict(setMiRNAName);
		listMiRNAdeep.ReadGffarray(novelMiRNADeep.getNovelMiRNAdeepMrdFile());
		
		miRNACount.setListMiRNALocation(listMiRNAdeep);
		if (lsBlastTo != null && !lsBlastTo.isEmpty()) {
			miRNACount.setMiRNAfile(novelMiRNADeep.getNovelMiRNAhairpin(), miRNAnovelAnnotaion.getMiRNAmatureCope());		
		} else {
			miRNACount.setMiRNAfile(novelMiRNADeep.getNovelMiRNAhairpin(), novelMiRNADeep.getNovelMiRNAmature());		
		}
	}
	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
	
}
