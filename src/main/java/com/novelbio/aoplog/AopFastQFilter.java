package com.novelbio.aoplog;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import uk.ac.babraham.FastQC.Modules.BasicStats;
import uk.ac.babraham.FastQC.Modules.DuplicationLevel;
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
import com.novelbio.analysis.seq.fastq.FastQC;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.nbcgui.controlseq.CtrlFastQfilter;

/**
 * 给FastQ添加report相关的参数说明
 */
public class AopFastQFilter {
	private static Logger logger = Logger.getLogger(AopFastQFilter.class);
	private static int smallPicSize = 1000;
	private static int bigPicSize = 1000;
	/**
	 * 用来拦截CtrlFastQ的running方法
	 * @param CtrlFastQ
	 */
	@After("execution (* com.novelbio.nbcgui.controlseq.CtrlFastQfilter.filteredAndCombineReads(..)) && target(ctrlFastQfilter)")
	public void fastQPoint(CtrlFastQfilter ctrlFastQfilter) {
		ReportBuilder fastQBuilder = new FastQBuilder(ctrlFastQfilter);
		fastQBuilder.writeInfo();
	}

	/**
	 * fastQ报告参数生成器
	 * @author novelbio
	 */
	private class FastQBuilder extends ReportBuilder {
		/** 结果的存放路径 */
		private String savePath;
		/** 拦截的对象 */
		private CtrlFastQfilter ctrlFastQfilter;
		/** 图片流集合 */
		private Map<String, BufferedImage> mapPath2Image = new LinkedHashMap<String, BufferedImage>();

		public FastQBuilder(CtrlFastQfilter ctrlFastQfilter) {
			this.ctrlFastQfilter = ctrlFastQfilter;
			String outFilePrefix = ctrlFastQfilter.getOutFilePrefix();
			this.savePath = outFilePrefix.endsWith(FileOperate.getSepPath()) ? outFilePrefix : FileOperate.getParentPathName(outFilePrefix);
			setParamPath(savePath);
		}
		
		@Override
		protected boolean buildExcels() {
			try {
				FastQC[] fastQCsBefore = ctrlFastQfilter.getFastQCbefore();
				readFastQC(fastQCsBefore, ctrlFastQfilter.getPrefix(), true);
				
				FastQC[] fastQCsAfter = ctrlFastQfilter.getFastQCbefore();
				readFastQC(fastQCsAfter, ctrlFastQfilter.getPrefix(), true);
			} catch (Exception e) {
				logger.error("aopFastQ生成excel出错！");
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		/**
		 * 读取fastQC并生成excel表格，并遍历图片
		 * @param fastQCs
		 * @param key
		 * @param isBefore
		 * @param isQc
		 * @throws Exception
		 */
		private void readFastQC(FastQC[] fastQCs, String prefix, boolean isBefore) throws Exception{
			int sepPic = 20;//两张图合并起来后，中间的空隙
			BufferedImage[] qualityScoreImages = new BufferedImage[2];
			BufferedImage[] sequenceGCContentImages = new BufferedImage[2];
			for (int i = 0; i < fastQCs.length; i++) {
				String key = prefix;
				String reportKey = prefix;
				if (fastQCs.length == 1 || fastQCs[1] == null) {
					key += isBefore?"_BeforeFilter":"_AfterFilter";
				} else {
					key += isBefore?"_BeforeFilter_"+(i+1):"_AfterFilter_"+(i+1);
				}
				reportKey += isBefore?"_BeforeFilter":"_AfterFilter";
				FastQC fastQC = fastQCs[i];
				if (fastQC == null) continue;
				
				for (FQrecordCopeInt fQrecordCopeInt : fastQC.getLsModules()) {
					if (fQrecordCopeInt instanceof BasicStats) {
						Map<String, String> mapTable = ((BasicStats)fQrecordCopeInt).getResult();
						TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + "BasicStats_" + key + ".xls", true);
						writeTable(txtWrite, mapTable);
						txtWrite.close();
						if (!fastQC.isQC()) {
							break;
						}
					} else {
						if (!fastQC.isQC()) continue;
						if (fQrecordCopeInt instanceof KmerContent) {
							try {
								Map<String, String> mapTable = ((KmerContent)fQrecordCopeInt).getResult();
								TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + "KmerContent" + key +".xls", true);
								writeTable(txtWrite, mapTable);
								txtWrite.close();
								mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "KmerContent_" + key +".png", ((KmerContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						else if (fQrecordCopeInt instanceof OverRepresentedSeqs) {
							try {
								Map<String, String> mapTable = ((OverRepresentedSeqs)fQrecordCopeInt).getResult();
								if (mapTable.size() > 0) {
									TxtReadandWrite txtWrite = new TxtReadandWrite(savePath + "OverRepresentedSeqs" + key +".xls", true);
									writeTable(txtWrite, mapTable);
									txtWrite.close();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						else if (fQrecordCopeInt instanceof PerBaseQualityScores) {
							try {
								mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "QualityScore_" + key +".png",((PerBaseQualityScores)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
								addParamInfo(Param.picParam, "QualityScore_" + reportKey +".png");
								if (fastQCs.length > 1) {
									qualityScoreImages[i] = ((PerBaseQualityScores)fQrecordCopeInt).getBufferedImage(smallPicSize, smallPicSize);
									if ((i+1) == fastQCs.length) {
										mapPath2Image.put(savePath + "QualityScore_" + reportKey +".png",ImageUtils.combineBfImage(true, sepPic, qualityScoreImages));
									}
								}else {
									mapPath2Image.put(savePath + "QualityScore_" + reportKey +".png",((PerBaseQualityScores)fQrecordCopeInt).getBufferedImage(smallPicSize, smallPicSize));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						else if (fQrecordCopeInt instanceof PerSequenceQualityScores) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "SequenceQuality_" + key +".png",((PerSequenceQualityScores)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof PerBaseSequenceContent) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "BaseSequence_" + key +".png",((PerBaseSequenceContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof PerBaseGCContent) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "BaseGCContent_" + key +".png",((PerBaseGCContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof DuplicationLevel) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "DuplicationLevel_" + key +".png",((DuplicationLevel)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof PerSequenceGCContent) {
							try {
								mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "SequenceGCContent_" + key +".png",((PerSequenceGCContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
								addParamInfo(Param.picParam, "SequenceGCContent_" + reportKey +".png");
								if (fastQCs.length > 1) {
									sequenceGCContentImages[i] = ((PerSequenceGCContent)fQrecordCopeInt).getBufferedImage(smallPicSize, smallPicSize);
									if ((i+1) == fastQCs.length) {
										mapPath2Image.put(savePath + "SequenceGCContent_" + reportKey +".png",ImageUtils.combineBfImage(true, sepPic, sequenceGCContentImages));
									}
								}else {
									mapPath2Image.put(savePath + "SequenceGCContent_" + reportKey +".png",((PerSequenceGCContent)fQrecordCopeInt).getBufferedImage(smallPicSize, smallPicSize));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						else if (fQrecordCopeInt instanceof NContent) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "nContent_" + key +".png",((NContent)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
						else if (fQrecordCopeInt instanceof SequenceLengthDistribution) {
							mapPath2Image.put(savePath + "QCImages" + FileOperate.getSepPath() + "LengthDistribution_" + key +".png",((SequenceLengthDistribution)fQrecordCopeInt).getBufferedImage(bigPicSize, bigPicSize));
						}
					}
				}
			}
		}

		@Override
		public boolean buildImages() {
			try {
				for (String path : mapPath2Image.keySet()) {
					System.out.println(path);
					File file = new File(path);
					if (!FileOperate.isFileFoldExist(FileOperate.getParentPathName(path))) {
						FileOperate.createFolders(FileOperate.getParentPathName(path));
					}
					ImageIO.write(mapPath2Image.get(path), "png", file);
				}
				// TODO image的说明文件在这里写
				// String descFile = FileOperate.changeFileSuffix(picName,
				// "_pic", "txt");
			} catch (Exception e) {
				logger.error("aopFastQ生成图片出错！");
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected boolean fillDescFile() {
			return true;
		}
		
		private void writeTable(TxtReadandWrite txtWrite, Map<String, String> mapTable) {
			for (String column : mapTable.keySet()) {
				txtWrite.writefileln(column + "\t" + mapTable.get(column));
			}
		}
	}

}
