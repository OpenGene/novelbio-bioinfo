package com.novelbio.analysis.seq.fastq;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import com.novelbio.analysis.seq.fastq.FQrecordCopeInt;
import com.novelbio.base.dataOperate.TxtReadandWrite;

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
		
		BufferedImage bufferedImage = perBaseQualityScores.getBufferedImage(1000, 1000);
		File file = new File(outPathPrefix + "QualityScore.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		
		bufferedImage = perSequenceQualityScores.getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "SequenceQuality.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }

		bufferedImage = perBaseSequenceContent.getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "BaseSequence.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }

		bufferedImage = perBaseGCContent.getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "BaseGCContent.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }

		bufferedImage = perSequenceGCContent.getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "SequenceGCContent.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }

		bufferedImage = nContent.getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "nContent.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		
		bufferedImage = sequenceLengthDistribution.getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "LengthDistribution.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		
		bufferedImage = os.duplicationLevelModule().getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "DuplicationLevel.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		
		bufferedImage = kmerContent.getBufferedImage(1000, 1000);
		file = new File(outPathPrefix + "KmerContent.png");
		try { ImageIO.write(bufferedImage, "png", file); } catch (IOException e) { e.printStackTrace(); }
		
		mapTable = kmerContent.getResult();
		txtWrite = new TxtReadandWrite(outPathPrefix + "KmerContent.xls", true);
		writeTable(txtWrite, mapTable);
		txtWrite.close();
		
		mapTable = os.getResult();
		if (mapTable.size() > 0) {
			txtWrite = new TxtReadandWrite(outPathPrefix + "OverRepresentedSeqs.xls", true);
			writeTable(txtWrite, mapTable);
			txtWrite.close();
		}

	}
	
	private void writeTable(TxtReadandWrite txtWrite, Map<String, String> mapTable) {
		for (String column : mapTable.keySet()) {
			txtWrite.writefileln(column + "\t" + mapTable.get(column));
		}
	}
}
