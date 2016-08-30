package com.novelbio.analysis.seq.trfrna;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.GeneExpTable.EnumAddAnnoType;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.TitleFormatNBC;

/** 计算trf表达量的模块，仅计算一个prefix的表达量，同时包含合并的代码 */
public class CtrlTrfCountPerPrefix {
	private static final Logger logger = LoggerFactory.getLogger(CtrlTrfCountPerPrefix.class);
	
	GeneExpTable expTrfPre = new GeneExpTable(TitleFormatNBC.trfRNApreName);
	GeneExpTable expTrfMature = new GeneExpTable(TitleFormatNBC.trfRNAName);
	
	String outpath;
	String prefix;
	
	List<String> lsBamFiles;
	TrfRNACount trfRNACount = new TrfRNACount();
	
	/** 如果一条reads比对到多个位置，是
	 * 1. 每个位置都加上 1/n 
	 * 2. 把第一个位置加上1
	 * 默认是 1 
	 */
	boolean isReadMultiMappedReadsOnce = false;

	int lenMax;
	int lenMin;
	boolean isUniqueMapping;
	
	String speciesName;
	String trfFile;
	
	public void setLenMax(int lenMax) {
		this.lenMax = lenMax;
	}
	public void setLenMin(int lenMin) {
		this.lenMin = lenMin;
	}
	public void setUniqueMapping(boolean isUniqueMapping) {
		this.isUniqueMapping = isUniqueMapping;
	}
	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}
	public void setTrfFile(String trfFile) {
		this.trfFile = trfFile;
	}
	
	/** 如果一条reads比对到多个位置，是
	 * 1. 每个位置都加上 1/n 
	 * 2. 把第一个位置加上1
	 * 默认是 1 
	 */
	public void setReadMultiMappedReadsOnce(boolean isReadMultiMappedReadsOnce) {
		this.isReadMultiMappedReadsOnce = isReadMultiMappedReadsOnce;
	}
	/** 输出文件夹 */
	public void setOutpath(String outpath) {
		this.outpath = FileOperate.addSep(outpath);
	}
	/** 本次要跑的前缀 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void setLsBamFiles(List<String> lsBamFiles) {
		this.lsBamFiles = lsBamFiles;
	}
	
	/** 比对和计数，每比对一次就计数。主要是为了防止出错 */
	public void mappingAndCounting() {
		FileOperate.createFolders(FileOperate.getPathName(outpath));
		trfRNACount.setReadMultiMappedReadsOnce(isReadMultiMappedReadsOnce);
		expTrfPre.setCurrentCondition(prefix);
		expTrfMature.setCurrentCondition(prefix);
		trfRNACount.setSpecies(speciesName, trfFile);
		trfRNACount.setExpTable(expTrfPre, expTrfMature);

		AlignSamReading alignSamReading = new AlignSamReading();
		if (lenMin > 0) alignSamReading.setLenMin(lenMin);
		if (lenMax > 0) alignSamReading.setLenMax(lenMax);
		alignSamReading.setUniqueMapping(isUniqueMapping);
		
		for (String bamFile : lsBamFiles) {
			alignSamReading.addSeq(new SamFile(bamFile));
		}
		alignSamReading.addAlignmentRecorder(trfRNACount);
		alignSamReading.run();
	}
	
	public void writeToTxt() {
		expTrfMature.writeFile(false, outpath + prefix + ".Trf_Mature." + EnumExpression.Counts.toString().toLowerCase() + ".exp.txt", EnumExpression.Counts);
		expTrfPre.writeFile(false, outpath + prefix + ".Trf_Pre." + EnumExpression.Counts.toString().toLowerCase() + ".exp.txt", EnumExpression.Counts);
	}
	
	public static void combine(List<String> lsInExps, String output) {
		GeneExpTable geneExpTable = new GeneExpTable();
		geneExpTable.read(lsInExps.get(0), EnumAddAnnoType.addAll);
		for (int i = 1; i < lsInExps.size(); i++) {
			geneExpTable.read(lsInExps.get(i), EnumAddAnnoType.notAdd);
		}
		geneExpTable.writeFile(true, output, EnumExpression.Counts);
	}
}
