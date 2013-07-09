package com.novelbio.analysis.seq.fastq;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import uk.ac.babraham.FastQC.Modules.BasicStats;
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
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.GraphicCope;
import com.novelbio.generalConf.Param;

public class FastQC {
	private List<FQrecordCopeInt> lsModules = new ArrayList<FQrecordCopeInt>();
	/** 如果不qc，则只计数，也就是只加入BasicStats过滤器 */
	boolean qc;
	BasicStats basicStats = new BasicStats();
	PerBaseQualityScores perBaseQualityScores = new PerBaseQualityScores();
	PerSequenceQualityScores perSequenceQualityScores = new PerSequenceQualityScores();
	PerBaseSequenceContent perBaseSequenceContent = new PerBaseSequenceContent();
	PerBaseGCContent perBaseGCContent = new PerBaseGCContent();
	PerSequenceGCContent perSequenceGCContent = new PerSequenceGCContent();
	NContent nContent = new NContent();
	SequenceLengthDistribution sequenceLengthDistribution = new SequenceLengthDistribution();
	OverRepresentedSeqs os = new OverRepresentedSeqs();
	KmerContent kmerContent = new KmerContent();
	
	/**
	 * @param fileName
	 * @param qc true 全面质检 false 仅计算reads数量
	 */
	public FastQC(String fileName, boolean qc) {
		this.qc = qc;
		basicStats.setName(fileName);
		lsModules.add(basicStats);
		if (qc) {
			lsModules.add(perBaseQualityScores);
			lsModules.add(perSequenceQualityScores);
			lsModules.add(perBaseSequenceContent);
			lsModules.add(perBaseGCContent);
			lsModules.add(perSequenceGCContent);
			lsModules.add(nContent);
			lsModules.add(sequenceLengthDistribution);
			lsModules.add(os.duplicationLevelModule());
			lsModules.add(os);
			lsModules.add(kmerContent);
		}
	}
	
	public boolean isQC() {
		return qc;
	}
	
	public void setFileName(String fileName) {
		basicStats.setName(fileName);
	}
	
	/**
	 * 取得fastQC模块集合<br>
	 * 每个模块的getBufferedImage()方法可以获得BufferedImage用来保存为所需的图表
	 * @return
	 */
	public List<FQrecordCopeInt> getLsModules() {
		return lsModules;
	}
	
	public void saveToPath(String outPathPrefix) {
		Map<String, String> mapTable = basicStats.getResult();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPathPrefix + "basicStats.xls", true);
		writeTable(txtWrite, mapTable);
		txtWrite.close();
		if (!qc) {
			return;
		}
		BufferedImage bufferedImage = null;
		File file = null;
		try {
			bufferedImage = perBaseQualityScores.getBufferedImage(1000, 1000);
			file = new File(outPathPrefix + "QualityScore.png");
			ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		try {
			bufferedImage = perSequenceQualityScores.getBufferedImage(1000, 1000);
			file = new File(outPathPrefix + "SequenceQuality.png");
			ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		try {
			bufferedImage = perBaseSequenceContent.getBufferedImage(1000, 1000);
			file = new File(outPathPrefix + "BaseSequence.png");
			ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		try {
			bufferedImage = perBaseGCContent.getBufferedImage(1000, 1000);
			file = new File(outPathPrefix + "BaseGCContent.png");
			ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		try {
			bufferedImage = perSequenceGCContent.getBufferedImage(1000, 1000);
			file = new File(outPathPrefix + "SequenceGCContent.png");
			ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		try {
			bufferedImage = nContent.getBufferedImage(1000, 1000);
			file = new File(outPathPrefix + "nContent.png");
			ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		
		bufferedImage = sequenceLengthDistribution.getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "LengthDistribution.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		try {
			bufferedImage = os.duplicationLevelModule().getBufferedImage(1000, 1000);
			file = new File(outPathPrefix + "DuplicationLevel.png");
			ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		try {
			bufferedImage = kmerContent.getBufferedImage(1000, 1000);
			file = new File(outPathPrefix + "KmerContent.png");
			ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		
		saveTable(outPathPrefix, this, null);
	}

	/**
	 * 把两个FastQC的结果合并起来
	 * @param sepPic 两张图片合并之后的间距，取30比较合适
	 * @param fastQCPairend
	 * @param outPathPrefix
	 */
	public void saveToPath(int sepPic, FastQC fastQCPairend, String outPathPrefix) {
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
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPathPrefix + "basicStats.xls", true);
		txtWrite.ExcelWrite(lsInfo);
		txtWrite.close();
		if (!qc) {
			return;
		}
		BufferedImage bufferedImage = null;
		BufferedImage bufferedImagePair = null;
		try {
			bufferedImage = perBaseQualityScores.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perBaseQualityScores.getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "QualityScore.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }

		try {
			bufferedImage = perSequenceQualityScores.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perSequenceQualityScores.getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "SequenceQuality.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = perBaseSequenceContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perBaseSequenceContent.getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "BaseSequence.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = perBaseGCContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perBaseGCContent.getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "BaseGCContent.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = perSequenceGCContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.perSequenceGCContent.getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "SequenceGCContent.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = nContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.nContent.getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "nContent.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = sequenceLengthDistribution.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.sequenceLengthDistribution.getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "LengthDistribution.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = os.duplicationLevelModule().getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.os.duplicationLevelModule().getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "DuplicationLevel.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }
		try {
			bufferedImage = kmerContent.getBufferedImage(1000, 1000);
			bufferedImagePair = fastQCPairend.kmerContent.getBufferedImage(1000, 1000);
			savePic(outPathPrefix + "KmerContent.png", sepPic, bufferedImage, bufferedImagePair);
		} catch (Exception e) {e.printStackTrace(); }
	
		saveTable(outPathPrefix, this, "1");
		saveTable(outPathPrefix, fastQCPairend, "2");
	}
	
	private static void writeTable(TxtReadandWrite txtWrite, Map<String, String> mapTable) {
		for (String column : mapTable.keySet()) {
			txtWrite.writefileln(column + "\t" + mapTable.get(column));
		}
	}
	
	private void savePic(String outPathPrefix, int sepPic, BufferedImage bufferedImageFirst, BufferedImage bufferedImageSecond) {
		BufferedImage bufferedImageCombine = GraphicCope.combineBfImage(true, sepPic, bufferedImageFirst, bufferedImageSecond);
		File file = new File(outPathPrefix);
		try { ImageIO.write(bufferedImageCombine, "png", file); } catch (IOException e) { e.printStackTrace(); }
	}
	
	/**
	 * @param outPathPrefix
	 * @param fastQC
	 * @param sub 后缀
	 */
	private static void saveTable(String outPathPrefix, FastQC fastQC, String sub) {
		Map<String, String> mapTable = fastQC.kmerContent.getResult();
		String fileKmer = outPathPrefix + "KmerContent.xls";
		String fileRepresentedSeq = outPathPrefix + "OverRepresentedSeqs.xls";
		if (sub != null && !sub.equals("")) {
			fileKmer = FileOperate.changeFileSuffix(fileKmer, "_" + sub, null);
			fileRepresentedSeq = FileOperate.changeFileSuffix(fileRepresentedSeq, "_" + sub, null);
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileKmer, true);
		writeTable(txtWrite, mapTable);
		txtWrite.close();
		
		mapTable = fastQC.os.getResult();
		if (mapTable.size() > 0) {
			txtWrite = new TxtReadandWrite(fileRepresentedSeq, true);
			writeTable(txtWrite, mapTable);
			txtWrite.close();
		}
	}

	/** 合并Fastqc的BaseStatistics信息 
	 * @return */
	public static List<String[]> combineFastQCbaseStatistics(Map<String, FastQC[]> mapParam2FastqcLR) {
		Map<String, Map<String, String>> mapPrefix2Table = new LinkedHashMap<String, Map<String,String>>();
		boolean isPairend = false;
		for (String prefix : mapParam2FastqcLR.keySet()) {
			FastQC[] fastQCs = mapParam2FastqcLR.get(prefix);
			Map<String, String> mapTable = fastQCs[0].basicStats.getResult();
			Map<String, String> mapTablePair = null;
			if (fastQCs.length > 1 && fastQCs[1] != null) {
				isPairend = true;
				mapTablePair = fastQCs[1].basicStats.getResult();
			} else {
				isPairend = false;
			}
			
			if (isPairend) {
				mapPrefix2Table.put(prefix + "1", mapTable);
				mapPrefix2Table.put(prefix + "2", mapTablePair);
			} else {
				mapPrefix2Table.put(prefix, mapTable);
			}
		}
		List<String[]> lsInfo = addBaseTotalTableList(mapPrefix2Table);
		return lsInfo;
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
