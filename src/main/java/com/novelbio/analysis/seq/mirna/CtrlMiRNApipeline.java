package com.novelbio.analysis.seq.mirna;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;
import com.novelbio.generalConf.TitleFormatNBC;

public class CtrlMiRNApipeline {
	private static final Logger logger = Logger.getLogger(CtrlMiRNApipeline.class);
	Species species = new Species(9940);
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.miRNAName);
	GeneExpTable expMirStatistics = new GeneExpTable(TitleFormatNBC.mirMappingType);
	
	CtrlMiRNAfastq ctrlMiRNAfastq;
	CtrlMiRNApredict ctrlMiRNApredict = new CtrlMiRNApredict();
	boolean mapMirna = true;
	boolean predictMirna = true;
	
	List<Species> lsSpeciesBlastTo;
	String outPath;
	
	Map<String, AlignSeq> mapPrefix2AlignFile;
	Map<String, String> mapPrefix2Fastq;

	/** 输入数据 */
	public void setLsfastqFile2Prefix(Map<String, String> mapPrefix2Fastq) {
		this.mapPrefix2Fastq = mapPrefix2Fastq;
	}
	/** 设定比对到的物种，不设定就不做这步分析 */
	public void setLsSpeciesBlastTo(List<Species> lsSpeciesBlastTo) {
		this.lsSpeciesBlastTo = lsSpeciesBlastTo;
	}
	/** 设定输出路径 */
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	public void run() {
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		ctrlMiRNAfastq = new CtrlMiRNAfastq();
		ctrlMiRNAfastq.setMiRNAexp(expMirPre, expMirMature);
		if (mapMirna) {
			runMapping(gffChrAbs, species, mapPrefix2Fastq);
			mapPrefix2AlignFile = ctrlMiRNAfastq.getMapPrefix2GenomeSam();
		}
		if (predictMirna) {
			runPredict(mapPrefix2AlignFile, gffChrAbs, species);
			mapPrefix2Fastq = ctrlMiRNApredict.getMapPrefix2UnmapFq();
		} else {
			mapPrefix2Fastq = convertAlign2Fq(mapPrefix2AlignFile);
		}
		if (lsSpeciesBlastTo.size() > 0) {
			blastToOtherSpecies();
		}
	}
	
	/** 将sam文件转化为fastq文件 */
	private Map<String, String> convertAlign2Fq(Map<String, AlignSeq> mapAlign2Prefix) {
		Map<String, String> mapPrefix2unmapFastq = new HashMap<>();
		for (String prefix : mapAlign2Prefix.keySet()) {
			AlignSeq alignSeq = mapAlign2Prefix.get(prefix);
			mapPrefix2unmapFastq.put(prefix, alignSeq.getFastQ().getReadFileName());
		}
		return mapPrefix2unmapFastq;
	}
	
	private void runMapping(GffChrAbs gffChrAbs, Species species, Map<String, String> mapPrefix2Fastq) {
		ctrlMiRNAfastq.setMappingAll2Genome(false);
		ctrlMiRNAfastq.setRfamSpeciesSpecific(false);
		ctrlMiRNAfastq.setSpecies(species);
		ctrlMiRNAfastq.setOutPath(outPath);
		ctrlMiRNAfastq.setGffChrAbs(gffChrAbs);
		ctrlMiRNAfastq.setMapPrefix2Fastq(mapPrefix2Fastq);
		ctrlMiRNAfastq.setMiRNAinfo(PathDetailNBC.getMiRNADat());
		ctrlMiRNAfastq.setRfamFile(PathDetailNBC.getRfamTab());
		ctrlMiRNAfastq.setMapAll2Rfam(true);
		ctrlMiRNAfastq.mappingAndCounting();
		ctrlMiRNAfastq.writeToFile();
		logger.info("finish mapping");
	}
	/** 从头预测 */
	private void runPredict(Map<String, AlignSeq> mapBedFile2Prefix, GffChrAbs gffChrAbs, Species species) {
		ctrlMiRNApredict.setGffChrAbs(gffChrAbs);
		ctrlMiRNApredict.setSpecies(species);
		ctrlMiRNApredict.setMapPrefix2GenomeSamFile(mapBedFile2Prefix);
		ctrlMiRNApredict.setOutPath(outPath);
		ctrlMiRNApredict.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		ctrlMiRNApredict.setExpMir(expMirPre, expMirMature);
		ctrlMiRNApredict.predictAndCalculate();
		ctrlMiRNApredict.writeToFile();
		logger.info("finish predict");
	}
	/** 比对到别的物种 */
	private void blastToOtherSpecies() {
		MirSpeciesPipline mirSpeciesPipline = new MirSpeciesPipline();
		mirSpeciesPipline.setMapPrefix2Fastq(mapPrefix2Fastq);
		mirSpeciesPipline.setExpMir(expMirPre, expMirMature);
		mirSpeciesPipline.setLsSpecies(lsSpeciesBlastTo); 
		mirSpeciesPipline.setOutPathTmp(outPath);
		mirSpeciesPipline.setThreadNum(4);
		mirSpeciesPipline.mappingPipeline(PathDetailNBC.getMiRNADat());
		mirSpeciesPipline.writeToFile(outPath);
	}

}
