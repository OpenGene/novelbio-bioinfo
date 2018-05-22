package com.novelbio.analysis.seq.fastq;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.babraham.FastQC.Modules.BasicStats;
import uk.ac.babraham.FastQC.Modules.FastQCmodules;
import uk.ac.babraham.FastQC.Modules.KmerContent;
import uk.ac.babraham.FastQC.Modules.NContent;
import uk.ac.babraham.FastQC.Modules.OverRepresentedSeqs;
import uk.ac.babraham.FastQC.Modules.PerBaseGCContent;
import uk.ac.babraham.FastQC.Modules.PerBaseQualityScores;
import uk.ac.babraham.FastQC.Modules.PerBaseSequenceContent;
import uk.ac.babraham.FastQC.Modules.PerSequenceGCContent;
import uk.ac.babraham.FastQC.Modules.PerSequenceQualityScores;
import uk.ac.babraham.FastQC.Modules.SequenceLengthDistribution;

import com.google.common.collect.HashMultimap;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.generalconf.Param;

public class FastQC {
	private List<FastQCmodules> lsModules = new ArrayList<FastQCmodules>();
	/** 如果不qc，则只计数，也就是只加入BasicStats过滤器 */
	boolean qc;
	FastQCmodules basicStats = new BasicStats();
	FastQCmodules perBaseQualityScores = new PerBaseQualityScores();
	FastQCmodules perSequenceQualityScores = new PerSequenceQualityScores();
	FastQCmodules perBaseSequenceContent = new PerBaseSequenceContent();
	FastQCmodules perBaseGCContent = new PerBaseGCContent();
	FastQCmodules perSequenceGCContent = new PerSequenceGCContent();
	FastQCmodules nContent = new NContent();
	FastQCmodules sequenceLengthDistribution = new SequenceLengthDistribution();
	FastQCmodules os = new OverRepresentedSeqs();
	FastQCmodules kmerContent = new KmerContent();
	FastQCmodules duplicate;
	/**
	 * @param fileName
	 * @param qc true 全面质检 false 仅计算reads数量
	 */
	public FastQC(String fileName, boolean qc) {
		this.qc = qc;
		((BasicStats)basicStats).setName(fileName);
		lsModules.add(basicStats);
		if (qc) {
			duplicate = ((OverRepresentedSeqs)os).duplicationLevelModule();
			lsModules.add(perBaseQualityScores);
			lsModules.add(perSequenceQualityScores);
			lsModules.add(perBaseSequenceContent);
			lsModules.add(perBaseGCContent);
			lsModules.add(perSequenceGCContent);
			lsModules.add(nContent);
			lsModules.add(sequenceLengthDistribution);
			lsModules.add(duplicate);
			lsModules.add(os);
			lsModules.add(kmerContent);
		}
	}
	public BasicStats getBasicStats() {
		return (BasicStats)basicStats;
	}
	
	public boolean isQC() {
		return qc;
	}
	
	public void setFileName(String fileName) {
		((BasicStats)basicStats).setName(fileName);
	}
	
	/**
	 * 取得fastQC模块集合<br>
	 * 每个模块的getBufferedImage()方法可以获得BufferedImage用来保存为所需的图表
	 * @return
	 */
	public List<FastQCmodules> getLsModules() {
		return lsModules;
	}
	
	public static String getQualityScoreFileName(String outPathPrefix) {
		FastQCmodules perBaseQualityScores = new PerBaseQualityScores();
		return perBaseQualityScores.getSavePath(outPathPrefix);
	}
	
	public List<String> saveToPathPic(String outPathPrefix) {
		List<String> lsSaveName = new ArrayList<>();
		if (!qc) return lsSaveName;
		
		BufferedImage bufferedImage = null;
		String outName = "";
		try {
			bufferedImage = perBaseQualityScores.getBufferedImage(1000, 1000);
			outName = perBaseQualityScores.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName);
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		try {
			bufferedImage = perSequenceQualityScores.getBufferedImage(1000, 1000);
			outName = perSequenceQualityScores.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName); 
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		try {
			bufferedImage = perBaseSequenceContent.getBufferedImage(1000, 1000);
			outName = perBaseSequenceContent.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName);
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		try {
			bufferedImage = perBaseGCContent.getBufferedImage(1000, 1000);
			outName = perBaseGCContent.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName); 
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		try {
			bufferedImage = perSequenceGCContent.getBufferedImage(1000, 1000);
			outName = perSequenceGCContent.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName); 
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		try {
			bufferedImage = nContent.getBufferedImage(1000, 1000);
			outName = nContent.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName); 
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		try {
			bufferedImage = sequenceLengthDistribution.getBufferedImage(1000, 1000);
			outName = sequenceLengthDistribution.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName);
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		try {
			bufferedImage = duplicate.getBufferedImage(1000, 1000);
			outName = duplicate.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName);
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		try {
			bufferedImage = kmerContent.getBufferedImage(1000, 1000);
			outName = kmerContent.getSavePath(outPathPrefix);
			ImageUtils.saveBufferedImage(bufferedImage, outName);
			lsSaveName.add(outName);} catch (Exception e) { e.printStackTrace(); }
		
		return lsSaveName;
	}
	
	public List<String> saveToPathTable(String outPathPrefix) {
		List<String> lsOutFile = new ArrayList<>();
		Map<String, String> mapTable = basicStats.getResult();
		Map<String, Map<String, String>> mapPrefix2Table = new LinkedHashMap<String, Map<String,String>>();
		String sampleName = "Sample_";
		if (!outPathPrefix.endsWith("\\") && !outPathPrefix.endsWith("/")) {
			sampleName = FileOperate.getFileName(outPathPrefix) + "_";
		}
		mapPrefix2Table.put(sampleName, mapTable);
		List<String[]> lsInfo = addBaseTotalTableList(mapPrefix2Table);
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(basicStats.getSavePath(outPathPrefix), true);
		txtWrite.ExcelWrite(lsInfo);
		txtWrite.close();
		lsOutFile.add(basicStats.getSavePath(outPathPrefix));
		
		if (!qc) return lsOutFile;
	
		lsOutFile.addAll(saveTable(outPathPrefix, this, null));
		return lsOutFile;
	}
	
	
	/**
	 * 把两个FastQC的结果合并起来
	 * @param sepPic 两张图片合并之后的间距，取30比较合适
	 * @param fastQCPairend
	 * @param outPathPrefix
	 * @return 返回保存成功的文件列表 
	 */
	public List<String> saveToPathPic(int sepPic, FastQC fastQCPairend, String outPathPrefix) {
		List<String> lsOutFileName = new ArrayList<>();
		if (!qc) return new ArrayList<>();
		
		BufferedImage bufferedImage = null;
		BufferedImage bufferedImagePair = null;
		String outName = null;
		try {
			bufferedImage = perBaseQualityScores.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perBaseQualityScores.getBufferedImage(1000, 1000);
			outName = perBaseQualityScores.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }

		try {
			bufferedImage = perSequenceQualityScores.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perSequenceQualityScores.getBufferedImage(1000, 1000);
			outName = perSequenceQualityScores.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = perBaseSequenceContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perBaseSequenceContent.getBufferedImage(1000, 1000);
			outName = perBaseSequenceContent.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = perBaseGCContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perBaseGCContent.getBufferedImage(1000, 1000);
			outName = perBaseGCContent.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = perSequenceGCContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perSequenceGCContent.getBufferedImage(1000, 1000);
			outName = perSequenceGCContent.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = nContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.nContent.getBufferedImage(1000, 1000);
			outName = nContent.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = sequenceLengthDistribution.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.sequenceLengthDistribution.getBufferedImage(1000, 1000);
			outName = sequenceLengthDistribution.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = duplicate.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.duplicate.getBufferedImage(1000, 1000);
			outName = duplicate.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = kmerContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.kmerContent.getBufferedImage(1000, 1000);
			outName = kmerContent.getSavePath(outPathPrefix);
			savePic(outName, sepPic, bufferedImage, bufferedImagePair);
			lsOutFileName.add(outName);
		} catch (Exception e) {e.printStackTrace(); }
		return lsOutFileName;
	}
	
	/**
	 * 把两个FastQC的结果合并起来
	 * @param fastQCPairend
	 * @param outPathPrefix
	 */
	public List<String> saveToPathTable(FastQC fastQCPairend, String outPathPrefix) {
		List<String> lsOutFile = new ArrayList<>();
		Map<String, String> mapTable = basicStats.getResult();
		Map<String, String> mapTablePair = fastQCPairend.basicStats.getResult();
		Map<String, Map<String, String>> mapPrefix2Table = new LinkedHashMap<String, Map<String,String>>();
		String sampleName = "Sample_";
		if (!outPathPrefix.endsWith("\\") && !outPathPrefix.endsWith("/")) {
			sampleName = FileOperate.getFileName(outPathPrefix) + "_";
		}
		mapPrefix2Table.put(sampleName + "1", mapTable);
		mapPrefix2Table.put(sampleName + "2", mapTablePair);
		List<String[]> lsInfo = addBaseTotalTableList(mapPrefix2Table);
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(basicStats.getSavePath(outPathPrefix), true);
		txtWrite.ExcelWrite(lsInfo);
		txtWrite.close();
		lsOutFile.add(basicStats.getSavePath(outPathPrefix));
		
		if (!qc) return lsOutFile;
	
		lsOutFile.addAll(saveTable(outPathPrefix, this, "1"));
		lsOutFile.addAll(saveTable(outPathPrefix, fastQCPairend, "2"));
		return lsOutFile;
	}
	
	private static void writeTable(TxtReadandWrite txtWrite, Map<String, String> mapTable) {
		for (String column : mapTable.keySet()) {
			txtWrite.writefileln(column + "\t" + mapTable.get(column));
		}
	}
	
	private void savePic(String outPathPrefix, int sepPic, BufferedImage bufferedImageFirst, BufferedImage bufferedImageSecond) {
		BufferedImage bufferedImageCombine = ImageUtils.combineBfImage(true, sepPic, bufferedImageFirst, bufferedImageSecond);
		ImageUtils.saveBufferedImage(bufferedImageCombine, outPathPrefix);
	}
	
	/**
	 * @param outPathPrefix
	 * @param fastQC
	 * @param sub 后缀
	 */
	private static List<String> saveTable(String outPathPrefix, FastQC fastQC, String sub) {
		List<String> lsSaveFileName = new ArrayList<>();
		Map<String, String> mapTable = fastQC.kmerContent.getResult();
		Map<String, String> mapLen2Num = fastQC.sequenceLengthDistribution.getResult();
		String fileKmer = fastQC.kmerContent.getSavePath(outPathPrefix);
		String fileLenDistribution = FileOperate.changeFilePrefix(fastQC.sequenceLengthDistribution.getSavePath(outPathPrefix), "", "xls");
		String fileRepresentedSeq = outPathPrefix + "OverRepresentedSeqs.xls";
		if (!StringOperate.isRealNull(sub)) {
			fileKmer = FileOperate.changeFileSuffix(fileKmer, "_" + sub, null);
			fileLenDistribution = FileOperate.changeFileSuffix(fileLenDistribution, "_" + sub, null);
			fileRepresentedSeq = FileOperate.changeFileSuffix(fileRepresentedSeq, "_" + sub, null);
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileKmer, true);
		writeTable(txtWrite, mapTable);
		txtWrite.close();
		lsSaveFileName.add(fileKmer);
		try {
			mapTable = fastQC.os.getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mapTable.size() > 0) {
			txtWrite = new TxtReadandWrite(fileRepresentedSeq, true);
			writeTable(txtWrite, mapTable);
			txtWrite.close();
			lsSaveFileName.add(fileRepresentedSeq);
		}
		
		txtWrite = new TxtReadandWrite(fileLenDistribution, true);
		writeTable(txtWrite, mapLen2Num);
		txtWrite.close();
		lsSaveFileName.add(fileLenDistribution);
		
		return lsSaveFileName;
	}
	
	/** 合并多个basicStatistics数据 */
	private static List<String[]> addBaseTotalTableList(Map<String, Map<String, String>> mapSampleName_2_prefix2Value) {
		List<String[]> lsResult = new ArrayList<String[]>();
		
		List<String> lsColumns = new ArrayList<String>();
		lsColumns.add( "SampleName");
		lsColumns.addAll(mapSampleName_2_prefix2Value.values().iterator().next().keySet());
		lsResult.add(lsColumns.toArray(new String[0]));
		for (String prefix : mapSampleName_2_prefix2Value.keySet()) {
			lsColumns.clear();
			lsColumns.add(prefix);
			Map<String, String> mapPrefix2Value = mapSampleName_2_prefix2Value.get(prefix);
			lsColumns.addAll(mapPrefix2Value.values());
			lsResult.add(lsColumns.toArray(new String[0]));
		}
		return lsResult;
	}
	
	/** 获得待写入文本的param参数 */
	public HashMultimap<String, String> getMapParam(String outPathPrefix) {
		HashMultimap<String, String> mapPublicParams = HashMultimap.create();
		String fileName = FileOperate.getFileName(outPathPrefix + "QualityScore.png");
		mapPublicParams.put(Param.picParam.toString(), fileName);
		fileName = FileOperate.getFileName(outPathPrefix + "SequenceGCContent.png");
		mapPublicParams.put(Param.picParam.toString(), fileName);
		return mapPublicParams;
	}
	
	
}
